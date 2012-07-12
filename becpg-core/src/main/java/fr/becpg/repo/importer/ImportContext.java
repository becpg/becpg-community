/*
 * 
 */
package fr.becpg.repo.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.config.mapping.AbstractAttributeMapping;

// TODO: Auto-generated Javadoc
/**
 * Context used during import.
 *
 * @author querephi
 */
public class ImportContext {

	/** The parent node ref. */
	private NodeRef parentNodeRef;	
	

	/** The type. */
	private QName type;
	
	/** The do update. */
	private boolean doUpdate;	
	
	private boolean stopOnFirstError;
	
	private String importFileName;
	
	/** The import type. */
	private ImportType importType = ImportType.Node;
	
	private List<QName> disabledPolicies = new ArrayList<QName>();
	
	private PropertyFormats propertyFormats;		

	/** The columns. */
	private List<AbstractAttributeMapping> columns = new ArrayList<AbstractAttributeMapping>();
	
	/** The class mappings. */
	private Map<QName, ClassMapping>classMappings = new HashMap<QName, ClassMapping>();	
	
	private Map<String, NodeRef> cacheNodes = new HashMap<String, NodeRef>();
		
	private List<String[]> lines = null;
	
	private int importIndex = 0; 
	
	private List<String> log = new ArrayList<String>();
	
	private String path;
	
	private boolean requiresNewTransaction = false;
	
	/** indicate the the import is in a site document library **/
	private boolean isSiteDocLib = false;
	
	/**
	 * Gets the parent node ref.
	 *
	 * @return the parent node ref
	 */
	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}
	
	/**
	 * Sets the parent node ref.
	 *
	 * @param parentNodeRef the new parent node ref
	 */
	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
	}
	
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public QName getType() {
		return type;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(QName type) {
		this.type = type;
	}
	
	/**
	 * Checks if is do update.
	 *
	 * @return true, if is do update
	 */
	public boolean isDoUpdate() {
		return doUpdate;
	}
	
	/**
	 * Sets the do update.
	 *
	 * @param doUpdate the new do update
	 */
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

	/**
	 * Gets the import type.
	 *
	 * @return the import type
	 */
	public ImportType getImportType() {
		return importType;
	}
	
	/**
	 * Sets the import type.
	 *
	 * @param importType the new import type
	 */
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

	/**
	 * Gets the columns.
	 *
	 * @return the columns
	 */
	public List<AbstractAttributeMapping> getColumns() {
		return columns;
	}
	
	/**
	 * Sets the columns.
	 *
	 * @param columns the new columns
	 */
	public void setColumns(List<AbstractAttributeMapping> columns) {
		this.columns = columns;
	}	
	
	/**
	 * Gets the class mappings.
	 *
	 * @return the class mappings
	 */
	public Map<QName, ClassMapping> getClassMappings() {
		return classMappings;
	}
	
	/**
	 * Sets the class mappings.
	 *
	 * @param classMappings the class mappings
	 */
	public void setClassMappings(Map<QName, ClassMapping> classMappings) {
		this.classMappings = classMappings;
	}

	public Map<String, NodeRef> getCacheNodes() {
		return cacheNodes;
	}
	
	public void setCacheNodes(Map<String, NodeRef> cacheNodes) {
		this.cacheNodes = cacheNodes;
	}

	public void setCsvReader(CSVReader csvReader) throws IOException {
		
		if(csvReader != null)
			lines = csvReader.readAll();
	}
	
	public int getImportIndex() {
		return importIndex;
	}

	public void setImportIndex(int importIndex) {
		this.importIndex = importIndex;
	}
			
	public List<String> getLog() {
		return log;
	}

	public void setLog(List<String> log) {
		this.log = log;
	}
	
	public boolean isRequiresNewTransaction() {
		return requiresNewTransaction;
	}

	public void setRequiresNewTransaction(boolean requiresNewTransaction) {
		this.requiresNewTransaction = requiresNewTransaction;
	}

	public boolean isSiteDocLib() {
		return isSiteDocLib;
	}

	public void setSiteDocLib(boolean isSiteDocLib) {
		this.isSiteDocLib = isSiteDocLib;
	}

	public List<String[]> getLines() {
		return lines;
	}

	/**
	 * Constructor
	 * @param csvReader
	 * @throws IOException
	 */
	public ImportContext() {
		
		propertyFormats = new PropertyFormats(true);

	}
	
	/**
	 * Read the current line
	 * @return
	 */
	public String[] readLine(){
		
		String[] line = null;
		
		if(lines != null && importIndex < lines.size()){
			line = lines.get(importIndex);
		}		
		
		return line;
	}		

	/**
	 * the line in the csv file
	 * @return
	 */
	public int getCSVLine() {
		return importIndex + 1;
	}
	
	public int goToPreviousLine(){
		return importIndex--;
	}
	
	public int goToNextLine(){
		return importIndex++;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	
}
