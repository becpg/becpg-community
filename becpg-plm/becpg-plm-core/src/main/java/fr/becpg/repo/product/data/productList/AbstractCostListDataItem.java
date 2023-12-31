/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.ForecastValueDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.Synchronisable;
import fr.becpg.repo.repository.model.UnitAwareDataItem;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

public abstract class AbstractCostListDataItem<T extends AbstractCostListDataItem<T>> extends VariantAwareDataItem implements SimpleListDataItem,
MinMaxValueDataItem, UnitAwareDataItem, FormulatedCharactDataItem, ForecastValueDataItem, CompositeDataItem<T> , Synchronisable {
	
	
	private static final long serialVersionUID = 4160545876076772520L;
	protected Double value = 0d;	
	protected String unit;		
	protected Double previousValue = 0d;
	protected Double futureValue = 0d;
	protected Double valuePerProduct = 0d;
	protected Double maxi = null;	
	protected NodeRef cost;	
	protected Boolean isFormulated;	
	protected List<NodeRef> plants = new ArrayList<>();
	protected Integer depthLevel;
	protected T parent;
	protected NodeRef componentNodeRef;
	protected Double simulatedValue;	

	protected AbstractCostListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual){
		super();
		this.nodeRef = nodeRef;		
		this.value = value;
		this.unit =  unit;
		this.maxi = maxi;
		this.cost = cost;
		this.isManual = isManual;
	}
	
	protected AbstractCostListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual, List<NodeRef> plants, Double previousValue, Double futureValue){
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
	
	protected AbstractCostListDataItem(T c){
		super(c);	
		this.value =c.value;	
		this.unit = c.unit;		
		this.previousValue = c.previousValue;
		this.futureValue = c.futureValue;
		this.valuePerProduct = c.valuePerProduct;
		this.maxi = c.maxi;	
		this.cost = c.cost;	
		this.isFormulated = c.isFormulated;	
		this.plants = c.plants;
		this.depthLevel = c.depthLevel;
		this.parent = c.parent;
		this.componentNodeRef = c.componentNodeRef;
		this.simulatedValue = c.simulatedValue;	
	}
	
	public abstract NodeRef getComponentNodeRef();

	public abstract Double getSimulatedValue();
	
	public abstract NodeRef getCost();

	public abstract Double getValuePerProduct();

	public abstract T copy();
		
	public void setValue(Double value) {
		this.value = value;
	}
	
	@Override
	public Double getFormulatedValue() {
		return getValue();
	}
	
	@Override
	public void setFormulatedValue(Double formulatedValue) {
		setValue(formulatedValue);
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

	public void setUnit(String unit) {
		this.unit = unit;
	}
		
	public void setPreviousValue(Double previousValue) {
		this.previousValue = previousValue;
	}

	public void setFutureValue(Double futureValue) {
		this.futureValue = futureValue;
	}

	public void setValuePerProduct(Double valuePerProduct) {
		this.valuePerProduct = valuePerProduct;
	}

	public void setCost(NodeRef cost) {
		this.cost = cost;
	}
	
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	@InternalField
	public Double getMini() {
		return null;
	}

	public void setMini(Double value) {
		
	}
	
	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
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
	
	protected AbstractCostListDataItem() {
		super();
	}
	
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public T getParent() {
		return parent;
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
	public void setParent(T parent) {
		this.parent = parent;
	}

	public void setComponentNodeRef(NodeRef componentNodeRef) {
		this.componentNodeRef = componentNodeRef;
	}

	public void setSimulatedValue(Double simulatedValue) {
		this.simulatedValue = simulatedValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(componentNodeRef, cost, depthLevel, futureValue, isFormulated, maxi, parent, plants,
				previousValue, simulatedValue, unit, value, valuePerProduct);
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
		AbstractCostListDataItem<?> other = (AbstractCostListDataItem<?>) obj;
		return Objects.equals(componentNodeRef, other.componentNodeRef) && Objects.equals(cost, other.cost)
				&& Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(futureValue, other.futureValue) && Objects.equals(isFormulated, other.isFormulated)
				&& Objects.equals(maxi, other.maxi) && Objects.equals(parent, other.parent) && Objects.equals(plants, other.plants)
				&& Objects.equals(previousValue, other.previousValue) && Objects.equals(simulatedValue, other.simulatedValue)
				&& Objects.equals(unit, other.unit) && Objects.equals(value, other.value) && Objects.equals(valuePerProduct, other.valuePerProduct);
	}

}

