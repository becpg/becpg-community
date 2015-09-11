/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;

@AlfType
@AlfQname(qname = "mpm:processList")
@MultiLevelDataList
public class ProcessListDataItem extends AbstractEffectiveVariantListDataItem implements CompositionDataItem {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8313567761346202059L;
	private Double qty = 0d;	
	private Double qtyResource = 0d;
	private Double rateResource = 0d;
	private Double yield = 0d;
	private Double rateProduct = 0d;
	
	private ProcessListUnit unit = ProcessListUnit.P;
	
	private NodeRef step;	
	private NodeRef product;
	private NodeRef resource;

	@AlfProp
	@AlfQname(qname="mpm:plQty")
	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	
	@AlfProp
	@AlfQname(qname="mpm:plUnit")
	public ProcessListUnit getUnit() {
		return unit;
	}

	public void setUnit(ProcessListUnit unit) {
		this.unit = unit;
	}

	@AlfProp
	@AlfQname(qname="mpm:plQtyResource")
	public Double getQtyResource() {
		return qtyResource;
	}

	public void setQtyResource(Double qtyResource) {
		this.qtyResource = qtyResource;
	}

	@AlfProp
	@AlfQname(qname="mpm:plRateResource")
	public Double getRateResource() {
		return rateResource;
	}

	public void setRateResource(Double rateResource) {
		this.rateResource = rateResource;
	}
	
	@AlfProp
	@AlfQname(qname="mpm:plYield")
	public Double getYield() {
		return yield;
	}

	public void setYield(Double yield) {
		this.yield = yield;
	}
	
	
	@AlfProp
	@AlfQname(qname="mpm:plRateProduct")
	public Double getRateProduct() {
		return rateProduct;
	}

	public void setRateProduct(Double rateProduct) {
		this.rateProduct = rateProduct;
	}
	
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc=false)
	@AlfQname(qname="mpm:plStep")
	public NodeRef getStep() {
		return step;
	}

	public void setStep(NodeRef step) {
		this.step = step;
	}
	
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc=false)
	@AlfQname(qname="mpm:plProduct")
	public NodeRef getProduct() {
		return product;
	}

	public void setProduct(NodeRef product) {
		this.product = product;
	}
	
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc=true)
	@AlfQname(qname="mpm:plResource")
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
		super();
	}
	
	
	public ProcessListDataItem(NodeRef nodeRef, Double qty, Double qtyResource, Double rateResource, ProcessListUnit unit, Double yield, Double rateProduct, NodeRef step, NodeRef product, NodeRef resource){
		
		setNodeRef(nodeRef);
		setQty(qty);
		setUnit(unit);
		setQtyResource(qtyResource);
		setRateResource(rateResource);
		setYield(yield);
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
		setRateProduct(p.getRateProduct());
		setStep(p.getStep());
		setProduct(p.getProduct());
		setResource(p.getResource());
	}


	@Override
	public CompositionDataItem clone() {
		return new ProcessListDataItem(this);
	}
	
	@Override
	public String toString() {
		return "ProcessListDataItem [qty=" + qty + ", qtyResource=" + qtyResource + ", rateResource=" + rateResource + ", yield=" + yield
				+ ", rateProduct=" + rateProduct + ", unit=" + unit + ", step=" + step + ", product=" + product + ", resource=" + resource + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		result = prime * result + ((qtyResource == null) ? 0 : qtyResource.hashCode());
		result = prime * result + ((rateProduct == null) ? 0 : rateProduct.hashCode());
		result = prime * result + ((rateResource == null) ? 0 : rateResource.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((step == null) ? 0 : step.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((yield == null) ? 0 : yield.hashCode());
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
		ProcessListDataItem other = (ProcessListDataItem) obj;
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
		if (unit != other.unit)
			return false;
		if (yield == null) {
			if (other.yield != null)
				return false;
		} else if (!yield.equals(other.yield))
			return false;
		return true;
	}

	@Override
	public NodeRef getComponent() {
		return getResource();
	}

	@Override
	public void setComponent(NodeRef targetItem) {
		setResource(targetItem);
	}


	
	
	
}

