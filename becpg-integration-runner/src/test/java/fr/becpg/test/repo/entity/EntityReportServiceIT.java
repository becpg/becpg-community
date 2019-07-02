/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductReportServiceTest.
 *
 * @author querephi
 */
public class EntityReportServiceIT extends PLMBaseTestCase {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(EntityReportServiceIT.class);

	@Resource
	private ReportTplService reportTplService;
	@Resource
	private EntityService entityService;
	@Resource
	private AssociationService associationService;
	@Resource
	private EntityReportService entityReportService;

	/** The PF node ref. */
	private NodeRef pfNodeRef;
	private Date createdDate;

	NodeRef defaultReportTplNodeRef = null;
	NodeRef otherReportTplNodeRef = null;
	NodeRef productReportTplFolder = null;

	private void initReports() {

		// Add report tpl
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			for (NodeRef n : reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "*")) {
				nodeService.deleteNode(n);
			}

			/*-- Add report tpl --*/
			NodeRef systemFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
			NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
			productReportTplFolder = repoService.getOrCreateFolderByPath(reportsFolder, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

			reportTplService.createTplRptDesign(productReportTplFolder, "report PF 2", "beCPG/birt/document/product/default/ProductReport.rptdesign",
					ReportType.Document, ReportFormat.PDF, PLMModel.TYPE_FINISHEDPRODUCT, true, false, true);

			return null;

		}, false, true);
	}

	/**
	 * Test report on product
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testProductReport() {

		logger.debug("testIsReportUpToDate()");

		initReports();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			List<NodeRef> ret = reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT);
			for (NodeRef ref : ret) {
				logger.info(nodeService.getProperty(ref, ContentModel.PROP_NAME));
			}
			assertEquals("check system templates", 5,
					reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT).size());
			return null;
		}, false, true);
		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// create PF
			FinishedProductData pfData = new FinishedProductData();
			pfData.setName("PF");
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, null, true, true, null, null, allergens.get(0), false));
			allergenList.add(new AllergenListDataItem(null, null, false, true, null, null, allergens.get(1), false));
			allergenList.add(new AllergenListDataItem(null, null, true, false, null, null, allergens.get(2), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergens.get(3), false));
			pfData.setAllergenList(allergenList);

			pfNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), pfData).getNodeRef();

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			createdDate = new Date();
			entityReportService.generateReports(pfNodeRef);

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// check report Tpl
			List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT);
			assertEquals("check system templates", 5, reportTplNodeRefs.size());

			for (NodeRef reportTplNodeRef : reportTplNodeRefs) {
				String name = (String) nodeService.getProperty(reportTplNodeRef, ContentModel.PROP_NAME);
				logger.debug("Report name: " + name);
				if (Boolean.TRUE.equals(nodeService.getProperty(reportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT))) {
					defaultReportTplNodeRef = reportTplNodeRef;
				} else if (name.contains("report PF 2")) {
					otherReportTplNodeRef = reportTplNodeRef;
				}
			}
			assertNotNull(defaultReportTplNodeRef);
			assertNotNull(otherReportTplNodeRef);

			// check reports in generated, its name
			Date generatedDate = (Date) nodeService.getProperty(pfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
			createdDate.before(generatedDate);
			List<NodeRef> reportNodeRefs = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(5, reportNodeRefs.size());

			checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs);

			// rename PF
			nodeService.setProperty(pfNodeRef, ContentModel.PROP_NAME, "PF renamed");
			entityReportService.generateReports(pfNodeRef);

			// check reports in generated, its name
			Date generatedDate2 = (Date) nodeService.getProperty(pfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
			generatedDate.before(generatedDate2);
			List<NodeRef> reportNodeRefs2 = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(5, reportNodeRefs2.size());

			checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs2);
			return null;
		}, false, true);

		// Test datalist modified
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData pfData = (FinishedProductData) alfrescoRepository.findOne(pfNodeRef);
			NodeRef nodeRef = pfData.getAllergenList().get(0).getNodeRef();

			// change nothing
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, true);

			return null;
		}, false, true);

		assertFalse(entityReportService.shouldGenerateReport(pfNodeRef, null));

		// Test datalist modified
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData pfData = (FinishedProductData) alfrescoRepository.findOne(pfNodeRef);
			NodeRef nodeRef = pfData.getAllergenList().get(0).getNodeRef();
			// change something
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			assertTrue(entityReportService.shouldGenerateReport(pfNodeRef, null));
			return null;

		}, false, true);
		// Delete report tpl -> report should be deleted
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("Delete report Tpl");
			nodeService.setProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			entityReportService.generateReports(pfNodeRef);

			// check report Tpl
			List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT);
			assertEquals("check system templates", 4, reportTplNodeRefs.size());

			// check other report is deleted
			List<NodeRef> reportNodeRefs = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
			assertEquals(4, reportNodeRefs.size());

			boolean hasReportPF2Name = false;
			for (NodeRef n : reportNodeRefs) {
				String reportName = (String) nodeService.getProperty(n, ContentModel.PROP_NAME);
				if (reportName.contains("report PF 2")) {
					hasReportPF2Name = true;
				}
			}
			assertFalse(hasReportPF2Name);

			nodeService.setProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, false);

			return null;

		}, false, true);

	}

	private void checkReportNames(NodeRef defaultReportTplNodeRef, NodeRef otherReportTplNodeRef, List<NodeRef> reportNodeRefs) {

		String defaultReportName = String.format("%s - %s", nodeService.getProperty(pfNodeRef, ContentModel.PROP_NAME),
				nodeService.getProperty(defaultReportTplNodeRef, ContentModel.PROP_NAME)).replace(RepoConsts.REPORT_EXTENSION_BIRT,
						((String) nodeService.getProperty(defaultReportTplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT)).toLowerCase());

		String otherReportName = String.format("%s - %s", nodeService.getProperty(pfNodeRef, ContentModel.PROP_NAME),
				nodeService.getProperty(otherReportTplNodeRef, ContentModel.PROP_NAME)).replace(RepoConsts.REPORT_EXTENSION_BIRT,
						((String) nodeService.getProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT)).toLowerCase());

		int checks = 0;
		for (NodeRef reportNodeRef : reportNodeRefs) {
			String reportName = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);
			logger.debug("Test report name:" + reportName + " compare with :" + defaultReportName);

			if (reportName.equals(defaultReportName)) {
				checks++;
			} else if (reportName.equals(otherReportName)) {
				checks++;
			}
		}
		assertEquals(2, checks);
	}

	/**
	 * Test get product system report templates.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testGetProductSystemReportTemplates() {

		Date startTime = new Date();
		initReports();
		
		String userReportTpl = "user tpl "+(new Date().getTime());
		String userReportTpl2 = "user tpl 2"+(new Date().getTime());
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			for(NodeRef tmpRef : reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "user*")) {
				nodeService.setProperty(tmpRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
			}
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef systemFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
			NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS,
					TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
			productReportTplFolder = repoService.getOrCreateFolderByPath(reportsFolder, PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

			// create PF
			FinishedProductData pfData = new FinishedProductData();
			pfData.setName("PF");
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, null, true, true, null, null, allergens.get(0), false));
			allergenList.add(new AllergenListDataItem(null, null, false, true, null, null, allergens.get(1), false));
			allergenList.add(new AllergenListDataItem(null, null, true, false, null, null, allergens.get(2), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergens.get(3), false));
			pfData.setAllergenList(allergenList);

			pfNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), pfData).getNodeRef();

			QName typeQName = nodeService.getType(pfNodeRef);
			assertEquals("check system templates", 5, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());

			
			// add a user template
			reportTplService.createTplRptDesign(productReportTplFolder, userReportTpl, "beCPG/birt/document/product/default/ProductReport.rptdesign",
					ReportType.Document, ReportFormat.PDF, PLMModel.TYPE_FINISHEDPRODUCT, false, true, true);

			return null;

		}, false, true);

		waitForSolr(startTime);

		startTime = new Date();

		final NodeRef userTpl2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertEquals("check user templates", 1,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, userReportTpl).size());

			// add a user template
			return reportTplService.createTplRptDesign(productReportTplFolder, userReportTpl2,
					"beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document, ReportFormat.PDF,
					PLMModel.TYPE_FINISHEDPRODUCT, false, false, true);

		}, false, true);

		waitForSolr(startTime);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertEquals("check user templates",2,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "u*").size());
			assertEquals("check user templates",2,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "user*").size());
			assertEquals("check user templates",2,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "user tpl 2").size());
			
			assertEquals("check user templates", 1,
					reportTplService.getUserReportTemplates(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, "\""+userReportTpl2+"\"").size());
			assertEquals("check user templates", userTpl2NodeRef,
					reportTplService.getUserReportTemplate(ReportType.Document, PLMModel.TYPE_FINISHEDPRODUCT, userReportTpl2));

			return null;

		}, false, true);
		
		

	}
}
