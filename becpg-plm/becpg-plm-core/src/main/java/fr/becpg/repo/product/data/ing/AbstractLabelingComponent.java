/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public abstract class AbstractLabelingComponent extends BeCPGDataObject implements LabelingComponent, Comparable<LabelingComponent> {

	protected Double qty = 0d;

	protected MLText legalName;
	
	
	public AbstractLabelingComponent() {
		super();
	}


	public AbstractLabelingComponent(AbstractLabelingComponent abstractLabelingComponent) 
	{
		super(abstractLabelingComponent);
	    this.qty = abstractLabelingComponent.qty;
	    this.legalName = abstractLabelingComponent.legalName;
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
		String ret = null;
		if (legalName != null) {
			if (legalName.containsKey(locale)) {
				ret =  legalName.get(locale);
			} else {
				ret =  legalName.getClosestValue(locale);
			}
		}
		if(ret==null || ret.isEmpty()){
			return name;
		}
		
		return ret;
	}

	@Override
	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((legalName == null) ? 0 : legalName.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
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
		if (legalName == null) {
			if (other.legalName != null)
				return false;
		} else if (!legalName.equals(other.legalName))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractIng [qty=" + qty + ", legalName=" + legalName + "]";
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
