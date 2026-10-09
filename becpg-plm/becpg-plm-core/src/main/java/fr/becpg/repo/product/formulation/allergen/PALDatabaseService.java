/*
 *
 */
package fr.becpg.repo.product.formulation.allergen;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Loads the reference-dose grids used by the PAL / VITAL allergen assessment.
 *
 * <p>Each regulatory framework is a CSV file dropped in
 * {@code /System/PALDatabases}, named after the framework code exposed by the
 * {@code AllergenRegulatoryFrameworks} list of values (e.g. {@code NL_ED05.csv}).
 * Publishing a new version of a framework means dropping a new file and adding a
 * new entry to that list: nothing changes in the content model.</p>
 *
 * <p>Expected columns, semicolon separated:
 * {@code allergenCode;rfdMg;maxActionPpm;proteinPerc}.</p>
 *
 * <p>Parsed grids are cached: replacing the content of a grid requires clearing
 * the beCPG caches before the new reference doses are taken into account.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("palDatabaseService")
public class PALDatabaseService {

	private static final String DATABASES_FOLDER = "/app:company_home/cm:System/cm:PALDatabases";

	private static final String CSV_EXTENSION = ".csv";

	private static final char CSV_SEPARATOR = ';';

	private static final char CSV_QUOTE = '"';

	private static final int ALLERGEN_CODE_COLUMN = 0;

	private static final int RFD_MG_COLUMN = 1;

	private static final int MAX_ACTION_PPM_COLUMN = 2;

	private static final int PROTEIN_PERC_COLUMN = 3;

	private static final String COMMENT_PREFIX = "#";

	private static final String ALLERGEN_CODE_HEADER = "allergenCode";

	private static final String CACHE_KEY = PALDatabaseService.class.getName();

	private static final Log logger = LogFactory.getLog(PALDatabaseService.class);

	private final FileFolderService fileFolderService;

	private final ContentService contentService;

	private final Repository repositoryHelper;

	private final BeCPGCacheService beCPGCacheService;

	/**
	 * <p>Constructor for PALDatabaseService.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 * @param repositoryHelper a {@link org.alfresco.repo.model.Repository} object
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object
	 */
	@Autowired
	public PALDatabaseService(@Qualifier("fileFolderService") FileFolderService fileFolderService,
			@Qualifier("contentService") ContentService contentService, @Qualifier("repositoryHelper") Repository repositoryHelper,
			BeCPGCacheService beCPGCacheService) {
		this.fileFolderService = fileFolderService;
		this.contentService = contentService;
		this.repositoryHelper = repositoryHelper;
		this.beCPGCacheService = beCPGCacheService;
	}

	/**
	 * <p>findReferenceDose.</p>
	 *
	 * @param frameworkCode the regulatory framework selected on the product
	 * @param allergenCode the code of the allergen to look up
	 * @return the matching {@link fr.becpg.repo.product.formulation.allergen.PALReferenceDose}, or {@code null} when the framework or the allergen is unknown
	 */
	public PALReferenceDose findReferenceDose(String frameworkCode, String allergenCode) {
		if ((frameworkCode == null) || frameworkCode.isBlank() || (allergenCode == null) || allergenCode.isBlank()) {
			return null;
		}

		return getReferenceDoses(frameworkCode).get(allergenCode);
	}

	/**
	 * <p>getPALDatabases.</p>
	 *
	 * @return the reference dose grids published in the database folder, never {@code null}
	 */
	public List<FileInfo> getPALDatabases() {
		NodeRef databasesFolder = BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);

		if (databasesFolder == null) {
			return new ArrayList<>();
		}

