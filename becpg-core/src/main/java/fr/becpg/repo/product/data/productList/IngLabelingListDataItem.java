/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class IngLabelingListDataItem.
 *
 * @author querephi
 */
public class IngLabelingListDataItem implements IManualDataItem{

	/** The node ref. */
	private NodeRef nodeRef;	
	
	/** The grp. */
	private String grp;
	
	/** The value. */
	private MLText value;
	
	private Boolean isManual;
	
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
	 * Gets the grp.
	 *
	 * @return the grp
	 */
	public String getGrp() {
		return grp;
	}

	/**
	 * Sets the grp.
	 *
	 * @param grp the new grp
	 */
	public void setGrp(String grp) {
		this.grp = grp;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public MLText getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(MLText value) {
		this.value = value;
	}
	
	@Override
	public Boolean getIsManual() {

		return isManual;
	}

	@Override
	public void setIsManual(Boolean isManual) {
		
		this.isManual = isManual;		
	}
	
	/**
	 * Instantiates a new ing labeling list data item.
	 */
	public IngLabelingListDataItem(){
	
	}
	
	/**
	 * Instantiates a new ing labeling list data item.
	 *
	 * @param nodeRef the node ref
	 * @param grp the grp
	 * @param value the value
	 */
	public IngLabelingListDataItem(NodeRef nodeRef, String grp, MLText value, Boolean isManual){
		setNodeRef(nodeRef);
		setGrp(grp);
		setValue(value);		
		setIsManual(isManual);
	}
	
	/**
	 * Copy constructor
	 * @param i
	 */
	public IngLabelingListDataItem(IngLabelingListDataItem i){
		setNodeRef(i.getNodeRef());
		setGrp(i.getGrp());
		setValue(i.getValue());
		setIsManual(i.getIsManual());
	}
}
