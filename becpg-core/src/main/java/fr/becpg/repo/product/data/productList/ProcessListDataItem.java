/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.BaseObject;

/**
 * The Class ProcessListDataItem.
 *
 * @author querephi
 */
public class ProcessListDataItem extends BaseObject{
	
	private NodeRef nodeRef;		
	private Double qty = 0d;	
	private Double qtyResource = 0d;
	private Double rateResource = 0d;
	private Double yield = 0d;
	private Double rateProcess = 0d;
	private Double rateProduct = 0d;
	
	private NodeRef step;	
	private NodeRef product;
	private NodeRef resource;


	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	public Double getQtyResource() {
		return qtyResource;
	}

	public void setQtyResource(Double qtyResource) {
		this.qtyResource = qtyResource;
	}

	public Double getRateResource() {
		return rateResource;
	}

	public void setRateResource(Double rateResource) {
		this.rateResource = rateResource;
	}

	public Double getYield() {
		return yield;
	}

	public void setYield(Double yield) {
		this.yield = yield;
	}

	public Double getRateProcess() {
		return rateProcess;
	}

	public void setRateProcess(Double rateProcess) {
		this.rateProcess = rateProcess;
	}

	public Double getRateProduct() {
		return rateProduct;
	}

	public void setRateProduct(Double rateProduct) {
		this.rateProduct = rateProduct;
	}

	public NodeRef getStep() {
		return step;
	}

	public void setStep(NodeRef step) {
		this.step = step;
	}

	public NodeRef getProduct() {
		return product;
	}

	public void setProduct(NodeRef product) {
		this.product = product;
	}

	public NodeRef getResource() {
		return resource;
	}

	public void setResource(NodeRef resource) {
		this.resource = resource;
	}

	/**
	 * Instantiates a new process list data item.
	 */
	public ProcessListDataItem() {
		
	}
	
	/**
	 * Instantiates a new process list data item.
	 * @param nodeRef
	 * @param qty
	 * @param qtyResource
	 * @param rateResource
	 * @param yield
	 * @param rateProcess
	 * @param rateProduct
	 * @param step
	 * @param product
	 * @param resource
	 */
	public ProcessListDataItem(NodeRef nodeRef, Double qty, Double qtyResource, Double rateResource, Double yield, Double rateProcess, Double rateProduct, NodeRef step, NodeRef product, NodeRef resource){
		
		setNodeRef(nodeRef);
		setQty(qty);
		setQtyResource(qtyResource);
		setRateResource(rateResource);
		setYield(yield);
		setRateProcess(rateProcess);
		setRateProduct(rateProduct);
		setStep(step);
		setProduct(product);
		setResource(resource);
	}
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public ProcessListDataItem(ProcessListDataItem p){
		
		setNodeRef(p.getNodeRef());
		setQty(p.getQty());
		setQtyResource(p.getQtyResource());
		setRateResource(p.getRateResource());
		setYield(p.getYield());
		setRateProcess(p.getRateProcess());
		setRateProduct(p.getRateProduct());
		setStep(p.getStep());
		setProduct(p.getProduct());
		setResource(p.getResource());
	}

	@Override
	public String toString() {
		return "ProcessListDataItem [nodeRef=" + nodeRef + ", qty=" + qty + ", qtyResource=" + qtyResource + ", rateResource=" + rateResource + ", yield=" + yield
				+ ", rateProcess=" + rateProcess + ", rateProduct=" + rateProduct + ", step=" + step + ", product=" + product + ", resource=" + resource + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		result = prime * result + ((qtyResource == null) ? 0 : qtyResource.hashCode());
		result = prime * result + ((rateProcess == null) ? 0 : rateProcess.hashCode());
		result = prime * result + ((rateProduct == null) ? 0 : rateProduct.hashCode());
		result = prime * result + ((rateResource == null) ? 0 : rateResource.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((step == null) ? 0 : step.hashCode());
		result = prime * result + ((yield == null) ? 0 : yield.hashCode());
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
		ProcessListDataItem other = (ProcessListDataItem) obj;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		if (qtyResource == null) {
			if (other.qtyResource != null)
				return false;
		} else if (!qtyResource.equals(other.qtyResource))
			return false;
		if (rateProcess == null) {
			if (other.rateProcess != null)
				return false;
		} else if (!rateProcess.equals(other.rateProcess))
			return false;
		if (rateProduct == null) {
			if (other.rateProduct != null)
				return false;
		} else if (!rateProduct.equals(other.rateProduct))
			return false;
		if (rateResource == null) {
			if (other.rateResource != null)
				return false;
		} else if (!rateResource.equals(other.rateResource))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (step == null) {
			if (other.step != null)
				return false;
		} else if (!step.equals(other.step))
			return false;
		if (yield == null) {
			if (other.yield != null)
				return false;
		} else if (!yield.equals(other.yield))
			return false;
		return true;
	}
	
	
	
}

