/*
 * 
 */
package fr.becpg.repo.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.config.mapping.AbstractAttributeMapping;

// TODO: Auto-generated Javadoc
/**
 * Context used during import.
 * 
 * @author querephi
 */
public class ImportContext {
	
	private ImportFileReader importFileReader;
	
	
	private static final String FORMAT_DATE_FRENCH = "dd/MM/yyyy";
	private static final String FORMAT_DATE_ENGLISH = "yyyy/MM/dd";
	private static final String MSG_ERROR_IMPORT_LINE = "import_service.error.err_import_line";
	private static final String MSG_INFO_IMPORT_LINE = "import_service.info.import_line";

	private NodeRef parentNodeRef;

	private NodeRef entityNodeRef;
	
	private QName listType;
	
	private QName type;

	private QName entityType;

	private boolean doUpdate;

	private boolean stopOnFirstError = true;

	private boolean deleteDataList = false;

	private String importFileName;

	private ImportType importType = ImportType.Node;

	private List<QName> disabledPolicies = new ArrayList<>();

	private PropertyFormats propertyFormats;

	private List<AbstractAttributeMapping> columns = new ArrayList<>();

	private Map<QName, ClassMapping> classMappings = new HashMap<>();

	private Map<String, NodeRef> cacheNodes = new HashMap<>();

	private List<String> log = new ArrayList<>();

	private String path;

	private String docsBasePath;

	private boolean isSiteDocLib = false;


	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}

	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
	}

	
	public QName getListType() {
		return listType;
	}

	public void setListType(QName listType) {
		this.listType = listType;
	}
	

	public QName getType() {
		return type;
	}

	public void setType(QName type) {
		this.type = type;
	}

	public QName getEntityType() {
		return entityType;
	}

	public void setEntityType(QName entityType) {
		this.entityType = entityType;
	}


	public boolean isDoUpdate() {
		return doUpdate;
	}


	public void setDoUpdate(boolean doUpdate) {
		this.doUpdate = doUpdate;
	}

	public boolean isStopOnFirstError() {
		return stopOnFirstError;
	}

	public void setStopOnFirstError(boolean stopOnFirstError) {
		this.stopOnFirstError = stopOnFirstError;
	}

	public String getImportFileName() {
		return importFileName;
	}

	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}


	public ImportType getImportType() {
		return importType;
	}


	public void setImportType(ImportType importType) {
		this.importType = importType;
	}

	public List<QName> getDisabledPolicies() {
		return disabledPolicies;
	}

	public void setDisabledPolicies(List<QName> disabledPolicies) {
		this.disabledPolicies = disabledPolicies;
	}

	public PropertyFormats getPropertyFormats() {
		return propertyFormats;
	}

	public void setPropertyFormats(PropertyFormats propertyFormats) {
		this.propertyFormats = propertyFormats;
	}


	public List<AbstractAttributeMapping> getColumns() {
		return columns;
	}

	
	public void setColumns(List<AbstractAttributeMapping> columns) {
		this.columns = columns;
	}


	public Map<QName, ClassMapping> getClassMappings() {
		return classMappings;
	}

	
	public void setClassMappings(Map<QName, ClassMapping> classMappings) {
		this.classMappings = classMappings;
	}

	public Map<String, NodeRef> getCacheNodes() {
		return cacheNodes;
	}

	public void setCacheNodes(Map<String, NodeRef> cacheNodes) {
		this.cacheNodes = cacheNodes;
	}

	

	public List<String> getLog() {
		return log;
	}

	public void setLog(List<String> log) {
		this.log = log;
	
	}

	public boolean isSiteDocLib() {
		return isSiteDocLib;
	}

	public void setSiteDocLib(boolean isSiteDocLib) {
		this.isSiteDocLib = isSiteDocLib;
	}

	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public void setEntityNodeRef(NodeRef entityNodeRef) {
		this.entityNodeRef = entityNodeRef;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDocsBasePath() {
		return docsBasePath;
	}

	public void setDocsBasePath(String docsBasePath) {
		this.docsBasePath = docsBasePath;
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param csvReader
	 * @throws IOException
	 */
	public ImportContext() {
		propertyFormats = new PropertyFormats(true);
		
		String dateFormat = (Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE)) ? FORMAT_DATE_FRENCH
				: FORMAT_DATE_ENGLISH;
		propertyFormats.setDateFormat(dateFormat);
		
	}

	

	public ImportFileReader getImportFileReader() {
		return importFileReader;
	}

	public void setImportFileReader(ImportFileReader importFileReader) {
		this.importFileReader = importFileReader;
	}
	
	


	private int importIndex = 0;


	public int getImportIndex() {
		return importIndex;
	}

	public void setImportIndex(int importIndex) {
		this.importIndex = importIndex;
	}
	


	public String[] nextLine() {
		
		return importFileReader.getLineAt(importIndex++);
		
	}

	final Set<NodeRef> deletedDataListEntityNodeRefs = new HashSet<>();

	public boolean isDeleteDataList(NodeRef entityNodeRef) {
		if (deleteDataList && !deletedDataListEntityNodeRefs.contains(entityNodeRef)) {

			deletedDataListEntityNodeRefs.add(entityNodeRef);
			return true;
		}

		return false;
	}

	public void setDeleteDataList(boolean deleteDataList) {
		this.deleteDataList = deleteDataList;
	}

	public String markCurrLineError(Exception e) {
		
		// store the exception and continue import...
		String error = I18NUtil.getMessage(MSG_ERROR_IMPORT_LINE, importFileName,importIndex,
				new Date(), e.getMessage());

		getLog().add(error);
		importFileReader.reportError(importIndex-1, e.getMessage(), columns.size());
		
		return error;
		
	}

	public String markCurrLineSuccess() {
		importFileReader.reportSuccess(importIndex-1, columns.size());
		
		return I18NUtil.getMessage(MSG_INFO_IMPORT_LINE, importIndex);
	}

	

}
