/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.regulatory.RegulatoryEntity;
import fr.becpg.repo.regulatory.RegulatoryResult;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>RegulatoryListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:regulatoryList")
public class RegulatoryListDataItem  extends BeCPGDataObject implements RegulatoryEntity {

	private static final long serialVersionUID = 6048458461427271748L;

	private List<NodeRef> regulatoryCountriesRef;
	
	private List<NodeRef> regulatoryUsagesRef;
	
	private RegulatoryResult regulatoryResult;
	
	private SystemState regulatoryState;
	
	private Date validationDate;
	
	private String requirementChecksum;
	
	private String regulatoryRecipeId;
	
	private List<NodeRef> limitingIngredients;
	
	private Double maximumDosage;
	
	/**
	 * <p>Getter for the field <code>maximumDosage</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryMaximumDosage")
	public Double getMaximumDosage() {
		return maximumDosage;
	}
	
	/**
	 * <p>Setter for the field <code>maximumDosage</code>.</p>
	 *
	 * @param maximumDosage a {@link java.lang.Double} object
	 */
	public void setMaximumDosage(Double maximumDosage) {
		this.maximumDosage = maximumDosage;
	}
	
	/**
	 * <p>Getter for the field <code>limitingIngredients</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:limitingIngredient")
	public List<NodeRef> getLimitingIngredients() {
		return limitingIngredients;
	}
	
	/**
	 * <p>Setter for the field <code>limitingIngredients</code>.</p>
	 *
	 * @param limitingIngredient a {@link java.util.List} object
	 */
	public void setLimitingIngredients(List<NodeRef> limitingIngredient) {
		this.limitingIngredients = limitingIngredient;
	}
	
	/**
	 * <p>Getter for the field <code>regulatoryRecipeId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryRecipeId")
	public String getRegulatoryRecipeId() {
		return regulatoryRecipeId;
	}
	
	/** {@inheritDoc} */
	public void setRegulatoryRecipeId(String regulatoryRecipeId) {
		this.regulatoryRecipeId = regulatoryRecipeId;
	}
	
	/**
	 * <p>Setter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @param requirementChecksum a {@link java.lang.String} object
	 */
	public void setRequirementChecksum(String requirementChecksum) {
		this.requirementChecksum = requirementChecksum;
	}
	
	/**
	 * <p>Getter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:requirementChecksum")
	public String getRequirementChecksum() {
		return requirementChecksum;
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
	 * <p>Getter for the field <code>regulatoryResult</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RegulatoryResult} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryResult")
	public RegulatoryResult getRegulatoryResult() {
		return regulatoryResult;
	}

	/** {@inheritDoc} */
	public void setRegulatoryResult(RegulatoryResult regulatoryResult) {
		this.regulatoryResult = regulatoryResult;
	}

	/**
	 * <p>Getter for the field <code>regulatoryState</code>.</p>
	 *
	 * @return a {@link fr.becpg.model.SystemState} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryState")
	public SystemState getRegulatoryState() {
		return regulatoryState;
	}

	/**
	 * <p>Setter for the field <code>regulatoryState</code>.</p>
	 *
	 * @param regulatoryState a {@link fr.becpg.model.SystemState} object
	 */
	public void setRegulatoryState(SystemState regulatoryState) {
		this.regulatoryState = regulatoryState;
	}

	/**
	 * <p>Getter for the field <code>validationDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryValidationDate")
	public Date getValidationDate() {
		return validationDate;
	}

	/**
	 * <p>Setter for the field <code>validationDate</code>.</p>
	 *
	 * @param validationDate a {@link java.util.Date} object
	 */
	public void setValidationDate(Date validationDate) {
		this.validationDate = validationDate;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(regulatoryCountriesRef, regulatoryResult, regulatoryState, regulatoryUsagesRef, validationDate);
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
		RegulatoryListDataItem other = (RegulatoryListDataItem) obj;
		return Objects.equals(regulatoryCountriesRef, other.regulatoryCountriesRef) && Objects.equals(regulatoryResult, other.regulatoryResult)
				&& regulatoryState == other.regulatoryState && Objects.equals(regulatoryUsagesRef, other.regulatoryUsagesRef)
				&& Objects.equals(validationDate, other.validationDate);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "RegulatoryListDataItem [regulatoryCountries=" + regulatoryCountriesRef + ", regulatoryUsages=" + regulatoryUsagesRef + ", regulatoryResult="
				+ regulatoryResult + ", regulatoryState=" + regulatoryState + ", validationDate=" + validationDate + "]";
	}
	
}
