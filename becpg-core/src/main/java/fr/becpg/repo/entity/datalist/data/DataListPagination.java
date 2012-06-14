package fr.becpg.repo.entity.datalist.data;

import java.util.List;

import fr.becpg.repo.RepoConsts;

/**
 * DataList Pagination container
 * 
 * @author matthieu
 * 
 */
public class DataListPagination {

	private int maxResults;

	private int pageSize;

	private int fullListSize;

	private int page;

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		if (maxResults == null) {
			maxResults = -1;
		}
		this.maxResults = maxResults;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		if (pageSize == null) {
			pageSize = RepoConsts.DATA_LISTS_PAGESIZE;
		}
		this.pageSize = pageSize;
	}

	public int getFullListSize() {
		return fullListSize;
	}

	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(Integer page) {
		if (page == null) {
			page = 1;
		}
		this.page = page;
	}

	public <T> List<T> paginate(List<T> list) {
		if (list != null) {
			fullListSize = list.size();

			// Pagination
			if (fullListSize > 0) {
				list = list.subList(Math.max((getPage() - 1) * getPageSize(), 0), Math.min(getPage() * getPageSize(), fullListSize));
			}
		}
		return list;
	}

	@Override
	public String toString() {
		return "DataListPagination [maxResults=" + maxResults + ", pageSize=" + pageSize + ", fullListSize=" + fullListSize + ", page=" + page + "]";
	}

	
	
}
