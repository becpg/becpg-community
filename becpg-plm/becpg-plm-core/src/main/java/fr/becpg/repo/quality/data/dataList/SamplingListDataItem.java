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
package fr.becpg.repo.quality.data.dataList;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "qa:samplingList")
public class SamplingListDataItem extends BeCPGDataObject {

	private Date dateTime;
	private String sampleId;
	private String sampleState;
	private NodeRef controlPoint;
	private NodeRef controlStep;

	@AlfProp
	@AlfQname(qname = "qa:slDateTime")
	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	@AlfProp
	@AlfQname(qname = "qa:slSampleId")
	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	@AlfProp
	@AlfQname(qname = "qa:slSampleState")
	public String getSampleState() {
		return sampleState;
	}

	public void setSampleState(String sampleState) {
		this.sampleState = sampleState;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "qa:slControlPoint")
	public NodeRef getControlPoint() {
		return controlPoint;
	}

	public void setControlPoint(NodeRef controlPoint) {
		this.controlPoint = controlPoint;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "qa:slControlStep")
	public NodeRef getControlStep() {
		return controlStep;
	}

	public void setControlStep(NodeRef controlStep) {
		this.controlStep = controlStep;
	}

	public SamplingListDataItem() {
		super();
	}

	public SamplingListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	
	public SamplingListDataItem(Date dateTime, String sampleId, String sampleState, NodeRef controlPoint, NodeRef controlStep) {
		super();
		this.dateTime = dateTime;
		this.sampleId = sampleId;
		this.sampleState = sampleState;
		this.controlPoint = controlPoint;
		this.controlStep = controlStep;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((controlPoint == null) ? 0 : controlPoint.hashCode());
		result = prime * result + ((controlStep == null) ? 0 : controlStep.hashCode());
		result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
		result = prime * result + ((sampleId == null) ? 0 : sampleId.hashCode());
		result = prime * result + ((sampleState == null) ? 0 : sampleState.hashCode());
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
		SamplingListDataItem other = (SamplingListDataItem) obj;
		if (controlPoint == null) {
			if (other.controlPoint != null)
				return false;
		} else if (!controlPoint.equals(other.controlPoint))
			return false;
		if (controlStep == null) {
			if (other.controlStep != null)
				return false;
		} else if (!controlStep.equals(other.controlStep))
			return false;
		if (dateTime == null) {
			if (other.dateTime != null)
				return false;
		} else if (!dateTime.equals(other.dateTime))
			return false;
		if (sampleId == null) {
			if (other.sampleId != null)
				return false;
		} else if (!sampleId.equals(other.sampleId))
			return false;
		if (sampleState == null) {
			if (other.sampleState != null)
				return false;
		} else if (!sampleState.equals(other.sampleState))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SamplingListDataItem [dateTime=" + dateTime + ", sampleId=" + sampleId + ", sampleState=" + sampleState + ", controlPoint=" + controlPoint + ", controlStep="
				+ controlStep + "]";
	}

}
