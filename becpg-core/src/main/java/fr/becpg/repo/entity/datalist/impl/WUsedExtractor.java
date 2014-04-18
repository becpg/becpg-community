/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.JSONHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * 
 * @author matthieu
 * 
 */
public class WUsedExtractor extends MultiLevelExtractor {

	private static Log logger = LogFactory.getLog(WUsedExtractor.class);

	private WUsedListService wUsedListService;

	private NamespaceService namespaceService;

	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination,
			boolean hasWriteAccess) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());

		QName associationName = null;

		if (dataListFilter.getDataListName() != null && dataListFilter.getDataListName().indexOf(RepoConsts.WUSED_SEPARATOR) > 0) {
			associationName = QName.createQName(dataListFilter.getDataListName().split(RepoConsts.WUSED_SEPARATOR)[1].replace("_", ":"), namespaceService);
		} else {
			associationName = entityDictionaryService.getDefaultPivotAssoc(dataListFilter.getDataType());
		}

		if (associationName == null) {
			logger.warn("No wUsed association name found for :" + dataListFilter.getDataType());
			return ret;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		String assocName = associationName.toPrefixString(namespaceService);

		props.put(PROP_ACCESSRIGHT, true); // TODO
		props.put(PROP_REVERSE_ASSOC, assocName);

		int pageSize = pagination.getPageSize();
		int startIndex = (pagination.getPage() - 1) * pagination.getPageSize();

		MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(getWusedNodeRefs(dataListFilter), getWUsedOperator(dataListFilter),
				associationName, dataListFilter.getMaxDepth());

		if (dataListFilter.getFilterId().equals(DataListFilter.FORM_FILTER)) {
			filter(dataListFilter, wUsedData);
		}

		appendNextLevel(ret, metadataFields, wUsedData, 0, startIndex, pageSize, props, dataListFilter.getFormat());

		ret.setFullListSize(wUsedData.getSize());

		return ret;

	}

	private WUsedOperator getWUsedOperator(DataListFilter dataListFilter) {
		if (dataListFilter.getExtraParams() != null && dataListFilter.getExtraParams().length() > 0) {
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

		if (ret == null || ret.isEmpty()) {
			if (dataListFilter.getExtraParams() != null && dataListFilter.getExtraParams().length() > 0) {
				try {
					JSONObject jsonObject = new JSONObject(dataListFilter.getExtraParams());
					if (jsonObject != null && jsonObject.has("searchQuery")) {
						JSONObject searchQuery = (JSONObject) jsonObject.get("searchQuery");
						if (searchQuery != null) {
							Map<String, String> criteriaMap = JSONHelper.extractCriteria(searchQuery);
							QName datatype = QName.createQName(searchQuery.getString("datatype"), namespaceService);

							BeCPGQueryBuilder queryBuilder = advSearchService.createSearchQuery(datatype, null, null, true, null, null);

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

	private Map<String, String> cleanCriteria(Map<String, String> criteriaMap) {
		Map<String, String> ret = new HashMap<>();

		for (String key : criteriaMap.keySet()) {
			if (criteriaMap.get(key) != null && !criteriaMap.get(key).isEmpty()) {
				if (!key.equals(DataListFilter.PROP_DEPTH_LEVEL)) {
					if (!key.startsWith("assoc_")) {
						ret.put(key.replace("prop_", "").replace("_", ":"), criteriaMap.get(key));
					} else if (key.endsWith("_added")) {
						ret.put(key.replace("assoc_", "").replace("_added", "").replace("_", ":"), criteriaMap.get(key));
					}
				}
			}
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	private void filter(DataListFilter dataListFilter, MultiLevelListData wUsedData) {
		Map<String, String> criteriaMap = cleanCriteria(dataListFilter.getCriteriaMap());
		if (!criteriaMap.isEmpty()) {
			for (Iterator<Entry<NodeRef, MultiLevelListData>> iterator = wUsedData.getTree().entrySet().iterator(); iterator.hasNext();) {
				Entry<NodeRef, MultiLevelListData> entry = iterator.next();
				NodeRef nodeRef = entry.getKey();

				Map<String, Object> comp = attributeExtractorService.extractNodeData(nodeRef, nodeService.getType(nodeRef), new ArrayList<>(
						criteriaMap.keySet()), AttributeExtractorMode.JSON);
				for (String key : comp.keySet()) {
					String critKey = key.replace("prop_", "").replace("assoc_", "").replace("_", ":");
					Map<String, Object> data = (Map<String, Object>) comp.get(key);
					if (data == null || data.get("value") == null || !data.get("value").toString().contains(criteriaMap.get(critKey))) {
						iterator.remove();
						break;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> extractJSON(NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields, Map<String, Object> props,
			Map<NodeRef, Map<String, Object>> cache) {
		Map<String, Object> ret = super.extractJSON(nodeRef, metadataFields, props, cache);

		Map<String, Object> permissions = (Map<String, Object>) ret.get(PROP_PERMISSIONS);
		Map<String, Boolean> userAccess = (Map<String, Boolean>) permissions.get("userAccess");

		userAccess.put("delete", userAccess.get("delete"));
		userAccess.put("create", false);
		userAccess.put("edit", userAccess.get("edit"));
		userAccess.put("sort", false);
		userAccess.put("details", false);

		ret.put(PROP_PERMISSIONS, permissions);

		return ret;
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && dataListFilter.getDataListName() != null
				&& dataListFilter.getDataListName().startsWith(RepoConsts.WUSED_PREFIX);
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