		return fileFolderService.listFiles(databasesFolder);
	}

	/**
	 * <p>getReferenceDoses.</p>
	 *
	 * @param frameworkCode the regulatory framework selected on the product
	 * @return the reference doses of that framework, indexed by allergen code, never {@code null}
	 */
	public Map<String, PALReferenceDose> getReferenceDoses(String frameworkCode) {
		Map<String, PALReferenceDose> referenceDoses = beCPGCacheService.getFromCache(CACHE_KEY, frameworkCode,
				() -> loadReferenceDoses(frameworkCode));

		return referenceDoses != null ? referenceDoses : loadReferenceDoses(frameworkCode);
	}

	/**
	 * Reads and parses the CSV grid of the given framework.
	 *
	 * @param frameworkCode the regulatory framework selected on the product
	 * @return the reference doses indexed by allergen code, never {@code null}
	 */
	private Map<String, PALReferenceDose> loadReferenceDoses(String frameworkCode) {
		Map<String, PALReferenceDose> referenceDoses = new HashMap<>();

		NodeRef databaseNodeRef = findDatabase(frameworkCode);
		if (databaseNodeRef == null) {
			logger.warn("No PAL database found for regulatory framework: " + frameworkCode);
			return referenceDoses;
		}

		try (CSVReader csvReader = createReader(databaseNodeRef)) {
			String[] line = csvReader.readNext();
			while (line != null) {
				appendReferenceDose(referenceDoses, line);
				line = csvReader.readNext();
			}
		} catch (IOException e) {
			logger.error("Could not read the PAL database of framework: " + frameworkCode, e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Loaded " + referenceDoses.size() + " reference doses for framework: " + frameworkCode);
		}

		return referenceDoses;
	}

	/**
	 * Parses one CSV line and registers it when it carries a usable reference dose.
	 *
	 * @param referenceDoses the map being filled
	 * @param line the raw CSV line
	 */
	private void appendReferenceDose(Map<String, PALReferenceDose> referenceDoses, String[] line) {
		if ((line.length <= RFD_MG_COLUMN) || line[ALLERGEN_CODE_COLUMN].isBlank()) {
			return;
		}

		String allergenCode = line[ALLERGEN_CODE_COLUMN].trim();

		if (allergenCode.startsWith(COMMENT_PREFIX) || ALLERGEN_CODE_HEADER.equalsIgnoreCase(allergenCode)) {
			return;
		}

		Double rfdMg = parseDouble(line, RFD_MG_COLUMN);

		if (rfdMg == null) {
			logger.warn("Skipping PAL reference dose without RfD for allergen: " + allergenCode);
			return;
		}

		referenceDoses.put(allergenCode,
				new PALReferenceDose(allergenCode, rfdMg, parseDouble(line, MAX_ACTION_PPM_COLUMN), parseDouble(line, PROTEIN_PERC_COLUMN)));
	}

	/**
	 * Reads an optional numeric column, tolerating both decimal separators.
	 *
	 * @param line the raw CSV line
	 * @param column the column index
	 * @return the parsed value, or {@code null} when the column is absent or empty
	 */
	private Double parseDouble(String[] line, int column) {
		if ((line.length <= column) || line[column].isBlank()) {
			return null;
		}

		try {
			return Double.valueOf(line[column].trim().replace(',', '.'));
		} catch (NumberFormatException e) {
			logger.warn("Ignoring non numeric PAL database value: " + line[column]);
			return null;
		}
	}

	/**
	 * Resolves the CSV file whose name matches the framework code.
	 *
	 * @param frameworkCode the regulatory framework selected on the product
	 * @return the file node reference, or {@code null} when no file matches
	 */
	private NodeRef findDatabase(String frameworkCode) {
		NodeRef databasesFolder = BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);

		if (databasesFolder == null) {
			return null;
		}

		List<FileInfo> databases = fileFolderService.listFiles(databasesFolder);
		for (FileInfo database : databases) {
			if (matches(database, frameworkCode)) {
				return database.getNodeRef();
			}
		}

		return null;
	}

	/**
	 * Tells whether a file of the database folder holds the grid of a framework.
	 *
	 * @param database the candidate file
	 * @param frameworkCode the regulatory framework selected on the product
	 * @return true when the file name matches the framework code
	 */
	private boolean matches(FileInfo database, String frameworkCode) {
		String fileName = (String) database.getProperties().get(ContentModel.PROP_NAME);

		return (fileName != null) && (fileName.equalsIgnoreCase(frameworkCode) || fileName.equalsIgnoreCase(frameworkCode + CSV_EXTENSION));
	}

	/**
	 * Opens a CSV reader on the content of a database file.
	 *
	 * @param databaseNodeRef the file node reference
	 * @return an open {@link fr.becpg.common.csv.CSVReader}
	 */
	private CSVReader createReader(NodeRef databaseNodeRef) throws IOException {
		ContentReader contentReader = contentService.getReader(databaseNodeRef, ContentModel.PROP_CONTENT);

		if (contentReader == null) {
			throw new IOException("No content on PAL database: " + databaseNodeRef);
		}

		return new CSVReader(new InputStreamReader(contentReader.getContentInputStream(), StandardCharsets.UTF_8), CSV_SEPARATOR, CSV_QUOTE, 0);
	}

}
