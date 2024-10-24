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
package fr.becpg.repo.entity.datalist.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.WUsedFilter;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.NestedAdvSearchPlugin;

/**
 * <p>
 * WUsedExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class WUsedExtractor extends MultiLevelExtractor {

	private static final Log logger = LogFactory.getLog(WUsedExtractor.class);

	private WUsedListService wUsedListService;

	private NamespaceService namespaceService;

	private NestedAdvSearchPlugin nestedAdvSearchPlugin;

	/**
	 * <p>
	 * Setter for the field <code>wUsedListService</code>.
	 * </p>
	 *
	 * @param wUsedListService
	 *            a {@link fr.becpg.repo.entity.datalist.WUsedListService}
	 *            object.
	 */
	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	/**
	 * <p>
	 * Setter for the field <code>namespaceService</code>.
	 * </p>
	 *
	 * @param namespaceService
	 *            a {@link org.alfresco.service.namespace.NamespaceService}
	 *            object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>
	 * Setter for the field <code>nestedAdvSearchPlugin</code>.
	 * </p>
	 *
	 * @param nestedAdvSearchPlugin
	 *            a {@link fr.becpg.repo.search.impl.NestedAdvSearchPlugin}
	 *            object.
	 */
	public void setNestedAdvSearchPlugin(NestedAdvSearchPlugin nestedAdvSearchPlugin) {
		this.nestedAdvSearchPlugin = nestedAdvSearchPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		QName associationName = null;

		if ((dataListFilter.getDataListName() != null) && (dataListFilter.getDataListName().indexOf(RepoConsts.WUSED_SEPARATOR) >= 0)) {
			associationName = QName.createQName(dataListFilter.getDataListName().split(RepoConsts.WUSED_SEPARATOR)[1].replace("_", ":"),
					namespaceService);
		} else {
			associationName = entityDictionaryService.getDefaultPivotAssoc(dataListFilter.getDataType());
		}

		if (associationName == null) {
			logger.warn("No wUsed association name found for :" + dataListFilter.getDataType());
			return ret;
		}

		Map<String, Object> props = new HashMap<>();
		String assocName = associationName.toPrefixString(namespaceService);

		// Rights are being checked latter
		dataListFilter.setHasWriteAccess(true);
		props.put(PROP_ACCESSRIGHT, true);
		props.put(PROP_REVERSE_ASSOC, assocName);
		props.put(PROP_DISABLE_TREE, true);

		int pageSize = dataListFilter.getPagination().getPageSize();
		int startIndex = (dataListFilter.getPagination().getPage() - 1) * dataListFilter.getPagination().getPageSize();

		MultiLevelListData wUsedData = paginatedSearchCache.getSearchMultiLevelResults(dataListFilter.getPagination().getQueryExecutionId());

		if (wUsedData == null) {

			wUsedData = wUsedListService.getWUsedEntity(getWusedNodeRefs(dataListFilter), getWUsedOperator(dataListFilter),
					getWUsedFilter(dataListFilter, associationName), associationName, dataListFilter.getMaxDepth());

			dataListFilter.getPagination().setQueryExecutionId(paginatedSearchCache.storeMultiLevelSearchResults(wUsedData));

		}

		appendNextLevel(ret, dataListFilter.getMetadataFields(), wUsedData, 0, startIndex, pageSize, props, dataListFilter);

		ret.setFullListSize(wUsedData.getSize());

		return ret;

	}

	private WUsedFilter getWUsedFilter(final DataListFilter dataListFilter, final QName reverseAssociationName) {
		return new WUsedFilter() {

			@Override
			public void filter(MultiLevelListData wUsedData) {

				if ((dataListFilter.getExtraParams() != null) && (dataListFilter.getExtraParams().length() > 0)) {
					try {
						JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
						if (jsonObject.has("typeFilter")) {
							String typeFilter = (String) jsonObject.get("typeFilter");
							if ((typeFilter != null) && !typeFilter.isEmpty() && !"all".equalsIgnoreCase(typeFilter)) {

								QName type = QName.createQName(typeFilter, namespaceService);
								for (Iterator<Entry<NodeRef, MultiLevelListData>> iterator = wUsedData.getTree().entrySet().iterator(); iterator
										.hasNext();) {
									Entry<NodeRef, MultiLevelListData> entry = iterator.next();
									NodeRef nodeRef = entry.getValue().getEntityNodeRef();
									if (!type.equals(nodeService.getType(nodeRef))) {
										iterator.remove();
									}
								}
							}
						}

					} catch (JSONException e) {
						logger.error(e);
					}
				}

				if (dataListFilter.getFilterId().equals(DataListFilter.FORM_FILTER)) {
					Map<String, String> criteriaMap = nestedAdvSearchPlugin.cleanCriteria(dataListFilter.getCriteriaMap());

					if (!criteriaMap.isEmpty()) {
						for (Iterator<Entry<NodeRef, MultiLevelListData>> iterator = wUsedData.getTree().entrySet().iterator(); iterator.hasNext();) {
							Entry<NodeRef, MultiLevelListData> entry = iterator.next();
							NodeRef nodeRef = entry.getKey();

							if (!nestedAdvSearchPlugin.match(nodeRef, criteriaMap)) {
								iterator.remove();
							}
						}
					}

					Map<String, Map<String, String>> nested = nestedAdvSearchPlugin.extractNested(dataListFilter.getCriteriaMap());

					if (!nested.isEmpty()) {

						for (Map.Entry<String, Map<String, String>> nestedEntry : nested.entrySet()) {
							String assocName = nestedEntry.getKey();
							QName assocQName = QName.createQName(assocName, namespaceService);
							criteriaMap = nestedAdvSearchPlugin.cleanCriteria(nestedEntry.getValue());

							for (Iterator<Entry<NodeRef, MultiLevelListData>> iterator = wUsedData.getTree().entrySet().iterator(); iterator
									.hasNext();) {
								Entry<NodeRef, MultiLevelListData> entry = iterator.next();
								boolean foundMatch = false;

								if (assocQName.equals(reverseAssociationName)) {
									foundMatch = nestedAdvSearchPlugin.match(entry.getValue().getEntityNodeRef(), criteriaMap);
								} else {

									NodeRef nodeRef = entry.getKey();

									List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocQName);

									for (AssociationRef assocRef : assocRefs) {
										if (nestedAdvSearchPlugin.match(assocRef.getTargetRef(), criteriaMap)) {
											foundMatch = true;
										}
									}

								}

								if (!foundMatch) {
									iterator.remove();
								}
							}
						}

					}
				}
			}

			@Override
			public WUsedFilterKind getFilterKind() {
				return WUsedFilterKind.STANDARD;
			}

		};
	}

	private WUsedOperator getWUsedOperator(DataListFilter dataListFilter) {
		if ((dataListFilter.getExtraParams() != null) && (dataListFilter.getExtraParams().length() > 0)) {
			try {
				JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
				if (jsonObject.has("operator")) {
					return Enum.valueOf(WUsedOperator.class, (String) jsonObject.get("operator"));
				}

			} catch (JSONException e) {
				logger.error(e);
			}
		}
		return WUsedOperator.AND;
	}

	private List<NodeRef> getWusedNodeRefs(DataListFilter dataListFilter) {

		List<NodeRef> ret = dataListFilter.getEntityNodeRefs();

		if ((ret == null) || ret.isEmpty()) {
			if ((dataListFilter.getExtraParams() != null) && (dataListFilter.getExtraParams().length() > 0)) {
				try {
					JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
					if ( jsonObject.has("searchQuery")) {
						JSONObject searchQuery = (JSONObject) jsonObject.get("searchQuery");
						String searchTerm = (String) jsonObject.get("searchTerm");
						if (searchQuery != null) {
							Map<String, String> criteriaMap = JsonHelper.extractCriteria(searchQuery);
							QName datatype = QName.createQName(searchQuery.getString("datatype"), namespaceService);

							BeCPGQueryBuilder queryBuilder = advSearchService.createSearchQuery(datatype, searchTerm, null, true, null, null);

							ret = advSearchService.queryAdvSearch(datatype, queryBuilder, criteriaMap, RepoConsts.MAX_RESULTS_256);
						}
					}

				} catch (JSONException e) {
					logger.error(e);
				}
			}

		}
		return ret;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> extractJSON(NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields, Map<String, Object> props,
			Map<NodeRef, Map<String, Object>> cache) {
		Map<String, Object> ret = super.extractJSON(nodeRef, metadataFields, props, cache);

		Map<String, Object> permissions = (Map<String, Object>) ret.get(PROP_PERMISSIONS);
		Map<String, Boolean> userAccess = (Map<String, Boolean>) permissions.get(PROP_USERACCESS);

		userAccess.put("delete", userAccess.get("delete"));
		userAccess.put("create", false);
		
		//TODO permissions not checked for dataLists
		
		userAccess.put("edit", userAccess.get("edit"));
		userAccess.put("sort", false);
		userAccess.put("details", false);
		userAccess.put("wused", true);

		ret.put(PROP_PERMISSIONS, permissions);

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && (dataListFilter.getDataListName() != null)
				&& dataListFilter.getDataListName().startsWith(RepoConsts.WUSED_PREFIX);
	}

	/** {@inheritDoc} */
	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
