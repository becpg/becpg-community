/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NcModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.action.executer.ImporterActionExecuter;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.report.ProductReportTplService;

/**
 * Initialize the folders of the repository (create folder, rules, WF and system contents).
 *
 * @author Quere
 * 
 * Init repository :
 * - directories
 * - System/Lists/*
 * - System/LinkedLists
 * - Products
 * - Import
 * 
 * - rules
 * - specialize type
 * - import
 * - document generation
 * - WF
 * - validation folder
 * -
 */
public class InitRepoVisitorImpl extends AbstractInitVisitorImpl implements InitVisitor{
	
	/** The Constant GROUP_SYSTEM_MGR. */
	public static final String GROUP_SYSTEM_MGR = "SystemMgr";
	
	/** The Constant GROUP_RD_USER. */
	public static final String GROUP_RD_USER = "RDUser";
	
	/** The Constant GROUP_RD_MGR. */
	public static final String GROUP_RD_MGR = "RDMgr";
	
	/** The Constant GROUP_QUALITY_USER. */
	public static final String GROUP_QUALITY_USER = "QualityUser";
	
	/** The Constant GROUP_QUALITY_MGR. */
	public static final String GROUP_QUALITY_MGR = "QualityMgr";
	
	/** The Constant GROUP_PURCHASING_USER. */
	public static final String GROUP_PURCHASING_USER = "PurchasingUser";
	
	/** The Constant GROUP_PURCHASING_MGR. */
	public static final String GROUP_PURCHASING_MGR = "PurchasingMgr";
	
	/** The Constant GROUP_PRODUCT_REVIEWER. */
	public static final String GROUP_PRODUCT_REVIEWER = "ProductReviewer";
	
	private static final String LOCALIZATION_PFX_GROUP	= "becpg.group";
	private static final String PRODUCT_REPORT_PATH = "beCPG/birt/ProductReport.rptdesign";
	private static final String COMPARE_PRODUCTS_REPORT_PATH = "beCPG/birt/CompareProducts.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/ExportSearch/Product/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/ExportSearch/Product/ExportSearchQuery.xml";
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;	
	
	/** The authority service. */
	private AuthorityService authorityService;
	
	/** The permission service. */
	private PermissionService permissionService;
	
	private ProductDAO productDAO;	

	private ProductReportTplService productReportTplService;
	
	private ContentService contentService;
	
	private MimetypeService mimetypeService;
	
	/**
	 * Sets the product dictionary service.
	 *
	 * @param productDictionaryService the new product dictionary service
	 */
	public void setProductDictionaryService(ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}
	
	/**
	 * Sets the authority service.
	 *
	 * @param authorityService the new authority service
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	/**
	 * Sets the permission service.
	 *
	 * @param permissionService the new permission service
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}		
	
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	public void setProductReportTplService(ProductReportTplService productReportTplService) {
		this.productReportTplService = productReportTplService;
	}		
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * Initialize the repository with system folders.
	 *
	 * @param companyHome the company home
	 * @param locale : locale of the system
	 */
	@Override
	public void visitContainer(NodeRef companyHome, Locale locale){
	
		logger.debug("visit");
		
		//create groups
		logger.debug("Visit system groups");
		createSystemGroups(locale);
		
		//System
		logger.debug("Visit folders");
		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM, locale); 
				
		//Lists of characteristics
		visitFolder(systemNodeRef, RepoConsts.PATH_LISTS, locale);
		visitFolder(systemNodeRef, RepoConsts.PATH_LINKED_LISTS, locale);
		visitFolder(systemNodeRef, RepoConsts.PATH_NUTS, locale);				
		visitFolder(systemNodeRef, RepoConsts.PATH_INGS, locale);		
		visitFolder(systemNodeRef, RepoConsts.PATH_ORGANOS, locale);	
		visitFolder(systemNodeRef, RepoConsts.PATH_ALLERGENS, locale);		
		visitFolder(systemNodeRef, RepoConsts.PATH_COSTS, locale);
		visitFolder(systemNodeRef, RepoConsts.PATH_PHYSICO_CHEM, locale);
		visitFolder(systemNodeRef, RepoConsts.PATH_MICROBIOS, locale);
		visitFolder(systemNodeRef, RepoConsts.PATH_GEO_ORIGINS, locale);
		visitFolder(systemNodeRef, RepoConsts.PATH_BIO_ORIGINS, locale);
				
