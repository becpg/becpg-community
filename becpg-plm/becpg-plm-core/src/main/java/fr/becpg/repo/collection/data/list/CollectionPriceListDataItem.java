/*
 *
 */
package fr.becpg.repo.collection.data.list;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;

/**
 * <p>CollectionPriceListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "gs1:collectionPriceList")
@MultiLevelDataList
public class CollectionPriceListDataItem extends AbstractEffectiveDataItem implements CompositeDataItem<CollectionPriceListDataItem> {

	/**
	 *
	 */
	private static final long serialVersionUID = -2469330028880405428L;
	private NodeRef product;
	private Double price;
	private Double priceTaxIncl;
	private String priceUnit;
	private Double priceBasisQuantity;
	private String priceBasisQuantityUnit;
	
	private Double turnover;

	private NodeRef dutyFeeTax;
	private Double dutyFeeTaxAmount;
	private Double unitTotalCost;
	private Double profitabilityRatio;
	private Integer depthLevel;

	private CollectionPriceListDataItem parent;

	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	@InternalField
	public CollectionPriceListDataItem getParent() {
		return parent;
	}

	@Override
	public void setParent(CollectionPriceListDataItem parent) {
		this.parent = parent;
	}

	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	@InternalField
	public Integer getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	@AlfProp
	@AlfQname(qname = "gs1:tradeItemPrice")
	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	@AlfProp
	@AlfQname(qname = "gs1:cplPriceTaxIncl")
	public Double getPriceTaxIncl() {
		return priceTaxIncl;
	}

	public void setPriceTaxIncl(Double priceTaxIncl) {
		this.priceTaxIncl = priceTaxIncl;
	}

	@AlfProp
	@AlfQname(qname = "gs1:tradeItemPriceUnit")
	public String getPriceUnit() {
		return priceUnit;
	}

	public void setPriceUnit(String priceUnit) {
		this.priceUnit = priceUnit;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "gs1:cplProduct")
	@DataListIdentifierAttr
	@InternalField
	public NodeRef getProduct() {
		return product;
	}

	public void setProduct(NodeRef product) {
		this.product = product;
	}

	@AlfProp
	@AlfQname(qname = "gs1:priceBasisQuantity")
	public Double getPriceBasisQuantity() {
		return priceBasisQuantity;
	}

	public void setPriceBasisQuantity(Double priceBasisQuantity) {
		this.priceBasisQuantity = priceBasisQuantity;
	}

	@AlfProp
	@AlfQname(qname = "gs1:priceBasisQuantityUnit")
	public String getPriceBasisQuantityUnit() {
		return priceBasisQuantityUnit;
	}

	public void setPriceBasisQuantityUnit(String priceBasisQuantityUnit) {
		this.priceBasisQuantityUnit = priceBasisQuantityUnit;
	}

	
	@AlfProp
	@AlfQname(qname = "gs1:cplProductTurnover")
	public Double getTurnover() {
		return turnover;
	}

	public void setTurnover(Double turnover) {
		this.turnover = turnover;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "gs1:dutyFeeTaxRef")
	public NodeRef getDutyFeeTax() {
		return dutyFeeTax;
	}

	public void setDutyFeeTax(NodeRef dutyFeeTax) {
		this.dutyFeeTax = dutyFeeTax;
	}

	@AlfProp
	@AlfQname(qname = "gs1:dutyFeeTaxAmount")
	public Double getDutyFeeTaxAmount() {
		return dutyFeeTaxAmount;
	}

	public void setDutyFeeTaxAmount(Double dutyFeeTaxAmount) {
		this.dutyFeeTaxAmount = dutyFeeTaxAmount;
	}

	@AlfProp
	@AlfQname(qname = "gs1:cplUnitTotalCost")
	public Double getUnitTotalCost() {
		return unitTotalCost;
	}

	public void setUnitTotalCost(Double unitTotalCost) {
		this.unitTotalCost = unitTotalCost;
	}

	@AlfProp
	@AlfQname(qname = "gs1:cplProfitabilityRatio")
	public Double getProfitabilityRatio() {
		return profitabilityRatio;
	}

	public void setProfitabilityRatio(Double profitabilityRatio) {
		this.profitabilityRatio = profitabilityRatio;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(depthLevel, dutyFeeTax, dutyFeeTaxAmount, parent, price, priceBasisQuantity, priceBasisQuantityUnit,
				priceTaxIncl, priceUnit, product, profitabilityRatio, unitTotalCost);
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
		CollectionPriceListDataItem other = (CollectionPriceListDataItem) obj;
		return Objects.equals(depthLevel, other.depthLevel) && Objects.equals(dutyFeeTax, other.dutyFeeTax)
				&& Objects.equals(dutyFeeTaxAmount, other.dutyFeeTaxAmount) && Objects.equals(parent, other.parent)
				&& Objects.equals(price, other.price) && Objects.equals(priceBasisQuantity, other.priceBasisQuantity)
				&& Objects.equals(priceBasisQuantityUnit, other.priceBasisQuantityUnit) && Objects.equals(priceTaxIncl, other.priceTaxIncl)
				&& Objects.equals(priceUnit, other.priceUnit) && Objects.equals(product, other.product)
				&& Objects.equals(profitabilityRatio, other.profitabilityRatio) && Objects.equals(unitTotalCost, other.unitTotalCost);
	}

	@Override
	public String toString() {
		return "CollectionPriceListDataItem [product=" + product + ", price=" + price + ", priceTaxIncl=" + priceTaxIncl + ", priceUnit=" + priceUnit
				+ ", priceBasisQuantity=" + priceBasisQuantity + ", priceBasisQuantityUnit=" + priceBasisQuantityUnit + ", dutyFeeTax=" + dutyFeeTax
				+ ", dutyFeeTaxAmount=" + dutyFeeTaxAmount + ", unitTotalCost=" + unitTotalCost + ", profitabilityRatio=" + profitabilityRatio
				+ ", depthLevel=" + depthLevel + ", parent=" + parent + "]";
	}
	
	

}
