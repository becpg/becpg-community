package fr.becpg.repo.audit.model;

import fr.becpg.repo.RepoConsts;

public class AuditQuery {

	private String sortBy;

	private String filter;

	private boolean order = true;

	private int maxResults = RepoConsts.MAX_RESULTS_256;
	
    private Long fromId;
    
    private Long toId;
    
    private String fromTime;
    
    private String toTime;
    
    private AuditQuery() {
    	
    }
    
    public static AuditQuery createQuery() {
    	return new AuditQuery();
    }
    
    public AuditQuery idRange(Long fromId, Long toId) {
    	this.fromId = fromId;
    	this.toId = toId;
    	return this;
    }
    
    public AuditQuery timeRange(String fromTime, String toTime) {
    	this.fromTime = fromTime;
    	this.toTime = toTime;
    	return this;
    }
    
    public AuditQuery sortBy(String sortBy) {
    	this.sortBy = sortBy;
    	return this;
    }
    
    public AuditQuery filter(String filter) {
    	this.filter = filter;
    	return this;
    }
    
    public AuditQuery order(boolean order) {
    	this.order = order;
    	return this;
    }
    
    public AuditQuery maxResults(int maxResults) {
    	this.maxResults = maxResults;
    	return this;
    }

	public Long getFromId() {
		return fromId;
	}

	public Long getToId() {
		return toId;
	}

	public String getFromTime() {
		return fromTime;
	}

	public String getToTime() {
		return toTime;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public String getSortBy() {
		return sortBy;
	}

	public String getFilter() {
		return filter;
	}

	public boolean isAscendingOrder() {
		return order;
	}

}
