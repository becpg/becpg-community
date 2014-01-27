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
package fr.becpg.repo.ecm.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Change Unit class
 * 
 * @author quere
 * 
 */
@AlfType
@AlfQname(qname = "ecm:changeUnitList")
public class ChangeUnitDataItem extends BeCPGDataObject {

	private RevisionType revision;
	private RequirementType reqType;
	private String reqDetails;
	private Boolean treated;
	private NodeRef sourceItem;
	private NodeRef targetItem;

	@AlfProp
	@AlfQname(qname = "ecm:culRevision")
	public RevisionType getRevision() {
		return revision;
	}

	public void setRevision(RevisionType revision) {
		this.revision = revision;
	}

	@AlfProp
	@AlfQname(qname = "ecm:culReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	@AlfProp
	@AlfQname(qname = "ecm:culReqDetails")
	public String getReqDetails() {
		return reqDetails;
	}

	public void setReqDetails(String reqDetails) {
		this.reqDetails = reqDetails;
	}

	@AlfProp
	@AlfQname(qname = "ecm:culTreated")
	public Boolean getTreated() {
		return treated;
	}

	public void setTreated(Boolean treated) {
		this.treated = treated;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "ecm:culSourceItem")
	public NodeRef getSourceItem() {
		return sourceItem;
	}

	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "ecm:culTargetItem")
	public NodeRef getTargetItem() {
		return targetItem;
	}

	@Deprecated
	public void setTargetItem(NodeRef targetItem) {
		this.targetItem = targetItem;
	}

	public ChangeUnitDataItem() {
		super();
	}

	public ChangeUnitDataItem(RevisionType revision, RequirementType reqType, String reqDetails, Boolean treated, NodeRef sourceItem, NodeRef targetItem) {
		super();
		this.revision = revision;
		this.reqType = reqType;
		this.reqDetails = reqDetails;
		this.treated = treated;
		this.sourceItem = sourceItem;
		this.targetItem = targetItem;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((reqDetails == null) ? 0 : reqDetails.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		result = prime * result + ((sourceItem == null) ? 0 : sourceItem.hashCode());
		result = prime * result + ((targetItem == null) ? 0 : targetItem.hashCode());
		result = prime * result + ((treated == null) ? 0 : treated.hashCode());
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
		ChangeUnitDataItem other = (ChangeUnitDataItem) obj;
		if (reqDetails == null) {
			if (other.reqDetails != null)
				return false;
		} else if (!reqDetails.equals(other.reqDetails))
			return false;
		if (reqType != other.reqType)
			return false;
		if (revision != other.revision)
			return false;
		if (sourceItem == null) {
			if (other.sourceItem != null)
				return false;
		} else if (!sourceItem.equals(other.sourceItem))
			return false;
		if (targetItem == null) {
			if (other.targetItem != null)
				return false;
		} else if (!targetItem.equals(other.targetItem))
			return false;
		if (treated == null) {
			if (other.treated != null)
				return false;
		} else if (!treated.equals(other.treated))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChangeUnitDataItem [revision=" + revision + ", reqType=" + reqType + ", reqDetails=" + reqDetails + ", treated=" + treated + ", sourceItem=" + sourceItem
				+ ", targetItem=" + targetItem + "]";
	}

}
