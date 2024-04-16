/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * beCPG constants for repository
 *
 * @author querephi
 * @version $Id: $Id
 */
public class RepoConsts
{


	private RepoConsts() {
		//Private constructor
	}
	
	/** Constant <code>FULL_PATH_THUMBNAIL="/cm:System/cm:Icons"</code> */
	public static final String FULL_PATH_THUMBNAIL = "/cm:System/cm:Icons";	
    
	/*-- Path --*/	
	/** Constant <code>PATH_SEPARATOR="/"</code> */
	public static final String PATH_SEPARATOR 	= "/";

	/** Constant <code>PATH_SYSTEM="System"</code> */
	public static final String PATH_SYSTEM = "System";
	/** Constant <code>PATH_CHARACTS="Characts"</code> */
	public static final String PATH_CHARACTS = "Characts";
	/** Constant <code>PATH_LISTS="Lists"</code> */
	public static final String PATH_LISTS = "Lists";
	/** Constant <code>PATH_LINKED_LISTS="LinkedLists"</code> */
	public static final String PATH_LINKED_LISTS 	= "LinkedLists";
	/** Constant <code>PATH_ENTITY_TEMPLATES="EntityTemplates"</code> */
	public static final String PATH_ENTITY_TEMPLATES = "EntityTemplates";
	//Notification
	/** Constant <code>PATH_NOTIFICATIONS="Notifications"</code> */
	public static final String PATH_NOTIFICATIONS = "Notifications";
	
	public static final String FORMULATION_ERRORS_NOTIFICATION = "formulationErrorsNotification";
	
	public static final String OBSOLETE_DOCUMENTS_NOTIFICATION = "obsoleteDocumentsNotification";
	
	public static final String IN_PROGRESS_PROJECTS_NOTIFICATION = "inProgressProjectsNotification";
	
	public static final String VALIDATED_PRODUCTS_NOTIFICATION = "validatedProductsNotification";
	
	public static final String VALIDATED_AND_UPDATED_PRODUCTS_NOTIFICATION = "validatedAndUpdatedProductsNotification";
	
	public static final String ARCHIVED_PRODUCTS_NOTIFICATION = "archivedProductsNotification";
			
	//Security
	/** Constant <code>PATH_SECURITY="Security"</code> */
	public static final String PATH_SECURITY = "Security";
	
	/** Constant <code>SCRIPTS_FULL_PATH="/app:company_home/app:dictionary/app:sc"{trunked}</code> */
	public static final String SCRIPTS_FULL_PATH = "/app:company_home/app:dictionary/app:scripts";
	
	//Icons
	/** Constant <code>PATH_ICON="Icons"</code> */
	public static final String PATH_ICON = "Icons";
	
	//Hierarchy
	/** Constant <code>PATH_PRODUCT_HIERARCHY="ProductHierarchy"</code> */
	public static final String PATH_PRODUCT_HIERARCHY = "ProductHierarchy";

	// Product folder
	/** Constant <code>PATH_IMAGES="Images"</code> */
	public static final String PATH_IMAGES = "Images";
	/** Constant <code>PATH_DOCUMENTS="Documents"</code> */
	public static final String PATH_DOCUMENTS = "Documents";
	

	public static final String PATH_SUPPLIER_DOCUMENTS= "SupplierDocuments";
	

	public static final String PATH_SUPPLIER_ENTITIES = "SupplierEntities";
	
	/** Constant <code>PATH_BRIEF="Brief"</code> */
	public static final String PATH_BRIEF = "Brief";
	/** Constant <code>PATH_LOGO_IMAGE="logoimage"</code> */
	public static final String PATH_LOGO_IMAGE = "logoimage";
	/** Constant <code>PATH_AUTO_NUM="AutoNum"</code> */
	public static final String PATH_AUTO_NUM = "AutoNum";
	
	// reports
	/** Constant <code>PATH_REPORTS="Reports"</code> */
	public static final String PATH_REPORTS	= "Reports";
	/** Constant <code>PATH_REPORTS_COMPARE_ENTITIES="CompareProducts"</code> */
	public static final String PATH_REPORTS_COMPARE_ENTITIES = "CompareProducts";
	/** Constant <code>PATH_REPORTS_EXPORT_SEARCH="ExportSearch"</code> */
	public static final String PATH_REPORTS_EXPORT_SEARCH = "ExportSearch";
	

	/** Constant <code>PATH_REPORT_PARAMS="ReportParams"</code> */
	public static final String PATH_REPORT_PARAMS = "ReportParams";

	/** Constant <code>PATH_REPORT_KINDLIST="ReportKindList"</code> */
	public static final String PATH_REPORT_KINDLIST = "ReportKindList";
	
	// olap
	/** Constant <code>PATH_OLAP_QUERIES="OlapQueries"</code> */
	public static final String PATH_OLAP_QUERIES = "OlapQueries";
	