		//Hierarchy
		NodeRef hierarchyNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_RAWMATERIAL_HIERARCHY1, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_PACKAGINGMATERIAL_HIERARCHY1, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_SEMIFINISHEDPRODUCT_HIERARCHY1, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY1, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_LOCASEMIFINISHEDPRODUCT_HIERARCHY1, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_PACKAGINGKIT_HIERARCHY1, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_CONDSALESUNIT_HIERARCHY1, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_RAWMATERIAL_HIERARCHY2, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_PACKAGINGMATERIAL_HIERARCHY2, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_SEMIFINISHEDPRODUCT_HIERARCHY2, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY2, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_LOCASEMIFINISHEDPRODUCT_HIERARCHY2, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_PACKAGINGKIT_HIERARCHY2, locale);
		visitFolder(hierarchyNodeRef, RepoConsts.PATH_HIERARCHY_CONDSALESUNIT_HIERARCHY2, locale);				
		
		//Exchange
		NodeRef exchangeNodeRef = visitFolder(companyHome, RepoConsts.PATH_EXCHANGE, locale);
		NodeRef importNodeRef = visitFolder(exchangeNodeRef, RepoConsts.PATH_IMPORT, locale);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_TO_TREAT, locale);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_SUCCEEDED, locale);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_FAILED, locale);			
		
		//Products		
		NodeRef productsNodeRef = visitFolder(companyHome, RepoConsts.PATH_PRODUCTS, locale);
		productDictionaryService.initializeRepoHierarchy(productsNodeRef);
		
		
		//Quality		
		NodeRef qualityNodeRef = visitFolder(companyHome, RepoConsts.PATH_QUALITY, locale);
		visitFolder(qualityNodeRef, RepoConsts.PATH_NC, locale);
		
		//ProductTemplates
		NodeRef productTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_PRODUCT_TEMPLATES, locale);			
		visitProductTpls(productTplsNodeRef);
		
		//ProductMicrobioCriteria		
		visitFolder(systemNodeRef, RepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA, locale);
		
		//Companies
		NodeRef companiesNodeRef = visitFolder(companyHome, RepoConsts.PATH_COMPANIES, locale);
		visitFolder(companiesNodeRef, RepoConsts.PATH_SUPPLIERS, locale);
		visitFolder(companiesNodeRef, RepoConsts.PATH_CLIENTS, locale);
		
		//Reports				
		visitReports(systemNodeRef, locale);
		
		//AutoNum
		visitFolder(systemNodeRef, RepoConsts.PATH_AUTO_NUM, locale);
		
		//System exchange
		NodeRef systemExchangeNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_EXCHANGE, locale);
		NodeRef systemImportNodeRef = visitFolder(systemExchangeNodeRef, RepoConsts.PATH_IMPORT, locale);
		visitFolder(systemImportNodeRef, RepoConsts.PATH_MAPPING, locale);		
		
	}	
	
	/**
	 * Initialize the rules of the repository
	 */	
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {
		
		QName specialiseType = null;
		boolean applyToChildren = false;
		
		if(folderName == RepoConsts.PATH_LISTS){			
			specialiseType = BeCPGModel.TYPE_LIST_VALUE;
			applyToChildren = true;
		}
		else if(folderName == RepoConsts.PATH_LINKED_LISTS){
			specialiseType = BeCPGModel.TYPE_LINKED_VALUE;
			applyToChildren = true;
		}	
		else if(folderName == RepoConsts.PATH_NUTS){
			specialiseType = BeCPGModel.TYPE_NUT;			
		}
		else if(folderName == RepoConsts.PATH_INGS){
			specialiseType = BeCPGModel.TYPE_ING;
		}
		else if(folderName == RepoConsts.PATH_ORGANOS){			
			specialiseType = BeCPGModel.TYPE_ORGANO;
		}
		else if(folderName == RepoConsts.PATH_ALLERGENS){
			specialiseType = BeCPGModel.TYPE_ALLERGEN;
		}
		else if(folderName == RepoConsts.PATH_COSTS){
			specialiseType = BeCPGModel.TYPE_COST;
		}
		else if(folderName == RepoConsts.PATH_PHYSICO_CHEM){
			specialiseType = BeCPGModel.TYPE_PHYSICO_CHEM;
		}
		else if(folderName == RepoConsts.PATH_MICROBIOS){
			specialiseType = BeCPGModel.TYPE_MICROBIO;
		}		
		else if(folderName == RepoConsts.PATH_GEO_ORIGINS){
			specialiseType = BeCPGModel.TYPE_GEO_ORIGIN;
		}		
		else if(folderName == RepoConsts.PATH_BIO_ORIGINS){
			specialiseType = BeCPGModel.TYPE_BIO_ORIGIN;
		}		
		else if(folderName == RepoConsts.PATH_PRODUCT_TEMPLATES){
			specialiseType = BeCPGModel.TYPE_PRODUCTTEMPLATE;
		}
		else if(folderName == RepoConsts.PATH_PRODUCT_REPORTTEMPLATES){
			specialiseType = BeCPGModel.TYPE_PRODUCT_REPORTTEMPLATE;
		}
		else if(folderName == RepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA){
			specialiseType = BeCPGModel.TYPE_PRODUCT_MICROBIO_CRITERIA;
		}		
		else if(folderName.endsWith(RepoConsts.PATH_HIERARCHY_SFX_HIERARCHY1)){
			specialiseType = BeCPGModel.TYPE_LIST_VALUE;
		}
		else if(folderName.endsWith(RepoConsts.PATH_HIERARCHY_SFX_HIERARCHY2)){
			specialiseType = BeCPGModel.TYPE_LINKED_VALUE;
		}
		else if(folderName == RepoConsts.PATH_PRODUCT_REPORTTEMPLATES){
			specialiseType = BeCPGModel.TYPE_PRODUCT_REPORTTEMPLATE;
		}
		else if(folderName == RepoConsts.PATH_IMPORT_TO_TREAT){
						
	        // action
	        CompositeAction compositeAction = actionService.createCompositeAction();
	        Action action = actionService.createAction(ImporterActionExecuter.NAME, null);        	        
	        compositeAction.addAction(action);
	        
	        // compare-mime-type == text/csv	        
	        ActionCondition conditionOnMimeType = actionService.createActionCondition(CompareMimeTypeEvaluator.NAME);
	        conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_TEXT_CSV);
	        conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
	        conditionOnMimeType.setInvertCondition(false);
	        compositeAction.addActionCondition(conditionOnMimeType);
	        
	        // compare-name == *.csv	        	        
	        ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
	        conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
	        conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, ImporterActionExecuter.PARAM_VALUE_EXTENSION);
	        conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
	        conditionOnName.setInvertCondition(false);
	        compositeAction.addActionCondition(conditionOnName);
	        
	        // rule
	        Rule rule = new Rule();
	        rule.setRuleType(RuleType.INBOUND);	        
	        rule.setAction(compositeAction);	                	        				  	  
	  	  	rule.applyToChildren(true);	  	
	  	  	rule.setTitle("import file");
	  	  	rule.setExecuteAsynchronously(true);
	  	  	rule.setDescription("Every item created will be imported");
	  	  	
		    ruleService.saveRule(nodeRef, rule);
		}
		else if(folderName == RepoConsts.PATH_SUPPLIERS){
			specialiseType = BeCPGModel.TYPE_SUPPLIER;
			applyToChildren = true;
		}
		else if(folderName == RepoConsts.PATH_CLIENTS){
			specialiseType = BeCPGModel.TYPE_CLIENT;
			applyToChildren = true;
		}
		else{
			return;
		}
		
		//specialise type
		if(specialiseType != null){
			
			createRuleSpecialiseType(nodeRef, applyToChildren, specialiseType);
		}
	}
	
	/**
	 * Initialize the permissions of the repository
	 */
	@Override
	protected void visitPermissions(NodeRef nodeRef, String folderName) {		
		
		if(folderName == RepoConsts.PATH_SYSTEM){
			
//			boolean hasSystemMgrPerm = false;
//			Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
//			for(AccessPermission permission : permissions){
//				if(permission.getAuthority().equals(PermissionService.GROUP_PREFIX + GROUP_SYSTEM_MGR) && permission.getPermission().equals(PermissionService.WRITE))
				
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_SYSTEM_MGR, PermissionService.WRITE, true);
		}
		else if(folderName == RepoConsts.PATH_PRODUCTS){			
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_RD_MGR, PermissionService.WRITE, true);
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_QUALITY_MGR, PermissionService.WRITE, true);
		}
		else if(folderName == RepoConsts.PATH_EXCHANGE){			
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_SYSTEM_MGR, PermissionService.WRITE, true);
		}	
		
		else if(folderName == RepoConsts.PATH_QUALITY){
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_QUALITY_MGR, PermissionService.WRITE, true);
		}
	}
	
	/**
	 * Create the product templates
	 * @param productTplsNodeRef
	 */
	private void visitProductTpls(NodeRef productTplsNodeRef){
		
		/*
		 * Create product tpls that have a product folder
		 */
		Set<SystemProductType> systemProductTypes = new HashSet<SystemProductType>();
		systemProductTypes.add(SystemProductType.RawMaterial);
		systemProductTypes.add(SystemProductType.SemiFinishedProduct);
		systemProductTypes.add(SystemProductType.FinishedProduct);
		systemProductTypes.add(SystemProductType.PackagingMaterial);
		systemProductTypes.add(SystemProductType.PackagingMaterial);
		systemProductTypes.add(SystemProductType.CondSalesUnit);
		
		for(SystemProductType systemProductType : systemProductTypes){

			String productTplName = TranslateHelper.getTranslatedPath(systemProductType.toString());
			
			// ProductFolder
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, productTplName);
						
			NodeRef productTplFolderNodeRef = nodeService.getChildByName(productTplsNodeRef, ContentModel.ASSOC_CONTAINS, productTplName);
			if(productTplFolderNodeRef == null){				
				productTplFolderNodeRef = nodeService.createNode(productTplsNodeRef, 
																ContentModel.ASSOC_CONTAINS, 
																QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, systemProductType.toString()), 
																BeCPGModel.TYPE_ENTITY_FOLDER, properties).getChildRef();
				
				// productTpl
				properties.clear();
				properties.put(ContentModel.PROP_NAME, productTplName);
				properties.put(BeCPGModel.PROP_PRODUCT_TYPE, systemProductType);
				
				NodeRef productTplNodeRef = nodeService.getChildByName(productTplFolderNodeRef, ContentModel.ASSOC_CONTAINS, productTplName);
				if(productTplNodeRef == null){
					productTplNodeRef = nodeService.createNode(productTplFolderNodeRef, 
																ContentModel.ASSOC_CONTAINS, 
																QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, systemProductType.toString()), 
																BeCPGModel.TYPE_PRODUCTTEMPLATE, properties).getChildRef();
				}
				
				// Images
				properties.clear();
				properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));
				NodeRef imagesFolderNodeRef = nodeService.getChildByName(productTplFolderNodeRef, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
				if(imagesFolderNodeRef == null){			
					nodeService.createNode(productTplFolderNodeRef, 
											ContentModel.ASSOC_CONTAINS, 
											QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, RepoConsts.PATH_IMAGES), 
											BeCPGModel.TYPE_ENTITY_FOLDER, properties).getChildRef();
				}			
				
				// Documents
				properties.clear();
				properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS));
				NodeRef documentsFolderNodeRef = nodeService.getChildByName(productTplFolderNodeRef, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
				if(documentsFolderNodeRef == null){			
					nodeService.createNode(productTplFolderNodeRef, 
											ContentModel.ASSOC_CONTAINS, 
											QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, RepoConsts.PATH_DOCUMENTS), 
											BeCPGModel.TYPE_ENTITY_FOLDER, 
											properties).getChildRef();
				}
					
				// Brief
				properties.clear();
				properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(RepoConsts.PATH_BRIEF));
				NodeRef briefFolderNodeRef = nodeService.getChildByName(productTplFolderNodeRef, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
				if(briefFolderNodeRef == null){			
					nodeService.createNode(productTplFolderNodeRef, 
											ContentModel.ASSOC_CONTAINS, 
											QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, RepoConsts.PATH_BRIEF), 
											BeCPGModel.TYPE_ENTITY_FOLDER, 
											properties).getChildRef();
				}			
				
				// datalists
				if(systemProductType.equals(SystemProductType.RawMaterial)){
					Set<QName> dataLists = new LinkedHashSet<QName>();
					dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
					dataLists.add(BeCPGModel.TYPE_COSTLIST);				
					dataLists.add(BeCPGModel.TYPE_NUTLIST);
					dataLists.add(BeCPGModel.TYPE_INGLIST);
					dataLists.add(BeCPGModel.TYPE_ORGANOLIST);
					dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);
					
					NodeRef listContainerNodeRef = productDAO.getListContainer(productTplNodeRef);
					if(listContainerNodeRef == null){
						listContainerNodeRef = productDAO.createListContainer(productTplNodeRef);
					}
					
					for(QName dataList : dataLists){
						
						NodeRef listNodeRef = productDAO.getList(listContainerNodeRef, dataList);
						if(listNodeRef == null){
							productDAO.createList(listContainerNodeRef, dataList);
						}
					}
				}
				else if(systemProductType.equals(SystemProductType.PackagingMaterial)){
					Set<QName> dataLists = new LinkedHashSet<QName>();
					dataLists.add(BeCPGModel.TYPE_COSTLIST);				
					dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);
					
					NodeRef listContainerNodeRef = productDAO.getListContainer(productTplNodeRef);
					if(listContainerNodeRef == null){
						listContainerNodeRef = productDAO.createListContainer(productTplNodeRef);
					}
					
					for(QName dataList : dataLists){
						
						NodeRef listNodeRef = productDAO.getList(listContainerNodeRef, dataList);
						if(listNodeRef == null){
							productDAO.createList(listContainerNodeRef, dataList);
						}
					}
				}
				else if(systemProductType.equals(SystemProductType.SemiFinishedProduct) ||
						systemProductType.equals(SystemProductType.FinishedProduct) ||
						systemProductType.equals(SystemProductType.CondSalesUnit)){
					
					Set<QName> dataLists = new LinkedHashSet<QName>();
					dataLists.add(BeCPGModel.TYPE_COMPOLIST);
					dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
					dataLists.add(BeCPGModel.TYPE_COSTLIST);				
					dataLists.add(BeCPGModel.TYPE_NUTLIST);
					dataLists.add(BeCPGModel.TYPE_INGLIST);
					dataLists.add(BeCPGModel.TYPE_ORGANOLIST);
					dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);
					
					NodeRef listContainerNodeRef = productDAO.getListContainer(productTplNodeRef);
					if(listContainerNodeRef == null){
						listContainerNodeRef = productDAO.createListContainer(productTplNodeRef);
					}
					
					for(QName dataList : dataLists){
						
						NodeRef listNodeRef = productDAO.getList(listContainerNodeRef, dataList);
						if(listNodeRef == null){
							productDAO.createList(listContainerNodeRef, dataList);
						}
					}
				}
				else if(systemProductType.equals(SystemProductType.PackagingKit)){
					
					Set<QName> dataLists = new LinkedHashSet<QName>();
					dataLists.add(BeCPGModel.TYPE_COMPOLIST);					
					dataLists.add(BeCPGModel.TYPE_COSTLIST);				
					dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);
					
					NodeRef listContainerNodeRef = productDAO.getListContainer(productTplNodeRef);
					if(listContainerNodeRef == null){
						listContainerNodeRef = productDAO.createListContainer(productTplNodeRef);
					}
					
					for(QName dataList : dataLists){
						
						NodeRef listNodeRef = productDAO.getList(listContainerNodeRef, dataList);
						if(listNodeRef == null){
							productDAO.createList(listContainerNodeRef, dataList);
						}
					}
				}
			}												
		}
		
		
		/*
		 * Create product tpls that don't have a product folder
		 */
		
		SystemProductType systemProductType = SystemProductType.LocalSemiFinishedProduct;
		String productTplName = TranslateHelper.getTranslatedPath(systemProductType.toString());
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, productTplName);
		properties.put(BeCPGModel.PROP_PRODUCT_TYPE, systemProductType);
		
		NodeRef productTplNodeRef = nodeService.getChildByName(productTplsNodeRef, ContentModel.ASSOC_CONTAINS, productTplName);
		if(productTplNodeRef == null){
			productTplNodeRef = nodeService.createNode(productTplsNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_PRODUCTTEMPLATE, properties).getChildRef();
		}		
		
	}
	
	/**
	 * Create the reports templates
	 * @param productReportTplsNodeRef
	 */
	private void visitReports(NodeRef systemNodeRef, Locale locale){
		
		// reports folder
		NodeRef reportsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_REPORTS, locale);
		
		// product report templates
		NodeRef productReportTplsNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES, locale);
		
		for(SystemProductType systemProductType : SystemProductType.values()){

			if(systemProductType.equals(SystemProductType.Unknown)){
				continue;
			}						
			
			try{
				String productTplName = TranslateHelper.getTranslatedPath(systemProductType.toString());
				productReportTplService.createTpl(productReportTplsNodeRef, productTplName, PRODUCT_REPORT_PATH, systemProductType, true, true);
			}
			catch(Exception e){
				logger.error("Failed to create product report tpl. SystemProductType: " + systemProductType, e);
			}
												
		}
		
		// compare report
		try{
			addReportTplInFolder(reportsNodeRef, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS), COMPARE_PRODUCTS_REPORT_PATH, "");
		}
		catch(IOException e){
			logger.error("Failed to create compare product report tpl.", e);
		}

		// export search report
		try{
			NodeRef exportSearchNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_EXPORT_SEARCH, locale);		
			String exportTplTitle = TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS);
			addExportSearchReportTplInFolder(exportSearchNodeRef, RepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS, EXPORT_PRODUCTS_REPORT_RPTFILE_PATH, EXPORT_PRODUCTS_REPORT_XMLFILE_PATH, exportTplTitle);
		}
		catch(IOException e){
			logger.error("Failed to create export search report tpl.", e);
		}		
	}
	
	/**
	 * Add a report template folder
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 */
	private void addReportTplInFolder(NodeRef parentNodeRef, String folderName, String tplFilePath, String tplTitle) throws IOException{
		
		ClassPathResource resource = new ClassPathResource(tplFilePath);
		
		if(resource.exists()){
			
			//create report template folder
		   	logger.debug("create report template folder");
	   		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, folderName);
			
			NodeRef productReportTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,  (String)properties.get(ContentModel.PROP_NAME));    	
	    	if(productReportTplNodeRef == null){
	    		productReportTplNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_FOLDER, properties).getChildRef();
	    		
	    		properties = new HashMap<QName, Serializable>();
	    		properties.put(ContentModel.PROP_NAME, resource.getFilename());
	    		properties.put(ContentModel.PROP_TITLE, tplTitle);
	        	NodeRef fileNodeRef = nodeService.createNode(productReportTplNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
	        	
	        	ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
	        	
	        	String mimetype = mimetypeService.guessMimetype(tplFilePath);
	    		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
	            Charset charset = charsetFinder.getCharset(resource.getInputStream(), mimetype);
	            String encoding = charset.name();

	        	writer.setMimetype(mimetype);
	        	writer.setEncoding(encoding);
	        	writer.putContent(resource.getInputStream());
	    	}
		}
		else{
			logger.error("Resource not found. Path: " + tplFilePath);
		}	   	    
	}
	
	/**
	 * Add a report template to export a search (rptdesign and xml files)
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 */
	private void addExportSearchReportTplInFolder(NodeRef parentNodeRef, String tplName, String rptdesignFilePath, String xmlFilePath, String tplTitle) throws IOException{
				
	   	//create report template folder
	   	logger.debug("create report template folder");
   		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, tplName);
		properties.put(ContentModel.PROP_TITLE, tplTitle);
		
		NodeRef productReportTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,  (String)properties.get(ContentModel.PROP_NAME));    	
    	if(productReportTplNodeRef == null){
    		productReportTplNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_FOLDER, properties).getChildRef();
    		
    		// *.rptdesign file
    		ClassPathResource resource = new ClassPathResource(rptdesignFilePath);
    		if(resource.exists()){
    			properties = new HashMap<QName, Serializable>();
        		properties.put(ContentModel.PROP_NAME, resource.getFilename());
        		properties.put(ContentModel.PROP_TITLE, tplTitle);
            	NodeRef fileNodeRef = nodeService.createNode(productReportTplNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
            	
            	ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
            	
            	String mimetype = mimetypeService.guessMimetype(rptdesignFilePath);
        		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
                Charset charset = charsetFinder.getCharset(resource.getInputStream(), mimetype);
                String encoding = charset.name();

            	writer.setMimetype(mimetype);
            	writer.setEncoding(encoding);
            	writer.putContent(resource.getInputStream());
    		}
    		else{
    			logger.error("Resource not found. Path: " + rptdesignFilePath);
    		}
        	
        	// *.xml file
        	resource = new ClassPathResource(xmlFilePath);
        	if(resource.exists()){
        	
        		properties = new HashMap<QName, Serializable>();
        		properties.put(ContentModel.PROP_NAME, resource.getFilename());
        		properties.put(ContentModel.PROP_TITLE, tplTitle);
        		NodeRef fileNodeRef = nodeService.createNode(productReportTplNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
            	
        		ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
            	
        		String mimetype = mimetypeService.guessMimetype(xmlFilePath);
        		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
        		BufferedInputStream bis = new BufferedInputStream(resource.getInputStream());
        		Charset charset = charsetFinder.getCharset(bis, mimetype);
        		String encoding = charset.name();

            	writer.setMimetype(mimetype);
            	writer.setEncoding(encoding);
            	writer.putContent(resource.getInputStream());
        	}
        	else{
        		logger.error("Resource not found. Path: " + xmlFilePath);
        	}
        	
    	}    	
	}
	
	/**
	 * Create system groups.
	 *
	 * @param locale the locale
	 */
	private void createSystemGroups(Locale locale){
		//http://forums.alfresco.com/en/viewtopic.php?t=14004
		

		String [] groups = {SystemGroup.SystemMgr.toString(), SystemGroup.RD.toString(), SystemGroup.RDUser.toString(), SystemGroup.RDMgr.toString(), SystemGroup.Quality.toString(), SystemGroup.QualityUser.toString(), SystemGroup.QualityMgr.toString(), SystemGroup.Purchasing.toString(), SystemGroup.PurchasingUser.toString(), SystemGroup.PurchasingMgr.toString(), SystemGroup.ProductReviewer.toString()};
		Set<String> zones = new HashSet<String>();
		zones.add(AuthorityService.ZONE_APP_DEFAULT);
		zones.add(AuthorityService.ZONE_APP_SHARE);
		zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
		
		for(String group : groups){
			
			logger.debug("group: " + group);
			String groupName = I18NUtil.getMessage(String.format("%s.%s",  LOCALIZATION_PFX_GROUP, group).toLowerCase(), locale);			
			
			if(!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)){
				logger.debug("create group: " + groupName);				
				authorityService.createAuthority(AuthorityType.GROUP, group, groupName, zones);				
			}
			else{
				Set<String>zonesAdded =  authorityService.getAuthorityZones(PermissionService.GROUP_PREFIX + group);
				Set<String>zonesToAdd = new HashSet<String>();  
				for(String zone : zones)
					if(!zonesAdded.contains(zone)){												
						zonesToAdd.add(zone);
					}
				
				if(zonesToAdd.size() > 0){
					logger.debug("Add group to zone: " + groupName + " - " + zonesToAdd.toString());
					authorityService.addAuthorityToZones(PermissionService.GROUP_PREFIX + group, zonesToAdd);
				}
			}
		}
		
		//Group hierarchy
		Set<String>authorities =  authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + SystemGroup.RD.toString(), true);
		if(!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.RDMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.RD.toString(), PermissionService.GROUP_PREFIX + SystemGroup.RDMgr.toString());
		if(!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.RDUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.RD.toString(), PermissionService.GROUP_PREFIX + SystemGroup.RDUser.toString());
		
		authorities =  authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + SystemGroup.Quality.toString(), true);
		if(!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.QualityMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Quality.toString(), PermissionService.GROUP_PREFIX + SystemGroup.QualityMgr.toString());
		if(!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.QualityUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Quality.toString(), PermissionService.GROUP_PREFIX + SystemGroup.QualityUser.toString());
		
		authorities =  authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + SystemGroup.Purchasing.toString(), true);
		if(!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.PurchasingMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Purchasing.toString(), PermissionService.GROUP_PREFIX + SystemGroup.PurchasingMgr.toString());
		if(!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.PurchasingUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Purchasing.toString(), PermissionService.GROUP_PREFIX + SystemGroup.PurchasingUser.toString());
			
	}
	
}
