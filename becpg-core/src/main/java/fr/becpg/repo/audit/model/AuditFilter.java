package fr.becpg.repo.audit.model;

import fr.becpg.repo.RepoConsts;

public class AuditFilter {

	private String sortBy;

	private String filter;

	private boolean ascendingOrder = true;

	private int maxResults = RepoConsts.MAX_RESULTS_256;

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public boolean isAscendingOrder() {
		return ascendingOrder;
	}

	public void setAscendingOrder(boolean ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}

}
