package fr.becpg.repo.entity.remote.extractor;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;

public class RemoteJSONContext {

	public enum JsonVisitNodeType {
		ENTITY, ENTITY_LIST, CONTENT, ASSOC, DATALIST, CHILD_ASSOC
	}

	private Map<NodeRef, NodeRef> cache = new HashMap<>();

	private String entityPath = null;

	private NodeRef entityNodeRef = null;

	private NodeRef currentNodeRef = null;

	private boolean retry = true;
	private boolean lastRetry = false;

	public RemoteJSONContext() {
		super();
	}

	public RemoteJSONContext(NodeRef entityNodeRef) {
		super();
		this.entityNodeRef = entityNodeRef;
	}

	String getEntityPath(NodeService nodeService, NamespaceService namespaceService) {
		if ((entityPath == null) && (entityNodeRef != null)) {
			entityPath = nodeService.getPath(entityNodeRef).toPrefixString(namespaceService);
		}
		return entityPath;
	}

	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public void setEntityNodeRef(NodeRef entityNodeRef) {
		this.entityNodeRef = entityNodeRef;
	}

	public NodeRef getCurrentNodeRef() {
		return currentNodeRef;
	}

	public void setCurrentNodeRef(NodeRef currentNodeRef) {
		this.currentNodeRef = currentNodeRef;
	}

	public Map<NodeRef, NodeRef> getCache() {
		return cache;
	}
	
	public String getEntityPath() {
		return entityPath;
	}

	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public boolean isLastRetry() {
		return lastRetry;
	}

	public void setLastRetry(boolean lastRetry) {
		this.lastRetry = lastRetry;
	}

}
