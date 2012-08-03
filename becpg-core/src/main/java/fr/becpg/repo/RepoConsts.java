/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * beCPG constants for repository
 * @author querephi
 *
 */
public class RepoConsts {

	/*-- Path query --*/			
	public static final String PATH_QUERY_SUGGEST_VALUE = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"bcpg:listValue\" +@cm\\:name:(%s) ";
	public static final String PATH_QUERY_SUGGEST_VALUE_ALL  = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"bcpg:listValue\" ";
	public static final String PATH_QUERY_SUGGEST_LKV_VALUE_BY_NAME  = " +PATH:\"/app:company_home/%s//*\" +TYPE:\"bcpg:linkedValue\" +@bcpg\\:lkvValue:\"%s\" ";
	public static final String PATH_QUERY_SUGGEST_LKV_VALUE_ALL  = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"bcpg:linkedValue\" +@bcpg\\:parentLevel:\"%s\" ";
	public static final String PATH_QUERY_SUGGEST_LKV_VALUE_ALL_ROOT  = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"bcpg:linkedValue\" +ISNULL:bcpg\\:parentLevel  ";
	public static final String PATH_QUERY_SUGGEST_LKV_VALUE = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"bcpg:linkedValue\" +@bcpg\\:parentLevel:\"%s\"  +@bcpg\\:lkvValue:\"%s\" ";
	public static final String PATH_QUERY_SUGGEST_LKV_VALUE_ROOT = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"bcpg:linkedValue\" +ISNULL:bcpg\\:parentLevel  +@bcpg\\:lkvValue:\"%s\" ";
	//public static final String QUERY_SUGGEST_PRODUCT_BY_NAME = " +TYPE:\"bcpg:product\" +@cm\\:name:(%s) -@bcpg\\:productState:%s -@bcpg\\:productState:%s AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\" ";
	//public static final String QUERY_SUGGEST_PRODUCT_BY_CODE = " +TYPE:\"bcpg:product\" +@bcpg\\:code:%s -@bcpg\\:productState:%s -@bcpg\\:productState:%s AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\" ";
	//public static final String QUERY_SUGGEST_PRODUCT_ALL = " +TYPE:\"bcpg:product\" -@bcpg\\:productState:%s -@cm\\:productState:%s AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\" ";
	public static final String QUERY_SUGGEST_TARGET_BY_NAME = " +TYPE:\"%s\"  +@cm\\:name:(%s) AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\" ";
	public static final String QUERY_SUGGEST_TARGET_ALL = " +TYPE:\"%s\" AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\" ";
	public static final String QUERY_SUGGEST_TARGET_BY_CODE = "  +TYPE:\"%s\"  +@bcpg\\:code:%s AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\" ";
	public static final String QUERY_FILTER_PRODUCT_STATE = " -@bcpg\\:productState:%s -@bcpg\\:productState:%s";
	public static final String PATH_QUERY_LIST_CONSTRAINTS = "+PATH:\"/app:company_home/%s/*\" +TYPE:\"%s\"";
	public static final String PATH_QUERY_IMPORT_FAILED_FOLDER = " +PATH:\"/app:company_home/cm:Exchange/cm:Import/cm:ImportFailed\"";
	public static final String PATH_QUERY_IMPORT_SUCCEEDED_FOLDER = " +PATH:\"/app:company_home/cm:Exchange/cm:Import/cm:ImportSucceeded\"";	
	public static final String PATH_QUERY_REPORT_COMPARE_ENTITIES = "+PATH:\"/app:company_home/cm:System/cm:Reports/cm:CompareProducts/*\" +@cm\\:name:\"CompareProducts*rptdesign\"";	
	public static final String QUERY_CHARACT_BY_TYPE_AND_NAME = " +TYPE:\"%s\" +@cm\\:name:\"%s\"";	
	public static final String QUERY_AUTONUM = " +TYPE:\"bcpg:autoNum\" +@bcpg\\:autoNumClassName:\"%s\" +@bcpg\\:autoNumPropertyName:\"%s\"";
	public static final String PATH_QUERY_IMPORT_MAPPING = " +PATH:\"/app:company_home/cm:System/cm:Exchange/cm:Import/cm:Mapping/*\" +@cm\\:name:\"%s.xml\"";
	public static final String PATH_QUERY_THUMBNAIL = " +PATH:\"/app:company_home/cm:System/cm:Icons/*\" +@cm\\:name:\"%s*\"";
	public static final String PATH_QUERY_IMPORT_TO_DO = " +PATH:\"/app:company_home/cm:Exchange/cm:Import/cm:ImportToDo\"";
	
	
	
