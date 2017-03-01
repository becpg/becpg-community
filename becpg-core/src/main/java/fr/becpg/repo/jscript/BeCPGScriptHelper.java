/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.search.PaginatedSearchCache;

/**
 * Utility script methods
 *
 * @author matthieu
 *
 */
public final class BeCPGScriptHelper extends BaseScopableProcessorExtension {

	private static Log logger = LogFactory.getLog(BeCPGScriptHelper.class);

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

	private EntityListDAO entityListDAO;

	private PaginatedSearchCache paginatedSearchCache;

	private boolean showEntitiesInTree = false;

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

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
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

	public String getMLProperty(ScriptNode sourceNode, String propQName, String locale) {
		MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), QName.createQName(propQName, namespaceService));
		if (mlText != null) {
			return MLTextHelper.getClosestValue(mlText, new Locale(locale));
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

		MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), QName.createQName(propQName, namespaceService));
		if (mlText == null) {
			mlText = new MLText();
		}
		if ((value != null) && !value.isEmpty()) {
			mlText.addValue(new Locale(locale), value);
		} else {
			mlText.removeValue(new Locale(locale));
		}
		mlNodeService.setProperty(sourceNode.getNodeRef(), QName.createQName(propQName, namespaceService), mlText);

	}

	public String getMessage(String messageKey) {
		return I18NUtil.getMessage(messageKey, Locale.getDefault());
	}

	public String getMessage(String messageKey, Object param) {
		return I18NUtil.getMessage(messageKey, param, Locale.getDefault());
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

	public boolean changeEntityListStates(ScriptNode entity, String state) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entity.getNodeRef());

		if (listContainerNodeRef != null) {
			for (NodeRef listNodeRef : entityListDAO.getExistingListsNodeRef(listContainerNodeRef)) {
				mlNodeService.setProperty(listNodeRef, BeCPGModel.PROP_ENTITYLIST_STATE, SystemState.valueOf(state));
			}
		}

		return true;
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

}
