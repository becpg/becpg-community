/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

@AlfType
@AlfQname(qname = "bcpg:costList")
public class CostListDataItem extends AbstractManualDataItem implements SimpleListDataItem{
	

	private Double value = 0d;
	
	private String unit;	
	
	private Double maxi = null;
	
	private NodeRef cost;
	
		
	@AlfProp
	@AlfQname(qname="bcpg:costListValue")
	public Double getValue() {
		return value;
	}
	
	
	public void setValue(Double value) {
		this.value = value;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:costListUnit")
	public String getUnit() {
		return unit;
	}
	
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:costListCost")
	public NodeRef getCost() {
		return cost;
	}
	
	@Override
	public NodeRef getCharactNodeRef() {
		return getCost();
	}
	
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setCost(nodeRef);
		
	}
	
	
	public void setCost(NodeRef cost) {
		this.cost = cost;
	}
	

	@AlfProp
	@AlfQname(qname="bcpg:costListMaxi")
	public Double getMaxi() {
		return maxi;
	}

	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	//////////////////////////////////////
	
	
	public Double getMini() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMini(Double value) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * Instantiates a new cost list data item.
	 */
	public CostListDataItem() {
		super();
	}
	
	/**
	 * Instantiates a new cost list data item.
	 *
	 * @param nodeRef the node ref
	 * @param value the value
	 * @param unit the unit
	 * @param cost the cost
	 */
	public CostListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual){
		super();
		this.nodeRef = nodeRef;		
		this.value = value;
		this.unit =  unit;
		this.maxi = maxi;
		this.cost = cost;
		this.isManual = isManual;
	}
	
	
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public CostListDataItem(CostListDataItem c){
		super();
		this.nodeRef = c.nodeRef;		
		this.value = c.value;
		this.unit =  c.unit;
		this.maxi = c.maxi;
		this.cost = c.cost;
		this.isManual = c.isManual;
		
	}
	
	@Deprecated
	public CostListDataItem(SimpleListDataItem c){
		super();
		this.nodeRef = c.getNodeRef();		
		this.value = c.getValue();
		this.maxi = c.getMaxi();
		this.cost = c.getCharactNodeRef();
		this.isManual = c.getIsManual();
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		CostListDataItem other = (CostListDataItem) obj;
		if (cost == null) {
			if (other.cost != null)
				return false;
		} else if (!cost.equals(other.cost))
			return false;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
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


	@Override
	public String toString() {
		return "CostListDataItem [value=" + value + ", unit=" + unit + ", maxi=" + maxi + ", cost=" + cost + "]";
	}

	
	
}

