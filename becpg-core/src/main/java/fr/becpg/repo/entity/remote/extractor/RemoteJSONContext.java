package fr.becpg.repo.entity.remote.extractor;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * <p>RemoteJSONContext class.</p>
 *
 * @author matthieu
 */
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

	/**
	 * <p>Constructor for RemoteJSONContext.</p>
	 */
	public RemoteJSONContext() {
		super();
	}

	/**
	 * <p>Constructor for RemoteJSONContext.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
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

	/**
	 * <p>Getter for the field <code>entityNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	/**
	 * <p>Setter for the field <code>entityNodeRef</code>.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setEntityNodeRef(NodeRef entityNodeRef) {
		this.entityNodeRef = entityNodeRef;
	}

	/**
	 * <p>Getter for the field <code>currentNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getCurrentNodeRef() {
		return currentNodeRef;
	}

	/**
	 * <p>Setter for the field <code>currentNodeRef</code>.</p>
	 *
	 * @param currentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setCurrentNodeRef(NodeRef currentNodeRef) {
		this.currentNodeRef = currentNodeRef;
	}

	/**
	 * <p>Getter for the field <code>cache</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<NodeRef, NodeRef> getCache() {
		return cache;
	}
	
	/**
	 * <p>Getter for the field <code>entityPath</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getEntityPath() {
		return entityPath;
	}

	/**
	 * <p>Setter for the field <code>entityPath</code>.</p>
	 *
	 * @param entityPath a {@link java.lang.String} object
	 */
	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}

	/**
	 * <p>isRetry.</p>
	 *
	 * @return a boolean
	 */
	public boolean isRetry() {
		return retry;
	}

	/**
	 * <p>Setter for the field <code>retry</code>.</p>
	 *
	 * @param retry a boolean
	 */
	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	/**
	 * <p>isLastRetry.</p>
	 *
	 * @return a boolean
	 */
	public boolean isLastRetry() {
		return lastRetry;
	}

	/**
	 * <p>Setter for the field <code>lastRetry</code>.</p>
	 *
	 * @param lastRetry a boolean
	 */
	public void setLastRetry(boolean lastRetry) {
		this.lastRetry = lastRetry;
	}

}
