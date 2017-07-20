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
import java.util.Objects;
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
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMGroup;
import fr.becpg.model.ECMModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.NCGroup;
import fr.becpg.model.PLMGroup;
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
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.repository.AlfrescoRepository;
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
public class PLMInitRepoVisitor extends AbstractInitVisitorImpl {

	public static final String PRODUCT_REPORT_CLIENT_PATH = "beCPG/birt/document/product/default/ProductReport.rptdesign";
	public static final String PRODUCT_REPORT_CLIENT_NAME = "path.productreportclienttemplate";
	public static final String PRODUCT_REPORT_PRODUCTION_PATH = "beCPG/birt/document/product/default/ProductReport_Prod.rptdesign";
	public static final String PRODUCT_REPORT_PRODUCTION_NAME = "path.productreportproductiontemplate";
	public static final String PRODUCT_REPORT_RAWMATERIAL_PATH = "beCPG/birt/document/product/default/RawMaterialReport.rptdesign";
	public static final String PRODUCT_REPORT_SUPPLIER_NAME = "path.rawmaterialreporttemplate";
	private static final String NC_REPORT_PATH = "beCPG/birt/document/nonconformity/NCReport.rptdesign";
	private static final String QUALITY_CONTROL_REPORT_PATH = "beCPG/birt/document/qualitycontrol/QualityControlReport.rptdesign";
	private static final String ECO_REPORT_PATH = "beCPG/birt/document/ecm/ECOReport.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearchQuery.xml";
	private static final String EXPORT_NC_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/nonconformity/NonConformitySynthesis.rptdesign";
	private static final String EXPORT_NC_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/nonconformity/ExportSearchQuery.xml";
	private static final String EXPORT_RAWMATERIAL_INGLIST_XLSX_PATH = "beCPG/birt/exportsearch/product/ExportRawMaterialIngList.xlsx";
	private static final String EXPORT_INGLABELING_XLSX_PATH = "beCPG/birt/exportsearch/product/ExportIngLabellingList.xlsx";

	private static final String PRODUCT_REPORT_FR_RESOURCE = "beCPG/birt/document/product/default/ProductReport_fr.properties";
	private static final String PRODUCT_REPORT_EN_RESOURCE = "beCPG/birt/document/product/default/ProductReport_en.properties";

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
	private EntitySystemService entitySystemService;

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;

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

		// Dynamic constraints
		visitSystemListValuesEntity(systemNodeRef, RepoConsts.PATH_LISTS);

		// Hierarchy
		visitSystemHierachiesEntity(systemNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);

		// Lists of characteristics for Quality
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

		// Quality
		NodeRef qualityNodeRef = visitFolder(companyHome, PlmRepoConsts.PATH_QUALITY);
		// Regulations
		NodeRef regulationsNodeRef = visitFolder(qualityNodeRef, PlmRepoConsts.PATH_REGULATIONS);
		NodeRef folderNodeRef = visitFolder(regulationsNodeRef, PlmRepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA);
		addSystemFolderAspect(folderNodeRef);
		// Specifications
		NodeRef qualSpecNodeRef = visitFolder(qualityNodeRef, PlmRepoConsts.PATH_QUALITY_SPECIFICATIONS);
		visitFolder(qualSpecNodeRef, PlmRepoConsts.PATH_CONTROL_PLANS);
		visitFolder(qualSpecNodeRef, PlmRepoConsts.PATH_CONTROL_POINTS);

		folderNodeRef = visitFolder(qualityNodeRef, PlmRepoConsts.PATH_PRODUCT_SPECIFICATIONS);
		addSystemFolderAspect(folderNodeRef);

		// NC
		visitFolder(qualityNodeRef, PlmRepoConsts.PATH_NC);
		// QualityControls
		visitFolder(qualityNodeRef, PlmRepoConsts.PATH_QUALITY_CONTROLS);

		// ECO
		folderNodeRef = visitFolder(systemNodeRef, PlmRepoConsts.PATH_ECO);
		addSystemFolderAspect(folderNodeRef);

