/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.NCGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.QualityModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.model.VariantModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.action.executer.ImporterActionExecuter;
import fr.becpg.repo.action.executer.UserImporterActionExecuter;
import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.designer.DesignerInitService;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;

/**
 * Initialize the folders of the repository (create folder, rules, WF and system
 * contents).
 * 
 * @author Quere
 * 
 *         Init repository : - directories - System/Lists/* - System/LinkedLists
 *         - Products - Import
 * 
 *         - rules - specialize type - import - document generation - WF -
 *         validation folder -
 */
@Service
public class PLMInitRepoVisitor extends AbstractInitVisitorImpl  {


	private static final String LOCALIZATION_PFX_GROUP = "becpg.group";
	public static final String PRODUCT_REPORT_CLIENT_PATH = "beCPG/birt/document/product/default/ProductReport.rptdesign";
	public static final String PRODUCT_REPORT_CLIENT_NAME = "path.productreportclienttemplate";	
	public static final String PRODUCT_REPORT_PRODUCTION_PATH = "beCPG/birt/document/product/default/ProductReport_Prod.rptdesign";
	public static final String PRODUCT_REPORT_PRODUCTION_NAME = "path.productreportproductiontemplate";
	private static final String NC_REPORT_PATH = "beCPG/birt/document/nonconformity/NCReport.rptdesign";
	private static final String ECO_REPORT_PATH = "beCPG/birt/document/ecm/ECOReport.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearchQuery.xml";
	private static final String EXPORT_NC_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/nonconformity/NonConformitySynthesis.rptdesign";
	private static final String EXPORT_NC_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/nonconformity/ExportSearchQuery.xml";


	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ReportTplService reportTplService;

	@Autowired
	private ContentHelper contentHelper;
	
	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private EntityTplService entityTplService;

	@Autowired
	private BeCPGMailService beCPGMailService;
	
	@Autowired
	private DesignerInitService designerInitService;
	
	@Autowired
	private EntitySystemService entitySystemService;


	/**
	 * Initialize the repository with system folders.
	 * 
	 * @param companyHome
	 *            the company home
	 * @param locale
	 *            : locale of the system
	 */
	@Override
	public void visitContainer(NodeRef companyHome) {

		logger.info("Run PLMInitRepoVisitor...");

		// create groups
		logger.debug("Visit system groups");
		createSystemGroups();

		// System
		logger.debug("Visit folders");
		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM);
		
		// Lists of characteristics
		visitSystemCharactsEntity(systemNodeRef, RepoConsts.PATH_CHARACTS);
		
		//Dynamic constraints
		visitSystemListValuesEntity(systemNodeRef, RepoConsts.PATH_LISTS);

		// Hierarchy
		visitSystemHierachiesEntity(systemNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);		
		
		
		//Lists of characteristics for Quality
		visitSystemQualityListValuesEntity(systemNodeRef, PlmRepoConsts.PATH_QUALITY_LISTS);
		
		// Exchange
		NodeRef exchangeNodeRef = visitFolder(companyHome, PlmRepoConsts.PATH_EXCHANGE);
		NodeRef importNodeRef = visitFolder(exchangeNodeRef, PlmRepoConsts.PATH_IMPORT);
		visitFolder(importNodeRef, PlmRepoConsts.PATH_IMPORT_TO_TREAT);
		visitFolder(importNodeRef, PlmRepoConsts.PATH_IMPORT_TO_DO);
		visitFolder(importNodeRef, PlmRepoConsts.PATH_IMPORT_SUCCEEDED);
		visitFolder(importNodeRef, PlmRepoConsts.PATH_IMPORT_FAILED);
		visitFolder(importNodeRef, PlmRepoConsts.PATH_IMPORT_LOG);
		visitFolder(importNodeRef, PlmRepoConsts.PATH_IMPORT_USER);

		// Products
		 visitFolder(companyHome, RepoConsts.PATH_PRODUCTS);
		 

