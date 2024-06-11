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

/**
 * <p>Abstract AbstractCostListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractCostListDataItem<T extends AbstractCostListDataItem<T>> extends VariantAwareDataItem implements SimpleListDataItem,
MinMaxValueDataItem, UnitAwareDataItem, FormulatedCharactDataItem, ForecastValueDataItem, CompositeDataItem<T> , Synchronisable {
	
	
	private static final long serialVersionUID = 4160545876076772520L;
	protected Double value = 0d;	
	protected String unit;
	protected Double valuePerProduct = 0d;
	protected Double maxi = null;	
	protected NodeRef charact;	
	protected Boolean isFormulated;	
	protected List<NodeRef> plants = new ArrayList<>();
	protected Integer depthLevel;
	protected T parent;
	protected NodeRef componentNodeRef;
	protected Double simulatedValue;	

	/**
	 * <p>Constructor for AbstractCostListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param value a {@link java.lang.Double} object
	 * @param unit a {@link java.lang.String} object
	 * @param maxi a {@link java.lang.Double} object
	 * @param charact a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param isManual a {@link java.lang.Boolean} object
	 */
	protected AbstractCostListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef charact, Boolean isManual){
		super();
		this.nodeRef = nodeRef;		
		this.value = value;
		this.unit =  unit;
		this.maxi = maxi;
		this.charact = charact;
		this.isManual = isManual;
	}
	
	/**
	 * <p>Constructor for AbstractCostListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param value a {@link java.lang.Double} object
	 * @param unit a {@link java.lang.String} object
	 * @param maxi a {@link java.lang.Double} object
	 * @param charact a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param isManual a {@link java.lang.Boolean} object
	 * @param plants a {@link java.util.List} object
	 */
	protected AbstractCostListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef charact, Boolean isManual, List<NodeRef> plants){
		super();
		this.nodeRef = nodeRef;		
		this.value = value;
		this.unit =  unit;
		this.maxi = maxi;
		this.charact = charact;
		this.isManual = isManual;
		this.plants = plants;
	}
	
	/**
	 * <p>Constructor for AbstractCostListDataItem.</p>
	 *
	 * @param c a T object
	 */
	protected AbstractCostListDataItem(T c){
		super(c);	
		this.value =c.value;	
		this.unit = c.unit;		
		this.valuePerProduct = c.valuePerProduct;
		this.maxi = c.maxi;	
		this.charact = c.charact;	
		this.isFormulated = c.isFormulated;	
		this.plants = c.plants;
		this.depthLevel = c.depthLevel;
		this.parent = c.parent;
		this.componentNodeRef = c.componentNodeRef;
		this.simulatedValue = c.simulatedValue;	
	}
	
	/**
	 * <p>Getter for the field <code>componentNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public abstract NodeRef getComponentNodeRef();

	/**
	 * <p>Getter for the field <code>simulatedValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public abstract Double getSimulatedValue();
	
	/**
	 * <p>Getter for the field <code>valuePerProduct</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public abstract Double getValuePerProduct();

	/**
	 * <p>copy.</p>
	 *
	 * @return a T object
	 */
	public abstract T copy();
		
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
	
	/** {@inheritDoc} */
	@Override
	@InternalField
	public NodeRef getCharactNodeRef() {
		return charact;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef charact) {
		this.charact = charact;
	}

	/** {@inheritDoc} */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * <p>Setter for the field <code>valuePerProduct</code>.</p>
	 *
	 * @param valuePerProduct a {@link java.lang.Double} object
	 */
	public void setValuePerProduct(Double valuePerProduct) {
		this.valuePerProduct = valuePerProduct;
	}

	/** {@inheritDoc} */
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}
	
	/**
	 * <p>getMini.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@InternalField
	public Double getMini() {
		return null;
	}

	/** {@inheritDoc} */
	public void setMini(Double value) {
		
	}
	
	/** {@inheritDoc} */
	public void setIsFormulated(Boolean isFormulated) {
		this.isFormulated = isFormulated;
	}

	/**
	 * <p>Getter for the field <code>plants</code>.</p>
	 *
	 * @return a {@link java.util.List} object
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
	 * @param plants a {@link java.util.List} object
	 */
	public void setPlants(List<NodeRef> plants) {
		this.plants = plants;
	}
	
	/**
	 * <p>Constructor for AbstractCostListDataItem.</p>
	 */
	protected AbstractCostListDataItem() {
		super();
	}
	
	/** {@inheritDoc} */
	@Override
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:parentLevel")
	public T getParent() {
		return parent;
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
	 * @param depthLevel a {@link java.lang.Integer} object
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(T parent) {
		this.parent = parent;
	}

	/**
	 * <p>Setter for the field <code>componentNodeRef</code>.</p>
	 *
	 * @param componentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setComponentNodeRef(NodeRef componentNodeRef) {
		this.componentNodeRef = componentNodeRef;
	}

	/**
	 * <p>Setter for the field <code>simulatedValue</code>.</p>
	 *
	 * @param simulatedValue a {@link java.lang.Double} object
	 */
	public void setSimulatedValue(Double simulatedValue) {
		this.simulatedValue = simulatedValue;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(componentNodeRef, charact, depthLevel, isFormulated, maxi, parent, plants,
				simulatedValue, unit, value, valuePerProduct);
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
		AbstractCostListDataItem<?> other = (AbstractCostListDataItem<?>) obj;
		return Objects.equals(componentNodeRef, other.componentNodeRef) && Objects.equals(charact, other.charact)
				&& Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(isFormulated, other.isFormulated)
				&& Objects.equals(maxi, other.maxi) && Objects.equals(parent, other.parent) && Objects.equals(plants, other.plants)
				&& Objects.equals(simulatedValue, other.simulatedValue)
				&& Objects.equals(unit, other.unit) && Objects.equals(value, other.value) && Objects.equals(valuePerProduct, other.valuePerProduct);
	}

}

