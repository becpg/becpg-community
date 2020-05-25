package fr.becpg.repo.product.formulation.labeling;

import org.alfresco.service.cmr.repository.NodeRef;

public class EvaporatedDataItem {

	private NodeRef productNodeRef;
	
	private Double rate;
	
	private Double qty;

	public EvaporatedDataItem(NodeRef productNodeRef, Double rate, Double qty) {
		super();
		this.productNodeRef = productNodeRef;
		this.rate = rate;
		this.qty = qty;
	}

	public NodeRef getProductNodeRef() {
		return productNodeRef;
	}

	public Double getRate() {
		return rate;
	}

	public Double getQty() {
		return qty;
	}

	public void addQty(Double qty) {
		this.qty+=qty;
		
	}
	
	
}
