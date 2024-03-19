/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.List;

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

	private String method;

	public LCAListDataItem() {
		super();
	}
	
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual){
		super(nodeRef, value, unit, maxi, cost, isManual);
	}
	
	public LCAListDataItem(NodeRef nodeRef, Double value, String unit, Double maxi, NodeRef cost, Boolean isManual, List<NodeRef> plants, Double previousValue, Double futureValue){
		super(nodeRef, value, unit, maxi, cost, isManual, plants, previousValue, futureValue);
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

	@AlfProp
	@AlfQname(qname="bcpg:lcaListFutureValue")
	public Double getFutureValue() {
		return futureValue;
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
	public String toString() {
		return "LcaListDataItem [value=" + value + ", unit=" + unit + ", maxi=" + maxi + ", method=" + method + ", lca=" + charact + "]";
	}

}

