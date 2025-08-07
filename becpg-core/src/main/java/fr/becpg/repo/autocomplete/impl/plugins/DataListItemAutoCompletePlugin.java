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
package fr.becpg.repo.autocomplete.impl.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>DataListItemAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 * Autocomplete plugin that suggest dataListItem from specific sources
 *
 * Possible source type ParentValue/DataListCharact
 *
 * Example:
 *
 * <pre>
 * {@code
 *
 *  Suggest current entity list (bcpg:ingList) and use attributeName (bcpg:ingListIng) as display name
 *
 *     <control-param name="ds">becpg/autocomplete/DataListCharact?className=bcpg:ingList&#38;attributeName=bcpg:ingListIng</control>
 *
 * Suggest entity list (bcpg:plant) where entity in assoc path (bcpg:clients) of the current entity
 *
 *     <control-param name="ds">becpg/autocomplete/DataListCharact?path=bcpg:clients&amp;className=bcpg:plant&amp;attributeName=cm:name
 *
 *  Suggest entity list (qa:stockList) where entity in assoc path (qa:product) of the current dataListitem
 *
 *     <control-param name="ds">becpg/autocomplete/DataListCharact?path=qa:product&amp;className=qa:stockList&amp;attributeName=qa:batchId&amp;filter=itemAsEntity</control-param>
 *     <control-param name="urlParamsToPass">itemId</control-param>
 *
 *  Suggest entity list (bcpg:contactList) where entity in html field bcpg_clients
 *
 *      <control-param name="ds">becpg/autocomplete/DataListCharact?className=bcpg:contactList&amp;attributeName=bcpg:contactListFirstName,bcpg:contactListLastName&amp;filter=parentAsEntity</control-param>
 *	    <control-param name="parentAssoc">bcpg_clients</control-param>
 *
 *	    <control-param name="ds">becpg/autocomplete/DataListCharact?className=bcpg:plant&amp;attributeName=cm:name&amp;filter=parentAsEntity</control-param>
 *	    <control-param name="parentAssoc">bcpg_clients</control-param>
 *
 *
 *  Suggest current entity list exclude current item
 *
 * 	    <control-param name="ds">becpg/autocomplete/ParentValue?className=bcpg:costList&#38;attributeName=bcpg:costListCost</control-param>
 *      <control-param name="urlParamsToPass">itemId</control-param>
 *
 *	    <control-param name="ds">becpg/autocomplete/ParentValue?className=bcpg:nutList&#38;attributeName=bcpg:nutListNut</control-param>
 *	    <control-param name="urlParamsToPass">itemId</control-param>
 *
 * }
 * </pre>
 *
 * Datasources:
 *
 * DataListCharact
 *
 * ds:  becpg/autocomplete/DataListCharact?path={path}&amp;className={className}&amp;attributeName={attributeName}&amp;filter={filter}
 * param: {className} type of dataListItem to retrieve
 * param: {path} specify a field name on the currentEntity to retrieve entity
 * param: {attributeName} attribute names coma separated list that is used to filter on and display title
 * param: {filter} (none,itemAsEntity, parentAsEntity)
 * control-param: {parent} (filter=parentAsEntity) htmlField that can be used as path
 * control-param: {urlParamsToPass} (filter=itemAsEntity)  itemId
 *
 * ParentValue
 *
 * ds:  becpg/autocomplete/ParentValue?className={className}&amp;attributeName={attributeName}&amp;extra.showFullPath=false
 * param: {showFullPath} false, show fullpath
 * param: {className} type of dataListItem to retrieve exclude current item
 * param: {attributeName} attribute names coma separated list that is used to filter on and display title
 * control-param: {urlParamsToPass} itemId
 */
