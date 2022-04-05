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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

/**
 * Utility script methods for supplier portal
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class SupplierPortalHelper extends BaseScopableProcessorExtension {

	/** Constant <code>SUPPLIER_SITE_ID="supplier-portal"</code> */
	public static final String SUPPLIER_SITE_ID = "supplier-portal";

	private static final Log logger = LogFactory.getLog(SupplierPortalHelper.class);

	private AssociationService associationService;

	private Repository repository;

	private NodeService nodeService;

	private PermissionService permissionService;

	private RepoService repoService;

	private SiteService siteService;

	private HierarchyService hierarchyService;

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>repository</code>.</p>
	 *
	 * @param repository a {@link org.alfresco.repo.model.Repository} object.
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object.
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * <p>assignToSupplier.</p>
	 *
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param entityNodeRef a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public void assignToSupplier(final ProjectData project, final TaskListDataItem task, final ScriptNode entityNodeRef) {
		assignToSupplier(project, task, entityNodeRef, true);
	}

	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object.
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	/**
	 * <p>Setter for the field <code>siteService</code>.</p>
	 *
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object.
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	

	/**
	 * <p>Setter for the field <code>hierarchyService</code>.</p>
	 *
	 * @param hierarchyService a {@link fr.becpg.repo.hierarchy.HierarchyService} object.
	 */
	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	/**
	 * <p>assignToSupplier.</p>
	 *
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param entityNodeRef a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param moveSupplier a boolean.
	 */
	public void assignToSupplier(final ProjectData project, final TaskListDataItem task, final ScriptNode entityNodeRef, boolean moveSupplier) {

		
		if (task != null) {
			if (entityNodeRef != null) {
				NodeRef supplierNodeRef = null;
				if (PLMModel.TYPE_SUPPLIER.equals(nodeService.getType(entityNodeRef.getNodeRef()))) {
					supplierNodeRef = entityNodeRef.getNodeRef();
					nodeService.setProperty(supplierNodeRef, PLMModel.PROP_SUPPLIER_STATE, SystemState.Simulation);

				} else {
					supplierNodeRef = associationService.getTargetAssoc(entityNodeRef.getNodeRef(), PLMModel.ASSOC_SUPPLIERS);
					if (supplierNodeRef != null) {
						if (moveSupplier) {
							nodeService.setProperty(supplierNodeRef, PLMModel.PROP_SUPPLIER_STATE, SystemState.Simulation);
						}
					}
					nodeService.setProperty(entityNodeRef.getNodeRef(), PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
				}

				if (supplierNodeRef != null) {

					associationService.update(project.getNodeRef(), PLMModel.ASSOC_SUPPLIERS, Collections.singletonList(supplierNodeRef));

					List<NodeRef> accountNodeRefs = associationService.getTargetAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
					if (accountNodeRefs != null && (task.getResources() ==null || task.getResources().isEmpty())) {
						if (task.getResources() == null) {
							task.setResources(new ArrayList<>());
						}
						task.getResources().addAll(accountNodeRefs);

					} else {
						logger.info("No account provided for supplier");
					}

				} else {
					logger.info("No supplier provided for entity");
				}

				AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

					@Override
					public NodeRef doWork() throws Exception {
						if ((task.getResources() != null) && !task.getResources().isEmpty()) {

							NodeRef supplierNodeRef = associationService.getTargetAssoc(entityNodeRef.getNodeRef(), PLMModel.ASSOC_SUPPLIERS);

							NodeRef dest = getSupplierDestFolder(supplierNodeRef, task.getResources());

							if (supplierNodeRef != null) {
								if (moveSupplier) {
									repoService.moveNode(supplierNodeRef, dest);
									permissionService.setInheritParentPermissions(supplierNodeRef, true);
								} else {
									for (NodeRef resourceRef : task.getResources()) {
										permissionService.setPermission(supplierNodeRef,
												(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME), PermissionService.CONSUMER,
												true);
									}
								}
							}

							nodeService.moveNode(entityNodeRef.getNodeRef(), dest, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
							permissionService.setInheritParentPermissions(entityNodeRef.getNodeRef(), true);

							for (NodeRef resourceRef : task.getResources()) {
								permissionService.setPermission(task.getNodeRef(),
										(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME), PermissionService.COORDINATOR,
										true);
								for (DeliverableListDataItem deliverable : ProjectHelper.getDeliverables(project, task.getNodeRef())) {
									permissionService.setPermission(deliverable.getNodeRef(),
											(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME), PermissionService.COORDINATOR,
											true);
									if ((deliverable.getContent() != null)
											&& ((deliverable.getScriptOrder() == null)
													|| DeliverableScriptOrder.None.equals(deliverable.getScriptOrder()))
											&& isInProjectFolder(deliverable.getContent(), project.getNodeRef())) {
										String name = (String) nodeService.getProperty(deliverable.getContent(), ContentModel.PROP_NAME);
										NodeRef existingNodeWithSameName = nodeService.getChildByName(entityNodeRef.getNodeRef(),
												ContentModel.ASSOC_CONTAINS, name);
										if (existingNodeWithSameName != null) {
											nodeService.deleteNode(deliverable.getContent());
											deliverable.setContent(existingNodeWithSameName);
										} else {
											repoService.moveNode(deliverable.getContent(), entityNodeRef.getNodeRef());
										}
									}

								}
							}
						} else {
							logger.warn("No one is assign to task");
							task.setTaskState(TaskState.OnHold);
						}

						return entityNodeRef.getNodeRef();
					}

					private NodeRef getSupplierDestFolder(NodeRef supplierNodeRef, List<NodeRef> resources) {
						NodeRef destFolder = null;

						if (supplierNodeRef != null) {
							SiteInfo siteInfo = siteService.getSite(SupplierPortalHelper.SUPPLIER_SITE_ID);

							if (siteInfo != null) {

								NodeRef documentLibraryNodeRef = siteService.getContainer(SupplierPortalHelper.SUPPLIER_SITE_ID,
										SiteService.DOCUMENT_LIBRARY);
								if (documentLibraryNodeRef != null) {
									Locale currentLocal = I18NUtil.getLocale();
									Locale currentContentLocal = I18NUtil.getContentLocale();

									try {
										I18NUtil.setLocale(Locale.getDefault());
										I18NUtil.setContentLocale(null);

										destFolder = repoService.getOrCreateFolderByPath(documentLibraryNodeRef, "Referencing",
												I18NUtil.getMessage("path.referencing"));

										if (destFolder != null) {

											destFolder = hierarchyService.getOrCreateHierachyFolder(supplierNodeRef, null, destFolder);

										}

										destFolder = repoService.getOrCreateFolderByPath(destFolder, supplierNodeRef.getId(),
												(String) nodeService.getProperty(supplierNodeRef, ContentModel.PROP_NAME));

										for (NodeRef resourceRef : task.getResources()) {
											permissionService.setPermission(destFolder,
													(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME),
													PermissionService.COORDINATOR, true);
										}

									} finally {
										I18NUtil.setLocale(currentLocal);
										I18NUtil.setContentLocale(currentContentLocal);
									}

								}

							}
						}

						if (destFolder == null) {
							NodeRef resourceRef = task.getResources().get(0);
							destFolder = repository.getUserHome(resourceRef);

						}

						permissionService.setPermission(destFolder, PermissionService.GROUP_PREFIX + PLMGroup.ReferencingMgr.toString(),
								PermissionService.COORDINATOR, true);

						return destFolder;
					}

				}, AuthenticationUtil.SYSTEM_USER_NAME);

			} else {
				logger.info("No entity provided for project");
			}

		} else {
			logger.error("No task provided");
		}
	}

	/**
	 * <p>validateProjectEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public void validateProjectEntity(final ScriptNode entityNodeRef) {
		validateProjectEntity(entityNodeRef, true);
	}

	/**
	 * <p>validateProjectEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param moveSupplier a boolean.
	 */
	public void validateProjectEntity(final ScriptNode entityNodeRef, boolean moveSupplier) {
		if (entityNodeRef != null) {
			if (PLMModel.TYPE_SUPPLIER.equals(nodeService.getType(entityNodeRef.getNodeRef()))) {
				nodeService.setProperty(entityNodeRef.getNodeRef(), PLMModel.PROP_SUPPLIER_STATE, SystemState.Valid);
			} else {
				nodeService.setProperty(entityNodeRef.getNodeRef(), PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
				if (moveSupplier) {
					NodeRef supplierNodeRef = associationService.getTargetAssoc(entityNodeRef.getNodeRef(), PLMModel.ASSOC_SUPPLIERS);
					if (supplierNodeRef != null) {
						nodeService.setProperty(supplierNodeRef, PLMModel.PROP_SUPPLIER_STATE, SystemState.Valid);
						nodeService.setProperty(supplierNodeRef, ContentModel.PROP_MODIFIED, new Date());
					}
				}
			}
		}
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

}
