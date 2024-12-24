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

import java.util.ArrayList;
import java.util.Date;
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
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>BatchListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:stockList")
public class StockListDataItem extends BeCPGDataObject {

	/**
	 *
	 */
	private static final long serialVersionUID = -888800732698611573L;
	private String batchId;
	private Double batchQty = 0d;
	private SystemState state = SystemState.Simulation;
	private ProductUnit unit = ProductUnit.kg;
	private NodeRef product;
	private Date useByDate;

	private List<NodeRef> plants = new ArrayList<>();
	private List<NodeRef> laboratories = new ArrayList<>();
	

	/**
	 * <p>Getter for the field <code>batchId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:batchId")
	public String getBatchId() {
		return batchId;
	}

	/**
	 * <p>Setter for the field <code>batchId</code>.</p>
	 *
	 * @param batchId a {@link java.lang.String} object.
	 */
	public void setBatchId(String batchId) {
		this.batchId = batchId;
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
	 * <p>Getter for the field <code>useByDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "qa:productUseByDate")
	public Date getUseByDate() {
		return useByDate;
	}

	/**
	 * <p>Setter for the field <code>useByDate</code>.</p>
	 *
	 * @param useByDate a {@link java.util.Date} object
	 */
	public void setUseByDate(Date useByDate) {
		this.useByDate = useByDate;
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
	 * <p>Getter for the field <code>plants</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:plants")
	public List<NodeRef> getPlants() {
		return plants;
	}

	/**
	 * <p>Setter for the field <code>plants</code>.</p>
	 *
	 * @param plants a {@link java.util.List} object
	 */
	public void setPlants(List<NodeRef> plants) {
		this.plants = plants;
	}
	/**
	 * <p>Getter for the field <code>laboratories</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:laboratories")
	public List<NodeRef> getLaboratories() {
		return laboratories;
	}

	/**
	 * <p>Setter for the field <code>laboratories</code>.</p>
	 *
	 * @param laboratories a {@link java.util.List} object
	 */
	public void setLaboratories(List<NodeRef> laboratories) {
		this.laboratories = laboratories;
	}

	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "BatchListDataItem [batchId=" + batchId + ", batchQty=" + batchQty + ", state=" + state + ", unit=" + unit + ", product=" + product
				+ "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(batchId, batchQty, laboratories, plants, product, state, unit, useByDate);
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
		StockListDataItem other = (StockListDataItem) obj;
		return Objects.equals(batchId, other.batchId) && Objects.equals(batchQty, other.batchQty) && Objects.equals(laboratories, other.laboratories)
				&& Objects.equals(plants, other.plants) && Objects.equals(product, other.product) && state == other.state && unit == other.unit
				&& Objects.equals(useByDate, other.useByDate);
	}

}
