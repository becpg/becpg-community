/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
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
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.QualityModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.VariantModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.action.executer.ImporterActionExecuter;
import fr.becpg.repo.action.executer.UserImporterActionExecuter;
import fr.becpg.repo.designer.DesignerInitService;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.product.hierarchy.HierarchyHelper;
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
public class InitRepoVisitorImpl extends AbstractInitVisitorImpl implements InitVisitor {

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

	private static final String LOCALIZATION_PFX_GROUP = "becpg.group";
	private static final String PRODUCT_REPORT_PATH = "beCPG/birt/document/product/default/ProductReport.rptdesign";
	private static final String PRODUCT_REPORT_EN_PATH = "beCPG/birt/document/product/default/ProductReport-en.rptdesign";
	private static final String NC_REPORT_PATH = "beCPG/birt/document/nonconformity/NCReport.rptdesign";
	private static final String COMPARE_ENTITIES_REPORT_PATH = "beCPG/birt/system/CompareEntities.rptdesign";
	private static final String ECO_REPORT_PATH = "beCPG/birt/system/ecm/ECOReport.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearchQuery.xml";
	private static final String EXPORT_NC_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/nonconformity/NonConformitySynthesis.rptdesign";
	private static final String EXPORT_NC_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/nonconformity/ExportSearchQuery.xml";


	/** The authority service. */
	private AuthorityService authorityService;

	/** The permission service. */
	private PermissionService permissionService;

	private ReportTplService reportTplService;

	
	private ContentHelper contentHelper;
	
	private DictionaryService dictionaryService;

	private EntityTplService entityTplService;

	private BeCPGMailService beCPGMailService;
	
	private DesignerInitService designerInitService;
	
	private EntitySystemService entitySystemService;

