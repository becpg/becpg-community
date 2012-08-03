package fr.becpg.repo.entity.datalist.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;

@Service
public class SimpleExtractor extends AbstractDataListExtractor {

	private FileFolderService fileFolderService;

	private DictionaryService dictionaryService;
	
	private static Log logger = LogFactory.getLog(SimpleExtractor.class);
	

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, hasWriteAccess);

		List<NodeRef> results = null;

		if (dataListFilter.isAllFilter()) {

			Collection<QName> qnames = dictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM, true);

			if(logger.isDebugEnabled()){
				logger.debug("DataType to filter :"+dataListFilter.getDataType());
			}
			
			Set<QName> ignoreTypeQNames = new HashSet<QName>(qnames.size());
			for (QName qname : qnames) {
				if (!qname.equals(dataListFilter.getDataType())) {
					if(logger.isDebugEnabled()){
						logger.debug("Add to ignore :"+qname);
					}
					ignoreTypeQNames.add(qname);
				}
			}

			int skipOffset = (pagination.getPage()-1) * pagination.getPageSize();
			int requestTotalCountMax = skipOffset + RepoConsts.MAX_RESULTS_1000;

			PagingRequest pageRequest = new PagingRequest(skipOffset, pagination.getPageSize(), pagination.getQueryExecutionId());
			pageRequest.setRequestTotalCountMax(requestTotalCountMax);

			PagingResults<FileInfo> pageOfNodeInfos = null;
			FileFilterMode.setClient(Client.script);
			try {

				pageOfNodeInfos = this.fileFolderService.list(dataListFilter.getDataListNodeRef(), true, false, null, ignoreTypeQNames, dataListFilter.getSortProps(), pageRequest);
			} finally {
				FileFilterMode.clearClient();
			}

			results = pagination.paginate(pageOfNodeInfos);

		} else {

			results = advSearchService.queryAdvSearch(dataListFilter.getSearchQuery(), SearchService.LANGUAGE_LUCENE, dataListFilter.getDataType(),
					dataListFilter.getCriteriaMap(), dataListFilter.getSortMap(), pagination.getMaxResults());

			results = pagination.paginate(results);

		}

		for (NodeRef nodeRef : results) {
			ret.getItems().add(extract(nodeRef, metadataFields, props));
		}

		ret.setFullListSize(pagination.getFullListSize());

		return ret;
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<String> metadataFields, Map<String, Object> props) {
		return attributeExtractorService.extractNodeData(nodeRef, itemType, metadataFields, false);
	}

}
