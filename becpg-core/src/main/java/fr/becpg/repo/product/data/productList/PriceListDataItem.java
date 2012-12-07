/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;

@AlfType
@AlfQname(qname = "bcpg:priceList")
public class PriceListDataItem extends AbstractEffectiveDataItem{
		
	private Double value = 0d;
	
	private String unit;
	
	private Double purchaseValue = 0d;
		
	private String purchaseUnit;
	
	private Integer prefRank = null;

	private NodeRef cost;
	
	private List<NodeRef> suppliers = new ArrayList<NodeRef>();
	
	@AlfProp
	@AlfQname(qname="bcpg:priceListValue")
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	@AlfProp
	@AlfQname(qname="bcpg:priceListUnit")
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@AlfProp
	@AlfQname(qname="bcpg:priceListPurchaseQty")
	public Double getPurchaseValue() {
		return purchaseValue;
	}

	public void setPurchaseValue(Double purchaseValue) {
		this.purchaseValue = purchaseValue;
	}

	@AlfProp
	@AlfQname(qname="bcpg:priceListPurchaseUnit")
	public String getPurchaseUnit() {
		return purchaseUnit;
	}

	public void setPurchaseUnit(String purchaseUnit) {
		this.purchaseUnit = purchaseUnit;
	}

	@AlfProp
	@AlfQname(qname="bcpg:priceListPrefRank")
	public Integer getPrefRank() {
		return prefRank;
	}

	public void setPrefRank(Integer prefRank) {
		this.prefRank = prefRank;
	}


	@AlfSingleAssoc
	@AlfQname(qname="bcpg:priceListCost")
	public NodeRef getCost() {
		return cost;
	}

	public void setCost(NodeRef cost) {
		this.cost = cost;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:suppliers")
	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}

	/**
	 * Instantiates a new cost list data item.
	 */
	public PriceListDataItem() {
		
	}
	
	/**
	 * Instantiates a new price list data item
	 * @param nodeRef
	 * @param value
	 * @param unit
	 * @param purchaseValue
	 * @param purchaseUnit
	 * @param prefRank
	 * @param startEffectivity
	 * @param endEffectivity
	 * @param cost
	 * @param suppliers
	 */
	public PriceListDataItem(NodeRef nodeRef, Double value, String unit, Double purchaseValue, String purchaseUnit, Integer prefRank, Date startEffectivity, Date endEffectivity, NodeRef cost, List<NodeRef> suppliers){
		
		setNodeRef(nodeRef);		
		setValue(value);
		setUnit(unit);
		setPurchaseValue(purchaseValue);
		setPurchaseUnit(purchaseUnit);
		setPrefRank(prefRank);
		setStartEffectivity(startEffectivity);
		setEndEffectivity(endEffectivity);		
		setCost(cost);
		setSuppliers(suppliers);
	}
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public PriceListDataItem(PriceListDataItem c){
		
		setNodeRef(c.getNodeRef());
		setValue(c.getValue());
		setUnit(c.getUnit());
		setPurchaseValue(c.getPurchaseValue());
		setPurchaseUnit(c.getPurchaseUnit());
		setPrefRank(c.getPrefRank());
		setStartEffectivity(c.getStartEffectivity());
		setEndEffectivity(c.getEndEffectivity());	
		setCost(c.getCost());
		setSuppliers(c.getSuppliers());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((prefRank == null) ? 0 : prefRank.hashCode());
		result = prime * result + ((purchaseUnit == null) ? 0 : purchaseUnit.hashCode());
		result = prime * result + ((purchaseValue == null) ? 0 : purchaseValue.hashCode());
		result = prime * result + ((suppliers == null) ? 0 : suppliers.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		PriceListDataItem other = (PriceListDataItem) obj;
		if (cost == null) {
			if (other.cost != null)
				return false;
		} else if (!cost.equals(other.cost))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (prefRank == null) {
			if (other.prefRank != null)
				return false;
		} else if (!prefRank.equals(other.prefRank))
			return false;
		if (purchaseUnit == null) {
			if (other.purchaseUnit != null)
				return false;
		} else if (!purchaseUnit.equals(other.purchaseUnit))
			return false;
		if (purchaseValue == null) {
			if (other.purchaseValue != null)
				return false;
		} else if (!purchaseValue.equals(other.purchaseValue))
			return false;
		if (suppliers == null) {
			if (other.suppliers != null)
				return false;
		} else if (!suppliers.equals(other.suppliers))
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
		return "PriceListDataItem [nodeRef=" + nodeRef + ", value=" + value + ", unit=" + unit + ", purchaseValue=" + purchaseValue + ", purchaseUnit=" + purchaseUnit
				+ ", prefRank=" + prefRank + ", cost=" + cost + ", suppliers=" + suppliers + "]";
	}
	
	
}

