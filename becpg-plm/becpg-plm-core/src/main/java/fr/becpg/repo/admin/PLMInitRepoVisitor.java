/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.evaluator.compare.ContentPropertyName;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMGroup;
import fr.becpg.model.ECMModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.MPMModel;
import fr.becpg.model.NCGroup;
import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.PublicationModel;
import fr.becpg.model.QualityModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.action.executer.ImporterActionExecuter;
import fr.becpg.repo.action.executer.UserImporterActionExecuter;
import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.notification.data.RecurringTimeType;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.report.template.ReportTplInformation;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.data.DateFilterType;
import fr.becpg.repo.search.data.VersionFilterType;
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
 * @version $Id: $Id
 */
@Service
public class PLMInitRepoVisitor extends AbstractInitVisitorImpl {

	/** Constant <code>logger</code> */
	protected static final Log logger = LogFactory.getLog(PLMInitRepoVisitor.class);

	private static List<String> supportedLocale = Arrays.asList("fr", "en", "es", "en_US", "it", "nl", "sv_SE", "fi", "ru", "pt");

	private static final String SIMULATION_SITE_ID = "simulation";
	private static final String VALID_SITE_ID = "valid";
	private static final String ARCHIVED_SITE_ID = "archived";

	private static final String PRODUCT_REPORT_CLIENT_PATH = "beCPG/birt/document/product/default/ProductReport.rptdesign";
	private static final String PRODUCT_REPORT_CLIENT_NAME = "path.productreportclienttemplate";
	private static final String PRODUCT_REPORT_PRODUCTION_PATH = "beCPG/birt/document/product/default/ProductReport_Prod.rptdesign";
	private static final String PRODUCT_REPORT_PRODUCTION_NAME = "path.productreportproductiontemplate";

	private static final String PRODUCT_REPORT_PACKAGING_PATH = "beCPG/birt/document/product/default/PackagingReport.rptdesign";
	private static final String PRODUCT_REPORT_COST_PATH = "beCPG/birt/document/product/default/ProductReport_Cost.rptdesign";
	private static final String PRODUCT_REPORT_COST_NAME = "path.productreportcosttemplate";
	private static final String PRODUCT_REPORT_RD_PATH = "beCPG/birt/document/product/default/ProductReport_RD.rptdesign";
	private static final String PRODUCT_REPORT_RD_NAME = "path.productreportrdtemplate";
	private static final String PRODUCT_REPORT_TECHNICAL_SHEET_NAME = "path.productreporttechnicalsheettemplate";

	private static final String COMPARE_PRODUCTS_REPORT_PATH = "beCPG/birt/system/CompareProducts.rptdesign";

	private static final String PRODUCT_REPORT_SUPPLIER_PATH = "beCPG/birt/document/product/default/SupplierReport.rptdesign";
	private static final String PRODUCT_REPORT_SUPPLIER_NAME = "path.productreportsuppliertemplate";

	private static final String NC_REPORT_PATH = "beCPG/birt/document/nonconformity/NCReport.rptdesign";
	private static final String QUALITY_CONTROL_REPORT_PATH = "beCPG/birt/document/qualitycontrol/QualityControlReport.rptdesign";
	private static final String QUALITY_CONTROL_AGING_REPORT_PATH = "beCPG/birt/document/qualitycontrol/QualityControlAgingReport.rptdesign";
	private static final String QUALITY_CONTROL_AGING_NAME = "path.aging";

	private static final String QUALITY_REPORT_RESOURCE_BY_LOCALE = "beCPG/birt/document/qualitycontrol/QualityControlReport_%s.properties";
	private static final String QUALITY_REPORT_RESOURCE = "beCPG/birt/document/qualitycontrol/QualityControlReport.properties";

	private static final String ECO_REPORT_PATH = "beCPG/birt/document/ecm/ECOReport.rptdesign";

	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/product/%s/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/product/%s/ExportSearchQuery.xml";
	private static final String EXPORT_NC_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/nonconformity/%s/NonConformitySynthesis.rptdesign";
	private static final String EXPORT_NC_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/nonconformity/%s/ExportSearchQuery.xml";

	private static final String NC_REPORT_RESOURCE_BY_LOCALE = "beCPG/birt/document/nonconformity/NCReport_%s.properties";
	private static final String NC_REPORT_RESOURCE = "beCPG/birt/document/nonconformity/NCReport.properties";

	private static final String PRODUCT_REPORT_RAWMATERIAL_PATH = "beCPG/birt/document/product/default/RawMaterialReport.rptdesign";

	private static final Map<String, String> reportKindCodes = new HashMap<>();
	private static final String NONE_KIND_REPORT = "none";

	static {
		reportKindCodes.put(PRODUCT_REPORT_CLIENT_PATH, "CustomerSheet");
		reportKindCodes.put(PRODUCT_REPORT_PRODUCTION_PATH, "ProductionSheet");
		reportKindCodes.put(PRODUCT_REPORT_RAWMATERIAL_PATH, "SupplierSheet");
		reportKindCodes.put(PRODUCT_REPORT_SUPPLIER_PATH, "SupplierSheet");
		reportKindCodes.put(NONE_KIND_REPORT, "None");
	}

	private static final String EXPORT_LABELLING_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportLabelling.xlsx";
	private static final String EXPORT_CITEO_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportCiteo.xlsx";
	private static final String EXPORT_ALLERGENS_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportAllergens.xlsx";
	private static final String EXPORT_NUTRIENTS_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportNutrients.xlsx";
	private static final String EXPORT_SUPPLIERS_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportSuppliers.xlsx";
	private static final String EXPORT_QUALITY_CONTROLS_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportQualityControls.xlsx";
	private static final String EXPORT_INGLIST_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportIngList.xlsx";
	private static final String EXPORT_LABELCLAIMLIST_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportLabelClaimList.xlsx";
	private static final String EXPORT_PHYSICOCHEMICALLIST_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportPhysicoChemicalList.xlsx";
	private static final String EXPORT_COMPOSITIONPACKAGING_XLSX_PATH = "beCPG/birt/exportsearch/product/%s/ExportCompositionPackaging.xlsx";

	private static final String PRODUCT_REPORT_DE_RESOURCE = "beCPG/birt/document/product/default/ProductReport_de.properties";
	private static final String PRODUCT_REPORT_EN_US_RESOURCE = "beCPG/birt/document/product/default/ProductReport_en_US.properties";
	private static final String PRODUCT_REPORT_EN_RESOURCE = "beCPG/birt/document/product/default/ProductReport_en.properties";
	private static final String PRODUCT_REPORT_ES_RESOURCE = "beCPG/birt/document/product/default/ProductReport_es.properties";
	private static final String PRODUCT_REPORT_FI_RESOURCE = "beCPG/birt/document/product/default/ProductReport_fi.properties";
	private static final String PRODUCT_REPORT_FR_RESOURCE = "beCPG/birt/document/product/default/ProductReport_fr.properties";
	private static final String PRODUCT_REPORT_IT_RESOURCE = "beCPG/birt/document/product/default/ProductReport_it.properties";
	private static final String PRODUCT_REPORT_NL_RESOURCE = "beCPG/birt/document/product/default/ProductReport_nl.properties";
	private static final String PRODUCT_REPORT_PT_RESOURCE = "beCPG/birt/document/product/default/ProductReport_pt.properties";
	private static final String PRODUCT_REPORT_RU_RESOURCE = "beCPG/birt/document/product/default/ProductReport_ru.properties";
	private static final String PRODUCT_REPORT_SV_RESOURCE = "beCPG/birt/document/product/default/ProductReport_sv.properties";

