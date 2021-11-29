package fr.becpg.repo.entity.datalist;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * 
 * @author matthieu
 *
 */
public class AsyncPaginatedExtractorWrapper extends PaginatedExtractedItems  {

	private DataListExtractor extractor;
	private DataListFilter dataListFilter;
	private List<String> metadataFields;

	private int curPage = 1;

	protected static final int PAGE_SIZE = 100;

	public AsyncPaginatedExtractorWrapper(DataListExtractor extractor, DataListFilter dataListFilter, List<String> metadataFields) {

		this.extractor = extractor;
		this.dataListFilter = dataListFilter;
		this.metadataFields = metadataFields;
		getNextWork();
	}

	
	public List<Map<String, Object>> getNextWork() {

		try {
			return this.getPageItems();
		} finally {
			if(curPage == 1 || curPage * PAGE_SIZE <  this.fullListSize) {
				dataListFilter.getPagination().setPage(curPage++);
				dataListFilter.getPagination().setPageSize(PAGE_SIZE);			
				clone(extractor.extract(dataListFilter, metadataFields));
			} else {
				this.items = null;
			}
		}

	}

	private void clone(PaginatedExtractedItems extract) {
		this.fullListSize = extract.getFullListSize();
		this.computedFields = extract.getComputedFields();
		this.items = extract.getPageItems();
	}

	public DataListFilter getDataListFilter() {
		return dataListFilter;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(curPage, dataListFilter, extractor, metadataFields);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AsyncPaginatedExtractorWrapper other = (AsyncPaginatedExtractorWrapper) obj;
		return curPage == other.curPage && Objects.equals(dataListFilter, other.dataListFilter) && Objects.equals(extractor, other.extractor)
				&& Objects.equals(metadataFields, other.metadataFields);
	}


	
	
}
