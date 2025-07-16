/*
Copyright (C) 2010-2021 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
*/
package fr.becpg.repo.project.extractor;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.impl.AbstractExprNameExtractor;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>ProjectAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProjectAttributeExtractorPlugin extends AbstractExprNameExtractor {
	

	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	private String projectNameFormat() {
		return systemConfigurationService.confValue("beCPG.project.name.format");
	}


	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Arrays.asList(ProjectModel.TYPE_TASK_LIST, ProjectModel.TYPE_DELIVERABLE_LIST, ProjectModel.TYPE_BUDGET_LIST,
				ProjectModel.TYPE_INVOICE_LIST, ProjectModel.TYPE_LOG_TIME_LIST,ProjectModel.TYPE_PROJECT);
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String result = "";
		if (ProjectModel.TYPE_DELIVERABLE_LIST.equals(type)) {
			result = (String) nodeService.getProperty(nodeRef, ProjectModel.PROP_DL_DESCRIPTION);
		} else if (ProjectModel.TYPE_BUDGET_LIST.equals(type)) {
			result = (String) nodeService.getProperty(nodeRef, ProjectModel.PROP_BL_ITEM);
		} else if (ProjectModel.TYPE_INVOICE_LIST.equals(type) || ProjectModel.TYPE_LOG_TIME_LIST.equals(type)) {
			result = type.toPrefixString();
		} else if (ProjectModel.TYPE_PROJECT.equals(type)) {
			result = extractExpr(nodeRef, projectNameFormat());
		} else {
			result = (String) nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_TASK_NAME);
		}
		return result != null ? result : "";
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		// TODO task state
		// TODO Handle also project state (search results)
		String[] parts = type.toPrefixString(namespaceService).split(":");
		return parts.length > 1 ? parts[1] : "";
	}

	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

}
