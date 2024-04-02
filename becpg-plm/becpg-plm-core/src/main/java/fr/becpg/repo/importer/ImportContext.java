/*
 *
 */
package fr.becpg.repo.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.config.mapping.AbstractAttributeMapping;

/**
 * Context used during import.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class ImportContext {

	private static final int MAX_IMPORT_PRECISION = 9;

	private ImportFileReader importFileReader;

	private static final String FORMAT_DATE_FRENCH = "dd/MM/yyyy";
	private static final String FORMAT_DATE_ENGLISH = "yyyy/MM/dd";
	private static final String FORMAT_DATETIME_FRENCH = "dd/MM/yyyy HH:mm:ss";
	private static final String FORMAT_DATETIME_ENGLISH = "yyyy/MM/dd HH:mm:ss";
	private static final String MSG_ERROR_IMPORT_LINE = "import_service.error.err_import_line";
	private static final String MSG_INFO_IMPORT_LINE = "import_service.info.import_line";

	private NodeRef nodeRef;
	
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

	private List<AbstractAttributeMapping> columns = new LinkedList<>();

	private Map<QName, ClassMapping> classMappings = new HashMap<>();

	private Map<String, NodeRef> cacheNodes = new HashMap<>();

	private List<String> log = new LinkedList<>();
	
	private String errorsLogs = "";

	private String path;

	private String docsBasePath;

	private boolean isSiteDocLib = false;

	public NodeRef getNodeRef() {
		return nodeRef;
	}
	
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	public String getErrorLogs() {
		return errorsLogs;
	}
	
	public void setErrorLogs(String unhandledLog) {
		this.errorsLogs = unhandledLog;
	}
	
	/**
	 * <p>Getter for the field <code>parentNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}

	/**
	 * <p>Setter for the field <code>parentNodeRef</code>.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
		this.cacheNodes.clear();
	}

	/**
	 * <p>Getter for the field <code>listType</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getListType() {
		return listType;
	}

	/**
	 * <p>Setter for the field <code>listType</code>.</p>
	 *
	 * @param listType a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setListType(QName listType) {
		this.listType = listType;
		this.cacheNodes.clear();
	}

	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getType() {
		return type;
	}

	/**
	 * <p>Setter for the field <code>type</code>.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setType(QName type) {
		this.type = type;
		this.cacheNodes.clear();
	}

	/**
	 * <p>Getter for the field <code>entityType</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getEntityType() {
		return entityType;
	}

	/**
	 * <p>Setter for the field <code>entityType</code>.</p>
	 *
	 * @param entityType a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setEntityType(QName entityType) {
		this.entityType = entityType;
		this.cacheNodes.clear();
	}

	/**
	 * <p>isDoUpdate.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDoUpdate() {
		return doUpdate;
	}

	/**
	 * <p>Setter for the field <code>doUpdate</code>.</p>
	 *
	 * @param doUpdate a boolean.
	 */
	public void setDoUpdate(boolean doUpdate) {
		this.doUpdate = doUpdate;
	}

	/**
	 * <p>isStopOnFirstError.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isStopOnFirstError() {
		return stopOnFirstError;
	}

	/**
	 * <p>Setter for the field <code>stopOnFirstError</code>.</p>
	 *
	 * @param stopOnFirstError a boolean.
	 */
	public void setStopOnFirstError(boolean stopOnFirstError) {
		this.stopOnFirstError = stopOnFirstError;
	}

	/**
	 * <p>Getter for the field <code>importFileName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getImportFileName() {
		return importFileName;
	}

	/**
	 * <p>Setter for the field <code>importFileName</code>.</p>
	 *
	 * @param importFileName a {@link java.lang.String} object.
	 */
	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}

	/**
	 * <p>Getter for the field <code>importType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.importer.ImportType} object.
	 */
	public ImportType getImportType() {
		return importType;
	}

	/**
	 * <p>Setter for the field <code>importType</code>.</p>
	 *
	 * @param importType a {@link fr.becpg.repo.importer.ImportType} object.
	 */
	public void setImportType(ImportType importType) {
		this.importType = importType;
	}

	/**
	 * <p>Getter for the field <code>disabledPolicies</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<QName> getDisabledPolicies() {
		return disabledPolicies;
	}

	/**
	 * <p>Setter for the field <code>disabledPolicies</code>.</p>
	 *
	 * @param disabledPolicies a {@link java.util.List} object.
	 */
	public void setDisabledPolicies(List<QName> disabledPolicies) {
		this.disabledPolicies = disabledPolicies;
	}

	/**
	 * <p>Getter for the field <code>propertyFormats</code>.</p>
	 *
	 * @return a {@link fr.becpg.config.format.PropertyFormats} object.
	 */
	public PropertyFormats getPropertyFormats() {
		return propertyFormats;
	}

	/**
	 * <p>Setter for the field <code>propertyFormats</code>.</p>
	 *
	 * @param propertyFormats a {@link fr.becpg.config.format.PropertyFormats} object.
	 */
	public void setPropertyFormats(PropertyFormats propertyFormats) {
		this.propertyFormats = propertyFormats;
	}

	/**
	 * <p>Getter for the field <code>columns</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<AbstractAttributeMapping> getColumns() {
		return columns;
	}

	/**
	 * <p>Setter for the field <code>columns</code>.</p>
	 *
	 * @param columns a {@link java.util.List} object.
	 */
	public void setColumns(List<AbstractAttributeMapping> columns) {
		this.columns = columns;
	}

	/**
	 * <p>Getter for the field <code>classMappings</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<QName, ClassMapping> getClassMappings() {
		return classMappings;
	}

	/**
	 * <p>Setter for the field <code>classMappings</code>.</p>
	 *
	 * @param classMappings a {@link java.util.Map} object.
	 */
	public void setClassMappings(Map<QName, ClassMapping> classMappings) {
		this.classMappings = classMappings;
	}

	/**
	 * <p>Getter for the field <code>cacheNodes</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, NodeRef> getCacheNodes() {
		return cacheNodes;
	}

	/**
	 * <p>Setter for the field <code>cacheNodes</code>.</p>
	 *
	 * @param cacheNodes a {@link java.util.Map} object.
	 */
	public void setCacheNodes(Map<String, NodeRef> cacheNodes) {
		this.cacheNodes = cacheNodes;
	}

	/**
	 * <p>Getter for the field <code>log</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getLog() {
		return log;
	}

	/**
	 * <p>Setter for the field <code>log</code>.</p>
	 *
	 * @param log a {@link java.util.List} object.
	 */
	public void setLog(List<String> log) {
		this.log = log;

	}

	/**
	 * <p>isSiteDocLib.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSiteDocLib() {
		return isSiteDocLib;
	}

	/**
	 * <p>setSiteDocLib.</p>
	 *
	 * @param isSiteDocLib a boolean.
	 */
	public void setSiteDocLib(boolean isSiteDocLib) {
		this.isSiteDocLib = isSiteDocLib;
	}

	/**
	 * <p>Getter for the field <code>entityNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	/**
	 * <p>Setter for the field <code>entityNodeRef</code>.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setEntityNodeRef(NodeRef entityNodeRef) {
		this.entityNodeRef = entityNodeRef;
	}

	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * <p>Setter for the field <code>path</code>.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * <p>Getter for the field <code>docsBasePath</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDocsBasePath() {
		return docsBasePath;
	}

	/**
	 * <p>Setter for the field <code>docsBasePath</code>.</p>
	 *
	 * @param docsBasePath a {@link java.lang.String} object.
	 */
	public void setDocsBasePath(String docsBasePath) {
		this.docsBasePath = docsBasePath;
	}

	/**
	 * <p>Constructor for ImportContext.</p>
	 */
	public ImportContext() {
		propertyFormats = new PropertyFormats(true, MAX_IMPORT_PRECISION);

		String dateFormat = (Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE)) ? FORMAT_DATE_FRENCH
				: FORMAT_DATE_ENGLISH;
		propertyFormats.setDateFormat(dateFormat);
		
		String datetimeFormat = (Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE)) ? FORMAT_DATETIME_FRENCH
				: FORMAT_DATETIME_ENGLISH;
		propertyFormats.setDatetimeFormat(datetimeFormat);

	}

	/**
	 * <p>Getter for the field <code>importFileReader</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.importer.ImportFileReader} object.
	 */
	public ImportFileReader getImportFileReader() {
		return importFileReader;
	}

	/**
	 * <p>Setter for the field <code>importFileReader</code>.</p>
	 *
	 * @param importFileReader a {@link fr.becpg.repo.importer.ImportFileReader} object.
	 */
	public void setImportFileReader(ImportFileReader importFileReader) {
		this.importFileReader = importFileReader;
	}

	private int importIndex = 0;

	/**
	 * <p>Getter for the field <code>importIndex</code>.</p>
	 *
	 * @return a int.
	 */
	public int getImportIndex() {
		return importIndex;
	}

	/**
	 * <p>Setter for the field <code>importIndex</code>.</p>
	 *
	 * @param importIndex a int.
	 */
	public void setImportIndex(int importIndex) {
		this.importIndex = importIndex;
	}

	/**
	 * <p>nextLine.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] nextLine() {
		String[] line = {};
		try {
			line = importFileReader.getLineAt(importIndex++, columns);
		} catch (ImporterException e) {
			markCurrLineError(e);
		}

		return line;

	}

	final Set<NodeRef> deletedDataListEntityNodeRefs = new HashSet<>();

	/**
	 * <p>isDeleteDataList.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	public boolean isDeleteDataList(NodeRef entityNodeRef) {
		if (deleteDataList && !deletedDataListEntityNodeRefs.contains(entityNodeRef)) {

			deletedDataListEntityNodeRefs.add(entityNodeRef);
			return true;
		}

		return false;
	}

	/**
	 * <p>Setter for the field <code>deleteDataList</code>.</p>
	 *
	 * @param deleteDataList a boolean.
	 */
	public void setDeleteDataList(boolean deleteDataList) {
		this.deleteDataList = deleteDataList;
	}

	/**
	 * <p>markCurrLineError.</p>
	 *
	 * @param e a {@link java.lang.Exception} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String markCurrLineError(Exception e) {

		// store the exception and continue import...
		String error = I18NUtil.getMessage(MSG_ERROR_IMPORT_LINE, importFileName, importIndex, new Date(), e.getMessage());

		getLog().add(error);
		importFileReader.reportError(importIndex - 1, e.getMessage(), columns.size());

		return error;

	}

	/**
	 * <p>markCurrLineSuccess.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String markCurrLineSuccess() {
		importFileReader.reportSuccess(importIndex - 1, columns.size());

		return I18NUtil.getMessage(MSG_INFO_IMPORT_LINE, importIndex);
	}

}
