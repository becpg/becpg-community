package fr.becpg.repo.product.formulation.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences.NON_FULL_SCREEN_PAGE_MODE;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.NutrientProfileCategory;

public class NutriScoreContext {
	
	private static final String NON_NUTRITIVE_SUGARS = "nonNutritiveSugars";

	public static final List<String> NUTRIENT_PROFILE_CLASSES = Arrays.asList("E","D","C","B","A");

	public static final String VALUE = "value"; 
	private static final String IS_WATER = "isWater";
	private static final String HAS_PROTEIN_SCORE = "hasProteinScore";
	private static final String PARTS_STRING = "parts";
	public static final String SCORE = "score";
	public static final String UPPER_VALUE = "upperValue";
	public static final String LOWER_VALUE = "lowerValue";
	private static final String CATEGORY_STRING = "category";
	private static final String CLASS_UPPER_VALUE = "classUpperValue";
	private static final String CLASS_LOWER_VALUE = "classLowerValue";
	private static final String NUTRIENT_CLASS = "nutrientClass";
	private static final String C_SCORE = "cScore";
	private static final String A_SCORE = "aScore";
	private static final String NUTRI_SCORE = "nutriScore";
	public static final String ENERGY_CODE = "ENER-KJO";
	public static final String SATFAT_CODE = "FASAT";
	public static final String FAT_CODE = "FAT";
	public static final String SUGAR_CODE = "SUGAR";
	public static final String SALT_CODE = "NACL";
	public static final String SODIUM_CODE = "NA";
	public static final String NSP_CODE = "PSACNS";
	public static final String AOAC_CODE = "FIBTG";
	public static final String PROTEIN_CODE = "PRO-";
	
	public static final String FRUIT_VEGETABLE_CODE = "FRUIT_VEGETABLE";
	
	public static final String[] NUTRIENT_CODE_LIST = { ENERGY_CODE, SATFAT_CODE, FAT_CODE, SUGAR_CODE, SALT_CODE, SODIUM_CODE, NSP_CODE, AOAC_CODE, PROTEIN_CODE };
	public static final String[] PHYSICO_CODE_LIST = { FRUIT_VEGETABLE_CODE };


	private JSONObject parts = new JSONObject();

	private String category;

	private String nutrientClass;
	private String classLowerValue;
	private String classUpperValue;

	private Integer nutriScore;
	private Integer aScore;
	private Integer cScore;
	
	private boolean isWater;
	
	private boolean hasProteinScore = false;
	private boolean hasSaltScore = false;
	
	private List<String> nonNutritiveSugars = new ArrayList<>();

	public NutriScoreContext() {
		
		createPartValue(ENERGY_CODE, 0d);
		createPartValue(SATFAT_CODE, 0d);
		createPartValue(FAT_CODE, 0d);
		createPartValue(SUGAR_CODE, 0d);
		createPartValue(SODIUM_CODE, 0d);
		createPartValue(FRUIT_VEGETABLE_CODE, 0d);
		createPartValue(NSP_CODE, 0d);
		createPartValue(AOAC_CODE, 0d);
		createPartValue(PROTEIN_CODE, 0d);
	}

	public NutriScoreContext(Double energyValue, Double satFatValue, Double totalFatValue, Double totalSugarValue, Double sodiumValue,
			Double percFruitsAndVetgsValue, Double nspFibreValue, Double aoacFibreValue, Double proteinValue, String category) {

		createPartValue(ENERGY_CODE, energyValue);
		createPartValue(SATFAT_CODE, satFatValue);
		createPartValue(FAT_CODE, totalFatValue);
		createPartValue(SUGAR_CODE, totalSugarValue);
		createPartValue(SODIUM_CODE, sodiumValue);
		createPartValue(FRUIT_VEGETABLE_CODE, percFruitsAndVetgsValue);
		createPartValue(NSP_CODE, nspFibreValue);
		createPartValue(AOAC_CODE, aoacFibreValue);
		createPartValue(PROTEIN_CODE, proteinValue);
		
		this.category = category;
	}
	
	public List<String> getNonNutritiveSugars() {
		return nonNutritiveSugars;
	}
	
	private void createPartValue(String code, Double value) {
		JSONObject part = new JSONObject();
		part.put(VALUE, value);
		parts.put(code, part);
	}
	
	public JSONObject getParts() {
		return parts;
	}
	
	public boolean hasSaltScore() {
		return hasSaltScore;
	}

	public void setHasSaltScore(boolean hasSaltScore) {
		this.hasSaltScore = hasSaltScore;
	}
	
	public boolean getHasProteinScore() {
		return hasProteinScore;
	}
	
