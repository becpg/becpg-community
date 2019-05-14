/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * 
 * @author matthieu
 * 
 */
@AlfType
@AlfQname(qname = "bcpg:productSpecCompatibilityList")
public class SpecCompatibilityDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2622448190271761027L;
	private RequirementType reqType;
	private String reqDetails;
	private NodeRef sourceItem;
	
	
	public SpecCompatibilityDataItem() {
		super();
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:psclReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:psclReqDetails")
	public String getReqDetails() {
		return reqDetails;
	}

	public void setReqDetails(String reqDetails) {
		this.reqDetails = reqDetails;
	}

	
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:psclSourceItem")
	public NodeRef getSourceItem() {
		return sourceItem;
	}

	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}

	

	public SpecCompatibilityDataItem( RequirementType reqType, String reqDetails, NodeRef sourceItem) {
		super();
		this.reqType = reqType;
		this.reqDetails = reqDetails;
		this.sourceItem = sourceItem;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((reqDetails == null) ? 0 : reqDetails.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		result = prime * result + ((sourceItem == null) ? 0 : sourceItem.hashCode());
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
		SpecCompatibilityDataItem other = (SpecCompatibilityDataItem) obj;
		if (reqDetails == null) {
			if (other.reqDetails != null)
				return false;
		} else if (!reqDetails.equals(other.reqDetails))
			return false;
		if (reqType != other.reqType)
			return false;
		if (sourceItem == null) {
			if (other.sourceItem != null)
				return false;
		} else if (!sourceItem.equals(other.sourceItem))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SpecCompatibilityDataItem [reqType=" + reqType + ", reqDetails=" + reqDetails + ", sourceItem="
				+ sourceItem + "]";
	}

}
