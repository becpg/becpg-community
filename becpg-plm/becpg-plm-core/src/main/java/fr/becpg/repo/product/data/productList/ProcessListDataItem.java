/*
 *
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.MPMModel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.CompositionDataItem;

/**
 * <p>ProcessListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "mpm:processList")
@MultiLevelDataList
public class ProcessListDataItem extends AbstractEffectiveVariantListDataItem implements CompositionDataItem {

	private static final long serialVersionUID = -8313567761346202059L;

	private Double qty = 0d;
	private Double qtyResource = 0d;
	private Double rateResource = 0d;
	private Double yield = 0d;
	private Double rateProduct = 0d;

	private ProductUnit unit;

	private NodeRef step;
	private NodeRef product;
	private NodeRef resource;

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "mpm:plQty")
	public Double getQty() {
		return qty;
	}

	/** {@inheritDoc} */
	@Override
	public void setQty(Double qty) {
		this.qty = qty;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	@AlfProp
	@AlfQname(qname = "mpm:plUnit")
	public ProductUnit getUnit() {
		return unit;
	}

	/**
	 * <p>Setter for the field <code>unit</code>.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 */
	public void setUnit(ProductUnit unit) {
		this.unit = unit;
	}

	/**
	 * <p>Getter for the field <code>qtyResource</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "mpm:plQtyResource")
	public Double getQtyResource() {
		return qtyResource;
	}

	/**
	 * <p>Setter for the field <code>qtyResource</code>.</p>
	 *
	 * @param qtyResource a {@link java.lang.Double} object.
	 */
	public void setQtyResource(Double qtyResource) {
		this.qtyResource = qtyResource;
	}

	/**
	 * <p>Getter for the field <code>rateResource</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "mpm:plRateResource")
	public Double getRateResource() {
		return rateResource;
	}

	/**
	 * <p>Setter for the field <code>rateResource</code>.</p>
	 *
	 * @param rateResource a {@link java.lang.Double} object.
	 */
	public void setRateResource(Double rateResource) {
		this.rateResource = rateResource;
	}

	/**
	 * <p>Getter for the field <code>yield</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "mpm:plYield")
	public Double getYield() {
		return yield;
	}

	/**
	 * <p>Setter for the field <code>yield</code>.</p>
	 *
	 * @param yield a {@link java.lang.Double} object.
	 */
	public void setYield(Double yield) {
		this.yield = yield;
	}

	/**
	 * <p>Getter for the field <code>rateProduct</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "mpm:plRateProduct")
	public Double getRateProduct() {
		return rateProduct;
	}

	/**
	 * <p>Setter for the field <code>rateProduct</code>.</p>
	 *
	 * @param rateProduct a {@link java.lang.Double} object.
	 */
	public void setRateProduct(Double rateProduct) {
		this.rateProduct = rateProduct;
	}

	/**
	 * <p>Getter for the field <code>step</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc = false)
	@AlfQname(qname = "mpm:plStep")
	public NodeRef getStep() {
		return step;
	}

	/**
	 * <p>Setter for the field <code>step</code>.</p>
	 *
	 * @param step a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setStep(NodeRef step) {
		this.step = step;
	}

	/**
	 * <p>Getter for the field <code>product</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc = false)
	@AlfQname(qname = "mpm:plProduct")
	public NodeRef getProduct() {
		return product;
	}

	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}

	/**
	 * <p>Getter for the field <code>resource</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc = true)
	@AlfQname(qname = "mpm:plResource")
	public NodeRef getResource() {
		return resource;
	}

	/**
	 * <p>Setter for the field <code>resource</code>.</p>
	 *
	 * @param resource a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setResource(NodeRef resource) {
		this.resource = resource;
	}

	/**
	 * <p>Constructor for ProcessListDataItem.</p>
	 */
	public ProcessListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ProcessListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qty a {@link java.lang.Double} object.
	 * @param qtyResource a {@link java.lang.Double} object.
	 * @param rateResource a {@link java.lang.Double} object.
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 * @param yield a {@link java.lang.Double} object.
	 * @param rateProduct a {@link java.lang.Double} object.
	 * @param step a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param resource a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public ProcessListDataItem(NodeRef nodeRef, Double qty, Double qtyResource, Double rateResource, ProductUnit unit, Double yield,
			Double rateProduct, NodeRef step, NodeRef product, NodeRef resource) {

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
	 * <p>Constructor for ProcessListDataItem.</p>
	 *
	 * @param p a {@link fr.becpg.repo.product.data.productList.ProcessListDataItem} object.
	 */
	public ProcessListDataItem(ProcessListDataItem p) {
		super(p);
		this.qty = p.qty;
		this.qtyResource = p.qtyResource;
		this.rateResource = p.rateResource;
		this.yield = p.yield;
		this.rateProduct = p.rateProduct;
		this.unit = p.unit;
		this.step = p.step;
		this.product =p.product;
		this.resource = p.resource;
	}

	/** {@inheritDoc} */
	@Override
	public CompositionDataItem copy() {
		ProcessListDataItem ret = new ProcessListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ProcessListDataItem [qty=" + qty + ", qtyResource=" + qtyResource + ", rateResource=" + rateResource + ", yield=" + yield
				+ ", rateProduct=" + rateProduct + ", unit=" + unit + ", step=" + step + ", product=" + product + ", resource=" + resource + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((product == null) ? 0 : product.hashCode());
		result = (prime * result) + ((qty == null) ? 0 : qty.hashCode());
		result = (prime * result) + ((qtyResource == null) ? 0 : qtyResource.hashCode());
		result = (prime * result) + ((rateProduct == null) ? 0 : rateProduct.hashCode());
		result = (prime * result) + ((rateResource == null) ? 0 : rateResource.hashCode());
		result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
		result = (prime * result) + ((step == null) ? 0 : step.hashCode());
		result = (prime * result) + ((unit == null) ? 0 : unit.hashCode());
		result = (prime * result) + ((yield == null) ? 0 : yield.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProcessListDataItem other = (ProcessListDataItem) obj;
		if (product == null) {
			if (other.product != null) {
				return false;
			}
		} else if (!product.equals(other.product)) {
			return false;
		}
		if (qty == null) {
			if (other.qty != null) {
				return false;
			}
		} else if (!qty.equals(other.qty)) {
			return false;
		}
		if (qtyResource == null) {
			if (other.qtyResource != null) {
				return false;
			}
		} else if (!qtyResource.equals(other.qtyResource)) {
			return false;
		}
		if (rateProduct == null) {
			if (other.rateProduct != null) {
				return false;
			}
		} else if (!rateProduct.equals(other.rateProduct)) {
			return false;
		}
		if (rateResource == null) {
			if (other.rateResource != null) {
				return false;
			}
		} else if (!rateResource.equals(other.rateResource)) {
			return false;
		}
		if (resource == null) {
			if (other.resource != null) {
				return false;
			}
		} else if (!resource.equals(other.resource)) {
			return false;
		}
		if (step == null) {
			if (other.step != null) {
				return false;
			}
		} else if (!step.equals(other.step)) {
			return false;
		}
		if (unit != other.unit) {
			return false;
		}
		if (yield == null) {
			if (other.yield != null) {
				return false;
			}
		} else if (!yield.equals(other.yield)) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getComponent() {
		return getResource();
	}

	/** {@inheritDoc} */
	@Override
	public void setComponent(NodeRef targetItem) {
		setResource(targetItem);
	}

	/** {@inheritDoc} */
	@Override
	public QName getComponentAssocName() {
		return MPMModel.ASSOC_PL_RESOURCE;
	}

	/** {@inheritDoc} */
	@Override
	public Double getLossPerc() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setLossPerc(Double d) {
	}

}
