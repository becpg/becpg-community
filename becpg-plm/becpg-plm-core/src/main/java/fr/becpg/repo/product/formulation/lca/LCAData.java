package fr.becpg.repo.product.formulation.lca;

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

	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public Double getScore() {
		return score;
	}

	public Double getClimateChange() {
		return climateChange;
	}

	public Double getOzoneDepletion() {
		return ozoneDepletion;
	}

	public Double getIonizingRadiation() {
		return ionizingRadiation;
	}

	public Double getPhotochemicalOzoneFormation() {
		return photochemicalOzoneFormation;
	}

	public Double getParticulateMatter() {
		return particulateMatter;
	}

	public Double getAcidification() {
		return acidification;
	}

	public Double getTerrestrialEutrophication() {
		return terrestrialEutrophication;
	}

	public Double getFreshwaterEutrophication() {
		return freshwaterEutrophication;
	}

	public Double getMarineEutrophication() {
		return marineEutrophication;
	}

	public Double getLandUse() {
		return landUse;
	}

	public Double getFreshwaterEcotoxicity() {
		return freshwaterEcotoxicity;
	}

	public Double getWaterUse() {
		return waterUse;
	}

	public Double getResourceUseFossils() {
		return resourceUseFossils;
	}

	public Double getResourceUseMineralsMetal() {
		return resourceUseMineralsMetal;
	}

	public Double getHumanToxicityNonCancer() {
		return humanToxicityNonCancer;
	}

	public Double getHumanToxicityCancer() {
		return humanToxicityCancer;
	}

	@Override
	public String toString() {
		return id + " - " + value;
	}
}
