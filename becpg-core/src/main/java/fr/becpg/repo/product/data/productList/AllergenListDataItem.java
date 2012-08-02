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
 * The Class AllergenListDataItem.
 *
 * @author querephi
 */
public class AllergenListDataItem extends BaseObject implements IManualDataItem{

	/** The node ref. */
	private NodeRef nodeRef;	
	
	/** The voluntary. */
	private Boolean voluntary = false;
	
	/** The in voluntary. */
	private Boolean inVoluntary = false;
	
	/** The voluntary sources. */
	private List<NodeRef> voluntarySources = new ArrayList<NodeRef>();
	
	/** The in voluntary sources. */
	private List<NodeRef> inVoluntarySources = new ArrayList<NodeRef>();
	
	/** The allergen. */
	private NodeRef allergen;
	
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
	 * Gets the voluntary.
	 *
	 * @return the voluntary
	 */
	public Boolean getVoluntary() {
		return voluntary;
	}
	
	/**
	 * Sets the voluntary.
	 *
	 * @param voluntary the new voluntary
	 */
	public void setVoluntary(Boolean voluntary) {
		this.voluntary = voluntary;
	}
	
	/**
	 * Gets the in voluntary.
	 *
	 * @return the in voluntary
	 */
	public Boolean getInVoluntary() {
		return inVoluntary;
	}
	
	/**
	 * Sets the in voluntary.
	 *
	 * @param inVoluntary the new in voluntary
	 */
	public void setInVoluntary(Boolean inVoluntary) {
		this.inVoluntary = inVoluntary;
	}
	
	/**
	 * Gets the voluntary sources.
	 *
	 * @return the voluntary sources
	 */
	public List<NodeRef> getVoluntarySources() {
		return voluntarySources;
	}
	
	/**
	 * Sets the voluntary sources.
	 *
	 * @param voluntarySources the new voluntary sources
	 */
	public void setVoluntarySources(List<NodeRef> voluntarySources) {
		this.voluntarySources = voluntarySources;
	}
	
	/**
	 * Gets the in voluntary sources.
	 *
	 * @return the in voluntary sources
	 */
	public List<NodeRef> getInVoluntarySources() {
		return inVoluntarySources;
	}
	
	/**
	 * Sets the in voluntary sources.
	 *
	 * @param inVoluntarySources the new in voluntary sources
	 */
	public void setInVoluntarySources(List<NodeRef> inVoluntarySources) {
		this.inVoluntarySources = inVoluntarySources;
	}
	
	/**
	 * Gets the allergen.
	 *
	 * @return the allergen
	 */
	public NodeRef getAllergen() {
		return allergen;
	}
	
	/**
	 * Sets the allergen.
	 *
	 * @param allergen the new allergen
	 */
	public void setAllergen(NodeRef allergen) {
		this.allergen = allergen;
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
	 * Instantiates a new allergen list data item.
	 */
	public AllergenListDataItem(){
		
	}
	
	/**
	 * Instantiates a new allergen list data item.
	 *
	 * @param nodeRef the node ref
	 * @param voluntary the voluntary
	 * @param inVoluntary the in voluntary
	 * @param voluntarySources the voluntary sources
	 * @param inVoluntarySources the in voluntary sources
	 * @param allergen the allergen
	 */
	public AllergenListDataItem(NodeRef nodeRef, Boolean voluntary, Boolean inVoluntary, List<NodeRef> voluntarySources, List<NodeRef> inVoluntarySources, NodeRef allergen, Boolean isManual){
		
		setNodeRef(nodeRef);
		setVoluntary(voluntary);
		setInVoluntary(inVoluntary);
		setVoluntarySources(voluntarySources);
		setInVoluntarySources(inVoluntarySources);
		setAllergen(allergen);
		setIsManual(isManual);
	}
	
	/**
	 * Copy constructor
	 * @param a
	 */
	public AllergenListDataItem(AllergenListDataItem a){
		
		setNodeRef(a.getNodeRef());
		setVoluntary(a.getVoluntary());
		setInVoluntary(a.getInVoluntary());
		setVoluntarySources(a.getVoluntarySources());
		setInVoluntarySources(a.getInVoluntarySources());
		setAllergen(a.getAllergen());
		setIsManual(a.getIsManual());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allergen == null) ? 0 : allergen.hashCode());
		result = prime * result + ((inVoluntary == null) ? 0 : inVoluntary.hashCode());
		result = prime * result + ((inVoluntarySources == null) ? 0 : inVoluntarySources.hashCode());
		result = prime * result + ((isManual == null) ? 0 : isManual.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((voluntary == null) ? 0 : voluntary.hashCode());
		result = prime * result + ((voluntarySources == null) ? 0 : voluntarySources.hashCode());
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
		AllergenListDataItem other = (AllergenListDataItem) obj;
		if (allergen == null) {
			if (other.allergen != null)
				return false;
		} else if (!allergen.equals(other.allergen))
			return false;
		if (inVoluntary == null) {
			if (other.inVoluntary != null)
				return false;
		} else if (!inVoluntary.equals(other.inVoluntary))
			return false;
		if (inVoluntarySources == null) {
			if (other.inVoluntarySources != null)
				return false;
		} else if (!inVoluntarySources.equals(other.inVoluntarySources))
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
		if (voluntary == null) {
			if (other.voluntary != null)
				return false;
		} else if (!voluntary.equals(other.voluntary))
			return false;
		if (voluntarySources == null) {
			if (other.voluntarySources != null)
				return false;
		} else if (!voluntarySources.equals(other.voluntarySources))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AllergenListDataItem [nodeRef=" + nodeRef + ", voluntary=" + voluntary + ", inVoluntary=" + inVoluntary + ", voluntarySources=" + voluntarySources
				+ ", inVoluntarySources=" + inVoluntarySources + ", allergen=" + allergen + ", isManual=" + isManual + "]";
	}
	
	
}
