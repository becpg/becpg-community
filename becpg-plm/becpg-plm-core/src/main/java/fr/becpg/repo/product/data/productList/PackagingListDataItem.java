/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>
 * PackagingListDataItem class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:packagingList")
@MultiLevelDataList(secondaryPivot = "bcpg:compoList")
public class PackagingListDataItem extends AbstractEffectiveVariantListDataItem implements CompositionDataItem, SimpleCharactDataItem {

	private static final long serialVersionUID = -8724448903680191263L;

	private Double qty = 0d;

	private ProductUnit packagingListUnit;

	private Double lossPerc = 0d;

	private PackagingLevel pkgLevel;

	private Boolean isMaster;

	private Boolean isRecycle;

	private NodeRef product;

	/**
	 * <p>
	 * Getter for the field <code>qty</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:packagingListQty")
	public Double getQty() {
		return qty;
	}

	/** {@inheritDoc} */
	public void setQty(Double qty) {
		this.qty = qty;
	}

	/**
	 * <p>
	 * Getter for the field <code>packagingListUnit</code>.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit}
	 *         object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:packagingListUnit")
	public ProductUnit getPackagingListUnit() {
		return packagingListUnit;
	}

	/**
	 * <p>
	 * Setter for the field <code>packagingListUnit</code>.
	 * </p>
	 *
	 * @param packagingListUnit
	 *            a {@link fr.becpg.repo.product.data.constraints.ProductUnit}
	 *            object.
	 */
	public void setPackagingListUnit(ProductUnit packagingListUnit) {
		this.packagingListUnit = packagingListUnit;
	}

	/**
	 * <p>
	 * Getter for the field <code>lossPerc</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:packagingListLossPerc")
	public Double getLossPerc() {
		return lossPerc;
	}

	/** {@inheritDoc} */
	public void setLossPerc(Double lossPerc) {
		this.lossPerc = lossPerc;
	}

	/**
	 * <p>
	 * Getter for the field <code>pkgLevel</code>.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.PackagingLevel}
	 *         object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:packagingListPkgLevel")
	public PackagingLevel getPkgLevel() {
		return pkgLevel;
	}

	/**
	 * <p>
	 * Setter for the field <code>pkgLevel</code>.
	 * </p>
	 *
	 * @param pkgLevel
	 *            a
	 *            {@link fr.becpg.repo.product.data.constraints.PackagingLevel}
	 *            object.
	 */
	public void setPkgLevel(PackagingLevel pkgLevel) {
		this.pkgLevel = pkgLevel;
	}

	/**
	 * <p>
	 * Getter for the field <code>isMaster</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:packagingListIsMaster")
	public Boolean getIsMaster() {
		return isMaster;
	}

	/**
	 * <p>
	 * Setter for the field <code>isMaster</code>.
	 * </p>
	 *
	 * @param isMaster
	 *            a {@link java.lang.Boolean} object.
	 */
	public void setIsMaster(Boolean isMaster) {
		this.isMaster = isMaster;
	}

	/**
	 * <p>
	 * Getter for the field <code>isRecycle</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:packagingListIsRecycle")
	public Boolean getIsRecycle() {
		return isRecycle;
	}

	/**
	 * <p>
	 * Setter for the field <code>isRecycle</code>.
	 * </p>
	 *
	 * @param isRecycle
	 *            a {@link java.lang.Boolean} object.
	 */
	public void setIsRecycle(Boolean isRecycle) {
		this.isRecycle = isRecycle;
	}

	/**
	 * <p>
	 * Getter for the field <code>product</code>.
	 * </p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@InternalField
	@AlfQname(qname = "bcpg:packagingListProduct")
	public NodeRef getProduct() {
		return product;
	}

	/**
	 * <p>
	 * Setter for the field <code>product</code>.
	 * </p>
	 *
	 * @param product
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}

	/**
	 * <p>
	 * Constructor for PackagingListDataItem.
	 * </p>
	 */
	public PackagingListDataItem() {
		super();
	}
	
