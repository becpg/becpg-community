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
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.star.uno.RuntimeException;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
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
	
	@Autowired
	private NamespaceService NamespaceService;

	@Autowired
	private AssociationService associationService;
	
	private final static Log logger = LogFactory.getLog(NutDatabaseServiceImpl.class);
	private final static String SYSTEM_NUTS_PATH = "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:Nuts";
	private final static String DATABASES_FOLDER = "/app:company_home/cm:System/cm:NutritionalDatabases";
	private static final String HIERARCHY_RAWMATERIAL_PATH = PlmRepoConsts.PATH_PRODUCT_HIERARCHY + "cm:" + HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_RAWMATERIAL);

	@Override
	public List<FileInfo> getNutDatabases() {

		NodeRef dbFolderNR = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);
		if(dbFolderNR != null){
			return fileFolderService.listFiles(dbFolderNR);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public ListValuePage suggest(String databaseNR, String query, int pageNum, int pageSize) {
		List<IdentifiedValue> matches = new ArrayList<>();
		logger.debug("databaseNR: "+databaseNR);
		
		if (databaseNR != null && !databaseNR.isEmpty()) {
			NodeRef dataBaseFile = new NodeRef(databaseNR);
			String[] headerRow = getHeaderRow(dataBaseFile);

			int nameColumn = extractNameColumnIndex(headerRow);

			matches.addAll(getColumn(dataBaseFile, nameColumn).stream().filter(res -> nameMatches(query, res.getValue())).limit(100)
					.collect(Collectors.toList()));

			logger.debug("suggestion for " + query + ", found " + matches.size() + " results");
			return new ListValuePage(matches, pageNum, pageSize, new IdentifiedValueListExtractor());
		}
		logger.debug("database noderef is null");
		return null;
	}

	@Override
	public List<NutListDataItem> getNuts(NodeRef databaseFile, String id) {
		List<NutListDataItem> ret = new ArrayList<>();
		logger.debug("getting nut list for RM of id "+id+" in file "+nodeService.getProperty(databaseFile, ContentModel.PROP_NAME));
		String[] headerRow = getHeaderRow(databaseFile);
		int idColumn = extractIdentifierColumnIndex(headerRow);
		int nameColumn = extractNameColumnIndex(headerRow);
		PropertyFormats propertyFormats = new PropertyFormats(true);
		
		String[] values = getLineByIndex(databaseFile, id, idColumn);
		for (int i = 0; i < headerRow.length; ++i) {
			
			if (!"COLUMNS".equals(headerRow[i]) && (i != idColumn) && (i != nameColumn) && (!isInDictionary(headerRow[i]) || !headerRow[i].contains("_"))) {
				NodeRef nutNodeRef = getNutNodeRef(headerRow[i]);
				if (nutNodeRef != null) {
					try {
						String nutValueToken = values[i];
						Number nutValue = null;
						if (nutValueToken != null) {
							nutValue = propertyFormats.parseDecimal(nutValueToken);
						}

						ret.add(new NutListDataItem(null, nutValue != null ? nutValue.doubleValue() : null, null, null, null, null, nutNodeRef,
								false));
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
			
			ProductData productData = alfrescoRepository.findOne(productNode);
			RawMaterialData dat = (RawMaterialData) productData;	
			
			if (dat != null) {
				
				String name = getProductName(file, idSplit, idColumn, nameColumn);

				if (name != null) {
					dat.setName(PropertiesHelper.cleanName(name));
				}

				for(int i=1; i<headerRow.length; ++i){
					if(isInDictionary(headerRow[i]) || (headerRow[i].contains("_") && (isInDictionary(headerRow[i].split("_")[0])) ) /* && nodeService.getProperties(productNode).containsKey(QName.createQName(headerRow[i], NamespaceService))*/){
						String value = extractValueById(file, idSplit, i);
						logger.info("setting property qnamed  \""+headerRow[i]+"\" to value  \""+value+"\"");
						QName attributeQName = QName.createQName(headerRow[i], NamespaceService);

						if(PLMModel.TYPE_SUPPLIER.equals(attributeQName)) {
							//supplier
							BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_SUPPLIER).andPropEquals(PLMModel.TYPE_SUPPLIER, extractValueById(file, idSplit, i));
							List<NodeRef> suppliers = queryBuilder.list();
							if(!suppliers.isEmpty()){
								logger.info("Setting suppliers to "+suppliers);
								dat.setSuppliers(suppliers);
							}
						} else if(PLMModel.PROP_PRODUCT_HIERARCHY1.equals(attributeQName) || PLMModel.PROP_PRODUCT_HIERARCHY2.equals(attributeQName)){
							//hierarchy
							/*HierarchyService hierarchyService;
							hierarchyService.getHierarchiesByPath(HIERARCHY_RAWMATERIAL_PATH, null, value);*/
						} else if(dictionaryDAO.getProperty(attributeQName) != null){

							nodeService.setProperty(productNode, QName.createQName(headerRow[i], NamespaceService), value);
						}
					}
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

		
		try (CSVReader csvReader = getCSVReaderFromNodeRef(file)) {

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
		
		try (CSVReader csvReader = getCSVReaderFromNodeRef(file)){

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
			csvReader.close();
			return res;
		} catch (IOException e) {
			throw new RuntimeException("Error reading product name", e);
		}
	}

	private NodeRef getNutNodeRef(String nutName) {
		logger.debug("Finding nodeRef for nut named \""+nutName+"\"");
		List<NodeRef> foundNuts = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_NUT).andPropEquals(BeCPGModel.PROP_CHARACT_NAME, nutName).list();

		if(!foundNuts.isEmpty()){
			NodeRef nutsListNodeRef = foundNuts.get(0);

			logger.debug("Found one");
			return nutsListNodeRef;
			
		} else {
			logger.debug("found no nodeRef for nut " + nutName + ", it might not be in the system.");
			return null;
		}
	}

	private String[] getHeaderRow(NodeRef file) {
		try (CSVReader csvReader = getCSVReaderFromNodeRef(file)){
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

	private boolean nameMatches(String query, String name) {
		return BeCPGQueryHelper.isQueryMatch(query, name, dictionaryDAO);
	}
	
	private boolean isInDictionary(String str){
			return (dictionaryDAO.getProperty(QName.createQName(str, NamespaceService)) != null 
					|| dictionaryDAO.getAssociation(QName.createQName(str, NamespaceService)) != null) ;
	}
	
	private String extractValueById(NodeRef file, String id, int column){
		String[] line = getLineByIndex(file, id, extractIdentifierColumnIndex(getHeaderRow(file)));
		
		if(line != null && line.length > column){
			return line[column];
		} else {
			throw new RuntimeException("error extracting value from cell from id "+id+" and column "+column);
		}
	}

}
