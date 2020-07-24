/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * The Class CharacteristicToCompare.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class CharacteristicToCompare {

	private String pivotKey;	
	
	private NodeRef nodeRef1;
	
	private NodeRef nodeRef2;
	
	
	/**
	 * <p>Getter for the field <code>pivotKey</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPivotKey() {
		return pivotKey;
	}

	/**
	 * <p>Setter for the field <code>pivotKey</code>.</p>
	 *
	 * @param pivotKey a {@link java.lang.String} object.
	 */
	public void setPivotKey(String pivotKey) {
		this.pivotKey = pivotKey;
	}

	/**
	 * <p>Getter for the field <code>nodeRef1</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getNodeRef1() {
		return nodeRef1;
	}

	/**
	 * <p>Setter for the field <code>nodeRef1</code>.</p>
	 *
	 * @param nodeRef1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setNodeRef1(NodeRef nodeRef1) {
		this.nodeRef1 = nodeRef1;
	}

	/**
	 * <p>Getter for the field <code>nodeRef2</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getNodeRef2() {
		return nodeRef2;
	}

	/**
	 * <p>Setter for the field <code>nodeRef2</code>.</p>
	 *
	 * @param nodeRef2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setNodeRef2(NodeRef nodeRef2) {
		this.nodeRef2 = nodeRef2;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef1 == null) ? 0 : nodeRef1.hashCode());
		result = prime * result + ((nodeRef2 == null) ? 0 : nodeRef2.hashCode());
		result = prime * result + ((pivotKey == null) ? 0 : pivotKey.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CharacteristicToCompare other = (CharacteristicToCompare) obj;
		if (nodeRef1 == null) {
			if (other.nodeRef1 != null)
				return false;
		} else if (!nodeRef1.equals(other.nodeRef1))
			return false;
		if (nodeRef2 == null) {
			if (other.nodeRef2 != null)
				return false;
		} else if (!nodeRef2.equals(other.nodeRef2))
			return false;
		if (pivotKey == null) {
			if (other.pivotKey != null)
				return false;
		} else if (!pivotKey.equals(other.pivotKey))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CharacteristicToCompare [pivotKey=" + pivotKey + ", nodeRef1=" + nodeRef1 + ", nodeRef2=" + nodeRef2 + "]";
	}

	/**
	 * <p>Constructor for CharacteristicToCompare.</p>
	 *
	 * @param pivotKey a {@link java.lang.String} object.
	 * @param nodeRef1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeRef2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public CharacteristicToCompare( String pivotKey, NodeRef nodeRef1, NodeRef nodeRef2) {
		super();
		this.pivotKey = pivotKey;
		this.nodeRef1 = nodeRef1;
		this.nodeRef2 = nodeRef2;
	}

	
}
