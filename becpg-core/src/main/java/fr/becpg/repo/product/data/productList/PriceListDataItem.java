/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Class PriceListDataItem.
 *
 * @author querephi
 */
public class PriceListDataItem{
		
	private NodeRef nodeRef;
		
	private Float value = 0f;
	
	private String unit;
	
	private Float purchaseValue = 0f;
		
	private String purchaseUnit;
	
	private Integer prefRank = null;
	
	private Date startEffectivity;
	
	private Date endEffectivity;
	
	private NodeRef cost;
	
	private List<NodeRef> suppliers = new ArrayList<NodeRef>();
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Float getPurchaseValue() {
		return purchaseValue;
	}

	public void setPurchaseValue(Float purchaseValue) {
		this.purchaseValue = purchaseValue;
	}

	public String getPurchaseUnit() {
		return purchaseUnit;
	}

	public void setPurchaseUnit(String purchaseUnit) {
		this.purchaseUnit = purchaseUnit;
	}

	public Integer getPrefRank() {
		return prefRank;
	}

	public void setPrefRank(Integer prefRank) {
		this.prefRank = prefRank;
	}

	public Date getStartEffectivity() {
		return startEffectivity;
	}

	public void setStartEffectivity(Date startEffectivity) {
		this.startEffectivity = startEffectivity;
	}

	public Date getEndEffectivity() {
		return endEffectivity;
	}

	public void setEndEffectivity(Date endEffectivity) {
		this.endEffectivity = endEffectivity;
	}

	public NodeRef getCost() {
		return cost;
	}

	public void setCost(NodeRef cost) {
		this.cost = cost;
	}

	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}

	/**
	 * Instantiates a new cost list data item.
	 */
	public PriceListDataItem() {
		
	}
	
	/**
	 * Instantiates a new price list data item
	 * @param nodeRef
	 * @param value
	 * @param unit
	 * @param purchaseValue
	 * @param purchaseUnit
	 * @param prefRank
	 * @param startEffectivity
	 * @param endEffectivity
	 * @param cost
	 * @param suppliers
	 */
	public PriceListDataItem(NodeRef nodeRef, Float value, String unit, Float purchaseValue, String purchaseUnit, Integer prefRank, Date startEffectivity, Date endEffectivity, NodeRef cost, List<NodeRef> suppliers){
		
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
	}
	
	/**
	 * Copy constructor
	 * @param c
	 */
	public PriceListDataItem(PriceListDataItem c){
		
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
	}
}

