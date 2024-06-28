/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.project;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.project.data.ProjectData;

/**
 * Test for checkout checkin
 * 
 * @author quere
 *
 */
public class ProjectCOCIIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectCOCIIT.class);

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private RuntimeRuleService ruleService;

	@Test
	public void testCheckOutCheckIn() {

		inWriteTx(() -> {

			Map<QName, Serializable> aspectProperties = new HashMap<>();
			aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
			nodeService.addAspect(projectTplNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

			// Check out
			logger.info(
					"Check out project " + projectTplNodeRef + ruleService.getSavedRuleFolderAssoc(projectTplNodeRef));

			NodeRef destNodeRef = nodeService.getPrimaryParent(projectTplNodeRef).getParentRef();
			NodeRef workingCopyNodeRef = entityVersionService.createBranch(projectTplNodeRef, destNodeRef);

			ProjectData workingCopyData = (ProjectData) alfrescoRepository.findOne(workingCopyNodeRef);
			assertTrue(workingCopyData.getDeliverableList().get(0).getTasks().get(0)
					.equals(workingCopyData.getTaskList().get(0).getNodeRef()));
			assertTrue(workingCopyData.getDeliverableList().get(1).getTasks().get(0)
					.equals(workingCopyData.getTaskList().get(1).getNodeRef()));
			assertTrue(workingCopyData.getDeliverableList().get(2).getTasks().get(0)
					.equals(workingCopyData.getTaskList().get(1).getNodeRef()));
			assertTrue(workingCopyData.getDeliverableList().get(3).getTasks().get(0)
					.equals(workingCopyData.getTaskList().get(2).getNodeRef()));

			// Check in
			logger.info(
					"Check in project " + workingCopyNodeRef + ruleService.getSavedRuleFolderAssoc(workingCopyNodeRef));
			NodeRef newProjectNodeRef = entityVersionService.mergeBranch(workingCopyNodeRef, projectTplNodeRef,
					VersionType.MAJOR, "This is a test version");

			logger.info("Check in project done " + newProjectNodeRef
					+ ruleService.getSavedRuleFolderAssoc(newProjectNodeRef));

			assertNotNull(newProjectNodeRef);

			return null;
		});
	}

}
