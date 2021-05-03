/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.repo.quality.data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>BatchData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:batch")
public class BatchData extends BeCPGDataObject implements FormulatedEntity {

	/**
	 *
	 */
	private static final long serialVersionUID = -2514037897788777042L;
	private String batchId;
	private Double batchQty = 0d;
	private SystemState state = SystemState.Simulation;
	private ProductUnit unit = ProductUnit.kg;
	private ProductData product;

	private NodeRef entityTpl;

	/*
	 * Formulation
	 */
	private Date formulatedDate;
	private Integer reformulateCount;
	private Integer currentReformulateCount;
	private String formulationChainId;
	private Boolean updateFormulatedDate = true;
	private String requirementChecksum;

	private List<AllocationListDataItem> allocationList;

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
	@AlfSingleAssoc(isEntity = true, isCacheable = false)
	@AlfQname(qname = "qa:product")
	public ProductData getProduct() {
		return product;
	}

	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProduct(ProductData product) {
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

	@DataList
	@AlfQname(qname = "qa:batchAllocationList")
	public List<AllocationListDataItem> getAllocationList() {
		return allocationList;
	}

	public void setAllocationList(List<AllocationListDataItem> allocationList) {
		this.allocationList = allocationList;
	}

	/**
	 * <p>Getter for the field <code>projectTpl</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:entityTplRef")
	public NodeRef getEntityTpl() {
		return entityTpl;
	}

	/**
	 * <p>getFormulatedEntityTpl.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@Override
	public NodeRef getFormulatedEntityTpl() {
		return getEntityTpl();
	}

	/**
	 * <p>Setter for the field <code>projectTpl</code>.</p>
	 *
	 * @param projectTpl a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setEntityTpl(NodeRef entityTpl) {
		this.entityTpl = entityTpl;
	}

	/**
	 * <p>Getter for the field <code>formulatedDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:formulatedDate")
	public Date getFormulatedDate() {
		return formulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulatedDate(Date formulatedDate) {
		this.formulatedDate = formulatedDate;
	}

	/**
	 * <p>Getter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:requirementChecksum")
	public String getRequirementChecksum() {
		return requirementChecksum;
	}

	/**
	 * <p>Setter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @param requirementChecksum a {@link java.lang.String} object.
	 */
	public void setRequirementChecksum(String requirementChecksum) {
		this.requirementChecksum = requirementChecksum;
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldUpdateFormulatedDate() {
		return updateFormulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setUpdateFormulatedDate(boolean updateFormulatedDate) {
		this.updateFormulatedDate = updateFormulatedDate;
	}

	/**
	 * <p>Getter for the field <code>reformulateCount</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Override
	public Integer getReformulateCount() {
		return reformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setReformulateCount(Integer reformulateCount) {
		this.reformulateCount = reformulateCount;
	}

	/**
	 * <p>Getter for the field <code>currentReformulateCount</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@Override
	public Integer getCurrentReformulateCount() {
		return currentReformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentReformulateCount(Integer currentReformulateCount) {
		this.currentReformulateCount = currentReformulateCount;
	}

	/**
	 * <p>Getter for the field <code>formulationChainId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	public String getFormulationChainId() {
		return formulationChainId;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulationChainId(String formulationChainId) {
		this.formulationChainId = formulationChainId;
	}

	@Override
	public String toString() {
		return "BatchData [batchId=" + batchId + ", batchQty=" + batchQty + ", state=" + state + ", unit=" + unit + ", product=" + product
				+ ", entityTpl=" + entityTpl + ", formulatedDate=" + formulatedDate + ", reformulateCount=" + reformulateCount
				+ ", currentReformulateCount=" + currentReformulateCount + ", formulationChainId=" + formulationChainId + ", updateFormulatedDate="
				+ updateFormulatedDate + ", requirementChecksum=" + requirementChecksum + ", allocationList=" + allocationList + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + Objects.hash(allocationList, batchId, batchQty, currentReformulateCount, entityTpl, formulatedDate,
				formulationChainId, product, reformulateCount, requirementChecksum, state, unit, updateFormulatedDate);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BatchData other = (BatchData) obj;
		return Objects.equals(allocationList, other.allocationList) && Objects.equals(batchId, other.batchId)
				&& Objects.equals(batchQty, other.batchQty) && Objects.equals(currentReformulateCount, other.currentReformulateCount)
				&& Objects.equals(entityTpl, other.entityTpl) && Objects.equals(formulatedDate, other.formulatedDate)
				&& Objects.equals(formulationChainId, other.formulationChainId) && Objects.equals(product, other.product)
				&& Objects.equals(reformulateCount, other.reformulateCount) && Objects.equals(requirementChecksum, other.requirementChecksum)
				&& (state == other.state) && (unit == other.unit) && Objects.equals(updateFormulatedDate, other.updateFormulatedDate);
	}

}
