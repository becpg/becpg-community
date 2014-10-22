/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.project.data.ProjectData;

/**
 * Test for checkout checkin
 * @author quere
 *
 */
public class ProjectCOCITest extends AbstractProjectTestCase {

	private static Log logger = LogFactory.getLog(ProjectCOCITest.class);
	
	@Resource
	private CheckOutCheckInService checkOutCheckInService;
	
	@Resource
	private RuntimeRuleService ruleService;

	@Test
	public void testCheckOutCheckIn(){
		
	
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(projectTplNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

				// Check out
				logger.info("Check out project " + projectTplNodeRef + ruleService.getSavedRuleFolderAssoc(projectTplNodeRef));
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(projectTplNodeRef);
				
				ProjectData workingCopyData = (ProjectData)alfrescoRepository.findOne(workingCopyNodeRef);								
				assertTrue(workingCopyData.getDeliverableList().get(0).getTasks().get(0).equals(workingCopyData.getTaskList().get(0).getNodeRef()));
				assertTrue(workingCopyData.getDeliverableList().get(1).getTasks().get(0).equals(workingCopyData.getTaskList().get(1).getNodeRef()));
				assertTrue(workingCopyData.getDeliverableList().get(2).getTasks().get(0).equals(workingCopyData.getTaskList().get(1).getNodeRef()));
				assertTrue(workingCopyData.getDeliverableList().get(3).getTasks().get(0).equals(workingCopyData.getTaskList().get(2).getNodeRef()));
				
				// Check in
				logger.info("Check in project " + workingCopyNodeRef  + ruleService.getSavedRuleFolderAssoc(workingCopyNodeRef));
				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
				NodeRef newProjectNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
				
				logger.info("Check in project done " + newProjectNodeRef  + ruleService.getSavedRuleFolderAssoc(newProjectNodeRef));
				
				assertNotNull(newProjectNodeRef);
				
				return null;
			}
		}, false, true);
	}
	
}
