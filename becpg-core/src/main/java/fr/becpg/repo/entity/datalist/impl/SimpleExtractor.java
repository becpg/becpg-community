package fr.becpg.repo.entity.datalist.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;

public class SimpleExtractor extends AbstractDataListExtractor {


	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());

		List<NodeRef> results = advSearchService.queryAdvSearch(dataListFilter.getSearchQuery(), SearchService.LANGUAGE_LUCENE, dataListFilter.getDataType(),
				dataListFilter.getCriteriaMap(), dataListFilter.getSortMap(), pagination.getMaxResults());

		results =  pagination.paginate(results);
		
		Map<String,Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, hasWriteAccess);
		
		for (NodeRef nodeRef : results) {
			ret.getItems().add(extract(nodeRef, metadataFields, props));
		}

		ret.setFullListSize(pagination.getFullListSize());
		return ret;
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<String> metadataFields, Map<String,Object> props) {
		return attributeExtractorService.extractNodeData(nodeRef, itemType, metadataFields, false);
	}

}
