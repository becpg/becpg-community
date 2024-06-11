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
package fr.becpg.repo.quality.data.dataList;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.QualityControlState;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

/**
 * <p>SamplingListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:samplingList")
public class SamplingListDataItem extends AbstractSamplingListDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6184834607047086429L;
	private Date dateTime;
	private String timePeriod;
	private String sampleId;
	private QualityControlState sampleState;

	/**
	 * <p>Getter for the field <code>dateTime</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:slDateTime")
	public Date getDateTime() {
		return dateTime;
	}

	/**
	 * <p>Setter for the field <code>dateTime</code>.</p>
	 *
	 * @param dateTime a {@link java.util.Date} object.
	 */
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	
	/**
	 * <p>Getter for the field <code>timePeriod</code>.</p>
	 *
	 * @return a {@link java.util.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:slTimePeriod")
	public String getTimePeriod() {
		return timePeriod;
	}

	/**
	 * <p>Setter for the field <code>timePeriod</code>.</p>
	 *
	 * @param timePeriod a {@link java.util.String} object.
	 */
	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}


	/**
	 * <p>Getter for the field <code>sampleId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:slSampleId")
	public String getSampleId() {
		return sampleId;
	}

	/**
	 * <p>Setter for the field <code>sampleId</code>.</p>
	 *
	 * @param sampleId a {@link java.lang.String} object.
	 */
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	/**
	 * <p>Getter for the field <code>sampleState</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:slSampleState")
	public QualityControlState getSampleState() {
		return sampleState;
	}

	/**
	 * <p>Setter for the field <code>sampleState</code>.</p>
	 *
	 * @param sampleState a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 */
	public void setSampleState(QualityControlState sampleState) {
		this.sampleState = sampleState;
	}

	/**
	 * <p>Constructor for SamplingListDataItem.</p>
	 */
	public SamplingListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for SamplingListDataItem.</p>
	 *
	 * @param dateTime a {@link java.util.Date} object.
	 * @param sampleState a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 * @param controlPoint a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param controlStep a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param samplingGroup a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param controlingGroup a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param fixingGroup a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reaction a {@link java.lang.String} object.
	 * @param timePeriod a {@link java.lang.String} object
	 */
	public SamplingListDataItem(Date dateTime, String timePeriod, QualityControlState sampleState, NodeRef controlPoint, NodeRef controlStep, NodeRef samplingGroup, NodeRef controlingGroup, NodeRef fixingGroup, String reaction) {
		super();
		this.dateTime = dateTime;
		this.timePeriod = timePeriod;
		this.sampleState = sampleState;
		this.controlPoint = controlPoint;
		this.controlStep = controlStep;
		this.samplingGroup = samplingGroup;
		this.controlingGroup = controlingGroup;
		this.fixingGroup = fixingGroup;
		this.reaction = reaction;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SamplingListDataItem [dateTime=" + dateTime + ", sampleId=" + sampleId + ", sampleState=" + sampleState + ", controlPoint=" + controlPoint + ", controlStep="
				+ controlStep + "]";
	}

}
