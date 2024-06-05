/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.product.data.constraints.DeclarationType;
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
 * CompoListDataItem class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:compoList")
@MultiLevelDataList
public class CompoListDataItem extends AbstractEffectiveVariantListDataItem
		implements CompositeDataItem<CompoListDataItem>, CompositionDataItem, SimpleCharactDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6389166205836523748L;

	private Integer depthLevel;

	private Double qty = 0d;

	private Double qtySubFormula = null;

	private ProductUnit compoListUnit;

	private Double lossPerc = 0d;

	private Double yieldPerc = null;

	private DeclarationType declType = DeclarationType.Declare;

	private Double overrunPerc = null;

	private Double volume;

	private NodeRef product;

	private CompoListDataItem parent;

	/**
	 * <p>
	 * Getter for the field <code>parent</code>.
	 * </p>
	 *
	 * @return a
	 *         {@link fr.becpg.repo.product.data.productList.CompoListDataItem}
	 *         object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	@InternalField
	public CompoListDataItem getParent() {
		return parent;
	}

	/**
	 * <p>
	 * Setter for the field <code>parent</code>.
	 * </p>
	 *
	 * @param parent
	 *            a
	 *            {@link fr.becpg.repo.product.data.productList.CompoListDataItem}
	 *            object.
	 */
	public void setParent(CompoListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * <p>
	 * Getter for the field <code>depthLevel</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	@InternalField
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>
	 * Setter for the field <code>depthLevel</code>.
	 * </p>
	 *
	 * @param depthLevel
	 *            a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/**
	 * <p>
	 * Getter for the field <code>qty</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListQty")
	public Double getQty() {
		return qty;
	}

	/** {@inheritDoc} */
	public void setQty(Double qty) {
		this.qty = qty;
	}

	/**
	 * <p>
	 * Getter for the field <code>qtySubFormula</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListQtySubFormula")
	public Double getQtySubFormula() {
		return qtySubFormula;
	}

	/**
	 * <p>
	 * Setter for the field <code>qtySubFormula</code>.
	 * </p>
	 *
	 * @param qtySubFormula
	 *            a {@link java.lang.Double} object.
	 */
	public void setQtySubFormula(Double qtySubFormula) {
		this.qtySubFormula = qtySubFormula;
	}

	/**
	 * <p>
	 * Getter for the field <code>compoListUnit</code>.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit}
	 *         object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListUnit")
	public ProductUnit getCompoListUnit() {
		return compoListUnit;
	}

	/**
	 * <p>
	 * Setter for the field <code>compoListUnit</code>.
	 * </p>
	 *
	 * @param compoListUnit
	 *            a {@link fr.becpg.repo.product.data.constraints.ProductUnit}
	 *            object.
	 */
	public void setCompoListUnit(ProductUnit compoListUnit) {
		this.compoListUnit = compoListUnit;
	}

	/**
	 * <p>
	 * Getter for the field <code>lossPerc</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListLossPerc")
	public Double getLossPerc() {
		return lossPerc;
	}

	/** {@inheritDoc} */
	public void setLossPerc(Double lossPerc) {
		this.lossPerc = lossPerc;
	}

	/**
	 * <p>
	 * Getter for the field <code>yieldPerc</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListYieldPerc")
	public Double getYieldPerc() {
		return yieldPerc;
	}

	/**
	 * <p>
	 * Setter for the field <code>yieldPerc</code>.
	 * </p>
	 *
	 * @param yieldPerc
	 *            a {@link java.lang.Double} object.
	 */
	public void setYieldPerc(Double yieldPerc) {
		this.yieldPerc = yieldPerc;
	}

	/**
	 * <p>
	 * Getter for the field <code>declType</code>.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.DeclarationType}
	 *         object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListDeclType")
	public DeclarationType getDeclType() {
		return declType;
	}

	/**
	 * <p>
	 * Setter for the field <code>declType</code>.
	 * </p>
	 *
	 * @param declType
	 *            a
	 *            {@link fr.becpg.repo.product.data.constraints.DeclarationType}
	 *            object.
	 */
	public void setDeclType(DeclarationType declType) {
		if (declType == null) {
			declType = DeclarationType.Declare;
		}

		this.declType = declType;
	}

	/**
	 * <p>
	 * Getter for the field <code>overrunPerc</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListOverrunPerc")
	public Double getOverrunPerc() {
		return overrunPerc;
	}

	/**
	 * <p>
	 * Setter for the field <code>overrunPerc</code>.
	 * </p>
	 *
	 * @param overrunPerc
	 *            a {@link java.lang.Double} object.
	 */
	public void setOverrunPerc(Double overrunPerc) {
		this.overrunPerc = overrunPerc;
	}

	/**
	 * <p>
	 * Getter for the field <code>volume</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:compoListVolume")
	public Double getVolume() {
		return volume;
	}

	/**
	 * <p>
	 * Setter for the field <code>volume</code>.
	 * </p>
	 *
	 * @param volume
	 *            a {@link java.lang.Double} object.
	 */
	public void setVolume(Double volume) {
		this.volume = volume;
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
	@AlfQname(qname = "bcpg:compoListProduct")
	@InternalField
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
	 * Instantiates a new compo list data item.
	 */
	public CompoListDataItem() {
		super();
	}

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
	public static CompoListDataItem build() {
		return new CompoListDataItem();
	}

	/**
	 * <p>withUnit.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
	public CompoListDataItem withUnit(ProductUnit unit) {
		this.compoListUnit = unit;
		return this;
	}

	/**
	 * <p>withQtyUsed.</p>
	 *
	 * @param qtySubFormula a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
	public CompoListDataItem withQtyUsed(Double qtySubFormula) {
		this.qtySubFormula = qtySubFormula;
		return this;
	}
	
	/**
	 * <p>withQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
	public CompoListDataItem withQty(Double qty) {
		this.qty = qty;
		return this;
	}


	/**
	 * <p>withProduct.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
	public CompoListDataItem withProduct(NodeRef product) {
		this.product = product;
		return this;
	}

	/**
	 * <p>withDeclarationType.</p>
	 *
	 * @param declType a {@link fr.becpg.repo.product.data.constraints.DeclarationType} object
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
	public CompoListDataItem withDeclarationType(DeclarationType declType) {
		this.declType = declType;
		return this;
	}

	/**
	 * <p>
	 * Constructor for CompoListDataItem.
	 * </p>
	 *
	 * @param nodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param parent
	 *            a
	 *            {@link fr.becpg.repo.product.data.productList.CompoListDataItem}
	 *            object.
	 * @param qty
	 *            a {@link java.lang.Double} object.
	 * @param qtySubFormula
	 *            a {@link java.lang.Double} object.
	 * @param compoListUnit
	 *            a {@link fr.becpg.repo.product.data.constraints.ProductUnit}
	 *            object.
	 * @param lossPerc
	 *            a {@link java.lang.Double} object.
	 * @param declType
	 *            a
	 *            {@link fr.becpg.repo.product.data.constraints.DeclarationType}
	 *            object.
	 * @param product
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@Deprecated
	public CompoListDataItem(NodeRef nodeRef, CompoListDataItem parent, Double qty, Double qtySubFormula, ProductUnit compoListUnit, Double lossPerc,
			DeclarationType declType, NodeRef product) {
		super();
		this.nodeRef = nodeRef;
		this.parent = parent;
		this.qty = qty;
		this.qtySubFormula = qtySubFormula;
		this.compoListUnit = compoListUnit;
		this.lossPerc = lossPerc;
		this.declType = declType;
		this.product = product;
		if (parent == null) {
			depthLevel = 1;
		} else {
			depthLevel = parent.getDepthLevel() + 1;
		}
	}

	/**
	 * Copy constructor
	 *
	 * @param c
	 *            a
	 *            {@link fr.becpg.repo.product.data.productList.CompoListDataItem}
	 *            object.
	 */
	public CompoListDataItem(CompoListDataItem c) {
		super(c);
		this.nodeRef = c.nodeRef;
		this.depthLevel = c.depthLevel;
		this.qty = c.qty;
		this.qtySubFormula = c.qtySubFormula;
		this.compoListUnit = c.compoListUnit;
		this.lossPerc = c.lossPerc;
		this.yieldPerc = c.yieldPerc;
		this.declType = c.declType;
		this.overrunPerc = c.overrunPerc;
		this.volume = c.volume;
		this.product = c.product;
		this.parent = c.parent;
	}

	/**
	 * <p>
	 * parseDeclarationType.
	 * </p>
	 *
	 * @param declType
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.constraints.DeclarationType}
	 *         object.
	 */
	public static DeclarationType parseDeclarationType(String declType) {

		return (declType != null && !Objects.equals(declType, "")) ? DeclarationType.valueOf(declType) : DeclarationType.Declare;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(compoListUnit, declType, depthLevel, lossPerc, overrunPerc,
				parent != null ? parent.getNodeRef() : null, product, qty, qtySubFormula, volume, yieldPerc);
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
		CompoListDataItem other = (CompoListDataItem) obj;
		return compoListUnit == other.compoListUnit && declType == other.declType && Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(lossPerc, other.lossPerc) && Objects.equals(overrunPerc, other.overrunPerc)
				&& Objects.equals(parent != null ? parent.getNodeRef() : null, other.parent != null ? other.parent.getNodeRef() : null)
				&& Objects.equals(product, other.product) && Objects.equals(qty, other.qty) && Objects.equals(qtySubFormula, other.qtySubFormula)
				&& Objects.equals(volume, other.volume) && Objects.equals(yieldPerc, other.yieldPerc);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CompoListDataItem [depthLevel=" + depthLevel + ", qty=" + qty + ", qtySubFormula=" + qtySubFormula + ", compoListUnit="
				+ compoListUnit + ", lossPerc=" + lossPerc + ", yieldPerc=" + yieldPerc + ", declType=" + declType + ", overrunPerc=" + overrunPerc
				+ ", volume=" + volume + ", product=" + product + "]";
	}

	/** {@inheritDoc} */
	@Override
	public CompoListDataItem copy() {
		CompoListDataItem ret = new CompoListDataItem(this);
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
		return PLMModel.ASSOC_COMPOLIST_PRODUCT;
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
