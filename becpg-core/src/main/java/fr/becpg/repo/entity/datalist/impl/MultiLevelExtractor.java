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
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.format.FormatMode;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>MultiLevelExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MultiLevelExtractor extends SimpleExtractor {

	private static final Log logger = LogFactory.getLog(MultiLevelExtractor.class);

	/** Constant <code>PROP_DEPTH="depth"</code> */
	public static final String PROP_DEPTH = "depth";

	/** Constant <code>PROP_LEAF="isLeaf"</code> */
	public static final String PROP_LEAF = "isLeaf";

	/** Constant <code>PROP_OPEN="open"</code> */
	public static final String PROP_OPEN = "open";

	/** Constant <code>PROP_ENTITYNODEREF="entityNodeRef"</code> */
	public static final String PROP_ENTITYNODEREF = "entityNodeRef";

	/** Constant <code>PROP_REVERSE_ASSOC="reverseAssoc"</code> */
	public static final String PROP_REVERSE_ASSOC = "reverseAssoc";

	/** Constant <code>PROP_ROOT_ENTITYNODEREF="rootEntityNodeRef"</code> */
	public static final String PROP_ROOT_ENTITYNODEREF = "rootEntityNodeRef";

	/** Constant <code>PREF_DEPTH_PREFIX="fr.becpg.MultiLevelExtractor."</code> */
	public static final String PREF_DEPTH_PREFIX = "fr.becpg.MultiLevelExtractor.";

	/** Constant <code>PROP_IS_MULTI_LEVEL="isMultiLevel"</code> */
	public static final String PROP_IS_MULTI_LEVEL = "isMultiLevel";

	/** Constant <code>PROP_DISABLE_TREE="disableTree"</code> */
	public static final String PROP_DISABLE_TREE = "disableTree";

	MultiLevelDataListService multiLevelDataListService;

	PreferenceService preferenceService;

	/**
	 * <p>Setter for the field <code>preferenceService</code>.</p>
	 *
	 * @param preferenceService a {@link org.alfresco.service.cmr.preference.PreferenceService} object.
	 */
	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	/**
	 * <p>Setter for the field <code>multiLevelDataListService</code>.</p>
	 *
	 * @param multiLevelDataListService a {@link fr.becpg.repo.entity.datalist.MultiLevelDataListService} object.
	 */
	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
	}

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter) {

		boolean resetTree = false;

		if (!dataListFilter.isDepthDefined()) {
			int depth = getDepthUserPref(dataListFilter);
			dataListFilter.updateMaxDepth(depth);
		} else {
			resetTree = updateDepthUserPref(dataListFilter);
		}

		int pageSize = dataListFilter.getPagination().getPageSize();
		int startIndex = (dataListFilter.getPagination().getPage() - 1) * dataListFilter.getPagination().getPageSize();

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pageSize);

		MultiLevelListData listData = paginatedSearchCache.getSearchMultiLevelResults(dataListFilter.getPagination().getQueryExecutionId());
		if (listData == null) {
			listData = multiLevelDataListService.getMultiLevelListData(dataListFilter, true, resetTree);
			dataListFilter.getPagination().setQueryExecutionId(paginatedSearchCache.storeMultiLevelSearchResults(listData));
		}

		Map<String, Object> props = new HashMap<>();
		props.put(PROP_ROOT_ENTITYNODEREF, dataListFilter.getEntityNodeRef());
		props.put(PROP_PATH, "");

		appendNextLevel(ret, dataListFilter.getMetadataFields(), listData, 0, startIndex, pageSize, props, dataListFilter);

		ret.setFullListSize(listData.getSize());

		return ret;
	}

	/**
	 * <p>appendNextLevel.</p>
	 *
	 * @param ret a {@link fr.becpg.repo.entity.datalist.PaginatedExtractedItems} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param listData a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 * @param currIndex a int.
	 * @param startIndex a int.
	 * @param pageSize a int.
	 * @param props a {@link java.util.Map} object.
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @return a int.
	 */
	protected int appendNextLevel(PaginatedExtractedItems ret, List<AttributeExtractorField> metadataFields, MultiLevelListData listData, int currIndex,
			int startIndex, int pageSize, Map<String, Object> props, DataListFilter dataListFilter) {

		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

		String curPath = (String) props.get(PROP_PATH);

		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			NodeRef nodeRef = entry.getKey();
			NodeRef entityNodeRef = entry.getValue().getEntityNodeRef();
			props.put(PROP_DEPTH, entry.getValue().getDepth());
			props.put(PROP_ENTITYNODEREF, entityNodeRef);
			props.put(PROP_PATH, curPath + "/" + entry.getKey().getId());

			if ((currIndex >= startIndex) && (currIndex < (startIndex + pageSize))) {

				QName itemType = nodeService.getType(nodeRef);
				if (!props.containsKey(PROP_DISABLE_TREE)) {
					props.put(PROP_OPEN, false);
					props.put(PROP_LEAF, false);

					if (entry.getValue().isLeaf()) {
						props.put(PROP_LEAF, true);
					} else if (multiLevelDataListService.isExpandedNode(nodeRef, (!entry.getValue().getTree().isEmpty()
							|| (dataListFilter.getMaxDepth() < 0) || (entry.getValue().getDepth() < dataListFilter.getMaxDepth())), false)) {
						props.put(PROP_OPEN, true);
					}
				}

				if (!itemType.equals(dataListFilter.getDataType())) {
					props.put(PROP_ACCESSRIGHT, false);
				} else {
					props.put(PROP_ACCESSRIGHT, dataListFilter.hasWriteAccess());
				}
				if (AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(nodeRef))) {
					if (ret.getComputedFields() == null) {
						ret.setComputedFields(attributeExtractorService.readExtractStructure(dataListFilter.getDataType(), metadataFields));
					}
	
					if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
						ret.addItem(extractExport(RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat()) ? FormatMode.XLSX : FormatMode.CSV, nodeRef,
								ret.getComputedFields(), props, cache));
					} else {
						ret.addItem(extractJSON(nodeRef, ret.getComputedFields(), props, cache));
					}
				}
			} else if (currIndex >= (startIndex + pageSize)) {
				return currIndex;
			}
			currIndex = appendNextLevel(ret, metadataFields, entry.getValue(), currIndex + 1, startIndex, pageSize, props, dataListFilter);
		}
		return currIndex;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields, FormatMode mode,
			Map<QName, Serializable> properties, Map<String, Object> extraProps, Map<NodeRef, Map<String, Object>> cache) {

		Map<String, Object> tmp = super.doExtract(nodeRef, itemType, metadataFields, mode, properties, extraProps, cache);

		if (FormatMode.JSON.equals(mode) || FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) {
			if (extraProps.get(PROP_DEPTH) != null) {
				if (FormatMode.JSON.equals(mode)) {
					@SuppressWarnings("unchecked")
					Map<String, Object> depth = (Map<String, Object>) tmp.get("prop_bcpg_depthLevel");
					if (depth == null) {
						depth = new HashMap<>();
					}

					Integer value = (Integer) extraProps.get(PROP_DEPTH);
					depth.put("value", value);
					depth.put("displayValue", value);

					tmp.put("prop_bcpg_depthLevel", depth);
				} else {
					tmp.put("prop_bcpg_depthLevel", extraProps.get(PROP_DEPTH).toString());
				}
			}

			if (FormatMode.JSON.equals(mode)) {
				if (extraProps.get(PROP_LEAF) != null) {
					tmp.put(PROP_LEAF, extraProps.get(PROP_LEAF));
				}
	
				if (extraProps.get(PROP_OPEN) != null) {
					tmp.put(PROP_OPEN, extraProps.get(PROP_OPEN));
				}
			}

			if ((extraProps.get(PROP_ENTITYNODEREF) != null) && (extraProps.get(PROP_REVERSE_ASSOC) != null)) {
				NodeRef entityNodeRef = (NodeRef) extraProps.get(PROP_ENTITYNODEREF);
				String assocName = (String) extraProps.get(PROP_REVERSE_ASSOC);
				if (FormatMode.JSON.equals(mode)) {
					// TODO better if retrieved from cache
					tmp.put("assoc_" + assocName.replaceFirst(":", "_"), attributeExtractorService.extractCommonNodeData(entityNodeRef));
				} else {
					tmp.put("assoc_" + assocName.replaceFirst(":", "_"), attributeExtractorService.extractPropName(entityNodeRef));
				}
			}
			
			if ((extraProps.get(PROP_ROOT_ENTITYNODEREF) != null)
					&& !extraProps.get(PROP_ROOT_ENTITYNODEREF).equals(entityListDAO.getEntity(nodeRef))) {
				tmp.put(PROP_IS_MULTI_LEVEL, true);
				if (extraProps.get(PROP_PATH) != null) {
					tmp.put(PROP_PATH, extraProps.get(PROP_PATH));
				}
			}

		}

		return tmp;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && (dataListFilter.getDataType() != null)
				&& entityDictionaryService.isMultiLevelDataList(dataListFilter.getDataType())
				&& !dataListFilter.getDataListName().startsWith(RepoConsts.WUSED_PREFIX) 
				&& !dataListFilter.getDataListName().equals("projectList") // TODO better
				&& !dataListFilter.getDataListName().equals("ingList") // TODO better
				&& !dataListFilter.isVersionFilter();
	}

	/** {@inheritDoc} */
	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		if (!dataListFilter.isDepthDefined()) {
			return super.computeLastModified(dataListFilter);
		}
		return null;
	}

	private boolean updateDepthUserPref(DataListFilter dataListFilter) {
		String username = AuthenticationUtil.getFullyAuthenticatedUser();

		Map<String, Serializable> prefs = preferenceService.getPreferences(username);

		Integer depth = (Integer) prefs.get(PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName());
		if ((depth == null) || !depth.equals(dataListFilter.getMaxDepth())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Update (" + PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName() + "):" + dataListFilter.getMaxDepth()
						+ "  for " + username);
			}
			prefs.put(PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName(), dataListFilter.getMaxDepth());
			preferenceService.setPreferences(username, prefs);

			return true;
		}
		return false;

	}

	private int getDepthUserPref(DataListFilter dataListFilter) {
		String username = AuthenticationUtil.getFullyAuthenticatedUser();

		Map<String, Serializable> prefs = preferenceService.getPreferences(username);

		Integer depth = (Integer) prefs.get(PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName());
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Getting (" + PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName() + "):" + depth + " from history for " + username);
		}
		return depth != null ? depth : 0;
	}

}