	public void setHasProteinScore(boolean hasProtein) {
		this.hasProteinScore = hasProtein;
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

	public String getClassLowerValue() {
		return classLowerValue;
	}

	public void setClassLowerValue(String classLowerValue) {
		this.classLowerValue = classLowerValue;
	}

	public String getClassUpperValue() {
		return classUpperValue;
	}

	public void setClassUpperValue(String classUpperValue) {
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
		json.put(CLASS_LOWER_VALUE, classLowerValue);
		json.put(CLASS_UPPER_VALUE, classUpperValue);
		json.put(PARTS_STRING, parts);
		
		json.put(IS_WATER, isWater);
		
		json.put(HAS_PROTEIN_SCORE, hasProteinScore);
		
		json.put(NON_NUTRITIVE_SUGARS, nonNutritiveSugars);
		
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
		nutriScoreContext.setClassLowerValue(jsonValue.getString(CLASS_LOWER_VALUE));
		nutriScoreContext.setClassUpperValue(jsonValue.getString(CLASS_UPPER_VALUE));
		
		nutriScoreContext.parts = jsonValue.getJSONObject(PARTS_STRING);
		
		nutriScoreContext.setWater(jsonValue.getBoolean(IS_WATER));
		
		nutriScoreContext.getNonNutritiveSugars().addAll(jsonValue.getJSONArray(NON_NUTRITIVE_SUGARS).toList().stream().map(Object::toString).collect(Collectors.toList()));

		return nutriScoreContext;

	}

	public String toHtmlDisplayValue() {

		StringBuilder sb = new StringBuilder();
		
		sb.append("@html");
		sb.append("<b>");
		sb.append(I18NUtil.getMessage("nutriscore.display.negative"));
		sb.append("</b>");
		
		sb.append("<ul>");
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.energy", parts.getJSONObject(ENERGY_CODE).get(LOWER_VALUE), parts.getJSONObject(ENERGY_CODE).get(VALUE), parts.getJSONObject(ENERGY_CODE).get(UPPER_VALUE), parts.getJSONObject(ENERGY_CODE).get(SCORE)));
		
		if (NutrientProfileCategory.Fats.equals(NutrientProfileCategory.valueOf(category))) {
			sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.totalfat", parts.getJSONObject(FAT_CODE).get(LOWER_VALUE), parts.getJSONObject(FAT_CODE).get(VALUE), parts.getJSONObject(FAT_CODE).get(UPPER_VALUE), parts.getJSONObject(FAT_CODE).get(SCORE)));
		} else {
			sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.satfat", parts.getJSONObject(SATFAT_CODE).get(LOWER_VALUE), parts.getJSONObject(SATFAT_CODE).get(VALUE), parts.getJSONObject(SATFAT_CODE).get(UPPER_VALUE), parts.getJSONObject(SATFAT_CODE).get(SCORE)));
		}
		
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.totalsugar", parts.getJSONObject(SUGAR_CODE).get(LOWER_VALUE), parts.getJSONObject(SUGAR_CODE).get(VALUE), parts.getJSONObject(SUGAR_CODE).get(UPPER_VALUE), parts.getJSONObject(SUGAR_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.sodium", parts.getJSONObject(SODIUM_CODE).get(LOWER_VALUE), parts.getJSONObject(SODIUM_CODE).get(VALUE), parts.getJSONObject(SODIUM_CODE).get(UPPER_VALUE), parts.getJSONObject(SODIUM_CODE).get(SCORE)));
		sb.append("</ul>");
		
		sb.append("</br>");
		
		sb.append("<b>");
		sb.append(I18NUtil.getMessage("nutriscore.display.positive"));
		sb.append("</b>");
		
		sb.append("<ul>");
		if (hasProteinScore) {
			sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.protein", parts.getJSONObject(PROTEIN_CODE).get(LOWER_VALUE), parts.getJSONObject(PROTEIN_CODE).get(VALUE), parts.getJSONObject(PROTEIN_CODE).get(UPPER_VALUE), parts.getJSONObject(PROTEIN_CODE).get(SCORE)));
		}
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.percfruitsandveg", parts.getJSONObject(FRUIT_VEGETABLE_CODE).get(LOWER_VALUE), parts.getJSONObject(FRUIT_VEGETABLE_CODE).get(VALUE), parts.getJSONObject(FRUIT_VEGETABLE_CODE).get(UPPER_VALUE), parts.getJSONObject(FRUIT_VEGETABLE_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.nspfibre", parts.getJSONObject(NSP_CODE).get(LOWER_VALUE), parts.getJSONObject(NSP_CODE).get(VALUE), parts.getJSONObject(NSP_CODE).get(UPPER_VALUE), parts.getJSONObject(NSP_CODE).get(SCORE)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.aoacfibre", parts.getJSONObject(AOAC_CODE).get(LOWER_VALUE), parts.getJSONObject(AOAC_CODE).get(VALUE), parts.getJSONObject(AOAC_CODE).get(UPPER_VALUE), parts.getJSONObject(AOAC_CODE).get(SCORE)));
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
