package fr.becpg.repo.audit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

	private List<String> inMemoryFilters = new ArrayList<>();

	private boolean asc = true;
	
	private boolean dbAsc = true;

	private int maxResults = RepoConsts.MAX_RESULTS_256;
	
    private Long fromId;
    
    private Long toId;
    
    private Date fromTime;

    private Date toTime;

    private Long startAfterId;

    /**
     * <p>Constructor for AuditQuery.</p>
     */
    private AuditQuery() {

    }

    /**
     * <p>copy.</p>
     *
     * A copy of this query, so that a caller can derive a query of its own without touching the
     * one it was given.
     *
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery copy() {
    	AuditQuery copy = new AuditQuery();
    	copy.sortBy = sortBy;
    	copy.filter = filter;
    	copy.inMemoryFilters = new ArrayList<>(inMemoryFilters);
    	copy.asc = asc;
    	copy.dbAsc = dbAsc;
    	copy.maxResults = maxResults;
    	copy.fromId = fromId;
    	copy.toId = toId;
    	copy.fromTime = fromTime;
    	copy.toTime = toTime;
    	copy.startAfterId = startAfterId;
    	return copy;
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
     * <p>startAfterId.</p>
     *
     * Resume a keyset paged scan right after the given entry identifier, in the database order the
     * query asks for.
     *
     * @param startAfterId a {@link java.lang.Long} object
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery startAfterId(Long startAfterId) {
    	this.startAfterId = startAfterId;
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
     * <p>filters.</p>
     *
     * The audit query of Alfresco matches a single key-value pair: the first filter is the one the
     * database reads with, the next ones are applied in memory on the entries it returns. The audit
     * service puts the most selective one first, following the filter hierarchy of the plugin.
     *
     * The given filters replace every filter of the query: an empty list leaves it unfiltered.
     *
     * @param filters the filters, each written "key=value", all of which an entry must match
     * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
     */
    public AuditQuery filters(List<String> filters) {
    	this.filter = filters.isEmpty() ? null : filters.get(0);
    	this.inMemoryFilters = filters.isEmpty() ? new ArrayList<>() : new ArrayList<>(filters.subList(1, filters.size()));
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
	 * <p>Getter for the field <code>startAfterId</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object
	 */
	public Long getStartAfterId() {
		return startAfterId;
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
	 * <p>getFilters.</p>
	 *
	 * Every filter of the query, the one the database reads with first.
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<String> getFilters() {
		List<String> filters = new ArrayList<>(inMemoryFilters.size() + 1);
		if (filter != null) {
			filters.add(filter);
		}
		filters.addAll(inMemoryFilters);
		return filters;
	}

	/**
	 * <p>Getter for the field <code>inMemoryFilters</code>.</p>
	 *
	 * The filters the entries read from the database still have to match.
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<String> getInMemoryFilters() {
		return Collections.unmodifiableList(inMemoryFilters);
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
