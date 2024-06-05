package fr.becpg.repo.product.formulation.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.NutrientProfileCategory;

/**
 * <p>NutriScoreContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutriScoreContext {
	
	private static final String NON_NUTRITIVE_SUGARS = "nonNutritiveSugars";

	/** Constant <code>NUTRIENT_PROFILE_CLASSES</code> */
	public static final List<String> NUTRIENT_PROFILE_CLASSES = Arrays.asList("E","D","C","B","A");

	/** Constant <code>VALUE="value"</code> */
	public static final String VALUE = "value"; 
	private static final String IS_WATER = "isWater";
	private static final String HAS_PROTEIN_SCORE = "hasProteinScore";
	private static final String DISPLAY_SALT_SCORE = "displaySaltScore";
	private static final String PARTS_STRING = "parts";
	/** Constant <code>SCORE="score"</code> */
	public static final String SCORE = "score";
	/** Constant <code>UPPER_VALUE="upperValue"</code> */
	public static final String UPPER_VALUE = "upperValue";
	/** Constant <code>LOWER_VALUE="lowerValue"</code> */
	public static final String LOWER_VALUE = "lowerValue";
	/** Constant <code>INCLUDE_LOWER="includeLower"</code> */
	public static final String INCLUDE_LOWER = "includeLower";
	private static final String CATEGORY_STRING = "category";
	private static final String CLASS_UPPER_VALUE = "classUpperValue";
	private static final String CLASS_LOWER_VALUE = "classLowerValue";
	private static final String NUTRIENT_CLASS = "nutrientClass";
	private static final String C_SCORE = "cScore";
	private static final String A_SCORE = "aScore";
	private static final String NUTRI_SCORE = "nutriScore";
	/** Constant <code>ENERGY_CODE="ENER-KJO"</code> */
	public static final String ENERGY_CODE = "ENER-KJO";
	/** Constant <code>SATFAT_CODE="FASAT"</code> */
	public static final String SATFAT_CODE = "FASAT";
	/** Constant <code>FAT_CODE="FAT"</code> */
	public static final String FAT_CODE = "FAT";
	/** Constant <code>SUGAR_CODE="SUGAR"</code> */
	public static final String SUGAR_CODE = "SUGAR";
	/** Constant <code>SALT_CODE="NACL"</code> */
	public static final String SALT_CODE = "NACL";
	/** Constant <code>SODIUM_CODE="NA"</code> */
	public static final String SODIUM_CODE = "NA";
	/** Constant <code>NSP_CODE="PSACNS"</code> */
	public static final String NSP_CODE = "PSACNS";
	/** Constant <code>AOAC_CODE="FIBTG"</code> */
	public static final String AOAC_CODE = "FIBTG";
	/** Constant <code>PROTEIN_CODE="PRO-"</code> */
	public static final String PROTEIN_CODE = "PRO-";
	
	/** Constant <code>FRUIT_VEGETABLE_CODE="FRUIT_VEGETABLE"</code> */
	public static final String FRUIT_VEGETABLE_CODE = "FRUIT_VEGETABLE";
	
	/** Constant <code>NUTRIENT_CODE_LIST</code> */
	public static final String[] NUTRIENT_CODE_LIST = { ENERGY_CODE, SATFAT_CODE, FAT_CODE, SUGAR_CODE, SALT_CODE, SODIUM_CODE, NSP_CODE, AOAC_CODE, PROTEIN_CODE };
	/** Constant <code>PHYSICO_CODE_LIST</code> */
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
	private boolean displaySaltScore = false;
	
	private List<String> nonNutritiveSugars = new ArrayList<>();
	
	private String version;

	/**
	 * <p>Constructor for NutriScoreContext.</p>
	 */
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

	/**
	 * <p>Constructor for NutriScoreContext.</p>
	 *
	 * @param energyValue a {@link java.lang.Double} object
	 * @param satFatValue a {@link java.lang.Double} object
	 * @param totalFatValue a {@link java.lang.Double} object
	 * @param totalSugarValue a {@link java.lang.Double} object
	 * @param sodiumValue a {@link java.lang.Double} object
	 * @param percFruitsAndVetgsValue a {@link java.lang.Double} object
	 * @param nspFibreValue a {@link java.lang.Double} object
	 * @param aoacFibreValue a {@link java.lang.Double} object
	 * @param proteinValue a {@link java.lang.Double} object
	 * @param category a {@link java.lang.String} object
	 */
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
	
	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * <p>Setter for the field <code>version</code>.</p>
	 *
	 * @param version a {@link java.lang.String} object
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * <p>Getter for the field <code>nonNutritiveSugars</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<String> getNonNutritiveSugars() {
		return nonNutritiveSugars;
	}
	
	private void createPartValue(String code, Double value) {
		JSONObject part = new JSONObject();
		part.put(VALUE, value);
		parts.put(code, part);
	}
	
	/**
	 * <p>Getter for the field <code>parts</code>.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
	public JSONObject getParts() {
		return parts;
	}
	
	/**
	 * <p>Setter for the field <code>displaySaltScore</code>.</p>
	 *
	 * @param displaySaltScore a boolean
	 */
	public void setDisplaySaltScore(boolean displaySaltScore) {
		this.displaySaltScore = displaySaltScore;
	}
	
	/**
	 * <p>Getter for the field <code>hasProteinScore</code>.</p>
	 *
	 * @return a boolean
	 */
	public boolean getHasProteinScore() {
		return hasProteinScore;
	}
	
	/**
	 * <p>Setter for the field <code>hasProteinScore</code>.</p>
	 *
	 * @param hasProtein a boolean
	 */
	public void setHasProteinScore(boolean hasProtein) {
		this.hasProteinScore = hasProtein;
	}
	
	/**
	 * <p>isWater.</p>
	 *
	 * @return a boolean
	 */
	public boolean isWater() {
		return isWater;
	}

	/**
	 * <p>setWater.</p>
	 *
	 * @param isWater a boolean
	 */
	public void setWater(boolean isWater) {
		this.isWater = isWater;
	}

	/**
	 * <p>Setter for the field <code>category</code>.</p>
	 *
	 * @param category a {@link java.lang.String} object
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * <p>Getter for the field <code>nutriScore</code>.</p>
	 *
	 * @return a int
	 */
	public int getNutriScore() {
		return nutriScore;
	}

	/**
	 * <p>Getter for the field <code>category</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * <p>Setter for the field <code>nutriScore</code>.</p>
	 *
	 * @param nutriScore a int
	 */
	public void setNutriScore(int nutriScore) {
		this.nutriScore = nutriScore;
	}

	/**
	 * <p>Setter for the field <code>nutrientClass</code>.</p>
	 *
	 * @param nutrientClass a {@link java.lang.String} object
	 */
	public void setNutrientClass(String nutrientClass) {
		this.nutrientClass = nutrientClass;
	}

	/**
	 * <p>Getter for the field <code>nutrientClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getNutrientClass() {
		return nutrientClass;
	}

	/**
	 * <p>Getter for the field <code>classLowerValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getClassLowerValue() {
		return classLowerValue;
	}

	/**
	 * <p>Setter for the field <code>classLowerValue</code>.</p>
	 *
	 * @param classLowerValue a {@link java.lang.String} object
	 */
	public void setClassLowerValue(String classLowerValue) {
		this.classLowerValue = classLowerValue;
	}

	/**
	 * <p>Getter for the field <code>classUpperValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getClassUpperValue() {
		return classUpperValue;
	}

	/**
	 * <p>Setter for the field <code>classUpperValue</code>.</p>
	 *
	 * @param classUpperValue a {@link java.lang.String} object
	 */
	public void setClassUpperValue(String classUpperValue) {
		this.classUpperValue = classUpperValue;
	}

	/**
	 * <p>Setter for the field <code>aScore</code>.</p>
	 *
	 * @param aScore a {@link java.lang.Integer} object
	 */
	public void setAScore(Integer aScore) {
		this.aScore = aScore;
	}

	/**
	 * <p>Setter for the field <code>cScore</code>.</p>
	 *
	 * @param cScore a {@link java.lang.Integer} object
	 */
	public void setCScore(Integer cScore) {
		this.cScore = cScore;
	}

	/**
	 * <p>Getter for the field <code>aScore</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getAScore() {
		return aScore;
	}

	/**
	 * <p>Getter for the field <code>cScore</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getCScore() {
		return cScore;
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
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
		json.put(DISPLAY_SALT_SCORE, displaySaltScore);
		
		json.put(NON_NUTRITIVE_SUGARS, new JSONArray(nonNutritiveSugars));
		
		return json;
	}

	/**
	 * <p>parse.</p>
	 *
	 * @param nutriScoreDetails a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.formulation.score.NutriScoreContext} object
	 */
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
		nutriScoreContext.setDisplaySaltScore(jsonValue.getBoolean(DISPLAY_SALT_SCORE));
		nutriScoreContext.setHasProteinScore(jsonValue.getBoolean(HAS_PROTEIN_SCORE));
		nutriScoreContext.getNonNutritiveSugars().addAll(jsonValue.getJSONArray(NON_NUTRITIVE_SUGARS).toList().stream().map(Object::toString).collect(Collectors.toList()));

		return nutriScoreContext;

	}

	/**
	 * <p>toHtmlDisplayValue.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
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
		String saltSodiumKey = displaySaltScore ? "nutriscore.display.salt" : "nutriscore.display.sodium";
		sb.append("<li>" + I18NUtil.getMessage(saltSodiumKey, parts.getJSONObject(SODIUM_CODE).get(LOWER_VALUE), parts.getJSONObject(SODIUM_CODE).get(VALUE), parts.getJSONObject(SODIUM_CODE).get(UPPER_VALUE), parts.getJSONObject(SODIUM_CODE).get(SCORE)));
		
		if (!nonNutritiveSugars.isEmpty()) {
			sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.nns", nonNutritiveSugars));
		}
		
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
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(aScore, cScore, category, classLowerValue, classUpperValue, parts, nutriScore, nutrientClass, isWater);
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NutriScoreContext [parts=" + parts + ", category=" + category + ", nutrientClass=" + nutrientClass + ", classLowerValue=" + classLowerValue
				+ ", classUpperValue=" + classUpperValue + ", nutriScore=" + nutriScore + ", aScore=" + aScore + ", cScore=" + cScore + ", isWater=" + isWater + "]";
	}

}
