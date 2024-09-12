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

	
	
	/**
	 * <p>Getter for the field <code>stockListItems</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "qa:batchAllocationStockRefs")
	public List<NodeRef> getStockListItems() {
		return stockListItems;
	}

	/**
	 * <p>Setter for the field <code>stockListItems</code>.</p>
	 *
	 * @param stockListItems a {@link java.util.List} object
	 */
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

	/**
	 * <p>Getter for the field <code>batchQty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "qa:batchQty")
	public Double getBatchQty() {
		return batchQty;
	}

	/**
	 * <p>Setter for the field <code>batchQty</code>.</p>
	 *
	 * @param batchQty a {@link java.lang.Double} object
	 */
	public void setBatchQty(Double batchQty) {
		this.batchQty = batchQty;
	}

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.model.SystemState} object
	 */
	@AlfProp
	@AlfQname(qname = "qa:batchState")
	public SystemState getState() {
		return state;
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param state a {@link fr.becpg.model.SystemState} object
	 */
	public void setState(SystemState state) {
		this.state = state;
	}
	
	
	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 */
	@AlfProp
	@AlfQname(qname = "qa:batchQtyUnit")
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AllocationListDataItem [batchQty=" + batchQty + ", state=" + state + ", unit=" + unit + ", product=" + product + ", stockListItems="
				+ stockListItems + ", sort=" + sort + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(batchQty, product, sort, state, stockListItems, unit);
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
		AllocationListDataItem other = (AllocationListDataItem) obj;
		return Objects.equals(batchQty, other.batchQty) && Objects.equals(product, other.product) && Objects.equals(sort, other.sort)
				&& state == other.state && Objects.equals(stockListItems, other.stockListItems) && unit == other.unit;
	}

}
