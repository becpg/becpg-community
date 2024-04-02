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
package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Repository;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>EntityListDAOImplV2 class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
@Repository("entityListDAO")
@DependsOn("bcpg.dictionaryBootstrap")
public class EntityListDAOImpl implements EntityListDAO {

	private static final Log logger = LogFactory.getLog(EntityListDAOImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private CopyService copyService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	@Qualifier("policyComponent")
	private PolicyComponent policyComponent;

	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

	private Set<QName> hiddenListQnames = new HashSet<>();

	/** {@inheritDoc} */
	@Override
	public void registerHiddenList(QName listTypeQname) {
		hiddenListQnames.add(listTypeQname);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getListContainer(NodeRef nodeRef) {

		return nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, String name) {

		NodeRef listNodeRef = null;
		if (listContainerNodeRef != null) {
			listNodeRef = nodeService.getChildByName(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, QName.createValidLocalName(name));
		}
		return listNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, QName listQName) {
		if (listQName == null) {
			return null;
		}

		return getList(listContainerNodeRef, listQName.getLocalName());
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createListContainer(NodeRef nodeRef) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
		properties.put(ContentModel.PROP_TITLE, RepoConsts.CONTAINER_DATALISTS);
		NodeRef ret = nodeService
				.createNode(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.TYPE_FOLDER, properties).getChildRef();
		nodeService.addAspect(ret, BeCPGModel.ASPECT_HIDDEN_FOLDER, new HashMap<>());
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, QName listQName) {

		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try {
			I18NUtil.setLocale(Locale.getDefault());
			I18NUtil.setContentLocale(null);

			ClassDefinition classDef = entityDictionaryService.getClass(listQName);

			if (classDef == null) {
				logger.error("No classDef found for :" + listQName);
				throw new InvalidParameterException("No classDef found for :" + listQName);
			}

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, listQName.getLocalName());

			MLText classTitleMLText = TranslateHelper.getTemplateTitleMLText(classDef.getName());
			MLText classDescritptionMLText = TranslateHelper.getTemplateDescriptionMLText(classDef.getName());

			properties.put(ContentModel.PROP_TITLE, classTitleMLText);
			properties.put(ContentModel.PROP_DESCRIPTION, classDescritptionMLText);
			properties.put(DataListModel.PROP_DATALISTITEMTYPE, listQName.toPrefixString(namespaceService));

			return nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, listQName, DataListModel.TYPE_DATALIST, properties)
					.getChildRef();
		} finally {
			I18NUtil.setLocale(currentLocal);
			I18NUtil.setContentLocale(currentContentLocal);
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, String name, QName listQName) {

		String entityTitle = TranslateHelper.getTranslatedPath(name);
		if (entityTitle == null) {
			entityTitle = name;
		}

		QName assocQname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));

		if (logger.isDebugEnabled()) {
			logger.debug("Create data list with name:" + name + " of type " + listQName.getLocalName() + " title " + entityTitle
					+ " with assocQname : " + assocQname.toPrefixString(namespaceService));
		}

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(ContentModel.PROP_TITLE, entityTitle);
		properties.put(DataListModel.PROP_DATALISTITEMTYPE, listQName.toPrefixString(namespaceService));

