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
package fr.becpg.repo.ecm.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname="ecm:calculatedCharactList")
public class SimulationListDataItem extends BeCPGDataObject{

	private NodeRef sourceItem;
	private NodeRef charact;
	private Double sourceValue;
	private Double targetValue;
	
	@AlfSingleAssoc
	@AlfQname(qname="ecm:cclSourceItem")
	public NodeRef getSourceItem() {
		return sourceItem;
	}
	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname="ecm:cclCharact")
	public NodeRef getCharact() {
		return charact;
	}
	public void setCharact(NodeRef charact) {
		this.charact = charact;
	}
	
	@AlfProp
	@AlfQname(qname="ecm:cclSourceValue")
	public Double getSourceValue() {
		return sourceValue;
	}
	
	public void setSourceValue(Double sourceValue) {
		this.sourceValue = sourceValue;
	}
	
	@AlfProp
	@AlfQname(qname="ecm:cclTargetValue")
	public Double getTargetValue() {
		return targetValue;
	}
	
	public void setTargetValue(Double targetValue) {
		this.targetValue = targetValue;
	}

	public SimulationListDataItem() {
		super();
	}
	
	public SimulationListDataItem(NodeRef nodeRef, NodeRef sourceItem, NodeRef charact, Double sourceValue, Double targetValue){
		this.nodeRef=nodeRef;
		this.sourceItem=sourceItem;
		this.charact=charact;
		this.sourceValue=sourceValue;
		this.targetValue=targetValue;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charact == null) ? 0 : charact.hashCode());
		result = prime * result + ((sourceItem == null) ? 0 : sourceItem.hashCode());
		result = prime * result + ((sourceValue == null) ? 0 : sourceValue.hashCode());
		result = prime * result + ((targetValue == null) ? 0 : targetValue.hashCode());
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
		SimulationListDataItem other = (SimulationListDataItem) obj;
		if (charact == null) {
			if (other.charact != null)
				return false;
		} else if (!charact.equals(other.charact))
			return false;
		if (sourceItem == null) {
			if (other.sourceItem != null)
				return false;
		} else if (!sourceItem.equals(other.sourceItem))
			return false;
		if (sourceValue == null) {
			if (other.sourceValue != null)
				return false;
		} else if (!sourceValue.equals(other.sourceValue))
			return false;
		if (targetValue == null) {
			if (other.targetValue != null)
				return false;
		} else if (!targetValue.equals(other.targetValue))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "SimulationListDataItem [sourceItem=" + sourceItem + ", charact=" + charact + ", sourceValue=" + sourceValue + ", targetValue=" + targetValue + "]";
	}

	
}
