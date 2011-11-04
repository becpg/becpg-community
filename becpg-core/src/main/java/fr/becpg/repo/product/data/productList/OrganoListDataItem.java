/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class OrganoListDataItem.
 *
 * @author querephi
 */
public class OrganoListDataItem {

	/** The node ref. */
	private NodeRef nodeRef;	
	
	/** The value. */
	private String value;
	
	/** The organo. */
	private NodeRef organo;
	
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
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Gets the organo.
	 *
	 * @return the organo
	 */
	public NodeRef getOrgano() {
		return organo;
	}
	
	/**
	 * Sets the organo.
	 *
	 * @param organo the new organo
	 */
	public void setOrgano(NodeRef organo) {
		this.organo = organo;
	}	
	
	/**
	 * Instantiates a new organo list data item.
	 */
	public OrganoListDataItem(){
	
	}
	
	/**
	 * Instantiates a new organo list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param organo the organo
	 */
	public OrganoListDataItem(NodeRef nodeRef, String value, NodeRef organo){
		setNodeRef(nodeRef);
		setValue(value);
		setOrgano(organo);
	}
	
	/**
	 * copy constructor
	 * @param o
	 */
	public OrganoListDataItem(OrganoListDataItem o){
		setNodeRef(nodeRef);
		setValue(value);
		setOrgano(organo);
	}
	
}
