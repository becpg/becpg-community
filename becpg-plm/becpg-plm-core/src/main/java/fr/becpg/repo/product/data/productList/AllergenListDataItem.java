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

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.regulatory.RegulatoryEntityItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.annotation.AlfMlText;
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
		implements SimpleCharactDataItem, AspectAwareDataItem, ControlableListDataItem, RegulatoryEntityItem {

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
	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();
	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();
	private RequirementType regulatoryType;
	private MLText regulatoryMessage;

	private Map<NodeRef, Double> qtyByVariant = null;

	/**
	 * <p>addQtyPerc.</p>
	 *
	 * @param variantDataItem a {@link fr.becpg.repo.variant.model.VariantDataItem} object
	 * @param toAdd a {@link java.lang.Double} object
	 */
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
	
	/**
	 * <p>Getter for the field <code>regulatoryCountriesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	/** {@inheritDoc} */
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}

	/**
	 * <p>Getter for the field <code>regulatoryUsagesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	/** {@inheritDoc} */
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}
	
	
	/**
	 * <p>Getter for the field <code>regulatoryType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementType} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:regulatoryType")
	public RequirementType getRegulatoryType() {
		return regulatoryType;
	}

	/** {@inheritDoc} */
	public void setRegulatoryType(RequirementType regulatoryType) {
		this.regulatoryType = regulatoryType;
	}

	/**
	 * <p>Getter for the field <code>regulatoryMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:regulatoryText")
	public MLText getRegulatoryMessage() {
		return regulatoryMessage;
	}

	/** {@inheritDoc} */
	public void setRegulatoryMessage(MLText regulatoryMessage) {
		this.regulatoryMessage = regulatoryMessage;
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

	/**
	 * <p>Getter for the field <code>onSite</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenListOnSite")
	@Nullable
	public Boolean getOnSite() {
		return onSite;
	}

	/**
	 * <p>Setter for the field <code>onSite</code>.</p>
	 *
	 * @param onSite a {@link java.lang.Boolean} object
	 */
	public void setOnSite(Boolean onSite) {
		this.onSite = onSite;
	}

	/**
	 * <p>Getter for the field <code>onLine</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenListOnLine")
	@Nullable
	public Boolean getOnLine() {
		return onLine;
	}

	/**
	 * <p>Setter for the field <code>onLine</code>.</p>
	 *
	 * @param onLine a {@link java.lang.Boolean} object
	 */
	public void setOnLine(Boolean onLine) {
		this.onLine = onLine;
	}

	/**
	 * <p>Getter for the field <code>isCleaned</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenListIsCleaned")
	@Nullable
	public Boolean getIsCleaned() {
		return isCleaned;
	}

	/**
	 * <p>Setter for the field <code>isCleaned</code>.</p>
	 *
	 * @param isCleaned a {@link java.lang.Boolean} object
	 */
	public void setIsCleaned(Boolean isCleaned) {
		this.isCleaned = isCleaned;
	}

	/**
	 * <p>Getter for the field <code>allergenValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenValue")
	public String getAllergenValue() {
		return allergenValue;
	}

	/**
	 * <p>Setter for the field <code>allergenValue</code>.</p>
	 *
	 * @param allergenValue a {@link java.lang.String} object
	 */
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
	public MLText getTextCriteria() {
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

	/**
	 * <p>Constructor for AllergenListDataItem.</p>
	 *
	 * @param allergenListDataItem a {@link fr.becpg.repo.product.data.productList.AllergenListDataItem} object
	 */
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
		this.regulatoryCountriesRef = new ArrayList<>(allergenListDataItem.regulatoryCountriesRef);
		this.regulatoryUsagesRef = new ArrayList<>(allergenListDataItem.regulatoryUsagesRef);
		this.regulatoryMessage = allergenListDataItem.regulatoryMessage;
		this.regulatoryType = allergenListDataItem.regulatoryType;
	}

	/** {@inheritDoc} */
	@Override
	public AllergenListDataItem copy() {
		return new AllergenListDataItem(this);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(allergen, allergenValue, inVoluntary, inVoluntarySources, isCleaned, onLine, onSite, qtyPerc,
				voluntary, voluntarySources);
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
		return Objects.equals(allergen, other.allergen) && Objects.equals(allergenValue, other.allergenValue)
				&& Objects.equals(inVoluntary, other.inVoluntary) && Objects.equals(inVoluntarySources, other.inVoluntarySources)
				&& Objects.equals(isCleaned, other.isCleaned) && Objects.equals(onLine, other.onLine) && Objects.equals(onSite, other.onSite)
				&& Objects.equals(qtyPerc, other.qtyPerc) && Objects.equals(voluntary, other.voluntary)
				&& Objects.equals(voluntarySources, other.voluntarySources);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AllergenListDataItem [qtyPerc=" + qtyPerc + ", voluntary=" + voluntary + ", inVoluntary=" + inVoluntary + ", allergenValue="
				+ allergenValue + ", voluntarySources=" + voluntarySources + ", inVoluntarySources=" + inVoluntarySources + ", allergen=" + allergen
				+ "]";
	}

}
