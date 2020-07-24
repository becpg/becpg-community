/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.ControlableListDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>AllergenListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:allergenList")
public class AllergenListDataItem extends AbstractManualVariantListDataItem implements SimpleCharactDataItem, AspectAwareDataItem, ControlableListDataItem {

	private static final long serialVersionUID = -6746076643301742367L;
	private Double qtyPerc;
	private Boolean voluntary = false;
	private Boolean inVoluntary = false;
	private List<NodeRef> voluntarySources = new ArrayList<>();
	private List<NodeRef> inVoluntarySources = new ArrayList<>();
	private NodeRef allergen;
	
	
	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		this.allergen = nodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		 this.qtyPerc = value;
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return allergen;
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return qtyPerc;
	}
	
	
	/**
	 * <p>Getter for the field <code>qtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:allergenListQtyPerc")
	public Double getQtyPerc() {
		return qtyPerc;
	}

	/**
	 * <p>Setter for the field <code>qtyPerc</code>.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object.
	 */
	public void setQtyPerc(Double qtyPerc) {
		this.qtyPerc = qtyPerc;
	}


	/**
	 * <p>Getter for the field <code>voluntary</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:allergenListVoluntary")
	public Boolean getVoluntary() {
		return voluntary;
	}
	

	/**
	 * <p>Setter for the field <code>voluntary</code>.</p>
	 *
	 * @param voluntary a {@link java.lang.Boolean} object.
	 */
	public void setVoluntary(Boolean voluntary) {
		this.voluntary = voluntary;
	}
	

	/**
	 * <p>Getter for the field <code>inVoluntary</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:allergenListInVoluntary")
	public Boolean getInVoluntary() {
		return inVoluntary;
	}
	
	
	/**
	 * <p>Setter for the field <code>inVoluntary</code>.</p>
	 *
	 * @param inVoluntary a {@link java.lang.Boolean} object.
	 */
	public void setInVoluntary(Boolean inVoluntary) {
		this.inVoluntary = inVoluntary;
	}
	

	/**
	 * <p>Getter for the field <code>voluntarySources</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@InternalField
	@AlfQname(qname="bcpg:allergenListVolSources")
	public List<NodeRef> getVoluntarySources() {
		return voluntarySources;
	}
	
	
	/**
	 * <p>Setter for the field <code>voluntarySources</code>.</p>
	 *
	 * @param voluntarySources a {@link java.util.List} object.
	 */
	public void setVoluntarySources(List<NodeRef> voluntarySources) {
		this.voluntarySources = voluntarySources;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getTextCriteria() {
		return null;
	}

	/**
	 * <p>Getter for the field <code>inVoluntarySources</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@InternalField
	@AlfQname(qname="bcpg:allergenListInVolSources")
	public List<NodeRef> getInVoluntarySources() {
		return inVoluntarySources;
	}
	
	
	/**
	 * <p>Setter for the field <code>inVoluntarySources</code>.</p>
	 *
	 * @param inVoluntarySources a {@link java.util.List} object.
	 */
	public void setInVoluntarySources(List<NodeRef> inVoluntarySources) {
		this.inVoluntarySources = inVoluntarySources;
	}
	

	/**
	 * <p>Getter for the field <code>allergen</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="bcpg:allergenListAllergen")
	@DataListIdentifierAttr
	public NodeRef getAllergen() {
		return allergen;
	}
	
	
	/**
	 * <p>Setter for the field <code>allergen</code>.</p>
	 *
	 * @param allergen a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setAllergen(NodeRef allergen) {
		this.allergen = allergen;
	}
	
	
	
	/**
	 * <p>Constructor for AllergenListDataItem.</p>
	 */
	public AllergenListDataItem(){
		super();
	}
	
	/**
	 * <p>Constructor for AllergenListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qtyPerc a {@link java.lang.Double} object.
	 * @param voluntary a {@link java.lang.Boolean} object.
	 * @param inVoluntary a {@link java.lang.Boolean} object.
	 * @param voluntarySources a {@link java.util.List} object.
	 * @param inVoluntarySources a {@link java.util.List} object.
	 * @param allergen a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param isManual a {@link java.lang.Boolean} object.
	 */
	public AllergenListDataItem(NodeRef nodeRef,Double qtyPerc, Boolean voluntary, Boolean inVoluntary, List<NodeRef> voluntarySources, List<NodeRef> inVoluntarySources, NodeRef allergen, Boolean isManual){
		super();
		this.nodeRef = nodeRef;
		this.qtyPerc = qtyPerc;
		this.voluntary  = voluntary;
		this.inVoluntary = inVoluntary;
		this.voluntarySources = voluntarySources;	
		this.inVoluntarySources = inVoluntarySources;
		this.allergen = allergen;
		this.isManual = isManual;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((allergen == null) ? 0 : allergen.hashCode());
		result = prime * result + ((inVoluntary == null) ? 0 : inVoluntary.hashCode());
		result = prime * result + ((inVoluntarySources == null) ? 0 : inVoluntarySources.hashCode());
		result = prime * result + ((qtyPerc == null) ? 0 : qtyPerc.hashCode());
		result = prime * result + ((voluntary == null) ? 0 : voluntary.hashCode());
		result = prime * result + ((voluntarySources == null) ? 0 : voluntarySources.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
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
		if (qtyPerc == null) {
			if (other.qtyPerc != null)
				return false;
		} else if (!qtyPerc.equals(other.qtyPerc))
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



	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AllergenListDataItem [voluntary=" + voluntary + ", inVoluntary=" + inVoluntary + ", voluntarySources=" + voluntarySources + ", inVoluntarySources="
				+ inVoluntarySources + ", allergen=" + allergen + ", isManual=" + isManual + ", nodeRef=" + nodeRef + ", parentNodeRef=" + parentNodeRef + ", name=" + name + "]";
	}




	
	
	
}
