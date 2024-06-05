package fr.becpg.repo.decernis.model;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>UsageContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UsageContext {

	private String name;
	
	private NodeRef nodeRef;
	
	private Integer moduleId;

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>Getter for the field <code>nodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * <p>Setter for the field <code>nodeRef</code>.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * <p>Getter for the field <code>moduleId</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getModuleId() {
		return moduleId;
	}

	/**
	 * <p>Setter for the field <code>moduleId</code>.</p>
	 *
	 * @param moduleId a {@link java.lang.Integer} object
	 */
	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}
	
}
