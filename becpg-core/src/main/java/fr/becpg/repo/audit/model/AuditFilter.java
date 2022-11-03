package fr.becpg.repo.audit.model;

public class AuditFilter {
	
	private String sortBy;

	private String filter;

	private boolean ascendingOrder = true;

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
