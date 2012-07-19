package fr.becpg.repo.entity.datalist.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

@Service
public class MultiLevelExtractor extends SimpleExtractor {

	public static final String PROP_DEPTH = "depth";

	public static final String PROP_ENTITYNODEREF = "entityNodeRef";

	public static final String PROP_REVERSE_ASSOC = "reverseAssoc";
	
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

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, hasWriteAccess);
		
		appendNextLevel(ret, metadataFields, listData, 0, startIndex, pageSize, props);


		ret.setFullListSize(listData.getSize());
		return ret;
	}

	protected int appendNextLevel(PaginatedExtractedItems ret, List<String> metadataFields, MultiLevelListData listData, int currIndex, int startIndex, int pageSize,
			Map<String, Object> props) {
		logger.debug("appendNextLevel :" + currIndex);
		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			NodeRef nodeRef = entry.getKey();
			props.put(PROP_DEPTH, entry.getValue().getDepth());
			props.put(PROP_ENTITYNODEREF, entry.getValue().getEntityNodeRef());
			props.put(PROP_ACCESSRIGHT, false);
			if(currIndex>=startIndex && currIndex< (startIndex+pageSize)){
				ret.getItems().add(extract(nodeRef, metadataFields, props));
			} else if(currIndex >= (startIndex+pageSize)){
				return currIndex;
			}
			currIndex = appendNextLevel(ret, metadataFields, entry.getValue(), currIndex+1, startIndex, pageSize, props);
		}
		return currIndex;
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<String> metadataFields, Map<String, Object> props) {

		Map<String, Object> tmp = super.doExtract(nodeRef, itemType, metadataFields, props);
		if(props.get(PROP_DEPTH)!=null){
			@SuppressWarnings("unchecked")
			Map<String, Object> depth = (Map<String, Object>) tmp.get("prop_bcpg_depthLevel");
			if (depth == null) {
				depth = new HashMap<String, Object>();
			}
			
			Integer value = (Integer) props.get(PROP_DEPTH);
			depth.put("value", value);
			depth.put("displayValue", value);
			
			tmp.put("prop_bcpg_depthLevel", depth);
			
		}
		
		if(props.get(PROP_ENTITYNODEREF)!=null && props.get(PROP_REVERSE_ASSOC)!=null){
			NodeRef entityNodeRef  = (NodeRef) props.get(PROP_ENTITYNODEREF);
			Map<String, Object> entity = new HashMap<String, Object>();
			entity.put("value",entityNodeRef);
			entity.put("displayValue",(String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
			entity.put("metadata", attributeExtractorService.extractMetadata(nodeService.getType(entityNodeRef), entityNodeRef));
			String siteId = attributeExtractorService.extractSiteId(entityNodeRef);
			if(siteId!=null){
				entity.put("siteId",siteId);
			}
			
			String assocName  = (String) props.get(PROP_REVERSE_ASSOC);	
			
			tmp.put("assoc_"+assocName.replaceFirst(":", "_"), entity);
		}
		
		

		return tmp;
	}

}
