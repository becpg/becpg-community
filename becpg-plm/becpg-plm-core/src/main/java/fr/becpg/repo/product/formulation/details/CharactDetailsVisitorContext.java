package fr.becpg.repo.product.formulation.details;

import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;

/**
 * <p>CharactDetailsVisitorContext class.</p>
 *
 * @author matthieu
 */
public class CharactDetailsVisitorContext {

	private ProductData rootProductData;
	
	private Integer maxLevel;
	
	private CharactDetails charactDetails;

	private double cumulatedYieldFactor = 1d;

	/**
	 * <p>Constructor for CharactDetailsVisitorContext.</p>
	 *
	 * @param rootProductData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param maxLevel a {@link java.lang.Integer} object
	 * @param charactDetails a {@link fr.becpg.repo.product.data.CharactDetails} object
	 */
	public CharactDetailsVisitorContext(ProductData rootProductData, Integer maxLevel, CharactDetails charactDetails) {
		super();
		this.rootProductData = rootProductData;
		this.maxLevel = maxLevel;
		this.charactDetails = charactDetails;
	}

	/**
	 * Returns the product of the yields of the components visited between the root product
	 * and the current composition level (1 when no intermediate component declares a yield).
	 *
	 * @return the cumulated yield factor of the current composition branch
	 */
	public double getCumulatedYieldFactor() {
		return cumulatedYieldFactor;
	}

	/**
	 * <p>Setter for the field <code>cumulatedYieldFactor</code>.</p>
	 *
	 * @param cumulatedYieldFactor the cumulated yield factor of the current composition branch
	 */
	public void setCumulatedYieldFactor(double cumulatedYieldFactor) {
		this.cumulatedYieldFactor = cumulatedYieldFactor;
	}

	/**
	 * <p>Getter for the field <code>rootProductData</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	public ProductData getRootProductData() {
		return rootProductData;
	}

	/**
	 * <p>Setter for the field <code>rootProductData</code>.</p>
	 *
	 * @param rootProductData a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	public void setRootProductData(ProductData rootProductData) {
		this.rootProductData = rootProductData;
	}

	/**
	 * <p>Getter for the field <code>maxLevel</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getMaxLevel() {
		return maxLevel;
	}

	/**
	 * <p>Setter for the field <code>maxLevel</code>.</p>
	 *
	 * @param maxLevel a {@link java.lang.Integer} object
	 */
	public void setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}

	/**
	 * <p>Getter for the field <code>charactDetails</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object
	 */
	public CharactDetails getCharactDetails() {
		return charactDetails;
	}

	/**
	 * <p>Setter for the field <code>charactDetails</code>.</p>
	 *
	 * @param charactDetails a {@link fr.becpg.repo.product.data.CharactDetails} object
	 */
	public void setCharactDetails(CharactDetails charactDetails) {
		this.charactDetails = charactDetails;
	}
	
	
}
