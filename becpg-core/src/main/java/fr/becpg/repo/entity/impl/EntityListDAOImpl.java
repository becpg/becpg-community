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
package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Repository;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 *
 * @author querephi
 *
 */
@Repository("entityListDAO")
public class EntityListDAOImpl implements EntityListDAO {

	private static final Log logger = LogFactory.getLog(EntityListDAOImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private CopyService copyService;

	@Autowired
	private AssociationService associationService;

	private Set<QName> hiddenListQnames = new HashSet<>();

	@Override
	public void registerHiddenList(QName listTypeQname) {
		hiddenListQnames.add(listTypeQname);
	}

	@Override
	public NodeRef getListContainer(NodeRef nodeRef) {

		return nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
	}

	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, String name) {

		NodeRef listNodeRef = null;
		if (listContainerNodeRef != null) {
			listNodeRef = nodeService.getChildByName(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, QName.createValidLocalName(name));
		}
		return listNodeRef;
	}

	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, QName listQName) {
		if (listQName == null) {
			return null;
		}

		return getList(listContainerNodeRef, listQName.getLocalName());
	}

	@Override
	public NodeRef createListContainer(NodeRef nodeRef) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
		properties.put(ContentModel.PROP_TITLE, RepoConsts.CONTAINER_DATALISTS);
		NodeRef ret = nodeService
				.createNode(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.TYPE_FOLDER, properties).getChildRef();
		nodeService.addAspect(ret, BeCPGModel.ASPECT_HIDDEN_FOLDER, new HashMap<QName, Serializable>());
		return ret;
	}

	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, QName listQName) {

		Locale currentLocal = I18NUtil.getLocale();

		try {
			I18NUtil.setLocale(Locale.getDefault());

			ClassDefinition classDef = dictionaryService.getClass(listQName);

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
		}
	}

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

	@Override
	public List<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef) {

		List<NodeRef> existingLists = new ArrayList<>();

		if (listContainerNodeRef != null) {
			List<FileInfo> nodes = fileFolderService.listFolders(listContainerNodeRef);

			for (FileInfo node : nodes) {

				NodeRef listNodeRef = node.getNodeRef();
				String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

				if ((dataListType != null) && !dataListType.isEmpty()) {

					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);

					if ((BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName)
							|| dictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM))) {

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

	@Override
	public List<NodeRef> getListItems(NodeRef dataListNodeRef, QName dataType) {

		// Map<String, Boolean> sortMap = new LinkedHashMap<>();
		// sortMap.put("@bcpg:sort", true);
		// sortMap.put("@cm:created", true);
		//
		// return getListItems(dataListNodeRef, dataType, sortMap);

		return getListItemsV2(dataListNodeRef, dataType);
	}

	@Override
	public List<NodeRef> getListItems(final NodeRef listNodeRef, final QName listQNameFilter, Map<String, Boolean> sortMap) {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().addSort(sortMap).parent(listNodeRef);

		if (listQNameFilter != null) {
			queryBuilder.ofType(listQNameFilter);
		} else {
			queryBuilder.ofType(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}

		return queryBuilder.childFileFolders(new PagingRequest(5000, null)).getPage();

	}

	private List<NodeRef> getListItemsV2(final NodeRef listNodeRef, final QName listQNameFilter) {

		List<NodeRef> ret = associationService.getChildAssocs(listNodeRef, ContentModel.ASSOC_CONTAINS);
		if (listQNameFilter != null) {
			CollectionUtils.filter(ret, object -> {

				if (!nodeService.exists((NodeRef) object)) {
					return false;
				}

				if ((object != null) && (object instanceof NodeRef) && nodeService.getType((NodeRef) object).equals(listQNameFilter)) {
					return true;
				}

				return false;
			});
		}

		Collections.sort(ret, (o1, o2) -> {

			Integer sort1 = (Integer) nodeService.getProperty(o1, BeCPGModel.PROP_SORT);
			Integer sort2 = (Integer) nodeService.getProperty(o2, BeCPGModel.PROP_SORT);

			if (sort1 == sort2) {

				Date created1 = (Date) nodeService.getProperty(o1, ContentModel.PROP_CREATED);
				Date created2 = (Date) nodeService.getProperty(o2, ContentModel.PROP_CREATED);

				if (created1 == created2) {
					return 0;
				}

				if (created1 == null) {
					return -1;
				}

				if (created2 == null) {
					return 1;
				}

				return created1.compareTo(created2);
			}

			if (sort1 == null) {
				return -1;
			}

			if (sort2 == null) {
				return 1;
			}

			return sort1.compareTo(sort2);
		});

		return ret;
	}

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

	@Override
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean override) {

		copyDataLists(sourceNodeRef, targetNodeRef, null, override);
	}

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

	@Override
	public void copyDataList(NodeRef dataListNodeRef, NodeRef entityNodeRef, boolean override) {

		NodeRef targetListContainerNodeRef = getListContainer(entityNodeRef);
		if (targetListContainerNodeRef != null) {
			copyDataListInternal(dataListNodeRef, targetListContainerNodeRef, null, override);
		}

	}

	private void copyDataListInternal(NodeRef dataListNodeRef, NodeRef targetListContainerNodeRef, Collection<QName> listQNames, boolean override) {

		String dataListType = (String) nodeService.getProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
		String name = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
		QName listQName = QName.createQName(dataListType, namespaceService);

		if ((listQNames == null) || listQNames.contains(listQName)) {

			NodeRef existingListNodeRef;

			if (name.startsWith(RepoConsts.WUSED_PREFIX) || name.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)) {
				existingListNodeRef = getList(targetListContainerNodeRef, name);
			} else {
				existingListNodeRef = getList(targetListContainerNodeRef, listQName);
			}

			boolean copy = true;
			if (existingListNodeRef != null) {
				if (override) {
					nodeService.deleteNode(existingListNodeRef);
				} else {
					copy = false;
				}
			}

			if (copy) {
				logger.debug("copy datalist " + listQName);
				NodeRef newDLNodeRef = copyService.copy(dataListNodeRef, targetListContainerNodeRef, ContentModel.ASSOC_CONTAINS,
						DataListModel.TYPE_DATALIST, true);
				nodeService.setProperty(newDLNodeRef, ContentModel.PROP_NAME, name);
			}
		}

	}

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

	@Override
	public void moveDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		NodeRef sourceListContainerNodeRef = getListContainer(sourceNodeRef);
		NodeRef targetListContainerNodeRef = getListContainer(targetNodeRef);

		if (sourceListContainerNodeRef != null) {
			if (targetListContainerNodeRef != null) {
				nodeService.deleteNode(targetListContainerNodeRef);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Move :" + nodeService.getPath(sourceListContainerNodeRef) + " to " + nodeService.getPath(targetNodeRef));
			}

			nodeService.moveNode(sourceListContainerNodeRef, targetNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS);

		}

	}

	@Override
	public NodeRef getEntity(NodeRef listItemNodeRef) {
		NodeRef listNodeRef = nodeService.getPrimaryParent(listItemNodeRef).getParentRef();

		if (listNodeRef != null) {
			NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef();

			if (listContainerNodeRef != null) {
				NodeRef rootNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
				logger.debug("rootNodeRef: " + rootNodeRef);
				return rootNodeRef;
			}
		}

		return null;
	}

}
