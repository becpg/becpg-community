/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelLeaf;

/**
 * <p>PackagingMaterialData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:packagingMaterial")
@MultiLevelLeaf
public class PackagingMaterialData extends ProductData {

	private static final long serialVersionUID = 1386599003766479590L;

	private List<NodeRef> packagingMaterials = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>packagingMaterials</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pack:pmMaterialRefs")
	public List<NodeRef> getPackagingMaterials() {
		return packagingMaterials;
	}

	/**
	 * <p>Setter for the field <code>packagingMaterials</code>.</p>
	 *
	 * @param packagingMaterials a {@link java.util.List} object.
	 */
	public void setPackagingMaterials(List<NodeRef> packagingMaterials) {
		this.packagingMaterials = packagingMaterials;
	}

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public static PackagingMaterialData build() {
		return new PackagingMaterialData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public PackagingMaterialData withName(String name) {
		setName(name);
		return this;
	}

	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public PackagingMaterialData withLegalName(String legalName) {
		setLegalName(legalName);
		return this;
	}

	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public PackagingMaterialData withLegalName(MLText legalName) {
		setLegalName(legalName);
		return this;
	}

	/**
	 * <p>withUnit.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public PackagingMaterialData withUnit(ProductUnit unit) {
		setUnit(unit);
		return this;
	}

	/**
	 * <p>withQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public PackagingMaterialData withQty(Double qty) {
		setQty(qty);
		return this;
	}


	/**
	 * <p>withTare.</p>
	 *
	 * @param tare a {@link java.lang.Double} object
	 * @param tareUnit a {@link fr.becpg.repo.product.data.constraints.TareUnit} object
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public PackagingMaterialData withTare(Double tare, TareUnit tareUnit) {
		setTare(tare);
		setTareUnit(tareUnit);
		return this;
	}
	

	/**
	 * <p>withPackMaterialList.</p>
	 *
	 * @param packMaterialList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 */
	public PackagingMaterialData withPackMaterialList(List<PackMaterialListDataItem> packMaterialList) {
		setPackMaterialList(packMaterialList);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((packagingMaterials == null) ? 0 : packagingMaterials.hashCode());
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
		PackagingMaterialData other = (PackagingMaterialData) obj;
		if (packagingMaterials == null) {
			if (other.packagingMaterials != null)
				return false;
		} else if (!packagingMaterials.equals(other.packagingMaterials))
			return false;
		return true;
	}



}
