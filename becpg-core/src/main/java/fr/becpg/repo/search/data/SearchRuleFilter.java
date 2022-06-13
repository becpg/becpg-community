package fr.becpg.repo.search.data;

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

	private NamespaceService namespaceService;

	private static final String QUERY = "query";
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

	private QName dateField;
	private DateFilterType dateFilterType = DateFilterType.From;
	private Integer dateFilterDelay;
	private DateFilterDelayUnit dateFilterDelayUnit = DateFilterDelayUnit.DATE;
	private VersionFilterType versionFilterType = VersionFilterType.NONE;

	/**
	 * <p>Constructor for NotificationRuleFilter.</p>
	 */
	public SearchRuleFilter(NamespaceService namespaceService) {
		super();
		this.namespaceService = namespaceService;
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

	public QName getDateField() {
		return dateField;
	}

	public void setDateField(QName dateField) {
		this.dateField = dateField;
	}

	public VersionFilterType getVersionFilterType() {
		return versionFilterType;
	}

	public void setVersionFilterType(VersionFilterType versionFilterType) {
		this.versionFilterType = versionFilterType;
	}

	public Path getNodePath() {
		return nodePath;
	}

	public void setNodePath(Path nodePath) {
		this.nodePath = nodePath;
	}

	public DateFilterType getDateFilterType() {
		return dateFilterType;
	}

	public void setDateFilterType(DateFilterType dateFilterType) {
		this.dateFilterType = dateFilterType;
	}

	public Integer getDateFilterDelay() {
		return dateFilterDelay;
	}

	public void setDateFilterDelay(Integer dateFilterDelay) {
		this.dateFilterDelay = dateFilterDelay;
	}

	public DateFilterDelayUnit getDateFilterDelayUnit() {
		return dateFilterDelayUnit;
	}

	public void setDateFilterDelayUnit(DateFilterDelayUnit dateFilterDelayUnit) {
		this.dateFilterDelayUnit = dateFilterDelayUnit;
	}

	public SearchRuleFilter fromJsonString(String jsonString) {
		try {
			if ((jsonString != null) && !jsonString.isEmpty()) {
				JSONObject filterObject = new JSONObject(jsonString);
				if (filterObject.has(QUERY)) {
					setQuery(filterObject.getString(QUERY));
				}
				if (filterObject.has(ENTITY_FILTER)) {
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
					JSONObject versionFilter = filterObject.getJSONObject(VERSION_FILTER);
					if (versionFilter.has(VERSION_FILTER_TYPE)) {
						setVersionFilterType(VersionFilterType.valueOf(versionFilter.getString(VERSION_FILTER_TYPE)));
					}
				}

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

	@Override
	public int hashCode() {
		return Objects.hash(dateField, dateFilterDelay, dateFilterDelayUnit, dateFilterType, entityCriteria, entityType, nodeCriteria, nodePath,
				nodeType, query, versionFilterType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchRuleFilter other = (SearchRuleFilter) obj;
		return Objects.equals(dateField, other.dateField) && Objects.equals(dateFilterDelay, other.dateFilterDelay)
				&& dateFilterDelayUnit == other.dateFilterDelayUnit && dateFilterType == other.dateFilterType
				&& Objects.equals(entityCriteria, other.entityCriteria) && Objects.equals(entityType, other.entityType)
				&& Objects.equals(nodeCriteria, other.nodeCriteria) && Objects.equals(nodePath, other.nodePath)
				&& Objects.equals(nodeType, other.nodeType) && Objects.equals(query, other.query) && versionFilterType == other.versionFilterType;
	}

	@Override
	public String toString() {
		return "NotificationRuleFilter [query=" + query + ", nodeType=" + nodeType + ", nodePath=" + nodePath + ", nodeCriteria=" + nodeCriteria
				+ ", entityType=" + entityType + ", entityCriteria=" + entityCriteria + ", dateField=" + dateField + ", dateFilterType="
				+ dateFilterType + ", dateFilterDelay=" + dateFilterDelay + ", dateFilterDelayUnit=" + dateFilterDelayUnit + ", versionFilterType="
				+ versionFilterType + "]";
	}

}
