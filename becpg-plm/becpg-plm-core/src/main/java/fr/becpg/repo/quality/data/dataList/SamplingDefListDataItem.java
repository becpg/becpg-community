package fr.becpg.repo.quality.data.dataList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "qa:samplingDefList")
public class SamplingDefListDataItem extends BeCPGDataObject {

	private Integer qty;
	private Integer freq;
	private String freqUnit;
	private NodeRef controlPoint;
	private NodeRef controlStep;
	private NodeRef controlingGroup;

	@AlfProp
	@AlfQname(qname = "qa:sdlQty")
	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	@AlfProp
	@AlfQname(qname = "qa:sdlFreq")
	public Integer getFreq() {
		return freq;
	}

	public void setFreq(Integer freq) {
		this.freq = freq;
	}

	@AlfProp
	@AlfQname(qname = "qa:sdlFreqUnit")
	public String getFreqUnit() {
		return freqUnit;
	}

	public void setFreqUnit(String freqUnit) {
		this.freqUnit = freqUnit;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "qa:sdlControlPoint")
	public NodeRef getControlPoint() {
		return controlPoint;
	}

	public void setControlPoint(NodeRef controlPoint) {
		this.controlPoint = controlPoint;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "qa:sdlControlStep")
	public NodeRef getControlStep() {
		return controlStep;
	}

	public void setControlStep(NodeRef controlStep) {
		this.controlStep = controlStep;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "qa:sdlControlingGroup")
	public NodeRef getControlingGroup() {
		return controlingGroup;
	}

	public void setControlingGroup(NodeRef controlingGroup) {
		this.controlingGroup = controlingGroup;
	}

	public SamplingDefListDataItem() {
		super();
	}

	public SamplingDefListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	public SamplingDefListDataItem(Integer qty, Integer freq, String freqUnit, NodeRef controlPoint, NodeRef controlStep, NodeRef controlingGroup) {
		super();
		this.qty = qty;
		this.freq = freq;
		this.freqUnit = freqUnit;
		this.controlPoint = controlPoint;
		this.controlStep = controlStep;
		this.controlingGroup = controlingGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((controlPoint == null) ? 0 : controlPoint.hashCode());
		result = prime * result + ((controlStep == null) ? 0 : controlStep.hashCode());
		result = prime * result + ((controlingGroup == null) ? 0 : controlingGroup.hashCode());
		result = prime * result + ((freq == null) ? 0 : freq.hashCode());
		result = prime * result + ((freqUnit == null) ? 0 : freqUnit.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
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
		SamplingDefListDataItem other = (SamplingDefListDataItem) obj;
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
		if (freq == null) {
			if (other.freq != null)
				return false;
		} else if (!freq.equals(other.freq))
			return false;
		if (freqUnit == null) {
			if (other.freqUnit != null)
				return false;
		} else if (!freqUnit.equals(other.freqUnit))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SamplingDefListDataItem [qty=" + qty + ", freq=" + freq + ", freqUnit=" + freqUnit + ", controlPoint=" + controlPoint + ", controlStep=" + controlStep
				+ ", controlingGroup=" + controlingGroup + "]";
	}

}
