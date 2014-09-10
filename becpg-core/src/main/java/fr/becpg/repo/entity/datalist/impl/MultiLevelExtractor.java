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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public class MultiLevelExtractor extends SimpleExtractor {

	public static final String PROP_DEPTH = "depth";

	public static final String PROP_ENTITYNODEREF = "entityNodeRef";

	public static final String PROP_REVERSE_ASSOC = "reverseAssoc";
	
	public static final String PROP_ROOT_ENTITYNODEREF = "rootEntityNodeRef";

	MultiLevelDataListService multiLevelDataListService;

	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
	}

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess) {

		if (!dataListFilter.isDepthDefined()) {
			return super.extract(dataListFilter, metadataFields, pagination, hasWriteAccess);
		}

		int pageSize = pagination.getPageSize();
		int startIndex = (pagination.getPage() - 1) * pagination.getPageSize();

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pageSize);

		MultiLevelListData listData = multiLevelDataListService.getMultiLevelListData(dataListFilter);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, true); //TODO
		props.put(PROP_ROOT_ENTITYNODEREF, dataListFilter.getEntityNodeRef());

		appendNextLevel(ret, metadataFields, listData, 0, startIndex, pageSize, props, dataListFilter.getFormat());

		ret.setFullListSize(listData.getSize());
		return ret;
	}

	protected int appendNextLevel(PaginatedExtractedItems ret, List<String> metadataFields, MultiLevelListData listData, int currIndex, int startIndex, int pageSize,
			Map<String, Object> props, String format) {

		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			NodeRef nodeRef = entry.getKey();
			props.put(PROP_DEPTH, entry.getValue().getDepth());
			props.put(PROP_ENTITYNODEREF, entry.getValue().getEntityNodeRef());

			if (currIndex >= startIndex && currIndex < (startIndex + pageSize)) {
				if (ret.getComputedFields() == null) {
					ret.setComputedFields(attributeExtractorService.readExtractStructure(nodeService.getType(nodeRef), metadataFields));
				}

				if (RepoConsts.FORMAT_CSV.equals(format)
						|| RepoConsts.FORMAT_XLS.equals(format)) {
					ret.addItem(extractCSV(nodeRef, ret.getComputedFields(), props, cache));
				} else {
					ret.addItem(extractJSON(nodeRef, ret.getComputedFields(), props, cache));
				}
			} else if (currIndex >= (startIndex + pageSize)) {
				return currIndex;
			}
			currIndex = appendNextLevel(ret, metadataFields, entry.getValue(), currIndex + 1, startIndex, pageSize, props, format);
		}
		return currIndex;
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields, AttributeExtractorMode mode,
			Map<QName, Serializable> properties, Map<String, Object> extraProps, Map<NodeRef, Map<String, Object>> cache) {

		Map<String, Object> tmp = super.doExtract(nodeRef, itemType, metadataFields, mode, properties, extraProps, cache);

		if (AttributeExtractorMode.JSON.equals(mode)) {
			if (extraProps.get(PROP_DEPTH) != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> depth = (Map<String, Object>) tmp.get("prop_bcpg_depthLevel");
				if (depth == null) {
					depth = new HashMap<String, Object>();
				}

				Integer value = (Integer) extraProps.get(PROP_DEPTH);
				depth.put("value", value);
				depth.put("displayValue", value);

				tmp.put("prop_bcpg_depthLevel", depth);
			}
			
			if(extraProps.get(PROP_ROOT_ENTITYNODEREF)!=null){
				if(!extraProps.get(PROP_ROOT_ENTITYNODEREF).equals(entityListDAO.getEntity(nodeRef))){
					tmp.put("isMultiLevel",true); 
				}				
			}

			if (extraProps.get(PROP_ENTITYNODEREF) != null && extraProps.get(PROP_REVERSE_ASSOC) != null) {
				NodeRef entityNodeRef = (NodeRef) extraProps.get(PROP_ENTITYNODEREF);
				Map<String, Object> entity = new HashMap<String, Object>();
				entity.put("value", entityNodeRef);
				entity.put("displayValue", (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
				entity.put("metadata", attributeExtractorService.extractMetadata(nodeService.getType(entityNodeRef), entityNodeRef));
				String siteId = attributeExtractorService.extractSiteId(entityNodeRef);
				if (siteId != null) {
					entity.put("siteId", siteId);
				}

				String assocName = (String) extraProps.get(PROP_REVERSE_ASSOC);

				tmp.put("assoc_" + assocName.replaceFirst(":", "_"), entity);
			}
		} else if (AttributeExtractorMode.CSV.equals(mode)) {
			if (extraProps.get(PROP_DEPTH) != null) {
				tmp.put("prop_bcpg_depthLevel", ((Integer) extraProps.get(PROP_DEPTH)).toString());
			}

			if (extraProps.get(PROP_ENTITYNODEREF) != null && extraProps.get(PROP_REVERSE_ASSOC) != null) {
				NodeRef entityNodeRef = (NodeRef) extraProps.get(PROP_ENTITYNODEREF);
				String assocName = (String) extraProps.get(PROP_REVERSE_ASSOC);

				tmp.put( "assoc_" + assocName.replaceFirst(":", "_"), (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
			}
		}
		
		return tmp;
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && dataListFilter.getDataType() != null && entityDictionaryService.isMultiLevelDataList(dataListFilter.getDataType())
				&& !dataListFilter.getDataListName().startsWith(RepoConsts.WUSED_PREFIX);
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		if (!dataListFilter.isDepthDefined()) {
			return super.computeLastModified(dataListFilter);
		}
		return null;
	}

}
