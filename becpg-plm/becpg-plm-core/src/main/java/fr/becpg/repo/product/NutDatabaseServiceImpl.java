package fr.becpg.repo.product;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.star.uno.RuntimeException;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("nutDatabaseService")
public class NutDatabaseServiceImpl implements NutDatabaseService {

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private Repository repositoryHelper;

	@Autowired
	private NodeService mlAwareNodeService;

	@Autowired
	private DictionaryDAO dictionaryDAO;

	private final static Log logger = LogFactory.getLog(NutDatabaseServiceImpl.class);
	private final static String SYSTEM_NUTS_PATH = "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:Nuts";
	private final static String DATABASES_FOLDER = "/app:company_home/cm:System/cm:NutritionalDatabases";

	@Override
	public List<FileInfo> getNutDatabases() {

		NodeRef dbFolderNR = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);

		return fileFolderService.listFiles(dbFolderNR);
	}

	@Override
	public ListValuePage suggest(String databaseNR, String query, int pageNum, int pageSize) {
		List<IdentifiedValue> matches = new ArrayList<>();

		if (databaseNR != null) {
			NodeRef dataBaseFile = new NodeRef(databaseNR);
			String[] headerRow = getHeaderRow(dataBaseFile);

			int nameColumn = extractNameColumnIndex(headerRow);

			matches.addAll(getColumn(dataBaseFile, nameColumn).stream().filter(res -> nameMatches(query, res.getValue())).limit(100)
					.collect(Collectors.toList()));

			// if(!matches.isEmpty()){
			logger.debug("suggestion for " + query + ", found " + matches.size() + " results");
			return new ListValuePage(matches, pageNum, pageSize, new IdentifiedValueListExtractor());
			// }

		}
		logger.debug("database noderef is null");
		return null;
	}

	@Override
	public List<NutListDataItem> getNuts(NodeRef databaseFile, String id) {
		List<NutListDataItem> ret = new ArrayList<>();

		String[] headerRow = getHeaderRow(databaseFile);
		int idColumn = extractIdentifierColumnIndex(headerRow);
		int nameColumn = extractNameColumnIndex(headerRow);
		PropertyFormats propertyFormats = new PropertyFormats(true);

		String[] values = getLineByIndex(databaseFile, id, idColumn);
		for (int i = 0; i < headerRow.length; ++i) {
			if (!"COLUMNS".equals(headerRow[i]) && (i != idColumn) && (i != nameColumn)) {
				NodeRef nutNodeRef = getNutNodeRef(headerRow[i]);
				if (nutNodeRef != null) {
					try {
						String nutValueToken = formatNutString(values[i]);
						Number nutValue = null;
						if (nutValueToken != null) {
							nutValue = propertyFormats.parseDecimal(nutValueToken);
						}

						ret.add(new NutListDataItem(null, nutValue != null ? nutValue.doubleValue() : null, null, null, null, null, nutNodeRef,
								true));
					} catch (ParseException e) {
						throw new RuntimeException("unable to parse value " + values[i], e);
					}
				}
			}
		}

		return ret;
	}

	@Override
	public NodeRef createProduct(NodeRef file, String id, NodeRef dest) {

		String[] headerRow = getHeaderRow(file);

		int idColumn = extractIdentifierColumnIndex(headerRow);
		int nameColumn = extractNameColumnIndex(headerRow);

		NodeRef productNode = null;

		String[] idSplits = id.split(",");
		for (String idSplit : idSplits) {
			productNode = nodeService.createNode(dest, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_RAWMATERIAL)
					.getChildRef();
			ProductData dat = alfrescoRepository.findOne(productNode);

			if (dat != null) {

				String name = getProductName(file, idSplit, idColumn, nameColumn);

				if (name != null) {
					dat.setName(PropertiesHelper.cleanName(name));
				}

				List<NutListDataItem> nutList = getNuts(file, idSplit);
				dat.setNutList(nutList);
				alfrescoRepository.save(dat);
			}

		}

		return productNode;
	}

	private CSVReader getCSVReaderFromNodeRef(NodeRef file) {
		ContentReader fileReader = contentService.getReader(file, ContentModel.PROP_CONTENT);
		CSVReader csvReader = new CSVReader(new InputStreamReader(fileReader.getContentInputStream()), ';');

		return csvReader;
	}

	private List<IdentifiedValue> getColumn(NodeRef file, int columnIndex) {
		List<IdentifiedValue> res = new ArrayList<>();

		if (columnIndex < 0) {
			return res;
		}

		int identifierColumn = extractIdentifierColumnIndex(getHeaderRow(file));
		logger.debug("identifier column: " + identifierColumn);
		if (identifierColumn < 0) {
			return res;
		}

		CSVReader csvReader = getCSVReaderFromNodeRef(file);
		try {

			String[] currentLine = null;

			currentLine = csvReader.readNext();
			while (currentLine != null) {
				if ((currentLine.length > columnIndex) && "VALUES".equals(currentLine[0])) {
					res.add(new IdentifiedValue(currentLine[identifierColumn], currentLine[columnIndex]));
				}
				currentLine = csvReader.readNext();
			}
			return res;
		} catch (IOException e) {
			throw new RuntimeException("Error reading column " + columnIndex, e);
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private int extractIdentifierColumnIndex(String[] header) {
		int res = -1;
		int i = 1;
		boolean foundId = false;
		while ((i < header.length) && !foundId) {
			if ("id".equals(header[i])) {
				res = i;
				foundId = true;
			}
			++i;
		}

		return res;
	}

	private int extractNameColumnIndex(String[] header) {
		int res = -1;
		int i = 1;
		boolean foundId = false;
		while ((i < header.length) && !foundId) {
			if ("cm:name".equals(header[i])) {
				res = i;
				foundId = true;
			}
			++i;
		}
		return res;
	}

	private String[] getLineByIndex(NodeRef file, String id, int indexColumn) {
		CSVReader csvReader = getCSVReaderFromNodeRef(file);
		try {

			String[] currentLine = null;

			currentLine = csvReader.readNext();
			while (currentLine != null) {
				if ((indexColumn < currentLine.length) && id.equals(currentLine[indexColumn])) {
					return currentLine;

				}
				currentLine = csvReader.readNext();
			}
			return new String[0];
		} catch (IOException e) {
			throw new RuntimeException("Error reading line", e);
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getProductName(NodeRef file, String id, int indexColumn, int nameColumn) {
		CSVReader csvReader = getCSVReaderFromNodeRef(file);
		try {
			String res = null;
			String[] currentLine = null;

			currentLine = csvReader.readNext();
			while (currentLine != null) {
				if ((indexColumn < currentLine.length) && id.equals(currentLine[indexColumn])) {
					if (nameColumn < currentLine.length) {
						res = currentLine[nameColumn];
					} else {
						break;
					}
				}
				currentLine = csvReader.readNext();
			}
			return res;
		} catch (IOException e) {
			throw new RuntimeException("Error reading product name", e);
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private NodeRef getNutNodeRef(String nutName) {
		NodeRef nutsListNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(), SYSTEM_NUTS_PATH);

		List<ChildAssociationRef> children = nodeService.getChildAssocs(nutsListNodeRef);
		for (ChildAssociationRef child : children) {
			NodeRef childNodeRef = child.getChildRef();
			MLText mlText = (MLText) mlAwareNodeService.getProperty(childNodeRef, BeCPGModel.PROP_CHARACT_NAME);
			List<String> filteredValues = mlText.getValues().stream().filter(val -> nutName.equals(val)).collect(Collectors.toList());
			if (filteredValues.size() > 0) {
				return childNodeRef;
			}
		}
		logger.debug("found no nodeRef for nut " + nutName);
		return null;
	}

	private String[] getHeaderRow(NodeRef file) {
		CSVReader csvReader = getCSVReaderFromNodeRef(file);
		try {
			String[] res = new String[0];
			String[] currentLine = null;

			currentLine = csvReader.readNext();
			while (currentLine != null) {
				if ("COLUMNS".equals(currentLine[0])) {
					res = currentLine;
					break;
				}
				currentLine = csvReader.readNext();
			}

			csvReader.close();
			return res;

		} catch (IOException e) {
			throw new RuntimeException("Error reading spreadsheet headers", e);
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class IdentifiedValue {
		private String id;
		private String value;

		public IdentifiedValue(String id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getId() {
			return id;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return id + " - " + value;
		}
	}

	private class IdentifiedValueListExtractor implements ListValueExtractor<IdentifiedValue> {

		@Override
		public List<ListValueEntry> extract(List<IdentifiedValue> values) {
			List<ListValueEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (IdentifiedValue value : values) {
					suggestions.add(new ListValueEntry(value.getId(), value.toString(), "rawMaterial"));
				}
			}
			return suggestions;
		}
	}

	public boolean nameMatches(String query, String name) {
		return BeCPGQueryHelper.isQueryMatch(query, name, dictionaryDAO);
	}

	private String formatNutString(String numberString) {
		if ("traces".equals(numberString) || (numberString == null) || numberString.isEmpty()) {
			return null;
		} else if (numberString.contains("<")) {
			return numberString.replace("<", "").trim();
		} else {
			return numberString.trim();
		}
	}

}
