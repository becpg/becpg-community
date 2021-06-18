/*
 * 
 */
package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelLeaf;

/**
 * <p>RawMaterialData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:rawMaterial")
@MultiLevelLeaf
@AlfCacheable
public class RawMaterialData extends ProductData {

	private static final long serialVersionUID = -2176815295417841030L;

	private List<NodeRef> suppliers = new ArrayList<>();
	
	private List<NodeRef> supplierPlants = new ArrayList<>();
	

	/**
	 * <p>Getter for the field <code>suppliers</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:suppliers")
	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	/**
	 * <p>Setter for the field <code>suppliers</code>.</p>
	 *
	 * @param suppliers a {@link java.util.List} object.
	 */
	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}

	/**
	 * <p>Getter for the field <code>supplierPlants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:supplierPlants")
	public List<NodeRef> getSupplierPlants() {
		return supplierPlants;
	}

	/**
	 * <p>Setter for the field <code>supplierPlants</code>.</p>
	 *
	 * @param supplierPlants a {@link java.util.List} object.
	 */
	public void setSupplierPlants(List<NodeRef> supplierPlants) {
		this.supplierPlants = supplierPlants;
	}
	

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((supplierPlants == null) ? 0 : supplierPlants.hashCode());
		result = prime * result + ((suppliers == null) ? 0 : suppliers.hashCode());
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
		RawMaterialData other = (RawMaterialData) obj;
		if (supplierPlants == null) {
			if (other.supplierPlants != null)
				return false;
		} else if (!supplierPlants.equals(other.supplierPlants))
			return false;
		if (suppliers == null) {
			if (other.suppliers != null)
				return false;
		} else if (!suppliers.equals(other.suppliers))
			return false;
		return true;
	}


}
