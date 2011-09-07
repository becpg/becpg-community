/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class CharacteristicToCompare.
 *
 * @author querephi
 */
public class CharacteristicToCompare {

	/** The charact path. */
	private List<NodeRef> charactPath;
	
	/** The characteristic. */
	private NodeRef characteristic;	
	
	/** The node ref1. */
	private NodeRef nodeRef1;
	
	/** The node ref2. */
	private NodeRef nodeRef2;
	
	/**
	 * Gets the charact path.
	 *
	 * @return the charact path
	 */
	public List<NodeRef> getCharactPath() {
		return charactPath;
	}
	
	/**
	 * Sets the charact path.
	 *
	 * @param charactPath the new charact path
	 */
	public void setCharactPath(List<NodeRef> charactPath) {
		this.charactPath = charactPath;
	}
	
	/**
	 * Gets the characteristic.
	 *
	 * @return the characteristic
	 */
	public NodeRef getCharacteristic() {
		return characteristic;
	}
	
	/**
	 * Sets the characteristic.
	 *
	 * @param characteristic the new characteristic
	 */
	public void setCharacteristic(NodeRef characteristic) {
		this.characteristic = characteristic;
	}
	
	/**
	 * Gets the node ref1.
	 *
	 * @return the node ref1
	 */
	public NodeRef getNodeRef1() {
		return nodeRef1;
	}
	
	/**
	 * Sets the node ref1.
	 *
	 * @param nodeRef1 the new node ref1
	 */
	public void setNodeRef1(NodeRef nodeRef1) {
		this.nodeRef1 = nodeRef1;
	}
	
	/**
	 * Gets the node ref2.
	 *
	 * @return the node ref2
	 */
	public NodeRef getNodeRef2() {
		return nodeRef2;
	}
	
	/**
	 * Sets the node ref2.
	 *
	 * @param nodeRef2 the new node ref2
	 */
	public void setNodeRef2(NodeRef nodeRef2) {
		this.nodeRef2 = nodeRef2;
	}
	
	/**
	 * Instantiates a new characteristic to compare.
	 *
	 * @param charactPath the charact path
	 * @param characteristic the characteristic
	 * @param nodeRef1 the node ref1
	 * @param nodeRef2 the node ref2
	 */
	public CharacteristicToCompare(List<NodeRef> charactPath, NodeRef characteristic, NodeRef nodeRef1, NodeRef nodeRef2){
		setCharactPath(charactPath);
		setCharacteristic(characteristic);
		setNodeRef1(nodeRef1);
		setNodeRef2(nodeRef2);
	}
}
