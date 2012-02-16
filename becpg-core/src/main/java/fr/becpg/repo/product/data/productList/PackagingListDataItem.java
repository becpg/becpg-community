/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Class PackagingListDataItem.
 *
 * @author querephi
 */
public class PackagingListDataItem{

	/** The node ref. */
	private NodeRef nodeRef;
	
	/** The qty. */
	private Float qty = 0f;
	
	private PackagingListUnit packagingListUnit = PackagingListUnit.Unknown;
	
	private String pkgLevel;			
	
	/** The product. */
	private NodeRef product;
	
	
	
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
	public PackagingListDataItem(NodeRef nodeRef, Float qty, PackagingListUnit packagingListUnit, String pkgLevel, NodeRef product){
		
		setNodeRef(nodeRef);
		setQty(qty);
		setPackagingListUnit(packagingListUnit);
		setPkgLevel(pkgLevel);
		setProduct(product);
	}	
}