	/*-- Path --*/	
	public static final String PATH_SEPARATOR 	= "/";

	public static final String PATH_SYSTEM = "System";
	public static final String PATH_CHARACTS = "Characts";
	public static final String PATH_LISTS = "Lists";
	public static final String PATH_ING_TYPES = "IngTypes";
	public static final String PATH_ALLERGEN_TYPES = "AllergenTypes";
	public static final String PATH_NUT_GROUPS = "NutGroups";
	public static final String PATH_NUT_TYPES = "NutTypes";
	public static final String PATH_PACKAGING_LEVELS = "PackagingLevels";
	public static final String PATH_LINKED_LISTS 	= "LinkedLists";
	public static final String PATH_NUTS = "Nuts";
	public static final String PATH_INGS = "Ings";
	public static final String PATH_ORGANOS = "Organos";
	public static final String PATH_ALLERGENS = "Allergens";
	public static final String PATH_COSTS = "Costs";
	public static final String PATH_PHYSICO_CHEM = "PhysicoChems";
	public static final String PATH_MICROBIOS = "Microbios";
	public static final String PATH_GEO_ORIGINS = "GeoOrigins";
	public static final String PATH_BIO_ORIGINS = "BioOrigins";
	public static final String PATH_SUBSIDIARIES = "Subsidiaries";	
	public static final String PATH_TRADEMARKS = "Trademarks";	
	public static final String PATH_PLANTS = "Plants";	
	public static final String PATH_CERTIFICATIONS = "Certifications";
	public static final String PATH_APPROVALNUMBERS = "ApprovalNumbers";
	public static final String PATH_PROCESSSTEPS = "ProcessSteps";
	public static final String PATH_VARIANT_CHARACTS = "VariantCharacts";
	public static final String PATH_ENTITY_TEMPLATES = "EntityTemplates";
	public static final String PATH_FOLDER_TEMPLATES = "FolderTemplates";
	public static final String PATH_PRODUCT_TEMPLATES = "ProductTemplates";
	public static final String PATH_QUALITY_TEMPLATES = "QualityTemplates";
	public static final String PATH_PRODUCTS = "Products";
	public static final String PATH_EXCHANGE = "Exchange";
	public static final String PATH_IMPORT = "Import";
	public static final String PATH_IMPORT_TO_TREAT	= "ImportToTreat";
	public static final String PATH_IMPORT_TO_DO	= "ImportToDo";
	public static final String PATH_IMPORT_SUCCEEDED = "ImportSucceeded";
	public static final String PATH_IMPORT_FAILED = "ImportFailed";		
	public static final String PATH_IMPORT_USER = "ImportUser";
	public static final String PATH_IMPORT_SAMPLES = "ImportSamples";
	public static final String PATH_MAPPING = "Mapping";
	//Quality
	public static final String PATH_QUALITY = "Quality";
	public static final String PATH_REGULATIONS = "Regulations";	
	public static final String PATH_PRODUCT_MICROBIO_CRITERIA = "ProductMicrobioCriteria";
	public static final String PATH_QUALITY_SPECIFICATIONS = "QualitySpecifications";	
	public static final String PATH_CONTROL_PLANS = "ControlPlans";
	public static final String PATH_CONTROL_POINTS = "ControlPoints";	
	public static final String PATH_CONTROL_STEPS = "ControlSteps";
	public static final String PATH_CONTROL_METHODS = "ControlMethods";
	public static final String PATH_QUALITY_CONTROLS = "QualityControls";	
	public static final String PATH_NC = "NonConformities";
	
