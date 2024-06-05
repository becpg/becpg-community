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

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	@InternalField
	public CollectionPriceListDataItem getParent() {
		return parent;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(CollectionPriceListDataItem parent) {
		this.parent = parent;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	@InternalField
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

	/**
	 * <p>Getter for the field <code>price</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:tradeItemPrice")
	public Double getPrice() {
		return price;
	}

	/**
	 * <p>Setter for the field <code>price</code>.</p>
	 *
	 * @param price a {@link java.lang.Double} object
	 */
	public void setPrice(Double price) {
		this.price = price;
	}

	/**
	 * <p>Getter for the field <code>priceTaxIncl</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:cplPriceTaxIncl")
	public Double getPriceTaxIncl() {
		return priceTaxIncl;
	}

	/**
	 * <p>Setter for the field <code>priceTaxIncl</code>.</p>
	 *
	 * @param priceTaxIncl a {@link java.lang.Double} object
	 */
	public void setPriceTaxIncl(Double priceTaxIncl) {
		this.priceTaxIncl = priceTaxIncl;
	}

	/**
	 * <p>Getter for the field <code>priceUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:tradeItemPriceUnit")
	public String getPriceUnit() {
		return priceUnit;
	}

	/**
	 * <p>Setter for the field <code>priceUnit</code>.</p>
	 *
	 * @param priceUnit a {@link java.lang.String} object
	 */
	public void setPriceUnit(String priceUnit) {
		this.priceUnit = priceUnit;
	}

	/**
	 * <p>Getter for the field <code>product</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "gs1:cplProduct")
	@DataListIdentifierAttr
	@InternalField
	public NodeRef getProduct() {
		return product;
	}

	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}

	/**
	 * <p>Getter for the field <code>priceBasisQuantity</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:priceBasisQuantity")
	public Double getPriceBasisQuantity() {
		return priceBasisQuantity;
	}

	/**
	 * <p>Setter for the field <code>priceBasisQuantity</code>.</p>
	 *
	 * @param priceBasisQuantity a {@link java.lang.Double} object
	 */
	public void setPriceBasisQuantity(Double priceBasisQuantity) {
		this.priceBasisQuantity = priceBasisQuantity;
	}

	/**
	 * <p>Getter for the field <code>priceBasisQuantityUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:priceBasisQuantityUnit")
	public String getPriceBasisQuantityUnit() {
		return priceBasisQuantityUnit;
	}

	/**
	 * <p>Setter for the field <code>priceBasisQuantityUnit</code>.</p>
	 *
	 * @param priceBasisQuantityUnit a {@link java.lang.String} object
	 */
	public void setPriceBasisQuantityUnit(String priceBasisQuantityUnit) {
		this.priceBasisQuantityUnit = priceBasisQuantityUnit;
	}

	
	/**
	 * <p>Getter for the field <code>turnover</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:cplProductTurnover")
	public Double getTurnover() {
		return turnover;
	}

	/**
	 * <p>Setter for the field <code>turnover</code>.</p>
	 *
	 * @param turnover a {@link java.lang.Double} object
	 */
	public void setTurnover(Double turnover) {
		this.turnover = turnover;
	}

	/**
	 * <p>Getter for the field <code>dutyFeeTax</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "gs1:dutyFeeTaxRef")
	public NodeRef getDutyFeeTax() {
		return dutyFeeTax;
	}

	/**
	 * <p>Setter for the field <code>dutyFeeTax</code>.</p>
	 *
	 * @param dutyFeeTax a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setDutyFeeTax(NodeRef dutyFeeTax) {
		this.dutyFeeTax = dutyFeeTax;
	}

	/**
	 * <p>Getter for the field <code>dutyFeeTaxAmount</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:dutyFeeTaxAmount")
	public Double getDutyFeeTaxAmount() {
		return dutyFeeTaxAmount;
	}

	/**
	 * <p>Setter for the field <code>dutyFeeTaxAmount</code>.</p>
	 *
	 * @param dutyFeeTaxAmount a {@link java.lang.Double} object
	 */
	public void setDutyFeeTaxAmount(Double dutyFeeTaxAmount) {
		this.dutyFeeTaxAmount = dutyFeeTaxAmount;
	}

	/**
	 * <p>Getter for the field <code>unitTotalCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:cplUnitTotalCost")
	public Double getUnitTotalCost() {
		return unitTotalCost;
	}

	/**
	 * <p>Setter for the field <code>unitTotalCost</code>.</p>
	 *
	 * @param unitTotalCost a {@link java.lang.Double} object
	 */
	public void setUnitTotalCost(Double unitTotalCost) {
		this.unitTotalCost = unitTotalCost;
	}

	/**
	 * <p>Getter for the field <code>profitabilityRatio</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:cplProfitabilityRatio")
	public Double getProfitabilityRatio() {
		return profitabilityRatio;
	}

	/**
	 * <p>Setter for the field <code>profitabilityRatio</code>.</p>
	 *
	 * @param profitabilityRatio a {@link java.lang.Double} object
	 */
	public void setProfitabilityRatio(Double profitabilityRatio) {
		this.profitabilityRatio = profitabilityRatio;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(depthLevel, dutyFeeTax, dutyFeeTaxAmount, parent, price, priceBasisQuantity, priceBasisQuantityUnit,
				priceTaxIncl, priceUnit, product, profitabilityRatio, unitTotalCost);
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
		CollectionPriceListDataItem other = (CollectionPriceListDataItem) obj;
		return Objects.equals(depthLevel, other.depthLevel) && Objects.equals(dutyFeeTax, other.dutyFeeTax)
				&& Objects.equals(dutyFeeTaxAmount, other.dutyFeeTaxAmount) && Objects.equals(parent, other.parent)
				&& Objects.equals(price, other.price) && Objects.equals(priceBasisQuantity, other.priceBasisQuantity)
				&& Objects.equals(priceBasisQuantityUnit, other.priceBasisQuantityUnit) && Objects.equals(priceTaxIncl, other.priceTaxIncl)
				&& Objects.equals(priceUnit, other.priceUnit) && Objects.equals(product, other.product)
				&& Objects.equals(profitabilityRatio, other.profitabilityRatio) && Objects.equals(unitTotalCost, other.unitTotalCost);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CollectionPriceListDataItem [product=" + product + ", price=" + price + ", priceTaxIncl=" + priceTaxIncl + ", priceUnit=" + priceUnit
				+ ", priceBasisQuantity=" + priceBasisQuantity + ", priceBasisQuantityUnit=" + priceBasisQuantityUnit + ", dutyFeeTax=" + dutyFeeTax
				+ ", dutyFeeTaxAmount=" + dutyFeeTaxAmount + ", unitTotalCost=" + unitTotalCost + ", profitabilityRatio=" + profitabilityRatio
				+ ", depthLevel=" + depthLevel + ", parent=" + parent + "]";
	}
	
	

}
