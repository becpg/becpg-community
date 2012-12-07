package fr.becpg.repo.ecm.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public class ChangeOrderData extends BeCPGDataObject {
	
	private String code;
	private ECOState ecoState;
	private ChangeOrderType ecoType;
	private List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>();
	
	private List<ReplacementListDataItem> replacementList = new ArrayList<ReplacementListDataItem>();
	private List<WUsedListDataItem> wUsedList = new ArrayList<WUsedListDataItem>();
	private Map<NodeRef, ChangeUnitDataItem> changeUnitMap = new LinkedHashMap<NodeRef, ChangeUnitDataItem>();
	private List<SimulationListDataItem> simulationList = new ArrayList<SimulationListDataItem>();				
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public ECOState getEcoState() {
		return ecoState;
	}
	public void setEcoState(ECOState ecoState) {
		this.ecoState = ecoState;
	}
	public ChangeOrderType getEcoType() {
		return ecoType;
	}
	public void setEcoType(ChangeOrderType ecoType) {
		this.ecoType = ecoType;
	}	
	public List<NodeRef> getCalculatedCharacts() {
		return calculatedCharacts;
	}
	public void setCalculatedCharacts(List<NodeRef> calculatedCharacts) {
		this.calculatedCharacts = calculatedCharacts;
	}
	public List<ReplacementListDataItem> getReplacementList() {
		return replacementList;
	}
	public void setReplacementList(List<ReplacementListDataItem> replacementList) {
		this.replacementList = replacementList;
	}
	public List<WUsedListDataItem> getWUsedList() {
		return wUsedList;
	}
	public void setWUsedList(List<WUsedListDataItem> wUsedList) {
		this.wUsedList = wUsedList;
	}		
	public Map<NodeRef, ChangeUnitDataItem> getChangeUnitMap() {
		return changeUnitMap;
	}
	public void setChangeUnitMap(Map<NodeRef, ChangeUnitDataItem> changeUnitMap) {
		this.changeUnitMap = changeUnitMap;
	}
	public List<SimulationListDataItem> getSimulationList() {
		return simulationList;
	}
	public void setSimulationList(List<SimulationListDataItem> simulationList) {
		this.simulationList = simulationList;
	}
	
	public ChangeOrderData(NodeRef nodeRef, String name, String code, ECOState ecoState, ChangeOrderType ecoType, List<NodeRef> calculatedCharacts){
		setNodeRef(nodeRef);
		setName(name);
		setCode(code);
		setEcoState(ecoState);
		setEcoType(ecoType);
		setCalculatedCharacts(calculatedCharacts);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((calculatedCharacts == null) ? 0 : calculatedCharacts.hashCode());
		result = prime * result + ((changeUnitMap == null) ? 0 : changeUnitMap.hashCode());
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeOrderData other = (ChangeOrderData) obj;
		if (calculatedCharacts == null) {
			if (other.calculatedCharacts != null)
				return false;
		} else if (!calculatedCharacts.equals(other.calculatedCharacts))
			return false;
		if (changeUnitMap == null) {
			if (other.changeUnitMap != null)
				return false;
		} else if (!changeUnitMap.equals(other.changeUnitMap))
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
				+ replacementList + ", wUsedList=" + wUsedList + ", changeUnitMap=" + changeUnitMap + ", simulationList=" + simulationList + "]";
	}
	
	
	
	
}