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
package fr.becpg.repo.jscript;

import java.util.ArrayList;
import java.util.Arrays;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

/**
 * Utility script methods for suplier portal
 * 
 * @author matthieu
 *
 */
public final class SupplierPortalHelper extends BaseScopableProcessorExtension {

	private static final Log logger = LogFactory.getLog(SupplierPortalHelper.class);

	private AssociationService associationService;

	private Repository repository;

	private NodeService nodeService;

	private PermissionService permissionService;


	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void assignToSupplier(final ProjectData project, final TaskListDataItem task, final ScriptNode entityNodeRef) {

		if (task != null) {
			if (entityNodeRef != null) {
				NodeRef supplierNodeRef = associationService.getTargetAssoc(entityNodeRef.getNodeRef(), PLMModel.ASSOC_SUPPLIERS);
				if (supplierNodeRef != null) {
					
				    associationService.update(project.getNodeRef(), PLMModel.ASSOC_SUPPLIERS, Arrays.asList(supplierNodeRef));
					
					NodeRef accountNodeRef = associationService.getTargetAssoc(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNT);
					if (accountNodeRef != null) {
						if (task.getResources() != null) {
							task.getResources().clear();
						} else {
							task.setResources(new ArrayList<NodeRef>());
						}
						task.getResources().add(accountNodeRef);

					} else {
						logger.info("No account provided for supplier");
					}

				} else {
					logger.info("No supplier provided for entity");
				}

				AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

					@Override
					public NodeRef doWork() throws Exception {
						if (task.getResources() != null && !task.getResources().isEmpty()) {
							NodeRef resourceRef = task.getResources().get(0);
							NodeRef userHome = repository.getUserHome(resourceRef);
							
							permissionService.setPermission(userHome, PermissionService.GROUP_PREFIX + PLMGroup.ReferencingMgr.toString(),
									PermissionService.COORDINATOR, true);
							
							NodeRef supplierNodeRef = associationService.getTargetAssoc(entityNodeRef.getNodeRef(), PLMModel.ASSOC_SUPPLIERS);
							if (supplierNodeRef != null) {
							   nodeService.moveNode(supplierNodeRef, userHome, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
							   permissionService.setInheritParentPermissions(supplierNodeRef, true);
							   
							   permissionService.setPermission(supplierNodeRef, PermissionService.ALL_PERMISSIONS,
										PermissionService.CONSUMER, true);
							}
							
							nodeService.moveNode(entityNodeRef.getNodeRef(), userHome, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
							permissionService.setInheritParentPermissions(entityNodeRef.getNodeRef(), true);
							
							permissionService.setPermission(task.getNodeRef(),(String)nodeService.getProperty(resourceRef,ContentModel.PROP_USERNAME),
									PermissionService.COORDINATOR, true);
					        for(DeliverableListDataItem deliverable :  ProjectHelper.getDeliverables(project, task.getNodeRef())){
					        	permissionService.setPermission(deliverable.getNodeRef(),(String)nodeService.getProperty(resourceRef,ContentModel.PROP_USERNAME),
										PermissionService.COORDINATOR, true);
					        }
						} else {
							logger.warn("No one is assign to task");
							task.setTaskState(TaskState.OnHold);
						}

						return entityNodeRef.getNodeRef();
					}
				}, AuthenticationUtil.SYSTEM_USER_NAME);

			} else {
				logger.info("No entity provided for project");
			}

		} else {
			logger.error("No task provided");
		}
	}

}
