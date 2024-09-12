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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>ReplacementListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "ecm:replacementList")
public class ReplacementListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7417848239590059467L;
	private RevisionType revision;
	private List<NodeRef> sourceItems;
	private NodeRef targetItem;
	private Integer qtyPerc;
	private Double loss;

	/**
	 * <p>Getter for the field <code>qtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:rlQtyPerc")
	public Integer getQtyPerc() {
		return qtyPerc;
	}
	
	/**
	 * <p>Getter for the field <code>loss</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "ecm:rlLoss")
	public Double getLoss() {
		return loss;
	}
	
	/**
	 * <p>Setter for the field <code>loss</code>.</p>
	 *
	 * @param lossPerc a {@link java.lang.Double} object
	 */
	public void setLoss(Double lossPerc) {
		this.loss = lossPerc;
	}

	/**
	 * <p>Setter for the field <code>qtyPerc</code>.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Integer} object.
	 */
	public void setQtyPerc(Integer qtyPerc) {
		this.qtyPerc = qtyPerc;
	}

	/**
	 * <p>Getter for the field <code>revision</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.ecm.data.RevisionType} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:rlRevisionType")
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
	 * <p>Getter for the field <code>sourceItems</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "ecm:rlSourceItems")
	public List<NodeRef> getSourceItems() {
		return sourceItems;
	}

	/**
	 * <p>Setter for the field <code>sourceItems</code>.</p>
	 *
	 * @param sourceItems a {@link java.util.List} object.
	 */
	public void setSourceItems(List<NodeRef> sourceItems) {
		this.sourceItems = sourceItems;
	}

	/**
	 * <p>Getter for the field <code>targetItem</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "ecm:rlTargetItem")
	public NodeRef getTargetItem() {
		return targetItem;
	}

	/**
	 * <p>Setter for the field <code>targetItem</code>.</p>
	 *
	 * @param targetItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setTargetItem(NodeRef targetItem) {
		this.targetItem = targetItem;
	}

	/**
	 * <p>Constructor for ReplacementListDataItem.</p>
	 */
	public ReplacementListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ReplacementListDataItem.</p>
	 *
	 * @param revision a {@link fr.becpg.repo.ecm.data.RevisionType} object.
	 * @param sourceItems a {@link java.util.List} object.
	 * @param targetItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qtyPerc a {@link java.lang.Integer} object.
	 */
	public ReplacementListDataItem(RevisionType revision, List<NodeRef> sourceItems, NodeRef targetItem, Integer qtyPerc) {
		super();
		this.revision = revision;
		this.sourceItems = sourceItems;
		this.targetItem = targetItem;
		this.qtyPerc = qtyPerc;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((qtyPerc == null) ? 0 : qtyPerc.hashCode());
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		result = prime * result + ((sourceItems == null) ? 0 : sourceItems.hashCode());
		result = prime * result + ((targetItem == null) ? 0 : targetItem.hashCode());
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
		ReplacementListDataItem other = (ReplacementListDataItem) obj;
		if (qtyPerc == null) {
			if (other.qtyPerc != null)
				return false;
		} else if (!qtyPerc.equals(other.qtyPerc))
			return false;
		if (revision != other.revision)
			return false;
		if (sourceItems == null) {
			if (other.sourceItems != null)
				return false;
		} else if (!sourceItems.equals(other.sourceItems))
			return false;
		if (targetItem == null) {
			if (other.targetItem != null)
				return false;
		} else if (!targetItem.equals(other.targetItem))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ReplacementListDataItem [revision=" + revision + ", sourceItems=" + sourceItems + ", targetItem=" + targetItem + ", qtyPerc=" + qtyPerc + "]";
	}

}
