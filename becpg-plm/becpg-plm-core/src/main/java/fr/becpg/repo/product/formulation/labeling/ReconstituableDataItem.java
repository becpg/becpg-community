package fr.becpg.repo.product.formulation.labeling;

import org.alfresco.service.cmr.repository.NodeRef;

public class ReconstituableDataItem {
	
	private NodeRef productNodeRef;
	
	private Double rate;
	
	private Integer priority;
	
	private NodeRef diluentIngNodeRef;
	
	private NodeRef targetIngNodeRef;

	public ReconstituableDataItem(NodeRef productNodeRef, Double rate, Integer priority, NodeRef diluentIngNodeRef, NodeRef targetIngNodeRef) {
		super();
		this.productNodeRef = productNodeRef;
		this.rate = rate;
		if(priority == null){
			priority = 0;
		} else {
			this.priority = priority;
		}
		this.diluentIngNodeRef = diluentIngNodeRef;
		this.targetIngNodeRef = targetIngNodeRef;
	}

	public NodeRef getProductNodeRef() {
		return productNodeRef;
	}

	public Double getRate() {
		return rate;
	}

	public Integer getPriority() {
		return priority;
	}

	public NodeRef getDiluentIngNodeRef() {
		return diluentIngNodeRef;
	}

	public NodeRef getTargetIngNodeRef() {
		return targetIngNodeRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diluentIngNodeRef == null) ? 0 : diluentIngNodeRef.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((productNodeRef == null) ? 0 : productNodeRef.hashCode());
		result = prime * result + ((rate == null) ? 0 : rate.hashCode());
		result = prime * result + ((targetIngNodeRef == null) ? 0 : targetIngNodeRef.hashCode());
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
		ReconstituableDataItem other = (ReconstituableDataItem) obj;
		if (diluentIngNodeRef == null) {
			if (other.diluentIngNodeRef != null)
				return false;
		} else if (!diluentIngNodeRef.equals(other.diluentIngNodeRef))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (productNodeRef == null) {
			if (other.productNodeRef != null)
				return false;
		} else if (!productNodeRef.equals(other.productNodeRef))
			return false;
		if (rate == null) {
			if (other.rate != null)
				return false;
		} else if (!rate.equals(other.rate))
			return false;
		if (targetIngNodeRef == null) {
			if (other.targetIngNodeRef != null)
				return false;
		} else if (!targetIngNodeRef.equals(other.targetIngNodeRef))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReconstituableDataItem [productNodeRef=" + productNodeRef + ", rate=" + rate + ", priority=" + priority + ", diluentIngNodeRef=" + diluentIngNodeRef + ", targetIngNodeRef="
				+ targetIngNodeRef + "]";
	}

	
	
	

}
