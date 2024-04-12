/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.RegulatoryEntity;
import fr.becpg.repo.product.data.constraints.RegulatoryResult;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

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
	
	private NodeRef limitingIngredient;
	
	private Double maximumDosage;
	
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryMaximumDosage")
	public Double getMaximumDosage() {
		return maximumDosage;
	}
	
	public void setMaximumDosage(Double maximumDosage) {
		this.maximumDosage = maximumDosage;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:limitingIngredient")
	public NodeRef getLimitingIngredient() {
		return limitingIngredient;
	}
	
	public void setLimitingIngredient(NodeRef limitingIngredient) {
		this.limitingIngredient = limitingIngredient;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryRecipeId")
	public String getRegulatoryRecipeId() {
		return regulatoryRecipeId;
	}
	
	public void setRegulatoryRecipeId(String regulatoryRecipeId) {
		this.regulatoryRecipeId = regulatoryRecipeId;
	}
	
	public void setRequirementChecksum(String requirementChecksum) {
		this.requirementChecksum = requirementChecksum;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:requirementChecksum")
	public String getRequirementChecksum() {
		return requirementChecksum;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryResult")
	public RegulatoryResult getRegulatoryResult() {
		return regulatoryResult;
	}

	public void setRegulatoryResult(RegulatoryResult regulatoryResult) {
		this.regulatoryResult = regulatoryResult;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryState")
	public SystemState getRegulatoryState() {
		return regulatoryState;
	}

	public void setRegulatoryState(SystemState regulatoryState) {
		this.regulatoryState = regulatoryState;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryValidationDate")
	public Date getValidationDate() {
		return validationDate;
	}

	public void setValidationDate(Date validationDate) {
		this.validationDate = validationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(regulatoryCountriesRef, regulatoryResult, regulatoryState, regulatoryUsagesRef, validationDate);
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
		RegulatoryListDataItem other = (RegulatoryListDataItem) obj;
		return Objects.equals(regulatoryCountriesRef, other.regulatoryCountriesRef) && Objects.equals(regulatoryResult, other.regulatoryResult)
				&& regulatoryState == other.regulatoryState && Objects.equals(regulatoryUsagesRef, other.regulatoryUsagesRef)
				&& Objects.equals(validationDate, other.validationDate);
	}

	@Override
	public String toString() {
		return "RegulatoryListDataItem [regulatoryCountries=" + regulatoryCountriesRef + ", regulatoryUsages=" + regulatoryUsagesRef + ", regulatoryResult="
				+ regulatoryResult + ", regulatoryState=" + regulatoryState + ", validationDate=" + validationDate + "]";
	}
	
}
