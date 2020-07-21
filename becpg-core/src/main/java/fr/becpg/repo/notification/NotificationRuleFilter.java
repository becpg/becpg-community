package fr.becpg.repo.notification;

import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * <p>NotificationRuleFilter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NotificationRuleFilter {

	private String query = "";
	private QName nodeType;
	private QName entityType;
	private Map<String, String> nodeCriteria;
	private Map<String, String> entityCriteria;

	/**
	 * <p>Constructor for NotificationRuleFilter.</p>
	 */
	public NotificationRuleFilter() {
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NotificationRuleFilter [query=" + query + ", nodeType=" + nodeType + ", entityType=" + entityType
				+ ", nodeCriteria=" + nodeCriteria + ", entityCriteria=" + entityCriteria + "]";
	}

	
	

}
