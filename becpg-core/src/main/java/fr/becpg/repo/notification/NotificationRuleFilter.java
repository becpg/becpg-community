package fr.becpg.repo.notification;

import java.util.Map;

import org.alfresco.service.namespace.QName;

public class NotificationRuleFilter {

	private String query = "";
	private QName nodeType;
	private QName entityType;
	private Map<String, String> nodeCriteria;
	private Map<String, String> entityCriteria;

	public NotificationRuleFilter() {
		super();
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public QName getEntityType() {
		return entityType;
	}

	public void setEntityType(QName entityType) {
		this.entityType = entityType;
	}

	public QName getNodeType() {
		return nodeType;
	}

	public void setNodeType(QName nodeType) {
		this.nodeType = nodeType;
	}

	public Map<String, String> getNodeCriteria() {
		return nodeCriteria;
	}

	public void setNodeCriteria(Map<String, String> nodeCriteria) {
		this.nodeCriteria = nodeCriteria;
	}

	public Map<String, String> getEntityCriteria() {
		return entityCriteria;
	}

	public void setEntityCriteria(Map<String, String> entityCriteria) {
		this.entityCriteria = entityCriteria;
	}

	@Override
	public String toString() {
		return "NotificationRuleFilter [query=" + query + ", nodeType=" + nodeType + ", entityType=" + entityType
				+ ", nodeCriteria=" + nodeCriteria + ", entityCriteria=" + entityCriteria + "]";
	}

	
	

}
