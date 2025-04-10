/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;

/**
 * <p>CostListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:costList")
public class CostListDataItem extends AbstractCostListDataItem<CostListDataItem> {
	
	private static final String FORECAST_COLUMN_UNKNOWN = "forecastColumn unknown: ";

	private static final long serialVersionUID = 4160545876076772520L;

	private Double previousValue = 0d;
	private Double futureValue = 0d;
	private Double futureValue2 = 0d;
	private Double futureValue3 = 0d;
	private Double futureValue4 = 0d;
	private Double previousValuePerProduct;
	private Double futureValuePerProduct;
	private Double futureValuePerProduct2;
	private Double futureValuePerProduct3;
	private Double futureValuePerProduct4;
		
	private static final List<ForecastContext<CostListDataItem>> FORECAST_CONTEXTS = List.of(
			new ForecastContext<>("bcpg:costListPreviousValue", "previousValue",
					CostListDataItem::setPreviousValue, CostListDataItem::getPreviousValue),
			new ForecastContext<>("bcpg:costListFutureValue", "futureValue",
					CostListDataItem::setFutureValue, CostListDataItem::getFutureValue),
			new ForecastContext<>("bcpg:costListFutureValue2", "futureValue2",
					CostListDataItem::setFutureValue2, CostListDataItem::getFutureValue2),
			new ForecastContext<>("bcpg:costListFutureValue3", "futureValue3",
					CostListDataItem::setFutureValue3, CostListDataItem::getFutureValue3),
			new ForecastContext<>("bcpg:costListFutureValue4", "futureValue4",
					CostListDataItem::setFutureValue4, CostListDataItem::getFutureValue4)
			);
	
	/** {@inheritDoc} */
	@Override
	public List<String> getForecastColumns() {
		return FORECAST_CONTEXTS.stream().map(c -> c.getForecastColumn()).toList();
	}
	
	private ForecastContext<CostListDataItem> getForecastContext(String forecastColumn) {
		for (ForecastContext<CostListDataItem> context : FORECAST_CONTEXTS) {
			if (context.getForecastColumn().equals(forecastColumn)) {
				return context;
			}
		}
		throw new IllegalStateException(FORECAST_COLUMN_UNKNOWN + forecastColumn);
	}
	
	/** {@inheritDoc} */
	@Override
	public void setForecastValue(String forecastColumn, Double value) {
		getForecastContext(forecastColumn).setValue(this, value);
	}
	
	/** {@inheritDoc} */
	@Override
	public Double getForecastValue(String forecastColumn) {
		return getForecastContext(forecastColumn).getValue(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public String getForecastAccessor(String forecastColumn) {
		return getForecastContext(forecastColumn).getAccessor();
	}
	
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
	
	/**
	 * <p>Setter for the field <code>previousValue</code>.</p>
	 *
	 * @param previousValue a {@link java.lang.Double} object
	 */
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
	
	/**
	 * <p>Setter for the field <code>futureValue</code>.</p>
	 *
	 * @param futureValue a {@link java.lang.Double} object
	 */
	public void setFutureValue(Double futureValue) {
		this.futureValue = futureValue;
	}
	
	/**
	 * <p>Getter for the field <code>futureValue2</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValue2")
	public Double getFutureValue2() {
		return futureValue2;
	}
	
	/**
	 * <p>Setter for the field <code>futureValue2</code>.</p>
	 *
	 * @param futureValue2 a {@link java.lang.Double} object
	 */
	public void setFutureValue2(Double futureValue2) {
		this.futureValue2 = futureValue2;
	}
	/**
	 * <p>Getter for the field <code>futureValue3</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValue3")
	public Double getFutureValue3() {
		return futureValue3;
	}
	
	/**
	 * <p>Setter for the field <code>futureValue3</code>.</p>
	 *
	 * @param futureValue3 a {@link java.lang.Double} object
	 */
	public void setFutureValue3(Double futureValue3) {
		this.futureValue3 = futureValue3;
	}
	/**
	 * <p>Getter for the field <code>futureValue4</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValue4")
	public Double getFutureValue4() {
		return futureValue4;
	}
	
	/**
	 * <p>Setter for the field <code>futureValue4</code>.</p>
	 *
	 * @param futureValue4 a {@link java.lang.Double} object
	 */
	public void setFutureValue4(Double futureValue4) {
		this.futureValue4 = futureValue4;
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
	 * <p>Setter for the field <code>futureValuePerProduct</code>.</p>
	 *
	 * @param futureValuePerProduct a {@link java.lang.Double} object
	 */
	public void setFutureValuePerProduct(Double futureValuePerProduct) {
		this.futureValuePerProduct = futureValuePerProduct;
	}
	
	/**
	 * <p>Setter for the field <code>futureValuePerProduct2</code>.</p>
	 *
	 * @param futureValuePerProduct2 a {@link java.lang.Double} object
	 */
	public void setFutureValuePerProduct2(Double futureValuePerProduct2) {
		this.futureValuePerProduct2 = futureValuePerProduct2;
	}
	
	/**
	 * <p>Setter for the field <code>futureValuePerProduct3</code>.</p>
	 *
	 * @param futureValuePerProduct3 a {@link java.lang.Double} object
	 */
	public void setFutureValuePerProduct3(Double futureValuePerProduct3) {
		this.futureValuePerProduct3 = futureValuePerProduct3;
	}
	
	/**
	 * <p>Setter for the field <code>futureValuePerProduct4</code>.</p>
	 *
	 * @param futureValuePerProduct4 a {@link java.lang.Double} object
	 */
	public void setFutureValuePerProduct4(Double futureValuePerProduct4) {
		this.futureValuePerProduct4 = futureValuePerProduct4;
	}
	
	/**
	 * <p>Setter for the field <code>previousValuePerProduct</code>.</p>
	 *
	 * @param previousValuePerProduct a {@link java.lang.Double} object
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
	 * <p>Getter for the field <code>futureValuePerProduct2</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValuePerProduct2")
	public Double getFutureValuePerProduct2() {
		return futureValuePerProduct2;
	}
	
	/**
	 * <p>Getter for the field <code>futureValuePerProduct3</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValuePerProduct3")
	public Double getFutureValuePerProduct3() {
		return futureValuePerProduct3;
	}
	
	/**
	 * <p>Getter for the field <code>futureValuePerProduct4</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:costListFutureValuePerProduct4")
	public Double getFutureValuePerProduct4() {
		return futureValuePerProduct4;
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
		return getCharactNodeRef();
	}
	
	/**
	 * <p>setCost.</p>
	 *
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setCost(NodeRef cost) {
		setCharactNodeRef(cost);
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
		super(nodeRef, value, unit, maxi, cost, isManual);
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
		super(nodeRef, value, unit, maxi, cost, isManual, plants);
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
		this.previousValue = c.previousValue;
		this.futureValue = c.futureValue;
		this.previousValuePerProduct = c.previousValuePerProduct;
		this.futureValuePerProduct = c.futureValuePerProduct;
		this.futureValuePerProduct2 = c.futureValuePerProduct2;
		this.futureValuePerProduct3 = c.futureValuePerProduct3;
		this.futureValuePerProduct4 = c.futureValuePerProduct4;
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
		result = prime * result + Objects.hash(futureValue, futureValue2, futureValue3, futureValue4,
				futureValuePerProduct, futureValuePerProduct2, futureValuePerProduct3, futureValuePerProduct4,
				previousValue, previousValuePerProduct);
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
		return Objects.equals(futureValue, other.futureValue) && Objects.equals(futureValue2, other.futureValue2)
				&& Objects.equals(futureValue3, other.futureValue3) && Objects.equals(futureValue4, other.futureValue4)
				&& Objects.equals(futureValuePerProduct, other.futureValuePerProduct)
				&& Objects.equals(futureValuePerProduct2, other.futureValuePerProduct2)
				&& Objects.equals(futureValuePerProduct3, other.futureValuePerProduct3)
				&& Objects.equals(futureValuePerProduct4, other.futureValuePerProduct4)
				&& Objects.equals(previousValue, other.previousValue)
				&& Objects.equals(previousValuePerProduct, other.previousValuePerProduct);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CostListDataItem [previousValue=" + previousValue + ", futureValue=" + futureValue + ", futureValue2="
				+ futureValue2 + ", futureValue3=" + futureValue3 + ", futureValue4=" + futureValue4
				+ ", previousValuePerProduct=" + previousValuePerProduct + ", futureValuePerProduct="
				+ futureValuePerProduct + ", futureValuePerProduct2=" + futureValuePerProduct2
				+ ", futureValuePerProduct3=" + futureValuePerProduct3 + ", futureValuePerProduct4="
				+ futureValuePerProduct4 + "]";
	}

}

