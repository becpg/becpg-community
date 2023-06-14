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
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.ForecastValueDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.Synchronisable;
import fr.becpg.repo.repository.model.UnitAwareDataItem;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

/**
 * <p>CostListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:costList")
public class CostListDataItem extends VariantAwareDataItem implements SimpleListDataItem,
MinMaxValueDataItem, UnitAwareDataItem, FormulatedCharactDataItem, ForecastValueDataItem, CompositeDataItem<CostListDataItem> , Synchronisable {
	
	
	private static final long serialVersionUID = 4160545876076772520L;
	private Double value = 0d;	
	private String unit;		
	private Double previousValue = 0d;
	private Double futureValue = 0d;
	private Double valuePerProduct = 0d;
	private Double previousValuePerProduct = 0d;
	private Double futureValuePerProduct = 0d;
	private Double maxi = null;	
	private NodeRef cost;	
	private Boolean isFormulated;	
	private String errorLog;	
	private List<NodeRef> plants = new ArrayList<>();
	private Integer depthLevel;
	private CostListDataItem parent;
	private NodeRef componentNodeRef;
	private Double simulatedValue;	

		
	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListValue")
	public Double getValue() {
		return value;
	}
	
	
	/** {@inheritDoc} */
	public void setValue(Double value) {
		this.value = value;
	}
	
	/** {@inheritDoc} */
	@Override
	public Double getFormulatedValue() {
		return getValue();
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void setFormulatedValue(Double formulatedValue) {
		setValue(formulatedValue);
	}

	
	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListUnit")
	public String getUnit() {
		return unit;
	}
		
	/** {@inheritDoc} */
	public void setUnit(String unit) {
		this.unit = unit;
	}
		
	/**
	 * <p>Getter for the field <code>previousValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListPreviousValue")
	public Double getPreviousValue() {
		return previousValue;
	}

	/** {@inheritDoc} */
	public void setPreviousValue(Double previousValue) {
		this.previousValue = previousValue;
	}

	/**
	 * <p>Getter for the field <code>futureValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValue")
	public Double getFutureValue() {
		return futureValue;
	}

	/** {@inheritDoc} */
	public void setFutureValue(Double futureValue) {
		this.futureValue = futureValue;
	}

	/**
	 * <p>Getter for the field <code>valuePerProduct</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListValuePerProduct")
	public Double getValuePerProduct() {
		return valuePerProduct;
	}

	/**
	 * <p>Setter for the field <code>valuePerProduct</code>.</p>
	 *
	 * @param valuePerProduct a {@link java.lang.Double} object.
	 */
	public void setValuePerProduct(Double valuePerProduct) {
		this.valuePerProduct = valuePerProduct;
	}
	
	
	/**
	 * <p>Getter for the field <code>previousValuePerProduct</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListPreviousValuePerProduct")
	public Double getPreviousValuePerProduct() {
		return previousValuePerProduct;
	}


	/**
	 * <p>Setter for the field <code>previousValuePerProduct</code>.</p>
	 *
	 * @param previousValuePerProduct a {@link java.lang.Double} object.
	 */
	public void setPreviousValuePerProduct(Double previousValuePerProduct) {
		this.previousValuePerProduct = previousValuePerProduct;
	}

	/**
	 * <p>Getter for the field <code>futureValuePerProduct</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValuePerProduct")
	public Double getFutureValuePerProduct() {
		return futureValuePerProduct;
	}


	/**
	 * <p>Setter for the field <code>futureValuePerProduct</code>.</p>
	 *
	 * @param futureValuePerProduct a {@link java.lang.Double} object.
	 */
	public void setFutureValuePerProduct(Double futureValuePerProduct) {
		this.futureValuePerProduct = futureValuePerProduct;
	}
	
	
	

	/**
	 * <p>Getter for the field <code>cost</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:costListCost")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getCost() {
		return cost;
	}
	
	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getCost();
	}
	
	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setCost(nodeRef);
		
	}
	
	
	/**
	 * <p>Setter for the field <code>cost</code>.</p>
	 *
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setCost(NodeRef cost) {
		this.cost = cost;
	}
	

	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListMaxi")
	public Double getMaxi() {
		return maxi;
	}

	/** {@inheritDoc} */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	//////////////////////////////////////
	
	/**
	 * <p>getMini.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@InternalField
	public Double getMini() {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public void setMini(Double value) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * <p>Getter for the field <code>isFormulated</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:costListIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}


	/** {@inheritDoc} */
	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}

	/** {@inheritDoc} */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:costListFormulaErrorLog")
	@Override
	public String getErrorLog() {
		return errorLog;
	}

	/** {@inheritDoc} */
	@Override
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}

	/**
	 * <p>Getter for the field <code>plants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@InternalField
	@AlfQname(qname="bcpg:plants")
	public List<NodeRef> getPlants() {
		return plants;
	}

	/**
	 * <p>Setter for the field <code>plants</code>.</p>
	 *
	 * @param plants a {@link java.util.List} object.
	 */
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
	 * @param maxi a {@link java.lang.Double} object.
	 * @param isManual a {@link java.lang.Boolean} object.
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
	 * <p>Constructor for CostListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param value a {@link java.lang.Double} object.
	 * @param unit a {@link java.lang.String} object.
	 * @param maxi a {@link java.lang.Double} object.
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param isManual a {@link java.lang.Boolean} object.
	 * @param plants a {@link java.util.List} object.
	 * @param previousValue a {@link java.lang.Double} object.
	 * @param futureValue a {@link java.lang.Double} object.
	 */
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
	 *
	 * @param c a {@link fr.becpg.repo.product.data.productList.CostListDataItem} object.
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
	
	/** {@inheritDoc} */
	@Override
	public CostListDataItem copy() {
		CostListDataItem ret =  new CostListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(componentNodeRef, cost, depthLevel, errorLog, futureValue, futureValuePerProduct, isFormulated, maxi,
				parent, plants, previousValue, previousValuePerProduct, simulatedValue, unit, value, valuePerProduct);
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
		CostListDataItem other = (CostListDataItem) obj;
		return Objects.equals(componentNodeRef, other.componentNodeRef) && Objects.equals(cost, other.cost)
				&& Objects.equals(depthLevel, other.depthLevel) && Objects.equals(errorLog, other.errorLog)
				&& Objects.equals(futureValue, other.futureValue) && Objects.equals(futureValuePerProduct, other.futureValuePerProduct)
				&& Objects.equals(isFormulated, other.isFormulated) && Objects.equals(maxi, other.maxi) && Objects.equals(parent, other.parent)
				&& Objects.equals(plants, other.plants) && Objects.equals(previousValue, other.previousValue)
				&& Objects.equals(previousValuePerProduct, other.previousValuePerProduct) && Objects.equals(simulatedValue, other.simulatedValue)
				&& Objects.equals(unit, other.unit) && Objects.equals(value, other.value) && Objects.equals(valuePerProduct, other.valuePerProduct);
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CostListDataItem [value=" + value + ", unit=" + unit + ", maxi=" + maxi + ", cost=" + cost + "]";
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSynchronisable() {
		return plants.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public CostListDataItem getParent() {
		return this.parent;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(CostListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * <p>Getter for the field <code>componentNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="bcpg:costListComponent")
	public NodeRef getComponentNodeRef() {
		return componentNodeRef;
	}

	/**
	 * <p>Setter for the field <code>componentNodeRef</code>.</p>
	 *
	 * @param componentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setComponentNodeRef(NodeRef componentNodeRef) {
		this.componentNodeRef = componentNodeRef;
	}

	/**
	 * <p>Getter for the field <code>simulatedValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:costListSimulatedValue")
	public Double getSimulatedValue() {
		return simulatedValue;
	}

	/**
	 * <p>Setter for the field <code>simulatedValue</code>.</p>
	 *
	 * @param simulatedValue a {@link java.lang.Double} object.
	 */
	public void setSimulatedValue(Double simulatedValue) {
		this.simulatedValue = simulatedValue;
	}


	
	
}

