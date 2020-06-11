/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.repo.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.mozilla.javascript.Context;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.ScriptValueConverter;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.EntityListState;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AutoNumHelper;
import fr.becpg.repo.helper.CheckSumHelper;
import fr.becpg.repo.helper.GTINHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.PaginatedSearchCache;

/**
 * Utility script methods
 *
 * @author matthieu
 *
 */
public final class BeCPGScriptHelper extends BaseScopableProcessorExtension {

	private static Log logger = LogFactory.getLog(BeCPGScriptHelper.class);

	private NodeService nodeService;

	private AutoNumService autoNumService;

	private OlapService olapService;

	private QuickShareService quickShareService;

	private NodeService mlNodeService;

	private NamespaceService namespaceService;

	private DictionaryService dictionaryService;

	private EntityDictionaryService entityDictionaryService;

	private EntityVersionService entityVersionService;

	private ServiceRegistry serviceRegistry;

	private AssociationService associationService;

	private EntityService entityService;

	private PaginatedSearchCache paginatedSearchCache;

	private PermissionService permissionService;

	private RepoService repoService;

	private EntityListDAO entityListDAO;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private boolean useBrowserLocale;

	private boolean showEntitiesInTree = false;

	private boolean showUnauthorizedWarning = true;

	public void setUseBrowserLocale(boolean useBrowserLocale) {
		this.useBrowserLocale = useBrowserLocale;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setOlapService(OlapService olapService) {
		this.olapService = olapService;
	}

	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	public void setQuickShareService(QuickShareService quickShareService) {
		this.quickShareService = quickShareService;
	}

	public String getOrCreateBeCPGCode(ScriptNode sourceNode) {
		return autoNumService.getOrCreateBeCPGCode(sourceNode.getNodeRef());
	}

	public String getAutoNumValue(String className, String propertyName) {
		return autoNumService.getAutoNumValue(QName.createQName(className, namespaceService), QName.createQName(propertyName, namespaceService));
	}

	public String getOrCreateCode(ScriptNode sourceNode, String propertyName) {
		return autoNumService.getOrCreateCode(sourceNode.getNodeRef(), QName.createQName(propertyName, namespaceService));
	}

	public void shareContent(ScriptNode sourceNode) {
		quickShareService.shareContent(sourceNode.getNodeRef());
	}

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}

	public boolean isShowEntitiesInTree() {
		return showEntitiesInTree;
	}

	public void setShowEntitiesInTree(boolean showEntitiesInTree) {
		this.showEntitiesInTree = showEntitiesInTree;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public boolean isShowUnauthorizedWarning() {
		return showUnauthorizedWarning;
	}

	public void setShowUnauthorizedWarning(boolean showUnauthorizedWarning) {
		this.showUnauthorizedWarning = showUnauthorizedWarning;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public String getMLProperty(ScriptNode sourceNode, String propQName, String locale) {
		MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), getQName(propQName));
		if (mlText != null) {
			return MLTextHelper.getClosestValue(mlText, MLTextHelper.parseLocale(locale));
		}
		return null;
	}

	public String getMLConstraint(String value, String propQName, String locale) {

		PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(propQName, namespaceService));

		String constraintName = null;
		DynListConstraint dynListConstraint = null;

