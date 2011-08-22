/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * Store an ingredient in the ingredient list.
 *
 * @author querephi
 */
public class IngListDataItem implements Comparable<IngListDataItem>{

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The qty perc. */
	private Float qtyPerc = 0f;
	
	/** The geo origin. */
	private List<NodeRef> geoOrigin = new ArrayList<NodeRef>();
	
	/** The bio origin. */
	private List<NodeRef> bioOrigin = new ArrayList<NodeRef>();
	
	/** The is gmo. */
	private Boolean isGMO = false;
	
	/** The is ionized. */
	private Boolean isIonized = false;	
	
	/** The ing. */
	private NodeRef ing;
	
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
	 * Gets the qty perc.
	 *
	 * @return the qty perc
	 */
	public Float getQtyPerc() {
		return qtyPerc;
	}
	
	/**
	 * Sets the qty perc.
	 *
	 * @param qtyPerc the new qty perc
	 */
	public void setQtyPerc(Float qtyPerc) {
		this.qtyPerc = qtyPerc;
	}
	
	/**
	 * Gets the geo origin.
	 *
	 * @return the geo origin
	 */
	public List<NodeRef> getGeoOrigin() {
		return geoOrigin;
	}
	
	/**
	 * Sets the geo origin.
	 *
	 * @param geoOrigin the new geo origin
	 */
	public void setGeoOrigin(List<NodeRef> geoOrigin) {
		this.geoOrigin = geoOrigin;
	}
	
	/**
	 * Gets the bio origin.
	 *
	 * @return the bio origin
	 */
	public List<NodeRef> getBioOrigin() {
		return bioOrigin;
	}
	
	/**
	 * Sets the bio origin.
	 *
	 * @param bioOrigin the new bio origin
	 */
	public void setBioOrigin(List<NodeRef> bioOrigin) {
		this.bioOrigin = bioOrigin;
	}
	
	/**
	 * Checks if is gMO.
	 *
	 * @return true, if is gMO
	 */
	public Boolean isGMO() {
		return isGMO;
	}
	
	/**
	 * Sets the checks if is gmo.
	 *
	 * @param isGMO the new checks if is gmo
	 */
	public void setIsGMO(Boolean isGMO) {
		this.isGMO = isGMO;
	}
	
	/**
	 * Checks if is ionized.
	 *
	 * @return true, if is ionized
	 */
	public Boolean isIonized() {
		return isIonized;
	}
	
	/**
	 * Sets the ionized.
	 *
	 * @param isIonized the new ionized
	 */
	public void setIonized(Boolean isIonized) {
		this.isIonized = isIonized;
	}
	
	/**
	 * Gets the ing.
	 *
	 * @return the ing
	 */
	public NodeRef getIng() {
		return ing;
	}
	
	/**
	 * Sets the ing.
	 *
	 * @param ing the new ing
	 */
	public void setIng(NodeRef ing) {
		this.ing = ing;
	}
	
	/**
	 * Instantiates a new ing list data item.
	 */
	public IngListDataItem()
	{
	}
	
	/**
	 * Instantiates a new ing list data item.
	 *
	 * @param nodeRef the node ref
	 * @param qtyPerc the qty perc
	 * @param geoOrigin the geo origin
	 * @param bioOrigin the bio origin
	 * @param isGMO the is gmo
	 * @param isIonized the is ionized
	 * @param ing the ing
	 */
	public IngListDataItem(NodeRef nodeRef,	Float qtyPerc, List<NodeRef> geoOrigin, List<NodeRef> bioOrigin, Boolean isGMO, Boolean isIonized,NodeRef ing)
	{
		setNodeRef(nodeRef);
		setQtyPerc(qtyPerc);
		setGeoOrigin(geoOrigin);
		setBioOrigin(bioOrigin);
		setIsGMO(isGMO);
		setIonized(isIonized);
		setIng(ing);
	}
	
	/**
	 * Sort by qty perc in descending order.
	 *
	 * @param o the o
	 * @return the int
	 * @author querephi
	 */
	@Override
	public int compareTo(IngListDataItem o) {
		
		return o.getQtyPerc().compareTo(this.getQtyPerc());
	}
}
