/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Class ProcessListDataItem.
 *
 * @author querephi
 */
public class ProcessListDataItem{
	
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
		return "ProcessListDataItem [nodeRef=" + nodeRef + ", qty=" + qty + ", qtyResource=" + qtyResource
				+ ", rateResource=" + rateResource + ", yield=" + yield + ", rateProcess=" + rateProcess
				+ ", rateProduct=" + rateProduct + ", step=" + step + ", product=" + product + ", resource=" + resource
				+ "]";
	}
}

