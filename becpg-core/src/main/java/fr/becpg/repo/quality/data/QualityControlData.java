package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;

public class QualityControlData extends BeCPGDataObject {

	
	List<NodeRef>controlPlans = new ArrayList<NodeRef>();
	Integer samplesCounter;
	String state;
	
	// batchAspect	
	String batchId;
	String orderId;
	Date batchStart;
	Integer batchDuration;
	NodeRef product;
	NodeRef client;
	NodeRef supplier;
	
	List<SamplingListDataItem> samplingList = new ArrayList<SamplingListDataItem>();


	public List<NodeRef> getControlPlans() {
		return controlPlans;
	}

	public void setControlPlans(List<NodeRef> controlPlans) {
		this.controlPlans = controlPlans;
	}
	
	public Integer getSamplesCounter() {
		return samplesCounter;
	}

	public void setSamplesCounter(Integer samplesCounter) {
		this.samplesCounter = samplesCounter;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public Date getBatchStart() {
		return batchStart;
	}

	public void setBatchStart(Date batchStart) {
		this.batchStart = batchStart;
	}

	public Integer getBatchDuration() {
		return batchDuration;
	}

	public void setBatchDuration(Integer batchDuration) {
		this.batchDuration = batchDuration;
	}

	public NodeRef getProduct() {
		return product;
	}

	public void setProduct(NodeRef product) {
		this.product = product;
	}

	public NodeRef getClient() {
		return client;
	}

	public void setClient(NodeRef client) {
		this.client = client;
	}

	public NodeRef getSupplier() {
		return supplier;
	}

	public void setSupplier(NodeRef supplier) {
		this.supplier = supplier;
	}

	public List<SamplingListDataItem> getSamplingList() {
		return samplingList;
	}

	public void setSamplingList(List<SamplingListDataItem> samplingList) {
		this.samplingList = samplingList;
	}		
}
