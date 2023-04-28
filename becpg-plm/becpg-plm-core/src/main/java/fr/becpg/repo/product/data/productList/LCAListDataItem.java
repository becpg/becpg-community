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

@AlfType
@AlfQname(qname = "bcpg:lcaList")
public class LCAListDataItem extends VariantAwareDataItem implements SimpleListDataItem,
MinMaxValueDataItem, UnitAwareDataItem, FormulatedCharactDataItem, ForecastValueDataItem, CompositeDataItem<LCAListDataItem> , Synchronisable {
	
	
	private static final long serialVersionUID = 4160545876076772520L;
	protected Double value = 0d;	
	protected String unit;		
	protected Double previousValue = 0d;
	protected Double futureValue = 0d;
	protected Double valuePerProduct = 0d;
	protected Double maxi = null;	
	private NodeRef lca;	
	protected Boolean isFormulated;	
	protected String errorLog;	
	protected List<NodeRef> plants = new ArrayList<>();
	protected Integer depthLevel;
	private LCAListDataItem parent;
	protected NodeRef componentNodeRef;
	protected Double simulatedValue;	

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListValue")
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
	@AlfQname(qname="bcpg:lcaListUnit")
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
	@AlfQname(qname="bcpg:lcaListPreviousValue")
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
	@AlfQname(qname="bcpg:lcaListFutureValue")
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
	@AlfQname(qname="bcpg:lcaListValuePerProduct")
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
	

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:lcaListLca")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getLca() {
		return lca;
	}
	
	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return getLca();
	}
	
	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef nodeRef) {
		setLca(nodeRef);
		
	}
	
	
	public void setLca(NodeRef lca) {
		this.lca = lca;
	}
	

	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListMaxi")
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
	@AlfQname(qname="bcpg:lcaListIsFormulated")
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
	@AlfQname(qname="bcpg:lcaListFormulaErrorLog")
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
	
	public LCAListDataItem() {
		super();
	}
	
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef lca, Boolean isManual){
		super();
		this.nodeRef = nodeRef;		
		this.value = value;
		this.unit =  unit;
		this.maxi = maxi;
		this.lca = lca;
		this.isManual = isManual;
	}
	
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef lca, Boolean isManual, List<NodeRef> plants, Double previousValue, Double futureValue){
		super();
		this.nodeRef = nodeRef;		
		this.value = value;
		this.unit =  unit;
		this.maxi = maxi;
		this.lca = lca;
		this.isManual = isManual;
		this.plants = plants;
		this.previousValue = previousValue;
		this.futureValue = futureValue;
	}
	
	
	
	public LCAListDataItem(LCAListDataItem c){
		super(c);	
		this.value =c.value;	
		this.unit = c.unit;		
		this.previousValue = c.previousValue;
		this.futureValue = c.futureValue;
		this.valuePerProduct = c.valuePerProduct;
		this.maxi = c.maxi;	
		this.lca = c.lca;	
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
	public LCAListDataItem copy() {
		return new LCAListDataItem(this);
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(componentNodeRef, lca, depthLevel, errorLog, futureValue, isFormulated, maxi,
				parent, plants, previousValue, simulatedValue, unit, value, valuePerProduct);
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
		LCAListDataItem other = (LCAListDataItem) obj;
		return Objects.equals(componentNodeRef, other.componentNodeRef) && Objects.equals(lca, other.lca)
				&& Objects.equals(depthLevel, other.depthLevel) && Objects.equals(errorLog, other.errorLog)
				&& Objects.equals(futureValue, other.futureValue)
				&& Objects.equals(isFormulated, other.isFormulated) && Objects.equals(maxi, other.maxi) && Objects.equals(parent, other.parent)
				&& Objects.equals(plants, other.plants) && Objects.equals(previousValue, other.previousValue)
				&& Objects.equals(simulatedValue, other.simulatedValue)
				&& Objects.equals(unit, other.unit) && Objects.equals(value, other.value) && Objects.equals(valuePerProduct, other.valuePerProduct);
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LcaListDataItem [value=" + value + ", unit=" + unit + ", maxi=" + maxi + ", lca=" + lca + "]";
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
	public LCAListDataItem getParent() {
		return this.parent;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(LCAListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * <p>Getter for the field <code>componentNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="bcpg:lcaListComponent")
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
	@AlfQname(qname = "bcpg:lcaListSimulatedValue")
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

