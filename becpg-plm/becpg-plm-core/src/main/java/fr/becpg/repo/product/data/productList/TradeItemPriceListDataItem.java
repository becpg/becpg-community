/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;

/**
 * <p>TradeItemPriceListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "gs1:tradeItemPriceList")
public class TradeItemPriceListDataItem extends AbstractEffectiveDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Double value = 0d;

	private String unit;

	private Double priceBasisQuantity = 0d;

	private String priceBasisQuantityUnit;

	private String priceType = null;

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "gs1:tradeItemPrice")
	public Double getValue() {
		return value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.Double} object.
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "gs1:tradeItemPriceUnit")
	public String getUnit() {
		return unit;
	}

	/**
	 * <p>Setter for the field <code>unit</code>.</p>
	 *
	 * @param unit a {@link java.lang.String} object.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
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
	 * <p>Getter for the field <code>priceType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "gs1:tradeItemPriceType")
	public String getPriceType() {
		return priceType;
	}

	/**
	 * <p>Setter for the field <code>priceType</code>.</p>
	 *
	 * @param priceType a {@link java.lang.String} object
	 */
	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(priceBasisQuantity, priceBasisQuantityUnit, priceType, unit, value);
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
		TradeItemPriceListDataItem other = (TradeItemPriceListDataItem) obj;
		return Objects.equals(priceBasisQuantity, other.priceBasisQuantity) && Objects.equals(priceBasisQuantityUnit, other.priceBasisQuantityUnit)
				&& Objects.equals(priceType, other.priceType) && Objects.equals(unit, other.unit) && Objects.equals(value, other.value);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TradeItemPriceListDataItem [value=" + value + ", unit=" + unit + ", priceBasisQuantity=" + priceBasisQuantity
				+ ", priceBasisQuantityUnit=" + priceBasisQuantityUnit + ", priceType=" + priceType + "]";
	}

}
