/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.common;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * beCPG constants for repository
 * @author querephi
 *
 */
public class RepoConsts {

	/*-- Path query --*/			
	public static final String PATH_QUERY_SUGGEST_VALUE = " +PATH:\"/app:company_home/%s/*\" +@cm\\:name:(%s) ";
	public static final String PATH_QUERY_SUGGEST_LKV_VALUE = " +PATH:\"/app:company_home/%s/*\" +@bcpg\\:lkvPrevValue:\"%s\"  +@bcpg\\:lkvValue:(%s) ";
	public static final String PATH_QUERY_SUGGEST_PRODUCT_BY_NAME = " +PATH:\"/app:company_home//*\" +TYPE:\"bcpg:product\" +@cm\\:name:(%s) -@cm\\:productState:%s -@cm\\:productState:%s ";
	public static final String PATH_QUERY_SUGGEST_PRODUCT_BY_CODE = " +PATH:\"/app:company_home//*\" +TYPE:\"bcpg:product\" +@bcpg\\:productCode:%s -@cm\\:productState:%s -@cm\\:productState:%s ";
	public static final String PATH_QUERY_SUGGEST_TARGET_BY_NAME = "  +PATH:\"/app:company_home//*\" +TYPE:\"%s\"  +@cm\\:name:(%s)";
	public static final String PATH_QUERY_SUGGEST_TARGET_BY_CODE = "  +PATH:\"/app:company_home//*\" +TYPE:\"%s\"  +@bcpg\\:code:%s";
	public static final String PATH_QUERY_PRODUCT_TEMPLATES = "  +PATH:\"/app:company_home/cm:System/cm:ProductTemplates/*/*\" +TYPE:\"bcpg:productTemplate\"";
	public static final String PATH_QUERY_PATH = " +PATH:\"%s\"";
	public static final String PATH_QUERY_LIST_CONSTRAINTS = "PATH:\"/app:company_home/%s/*\" +TYPE:\"%s\"";
	public static final String PATH_QUERY_IMPORT_FAILED_FOLDER = " +PATH:\"/app:company_home/cm:Exchange/cm:Import/cm:ImportFailed\"";
	public static final String PATH_QUERY_IMPORT_SUCCEEDED_FOLDER = " +PATH:\"/app:company_home/cm:Exchange/cm:Import/cm:ImportSucceeded\"";
	public static final String PATH_QUERY_PRODUCT_REPORTTEMPLATES = "  +PATH:\"/app:company_home/cm:System/cm:Reports/cm:ProductReportTemplates/*\"  +TYPE:\"bcpg:productReportTemplate\" +@bcpg\\:prtIsSystem:%s";
	public static final String PATH_QUERY_LIST_PRODUCTSTATES = "PATH:\"/app:company_home/System/States/Product/*\" +TYPE:\"bcpg:listState\"";
	public static final String PATH_QUERY_LIST_PRODUCTTYPES = "PATH:\"/app:company_home/System/ProductTypes/*\" +TYPE:\"bcpg:listProductType\"";
	public static final String PATH_QUERY_PRODUCTS = "PATH:\"/app:company_home/st:sites/cm:%s/cm:documentLibrary/%s\"";
	public static final String PATH_QUERY_REPORT_COMPARE_PRODUCTS = "PATH:\"/app:company_home/cm:System/cm:Reports/cm:CompareProducts/*\" +@cm\\:name:\"CompareProducts*rptdesign\"";
	public static final String PATH_QUERY_REPORTS_EXPORT_SEARCH = " +PATH:\"/app:company_home/cm:System/cm:Reports/cm:ExportSearch/*\"";
	public static final String PATH_QUERY_REPORT_EXPORT_SEARCH = " +PATH:\"/app:company_home/cm:System/cm:Reports/cm:ExportSearch/cm:%s\"";	
	public static final String PATH_QUERY_CHARACT_BY_TYPE_AND_NAME = " +PATH:\"/app:company_home/cm:System//*\" +TYPE:\"%s\" +@cm\\:name:\"%s\"";	
	public static final String PATH_QUERY_AUTONUM = "PATH:\"/app:company_home/cm:System/cm:AutoNum/*\" +TYPE:\"bcpg:autoNum\" +@bcpg\\:autoNumClassName:%s +@bcpg\\:autoNumPropertyName:%s";
	public static final String PATH_QUERY_IMPORT_MAPPING = " +PATH:\"/app:company_home/cm:System/cm:Exchange/cm:Import/cm:Mapping/*\" +@cm\\:name:\"%s*xml\"";
	public static final String PATH_QUERY_NODE_BY_CODE = " +PATH:\"/app:company_home//*\" +TYPE:\"%s\" +@bcpg\\:code:%s ";// TODO : refactoriser avec SUGGEST_SUPPLIER_BY_CODE	
	
