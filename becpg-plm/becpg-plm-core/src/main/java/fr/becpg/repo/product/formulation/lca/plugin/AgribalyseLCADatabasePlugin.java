package fr.becpg.repo.product.formulation.lca.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * @author matthieu
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
				try (CSVReader csvReader = new CSVReader(inReader, ';', '"', 1)) {
					String[] line = null;
					while ((line = csvReader.readNext()) != null) {
						lcaData.put(line[1],
								new LCAData(line[1], line[4], parseDouble(line[12]),
										parseDouble(line[27]), parseDouble(line[28]), parseDouble(line[29]),
										parseDouble(line[30]), parseDouble(line[31]), parseDouble(line[32]),
										parseDouble(line[33]), parseDouble(line[34]), parseDouble(line[35]),
										parseDouble(line[36]), parseDouble(line[37]), parseDouble(line[38]),
										parseDouble(line[39]), parseDouble(line[40]), parseDouble(line[41]),
										parseDouble(line[42])));
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
