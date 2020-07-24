package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

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
public class PackMaterialListDataItem extends BeCPGDataObject implements SimpleCharactDataItem,AspectAwareDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Double pmlWeight;
	private NodeRef pmlMaterial;

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
		return pmlMaterial;
	}

	/** {@inheritDoc} */
	@Override
	public Double getValue() {
		return pmlWeight;
	}


	/**
	 * <p>Getter for the field <code>pmlWeight</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="pack:pmlWeight")
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
	 * <p>Getter for the field <code>pmlMaterial</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="pack:pmlMaterial")
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
	 * <p>Constructor for PackMaterialListDataItem.</p>
	 *
	 * @param pmlMaterial a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param pmlWeight a {@link java.lang.Double} object.
	 */
	public PackMaterialListDataItem(NodeRef pmlMaterial,Double pmlWeight){
		super();
		this.pmlMaterial = pmlMaterial;
		this.pmlWeight = pmlWeight;
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((pmlMaterial == null) ? 0 : pmlMaterial.hashCode());
		result = prime * result + ((pmlWeight == null) ? 0 : pmlWeight.hashCode());
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
		if (pmlMaterial == null) {
			if (other.pmlMaterial != null)
				return false;
		} else if (!pmlMaterial.equals(other.pmlMaterial))
			return false;
		if (pmlWeight == null) {
			if (other.pmlWeight != null)
				return false;
		} else if (!pmlWeight.equals(other.pmlWeight))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PackMaterialListDataItem [pmlWeight=" + pmlWeight + ", pmlMaterial=" + pmlMaterial + "]";
	}


}
