package fr.becpg.repo.product.data;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.product.data.productList.ContactListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PlantListDataItem;
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
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private SystemState state = SystemState.Simulation;

	private List<CostListDataItem> costList;

	private List<PlantListDataItem> plantList;
	
	private List<ContactListDataItem> contactList;

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
		return state != null ? state.toString() : null;
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

	@DataList
	@AlfQname(qname = "bcpg:plant")
	public List<PlantListDataItem> getPlantList() {
		return plantList;
	}

	public void setPlantList(List<PlantListDataItem> plantList) {
		this.plantList = plantList;
	}

	
	@DataList
	@AlfQname(qname = "bcpg:contactList")
	public List<ContactListDataItem> getContactList() {
		return contactList;
	}

	public void setContactList(List<ContactListDataItem> contactList) {
		this.contactList = contactList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(costList, hierarchy1, hierarchy2, plantList, state);
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
		SupplierData other = (SupplierData) obj;
		return Objects.equals(costList, other.costList) && Objects.equals(hierarchy1, other.hierarchy1)
				&& Objects.equals(hierarchy2, other.hierarchy2) && Objects.equals(plantList, other.plantList) && state == other.state;
	}

	@Override
	public String toString() {
		return "SupplierData [name=" + name + ", hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", state=" + state + "]";
	}

}