		// Icons
		visitFolder(systemNodeRef, RepoConsts.PATH_ICON);

		// EntityTemplates
		visitEntityTpls(systemNodeRef);

		// MailTemplates
		contentHelper.addFilesResources(beCPGMailService.getEmailTemplatesFolder(), "classpath*:beCPG/mails/*.ftl");
		contentHelper.addFilesResources(beCPGMailService.getEmailWorkflowTemplatesFolder(), "classpath*:beCPG/mails/workflow/*.ftl");

		// Reports
		visitReports(systemNodeRef);

		// AutoNum
		visitFolder(systemNodeRef, RepoConsts.PATH_AUTO_NUM);

		// System exchange
		NodeRef systemExchangeNodeRef = visitFolder(systemNodeRef, PlmRepoConsts.PATH_EXCHANGE);
		NodeRef systemImportNodeRef = visitFolder(systemExchangeNodeRef, PlmRepoConsts.PATH_IMPORT);
		visitFolder(systemImportNodeRef, PlmRepoConsts.PATH_MAPPING);

		visitFolder(systemImportNodeRef, PlmRepoConsts.PATH_IMPORT_SAMPLES);

		// OLAP
		visitFolder(systemNodeRef, RepoConsts.PATH_OLAP_QUERIES);
		
		//NutDatabases
		visitFolder(systemNodeRef, PlmRepoConsts.PATH_NUT_DATABASES);
	}

	/**
	 * Add resources to folder
	 */
	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName) {
		if (Objects.equals(folderName, RepoConsts.PATH_ICON)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/images/*.png");
		}
		if (Objects.equals(folderName, PlmRepoConsts.PATH_MAPPING)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/import/mapping/*.xml");
		}
		if (Objects.equals(folderName, RepoConsts.PATH_OLAP_QUERIES)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/olap/*.saiku");
		}
		if (Objects.equals(folderName, PlmRepoConsts.PATH_NUT_DATABASES)) {
			if(Locale.FRENCH.toString().equals(Locale.getDefault().getLanguage())){
				contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/databases/nuts/fr/*.csv");
			} else {
				contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/databases/nuts/en/*.csv");
			}
		}
	}

	/**
	 * Initialize the rules of the repository
	 */
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {

		QName specialiseType = null;
		boolean applyToChildren = false;

		if (Objects.equals(folderName, PlmRepoConsts.PATH_ECO)) {
			specialiseType = ECMModel.TYPE_ECO;
		} else if (Objects.equals(folderName, PlmRepoConsts.PATH_IMPORT_TO_TREAT)) {

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
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, ImporterActionExecuter.CSV_EXTENSION);
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
		} else if (Objects.equals(folderName, PlmRepoConsts.PATH_IMPORT_USER)) {

			// action
			CompositeAction compositeAction = actionService.createCompositeAction();
			Action action = actionService.createAction(UserImporterActionExecuter.NAME, null);
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
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, UserImporterActionExecuter.PARAM_VALUE_EXTENSION);
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
		else if (Objects.equals(folderName, PlmRepoConsts.PATH_PRODUCT_MICROBIO_CRITERIA)) {
			specialiseType = PLMModel.TYPE_PRODUCT_MICROBIO_CRITERIA;
		} else if (Objects.equals(folderName, PlmRepoConsts.PATH_CONTROL_PLANS)) {
			specialiseType = QualityModel.TYPE_CONTROL_PLAN;
		} else if (Objects.equals(folderName, PlmRepoConsts.PATH_CONTROL_POINTS)) {
			specialiseType = QualityModel.TYPE_CONTROL_POINT;
		} else if (Objects.equals(folderName, PlmRepoConsts.PATH_QUALITY_CONTROLS)) {
			specialiseType = QualityModel.TYPE_QUALITY_CONTROL;
		} else if (Objects.equals(folderName, PlmRepoConsts.PATH_NC)) {
			specialiseType = QualityModel.TYPE_NC;
		} else if (Objects.equals(folderName, PlmRepoConsts.PATH_PRODUCT_SPECIFICATIONS)) {
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

		if (Objects.equals(folderName, PlmRepoConsts.PATH_EXCHANGE)) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.SystemMgr.toString(), PermissionService.WRITE,
					true);
		}

		else if (Objects.equals(folderName, PlmRepoConsts.PATH_QUALITY)) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + PLMGroup.QualityMgr.toString(), PermissionService.WRITE, true);
		}

		else if (Objects.equals(folderName, PlmRepoConsts.PATH_NC)) {

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimStart, PermissionService.WRITE, true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimAnalysis.toString(), PermissionService.WRITE,
					true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimClassification.toString(), PermissionService.WRITE,
					true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimTreatment.toString(), PermissionService.WRITE,
					true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimResponse.toString(), PermissionService.WRITE,
					true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimClosing.toString(), PermissionService.WRITE, true);
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

		Set<String> subFolders = new HashSet<>();
		subFolders.add(RepoConsts.PATH_IMAGES);

		// visit supplier
		Set<QName> dataLists = new LinkedHashSet<>();
		dataLists.add(PLMModel.TYPE_CONTACTLIST);
		dataLists.add(PLMModel.TYPE_PLANT);
		NodeRef entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, PLMModel.TYPE_SUPPLIER, null, true, true, dataLists, subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

		// visit client
		dataLists = new LinkedHashSet<>();
		dataLists.add(PLMModel.TYPE_CONTACTLIST);
		entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, PLMModel.TYPE_CLIENT, null, true, true, dataLists, subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

		// visit ECO
		dataLists = new LinkedHashSet<>();
		dataLists.add(ECMModel.TYPE_REPLACEMENTLIST);
		dataLists.add(ECMModel.TYPE_WUSEDLIST);
		dataLists.add(ECMModel.TYPE_CALCULATEDCHARACTLIST);
		dataLists.add(ECMModel.TYPE_CHANGEUNITLIST);
		entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, ECMModel.TYPE_ECO, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit quality
		visitQuality(entityTplsNodeRef);
	}

	/**
	 * Create system charact file
	 *
	 * @param systemNodeRef
	 * @param pathCharacts
	 * @return
	 */
	private NodeRef visitSystemCharactsEntity(NodeRef parentNodeRef, String path) {

		Map<String, QName> entityLists = new LinkedHashMap<>();

		entityLists.put(PlmRepoConsts.PATH_NUTS, PLMModel.TYPE_NUT);
		entityLists.put(PlmRepoConsts.PATH_INGS, PLMModel.TYPE_ING);
		entityLists.put(PlmRepoConsts.PATH_ORGANOS, PLMModel.TYPE_ORGANO);
		entityLists.put(PlmRepoConsts.PATH_ALLERGENS, PLMModel.TYPE_ALLERGEN);
		entityLists.put(PlmRepoConsts.PATH_COSTS, PLMModel.TYPE_COST);
		entityLists.put(PlmRepoConsts.PATH_PHYSICO_CHEM, PLMModel.TYPE_PHYSICO_CHEM);
		entityLists.put(PlmRepoConsts.PATH_MICROBIOS, PLMModel.TYPE_MICROBIO);
		entityLists.put(PlmRepoConsts.PATH_GEO_ORIGINS, PLMModel.TYPE_GEO_ORIGIN);
		entityLists.put(PlmRepoConsts.PATH_BIO_ORIGINS, PLMModel.TYPE_BIO_ORIGIN);
		entityLists.put(PlmRepoConsts.PATH_SUBSIDIARIES, PLMModel.TYPE_SUBSIDIARY);
		entityLists.put(PlmRepoConsts.PATH_TRADEMARKS, PLMModel.TYPE_TRADEMARK);
		entityLists.put(PlmRepoConsts.PATH_PLANTS, PLMModel.TYPE_PLANT);
		entityLists.put(PlmRepoConsts.PATH_CUSTOMSCODES, PLMModel.TYPE_CUSTOMSCODE);
		entityLists.put(PlmRepoConsts.PATH_CERTIFICATIONS, PLMModel.TYPE_CERTIFICATION);
		entityLists.put(PlmRepoConsts.PATH_APPROVALNUMBERS, PLMModel.TYPE_APPROVAL_NUMBER);
		entityLists.put(PlmRepoConsts.PATH_LABELCLAIMS, PLMModel.TYPE_LABEL_CLAIM);
		entityLists.put(PlmRepoConsts.PATH_NUTRIENTPROFILES, PLMModel.TYPE_NUTRIENT_PROFILE);
		entityLists.put(PlmRepoConsts.PATH_PROCESSSTEPS, MPMModel.TYPE_PROCESSSTEP);
		entityLists.put(PlmRepoConsts.PATH_RESOURCEPARAMS, MPMModel.TYPE_RESOURCEPARAM);
		entityLists.put(PlmRepoConsts.PATH_VARIANT_CHARACTS, VariantModel.TYPE_CHARACT);
		entityLists.put(PlmRepoConsts.PATH_STORAGE_CONDITIONS, PLMModel.TYPE_STORAGE_CONDITIONS);
		entityLists.put(PlmRepoConsts.PATH_PRECAUTION_OF_USE, PLMModel.TYPE_PRECAUTION_OF_USE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_TEMPLATES, PackModel.TYPE_LABELING_TEMPLATE);
		entityLists.put(PlmRepoConsts.PATH_LABEL, PackModel.TYPE_LABEL);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	private NodeRef visitSystemHierachiesEntity(NodeRef parentNodeRef, String path) {

		Map<String, QName> entityLists = new LinkedHashMap<>();

		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_RAWMATERIAL), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_PACKAGINGMATERIAL), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_SEMIFINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_FINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_PACKAGINGKIT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_RESOURCEPRODUCT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_CLIENT), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_SUPPLIER), BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_HIERARCHY, BeCPGModel.TYPE_LINKED_VALUE);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	/**
	 * Create dyn List values
	 *
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	private NodeRef visitSystemListValuesEntity(NodeRef parentNodeRef, String path) {

		Map<String, QName> entityLists = new LinkedHashMap<>();

		entityLists.put(PlmRepoConsts.PATH_ING_TYPES, PLMModel.TYPE_ING_TYPE_ITEM);
		entityLists.put(PlmRepoConsts.PATH_TRADEMARK_TYPES,  BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_GROUPS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_FACTS_METHODS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_POSITIONS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABEL_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABELCLAIMS_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_MICROBIO_CONTROL_STEPS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_MICROBIO_UNITS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_RESOURCE_PARAM_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_PHYSICO_UNITS, BeCPGModel.TYPE_LIST_VALUE);
		
		entityLists.put(PlmRepoConsts.PATH_PM_MATERIALS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_PM_PRINT_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_PM_PRINT_VANISHS, BeCPGModel.TYPE_LIST_VALUE);
		
		entityLists.put(RepoConsts.PATH_REPORT_PARAMS, BeCPGModel.TYPE_LIST_VALUE);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	private NodeRef visitSystemQualityListValuesEntity(NodeRef parentNodeRef, String path) {

		Map<String, QName> entityLists = new LinkedHashMap<>();
		entityLists.put(PlmRepoConsts.PATH_CLAIM_ORIGIN_HIERARCHY, BeCPGModel.TYPE_LINKED_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_SOURCES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_DISTRIBUTION_NETWORKS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_TRACKING_VALUES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CLAIM_RESPONSES_STATES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_METHODS, QualityModel.TYPE_CONTROL_METHOD);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_STEPS, QualityModel.TYPE_CONTROL_STEP);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_CHARACTS, QualityModel.TYPE_CONTROL_CHARACT);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_UNITS, BeCPGModel.TYPE_LIST_VALUE);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	/**
	 * Create product tpls
	 *
	 * @param entityTplsNodeRef
	 */
	private void visitProductTpls(NodeRef entityTplsNodeRef) {

		NodeRef productTplsNodeRef = visitFolder(entityTplsNodeRef, PlmRepoConsts.PATH_PRODUCT_TEMPLATES);

		Set<QName> productTypes = new HashSet<>();
		productTypes.add(PLMModel.TYPE_RAWMATERIAL);
		productTypes.add(PLMModel.TYPE_SEMIFINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_FINISHEDPRODUCT);
		productTypes.add(PLMModel.TYPE_PACKAGINGMATERIAL);
		productTypes.add(PLMModel.TYPE_PACKAGINGKIT);
		productTypes.add(PLMModel.TYPE_RESOURCEPRODUCT);

		Set<String> subFolders = new HashSet<>();
		subFolders.add(RepoConsts.PATH_IMAGES);
		subFolders.add(RepoConsts.PATH_BRIEF);
		subFolders.add(RepoConsts.PATH_DOCUMENTS);

		for (QName productType : productTypes) {

			// datalists
			Set<QName> dataLists = new LinkedHashSet<>();
			QName wusedQName = null;

			if (productType.equals(PLMModel.TYPE_RAWMATERIAL)) {

				dataLists.add(PLMModel.TYPE_ALLERGENLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
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
				dataLists.add(MPMModel.TYPE_RESOURCEPARAMLIST);
				dataLists.add(PLMModel.TYPE_ALLERGENLIST);
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

			} else if (productType.equals(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {

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

			NodeRef entityTplNodeRef = entityTplService.createEntityTpl(productTplsNodeRef, productType, null, true, true, dataLists, subFolders);
			if (wusedQName != null) {
				entityTplService.createWUsedList(entityTplNodeRef, wusedQName, null);
			}
			entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
			
			if(!productType.equals(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {
				entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_REPORTS);
				entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);
			}
			
		}
	}

	private void visitQuality(NodeRef entityTplsNodeRef) {

		NodeRef qualityTplsNodeRef = visitFolder(entityTplsNodeRef, PlmRepoConsts.PATH_QUALITY_TEMPLATES);

		// visit productMicrobioCriteria
		Set<QName> dataLists = new LinkedHashSet<>();
		dataLists.add(PLMModel.TYPE_MICROBIOLIST);
		NodeRef entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, PLMModel.TYPE_PRODUCT_MICROBIO_CRITERIA, null, true, true,
				dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit productSpecification
		dataLists.clear();
		dataLists.add(PLMModel.TYPE_FORBIDDENINGLIST);
		dataLists.add(PLMModel.TYPE_LABELING_RULE_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, PLMModel.TYPE_PRODUCT_SPECIFICATION, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit controlPlan
		Set<String> subFolders = new HashSet<>();
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_SAMPLINGDEF_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_CONTROL_PLAN, null, true, true, dataLists, subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit qualityControl
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_SAMPLING_LIST);
		dataLists.add(QualityModel.TYPE_CONTROL_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_QUALITY_CONTROL, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_REPORTS);

		// visit controlPoint
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_CONTROLDEF_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_CONTROL_POINT, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit workItemAnalysis
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_CONTROL_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_WORK_ITEM_ANALYSIS, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit NC
		subFolders = new HashSet<>();
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_WORK_LOG);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_NC, null, true, true, dataLists, subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
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
		String productReportClientName = I18NUtil.getMessage(PRODUCT_REPORT_CLIENT_NAME, Locale.getDefault());
		String productReportSupplierName = I18NUtil.getMessage(PRODUCT_REPORT_SUPPLIER_NAME, Locale.getDefault());
		String productReportProductionName = I18NUtil.getMessage(PRODUCT_REPORT_PRODUCTION_NAME, Locale.getDefault());

		try {

			QName[] productTypes = { PLMModel.TYPE_FINISHEDPRODUCT, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_SEMIFINISHEDPRODUCT,
					PLMModel.TYPE_PACKAGINGMATERIAL };
			String[] defaultReport = { PRODUCT_REPORT_CLIENT_PATH, PRODUCT_REPORT_RAWMATERIAL_PATH, PRODUCT_REPORT_PRODUCTION_PATH,
					PRODUCT_REPORT_CLIENT_PATH };
			String[] defaultReportName = { productReportClientName, productReportSupplierName, productReportProductionName, productReportClientName };
			String[] otherReport = { PRODUCT_REPORT_PRODUCTION_PATH, null, null, null };
			String[] otherReportName = { productReportProductionName, null, null, null };
			String[] productReportResource = { PRODUCT_REPORT_FR_RESOURCE, PRODUCT_REPORT_EN_RESOURCE };

			int i = 0;

			for (QName productType : productTypes) {

				ClassDefinition classDef = dictionaryService.getClass(productType);

				if (repoService.getFolderByPath(productReportTplsNodeRef, classDef.getTitle(dictionaryService)) == null) {

					NodeRef folderNodeRef = repoService.getOrCreateFolderByPath(productReportTplsNodeRef, classDef.getTitle(dictionaryService),
							classDef.getTitle(dictionaryService));

					if ((defaultReport[i] != null) && (defaultReportName[i] != null)) {
						reportTplService.createTplRptDesign(folderNodeRef, defaultReportName[i], defaultReport[i], ReportType.Document,
								ReportFormat.PDF, productType, true, true, false);
						if (defaultReportName[i].equals(productReportSupplierName) || defaultReportName[i].equals(productReportClientName)) {
							for (String element : productReportResource) {
								reportTplService.createTplRessource(productReportTplsNodeRef, element, true);
							}
						}
					}

					if ((otherReport[i] != null) && (otherReportName[i] != null)) {
						reportTplService.createTplRptDesign(folderNodeRef, otherReportName[i], otherReport[i], ReportType.Document, ReportFormat.PDF,
								productType, true, false, false);
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
			NodeRef qualityFolderNodeRef = repoService.getOrCreateFolderByPath(qualityReportTplsNodeRef, classDef.getTitle(dictionaryService),
					classDef.getTitle(dictionaryService));
			reportTplService.createTplRptDesign(qualityFolderNodeRef, classDef.getTitle(dictionaryService), NC_REPORT_PATH, ReportType.Document,
					ReportFormat.PDF, QualityModel.TYPE_NC, true, true, false);
		} catch (Exception e) {
			logger.error("Failed to create nc report tpl." + QualityModel.TYPE_NC, e);
		}

		
		try {

			ClassDefinition classDef = dictionaryService.getClass(QualityModel.TYPE_QUALITY_CONTROL);
			NodeRef qualityFolderNodeRef = repoService.getOrCreateFolderByPath(qualityReportTplsNodeRef, classDef.getTitle(dictionaryService),
					classDef.getTitle(dictionaryService));
			reportTplService.createTplRptDesign(qualityFolderNodeRef, classDef.getTitle(dictionaryService), QUALITY_CONTROL_REPORT_PATH, ReportType.Document,
					ReportFormat.PDF, QualityModel.TYPE_QUALITY_CONTROL, true, true, false);
		} catch (Exception e) {
			logger.error("Failed to create nc report tpl." + QualityModel.TYPE_QUALITY_CONTROL, e);
		}
		
		// eco report
		try {
			NodeRef ecoFolderNodeRef = visitFolder(reportsNodeRef, PlmRepoConsts.PATH_REPORTS_ECO);
			reportTplService.createTplRptDesign(ecoFolderNodeRef, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_ECO), ECO_REPORT_PATH,
					ReportType.Document, ReportFormat.PDF, ECMModel.TYPE_ECO, true, true, false);
		} catch (IOException e) {
			logger.error("Failed to create eco report tpl.", e);
		}

		/*
		 * Export Search reports
		 */
		NodeRef exportSearchNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_EXPORT_SEARCH);

		// export search products
		try {
			NodeRef exportSearchProductsNodeRef = visitFolder(exportSearchNodeRef, PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS), EXPORT_PRODUCTS_REPORT_RPTFILE_PATH,
					ReportType.ExportSearch, ReportFormat.XLSX, PLMModel.TYPE_PRODUCT, false, false, false);

			reportTplService.createTplRessource(exportSearchProductsNodeRef, EXPORT_PRODUCTS_REPORT_XMLFILE_PATH, false);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_RAWMATERIAL_INGLIST),
					EXPORT_RAWMATERIAL_INGLIST_XLSX_PATH, ReportType.ExportSearch, ReportFormat.XLSX, PLMModel.TYPE_RAWMATERIAL, false, false, false);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_INGLABELING), EXPORT_INGLABELING_XLSX_PATH,
					ReportType.ExportSearch, ReportFormat.XLSX, PLMModel.TYPE_PRODUCT, false, false, false);

		} catch (IOException e) {
			logger.error("Failed to create export search report tpl.", e);
		}

		// export search NC
		try {
			NodeRef exportNCSynthesisNodeRef = visitFolder(exportSearchNodeRef, PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES);

			reportTplService.createTplRptDesign(exportNCSynthesisNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES), EXPORT_NC_REPORT_RPTFILE_PATH,
					ReportType.ExportSearch, ReportFormat.PDF, QualityModel.TYPE_NC, false, true, false);

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

		String[] groups = { PLMGroup.RD.toString(), PLMGroup.RDUser.toString(), PLMGroup.RDMgr.toString(), PLMGroup.Quality.toString(),
				PLMGroup.QualityUser.toString(), PLMGroup.QualityMgr.toString(), PLMGroup.Purchasing.toString(), PLMGroup.PurchasingUser.toString(),
				PLMGroup.PurchasingMgr.toString(), PLMGroup.Production.toString(), PLMGroup.ProductionUser.toString(),
				PLMGroup.ProductionMgr.toString(), PLMGroup.ReferencingMgr.toString(), PLMGroup.Trade.toString(), PLMGroup.TradeUser.toString(),
				PLMGroup.TradeMgr.toString(), NCGroup.ClaimStart.toString(), NCGroup.ClaimAnalysis.toString(), NCGroup.ClaimClassification.toString(),
				NCGroup.ClaimTreatment.toString(), NCGroup.ClaimResponse.toString(), NCGroup.ClaimClosing.toString(), ECMGroup.CreateChangeOrder.toString(),
				ECMGroup.ApplyChangeOrder.toString() };

		createGroups(groups);

		// Group hierarchy
		Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
				PermissionService.GROUP_PREFIX + PLMGroup.RD.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.RDMgr.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.RD.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.RDMgr.toString());
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.RDUser.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.RD.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.RDUser.toString());
		}

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + PLMGroup.Quality.toString(),
				true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.QualityMgr.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Quality.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.QualityMgr.toString());
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.QualityUser.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Quality.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.QualityUser.toString());
		}

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + PLMGroup.Purchasing.toString(),
				true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.PurchasingMgr.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Purchasing.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.PurchasingMgr.toString());
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.PurchasingUser.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Purchasing.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.PurchasingUser.toString());
		}

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + PLMGroup.Production.toString(),
				true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.ProductionMgr.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Production.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.ProductionMgr.toString());
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.ProductionUser.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Production.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.ProductionUser.toString());
		}

		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + PLMGroup.Trade.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.TradeMgr.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Trade.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.TradeMgr.toString());
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + PLMGroup.TradeUser.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.Trade.toString(),
					PermissionService.GROUP_PREFIX + PLMGroup.TradeUser.toString());
		}
		
		
		
		authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP, PermissionService.GROUP_PREFIX + SystemGroup.SecurityRole.toString(),
				true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + ECMGroup.ApplyChangeOrder.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.SecurityRole.toString(),
					PermissionService.GROUP_PREFIX + ECMGroup.ApplyChangeOrder.toString());
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + ECMGroup.CreateChangeOrder.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.SecurityRole.toString(),
					PermissionService.GROUP_PREFIX + ECMGroup.CreateChangeOrder.toString());
		}
		
	}
	
	@Override
	public Integer initOrder() {
		return 3;
	}
}
