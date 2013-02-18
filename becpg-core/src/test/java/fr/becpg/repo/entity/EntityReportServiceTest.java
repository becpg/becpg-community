/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ProductReportServiceTest.
 * 
 * @author querephi
 */
public class EntityReportServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityReportServiceTest.class);

	@Resource
	private ReportTplService reportTplService;
	@Resource
	private EntityService entityService;
	@Resource
	private AssociationService associationService;
	@Resource 
	private EntityReportService entityReportService;
	
	/** The sf node ref. */
	private NodeRef sfNodeRef;
	private Date createdDate;	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				deleteReportTpls();

				return null;

			}
		}, false, true);

	}

	/**
	 * Test report on product
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testProductReport() throws InterruptedException {

		logger.debug("testIsReportUpToDate()");

		// Add report tpl
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- Add report tpl --*/
				NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef reportsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
				NodeRef productReportTplFolder = repoService.createFolderByPath(reportsFolder, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

				reportTplService.createTplRptDesign(productReportTplFolder, "report SF", "beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document,
						ReportFormat.PDF, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, true, true, true);
				reportTplService.createTplRptDesign(productReportTplFolder, "report SF 2", "beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document,
						ReportFormat.PDF, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, true, false, true);

				return null;

			}
		});

		assertEquals("check system templates", 2, reportTplService.getSystemReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT).size());

		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);

				sfNodeRef = alfrescoRepository.create(testFolderNodeRef, sfData).getNodeRef();				
				createdDate = new Date();				
				entityReportService.generateReport(sfNodeRef);

				return null;
			}
		});

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				// check report Tpl
				List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT);
				assertEquals("check system templates", 2, reportTplNodeRefs.size());
				
				NodeRef defaultReportTplNodeRef = null;
				NodeRef otherReportTplNodeRef = null;
				for(NodeRef reportTplNodeRef : reportTplNodeRefs){
					if(Boolean.TRUE.equals(nodeService.getProperty(reportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT))){
						defaultReportTplNodeRef = reportTplNodeRef;
					}
					else{
						otherReportTplNodeRef = reportTplNodeRef;
					}
				}
				assertNotNull(defaultReportTplNodeRef);
				assertNotNull(otherReportTplNodeRef);						
				
				// check reports in generated, its name
				Date generatedDate = (Date)nodeService.getProperty(sfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
				createdDate.before(generatedDate);
				List<NodeRef> reportNodeRefs = associationService.getTargetAssocs(sfNodeRef, ReportModel.ASSOC_REPORTS);
				assertEquals(2, reportNodeRefs.size());
				
				checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs);

				// rename SF
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_NAME, "SF renamed");
				entityReportService.generateReport(sfNodeRef);
				
				// check reports in generated, its name
				Date generatedDate2 = (Date)nodeService.getProperty(sfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
				generatedDate.before(generatedDate2);
				List<NodeRef> reportNodeRefs2 = associationService.getTargetAssocs(sfNodeRef, ReportModel.ASSOC_REPORTS);
				assertEquals(2, reportNodeRefs2.size());
				
				checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs2);							
				return null;
			}
		});
				
		// Test datalist modified
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				SemiFinishedProductData sfData = (SemiFinishedProductData) alfrescoRepository.findOne(sfNodeRef);
				NodeRef nodeRef = sfData.getAllergenList().get(0).getNodeRef();
				
				// change nothing
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, true);				
				assertFalse(entityService.hasDataListModified(sfNodeRef));
				
				// change something
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, false);				
				assertTrue(entityService.hasDataListModified(sfNodeRef));
				return null;

			}
		});

	}
	
	private void checkReportNames(NodeRef defaultReportTplNodeRef, NodeRef otherReportTplNodeRef, List<NodeRef>reportNodeRefs){
		
		boolean isDefault = !((String)nodeService.getProperty(reportNodeRefs.get(0), ContentModel.PROP_NAME)).contains("report SF 2");
		NodeRef defaultReportNodeRef = isDefault ? reportNodeRefs.get(0) : reportNodeRefs.get(1);
		NodeRef otherReportNodeRef = isDefault ? reportNodeRefs.get(1) : reportNodeRefs.get(0);
		
		String defaultReportName = String.format("%s - %s", 
				nodeService.getProperty(sfNodeRef, ContentModel.PROP_NAME), 
				nodeService.getProperty(defaultReportTplNodeRef, ContentModel.PROP_NAME));
		
		String otherReportName = String.format("%s - %s", 
						nodeService.getProperty(sfNodeRef, ContentModel.PROP_NAME), 
						nodeService.getProperty(otherReportTplNodeRef, ContentModel.PROP_NAME));
		
		assertEquals(defaultReportName , nodeService.getProperty(defaultReportNodeRef, ContentModel.PROP_NAME));
		assertEquals(otherReportName , nodeService.getProperty(otherReportNodeRef, ContentModel.PROP_NAME));
	}

	/**
	 * Test get product system report templates.
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testGetProductSystemReportTemplates() throws InterruptedException {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef reportsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
				NodeRef productReportTplFolder = repoService.createFolderByPath(reportsFolder, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);

				sfNodeRef = alfrescoRepository.create(testFolderNodeRef, sfData).getNodeRef();

				QName typeQName = nodeService.getType(sfNodeRef);
				assertEquals("check system templates", 0, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());

				// add a system template
				reportTplService.createTplRptDesign(productReportTplFolder, "report MP", "beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document,
						ReportFormat.PDF, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, true, true, true);

				assertEquals("check system templates", 1, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());

				// add a system template
				reportTplService.createTplRptDesign(productReportTplFolder, "report MP 2", "beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document,
						ReportFormat.PDF, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, true, false, true);
				assertEquals("check system templates", 2, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());

				assertEquals("check user templates", 0, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user").size());

				// add a user template
				reportTplService.createTplRptDesign(productReportTplFolder, "user tpl", "beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document,
						ReportFormat.PDF, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, false, true, true);

				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user").size());

				// add a user template
				NodeRef userTpl2NodeRef = reportTplService.createTplRptDesign(productReportTplFolder, "user tpl 2", "beCPG/birt/document/product/default/ProductReport.rptdesign",
						ReportType.Document, ReportFormat.PDF, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, false, false, true);

				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "u*").size());
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user*").size());
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user tpl 2").size());
				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "\"user tpl 2\"")
						.size());
				assertEquals("check user templates", userTpl2NodeRef,
						reportTplService.getUserReportTemplate(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user tpl 2"));

				return null;

			}
		}, false, true);

	}
}
