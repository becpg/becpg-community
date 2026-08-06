package fr.becpg.repo.product.formulation.lca.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
 * <p>AgribalyseLCADatabasePlugin class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@Service
public class AgribalyseLCADatabasePlugin implements LCADatabasePlugin {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(AgribalyseLCADatabasePlugin.class);

	@Autowired
	private ContentService contentService;
	
	/** {@inheritDoc} */
	@Override
	public boolean acceptDatabaseFilename(String databaseName) {
		return databaseName.contains("agribalyse");
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMethod() {
		return "Agribalyse";
	}

	/** Constant <code>LCA_CODES</code> */
	private static final List<String> LCA_CODES = List.of("CLIMATE_CHANGE", "PARTICULATE_MATTER", "WATER_USE", "LAND_USE",
			"RESOURCE_USE_MINERALS_METALS", "OZONE_DEPLETION", "ACIDIFICATION", "IONIZING_RADIATION", "PHOTOCHEMICAL_OZONE_FORMATION",
			"EUTROPHICATION_TERRESTRIAL", "EUTROPHICATION_MARINE", "EUTROPHICATION_FRESHWATER", "ECOTOXICITY_FRESHWATER", "HUMAN_TOXICITY_CANCER",
			"HUMAN_TOXICITY_NON_CANCER", "RESOURCE_USE_FOSSILS");

	/** Constant <code>ID_COLUMN=1</code> */
	private static final int ID_COLUMN = 1;

	/** Constant <code>NAME_COLUMN=4</code> */
	private static final int NAME_COLUMN = 4;

	/** Constant <code>SCORE_COLUMN=12</code> */
	private static final int SCORE_COLUMN = 12;

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
				lcaData.put(data.getId(), data);
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
	 * @return the column index of each header, indexed by header name
	 * @throws java.io.IOException if the header cannot be read
	 */
	private Map<String, Integer> readHeaderIndexes(CSVReader csvReader) throws IOException {
		String[] headers = csvReader.readNext();

		Map<String, Integer> headerIndexes = new HashMap<>();
		for (int i = 0; i < headers.length; i++) {
			headerIndexes.put(headers[i], i);
		}

		return headerIndexes;
	}

	/**
	 * <p>extractLine.</p>
	 *
	 * @param line the CSV line
	 * @param headerIndexes the column index of each header
	 * @return a {@link fr.becpg.repo.product.formulation.lca.LCAData} object
	 */
	private LCAData extractLine(String[] line, Map<String, Integer> headerIndexes) {
		LCAData data = new LCAData(line[ID_COLUMN], line[NAME_COLUMN], parseDouble(line[SCORE_COLUMN]));

		for (String lcaCode : LCA_CODES) {
			Integer column = headerIndexes.get(lcaCode);
			if (column != null) {
				data.withImpact(lcaCode, parseDouble(line[column]));
			}
		}

		return data;
	}

	/**
	 * <p>parseDouble.</p>
	 *
	 * @param value a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double parseDouble(String value) {
		if ((value != null) && !value.trim().isEmpty()) {
			return Double.valueOf(value.trim().replace(",", "."));
		}
		return null;
	}

}
