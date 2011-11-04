package fr.becpg.repo.eco.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.eco.ECOState;
import fr.becpg.repo.eco.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.eco.data.dataList.SimulationListDataItem;
import fr.becpg.repo.eco.data.dataList.WUsedListDataItem;

public class ChangeOrderData extends BeCPGDataObject {

	private static final String KEY = "%s-%s";
	
	private String code;
	private ECOState ecoState;
	private ChangeOrderType ecoType;
	private List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>();
	
	private List<ReplacementListDataItem> replacementList = new ArrayList<ReplacementListDataItem>();
	private List<WUsedListDataItem> wUsedList = new ArrayList<WUsedListDataItem>();
	private List<SimulationListDataItem> simulationList = new ArrayList<SimulationListDataItem>();
	
	//for internal use
	private Map<String, NodeRef> changeUnitsMap = new HashMap<String, NodeRef>();
			
	
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
	public Map<String, NodeRef> getChangeUnitsMap() {
		return changeUnitsMap;
	}
	public void setChangeUnitsMap(Map<String, NodeRef> changeUnitsMap) {
		this.changeUnitsMap = changeUnitsMap;
	}	
	public List<SimulationListDataItem> getSimulationList() {
		return simulationList;
	}
	public void setSimulationList(List<SimulationListDataItem> simulationList) {
		this.simulationList = simulationList;
	}
	
	public static String getChangeUnitKey(NodeRef sourceItem, NodeRef targetItem){
		
		return String.format(ChangeOrderData.KEY, sourceItem, targetItem);
	}
	
	public ChangeOrderData(NodeRef nodeRef, String name, String code, ECOState ecoState, ChangeOrderType ecoType, List<NodeRef> calculatedCharacts){
		setNodeRef(nodeRef);
		setName(name);
		setCode(code);
		setEcoState(ecoState);
		setEcoType(ecoType);
		setCalculatedCharacts(calculatedCharacts);
	}
	
}