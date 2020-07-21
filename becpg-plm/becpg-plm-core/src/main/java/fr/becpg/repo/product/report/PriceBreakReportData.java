package fr.becpg.repo.product.report;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>PriceBreakReportData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PriceBreakReportData {

	private Double simulatedValue = 0d;

	private Double projectedQty = 0d;

	private NodeRef cost;

	private Double priceListValue;

	private String priceListUnit;

	private Double priceListPurchaseValue;

	private String priceListPurchaseUnit;

	private Integer priceListPrefRank;

	private List<NodeRef> suppliers = new ArrayList<>();
	
	private List<NodeRef> geoOrigins = new ArrayList<>();

	private NodeRef product;

	/**
	 * <p>Getter for the field <code>simulatedValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getSimulatedValue() {
		return simulatedValue;
	}

	/**
	 * <p>Setter for the field <code>simulatedValue</code>.</p>
	 *
	 * @param simulatedValue a {@link java.lang.Double} object.
	 */
	public void setSimulatedValue(Double simulatedValue) {
		this.simulatedValue = simulatedValue;
	}

	/**
	 * <p>Getter for the field <code>projectedQty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getProjectedQty() {
		return projectedQty;
	}

	/**
	 * <p>Setter for the field <code>projectedQty</code>.</p>
	 *
	 * @param projectedQty a {@link java.lang.Double} object.
	 */
	public void setProjectedQty(Double projectedQty) {
		this.projectedQty = projectedQty;
	}

	/**
	 * <p>Getter for the field <code>cost</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
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
	 * <p>Getter for the field <code>product</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getProduct() {
		return product;
	}

	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}

	/**
	 * <p>Getter for the field <code>priceListValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getPriceListValue() {
		return priceListValue;
	}

	/**
	 * <p>Setter for the field <code>priceListValue</code>.</p>
	 *
	 * @param priceListValue a {@link java.lang.Double} object.
	 */
	public void setPriceListValue(Double priceListValue) {
		this.priceListValue = priceListValue;
	}

	/**
	 * <p>Getter for the field <code>priceListUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPriceListUnit() {
		return priceListUnit;
	}

	/**
	 * <p>Setter for the field <code>priceListUnit</code>.</p>
	 *
	 * @param priceListUnit a {@link java.lang.String} object.
	 */
	public void setPriceListUnit(String priceListUnit) {
		this.priceListUnit = priceListUnit;
	}

	/**
	 * <p>Getter for the field <code>priceListPurchaseValue</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getPriceListPurchaseValue() {
		return priceListPurchaseValue;
	}

	/**
	 * <p>Setter for the field <code>priceListPurchaseValue</code>.</p>
	 *
	 * @param priceListPurchaseValue a {@link java.lang.Double} object.
	 */
	public void setPriceListPurchaseValue(Double priceListPurchaseValue) {
		this.priceListPurchaseValue = priceListPurchaseValue;
	}

	/**
	 * <p>Getter for the field <code>priceListPurchaseUnit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPriceListPurchaseUnit() {
		return priceListPurchaseUnit;
	}

	/**
	 * <p>Setter for the field <code>priceListPurchaseUnit</code>.</p>
	 *
	 * @param priceListPurchaseUnit a {@link java.lang.String} object.
	 */
	public void setPriceListPurchaseUnit(String priceListPurchaseUnit) {
		this.priceListPurchaseUnit = priceListPurchaseUnit;
	}

	/**
	 * <p>Getter for the field <code>priceListPrefRank</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getPriceListPrefRank() {
		return priceListPrefRank;
	}

	/**
	 * <p>Setter for the field <code>priceListPrefRank</code>.</p>
	 *
	 * @param priceListPrefRank a {@link java.lang.Integer} object.
	 */
	public void setPriceListPrefRank(Integer priceListPrefRank) {
		this.priceListPrefRank = priceListPrefRank;
	}

	/**
	 * <p>Getter for the field <code>geoOrigins</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
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

	
	
}
