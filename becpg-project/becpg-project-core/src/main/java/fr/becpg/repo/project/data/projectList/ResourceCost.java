package fr.becpg.repo.project.data.projectList;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Cost of a resource
 * @author quere
 *
 */
@AlfType
@AlfQname(qname = "pjt:resourceCost")
public class ResourceCost extends BeCPGDataObject {

	private Double value;
	private Double hoursPerDay;

	@AlfProp
	@AlfQname(qname = "pjt:resourceCostValue")
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	@AlfProp
	@AlfQname(qname = "pjt:resourceCostHoursPerDay")
	public Double getHoursPerDay() {
		return hoursPerDay;
	}

	public void setHoursPerDay(Double hoursPerDay) {
		this.hoursPerDay = hoursPerDay;
	}

	@Override
	public String toString() {
		return "ResourceCost [value=" + value + ", hoursPerDay=" + hoursPerDay + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((hoursPerDay == null) ? 0 : hoursPerDay.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ResourceCost other = (ResourceCost) obj;
		if (hoursPerDay == null) {
			if (other.hoursPerDay != null)
				return false;
		} else if (!hoursPerDay.equals(other.hoursPerDay))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
