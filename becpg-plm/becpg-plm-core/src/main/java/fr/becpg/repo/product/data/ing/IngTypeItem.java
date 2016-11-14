/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.data.ing;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:ingTypeItem")
public class IngTypeItem extends AbstractLabelingComponent{

	private static final long serialVersionUID = 182156222574786727L;

    public static final IngTypeItem DEFAULT_GROUP = new IngTypeItem();
	
	private Double decThreshold;
	
	private String lvValue;
	
	private MLText pluralLegalName;
	
	public IngTypeItem(){
		super();
	}

	public IngTypeItem(IngTypeItem ingTypeItem) {
		super(ingTypeItem);
		this.decThreshold = ingTypeItem.decThreshold;
		this.pluralLegalName = ingTypeItem.pluralLegalName;
		this.lvValue = ingTypeItem.lvValue;
	}

	
	@AlfProp
	@AlfQname(qname = "bcpg:lvValue")
	public String getLvValue() {
		return lvValue;
	}

	public void setLvValue(String lvValue) {
		this.lvValue = lvValue;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:ingTypeDecThreshold")
	public Double getDecThreshold() {
		return decThreshold;
	}


	public void setDecThreshold(Double decThreshold) {
		this.decThreshold = decThreshold;
	}

	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:ingTypePluralLegalName")
	public MLText getPluralLegalName() {
		return pluralLegalName;
	}

	public void setPluralLegalName(MLText pluralLegalName) {
		this.pluralLegalName = pluralLegalName;
	}
	
	
	public String getPluralLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(pluralLegalName, locale);
		
		if(ret==null || ret.isEmpty()){
			return getLegalName(locale);
		}
		
		return ret;
	}
	
	
	@Override
	public String getLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(legalName, locale);

		if(ret==null || ret.isEmpty()){
			return lvValue;
		}
		
		return ret;
	}

	@Override
	public IngTypeItem clone() {
		return new IngTypeItem(this);
	}
	
	@Override
	public String toString() {
		return "IngTypeItem [decThreshold=" + decThreshold + ", lvValue=" + lvValue + ", pluralLegalName=" + pluralLegalName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((decThreshold == null) ? 0 : decThreshold.hashCode());
		result = prime * result + ((lvValue == null) ? 0 : lvValue.hashCode());
		result = prime * result + ((pluralLegalName == null) ? 0 : pluralLegalName.hashCode());
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
		IngTypeItem other = (IngTypeItem) obj;
		if (decThreshold == null) {
			if (other.decThreshold != null)
				return false;
		} else if (!decThreshold.equals(other.decThreshold))
			return false;
		if (lvValue == null) {
			if (other.lvValue != null)
				return false;
		} else if (!lvValue.equals(other.lvValue))
			return false;
		if (pluralLegalName == null) {
			if (other.pluralLegalName != null)
				return false;
		} else if (!pluralLegalName.equals(other.pluralLegalName))
			return false;
		return true;
	}


}
