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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * Utility script methods for supplier portal
 *
 * @author matthieu
 * @version $Id: $Id
 */
@BeCPGPublicApi
public final class SupplierPortalHelper extends BaseScopableProcessorExtension {

	/** Constant <code>SUPPLIER_SITE_ID="supplier-portal"</code> */
	public static final String SUPPLIER_SITE_ID = "supplier-portal";

	private static final Log logger = LogFactory.getLog(SupplierPortalHelper.class);

	private AssociationService associationService;

	private NodeService nodeService;

	private PermissionService permissionService;

	private RepoService repoService;

	private EntityVersionService entityVersionService;

	private ProjectService projectService;

	private ServiceRegistry serviceRegistry;

	private SupplierPortalService supplierPortalService;
	
	protected EntityDictionaryService entityDictionaryService;
	
	private NamespaceService namespaceService;
	
	private EntityService entityService;
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}


	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setSupplierPortalService(SupplierPortalService supplierPortalService) {
		this.supplierPortalService = supplierPortalService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}


	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * <p>
	 * assignToSupplier.
	 * </p>
	 *
	 * @param project       a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param task          a
	 *                      {@link fr.becpg.repo.project.data.projectList.TaskListDataItem}
	 *                      object.
	 * @param entityNodeRef a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param moveSupplier  a boolean.
	 */
	public ScriptNode[] assignToSupplier(final ProjectData project, final TaskListDataItem task, final ScriptNode entityNode) {

		if (task != null) {
			if (entityNode != null) {
				NodeRef entityNodeRef = entityNode.getNodeRef();
				NodeRef supplierNodeRef = getSupplierNodeRef(project, entityNodeRef);

				if (supplierNodeRef != null) {

					List<NodeRef> accountNodeRefs = associationService.getTargetAssocs(project.getNodeRef(),
							PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
					if (accountNodeRefs != null && (task.getResources() == null || task.getResources().isEmpty())) {
						if (task.getResources() == null) {
							task.setResources(new ArrayList<>());
						}
						task.getResources().addAll(accountNodeRefs);

					} else {
						logger.info("No account provided for supplier");
					}
					

					List<NodeRef> ret =	AuthenticationUtil.runAs(new RunAsWork<List<NodeRef>>() {

						@Override
						public List<NodeRef> doWork() throws Exception {

							List<NodeRef> resources = projectService.extractResources(project.getNodeRef(),
									task.getResources());

							resources.removeIf((resource -> ContentModel.TYPE_AUTHORITY_CONTAINER
									.equals(nodeService.getType(resource))));

							if ((resources != null) && !resources.isEmpty()) {

								NodeRef dest = supplierPortalService.getOrCreateSupplierDestFolder(supplierNodeRef,
										resources);

								if(entityNodeRef!=null && !entityNodeRef.equals(supplierNodeRef)) {
									repoService.moveNode(entityNodeRef, dest);				
								}
								
								if(!permissionService.getInheritParentPermissions(entityNodeRef)) {
									permissionService.setInheritParentPermissions(entityNodeRef, true);
								}

								for (NodeRef resourceRef : resources) {
									permissionService.setPermission(task.getNodeRef(),
											(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME),
											PermissionService.CONTRIBUTOR, true);
									
									for (DeliverableListDataItem deliverable : ProjectHelper.getDeliverables(project,
											task.getNodeRef())) {
										permissionService.setPermission(deliverable.getNodeRef(),
												(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME),
												PermissionService.CONTRIBUTOR, true);
										if ((deliverable.getContent() != null)
												&& ((deliverable.getScriptOrder() == null)
														|| DeliverableScriptOrder.None.equals(deliverable.getScriptOrder()))
												&& isInProjectFolder(deliverable.getContent(), project.getNodeRef())) {
											String name = (String) nodeService.getProperty(deliverable.getContent(),
													ContentModel.PROP_NAME);
											NodeRef existingNodeWithSameName = nodeService.getChildByName(
													entityNodeRef, ContentModel.ASSOC_CONTAINS, name);
											if (existingNodeWithSameName != null) {
												nodeService.deleteNode(deliverable.getContent());
												deliverable.setContent(existingNodeWithSameName);
											} else {
												if(deliverable.getContent()!=null && !deliverable.getContent().equals(supplierNodeRef)) {
													repoService.moveNode(deliverable.getContent(), entityNodeRef);
												}
											}
										}

									}
								}
							} else {
								logger.warn("No one is assign to task");
								task.setTaskState(TaskState.OnHold);
							}

							return resources;
						}

					}, AuthenticationUtil.SYSTEM_USER_NAME);

					return ret.stream().map(n -> new ActivitiScriptNode(n, serviceRegistry))
							.toArray(ScriptNode[]::new);
					

				} else {
					logger.info("No supplier provided for entity");
				}


			} else {
				logger.info("No entity provided for project");
			}

		} else {
			logger.error("No task provided");
		}

		return new ScriptNode[0];
	}

	private NodeRef getSupplierNodeRef(final ProjectData project, NodeRef entityNodeRef) {
		NodeRef supplierNodeRef = supplierPortalService.getSupplierNodeRef(project.getNodeRef());
		
		if(supplierNodeRef == null) {
			 supplierNodeRef = supplierPortalService.getSupplierNodeRef(entityNodeRef);
		}
		return supplierNodeRef;
	}


	/**
	 * <p>
	 * validateProjectEntity.
	 * </p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode validateProjectEntity(final ScriptNode entityNode) {

		if (entityNode != null) {

			NodeRef entityNodeRef = entityNode.getNodeRef();
			
			if(nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_AUTO_MERGE_ASPECT)
					&& nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_AUTO_MERGE_DATE) == null) {
				entityNodeRef = entityVersionService.mergeBranch(entityNodeRef, null);
			}
			
			QName type = nodeService.getType(entityNodeRef);
			
			if (entityDictionaryService.isSubClass(type, PLMModel.TYPE_PRODUCT)) {
				nodeService.setProperty(entityNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
			} else if (entityDictionaryService.isSubClass(type, PLMModel.TYPE_SUPPLIER)) {
				nodeService.setProperty(entityNodeRef, PLMModel.PROP_SUPPLIER_STATE, SystemState.Valid);
			}
			
			return new ScriptNode(entityNodeRef, serviceRegistry, getScope());

		}

		return entityNode;
	}
	
	public String extractSupplierProjectName(ScriptNode[] items) {
		if (items != null) {
			for (ScriptNode item : items) {
				Date currentDate = Calendar.getInstance().getTime();

				NodeRef entityNodeRef = entityService.getEntityNodeRef(item.getNodeRef(), nodeService.getType(item.getNodeRef()));
				
				NodeRef supplierNodeRef = supplierPortalService.getSupplierNodeRef(entityNodeRef);
				
				if (supplierNodeRef != null) {
					return supplierPortalService.createName(item.getNodeRef(), supplierNodeRef,
							supplierPortalService.getProjectNameTpl(), currentDate);
				}
			}
		}
		return "";
	}

	public ScriptNode[] extractSupplierAccountRefs(ScriptNode[] items) {
		if (items != null) {
			for (ScriptNode item : items) {

				List<NodeRef> accountNodeRefs = supplierPortalService.extractSupplierAccountRefs(item.getNodeRef());

				if (!accountNodeRefs.isEmpty()) {
					return accountNodeRefs.stream().map(n -> new ActivitiScriptNode(n, serviceRegistry)).toArray(ScriptNode[]::new);
				}
			}
		}

		return new ScriptNode[0];
	}

	public ScriptNode createSupplierProject(ScriptNode[] items, ScriptNode projectTemplate, String[] supplierAccounts) {
		if (items != null && items.length > 0) {
			List<NodeRef> supplierAccountNodeRefs = new ArrayList<>();

			if(supplierAccounts!=null) {
				for(String tmp : supplierAccounts) {
					supplierAccountNodeRefs.add(new NodeRef(tmp));
				}
			}

			return new ActivitiScriptNode(
						supplierPortalService.createSupplierProject(items[0].getNodeRef(), projectTemplate.getNodeRef(), supplierAccountNodeRefs),
						serviceRegistry);

		}

		return null;

	}

	private boolean isInProjectFolder(NodeRef documentNodeRef, NodeRef projectNodeRef) {

		if (documentNodeRef != null) {
			NodeRef folderNodeRef = nodeService.getPrimaryParent(documentNodeRef).getParentRef();

			while ((folderNodeRef != null) && !nodeService.hasAspect(folderNodeRef, BeCPGModel.ASPECT_ENTITYLISTS)) {
				folderNodeRef = nodeService.getPrimaryParent(folderNodeRef).getParentRef();
			}

			if ((folderNodeRef != null) && folderNodeRef.equals(projectNodeRef)) {
				return true;
			}

		}
		return false;
	}
	
	public ScriptNode createExternalUser(String email, String firstName, String lastName, boolean notify, Map<String, Serializable> extraProps) {
		Map<QName, Serializable> convertedExtraProps = new HashMap<>();
		if (extraProps != null) {
			for (Entry<String, Serializable> entry : extraProps.entrySet()) {
				convertedExtraProps.put(QName.createQName(entry.getKey(), namespaceService), entry.getValue());
			}
		}
		return new ScriptNode(supplierPortalService.createExternalUser(email, firstName, lastName, notify, convertedExtraProps), serviceRegistry, getScope());
	}
	
}