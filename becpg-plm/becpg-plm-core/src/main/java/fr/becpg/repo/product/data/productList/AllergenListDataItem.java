/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

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
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * <p>AllergenListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:allergenList")
public class AllergenListDataItem extends AbstractManualVariantListDataItem
		implements SimpleCharactDataItem, AspectAwareDataItem, ControlableListDataItem {

	private static final long serialVersionUID = -6746076643301742367L;
	private Double qtyPerc;
	private Boolean voluntary = null;
	private Boolean inVoluntary = null;
	private Boolean onSite = null;
	private Boolean onLine = null;
	private Boolean isCleaned = null;

	private String allergenValue = null;
	private List<NodeRef> voluntarySources = new ArrayList<>();
	private List<NodeRef> inVoluntarySources = new ArrayList<>();
	private NodeRef allergen;

	Map<NodeRef, Double> qtyByVariant = null;

	@InternalField
	public void addQtyPerc(VariantDataItem variantDataItem, Double toAdd) {
		if (variantDataItem != null && variantDataItem.getVariants()!=null &&  !variantDataItem.getVariants().isEmpty()) {
	        if (qtyByVariant == null) qtyByVariant = new HashMap<>();
	        
	        variantDataItem.getVariants().forEach(variant -> {
	            Double qty = qtyByVariant.computeIfAbsent(variant, v -> 0d) + toAdd;
	            qtyByVariant.put(variant, qty > 100d ? 100d : qty);
	        });
	    } else {
	        if (qtyPerc == null) qtyPerc = 0d;
	        qtyPerc = Math.min(qtyPerc + toAdd, 100d);
	    }

	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setAllergen(nodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		setQtyPerc(value);
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getAllergen();
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return getQtyPerc();
	}

	/**
	 * <p>Getter for the field <code>qtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:allergenListQtyPerc")
	public Double getQtyPerc() {
		if (qtyByVariant != null) {
			Optional<Double> maxQtyOptional = qtyByVariant.values().stream().max(Double::compareTo);
			if (maxQtyOptional.isPresent()) {
				return qtyPerc == null ? maxQtyOptional.get() : qtyPerc + maxQtyOptional.get();
			}
		}

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
	@AlfQname(qname = "bcpg:allergenListVoluntary")
	@Nullable
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
	@AlfQname(qname = "bcpg:allergenListInVoluntary")
	@Nullable
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

	@AlfProp
	@AlfQname(qname = "bcpg:allergenListOnSite")
	@Nullable
	public Boolean getOnSite() {
		return onSite;
	}

	public void setOnSite(Boolean onSite) {
		this.onSite = onSite;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:allergenListOnLine")
	@Nullable
	public Boolean getOnLine() {
		return onLine;
	}

	public void setOnLine(Boolean onLine) {
		this.onLine = onLine;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:allergenListIsCleaned")
	@Nullable
	public Boolean getIsCleaned() {
		return isCleaned;
	}

	public void setIsCleaned(Boolean isCleaned) {
		this.isCleaned = isCleaned;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:allergenValue")
	public String getAllergenValue() {
		return allergenValue;
	}

	public void setAllergenValue(String allergenValue) {
		this.allergenValue = allergenValue;
	}

	/**
	 * <p>Getter for the field <code>voluntarySources</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@InternalField
	@AlfQname(qname = "bcpg:allergenListVolSources")
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
	@AlfQname(qname = "bcpg:allergenListInVolSources")
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
	@AlfQname(qname = "bcpg:allergenListAllergen")
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
	public AllergenListDataItem() {
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
	public AllergenListDataItem(NodeRef nodeRef, Double qtyPerc, Boolean voluntary, Boolean inVoluntary, List<NodeRef> voluntarySources,
			List<NodeRef> inVoluntarySources, NodeRef allergen, Boolean isManual) {
		super();
		this.nodeRef = nodeRef;
		this.qtyPerc = qtyPerc;
		this.voluntary = voluntary;
		this.inVoluntary = inVoluntary;
		this.voluntarySources = voluntarySources;
		this.inVoluntarySources = inVoluntarySources;
		this.allergen = allergen;
		this.isManual = isManual;
	}

	public AllergenListDataItem(AllergenListDataItem allergenListDataItem) {
		super(allergenListDataItem);

		this.qtyPerc = allergenListDataItem.qtyPerc;
		this.voluntary = allergenListDataItem.voluntary;
		this.inVoluntary = allergenListDataItem.inVoluntary;
		this.onSite = allergenListDataItem.onSite;
		this.onLine = allergenListDataItem.onLine;
		this.isCleaned = allergenListDataItem.isCleaned;
		this.allergenValue = allergenListDataItem.allergenValue;
		this.voluntarySources = new ArrayList<>(allergenListDataItem.voluntarySources);
		this.inVoluntarySources = new ArrayList<>(allergenListDataItem.inVoluntarySources);
		this.allergen = allergenListDataItem.allergen;
	}

	@Override
	public AllergenListDataItem copy() {
		return new AllergenListDataItem(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(allergen, allergenValue, inVoluntary, inVoluntarySources, isCleaned, onLine, onSite, qtyPerc,
				voluntary, voluntarySources);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AllergenListDataItem other = (AllergenListDataItem) obj;
		return Objects.equals(allergen, other.allergen) && Objects.equals(allergenValue, other.allergenValue)
				&& Objects.equals(inVoluntary, other.inVoluntary) && Objects.equals(inVoluntarySources, other.inVoluntarySources)
				&& Objects.equals(isCleaned, other.isCleaned) && Objects.equals(onLine, other.onLine) && Objects.equals(onSite, other.onSite)
				&& Objects.equals(qtyPerc, other.qtyPerc) && Objects.equals(voluntary, other.voluntary)
				&& Objects.equals(voluntarySources, other.voluntarySources);
	}

	@Override
	public String toString() {
		return "AllergenListDataItem [qtyPerc=" + qtyPerc + ", voluntary=" + voluntary + ", inVoluntary=" + inVoluntary + ", allergenValue="
				+ allergenValue + ", voluntarySources=" + voluntarySources + ", inVoluntarySources=" + inVoluntarySources + ", allergen=" + allergen
				+ "]";
	}

}
