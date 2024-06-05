/*
 *
 */
package fr.becpg.repo.collection.data.list;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;

/**
 * <p>
 * ProductListDataItem class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:productList")
@MultiLevelDataList
public class ProductListDataItem extends AbstractEffectiveDataItem implements CompositeDataItem<ProductListDataItem> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5017468197868519894L;

	private Integer depthLevel;

	private NodeRef product;

	private ProductListDataItem parent;

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Getter for the field <code>parent</code>.
	 * </p>
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	@InternalField
	public ProductListDataItem getParent() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Setter for the field <code>parent</code>.
	 * </p>
	 */
	@Override
	public void setParent(ProductListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Getter for the field <code>depthLevel</code>.
	 * </p>
	 */
	@Override
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
	 * Getter for the field <code>product</code>.
	 * </p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr
	@AlfQname(qname = "bcpg:productListProduct")
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

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + Objects.hash(depthLevel, parent, product);
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
		ProductListDataItem other = (ProductListDataItem) obj;
		return Objects.equals(depthLevel, other.depthLevel) && Objects.equals(parent, other.parent) && Objects.equals(product, other.product);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ProductListDataItem [depthLevel=" + depthLevel + ", product=" + product + ", parent=" + parent + "]";
	}



}
