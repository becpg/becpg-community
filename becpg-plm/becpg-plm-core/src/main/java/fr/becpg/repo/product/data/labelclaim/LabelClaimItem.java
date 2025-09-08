/*
 * 
 */
package fr.becpg.repo.product.data.labelclaim;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:labelClaim")
@AlfCacheable(isCharact = true)
public class LabelClaimItem extends BeCPGDataObject {
	
	private static final long serialVersionUID = 1L;

	private String charactName;
	
	private String labelClaimCode;
	
	private String labelClaimType;
	
	private String labelClaimFormula;
	
	private Double labelClaimRegulatoryThreshold;
	
	private Boolean isLabelClaimPropagateUp;
	
	private Boolean isCharactPropagateUp;
	
	private Boolean isManualListItem;

	@AlfProp
	@AlfQname(qname = "bcpg:charactName")
	public String getCharactName() {
		return charactName;
	}

	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimCode")
	public String getLabelClaimCode() {
		return labelClaimCode;
	}

	public void setLabelClaimCode(String labelClaimCode) {
		this.labelClaimCode = labelClaimCode;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimType")
	public String getLabelClaimType() {
		return labelClaimType;
	}

	public void setLabelClaimType(String labelClaimType) {
		this.labelClaimType = labelClaimType;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimFormula")
	public String getLabelClaimFormula() {
		return labelClaimFormula;
	}

	public void setLabelClaimFormula(String labelClaimFormula) {
		this.labelClaimFormula = labelClaimFormula;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimRegulatoryThreshold")
	public Double getLabelClaimRegulatoryThreshold() {
		return labelClaimRegulatoryThreshold;
	}

	public void setLabelClaimRegulatoryThreshold(Double labelClaimRegulatoryThreshold) {
		this.labelClaimRegulatoryThreshold = labelClaimRegulatoryThreshold;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:isLabelClaimPropagateUp")
	public Boolean getIsLabelClaimPropagateUp() {
		return isLabelClaimPropagateUp;
	}

	public void setIsLabelClaimPropagateUp(Boolean isLabelClaimPropagateUp) {
		this.isLabelClaimPropagateUp = isLabelClaimPropagateUp;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:isManualListItem")
	public Boolean getIsManualListItem() {
		return isManualListItem;
	}

	public void setIsManualListItem(Boolean isManualListItem) {
		this.isManualListItem = isManualListItem;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:isCharactPropagateUp")
	public Boolean getIsCharactPropagateUp() {
		return isCharactPropagateUp;
	}

	public void setIsCharactPropagateUp(Boolean isCharactPropagateUp) {
		this.isCharactPropagateUp = isCharactPropagateUp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(charactName, isCharactPropagateUp, isLabelClaimPropagateUp, isManualListItem, labelClaimCode,
				labelClaimFormula, labelClaimRegulatoryThreshold, labelClaimType);
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
		LabelClaimItem other = (LabelClaimItem) obj;
		return Objects.equals(charactName, other.charactName) && Objects.equals(isCharactPropagateUp, other.isCharactPropagateUp)
				&& Objects.equals(isLabelClaimPropagateUp, other.isLabelClaimPropagateUp) && Objects.equals(isManualListItem, other.isManualListItem)
				&& Objects.equals(labelClaimCode, other.labelClaimCode) && Objects.equals(labelClaimFormula, other.labelClaimFormula)
				&& Objects.equals(labelClaimRegulatoryThreshold, other.labelClaimRegulatoryThreshold)
				&& Objects.equals(labelClaimType, other.labelClaimType);
	}

	@Override
	public String toString() {
		return "LabelClaimItem [charactName=" + charactName + ", labelClaimCode=" + labelClaimCode + ", labelClaimType=" + labelClaimType
				+ ", labelClaimFormula=" + labelClaimFormula + ", labelClaimRegulatoryThreshold=" + labelClaimRegulatoryThreshold
				+ ", isLabelClaimPropagateUp=" + isLabelClaimPropagateUp + ", isCharactPropagateUp=" + isCharactPropagateUp + ", isManualListItem="
				+ isManualListItem + "]";
	}
	
}
