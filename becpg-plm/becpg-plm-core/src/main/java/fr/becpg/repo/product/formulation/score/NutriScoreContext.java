package fr.becpg.repo.product.formulation.score;

import java.util.Objects;

import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public class NutriScoreContext {

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

	private NutriScoreFrame energy = new NutriScoreFrame();
	private NutriScoreFrame satFat = new NutriScoreFrame();
	private NutriScoreFrame totalFat = new NutriScoreFrame();
	private NutriScoreFrame totalSugar = new NutriScoreFrame();
	private NutriScoreFrame sodium = new NutriScoreFrame();
	private NutriScoreFrame percFruitsAndVetgs = new NutriScoreFrame();
	private NutriScoreFrame nspFibre = new NutriScoreFrame();
	private NutriScoreFrame aoacFibre = new NutriScoreFrame();
	private NutriScoreFrame protein = new NutriScoreFrame();

	private String category;

	private String nutrientClass;
	private Double classLowerValue;
	private Double classUpperValue;

	private Integer nutriScore;
	private Integer aScore;
	private Integer cScore;

	public NutriScoreContext() {

	}

	public NutriScoreContext(Double energyValue, Double satFatValue, Double totalFatValue, Double totalSugarValue, Double sodiumValue,
			Double percFruitsAndVetgsValue, Double nspFibreValue, Double aoacFibreValue, Double proteinValue, String category) {

		this.energy = energyValue == null ? null : new NutriScoreFrame(energyValue);
		this.satFat = satFatValue == null ? null : new NutriScoreFrame(satFatValue);
		this.totalFat = totalFatValue == null ? null : new NutriScoreFrame(totalFatValue);
		this.totalSugar = totalSugarValue == null ? null : new NutriScoreFrame(totalSugarValue);
		this.sodium = sodiumValue == null ? null : new NutriScoreFrame(sodiumValue);
		this.percFruitsAndVetgs = percFruitsAndVetgsValue == null ? null : new NutriScoreFrame(percFruitsAndVetgsValue);
		this.nspFibre = nspFibreValue == null ? null : new NutriScoreFrame(nspFibreValue);
		this.aoacFibre = aoacFibreValue == null ? null : new NutriScoreFrame(aoacFibreValue);
		this.protein = proteinValue == null ? null : new NutriScoreFrame(proteinValue);
		this.category = category;
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

	public JSONObject toJSON() {

		JSONObject json = new JSONObject();

		json.put(NUTRI_SCORE, nutriScore);
		json.put(A_SCORE, aScore);
		json.put(C_SCORE, cScore);

		json.put(CATEGORY_STRING, category);

		json.put(NUTRIENT_CLASS, nutrientClass);
		json.put(CLASS_LOWER_VALUE, classLowerValue == Double.MIN_VALUE ? "-Inf" : classLowerValue.intValue());
		json.put(CLASS_UPPER_VALUE, classUpperValue == Double.MAX_VALUE ? "+Inf" : classUpperValue.intValue());

		json.put(ENERGY_STRING, energy.toJSON());
		json.put(SAT_FAT, satFat.toJSON());
		json.put(TOTAL_FAT, totalFat.toJSON());
		json.put(TOTAL_SUGAR, totalSugar.toJSON());
		json.put(SODIUM_STRING, sodium.toJSON());
		json.put(PERC_FRUITS_AND_VETGS, percFruitsAndVetgs.toJSON());
		json.put(NSP_FIBRE, nspFibre.toJSON());
		json.put(AOAC_FIBRE, aoacFibre.toJSON());
		json.put(PROTEIN_STRING, protein.toJSON());

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

	public String toHtmlDisplayValue() {

		StringBuilder sb = new StringBuilder();
		
		sb.append("@html");
		sb.append("<b>");
		sb.append(I18NUtil.getMessage("nutriscore.display.negative"));
		sb.append("</b>");
		
		sb.append("<ul>");
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.energy", energy.toJSON().get(NutriScoreFrame.LOWER_VALUE), energy.toJSON().get(NutriScoreFrame.VALUE_STRING), energy.toJSON().get(NutriScoreFrame.UPPER_VALUE), energy.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.satfat", satFat.toJSON().get(NutriScoreFrame.LOWER_VALUE), satFat.toJSON().get(NutriScoreFrame.VALUE_STRING), satFat.toJSON().get(NutriScoreFrame.UPPER_VALUE), satFat.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.totalfat", totalFat.toJSON().get(NutriScoreFrame.LOWER_VALUE), totalFat.toJSON().get(NutriScoreFrame.VALUE_STRING), totalFat.toJSON().get(NutriScoreFrame.UPPER_VALUE), totalFat.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.totalsugar", totalSugar.toJSON().get(NutriScoreFrame.LOWER_VALUE), totalSugar.toJSON().get(NutriScoreFrame.VALUE_STRING), totalSugar.toJSON().get(NutriScoreFrame.UPPER_VALUE), totalSugar.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.sodium", sodium.toJSON().get(NutriScoreFrame.LOWER_VALUE), sodium.toJSON().get(NutriScoreFrame.VALUE_STRING), sodium.toJSON().get(NutriScoreFrame.UPPER_VALUE), sodium.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("</ul>");
		
		sb.append("</br>");
		
		sb.append("<b>");
		sb.append(I18NUtil.getMessage("nutriscore.display.positive"));
		sb.append("</b>");
		
		sb.append("<ul>");
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.protein", protein.toJSON().get(NutriScoreFrame.LOWER_VALUE), protein.toJSON().get(NutriScoreFrame.VALUE_STRING), protein.toJSON().get(NutriScoreFrame.UPPER_VALUE), protein.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.percfruitsandveg", percFruitsAndVetgs.toJSON().get(NutriScoreFrame.LOWER_VALUE), percFruitsAndVetgs.toJSON().get(NutriScoreFrame.VALUE_STRING), percFruitsAndVetgs.toJSON().get(NutriScoreFrame.UPPER_VALUE), percFruitsAndVetgs.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.nspfibre", nspFibre.toJSON().get(NutriScoreFrame.LOWER_VALUE), nspFibre.toJSON().get(NutriScoreFrame.VALUE_STRING), nspFibre.toJSON().get(NutriScoreFrame.UPPER_VALUE), nspFibre.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("<li>" + I18NUtil.getMessage("nutriscore.display.aoacfibre", aoacFibre.toJSON().get(NutriScoreFrame.LOWER_VALUE), aoacFibre.toJSON().get(NutriScoreFrame.VALUE_STRING), aoacFibre.toJSON().get(NutriScoreFrame.UPPER_VALUE), aoacFibre.toJSON().get(NutriScoreFrame.SCORE_STRING)));
		sb.append("</ul>");

		sb.append("</br>");
		
		JSONObject contextJson = toJSON();
		
		sb.append("<p>");
		sb.append(I18NUtil.getMessage("nutriscore.display.finalScore", contextJson.get(A_SCORE), contextJson.get(C_SCORE), contextJson.get(NUTRI_SCORE)));
		sb.append("</p>");
		
		sb.append("</br>");

		sb.append("<p>");
		sb.append(I18NUtil.getMessage("nutriscore.display.class", contextJson.get(CLASS_LOWER_VALUE), contextJson.get(NUTRI_SCORE), contextJson.get(CLASS_UPPER_VALUE), contextJson.getString(NUTRIENT_CLASS)));
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
		return Objects.hash(aScore, aoacFibre, cScore, category, classLowerValue, classUpperValue, energy, nspFibre, nutriScore, nutrientClass,
				percFruitsAndVetgs, protein, satFat, sodium, totalFat, totalSugar);
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
		return Objects.equals(aScore, other.aScore) && Objects.equals(aoacFibre, other.aoacFibre) && Objects.equals(cScore, other.cScore)
				&& Objects.equals(category, other.category) && Objects.equals(classLowerValue, other.classLowerValue)
				&& Objects.equals(classUpperValue, other.classUpperValue) && Objects.equals(energy, other.energy)
				&& Objects.equals(nspFibre, other.nspFibre) && Objects.equals(nutriScore, other.nutriScore)
				&& Objects.equals(nutrientClass, other.nutrientClass) && Objects.equals(percFruitsAndVetgs, other.percFruitsAndVetgs)
				&& Objects.equals(protein, other.protein) && Objects.equals(satFat, other.satFat) && Objects.equals(sodium, other.sodium)
				&& Objects.equals(totalFat, other.totalFat) && Objects.equals(totalSugar, other.totalSugar);
	}

	@Override
	public String toString() {
		return "NutriScoreContext [energy=" + energy + ", satFat=" + satFat + ", totalFat=" + totalFat + ", totalSugar=" + totalSugar + ", sodium="
				+ sodium + ", percFruitsAndVetgs=" + percFruitsAndVetgs + ", nspFibre=" + nspFibre + ", aoacFibre=" + aoacFibre + ", protein="
				+ protein + ", category=" + category + ", nutrientClass=" + nutrientClass + ", classLowerValue=" + classLowerValue
				+ ", classUpperValue=" + classUpperValue + ", nutriScore=" + nutriScore + ", aScore=" + aScore + ", cScore=" + cScore + "]";
	}

}
