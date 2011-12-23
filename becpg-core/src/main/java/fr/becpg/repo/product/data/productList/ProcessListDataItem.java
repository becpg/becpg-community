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
	private Float qty = 0f;	
	private Float qtyResource = 0f;
	private Float rateResource = 0f;
	private Float yield = 0f;
	private Float rateProcess = 0f;
	private Float rateProduct = 0f;
	
	private NodeRef step;	
	private NodeRef product;
	private NodeRef resource;


	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public Float getQty() {
		return qty;
	}

	public void setQty(Float qty) {
		this.qty = qty;
	}

	public Float getQtyResource() {
		return qtyResource;
	}

	public void setQtyResource(Float qtyResource) {
		this.qtyResource = qtyResource;
	}

	public Float getRateResource() {
		return rateResource;
	}

	public void setRateResource(Float rateResource) {
		this.rateResource = rateResource;
	}

	public Float getYield() {
		return yield;
	}

	public void setYield(Float yield) {
		this.yield = yield;
	}

	public Float getRateProcess() {
		return rateProcess;
	}

	public void setRateProcess(Float rateProcess) {
		this.rateProcess = rateProcess;
	}

	public Float getRateProduct() {
		return rateProduct;
	}

	public void setRateProduct(Float rateProduct) {
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
	public ProcessListDataItem(NodeRef nodeRef, Float qty, Float qtyResource, Float rateResource, Float yield, Float rateProcess, Float rateProduct, NodeRef step, NodeRef product, NodeRef resource){
		
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