		return nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, assocQname, DataListModel.TYPE_DATALIST, properties)
				.getChildRef();
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef) {

		List<NodeRef> existingLists = new ArrayList<>();

		if (listContainerNodeRef != null) {

			for (NodeRef listNodeRef : associationService.getChildAssocs(listContainerNodeRef, ContentModel.ASSOC_CONTAINS)) {

				String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

				if ((dataListType != null) && !dataListType.isEmpty()) {

					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);

					if ((BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName)
							|| entityDictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)
							|| ((String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME)).startsWith(RepoConsts.WUSED_PREFIX))) {

						if (!hiddenListQnames.contains(dataListTypeQName)) {
							existingLists.add(listNodeRef);
						}
					} else {
						logger.warn("Existing " + dataListTypeQName + " list doesn't inheritate from 'bcpg:entityListItem'.");
					}
				}

			}
		}
		return existingLists;
	}

	/** {@inheritDoc} 
	 * @return */
	@Override
	public Map<QName, List<NodeRef>> getListItemsByType(NodeRef dataListNodeRef) {
		return associationService.getChildAssocsByType(dataListNodeRef, ContentModel.ASSOC_CONTAINS).getItemsByType();
	}
	
	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getListItems(NodeRef dataListNodeRef, QName dataType) {
		return getListItems(dataListNodeRef, dataType, null);
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getListItems(final NodeRef listNodeRef, final QName listQNameFilter, Map<String, Boolean> sortMap) {
		return associationService.getChildAssocs(listNodeRef, ContentModel.ASSOC_CONTAINS, listQNameFilter, sortMap);

	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty(final NodeRef listNodeRef, final QName listQNameFilter) {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().parent(listNodeRef);

		if (listQNameFilter != null) {
			queryBuilder.ofType(listQNameFilter);
		} else {
			queryBuilder.ofType(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}

		return queryBuilder.inDB().singleValue() == null;

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getListItem(NodeRef listContainerNodeRef, QName assocQName, NodeRef nodeRef) {

		if ((listContainerNodeRef != null) && (assocQName != null) && (nodeRef != null)) {

			for (NodeRef listItemNodeRef : getListItems(listContainerNodeRef, null)) {

				NodeRef assocRef = associationService.getTargetAssoc(listItemNodeRef, assocQName);
				if ((assocRef != null) && nodeRef.equals(assocRef)) {
					return listItemNodeRef;
				}

			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean override) {

		copyDataLists(sourceNodeRef, targetNodeRef, null, override);
	}

	/** {@inheritDoc} */
	@Override
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, Collection<QName> listQNames, boolean override) {

		logger.debug("/*-- copyDataLists --*/");

		// do not initialize entity version
		if (nodeService.hasAspect(targetNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
			return;
		}

		if (sourceNodeRef != null) {

			/*-- copy source datalists--*/
			logger.debug("copy source datalists");
			NodeRef sourceListContainerNodeRef = getListContainer(sourceNodeRef);
			NodeRef targetListContainerNodeRef = getListContainer(targetNodeRef);

			if (sourceListContainerNodeRef != null) {

				if (targetListContainerNodeRef == null) {

					logger.debug("copy datalist container");

					// copy all datalist in order to have assoc references
					// updated (ie: taskList is referenced in deliverableList,
					// so when doing checkout assoc must be updated)
					targetListContainerNodeRef = copyService.copy(sourceListContainerNodeRef, targetNodeRef, BeCPGModel.ASSOC_ENTITYLISTS,
							BeCPGModel.ASSOC_ENTITYLISTS, true);
					nodeService.setProperty(targetListContainerNodeRef, ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
				} else {

					List<NodeRef> sourceListsNodeRef = getExistingListsNodeRef(sourceListContainerNodeRef);
					for (NodeRef sourceListNodeRef : sourceListsNodeRef) {

						copyDataListInternal(sourceListNodeRef, targetListContainerNodeRef, listQNames, override);
					}
				}
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public void copyDataList(NodeRef dataListNodeRef, NodeRef entityNodeRef, boolean override) {

		NodeRef targetListContainerNodeRef = getListContainer(entityNodeRef);
		if (targetListContainerNodeRef != null) {
			copyDataListInternal(dataListNodeRef, targetListContainerNodeRef, null, override);
		}

	}

	private void copyDataListInternal(NodeRef dataListNodeRef, NodeRef targetListContainerNodeRef, Collection<QName> listQNames, boolean override) {

		String dataListType = (String) nodeService.getProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

		QName listQName = QName.createQName(dataListType, namespaceService);

		if ((listQNames == null) || listQNames.contains(listQName)) {

			NodeRef existingListNodeRef = findMatchingList(dataListNodeRef, targetListContainerNodeRef);

			boolean copy = true;
			if (existingListNodeRef != null) {
				if (override) {
					nodeService.addAspect(existingListNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(existingListNodeRef);
				} else {
					copy = false;
				}
			}

			if (copy) {
				String name = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
				logger.debug("copy datalist " + listQName);
				NodeRef newDLNodeRef = copyService.copy(dataListNodeRef, targetListContainerNodeRef, ContentModel.ASSOC_CONTAINS,
						DataListModel.TYPE_DATALIST, true);
				nodeService.setProperty(newDLNodeRef, ContentModel.PROP_NAME, name);
			}
		}

	}

	@Override
	public void mergeDataList(NodeRef dataListNodeRef, NodeRef entityNodeRef, boolean appendOnly) {
		NodeRef targetListContainerNodeRef = getListContainer(entityNodeRef);
		if (targetListContainerNodeRef != null) {

			String dataListType = (String) nodeService.getProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

			QName listQName = QName.createQName(dataListType, namespaceService);

			NodeRef existingListNodeRef = findMatchingList(dataListNodeRef, targetListContainerNodeRef);

			if (existingListNodeRef != null) {
				for (NodeRef itemNodeRef : getListItems(dataListNodeRef, null, null)) {
					if (appendOnly || (findMatchingListItem(itemNodeRef, existingListNodeRef) == null)) {
						copyService.copy(itemNodeRef, existingListNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
					}
				}

			} else {
				String name = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
				logger.debug("copy datalist " + listQName);
				NodeRef newDLNodeRef = copyService.copy(dataListNodeRef, targetListContainerNodeRef, ContentModel.ASSOC_CONTAINS,
						DataListModel.TYPE_DATALIST, true);
				nodeService.setProperty(newDLNodeRef, ContentModel.PROP_NAME, name);
			}

		}

	}

	@SuppressWarnings("unchecked")
	private NodeRef findMatchingListItem(NodeRef targetItemNodeRef, NodeRef dataListNodeRef) {
		Set<QName> isIgnoredTypes = new HashSet<>();
		isIgnoredTypes.add(ContentModel.PROP_NAME);
		isIgnoredTypes.add(ContentModel.PROP_CREATED);
		isIgnoredTypes.add(ContentModel.PROP_CREATOR);
		isIgnoredTypes.add(ContentModel.PROP_MODIFIED);
		isIgnoredTypes.add(ContentModel.PROP_MODIFIER);
		isIgnoredTypes.add(ForumModel.PROP_COMMENT_COUNT);
		isIgnoredTypes.add(BeCPGModel.PROP_SORT);
		isIgnoredTypes.add(BeCPGModel.PROP_DEPTH_LEVEL);

		Map<QName, Serializable> targetPropertiesAndAssocs = nodeService.getProperties(targetItemNodeRef);
		for (AssociationRef ref : this.nodeService.getTargetAssocs(targetItemNodeRef, RegexQNamePattern.MATCH_ALL)) {
			List<NodeRef> nodes = (List<NodeRef>) targetPropertiesAndAssocs.get(ref.getTypeQName());
			if (nodes == null) {
				nodes = new ArrayList<>(4);
			}
			targetPropertiesAndAssocs.put(ref.getTypeQName(), (Serializable) nodes);
			nodes.add(ref.getTargetRef());
		}

		for (NodeRef itemNodeRef : getListItems(dataListNodeRef, null, null)) {
			Map<QName, Serializable> propertiesAndAssocs = nodeService.getProperties(itemNodeRef);
			for (AssociationRef ref : this.nodeService.getTargetAssocs(itemNodeRef, RegexQNamePattern.MATCH_ALL)) {
				List<NodeRef> nodes = (List<NodeRef>) propertiesAndAssocs.get(ref.getTypeQName());
				if (nodes == null) {
					nodes = new ArrayList<>(4);
				}
				propertiesAndAssocs.put(ref.getTypeQName(), (Serializable) nodes);
				nodes.add(ref.getTargetRef());
			}
			boolean isDifferent = false;
			MapDifference<QName, Serializable> diff = Maps.difference(targetPropertiesAndAssocs, propertiesAndAssocs);
			for (QName afterType : diff.entriesDiffering().keySet()) {
				if (!afterType.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI) && !isIgnoredTypes.contains(afterType)
						&& (propertiesAndAssocs.get(afterType) != null) && (propertiesAndAssocs.get(afterType) != "")) {

					isDifferent = true;

					break;
				}
			}

			if (!isDifferent) {
				return itemNodeRef;
			}

		}

		return null;
	}

	@Override
	public NodeRef findMatchingList(NodeRef dataListNodeRef, NodeRef targetListContainerNodeRef) {

		String dataListType = (String) nodeService.getProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
		String name = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
		QName listQName = QName.createQName(dataListType, namespaceService);

		NodeRef existingListNodeRef;

		if (name.startsWith(RepoConsts.WUSED_PREFIX) || name.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)
				|| name.startsWith(RepoConsts.SMART_CONTENT_PREFIX) || name.contains("@")) {
			existingListNodeRef = getList(targetListContainerNodeRef, name);
		} else {
			existingListNodeRef = getList(targetListContainerNodeRef, listQName);
		}

		return existingListNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createListItem(NodeRef listNodeRef, QName listType, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> associations) {

		// create
		NodeRef nodeRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, listType, properties)
				.getChildRef();

		for (Map.Entry<QName, List<NodeRef>> kv : associations.entrySet()) {
			associationService.update(nodeRef, kv.getKey(), kv.getValue());
		}

		return nodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public void moveDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		NodeRef sourceListContainerNodeRef = getListContainer(sourceNodeRef);
		NodeRef targetListContainerNodeRef = getListContainer(targetNodeRef);

		if (sourceListContainerNodeRef != null) {
			if (targetListContainerNodeRef != null) {
				nodeService.addAspect(targetListContainerNodeRef, ContentModel.ASPECT_TEMPORARY, null);
				nodeService.deleteNode(targetListContainerNodeRef);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Move :" + nodeService.getPath(sourceListContainerNodeRef) + " to " + nodeService.getPath(targetNodeRef));
			}

			nodeService.moveNode(sourceListContainerNodeRef, targetNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS);

		}

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntity(NodeRef listItemNodeRef) {
		NodeRef listNodeRef = nodeService.getPrimaryParent(listItemNodeRef).getParentRef();

		if (listNodeRef != null) {
			return getEntityFromList(listNodeRef);
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntityFromList(NodeRef listNodeRef) {

		NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef();

		if (listContainerNodeRef != null) {
			return nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
		}

		return null;
	}

}
