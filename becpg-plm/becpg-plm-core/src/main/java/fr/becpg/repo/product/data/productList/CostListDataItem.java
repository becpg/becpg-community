/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.ForecastValueDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.Synchronisable;
import fr.becpg.repo.repository.model.UnitAwareDataItem;

@AlfType
@AlfQname(qname = "bcpg:costList")
public class CostListDataItem extends AbstractManualDataItem implements SimpleListDataItem,
MinMaxValueDataItem, UnitAwareDataItem, FormulatedCharactDataItem, ForecastValueDataItem, CompositeDataItem<CostListDataItem> , Synchronisable{
	
	
	private static final long serialVersionUID = 4160545876076772520L;
	private Double value = 0d;	
	private String unit;		
	private Double previousValue = 0d;
	private Double futureValue = 0d;
	private Double valuePerProduct = 0d;
	private Double maxi = null;	
	private NodeRef cost;	
	private Boolean isFormulated;	
	private String errorLog;	
	private List<NodeRef> plants = new ArrayList<>();
	private Integer depthLevel;
	private CostListDataItem parent;
	private NodeRef componentNodeRef;
	private Double simulatedValue;	
		
	@AlfProp
	@AlfQname(qname="bcpg:costListValue")
	public Double getValue() {
		return value;
	}
	
	
	public void setValue(Double value) {
		this.value = value;
	}
	
	@Override
	public Double getFormulatedValue() {
		return getValue();
	}
	
	
	@AlfProp
	@AlfQname(qname="bcpg:costListUnit")
	public String getUnit() {
		return unit;
	}
		
	public void setUnit(String unit) {
		this.unit = unit;
	}
		
	@AlfProp
	@AlfQname(qname="bcpg:costListPreviousValue")
	public Double getPreviousValue() {
		return previousValue;
	}

	public void setPreviousValue(Double previousValue) {
		this.previousValue = previousValue;
	}

	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValue")
	public Double getFutureValue() {
		return futureValue;
	}

	public void setFutureValue(Double futureValue) {
		this.futureValue = futureValue;
	}

	@AlfProp
	@AlfQname(qname="bcpg:costListValuePerProduct")
	public Double getValuePerProduct() {
		return valuePerProduct;
	}

	public void setValuePerProduct(Double valuePerProduct) {
		this.valuePerProduct = valuePerProduct;
	}

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:costListCost")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getCost() {
		return cost;
	}
	
	@Override
	@InternalField
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
	
	@InternalField
	public Double getMini() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMini(Double value) {
		// TODO Auto-generated method stub
		
	}
	
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:costListIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}


	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}

	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:costListFormulaErrorLog")
	@Override
	public String getErrorLog() {
		return errorLog;
	}

	@Override
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}

	@AlfMultiAssoc
	@InternalField
	@AlfQname(qname="bcpg:plants")
	public List<NodeRef> getPlants() {
		return plants;
	}

	public void setPlants(List<NodeRef> plants) {
		this.plants = plants;
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
	
	public CostListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual, List<NodeRef> plants, Double previousValue, Double futureValue){
		super();
		this.nodeRef = nodeRef;		
		this.value = value;
		this.unit =  unit;
		this.maxi = maxi;
		this.cost = cost;
		this.isManual = isManual;
		this.plants = plants;
		this.previousValue = previousValue;
		this.futureValue = futureValue;
	}
	
	
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public CostListDataItem(CostListDataItem c){
		super(c);	
		this.value =c.value;	
		this.unit = c.unit;		
		this.previousValue = c.previousValue;
		this.futureValue = c.futureValue;
		this.valuePerProduct = c.valuePerProduct;
		this.maxi = c.maxi;	
		this.cost = c.cost;	
		this.isFormulated = c.isFormulated;	
		this.errorLog = c.errorLog;	
		this.plants = c.plants;
		this.depthLevel = c.depthLevel;
		this.parent = c.parent;
		this.componentNodeRef = c.componentNodeRef;
		this.simulatedValue = c.simulatedValue;	
	}
	
	@Override
	public CostListDataItem clone() {
		return new CostListDataItem(this);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result + ((errorLog == null) ? 0 : errorLog.hashCode());
		result = prime * result + ((isFormulated == null) ? 0 : isFormulated.hashCode());
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
		if (errorLog == null) {
			if (other.errorLog != null)
				return false;
		} else if (!errorLog.equals(other.errorLog))
			return false;
		if (isFormulated == null) {
			if (other.isFormulated != null)
				return false;
		} else if (!isFormulated.equals(other.isFormulated))
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

	@Override
	public boolean isSynchronisable() {
		return plants.isEmpty();
	}

	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public CostListDataItem getParent() {
		return this.parent;
	}

	@Override
	public void setParent(CostListDataItem parent) {
		this.parent = parent;
	}

	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="bcpg:costListComponent")
	public NodeRef getComponentNodeRef() {
		return componentNodeRef;
	}

	public void setComponentNodeRef(NodeRef componentNodeRef) {
		this.componentNodeRef = componentNodeRef;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:costListSimulatedValue")
	public Double getSimulatedValue() {
		return simulatedValue;
	}

	public void setSimulatedValue(Double simulatedValue) {
		this.simulatedValue = simulatedValue;
	}


	
	
}

