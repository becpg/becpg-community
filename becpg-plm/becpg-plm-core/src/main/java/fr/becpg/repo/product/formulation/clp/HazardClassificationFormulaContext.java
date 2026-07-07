package fr.becpg.repo.product.formulation.clp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.repo.formulation.spel.SpelFormulaContext;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.IngItem;

/**
 * <p>HazardClassificationFormulaContext class.</p>
 *
 * Aggregates CLP hazard quantities per substance so that helper methods
 * (hSum, hMax, hSumUnique, eta*) can apply a per-rule substance inclusion
 * threshold before summing.
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

	private static final double ROUNDING_FACTOR = 1e6d;

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

	/**
	 * Substance inclusion threshold parsed from the CLP rules file
	 * (e.g. "&gt;=1%", "&gt;0,1%"). Substances whose quantity percentage in the
	 * product does not satisfy the threshold are excluded from sums.
	 *
	 * @param value the percentage threshold
	 * @param strict true for a strict comparison (&gt;), false for &gt;=
	 */
	public record SubstanceThreshold(double value, boolean strict) {

		private static final Pattern THRESHOLD_PATTERN = Pattern.compile("^\\s*(>=|>)?\\s*([0-9]+(?:[.,][0-9]+)?)\\s*%?\\s*$");

		/**
		 * Parses a threshold expression such as "&gt;=1%", "&gt;0,1%" or "1%".
		 *
		 * @param expression the raw threshold cell content
		 * @return the parsed threshold, or null when blank or not parseable
		 */
		public static SubstanceThreshold parse(String expression) {
			if ((expression == null) || expression.isBlank()) {
				return null;
			}
			Matcher matcher = THRESHOLD_PATTERN.matcher(expression);
			if (!matcher.matches()) {
				return null;
			}
			double value = Double.parseDouble(matcher.group(2).replace(',', '.'));
			return new SubstanceThreshold(value, ">".equals(matcher.group(1)));
		}

		/**
		 * Checks whether a substance quantity percentage satisfies the threshold.
		 *
		 * @param qtyPerc the substance quantity percentage in the product
		 * @return true if the substance must be taken into account
		 */
		public boolean accept(double qtyPerc) {
			double rounded = round(qtyPerc);
			return strict ? rounded > value : rounded >= value;
		}
	}

	private final Map<String, Map<IngItem, Double>> substanceValues;
	private final Map<IngItem, Double> substanceQtyPercs;

	private ProductData entity;

	private Double boilingPoint;
	private Double flashPoint;
	private Double hydrocarbonPerc;

	private SubstanceThreshold substanceThreshold;

	/**
	 * <p>Constructor for HazardClassificationFormulaContext.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param substanceValues per hazard key, the value contributed by each substance
	 * @param substanceQtyPercs the raw quantity percentage of each substance in the product
	 * @param boilingPoint a {@link java.lang.Double} object
	 * @param flashPoint a {@link java.lang.Double} object
	 * @param hydrocarbonPerc a {@link java.lang.Double} object
	 */
	public HazardClassificationFormulaContext(ProductData entity, Map<String, Map<IngItem, Double>> substanceValues,
			Map<IngItem, Double> substanceQtyPercs, Double boilingPoint, Double flashPoint, Double hydrocarbonPerc) {
		this.substanceValues = substanceValues;
		this.substanceQtyPercs = substanceQtyPercs;
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
	 * Sets the substance inclusion threshold of the CLP rule being evaluated.
	 *
	 * @param substanceThreshold the current rule threshold, or null for none
	 */
	public void setSubstanceThreshold(SubstanceThreshold substanceThreshold) {
		this.substanceThreshold = substanceThreshold;
	}

	/**
	 *
	 * Helpers for CLP Spel formula
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaVo() {
		return computeETA(filteredSum(ETA_VO));
	}

	/**
	 * <p>computeETA.</p>
	 *
	 * @param ret a {@link java.lang.Double} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double computeETA(Double ret) {
		if (ret != 0d) {
			ret = 100 / ret;
		}
		return ret;
	}

	/**
	 * <p>getEtaVc.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaVc() {
		return computeETA(filteredSum(ETA_VC));
	}

	/**
	 * <p>getEtaInGas.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaInGas() {
		return computeETA(filteredSum(ETA_IN_GAS));
	}

	/**
	 * <p>getEtaInVapor.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaInVapor() {
		return computeETA(filteredSum(ETA_IN_VAPOR));
	}

	/**
	 * <p>getEtaInMist.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getEtaInMist() {
		return computeETA(filteredSum(ETA_IN_MIST));
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
		return filteredSum(toCode(hazardStatement, hazardClassCode));
	}

	/**
	 * Sums the quantity of the substances carrying at least one of the given
	 * hazard statements, counting each substance only once even when it
	 * carries several of them (e.g. a substance classified both H314 and H318).
	 *
	 * @param hazardStatements the hazard statement codes to combine
	 * @return the sum of the distinct substance quantities
	 */
	public Double hSumUnique(String... hazardStatements) {
		Map<IngItem, Double> merged = new HashMap<>();
		for (String hazardStatement : hazardStatements) {
			Map<IngItem, Double> values = substanceValues.get(hazardStatement);
			if (values != null) {
				for (Map.Entry<IngItem, Double> entry : values.entrySet()) {
					merged.merge(entry.getKey(), entry.getValue(), Double::max);
				}
			}
		}
		double sum = 0d;
		for (Map.Entry<IngItem, Double> entry : merged.entrySet()) {
			if (accept(entry.getKey())) {
				sum += entry.getValue();
			}
		}
		return round(sum);
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
		Map<IngItem, Double> values = substanceValues.get(toCode(hazardStatement, hazardClassCode));
		double max = 0d;
		if (values != null) {
			for (Map.Entry<IngItem, Double> entry : values.entrySet()) {
				if (accept(entry.getKey()) && (entry.getValue() != null) && (entry.getValue() > max)) {
					max = entry.getValue();
				}
			}
		}
		return round(max);
	}

	/**
	 * <p>toCode.</p>
	 *
	 * @param hazardStatement a {@link java.lang.String} object
	 * @param hazardClassCode a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
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
		return entity.getHcList() != null
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
		Map<IngItem, Double> detail = substanceValues.getOrDefault(toCode(hazardStatement, hazardClassCode), new HashMap<>());

		// Convert Map to a string format "(key value%, key2 value2%)"
		StringBuilder result = new StringBuilder(toCode(hazardStatement, hazardClassCode) + " [");
		boolean empty = true;
		for (Map.Entry<IngItem, Double> entry : detail.entrySet()) {
			if (accept(entry.getKey())) {
				result.append("{").append(entry.getKey().getNodeRef().getId()).append(":")
						.append(entry.getKey().getIngCASCode() != null ? entry.getKey().getIngCASCode() : entry.getKey().getCharactName())
						.append("} ").append(formatNumber(entry.getValue())).append("%, ");
				empty = false;
			}
		}
		if (empty) {
			return toCode(hazardStatement, hazardClassCode) + " [none]";
		}
		// Remove the last ", " and close the parenthesis
		result.setLength(result.length() - 2);
		result.append("]");
		return result.toString();
	}

	/**
	 * <p>formatNumber.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @return a {@link java.lang.String} object
	 */
	private String formatNumber(Double value) {
		if (value != null) {
			return PropertyFormats.forMode(FormatMode.JSON, true).formatDecimal(value);
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

	/**
	 * Sums the values of the given hazard key over the substances satisfying
	 * the current substance threshold.
	 *
	 * @param key the hazard aggregation key
	 * @return the rounded filtered sum
	 */
	private Double filteredSum(String key) {
		Map<IngItem, Double> values = substanceValues.get(key);
		double sum = 0d;
		if (values != null) {
			for (Map.Entry<IngItem, Double> entry : values.entrySet()) {
				if (accept(entry.getKey()) && (entry.getValue() != null)) {
					sum += entry.getValue();
				}
			}
		}
		return round(sum);
	}

	/**
	 * Checks whether a substance satisfies the current rule threshold.
	 *
	 * @param ingItem the substance
	 * @return true if the substance must be taken into account
	 */
	private boolean accept(IngItem ingItem) {
		if (substanceThreshold == null) {
			return true;
		}
		return substanceThreshold.accept(substanceQtyPercs.getOrDefault(ingItem, 0d));
	}

	/**
	 * Rounds a value to 6 decimals to avoid floating point artifacts around
	 * regulatory thresholds (e.g. 0.09999999999999998 vs 0.1).
	 *
	 * @param value the value to round
	 * @return the rounded value
	 */
	private static double round(double value) {
		return Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;
	}

}