	//Security
	public static final String PATH_SECURITY = "Security";
	
	//Icons
	public static final String PATH_ICON = "Icons";
	
	//Hierarchy
	public static final String PATH_PRODUCT_HIERARCHY = "ProductHierarchy";

	// Product folder
	public static final String PATH_IMAGES = "Images";
	public static final String PATH_DOCUMENTS = "Documents";
	public static final String PATH_BRIEF = "Brief";
	public static final String PATH_PRODUCT_IMAGE = "productimage";
	public static final String PATH_LOGO_IMAGE = "logoimage";
	public static final String PATH_COMPANIES = "Companies";
	public static final String PATH_SUPPLIERS = "Suppliers";
	public static final String PATH_CLIENTS	= "Clients";
	public static final String PATH_AUTO_NUM = "AutoNum";
	// reports
	public static final String PATH_REPORTS	= "Reports";
	public static final String PATH_PRODUCT_REPORTTEMPLATES = "ProductReportTemplates";
	public static final String PATH_QUALITY_REPORTTEMPLATES = "QualityReportTemplates";
	public static final String PATH_REPORTS_COMPARE_PRODUCTS = "CompareProducts";
	public static final String PATH_REPORTS_EXPORT_SEARCH = "ExportSearch";
	public static final String PATH_REPORTS_EXPORT_SEARCH_PRODUCTS = "ExportProducts";
	public static final String PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES = "ExportNCSynthesis";
	public static final String PATH_REPORTS_ECO = "ECOReports";
	
	// ECO
	public static final String PATH_ECO = "ECO";
	public static final String PATH_ECO_TEMPORARY = "Temporary";	
	
	/*-- DataLists --*/
	public static final String CONTAINER_DATALISTS = "DataLists";	
	
	public static final StoreRef SPACES_STORE =  new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
	public static final StoreRef VERSION_STORE =  new StoreRef(Version2Model.STORE_PROTOCOL, Version2Model.STORE_ID);
	
	/*-- Regex --*/	
	public static final String REGEX_NON_NEGATIVE_INTEGER_FIELD = "^\\d*$";

	/*-- Lucene --*/
	public static final int MAX_RESULTS_SINGLE_VALUE = 1;
	
	public static final int MAX_RESULTS_256 = 256;
	
	public static final int MAX_RESULTS_1000 = 1000;
	
	public static final int MAX_RESULTS_UNLIMITED = -1;
	
	public static final int MAX_SUGGESTIONS = 100;
	
	
	
	/*-- IHM --*/
	public static final String LABEL_SEPARATOR = ", ";
	
	/*-- Report --*/
	public static final String REPORT_EXTENSION_PDF = ".PDF";
	public static final String REPORT_EXTENSION_XLS = ".XLS";
	
	// FORMAT
	public static final String FORMAT_DATE = "EEE d MMM yyyy";	
	public static final String FORMAT_DATETIME = "EEE d MMM yyyy HH:mm:ss";
	public static final String MULTI_VALUES_SEPARATOR = ",";
	
	//Site containers
	public static final String CONTAINER_DOCUMENT_LIBRARY = "documentLibrary";
	
	//Model
	public static final String MODEL_PREFIX_SEPARATOR = ":";

	// Permissions
	public static final String PERMISSION_CONSUMER = "Consumer";
	public static final String PERMISSION_EDITOR = "Editor";
	public static final String PERMISSION_CONTRIBUTOR = "Contributor";
	public static final String PERMISSION_COLLABORATOR = "Collaborator";

	// Configuration
	public static final int MAX_DEPTH_LEVEL = 256;
	
	/*
	 * Email templates
	 */
	public static final String EMAIL_NEW_USER_TEMPLATE = "importuser-email.ftl";

	/**
	 * ISO charset
	 */
	public static final String ISO_CHARSET = "ISO-8859-15";
	
	public static final Integer DATA_LISTS_PAGESIZE = 25;

	
	// Sort
	public static int SORT_DEFAULT_STEP = 100;
	public static int SORT_INSERTING_STEP = 1;
}
