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
 * <p>LCAListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:lcaList")
public class LCAListDataItem extends AbstractCostListDataItem<LCAListDataItem> {
	
	
	private static final long serialVersionUID = 4160545876076772520L;

	private static final String FORECAST_COLUMN_UNKNOWN = "forecastColumn unknown: ";

	private static final List<ForecastContext<LCAListDataItem>> FORECAST_CONTEXTS = List.of(
			new ForecastContext<>("bcpg:lcaListPreviousValue", "previousValue",
					LCAListDataItem::setPreviousValue, LCAListDataItem::getPreviousValue),
			new ForecastContext<>("bcpg:lcaListFutureValue", "futureValue",
					LCAListDataItem::setFutureValue, LCAListDataItem::getFutureValue)
			);
	
	/** {@inheritDoc} */
	@Override
	public List<String> getForecastColumns() {
		return FORECAST_CONTEXTS.stream().map(c -> c.getForecastColumn()).toList();
	}
	
	private ForecastContext<LCAListDataItem> getForecastContext(String forecastColumn) {
		for (ForecastContext<LCAListDataItem> context : FORECAST_CONTEXTS) {
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
	
	private String method;

	private Double previousValue = 0d;
	private Double futureValue = 0d;

	/**
	 * <p>Constructor for LCAListDataItem.</p>
	 */
	public LCAListDataItem() {
		super();
	}
	
	/**
	 * <p>Constructor for LCAListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param value a {@link java.lang.Double} object
	 * @param unit a {@link java.lang.String} object
	 * @param maxi a {@link java.lang.Double} object
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param isManual a {@link java.lang.Boolean} object
	 */
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual){
		super(nodeRef, value, unit, maxi, cost, isManual);
	}
	
	/**
	 * <p>Constructor for LCAListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param value a {@link java.lang.Double} object
	 * @param unit a {@link java.lang.String} object
	 * @param maxi a {@link java.lang.Double} object
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param isManual a {@link java.lang.Boolean} object
	 * @param plants a {@link java.util.List} object
	 * @param previousValue a {@link java.lang.Double} object
	 * @param futureValue a {@link java.lang.Double} object
	 */
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual, List<NodeRef> plants, Double previousValue, Double futureValue){
		super(nodeRef, value, unit, maxi, cost, isManual, plants);
		this.previousValue = previousValue;
		this.futureValue = futureValue;
	}
	
	/**
	 * <p>Constructor for LCAListDataItem.</p>
	 *
	 * @param c a {@link fr.becpg.repo.product.data.productList.LCAListDataItem} object
	 */
	public LCAListDataItem(LCAListDataItem c){
		super(c);	
	}
	
	/** {@inheritDoc} */
	@Override
	public LCAListDataItem copy() {
		return new LCAListDataItem(this);
	}
	
	/**
	 * <p>Getter for the field <code>method</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:lcaListMethod")
	public String getMethod() {
		return method;
	}
	
	/**
	 * <p>Setter for the field <code>method</code>.</p>
	 *
	 * @param method a {@link java.lang.String} object
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	
	/**
	 * <p>getValue.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListValue")
	public Double getValue() {
		return value;
	}
	
	/**
	 * <p>getUnit.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListUnit")
	public String getUnit() {
		return unit;
	}
		
	/**
	 * <p>Getter for the field <code>previousValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListPreviousValue")
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
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListFutureValue")
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
	 * <p>getValuePerProduct.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListValuePerProduct")
	public Double getValuePerProduct() {
		return valuePerProduct;
	}

	/**
	 * <p>getLca.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:lcaListLca")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getLca() {
		return getCharactNodeRef();
	}
	
	/**
	 * <p>setLca.</p>
	 *
	 * @param lca a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setLca(NodeRef lca) {
		setCharactNodeRef(lca);
	}
	
	/**
	 * <p>getMaxi.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lcaListMaxi")
	public Double getMaxi() {
		return maxi;
	}
	
	/**
	 * <p>getIsFormulated.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:lcaListIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}
	
	/**
	 * <p>getComponentNodeRef.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="bcpg:lcaListComponent")
	public NodeRef getComponentNodeRef() {
		return componentNodeRef;
	}

	/**
	 * <p>getSimulatedValue.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:lcaListSimulatedValue")
	public Double getSimulatedValue() {
		return simulatedValue;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(futureValue, method, previousValue);
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
		return Objects.equals(futureValue, other.futureValue) && Objects.equals(method, other.method)
				&& Objects.equals(previousValue, other.previousValue);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LCAListDataItem [method=" + method + ", previousValue=" + previousValue + ", futureValue=" + futureValue + "]";
	}

}

