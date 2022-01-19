package fr.becpg.repo.product.formulation.score;

import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public class NutriScoreContext {

	private static final String DISPLAY_VALUE = "displayValue";
	private static final String VALUE = "value";
	private static final String PROTEIN_STRING = "protein";
	private static final String AOAC_FIBRE = "aoacFibre";
	private static final String NSP_FIBRE = "nspFibre";
	private static final String PERC_FRUITS_AND_VETGS = "percFruitsAndVetgs";
	private static final String SODIUM_STRING = "sodium";
	private static final String TOTAL_SUGAR = "totalSugar";
	private static final String TOTAL_FAT = "totalFat";
	private static final String SAT_FAT = "satFat";
	private static final String ENERGY_STRING = "energy";
	private static final String CATEGORY_STRING = "category";
	private static final String CLASS_UPPER_VALUE = "classUpperValue";
	private static final String CLASS_LOWER_VALUE = "classLowerValue";
	private static final String NUTRIENT_CLASS = "nutrientClass";
	private static final String C_SCORE = "cScore";
	private static final String A_SCORE = "aScore";
	private static final String NUTRI_SCORE = "nutriScore";
	
	private NutriScoreFrame energy;
	private NutriScoreFrame satFat;
	private NutriScoreFrame totalFat;
	private NutriScoreFrame totalSugar;
	private NutriScoreFrame sodium;
	private NutriScoreFrame percFruitsAndVetgs;
	private NutriScoreFrame nspFibre;
	private NutriScoreFrame aoacFibre;
	private NutriScoreFrame protein;
	
	private String category;

	private String nutrientClass;
	private Double classLowerValue;
	private Double classUpperValue;
	
	private Integer nutriScore;
	private Integer aScore;
	private Integer cScore;
	
	public NutriScoreContext() {
		
	}

	// for JUnit tests only
	public NutriScoreContext(Double energyValue, Double satFatValue, Double totalFatValue, Double totalSugarValue, Double sodiumValue, Double percFruitsAndVetgsValue,
			Double nspFibreValue, Double aoacFibreValue, Double proteinValue, String category) {
		
		energy = energyValue == null ? null : new NutriScoreFrame(energyValue);
		satFat = satFatValue == null ? null : new NutriScoreFrame(satFatValue);
		totalFat = totalFatValue == null ? null : new NutriScoreFrame(totalFatValue);
		totalSugar = totalSugarValue == null ? null : new NutriScoreFrame(totalSugarValue);
		sodium = sodiumValue == null ? null : new NutriScoreFrame(sodiumValue);
		percFruitsAndVetgs = percFruitsAndVetgsValue == null ? null : new NutriScoreFrame(percFruitsAndVetgsValue);
		nspFibre = nspFibreValue == null ? null : new NutriScoreFrame(nspFibreValue);
		aoacFibre = aoacFibreValue == null ? null : new NutriScoreFrame(aoacFibreValue);
		protein = proteinValue == null ? null : new NutriScoreFrame(proteinValue);
		
		this.category = category;
	}

	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		
		json.put(NUTRI_SCORE, nutriScore);
		json.put(A_SCORE, aScore);
		json.put(C_SCORE, cScore);
		
		json.put(CATEGORY_STRING, category);
		
		json.put(NUTRIENT_CLASS, nutrientClass);
		json.put(CLASS_LOWER_VALUE, classLowerValue == Double.MIN_VALUE ? "-Inf" : classLowerValue.intValue());
		json.put(CLASS_UPPER_VALUE, classUpperValue == Double.MAX_VALUE ? "+Inf" : classUpperValue.intValue());
		
		json.put(ENERGY_STRING, energy == null ? new JSONObject() : energy.toJSON());
		json.put(SAT_FAT, satFat == null ? new JSONObject() : satFat.toJSON());
		json.put(TOTAL_FAT, totalFat == null ? new JSONObject() : totalFat.toJSON());
		json.put(TOTAL_SUGAR, totalSugar == null ? new JSONObject() : totalSugar.toJSON());
		json.put(SODIUM_STRING, sodium == null ? new JSONObject() : sodium.toJSON());
		json.put(PERC_FRUITS_AND_VETGS, percFruitsAndVetgs == null ? new JSONObject() : percFruitsAndVetgs.toJSON());
		json.put(NSP_FIBRE, nspFibre == null ? new JSONObject() : nspFibre.toJSON());
		json.put(AOAC_FIBRE, aoacFibre == null ? new JSONObject() : aoacFibre.toJSON());
		json.put(PROTEIN_STRING, protein == null ? new JSONObject() : protein.toJSON());
		
		return json;
	}
	
	public static NutriScoreContext parse(String nutriScoreDetails) {
		
		JSONObject jsonValue = new JSONObject(nutriScoreDetails).getJSONObject(VALUE);
		
		NutriScoreContext nutriScoreContext = new NutriScoreContext();
		
		nutriScoreContext.setNutriScore((int) jsonValue.get(NUTRI_SCORE));
		nutriScoreContext.setAScore((int) jsonValue.get(A_SCORE));
		nutriScoreContext.setCScore((int) jsonValue.get(C_SCORE));
		nutriScoreContext.setCategory(jsonValue.getString(CATEGORY_STRING));
		nutriScoreContext.setNutrientClass(jsonValue.getString(NUTRIENT_CLASS));
		nutriScoreContext.setClassLowerValue("-Inf".equals(jsonValue.get(CLASS_LOWER_VALUE)) ? Double.MIN_VALUE : ((Integer) jsonValue.get(CLASS_LOWER_VALUE)).doubleValue());
		nutriScoreContext.setClassUpperValue("+Inf".equals(jsonValue.get(CLASS_UPPER_VALUE)) ? Double.MAX_VALUE : ((Integer) jsonValue.get(CLASS_UPPER_VALUE)).doubleValue());
		nutriScoreContext.setEnergy(NutriScoreFrame.parse(jsonValue.get(ENERGY_STRING)));
		nutriScoreContext.setSatFat(NutriScoreFrame.parse(jsonValue.get(SAT_FAT)));
		nutriScoreContext.setTotalFat(NutriScoreFrame.parse(jsonValue.get(TOTAL_FAT)));
		nutriScoreContext.setTotalSugar(NutriScoreFrame.parse(jsonValue.get(TOTAL_SUGAR)));
		nutriScoreContext.setSodium(NutriScoreFrame.parse(jsonValue.get(SODIUM_STRING)));
		nutriScoreContext.setNspFibre(NutriScoreFrame.parse(jsonValue.get(NSP_FIBRE)));
		nutriScoreContext.setAoacFibre(NutriScoreFrame.parse(jsonValue.get(AOAC_FIBRE)));
		nutriScoreContext.setProtein(NutriScoreFrame.parse(jsonValue.get(PROTEIN_STRING)));
		nutriScoreContext.setPercFruitsAndVetgs(NutriScoreFrame.parse(jsonValue.get(PERC_FRUITS_AND_VETGS)));
		
		return nutriScoreContext;
		
	}

	public void setTotalFat(NutriScoreFrame totalFat) {
		this.totalFat = totalFat;
	}

	public void setTotalSugar(NutriScoreFrame totalSugar) {
		this.totalSugar = totalSugar;
	}

	public void setSodium(NutriScoreFrame sodium) {
		this.sodium = sodium;
	}

	public void setPercFruitsAndVetgs(NutriScoreFrame percFruitsAndVetgs) {
		this.percFruitsAndVetgs = percFruitsAndVetgs;
	}

	public void setNspFibre(NutriScoreFrame nspFibre) {
		this.nspFibre = nspFibre;
	}

	public void setAoacFibre(NutriScoreFrame aoacFibre) {
		this.aoacFibre = aoacFibre;
	}

	public void setProtein(NutriScoreFrame protein) {
		this.protein = protein;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getNutriScore() {
		return nutriScore;
	}

	public NutriScoreFrame getEnergy() {
		return energy;
	}

	public NutriScoreFrame getSatFat() {
		return satFat;
	}

	public NutriScoreFrame getTotalFat() {
		return totalFat;
	}

	public NutriScoreFrame getTotalSugar() {
		return totalSugar;
	}

	public NutriScoreFrame getSodium() {
		return sodium;
	}

	public NutriScoreFrame getPercFruitsAndVetgs() {
		return percFruitsAndVetgs;
	}

	public NutriScoreFrame getNspFibre() {
		return nspFibre;
	}

	public NutriScoreFrame getAoacFibre() {
		return aoacFibre;
	}

	public NutriScoreFrame getProtein() {
		return protein;
	}

	public String getCategory() {
		return category;
	}

	public String toDisplayValue() {

		StringBuilder sb = new StringBuilder();

		JSONObject contextJson = toJSON();
		
		sb.append(I18NUtil.getMessage("nutriscore.display.negative"));
		sb.append("\n");

		appendFrame(sb, energy, "nutriscore.display.energy");
		appendFrame(sb, satFat, "nutriscore.display.satfat");
		appendFrame(sb, totalFat, "nutriscore.display.totalfat");
		appendFrame(sb, totalSugar, "nutriscore.display.totalsugar");
		appendFrame(sb, sodium, "nutriscore.display.sodium");

		sb.append("\n");
		sb.append(I18NUtil.getMessage("nutriscore.display.positive"));
		sb.append("\n");

		appendFrame(sb, protein, "nutriscore.display.protein");
		appendFrame(sb, percFruitsAndVetgs, "nutriscore.display.percfruitsandveg");
		appendFrame(sb, nspFibre, "nutriscore.display.nspfibre");
		appendFrame(sb, aoacFibre, "nutriscore.display.aoacfibre");

		sb.append("\n");
		sb.append(I18NUtil.getMessage("nutriscore.display.finalScore", contextJson.get(A_SCORE), contextJson.get(C_SCORE), contextJson.get(NUTRI_SCORE)));
		sb.append("\n");
		
		sb.append("\n");
		sb.append(I18NUtil.getMessage("nutriscore.display.class", contextJson.get(CLASS_LOWER_VALUE), contextJson.get(NUTRI_SCORE), contextJson.get(CLASS_UPPER_VALUE), contextJson.getString(NUTRIENT_CLASS)));
		
		return sb.toString();
	}
	
	private void appendFrame(StringBuilder sb, NutriScoreFrame frame, String messageKey) {
		if (frame != null && frame.getValue() != null && frame.getLowerValue() != null && frame.getUpperValue() != null) {
			
			JSONObject frameJson = frame.toJSON();
			
			sb.append(I18NUtil.getMessage(messageKey, frameJson.get("lowerValue"), frameJson.get(VALUE), frameJson.get("upperValue"), frameJson.get("score")));
			sb.append("\n");
		}
	}

	public void setNutriScore(int nutriScore) {
		this.nutriScore = nutriScore;
	}

	public void setNutrientClass(String nutrientClass) {
		this.nutrientClass = nutrientClass;
	}
	
	public String getNutrientClass() {
		return nutrientClass;
	}

	public Double getClassLowerValue() {
		return classLowerValue;
	}

	public void setClassLowerValue(Double classLowerValue) {
		this.classLowerValue = classLowerValue;
	}

	public Double getClassUpperValue() {
		return classUpperValue;
	}

	public void setClassUpperValue(Double classUpperValue) {
		this.classUpperValue = classUpperValue;
	}

	public void setAScore(Integer aScore) {
		this.aScore = aScore;
	}

	public void setCScore(Integer cScore) {
		this.cScore = cScore;
	}

	public void setEnergy(NutriScoreFrame energy) {
		this.energy = energy;
	}

	public void setSatFat(NutriScoreFrame satFat) {
		this.satFat = satFat;
	}
	
	public Integer getAScore() {
		return aScore;
	}
	
	public Integer getCScore() {
		return cScore;
	}

	public String buildNutrientDetails() {
		
		JSONObject nutrientScoreDetails = new JSONObject();
		nutrientScoreDetails.put(VALUE, toJSON());
		nutrientScoreDetails.put(DISPLAY_VALUE, toDisplayValue());

		return nutrientScoreDetails.toString();
	}
}
