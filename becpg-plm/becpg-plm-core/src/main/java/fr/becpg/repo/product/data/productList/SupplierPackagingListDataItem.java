/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>
 * Describes how a raw material or a packaging is delivered by its supplier.
 * </p>
 * <p>
 * This is upstream packaging: it does not travel with the product that is sold, so it
 * contributes neither to the tare nor to the packaging materials of the entity. It is kept
 * for the transport declaration and for supplier information only.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pack:supplierPackagingList")
@MultiLevelDataList
public class SupplierPackagingListDataItem extends BeCPGDataObject
		implements CompositeDataItem<SupplierPackagingListDataItem>, SimpleCharactDataItem, AspectAwareDataItem {

	private static final long serialVersionUID = -3391208563218477125L;

	private Integer depthLevel;

	private SupplierPackagingListDataItem parent;

	private String component;

	private Double qty = 1d;

	private ProductUnit unit;

	private PackagingLevel pkgLevel = PackagingLevel.Primary;

	private Double recycledPerc;

	private DeclarationType declType = DeclarationType.Declare;

	private Double tare;

	private TareUnit tareUnit = TareUnit.g;

	private NodeRef material;

	private NodeRef product;

	/**
	 * <p>Getter for the field <code>parent</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	@InternalField
	@Override
	public SupplierPackagingListDataItem getParent() {
		return parent;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(SupplierPackagingListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * <p>Getter for the field <code>depthLevel</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	@InternalField
	@Override
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/**
	 * <p>Getter for the field <code>component</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:splComponent")
	@DataListIdentifierAttr
	public String getComponent() {
		return component;
	}

	/**
	 * <p>Setter for the field <code>component</code>.</p>
	 *
	 * @param component a {@link java.lang.String} object
	 */
	public void setComponent(String component) {
		this.component = component;
	}

	/**
	 * <p>Getter for the field <code>qty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:splQty")
	public Double getQty() {
		return qty;
	}

	/**
	 * <p>Setter for the field <code>qty</code>.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 */
	public void setQty(Double qty) {
		this.qty = qty;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:splUnit")
	public ProductUnit getUnit() {
		return unit;
	}

	/**
	 * <p>Setter for the field <code>unit</code>.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 */
	public void setUnit(ProductUnit unit) {
		this.unit = unit;
	}

	/**
	 * <p>Getter for the field <code>pkgLevel</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:splPkgLevel")
	public PackagingLevel getPkgLevel() {
		return pkgLevel;
	}

	/**
	 * <p>Setter for the field <code>pkgLevel</code>.</p>
	 *
	 * @param pkgLevel a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 */
	public void setPkgLevel(PackagingLevel pkgLevel) {
		this.pkgLevel = pkgLevel;
	}

	/**
	 * <p>Getter for the field <code>recycledPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:splRecycledPerc")
	public Double getRecycledPerc() {
		return recycledPerc;
	}

	/**
	 * <p>Setter for the field <code>recycledPerc</code>.</p>
	 *
	 * @param recycledPerc a {@link java.lang.Double} object
	 */
	public void setRecycledPerc(Double recycledPerc) {
		this.recycledPerc = recycledPerc;
	}

	/**
	 * <p>Getter for the field <code>declType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.DeclarationType} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:splDeclType")
	public DeclarationType getDeclType() {
		return declType;
	}

	/**
	 * <p>Setter for the field <code>declType</code>.</p>
	 *
	 * @param declType a {@link fr.becpg.repo.product.data.constraints.DeclarationType} object
	 */
	public void setDeclType(DeclarationType declType) {
		this.declType = declType;
	}

	/**
	 * <p>Getter for the field <code>tare</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:tare")
	public Double getTare() {
		return tare;
	}

	/**
	 * <p>Setter for the field <code>tare</code>.</p>
	 *
	 * @param tare a {@link java.lang.Double} object
	 */
	public void setTare(Double tare) {
		this.tare = tare;
	}

	/**
	 * <p>Getter for the field <code>tareUnit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.TareUnit} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:tareUnit")
	public TareUnit getTareUnit() {
		return tareUnit;
	}

	/**
	 * <p>Setter for the field <code>tareUnit</code>.</p>
	 *
	 * @param tareUnit a {@link fr.becpg.repo.product.data.constraints.TareUnit} object
	 */
	public void setTareUnit(TareUnit tareUnit) {
		this.tareUnit = tareUnit;
	}

	/**
	 * <p>Getter for the field <code>material</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pack:splMaterial")
	@InternalField
	public NodeRef getMaterial() {
		return material;
	}

	/**
	 * <p>Setter for the field <code>material</code>.</p>
	 *
	 * @param material a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setMaterial(NodeRef material) {
		this.material = material;
	}

	/**
	 * <p>Getter for the field <code>product</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pack:splProduct")
	@InternalField
	public NodeRef getProduct() {
		return product;
	}

	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getMaterial();
	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setMaterial(nodeRef);
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public Double getValue() {
		return getTare();
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		setTare(value);
	}

	/**
	 * <p>Constructor for SupplierPackagingListDataItem.</p>
	 */
	public SupplierPackagingListDataItem() {
		super();
	}

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public static SupplierPackagingListDataItem build() {
		return new SupplierPackagingListDataItem();
	}

	/**
	 * <p>withComponent.</p>
	 *
	 * @param component a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public SupplierPackagingListDataItem withComponent(String component) {
		setComponent(component);
		return this;
	}

	/**
	 * <p>withQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public SupplierPackagingListDataItem withQty(Double qty) {
		setQty(qty);
		return this;
	}

	/**
	 * <p>withTare.</p>
	 *
	 * @param tare a {@link java.lang.Double} object
	 * @param tareUnit a {@link fr.becpg.repo.product.data.constraints.TareUnit} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public SupplierPackagingListDataItem withTare(Double tare, TareUnit tareUnit) {
		setTare(tare);
		setTareUnit(tareUnit);
		return this;
	}

	/**
	 * <p>withMaterial.</p>
	 *
	 * @param material a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public SupplierPackagingListDataItem withMaterial(NodeRef material) {
		setMaterial(material);
		return this;
	}

	/**
	 * <p>withPkgLevel.</p>
	 *
	 * @param pkgLevel a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public SupplierPackagingListDataItem withPkgLevel(PackagingLevel pkgLevel) {
		setPkgLevel(pkgLevel);
		return this;
	}

	/**
	 * <p>withParent.</p>
	 *
	 * @param parent a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 * @return a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public SupplierPackagingListDataItem withParent(SupplierPackagingListDataItem parent) {
		setParent(parent);
		setDepthLevel(parent == null || parent.getDepthLevel() == null ? 1 : parent.getDepthLevel() + 1);
		return this;
	}

	/**
	 * <p>Constructor for SupplierPackagingListDataItem.</p>
	 *
	 * @param o a {@link fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem} object
	 */
	public SupplierPackagingListDataItem(SupplierPackagingListDataItem o) {
		super(o);
		this.depthLevel = o.depthLevel;
		this.parent = o.parent;
		this.component = o.component;
		this.qty = o.qty;
		this.unit = o.unit;
		this.pkgLevel = o.pkgLevel;
		this.recycledPerc = o.recycledPerc;
		this.declType = o.declType;
		this.tare = o.tare;
		this.tareUnit = o.tareUnit;
		this.material = o.material;
		this.product = o.product;
	}

	/** {@inheritDoc} */
	@Override
	public SupplierPackagingListDataItem copy() {
		SupplierPackagingListDataItem ret = new SupplierPackagingListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(component, declType, depthLevel, material, pkgLevel, product, qty, recycledPerc, tare, tareUnit, unit);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		SupplierPackagingListDataItem other = (SupplierPackagingListDataItem) obj;
		return Objects.equals(component, other.component) && (declType == other.declType) && Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(material, other.material) && (pkgLevel == other.pkgLevel) && Objects.equals(product, other.product)
				&& Objects.equals(qty, other.qty) && Objects.equals(recycledPerc, other.recycledPerc) && Objects.equals(tare, other.tare)
				&& (tareUnit == other.tareUnit) && (unit == other.unit);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SupplierPackagingListDataItem [component=" + component + ", qty=" + qty + ", pkgLevel=" + pkgLevel + ", tare=" + tare
				+ ", material=" + material + "]";
	}

}
