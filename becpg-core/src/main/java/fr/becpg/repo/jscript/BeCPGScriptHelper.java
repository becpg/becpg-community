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
 * @version $Id: $Id
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

	/**
	 * <p>Setter for the field <code>useBrowserLocale</code>.</p>
	 *
	 * @param useBrowserLocale a boolean.
	 */
	public void setUseBrowserLocale(boolean useBrowserLocale) {
		this.useBrowserLocale = useBrowserLocale;
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
	 * <p>Setter for the field <code>olapService</code>.</p>
	 *
	 * @param olapService a {@link fr.becpg.repo.olap.OlapService} object.
	 */
	public void setOlapService(OlapService olapService) {
		this.olapService = olapService;
	}

	/**
	 * <p>Setter for the field <code>autoNumService</code>.</p>
	 *
	 * @param autoNumService a {@link fr.becpg.repo.entity.AutoNumService} object.
	 */
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	/**
	 * <p>Setter for the field <code>quickShareService</code>.</p>
	 *
	 * @param quickShareService a {@link org.alfresco.service.cmr.quickshare.QuickShareService} object.
	 */
	public void setQuickShareService(QuickShareService quickShareService) {
		this.quickShareService = quickShareService;
	}

	/**
	 * <p>getOrCreateBeCPGCode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getOrCreateBeCPGCode(ScriptNode sourceNode) {
		return autoNumService.getOrCreateBeCPGCode(sourceNode.getNodeRef());
	}

	/**
	 * <p>getAutoNumValue.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getAutoNumValue(String className, String propertyName) {
		return autoNumService.getAutoNumValue(QName.createQName(className, namespaceService), QName.createQName(propertyName, namespaceService));
	}

	/**
	 * <p>getOrCreateCode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getOrCreateCode(ScriptNode sourceNode, String propertyName) {
		return autoNumService.getOrCreateCode(sourceNode.getNodeRef(), QName.createQName(propertyName, namespaceService));
	}

	/**
	 * <p>shareContent.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public void shareContent(ScriptNode sourceNode) {
		quickShareService.shareContent(sourceNode.getNodeRef());
	}

	/**
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>paginatedSearchCache</code>.</p>
	 *
	 * @param paginatedSearchCache a {@link fr.becpg.repo.search.PaginatedSearchCache} object.
	 */
	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}

	/**
	 * <p>isShowEntitiesInTree.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isShowEntitiesInTree() {
		return showEntitiesInTree;
	}

	/**
	 * <p>Setter for the field <code>showEntitiesInTree</code>.</p>
	 *
	 * @param showEntitiesInTree a boolean.
	 */
	public void setShowEntitiesInTree(boolean showEntitiesInTree) {
		this.showEntitiesInTree = showEntitiesInTree;
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
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object.
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
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
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>isShowUnauthorizedWarning.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isShowUnauthorizedWarning() {
		return showUnauthorizedWarning;
	}

	/**
	 * <p>Setter for the field <code>showUnauthorizedWarning</code>.</p>
	 *
	 * @param showUnauthorizedWarning a boolean.
	 */
	public void setShowUnauthorizedWarning(boolean showUnauthorizedWarning) {
		this.showUnauthorizedWarning = showUnauthorizedWarning;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>getMLProperty.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @param locale a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMLProperty(ScriptNode sourceNode, String propQName, String locale) {
		MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), getQName(propQName));
		if (mlText != null) {
			return MLTextHelper.getClosestValue(mlText, MLTextHelper.parseLocale(locale));
		}
		return null;
	}

	/**
	 * <p>getMLConstraint.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @param locale a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMLConstraint(String value, String propQName, String locale) {

		PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(propQName, namespaceService));

		String constraintName = null;
		DynListConstraint dynListConstraint = null;

		if (!propertyDef.getConstraints().isEmpty()) {
			for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
				if (constraint.getConstraint() instanceof DynListConstraint) {
					dynListConstraint = (DynListConstraint) constraint.getConstraint();
					
				} else if ("LIST".equals(constraint.getConstraint().getType())) {
					constraintName = constraint.getRef().toPrefixString(namespaceService).replace(":", "_");
					
				}
				
				if(constraintName!=null || dynListConstraint!=null) {
					break;
				}
			}
		}

		if (dynListConstraint != null) {
			return dynListConstraint.getDisplayLabel(value, new Locale(locale));
		}

		return constraintName != null ? TranslateHelper.getConstraint(constraintName, value, new Locale(locale)) : value;
	}

	/**
	 * <p>setMLProperty.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @param locale a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
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

	/**
	 * <p>getQName.</p>
	 *
	 * @param qName a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getQName(String qName) {
		return QName.createQName(qName, namespaceService);
	}

	/**
	 * <p>assocValue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocValue(NodeRef nodeRef, String assocQname) {
		return associationService.getTargetAssoc(nodeRef, getQName(assocQname));
	}

	/**
	 * <p>assocValue.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocValue(ScriptNode sourceNode, String assocQname) {
		return assocValue(sourceNode.getNodeRef(), assocQname);
	}

	/**
	 * <p>assocValue.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocValue(String nodeRef, String assocQname) {
		return assocValue(new NodeRef(nodeRef), assocQname);
	}

	/**
	 * <p>assocValues.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocValues(ScriptNode sourceNode, String assocQname) {
		return assocValues(sourceNode.getNodeRef(), assocQname);
	}

	/**
	 * <p>assocValues.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocValues(String nodeRef, String assocQname) {
		return assocValues(new NodeRef(nodeRef), assocQname);
	}

	/**
	 * <p>assocValues.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocValues(NodeRef nodeRef, String assocQname) {
		return wrapValue(associationService.getTargetAssocs(nodeRef, getQName(assocQname)));
	}

	// TODO Perfs
	private Object wrapValue(Object object) {
		return ScriptValueConverter.wrapValue(Context.getCurrentContext().initSafeStandardObjects(), object);
	}

	/**
	 * <p>assocPropValues.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocPropValues(String nodeRef, String assocQname, String propQName) {
		return assocPropValues(new NodeRef(nodeRef), assocQname, propQName);
	}

	/**
	 * <p>assocPropValues.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocPropValues(ScriptNode sourceNode, String assocQname, String propQName) {
		return assocPropValues(sourceNode.getNodeRef(), assocQname, propQName);
	}

	/**
	 * <p>assocPropValues.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
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

	/**
	 * <p>assocAssocValue.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocAssocValue(String nodeRef, String assocQname, String assocAssocsQname) {
		return assocAssocValue(new NodeRef(nodeRef), assocQname, assocAssocsQname);
	}

	/**
	 * <p>assocAssocValue.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocAssocValue(ScriptNode sourceNode, String assocQname, String assocAssocsQname) {
		return assocAssocValue(sourceNode.getNodeRef(), assocQname, assocAssocsQname);
	}

	/**
	 * <p>assocAssocValue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocAssocValue(NodeRef nodeRef, String assocQname, String assocAssocsQname) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return associationService.getTargetAssoc(assocNodeRef, getQName(assocAssocsQname));
		}
		return null;
	}

	/**
	 * <p>assocAssocValues.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocAssocValues(ScriptNode sourceNode, String assocQname, String assocAssocsQname) {
		return assocAssocValues(sourceNode.getNodeRef(), assocQname, assocAssocsQname);
	}

	/**
	 * <p>assocAssocValues.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocAssocValues(NodeRef nodeRef, String assocQname, String assocAssocsQname) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return wrapValue(associationService.getTargetAssocs(assocNodeRef, getQName(assocAssocsQname)));
		}
		return wrapValue(new ArrayList<>());
	}

	/**
	 * <p>assocPropValue.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.io.Serializable} object.
	 */
	public Serializable assocPropValue(ScriptNode sourceNode, String assocQname, String propQName) {
		return assocPropValue(sourceNode.getNodeRef(), assocQname, propQName);
	}

	/**
	 * <p>assocPropValue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.io.Serializable} object.
	 */
	public Serializable assocPropValue(NodeRef nodeRef, String assocQname, String propQName) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return nodeService.getProperty(assocNodeRef, getQName(propQName));
		}
		return null;
	}

	/**
	 * <p>updateAssoc.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocs a {@link java.lang.Object} object.
	 */
	public void updateAssoc(ScriptNode sourceNode, String assocQname, Object assocs) {
		updateAssoc(sourceNode.getNodeRef(), assocQname, assocs);
	}

	/**
	 * <p>updateAssoc.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocs a {@link java.lang.Object} object.
	 */
	public void updateAssoc(String nodeRef, String assocQname, Object assocs) {
		updateAssoc(new NodeRef(nodeRef), assocQname, assocs);
	}

	/**
	 * <p>updateAssoc.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocs a {@link java.lang.Object} object.
	 */
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

	/**
	 * <p>updateChecksum.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param checksum a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String updateChecksum(String key, String value, String checksum) {
		return CheckSumHelper.updateChecksum(key, value, checksum);
	}

	/**
	 * <p>isSameChecksum.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param checksum a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isSameChecksum(String key, String value, String checksum) {
		return CheckSumHelper.isSameChecksum(key, value, checksum);
	}

	/**
	 * <p>findOne.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 */
	public RepositoryEntity findOne(String nodeRef) {
		if (NodeRef.isNodeRef(nodeRef)) {
			return alfrescoRepository.findOne(new NodeRef(nodeRef));
		}
		return null;
	}
	
	public RepositoryEntity save(RepositoryEntity entity) {
		return alfrescoRepository.save(entity);
	}
	
	public void setExtraValue(RepositoryEntity entity, String qName,  Object value) {
		 entity.getExtraProperties().put(getQName(qName), (Serializable) ScriptValueConverter.unwrapValue(value));
	}

	/**
	 * <p>getMessage.</p>
	 *
	 * @param messageKey a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMessage(String messageKey) {
		return I18NUtil.getMessage(messageKey, I18NUtil.getLocale());
	}

	/**
	 * <p>getMessage.</p>
	 *
	 * @param messageKey a {@link java.lang.String} object.
	 * @param param a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMessage(String messageKey, Object param) {
		return I18NUtil.getMessage(messageKey, param, I18NUtil.getLocale());
	}

	/**
	 * <p>getOlapSSOUrl.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOlapSSOUrl() {
		return olapService.getSSOUrl();
	}

	/**
	 * <p>createBranch.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param parent a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param setAutoMerge a boolean.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode createBranch(ScriptNode entity, ScriptNode parent, boolean setAutoMerge) {
		NodeRef branchNodeRef = entityVersionService.createBranch(entity.getNodeRef(), parent.getNodeRef());

		if (setAutoMerge) {
			associationService.update(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO, entity.getNodeRef());
		}

		return new ScriptNode(branchNodeRef, serviceRegistry);
	}

	/**
	 * <p>createBranch.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param parent a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode createBranch(ScriptNode entity, ScriptNode parent) {
		return createBranch(entity, parent, false);
	}

	/**
	 * <p>mergeBranch.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param branchTo a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param description a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode mergeBranch(ScriptNode entity, ScriptNode branchTo, String description, String type) {
		NodeRef retNodeRef = entityVersionService.mergeBranch(entity.getNodeRef(), branchTo != null ? branchTo.getNodeRef() : null,
				VersionType.valueOf(type), description);

		return new ScriptNode(retNodeRef, serviceRegistry);
	}
	
	
	
	public void updateLastVersionLabel(ScriptNode entity,String versionLabel) {
		entityVersionService.updateLastVersionLabel(entity.getNodeRef(), versionLabel);
	}

	/**
	 * <p>moveAndRename.</p>
	 *
	 * @param nodeToMove a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param destination a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode moveAndRename(ScriptNode nodeToMove, ScriptNode destination) {
		repoService.moveNode(nodeToMove.getNodeRef(), destination.getNodeRef());
		return nodeToMove;
	}

	/**
	 * <p>changeEntityListStates.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param state a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean changeEntityListStates(ScriptNode entity, String state) {
		return entityService.changeEntityListStates(entity.getNodeRef(), EntityListState.valueOf(state));
	}

	/**
	 * <p>copyList.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param destNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param listQname a {@link java.lang.String} object.
	 */
	public void copyList(ScriptNode sourceNode, ScriptNode destNode, String listQname) {
		entityListDAO.copyDataList(entityListDAO.getList(entityListDAO.getListContainer(sourceNode.getNodeRef()), getQName(listQname)),
				destNode.getNodeRef(), true);
	}

	/**
	 * <p>listExist.</p>
	 *
	 * @param node a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param listQname a {@link java.lang.String} object.
	 * @return a boolean.
	 */
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

	/**
	 * <p>getListItems.</p>
	 *
	 * @param node a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param listQname a {@link java.lang.String} object.
	 * @return an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects.
	 */
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

	/**
	 * <p>getSubTypes.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getSubTypes(String type) {
		Set<String> ret = new HashSet<>();

		for (QName typeQname : entityDictionaryService.getSubTypes(QName.createQName(type, namespaceService))) {
			ret.add(typeQname.toPrefixString(namespaceService));
		}

		return ret.toArray(new String[ret.size()]);

	}

	/**
	 * <p>getSearchResults.</p>
	 *
	 * @param queryId a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getSearchResults(String queryId) {
		List<NodeRef> ret = paginatedSearchCache.getSearchResults(queryId);
		if (ret != null) {
			return ret.stream().map(NodeRef::toString).toArray(String[]::new);
		} else {
			logger.warn("No results found for queryId: " + queryId);
		}

		return new String[] {};
	}

	/**
	 * <p>setPermissionAsSystem.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param permission a {@link java.lang.String} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean setPermissionAsSystem(ScriptNode sourceNode, String permission, String authority) {
		return setPermissionAsSystem(sourceNode.getNodeRef(),permission,authority);
	}
	
	public boolean setPermissionAsSystem(String nodeRef, String permission, String authority) {
		return setPermissionAsSystem(new NodeRef(nodeRef),permission,authority);
	}
	
	public boolean setPermissionAsSystem(NodeRef nodeRef, String permission, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(nodeRef, authority, permission, true);
			return true;
		});
	}

	/**
	 * <p>allowWrite.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean allowWrite(ScriptNode sourceNode, String authority) {
		return allowWrite(sourceNode.getNodeRef(), authority);
	}
	
	public boolean allowWrite(String nodeRef,  String authority) {
		return allowWrite(new NodeRef(nodeRef), authority);
	}
	
	public boolean allowWrite(NodeRef nodeRef, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(nodeRef, authority, PermissionService.EDITOR, true);
			return true;
		});
	}

	/**
	 * <p>allowRead.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean allowRead(ScriptNode sourceNode, String authority) {
		return allowRead(sourceNode.getNodeRef(), authority);
	}

	public boolean allowRead(String nodeRef, String authority) {
		return allowWrite(new NodeRef(nodeRef), authority);
	}

	public boolean allowRead(NodeRef nodeRef, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(nodeRef, authority, PermissionService.READ, true);
			return true;
		});
	}

	/**
	 * <p>clearPermissions.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param inherit a boolean.
	 * @return a boolean.
	 */
	public boolean clearPermissions(ScriptNode sourceNode, boolean inherit) {
		return clearPermissions(sourceNode.getNodeRef(),inherit);
	}

	public boolean clearPermissions(String nodeRef, boolean inherit) {
		return clearPermissions(new NodeRef(nodeRef),inherit);
	}
	
	public boolean clearPermissions(NodeRef nodeRef, boolean inherit) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.deletePermissions(nodeRef);
			permissionService.setInheritParentPermissions(nodeRef, inherit);
			return true;
		});
	}
	
	/**
	 * <p>deleteGroupPermission.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean deleteGroupPermission(ScriptNode sourceNode, String authority) {
		return deleteGroupPermission(sourceNode.getNodeRef(), authority);
	}
	
	public boolean deleteGroupPermission(String nodeRef, String authority) {
		return deleteGroupPermission(new NodeRef(nodeRef), authority);
	}
	
	public boolean deleteGroupPermission(NodeRef nodeRef, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
			clearPermissions(nodeRef, false);
			for (AccessPermission permission : permissions) {
				if (!permission.getAuthority().equals(authority)) {
					permissionService.setPermission(nodeRef, permission.getAuthority(), permission.getPermission(), true);
				}
			}
			return true;
		});
	}

	/**
	 * <p>getUserLocale.</p>
	 *
	 * @param personNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link java.lang.String} object.
	 */
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

	/**
	 * <p>getUserContentLocale.</p>
	 *
	 * @param personNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link java.lang.String} object.
	 */
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

	/**
	 * <p>generateEAN13Code.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws org.apache.commons.validator.routines.checkdigit.CheckDigitException if any.
	 */
	public static String generateEAN13Code(String prefix) throws CheckDigitException {
		return GTINHelper.createEAN13Code(prefix, AutoNumHelper.getAutoNumValue("bcpg:eanCode", "bcpg:ean13Pref" + prefix));
	}
}
