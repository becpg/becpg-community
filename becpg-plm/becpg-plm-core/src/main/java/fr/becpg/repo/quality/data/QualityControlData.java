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
package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.dataList.ControlListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>QualityControlData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:qualityControl")
public class QualityControlData extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2908648013735858142L;
	List<NodeRef> controlPlans = new ArrayList<>();
	Integer samplesCounter;
	Date nextAnalysisDate;
	QualityControlState state;

	// batchAspect
	String batchId;
	String orderId;
	Date batchStart;
	Integer batchDuration;
	NodeRef product;
	NodeRef client;
	NodeRef supplier;

	List<SamplingListDataItem> samplingList = new ArrayList<>();
	
	List<ControlListDataItem> controlList = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>controlPlans</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "qa:qcControlPlans")
	public List<NodeRef> getControlPlans() {
		return controlPlans;
	}

	/**
	 * <p>Setter for the field <code>controlPlans</code>.</p>
	 *
	 * @param controlPlans a {@link java.util.List} object.
	 */
	public void setControlPlans(List<NodeRef> controlPlans) {
		this.controlPlans = controlPlans;
	}
	
	/**
	 * <p>Getter for the field <code>samplesCounter</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:qcSamplesCounter")
	public Integer getSamplesCounter() {
		return samplesCounter;
	}

	/**
	 * <p>Setter for the field <code>samplesCounter</code>.</p>
	 *
	 * @param samplesCounter a {@link java.lang.Integer} object.
	 */
	public void setSamplesCounter(Integer samplesCounter) {
		this.samplesCounter = samplesCounter;
	}
	
	/**
	 * <p>Getter for the field <code>nextAnalysisDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:qcNextAnalysisDate")
	public Date getNextAnalysisDate() {
		return nextAnalysisDate;
	}

	/**
	 * <p>Setter for the field <code>nextAnalysisDate</code>.</p>
	 *
	 * @param nextAnalysisDate a {@link java.util.Date} object.
	 */
	public void setNextAnalysisDate(Date nextAnalysisDate) {
		this.nextAnalysisDate = nextAnalysisDate;
	}

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:qcState")
	public QualityControlState getState() {
		return state;
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param state a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 */
	public void setState(QualityControlState state) {
		this.state = state;
	}

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
	 * <p>Getter for the field <code>orderId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:orderId")
	public String getOrderId() {
		return orderId;
	}

	/**
	 * <p>Setter for the field <code>orderId</code>.</p>
	 *
	 * @param orderId a {@link java.lang.String} object.
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	/**
	 * <p>Getter for the field <code>batchStart</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:batchStart")
	public Date getBatchStart() {
		return batchStart;
	}

	/**
	 * <p>Setter for the field <code>batchStart</code>.</p>
	 *
	 * @param batchStart a {@link java.util.Date} object.
	 */
	public void setBatchStart(Date batchStart) {
		this.batchStart = batchStart;
	}

	/**
	 * <p>Getter for the field <code>batchDuration</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:batchDuration")
	public Integer getBatchDuration() {
		return batchDuration;
	}

	/**
	 * <p>Setter for the field <code>batchDuration</code>.</p>
	 *
	 * @param batchDuration a {@link java.lang.Integer} object.
	 */
	public void setBatchDuration(Integer batchDuration) {
		this.batchDuration = batchDuration;
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
	 * <p>Getter for the field <code>client</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:clients")
	public NodeRef getClient() {
		return client;
	}

	/**
	 * <p>Setter for the field <code>client</code>.</p>
	 *
	 * @param client a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setClient(NodeRef client) {
		this.client = client;
	}

	/**
	 * <p>Getter for the field <code>supplier</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:suppliers")
	public NodeRef getSupplier() {
		return supplier;
	}

	/**
	 * <p>Setter for the field <code>supplier</code>.</p>
	 *
	 * @param supplier a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setSupplier(NodeRef supplier) {
		this.supplier = supplier;
	}

	/**
	 * <p>Getter for the field <code>samplingList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "qa:samplingList")
	public List<SamplingListDataItem> getSamplingList() {
		return samplingList;
	}

	/**
	 * <p>Setter for the field <code>samplingList</code>.</p>
	 *
	 * @param samplingList a {@link java.util.List} object.
	 */
	public void setSamplingList(List<SamplingListDataItem> samplingList) {
		this.samplingList = samplingList;
	}
	
	/**
	 * <p>Getter for the field <code>controlList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "qa:controlList")
	public List<ControlListDataItem> getControlList() {
		return controlList;
	}

	/**
	 * <p>Setter for the field <code>controlList</code>.</p>
	 *
	 * @param controlList a {@link java.util.List} object.
	 */
	public void setControlList(List<ControlListDataItem> controlList) {
		this.controlList = controlList;
	}

	/**
	 * <p>Constructor for QualityControlData.</p>
	 */
	public QualityControlData() {
		super();
	}

	/**
	 * <p>Constructor for QualityControlData.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public QualityControlData(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "QualityControlData [controlPlans=" + controlPlans + ", samplesCounter=" + samplesCounter
				+ ", nextAnalysisDate=" + nextAnalysisDate + ", state=" + state + ", batchId=" + batchId + ", orderId="
				+ orderId + ", batchStart=" + batchStart + ", batchDuration=" + batchDuration + ", product=" + product
				+ ", client=" + client + ", supplier=" + supplier + ", samplingList=" + samplingList + ", controlList="
				+ controlList + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((batchDuration == null) ? 0 : batchDuration.hashCode());
		result = prime * result + ((batchId == null) ? 0 : batchId.hashCode());
		result = prime * result + ((batchStart == null) ? 0 : batchStart.hashCode());
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + ((controlList == null) ? 0 : controlList.hashCode());
		result = prime * result + ((controlPlans == null) ? 0 : controlPlans.hashCode());
		result = prime * result + ((nextAnalysisDate == null) ? 0 : nextAnalysisDate.hashCode());
		result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((samplesCounter == null) ? 0 : samplesCounter.hashCode());
		result = prime * result + ((samplingList == null) ? 0 : samplingList.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
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
		QualityControlData other = (QualityControlData) obj;
		if (batchDuration == null) {
			if (other.batchDuration != null)
				return false;
		} else if (!batchDuration.equals(other.batchDuration))
			return false;
		if (batchId == null) {
			if (other.batchId != null)
				return false;
		} else if (!batchId.equals(other.batchId))
			return false;
		if (batchStart == null) {
			if (other.batchStart != null)
				return false;
		} else if (!batchStart.equals(other.batchStart))
			return false;
		if (client == null) {
			if (other.client != null)
				return false;
		} else if (!client.equals(other.client))
			return false;
		if (controlList == null) {
			if (other.controlList != null)
				return false;
		} else if (!controlList.equals(other.controlList))
			return false;
		if (controlPlans == null) {
			if (other.controlPlans != null)
				return false;
		} else if (!controlPlans.equals(other.controlPlans))
			return false;
		if (nextAnalysisDate == null) {
			if (other.nextAnalysisDate != null)
				return false;
		} else if (!nextAnalysisDate.equals(other.nextAnalysisDate))
			return false;
		if (orderId == null) {
			if (other.orderId != null)
				return false;
		} else if (!orderId.equals(other.orderId))
			return false;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		if (samplesCounter == null) {
			if (other.samplesCounter != null)
				return false;
		} else if (!samplesCounter.equals(other.samplesCounter))
			return false;
		if (samplingList == null) {
			if (other.samplingList != null)
				return false;
		} else if (!samplingList.equals(other.samplingList))
			return false;
		if (state != other.state)
			return false;
		if (supplier == null) {
			if (other.supplier != null)
				return false;
		} else if (!supplier.equals(other.supplier))
			return false;
		return true;
	}

	
}