	/**
	 * Sets the authority service.
	 * 
	 * @param authorityService
	 *            the new authority service
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * Sets the permission service.
	 * 
	 * @param permissionService
	 *            the new permission service
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setContentHelper(ContentHelper contentHelper) {
		this.contentHelper = contentHelper;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setBeCPGMailService(BeCPGMailService beCPGMailService) {
		this.beCPGMailService = beCPGMailService;
	}
	
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	public void setDesignerInitService(DesignerInitService designerInitService) {
		this.designerInitService = designerInitService;
	}

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

		logger.debug("visit");

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
		
		//Lists of characteristics for Project
		visitSystemProjectListValuesEntity(systemNodeRef, RepoConsts.PATH_PROJECT_LISTS);
		
		// Exchange
		NodeRef exchangeNodeRef = visitFolder(companyHome, RepoConsts.PATH_EXCHANGE);
		NodeRef importNodeRef = visitFolder(exchangeNodeRef, RepoConsts.PATH_IMPORT);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_TO_TREAT);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_TO_DO);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_SUCCEEDED);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_FAILED);
		visitFolder(importNodeRef, RepoConsts.PATH_IMPORT_USER);

		// Products
		 visitFolder(companyHome, RepoConsts.PATH_PRODUCTS);
		 
		 //Projects
		 visitFolder(companyHome, RepoConsts.PATH_PROJECTS);

		// Quality
		NodeRef qualityNodeRef = visitFolder(companyHome, RepoConsts.PATH_QUALITY);
		// Regulations
		NodeRef regulationsNodeRef = visitFolder(qualityNodeRef, RepoConsts.PATH_REGULATIONS);
		visitFolder(regulationsNodeRef, RepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA);
		// Specifications
		NodeRef qualSpecNodeRef = visitFolder(qualityNodeRef, RepoConsts.PATH_QUALITY_SPECIFICATIONS);
		visitFolder(qualSpecNodeRef, RepoConsts.PATH_CONTROL_PLANS);
		visitFolder(qualSpecNodeRef, RepoConsts.PATH_CONTROL_POINTS);
		visitFolder(qualSpecNodeRef, RepoConsts.PATH_CONTROL_METHODS);
		visitFolder(qualSpecNodeRef, RepoConsts.PATH_CONTROL_STEPS);
		
		visitFolder(qualityNodeRef, RepoConsts.PATH_PRODUCT_SPECIFICATIONS);
		
		// NC
		visitFolder(qualityNodeRef, RepoConsts.PATH_NC);
		// QualityControls
		visitFolder(qualityNodeRef, RepoConsts.PATH_QUALITY_CONTROLS);

		// Security
		visitFolder(systemNodeRef, RepoConsts.PATH_SECURITY);

		// ECO
		visitFolder(systemNodeRef, RepoConsts.PATH_ECO);

		// Icons
		visitFolder(systemNodeRef, RepoConsts.PATH_ICON);

		// EntityTemplates
		visitEntityTpls(systemNodeRef);

		// MailTemplates
		contentHelper.addFilesResources(beCPGMailService.getEmailTemplatesFolder(), "classpath:beCPG/mails/*.ftl");	
		contentHelper.addFilesResources(beCPGMailService.getEmailWorkflowTemplatesFolder(), "classpath:beCPG/mails/workflow/*.ftl");

		// Companies
		NodeRef companiesNodeRef = visitFolder(companyHome, RepoConsts.PATH_COMPANIES);
		visitFolder(companiesNodeRef, RepoConsts.PATH_SUPPLIERS);
		visitFolder(companiesNodeRef, RepoConsts.PATH_CLIENTS);

		// Reports
		visitReports(systemNodeRef);

		// AutoNum
		visitFolder(systemNodeRef, RepoConsts.PATH_AUTO_NUM);

		// System exchange
		NodeRef systemExchangeNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_EXCHANGE);
		NodeRef systemImportNodeRef = visitFolder(systemExchangeNodeRef, RepoConsts.PATH_IMPORT);
		visitFolder(systemImportNodeRef, RepoConsts.PATH_MAPPING);

		visitFolder(systemImportNodeRef, RepoConsts.PATH_IMPORT_SAMPLES);
		
		// Project Tpl
		visitFolder(systemNodeRef, RepoConsts.PATH_PROJECT_TEMPLATES);
		
		//Designer		
		designerInitService.addReadOnlyDesignerFiles("classpath:alfresco/module/becpg-core/model/becpgModel.xml");
		designerInitService.addReadOnlyDesignerFiles("classpath:alfresco/module/becpg-core/model/qualityModel.xml");
		
		
		//OLAP
		visitFolder(systemNodeRef, RepoConsts.PATH_OLAP_QUERIES);
		
		
	}




	/**
	 * Add resources to folder
	 */
	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName) {

		if (folderName == RepoConsts.PATH_ICON) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/images/*.png");
		}
		if (folderName == RepoConsts.PATH_MAPPING) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/import/mapping/*.xml");
		}
		if (folderName == RepoConsts.PATH_IMPORT_SAMPLES) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/import/samples/*.csv");
		}
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

		if (folderName == RepoConsts.PATH_ENTITY_TEMPLATES) {
			specialiseType = BeCPGModel.TYPE_ENTITY_V2;
		} else if (folderName == RepoConsts.PATH_REPORTS) {

			// Action : apply type
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ReportModel.TYPE_REPORT_TPL);
			CompositeAction compositeAction = actionService.createCompositeAction();
			Action myAction = actionService.createAction(SpecialiseTypeActionExecuter.NAME, params);
			compositeAction.addAction(myAction);

			// Conditions for the Rule : type must be different
			ActionCondition actionCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
			actionCondition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, ReportModel.TYPE_REPORT_TPL);
			actionCondition.setInvertCondition(true);
			compositeAction.addActionCondition(actionCondition);

			// compare-name == *.rptdesign
			ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION,
					ComparePropertyValueOperation.ENDS.toString());
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE,
					ReportTplService.PARAM_VALUE_DESIGN_EXTENSION);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
			conditionOnName.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnName);

			// Create Rule
			Rule rule = new Rule();
			rule.setTitle("Specialise type");
			rule.setDescription("Every item created will have this type");
			rule.applyToChildren(applyToChildren);
			rule.setExecuteAsynchronously(false);
			rule.setRuleDisabled(false);
			rule.setRuleType(RuleType.INBOUND);
			rule.setAction(compositeAction);
			ruleService.saveRule(nodeRef, rule);
		}  else if (folderName == RepoConsts.PATH_SECURITY) {
			specialiseType = SecurityModel.TYPE_ACL_GROUP;
		} else if (folderName == RepoConsts.PATH_ECO) {
			specialiseType = ECMModel.TYPE_ECO;
		} else if (folderName == RepoConsts.PATH_IMPORT_TO_TREAT) {

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
		} else if (folderName == RepoConsts.PATH_IMPORT_USER) {

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
		else if (folderName == RepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA) {
			specialiseType = BeCPGModel.TYPE_PRODUCT_MICROBIO_CRITERIA;
		} else if (folderName == RepoConsts.PATH_CONTROL_PLANS) {
			specialiseType = QualityModel.TYPE_CONTROL_PLAN;
		} else if (folderName == RepoConsts.PATH_CONTROL_POINTS) {
			specialiseType = QualityModel.TYPE_CONTROL_POINT;
		} else if (folderName == RepoConsts.PATH_CONTROL_STEPS) {
			specialiseType = QualityModel.TYPE_CONTROL_STEP;
		} else if (folderName == RepoConsts.PATH_CONTROL_METHODS) {
			specialiseType = QualityModel.TYPE_CONTROL_METHOD;
		} else if (folderName == RepoConsts.PATH_QUALITY_CONTROLS) {
			specialiseType = QualityModel.TYPE_QUALITY_CONTROL;
		} else if (folderName == RepoConsts.PATH_NC) {
			specialiseType = QualityModel.TYPE_NC;
		} else if (folderName == RepoConsts.PATH_PROJECT_TEMPLATES) {
			createRuleAspect(nodeRef, true, ProjectModel.TYPE_PROJECT, BeCPGModel.ASPECT_ENTITY_TPL);
		} else if (folderName == RepoConsts.PATH_PRODUCT_SPECIFICATIONS) {
			specialiseType = BeCPGModel.TYPE_PRODUCT_SPECIFICATION;
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

			// boolean hasSystemMgrPerm = false;
			// Set<AccessPermission> permissions =
			// permissionService.getAllSetPermissions(nodeRef);
			// for(AccessPermission permission : permissions){
			// if(permission.getAuthority().equals(PermissionService.GROUP_PREFIX
			// + GROUP_SYSTEM_MGR) &&
			// permission.getPermission().equals(PermissionService.WRITE))

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_SYSTEM_MGR,
					PermissionService.WRITE, true);
		} else if (folderName == RepoConsts.PATH_PRODUCTS) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_RD_MGR,
					PermissionService.WRITE, true);
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_QUALITY_MGR,
					PermissionService.WRITE, true);
		} else if (folderName == RepoConsts.PATH_EXCHANGE) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_SYSTEM_MGR,
					PermissionService.WRITE, true);
		}

		else if (folderName == RepoConsts.PATH_QUALITY) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + GROUP_QUALITY_MGR,
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
		dataLists.add(BeCPGModel.TYPE_CONTACTLIST);
		entityTplService.createEntityTpl(entityTplsNodeRef, BeCPGModel.TYPE_SUPPLIER, true, dataLists, subFolders);

		// visit client
		dataLists = new LinkedHashSet<QName>();
		dataLists.add(BeCPGModel.TYPE_CONTACTLIST);
		entityTplService.createEntityTpl(entityTplsNodeRef, BeCPGModel.TYPE_CLIENT, true, dataLists, subFolders);

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
		
		// visit project and project Tpl
		dataLists = new LinkedHashSet<QName>();		
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
		dataLists.add(ProjectModel.TYPE_SCORE_LIST);
		entityTplService.createEntityTpl(entityTplsNodeRef, ProjectModel.TYPE_PROJECT, true, dataLists, null);
	}

	
	
	/** 
	 * Create system charact file
	 * @param systemNodeRef
	 * @param pathCharacts
	 * @return
	 */
	private NodeRef visitSystemCharactsEntity(NodeRef parentNodeRef, String path) {
		
		Map<String,QName> entityLists = new HashMap<String,QName>();
		
		entityLists.put(RepoConsts.PATH_NUTS, BeCPGModel.TYPE_NUT);
		entityLists.put(RepoConsts.PATH_INGS, BeCPGModel.TYPE_ING);
		entityLists.put(RepoConsts.PATH_ORGANOS,BeCPGModel.TYPE_ORGANO);
		entityLists.put(RepoConsts.PATH_ALLERGENS,BeCPGModel.TYPE_ALLERGEN);
		entityLists.put(RepoConsts.PATH_COSTS,BeCPGModel.TYPE_COST);
		entityLists.put(RepoConsts.PATH_PHYSICO_CHEM,BeCPGModel.TYPE_PHYSICO_CHEM);
		entityLists.put(RepoConsts.PATH_MICROBIOS,BeCPGModel.TYPE_MICROBIO);
		entityLists.put(RepoConsts.PATH_GEO_ORIGINS,BeCPGModel.TYPE_GEO_ORIGIN);
		entityLists.put(RepoConsts.PATH_BIO_ORIGINS,BeCPGModel.TYPE_BIO_ORIGIN);
		entityLists.put(RepoConsts.PATH_SUBSIDIARIES,BeCPGModel.TYPE_SUBSIDIARY);
		entityLists.put(RepoConsts.PATH_TRADEMARKS,BeCPGModel.TYPE_TRADEMARK);
		entityLists.put(RepoConsts.PATH_PLANTS,BeCPGModel.TYPE_PLANT);
		entityLists.put(RepoConsts.PATH_CERTIFICATIONS,BeCPGModel.TYPE_CERTIFICATION);
		entityLists.put(RepoConsts.PATH_APPROVALNUMBERS,BeCPGModel.TYPE_APPROVAL_NUMBER);
		entityLists.put(RepoConsts.PATH_PROCESSSTEPS,MPMModel.TYPE_PROCESSSTEP);
		entityLists.put(RepoConsts.PATH_VARIANT_CHARACTS,VariantModel.TYPE_CHARACT);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}
	
	

	private NodeRef visitSystemHierachiesEntity(NodeRef parentNodeRef, String path) {
		
       Map<String,QName> entityLists = new HashMap<String,QName>();
		
		entityLists.put(HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_RAWMATERIAL), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_PACKAGINGMATERIAL), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_FINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_PACKAGINGKIT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_RESOURCEPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		
		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
		
	}
	
	/**
	 * Create dyn List values
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	private NodeRef visitSystemListValuesEntity(NodeRef parentNodeRef, String path) {
		
		Map<String,QName> entityLists = new HashMap<String,QName>();
		
		entityLists.put(RepoConsts.PATH_ING_TYPES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_ALLERGEN_TYPES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_NUT_GROUPS,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_NUT_TYPES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_PACKAGING_LEVELS,BeCPGModel.TYPE_LIST_VALUE);
		
		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}
	
	/**
	 * Create NPD List values
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	private NodeRef visitSystemProjectListValuesEntity(NodeRef parentNodeRef, String path) {
		
		Map<String,QName> entityLists = new HashMap<String,QName>();
		
		entityLists.put(RepoConsts.PATH_TASK_LEGENDS,ProjectModel.TYPE_TASK_LEGEND);
		entityLists.put(RepoConsts.PATH_PROJECT_HIERARCHY, BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(RepoConsts.PATH_REQUEST_STATES,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_REQUEST_ORIGINS,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_SCORE_CRITERIA,BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(RepoConsts.PATH_SPONSORS,BeCPGModel.TYPE_LIST_VALUE);
		
		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	
	/**
	 * Create product tpls
	 * 
	 * @param entityTplsNodeRef
	 */
	private void visitProductTpls(NodeRef entityTplsNodeRef) {

		NodeRef productTplsNodeRef = visitFolder(entityTplsNodeRef, RepoConsts.PATH_PRODUCT_TEMPLATES);

		Set<QName> productTypes = new HashSet<QName>();
		productTypes.add(BeCPGModel.TYPE_RAWMATERIAL);
		productTypes.add(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT);
		productTypes.add(BeCPGModel.TYPE_FINISHEDPRODUCT);
		productTypes.add(BeCPGModel.TYPE_PACKAGINGMATERIAL);
		productTypes.add(BeCPGModel.TYPE_PACKAGINGKIT);
		productTypes.add(BeCPGModel.TYPE_RESOURCEPRODUCT);

		Set<String> subFolders = new HashSet<String>();
		subFolders.add(RepoConsts.PATH_IMAGES);
		subFolders.add(RepoConsts.PATH_DOCUMENTS);
		subFolders.add(RepoConsts.PATH_BRIEF);

		for (QName productType : productTypes) {

			// datalists
			Set<QName> dataLists = new LinkedHashSet<QName>();

			if (productType.equals(BeCPGModel.TYPE_RAWMATERIAL)) {

				dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
				dataLists.add(BeCPGModel.TYPE_COSTLIST);
				dataLists.add(BeCPGModel.TYPE_PRICELIST);
				dataLists.add(BeCPGModel.TYPE_NUTLIST);
				dataLists.add(BeCPGModel.TYPE_INGLIST);
				dataLists.add(BeCPGModel.TYPE_ORGANOLIST);
				dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);

			} else if (productType.equals(BeCPGModel.TYPE_PACKAGINGMATERIAL)) {

				dataLists.add(BeCPGModel.TYPE_COSTLIST);
				dataLists.add(BeCPGModel.TYPE_PRICELIST);
				dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);

			} else if (productType.equals(BeCPGModel.TYPE_RESOURCEPRODUCT)) {

				dataLists.add(BeCPGModel.TYPE_COSTLIST);
				dataLists.add(BeCPGModel.TYPE_PRICELIST);
				dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);

			} else if (productType.equals(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT)) {

				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				dataLists.add(MPMModel.TYPE_PROCESSLIST);
				dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
				dataLists.add(BeCPGModel.TYPE_COSTLIST);
				dataLists.add(BeCPGModel.TYPE_NUTLIST);
				dataLists.add(BeCPGModel.TYPE_INGLIST);
				dataLists.add(BeCPGModel.TYPE_ORGANOLIST);
				dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);

			} else if (productType.equals(BeCPGModel.TYPE_FINISHEDPRODUCT)) {

				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				dataLists.add(BeCPGModel.TYPE_PACKAGINGLIST);
				dataLists.add(MPMModel.TYPE_PROCESSLIST);
				dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
				dataLists.add(BeCPGModel.TYPE_COSTLIST);
				dataLists.add(BeCPGModel.TYPE_NUTLIST);
				dataLists.add(BeCPGModel.TYPE_INGLIST);
				dataLists.add(BeCPGModel.TYPE_ORGANOLIST);
				dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);

			} else if (productType.equals(BeCPGModel.TYPE_PACKAGINGKIT)) {

				dataLists.add(BeCPGModel.TYPE_PACKAGINGLIST);
				dataLists.add(BeCPGModel.TYPE_COSTLIST);
				dataLists.add(BeCPGModel.TYPE_PHYSICOCHEMLIST);

			} else if (productType.equals(SecurityModel.TYPE_ACL_GROUP)) {

				dataLists.add(SecurityModel.TYPE_ACL_ENTRY);

			}

			entityTplService.createEntityTpl(productTplsNodeRef, productType, true, dataLists, subFolders);
		}
	}

	private void visitQuality(NodeRef entityTplsNodeRef) {

		NodeRef qualityTplsNodeRef = visitFolder(entityTplsNodeRef, RepoConsts.PATH_QUALITY_TEMPLATES);

		// visit productMicrobioCriteria
		Set<QName> dataLists = new LinkedHashSet<QName>();
		dataLists.add(BeCPGModel.TYPE_MICROBIOLIST);
		entityTplService
				.createEntityTpl(qualityTplsNodeRef, BeCPGModel.TYPE_PRODUCT_MICROBIO_CRITERIA, true, dataLists, null);

		// visit productSpecification
		dataLists.clear();
		dataLists.add(BeCPGModel.TYPE_FORBIDDENINGLIST);
		entityTplService.createEntityTpl(qualityTplsNodeRef, BeCPGModel.TYPE_PRODUCT_SPECIFICATION, true, dataLists, null);

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
		NodeRef productReportTplsNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES);
		QName[] productTypes = { BeCPGModel.TYPE_RAWMATERIAL, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT,
				BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT, BeCPGModel.TYPE_FINISHEDPRODUCT,
				BeCPGModel.TYPE_PACKAGINGMATERIAL, BeCPGModel.TYPE_PACKAGINGKIT, BeCPGModel.TYPE_RESOURCEPRODUCT};

		for (QName productType : productTypes) {

			try {

				ClassDefinition classDef = dictionaryService.getClass(productType);
				String reportPath = Locale.getDefault().equals(Locale.FRENCH) ? PRODUCT_REPORT_PATH : PRODUCT_REPORT_EN_PATH;
				NodeRef compareProductFolderNodeRef = repoService.createFolderByPath(productReportTplsNodeRef,
						classDef.getTitle(), classDef.getTitle());
				reportTplService.createTplRptDesign(compareProductFolderNodeRef, classDef.getTitle(),
						reportPath, ReportType.Document, ReportFormat.PDF, productType, true, true, false);
			} catch (Exception e) {
				logger.error("Failed to create product report tpl. SystemProductType: " + productType, e);
			}

		}
		
		// quality report templates
		NodeRef qualityReportTplsNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_QUALITY_REPORTTEMPLATES);

		// nc
		try {

			ClassDefinition classDef = dictionaryService.getClass(QualityModel.TYPE_NC);
			NodeRef qualityFolderNodeRef = repoService.createFolderByPath(qualityReportTplsNodeRef,
					classDef.getTitle(), classDef.getTitle());
			reportTplService.createTplRptDesign(qualityFolderNodeRef, classDef.getTitle(),
					NC_REPORT_PATH, ReportType.Document, ReportFormat.PDF, QualityModel.TYPE_NC, true, true, false);
		} catch (Exception e) {
			logger.error("Failed to create nc report tpl. SystemProductType: " + QualityModel.TYPE_NC, e);
		}

		// compare report
		try {
			NodeRef compareProductFolderNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS);
			reportTplService.createTplRptDesign(compareProductFolderNodeRef,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_COMPARE_PRODUCTS),
					COMPARE_ENTITIES_REPORT_PATH, ReportType.System, ReportFormat.PDF, null, true, true, false);
		} catch (IOException e) {
			logger.error("Failed to create compare product report tpl.", e);
		}
		
		// eco report
		try {
			NodeRef ecoFolderNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_ECO);
			reportTplService.createTplRptDesign(ecoFolderNodeRef,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_ECO),
					ECO_REPORT_PATH, ReportType.System, ReportFormat.PDF, null, true, true, false);
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
					RepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS),
					EXPORT_PRODUCTS_REPORT_RPTFILE_PATH, ReportType.ExportSearch, ReportFormat.XLS,
					BeCPGModel.TYPE_PRODUCT, false, true, false);

			reportTplService
					.createTplRessource(exportSearchProductsNodeRef, EXPORT_PRODUCTS_REPORT_XMLFILE_PATH, false);
		} catch (IOException e) {
			logger.error("Failed to create export search report tpl.", e);
		}

		// export search NC
		try {
			NodeRef exportNCSynthesisNodeRef = visitFolder(exportSearchNodeRef,
					RepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES);

			reportTplService.createTplRptDesign(exportNCSynthesisNodeRef,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES),
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
				SystemGroup.Trade.toString(), SystemGroup.TradeUser.toString(), SystemGroup.TradeMgr.toString(),
				SystemGroup.ProductReviewer.toString(), NPDGroup.NPD.toString(),
				NPDGroup.NeedDefinition.toString(), NPDGroup.ValidateNeedDefinition.toString(),
				NPDGroup.DoPrototype.toString(), NPDGroup.StartProduction.toString(),
				NPDGroup.ValidateFaisability.toString(), NPDGroup.FaisabilityAssignersGroup.toString() };

		Set<String> zones = new HashSet<String>();
		zones.add(AuthorityService.ZONE_APP_DEFAULT);
		zones.add(AuthorityService.ZONE_APP_SHARE);
		zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

		for (String group : groups) {

			logger.debug("group: " + group);
			String groupName = I18NUtil.getMessage(String.format("%s.%s", LOCALIZATION_PFX_GROUP, group).toLowerCase());

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
				+ SystemGroup.Trade.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.TradeMgr.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Trade.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.TradeMgr.toString());
		if (!authorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.TradeUser.toString()))
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.Trade.toString(),
					PermissionService.GROUP_PREFIX + SystemGroup.TradeUser.toString());
		// NPD
		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX
				+ NPDGroup.NPD.toString(), true);

		for (String group : new String[] {NPDGroup.NeedDefinition.toString(),
				NPDGroup.ValidateNeedDefinition.toString(), NPDGroup.DoPrototype.toString(),
				NPDGroup.StartProduction.toString(), NPDGroup.ValidateFaisability.toString(),
				NPDGroup.FaisabilityAssignersGroup.toString() }) {
			if (!authorities.contains(PermissionService.GROUP_PREFIX + group))
				authorityService.addAuthority(PermissionService.GROUP_PREFIX + NPDGroup.NPD.toString(),
						PermissionService.GROUP_PREFIX + group);

		}

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX
				+ NPDGroup.FaisabilityAssignersGroup.toString(), true);

		for (String group : new String[] { SystemGroup.RDMgr.toString(), SystemGroup.QualityMgr.toString() }) {
			if (!authorities.contains(PermissionService.GROUP_PREFIX + group))
				authorityService.addAuthority(
						PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString(),
						PermissionService.GROUP_PREFIX + group);

		}

	}

	@Override
	protected void vivitFolderAspects(NodeRef folderNodeRef, String folderName) {
		switch (folderName) {
		case RepoConsts.PATH_ENTITY_TEMPLATES:
		case RepoConsts.PATH_PROJECT_TEMPLATES:
		case RepoConsts.PATH_REPORTS:
		case RepoConsts.PATH_SECURITY:
		case RepoConsts.PATH_ICON:	
			if(!nodeService.hasAspect(folderNodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER)){
				nodeService.addAspect(folderNodeRef, BeCPGModel.ASPECT_SYSTEM_FOLDER, null);
			}
			break;
		default:
			break;
		}
		
		
	}

}