	private static final String PRODUCT_REPORT_SETTINGS_RESOURCE = "beCPG/birt/document/product/default/settings.properties";
	private static final String PRODUCT_REPORT_LOGO_RESOURCE = "beCPG/birt/document/product/default/logo.png";
	private static final String PRODUCT_REPORT_CSS_RESOURCE = "beCPG/birt/document/product/default/becpg-report.css";
	private static final String PRODUCT_REPORT_IMG_CCCCCC = "beCPG/birt/document/product/default/cccccc-200X30.png";
	private static final String PRODUCT_REPORT_IMG_TRAFFICLIGHTS_ENERGY = "beCPG/birt/document/product/default/images/trafficLights_Energy.png";
	private static final String PRODUCT_REPORT_IMG_TRAFFICLIGHTS_GREEN = "beCPG/birt/document/product/default/images/trafficLights_Green.png";
	private static final String PRODUCT_REPORT_IMG_TRAFFICLIGHTS_ORANGE = "beCPG/birt/document/product/default/images/trafficLights_Orange.png";
	private static final String PRODUCT_REPORT_IMG_TRAFFICLIGHTS_RED = "beCPG/birt/document/product/default/images/trafficLights_Red.png";
	private static final String PRODUCT_REPORT_IMG_TRAFFICLIGHTS_SERVING = "beCPG/birt/document/product/default/images/trafficLights_Serving.png";

	private static final String CLASSIFY_RULE_TITLE = "classifyEntityRule";

	@Autowired
	private SiteService siteService;

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

	@Autowired
	@Qualifier("mlAwareNodeService")
	protected NodeService mlNodeService;

	@Autowired
	protected BeCPGCacheService beCPGCacheService;

	@Autowired
	private Repository repository;

	@Autowired
	private AssociationService associationService;

	/**
	 * {@inheritDoc}
	 *
	 * Initialize the repository with system folders.
	 */
	@Override
	public List<SiteInfo> visitContainer(NodeRef companyHome) {

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
		NodeRef qualityListNodeRef = visitSystemQualityListValuesEntity(systemNodeRef, PlmRepoConsts.PATH_QUALITY_LISTS);
		fillSystemQualityList(qualityListNodeRef);

		//Lists of characteristics security
		visitSystemSecurityListValuesEntity(systemNodeRef, PlmRepoConsts.PATH_SECURITY_LISTS);

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
		createNotifications(systemNodeRef);

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

		// NutDatabases
		visitFolder(systemNodeRef, PlmRepoConsts.PATH_NUT_DATABASES);

		// LCADatabases
		visitFolder(systemNodeRef, PlmRepoConsts.PATH_LCA_DATABASES);

		// Property catalogs
		visitFolder(systemNodeRef, PlmRepoConsts.PATH_CATALOGS);

		//Config folder
		visitFolder(systemNodeRef, PlmRepoConsts.PATH_CONFIG);

		beCPGCacheService.clearCache(EntityCatalogService.class.getName());

		visitFolder(systemNodeRef, PlmRepoConsts.PATH_WORKFLOW_SCRIPTS);

		addClassifyRule(companyHome);

		// signature scripts
		NodeRef scriptsFolderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, "./app:dictionary/app:scripts");
		contentHelper.addFilesResources(scriptsFolderNodeRef, "classpath*:beCPG/signature/*.js");

