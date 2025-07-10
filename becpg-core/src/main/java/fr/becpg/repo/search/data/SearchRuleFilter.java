package fr.becpg.repo.search.data;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.helper.JsonHelper;

/**
 * <p>NotificationRuleFilter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SearchRuleFilter {

	private static final String PROP_QUERY = "query";

	private static final String PROP_IS_FILTER = "isFilter";
	private static final String PROP_EXCLUDE_PUBLISHED_ENTITIES = "excludePublishedEntities";
	private static final String ENTITY_FILTER = "entityFilter";
	private static final String ENTITY_TYPE = "entityType";

	private static final String NODE_FILTER = "nodeFilter";
	private static final String NODE_TYPE = "nodeType";
	private static final String NODE_PATH = "nodePath";
	private static final String CRITERIA = "criteria";

	private static final String DATE_FILTER = "dateFilter";
	private static final String DATE_FIELD = "dateField";
	private static final String DATE_FILTER_TYPE = "dateFilterType";
	private static final String DATE_FILTER_DELAY = "dateFilterDelay";
	private static final String DATE_FILTER_DELAY_UNIT = "dateFilterDelayUnit";

	private static final String VERSION_FILTER = "versionFilter";
	private static final String VERSION_FILTER_TYPE = "versionFilterType";

	private static Log logger = LogFactory.getLog(SearchRuleFilter.class);

	private String query = "";

	private QName nodeType;
	private Path nodePath;
	private Map<String, String> nodeCriteria;

	private QName entityType;
	private Map<String, String> entityCriteria;

	private Date currentDate;
	private QName dateField;

	private Integer dateFilterDelay;
	private DateFilterType dateFilterType = DateFilterType.From;
	private DateFilterDelayUnit dateFilterDelayUnit = DateFilterDelayUnit.DATE;
	private VersionFilterType versionFilterType = VersionFilterType.NONE;

	private Boolean ensureDbQuery = false;

	private Boolean isEmptyJsonQuery = true;

	private Boolean isFilter = false;
	
	private Boolean excludePublishedEntities = true;
	
	private Integer maxResults = null;
	
	/**
	 * <p>Setter for the field <code>maxResults</code>.</p>
	 *
	 * @param maxResults a {@link java.lang.Integer} object
	 */
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}
	
	/**
	 * <p>Getter for the field <code>maxResults</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getMaxResults() {
		return maxResults;
	}

	/**
	 * <p>Constructor for NotificationRuleFilter.</p>
	 */
	public SearchRuleFilter() {
		super();
	}

	/**
	 * <p>Getter for the field <code>query</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * <p>Setter for the field <code>query</code>.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * <p>Getter for the field <code>entityType</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getEntityType() {
		return entityType;
	}

	/**
	 * <p>Setter for the field <code>entityType</code>.</p>
	 *
	 * @param entityType a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setEntityType(QName entityType) {
		this.entityType = entityType;
	}

	/**
	 * <p>Getter for the field <code>nodeType</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getNodeType() {
		return nodeType;
	}

	/**
	 * <p>Setter for the field <code>nodeType</code>.</p>
	 *
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setNodeType(QName nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * <p>Getter for the field <code>nodeCriteria</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getNodeCriteria() {
		return nodeCriteria;
	}

	/**
	 * <p>Setter for the field <code>nodeCriteria</code>.</p>
	 *
	 * @param nodeCriteria a {@link java.util.Map} object.
	 */
	public void setNodeCriteria(Map<String, String> nodeCriteria) {
		this.nodeCriteria = nodeCriteria;
	}

	/**
	 * <p>Getter for the field <code>entityCriteria</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getEntityCriteria() {
		return entityCriteria;
	}

	/**
	 * <p>Setter for the field <code>entityCriteria</code>.</p>
	 *
	 * @param entityCriteria a {@link java.util.Map} object.
	 */
	public void setEntityCriteria(Map<String, String> entityCriteria) {
		this.entityCriteria = entityCriteria;
	}

	/**
	 * <p>Getter for the field <code>dateField</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object
	 */
	public QName getDateField() {
		return dateField;
	}

	/**
	 * <p>Setter for the field <code>dateField</code>.</p>
	 *
	 * @param dateField a {@link org.alfresco.service.namespace.QName} object
	 */
	public void setDateField(QName dateField) {
		this.dateField = dateField;
	}

	/**
	 * <p>Getter for the field <code>versionFilterType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.data.VersionFilterType} object
	 */
	public VersionFilterType getVersionFilterType() {
		return versionFilterType;
	}

	/**
	 * <p>Setter for the field <code>versionFilterType</code>.</p>
	 *
	 * @param versionFilterType a {@link fr.becpg.repo.search.data.VersionFilterType} object
	 */
	public void setVersionFilterType(VersionFilterType versionFilterType) {
		this.versionFilterType = versionFilterType;
	}

	/**
	 * <p>Getter for the field <code>nodePath</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.Path} object
	 */
	public Path getNodePath() {
		return nodePath;
	}

	/**
	 * <p>Setter for the field <code>nodePath</code>.</p>
	 *
	 * @param nodePath a {@link org.alfresco.service.cmr.repository.Path} object
	 */
	public void setNodePath(Path nodePath) {
		this.nodePath = nodePath;
	}

	/**
	 * <p>Getter for the field <code>dateFilterType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.data.DateFilterType} object
	 */
	public DateFilterType getDateFilterType() {
		return dateFilterType;
	}

	/**
	 * <p>Setter for the field <code>dateFilterType</code>.</p>
	 *
	 * @param dateFilterType a {@link fr.becpg.repo.search.data.DateFilterType} object
	 */
	public void setDateFilterType(DateFilterType dateFilterType) {
		this.dateFilterType = dateFilterType;
	}

	/**
	 * <p>Getter for the field <code>dateFilterDelay</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getDateFilterDelay() {
		return dateFilterDelay;
	}

	/**
	 * <p>Setter for the field <code>dateFilterDelay</code>.</p>
	 *
	 * @param dateFilterDelay a {@link java.lang.Integer} object
	 */
	public void setDateFilterDelay(Integer dateFilterDelay) {
		this.dateFilterDelay = dateFilterDelay;
	}

	/**
	 * <p>Getter for the field <code>dateFilterDelayUnit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.data.DateFilterDelayUnit} object
	 */
	public DateFilterDelayUnit getDateFilterDelayUnit() {
		return dateFilterDelayUnit;
	}

	/**
	 * <p>Setter for the field <code>dateFilterDelayUnit</code>.</p>
	 *
	 * @param dateFilterDelayUnit a {@link fr.becpg.repo.search.data.DateFilterDelayUnit} object
	 */
	public void setDateFilterDelayUnit(DateFilterDelayUnit dateFilterDelayUnit) {
		this.dateFilterDelayUnit = dateFilterDelayUnit;
	}

	/**
	 * <p>Getter for the field <code>currentDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	public Date getCurrentDate() {
		return currentDate;
	}

	/**
	 * <p>Setter for the field <code>currentDate</code>.</p>
	 *
	 * @param currentDate a {@link java.util.Date} object
	 */
	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	/**
	 * <p>Getter for the field <code>ensureDbQuery</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getEnsureDbQuery() {
		return ensureDbQuery;
	}

	/**
	 * <p>Setter for the field <code>ensureDbQuery</code>.</p>
	 *
	 * @param ensureDbQuery a {@link java.lang.Boolean} object
	 */
	public void setEnsureDbQuery(Boolean ensureDbQuery) {
		this.ensureDbQuery = ensureDbQuery;
	}

	/**
	 * <p>isEmptyJsonQuery.</p>
	 *
	 * @return a boolean
	 */
	public boolean isEmptyJsonQuery() {
		return isEmptyJsonQuery;
	}

	/**
	 * <p>isFilter.</p>
	 *
	 * @return a boolean
	 */
	public boolean isFilter() {
		return isFilter;
	}

	/**
	 * <p>Setter for the field <code>isFilter</code>.</p>
	 *
	 * @param isFilter a {@link java.lang.Boolean} object
	 */
	public void setIsFilter(Boolean isFilter) {
		this.isFilter = isFilter;
	}
	

	/**
	 * <p>excludePublishedEntities.</p>
	 *
	 * @return a boolean
	 */
	public boolean excludePublishedEntities() {
		return excludePublishedEntities;
	}

	/**
	 * <p>Setter for the field <code>excludePublishedEntities</code>.</p>
	 *
	 * @param excludePublishedEntities a {@link java.lang.Boolean} object
	 */
	public void setExcludePublishedEntities(Boolean excludePublishedEntities) {
		this.excludePublishedEntities = excludePublishedEntities;
	}

	/**
	 * <p>fromJsonString.</p>
	 *
	 * @param jsonString a {@link java.lang.String} object
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 * @return a {@link fr.becpg.repo.search.data.SearchRuleFilter} object
	 */
	public SearchRuleFilter fromJsonString(String jsonString, NamespaceService namespaceService) {
		try {
			if ((jsonString != null) && !jsonString.isEmpty()) {
				return fromJsonObject(new JSONObject(jsonString), namespaceService);
			}
		} catch (JSONException e) {
			if (jsonString.contains("{")) {
				logger.warn("Invalid JSON notification filter", e);
			} else {
				setQuery(jsonString);
			}
		}
		return this;
	}

	/**
	 * <p>fromJsonObject.</p>
	 *
	 * @param filterObject a {@link org.json.JSONObject} object
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 * @return a {@link fr.becpg.repo.search.data.SearchRuleFilter} object
	 */
	public SearchRuleFilter fromJsonObject(JSONObject filterObject, NamespaceService namespaceService) {
		if (filterObject.has(PROP_QUERY)) {
			setQuery(filterObject.getString(PROP_QUERY));
			isEmptyJsonQuery = false;
		}
		if (filterObject.has(PROP_IS_FILTER)) {
			setIsFilter(filterObject.getBoolean(PROP_IS_FILTER));
		}
		
		if (filterObject.has(PROP_EXCLUDE_PUBLISHED_ENTITIES)) {
			setExcludePublishedEntities(filterObject.getBoolean(PROP_EXCLUDE_PUBLISHED_ENTITIES));
		}
		
		if (filterObject.has(ENTITY_FILTER)) {
			isEmptyJsonQuery = false;
			JSONObject entityFilter = filterObject.getJSONObject(ENTITY_FILTER);
			if (entityFilter.has(ENTITY_TYPE)) {
				setEntityType(QName.createQName(entityFilter.getString(ENTITY_TYPE), namespaceService));
			}
			if (entityFilter.has(CRITERIA)) {
				JSONObject entityAssocs = entityFilter.getJSONObject(CRITERIA);
				setEntityCriteria(JsonHelper.extractCriteria(entityAssocs));
			}
		}
		if (filterObject.has(NODE_FILTER)) {
			isEmptyJsonQuery = false;
			JSONObject nodeFilter = filterObject.getJSONObject(NODE_FILTER);
			if (nodeFilter.has(NODE_TYPE)) {
				setNodeType(QName.createQName(nodeFilter.getString(NODE_TYPE), namespaceService));
			}

			if (nodeFilter.has(NODE_PATH)) {
				//TODOsetNodeType();
			}

			if (nodeFilter.has(CRITERIA)) {
				JSONObject nodeAssocs = nodeFilter.getJSONObject(CRITERIA);
				setNodeCriteria(JsonHelper.extractCriteria(nodeAssocs));
			}

		}

		if (filterObject.has(DATE_FILTER)) {
			isEmptyJsonQuery = false;
			JSONObject dateFilter = filterObject.getJSONObject(DATE_FILTER);

			if (dateFilter.has(DATE_FIELD)) {
				setDateField(QName.createQName(dateFilter.getString(DATE_FIELD), namespaceService));
			}

			if (dateFilter.has(DATE_FILTER_DELAY)) {
				setDateFilterDelay(dateFilter.getInt(DATE_FILTER_DELAY));
			}

			if (dateFilter.has(DATE_FILTER_DELAY_UNIT)) {
				setDateFilterDelayUnit(DateFilterDelayUnit.valueOf(dateFilter.getString(DATE_FILTER_DELAY_UNIT)));
			}
			if (dateFilter.has(DATE_FILTER_TYPE)) {
				setDateFilterType(DateFilterType.valueOf(dateFilter.getString(DATE_FILTER_TYPE)));
			}

		}

		if (filterObject.has(VERSION_FILTER)) {
			isEmptyJsonQuery = false;
			JSONObject versionFilter = filterObject.getJSONObject(VERSION_FILTER);
			if (versionFilter.has(VERSION_FILTER_TYPE)) {
				setVersionFilterType(VersionFilterType.valueOf(versionFilter.getString(VERSION_FILTER_TYPE)));
			}
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(currentDate, dateField, dateFilterDelay, dateFilterDelayUnit, dateFilterType, ensureDbQuery, entityCriteria, entityType,
				nodeCriteria, nodePath, nodeType, query, versionFilterType, maxResults);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		SearchRuleFilter other = (SearchRuleFilter) obj;
		return Objects.equals(currentDate, other.currentDate) && Objects.equals(dateField, other.dateField)
				&& Objects.equals(dateFilterDelay, other.dateFilterDelay) && (dateFilterDelayUnit == other.dateFilterDelayUnit)
				&& (dateFilterType == other.dateFilterType) && Objects.equals(ensureDbQuery, other.ensureDbQuery)
				&& Objects.equals(entityCriteria, other.entityCriteria) && Objects.equals(entityType, other.entityType)
				&& Objects.equals(nodeCriteria, other.nodeCriteria) && Objects.equals(nodePath, other.nodePath)
				&& Objects.equals(nodeType, other.nodeType) && Objects.equals(maxResults, other.maxResults) && Objects.equals(query, other.query)
				&& (versionFilterType == other.versionFilterType);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SearchRuleFilter [query=" + query + ", nodeType=" + nodeType + ", nodePath=" + nodePath + ", nodeCriteria=" + nodeCriteria
				+ ", entityType=" + entityType + ", entityCriteria=" + entityCriteria + ", currentDate=" + currentDate + ", dateField=" + dateField
				+ ", dateFilterType=" + dateFilterType + ", dateFilterDelay=" + dateFilterDelay + ", dateFilterDelayUnit=" + dateFilterDelayUnit
				+ ", versionFilterType=" + versionFilterType + ", ensureDbQuery=" + ensureDbQuery + ", maxResults=" + maxResults + "]";
	}

}
