package fr.becpg.repo.product.formulation.clp;

import java.util.HashMap;
import java.util.Map;

import fr.becpg.repo.formulation.spel.SpelFormulaContext;
import fr.becpg.repo.product.data.ProductData;

public class HazardClassificationFormulaContext implements SpelFormulaContext<ProductData> {

	public static final String BOILING_POINT = "BOILING_POINT";
	public static final String FLASH_POINT = "FLASH_POINT";
	public static final String HYDROCARBON_PERC = "HYDROCARBON_PERC";

	public static final String ETA_VO = "ETA_VO";
	public static final String ETA_VC = "ETA_VC";
	public static final String ETA_IN_GAS = "ETA_IN_GAS";
	public static final String ETA_IN_MIST = "ETA_IN_MIST";
	public static final String ETA_IN_VAPOR = "ETA_IN_VAPOR";

	public static String etaType(String toxicityAcuteInhalationType) {
		switch (toxicityAcuteInhalationType) {
		case "Gas":
			return ETA_IN_GAS;
		case "Mist":
			return ETA_IN_MIST;
		case "Vapor":
			return ETA_IN_VAPOR;
		default:
			return null;
		}
	}

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

	public Double getEtaInGas() {
		return hMax.getOrDefault(ETA_IN_GAS, 0d);
	}

	public Double getEtaInVapor() {
		return hMax.getOrDefault(ETA_IN_GAS, 0d);
	}

	public Double getEtaInMist() {
		return hMax.getOrDefault(ETA_IN_MIST, 0d);
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

	public Boolean isDangerousMisture() {
		return false; //TODO
	}

	public String detail(String hazardStatement, String hazardClassCode) {
		Map<String, Double> detail = null;

		if (hazardClassCode != null) {
			detail = details.getOrDefault(toCode(hazardStatement, hazardClassCode), new HashMap<>());
		}
		detail = details.getOrDefault(hazardStatement, new HashMap<>());

		// Convert Map to a string format "(key value%, key2 value2%)"
		if (detail != null && !detail.isEmpty()) {
			StringBuilder result = new StringBuilder("(");
			for (Map.Entry<String, Double> entry : detail.entrySet()) {
				result.append(entry.getKey()).append(" ").append(entry.getValue()).append("%, ");
			}
			// Remove the last ", " and close the parenthesis
			if (result.length() > 1) {
				result.setLength(result.length() - 2); // Remove ", "
			}
			result.append(")");
			return result.toString();
		}
		return "()"; // Return empty parenthesis if detail is null or empty
	}

	public String detail(String hazardStatement) {
		return detail(hazardStatement, null);
	}

}
