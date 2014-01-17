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
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:forbiddenIngList")
public class ForbiddenIngListDataItem extends BeCPGDataObject{

	
	RequirementType reqType;
	String reqMessage;
	Double qtyPercMaxi;
	String isGMO;
	String isIonized;
	private List<NodeRef> ings = new ArrayList<NodeRef>();
	private List<NodeRef> geoOrigins = new ArrayList<NodeRef>();
	private List<NodeRef> bioOrigins = new ArrayList<NodeRef>();
	

	@AlfProp
	@AlfQname(qname="bcpg:filReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	@AlfProp
	@AlfQname(qname="bcpg:filReqMessage")
	public String getReqMessage() {
		return reqMessage;
	}

	public void setReqMessage(String reqMessage) {
		this.reqMessage = reqMessage;
	}

	@AlfProp
	@AlfQname(qname="bcpg:filQtyPercMaxi")
	public Double getQtyPercMaxi() {
		return qtyPercMaxi;
	}

	public void setQtyPercMaxi(Double qtyPercMaxi) {
		this.qtyPercMaxi = qtyPercMaxi;
	}


	@AlfProp
	@AlfQname(qname="bcpg:filIsGMO")
	public String getIsGMO() {
		return isGMO;
	}

	public void setIsGMO(String isGMO) {
		this.isGMO = isGMO;
	}
	

	public void setIsGMO(Boolean isGMO) {
		this.isGMO = isGMO!=null ? isGMO.toString() : null;
	}


	@AlfProp
	@AlfQname(qname="bcpg:filIsIonized")
	public String getIsIonized() {
		return isIonized;
	}

	public void setIsIonized(Boolean isIonized) {
		this.isIonized  = isIonized!=null ? isIonized.toString() : null;
	}
	
	public void setIsIonized(String isIonized) {
		this.isIonized  = isIonized;
	}
	

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:filIngs")
	public List<NodeRef> getIngs() {
		return ings;
	}

	public void setIngs(List<NodeRef> ings) {
		this.ings = ings;
	}
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:filGeoOrigins")
	public List<NodeRef> getGeoOrigins() {
		return geoOrigins;
	}

	public void setGeoOrigins(List<NodeRef> geoOrigins) {
		this.geoOrigins = geoOrigins;
	}
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:filBioOrigins")
	public List<NodeRef> getBioOrigins() {
		return bioOrigins;
	}

	public void setBioOrigins(List<NodeRef> bioOrigins) {
		this.bioOrigins = bioOrigins;
	}
	
	
	
	public ForbiddenIngListDataItem() {
		super();
	}

	public ForbiddenIngListDataItem(NodeRef nodeRef, RequirementType reqType, String reqMessage, Double qtyPercMaxi, Boolean isGMO, Boolean isIonized, List<NodeRef> ings, List<NodeRef> geoOrigins, List<NodeRef> bioOrigins)
	{
		this.nodeRef = nodeRef;
		this.reqType = reqType;
		this.reqMessage = reqMessage;
		this.qtyPercMaxi = qtyPercMaxi;
		this.geoOrigins = geoOrigins;
		this.bioOrigins = bioOrigins;
		setIsGMO(isGMO);
		setIsIonized(isIonized);		
		this.ings = ings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bioOrigins == null) ? 0 : bioOrigins.hashCode());
		result = prime * result + ((geoOrigins == null) ? 0 : geoOrigins.hashCode());
		result = prime * result + ((ings == null) ? 0 : ings.hashCode());
		result = prime * result + ((isGMO == null) ? 0 : isGMO.hashCode());
		result = prime * result + ((isIonized == null) ? 0 : isIonized.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((qtyPercMaxi == null) ? 0 : qtyPercMaxi.hashCode());
		result = prime * result + ((reqMessage == null) ? 0 : reqMessage.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForbiddenIngListDataItem other = (ForbiddenIngListDataItem) obj;
		if (bioOrigins == null) {
			if (other.bioOrigins != null)
				return false;
		} else if (!bioOrigins.equals(other.bioOrigins))
			return false;
		if (geoOrigins == null) {
			if (other.geoOrigins != null)
				return false;
		} else if (!geoOrigins.equals(other.geoOrigins))
			return false;
		if (ings == null) {
			if (other.ings != null)
				return false;
		} else if (!ings.equals(other.ings))
			return false;
		if (isGMO == null) {
			if (other.isGMO != null)
				return false;
		} else if (!isGMO.equals(other.isGMO))
			return false;
		if (isIonized == null) {
			if (other.isIonized != null)
				return false;
		} else if (!isIonized.equals(other.isIonized))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (qtyPercMaxi == null) {
			if (other.qtyPercMaxi != null)
				return false;
		} else if (!qtyPercMaxi.equals(other.qtyPercMaxi))
			return false;
		if (reqMessage == null) {
			if (other.reqMessage != null)
				return false;
		} else if (!reqMessage.equals(other.reqMessage))
			return false;
		if (reqType != other.reqType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ForbiddenIngListDataItem [nodeRef=" + nodeRef + ", reqType=" + reqType + ", reqMessage=" + reqMessage + ", qtyPercMaxi=" + qtyPercMaxi + ", isGMO=" + isGMO
				+ ", isIonized=" + isIonized + ", ings=" + ings + ", geoOrigins=" + geoOrigins + ", bioOrigins=" + bioOrigins + "]";
	}	
	
	
	
}
