package fr.becpg.repo.product.formulation.lca;

/**
 * <p>LCAData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LCAData {
	private String id;
	private String value;
	private Double score;
	private Double climateChange;
	private Double ozoneDepletion;
	private Double ionizingRadiation;
	private Double photochemicalOzoneFormation;
	private Double particulateMatter;
	private Double acidification;
	private Double terrestrialEutrophication;
	private Double freshwaterEutrophication;
	private Double marineEutrophication;
	private Double landUse;
	private Double freshwaterEcotoxicity;
	private Double waterUse;
	private Double resourceUseFossils;
	private Double resourceUseMineralsMetal;
	private Double humanToxicityNonCancer;
	private Double humanToxicityCancer;

	/**
	 * <p>Constructor for LCAData.</p>
	 *
	 * @param id a {@link java.lang.String} object
	 * @param value a {@link java.lang.String} object
	 * @param score a {@link java.lang.Double} object
	 * @param climateChange a {@link java.lang.Double} object
	 * @param particulateMatter a {@link java.lang.Double} object
	 * @param waterUse a {@link java.lang.Double} object
	 * @param landUse a {@link java.lang.Double} object
	 * @param resourceUseMineralsMetal a {@link java.lang.Double} object
	 * @param ozoneDepletion a {@link java.lang.Double} object
	 * @param acidification a {@link java.lang.Double} object
	 * @param ionizingRadiation a {@link java.lang.Double} object
	 * @param photochemicalOzoneFormation a {@link java.lang.Double} object
	 * @param terrestrialEutrophication a {@link java.lang.Double} object
	 * @param marineEutrophication a {@link java.lang.Double} object
	 * @param freshwaterEutrophication a {@link java.lang.Double} object
	 * @param freshwaterEcotoxicity a {@link java.lang.Double} object
	 * @param humanToxicityCancer a {@link java.lang.Double} object
	 * @param humanToxicityNonCancer a {@link java.lang.Double} object
	 * @param resourceUseFossils a {@link java.lang.Double} object
	 */
	public LCAData(String id, String value, Double score, Double climateChange,
			Double particulateMatter, Double waterUse, Double landUse, Double resourceUseMineralsMetal,
			Double ozoneDepletion, Double acidification, Double ionizingRadiation,
			Double photochemicalOzoneFormation, Double terrestrialEutrophication, Double marineEutrophication,
			Double freshwaterEutrophication, Double freshwaterEcotoxicity, Double humanToxicityCancer,
			Double humanToxicityNonCancer, Double resourceUseFossils) {
		super();
		this.id = id;
		this.value = value;
		this.score = score;
		this.climateChange = climateChange;
		this.ozoneDepletion = ozoneDepletion;
		this.ionizingRadiation = ionizingRadiation;
		this.photochemicalOzoneFormation = photochemicalOzoneFormation;
		this.particulateMatter = particulateMatter;
		this.acidification = acidification;
		this.terrestrialEutrophication = terrestrialEutrophication;
		this.freshwaterEutrophication = freshwaterEutrophication;
		this.marineEutrophication = marineEutrophication;
		this.landUse = landUse;
		this.freshwaterEcotoxicity = freshwaterEcotoxicity;
		this.waterUse = waterUse;
		this.resourceUseFossils = resourceUseFossils;
		this.resourceUseMineralsMetal = resourceUseMineralsMetal;
		this.humanToxicityNonCancer = humanToxicityNonCancer;
		this.humanToxicityCancer = humanToxicityCancer;
	}

	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <p>Getter for the field <code>score</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getScore() {
		return score;
	}

	/**
	 * <p>Getter for the field <code>climateChange</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getClimateChange() {
		return climateChange;
	}

	/**
	 * <p>Getter for the field <code>ozoneDepletion</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getOzoneDepletion() {
		return ozoneDepletion;
	}

	/**
	 * <p>Getter for the field <code>ionizingRadiation</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getIonizingRadiation() {
		return ionizingRadiation;
	}

	/**
	 * <p>Getter for the field <code>photochemicalOzoneFormation</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getPhotochemicalOzoneFormation() {
		return photochemicalOzoneFormation;
	}

	/**
	 * <p>Getter for the field <code>particulateMatter</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getParticulateMatter() {
		return particulateMatter;
	}

	/**
	 * <p>Getter for the field <code>acidification</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getAcidification() {
		return acidification;
	}

	/**
	 * <p>Getter for the field <code>terrestrialEutrophication</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getTerrestrialEutrophication() {
		return terrestrialEutrophication;
	}

	/**
	 * <p>Getter for the field <code>freshwaterEutrophication</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getFreshwaterEutrophication() {
		return freshwaterEutrophication;
	}

	/**
	 * <p>Getter for the field <code>marineEutrophication</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMarineEutrophication() {
		return marineEutrophication;
	}

	/**
	 * <p>Getter for the field <code>landUse</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getLandUse() {
		return landUse;
	}

	/**
	 * <p>Getter for the field <code>freshwaterEcotoxicity</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getFreshwaterEcotoxicity() {
		return freshwaterEcotoxicity;
	}

	/**
	 * <p>Getter for the field <code>waterUse</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getWaterUse() {
		return waterUse;
	}

	/**
	 * <p>Getter for the field <code>resourceUseFossils</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getResourceUseFossils() {
		return resourceUseFossils;
	}

	/**
	 * <p>Getter for the field <code>resourceUseMineralsMetal</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getResourceUseMineralsMetal() {
		return resourceUseMineralsMetal;
	}

	/**
	 * <p>Getter for the field <code>humanToxicityNonCancer</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getHumanToxicityNonCancer() {
		return humanToxicityNonCancer;
	}

	/**
	 * <p>Getter for the field <code>humanToxicityCancer</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getHumanToxicityCancer() {
		return humanToxicityCancer;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return id + " - " + value;
	}
}
