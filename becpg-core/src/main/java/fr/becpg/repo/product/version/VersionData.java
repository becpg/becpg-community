/*
 * 
 */
package fr.becpg.repo.product.version;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class VersionData.
 *
 * @author querephi
 */
public class VersionData {

	/** The node ref. */
	private NodeRef nodeRef;		
	
	/** The name. */
	private String name;
	
	/** The label. */
	private String label;
	
	/** The description. */
	private String description;
	
	/** The created date. */
	private Date createdDate;
	
	/** The creator user name. */
	private String creatorUserName;
	
	/** The creator first name. */
	private String creatorFirstName;
	
	/** The creator last name. */
	private String creatorLastName;
	
	/**
	 * Instantiates a new version data.
	 */
	public VersionData(){
		
	}
	
	/**
	 * Instantiates a new version data.
	 *
	 * @param nodeRef the node ref
	 * @param name the name
	 * @param label the label
	 * @param description the description
	 * @param createdDate the created date
	 * @param creatorUserName the creator user name
	 * @param creatorFirstName the creator first name
	 * @param creatorLastName the creator last name
	 */
	public VersionData(NodeRef nodeRef, String name, String label, String description, Date createdDate, String creatorUserName, String creatorFirstName, String creatorLastName){
		this.setNodeRef(nodeRef);
		this.setName(name);
		this.setLabel(label);
		this.setDescription(description);
		this.setCreatedDate(createdDate);
		this.setCreatorUserName(creatorUserName);
		this.setCreatorFirstName(creatorFirstName);
		this.setCreatorLastName(creatorLastName);
	}
	
	/**
	 * Gets the node ref.
	 *
	 * @return the node ref
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	
	/**
	 * Sets the node ref.
	 *
	 * @param nodeRef the new node ref
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the created date.
	 *
	 * @return the created date
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	
	/**
	 * Sets the created date.
	 *
	 * @param createdDate the new created date
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	/**
	 * Gets the creator user name.
	 *
	 * @return the creator user name
	 */
	public String getCreatorUserName() {
		return creatorUserName;
	}
	
	/**
	 * Sets the creator user name.
	 *
	 * @param creatorUserName the new creator user name
	 */
	public void setCreatorUserName(String creatorUserName) {
		this.creatorUserName = creatorUserName;
	}
	
	/**
	 * Gets the creator first name.
	 *
	 * @return the creator first name
	 */
	public String getCreatorFirstName() {
		return creatorFirstName;
	}
	
	/**
	 * Sets the creator first name.
	 *
	 * @param creatorFirstName the new creator first name
	 */
	public void setCreatorFirstName(String creatorFirstName) {
		this.creatorFirstName = creatorFirstName;
	}
	
	/**
	 * Gets the creator last name.
	 *
	 * @return the creator last name
	 */
	public String getCreatorLastName() {
		return creatorLastName;
	}
	
	/**
	 * Sets the creator last name.
	 *
	 * @param creatorLastName the new creator last name
	 */
	public void setCreatorLastName(String creatorLastName) {
		this.creatorLastName = creatorLastName;
	}
}