	/*-- DataLists --*/
	/** Constant <code>CONTAINER_DATALISTS="DataLists"</code> */
	public static final String CONTAINER_DATALISTS = "DataLists";	
	
	/** Constant <code>ARCHIVE_STORE</code> */
	public static final StoreRef ARCHIVE_STORE =  new StoreRef(StoreRef.PROTOCOL_ARCHIVE, "SpacesStore");
	/** Constant <code>SPACES_STORE</code> */
	public static final StoreRef SPACES_STORE =  new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
	/** Constant <code>VERSION_STORE</code> */
	public static final StoreRef VERSION_STORE =  new StoreRef(VersionBaseModel.STORE_PROTOCOL, Version2Model.STORE_ID);
	
	/*-- Regex --*/	
	/** Constant <code>REGEX_NON_NEGATIVE_INTEGER_FIELD="^\\d*$"</code> */
	public static final String REGEX_NON_NEGATIVE_INTEGER_FIELD = "^\\d*$";

	/*-- Lucene --*/
	/** Constant <code>MAX_RESULTS_SINGLE_VALUE</code> */
	public static final Integer MAX_RESULTS_SINGLE_VALUE = 1;
	
	/** Constant <code>MAX_RESULTS_256</code> */
	public static final Integer MAX_RESULTS_256 = 256;
	
	/** Constant <code>MAX_RESULTS_1000</code> */
	public static final Integer MAX_RESULTS_1000 = 1000;
	
	/** Constant <code>MAX_RESULTS_UNLIMITED</code> */
	public static final Integer MAX_RESULTS_UNLIMITED = -1;
	
	/** Constant <code>MAX_SUGGESTIONS</code> */
	public static final Integer MAX_SUGGESTIONS = 100;

	/** Constant <code>MAX_RESULTS_5000</code> */
	public static final Integer MAX_RESULTS_5000 = 5000;
	
	public static final Integer MAX_RESULTS_1000000 = 1000000;
	

	
	public static final Map<String, Boolean> DEFAULT_SORT = new LinkedHashMap<>();
	
	static {

		DEFAULT_SORT.put("@bcpg:sort", true);
		DEFAULT_SORT.put("@cm:created", true);
		
	}
	
	/*-- IHM --*/
	/** Constant <code>LABEL_SEPARATOR=", "</code> */
	public static final String LABEL_SEPARATOR = ", ";
	
	/*-- Report --*/
	/** Constant <code>REPORT_EXTENSION_BIRT="rptdesign"</code> */
	public static final String REPORT_EXTENSION_BIRT = "rptdesign";
	/** Constant <code>REPORT_EXTENSION_JXLS="jxls"</code> */
	public static final String REPORT_EXTENSION_JXLS = "jxls";
	/** Constant <code>REPORT_EXTENSION_PDF=".PDF"</code> */
	public static final String REPORT_EXTENSION_PDF = ".PDF";
	/** Constant <code>REPORT_EXTENSION_XLSX=".XLSX"</code> */
	public static final String REPORT_EXTENSION_XLSX = ".XLSX";
	/** Constant <code>EXTENSION_LOG=".log"</code> */
	public static final String EXTENSION_LOG = ".log";
	
	// FORMAT
	/** Constant <code>MULTI_VALUES_SEPARATOR=","</code> */
	public static final String MULTI_VALUES_SEPARATOR = ",";
	
	//Site containers
	/** Constant <code>CONTAINER_DOCUMENT_LIBRARY="documentLibrary"</code> */
	public static final String CONTAINER_DOCUMENT_LIBRARY = "documentLibrary";
	
	//Model
	/** Constant <code>MODEL_PREFIX_SEPARATOR=":"</code> */
	public static final String MODEL_PREFIX_SEPARATOR = ":";

	// Permissions
	/** Constant <code>PERMISSION_CONSUMER="Consumer"</code> */
	public static final String PERMISSION_CONSUMER = "Consumer";
	/** Constant <code>PERMISSION_EDITOR="Editor"</code> */
	public static final String PERMISSION_EDITOR = "Editor";
	/** Constant <code>PERMISSION_CONTRIBUTOR="Contributor"</code> */
	public static final String PERMISSION_CONTRIBUTOR = "Contributor";
	/** Constant <code>PERMISSION_COLLABORATOR="Collaborator"</code> */
	public static final String PERMISSION_COLLABORATOR = "Collaborator";

	// Configuration
	/** Constant <code>DEFAULT_LEVEL=1</code> */
	public static final int DEFAULT_LEVEL = 1;
	/** Constant <code>MAX_DEPTH_LEVEL=256</code> */
	public static final int MAX_DEPTH_LEVEL = 256;
	
