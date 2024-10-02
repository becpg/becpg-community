package fr.becpg.repo.product.formulation.nutrient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.common.BeCPGException;
import fr.becpg.common.csv.CSVReader;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>NutDatabaseServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
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
	private DictionaryService dictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	private static final  Log logger = LogFactory.getLog(NutDatabaseServiceImpl.class);
	
	private static final String DATABASES_FOLDER = "/app:company_home/cm:System/cm:NutritionalDatabases";
	
	private static final String NUT_CSV_DECIMAL_FORMAT = "###,###.####";

	/** {@inheritDoc} */
	@Override
	public List<FileInfo> getNutDatabases() {

		NodeRef dbFolderNR = BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);
		if (dbFolderNR != null) {
			return fileFolderService.listFiles(dbFolderNR);
		} else {
			return new ArrayList<>();
		}
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String databaseNR, String query, int pageNum, int pageSize) {
		List<IdentifiedValue> matches = new ArrayList<>();
		
		if ((databaseNR != null) && !databaseNR.isEmpty()) {
			NodeRef dataBaseFile = new NodeRef(databaseNR);
			String[] headerRow = getHeaderRow(dataBaseFile);

			int nameColumn = extractNameColumnIndex(headerRow);

			String preparedQuery = BeCPGQueryHelper.prepareQuery(query).replace("*", "");

			matches.addAll(getColumn(dataBaseFile, nameColumn).stream().filter(res -> nameMatches(query, res.toString())).limit(100)
					.collect(Collectors.toList()));

			matches.sort((o1, o2) -> {

				if (BeCPGQueryHelper.isAllQuery(query)) {
					return o1.getValue().compareTo(o2.getValue());
				}

				String value = BeCPGQueryHelper.prepareQueryForSorting(o1.getValue()).replace("*", "").replace(preparedQuery, "A");
				String value2 = BeCPGQueryHelper.prepareQueryForSorting(o2.getValue()).replace("*", "").replace(preparedQuery, "A");

				return value.compareTo(value2);

			});

			logger.debug("suggestion for " + query + ", found " + matches.size() + " results");
		} else {
			logger.debug("database noderef is null");
		}

		return new AutoCompletePage(matches, pageNum, pageSize, new IdentifiedValueListExtractor());
	}

	
	
	/** {@inheritDoc} */
	@Override
	public List<NutListDataItem> getNuts(NodeRef databaseFile, String id) {
		List<NutListDataItem> ret = new ArrayList<>();
		logger.debug("getting nut list for RM of id " + id + " in file " + nodeService.getProperty(databaseFile, ContentModel.PROP_NAME));

		if ((id != null) && (id.length() > 0)) {
			String[] headerRow = getHeaderRow(databaseFile);
			int idColumn = extractIdentifierColumnIndex(headerRow);
			int nameColumn = extractNameColumnIndex(headerRow);
			
			DecimalFormat decimalFormat =  new DecimalFormat(NUT_CSV_DECIMAL_FORMAT);

			String[] values = getLineByIndex(databaseFile, id, idColumn);
			for (int i = 0; i < headerRow.length; ++i) {

				if (!"COLUMNS".equals(headerRow[i]) && (i != idColumn) && (i != nameColumn)
						&& (!isInDictionary(headerRow[i]) || !headerRow[i].contains("_"))) {
					NodeRef nutNodeRef = getNutNodeRef(headerRow[i]);
					if (nutNodeRef != null) {
						try {
							String nutValueToken = values[i];
							Number nutValue = null;
							if ((nutValueToken != null) && !nutValueToken.isEmpty()) {

								nutValue = decimalFormat.parse(nutValueToken);
							}

							NutListDataItem nut = NutListDataItem.build().withNut(nutNodeRef).withIsManual(false)
;

							String newMethod = extractNutMethod(databaseFile);
							PropertyDefinition methodDef = dictionaryService.getProperty(PLMModel.PROP_NUTLIST_METHOD);

							if (methodDef != null) {
								List<ConstraintDefinition> methodConstraints = methodDef.getConstraints();
								for (ConstraintDefinition constraint : methodConstraints) {
									if (constraint.getConstraint() instanceof DynListConstraint) {
										DynListConstraint dynConstraint = (DynListConstraint) constraint.getConstraint();
										List<String> allowedValues = dynConstraint.getAllowedValues();
										logger.debug("Allowed values: " + allowedValues);

										for (String value : allowedValues) {
											logger.debug("current value: " + value);
											if (newMethod.contains(value) && !"".equals(value)) {
												logger.debug("value matches method named " + newMethod);
												nut.setMethod(value);
												break;
											}
										}
										break;
									}
								}
							} else {
								logger.debug("Can't find method definition for " + PLMModel.PROP_NUTLIST_METHOD);
							}

							if (nutValue != null) {
								nut.setManualValue(nutValue.doubleValue());
							}
							

							// nutListGroup
							String nutGroup = (String) nodeService.getProperty(nutNodeRef, PLMModel.PROP_NUTGROUP);

							nut.setGroup(nutGroup);
							nut.setSort(i);
							nut.setDepthLevel(1);
							ret.add(nut);
						} catch (ParseException e) {
							throw new BeCPGException("unable to parse value " + values[i], e);
						}
					}
				}
			}
		} else {
			logger.debug("invalid id (null or empty string) ");
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createProduct(NodeRef file, String id, NodeRef dest) {

		StopWatch watch = null;

		boolean mlAware = MLPropertyInterceptor.setMLAware(true);
		try {

			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				// Only for transaction do not reenable it
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);

				String[] headerRow = getHeaderRow(file);

				int idColumn = extractIdentifierColumnIndex(headerRow);
				int nameColumn = extractNameColumnIndex(headerRow);

				NodeRef productNode = null;

				String[] idSplits = id.split(",");
				for (String idSplit : idSplits) {
					productNode = nodeService.createNode(dest, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_RAWMATERIAL)
							.getChildRef();

					ProductData productData = alfrescoRepository.findOne(productNode);
					RawMaterialData dat = (RawMaterialData) productData;

					if (dat != null) {

						String name = getProductName(file, idSplit, idColumn, nameColumn);
						
						if (name != null) {
							dat.setName(PropertiesHelper.cleanName(name));
						}

						for (int i = 1; i < headerRow.length; ++i) {
							if (isInDictionary(headerRow[i]) || (headerRow[i].contains("_") && (isInDictionary(headerRow[i].split("_")[0])))) {
								String value = extractValueById(file, idSplit, i);
								if(logger.isDebugEnabled()) {
									logger.debug("setting property qnamed  \"" + headerRow[i] + "\" to value  \"" + value + "\"");
								}
								QName attributeQName = QName.createQName(headerRow[i], namespaceService);

								if (PLMModel.TYPE_SUPPLIER.equals(attributeQName)) {
									// supplier
									BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_SUPPLIER)
											.andPropEquals(PLMModel.TYPE_SUPPLIER, extractValueById(file, idSplit, i));
									List<NodeRef> suppliers = queryBuilder.list();
									if (!suppliers.isEmpty()) {
										logger.debug("Setting suppliers to " + suppliers);
										dat.setSuppliers(suppliers);
									}
								}
							}
						}

						List<NutListDataItem> nutList = getNuts(file, idSplit);
						dat.setNutList(nutList);
						alfrescoRepository.save(dat);
						productNode = dat.getNodeRef();

						for (int i = 1; i < headerRow.length; ++i) {
							if (isInDictionary(headerRow[i]) || (headerRow[i].contains("_") && (isInDictionary(headerRow[i].split("_")[0])))) {
								String value = extractValueById(file, idSplit, i);
								if(logger.isDebugEnabled()) {
									logger.debug("setting property qnamed  \"" + headerRow[i] + "\" to value  \"" + value + "\"");
								}
								QName attributeQName = QName.createQName(headerRow[i], namespaceService);

								if (!(PLMModel.TYPE_SUPPLIER.equals(attributeQName) || PLMModel.PROP_PRODUCT_HIERARCHY1.equals(attributeQName)
										|| PLMModel.PROP_PRODUCT_HIERARCHY2.equals(attributeQName) || ContentModel.PROP_NAME.equals(attributeQName))
										&& (dictionaryService.getProperty(attributeQName) != null)) {
									nodeService.setProperty(productNode, QName.createQName(headerRow[i], namespaceService), value);
								}
							}
						}

					}
				}

				return productNode;

			}, false, false);

		} finally {
			MLPropertyInterceptor.setMLAware(mlAware);

			if (logger.isDebugEnabled() && watch != null) {
				watch.stop();
				logger.debug("createProduct run in  " + watch.getTotalTimeSeconds() + " seconds ");
			}

		}
	}

	private CSVReader getCSVReaderFromNodeRef(NodeRef file) {
		ContentReader fileReader = contentService.getReader(file, ContentModel.PROP_CONTENT);
		return new CSVReader(new InputStreamReader(fileReader.getContentInputStream()), ';');
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

		try (CSVReader csvReader = getCSVReaderFromNodeRef(file)) {

			String[] currentLine = csvReader.readNext();
			while (currentLine != null) {
				if ((currentLine.length > columnIndex) && "VALUES".equals(currentLine[0])) {
					
					
					res.add(new IdentifiedValue(currentLine[identifierColumn], currentLine[columnIndex]));
				}
				currentLine = csvReader.readNext();
			}
			return res;
		} catch (IOException e) {
			throw new RuntimeException("Error reading column " + columnIndex, e);
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

		try (CSVReader csvReader = getCSVReaderFromNodeRef(file)) {

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
			throw new BeCPGException("Error reading line", e);
		}
	}

	private String getProductName(NodeRef file, String id, int indexColumn, int nameColumn) {
		try (CSVReader csvReader = getCSVReaderFromNodeRef(file)) {
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
			throw new BeCPGException("Error reading product name", e);
		}
	}

	private NodeRef getNutNodeRef(String nutName) {
		logger.debug("Finding nodeRef for nut named \"" + nutName + "\"");
		List<NodeRef> foundNuts = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_NUT)
				.andPropEquals(BeCPGModel.PROP_CHARACT_NAME, nutName).list();

		if (!foundNuts.isEmpty()) {
			NodeRef nutsListNodeRef = foundNuts.get(0);

			logger.debug("Found one");
			return nutsListNodeRef;

		} else {
			logger.debug("found no nodeRef for nut " + nutName + ", it might not be in the system.");
			return null;
		}
	}

	private String[] getHeaderRow(NodeRef file) {
		try (CSVReader csvReader = getCSVReaderFromNodeRef(file)) {
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

			return res;

		} catch (IOException e) {
			throw new BeCPGException("Error reading spreadsheet headers", e);
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

	private class IdentifiedValueListExtractor implements AutoCompleteExtractor<IdentifiedValue> {

		@Override
		public List<AutoCompleteEntry> extract(List<IdentifiedValue> values) {
			List<AutoCompleteEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (IdentifiedValue value : values) {
					suggestions.add(new AutoCompleteEntry(value.getId(), value.toString(), "rawMaterial"));
				}
			}
			return suggestions;
		}
	}

	private boolean nameMatches(String query, String name) {
		return BeCPGQueryHelper.isQueryMatch(query, name);
	}

	private boolean isInDictionary(String str) {
		try {
			return ((dictionaryService.getProperty(QName.createQName(str, namespaceService)) != null)
					|| (dictionaryService.getAssociation(QName.createQName(str, namespaceService)) != null));
		} catch (NamespaceException e) {
			return false;
		}
	}

	private String extractValueById(NodeRef file, String id, int column) {
		String[] line = getLineByIndex(file, id, extractIdentifierColumnIndex(getHeaderRow(file)));

		if (line.length > column) {
			return line[column];
		} else {
			throw new RuntimeException("error extracting value from cell from id " + id + " and column " + column);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getProductName(NodeRef file, String id) {
		String[] headerRow = getHeaderRow(file);
		return getProductName(file, id, extractIdentifierColumnIndex(headerRow), extractNameColumnIndex(headerRow));
	}

	/**
	 * <p>extractNutMethod.</p>
	 *
	 * @param node a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String extractNutMethod(NodeRef node) {
		return FilenameUtils.removeExtension((String) nodeService.getProperty(node, ContentModel.PROP_NAME)).toLowerCase().replace("table", "").trim()
				.toUpperCase();
	}

}
