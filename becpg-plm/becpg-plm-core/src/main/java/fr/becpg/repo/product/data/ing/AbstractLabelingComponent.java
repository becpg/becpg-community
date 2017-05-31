/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public abstract class AbstractLabelingComponent extends BeCPGDataObject implements LabelingComponent, Comparable<LabelingComponent>, Cloneable {

	private static final long serialVersionUID = 270866664168102414L;

	protected Double qty = 0d;
	
	protected Double volume = 0d;

	protected MLText legalName;
	
	private boolean isPlural = false;
	
	private MLText pluralLegalName;
	
	private Set<NodeRef> allergens = new HashSet<NodeRef>();
	
	private Set<NodeRef> geoOrigins = new HashSet<NodeRef>();
	
	public AbstractLabelingComponent() {
		super();
	}

	public AbstractLabelingComponent(AbstractLabelingComponent abstractLabelingComponent) 
	{
		super(abstractLabelingComponent);
		this.pluralLegalName = abstractLabelingComponent.pluralLegalName;
	    this.qty = abstractLabelingComponent.qty;
	    this.volume = abstractLabelingComponent.volume;
	    this.legalName = abstractLabelingComponent.legalName;
	    this.allergens = abstractLabelingComponent.allergens;
	    this.geoOrigins = abstractLabelingComponent.geoOrigins;
	    this.isPlural = abstractLabelingComponent.isPlural;
	}

	
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:legalName")
	public MLText getLegalName() {
		return legalName;
	}

	public void setLegalName(MLText legalName) {
		this.legalName = legalName;
	}

	@Override
	public String getLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(legalName, locale);
		
		if(ret==null || ret.isEmpty()){
			return name;
		}
		
		return ret;
	}

	
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:pluralLegalName")
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
	
	public boolean isPlural() {
		return isPlural && pluralLegalName!=null && !MLTextHelper.isEmpty(pluralLegalName);
	}

	public void setPlural(boolean isPlural) {
		this.isPlural = isPlural;
	}

	@Override
	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}
	
	
	@Override
	public Double getVolume() {
		return volume;
	}


	public void setVolume(Double volume) {
		this.volume = volume;
	}

	@Override
	public Set<NodeRef> getAllergens() {
		return allergens;
	}

	public void setAllergens(Set<NodeRef> allergens) {
		this.allergens = allergens;
	}

	@Override
	public Set<NodeRef> getGeoOrigins() {
		return geoOrigins;
	}

	public void setGeoOrigins(Set<NodeRef> geoOrigins) {
		this.geoOrigins = geoOrigins;
	}

	public abstract AbstractLabelingComponent clone();
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((allergens == null) ? 0 : allergens.hashCode());
		result = prime * result + ((geoOrigins == null) ? 0 : geoOrigins.hashCode());
		result = prime * result + ((legalName == null) ? 0 : legalName.hashCode());
		result = prime * result + ((pluralLegalName == null) ? 0 : pluralLegalName.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		result = prime * result + ((volume == null) ? 0 : volume.hashCode());
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
		AbstractLabelingComponent other = (AbstractLabelingComponent) obj;
		if (allergens == null) {
			if (other.allergens != null)
				return false;
		} else if (!allergens.equals(other.allergens))
			return false;
		if (geoOrigins == null) {
			if (other.geoOrigins != null)
				return false;
		} else if (!geoOrigins.equals(other.geoOrigins))
			return false;
		if (legalName == null) {
			if (other.legalName != null)
				return false;
		} else if (!legalName.equals(other.legalName))
			return false;
		if (pluralLegalName == null) {
			if (other.pluralLegalName != null)
				return false;
		} else if (!pluralLegalName.equals(other.pluralLegalName))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		if (volume == null) {
			if (other.volume != null)
				return false;
		} else if (!volume.equals(other.volume))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractLabelingComponent [qty=" + qty + ", volume=" + volume + ", legalName=" + legalName + ", nodeRef=" + nodeRef
				+ ", name=" + name + "]";
	}

	@Override
	public int compareTo(LabelingComponent lblComponent) {

		if (lblComponent instanceof CompositeLabeling && ((CompositeLabeling) lblComponent).isGroup()
				&& !(this instanceof CompositeLabeling && ((CompositeLabeling) this).isGroup())) {
			return 1;
		}

		if (!(lblComponent instanceof CompositeLabeling && ((CompositeLabeling) lblComponent).isGroup()) && 
				(this instanceof CompositeLabeling
				&& ((CompositeLabeling) this).isGroup())) {
			return -1;
		}

		if (lblComponent.getQty() != null && this.getQty() != null) {
			return Double.compare(lblComponent.getQty(), this.getQty());
		} else if (this.getQty() == null && lblComponent.getQty() != null) {
			return 1; // after
		} else if (this.getQty() != null && lblComponent.getQty() == null) {
			return -1; // before
		}
		return 0;// equals
	}

}
