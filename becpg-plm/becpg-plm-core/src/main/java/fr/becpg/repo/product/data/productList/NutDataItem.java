/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:nut")
public class NutDataItem extends BeCPGDataObject {


	private static final long serialVersionUID = -4580421935974923617L;

	
	private String nutCode;

	private String charactName;
	
	private String nutGroup;
	
	private String nutType;
	
	private String nutUnit;
	
	private Double nutGDA;
	
	private Double nutUL;
	
	private String nutFormula;

	
	@AlfProp
	@AlfQname(qname="gs1:nutrientTypeCode")
	public String getNutCode() {
		return nutCode;
	}

	public void setNutCode(String nutCode) {
		this.nutCode = nutCode;
	}

	@AlfProp
	@AlfQname(qname="bcpg:charactName")
	public String getCharactName() {
		return charactName;
	}

	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutGroup")
	public String getNutGroup() {
		return nutGroup;
	}

	public void setNutGroup(String nutGroup) {
		this.nutGroup = nutGroup;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutType")
	public String getNutType() {
		return nutType;
	}

	public void setNutType(String nutType) {
		this.nutType = nutType;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutUnit")
	public String getNutUnit() {
		return nutUnit;
	}

	public void setNutUnit(String nutUnit) {
		this.nutUnit = nutUnit;
	}

	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:nutGDA")
	public Double getNutGDA() {
		return nutGDA;
	}

	public void setNutGDA(Double nutGDA) {
		this.nutGDA = nutGDA;
	}

	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:nutUL")
	public Double getNutUL() {
		return nutUL;
	}

	public void setNutUL(Double nutUL) {
		this.nutUL = nutUL;
	}


	@AlfProp
	@AlfQname(qname="bcpg:nutFormula")
	public String getNutFormula() {
		return nutFormula;
	}

	public void setNutFormula(String nutFormula) {
		this.nutFormula = nutFormula;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charactName == null) ? 0 : charactName.hashCode());
		result = prime * result + ((nutCode == null) ? 0 : nutCode.hashCode());
		result = prime * result + ((nutFormula == null) ? 0 : nutFormula.hashCode());
		result = prime * result + ((nutGDA == null) ? 0 : nutGDA.hashCode());
		result = prime * result + ((nutGroup == null) ? 0 : nutGroup.hashCode());
		result = prime * result + ((nutType == null) ? 0 : nutType.hashCode());
		result = prime * result + ((nutUL == null) ? 0 : nutUL.hashCode());
		result = prime * result + ((nutUnit == null) ? 0 : nutUnit.hashCode());
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
		NutDataItem other = (NutDataItem) obj;
		if (charactName == null) {
			if (other.charactName != null)
				return false;
		} else if (!charactName.equals(other.charactName))
			return false;
		if (nutCode == null) {
			if (other.nutCode != null)
				return false;
		} else if (!nutCode.equals(other.nutCode))
			return false;
		if (nutFormula == null) {
			if (other.nutFormula != null)
				return false;
		} else if (!nutFormula.equals(other.nutFormula))
			return false;
		if (nutGDA == null) {
			if (other.nutGDA != null)
				return false;
		} else if (!nutGDA.equals(other.nutGDA))
			return false;
		if (nutGroup == null) {
			if (other.nutGroup != null)
				return false;
		} else if (!nutGroup.equals(other.nutGroup))
			return false;
		if (nutType == null) {
			if (other.nutType != null)
				return false;
		} else if (!nutType.equals(other.nutType))
			return false;
		if (nutUL == null) {
			if (other.nutUL != null)
				return false;
		} else if (!nutUL.equals(other.nutUL))
			return false;
		if (nutUnit == null) {
			if (other.nutUnit != null)
				return false;
		} else if (!nutUnit.equals(other.nutUnit))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NutDataItem [nutCode=" + nutCode + ", charactName=" + charactName + ", nutGroup=" + nutGroup + ", nutType=" + nutType + ", nutUnit="
				+ nutUnit + ", nutGDA=" + nutGDA + ", nutUL=" + nutUL + ", nutFormula=" + nutFormula + "]";
	}
}
	