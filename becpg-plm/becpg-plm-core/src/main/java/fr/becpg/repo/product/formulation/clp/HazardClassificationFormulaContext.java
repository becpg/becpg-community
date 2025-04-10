package fr.becpg.repo.product.formulation.clp;

import java.util.HashMap;
import java.util.Map;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormatService;
import fr.becpg.repo.formulation.spel.SpelFormulaContext;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.IngItem;

/**
 * <p>HazardClassificationFormulaContext class.</p>
 *
 * @author matthieu
 */
public class HazardClassificationFormulaContext implements SpelFormulaContext<ProductData> {

	/** Constant <code>BOILING_POINT="BOILING_POINT"</code> */
	public static final String BOILING_POINT = "BOILING_POINT";
	/** Constant <code>FLASH_POINT="FLASH_POINT"</code> */
	public static final String FLASH_POINT = "FLASH_POINT";
	/** Constant <code>HYDROCARBON_PERC="HYDROCARBON_PERC"</code> */
	public static final String HYDROCARBON_PERC = "HYDROCARBON_PERC";
	
	/** Constant <code>ETA_VO="ETA_VO"</code> */
	public static final String ETA_VO = "ETA_VO";
	/** Constant <code>ETA_VC="ETA_VC"</code> */
	public static final String ETA_VC = "ETA_VC";
	/** Constant <code>ETA_IN_GAS="ETA_IN_GAS"</code> */
	public static final String ETA_IN_GAS = "ETA_IN_GAS";
	/** Constant <code>ETA_IN_MIST="ETA_IN_MIST"</code> */
	public static final String ETA_IN_MIST = "ETA_IN_MIST";
	/** Constant <code>ETA_IN_VAPOR="ETA_IN_VAPOR"</code> */
	public static final String ETA_IN_VAPOR = "ETA_IN_VAPOR";

	/**
	 * <p>etaType.</p>
	 *
	 * @param toxicityAcuteInhalationType a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
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
	Map<String, Map<IngItem, Double>> details;
	Map<String, Double> hMax;

	ProductData entity;

	Double boilingPoint;
	Double flashPoint;
	Double hydrocarbonPerc;

	/**
	 * <p>Constructor for HazardClassificationFormulaContext.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param hSum a {@link java.util.Map} object
	 * @param hMax a {@link java.util.Map} object
	 * @param details a {@link java.util.Map} object
	 * @param boilingPoint a {@link java.lang.Double} object
	 * @param flashPoint a {@link java.lang.Double} object
	 * @param hydrocarbonPerc a {@link java.lang.Double} object
	 */
	public HazardClassificationFormulaContext(ProductData entity, Map<String, Double> hSum, Map<String, Double> hMax,
			Map<String, Map<IngItem, Double>> details, Double boilingPoint, Double flashPoint, Double hydrocarbonPerc) {
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
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaVo() {
		return computeETA(hSum.getOrDefault(ETA_VO, 0d));
	}

	private Double computeETA(Double ret) {
		if(ret!=0d) {
			ret = 100/ret;
		}
		return ret;
	}

	/**
	 * <p>getEtaVc.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaVc() {
		return computeETA(hSum.getOrDefault(ETA_VC, 0d));
	}

	/**
	 * <p>getEtaInGas.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaInGas() {
		return computeETA(hSum.getOrDefault(ETA_IN_GAS, 0d));
	}

	/**
	 * <p>getEtaInVapor.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaInVapor() {
		return computeETA(hSum.getOrDefault(ETA_IN_GAS, 0d));
	}

	/**
	 * <p>getEtaInMist.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaInMist() {
		return computeETA(hSum.getOrDefault(ETA_IN_MIST, 0d));
	}

	/**
	 * <p>Getter for the field <code>flashPoint</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getFlashPoint() {
		return flashPoint;
	}

	/**
	 * <p>getFP.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getFP() {
		return getFlashPoint();
	}

	/**
	 * <p>Getter for the field <code>hydrocarbonPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getHydrocarbonPerc() {
		return hydrocarbonPerc;
	}

	/**
	 * <p>Getter for the field <code>boilingPoint</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getBoilingPoint() {
		return boilingPoint;
	}

	/**
	 * <p>getBP.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getBP() {
		return getBoilingPoint();
	}

	/**
	 * <p>hSum.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double hSum(String hazardStatement) {
		return hSum(hazardStatement, null);
	}

	/**
	 * <p>hSum.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @param hazardClassCode a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double hSum(String hazardStatement, String hazardClassCode) {
		return hSum.getOrDefault(toCode(hazardStatement, hazardClassCode), 0d);
	}

	/**
	 * <p>hMax.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double hMax(String hazardStatement) {
		return hMax(hazardStatement, null);
	}

	/**
	 * <p>hMax.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @param hazardClassCode a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double hMax(String hazardStatement, String hazardClassCode) {
		return hMax.getOrDefault(toCode(hazardStatement, hazardClassCode), 0d);
	}

	private String toCode(String hazardStatement, String hazardClassCode) {
		if (hazardClassCode != null) {
			return hazardClassCode + ":" + hazardStatement;
		}
		return hazardStatement;
	}

	// Oblige à reformuler x2 ??
	/**
	 * <p>isDangerousMisture.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean isDangerousMisture() {
		return entity.getHcList()!=null 
				&& entity.getHcList().stream().anyMatch(h -> "Danger".equals(h.getSignalWord())); 
	}

	/**
	 * <p>detail.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @param hazardClassCode a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String detail(String hazardStatement, String hazardClassCode) {
		Map<IngItem, Double> detail = null;

		if (hazardClassCode != null) {
			detail = details.getOrDefault(toCode(hazardStatement, hazardClassCode), new HashMap<>());
		} else {
			detail = details.getOrDefault(hazardStatement, new HashMap<>());
		}

		// Convert Map to a string format "(key value%, key2 value2%)"
		if (detail != null && !detail.isEmpty()) {
			 StringBuilder result = new StringBuilder(toCode(hazardStatement, hazardClassCode) + " [");
		        for (Map.Entry<IngItem, Double> entry : detail.entrySet()) {
		            result.append("{").append(entry.getKey().getNodeRef().getId()).append(":")
		                  .append(entry.getKey().getIngCASCode() != null ? entry.getKey().getIngCASCode() : entry.getKey().getCharactName())
		                  .append("} ").append(formatNumber(entry.getValue())).append("%, ");
		        }
		        // Remove the last ", " and close the parenthesis
		        if (result.length() > 1) {
		            result.setLength(result.length() - 2); // Remove ", "
		        }
		        result.append("]");
		        return result.toString();
		}
		return toCode(hazardStatement, hazardClassCode) + " [none]"; // Return empty parenthesis if detail is null or empty
	}

	private String formatNumber(Double value) {
		if(value!=null) {
			return PropertyFormatService.instance().getPropertyFormats(FormatMode.JSON, true).formatDecimal(value);
		}
		return "N/A";
	}

	/**
	 * <p>detail.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String detail(String hazardStatement) {
		return detail(hazardStatement, null);
	}

}
