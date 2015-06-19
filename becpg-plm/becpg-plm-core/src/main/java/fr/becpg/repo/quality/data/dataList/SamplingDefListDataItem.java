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
package fr.becpg.repo.quality.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "qa:samplingDefList")
public class SamplingDefListDataItem extends AbstractSamplingListDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 622827042336832867L;
	private Integer qty;
	private Integer freq;
	private String freqUnit;

	@AlfProp
	@AlfQname(qname = "qa:sdlQty")
	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	@AlfProp
	@AlfQname(qname = "qa:sdlFreq")
	public Integer getFreq() {
		return freq;
	}

	public void setFreq(Integer freq) {
		this.freq = freq;
	}

	@AlfProp
	@AlfQname(qname = "qa:sdlFreqUnit")
	public String getFreqUnit() {
		return freqUnit;
	}

	public void setFreqUnit(String freqUnit) {
		this.freqUnit = freqUnit;
	}

	public SamplingDefListDataItem() {
		super();
	}

	public SamplingDefListDataItem(Integer qty, Integer freq, String freqUnit, NodeRef controlPoint, NodeRef controlStep, NodeRef samplingGroup, NodeRef controlingGroup, NodeRef fixingGroup, String reaction) {
		super();
		this.qty = qty;
		this.freq = freq;
		this.freqUnit = freqUnit;
		this.controlPoint = controlPoint;
		this.controlStep = controlStep;
		this.samplingGroup = samplingGroup;
		this.controlingGroup = controlingGroup;
		this.fixingGroup = fixingGroup;
		this.reaction = reaction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((controlPoint == null) ? 0 : controlPoint.hashCode());
		result = prime * result + ((controlStep == null) ? 0 : controlStep.hashCode());
		result = prime * result + ((controlingGroup == null) ? 0 : controlingGroup.hashCode());
		result = prime * result + ((freq == null) ? 0 : freq.hashCode());
		result = prime * result + ((freqUnit == null) ? 0 : freqUnit.hashCode());
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
		SamplingDefListDataItem other = (SamplingDefListDataItem) obj;
		if (controlPoint == null) {
			if (other.controlPoint != null)
				return false;
		} else if (!controlPoint.equals(other.controlPoint))
			return false;
		if (controlStep == null) {
			if (other.controlStep != null)
				return false;
		} else if (!controlStep.equals(other.controlStep))
			return false;
		if (controlingGroup == null) {
			if (other.controlingGroup != null)
				return false;
		} else if (!controlingGroup.equals(other.controlingGroup))
			return false;
		if (freq == null) {
			if (other.freq != null)
				return false;
		} else if (!freq.equals(other.freq))
			return false;
		if (freqUnit == null) {
			if (other.freqUnit != null)
				return false;
		} else if (!freqUnit.equals(other.freqUnit))
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
		return "SamplingDefListDataItem [qty=" + qty + ", freq=" + freq + ", freqUnit=" + freqUnit + ", controlPoint=" + controlPoint + ", controlStep=" + controlStep
				+ ", controlingGroup=" + controlingGroup + "]";
	}

}