		// Quality
		NodeRef qualityNodeRef = visitFolder(companyHome, PlmRepoConsts.PATH_QUALITY);
		// Regulations
		NodeRef regulationsNodeRef = visitFolder(qualityNodeRef, PlmRepoConsts.PATH_REGULATIONS);
		visitFolder(regulationsNodeRef, PlmRepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA);
		// Specifications
		NodeRef qualSpecNodeRef = visitFolder(qualityNodeRef, PlmRepoConsts.PATH_QUALITY_SPECIFICATIONS);
		visitFolder(qualSpecNodeRef, PlmRepoConsts.PATH_CONTROL_PLANS);
		visitFolder(qualSpecNodeRef, PlmRepoConsts.PATH_CONTROL_POINTS);		
		
		visitFolder(qualityNodeRef, PlmRepoConsts.PATH_PRODUCT_SPECIFICATIONS);
		
		// NC
		visitFolder(qualityNodeRef, PlmRepoConsts.PATH_NC);
		// QualityControls
		visitFolder(qualityNodeRef, PlmRepoConsts.PATH_QUALITY_CONTROLS);

		// ECO
		visitFolder(systemNodeRef, PlmRepoConsts.PATH_ECO);

		// Icons
		visitFolder(systemNodeRef, RepoConsts.PATH_ICON);

		// EntityTemplates
		visitEntityTpls(systemNodeRef);

		// MailTemplates
		contentHelper.addFilesResources(beCPGMailService.getEmailTemplatesFolder(), "classpath:beCPG/mails/*.ftl");	
		contentHelper.addFilesResources(beCPGMailService.getEmailWorkflowTemplatesFolder(), "classpath:beCPG/mails/workflow/*.ftl");

		// Companies
		NodeRef companiesNodeRef = visitFolder(companyHome, PlmRepoConsts.PATH_COMPANIES);
		visitFolder(companiesNodeRef, PlmRepoConsts.PATH_SUPPLIERS);
		visitFolder(companiesNodeRef, PlmRepoConsts.PATH_CLIENTS);

		// Reports
		visitReports(systemNodeRef);

		// AutoNum
		visitFolder(systemNodeRef, RepoConsts.PATH_AUTO_NUM);

		// System exchange
		NodeRef systemExchangeNodeRef = visitFolder(systemNodeRef, PlmRepoConsts.PATH_EXCHANGE);
		NodeRef systemImportNodeRef = visitFolder(systemExchangeNodeRef, PlmRepoConsts.PATH_IMPORT);
		visitFolder(systemImportNodeRef, PlmRepoConsts.PATH_MAPPING);

