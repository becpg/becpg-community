/*
 * 
 */
package fr.becpg.repo.product.data.allergen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

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
	
	private List<NodeRef> allergenSubset = new ArrayList<>();

	@AlfProp
	@AlfQname(qname = "bcpg:charactName")
	public String getCharactName() {
		return charactName;
	}

	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:allergenCode")
	public String getAllergenCode() {
		return allergenCode;
	}

	public void setAllergenCode(String allergenCode) {
		this.allergenCode = allergenCode;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:allergenType")
	public String getAllergenType() {
		return allergenType;
	}

	public void setAllergenType(String allergenType) {
		this.allergenType = allergenType;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:allergenRegulatoryThreshold")
	public Double getAllergenRegulatoryThreshold() {
		return allergenRegulatoryThreshold;
	}

	public void setAllergenRegulatoryThreshold(Double allergenRegulatoryThreshold) {
		this.allergenRegulatoryThreshold = allergenRegulatoryThreshold;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:allergenInVoluntaryRegulatoryThreshold")
	public Double getAllergenInVoluntaryRegulatoryThreshold() {
		return allergenInVoluntaryRegulatoryThreshold;
	}

	public void setAllergenInVoluntaryRegulatoryThreshold(Double allergenInVoluntaryRegulatoryThreshold) {
		this.allergenInVoluntaryRegulatoryThreshold = allergenInVoluntaryRegulatoryThreshold;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:allergenSubset")
	public List<NodeRef> getAllergenSubset() {
		return allergenSubset;
	}

	public void setAllergenSubset(List<NodeRef> allergenSubset) {
		this.allergenSubset = allergenSubset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(allergenCode, allergenInVoluntaryRegulatoryThreshold, allergenRegulatoryThreshold, allergenSubset,
				allergenType, charactName);
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
		AllergenItem other = (AllergenItem) obj;
		return Objects.equals(allergenCode, other.allergenCode)
				&& Objects.equals(allergenInVoluntaryRegulatoryThreshold, other.allergenInVoluntaryRegulatoryThreshold)
				&& Objects.equals(allergenRegulatoryThreshold, other.allergenRegulatoryThreshold)
				&& Objects.equals(allergenSubset, other.allergenSubset) && Objects.equals(allergenType, other.allergenType)
				&& Objects.equals(charactName, other.charactName);
	}

	@Override
	public String toString() {
		return "AllergenItem [charactName=" + charactName + ", allergenCode=" + allergenCode + ", allergenType=" + allergenType
				+ ", allergenRegulatoryThreshold=" + allergenRegulatoryThreshold + ", allergenInVoluntaryRegulatoryThreshold="
				+ allergenInVoluntaryRegulatoryThreshold + ", allergenSubset=" + allergenSubset + "]";
	}

}
