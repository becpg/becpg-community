package fr.becpg.repo.product.formulation.nutrient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.common.csv.CSVReader;

public abstract class AbstractNutrientRegulation implements NutrientRegulation {

	private String path;
	
	protected static Log logger = LogFactory.getLog(AbstractNutrientRegulation.class);
	
	protected class NutrientDefinition {
		private Integer sort;
		private Integer depthLevel;
		private Boolean mandatory;
		private Boolean optional;
		private Boolean bold;
		private Double gda;
		private Double ul;
		private String unit;

		public Integer getDepthLevel() {
			return depthLevel;
		}

		public NutrientDefinition(String nutCode, Integer sort, Integer depthLevel, Boolean mandatory, Boolean optional, Boolean bold, Double gda,
				Double ul, String unit) {
			super();
			this.sort = sort;
			this.depthLevel = depthLevel;
			this.mandatory = mandatory;
			this.optional = optional;
			this.bold = bold;
			this.gda = gda;
			this.ul = ul;
			this.unit = unit;
		}

		public Integer getSort() {
			return sort;
		}

		public Boolean getBold() {
			return bold;
		}

		public Boolean getMandatory() {
			return mandatory;
		}

		public Boolean getOptional() {
			return optional;
		}

		public Double getGda() {
			return gda;
		}

		public Double getUl() {
			return ul;
		}
		
		public String getUnit() {
			return unit;
		}

	}

	private Map<String, NutrientDefinition> definitions = null;

	public AbstractNutrientRegulation(String path) {
		this.path = path;

	}

	private void loadRegulation() {
		definitions = new LinkedHashMap<>();
		ClassPathResource resource = new ClassPathResource(path);
		try (InputStream in = resource.getInputStream()) {
			try (InputStreamReader inReader = new InputStreamReader(resource.getInputStream())) {
				try (CSVReader csvReader = new CSVReader(inReader, ';','"',1)) {
					String[] line = null;
					// nutCode charactName sort depthLevel mandatory optionnal bold gda ul
					while ((line = csvReader.readNext()) != null) {
						definitions.put(line[0], new NutrientDefinition(line[0], parseInt(line[2]), parseInt(line[3]), "true".equals(line[4]),
								"true".equals(line[5]), "true".equals(line[6]), parseDouble(line[7]), parseDouble(line[8]), line[9]));
					}
				}
			}
		} catch (IOException e) {
			logger.error(e,e);
		}
	}
	
	private Double parseDouble(String value) {
		if (value != null && !value.trim().isEmpty()) {
			return Double.valueOf(value.trim().replace(",", "."));
		}

		return null;
	}

	private Integer parseInt(String value) {
		if (value != null && !value.isEmpty()) {
			return Integer.valueOf(value);
		}

		return null;
	}

	@Override
	public NutrientDefinition getNutrientDefinition(String nutCode) {
		//Defers loading
		if(definitions == null) {
			loadRegulation();
		}
		return definitions.get(nutCode);
	}

	@Override
	public Double round(Double value, String nutrientTypeCode, String nutUnit) {

		if (value == null) {
			return null;
		}
		if (nutrientTypeCode != null && !nutrientTypeCode.isEmpty()) {
			String regulUnit = nutUnit;
			NutrientDefinition def = getNutrientDefinition(nutrientTypeCode);
			if(def != null){
				regulUnit = def.getUnit();
			}
			if(logger.isDebugEnabled()){
				logger.debug("round : nutrientTypeCode " + nutrientTypeCode + " value " + value + " nutUnit " + nutUnit + " regulUnit " + regulUnit);
			}
			return roundByCode(convertValue(value, nutUnit, regulUnit), nutrientTypeCode);
		}
		return roundValue(value, 1d);
	}
	
	@Override
	public String displayValue(Double value, Double roundedValue, String nutrientTypeCode, Locale locale) {
		if (nutrientTypeCode != null && !nutrientTypeCode.isEmpty()) {
			return displayValueByCode(value, roundedValue, nutrientTypeCode, locale);
		}
		return formatDouble(roundedValue, locale);
	}
	
	protected String formatDouble(Double value, Locale locale){
		if(value != null){
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
			DecimalFormat df = new DecimalFormat("#,###.######", symbols);
	        return df.format(value);
		}
		else{
			return null;
		}
	}
	
	protected Double roundValue(Double value, Double delta) {
		if(value != null){
			// round by delta (eg: 0.5)
			double roundedValue = (delta * Math.round(value / delta));
			// sometime roundedValue is 0.3000000001 when 0.1 * 3 --> we round twice
			return (Math.round(roundedValue * 1000d) / 1000d);
		}
		else{
			return null;
		}
	}
	