		visitFolder(systemImportNodeRef, PlmRepoConsts.PATH_IMPORT_SAMPLES);
		
		
		//Designer		
		designerInitService.addReadOnlyDesignerFiles("classpath:alfresco/module/becpg-core/model/becpgModel.xml");
		designerInitService.addReadOnlyDesignerFiles("classpath:alfresco/module/becpg-plm-core/model/qualityModel.xml");
		
		
		//OLAP
		visitFolder(systemNodeRef, RepoConsts.PATH_OLAP_QUERIES);
		
		
	}


	@Override
	public boolean shouldInit(NodeRef companyHomeNodeRef) {
	return nodeService.getChildByName(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_EXCHANGE)) == null;
		
	}
	

	/**
	 * Add resources to folder
	 */
	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName) {

		if (folderName == RepoConsts.PATH_ICON) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/images/*.png");
		}
		if (folderName == PlmRepoConsts.PATH_MAPPING) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/import/mapping/*.xml");
		}
//		if (folderName == RepoConsts.PATH_IMPORT_SAMPLES) {
//			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/import/samples/*.csv");
//		}
		if (folderName == RepoConsts.PATH_OLAP_QUERIES) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/olap/*.saiku");
		}

	}

	
	
	
	
	/**
	 * Initialize the rules of the repository
	 */
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {

		QName specialiseType = null;
		boolean applyToChildren = false;

	     if (folderName == PlmRepoConsts.PATH_ECO) {
			specialiseType = ECMModel.TYPE_ECO;
		} else if (folderName == PlmRepoConsts.PATH_IMPORT_TO_TREAT) {

			// action
			CompositeAction compositeAction = actionService.createCompositeAction();
			Action action = actionService.createAction(ImporterActionExecuter.NAME, null);
			compositeAction.addAction(action);

			// compare-mime-type == text/csv
			ActionCondition conditionOnMimeType = actionService.createActionCondition(CompareMimeTypeEvaluator.NAME);
			conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE,
					MimetypeMap.MIMETYPE_TEXT_CSV);
			conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY,
					ContentModel.PROP_CONTENT);
			conditionOnMimeType.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnMimeType);

			// compare-name == *.csv
			ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION,
					ComparePropertyValueOperation.ENDS.toString());
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE,
					ImporterActionExecuter.PARAM_VALUE_EXTENSION);
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
		} else if (folderName == PlmRepoConsts.PATH_IMPORT_USER) {

			// action
			CompositeAction compositeAction = actionService.createCompositeAction();
			Action action = actionService.createAction(UserImporterActionExecuter.NAME, null);
			compositeAction.addAction(action);

			// compare-mime-type == text/csv
			ActionCondition conditionOnMimeType = actionService.createActionCondition(CompareMimeTypeEvaluator.NAME);
			conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE,
					MimetypeMap.MIMETYPE_TEXT_CSV);
			conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY,
					ContentModel.PROP_CONTENT);
			conditionOnMimeType.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnMimeType);

			// compare-name == *.csv
			ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION,
					ComparePropertyValueOperation.ENDS.toString());
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE,
					UserImporterActionExecuter.PARAM_VALUE_EXTENSION);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
			conditionOnName.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnName);

			// rule
			Rule rule = new Rule();
			rule.setRuleType(RuleType.INBOUND);
			rule.setAction(compositeAction);
			rule.applyToChildren(true);
			rule.setTitle("import user");
			rule.setExecuteAsynchronously(true);
			rule.setDescription("Every item created will be imported");

			ruleService.saveRule(nodeRef, rule);
		}

		// quality
		else if (folderName == PlmRepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA) {
			specialiseType = PLMModel.TYPE_PRODUCT_MICROBIO_CRITERIA;
		} else if (folderName == PlmRepoConsts.PATH_CONTROL_PLANS) {
			specialiseType = QualityModel.TYPE_CONTROL_PLAN;
		} else if (folderName == PlmRepoConsts.PATH_CONTROL_POINTS) {
			specialiseType = QualityModel.TYPE_CONTROL_POINT;
		} else if (folderName == PlmRepoConsts.PATH_QUALITY_CONTROLS) {
			specialiseType = QualityModel.TYPE_QUALITY_CONTROL;
		} else if (folderName == PlmRepoConsts.PATH_NC) {
			specialiseType = QualityModel.TYPE_NC;
		} else if (folderName == PlmRepoConsts.PATH_PRODUCT_SPECIFICATIONS) {
			specialiseType = PLMModel.TYPE_PRODUCT_SPECIFICATION;
		}

		// specialise type
		if (specialiseType != null) {

			createRuleSpecialiseType(nodeRef, applyToChildren, specialiseType);
		}
	}

	/**
	 * Initialize the permissions of the repository
	 */
	@Override
	protected void visitPermissions(NodeRef nodeRef, String folderName) {

		if (folderName == RepoConsts.PATH_SYSTEM) {

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.SystemMgr.toString(),
					PermissionService.WRITE, true);
		} else if (folderName == RepoConsts.PATH_PRODUCTS) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.RDMgr.toString(),
					PermissionService.WRITE, true);
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.QualityMgr.toString(),
					PermissionService.WRITE, true);
		} else if (folderName == PlmRepoConsts.PATH_EXCHANGE) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.SystemMgr.toString(),
					PermissionService.WRITE, true);
		}

		else if (folderName == PlmRepoConsts.PATH_QUALITY) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.QualityMgr.toString(),
					PermissionService.WRITE, true);
		}
		
		else if (folderName == PlmRepoConsts.PATH_NC) {
			
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimStart,
					PermissionService.WRITE, true);
			
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimAnalysis.toString(),
					PermissionService.WRITE, true);
			
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimClassification.toString(),
					PermissionService.WRITE, true);						

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimTreatment.toString(),
					PermissionService.WRITE, true);
			
	

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimResponse.toString(),
					PermissionService.WRITE, true);
			
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimClosing.toString(),
					PermissionService.WRITE, true);
		}
	}

	/**
	 * Create the entity templates
	 * 
	 * @param productTplsNodeRef
	 */
	private void visitEntityTpls(NodeRef systemNodeRef) {

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);

		// create product tpls
		visitProductTpls(entityTplsNodeRef);

		Set<String> subFolders = new HashSet<String>();		
		subFolders.add(RepoConsts.PATH_DOCUMENTS);
		subFolders.add(RepoConsts.PATH_IMAGES);

		// visit supplier
		Set<QName> dataLists = new LinkedHashSet<QName>();
		dataLists.add(PLMModel.TYPE_CONTACTLIST);
		dataLists.add(PLMModel.TYPE_PLANT);
		entityTplService.createEntityTpl(entityTplsNodeRef, PLMModel.TYPE_SUPPLIER, true, dataLists, subFolders);

		// visit client
		dataLists = new LinkedHashSet<QName>();
		dataLists.add(PLMModel.TYPE_CONTACTLIST);
		entityTplService.createEntityTpl(entityTplsNodeRef, PLMModel.TYPE_CLIENT, true, dataLists, subFolders);

		// visit acls
		dataLists = new LinkedHashSet<QName>();
		dataLists.add(SecurityModel.TYPE_ACL_ENTRY);
		entityTplService.createEntityTpl(entityTplsNodeRef, SecurityModel.TYPE_ACL_GROUP, true, dataLists, null);
		
		// visit ECO
		dataLists = new LinkedHashSet<QName>();
		dataLists.add(ECMModel.TYPE_REPLACEMENTLIST);
		dataLists.add(ECMModel.TYPE_WUSEDLIST);
		dataLists.add(ECMModel.TYPE_CALCULATEDCHARACTLIST);
		dataLists.add(ECMModel.TYPE_CHANGEUNITLIST);
		entityTplService.createEntityTpl(entityTplsNodeRef, ECMModel.TYPE_ECO, true, dataLists, null);

		// visit quality
		visitQuality(entityTplsNodeRef);
		
	}

	
	
	/** 
	 * Create system charact file
	 * @param systemNodeRef
	 * @param pathCharacts
	 * @return
	 */
	private NodeRef visitSystemCharactsEntity(NodeRef parentNodeRef, String path) {
		
		Map<String,QName> entityLists = new LinkedHashMap<String,QName>();
		
		entityLists.put(PlmRepoConsts.PATH_NUTS, PLMModel.TYPE_NUT);
		entityLists.put(PlmRepoConsts.PATH_INGS, PLMModel.TYPE_ING);
		entityLists.put(PlmRepoConsts.PATH_ORGANOS,PLMModel.TYPE_ORGANO);
		entityLists.put(PlmRepoConsts.PATH_ALLERGENS,PLMModel.TYPE_ALLERGEN);
		entityLists.put(PlmRepoConsts.PATH_COSTS,PLMModel.TYPE_COST);
		entityLists.put(PlmRepoConsts.PATH_PHYSICO_CHEM,PLMModel.TYPE_PHYSICO_CHEM);
		entityLists.put(PlmRepoConsts.PATH_MICROBIOS,PLMModel.TYPE_MICROBIO);
		entityLists.put(PlmRepoConsts.PATH_GEO_ORIGINS,PLMModel.TYPE_GEO_ORIGIN);
		entityLists.put(PlmRepoConsts.PATH_BIO_ORIGINS,PLMModel.TYPE_BIO_ORIGIN);
		entityLists.put(PlmRepoConsts.PATH_SUBSIDIARIES,PLMModel.TYPE_SUBSIDIARY);
		entityLists.put(PlmRepoConsts.PATH_TRADEMARKS,PLMModel.TYPE_TRADEMARK);
		entityLists.put(PlmRepoConsts.PATH_PLANTS,PLMModel.TYPE_PLANT);
		entityLists.put(PlmRepoConsts.PATH_CERTIFICATIONS,PLMModel.TYPE_CERTIFICATION);
		entityLists.put(PlmRepoConsts.PATH_APPROVALNUMBERS,PLMModel.TYPE_APPROVAL_NUMBER);
		entityLists.put(PlmRepoConsts.PATH_LABELCLAIMS,PLMModel.TYPE_LABEL_CLAIM);
		entityLists.put(PlmRepoConsts.PATH_PROCESSSTEPS,MPMModel.TYPE_PROCESSSTEP);
		entityLists.put(PlmRepoConsts.PATH_VARIANT_CHARACTS,VariantModel.TYPE_CHARACT);		
		entityLists.put(PlmRepoConsts.PATH_STORAGE_CONDITIONS,PLMModel.TYPE_STORAGE_CONDITIONS);
		entityLists.put(PlmRepoConsts.PATH_PRECAUTION_OF_USE,PLMModel.TYPE_PRECAUTION_OF_USE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_TEMPLATES,PackModel.TYPE_LABELING_TEMPLATE);
		entityLists.put(PlmRepoConsts.PATH_LABEL,PackModel.TYPE_LABEL);
		
		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}
	
	

	private NodeRef visitSystemHierachiesEntity(NodeRef parentNodeRef, String path) {
		
       Map<String,QName> entityLists = new LinkedHashMap<String,QName>();
		
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_RAWMATERIAL), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_PACKAGINGMATERIAL), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_SEMIFINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_FINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_PACKAGINGKIT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_RESOURCEPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		
		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);		
	}
	

	
	/**
	 * Create dyn List values
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	private NodeRef visitSystemListValuesEntity(NodeRef parentNodeRef, String path) {
		
		Map<String,QName> entityLists = new LinkedHashMap<String,QName>();
		
		entityLists.put(PlmRepoConsts.PATH_ING_TYPES,PLMModel.TYPE_ING_TYPE_ITEM);
		entityLists.put(PlmRepoConsts.PATH_ALLERGEN_TYPES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_GROUPS,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_TYPES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_FACTS_METHODS,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_POSITIONS,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABEL_TYPES,BeCPGModel.TYPE_LIST_VALUE);
		
		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}
	
	

	
	
	
	private NodeRef visitSystemQualityListValuesEntity(NodeRef parentNodeRef, String path) {
		
		Map<String,QName> entityLists = new LinkedHashMap<String,QName>();
		entityLists.put(PlmRepoConsts.PATH_CLAIM_ORIGIN_HIERARCHY, BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_SOURCES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_TRACKING_VALUES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_TYPES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_RESPONSES_STATES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_METHODS,QualityModel.TYPE_CONTROL_METHOD);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_STEPS,QualityModel.TYPE_CONTROL_STEP);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_CHARACTS,QualityModel.TYPE_CONTROL_CHARACT);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_UNITS,BeCPGModel.TYPE_LIST_VALUE);
		
		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
		
	}
	
	
	/**
	 * Create product tpls
	 * 
	 * @param entityTplsNodeRef
	 */
	private void visitProductTpls(NodeRef entityTplsNodeRef) {

		NodeRef productTplsNodeRef = visitFolder(entityTplsNodeRef, PlmRepoConsts.PATH_PRODUCT_TEMPLATES);

		Set<QName> productTypes = new HashSet<QName>();
		productTypes.add(PLMModel.TYPE_RAWMATERIAL);
		productTypes.add(PLMModel.TYPE_SEMIFINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_FINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_PACKAGINGMATERIAL);
		productTypes.add(PLMModel.TYPE_PACKAGINGKIT);
		productTypes.add(PLMModel.TYPE_RESOURCEPRODUCT);

		Set<String> subFolders = new HashSet<String>();
		subFolders.add(RepoConsts.PATH_IMAGES);
		subFolders.add(RepoConsts.PATH_DOCUMENTS);
		subFolders.add(RepoConsts.PATH_BRIEF);

		for (QName productType : productTypes) {

			// datalists
			Set<QName> dataLists = new LinkedHashSet<QName>();
			QName wusedQName = null;
			
			
			if (productType.equals(PLMModel.TYPE_RAWMATERIAL)) {

				dataLists.add(PLMModel.TYPE_ALLERGENLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_PRICELIST);
				dataLists.add(PLMModel.TYPE_NUTLIST);
				dataLists.add(PLMModel.TYPE_INGLIST);
				dataLists.add(PLMModel.TYPE_ORGANOLIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
				
				wusedQName = PLMModel.TYPE_COMPOLIST;

			} else if (productType.equals(PLMModel.TYPE_PACKAGINGMATERIAL)) {

				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_PRICELIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
				dataLists.add(PackModel.TYPE_LABELING_LIST);
				
				wusedQName = PLMModel.TYPE_PACKAGINGLIST;

			} else if (productType.equals(PLMModel.TYPE_RESOURCEPRODUCT)) {

				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_PRICELIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				
				wusedQName = MPMModel.TYPE_PROCESSLIST;

			} else if (productType.equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {

				dataLists.add(PLMModel.TYPE_COMPOLIST);
				dataLists.add(MPMModel.TYPE_PROCESSLIST);
				dataLists.add(PLMModel.TYPE_ALLERGENLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_NUTLIST);
				dataLists.add(PLMModel.TYPE_INGLIST);
				dataLists.add(PLMModel.TYPE_ORGANOLIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				
				wusedQName = PLMModel.TYPE_COMPOLIST;

			} else if (productType.equals(PLMModel.TYPE_FINISHEDPRODUCT)) {

				dataLists.add(PLMModel.TYPE_COMPOLIST);
				dataLists.add(PLMModel.TYPE_PACKAGINGLIST);
				dataLists.add(MPMModel.TYPE_PROCESSLIST);
				dataLists.add(PLMModel.TYPE_ALLERGENLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_NUTLIST);
				dataLists.add(PLMModel.TYPE_INGLIST);
				dataLists.add(PLMModel.TYPE_INGLABELINGLIST);
				dataLists.add(PLMModel.TYPE_ORGANOLIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
				
				wusedQName = PLMModel.TYPE_COMPOLIST;

			} else if (productType.equals(PLMModel.TYPE_PACKAGINGKIT)) {

				dataLists.add(PLMModel.TYPE_PACKAGINGLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				
				wusedQName = PLMModel.TYPE_PACKAGINGLIST;

			} else if (productType.equals(SecurityModel.TYPE_ACL_GROUP)) {
				dataLists.add(SecurityModel.TYPE_ACL_ENTRY);
			}

			
			NodeRef entityTplNodeRef = entityTplService.createEntityTpl(productTplsNodeRef, productType, true, dataLists, subFolders);
			if(wusedQName!=null) {
				entityTplService.createWUsedList(entityTplNodeRef, wusedQName, null);
			}
		}
		
		
		
	}

	private void visitQuality(NodeRef entityTplsNodeRef) {

		NodeRef qualityTplsNodeRef = visitFolder(entityTplsNodeRef, PlmRepoConsts.PATH_QUALITY_TEMPLATES);

		// visit productMicrobioCriteria
		Set<QName> dataLists = new LinkedHashSet<QName>();
		dataLists.add(PLMModel.TYPE_MICROBIOLIST);
		entityTplService
				.createEntityTpl(qualityTplsNodeRef, PLMModel.TYPE_PRODUCT_MICROBIO_CRITERIA, true, dataLists, null);

		// visit productSpecification
		dataLists.clear();
		dataLists.add(PLMModel.TYPE_FORBIDDENINGLIST);
		dataLists.add(PLMModel.TYPE_LABELING_RULE_LIST);
		entityTplService.createEntityTpl(qualityTplsNodeRef, PLMModel.TYPE_PRODUCT_SPECIFICATION, true, dataLists, null);

		// visit controlPlan
		Set<String> subFolders = new HashSet<String>();
		subFolders.add(RepoConsts.PATH_DOCUMENTS);
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_SAMPLINGDEF_LIST);
		entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_CONTROL_PLAN, true, dataLists, subFolders);

		// visit qualityControl
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_SAMPLING_LIST);
		entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_QUALITY_CONTROL, true, dataLists, null);

		// visit controlPoint
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_CONTROLDEF_LIST);
		entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_CONTROL_POINT, true, dataLists,null);

		// visit workItemAnalysis
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_CONTROL_LIST);
		entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_WORK_ITEM_ANALYSIS, true, dataLists, null);
		
		// visit NC
		subFolders = new HashSet<String>();		
		subFolders.add(RepoConsts.PATH_DOCUMENTS);
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_WORK_LOG);
		entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_NC, true, dataLists, subFolders);
	}

	/**
	 * Create the reports templates
	 * 
	 * @param productReportTplsNodeRef
	 */
	private void visitReports(NodeRef systemNodeRef) {

		// reports folder
		NodeRef reportsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_REPORTS);

		// product report templates
		NodeRef productReportTplsNodeRef = visitFolder(reportsNodeRef, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES);
		String productReportClientName = I18NUtil.getMessage(PRODUCT_REPORT_CLIENT_NAME,Locale.getDefault());
		String productReportProductionName = I18NUtil.getMessage(PRODUCT_REPORT_PRODUCTION_NAME,Locale.getDefault());
		
		try {

			QName [] productTypes = {PLMModel.TYPE_FINISHEDPRODUCT, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_SEMIFINISHEDPRODUCT, PLMModel.TYPE_PACKAGINGMATERIAL};
			String [] defaultReport = {PRODUCT_REPORT_CLIENT_PATH, PRODUCT_REPORT_CLIENT_PATH, PRODUCT_REPORT_PRODUCTION_PATH, PRODUCT_REPORT_CLIENT_PATH};
			String [] defaultReportName = {productReportClientName, productReportClientName, productReportProductionName, productReportClientName};
			String [] otherReport = {PRODUCT_REPORT_PRODUCTION_PATH, null, null, null};
			String [] otherReportName = {productReportProductionName, null, null, null};
			
			int i = 0;
			
			for(QName productType : productTypes){
				
				ClassDefinition classDef = dictionaryService.getClass(productType);
				
				if(repoService.getFolderByPath(productReportTplsNodeRef, classDef.getTitle()) == null){
					
					NodeRef folderNodeRef = repoService.getOrCreateFolderByPath(productReportTplsNodeRef,
							classDef.getTitle(), classDef.getTitle());
					
					if(defaultReport[i] != null && defaultReportName[i] != null){
						reportTplService.createTplRptDesign(folderNodeRef, defaultReportName[i],
								defaultReport[i], 
								ReportType.Document, ReportFormat.PDF, productType, true, true, false);
					}
					
					if(otherReport[i] != null && otherReportName[i] != null){
						reportTplService.createTplRptDesign(folderNodeRef, otherReportName[i],
								otherReport[i], 
								ReportType.Document, ReportFormat.PDF, productType, true, false, false);
					}
				}
								
				i++;			
			}			
			
		} catch (Exception e) {
			logger.error("Failed to create product report.", e);
		}
		
		// quality report templates
		NodeRef qualityReportTplsNodeRef = visitFolder(reportsNodeRef, PlmRepoConsts.PATH_QUALITY_REPORTTEMPLATES);

		// nc
		try {

			ClassDefinition classDef = dictionaryService.getClass(QualityModel.TYPE_NC);
			NodeRef qualityFolderNodeRef = repoService.getOrCreateFolderByPath(qualityReportTplsNodeRef,
					classDef.getTitle(), classDef.getTitle());
			reportTplService.createTplRptDesign(qualityFolderNodeRef, classDef.getTitle(),
					NC_REPORT_PATH, ReportType.Document, ReportFormat.PDF, QualityModel.TYPE_NC, true, true, false);
		} catch (Exception e) {
			logger.error("Failed to create nc report tpl." + QualityModel.TYPE_NC, e);
		}


		
		// eco report
		try {
			NodeRef ecoFolderNodeRef = visitFolder(reportsNodeRef, PlmRepoConsts.PATH_REPORTS_ECO);
			reportTplService.createTplRptDesign(ecoFolderNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_ECO),
					ECO_REPORT_PATH, ReportType.Document, ReportFormat.PDF, ECMModel.TYPE_ECO, true, true, false);
		} catch (IOException e) {
			logger.error("Failed to create eco report tpl.", e);
		}

		/*
		 * Export Search reports
		 */
		NodeRef exportSearchNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_EXPORT_SEARCH);

		// export search products
		try {
			NodeRef exportSearchProductsNodeRef = visitFolder(exportSearchNodeRef,
					PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS),
					EXPORT_PRODUCTS_REPORT_RPTFILE_PATH, ReportType.ExportSearch, ReportFormat.XLS,
					PLMModel.TYPE_PRODUCT, false, true, false);

			reportTplService
					.createTplRessource(exportSearchProductsNodeRef, EXPORT_PRODUCTS_REPORT_XMLFILE_PATH, false);
		} catch (IOException e) {
			logger.error("Failed to create export search report tpl.", e);
		}

		// export search NC
		try {
			NodeRef exportNCSynthesisNodeRef = visitFolder(exportSearchNodeRef,
					PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES);

			reportTplService.createTplRptDesign(exportNCSynthesisNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES),
					EXPORT_NC_REPORT_RPTFILE_PATH, ReportType.ExportSearch, ReportFormat.PDF, QualityModel.TYPE_NC,
					false, true, false);

			reportTplService.createTplRessource(exportNCSynthesisNodeRef, EXPORT_NC_REPORT_XMLFILE_PATH, false);
		} catch (IOException e) {
			logger.error("Failed to create export search report tpl.", e);
		}
	}

	/**
	 * Create system groups.
	 * 
	 * @param locale
	 *            the locale
	 */
	private void createSystemGroups() {

		String[] groups = { SystemGroup.SystemMgr.toString(), SystemGroup.RD.toString(), SystemGroup.RDUser.toString(),
				SystemGroup.RDMgr.toString(), SystemGroup.Quality.toString(), SystemGroup.QualityUser.toString(),
				SystemGroup.QualityMgr.toString(), SystemGroup.Purchasing.toString(),
				SystemGroup.PurchasingUser.toString(), SystemGroup.PurchasingMgr.toString(),
				SystemGroup.Production.toString(),
				SystemGroup.ProductionUser.toString(), SystemGroup.ProductionMgr.toString(),
				SystemGroup.Trade.toString(), SystemGroup.TradeUser.toString(), SystemGroup.TradeMgr.toString(),NCGroup.ClaimStart.toString(),
				NCGroup.ClaimAnalysis.toString(),NCGroup.ClaimClassification.toString(), NCGroup.ClaimTreatment.toString(), 
				NCGroup.ClaimResponse.toString(), NCGroup.ClaimClosing.toString()
					};

		Set<String> zones = new HashSet<String>();
		zones.add(AuthorityService.ZONE_APP_DEFAULT);
		zones.add(AuthorityService.ZONE_APP_SHARE);
		zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

		for (String group : groups) {

			logger.debug("group: " + group);
			String groupName = I18NUtil.getMessage(String.format("%s.%s", LOCALIZATION_PFX_GROUP, group).toLowerCase(), Locale.getDefault());

			if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)) {
				logger.debug("create group: " + groupName);
				authorityService.createAuthority(AuthorityType.GROUP, group, groupName, zones);
			} else {
				Set<String> zonesAdded = authorityService.getAuthorityZones(PermissionService.GROUP_PREFIX + group);
				Set<String> zonesToAdd = new HashSet<String>();
				for (String zone : zones)
					if (!zonesAdded.contains(zone)) {
						zonesToAdd.add(zone);
					}

				if (!zonesToAdd.isEmpty()) {
					logger.debug("Add group to zone: " + groupName + " - " + zonesToAdd.toString());
					authorityService.addAuthorityToZones(PermissionService.GROUP_PREFIX + group, zonesToAdd);
				}
			}
		}

		// Group hierarchy
		Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
				PermissionService.GROUP_PREFIX + SystemGroup.RD.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.RDMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.RD.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.RDMgr.toString());
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.RDUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.RD.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.RDUser.toString());

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX
				+ SystemGroup.Quality.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.QualityMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Quality.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.QualityMgr.toString());
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.QualityUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Quality.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.QualityUser.toString());

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX
				+ SystemGroup.Purchasing.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.PurchasingMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Purchasing.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.PurchasingMgr.toString());
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.PurchasingUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Purchasing.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.PurchasingUser.toString());

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX
				+ SystemGroup.Production.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.ProductionMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Production.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.ProductionMgr.toString());
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.ProductionUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Production.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.ProductionUser.toString());
		
		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX
				+ SystemGroup.Trade.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.TradeMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Trade.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.TradeMgr.toString());
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.TradeUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Trade.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.TradeUser.toString());
	

	}



}
