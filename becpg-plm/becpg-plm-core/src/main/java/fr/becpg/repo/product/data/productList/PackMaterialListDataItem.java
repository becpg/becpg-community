package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>PackMaterialListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pack:packMaterialList")
public class PackMaterialListDataItem extends BeCPGDataObject implements SimpleCharactDataItem, AspectAwareDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Double pmlPerc;
	private Double pmlWeight;
	private Double pmlRecycledPercentage;
	private NodeRef pmlMaterial;
	private PackagingLevel pkgLevel = PackagingLevel.Primary;

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		this.pmlMaterial = nodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(Double value) {
		this.pmlWeight = value;
	}

	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getPmlMaterial();
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return getPmlWeight();
	}

	/**
	 * <p>Getter for the field <code>pmlRecycledPercentage</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:pmlRecycledPercentage")
	public Double getPmlRecycledPercentage() {
		return pmlRecycledPercentage;
	}

	/**
	 * <p>Setter for the field <code>pmlRecycledPercentage</code>.</p>
	 *
	 * @param pmlRecycledPercentage a {@link java.lang.Double} object
	 */
	public void setPmlRecycledPercentage(Double pmlRecycledPercentage) {
		this.pmlRecycledPercentage = pmlRecycledPercentage;
	}

	/**
	 * <p>Getter for the field <code>pkgLevel</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:pmlLevel")
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
	 * <p>Getter for the field <code>pmlWeight</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "pack:pmlWeight")
	public Double getPmlWeight() {
		return pmlWeight;
	}

	/**
	 * <p>Setter for the field <code>pmlWeight</code>.</p>
	 *
	 * @param pmlWeight a {@link java.lang.Double} object.
	 */
	public void setPmlWeight(Double pmlWeight) {
		this.pmlWeight = pmlWeight;
	}

	/**
	 * <p>Getter for the field <code>pmlPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "pack:pmlPerc")
	public Double getPmlPerc() {
		return pmlPerc;
	}

	/**
	 * <p>Setter for the field <code>pmlPerc</code>.</p>
	 *
	 * @param pmlPerc a {@link java.lang.Double} object
	 */
	public void setPmlPerc(Double pmlPerc) {
		this.pmlPerc = pmlPerc;
	}

	/**
	 * <p>Getter for the field <code>pmlMaterial</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname = "pack:pmlMaterial")
	@DataListIdentifierAttr
	public NodeRef getPmlMaterial() {
		return pmlMaterial;
	}

	/**
	 * <p>Setter for the field <code>pmlMaterial</code>.</p>
	 *
	 * @param pmlMaterial a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setPmlMaterial(NodeRef pmlMaterial) {
		this.pmlMaterial = pmlMaterial;
	}

	/**
	 * <p>Constructor for PackMaterialListDataItem.</p>
	 */
	public PackMaterialListDataItem() {
		super();
	}

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 */
	public static PackMaterialListDataItem build() {
		return new PackMaterialListDataItem();
	}

	/**
	 * <p>withMaterial.</p>
	 *
	 * @param pmlMaterial a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 */
	public PackMaterialListDataItem withMaterial(NodeRef pmlMaterial) {
		this.pmlMaterial = pmlMaterial;
		return this;
	}

	/**
	 * <p>withWeight.</p>
	 *
	 * @param pmlWeight a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 */
	public PackMaterialListDataItem withWeight(Double pmlWeight) {
		this.pmlWeight = pmlWeight;
		return this;
	}

	/**
	 * <p>withPerc.</p>
	 *
	 * @param pmlPerc a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 */
	public PackMaterialListDataItem withPerc(Double pmlPerc) {
		this.pmlPerc = pmlPerc;
		return this;
	}

	/**
	 * <p>withRecycledPerc.</p>
	 *
	 * @param pmlRecycledPercentage a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 */
	public PackMaterialListDataItem withRecycledPerc(Double pmlRecycledPercentage) {
		this.pmlRecycledPercentage = pmlRecycledPercentage;
		return this;
	}

	/**
	 * <p>withPkgLevel.</p>
	 *
	 * @param pkgLevel a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 * @return a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 */
	public PackMaterialListDataItem withPkgLevel(PackagingLevel pkgLevel) {
		this.pkgLevel = pkgLevel;
		return this;
	}


	/**
	 * <p>Constructor for PackMaterialListDataItem.</p>
	 *
	 * @param o a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 */
	public PackMaterialListDataItem(PackMaterialListDataItem o) {
		super(o);
		this.pmlMaterial = o.pmlMaterial;
		this.pmlPerc = o.pmlPerc;
		this.pmlWeight = o.pmlWeight;
		this.pkgLevel = o.pkgLevel;
		this.pmlRecycledPercentage = o.pmlRecycledPercentage;
	}

	/** {@inheritDoc} */
	@Override
	public PackMaterialListDataItem copy() {
		PackMaterialListDataItem ret = new PackMaterialListDataItem(this);
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
		result = prime * result + Objects.hash(pkgLevel, pmlMaterial, pmlPerc, pmlRecycledPercentage, pmlWeight);
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
		PackMaterialListDataItem other = (PackMaterialListDataItem) obj;
		return pkgLevel == other.pkgLevel && Objects.equals(pmlMaterial, other.pmlMaterial) && Objects.equals(pmlPerc, other.pmlPerc)
				&& Objects.equals(pmlRecycledPercentage, other.pmlRecycledPercentage) && Objects.equals(pmlWeight, other.pmlWeight);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PackMaterialListDataItem [pmlWeight=" + pmlWeight + ", pmlMaterial=" + pmlMaterial + "]";
	}

}
