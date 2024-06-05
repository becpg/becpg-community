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

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>WUsedListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "ecm:wUsedList")
public class WUsedListDataItem extends BeCPGDataObject implements CompositeDataItem<WUsedListDataItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4323547879052073360L;
	private Integer depthLevel;
	private Boolean isWUsedImpacted;
	private QName impactedDataList;
	private NodeRef link;
	private NodeRef targetItem;
	private List<NodeRef> sourceItems;
	private WUsedListDataItem parent;
	private Integer sort;
	private Date effectiveDate;
	private Double qty;
	private Double loss;

	/**
	 * <p>Getter for the field <code>effectiveDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "ecm:wulEffectiveDate")
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	
	/**
	 * <p>Setter for the field <code>effectiveDate</code>.</p>
	 *
	 * @param effectiveDate a {@link java.util.Date} object
	 */
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
	/**
	 * <p>Getter for the field <code>qty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "ecm:wulQty")
	public Double getQty() {
		return qty;
	}
	
	/**
	 * <p>Setter for the field <code>qty</code>.</p>
	 *
	 * @param qtyPerc a {@link java.lang.Double} object
	 */
	public void setQty(Double qtyPerc) {
		this.qty = qtyPerc;
	}
	
	/**
	 * <p>Getter for the field <code>loss</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "ecm:wulLoss")
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
	 * <p>Getter for the field <code>parent</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.ecm.data.dataList.WUsedListDataItem} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	public WUsedListDataItem getParent() {
		return parent;
	}

	/**
	 * <p>getRoot.</p>
	 *
	 * @return a {@link fr.becpg.repo.ecm.data.dataList.WUsedListDataItem} object.
	 */
	public WUsedListDataItem getRoot() {
		WUsedListDataItem root = this;
		WUsedListDataItem parent = getParent();
		while(parent !=null) {
			root = parent;
			parent = parent.getParent();
		}
		
		return root;
	}
	
	
	/**
	 * <p>Setter for the field <code>parent</code>.</p>
	 *
	 * @param parent a {@link fr.becpg.repo.ecm.data.dataList.WUsedListDataItem} object.
	 */
	public void setParent(WUsedListDataItem parent) {
		if (parent == null || parent.getDepthLevel() == null) {
			depthLevel = 1;
		} else {
			depthLevel = parent.getDepthLevel() + 1;
		}
		this.parent = parent;
	}

	/**
	 * <p>Getter for the field <code>depthLevel</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/**
	 * <p>Getter for the field <code>sort</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	/**
	 * <p>Setter for the field <code>sort</code>.</p>
	 *
	 * @param sort a {@link java.lang.Integer} object.
	 */
	public void setSort(Integer sort) {
		this.sort = sort;
	}

	
	
	/**
	 * <p>Getter for the field <code>isWUsedImpacted</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:isWUsedImpacted")
	public Boolean getIsWUsedImpacted() {
		return isWUsedImpacted;
	}

	/**
	 * <p>Setter for the field <code>isWUsedImpacted</code>.</p>
	 *
	 * @param isWUsedImpacted a {@link java.lang.Boolean} object.
	 */
	public void setIsWUsedImpacted(Boolean isWUsedImpacted) {
		this.isWUsedImpacted = isWUsedImpacted;
	}

	/**
	 * <p>Getter for the field <code>impactedDataList</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:impactedDataList")
	public QName getImpactedDataList() {
		return impactedDataList;
	}

	/**
	 * <p>Setter for the field <code>impactedDataList</code>.</p>
	 *
	 * @param impactedDataList a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setImpactedDataList(QName impactedDataList) {
		this.impactedDataList = impactedDataList;
	}

	/**
	 * <p>Getter for the field <code>link</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "ecm:wulLink")
	public NodeRef getLink() {
		return link;
	}

	/**
	 * <p>Setter for the field <code>link</code>.</p>
	 *
	 * @param link a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setLink(NodeRef link) {
		this.link = link;
	}
	
	/**
	 * <p>Getter for the field <code>targetItem</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "ecm:wulTargetItem")
	public NodeRef getTargetItem() {
		return targetItem;
	}
	
	/**
	 * <p>Setter for the field <code>targetItem</code>.</p>
	 *
	 * @param targetItem a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setTargetItem(NodeRef targetItem) {
		this.targetItem = targetItem;
	}

	/**
	 * <p>Getter for the field <code>sourceItems</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "ecm:wulSourceItems")
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
	 * <p>Constructor for WUsedListDataItem.</p>
	 */
	public WUsedListDataItem() {
		super();
	}
	


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		result = prime * result + ((impactedDataList == null) ? 0 : impactedDataList.hashCode());
		result = prime * result + ((isWUsedImpacted == null) ? 0 : isWUsedImpacted.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((sourceItems == null) ? 0 : sourceItems.hashCode());
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
		WUsedListDataItem other = (WUsedListDataItem) obj;
		if (depthLevel == null) {
			if (other.depthLevel != null)
				return false;
		} else if (!depthLevel.equals(other.depthLevel))
			return false;
		if (sort == null) {
			if (other.sort != null)
				return false;
		} else if (!sort.equals(other.sort))
			return false;
		if (impactedDataList == null) {
			if (other.impactedDataList != null)
				return false;
		} else if (!impactedDataList.equals(other.impactedDataList))
			return false;
		if (isWUsedImpacted == null) {
			if (other.isWUsedImpacted != null)
				return false;
		} else if (!isWUsedImpacted.equals(other.isWUsedImpacted))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (sourceItems == null) {
			if (other.sourceItems != null)
				return false;
		} else if (!sourceItems.equals(other.sourceItems))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "WUsedListDataItem [isWUsedImpacted=" + isWUsedImpacted + ", impactedDataList=" + impactedDataList + ", sourceItems=" + sourceItems + "]";
	}

	

}
