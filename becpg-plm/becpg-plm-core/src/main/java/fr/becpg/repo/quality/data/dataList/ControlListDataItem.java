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
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.QualityControlState;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>ControlListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:controlList")
public class ControlListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7794847133363024701L;
	private String type;
	private Double mini;
	private Double maxi;
	private Boolean required;
	private String sampleId;
	private Double value;
	private Double target;
	private String unit;
	private MLText textCriteria;
	private QualityControlState state;
	private String temperature;
	private String timePeriod;
	private NodeRef method;
	private List<NodeRef> characts = new ArrayList<>();
	private NodeRef controlPoint;

	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clType")
	public String getType() {
		return type;
	}

	/**
	 * <p>Setter for the field <code>type</code>.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * <p>Getter for the field <code>mini</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clMini")
	public Double getMini() {
		return mini;
	}

	/**
	 * <p>Setter for the field <code>mini</code>.</p>
	 *
	 * @param mini a {@link java.lang.Double} object.
	 */
	public void setMini(Double mini) {
		this.mini = mini;
	}

	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clMaxi")
	public Double getMaxi() {
		return maxi;
	}

	/**
	 * <p>Setter for the field <code>maxi</code>.</p>
	 *
	 * @param maxi a {@link java.lang.Double} object.
	 */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	/**
	 * <p>Getter for the field <code>required</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clRequired")
	public Boolean getRequired() {
		return required;
	}

	/**
	 * <p>Setter for the field <code>required</code>.</p>
	 *
	 * @param required a {@link java.lang.Boolean} object.
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}

	/**
	 * <p>Getter for the field <code>sampleId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clSampleId")
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
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clValue")
	public Double getValue() {
		return value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * <p>Getter for the field <code>textCriteria</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "qa:clTextCriteria")
	public MLText getTextCriteria() {
		return textCriteria;
	}

	/**
	 * <p>Setter for the field <code>textCriteria</code>.</p>
	 *
	 * @param textCriteria a {@link java.lang.String} object.
	 */
	public void setTextCriteria(MLText textCriteria) {
		this.textCriteria = textCriteria;
	}

	/**
	 * <p>Getter for the field <code>target</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clTarget")
	public Double getTarget() {
		return target;
	}

	/**
	 * <p>Setter for the field <code>target</code>.</p>
	 *
	 * @param target a {@link java.lang.Double} object.
	 */
	public void setTarget(Double target) {
		this.target = target;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clUnit")
	public String getUnit() {
		return unit;
	}

	/**
	 * <p>Setter for the field <code>unit</code>.</p>
	 *
	 * @param unit a {@link java.lang.String} object.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clState")
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
	 * <p>Getter for the field <code>temperature</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clTemperature")
	public String getTemperature() {
		return temperature;
	}

	/**
	 * <p>Setter for the field <code>temperature</code>.</p>
	 *
	 * @param temperature a {@link java.lang.String} object.
	 */
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	/**
	 * <p>Getter for the field <code>method</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc = true)
	@AlfQname(qname = "qa:clMethod")
	public NodeRef getMethod() {
		return method;
	}

	/**
	 * <p>Setter for the field <code>method</code>.</p>
	 *
	 * @param method a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setMethod(NodeRef method) {
		this.method = method;
	}

	/**
	 * <p>Getter for the field <code>characts</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc = true)
	@AlfQname(qname = "qa:clCharacts")
	public List<NodeRef> getCharacts() {
		return characts;
	}

	/**
	 * <p>Setter for the field <code>characts</code>.</p>
	 *
	 * @param characts a {@link java.util.List} object.
	 */
	public void setCharacts(List<NodeRef> characts) {
		this.characts = characts;
	}

	/**
	 * <p>Getter for the field <code>controlPoint</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "qa:clControlPoint")
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
	 * <p>Getter for the field <code>timePeriod</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "qa:clTimePeriod")
	public String getTimePeriod() {
		return timePeriod;
	}

	/**
	 * <p>Setter for the field <code>timePeriod</code>.</p>
	 *
	 * @param timePeriod a {@link java.lang.String} object.
	 */
	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}

	/**
	 * <p>Constructor for ControlListDataItem.</p>
	 */
	public ControlListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ControlListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public ControlListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	/**
	 * <p>Constructor for ControlListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param type a {@link java.lang.String} object.
	 * @param mini a {@link java.lang.Double} object.
	 * @param maxi a {@link java.lang.Double} object.
	 * @param required a {@link java.lang.Boolean} object.
	 * @param sampleId a {@link java.lang.String} object.
	 * @param value a {@link java.lang.Double} object.
	 * @param target a {@link java.lang.Double} object.
	 * @param unit a {@link java.lang.String} object.
	 * @param textCriteria a {@link java.lang.String} object.
	 * @param state a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 * @param temperature a {@link java.lang.String} object.
	 * @param method a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param characts a {@link java.util.List} object.
	 * @param controlPoint a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param timePeriod a {@link java.lang.String} object
	 */
	public ControlListDataItem(NodeRef nodeRef, String type, Double mini, Double maxi, Boolean required, String sampleId, Double value, Double target,
			String unit, MLText textCriteria, QualityControlState state, String temperature, String timePeriod, NodeRef method,
			List<NodeRef> characts, NodeRef controlPoint) {

		setNodeRef(nodeRef);
		setType(type);
		setMini(mini);
		setMaxi(maxi);
		setRequired(required);
		setSampleId(sampleId);
		setValue(value);
		setTarget(target);
		setUnit(unit);
		setTextCriteria(textCriteria);
		setState(state);
		setTemperature(temperature);
		setTimePeriod(timePeriod);
		setMethod(method);
		setCharacts(characts);
		setControlPoint(controlPoint);
	}

	/**
	 * <p>Constructor for ControlListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param sampleId a {@link java.lang.String} object.
	 * @param value a {@link java.lang.Double} object.
	 * @param target a {@link java.lang.Double} object.
	 * @param unit a {@link java.lang.String} object.
	 * @param state a {@link fr.becpg.repo.quality.data.QualityControlState} object.
	 * @param controlDefListDataItem a {@link fr.becpg.repo.quality.data.dataList.ControlDefListDataItem} object.
	 */
	public ControlListDataItem(NodeRef nodeRef, String sampleId, Double value, Double target, String unit, QualityControlState state,
			ControlDefListDataItem controlDefListDataItem) {

		setNodeRef(nodeRef);
		setSampleId(sampleId);
		setValue(value);
		setTarget(target);
		setUnit(unit);
		setState(state);

		setType(controlDefListDataItem.getType());
		setMini(controlDefListDataItem.getMini());
		setMaxi(controlDefListDataItem.getMaxi());
		setRequired(controlDefListDataItem.getRequired());
		setMethod(controlDefListDataItem.getMethod());
		setCharacts(controlDefListDataItem.getCharacts());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((characts == null) ? 0 : characts.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((required == null) ? 0 : required.hashCode());
		result = prime * result + ((sampleId == null) ? 0 : sampleId.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ControlListDataItem other = (ControlListDataItem) obj;
		if (characts == null) {
			if (other.characts != null)
				return false;
		} else if (!characts.equals(other.characts))
			return false;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (mini == null) {
			if (other.mini != null)
				return false;
		} else if (!mini.equals(other.mini))
			return false;
		if (required == null) {
			if (other.required != null)
				return false;
		} else if (!required.equals(other.required))
			return false;
		if (sampleId == null) {
			if (other.sampleId != null)
				return false;
		} else if (!sampleId.equals(other.sampleId))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ControlListDataItem [type=" + type + ", mini=" + mini + ", maxi=" + maxi + ", required=" + required + ", sampleId=" + sampleId
				+ ", value=" + value + ", target=" + target + ", unit=" + unit + ", state=" + state + ", method=" + method + ", characts=" + characts
				+ "]";
	}

}
