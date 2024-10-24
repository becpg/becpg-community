package fr.becpg.repo.entity.datalist;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * <p>AsyncPaginatedExtractorWrapper class.</p>
 *
 * @author matthieu
 */
public class AsyncPaginatedExtractorWrapper extends PaginatedExtractedItems  {

	private DataListExtractor extractor;
	private DataListFilter dataListFilter;
	private int curPage = 1;

	/** Constant <code>PAGE_SIZE=100</code> */
	protected static final int PAGE_SIZE = 100;

	/**
	 * <p>Constructor for AsyncPaginatedExtractorWrapper.</p>
	 *
	 * @param extractor a {@link fr.becpg.repo.entity.datalist.DataListExtractor} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @param metadataFields a {@link java.util.List} object
	 */
	public AsyncPaginatedExtractorWrapper(DataListExtractor extractor, DataListFilter dataListFilter) {

		this.extractor = extractor;
		this.dataListFilter = dataListFilter;
		getNextWork();
	}

	
	/**
	 * <p>getNextWork.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<Map<String, Object>> getNextWork() {

		try {
			return this.getPageItems();
		} finally {
			int pages = ((this.fullListSize - 1) / PAGE_SIZE) + 1;
			if( curPage  <=  pages ) {
				dataListFilter.getPagination().setPage(curPage++);
				dataListFilter.getPagination().setPageSize(PAGE_SIZE);			
				clone(extractor.extract(dataListFilter));
			} else {
				this.items = null;
			}
		}

	}

	private void clone(PaginatedExtractedItems extract) {
		this.fullListSize = extract.getFullListSize();
		this.items = extract.getPageItems();
	}

	/**
	 * <p>Getter for the field <code>dataListFilter</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 */
	public DataListFilter getDataListFilter() {
		return dataListFilter;
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(curPage, dataListFilter, extractor);
		return result;
	}


	/** {@inheritDoc} */
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
				;
	}


	
	
}
