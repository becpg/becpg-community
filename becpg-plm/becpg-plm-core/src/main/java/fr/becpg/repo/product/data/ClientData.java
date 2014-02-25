package fr.becpg.repo.product.data;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.StateableEntity;

public class ClientData extends BeCPGDataObject implements HierarchicalEntity, StateableEntity {

	private String name;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private SystemState state = SystemState.Simulation;
	
	@AlfProp
	@AlfQname(qname = "cm:name")
	public String getName() {
		return name;
	}
		
	public void setName(String name) {
		this.name = name;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:clientState")	
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
	@AlfQname(qname = "bcpg:clientHierarchy1")
	@Override
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:clientHierarchy2")
	@Override
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}

	@Override
	public String toString() {
		return "ClientData [name=" + name + ", hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", state="
				+ state + "]";
	}

}
