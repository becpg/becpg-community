/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.BaseObject;

// TODO: Auto-generated Javadoc
/**
 * Store an ingredient in the ingredient list.
 *
 * @author querephi
 */
public class IngListDataItem extends BaseObject  implements Comparable<IngListDataItem>, IManualDataItem, SimpleCharactDataItem{

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The qty perc. */
	private Double qtyPerc = 0d;
	
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
	 * Gets the qty perc.
	 *
	 * @return the qty perc
	 */
	public Double getQtyPerc() {
		return qtyPerc;
	}
	
	/**
	 * Sets the qty perc.
	 *
	 * @param qtyPerc the new qty perc
	 */
	public void setQtyPerc(Double qtyPerc) {
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
	
	@Override
	public Boolean getIsManual() {

		return isManual;
	}

	@Override
	public void setIsManual(Boolean isManual) {
		
		this.isManual = isManual;		
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
	public IngListDataItem(NodeRef nodeRef,	Double qtyPerc, List<NodeRef> geoOrigin, List<NodeRef> bioOrigin, Boolean isGMO, Boolean isIonized, NodeRef ing, Boolean isManual)
	{
		setNodeRef(nodeRef);
		setQtyPerc(qtyPerc);
		setGeoOrigin(geoOrigin);
		setBioOrigin(bioOrigin);
		setIsGMO(isGMO);
		setIonized(isIonized);
		setIng(ing);
		setIsManual(isManual);
	}
	
	/**
	 * Copy contructor
	 * @param i
	 */
	public IngListDataItem(IngListDataItem i){
		
		setNodeRef(i.getNodeRef());
		setQtyPerc(i.getQtyPerc());
		setGeoOrigin(i.getGeoOrigin());
		setBioOrigin(i.getBioOrigin());
		setIsGMO(i.isGMO());
		setIonized(i.isIonized());
		setIng(i.getIng());
		setIsManual(i.getIsManual());
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bioOrigin == null) ? 0 : bioOrigin.hashCode());
		result = prime * result + ((geoOrigin == null) ? 0 : geoOrigin.hashCode());
		result = prime * result + ((ing == null) ? 0 : ing.hashCode());
		result = prime * result + ((isGMO == null) ? 0 : isGMO.hashCode());
		result = prime * result + ((isIonized == null) ? 0 : isIonized.hashCode());
		result = prime * result + ((isManual == null) ? 0 : isManual.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((qtyPerc == null) ? 0 : qtyPerc.hashCode());
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
		IngListDataItem other = (IngListDataItem) obj;
		if (bioOrigin == null) {
			if (other.bioOrigin != null)
				return false;
		} else if (!bioOrigin.equals(other.bioOrigin))
			return false;
		if (geoOrigin == null) {
			if (other.geoOrigin != null)
				return false;
		} else if (!geoOrigin.equals(other.geoOrigin))
			return false;
		if (ing == null) {
			if (other.ing != null)
				return false;
		} else if (!ing.equals(other.ing))
			return false;
		if (isGMO == null) {
			if (other.isGMO != null)
				return false;
		} else if (!isGMO.equals(other.isGMO))
			return false;
		if (isIonized == null) {
			if (other.isIonized != null)
				return false;
		} else if (!isIonized.equals(other.isIonized))
			return false;
		if (isManual == null) {
			if (other.isManual != null)
				return false;
		} else if (!isManual.equals(other.isManual))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (qtyPerc == null) {
			if (other.qtyPerc != null)
				return false;
		} else if (!qtyPerc.equals(other.qtyPerc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IngListDataItem [nodeRef=" + nodeRef + ", qtyPerc=" + qtyPerc + ", geoOrigin=" + geoOrigin + ", bioOrigin=" + bioOrigin + ", isGMO=" + isGMO + ", isIonized="
				+ isIonized + ", ing=" + ing + ", isManual=" + isManual + "]";
	}

	@Override
	public NodeRef getCharactNodeRef() {
		return ing;
	}

	@Override
	public Double getValue() {
		return qtyPerc;
	}
	
	
}