	/*-- Path --*/	
	public static final String PATH_SEPARATOR 	= "/";
	public static final String PATH_SYSTEM = "System";
	public static final String PATH_LISTS = "Lists";
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
	public static final String PATH_PRODUCT_TEMPLATES = "ProductTemplates";
	public static final String PATH_PRODUCTS = "Products";
	public static final String PATH_EXCHANGE = "Exchange";
	public static final String PATH_IMPORT = "Import";
	public static final String PATH_IMPORT_TO_TREAT	= "ImportToTreat";
	public static final String PATH_IMPORT_SUCCEEDED = "ImportSucceeded";
	public static final String PATH_IMPORT_FAILED = "ImportFailed";	
	public static final String PATH_PRODUCT_MICROBIO_CRITERIA = "ProductMicrobioCriteria";	
	public static final String PATH_MAPPING = "Mapping";
	//Hierarchy
	public static final String PATH_PRODUCT_HIERARCHY = "ProductHierarchy";
	public static final String PATH_HIERARCHY_SFX_HIERARCHY1 = "Hierarchy1";
	public static final String PATH_HIERARCHY_SFX_HIERARCHY2 = "Hierarchy2";
	public static final String PATH_HIERARCHY_RAWMATERIAL_HIERARCHY1 = "RawMaterial_Hierarchy1";
	public static final String PATH_HIERARCHY_PACKAGINGMATERIAL_HIERARCHY1 = "PackagingMaterial_Hierarchy1";
	public static final String PATH_HIERARCHY_SEMIFINISHEDPRODUCT_HIERARCHY1 = "SemiFinishedProduct_Hierarchy1";
	public static final String PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY1 = "FinishedProduct_Hierarchy1";
	public static final String PATH_HIERARCHY_LOCASEMIFINISHEDPRODUCT_HIERARCHY1 = "LocalSemiFinishedProduct_Hierarchy1";
	public static final String PATH_HIERARCHY_PACKAGINGKIT_HIERARCHY1 = "PackagingKit_Hierarchy1";
	public static final String PATH_HIERARCHY_CONDSALESUNIT_HIERARCHY1 = "CondSalesUnit_Hierarchy1";	
	public static final String PATH_HIERARCHY_RAWMATERIAL_HIERARCHY2 = "RawMaterial_Hierarchy2";
	public static final String PATH_HIERARCHY_PACKAGINGMATERIAL_HIERARCHY2 = "PackagingMaterial_Hierarchy2";
	public static final String PATH_HIERARCHY_SEMIFINISHEDPRODUCT_HIERARCHY2 = "SemiFinishedProduct_Hierarchy2";
	public static final String PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY2 = "FinishedProduct_Hierarchy2";
	public static final String PATH_HIERARCHY_LOCASEMIFINISHEDPRODUCT_HIERARCHY2 = "LocalSemiFinishedProduct_Hierarchy2";
	public static final String PATH_HIERARCHY_PACKAGINGKIT_HIERARCHY2 = "PackagingKit_Hierarchy2";
	public static final String PATH_HIERARCHY_CONDSALESUNIT_HIERARCHY2 = "CondSalesUnit_Hierarchy2";			
	// Product folder
	public static final String PATH_IMAGES = "Images";
	public static final String PATH_DOCUMENTS = "Documents";
	public static final String PATH_BRIEF = "Brief";
	public static final String PATH_PRODUCT_IMAGE = "productimage";
	public static final String PATH_COMPANIES = "Companies";
	public static final String PATH_SUPPLIERS = "Suppliers";
	public static final String PATH_CLIENTS	= "Clients";
	public static final String PATH_REPORTS	= "Reports";
	public static final String PATH_PRODUCT_REPORTTEMPLATES = "ProductReportTemplates";
	public static final String PATH_REPORTS_COMPARE_PRODUCTS = "CompareProducts";
	public static final String PATH_REPORTS_EXPORT_SEARCH = "ExportSearch";
	public static final String PATH_REPORTS_EXPORT_SEARCH_PRODUCTS = "ExportProducts";
	public static final String PATH_AUTO_NUM = "AutoNum";
	
	/*-- DataLists --*/
	public static final String CONTAINER_DATALISTS = "DataLists";	
	
	public static final StoreRef SPACES_STORE =  new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
	
	/*-- Regex --*/	
	public static final String REGEX_NON_NEGATIVE_INTEGER_FIELD = "^\\d*$";

	/*-- Lucene --*/
	public static final int MAX_RESULTS_SINGLE_VALUE = 1;
	public static final int MAX_RESULTS_NO_LIMIT = 256;
	
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
}
