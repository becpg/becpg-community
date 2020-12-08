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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.listvalue.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ParentValuePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ParentValuePlugin extends EntityListValuePlugin {

	private static final Log logger = LogFactory.getLog(ParentValuePlugin.class);

	private static final String SOURCE_TYPE_PARENT_VALUE = "ParentValue";
	private static final String SOURCE_TYPE_DATA_LIST_CHARACT = "DataListCharact";
	private static final String FILTER_PARENT_AS_ENTITY = "parentAsEntity";

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PARENT_VALUE, SOURCE_TYPE_DATA_LIST_CHARACT };
	}

	/** {@inheritDoc} */
	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef entityNodeRef = null;

		String parent = (String) props.get(ListValueService.PROP_PARENT);

		String queryFilter = (String) props.get(ListValueService.PROP_FILTER);
		if ((queryFilter != null) && FILTER_PARENT_AS_ENTITY.equals(queryFilter) && (parent != null)) {
			queryFilter = null;
			if (!parent.isEmpty() && NodeRef.isNodeRef(parent)) {
				entityNodeRef = new NodeRef(parent);
				parent = null;
			}
		} else if (((String) props.get(ListValueService.PROP_NODEREF) != null)
				&& NodeRef.isNodeRef((String) props.get(ListValueService.PROP_NODEREF))) {
			entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		}

		if (entityNodeRef != null) {
			logger.debug("ParentValue sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

			String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
			QName type = QName.createQName(className, namespaceService);

			String listName = (String) props.get(ListValueService.PROP_PATH);

			String attributeNames = (String) props.get(ListValueService.PROP_ATTRIBUTE_NAME);
			Set<QName> attributeQNames = new HashSet<>();
			if (attributeNames != null) {
				for (String attributeName : attributeNames.split(",")) {
					attributeQNames.add(QName.createQName(attributeName, namespaceService));
				}
			}

			NodeRef itemId = null;

			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("itemId") != null) {
					itemId = new NodeRef(extras.get("itemId"));
				}
			}

			NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
			if (listsContainerNodeRef != null) {
				NodeRef dataListNodeRef;
				if (listName == null) {
					dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, type);
				} else {
					dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, listName);
				}
				if (dataListNodeRef != null) {
					if (dictionaryService.getProperty(attributeQNames.iterator().next()) != null) {
						return suggestFromProp(sourceType, dataListNodeRef, itemId, type, attributeQNames, query, queryFilter, parent, pageNum,
								pageSize, props);
					} else {
						return suggestFromAssoc(sourceType, dataListNodeRef, itemId, type, attributeQNames.iterator().next(), query, queryFilter,
								pageNum, pageSize, props);
					}
				}
			}
		}
		return new ListValuePage(new ArrayList<>(), pageNum, pageSize, null);
	}

	private ListValuePage suggestFromProp(String sourceType, NodeRef dataListNodeRef, NodeRef itemId, QName datalistType, Set<QName> propertyQNames,
			String query, String queryFilter, String parent, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery().ofType(datalistType).parent(dataListNodeRef);

		if (propertyQNames.size() == 1) {
			beCPGQueryBuilder.andPropQuery(propertyQNames.iterator().next(), prepareQuery(query));
		} else {
			String searchTemplate = "%(";
			for (QName propertyQName : propertyQNames) {
				searchTemplate += " " + entityDictionaryService.toPrefixString(propertyQName);
			}
			searchTemplate += ")";

			beCPGQueryBuilder.excludeDefaults().inSearchTemplate(searchTemplate).locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

			if (!isAllQuery(query)) {
				StringBuilder ftsQuery = new StringBuilder();
				if (query.length() > 2) {
					ftsQuery.append("(" + prepareQuery(query.trim()) + ") OR ");
				}
				ftsQuery.append("(" + query + ")");
				beCPGQueryBuilder.andFTSQuery(ftsQuery.toString());
			}

		}

		if ((queryFilter != null) && (queryFilter.length() > 0)) {
			String[] splitted = queryFilter.split("\\|");
			beCPGQueryBuilder.andPropEquals(QName.createQName(splitted[0], namespaceService), splitted[1]);
		}

		if (parent != null) {
			if (!parent.isEmpty() && NodeRef.isNodeRef(parent)) {
				beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, parent);
			} else {
				beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, String.valueOf(RepoConsts.DEFAULT_LEVEL));
			}
		}

		if (itemId != null) {
			beCPGQueryBuilder.andNotID(itemId);
		}

		logger.debug("suggestDatalistItem for query : " + beCPGQueryBuilder.toString());
		List<NodeRef> ret = beCPGQueryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS).list();
		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(propertyQNames, nodeService));
	}

	private ListValuePage suggestFromAssoc(String sourceType, NodeRef dataListNodeRef, NodeRef itemId, QName datalistType, QName associationQName,
			String query, String queryFilter, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		List<ListValueEntry> result = new ArrayList<>();
		for (NodeRef dataListItemNodeRef : entityListDAO.getListItems(dataListNodeRef, datalistType)) {
			if (!dataListItemNodeRef.equals(itemId)) {
				NodeRef targetNode = associationService.getTargetAssoc(dataListItemNodeRef, associationQName);

				if (targetNode != null) {
					QName type = nodeService.getType(targetNode);
					String name = attributeExtractorService.extractPropName(type, targetNode);
					if (isQueryMatch(query, name)) {
						String cssClass = attributeExtractorService.extractMetadata(type, targetNode);
						if (SOURCE_TYPE_DATA_LIST_CHARACT.equals(sourceType)) {
							result.add(new ListValueEntry(targetNode.toString(), name, cssClass));
						} else {
							result.add(new ListValueEntry(dataListItemNodeRef.toString(), name, cssClass));
						}

					}
				}
			}
		}
		return new ListValuePage(result, pageNum, pageSize, null);
	}
}
