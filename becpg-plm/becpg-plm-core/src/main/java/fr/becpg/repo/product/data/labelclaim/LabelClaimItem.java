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

/**
 * <p>LabelClaimItem class.</p>
 *
 * @author matthieu
 */
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
	
	private Boolean isCertificationPropagateUp;
	
	private Boolean isCharactPropagateUp;
	
	private Boolean isManualListItem;

	/**
	 * <p>Getter for the field <code>charactName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:charactName")
	public String getCharactName() {
		return charactName;
	}

	/**
	 * <p>Setter for the field <code>charactName</code>.</p>
	 *
	 * @param charactName a {@link java.lang.String} object
	 */
	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}

	/**
	 * <p>Getter for the field <code>labelClaimCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimCode")
	public String getLabelClaimCode() {
		return labelClaimCode;
	}

	/**
	 * <p>Setter for the field <code>labelClaimCode</code>.</p>
	 *
	 * @param labelClaimCode a {@link java.lang.String} object
	 */
	public void setLabelClaimCode(String labelClaimCode) {
		this.labelClaimCode = labelClaimCode;
	}

	/**
	 * <p>Getter for the field <code>labelClaimType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimType")
	public String getLabelClaimType() {
		return labelClaimType;
	}

	/**
	 * <p>Setter for the field <code>labelClaimType</code>.</p>
	 *
	 * @param labelClaimType a {@link java.lang.String} object
	 */
	public void setLabelClaimType(String labelClaimType) {
		this.labelClaimType = labelClaimType;
	}

	/**
	 * <p>Getter for the field <code>labelClaimFormula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimFormula")
	public String getLabelClaimFormula() {
		return labelClaimFormula;
	}

	/**
	 * <p>Setter for the field <code>labelClaimFormula</code>.</p>
	 *
	 * @param labelClaimFormula a {@link java.lang.String} object
	 */
	public void setLabelClaimFormula(String labelClaimFormula) {
		this.labelClaimFormula = labelClaimFormula;
	}

	/**
	 * <p>Getter for the field <code>labelClaimRegulatoryThreshold</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:labelClaimRegulatoryThreshold")
	public Double getLabelClaimRegulatoryThreshold() {
		return labelClaimRegulatoryThreshold;
	}

	/**
	 * <p>Setter for the field <code>labelClaimRegulatoryThreshold</code>.</p>
	 *
	 * @param labelClaimRegulatoryThreshold a {@link java.lang.Double} object
	 */
	public void setLabelClaimRegulatoryThreshold(Double labelClaimRegulatoryThreshold) {
		this.labelClaimRegulatoryThreshold = labelClaimRegulatoryThreshold;
	}

	

	/**
	 * <p>Getter for the field <code>isCertificationPropagateUp</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:isCertificationPropagateUp")
	public Boolean getIsCertificationPropagateUp() {
		return isCertificationPropagateUp;
	}

	/**
	 * <p>Setter for the field <code>isCertificationPropagateUp</code>.</p>
	 *
	 * @param isCertificationPropagateUp a {@link java.lang.Boolean} object
	 */
	public void setIsCertificationPropagateUp(Boolean isCertificationPropagateUp) {
		this.isCertificationPropagateUp = isCertificationPropagateUp;
	}

	/**
	 * <p>Getter for the field <code>isManualListItem</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:isManualListItem")
	public Boolean getIsManualListItem() {
		return isManualListItem;
	}

	/**
	 * <p>Setter for the field <code>isManualListItem</code>.</p>
	 *
	 * @param isManualListItem a {@link java.lang.Boolean} object
	 */
	public void setIsManualListItem(Boolean isManualListItem) {
		this.isManualListItem = isManualListItem;
	}

	/**
	 * <p>Getter for the field <code>isCharactPropagateUp</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:isCharactPropagateUp")
	public Boolean getIsCharactPropagateUp() {
		return isCharactPropagateUp;
	}

	/**
	 * <p>Setter for the field <code>isCharactPropagateUp</code>.</p>
	 *
	 * @param isCharactPropagateUp a {@link java.lang.Boolean} object
	 */
	public void setIsCharactPropagateUp(Boolean isCharactPropagateUp) {
		this.isCharactPropagateUp = isCharactPropagateUp;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(charactName, isCertificationPropagateUp, isCharactPropagateUp,
				isManualListItem, labelClaimCode, labelClaimFormula, labelClaimRegulatoryThreshold, labelClaimType);
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
		LabelClaimItem other = (LabelClaimItem) obj;
		return Objects.equals(charactName, other.charactName) && Objects.equals(isCertificationPropagateUp, other.isCertificationPropagateUp)
				&& Objects.equals(isCharactPropagateUp, other.isCharactPropagateUp)
			    && Objects.equals(isManualListItem, other.isManualListItem)
				&& Objects.equals(labelClaimCode, other.labelClaimCode) && Objects.equals(labelClaimFormula, other.labelClaimFormula)
				&& Objects.equals(labelClaimRegulatoryThreshold, other.labelClaimRegulatoryThreshold)
				&& Objects.equals(labelClaimType, other.labelClaimType);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LabelClaimItem [charactName=" + charactName + ", labelClaimCode=" + labelClaimCode + ", labelClaimType=" + labelClaimType
				+ ", labelClaimFormula=" + labelClaimFormula + ", labelClaimRegulatoryThreshold=" + labelClaimRegulatoryThreshold
				+ ", isCertificationPropagateUp=" + isCertificationPropagateUp
				+ ", isCharactPropagateUp=" + isCharactPropagateUp + ", isManualListItem=" + isManualListItem + "]";
	}
	
}