	/*
	 * Email templates
	 */
	/** Constant <code>EMAIL_NEW_USER_TEMPLATE="/app:company_home/app:dictionary/app:em"{trunked}</code> */
	public static final String EMAIL_NEW_USER_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:importuser-email.ftl";
	/** Constant <code>EMAIL_ASYNC_ACTIONS_TEMPLATE="/app:company_home/app:dictionary/app:em"{trunked}</code> */
	public static final String EMAIL_ASYNC_ACTIONS_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:asynchrone-actions-email.html.ftl";
	/** Constant <code>EMAIL_NOTIF_RULE_LIST_TEMPLATE="/app:company_home/app:dictionary/app:em"{trunked}</code> */
	public static final String EMAIL_NOTIF_RULE_LIST_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:notification-rule-list-email.html.ftl";
	
	/*
	 * Asynchrone actions by mail 
	 */
	/** Constant <code>ARG_ACTION_URL="url"</code> */
	public static final String ARG_ACTION_URL = "url";
	/** Constant <code>ARG_ACTION_BODY="mailBody"</code> */
	public static final String ARG_ACTION_BODY = "mailBody";
	/** Constant <code>ARG_ACTION_STATE="actionState"</code> */
	public static final String ARG_ACTION_STATE = "actionState";
	/** Constant <code>ARG_ACTION_RUN_TIME="runTime"</code> */
	public static final String ARG_ACTION_RUN_TIME = "runTime";

	/**
	 * ISO charset
	 */
	public static final String ISO_CHARSET = "ISO-8859-15";
	
	/** Constant <code>DATA_LISTS_PAGESIZE</code> */
	public static final Integer DATA_LISTS_PAGESIZE = 25;
	

	// Sort
	/** Constant <code>SORT_DEFAULT_STEP=100</code> */
	public static final  int SORT_DEFAULT_STEP = 100;
	/** Constant <code>SORT_INSERTING_STEP=1</code> */
	public static final  int SORT_INSERTING_STEP = 1;
	
	
	//WUsed
	/** Constant <code>WUSED_PREFIX="WUsed"</code> */
	public static final  String WUSED_PREFIX = "WUsed";
	/** Constant <code>WUSED_SEPARATOR="-"</code> */
	public static final  String WUSED_SEPARATOR = "-";
	
	//Smart content
	/** Constant <code>SMART_CONTENT_PREFIX="SmartContent"</code> */
	public static final String SMART_CONTENT_PREFIX = "SmartContent";
	
	//View
	/** Constant <code>CUSTOM_VIEW_PREFIX="View"</code> */
	public static final  String CUSTOM_VIEW_PREFIX = "View";

	/** Constant <code>VIEW_PROPERTIES="CUSTOM_VIEW_PREFIX+-properties"</code> */
	public static final String VIEW_PROPERTIES = CUSTOM_VIEW_PREFIX+"-properties";
	/** Constant <code>VIEW_REPORTS="CUSTOM_VIEW_PREFIX+-reports"</code> */
	public static final String VIEW_REPORTS = CUSTOM_VIEW_PREFIX+"-reports";
	/** Constant <code>VIEW_DOCUMENTS="CUSTOM_VIEW_PREFIX+-documents"</code> */
	public static final String VIEW_DOCUMENTS = CUSTOM_VIEW_PREFIX+"-documents";
	
	//Version
	/** Constant <code>VERSION_NAME_DELIMITER=" v"</code> */
	public static final String VERSION_NAME_DELIMITER = " v";
	/** Constant <code>INITIAL_VERSION="1.0"</code> */
	public static final String INITIAL_VERSION = "1.0";
	/** Constant <code>VERSION_DELIMITER="."</code> */
	public static final String VERSION_DELIMITER = ".";
	
	//History
	/** Constant <code>ENTITIES_HISTORY_NAME="entitiesHistory"</code> */
	public static final String ENTITIES_HISTORY_NAME = "entitiesHistory";
	/** Constant <code>ENTITIES_HISTORY_XPATH="/bcpg:entitiesHistory"</code> */
	public static final String ENTITIES_HISTORY_XPATH = "/bcpg:entitiesHistory";
	
	//Formats
	/** Constant <code>FORMAT_CSV="csv"</code> */
	public static final String FORMAT_CSV = "csv";
	/** Constant <code>FORMAT_XLSX="xlsx"</code> */
	public static final String FORMAT_XLSX = "xlsx";

	/** Constant <code>PATH_LICENSE="license"</code> */
	public static final String PATH_LICENSE = "license";

	/** Constant <code>SUPPORTED_UI_LOCALES="en,en_US,fr,sv_SE,fi,es,it,pt_BR,ru,de,tr,ja_JP"</code> */
	public static final String SUPPORTED_UI_LOCALES =  "en,en_US,fr,sv_SE,fi,es,it,pt_BR,ru,de,tr,ja_JP";
	
	/** Constant <code>CATALOGS_PATH="/app:company_home/cm:System/cm:Property"{trunked}</code> */
	public static final String CATALOGS_PATH = "/app:company_home/cm:System/cm:PropertyCatalogs";


	
}
