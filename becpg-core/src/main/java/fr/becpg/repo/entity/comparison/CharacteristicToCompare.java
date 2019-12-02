/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class CharacteristicToCompare.
 *
 * @author querephi
 */
public class CharacteristicToCompare {

	private String pivotKey;	
	
	private NodeRef nodeRef1;
	
	private NodeRef nodeRef2;
	
	
	public String getPivotKey() {
		return pivotKey;
	}

	public void setPivotKey(String pivotKey) {
		this.pivotKey = pivotKey;
	}

	public NodeRef getNodeRef1() {
		return nodeRef1;
	}

	public void setNodeRef1(NodeRef nodeRef1) {
		this.nodeRef1 = nodeRef1;
	}

	public NodeRef getNodeRef2() {
		return nodeRef2;
	}

	public void setNodeRef2(NodeRef nodeRef2) {
		this.nodeRef2 = nodeRef2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef1 == null) ? 0 : nodeRef1.hashCode());
		result = prime * result + ((nodeRef2 == null) ? 0 : nodeRef2.hashCode());
		result = prime * result + ((pivotKey == null) ? 0 : pivotKey.hashCode());
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

	@Override
	public String toString() {
		return "CharacteristicToCompare [pivotKey=" + pivotKey + ", nodeRef1=" + nodeRef1 + ", nodeRef2=" + nodeRef2 + "]";
	}

	public CharacteristicToCompare( String pivotKey, NodeRef nodeRef1, NodeRef nodeRef2) {
		super();
		this.pivotKey = pivotKey;
		this.nodeRef1 = nodeRef1;
		this.nodeRef2 = nodeRef2;
	}

	
}
