/*******************************************************************************
private * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.repo.quality.data.dataList;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.SortableDataItem;

/**
 * <p>BatchListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:batchAllocationList")
public class AllocationListDataItem extends BeCPGDataObject implements SortableDataItem {

	/**
	 *
	 */
	private static final long serialVersionUID = -888800732698611573L;

	private Double batchQty = 0d;
	private SystemState state = SystemState.Simulation;
	private ProductUnit unit = ProductUnit.kg;
	private NodeRef product;
	private List<NodeRef> stockListItems; 
	private Integer sort;

	
	
	@AlfMultiAssoc
	@AlfQname(qname = "qa:batchAllocationStockRefs")
	public List<NodeRef> getStockListItems() {
		return stockListItems;
	}

	public void setStockListItems(List<NodeRef> stockListItems) {
		this.stockListItems = stockListItems;
	}

	

	/**
	 * <p>Getter for the field <code>product</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "qa:product")
	public NodeRef getProduct() {
		return product;
	}

	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}

	@AlfProp
	@AlfQname(qname = "qa:batchQty")
	public Double getBatchQty() {
		return batchQty;
	}

	public void setBatchQty(Double batchQty) {
		this.batchQty = batchQty;
	}

	@AlfProp
	@AlfQname(qname = "qa:batchState")
	public SystemState getState() {
		return state;
	}

	public void setState(SystemState state) {
		this.state = state;
	}
	
	
	@AlfProp
	@AlfQname(qname = "qa:batchQtyUnit")
	public ProductUnit getUnit() {
		return unit;
	}

	public void setUnit(ProductUnit unit) {
		this.unit = unit;
	}
	

	/**
	 * <p>Getter for the field <code>sort</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	/** {@inheritDoc} */
	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		return "AllocationListDataItem [batchQty=" + batchQty + ", state=" + state + ", unit=" + unit + ", product=" + product + ", stockListItems="
				+ stockListItems + ", sort=" + sort + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(batchQty, product, sort, state, stockListItems, unit);
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
		AllocationListDataItem other = (AllocationListDataItem) obj;
		return Objects.equals(batchQty, other.batchQty) && Objects.equals(product, other.product) && Objects.equals(sort, other.sort)
				&& state == other.state && Objects.equals(stockListItems, other.stockListItems) && unit == other.unit;
	}

}
