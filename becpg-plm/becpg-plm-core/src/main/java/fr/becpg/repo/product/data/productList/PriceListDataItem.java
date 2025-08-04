/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;

/**
 * <p>PriceListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:priceList")
public class PriceListDataItem extends AbstractEffectiveDataItem {

	/**
	 *
	 */
	private static final long serialVersionUID = -5767191493924794423L;

	private Double value = 0d;

	private String unit;

	private Double purchaseValue = 0d;

	private String purchaseUnit;

	private Integer prefRank = null;

	private NodeRef cost;

	private List<NodeRef> suppliers = new ArrayList<>();
	
	private List<NodeRef> plants = new ArrayList<>();

	private List<NodeRef> geoOrigins = new ArrayList<>();

	/**
	 * <p>Getter for the field <code>geoOrigins</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:priceListGeoOrigins")
	public List<NodeRef> getGeoOrigins() {
		return geoOrigins;
	}

	/**
	 * <p>Setter for the field <code>geoOrigins</code>.</p>
	 *
	 * @param geoOrigins a {@link java.util.List} object.
	 */
	public void setGeoOrigins(List<NodeRef> geoOrigins) {
		this.geoOrigins = geoOrigins;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:priceListValue")
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
	@AlfQname(qname = "bcpg:priceListUnit")
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
	 * <p>Getter for the field <code>purchaseValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:priceListPurchaseQty")
	public Double getPurchaseValue() {
		return purchaseValue;
	}

	/**
	 * <p>Setter for the field <code>purchaseValue</code>.</p>
	 *
	 * @param purchaseValue a {@link java.lang.Double} object.
	 */
	public void setPurchaseValue(Double purchaseValue) {
		this.purchaseValue = purchaseValue;
	}

	/**
	 * <p>Getter for the field <code>purchaseUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:priceListPurchaseUnit")
	public String getPurchaseUnit() {
		return purchaseUnit;
	}

	/**
	 * <p>Setter for the field <code>purchaseUnit</code>.</p>
	 *
	 * @param purchaseUnit a {@link java.lang.String} object.
	 */
	public void setPurchaseUnit(String purchaseUnit) {
		this.purchaseUnit = purchaseUnit;
	}

	/**
	 * <p>Getter for the field <code>prefRank</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:priceListPrefRank")
	public Integer getPrefRank() {
		return prefRank;
	}

	/**
	 * <p>Setter for the field <code>prefRank</code>.</p>
	 *
	 * @param prefRank a {@link java.lang.Integer} object.
	 */
	public void setPrefRank(Integer prefRank) {
		this.prefRank = prefRank;
	}

	/**
	 * <p>Getter for the field <code>cost</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:priceListCost")
	public NodeRef getCost() {
		return cost;
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
	 * <p>Getter for the field <code>suppliers</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:suppliers")
	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	/**
	 * <p>Setter for the field <code>suppliers</code>.</p>
	 *
	 * @param suppliers a {@link java.util.List} object.
	 */
	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}
	

	/**
	 * <p>Getter for the field <code>plants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:plants")
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
	public PriceListDataItem() {

	}

	/**
	 * Instantiates a new price list data item
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param value a {@link java.lang.Double} object.
	 * @param unit a {@link java.lang.String} object.
	 * @param purchaseValue a {@link java.lang.Double} object.
	 * @param purchaseUnit a {@link java.lang.String} object.
	 * @param prefRank a {@link java.lang.Integer} object.
	 * @param startEffectivity a {@link java.util.Date} object.
	 * @param endEffectivity a {@link java.util.Date} object.
	 * @param cost a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param suppliers a {@link java.util.List} object.
	 * @param origins a {@link java.util.List} object.
	 */
	public PriceListDataItem(NodeRef nodeRef, Double value, String unit, Double purchaseValue, String purchaseUnit, Integer prefRank,
			Date startEffectivity, Date endEffectivity, NodeRef cost, List<NodeRef> suppliers, List<NodeRef> origins) {

		setNodeRef(nodeRef);
		setValue(value);
		setUnit(unit);
		setPurchaseValue(purchaseValue);
		setPurchaseUnit(purchaseUnit);
		setPrefRank(prefRank);
		setStartEffectivity(startEffectivity);
		setEndEffectivity(endEffectivity);
		setCost(cost);
		setSuppliers(suppliers);
		setGeoOrigins(origins);
	}

	/**
	 * Copy constructor
	 *
	 * @param c a {@link fr.becpg.repo.product.data.productList.PriceListDataItem} object.
	 */
	public PriceListDataItem(PriceListDataItem c) {

		setNodeRef(c.getNodeRef());
		setValue(c.getValue());
		setUnit(c.getUnit());
		setPurchaseValue(c.getPurchaseValue());
		setPurchaseUnit(c.getPurchaseUnit());
		setPrefRank(c.getPrefRank());
		setStartEffectivity(c.getStartEffectivity());
		setEndEffectivity(c.getEndEffectivity());
		setCost(c.getCost());
		setSuppliers(c.getSuppliers());
		setGeoOrigins(c.getGeoOrigins());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((cost == null) ? 0 : cost.hashCode());
		result = (prime * result) + ((geoOrigins == null) ? 0 : geoOrigins.hashCode());
		result = (prime * result) + ((prefRank == null) ? 0 : prefRank.hashCode());
		result = (prime * result) + ((purchaseUnit == null) ? 0 : purchaseUnit.hashCode());
		result = (prime * result) + ((purchaseValue == null) ? 0 : purchaseValue.hashCode());
		result = (prime * result) + ((suppliers == null) ? 0 : suppliers.hashCode());
		result = (prime * result) + ((unit == null) ? 0 : unit.hashCode());
		result = (prime * result) + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PriceListDataItem other = (PriceListDataItem) obj;
		if (cost == null) {
			if (other.cost != null) {
				return false;
			}
		} else if (!cost.equals(other.cost)) {
			return false;
		}
		if (geoOrigins == null) {
			if (other.geoOrigins != null) {
				return false;
			}
		} else if (!geoOrigins.equals(other.geoOrigins)) {
			return false;
		}
		if (prefRank == null) {
			if (other.prefRank != null) {
				return false;
			}
		} else if (!prefRank.equals(other.prefRank)) {
			return false;
		}
		if (purchaseUnit == null) {
			if (other.purchaseUnit != null) {
				return false;
			}
		} else if (!purchaseUnit.equals(other.purchaseUnit)) {
			return false;
		}
		if (purchaseValue == null) {
			if (other.purchaseValue != null) {
				return false;
			}
		} else if (!purchaseValue.equals(other.purchaseValue)) {
			return false;
		}
		if (suppliers == null) {
			if (other.suppliers != null) {
				return false;
			}
		} else if (!suppliers.equals(other.suppliers)) {
			return false;
		}
		if (unit == null) {
			if (other.unit != null) {
				return false;
			}
		} else if (!unit.equals(other.unit)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PriceListDataItem [value=" + value + ", unit=" + unit + ", purchaseValue=" + purchaseValue + ", purchaseUnit=" + purchaseUnit
				+ ", prefRank=" + prefRank + ", cost=" + cost + ", suppliers=" + suppliers + ", geoOrigins=" + geoOrigins + "]";
	}

}
