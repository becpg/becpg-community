package fr.becpg.repo.entity.datalist.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

public class MultiLevelExtractor extends SimpleExtractor {

	private static final String PROP_DEPTH = "depth";

	private static Log logger = LogFactory.getLog(MultiLevelExtractor.class);

	MultiLevelDataListService multiLevelDataListService;

	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
	}

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess) {

		if(!dataListFilter.isDepthDefined()){
			return super.extract(dataListFilter, metadataFields, pagination, hasWriteAccess);
		}
		
		int pageSize = pagination.getPageSize();
		int startIndex = (pagination.getPage() - 1) * pagination.getPageSize();

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pageSize);

		MultiLevelListData listData = multiLevelDataListService
				.getMultiLevelListData(dataListFilter);

		appendNextLevel(ret, metadataFields, listData, 0, startIndex, pageSize, hasWriteAccess);

		return ret;
	}

	private int appendNextLevel(PaginatedExtractedItems ret, List<String> metadataFields, MultiLevelListData listData, int currIndex, int startIndex, int pageSize,
			boolean hasWriteAccess) {
		logger.debug("appendNextLevel :" + currIndex);
		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			// if(currIndex>=startIndex && currIndex< (startIndex+pageSize)){
			NodeRef nodeRef = entry.getKey();
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(PROP_DEPTH, listData.getDepth()-1);
			props.put(PROP_ACCESSRIGHT, listData.getDepth()<=1 && hasWriteAccess);
			ret.getItems().add(extract(nodeRef, metadataFields, props));
			currIndex++;
			appendNextLevel(ret, metadataFields, entry.getValue(), currIndex, startIndex, pageSize, false);
			// } else {
			// currIndex++;
			// }
		}
		return currIndex;
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<String> metadataFields, Map<String, Object> props) {

		
		Map<String, Object> tmp = super.doExtract(nodeRef, itemType, metadataFields, props);
		if(props.get("depth")!=null){
			@SuppressWarnings("unchecked")
			Map<String, Object> depth = (Map<String, Object>) tmp.get("prop_bcpg_depthLevel");
			if (depth != null) {
				Integer value = (depth.get("value") != null ? (Integer) depth.get("value") : 0) + (Integer) props.get(PROP_DEPTH);
				
				depth.put("value", value);
				depth.put("displayValue", value);
			}
		}

		return tmp;
	}

}
