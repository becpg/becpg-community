/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Class AllergenListDataItem.
 *
 * @author querephi
 */
public class AllergenListDataItem implements IManualDataItem{

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
}
