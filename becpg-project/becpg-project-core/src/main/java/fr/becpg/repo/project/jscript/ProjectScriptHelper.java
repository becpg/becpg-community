/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.jscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.BudgetListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Utility script methods for budget
 * 
 * @author matthieu
 *
 */
public final class ProjectScriptHelper extends BaseScopableProcessorExtension {

	private AlfrescoRepository<ProjectData> alfrescoRepository;

	private EntityListDAO entityListDAO;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void calculateBudgetParentValues(ScriptNode listItemNode, String[] qNames) {

		NodeRef projectNodeRef = entityListDAO.getEntity(listItemNode.getNodeRef());
		ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);

		List<QName> props = new ArrayList<QName>();

		for (String qName : qNames) {
			props.add(QName.createQName(qName, namespaceService));
		}

		Composite<BudgetListDataItem> compositeBugdet = CompositeHelper.getHierarchicalCompoList(projectData.getBudgetList());
		calculateBudgetParentValues(compositeBugdet, props);
		
		alfrescoRepository.save(projectData);

	}

	private void calculateBudgetParentValues(Composite<BudgetListDataItem> parent, List<QName> props) {
		Map<QName, Double> values = new HashMap<>();

		if (!parent.isLeaf()) {
			for (Composite<BudgetListDataItem> component : parent.getChildren()) {
				calculateBudgetParentValues(component, props);

				for (QName prop : props) {

					Double value = values.get(prop);
					if (value == null) {
						value = 0d;
					}

					if (nodeService.getProperty(component.getData().getNodeRef(), prop) != null) {
						value += (Double) nodeService.getProperty(component.getData().getNodeRef(), prop);
					}

					values.put(prop, value);

				}
			}
			if (!parent.isRoot()) {

				for (QName prop : props) {
					Double value = values.get(prop);
					if (value == null) {
						value = 0d;
					}
					nodeService.setProperty(parent.getData().getNodeRef(), prop, value);
				}
			}
		}
	}

}
