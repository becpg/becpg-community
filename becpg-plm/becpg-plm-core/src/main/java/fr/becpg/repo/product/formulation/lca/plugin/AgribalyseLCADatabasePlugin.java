package fr.becpg.repo.product.formulation.lca.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * <p>AgribalyseLCADatabasePlugin class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@Service
public class AgribalyseLCADatabasePlugin implements LCADatabasePlugin {

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

	/** {@inheritDoc} */
	@Override
	public Map<String, LCAData> extractData(NodeRef databaseNodeRef) {
		Map<String, LCAData> lcaData = new LinkedHashMap<>();
		ContentReader reader = contentService.getReader(databaseNodeRef, ContentModel.PROP_CONTENT);
		try (InputStream in = reader.getContentInputStream()) {
			try (InputStreamReader inReader = new InputStreamReader(in)) {
				try (CSVReader csvReader = new CSVReader(inReader, ';', '"', 0)) {
					String[] headers = csvReader.readNext();
		            
		            Map<String, Integer> headerIndexMap = new HashMap<>();
		            for (int i = 0; i < headers.length; i++) {
		                headerIndexMap.put(headers[i], i);
		            }
		            
					String[] line = null;
					while ((line = csvReader.readNext()) != null) {
						lcaData.put(line[1],
								new LCAData(line[1], line[4], parseDouble(line[12]),
										parseDouble(line[headerIndexMap.get("CLIMATE_CHANGE")]), parseDouble(line[headerIndexMap.get("PARTICULATE_MATTER")]), parseDouble(line[headerIndexMap.get("WATER_USE")]),
										parseDouble(line[headerIndexMap.get("LAND_USE")]), parseDouble(line[headerIndexMap.get("RESOURCE_USE_MINERALS_METALS")]), parseDouble(line[headerIndexMap.get("OZONE_DEPLETION")]),
										parseDouble(line[headerIndexMap.get("ACIDIFICATION")]), parseDouble(line[headerIndexMap.get("IONIZING_RADIATION")]), parseDouble(line[headerIndexMap.get("PHOTOCHEMICAL_OZONE_FORMATION")]),
										parseDouble(line[headerIndexMap.get("EUTROPHICATION_TERRESTRIAL")]), parseDouble(line[headerIndexMap.get("EUTROPHICATION_MARINE")]), parseDouble(line[headerIndexMap.get("EUTROPHICATION_FRESHWATER")]),
										parseDouble(line[headerIndexMap.get("ECOTOXICITY_FRESHWATER")]), parseDouble(line[headerIndexMap.get("HUMAN_TOXICITY_CANCER")]), parseDouble(line[headerIndexMap.get("HUMAN_TOXICITY_NON_CANCER")]),
										parseDouble(line[headerIndexMap.get("RESOURCE_USE_FOSSILS")])));
					}
				}
			}
		} catch (IOException e) {
			logger.error("Error while reading content of: " + databaseNodeRef, e);
		}
		
		return lcaData;
	}
	
	private Double parseDouble(String value) {
		if ((value != null) && !value.trim().isEmpty()) {
			return Double.valueOf(value.trim().replace(",", "."));
		}
		return null;
	}

}
