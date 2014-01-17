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
package fr.becpg.repo.ecm.data;

import java.util.Collections;
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

@AlfType
@AlfQname(qname = "ecm:changeOrder")
public class ChangeOrderData extends BeCPGDataObject {

	private String code;
	private ECOState ecoState;
	private ChangeOrderType ecoType;
	private List<NodeRef> calculatedCharacts;
	private List<ReplacementListDataItem> replacementList;
	private List<WUsedListDataItem> wUsedList;
	private List<SimulationListDataItem> simulationList;
	private List<ChangeUnitDataItem> changeUnitList;

	@AlfProp
	@AlfQname(qname = "bcpg:code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@AlfProp
	@AlfQname(qname = "ecm:ecoState")
	public ECOState getEcoState() {
		return ecoState;
	}

	public void setEcoState(ECOState ecoState) {
		this.ecoState = ecoState;
	}

	@AlfProp
	@AlfQname(qname = "ecm:ecoType")
	public ChangeOrderType getEcoType() {
		return ecoType;
	}

	public void setEcoType(ChangeOrderType ecoType) {
		this.ecoType = ecoType;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "ecm:calculatedCharacts")
	public List<NodeRef> getCalculatedCharacts() {
		return calculatedCharacts;
	}

	public void setCalculatedCharacts(List<NodeRef> calculatedCharacts) {
		this.calculatedCharacts = calculatedCharacts;
	}

	@DataList
	@AlfQname(qname = "ecm:replacementList")
	public List<ReplacementListDataItem> getReplacementList() {
		return replacementList;
	}

	public void setReplacementList(List<ReplacementListDataItem> replacementList) {
		this.replacementList = replacementList;
	}

	@DataList
	@AlfQname(qname = "ecm:wUsedList")
	public List<WUsedListDataItem> getWUsedList() {
		return wUsedList;
	}

	public void setWUsedList(List<WUsedListDataItem> wUsedList) {
		this.wUsedList = wUsedList;
	}

	@DataList
	@AlfQname(qname = "ecm:calculatedCharactList")
	public List<SimulationListDataItem> getSimulationList() {
		return simulationList;
	}

	public void setSimulationList(List<SimulationListDataItem> simulationList) {
		this.simulationList = simulationList;
	}

	@DataList
	@AlfQname(qname = "ecm:changeUnitList")
	public List<ChangeUnitDataItem> getChangeUnitList() {
		return changeUnitList;
	}

	public void setChangeUnitList(List<ChangeUnitDataItem> changeUnitList) {
		this.changeUnitList = changeUnitList;
	}

	public Map<NodeRef, ChangeUnitDataItem> getChangeUnitMap() {

		Map<NodeRef, ChangeUnitDataItem> changeUnitMap = new LinkedHashMap<NodeRef, ChangeUnitDataItem>();

		for (ChangeUnitDataItem dataItem : changeUnitList) {
			changeUnitMap.put(dataItem.getSourceItem(), dataItem);
		}

		return Collections.unmodifiableMap(changeUnitMap);
	}

	public ChangeOrderData() {
		super();
	}

	public ChangeOrderData(NodeRef nodeRef, String name, String code, ECOState ecoState, ChangeOrderType ecoType, List<NodeRef> calculatedCharacts) {
		this.nodeRef = nodeRef;
		this.name = name;
		this.code = code;
		this.ecoState = ecoState;
		this.ecoType = ecoType;
		this.calculatedCharacts = calculatedCharacts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((calculatedCharacts == null) ? 0 : calculatedCharacts.hashCode());
		result = prime * result + ((changeUnitList == null) ? 0 : changeUnitList.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((ecoState == null) ? 0 : ecoState.hashCode());
		result = prime * result + ((ecoType == null) ? 0 : ecoType.hashCode());
		result = prime * result + ((replacementList == null) ? 0 : replacementList.hashCode());
		result = prime * result + ((simulationList == null) ? 0 : simulationList.hashCode());
		result = prime * result + ((wUsedList == null) ? 0 : wUsedList.hashCode());
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
		if (ecoState != other.ecoState)
			return false;
		if (ecoType != other.ecoType)
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

	@Override
	public String toString() {
		return "ChangeOrderData [code=" + code + ", ecoState=" + ecoState + ", ecoType=" + ecoType + ", calculatedCharacts=" + calculatedCharacts + ", replacementList="
				+ replacementList + ", wUsedList=" + wUsedList + ", simulationList=" + simulationList + ", changeUnitList=" + changeUnitList + "]";
	}

}
