/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.repository.model;

import java.util.Date;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;

public abstract class AbstractEffectiveDataItem extends BeCPGDataObject implements EffectiveDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4228655692015575076L;

	protected Date startEffectivity;
	
	protected Date endEffectivity;
	
	
	@AlfProp
	@AlfQname(qname="bcpg:startEffectivity")
	@Override
	public Date getStartEffectivity() {
		return startEffectivity;
	}
	
	@Override
	public void setStartEffectivity(Date startEffectivity) {
		this.startEffectivity = startEffectivity;
	}

	@AlfProp
	@AlfQname(qname="bcpg:endEffectivity")
	@Override
	public Date getEndEffectivity() {
		return endEffectivity;
	}
	
	@Override
	public void setEndEffectivity(Date endEffectivity) {
		this.endEffectivity = endEffectivity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((endEffectivity == null) ? 0 : endEffectivity.hashCode());
		result = prime * result + ((startEffectivity == null) ? 0 : startEffectivity.hashCode());
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
		AbstractEffectiveDataItem other = (AbstractEffectiveDataItem) obj;
		if (endEffectivity == null) {
			if (other.endEffectivity != null)
				return false;
		} else if (!endEffectivity.equals(other.endEffectivity))
			return false;
		if (startEffectivity == null) {
			if (other.startEffectivity != null)
				return false;
		} else if (!startEffectivity.equals(other.startEffectivity))
			return false;
		return true;
	}
	

}
