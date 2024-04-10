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
	
	@Override
	public void setForecastValue(String forecastColumn, Double value) {
		getForecastContext(forecastColumn).setValue(this, value);
	}
	
	@Override
	public Double getForecastValue(String forecastColumn) {
		return getForecastContext(forecastColumn).getValue(this);
	}
	
	@Override
	public String getForecastAccessor(String forecastColumn) {
		return getForecastContext(forecastColumn).getAccessor();
	}
	
	private String method;

	private Double previousValue = 0d;
	private Double futureValue = 0d;

	public LCAListDataItem() {
		super();
	}
	
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual){
		super(nodeRef, value, unit, maxi, cost, isManual);
	}
	
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual, List<NodeRef> plants, Double previousValue, Double futureValue){
		super(nodeRef, value, unit, maxi, cost, isManual, plants);
		this.previousValue = previousValue;
		this.futureValue = futureValue;
	}
	
	public LCAListDataItem(LCAListDataItem c){
		super(c);	
	}
	
	@Override
	public LCAListDataItem copy() {
		return new LCAListDataItem(this);
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:lcaListMethod")
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lcaListValue")
	public Double getValue() {
		return value;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lcaListUnit")
	public String getUnit() {
		return unit;
	}
		
	@AlfProp
	@AlfQname(qname="bcpg:lcaListPreviousValue")
	public Double getPreviousValue() {
		return previousValue;
	}
	
	public void setPreviousValue(Double previousValue) {
		this.previousValue = previousValue;
	}

	@AlfProp
	@AlfQname(qname="bcpg:lcaListFutureValue")
	public Double getFutureValue() {
		return futureValue;
	}
	
	public void setFutureValue(Double futureValue) {
		this.futureValue = futureValue;
	}

	@AlfProp
	@AlfQname(qname="bcpg:lcaListValuePerProduct")
	public Double getValuePerProduct() {
		return valuePerProduct;
	}

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:lcaListLca")
	@InternalField
	@DataListIdentifierAttr
	public NodeRef getLca() {
		return getCharactNodeRef();
	}
	
	public void setLca(NodeRef lca) {
		setCharactNodeRef(lca);
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:lcaListMaxi")
	public Double getMaxi() {
		return maxi;
	}
	
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:lcaListIsFormulated")
	public Boolean getIsFormulated() {
		return isFormulated;
	}
	
	@AlfSingleAssoc
	@InternalField
	@AlfQname(qname="bcpg:lcaListComponent")
	public NodeRef getComponentNodeRef() {
		return componentNodeRef;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:lcaListSimulatedValue")
	public Double getSimulatedValue() {
		return simulatedValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(futureValue, method, previousValue);
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
		LCAListDataItem other = (LCAListDataItem) obj;
		return Objects.equals(futureValue, other.futureValue) && Objects.equals(method, other.method)
				&& Objects.equals(previousValue, other.previousValue);
	}

	@Override
	public String toString() {
		return "LCAListDataItem [method=" + method + ", previousValue=" + previousValue + ", futureValue=" + futureValue + "]";
	}

}

