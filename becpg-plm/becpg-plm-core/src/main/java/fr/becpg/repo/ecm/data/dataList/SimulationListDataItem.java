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

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>SimulationListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname="ecm:calculatedCharactList")
public class SimulationListDataItem extends BeCPGDataObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5349047677356009299L;
	private NodeRef sourceItem;
	private NodeRef charact;
	private Object sourceValue;
	private Object targetValue;
	private Integer sort;
	
	/**
	 * <p>Getter for the field <code>sourceItem</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="ecm:cclSourceItem")
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
	 * <p>Getter for the field <code>charact</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="ecm:cclCharact")
	public NodeRef getCharact() {
		return charact;
	}
	/**
	 * <p>Setter for the field <code>charact</code>.</p>
	 *
	 * @param charact a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setCharact(NodeRef charact) {
		this.charact = charact;
	}
	
	/**
	 * <p>Getter for the field <code>sourceValue</code>.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	@AlfProp
	@AlfQname(qname="ecm:cclSourceValue")
	public Object getSourceValue() {
		return sourceValue;
	}
	
	/**
	 * <p>Setter for the field <code>sourceValue</code>.</p>
	 *
	 * @param sourceValue a {@link java.lang.Object} object.
	 */
	public void setSourceValue(Object sourceValue) {
		this.sourceValue = sourceValue;
	}
	
	/**
	 * <p>Getter for the field <code>targetValue</code>.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	@AlfProp
	@AlfQname(qname="ecm:cclTargetValue")
	public Object getTargetValue() {
		return targetValue;
	}
	
	/**
	 * <p>Setter for the field <code>targetValue</code>.</p>
	 *
	 * @param targetValue a {@link java.lang.Object} object.
	 */
	public void setTargetValue(Object targetValue) {
		this.targetValue = targetValue;
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
	 * <p>Constructor for SimulationListDataItem.</p>
	 */
	public SimulationListDataItem() {
		super();
	}
	
	/**
	 * <p>Constructor for SimulationListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param sourceItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param charact a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param sourceValue a {@link java.lang.Object} object.
	 * @param targetValue a {@link java.lang.Object} object.
	 * @param sort a {@link java.lang.Integer} object.
	 */
	public SimulationListDataItem(NodeRef nodeRef, NodeRef sourceItem, NodeRef charact, Object sourceValue, Object targetValue, Integer sort){
		this.nodeRef=nodeRef;
		this.sourceItem=sourceItem;
		this.charact=charact;
		this.sourceValue=sourceValue;
		this.targetValue=targetValue;
		this.sort=sort;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charact == null) ? 0 : charact.hashCode());
		result = prime * result + ((sourceItem == null) ? 0 : sourceItem.hashCode());
		result = prime * result + ((sourceValue == null) ? 0 : sourceValue.hashCode());
		result = prime * result + ((targetValue == null) ? 0 : targetValue.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
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
		SimulationListDataItem other = (SimulationListDataItem) obj;
		if (charact == null) {
			if (other.charact != null)
				return false;
		} else if (!charact.equals(other.charact))
			return false;
		
		if (sort == null) {
			if (other.sort != null)
				return false;
		} else if (!sort.equals(other.sort))
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
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SimulationListDataItem [sourceItem=" + sourceItem + ", charact=" + charact + ", sourceValue=" + sourceValue + ", targetValue=" + targetValue + "]";
	}

	
}
