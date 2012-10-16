/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.AbstractEffectiveDataItem;

/**
 * The Class PackagingListDataItem.
 *
 * @author querephi
 */
public class PackagingListDataItem extends AbstractEffectiveDataItem {

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The qty. */
	private Double qty = 0d;
	
	private PackagingListUnit packagingListUnit = PackagingListUnit.Unknown;
	
	private String pkgLevel;	
	
	private Boolean isMaster;
	
	/** The product. */
	private NodeRef product;
	
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

	public PackagingListUnit getPackagingListUnit() {
		return packagingListUnit;
	}

	public void setPackagingListUnit(PackagingListUnit packagingListUnit) {
		this.packagingListUnit = packagingListUnit;
	}

	public String getPkgLevel() {
		return pkgLevel;
	}

	public void setPkgLevel(String pkgLevel) {
		this.pkgLevel = pkgLevel;
	}

	public Boolean getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(Boolean isMaster) {
		this.isMaster = isMaster;
	}

	/**
	 * Gets the product.
	 *
	 * @return the product
	 */
	public NodeRef getProduct() {
		return product;
	}
	
	/**
	 * Sets the product.
	 *
	 * @param product the new product
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}	

	/**
	 * Instantiates a new compo list data item.
	 */
	public PackagingListDataItem() {
		
	}
	
	/**
	 * Instantiates a new packaging list data item.
	 * @param nodeRef
	 * @param qty
	 * @param packagingListUnit
	 * @param pkgLevel
	 * @param product
	 */
	public PackagingListDataItem(NodeRef nodeRef, Double qty, PackagingListUnit packagingListUnit, String pkgLevel, Boolean isMaster, NodeRef product){
		
		setNodeRef(nodeRef);
		setQty(qty);
		setPackagingListUnit(packagingListUnit);
		setPkgLevel(pkgLevel);
		setIsMaster(isMaster);
		setProduct(product);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isMaster == null) ? 0 : isMaster.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((packagingListUnit == null) ? 0 : packagingListUnit.hashCode());
		result = prime * result + ((pkgLevel == null) ? 0 : pkgLevel.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
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
		PackagingListDataItem other = (PackagingListDataItem) obj;
		if (isMaster == null) {
			if (other.isMaster != null)
				return false;
		} else if (!isMaster.equals(other.isMaster))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (packagingListUnit != other.packagingListUnit)
			return false;
		if (pkgLevel == null) {
			if (other.pkgLevel != null)
				return false;
		} else if (!pkgLevel.equals(other.pkgLevel))
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
		return true;
	}

	@Override
	public String toString() {
		return "PackagingListDataItem [nodeRef=" + nodeRef + ", qty=" + qty + ", packagingListUnit=" + packagingListUnit + ", pkgLevel=" + pkgLevel + ", product=" + product + "]";
	}	
	
	
}
