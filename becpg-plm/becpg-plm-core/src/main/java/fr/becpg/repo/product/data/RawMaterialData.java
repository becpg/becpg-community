/*
 * 
 */
package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:rawMaterial")
public class RawMaterialData extends ProductData {

	private List<NodeRef> suppliers = new ArrayList<>();
	
	private List<NodeRef> supplierPlants = new ArrayList<>();

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:suppliers")
	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:supplierPlants")
	public List<NodeRef> getSupplierPlants() {
		return supplierPlants;
	}

	public void setSupplierPlants(List<NodeRef> supplierPlants) {
		this.supplierPlants = supplierPlants;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((supplierPlants == null) ? 0 : supplierPlants.hashCode());
		result = prime * result + ((suppliers == null) ? 0 : suppliers.hashCode());
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
