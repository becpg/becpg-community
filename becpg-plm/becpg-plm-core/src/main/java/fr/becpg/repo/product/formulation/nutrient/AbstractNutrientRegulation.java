package fr.becpg.repo.product.formulation.nutrient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.common.csv.CSVReader;

public abstract class AbstractNutrientRegulation {

	protected class NutrientDefinition {
		private Integer sort;
		private Integer depthLevel;
		private Boolean mandatory;
		private Boolean optionnal;
		private Boolean bold;
		private Double gda;
		private Double ul;

		public Integer getDepthLevel() {
			return depthLevel;
		}

		public NutrientDefinition(String nutCode, Integer sort, Integer depthLevel, Boolean mandatory, Boolean optionnal, Boolean bold, Double gda,
				Double ul) {
			super();
			this.sort = sort;
			this.depthLevel = depthLevel;
			this.mandatory = mandatory;
			this.optionnal = optionnal;
			this.bold = bold;
			this.gda = gda;
			this.ul = ul;
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

		public Boolean getOptionnal() {
			return optionnal;
		}

		public Double getGda() {
			return gda;
		}

		public Double getUl() {
			return ul;
		}

	}

	protected Map<String, NutrientDefinition> definitions = new LinkedHashMap<>();

	public AbstractNutrientRegulation(String path) {
		ClassPathResource resource = new ClassPathResource(path);
		try (InputStream in = resource.getInputStream()) {
			try (InputStreamReader inReader = new InputStreamReader(resource.getInputStream())) {
				try (CSVReader csvReader = new CSVReader(inReader, ';','"',1)) {
					String[] line = null;
					// nutCode charactName sort depthLevel mandatory optionnal bold gda ul
					while ((line = csvReader.readNext()) != null) {
						definitions.put(line[0], new NutrientDefinition(line[0], parseInt(line[2]), parseInt(line[3]), "true".equals(line[4]),
								"true".equals(line[5]), "true".equals(line[6]), parseDouble(line[7]), parseDouble(line[8])));
					}

				}
			}
		} catch (IOException e) {
			logger.error(e,e);
		}

	}

	private Double parseDouble(String value) {
		if (value != null && !value.isEmpty()) {
			return Double.valueOf(value);
		}

		return null;
	}

	private Integer parseInt(String value) {
		if (value != null && !value.isEmpty()) {
			return Integer.valueOf(value);
		}

		return null;
	}

	public NutrientDefinition getNutrientDefinition(String nutCode) {
		return definitions.get(nutCode);
	}

	protected static Log logger = LogFactory.getLog(AbstractNutrientRegulation.class);

	public Double round(Double value, String nutrientTypeCode, String nutUnit) {

		if (value == null) {
			return null;
		}
		if ((nutrientTypeCode != null) && !nutrientTypeCode.isEmpty()) {

			if ((nutUnit != null) && nutUnit.equals("mg/100g")) { // convert mg
																	// to g
				value = value / 1000;
				value = roundByCode(value, nutrientTypeCode);
				if (value != null) {
					value = value * 1000;
				}
				return value;
			}
			if ((nutUnit != null) && nutUnit.equals("KJ/100g")) { // convert KJ
																	// to Kcal
				value = value / 4.184;
				value = roundByCode(value, nutrientTypeCode);
				return (double) Math.round((value * 4.184 * 100) / 100);
			}
			return roundByCode(value, nutrientTypeCode);
		}
		return (double) Math.round(value);
	}
	
	

	protected Double nearByDefault(Double value) {
		
		if (value == null) {
			return null;
		} 
		
		if ((value < 10)) {
			return (double) Math.round(10 * value) / 10;
		} 
		return (double) Math.round(value);
		
	}

	/*
	(***) NOTES FOR ROUNDING % Daily Value (DV):

	    To calculate % DV, divide either the actual (unrounded) quantitative amount or the declared (rounded) amount by the appropriate RDI or DRV. Use whichever amount will provide the greatest consistency on the food label and prevent unnecessary consumer confusion 109.9(d)(7)(2).
	    When %DV values fall between two whole numbers, rounding shall be as follows:
	        for values exactly halfway between two whole numbers or higher (e.g., 2.5 to 2.990 the values shall round up (e.g., 3%)
	        for values less than halfway between two whole numbers (e.g., 2.01 to 2.49) the values shall round down (e.g., 2%).
	 */
	public Double roundGDA(Double value) {
		if (value == null) {
			return null;
		} 
		return  (double) Math.round(10 * value) / 10;
	}
	

	protected abstract Double roundByCode(Double value, String nutrientTypeCode);


}
