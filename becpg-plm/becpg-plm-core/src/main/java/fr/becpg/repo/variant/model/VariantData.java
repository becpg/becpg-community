package fr.becpg.repo.variant.model;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname="bcpg:variant")
public class VariantData extends BeCPGDataObject {

	protected Boolean isDefaultVariant;

	@AlfProp
	@AlfQname(qname="bcpg:isDefaultVariant")
	public Boolean getIsDefaultVariant() {
		return isDefaultVariant;
	}

	public void setIsDefaultVariant(Boolean isDefaultVariant) {
		this.isDefaultVariant = isDefaultVariant;
	}

	@Override
	public String toString() {
		return "VariantData [isDefaultVariant=" + isDefaultVariant + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((isDefaultVariant == null) ? 0 : isDefaultVariant.hashCode());
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
		VariantData other = (VariantData) obj;
		if (isDefaultVariant == null) {
			if (other.isDefaultVariant != null)
				return false;
		} else if (!isDefaultVariant.equals(other.isDefaultVariant))
			return false;
		return true;
	}
	
	

}
