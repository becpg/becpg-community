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
package fr.becpg.repo.ecm.data;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>ChangeOrderData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "ecm:changeOrder")
public class ChangeOrderData extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4704853499333377270L;
	private String code;
	private String description;
	private ECOState ecoState;
	private ChangeOrderType ecoType;
	private Date effectiveDate;
	private Boolean applyToAll;
	private List<NodeRef> calculatedCharacts;
	private List<ReplacementListDataItem> replacementList;
	private List<WUsedListDataItem> wUsedList;
	private List<SimulationListDataItem> simulationList;
	private List<ChangeUnitDataItem> changeUnitList;
	private List<String> propertiesToCopy;

	/**
	 * <p>Getter for the field <code>applyToAll</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "ecm:applyToAll")
	public Boolean getApplyToAll() {
		return applyToAll;
	}
	
	/**
	 * <p>Setter for the field <code>applyToAll</code>.</p>
	 *
	 * @param applyToAll a {@link java.lang.Boolean} object
	 */
	public void setApplyToAll(Boolean applyToAll) {
		this.applyToAll = applyToAll;
	}
	
	/**
	 * <p>Getter for the field <code>propertiesToCopy</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfProp
	@AlfQname(qname = "ecm:propertiesToCopy")
	public List<String> getPropertiesToCopy() {
		return propertiesToCopy;
	}

	/**
	 * <p>Setter for the field <code>propertiesToCopy</code>.</p>
	 *
	 * @param propertiesToCopy a {@link java.util.List} object
	 */
	public void setPropertiesToCopy(List<String> propertiesToCopy) {
		this.propertiesToCopy = propertiesToCopy;
	}

	/**
	 * <p>Getter for the field <code>description</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "cm:description")
	public String getDescription() {
		return description;
	}

	/**
	 * <p>Setter for the field <code>description</code>.</p>
	 *
	 * @param description a {@link java.lang.String} object.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:code")
	public String getCode() {
		return code;
	}

	/**
	 * <p>Setter for the field <code>code</code>.</p>
	 *
	 * @param code a {@link java.lang.String} object.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * <p>Getter for the field <code>ecoState</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.ecm.ECOState} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:ecoState")
	public ECOState getEcoState() {
		return ecoState;
	}

	/**
	 * <p>Setter for the field <code>ecoState</code>.</p>
	 *
	 * @param ecoState a {@link fr.becpg.repo.ecm.ECOState} object.
	 */
	public void setEcoState(ECOState ecoState) {
		this.ecoState = ecoState;
	}
	
	
	/**
	 * <p>Getter for the field <code>effectiveDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:effectiveDate")
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * <p>Setter for the field <code>effectiveDate</code>.</p>
	 *
	 * @param effectiveDate a {@link java.util.Date} object.
	 */
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	/**
	 * <p>Getter for the field <code>ecoType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.ecm.data.ChangeOrderType} object.
	 */
	@AlfProp
	@AlfQname(qname = "ecm:ecoType")
	public ChangeOrderType getEcoType() {
		return ecoType;
	}

	/**
	 * <p>Setter for the field <code>ecoType</code>.</p>
	 *
	 * @param ecoType a {@link fr.becpg.repo.ecm.data.ChangeOrderType} object.
	 */
	public void setEcoType(ChangeOrderType ecoType) {
		this.ecoType = ecoType;
	}

	/**
	 * <p>Getter for the field <code>calculatedCharacts</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "ecm:calculatedCharacts")
	public List<NodeRef> getCalculatedCharacts() {
		return calculatedCharacts;
	}

	/**
	 * <p>Setter for the field <code>calculatedCharacts</code>.</p>
	 *
	 * @param calculatedCharacts a {@link java.util.List} object.
	 */
	public void setCalculatedCharacts(List<NodeRef> calculatedCharacts) {
		this.calculatedCharacts = calculatedCharacts;
	}

	/**
	 * <p>Getter for the field <code>replacementList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "ecm:replacementList")
	public List<ReplacementListDataItem> getReplacementList() {
		return replacementList;
	}

	/**
	 * <p>Setter for the field <code>replacementList</code>.</p>
	 *
	 * @param replacementList a {@link java.util.List} object.
	 */
	public void setReplacementList(List<ReplacementListDataItem> replacementList) {
		this.replacementList = replacementList;
	}

	/**
	 * <p>Getter for the field <code>wUsedList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "ecm:wUsedList")
	public List<WUsedListDataItem> getWUsedList() {
		return wUsedList;
	}

	/**
	 * <p>Setter for the field <code>wUsedList</code>.</p>
	 *
	 * @param wUsedList a {@link java.util.List} object.
	 */
	public void setWUsedList(List<WUsedListDataItem> wUsedList) {
		this.wUsedList = wUsedList;
	}

	/**
	 * <p>Getter for the field <code>simulationList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "ecm:calculatedCharactList")
	public List<SimulationListDataItem> getSimulationList() {
		return simulationList;
	}

	/**
	 * <p>Setter for the field <code>simulationList</code>.</p>
	 *
	 * @param simulationList a {@link java.util.List} object.
	 */
	public void setSimulationList(List<SimulationListDataItem> simulationList) {
		this.simulationList = simulationList;
	}

	/**
	 * <p>Getter for the field <code>changeUnitList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "ecm:changeUnitList")
	public List<ChangeUnitDataItem> getChangeUnitList() {
		return changeUnitList;
	}

	/**
	 * <p>Setter for the field <code>changeUnitList</code>.</p>
	 *
	 * @param changeUnitList a {@link java.util.List} object.
	 */
	public void setChangeUnitList(List<ChangeUnitDataItem> changeUnitList) {
		this.changeUnitList = changeUnitList;
	}

	/**
	 * <p>getChangeUnitMap.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, ChangeUnitDataItem> getChangeUnitMap() {

		Map<NodeRef, ChangeUnitDataItem> changeUnitMap = new LinkedHashMap<>();

		if(changeUnitList!=null){
			for (ChangeUnitDataItem dataItem : changeUnitList) {
				changeUnitMap.put(dataItem.getSourceItem(), dataItem);
			}
		}

		return Collections.unmodifiableMap(changeUnitMap);
	}

	/**
	 * <p>Constructor for ChangeOrderData.</p>
	 */
	public ChangeOrderData() {
		super();
	}

	/**
	 * <p>Constructor for ChangeOrderData.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param ecoState a {@link fr.becpg.repo.ecm.ECOState} object.
	 * @param ecoType a {@link fr.becpg.repo.ecm.data.ChangeOrderType} object.
	 * @param calculatedCharacts a {@link java.util.List} object.
	 */
	public ChangeOrderData( String name, ECOState ecoState, ChangeOrderType ecoType, List<NodeRef> calculatedCharacts) {
		this.name = name;
		this.ecoState = ecoState;
		this.ecoType = ecoType;
		this.calculatedCharacts = calculatedCharacts;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((calculatedCharacts == null) ? 0 : calculatedCharacts.hashCode());
		result = prime * result + ((changeUnitList == null) ? 0 : changeUnitList.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((ecoState == null) ? 0 : ecoState.hashCode());
		result = prime * result + ((ecoType == null) ? 0 : ecoType.hashCode());
		result = prime * result + ((effectiveDate == null) ? 0 : effectiveDate.hashCode());
		result = prime * result + ((replacementList == null) ? 0 : replacementList.hashCode());
		result = prime * result + ((simulationList == null) ? 0 : simulationList.hashCode());
		result = prime * result + ((wUsedList == null) ? 0 : wUsedList.hashCode());
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
		ChangeOrderData other = (ChangeOrderData) obj;
		if (calculatedCharacts == null) {
			if (other.calculatedCharacts != null)
				return false;
		} else if (!calculatedCharacts.equals(other.calculatedCharacts))
			return false;
		if (changeUnitList == null) {
			if (other.changeUnitList != null)
				return false;
		} else if (!changeUnitList.equals(other.changeUnitList))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (ecoState != other.ecoState)
			return false;
		if (ecoType != other.ecoType)
			return false;
		if (effectiveDate == null) {
			if (other.effectiveDate != null)
				return false;
		} else if (!effectiveDate.equals(other.effectiveDate))
			return false;
		if (replacementList == null) {
			if (other.replacementList != null)
				return false;
		} else if (!replacementList.equals(other.replacementList))
			return false;
		if (simulationList == null) {
			if (other.simulationList != null)
				return false;
		} else if (!simulationList.equals(other.simulationList))
			return false;
		if (wUsedList == null) {
			if (other.wUsedList != null)
				return false;
		} else if (!wUsedList.equals(other.wUsedList))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ChangeOrderData [code=" + code + ", ecoState=" + ecoState + ", ecoType=" + ecoType + ", calculatedCharacts=" + calculatedCharacts + ", replacementList="
				+ replacementList + ", wUsedList=" + wUsedList + ", simulationList=" + simulationList + ", changeUnitList=" + changeUnitList + "]";
	}

}
