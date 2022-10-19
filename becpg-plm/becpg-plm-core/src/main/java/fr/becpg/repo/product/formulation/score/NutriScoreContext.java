package fr.becpg.repo.product.formulation.score;

import java.util.Objects;

import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.NutrientProfileCategory;

public class NutriScoreContext {

	public static final String VALUE = "value"; 
	
	private static final String IS_WATER = "isWater";
	private static final String PARTS2 = "parts";
	private static final String SCORE = "score";
	private static final String UPPER_VALUE = "upperValue";
	private static final String LOWER_VALUE = "lowerValue";
	private static final String CATEGORY_STRING = "category";
	private static final String CLASS_UPPER_VALUE = "classUpperValue";
	private static final String CLASS_LOWER_VALUE = "classLowerValue";
	private static final String NUTRIENT_CLASS = "nutrientClass";
	private static final String C_SCORE = "cScore";
	private static final String A_SCORE = "aScore";
	private static final String NUTRI_SCORE = "nutriScore";

	private JSONObject parts = new JSONObject();

	private String category;

	private String nutrientClass;
	private Double classLowerValue;
	private Double classUpperValue;

	private Integer nutriScore;
	private Integer aScore;
	private Integer cScore;
	
	private boolean isWater;

	public NutriScoreContext() {

	}

	public NutriScoreContext(Double energyValue, Double satFatValue, Double totalFatValue, Double totalSugarValue, Double sodiumValue,
			Double percFruitsAndVetgsValue, Double nspFibreValue, Double aoacFibreValue, Double proteinValue, String category) {

		addPartValue(NutriScore.ENERGY_CODE, energyValue);
		addPartValue(NutriScore.SATFAT_CODE, satFatValue);
		addPartValue(NutriScore.FAT_CODE, totalFatValue);
		addPartValue(NutriScore.SUGAR_CODE, totalSugarValue);
		addPartValue(NutriScore.SODIUM_CODE, sodiumValue);
		addPartValue(NutriScore.FRUIT_VEGETABLE_CODE, percFruitsAndVetgsValue);
		addPartValue(NutriScore.NSP_CODE, nspFibreValue);
		addPartValue(NutriScore.AOAC_CODE, aoacFibreValue);
		addPartValue(NutriScore.PROTEIN_CODE, proteinValue);
		
		this.category = category;
	}
	
	private void addPartValue(String code, Double value) {
		JSONObject part = new JSONObject();
		part.put(VALUE, value);
		parts.put(code, part);
	}
	
	public JSONObject getParts() {
		return parts;
	}

	public boolean isWater() {
		return isWater;
	}

