package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.StateableEntity;

@AlfType
@AlfQname(qname = "bcpg:supplier")
public class SupplierData extends BeCPGDataObject implements HierarchicalEntity, StateableEntity {


	private static final long serialVersionUID = -2554133542406623412L;
	private String name;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private SystemState state = SystemState.Simulation;
	

	private List<CostListDataItem> costList;
	
	@AlfProp
	@AlfQname(qname = "cm:name")
	public String getName() {
		return name;
	}
		
	public void setName(String name) {
		this.name = name;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:supplierState")	
	public SystemState getState() {
		return state;
	}

	public void setState(SystemState state) {
		this.state = state;
	}
	
	@Override
	public String getEntityState() {
		return state!=null ? state.toString() : null;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:supplierHierarchy1")
	@Override
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:supplierHierarchy2")
	@Override
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}


	@DataList
	@AlfQname(qname = "bcpg:costList")
	public List<CostListDataItem> getCostList() {
		return costList;
	}

	public void setCostList(List<CostListDataItem> costList) {
		this.costList = costList;
	}
	
	@Override
	public String toString() {
		return "SupplierData [name=" + name + ", hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", state="
				+ state + "]";
	}

}