	public static PackagingListDataItem build() {
		return new PackagingListDataItem();
	}

	public PackagingListDataItem withUnit(ProductUnit unit) {
		setPackagingListUnit(unit);
		return this;
	}

	public PackagingListDataItem withQty(Double qty) {
		setQty(qty);
		return this;
	}

	public PackagingListDataItem withProduct(NodeRef product) {
		setProduct(product);
		return this;
	}

	public PackagingListDataItem withPkgLevel(PackagingLevel pkgLevel) {
		setPkgLevel(pkgLevel);
		return this;
	}

	public PackagingListDataItem withIsMaster(Boolean isMaster) {
		setIsMaster(isMaster);
		return this;
	}

	/**
	 * <p>
	 * Constructor for PackagingListDataItem.
	 * </p>
	 *
	 * @param nodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param qty
	 *            a {@link java.lang.Double} object.
	 * @param packagingListUnit
	 *            a {@link fr.becpg.repo.product.data.constraints.ProductUnit}
	 *            object.
	 * @param pkgLevel
	 *            a
	 *            {@link fr.becpg.repo.product.data.constraints.PackagingLevel}
	 *            object.
	 * @param isMaster
	 *            a {@link java.lang.Boolean} object.
	 * @param product
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@Deprecated
	public PackagingListDataItem(NodeRef nodeRef, Double qty, ProductUnit packagingListUnit, PackagingLevel pkgLevel, Boolean isMaster,
			NodeRef product) {

		setNodeRef(nodeRef);
		setQty(qty);
		setPackagingListUnit(packagingListUnit);
		setPkgLevel(pkgLevel);
		setIsMaster(isMaster);
		setProduct(product);
	}

	/**
	 * <p>
	 * Constructor for PackagingListDataItem.
	 * </p>
	 *
	 * @param c
	 *            a
	 *            {@link fr.becpg.repo.product.data.productList.PackagingListDataItem}
	 *            object.
	 */
	public PackagingListDataItem(PackagingListDataItem c) {
		super(c);
		this.qty = c.qty;
		this.packagingListUnit = c.packagingListUnit;
		this.pkgLevel = c.pkgLevel;
		this.isMaster = c.isMaster;
		this.isRecycle = c.isRecycle;
		this.product = c.product;
		this.lossPerc = c.lossPerc;

	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((isMaster == null) ? 0 : isMaster.hashCode());
		result = prime * result + ((isRecycle == null) ? 0 : isRecycle.hashCode());
		result = prime * result + ((lossPerc == null) ? 0 : lossPerc.hashCode());
		result = prime * result + ((packagingListUnit == null) ? 0 : packagingListUnit.hashCode());
		result = prime * result + ((pkgLevel == null) ? 0 : pkgLevel.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		return result;
	}

	/** {@inheritDoc} */
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
		if (isRecycle == null) {
			if (other.isRecycle != null)
				return false;
		} else if (!isRecycle.equals(other.isRecycle))
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PackagingListDataItem [qty=" + qty + ", packagingListUnit=" + packagingListUnit + ", lossPerc=" + lossPerc + ", pkgLevel=" + pkgLevel
				+ ", isMaster=" + isMaster + ", isRecycle=" + isRecycle + ", product=" + product + "]";
	}

	/** {@inheritDoc} */
	@Override
	public PackagingListDataItem copy() {
		PackagingListDataItem ret =  new PackagingListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getComponent() {
		return getProduct();
	}

	/** {@inheritDoc} */
	@Override
	public void setComponent(NodeRef targetItem) {
		setProduct(targetItem);
	}

	/** {@inheritDoc} */
	@Override
	public QName getComponentAssocName() {
		return PLMModel.ASSOC_PACKAGINGLIST_PRODUCT;
	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setComponent(nodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		setQty(value);

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getCharactNodeRef() {
		return getComponent();
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return getQty();
	}

}
