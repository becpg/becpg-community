/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "qa:qualityControl")
public class QualityControlData extends BeCPGDataObject {

	List<NodeRef> controlPlans = new ArrayList<NodeRef>();
	Integer samplesCounter;
	QualityControlState state;

	// batchAspect
	String batchId;
	String orderId;
	Date batchStart;
	Integer batchDuration;
	NodeRef product;
	NodeRef client;
	NodeRef supplier;

	List<SamplingListDataItem> samplingList = new LinkedList<SamplingListDataItem>();

	@AlfMultiAssoc
	@AlfQname(qname = "qa:qcControlPlans")
	public List<NodeRef> getControlPlans() {
		return controlPlans;
	}

	public void setControlPlans(List<NodeRef> controlPlans) {
		this.controlPlans = controlPlans;
	}

	@AlfProp
	@AlfQname(qname = "qa:qcSamplesCounter")
	public Integer getSamplesCounter() {
		return samplesCounter;
	}

	public void setSamplesCounter(Integer samplesCounter) {
		this.samplesCounter = samplesCounter;
	}

	@AlfProp
	@AlfQname(qname = "qa:qcState")
	public QualityControlState getState() {
		return state;
	}

	public void setState(QualityControlState state) {
		this.state = state;
	}

	@AlfProp
	@AlfQname(qname = "qa:batchId")
	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	@AlfProp
	@AlfQname(qname = "qa:orderId")
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@AlfProp
	@AlfQname(qname = "qa:batchStart")
	public Date getBatchStart() {
		return batchStart;
	}

	public void setBatchStart(Date batchStart) {
		this.batchStart = batchStart;
	}

	@AlfProp
	@AlfQname(qname = "qa:batchDuration")
	public Integer getBatchDuration() {
		return batchDuration;
	}

	public void setBatchDuration(Integer batchDuration) {
		this.batchDuration = batchDuration;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "qa:product")
	public NodeRef getProduct() {
		return product;
	}

	public void setProduct(NodeRef product) {
		this.product = product;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:clients")
	public NodeRef getClient() {
		return client;
	}

	public void setClient(NodeRef client) {
		this.client = client;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:suppliers")
	public NodeRef getSupplier() {
		return supplier;
	}

	public void setSupplier(NodeRef supplier) {
		this.supplier = supplier;
	}

	@DataList
	@AlfQname(qname = "qa:samplingList")
	public List<SamplingListDataItem> getSamplingList() {
		return samplingList;
	}

	public void setSamplingList(List<SamplingListDataItem> samplingList) {
		this.samplingList = samplingList;
	}

	public QualityControlData() {
		super();
	}

	public QualityControlData(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	@Override
	public String toString() {
		return "QualityControlData [controlPlans=" + controlPlans + ", samplesCounter=" + samplesCounter + ", state=" + state + ", batchId=" + batchId + ", orderId=" + orderId
				+ ", batchStart=" + batchStart + ", batchDuration=" + batchDuration + ", product=" + product + ", client=" + client + ", supplier=" + supplier + ", samplingList="
				+ samplingList + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((batchDuration == null) ? 0 : batchDuration.hashCode());
		result = prime * result + ((batchId == null) ? 0 : batchId.hashCode());
		result = prime * result + ((batchStart == null) ? 0 : batchStart.hashCode());
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + ((controlPlans == null) ? 0 : controlPlans.hashCode());
		result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((samplesCounter == null) ? 0 : samplesCounter.hashCode());
		result = prime * result + ((samplingList == null) ? 0 : samplingList.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
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
		if (controlPlans == null) {
			if (other.controlPlans != null)
				return false;
		} else if (!controlPlans.equals(other.controlPlans))
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
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (supplier == null) {
			if (other.supplier != null)
				return false;
		} else if (!supplier.equals(other.supplier))
			return false;
		return true;
	}

}
