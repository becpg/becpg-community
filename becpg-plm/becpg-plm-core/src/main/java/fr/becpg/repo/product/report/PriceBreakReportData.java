package fr.becpg.repo.product.report;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class PriceBreakReportData {

	private Double simulatedValue = 0d;

	private Double projectedQty = 0d;

	private NodeRef cost;

	private List<NodeRef> suppliers = new ArrayList<>();

	private NodeRef product;

	public Double getSimulatedValue() {
		return simulatedValue;
	}

	public void setSimulatedValue(Double simulatedValue) {
		this.simulatedValue = simulatedValue;
	}

	public Double getProjectedQty() {
		return projectedQty;
	}

	public void setProjectedQty(Double projectedQty) {
		this.projectedQty = projectedQty;
	}

	public NodeRef getCost() {
		return cost;
	}

	public void setCost(NodeRef cost) {
		this.cost = cost;
	}

	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}

	public NodeRef getProduct() {
		return product;
	}

	public void setProduct(NodeRef product) {
		this.product = product;
	}

}