		if (!propertyDef.getConstraints().isEmpty()) {
			for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
				if (constraint.getConstraint() instanceof DynListConstraint) {
					dynListConstraint = (DynListConstraint) constraint.getConstraint();
					break;
				} else if ("LIST".equals(constraint.getConstraint().getType())) {
					constraintName = constraint.getRef().toPrefixString(namespaceService).replace(":", "_");
					break;
				}
			}
		}

		if (dynListConstraint != null) {
			return dynListConstraint.getDisplayLabel(value, new Locale(locale));
		}

		return constraintName != null ? TranslateHelper.getConstraint(constraintName, value, new Locale(locale)) : value;
	}

	public void setMLProperty(ScriptNode sourceNode, String propQName, String locale, String value) {

		MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), getQName(propQName));
		if (mlText == null) {
			mlText = new MLText();
		}

		if ((value != null) && !value.isEmpty()) {
			mlText.addValue(MLTextHelper.parseLocale(locale), value);
		} else {
			mlText.removeValue(MLTextHelper.parseLocale(locale));
		}
		mlNodeService.setProperty(sourceNode.getNodeRef(), getQName(propQName), mlText);

	}

	public QName getQName(String qName) {
		return QName.createQName(qName, namespaceService);
	}

	public NodeRef assocValue(NodeRef nodeRef, String assocQname) {
		return associationService.getTargetAssoc(nodeRef, getQName(assocQname));
	}

	public NodeRef assocValue(ScriptNode sourceNode, String assocQname) {
		return assocValue(sourceNode.getNodeRef(), assocQname);
	}

	public NodeRef assocValue(String nodeRef, String assocQname) {
		return assocValue(new NodeRef(nodeRef), assocQname);
	}

	public Object assocValues(ScriptNode sourceNode, String assocQname) {
		return assocValues(sourceNode.getNodeRef(), assocQname);
	}

	public Object assocValues(String nodeRef, String assocQname) {
		return assocValues(new NodeRef(nodeRef), assocQname);
	}

	public Object assocValues(NodeRef nodeRef, String assocQname) {
		return wrapValue(associationService.getTargetAssocs(nodeRef, getQName(assocQname)));
	}

	// TODO Perfs
	private Object wrapValue(Object object) {
		return ScriptValueConverter.wrapValue(Context.getCurrentContext().initSafeStandardObjects(), object);
	}

	public Object assocPropValues(String nodeRef, String assocQname, String propQName) {
		return assocPropValues(new NodeRef(nodeRef), assocQname, propQName);
	}

	public Object assocPropValues(ScriptNode sourceNode, String assocQname, String propQName) {
		return assocPropValues(sourceNode.getNodeRef(), assocQname, propQName);
	}

	public Object assocPropValues(NodeRef nodeRef, String assocQname, String propQName) {
		List<String> ret = new ArrayList<>();
		for (NodeRef assoc : associationService.getTargetAssocs(nodeRef, getQName(assocQname))) {
			if (assoc != null) {
				String value = (String) nodeService.getProperty(assoc, getQName(propQName));
				if (value != null) {
					ret.add(value);
				}
			}
		}
		return wrapValue(ret);
	}

	public NodeRef assocAssocValue(String nodeRef, String assocQname, String assocAssocsQname) {
		return assocAssocValue(new NodeRef(nodeRef), assocQname, assocAssocsQname);
	}

	public NodeRef assocAssocValue(ScriptNode sourceNode, String assocQname, String assocAssocsQname) {
		return assocAssocValue(sourceNode.getNodeRef(), assocQname, assocAssocsQname);
	}

	public NodeRef assocAssocValue(NodeRef nodeRef, String assocQname, String assocAssocsQname) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return associationService.getTargetAssoc(assocNodeRef, getQName(assocAssocsQname));
		}
		return null;
	}

	public Object assocAssocValues(ScriptNode sourceNode, String assocQname, String assocAssocsQname) {
		return assocAssocValues(sourceNode.getNodeRef(), assocQname, assocAssocsQname);
	}

	public Object assocAssocValues(NodeRef nodeRef, String assocQname, String assocAssocsQname) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return wrapValue(associationService.getTargetAssocs(assocNodeRef, getQName(assocAssocsQname)));
		}
		return wrapValue(new ArrayList<>());
	}

	public Serializable assocPropValue(ScriptNode sourceNode, String assocQname, String propQName) {
		return assocPropValue(sourceNode.getNodeRef(), assocQname, propQName);
	}

	public Serializable assocPropValue(NodeRef nodeRef, String assocQname, String propQName) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return nodeService.getProperty(assocNodeRef, getQName(propQName));
		}
		return null;
	}

	public void updateAssoc(ScriptNode sourceNode, String assocQname, Object assocs) {
		updateAssoc(sourceNode.getNodeRef(), assocQname, assocs);
	}

	public void updateAssoc(String nodeRef, String assocQname, Object assocs) {
		updateAssoc(new NodeRef(nodeRef), assocQname, assocs);
	}

	public void updateAssoc(NodeRef nodeRef, String assocQname, Object assocs) {

		Object unwrapped = ScriptValueConverter.unwrapValue(assocs);

		if (unwrapped == null) {
			associationService.update(nodeRef, getQName(assocQname), new ArrayList<>());
		} else if (unwrapped instanceof ScriptNode) {
			associationService.update(nodeRef, getQName(assocQname), ((ScriptNode) unwrapped).getNodeRef());
		} else if (unwrapped instanceof NodeRef) {
			associationService.update(nodeRef, getQName(assocQname), (NodeRef) unwrapped);
		} else if (unwrapped instanceof Iterable<?>) {

			List<NodeRef> nodes = new ArrayList<>();
			for (Object element : (Iterable<?>) unwrapped) {
				if (element instanceof ScriptNode) {
					nodes.add(((ScriptNode) element).getNodeRef());

				} else if (element instanceof NodeRef) {
					nodes.add((NodeRef) element);
				}
			}
			associationService.update(nodeRef, getQName(assocQname), nodes);
		}

	}

	public String updateChecksum(String key, String value, String checksum) {
		return CheckSumHelper.updateChecksum(key, value, checksum);
	}

	public boolean isSameChecksum(String key, String value, String checksum) {
		return CheckSumHelper.isSameChecksum(key, value, checksum);
	}

	public RepositoryEntity findOne(String nodeRef) {
		if (NodeRef.isNodeRef(nodeRef)) {
			return alfrescoRepository.findOne(new NodeRef(nodeRef));
		}
		return null;
	}

	public String getMessage(String messageKey) {
		return I18NUtil.getMessage(messageKey, I18NUtil.getLocale());
	}

	public String getMessage(String messageKey, Object param) {
		return I18NUtil.getMessage(messageKey, param, I18NUtil.getLocale());
	}

	public String getOlapSSOUrl() {
		return olapService.getSSOUrl();
	}

	public ScriptNode createBranch(ScriptNode entity, ScriptNode parent, boolean setAutoMerge) {
		NodeRef branchNodeRef = entityVersionService.createBranch(entity.getNodeRef(), parent.getNodeRef());

		if (setAutoMerge) {
			associationService.update(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO, entity.getNodeRef());
		}

		return new ScriptNode(branchNodeRef, serviceRegistry);
	}

	public ScriptNode createBranch(ScriptNode entity, ScriptNode parent) {
		return createBranch(entity, parent, false);
	}

	public ScriptNode mergeBranch(ScriptNode entity, ScriptNode branchTo, String description, String type) {
		NodeRef retNodeRef = entityVersionService.mergeBranch(entity.getNodeRef(), branchTo != null ? branchTo.getNodeRef() : null,
				VersionType.valueOf(type), description);

		return new ScriptNode(retNodeRef, serviceRegistry);
	}

	public ScriptNode moveAndRename(ScriptNode nodeToMove, ScriptNode destination) {
		repoService.moveNode(nodeToMove.getNodeRef(), destination.getNodeRef());
		return nodeToMove;
	}

	public boolean changeEntityListStates(ScriptNode entity, String state) {
		return entityService.changeEntityListStates(entity.getNodeRef(), EntityListState.valueOf(state));
	}

	public void copyList(ScriptNode sourceNode, ScriptNode destNode, String listQname) {
		entityListDAO.copyDataList(entityListDAO.getList(entityListDAO.getListContainer(sourceNode.getNodeRef()), getQName(listQname)),
				destNode.getNodeRef(), true);
	}

	public boolean listExist(ScriptNode node, String listQname) {
		NodeRef listContainer = entityListDAO.getListContainer(node.getNodeRef());
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, getQName(listQname));
			if (listNodeRef != null) {

				return !entityListDAO.isEmpty(listNodeRef, getQName(listQname));

			}

		}
		return false;

	}

	public NodeRef[] getListItems(ScriptNode node, String listQname) {
		NodeRef listContainer = entityListDAO.getListContainer(node.getNodeRef());
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, getQName(listQname));
			if (listNodeRef != null) {

				return entityListDAO.getListItems(listNodeRef, getQName(listQname)).toArray(new NodeRef[] {});

			}

		}
		return new NodeRef[] {};
	}

	public String[] getSubTypes(String type) {
		Set<String> ret = new HashSet<>();

		for (QName typeQname : entityDictionaryService.getSubTypes(QName.createQName(type, namespaceService))) {
			ret.add(typeQname.toPrefixString(namespaceService));
		}

		return ret.toArray(new String[ret.size()]);

	}

	public String[] getSearchResults(String queryId) {
		List<NodeRef> ret = paginatedSearchCache.getSearchResults(queryId);
		if (ret != null) {
			return ret.stream().map(n -> n.toString()).toArray(String[]::new);
		} else {
			logger.warn("No results found for queryId: " + queryId);
		}

		return null;
	}

	public boolean setPermissionAsSystem(ScriptNode sourceNode, String permission, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(sourceNode.getNodeRef(), authority, permission, true);
			return true;
		});
	}

	public boolean allowWrite(ScriptNode sourceNode, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(sourceNode.getNodeRef(), authority, PermissionService.EDITOR, true);
			return true;
		});
	}

	public boolean allowRead(ScriptNode sourceNode, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(sourceNode.getNodeRef(), authority, PermissionService.READ, true);
			return true;
		});
	}

	public boolean clearPermissions(ScriptNode sourceNode, boolean inherit) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.deletePermissions(sourceNode.getNodeRef());
			permissionService.setInheritParentPermissions(sourceNode.getNodeRef(), inherit);
			return true;
		});
	}

	public boolean deleteGroupPermission(ScriptNode sourceNode, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			Set<AccessPermission> permissions = permissionService.getAllSetPermissions(sourceNode.getNodeRef());
			clearPermissions(sourceNode, false);
			for (AccessPermission permission : permissions) {
				if (!permission.getAuthority().equals(authority)) {
					permissionService.setPermission(sourceNode.getNodeRef(), permission.getAuthority(), permission.getPermission(), true);
				}
			}
			return true;
		});
	}

	public String getUserLocale(ScriptNode personNode) {
		String loc = (String) mlNodeService.getProperty(personNode.getNodeRef(), BeCPGModel.PROP_USER_LOCAL);
		if ((loc == null) || loc.isEmpty()) {
			if (useBrowserLocale) {
				return null;
			} else {
				return MLTextHelper.localeKey(I18NUtil.getLocale());
			}
		}
		return loc;
	}

	public String getUserContentLocale(ScriptNode personNode) {
		String loc = (String) mlNodeService.getProperty(personNode.getNodeRef(), BeCPGModel.PROP_USER_CONTENT_LOCAL);
		if ((loc == null) || loc.isEmpty()) {
			if (useBrowserLocale) {
				return null;
			} else {
				loc = MLTextHelper.localeKey(I18NUtil.getContentLocale());
			}
		}
		return loc;
	}

	public static String generateEAN13Code(String prefix) throws CheckDigitException {
		return GTINHelper.createEAN13Code(prefix, AutoNumHelper.getAutoNumValue("bcpg:eanCode", "bcpg:ean13Pref" + prefix));
	}
}
