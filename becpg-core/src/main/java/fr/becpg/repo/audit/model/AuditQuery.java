package fr.becpg.repo.audit.model;

import java.util.Date;

import fr.becpg.repo.RepoConsts;

public class AuditQuery {

	private String sortBy;

	private String filter;

	private boolean asc = true;
	
	private boolean dbAsc = true;

	private int maxResults = RepoConsts.MAX_RESULTS_256;
	
    private Long fromId;
    
    private Long toId;
    
    private Date fromTime;
    
    private Date toTime;
    
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
    
    public AuditQuery timeRange(Date fromTime, Date toTime) {
    	this.fromTime = fromTime;
    	this.toTime = toTime;
    	return this;
    }
    
    public AuditQuery sortBy(String sortBy) {
    	this.sortBy = sortBy;
    	return this;
    }
    
    public AuditQuery filter(String key, String value) {
    	this.filter = key + "=" + value;
    	return this;
    }
    
    public AuditQuery filter(String filter) {
    	this.filter = filter;
    	return this;
    }
    
    public AuditQuery dbAsc(boolean dbAsc) {
    	this.dbAsc = dbAsc;
    	return this;
    }
    
    public AuditQuery asc(boolean asc) {
    	this.asc = asc;
    	return this;
    }
    
    public AuditQuery maxResults(int maxResults) {
    	this.maxResults = maxResults;
    	return this;
    }
    
	public boolean isDbAscending() {
		return dbAsc;
	}

	public Long getFromId() {
		return fromId;
	}

	public Long getToId() {
		return toId;
	}

	public Date getFromTime() {
		return fromTime;
	}

	public Date getToTime() {
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

	public boolean isAscending() {
		return asc;
	}

}
