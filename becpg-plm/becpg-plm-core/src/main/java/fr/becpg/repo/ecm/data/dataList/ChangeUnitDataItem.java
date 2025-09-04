/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Change Unit class
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "ecm:changeUnitList")
public class ChangeUnitDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 465151815242873452L;
	private RevisionType revision;
	private RequirementType reqType;
	private String reqDetails;
	private Boolean treated;
	private NodeRef sourceItem;
	private NodeRef targetItem;
	private String errorMsg;
	
	
	/**
	 * <p>Getter for the field <code>revision</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.ecm.data.RevisionType} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:culRevision")
	public RevisionType getRevision() {
		return revision;
	}

	/**
	 * <p>Setter for the field <code>revision</code>.</p>
	 *
	 * @param revision a {@link fr.becpg.repo.ecm.data.RevisionType} object.
	 */
	public void setRevision(RevisionType revision) {
		this.revision = revision;
	}

	/**
	 * <p>Getter for the field <code>reqType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RequirementType} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:culReqType")
	public RequirementType getReqType() {
		return reqType;
	}

	/**
	 * <p>Setter for the field <code>reqType</code>.</p>
	 *
	 * @param reqType a {@link fr.becpg.repo.regulatory.RequirementType} object.
	 */
	public void setReqType(RequirementType reqType) {
		this.reqType = reqType;
	}

	/**
	 * <p>Getter for the field <code>reqDetails</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:culReqDetails")
	public String getReqDetails() {
		return reqDetails;
	}

	/**
	 * <p>Setter for the field <code>reqDetails</code>.</p>
	 *
	 * @param reqDetails a {@link java.lang.String} object.
	 */
	public void setReqDetails(String reqDetails) {
		this.reqDetails = reqDetails;
	}

	/**
	 * <p>Getter for the field <code>treated</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:culTreated")
	public Boolean getTreated() {
		return treated;
	}

	/**
	 * <p>Setter for the field <code>treated</code>.</p>
	 *
	 * @param treated a {@link java.lang.Boolean} object.
	 */
	public void setTreated(Boolean treated) {
		this.treated = treated;
	}

	/**
	 * <p>Getter for the field <code>sourceItem</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "ecm:culSourceItem")
	public NodeRef getSourceItem() {
		return sourceItem;
	}

	/**
	 * <p>Setter for the field <code>sourceItem</code>.</p>
	 *
	 * @param sourceItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setSourceItem(NodeRef sourceItem) {
		this.sourceItem = sourceItem;
	}

	/**
	 * <p>Getter for the field <code>targetItem</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "ecm:culTargetItem")
	public NodeRef getTargetItem() {
		return targetItem;
	}
	
	/**
	 * <p>Setter for the field <code>targetItem</code>.</p>
	 *
	 * @param targetItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@Deprecated
	public void setTargetItem(NodeRef targetItem) {
		this.targetItem = targetItem;
	}
	
	/**
	 * <p>Getter for the field <code>errorMsg</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:culReqError")
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * <p>Setter for the field <code>errorMsg</code>.</p>
	 *
	 * @param errorMsg a {@link java.lang.String} object.
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * <p>Constructor for ChangeUnitDataItem.</p>
	 */
	public ChangeUnitDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ChangeUnitDataItem.</p>
	 *
	 * @param revision a {@link fr.becpg.repo.ecm.data.RevisionType} object.
	 * @param reqType a {@link fr.becpg.repo.regulatory.RequirementType} object.
	 * @param reqDetails a {@link java.lang.String} object.
	 * @param treated a {@link java.lang.Boolean} object.
	 * @param sourceItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param targetItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public ChangeUnitDataItem(RevisionType revision, RequirementType reqType, String reqDetails, Boolean treated, NodeRef sourceItem, NodeRef targetItem) {
		super();
		this.revision = revision;
		this.reqType = reqType;
		this.reqDetails = reqDetails;
		this.treated = treated;
		this.sourceItem = sourceItem;
		this.targetItem = targetItem;
		this.errorMsg = null;
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
		result = prime * result + ((reqDetails == null) ? 0 : reqDetails.hashCode());
		result = prime * result + ((reqType == null) ? 0 : reqType.hashCode());
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		result = prime * result + ((sourceItem == null) ? 0 : sourceItem.hashCode());
		result = prime * result + ((targetItem == null) ? 0 : targetItem.hashCode());
		result = prime * result + ((treated == null) ? 0 : treated.hashCode());
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
		ChangeUnitDataItem other = (ChangeUnitDataItem) obj;
		if (errorMsg == null) {
			if (other.errorMsg != null)
				return false;
		} else if (!errorMsg.equals(other.errorMsg))
			return false;
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ChangeUnitDataItem [revision=" + revision + ", reqType=" + reqType + ", reqDetails=" + reqDetails + ", treated=" + treated
				+ ", sourceItem=" + sourceItem + ", targetItem=" + targetItem + ", errorMsg=" + errorMsg + "]";
	}

}
