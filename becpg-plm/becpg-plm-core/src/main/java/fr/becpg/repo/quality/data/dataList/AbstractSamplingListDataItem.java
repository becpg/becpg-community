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

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>Abstract AbstractSamplingListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractSamplingListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4363331493366178223L;
	protected NodeRef controlPoint;
	protected NodeRef controlStep;
	protected NodeRef samplingGroup;
	protected NodeRef controlingGroup;
	protected NodeRef fixingGroup;
	protected String reaction;
	
	/**
	 * <p>Getter for the field <code>controlPoint</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "qa:slControlPoint")
	public NodeRef getControlPoint() {
		return controlPoint;
	}

	/**
	 * <p>Setter for the field <code>controlPoint</code>.</p>
	 *
	 * @param controlPoint a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setControlPoint(NodeRef controlPoint) {
		this.controlPoint = controlPoint;
	}

	/**
	 * <p>Getter for the field <code>controlStep</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "qa:slControlStep")
	public NodeRef getControlStep() {
		return controlStep;
	}

	/**
	 * <p>Setter for the field <code>controlStep</code>.</p>
	 *
	 * @param controlStep a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setControlStep(NodeRef controlStep) {
		this.controlStep = controlStep;
	}

	/**
	 * <p>Getter for the field <code>controlingGroup</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "qa:slControlingGroup")
	public NodeRef getControlingGroup() {
		return controlingGroup;
	}

	/**
	 * <p>Setter for the field <code>controlingGroup</code>.</p>
	 *
	 * @param controlingGroup a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setControlingGroup(NodeRef controlingGroup) {
		this.controlingGroup = controlingGroup;
	}

	/**
	 * <p>Getter for the field <code>samplingGroup</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "qa:slSamplingGroup")
	public NodeRef getSamplingGroup() {
		return samplingGroup;
	}

	/**
	 * <p>Setter for the field <code>samplingGroup</code>.</p>
	 *
	 * @param samplingGroup a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setSamplingGroup(NodeRef samplingGroup) {
		this.samplingGroup = samplingGroup;
	}

	/**
	 * <p>Getter for the field <code>fixingGroup</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "qa:slFixingGroup")
	public NodeRef getFixingGroup() {
		return fixingGroup;
	}

	/**
	 * <p>Setter for the field <code>fixingGroup</code>.</p>
	 *
	 * @param fixingGroup a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setFixingGroup(NodeRef fixingGroup) {
		this.fixingGroup = fixingGroup;
	}

	/**
	 * <p>Getter for the field <code>reaction</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:slReaction")
	public String getReaction() {
		return reaction;
	}

	/**
	 * <p>Setter for the field <code>reaction</code>.</p>
	 *
	 * @param reaction a {@link java.lang.String} object.
	 */
	public void setReaction(String reaction) {
		this.reaction = reaction;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((controlPoint == null) ? 0 : controlPoint.hashCode());
		result = prime * result + ((controlStep == null) ? 0 : controlStep.hashCode());
		result = prime * result + ((controlingGroup == null) ? 0 : controlingGroup.hashCode());
		result = prime * result + ((fixingGroup == null) ? 0 : fixingGroup.hashCode());
		result = prime * result + ((reaction == null) ? 0 : reaction.hashCode());
		result = prime * result + ((samplingGroup == null) ? 0 : samplingGroup.hashCode());
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
		AbstractSamplingListDataItem other = (AbstractSamplingListDataItem) obj;
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
		if (controlingGroup == null) {
			if (other.controlingGroup != null)
				return false;
		} else if (!controlingGroup.equals(other.controlingGroup))
			return false;
		if (fixingGroup == null) {
			if (other.fixingGroup != null)
				return false;
		} else if (!fixingGroup.equals(other.fixingGroup))
			return false;
		if (reaction == null) {
			if (other.reaction != null)
				return false;
		} else if (!reaction.equals(other.reaction))
			return false;
		if (samplingGroup == null) {
			if (other.samplingGroup != null)
				return false;
		} else if (!samplingGroup.equals(other.samplingGroup))
			return false;
		return true;
	}

}