@Service("dataListItemAutoCompletePlugin")
@BeCPGPublicApi
public class DataListItemAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final Log logger = LogFactory.getLog(DataListItemAutoCompletePlugin.class);

	private static final String SOURCE_TYPE_PARENT_VALUE = "ParentValue";
	private static final String SOURCE_TYPE_DATA_LIST_CHARACT = "DataListCharact";
	private static final String FILTER_PARENT_AS_ENTITY = "parentAsEntity";
	private static final String FILTER_ITEM_AS_ENTITY = "itemAsEntity";

	@Autowired
	private EntityListDAO entityListDAO;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PARENT_VALUE, SOURCE_TYPE_DATA_LIST_CHARACT };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		List<NodeRef> entityNodeRefs = new ArrayList<>();

		NodeRef itemId = null;
		String listName = null;
		boolean showFullPath = false;

		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
		if (extras != null) {
			if (extras.get("itemId") != null) {
				itemId = new NodeRef(extras.get("itemId"));
			}
			if (extras.get("dataListsName") != null) {
				listName = extras.get("dataListsName");
			}
			
			if (extras.get("showFullPath") != null) {
				showFullPath = "true".equalsIgnoreCase(extras.get("showFullPath"));
			}
		}
		String parent = (String) props.get(AutoCompleteService.PROP_PARENT);

		String queryFilter = (String) props.get(AutoCompleteService.PROP_FILTER);
		if ((queryFilter != null) && FILTER_PARENT_AS_ENTITY.equals(queryFilter) && (parent != null)) {
			queryFilter = null;
			if (!parent.isEmpty()) {
				String[] splitted = parent.split(",");

				for (String node : splitted) {
					if (NodeRef.isNodeRef(node)) {
						entityNodeRefs.add(new NodeRef(node));
						parent = null;
					}
				}

			}
		} else if ((queryFilter != null) && FILTER_ITEM_AS_ENTITY.equals(queryFilter) && (itemId != null)) {
			entityNodeRefs.add(itemId);
			itemId = null;
			queryFilter = null;
		} else if (((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF) != null)

				&& NodeRef.isNodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF))) {
			entityNodeRefs.add(new NodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF)));
		}

		if (!entityNodeRefs.isEmpty()) {

			List<NodeRef> dataListNodeRefs = new ArrayList<>();
			String className = (String) props.get(AutoCompleteService.PROP_CLASS_NAME);
			QName type = QName.createQName(className, namespaceService);

			for (NodeRef entityNodeRef : entityNodeRefs) {

				String path = (String) props.get(AutoCompleteService.PROP_PATH);
				if ((path != null) && !path.isEmpty()) {
					if (path.contains(":") || path.contains("{")) {
						try {
							QName assocQname = QName.createQName(path, namespaceService);

							NodeRef targetAssocNodeRef = associationService.getTargetAssoc(entityNodeRefs.get(0), assocQname);
							if (targetAssocNodeRef != null) {
								entityNodeRef = targetAssocNodeRef;
							}

						} catch (NamespaceException e) {
							logger.warn("Wrong path assoc:" + path);
						}
					} else {
						listName = path;
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("ParentValue sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);
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
						dataListNodeRefs.add(dataListNodeRef);
					} else {
						logger.warn("No datalists found for type: " + (listName != null ? listName : type));
					}
				}

			}

			if (!dataListNodeRefs.isEmpty()) {

				String attributeNames = (String) props.get(AutoCompleteService.PROP_ATTRIBUTE_NAME);
				Set<QName> attributeQNames = new HashSet<>();
				if (attributeNames != null) {
					for (String attributeName : attributeNames.split(",")) {
						attributeQNames.add(QName.createQName(attributeName, namespaceService));
					}
				}

				if (dictionaryService.getProperty(attributeQNames.iterator().next()) != null) {
					return suggestFromProp(dataListNodeRefs, itemId, type, attributeQNames, query, queryFilter, parent, pageNum, pageSize);
				} else {
					return suggestFromAssoc(sourceType, dataListNodeRefs, itemId, type, attributeQNames.iterator().next(), query, pageNum, pageSize, showFullPath);
				}
			}

		}
		return new AutoCompletePage(new ArrayList<>(), pageNum, pageSize, null);
	}

	private AutoCompletePage suggestFromProp(List<NodeRef> dataListNodeRefs, NodeRef itemId, QName datalistType, Set<QName> propertyQNames,
			String query, String queryFilter, String parent, Integer pageNum, Integer pageSize) {
		List<NodeRef> ret = new ArrayList<>();
		for (NodeRef dataListNodeRef : dataListNodeRefs) {
			BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery().ofType(datalistType).parent(dataListNodeRef);

			if (propertyQNames.size() == 1) {
				beCPGQueryBuilder.andPropQuery(propertyQNames.iterator().next(), prepareQuery(query));
			} else {
				StringBuilder searchTemplate = new StringBuilder();
				searchTemplate.append("%(");
				for (QName propertyQName : propertyQNames) {
					searchTemplate.append(" " + entityDictionaryService.toPrefixString(propertyQName));
				}
				searchTemplate.append(")");

				beCPGQueryBuilder.excludeDefaults().inSearchTemplate(searchTemplate.toString()).locale(I18NUtil.getContentLocale()).andOperator()
						.ftsLanguage();

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
			if (logger.isDebugEnabled()) {
				logger.debug("suggestDatalistItem for query : " + beCPGQueryBuilder.toString());
			}
			ret.addAll(beCPGQueryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS).list());

		}
		return new AutoCompletePage(ret, pageNum, pageSize, new NodeRefAutoCompleteExtractor(propertyQNames, nodeService));
	}

	private AutoCompletePage suggestFromAssoc(String sourceType, List<NodeRef> dataListNodeRefs, NodeRef itemId, QName datalistType,
			QName associationQName, String query, Integer pageNum, Integer pageSize, boolean showFullPath) {

		List<AutoCompleteEntry> result = new ArrayList<>();
		for (NodeRef dataListNodeRef : dataListNodeRefs) {
			for (NodeRef dataListItemNodeRef : entityListDAO.getListItems(dataListNodeRef, datalistType)) {
				if (accepts(dataListItemNodeRef, itemId, sourceType)) {
					NodeRef targetNode = associationService.getTargetAssoc(dataListItemNodeRef, associationQName);

					if (targetNode != null) {
						QName type = nodeService.getType(targetNode);
						String name = attributeExtractorService.extractPropName(type, targetNode);

						if (isQueryMatch(query, name)) {
							String cssClass = attributeExtractorService.extractMetadata(type, targetNode);
						
							
							if (SOURCE_TYPE_DATA_LIST_CHARACT.equals(sourceType)) {
								result.add(new AutoCompleteEntry(targetNode.toString(), name, cssClass));
							} else {
								if(showFullPath) {
								   name = extractHierarchyFullName(dataListItemNodeRef,associationQName, new HashSet<>() );
								}
								
								result.add(new AutoCompleteEntry(dataListItemNodeRef.toString(), name, cssClass));
							}

						}
					}
				}
			}
		}
		return new AutoCompletePage(result, pageNum, pageSize, null);
	}

	private String extractHierarchyFullName(NodeRef dataListItemNodeRef, QName associationQName, Set<NodeRef> visited) {
		visited.add(dataListItemNodeRef);

		NodeRef targetNode = associationService.getTargetAssoc(dataListItemNodeRef, associationQName);
		String res = "";
		if (targetNode != null) {
			res = extractHierarchyName(targetNode);
			NodeRef parent = (NodeRef) nodeService.getProperty(dataListItemNodeRef, BeCPGModel.PROP_PARENT_LEVEL);
			if ((parent != null) && !visited.contains(parent)) {
				res = extractHierarchyFullName(parent, associationQName, visited) + " > " + res;
			}
		}
		return res;
	}

	private String extractHierarchyName(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		return attributeExtractorService.extractPropName(type, nodeRef);
	}

	private boolean accepts(NodeRef dataListItemNodeRef, NodeRef itemId, String sourceType) {

		if (dataListItemNodeRef.equals(itemId)) {
			return false;
		}

		if (SOURCE_TYPE_PARENT_VALUE.equals(sourceType)) {
			return !isChildOf(dataListItemNodeRef, itemId);
		}

		return true;
	}

	private boolean isChildOf(NodeRef dataListItemNodeRef, NodeRef itemId) {

		NodeRef parentNodeRef = (NodeRef) nodeService.getProperty(dataListItemNodeRef, BeCPGModel.PROP_PARENT_LEVEL);

		if (parentNodeRef != null) {
			if (parentNodeRef.equals(itemId)) {
				return true;
			}
			return isChildOf(parentNodeRef, itemId);
		}

		return false;
	}

}