	protected boolean isVitamin(String nutrientTypeCode){
		if(nutrientTypeCode!=null){
			if(nutrientTypeCode.equals(NutrientCode.FolicAcid)
				|| nutrientTypeCode.equals(NutrientCode.VitA)
				|| nutrientTypeCode.equals(NutrientCode.VitC)
				|| nutrientTypeCode.equals(NutrientCode.VitD)
				|| nutrientTypeCode.equals(NutrientCode.VitE)
				|| nutrientTypeCode.equals(NutrientCode.VitK1)
				|| nutrientTypeCode.equals(NutrientCode.VitK2)
				|| nutrientTypeCode.equals(NutrientCode.VitB1)
				|| nutrientTypeCode.equals(NutrientCode.VitB2)
				|| nutrientTypeCode.equals(NutrientCode.VitB3)
				|| nutrientTypeCode.equals(NutrientCode.PantoAcid)
				|| nutrientTypeCode.equals(NutrientCode.VitB6)
				|| nutrientTypeCode.equals(NutrientCode.VitB12)
				|| nutrientTypeCode.equals(NutrientCode.Thiamin)
				|| nutrientTypeCode.equals(NutrientCode.Riboflavin)
				|| nutrientTypeCode.equals(NutrientCode.Niacin)
				|| nutrientTypeCode.equals(NutrientCode.Folate)
				|| nutrientTypeCode.equals(NutrientCode.FolateDFE)
				|| nutrientTypeCode.equals(NutrientCode.Biotin)
				|| nutrientTypeCode.equals(NutrientCode.Choline)
				|| nutrientTypeCode.equals(NutrientCode.Retinol)
				|| nutrientTypeCode.equals(NutrientCode.BetaCarotene)
				|| nutrientTypeCode.equals(NutrientCode.BetaCrypt)
				|| nutrientTypeCode.equals(NutrientCode.Lycopene)
				|| nutrientTypeCode.equals(NutrientCode.AlphaCarot)
				|| nutrientTypeCode.equals(NutrientCode.ProvitaminA)){
				return true;
			}
		}
		return false;
	}
	
	protected boolean isMineral(String nutrientTypeCode){
		if(nutrientTypeCode!=null){
			if(nutrientTypeCode.equals(NutrientCode.Calcium)
				|| nutrientTypeCode.equals(NutrientCode.Sodium)
				|| nutrientTypeCode.equals(NutrientCode.Potassium)
				|| nutrientTypeCode.equals(NutrientCode.Iron)	
				|| nutrientTypeCode.equals(NutrientCode.Salt)
				|| nutrientTypeCode.equals(NutrientCode.Copper)
				|| nutrientTypeCode.equals(NutrientCode.Phosphorus)
				|| nutrientTypeCode.equals(NutrientCode.Magnesium)
				|| nutrientTypeCode.equals(NutrientCode.Zinc)
				|| nutrientTypeCode.equals(NutrientCode.Iodine)
				|| nutrientTypeCode.equals(NutrientCode.Selenium)
				|| nutrientTypeCode.equals(NutrientCode.Fluoride)
				|| nutrientTypeCode.equals(NutrientCode.Manganese)
				|| nutrientTypeCode.equals(NutrientCode.Chromium)
				|| nutrientTypeCode.equals(NutrientCode.Starch)
				|| nutrientTypeCode.equals(NutrientCode.Molybdenum)
				|| nutrientTypeCode.equals(NutrientCode.Chloride)){
				return true;
			}
		}
		return false;
	}
	
	private Double convertValue(Double value, String nutUnit, String regulUnit){
		
		if (value != null && nutUnit != null && regulUnit != null ) { 
			// convert mg to g
			double coef = 1d;
			if(nutUnit.startsWith("mg") && regulUnit.startsWith("g")){
				coef = 1000;
			}else if(nutUnit.startsWith("µg") && regulUnit.startsWith("g")){
				coef = 1000000;
			}else if(nutUnit.startsWith("µg") && regulUnit.startsWith("mg")){
				coef = 1000;
			}else if(nutUnit.startsWith("g") && regulUnit.startsWith("mg")){
				coef = 0.001;
			}
			else if(nutUnit.startsWith("g") && regulUnit.startsWith("µg")){
				coef = 0.000001;
			}
			else if(nutUnit.startsWith("mg") && regulUnit.startsWith("µg")){
				coef = 0.001;
			}
			if(logger.isDebugEnabled()){
				logger.debug("value " +  value + " coef " + coef);
			}
			return value / coef;
		}
		else{
			return value;
		}
	}

	/*
	(***) NOTES FOR ROUNDING % Daily Value (DV):

	    To calculate % DV, divide either the actual (unrounded) quantitative amount or the declared (rounded) amount by the appropriate RDI or DRV. Use whichever amount will provide the greatest consistency on the food label and prevent unnecessary consumer confusion 109.9(d)(7)(2).
	    When %DV values fall between two whole numbers, rounding shall be as follows:
	        for values exactly halfway between two whole numbers or higher (e.g., 2.5 to 2.990 the values shall round up (e.g., 3%)
	        for values less than halfway between two whole numbers (e.g., 2.01 to 2.49) the values shall round down (e.g., 2%).
	 */
	@Override
	public Double roundGDA(Double value, String nutrientTypeCode) {
		if (value == null) {
			return null;
		}
		return  roundValue(value, 1d);
	}
	

	protected abstract Double roundByCode(Double value, String nutrientTypeCode);
	
	protected abstract String displayValueByCode(Double value, Double roundedValue, String nutrientTypeCode, Locale locale);

}