	public void setWater(boolean isWater) {
		this.isWater = isWater;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getNutriScore() {
		return nutriScore;
	}

	public String getCategory() {
		return category;
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

	public Integer getAScore() {
		return aScore;
	}

	public Integer getCScore() {
		return cScore;
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
		json.put(PARTS2, parts);
		
		json.put(IS_WATER, isWater);
		
		return json;
	}

	public static NutriScoreContext parse(String nutriScoreDetails) {

		JSONObject jsonValue = new JSONObject(nutriScoreDetails);

		NutriScoreContext nutriScoreContext = new NutriScoreContext();
		
		nutriScoreContext.setNutriScore((int) jsonValue.get(NUTRI_SCORE));
		nutriScoreContext.setAScore((int) jsonValue.get(A_SCORE));
		nutriScoreContext.setCScore((int) jsonValue.get(C_SCORE));
		nutriScoreContext.setCategory(jsonValue.getString(CATEGORY_STRING));
		nutriScoreContext.setNutrientClass(jsonValue.getString(NUTRIENT_CLASS));
		nutriScoreContext.setClassLowerValue(
				"-Inf".equals(jsonValue.get(CLASS_LOWER_VALUE)) ? Double.MIN_VALUE : ((Integer) jsonValue.get(CLASS_LOWER_VALUE)).doubleValue());
		nutriScoreContext.setClassUpperValue(
				"+Inf".equals(jsonValue.get(CLASS_UPPER_VALUE)) ? Double.MAX_VALUE : ((Integer) jsonValue.get(CLASS_UPPER_VALUE)).doubleValue());
		
		
		nutriScoreContext.parts = jsonValue.getJSONObject(PARTS2);
		
		nutriScoreContext.setWater(jsonValue.getBoolean(IS_WATER));

		return nutriScoreContext;

	}

	public String toHtmlDisplayValue() {

		StringBuilder sb = new StringBuilder();
		
		sb.append("@html");
		sb.append("<b>");
		sb.append(I18NUtil.getMessage("nutriscore.display.negative"));
		sb.append("</b>");
		
		sb.append("<ul>");
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.energy", parts.getJSONObject(NutriScore.ENERGY_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.ENERGY_CODE).get(VALUE), parts.getJSONObject(NutriScore.ENERGY_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.ENERGY_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.satfat", parts.getJSONObject(NutriScore.SATFAT_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.SATFAT_CODE).get(VALUE), parts.getJSONObject(NutriScore.SATFAT_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.SATFAT_CODE).get(SCORE)));
		if (NutrientProfileCategory.Fats.equals(NutrientProfileCategory.valueOf(category))) {
			sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.totalfat", parts.getJSONObject(NutriScore.FAT_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.FAT_CODE).get(VALUE), parts.getJSONObject(NutriScore.FAT_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.FAT_CODE).get(SCORE)));
		}
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.totalsugar", parts.getJSONObject(NutriScore.SUGAR_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.SUGAR_CODE).get(VALUE), parts.getJSONObject(NutriScore.SUGAR_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.SUGAR_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.sodium", parts.getJSONObject(NutriScore.SODIUM_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.SODIUM_CODE).get(VALUE), parts.getJSONObject(NutriScore.SODIUM_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.SODIUM_CODE).get(SCORE)));
		sb.append("</ul>");
		
		sb.append("</br>");
		
		sb.append("<b>");
		sb.append(I18NUtil.getMessage("nutriscore.display.positive"));
		sb.append("</b>");
		
		sb.append("<ul>");
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.protein", parts.getJSONObject(NutriScore.PROTEIN_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.PROTEIN_CODE).get(VALUE), parts.getJSONObject(NutriScore.PROTEIN_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.PROTEIN_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.percfruitsandveg", parts.getJSONObject(NutriScore.FRUIT_VEGETABLE_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.FRUIT_VEGETABLE_CODE).get(VALUE), parts.getJSONObject(NutriScore.FRUIT_VEGETABLE_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.FRUIT_VEGETABLE_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.nspfibre", parts.getJSONObject(NutriScore.NSP_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.NSP_CODE).get(VALUE), parts.getJSONObject(NutriScore.NSP_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.NSP_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.aoacfibre", parts.getJSONObject(NutriScore.AOAC_CODE).get(LOWER_VALUE), parts.getJSONObject(NutriScore.AOAC_CODE).get(VALUE), parts.getJSONObject(NutriScore.AOAC_CODE).get(UPPER_VALUE), parts.getJSONObject(NutriScore.AOAC_CODE).get(SCORE)));
		sb.append("</ul>");

		sb.append("</br>");
		
		sb.append("<p>");
		sb.append(I18NUtil.getMessage("nutriscore.display.finalScore", aScore, cScore, nutriScore));
		sb.append("</p>");
		
		sb.append("</br>");

		sb.append("<p>");
		sb.append(I18NUtil.getMessage("nutriscore.display.class", classLowerValue, nutriScore, classUpperValue, nutrientClass));
		sb.append("</p>");
		
		sb.append("<p>");
		
		String aClass = "A".equals(nutrientClass) ? "selected nutrient-class-a" : "nutrient-class-a";
		String bClass = "B".equals(nutrientClass) ? "selected nutrient-class-b" : "nutrient-class-b";
		String cClass = "C".equals(nutrientClass) ? "selected nutrient-class-c" : "nutrient-class-c";
		String dClass = "D".equals(nutrientClass) ? "selected nutrient-class-d" : "nutrient-class-d";
		String eClass = "E".equals(nutrientClass) ? "selected nutrient-class-e" : "nutrient-class-e";
		
        sb.append("<span class=\"" + aClass + "\">A</span>");
        sb.append("<span class=\"" + bClass + "\">B</span>");
        sb.append("<span class=\"" + cClass + "\">C</span>");
        sb.append("<span class=\"" + dClass + "\">D</span>");
        sb.append("<span class=\"" + eClass + "\">E</span>");
		sb.append("</p>");
		
		return sb.toString();
		
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(aScore, cScore, category, classLowerValue, classUpperValue, parts, nutriScore, nutrientClass, isWater);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		NutriScoreContext other = (NutriScoreContext) obj;
		return Objects.equals(aScore, other.aScore) && Objects.equals(cScore, other.cScore)
				&& Objects.equals(category, other.category) && Objects.equals(classLowerValue, other.classLowerValue)
				&& Objects.equals(classUpperValue, other.classUpperValue)
				&& Objects.equals(nutriScore, other.nutriScore)
				&& Objects.equals(nutrientClass, other.nutrientClass) 
				&& Objects.equals(isWater, other.isWater) && Objects.equals(parts, other.parts);
	}

	@Override
	public String toString() {
		return "NutriScoreContext [parts=" + parts + ", category=" + category + ", nutrientClass=" + nutrientClass + ", classLowerValue=" + classLowerValue
				+ ", classUpperValue=" + classUpperValue + ", nutriScore=" + nutriScore + ", aScore=" + aScore + ", cScore=" + cScore + ", isWater=" + isWater + "]";
	}

}
