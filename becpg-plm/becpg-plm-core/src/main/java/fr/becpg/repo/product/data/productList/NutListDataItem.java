/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

@AlfType
@AlfQname(qname = "bcpg:nutList")
public class NutListDataItem extends AbstractManualDataItem  implements SimpleListDataItem {

	
	
	private Double value;
	
	private String unit;
	
	private Double mini;
	
	private Double maxi;
	
	private Double valuePerServing;
	
	private Double gdaPerc;

	private String group;
	
	private String method;
	
	private NodeRef nut;
	
			
		
	@AlfProp
	@AlfQname(qname="bcpg:nutListValue")
	public Double getValue() {
		return value;
	}
	
	
	public void setValue(Double value) {
		this.value = value;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:nutListUnit")
	public String getUnit() {
		return unit;
	}
	
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:nutListMini")
	public Double getMini() {
		return mini;
	}

	public void setMini(Double mini) {
		this.mini = mini;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListMaxi")
	public Double getMaxi() {
		return maxi;
	}

	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListValuePerServing")
	public Double getValuePerServing() {
		return valuePerServing;
	}


	public void setValuePerServing(Double valuePerServing) {
		this.valuePerServing = valuePerServing;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nutListGDAPerc")
	public Double getGdaPerc() {
		return gdaPerc;
	}


	public void setGdaPerc(Double gdaPerc) {
		this.gdaPerc = gdaPerc;
	}


	@AlfProp
	@AlfQname(qname="bcpg:nutListGroup")
	public String getGroup() {
		return group;
	}
	
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:nutListMethod")
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:nutListNut")
	@DataListIdentifierAttr
	public NodeRef getNut() {
		return nut;
	}

	@Override
	public NodeRef getCharactNodeRef() {
		return getNut();
	}
	
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setNut(nodeRef);		
	}
	
	/**
	 * Sets the nut.
	 *
	 * @param nut the new nut
	 */
	public void setNut(NodeRef nut) {
		this.nut = nut;
	}	

	
	/**
	 * Instantiates a new nut list data item.
	 */
	public NutListDataItem()
	{
	}
	
	/**
	 * Instantiates a new nut list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param group the group
	 * @param nut the nut
	 */
	public NutListDataItem(NodeRef nodeRef,	Double value, String unit, Double mini, Double maxi, String group, NodeRef nut, Boolean isManual)
	{
		setNodeRef(nodeRef);
		setValue(value);
		setUnit(unit);
		setMini(mini);
		setMaxi(maxi);
		setGroup(group);
		setNut(nut);
		setIsManual(isManual);
	}
	
	/**
	 * Copy constructor
	 * @param n
	 */
	public NutListDataItem(NutListDataItem n){

		setNodeRef(n.getNodeRef());
		setValue(n.getValue());
		setUnit(n.getUnit());
		setMini(n.getMini());
		setMaxi(n.getMaxi());
		setGroup(n.getGroup());
		setNut(n.getNut());
		setIsManual(n.getIsManual());
    }
	
	/**
	 * Copy constructor
	 * @param n
	 */
	public NutListDataItem(SimpleListDataItem n){

		setValue(n.getValue());
		setMini(n.getMini());
		setMaxi(n.getMaxi());
		setIsManual(n.getIsManual());
		setNut(n.getCharactNodeRef());
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((gdaPerc == null) ? 0 : gdaPerc.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((nut == null) ? 0 : nut.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((valuePerServing == null) ? 0 : valuePerServing.hashCode());
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
		NutListDataItem other = (NutListDataItem) obj;
		if (gdaPerc == null) {
			if (other.gdaPerc != null)
				return false;
		} else if (!gdaPerc.equals(other.gdaPerc))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
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
		if (nut == null) {
			if (other.nut != null)
				return false;
		} else if (!nut.equals(other.nut))
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
		if (valuePerServing == null) {
			if (other.valuePerServing != null)
				return false;
		} else if (!valuePerServing.equals(other.valuePerServing))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NutListDataItem [value=" + value + ", unit=" + unit + ", mini=" + mini + ", maxi=" + maxi
				+ ", valuePerServing=" + valuePerServing + ", gdaPerc=" + gdaPerc + ", group=" + group + ", method="
				+ method + ", nut=" + nut + "]";
	}	
}