		// Create default sites
		return visitSites();

	}

	private void fillSystemQualityList(NodeRef qualityListNodeRef) {

		NodeRef controTypesFolder = entitySystemService.getSystemEntityDataList(qualityListNodeRef, PlmRepoConsts.PATH_CONTROL_TYPES);

		if (controTypesFolder != null) {

			for (String value : Arrays.asList("bcpg_allergenList", "bcpg_microbioList", "bcpg_nutList", "bcpg_physicoChemList", "bcpg_organoList",
					"dimension", "weight", "composition", "packaging", "volume", "performance", "counter_analysis", "contaminant")) {

				NodeRef nodeRef = nodeService.getChildByName(controTypesFolder, ContentModel.ASSOC_CONTAINS, value);
				if (nodeRef == null) {

					MLText mltValue = new MLText();

					for (String locKey : supportedLocale) {
						String i18NKey = I18NUtil.getMessage("listconstraint.qa_controlTypes." + value, MLTextHelper.parseLocale(locKey));
						if (i18NKey != null) {
							mltValue.put(MLTextHelper.parseLocale(locKey), i18NKey);
						}
					}
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, value);
					properties.put(BeCPGModel.PROP_LV_CODE, value);
					properties.put(BeCPGModel.PROP_LV_VALUE, mltValue);
					mlNodeService.createNode(controTypesFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, BeCPGModel.TYPE_LIST_VALUE,
							properties);
				}
			}
		}
	}

	private void addClassifyRule(NodeRef companyHome) {

		NodeRef scriptsFolderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, "./app:dictionary/app:scripts");
		List<NodeRef> scriptNodeRefs = contentHelper.addFilesResources(scriptsFolderNodeRef, "classpath:beCPG/rules/classify-entity.js");

		List<Rule> rules = ruleService.getRules(companyHome, false);

		for (Rule rule : rules) {
			if (CLASSIFY_RULE_TITLE.equals(rule.getTitle())) {
				return;
			}
		}

		CompositeAction compositeAction = actionService.createCompositeAction();
		Action action = actionService.createAction(ScriptActionExecuter.NAME, null);
		action.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, scriptNodeRefs.get(0));

		compositeAction.addAction(action);

		ActionCondition condition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
		condition.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE, BeCPGModel.TYPE_ENTITY_V2);
		condition.setInvertCondition(false);
		compositeAction.addActionCondition(condition);

		boolean isDisabled = false;
		List<SiteInfo> sites = siteService.listSites(null, null);
		if ((sites != null) && (sites.size() > 2)) {
			isDisabled = true;
		}

		// rule
		Rule rule = new Rule();
		rule.setRuleType(RuleType.INBOUND);
		rule.setRuleTypes(Arrays.asList(RuleType.INBOUND, RuleType.UPDATE));
		rule.setAction(compositeAction);
		rule.applyToChildren(true);
		rule.setTitle(CLASSIFY_RULE_TITLE);
		rule.setExecuteAsynchronously(false);
		rule.setRuleDisabled(isDisabled);
		rule.setDescription("Classify entity by state");

		ruleService.saveRule(companyHome, rule);

	}

	private List<SiteInfo> visitSites() {

		List<SiteInfo> ret = new ArrayList<>();

		List<SiteInfo> sites = siteService.listSites(null, null);
		if ((sites != null) && (sites.size() > 2)) {
			return ret;
		}

		for (String siteId : new String[] { SIMULATION_SITE_ID, VALID_SITE_ID, ARCHIVED_SITE_ID }) {

			SiteInfo siteInfo = siteService.getSite(siteId);
			if (siteInfo == null) {
				siteInfo = siteService.createSite(siteId + "-product-site-dashboard", siteId, I18NUtil.getMessage("plm.site." + siteId + ".title"),
						"", SiteVisibility.PRIVATE);

				// pre-create doclib
				NodeRef documentLibraryNodeRef = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

				if (SIMULATION_SITE_ID.equals(siteId)) {

					// Manager
					for (PLMGroup authority : new PLMGroup[] { PLMGroup.RDMgr, PLMGroup.QualityMgr, PLMGroup.PackagingMgr }) {

						siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + authority.toString(),
								SiteModel.SITE_MANAGER);
					}

					// Collaborator
					for (PLMGroup authority : new PLMGroup[] { PLMGroup.RDUser, PLMGroup.QualityUser, PLMGroup.PackagingUser }) {

						siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + authority.toString(),
								SiteModel.SITE_COLLABORATOR);

						permissionService.setPermission(documentLibraryNodeRef, PermissionService.GROUP_PREFIX + authority.toString(),
								PermissionService.COORDINATOR, true);

					}

					// Consumer
					for (PLMGroup authority : new PLMGroup[] { PLMGroup.ProductionMgr }) {
						siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + authority.toString(),
								SiteModel.SITE_CONSUMER);

					}

					siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + PLMGroup.ProductionMgr.toString(),
							SiteModel.SITE_CONSUMER);

				} else if (VALID_SITE_ID.equals(siteId)) {

					// Manager
					for (PLMGroup authority : new PLMGroup[] { PLMGroup.RDMgr, PLMGroup.QualityMgr }) {

						siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + authority.toString(),
								SiteModel.SITE_MANAGER);
					}

					// Consumer
					for (PLMGroup authority : new PLMGroup[] { PLMGroup.RDUser, PLMGroup.QualityUser, PLMGroup.PackagingUser, PLMGroup.ProductionMgr,
							PLMGroup.ProductionUser, PLMGroup.TradeUser }) {
						siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + authority.toString(),
								SiteModel.SITE_CONSUMER);

					}

				} else if (ARCHIVED_SITE_ID.equals(siteId)) {
					// Manager
					for (PLMGroup authority : new PLMGroup[] { PLMGroup.RDMgr, PLMGroup.QualityMgr }) {

						siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + authority.toString(),
								SiteModel.SITE_MANAGER);
					}
				}

				ret.add(siteInfo);
			}

		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Add resources to folder
	 */
	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName, boolean folderExists) {
		if (Objects.equals(folderName, RepoConsts.PATH_ICON)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/images/*.png");
		}
		if (Objects.equals(folderName, PlmRepoConsts.PATH_MAPPING)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/import/mapping/*.xml");
		}
		if (Objects.equals(folderName, RepoConsts.PATH_OLAP_QUERIES) && !folderExists) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/olap/*.saiku");
		}
		if (Objects.equals(folderName, PlmRepoConsts.PATH_NUT_DATABASES)) {
			if (Locale.FRENCH.toString().equals(Locale.getDefault().getLanguage())) {
				contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/databases/nuts/fr/*.csv");
			} else {
				contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/databases/nuts/en/*.csv");
			}
		}
		if (Objects.equals(folderName, PlmRepoConsts.PATH_LCA_DATABASES)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/databases/ecoscore/agribalyse_3_0.csv");
		}
		if (Objects.equals(folderName, PlmRepoConsts.PATH_CATALOGS)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/catalogs/*.json");
		}

		if (Objects.equals(folderName, PlmRepoConsts.PATH_CONFIG)) {

			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/search/*.json");
			contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/script/workflow/*.json");

		}

		if (Objects.equals(folderName, PlmRepoConsts.PATH_WORKFLOW_SCRIPTS)) {
			for (NodeRef scriptNodeRef : contentHelper.addFilesResources(folderNodeRef, "classpath*:beCPG/script/workflow/*.js")) {
				String title = (String) nodeService.getProperty(scriptNodeRef, ContentModel.PROP_TITLE);
				if ((title == null) || title.isEmpty()) {
					nodeService.setProperty(scriptNodeRef, ContentModel.PROP_TITLE,
							I18NUtil.getMessage("plm.script." + nodeService.getProperty(scriptNodeRef, ContentModel.PROP_NAME) + ".title"));
				}

			}
		}

	}

	private void createNotifications(NodeRef systemNodeRef) {

		NodeRef folderNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);

		NodeRef listContainer = folderNodeRef == null ? null
				: nodeService.getChildByName(folderNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);

		NodeRef notificationFolder = listContainer == null ? null
				: nodeService.getChildByName(listContainer, ContentModel.ASSOC_CONTAINS, RepoConsts.PATH_NOTIFICATIONS);

		createRequirementsNotification(notificationFolder);

		createObsoleteDocumentsNotification(notificationFolder);

		createInProgressProjectsNotification(notificationFolder);

		createValidatedProductsNotification(notificationFolder);

		createValidatedAndUpdatedProductsNotification(notificationFolder);

		createArchivedProductsNotification(notificationFolder);
	}

	private void createNotification(NodeRef notificationFolder, Map<QName, Serializable> properties, String mailTemplate,
			NodeRef notificationAuthorities, NodeRef target) {
		NodeRef notification = notificationFolder == null ? null
				: nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, (String) properties.get(ContentModel.PROP_NAME));

		if ((notificationFolder != null) && (notification == null)) {

			notification = nodeService
					.createNode(notificationFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, BeCPGModel.TYPE_NOTIFICATIONRULELIST)
					.getChildRef();

			nodeService.setProperties(notification, properties);

			if (mailTemplate != null) {
				NodeRef mailTemplateNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), mailTemplate);

				associationService.update(notification, QName.createQName(BeCPGModel.BECPG_URI, "nrEmail"), mailTemplateNodeRef);
			}

			associationService.update(notification, QName.createQName(BeCPGModel.BECPG_URI, "nrNotificationAuthorities"), notificationAuthorities);

			associationService.update(notification, QName.createQName(BeCPGModel.BECPG_URI, "nrTarget"), target);
		}
	}

	private void createRequirementsNotification(NodeRef notificationFolder) {
		if (nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.FORMULATION_ERRORS_NOTIFICATION) == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, RepoConsts.FORMULATION_ERRORS_NOTIFICATION);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrSubject"), "Formulation errors");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrNodeType"), "bcpg:reqCtrlList");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringTimeType"), RecurringTimeType.Day);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrDateField"), "cm:created");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeType"), DateFilterType.Before);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeNumber"), 0);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequency"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"),
					"{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}rclDataType:\\\"Formulation\\\" AND +@{http://www.bcpg.fr/model/becpg/1.0}rclReqType:\\\"Forbidden\\\"\"}");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequencyStartDate"), new Date());
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrVersionFilter"), VersionFilterType.NONE);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrForceNotification"), false);

			String mailTemplate = "/app:company_home/app:dictionary/app:email_templates/cm:formulation-errors-notification-rule-list-email.html.ftl";

			NodeRef adminGroupNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "GROUP_ALFRESCO_ADMINISTRATORS");

			NodeRef siteRoot = siteService.getSiteRoot();

			createNotification(notificationFolder, properties, mailTemplate, adminGroupNodeRef, siteRoot);
		}
	}

	private void createObsoleteDocumentsNotification(NodeRef notificationFolder) {
		if (nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.OBSOLETE_DOCUMENTS_NOTIFICATION) == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, RepoConsts.OBSOLETE_DOCUMENTS_NOTIFICATION);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrSubject"), "Obsolete documents");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrNodeType"), "cm:folder");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringTimeType"), RecurringTimeType.Day);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrDateField"), "cm:to");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeType"), DateFilterType.To);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeNumber"), 90);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequency"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringDay"), "MONDAY");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequencyStartDate"), new Date());
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrVersionFilter"), VersionFilterType.NONE);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrForceNotification"), false);

			NodeRef adminGroupNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "GROUP_ALFRESCO_ADMINISTRATORS");

			NodeRef siteRoot = siteService.getSiteRoot();

			createNotification(notificationFolder, properties, null, adminGroupNodeRef, siteRoot);
		}
	}

	private void createInProgressProjectsNotification(NodeRef notificationFolder) {
		if (nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.IN_PROGRESS_PROJECTS_NOTIFICATION) == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, RepoConsts.IN_PROGRESS_PROJECTS_NOTIFICATION);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrSubject"), "In progress projects");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrNodeType"), "pjt:project");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringTimeType"), RecurringTimeType.Day);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrDateField"), "cm:created");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeType"), DateFilterType.Before);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeNumber"), 0);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequency"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringDay"), "MONDAY");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"),
					"{\"query\":\"+@{http://www.bcpg.fr/model/project/1.0}projectState:\\\"InProgress\\\"\"}");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequencyStartDate"), new Date());
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrVersionFilter"), VersionFilterType.NONE);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrForceNotification"), false);

			String mailTemplate = "/app:company_home/app:dictionary/app:email_templates/cm:notification-rule-projects-list-email.html.ftl";

			NodeRef adminGroupNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "GROUP_ALFRESCO_ADMINISTRATORS");

			NodeRef siteRoot = siteService.getSiteRoot();

			createNotification(notificationFolder, properties, mailTemplate, adminGroupNodeRef, siteRoot);
		}
	}

	private void createValidatedProductsNotification(NodeRef notificationFolder) {
		if (nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.VALIDATED_PRODUCTS_NOTIFICATION) == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, RepoConsts.VALIDATED_PRODUCTS_NOTIFICATION);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrSubject"), "Validated products");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrNodeType"), "bcpg:product");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringTimeType"), RecurringTimeType.Day);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrDateField"), "bcpg:stateActivityModified");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeType"), DateFilterType.From);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeNumber"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequency"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringDay"), "MONDAY");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"),
					"{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}productState:\\\"Valid\\\"\"}");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequencyStartDate"), new Date());
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrVersionFilter"), VersionFilterType.NONE);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrForceNotification"), false);

			NodeRef adminGroupNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "GROUP_ALFRESCO_ADMINISTRATORS");

			NodeRef siteRoot = siteService.getSiteRoot();

			createNotification(notificationFolder, properties, null, adminGroupNodeRef, siteRoot);
		}
	}

	private void createValidatedAndUpdatedProductsNotification(NodeRef notificationFolder) {
		if (nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS,
				RepoConsts.VALIDATED_AND_UPDATED_PRODUCTS_NOTIFICATION) == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, RepoConsts.VALIDATED_AND_UPDATED_PRODUCTS_NOTIFICATION);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrSubject"), "Validated and updated products");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrNodeType"), "bcpg:product");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringTimeType"), RecurringTimeType.Day);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrDateField"), "cm:modified");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeType"), DateFilterType.From);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeNumber"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequency"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringDay"), "MONDAY");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"),
					"{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}productState:\\\"Valid\\\"\"}");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequencyStartDate"), new Date());
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrVersionFilter"), VersionFilterType.MAJOR);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrForceNotification"), false);

			NodeRef adminGroupNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "GROUP_ALFRESCO_ADMINISTRATORS");

			NodeRef siteRoot = siteService.getSiteRoot();

			createNotification(notificationFolder, properties, null, adminGroupNodeRef, siteRoot);
		}
	}

	private void createArchivedProductsNotification(NodeRef notificationFolder) {
		if (nodeService.getChildByName(notificationFolder, ContentModel.ASSOC_CONTAINS, RepoConsts.ARCHIVED_PRODUCTS_NOTIFICATION) == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, RepoConsts.ARCHIVED_PRODUCTS_NOTIFICATION);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrSubject"), "Archived products");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrNodeType"), "bcpg:product");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringTimeType"), RecurringTimeType.Day);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrDateField"), "bcpg:stateActivityModified");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeType"), DateFilterType.From);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrTimeNumber"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequency"), 7);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrRecurringDay"), "MONDAY");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrConditions"),
					"{\"query\":\"+@{http://www.bcpg.fr/model/becpg/1.0}productState:\\\"Archived\\\"\"}");
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrFrequencyStartDate"), new Date());
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrVersionFilter"), VersionFilterType.NONE);
			properties.put(QName.createQName(BeCPGModel.BECPG_URI, "nrForceNotification"), false);

			NodeRef adminGroupNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "GROUP_ALFRESCO_ADMINISTRATORS");

			NodeRef siteRoot = siteService.getSiteRoot();

			createNotification(notificationFolder, properties, null, adminGroupNodeRef, siteRoot);
		}
	}

	/**
	 * {@inheritDoc}
	 *
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

			// fix #6103 in case webdav we should check file size
			ActionCondition conditionOnSize = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN.toString());
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, ContentPropertyName.SIZE.toString());
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 5);
			conditionOnSize.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnSize);

			// rule
			Rule rule = new Rule();
			rule.setRuleTypes(Arrays.asList(RuleType.INBOUND));
			rule.setAction(compositeAction);
			rule.applyToChildren(true);
			rule.setTitle("import csv file");
			rule.setExecuteAsynchronously(true);
			rule.setDescription("Every csv item created will be imported");

			ruleService.saveRule(nodeRef, rule);

			action = actionService.createAction(ImporterActionExecuter.NAME, null);
			compositeAction = actionService.createCompositeAction();
			compositeAction.addAction(action);

			// compare-mime-type == text/csv
			conditionOnMimeType = actionService.createActionCondition(CompareMimeTypeEvaluator.NAME);
			conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET);
			conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
			conditionOnMimeType.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnMimeType);

			// compare-name == *.csv
			conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, ImporterActionExecuter.XLSX_EXTENSION);
			conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
			conditionOnName.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnName);

			// fix #6103 in case webdav we should check file size
			conditionOnSize = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN.toString());
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, ContentPropertyName.SIZE.toString());
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
			conditionOnSize.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 5);
			conditionOnSize.setInvertCondition(false);
			compositeAction.addActionCondition(conditionOnSize);

			// rule
			rule = new Rule();
			rule.setRuleTypes(Arrays.asList(RuleType.INBOUND));
			rule.setAction(compositeAction);
			rule.applyToChildren(true);
			rule.setTitle("import xlsx file");
			rule.setExecuteAsynchronously(true);
			rule.setDescription("Every xlsx item created will be imported");

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
			rule.setRuleTypes(Arrays.asList(RuleType.INBOUND));
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
	 * {@inheritDoc}
	 *
	 * Initialize the permissions of the repository
	 */
	@Override
	protected void visitPermissions(NodeRef nodeRef, String folderName) {

		if (Objects.equals(folderName, PlmRepoConsts.PATH_EXCHANGE)) {
			permissionService.setInheritParentPermissions(nodeRef, false);
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + SystemGroup.SystemMgr.toString(), PermissionService.COORDINATOR,
					true);
		}

		else if (Objects.equals(folderName, PlmRepoConsts.PATH_QUALITY)) {
			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + PLMGroup.QualityMgr.toString(), PermissionService.COORDINATOR,
					true);
		}

		else if (Objects.equals(folderName, PlmRepoConsts.PATH_NC)) {

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimStart, PermissionService.COORDINATOR, true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimAnalysis.toString(), PermissionService.COORDINATOR,
					true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimClassification.toString(),
					PermissionService.COORDINATOR, true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimTreatment.toString(),
					PermissionService.COORDINATOR, true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimResponse.toString(), PermissionService.COORDINATOR,
					true);

			permissionService.setPermission(nodeRef, PermissionService.GROUP_PREFIX + NCGroup.ClaimClosing.toString(), PermissionService.COORDINATOR,
					true);
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
		dataLists.add(PLMModel.TYPE_CERTIFICATION);
		dataLists.add(PLMModel.TYPE_PLANT);
		subFolders.add(RepoConsts.PATH_SUPPLIER_DOCUMENTS);
		NodeRef entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, PLMModel.TYPE_SUPPLIER, null, true, true, dataLists,
				subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);
		entityTplService.createActivityList(entityTplNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

		subFolders.remove(RepoConsts.PATH_SUPPLIER_DOCUMENTS);
		// visit client
		dataLists = new LinkedHashSet<>();
		dataLists.add(PLMModel.TYPE_CONTACTLIST);
		entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, PLMModel.TYPE_CLIENT, null, true, true, dataLists, subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);
		entityTplService.createActivityList(entityTplNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

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
		entityLists.put(PlmRepoConsts.PATH_LCA, PLMModel.TYPE_LCA);
		entityLists.put(PlmRepoConsts.PATH_PHYSICO_CHEM, PLMModel.TYPE_PHYSICO_CHEM);
		entityLists.put(PlmRepoConsts.PATH_MICROBIOS, PLMModel.TYPE_MICROBIO);
		entityLists.put(PlmRepoConsts.PATH_GEO_ORIGINS, PLMModel.TYPE_GEO_ORIGIN);
		entityLists.put(PlmRepoConsts.PATH_BIO_ORIGINS, PLMModel.TYPE_BIO_ORIGIN);
		entityLists.put(PlmRepoConsts.PATH_SUBSIDIARIES, PLMModel.TYPE_SUBSIDIARY);
		entityLists.put(PlmRepoConsts.PATH_TRADEMARKS, PLMModel.TYPE_TRADEMARK);
		entityLists.put(PlmRepoConsts.PATH_PLANTS, PLMModel.TYPE_PLANT);
		entityLists.put(PlmRepoConsts.PATH_CUSTOMSCODES, PLMModel.TYPE_CUSTOMSCODE);
		entityLists.put(PlmRepoConsts.PATH_CERTIFICATIONS, PLMModel.TYPE_CERTIFICATION);
		entityLists.put(PlmRepoConsts.PATH_LABELCLAIMS, PLMModel.TYPE_LABEL_CLAIM);
		entityLists.put(PlmRepoConsts.PATH_PROCESSSTEPS, MPMModel.TYPE_PROCESSSTEP);
		entityLists.put(PlmRepoConsts.PATH_RESOURCEPARAMS, MPMModel.TYPE_RESOURCEPARAM);
		entityLists.put(PlmRepoConsts.PATH_STORAGE_CONDITIONS, PLMModel.TYPE_STORAGE_CONDITIONS);
		entityLists.put(PlmRepoConsts.PATH_PRECAUTION_OF_USE, PLMModel.TYPE_PRECAUTION_OF_USE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_TEMPLATES, PackModel.TYPE_LABELING_TEMPLATE);
		entityLists.put(PlmRepoConsts.PATH_LABEL, PackModel.TYPE_LABEL);
		entityLists.put(PlmRepoConsts.PATH_GS1_TARGET_MARKETS, GS1Model.TYPE_TARGET_MARKET);
		entityLists.put(PlmRepoConsts.PATH_GS1_DUTY_FEE_TAXES, GS1Model.TYPE_DUTY_FEE_TAX);
		entityLists.put(PlmRepoConsts.PATH_GS1_ALCOHOL_BEVERAGE_CONTAINERS, GS1Model.TYPE_ALCOHOL_BEVERAGE_CONTAINER);
		entityLists.put(PlmRepoConsts.PATH_CONTACTS, PLMModel.TYPE_CONTACTLIST);
		entityLists.put(PlmRepoConsts.PATH_REGULATORY_USAGES, PLMModel.TYPE_REGULATORY_USAGE);

		entityLists.put(RepoConsts.PATH_NOTIFICATIONS, BeCPGModel.TYPE_NOTIFICATIONRULELIST);
		entityLists.put(PlmRepoConsts.PATH_PUBCHANNELS, PublicationModel.TYPE_PUBLICATION_CHANNEL);

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
		entityLists.put(HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_PRODUCTCOLLECTION), BeCPGModel.TYPE_LINKED_VALUE);
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
		entityLists.put(PlmRepoConsts.PATH_TRADEMARK_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_GROUPS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_NUT_FACTS_METHODS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LCA_LIST_METHODS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_POSITIONS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABELING_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABEL_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_LABELCLAIMS_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_MICROBIO_CONTROL_STEPS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_MICROBIO_UNITS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_MICROBIO_TYPES, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_RESOURCE_PARAM_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_PHYSICO_UNITS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_PHYSICO_TYPES, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_SVHC_REASONS_FOR_INCLUSIONS, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_PM_MATERIALS, PackModel.TYPE_PACKAGING_MATERIAL);
		entityLists.put(PlmRepoConsts.PATH_PM_PRINT_TYPES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_PM_PRINT_VANISHS, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_MEAT_TYPES, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_LCA_UNITS, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_GS1_PACKAGING_TYPE_CODES, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_GS1_PACKAGING_RECYCLING_SCHEME_CODES, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_GS1_PALLET_TYPE_CODES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_PLATFORM_TERM_AND_CONDITIONS_CODES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_SORTING_BONUS_CRITERIA, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_SORTING_MALUS_CRITERIA, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_GS1_HANDLING_INSTRUCTIONS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_PREPARATION_TYPE, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_GS1_DATA_CARRIER_TYPE_CODES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_TRADE_ITEM_UNIT_DESCRIPTOR_CODES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_PACKAGING_TERMS_AND_CONDITIONS_CODES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_TRADE_ITEM_TRADE_CHANNELS, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_SELLING_UNITS, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_GS1_SPECIES_FOR_FISHERY, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_CATCH_METHID_CODES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_GS1_CATCH_AREA_CODES, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(PlmRepoConsts.PATH_CONTACT_TYPES, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(RepoConsts.PATH_REPORT_PARAMS, BeCPGModel.TYPE_LIST_VALUE);

		entityLists.put(RepoConsts.PATH_REPORT_KINDLIST, BeCPGModel.TYPE_LIST_VALUE);

		return entitySystemService.createSystemEntity(parentNodeRef, path, entityLists);
	}

	private NodeRef visitSystemSecurityListValuesEntity(NodeRef parentNodeRef, String path) {
		Map<String, QName> entityLists = new LinkedHashMap<>();
		entityLists.put(PlmRepoConsts.PATH_PERSONAL_PROTECTIONS, GHSModel.TYPE_PERSONAL_PROTECTION);
		entityLists.put(PlmRepoConsts.PATH_PICTOGRAMS, GHSModel.TYPE_PICTOGRAM);
		entityLists.put(PlmRepoConsts.PATH_HAZARD_STATEMENTS, GHSModel.TYPE_HAZARD_STATEMENT);
		entityLists.put(PlmRepoConsts.PATH_PRECAUTIONARY_STATEMENTS, GHSModel.TYPE_PRECAUTIONARY_STATEMENT);
		entityLists.put(PlmRepoConsts.PATH_HAZARD_CATEGORIES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_ONU_CODES, GHSModel.TYPE_ONU_CODE);
		entityLists.put(PlmRepoConsts.PATH_CLASS_CODES, GHSModel.TYPE_CLASS_CODE);
		entityLists.put(PlmRepoConsts.PATH_PACKAGING_GROUP_CODES, GHSModel.TYPE_PACKAGING_GROUP_CODE);
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
		entityLists.put(PlmRepoConsts.PATH_CONTROL_TEMPERATURES, BeCPGModel.TYPE_LIST_VALUE);
		entityLists.put(PlmRepoConsts.PATH_CONTROL_TYPES, BeCPGModel.TYPE_LIST_VALUE);

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
		productTypes.add(PLMModel.TYPE_PRODUCTCOLLECTION);

		Set<String> subFolders = new HashSet<>();
		subFolders.add(RepoConsts.PATH_IMAGES);
		subFolders.add(RepoConsts.PATH_BRIEF);
		subFolders.add(RepoConsts.PATH_DOCUMENTS);

		for (QName productType : productTypes) {

			subFolders.remove(RepoConsts.PATH_SUPPLIER_DOCUMENTS);

			// datalists
			Set<QName> dataLists = new LinkedHashSet<>();
			QName wusedQName = null;

			if (productType.equals(PLMModel.TYPE_RAWMATERIAL)) {

				dataLists.add(PLMModel.TYPE_ALLERGENLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_LCALIST);
				dataLists.add(PLMModel.TYPE_NUTLIST);
				dataLists.add(PLMModel.TYPE_INGLIST);
				dataLists.add(PLMModel.TYPE_ORGANOLIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
				dataLists.add(PLMModel.TYPE_SVHCLIST);

				wusedQName = PLMModel.TYPE_COMPOLIST;

				subFolders.add(RepoConsts.PATH_SUPPLIER_DOCUMENTS);

			} else if (productType.equals(PLMModel.TYPE_PACKAGINGMATERIAL)) {

				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_LCALIST);
				dataLists.add(PLMModel.TYPE_PRICELIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
				dataLists.add(PLMModel.TYPE_SVHCLIST);
				dataLists.add(PackModel.TYPE_LABELING_LIST);
				dataLists.add(PackModel.PACK_MATERIAL_LIST_TYPE);

				wusedQName = PLMModel.TYPE_PACKAGINGLIST;

				subFolders.add(RepoConsts.PATH_SUPPLIER_DOCUMENTS);

			} else if (productType.equals(PLMModel.TYPE_RESOURCEPRODUCT)) {

				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_LCALIST);
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
				dataLists.add(PLMModel.TYPE_LCALIST);
				dataLists.add(PLMModel.TYPE_NUTLIST);
				dataLists.add(PLMModel.TYPE_INGLIST);
				dataLists.add(PLMModel.TYPE_ORGANOLIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
				dataLists.add(PLMModel.TYPE_SVHCLIST);
				dataLists.add(PackModel.PACK_MATERIAL_LIST_TYPE);

				wusedQName = PLMModel.TYPE_COMPOLIST;

			} else if (productType.equals(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {

				wusedQName = PLMModel.TYPE_COMPOLIST;

			} else if (productType.equals(PLMModel.TYPE_FINISHEDPRODUCT)) {

				dataLists.add(PLMModel.TYPE_COMPOLIST);
				dataLists.add(PLMModel.TYPE_PACKAGINGLIST);
				dataLists.add(MPMModel.TYPE_PROCESSLIST);
				dataLists.add(PLMModel.TYPE_ALLERGENLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_LCALIST);
				dataLists.add(PLMModel.TYPE_NUTLIST);
				dataLists.add(PLMModel.TYPE_INGLIST);
				dataLists.add(PLMModel.TYPE_INGLABELINGLIST);
				dataLists.add(PLMModel.TYPE_ORGANOLIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
				dataLists.add(PLMModel.TYPE_SVHCLIST);
				dataLists.add(PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
				dataLists.add(PackModel.PACK_MATERIAL_LIST_TYPE);

				wusedQName = PLMModel.TYPE_COMPOLIST;

			} else if (productType.equals(PLMModel.TYPE_PACKAGINGKIT)) {

				dataLists.add(PLMModel.TYPE_PACKAGINGLIST);
				dataLists.add(PLMModel.TYPE_COSTLIST);
				dataLists.add(PLMModel.TYPE_LCALIST);
				dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
				dataLists.add(PLMModel.TYPE_SVHCLIST);
				dataLists.add(PackModel.PACK_MATERIAL_LIST_TYPE);

				wusedQName = PLMModel.TYPE_PACKAGINGLIST;

			} else if (productType.equals(SecurityModel.TYPE_ACL_GROUP)) {
				dataLists.add(SecurityModel.TYPE_ACL_ENTRY);
			} else if (productType.equals(PLMModel.TYPE_PRODUCTCOLLECTION)) {
				dataLists.add(PLMModel.TYPE_PRODUCTLIST);
				dataLists.add(GS1Model.TYPE_COLLECTION_PRICE_LIST);
			}

			NodeRef entityTplNodeRef = entityTplService.createEntityTpl(productTplsNodeRef, productType, null, true, true, dataLists, subFolders);

			entityTplService.createActivityList(entityTplNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			if (productType.equals(PLMModel.TYPE_PACKAGINGKIT) && !nodeService.hasAspect(entityTplNodeRef, PackModel.ASPECT_PALLET)) {
				nodeService.addAspect(entityTplNodeRef, PackModel.ASPECT_PALLET, new HashMap<>());
			}

			if (wusedQName != null) {
				entityTplService.createWUsedList(entityTplNodeRef, wusedQName, null);
			}
			entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

			if (!productType.equals(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {
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
		dataLists.add(PLMModel.TYPE_LABELINGRULELIST);
		dataLists.add(PLMModel.TYPE_DYNAMICCHARACTLIST);
		dataLists.add(PLMModel.TYPE_NUTLIST);
		dataLists.add(PLMModel.TYPE_ALLERGENLIST);
		dataLists.add(PLMModel.TYPE_PHYSICOCHEMLIST);
		dataLists.add(PLMModel.TYPE_LABELCLAIMLIST);
		dataLists.add(PLMModel.TYPE_SPEC_COMPATIBILTY_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, PLMModel.TYPE_PRODUCT_SPECIFICATION, null, true, true, dataLists,
				null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

		// visit controlPlan
		Set<String> subFolders = new HashSet<>();
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_SAMPLINGDEF_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_CONTROL_PLAN, null, true, true, dataLists,
				subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit qualityControl
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_SAMPLING_LIST);
		dataLists.add(QualityModel.TYPE_CONTROL_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_QUALITY_CONTROL, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_REPORTS);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

		//visit batch
		dataLists.clear();
		dataLists.add(PLMModel.TYPE_COMPOLIST);
		dataLists.add(QualityModel.TYPE_BATCH_ALLOCATION_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_BATCH, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_REPORTS);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

		// visit controlPoint
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_CONTROLDEF_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_CONTROL_POINT, null, true, true, dataLists, null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit workItemAnalysis
		dataLists.clear();
		dataLists.add(QualityModel.TYPE_CONTROL_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_WORK_ITEM_ANALYSIS, null, true, true, dataLists,
				null);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		// visit NC
		subFolders = new HashSet<>();
		dataLists.clear();
		dataLists.add(BeCPGModel.TYPE_ACTIVITY_LIST);
		entityTplNodeRef = entityTplService.createEntityTpl(qualityTplsNodeRef, QualityModel.TYPE_NC, null, true, true, dataLists, subFolders);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);
	}

	private void visitReportKindList(Map<String, Map<QName, Serializable>> reportKindListDefaultValues) {
		NodeRef systemFolderNodeRef = repoService.getFolderByPath(RepoConsts.PATH_SYSTEM);
		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_LISTS);
		NodeRef reportKindListFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_REPORT_KINDLIST);
		reportKindListDefaultValues.forEach((key, val) -> {

			NodeRef nodeRef = nodeService.getChildByName(reportKindListFolder, ContentModel.ASSOC_CONTAINS, (String) val.get(ContentModel.PROP_NAME));
			if (nodeRef == null) {

				mlNodeService.createNode(reportKindListFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, BeCPGModel.TYPE_LIST_VALUE,
						val);
			}
		});

	}

	/**
	 * Create the reports templates
	 *
	 * @param productReportTplsNodeRef
	 */
	private void visitReports(NodeRef systemNodeRef) {
		try {
			// reports folder
			NodeRef reportsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_REPORTS);

			// product report templates
			NodeRef productReportTplsNodeRef = visitFolder(reportsNodeRef, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES);
			String productReportClientName = I18NUtil.getMessage(PRODUCT_REPORT_CLIENT_NAME, Locale.getDefault());
			String productReportTechnicalName = I18NUtil.getMessage(PRODUCT_REPORT_TECHNICAL_SHEET_NAME, Locale.getDefault());
			String productReportProductionName = I18NUtil.getMessage(PRODUCT_REPORT_PRODUCTION_NAME, Locale.getDefault());
			String productReportPackagingName = I18NUtil.getMessage(PRODUCT_REPORT_TECHNICAL_SHEET_NAME, Locale.getDefault());
			String productReportCostName = I18NUtil.getMessage(PRODUCT_REPORT_COST_NAME, Locale.getDefault());
			String productReportRDName = I18NUtil.getMessage(PRODUCT_REPORT_RD_NAME, Locale.getDefault());
			String qualityControlAgingName = I18NUtil.getMessage(QUALITY_CONTROL_AGING_NAME, Locale.getDefault());
			String productReportSupplierName = I18NUtil.getMessage(PRODUCT_REPORT_SUPPLIER_NAME, Locale.getDefault());

			List<NodeRef> commonResources = new ArrayList<>();

			QName[] productTypes = { PLMModel.TYPE_FINISHEDPRODUCT, PLMModel.TYPE_RAWMATERIAL, PLMModel.TYPE_SEMIFINISHEDPRODUCT,
					PLMModel.TYPE_PACKAGINGMATERIAL, PLMModel.TYPE_SUPPLIER };
			String[] defaultReport = { PRODUCT_REPORT_CLIENT_PATH, PRODUCT_REPORT_RAWMATERIAL_PATH, PRODUCT_REPORT_PRODUCTION_PATH,
					PRODUCT_REPORT_PACKAGING_PATH, PRODUCT_REPORT_SUPPLIER_PATH };
			String[] defaultReportName = { productReportClientName, productReportTechnicalName, productReportProductionName,
					productReportPackagingName, productReportSupplierName };

			String[][] otherReport = { { PRODUCT_REPORT_PRODUCTION_PATH, PRODUCT_REPORT_COST_PATH, PRODUCT_REPORT_RD_PATH }, null, null, null, null };
			String[][] otherReportName = { { productReportProductionName, productReportCostName, productReportRDName }, null, null, null, null };

			String[] productReportResource = { PRODUCT_REPORT_DE_RESOURCE, PRODUCT_REPORT_EN_US_RESOURCE, PRODUCT_REPORT_EN_RESOURCE,
					PRODUCT_REPORT_ES_RESOURCE, PRODUCT_REPORT_FI_RESOURCE, PRODUCT_REPORT_FR_RESOURCE, PRODUCT_REPORT_IT_RESOURCE,
					PRODUCT_REPORT_NL_RESOURCE, PRODUCT_REPORT_PT_RESOURCE, PRODUCT_REPORT_RU_RESOURCE, PRODUCT_REPORT_SV_RESOURCE,
					PRODUCT_REPORT_IMG_CCCCCC, PRODUCT_REPORT_IMG_TRAFFICLIGHTS_ENERGY, PRODUCT_REPORT_IMG_TRAFFICLIGHTS_GREEN,
					PRODUCT_REPORT_IMG_TRAFFICLIGHTS_ORANGE, PRODUCT_REPORT_IMG_TRAFFICLIGHTS_RED, PRODUCT_REPORT_IMG_TRAFFICLIGHTS_SERVING };

			String[] commonReportResource = { PRODUCT_REPORT_LOGO_RESOURCE, PRODUCT_REPORT_SETTINGS_RESOURCE, PRODUCT_REPORT_CSS_RESOURCE };

			Map<String, Map<QName, Serializable>> reportKindDefaultValues = new HashMap<>();
			Map<String, Map<QName, Serializable>> reportKindTplAssoc = new HashMap<>();
			List<String> defaultKindReport = new ArrayList<>(Arrays.asList(defaultReport));
			defaultKindReport.add(NONE_KIND_REPORT);

			for (String reportKind : defaultKindReport) {
				if (PRODUCT_REPORT_PACKAGING_PATH.equals(reportKind) || PRODUCT_REPORT_COST_PATH.equals(reportKind)
						|| PRODUCT_REPORT_RD_PATH.equals(reportKind)) {
					continue;
				}

				String reportKindCode = reportKindCodes.get(reportKind);

				MLText mltValue = new MLText();
				mltValue.put(Locale.FRENCH, I18NUtil.getMessage("becpg.reportkind." + reportKindCode.toLowerCase() + ".value", Locale.FRENCH));
				mltValue.put(Locale.ENGLISH, I18NUtil.getMessage("becpg.reportkind." + reportKindCode.toLowerCase() + ".value", Locale.ENGLISH));

				// for aspect on report template
				Map<QName, Serializable> reportKindTplProps = new HashMap<>();
				reportKindTplProps.put(ReportModel.PROP_REPORT_KINDS, reportKindCode);
				reportKindTplAssoc.put(reportKind, reportKindTplProps);

				//for reportKindList default values
				Map<QName, Serializable> reportKindListProps = new HashMap<>();
				reportKindListProps.put(ContentModel.PROP_NAME, reportKindCode);
				reportKindListProps.put(BeCPGModel.PROP_LV_CODE, reportKindCode);
				reportKindListProps.put(BeCPGModel.PROP_LV_VALUE, mltValue);
				reportKindDefaultValues.put(reportKind, reportKindListProps);
			}

			visitReportKindList(reportKindDefaultValues);

			List<NodeRef> resources = new ArrayList<>();
			for (String element : productReportResource) {
				resources.add(reportTplService.createTplRessource(productReportTplsNodeRef, element, false));
			}

			for (String element : commonReportResource) {
				commonResources.add(reportTplService.createTplRessource(productReportTplsNodeRef, element, false));
			}

			resources.addAll(commonResources);

			int i = 0;
			for (QName productType : productTypes) {

				ClassDefinition classDef = dictionaryService.getClass(productType);

				NodeRef folderNodeRef = repoService.getOrCreateFolderByPath(productReportTplsNodeRef, classDef.getTitle(dictionaryService),
						classDef.getTitle(dictionaryService));

				if ((defaultReport[i] != null) && (defaultReportName[i] != null)) {

					ReportTplInformation reportTplInformation = new ReportTplInformation();
					reportTplInformation.setReportType(ReportType.Document);
					reportTplInformation.setReportFormat(ReportFormat.PDF);
					reportTplInformation.setNodeType(productType);
					reportTplInformation.setDefaultTpl(true);
					reportTplInformation.setSystemTpl(true);
					reportTplInformation.setReportKindAspectProperties(reportKindTplAssoc.get(defaultReport[i]));
					reportTplInformation.setResources(resources);
					reportTplInformation.setSupportedLocale(supportedLocale);
					if (productType == PLMModel.TYPE_PACKAGINGMATERIAL) {
						reportTplInformation.setTextParameter("{ prefs: { assocsToExtract: \"pack:pmMaterialRefs\"  } }");
					}

					reportTplService.createTplRptDesign(folderNodeRef, defaultReportName[i], defaultReport[i], reportTplInformation, false);

				}

				if ((otherReport[i] != null) && (otherReportName[i] != null)) {

					for (int b = 0; b < otherReport[i].length; b++) {

						ReportTplInformation reportTplInformation = new ReportTplInformation();
						reportTplInformation.setReportType(ReportType.Document);
						reportTplInformation.setReportFormat(ReportFormat.PDF);
						reportTplInformation.setNodeType(productType);
						reportTplInformation.setDefaultTpl(false);
						reportTplInformation.setSystemTpl(true);
						reportTplInformation.setReportKindAspectProperties(reportKindTplAssoc.get(otherReport[i][b]));
						reportTplInformation.setResources(resources);
						reportTplInformation.setSupportedLocale(supportedLocale);

						reportTplService.createTplRptDesign(folderNodeRef, otherReportName[i][b], otherReport[i][b], reportTplInformation, false);

					}

				}

				i++;
			}

			// quality report templates
			NodeRef qualityReportTplsNodeRef = visitFolder(reportsNodeRef, PlmRepoConsts.PATH_QUALITY_REPORTTEMPLATES);

			supportedLocale = Arrays.asList("fr", "en", "es", "it", "nl", "sv_SE", "fi", "ru", "pt");

			// nc
			ClassDefinition classDef = dictionaryService.getClass(QualityModel.TYPE_NC);
			NodeRef qualityFolderNodeRef = repoService.getOrCreateFolderByPath(qualityReportTplsNodeRef, classDef.getTitle(dictionaryService),
					classDef.getTitle(dictionaryService));

			resources = new ArrayList<>();
			resources.addAll(commonResources);
			resources.add(reportTplService.createTplRessource(qualityFolderNodeRef, NC_REPORT_RESOURCE, false));

			for (String lang : supportedLocale) {
				resources.add(reportTplService.createTplRessource(qualityFolderNodeRef, String.format(NC_REPORT_RESOURCE_BY_LOCALE, lang), false));
			}

			ReportTplInformation reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Document);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType(QualityModel.TYPE_NC);
			reportTplInformation.setDefaultTpl(true);
			reportTplInformation.setSystemTpl(true);
			reportTplInformation.setResources(resources);
			reportTplInformation.setSupportedLocale(supportedLocale);

			reportTplService.createTplRptDesign(qualityFolderNodeRef, classDef.getTitle(dictionaryService), NC_REPORT_PATH, reportTplInformation,
					false);

			classDef = dictionaryService.getClass(QualityModel.TYPE_QUALITY_CONTROL);
			qualityFolderNodeRef = repoService.getOrCreateFolderByPath(qualityReportTplsNodeRef, classDef.getTitle(dictionaryService),
					classDef.getTitle(dictionaryService));

			resources = new ArrayList<>();
			resources.addAll(commonResources);
			resources.add(reportTplService.createTplRessource(qualityFolderNodeRef, QUALITY_REPORT_RESOURCE, false));

			for (String lang : supportedLocale) {
				resources.add(
						reportTplService.createTplRessource(qualityFolderNodeRef, String.format(QUALITY_REPORT_RESOURCE_BY_LOCALE, lang), false));
			}

			reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Document);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType(QualityModel.TYPE_QUALITY_CONTROL);
			reportTplInformation.setDefaultTpl(true);
			reportTplInformation.setSystemTpl(true);
			reportTplInformation.setResources(resources);
			reportTplInformation.setSupportedLocale(supportedLocale);

			reportTplService.createTplRptDesign(qualityFolderNodeRef, classDef.getTitle(dictionaryService), QUALITY_CONTROL_REPORT_PATH,
					reportTplInformation, false);

			reportTplInformation.setReportFormat(ReportFormat.XLSX);
			reportTplInformation.setNodeType(QualityModel.TYPE_QUALITY_CONTROL);
			reportTplInformation.setSystemTpl(false);

			reportTplService.createTplRptDesign(qualityFolderNodeRef, classDef.getTitle(dictionaryService) + " - " + qualityControlAgingName,
					QUALITY_CONTROL_AGING_REPORT_PATH, reportTplInformation, false);

			// eco report

			NodeRef ecoFolderNodeRef = visitFolder(reportsNodeRef, PlmRepoConsts.PATH_REPORTS_ECO);

			resources = new ArrayList<>();
			resources.addAll(commonResources);

			reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Document);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType(ECMModel.TYPE_ECO);
			reportTplInformation.setDefaultTpl(true);
			reportTplInformation.setSystemTpl(true);
			reportTplInformation.setResources(resources);

			reportTplService.createTplRptDesign(ecoFolderNodeRef, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_ECO), ECO_REPORT_PATH,
					reportTplInformation, false);

			/*
			 * Export Search reports
			 */
			NodeRef exportSearchNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_EXPORT_SEARCH);

			// export search products

			NodeRef exportSearchProductsNodeRef = visitFolder(exportSearchNodeRef, PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS);

			reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.ExportSearch);
			reportTplInformation.setReportFormat(ReportFormat.XLSX);
			reportTplInformation.setNodeType(PLMModel.TYPE_PRODUCT);
			reportTplInformation.setDefaultTpl(false);
			reportTplInformation.setSystemTpl(false);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS),
					TranslateHelper.getLocaleAwarePath(EXPORT_PRODUCTS_REPORT_RPTFILE_PATH), reportTplInformation, false);

			reportTplService.createTplRessource(exportSearchProductsNodeRef, TranslateHelper.getLocaleAwarePath(EXPORT_PRODUCTS_REPORT_XMLFILE_PATH),
					false);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_INGLIST),
					TranslateHelper.getLocaleAwarePath(EXPORT_INGLIST_XLSX_PATH), reportTplInformation, false);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_LABELLING),
					TranslateHelper.getLocaleAwarePath(EXPORT_LABELLING_XLSX_PATH), reportTplInformation, false);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_CITEO),
					TranslateHelper.getLocaleAwarePath(EXPORT_CITEO_XLSX_PATH), reportTplInformation, false);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_ALLERGENS),
					TranslateHelper.getLocaleAwarePath(EXPORT_ALLERGENS_XLSX_PATH), reportTplInformation, false);
			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_NUTRIENTS),
					TranslateHelper.getLocaleAwarePath(EXPORT_NUTRIENTS_XLSX_PATH), reportTplInformation, false);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_LABELCLAIMLIST),
					TranslateHelper.getLocaleAwarePath(EXPORT_LABELCLAIMLIST_XLSX_PATH), reportTplInformation, false);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PHYSICOCHEMICALLIST),
					TranslateHelper.getLocaleAwarePath(EXPORT_PHYSICOCHEMICALLIST_XLSX_PATH), reportTplInformation, false);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_COMPOSITIONPACKAGING),
					TranslateHelper.getLocaleAwarePath(EXPORT_COMPOSITIONPACKAGING_XLSX_PATH), reportTplInformation, false);

			reportTplInformation.setNodeType(PLMModel.TYPE_SUPPLIER);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_SUPPLIERS_CONTACTS),
					TranslateHelper.getLocaleAwarePath(EXPORT_SUPPLIERS_XLSX_PATH), reportTplInformation, false);

			reportTplInformation.setNodeType(QualityModel.TYPE_QUALITY_CONTROL);

			reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_QUALITY_CONTROLS),
					TranslateHelper.getLocaleAwarePath(EXPORT_QUALITY_CONTROLS_XLSX_PATH), reportTplInformation, false);

			// export search NC

			NodeRef exportNCSynthesisNodeRef = visitFolder(exportSearchNodeRef, PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES);

			reportTplInformation.setNodeType(QualityModel.TYPE_NC);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setDefaultTpl(false);

			reportTplService.createTplRptDesign(exportNCSynthesisNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_NON_CONFORMITIES),
					TranslateHelper.getLocaleAwarePath(EXPORT_NC_REPORT_RPTFILE_PATH), reportTplInformation, false);

			reportTplService.createTplRessource(exportNCSynthesisNodeRef, TranslateHelper.getLocaleAwarePath(EXPORT_NC_REPORT_XMLFILE_PATH), false);

			reportTplInformation = new ReportTplInformation();
			reportTplInformation.setReportType(ReportType.Compare);
			reportTplInformation.setReportFormat(ReportFormat.PDF);
			reportTplInformation.setNodeType(null);
			reportTplInformation.setDefaultTpl(true);
			reportTplInformation.setSystemTpl(false);

			NodeRef compareProductFolderNodeRef = visitFolder(reportsNodeRef, RepoConsts.PATH_REPORTS_COMPARE_ENTITIES);

			reportTplService.createTplRptDesign(compareProductFolderNodeRef,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_SIMPLIFIED_COMPARISON_REPORT), COMPARE_PRODUCTS_REPORT_PATH,
					reportTplInformation, false);

		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	/**
	 * Create system groups.
	 *
	 * @param locale
	 *            the locale
	 */
	private void createSystemGroups() {

		String[] groups = { PLMGroup.RDUser.toString(), PLMGroup.RDMgr.toString(), PLMGroup.QualityUser.toString(), PLMGroup.QualityMgr.toString(),
				PLMGroup.ProductionUser.toString(), PLMGroup.ProductionMgr.toString(), PLMGroup.PackagingMgr.toString(),
				PLMGroup.PackagingUser.toString(), PLMGroup.ReferencingMgr.toString(), PLMGroup.TradeUser.toString(),
				PLMGroup.ProductValidationStart.toString(), NCGroup.ClaimStart.toString(), NCGroup.ClaimAnalysis.toString(),
				NCGroup.ClaimClassification.toString(), NCGroup.ClaimTreatment.toString(), NCGroup.ClaimResponse.toString(),
				NCGroup.ClaimClosing.toString(), ECMGroup.CreateChangeOrder.toString(), ECMGroup.ApplyChangeOrder.toString() };

		createGroups(groups);

		// Group hierarchy
		Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
				PermissionService.GROUP_PREFIX + SystemGroup.SecurityRole.toString(), true);
		if (!authorities.contains(PermissionService.GROUP_PREFIX + ECMGroup.ApplyChangeOrder.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.SecurityRole.toString(),
					PermissionService.GROUP_PREFIX + ECMGroup.ApplyChangeOrder.toString());
		}
		if (!authorities.contains(PermissionService.GROUP_PREFIX + ECMGroup.CreateChangeOrder.toString())) {
			authorityService.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.SecurityRole.toString(),
					PermissionService.GROUP_PREFIX + ECMGroup.CreateChangeOrder.toString());
		}

	}

	/** {@inheritDoc} */
	@Override
	public Integer initOrder() {
		return 3;
	}
}
