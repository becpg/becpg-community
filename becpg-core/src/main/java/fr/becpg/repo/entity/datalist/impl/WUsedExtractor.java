package fr.becpg.repo.entity.datalist.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

/**
 * 
 * @author matthieu
 *
 */
public class WUsedExtractor extends MultiLevelExtractor {
	
	

	private WUsedListService wUsedListService;

	
	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}


	NamespaceService namespaceService;
	
	
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess) {

		//TODO filter with query filter
		
		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());
		
		QName associationName = BeCPGModel.ASSOC_COMPOLIST_PRODUCT;
		
		 if (BeCPGModel.TYPE_PACKAGINGLIST.equals(dataListFilter.getDataType())){
			 associationName =  BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT;
		 }
		 
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, false);
		props.put(PROP_REVERSE_ASSOC, associationName.toPrefixString(namespaceService));
		 
		 Iterator<String> it  = metadataFields.iterator();
		 while (it.hasNext()) {
			String propName = (String) it.next();
			if(propName.equals(props.get(PROP_REVERSE_ASSOC))){
				it.remove();
			}
		}
		 

		int pageSize = pagination.getPageSize();
		int startIndex = (pagination.getPage() - 1) * pagination.getPageSize();
	
		
		MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(dataListFilter.getEntityNodeRef(), associationName, dataListFilter.getMaxDepth());
		
		appendNextLevel(ret, metadataFields, wUsedData, 0, startIndex, pageSize, props);

		ret.setFullListSize(wUsedData.getSize());
		return ret;
		
	}
	

	

}
