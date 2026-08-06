package fr.becpg.repo.product.formulation.lca.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.repo.product.formulation.lca.LCAData;
import fr.becpg.repo.product.formulation.lca.LCADatabasePlugin;

/**
 * Reads an impact export of the Ecobalyse method.
 *
 * <p>Ecobalyse names its indicators with short codes, which this plugin maps to the beCPG
 * ones. Three of them have no counterpart in the historical Agribalyse set: the corrected
 * toxicity indicators, which the environmental cost weighs instead of the raw ones.</p>
 *
 * <p>The file is not shipped with beCPG: the customer drops its own export in
 * {@code /System/LCADatabases}, as it does for Agribalyse.</p>
 *
 * @author matthieu
 */
@Service
public class EcobalyseLCADatabasePlugin implements LCADatabasePlugin {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(EcobalyseLCADatabasePlugin.class);

	/** Constant <code>DATABASE_NAME="ecobalyse"</code> */
	private static final String DATABASE_NAME = "ecobalyse";

	/** Constant <code>METHOD="Ecobalyse"</code> */
	private static final String METHOD = "Ecobalyse";

	/** Constant <code>ID_COLUMN="id"</code> */
	private static final String ID_COLUMN = "id";

	/** Constant <code>NAME_COLUMN="name"</code> */
	private static final String NAME_COLUMN = "name";

	/** Constant <code>SCORE_COLUMN="ecs"</code> */
	private static final String SCORE_COLUMN = "ecs";

	/** Maps the Ecobalyse indicator codes to the beCPG ones */
	private static final Map<String, String> LCA_CODES = buildCodeMapping();

	@Autowired
	private ContentService contentService;

	/** {@inheritDoc} */
	@Override
	public boolean acceptDatabaseFilename(String databaseName) {
		return databaseName.toLowerCase().contains(DATABASE_NAME);
	}

	/** {@inheritDoc} */
	@Override
	public String getMethod() {
		return METHOD;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, LCAData> extractData(NodeRef databaseNodeRef) {
		Map<String, LCAData> lcaData = new LinkedHashMap<>();
		ContentReader reader = contentService.getReader(databaseNodeRef, ContentModel.PROP_CONTENT);

		try (InputStream in = reader.getContentInputStream();
				InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
				CSVReader csvReader = new CSVReader(inReader, ';', '"', 0)) {

			Map<String, Integer> headerIndexes = readHeaderIndexes(csvReader);

			String[] line = null;
			while ((line = csvReader.readNext()) != null) {
				LCAData data = extractLine(line, headerIndexes);
				if (data != null) {
					lcaData.put(data.getId(), data);
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Read " + lcaData.size() + " Ecobalyse entries");
			}
		} catch (IOException e) {
			logger.error("Error while reading content of: " + databaseNodeRef, e);
		}

		return lcaData;
	}

	/**
	 * <p>readHeaderIndexes.</p>
	 *
	 * @param csvReader a {@link fr.becpg.common.csv.CSVReader} object
	 * @return the column index of each header, indexed by lower case header name
	 * @throws java.io.IOException if the header cannot be read
	 */
	private Map<String, Integer> readHeaderIndexes(CSVReader csvReader) throws IOException {
		String[] headers = csvReader.readNext();

		Map<String, Integer> headerIndexes = new HashMap<>();
		for (int i = 0; i < headers.length; i++) {
			headerIndexes.put(headers[i].trim().toLowerCase(), i);
		}

		return headerIndexes;
	}

	/**
	 * <p>extractLine.</p>
	 *
	 * @param line the CSV line
	 * @param headerIndexes the column index of each header
	 * @return a {@link fr.becpg.repo.product.formulation.lca.LCAData} object, null when the
	 *         line carries no identifier
	 */
	private LCAData extractLine(String[] line, Map<String, Integer> headerIndexes) {
		String id = column(line, headerIndexes, ID_COLUMN);

		if ((id == null) || id.isBlank()) {
			return null;
		}

		LCAData data = new LCAData(id, column(line, headerIndexes, NAME_COLUMN), parseDouble(column(line, headerIndexes, SCORE_COLUMN)));

		for (Map.Entry<String, String> mapping : LCA_CODES.entrySet()) {
			data.withImpact(mapping.getValue(), parseDouble(column(line, headerIndexes, mapping.getKey())));
		}

		return data;
	}

	/**
	 * <p>column.</p>
	 *
	 * @param line the CSV line
	 * @param headerIndexes the column index of each header
	 * @param header the wanted header, in lower case
	 * @return the value of the column, null when the export does not hold it
	 */
	private String column(String[] line, Map<String, Integer> headerIndexes, String header) {
		Integer index = headerIndexes.get(header);
		return ((index == null) || (index >= line.length)) ? null : line[index];
	}

	/**
	 * <p>parseDouble.</p>
	 *
	 * @param value a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double parseDouble(String value) {
		if ((value == null) || value.trim().isEmpty()) {
			return null;
		}
		return Double.valueOf(value.trim().replace(",", "."));
	}

	/**
	 * <p>buildCodeMapping.</p>
	 *
	 * @return the beCPG indicator code of each Ecobalyse one
	 */
	private static Map<String, String> buildCodeMapping() {
		Map<String, String> codes = new LinkedHashMap<>();

		codes.put("acd", "ACIDIFICATION");
		codes.put("cch", "CLIMATE_CHANGE");
		codes.put("etf", "ECOTOXICITY_FRESHWATER");
		codes.put("etf-c", "ECOTOXICITY_FRESHWATER_CORRECTED");
		codes.put("fru", "RESOURCE_USE_FOSSILS");
		codes.put("fwe", "EUTROPHICATION_FRESHWATER");
		codes.put("htc", "HUMAN_TOXICITY_CANCER");
		codes.put("htc-c", "HUMAN_TOXICITY_CANCER_CORRECTED");
		codes.put("htn", "HUMAN_TOXICITY_NON_CANCER");
		codes.put("htn-c", "HUMAN_TOXICITY_NON_CANCER_CORRECTED");
		codes.put("ior", "IONIZING_RADIATION");
		codes.put("ldu", "LAND_USE");
		codes.put("mru", "RESOURCE_USE_MINERALS_METALS");
		codes.put("ozd", "OZONE_DEPLETION");
		codes.put("pco", "PHOTOCHEMICAL_OZONE_FORMATION");
		codes.put("pma", "PARTICULATE_MATTER");
		codes.put("swe", "EUTROPHICATION_MARINE");
		codes.put("tre", "EUTROPHICATION_TERRESTRIAL");
		codes.put("wtu", "WATER_USE");

		return codes;
	}

}
