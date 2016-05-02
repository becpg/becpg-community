/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;

@AlfType
@AlfQname(qname = "bcpg:packagingList")
@MultiLevelDataList
public class PackagingListDataItem extends AbstractEffectiveVariantListDataItem implements CompositionDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8724448903680191263L;

	private Double qty = 0d;

	private PackagingListUnit packagingListUnit = PackagingListUnit.Unknown;
	
	private Double lossPerc = 0d;

	private PackagingLevel pkgLevel;

	private Boolean isMaster;

	private NodeRef product;

	@AlfProp
	@AlfQname(qname = "bcpg:packagingListQty")
	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:packagingListUnit")
	public PackagingListUnit getPackagingListUnit() {
		return packagingListUnit;
	}

	public void setPackagingListUnit(PackagingListUnit packagingListUnit) {
		this.packagingListUnit = packagingListUnit;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:packagingListLossPerc")
	public Double getLossPerc() {
		return lossPerc;
	}

	public void setLossPerc(Double lossPerc) {
		this.lossPerc = lossPerc;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:packagingListPkgLevel")
	public PackagingLevel getPkgLevel() {
		return pkgLevel;
	}

	public void setPkgLevel(PackagingLevel pkgLevel) {
		this.pkgLevel = pkgLevel;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:packagingListIsMaster")
	public Boolean getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(Boolean isMaster) {
		this.isMaster = isMaster;
	}

	@AlfSingleAssoc
	@DataListIdentifierAttr
	@InternalField
	@AlfQname(qname = "bcpg:packagingListProduct")
	public NodeRef getProduct() {
		return product;
	}

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
	 * 
	 * @param nodeRef
	 * @param qty
	 * @param packagingListUnit
	 * @param pkgLevel
	 * @param product
	 */
	public PackagingListDataItem(NodeRef nodeRef, Double qty, PackagingListUnit packagingListUnit, PackagingLevel pkgLevel, Boolean isMaster, NodeRef product) {

		setNodeRef(nodeRef);
		setQty(qty);
		setPackagingListUnit(packagingListUnit);
		setPkgLevel(pkgLevel);
		setIsMaster(isMaster);
		setProduct(product);
	}

	public PackagingListDataItem(PackagingListDataItem c) {
		super();
		this.nodeRef = c.nodeRef;
		this.qty = c.qty;
		this.packagingListUnit = c.packagingListUnit;
		this.pkgLevel = c.pkgLevel;
		this.isMaster = c.isMaster;
		this.product = c.product;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((isMaster == null) ? 0 : isMaster.hashCode());
		result = prime * result + ((lossPerc == null) ? 0 : lossPerc.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PackagingListDataItem other = (PackagingListDataItem) obj;
		if (isMaster == null) {
			if (other.isMaster != null)
				return false;
		} else if (!isMaster.equals(other.isMaster))
			return false;
		if (lossPerc == null) {
			if (other.lossPerc != null)
				return false;
		} else if (!lossPerc.equals(other.lossPerc))
			return false;
		if (packagingListUnit != other.packagingListUnit)
			return false;
		if (pkgLevel != other.pkgLevel)
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

	@Override
	public CompositionDataItem clone() {
		return new PackagingListDataItem(this);
	}

	@Override
	@InternalField
	public NodeRef getComponent() {
		return getProduct();
	}

	@Override
	public void setComponent(NodeRef targetItem) {
		setProduct(targetItem);
	}
	
	@Override
	public QName getComponentAssocName() {
		return PLMModel.ASSOC_PACKAGINGLIST_PRODUCT;
	}

}
