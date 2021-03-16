/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.Date;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.formulation.CacheableEntity;
import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>NutDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:nut")
@AlfCacheable(isCharact = true)
public class NutDataItem extends BeCPGDataObject implements CacheableEntity {


	private static final long serialVersionUID = -4580421935974923617L;

	
	private String nutCode;

	private MLText charactName;
	
	private String nutGroup;
	
	private String nutType;
	
	private String nutUnit;
	
	private Double nutGDA;
	
	private Double nutUL;
	
	private String nutFormula;

	private String nutColor;
	
	private Date modifiedDate;
	
	
	/**
	 * <p>Getter for the field <code>modifiedDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfReadOnly
	@AlfQname(qname = "cm:modified")
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * <p>Setter for the field <code>modifiedDate</code>.</p>
	 *
	 * @param modifiedDate a {@link java.util.Date} object.
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	/**
	 * <p>Getter for the field <code>nutCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="gs1:nutrientTypeCode")
	public String getNutCode() {
		return nutCode;
	}

	/**
	 * <p>Setter for the field <code>nutCode</code>.</p>
	 *
	 * @param nutCode a {@link java.lang.String} object.
	 */
	public void setNutCode(String nutCode) {
		this.nutCode = nutCode;
	}

	/**
	 * <p>Getter for the field <code>charactName</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:charactName")
	public MLText getCharactName() {
		return charactName;
	}

	/**
	 * <p>Setter for the field <code>charactName</code>.</p>
	 *
	 * @param charactName a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setCharactName(MLText charactName) {
		this.charactName = charactName;
	}

	/**
	 * <p>Getter for the field <code>nutGroup</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nutGroup")
	public String getNutGroup() {
		return nutGroup;
	}

	/**
	 * <p>Setter for the field <code>nutGroup</code>.</p>
	 *
	 * @param nutGroup a {@link java.lang.String} object.
	 */
	public void setNutGroup(String nutGroup) {
		this.nutGroup = nutGroup;
	}

	/**
	 * <p>Getter for the field <code>nutType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nutType")
	public String getNutType() {
		return nutType;
	}

	/**
	 * <p>Setter for the field <code>nutType</code>.</p>
	 *
	 * @param nutType a {@link java.lang.String} object.
	 */
	public void setNutType(String nutType) {
		this.nutType = nutType;
	}

	/**
	 * <p>Getter for the field <code>nutUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nutUnit")
	public String getNutUnit() {
		return nutUnit;
	}

	/**
	 * <p>Setter for the field <code>nutUnit</code>.</p>
	 *
	 * @param nutUnit a {@link java.lang.String} object.
	 */
	public void setNutUnit(String nutUnit) {
		this.nutUnit = nutUnit;
	}

	/**
	 * <p>Getter for the field <code>nutGDA</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:nutGDA")
	public Double getNutGDA() {
		return nutGDA;
	}

	/**
	 * <p>Setter for the field <code>nutGDA</code>.</p>
	 *
	 * @param nutGDA a {@link java.lang.Double} object.
	 */
	public void setNutGDA(Double nutGDA) {
		this.nutGDA = nutGDA;
	}

	/**
	 * <p>Getter for the field <code>nutUL</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:nutUL")
	public Double getNutUL() {
		return nutUL;
	}

	/**
	 * <p>Setter for the field <code>nutUL</code>.</p>
	 *
	 * @param nutUL a {@link java.lang.Double} object.
	 */
	public void setNutUL(Double nutUL) {
		this.nutUL = nutUL;
	}


	/**
	 * <p>Getter for the field <code>nutFormula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nutFormula")
	public String getNutFormula() {
		return nutFormula;
	}

	/**
	 * <p>Setter for the field <code>nutFormula</code>.</p>
	 *
	 * @param nutFormula a {@link java.lang.String} object.
	 */
	public void setNutFormula(String nutFormula) {
		this.nutFormula = nutFormula;
	}
	
	/**
	 * <p>Getter for the field <code>nutColor</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:color")
	public String getNutColor() {
		return nutColor;
	}

	/**
	 * <p>Setter for the field <code>nutColor</code>.</p>
	 *
	 * @param nutColor a {@link java.lang.String} object.
	 */
	public void setNutColor(String nutColor) {
		this.nutColor = nutColor;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charactName == null) ? 0 : charactName.hashCode());
		result = prime * result + ((nutCode == null) ? 0 : nutCode.hashCode());
		result = prime * result + ((nutColor == null) ? 0 : nutColor.hashCode());
		result = prime * result + ((nutFormula == null) ? 0 : nutFormula.hashCode());
		result = prime * result + ((nutGDA == null) ? 0 : nutGDA.hashCode());
		result = prime * result + ((nutGroup == null) ? 0 : nutGroup.hashCode());
		result = prime * result + ((nutType == null) ? 0 : nutType.hashCode());
		result = prime * result + ((nutUL == null) ? 0 : nutUL.hashCode());
		result = prime * result + ((nutUnit == null) ? 0 : nutUnit.hashCode());
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
		if (nutColor == null) {
			if (other.nutColor != null)
				return false;
		} else if (!nutColor.equals(other.nutColor))
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NutDataItem [nutCode=" + nutCode + ", charactName=" + charactName + ", nutGroup=" + nutGroup + ", nutType=" + nutType + ", nutUnit="
				+ nutUnit + ", nutGDA=" + nutGDA + ", nutUL=" + nutUL + ", nutFormula=" + nutFormula + ", nutColor=" + nutColor + "]";
	}
}
	
