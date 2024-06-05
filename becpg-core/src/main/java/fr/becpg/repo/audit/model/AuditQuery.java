package fr.becpg.repo.audit.model;

import java.util.Date;

import fr.becpg.repo.RepoConsts;

/**
 * <p>AuditQuery class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
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
    
    /**
     * <p>createQuery.</p>
     *
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public static AuditQuery createQuery() {
    	return new AuditQuery();
    }
    
    /**
     * <p>idRange.</p>
     *
     * @param fromId a {@link java.lang.Long} object
     * @param toId a {@link java.lang.Long} object
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery idRange(Long fromId, Long toId) {
    	this.fromId = fromId;
    	this.toId = toId;
    	return this;
    }
    
    /**
     * <p>timeRange.</p>
     *
     * @param fromTime a {@link java.util.Date} object
     * @param toTime a {@link java.util.Date} object
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery timeRange(Date fromTime, Date toTime) {
    	this.fromTime = fromTime;
    	this.toTime = toTime;
    	return this;
    }
    
    /**
     * <p>sortBy.</p>
     *
     * @param sortBy a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery sortBy(String sortBy) {
    	this.sortBy = sortBy;
    	return this;
    }
    
    /**
     * <p>filter.</p>
     *
     * @param key a {@link java.lang.String} object
     * @param value a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery filter(String key, String value) {
    	this.filter = key + "=" + value;
    	return this;
    }
    
    /**
     * <p>filter.</p>
     *
     * @param filter a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery filter(String filter) {
    	this.filter = filter;
    	return this;
    }
    
    /**
     * <p>dbAsc.</p>
     *
     * @param dbAsc a boolean
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery dbAsc(boolean dbAsc) {
    	this.dbAsc = dbAsc;
    	return this;
    }
    
    /**
     * <p>asc.</p>
     *
     * @param asc a boolean
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery asc(boolean asc) {
    	this.asc = asc;
    	return this;
    }
    
    /**
     * <p>maxResults.</p>
     *
     * @param maxResults a int
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery maxResults(int maxResults) {
    	this.maxResults = maxResults;
    	return this;
    }
    
	/**
	 * <p>isDbAscending.</p>
	 *
	 * @return a boolean
	 */
	public boolean isDbAscending() {
		return dbAsc;
	}

	/**
	 * <p>Getter for the field <code>fromId</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object
	 */
	public Long getFromId() {
		return fromId;
	}

	/**
	 * <p>Getter for the field <code>toId</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object
	 */
	public Long getToId() {
		return toId;
	}

	/**
	 * <p>Getter for the field <code>fromTime</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	public Date getFromTime() {
		return fromTime;
	}

	/**
	 * <p>Getter for the field <code>toTime</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	public Date getToTime() {
		return toTime;
	}

	/**
	 * <p>Getter for the field <code>maxResults</code>.</p>
	 *
	 * @return a int
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/**
	 * <p>Getter for the field <code>sortBy</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSortBy() {
		return sortBy;
	}

	/**
	 * <p>Getter for the field <code>filter</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * <p>isAscending.</p>
	 *
	 * @return a boolean
	 */
	public boolean isAscending() {
		return asc;
	}

}
