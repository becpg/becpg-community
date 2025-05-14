package fr.becpg.repo.product.formulation.ecoscore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import fr.becpg.common.csv.CSVReader;

/**
 * <p>EcoScoreService class.</p>
 *
 * @author matthieu
 */
@Service
public class EcoScoreService {
	private static final Log LOGGER = LogFactory.getLog(EcoScoreService.class);

	private static final String DEFAULT_AGRIBALYSE_PATH = "beCPG/databases/ecoscore/agribalyse_3_2.csv";
	private static final String DEFAULT_COUNTRY_SCORE_PATH = "beCPG/databases/ecoscore/country_score_2021.csv";
	private static final String DEFAULT_COUNTRY_POSITION_PATH = "beCPG/databases/ecoscore/countries.csv";
	private static final char CSV_DELIMITER = ';';
	private static final char CSV_QUOTE = '"';
	private static final int SKIP_LINES = 1;

	private static final String INVALID_COUNTRY_CODE = "Invalid country codes provided: %s";

	private Map<String, EnvironmentalFootprintValue> environmentalFootprints;
	private Map<String, Pair<Double, Double>> countryScores;
	private Map<String, Pair<Double, Double>> countryPositions;

	public static class EnvironmentalFootprintValue {
		private final String id;
		private final String value;
		private final Double score;

		public EnvironmentalFootprintValue(String id, String value, Double score) {
			this.id = id;
			this.value = value;
			this.score = score;
		}

		public String getId() {
			return id;
		}

		public Double getScore() {
			return score;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.format("%s - %s", id, value);
		}
	}

	private void loadEnvironmentalFootprints() {
		environmentalFootprints = new LinkedHashMap<>();
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_AGRIBALYSE_PATH);
			try (CSVReader csvReader = createCsvReader(resource)) {
				String[] line;
				while ((line = csvReader.readNext()) != null) {
					if (line.length >= 13) {
						environmentalFootprints.put(line[1], new EnvironmentalFootprintValue(line[1], line[4], parseDouble(line[12])));
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load environmental footprints from " + DEFAULT_AGRIBALYSE_PATH, e);
			environmentalFootprints = Collections.emptyMap();
		}
	}

	private void loadCountryScores() {
		countryScores = new LinkedHashMap<>();
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_COUNTRY_SCORE_PATH);
			try (CSVReader csvReader = createCsvReader(resource)) {
				String[] line;
				while ((line = csvReader.readNext()) != null) {
					if (line.length >= 4) {
						countryScores.put(line[0], new Pair<>(parseDouble(line[2]), parseDouble(line[3])));
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load country scores from " + DEFAULT_COUNTRY_SCORE_PATH, e);
			countryScores = Collections.emptyMap();
		}
	}

	private void loadCountryPositions() {
		countryPositions = new LinkedHashMap<>();
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_COUNTRY_POSITION_PATH);
			try (CSVReader csvReader = createCsvReader(resource)) {
				String[] line;
				while ((line = csvReader.readNext()) != null) {
					if (line.length >= 4) {
						countryPositions.put(line[0], new Pair<>(parseDouble(line[1]), parseDouble(line[2])));
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load country positions from " + DEFAULT_COUNTRY_POSITION_PATH, e);
			countryPositions = Collections.emptyMap();
		}
	}

	private CSVReader createCsvReader(ClassPathResource resource) throws IOException {
		InputStream inputStream = resource.getInputStream();
		InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		return new CSVReader(reader, CSV_DELIMITER, CSV_QUOTE, SKIP_LINES);
	}

	private Double parseDouble(String value) {
		return Optional.ofNullable(value).map(String::trim).filter(s -> !s.isEmpty()).map(s -> s.replace(",", ".")).map(Double::valueOf).orElse(null);
	}

	/**
	 * <p>Getter for the field <code>environmentalFootprints</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, EnvironmentalFootprintValue> getEnvironmentalFootprints() {
		if (environmentalFootprints == null) {
			loadEnvironmentalFootprints();
		}
		return Collections.unmodifiableMap(environmentalFootprints);
	}

	/**
	 * <p>Getter for the field <code>countryScores</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, Pair<Double, Double>> getCountryScores() {
		if (countryScores == null) {
			loadCountryScores();
		}
		return Collections.unmodifiableMap(countryScores);
	}

	// Haversine formula to calculate distance between two lat/lon points
	private double haversine(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371; // Radius of Earth in km
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.pow(Math.sin(dLat / 2), 2)
				+ (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c; // Distance in km
	}

	/**
	 * <p>getCountryLocations.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, Pair<Double, Double>> getCountryLocations() {
		if (countryPositions == null) {
			loadCountryPositions();
		}
		return Collections.unmodifiableMap(countryPositions);
	}

	// Method to calculate distance between two countries using country codes
	/**
	 * <p>distance.</p>
	 *
	 * @param countryCode a {@link java.lang.String} object
	 * @param countryCode2 a {@link java.lang.String} object
	 * @return a {@link java.lang.Long} object
	 */
	public Long distance(String countryCode, String countryCode2) {
		Map<String, Pair<Double, Double>> locations = getCountryLocations();
		Pair<Double, Double> loc1 = locations.get(countryCode);
		Pair<Double, Double> loc2 = locations.get(countryCode2);

		if ((loc1 == null) || (loc2 == null)) {
			throw new IllegalArgumentException(String.format(INVALID_COUNTRY_CODE, countryCode));
		}

		return Math.round(haversine(loc1.getFirst(), loc1.getSecond(), loc2.getFirst(), loc2.getSecond()));
	}
	
	
	/**
	 * SCORE EPI (Environmental Performance Index)
	 *
	 * @param countryCode a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double countryEPI(String countryCode) {
		Pair<Double, Double> score = getCountryScores().get(countryCode);
		if ((score == null)) {
			throw new IllegalArgumentException(String.format(INVALID_COUNTRY_CODE, countryCode));
		}
		
		return score.getFirst();
		
	}
	
	/**
	 * SCORE SPI (Social Progress Index)
	 *
	 * @param countryCode a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double countrySPI(String countryCode) {
		Pair<Double, Double> score = getCountryScores().get(countryCode);
		if ((score == null)) {
			throw new IllegalArgumentException(String.format(INVALID_COUNTRY_CODE, countryCode));
		}
		
		return score.getSecond();
		
	}

}
