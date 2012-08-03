/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.BaseObject;

// TODO: Auto-generated Javadoc
/**
 * The Class OrganoListDataItem.
 *
 * @author querephi
 */
public class OrganoListDataItem extends BaseObject {

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((organo == null) ? 0 : organo.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrganoListDataItem other = (OrganoListDataItem) obj;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (organo == null) {
			if (other.organo != null)
				return false;
		} else if (!organo.equals(other.organo))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OrganoListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", organo=" + organo + "]";
	}
	
	
	
}
