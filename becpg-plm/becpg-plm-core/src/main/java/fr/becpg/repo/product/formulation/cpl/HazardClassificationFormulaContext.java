package fr.becpg.repo.product.formulation.cpl;

import java.util.Map;

import fr.becpg.repo.formulation.spel.SpelFormulaContext;
import fr.becpg.repo.product.data.ProductData;

public class HazardClassificationFormulaContext implements SpelFormulaContext<ProductData> {

	public static final String BOILING_POINT = "BOILING_POINT";
	public static final String FLASH_POINT = "FLASH_POINT";
	public static final String HYDROCARBON_PERC = "HYDROCARBON_PERC";

	public static final String ETA_VO = "ETA_VO";
	public static final String ETA_VC = "ETA_VC";
	public static final String ETA_IN = "ETA_IN";

	Map<String, Double> hSum;
	Map<String, Map<String, Double>> details;
	Map<String, Double> hMax;

	ProductData entity;

	Double boilingPoint = null;
	Double flashPoint = null;
	Double hydrocarbonPerc = null;

	public HazardClassificationFormulaContext(ProductData entity, Map<String, Double> hSum, Map<String, Double> hMax,
			Map<String, Map<String, Double>> details, Double boilingPoint, Double flashPoint, Double hydrocarbonPerc) {
		this.hSum = hSum;
		this.hMax = hMax;
		this.details = details;
		this.entity = entity;
		this.boilingPoint = boilingPoint;
		this.flashPoint = flashPoint;
		this.hydrocarbonPerc = hydrocarbonPerc;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Getter for the field <code>entity</code>.
	 * </p>
	 */
	@Override
	public ProductData getEntity() {
		return entity;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Setter for the field <code>entity</code>.
	 * </p>
	 */
	@Override
	public void setEntity(ProductData entity) {
		this.entity = entity;
	}

	/**
	 *
	 * Helpers for CLP Spel formula
	 */

	public Double getEtaVo() {
		return hMax.getOrDefault(ETA_VO, 0d);
	}

	public Double getEtaVc() {
		return hMax.getOrDefault(ETA_VC, 0d);
	}

	public Double getEtaIn() {
		return hMax.getOrDefault(ETA_IN, 0d);
	}

	public Double getFlashPoint() {
		return flashPoint;
	}

	public Double getFP() {
		return getFlashPoint();
	}

	public Double getHydrocarbonPerc() {
		return hydrocarbonPerc;
	}

	public Double getBoilingPoint() {
		return boilingPoint;
	}

	public Double getBP() {
		return getBoilingPoint();
	}

	public Double hSum(String hazardStatement) {
		return hSum(hazardStatement, null);
	}

	public Double hSum(String hazardStatement, String hazardClassCode) {
		if (hazardClassCode != null) {
			return hSum.getOrDefault(toCode(hazardStatement, hazardClassCode), 0d);
		}
		return hSum.getOrDefault(hazardStatement, 0d);
	}

	public Double hMax(String hazardStatement) {
		return hMax(hazardStatement, null);
	}

	public Double hMax(String hazardStatement, String hazardClassCode) {
		if (hazardClassCode != null) {
			return hMax.getOrDefault(toCode(hazardStatement, hazardClassCode), 0d);
		}
		return hMax.getOrDefault(hazardStatement, 0d);
	}

	private String toCode(String hazardStatement, String hazardClassCode) {
		return hazardClassCode + ":" + hazardStatement;
	}

}
