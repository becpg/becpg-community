/*
 * 
 */
package fr.becpg.repo.product.data.allergen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>AllergenItem class.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:allergen")
@AlfCacheable(isCharact = true)
public class AllergenItem extends BeCPGDataObject {
	
	private static final long serialVersionUID = 1907139408734937414L;
	
	private String charactName;
	
	private String allergenCode;
	
	private String allergenType;
	
	private Double allergenRegulatoryThreshold;
	
	private Double allergenInVoluntaryRegulatoryThreshold;

	private MLText allergenOthersLegalName;

	private List<NodeRef> allergenSubset = new ArrayList<>();

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
	 * <p>Getter for the field <code>allergenCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenCode")
	public String getAllergenCode() {
		return allergenCode;
	}

	/**
	 * <p>Setter for the field <code>allergenCode</code>.</p>
	 *
	 * @param allergenCode a {@link java.lang.String} object
	 */
	public void setAllergenCode(String allergenCode) {
		this.allergenCode = allergenCode;
	}

	/**
	 * <p>Getter for the field <code>allergenType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenType")
	public String getAllergenType() {
		return allergenType;
	}

	/**
	 * <p>Setter for the field <code>allergenType</code>.</p>
	 *
	 * @param allergenType a {@link java.lang.String} object
	 */
	public void setAllergenType(String allergenType) {
		this.allergenType = allergenType;
	}

	/**
	 * <p>Getter for the field <code>allergenRegulatoryThreshold</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenRegulatoryThreshold")
	public Double getAllergenRegulatoryThreshold() {
		return allergenRegulatoryThreshold;
	}

	/**
	 * <p>Setter for the field <code>allergenRegulatoryThreshold</code>.</p>
	 *
	 * @param allergenRegulatoryThreshold a {@link java.lang.Double} object
	 */
	public void setAllergenRegulatoryThreshold(Double allergenRegulatoryThreshold) {
		this.allergenRegulatoryThreshold = allergenRegulatoryThreshold;
	}

	/**
	 * <p>Getter for the field <code>allergenInVoluntaryRegulatoryThreshold</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:allergenInVoluntaryRegulatoryThreshold")
	public Double getAllergenInVoluntaryRegulatoryThreshold() {
		return allergenInVoluntaryRegulatoryThreshold;
	}

	/**
	 * <p>Setter for the field <code>allergenInVoluntaryRegulatoryThreshold</code>.</p>
	 *
	 * @param allergenInVoluntaryRegulatoryThreshold a {@link java.lang.Double} object
	 */
	public void setAllergenInVoluntaryRegulatoryThreshold(Double allergenInVoluntaryRegulatoryThreshold) {
		this.allergenInVoluntaryRegulatoryThreshold = allergenInVoluntaryRegulatoryThreshold;
	}

	/**
	 * <p>Getter for the field <code>allergenOthersLegalName</code>.</p>
	 *
	 * <p>Localized label substituted to {@code bcpg:legalName} when this allergen is
	 * a category (has {@code allergenSubset}) and at least one of its children is
	 * declared as involuntary on the current product. Typical usage is "autres
	 * fruits à coque" / "autres céréales contenant du gluten" for locales that
	 * require a grouped rendering.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:allergenOthersLegalName")
	public MLText getAllergenOthersLegalName() {
		return allergenOthersLegalName;
	}

	/**
	 * <p>Setter for the field <code>allergenOthersLegalName</code>.</p>
	 *
	 * @param allergenOthersLegalName a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setAllergenOthersLegalName(MLText allergenOthersLegalName) {
		this.allergenOthersLegalName = allergenOthersLegalName;
	}

	/**
	 * <p>Getter for the field <code>allergenSubset</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:allergenSubset")
	public List<NodeRef> getAllergenSubset() {
		return allergenSubset;
	}

	/**
	 * <p>Setter for the field <code>allergenSubset</code>.</p>
	 *
	 * @param allergenSubset a {@link java.util.List} object
	 */
	public void setAllergenSubset(List<NodeRef> allergenSubset) {
		this.allergenSubset = allergenSubset;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(allergenCode, allergenInVoluntaryRegulatoryThreshold, allergenOthersLegalName,
				allergenRegulatoryThreshold, allergenSubset, allergenType, charactName);
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
		AllergenItem other = (AllergenItem) obj;
		return Objects.equals(allergenCode, other.allergenCode)
				&& Objects.equals(allergenInVoluntaryRegulatoryThreshold, other.allergenInVoluntaryRegulatoryThreshold)
				&& Objects.equals(allergenOthersLegalName, other.allergenOthersLegalName)
				&& Objects.equals(allergenRegulatoryThreshold, other.allergenRegulatoryThreshold)
				&& Objects.equals(allergenSubset, other.allergenSubset) && Objects.equals(allergenType, other.allergenType)
				&& Objects.equals(charactName, other.charactName);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AllergenItem [charactName=" + charactName + ", allergenCode=" + allergenCode + ", allergenType=" + allergenType
				+ ", allergenRegulatoryThreshold=" + allergenRegulatoryThreshold + ", allergenInVoluntaryRegulatoryThreshold="
				+ allergenInVoluntaryRegulatoryThreshold + ", allergenOthersLegalName=" + allergenOthersLegalName
				+ ", allergenSubset=" + allergenSubset + "]";
	}

}
