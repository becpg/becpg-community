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
public class EntityReportServiceTest extends PLMBaseTestCase {

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
	
	/** The PF node ref. */
	private NodeRef pfNodeRef;
	private Date createdDate;	
	
	NodeRef defaultReportTplNodeRef = null;
	NodeRef otherReportTplNodeRef = null;
	
	private void initReports(){
		
		// Add report tpl
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				for(NodeRef n : reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "*")){
					nodeService.deleteNode(n);
				}

				/*-- Add report tpl --*/
				NodeRef systemFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
				NodeRef productReportTplFolder = repoService.getOrCreateFolderByPath(reportsFolder, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

				reportTplService.createTplRptDesign(productReportTplFolder, "report PF 2", "beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document,
						ReportFormat.PDF, BeCPGModel.TYPE_FINISHEDPRODUCT, true, false, true);
				
				return null;

			}
		});			
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
		
		initReports();

		assertEquals("check system templates", 3, reportTplService.getSystemReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT).size());

		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create PF
				FinishedProductData pfData = new FinishedProductData();
				pfData.setName("PF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3), false));
				pfData.setAllergenList(allergenList);

				pfNodeRef = alfrescoRepository.create(testFolderNodeRef, pfData).getNodeRef();				
				

				return null;
			}
		});
		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				createdDate = new Date();				
				entityReportService.generateReport(pfNodeRef);

				return null;
			}
		});

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				// check report Tpl
				List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT);
				assertEquals("check system templates", 3, reportTplNodeRefs.size());
				
				
				for(NodeRef reportTplNodeRef : reportTplNodeRefs){
					String name = (String)nodeService.getProperty(reportTplNodeRef, ContentModel.PROP_NAME);
					if(Boolean.TRUE.equals(nodeService.getProperty(reportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT))){
						defaultReportTplNodeRef = reportTplNodeRef;
					}
					else if(name.contains("report PF 2")){
						otherReportTplNodeRef = reportTplNodeRef;
					}
				}
				assertNotNull(defaultReportTplNodeRef);
				assertNotNull(otherReportTplNodeRef);						
				
				// check reports in generated, its name
				Date generatedDate = (Date)nodeService.getProperty(pfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
				createdDate.before(generatedDate);
				List<NodeRef> reportNodeRefs = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
				assertEquals(3, reportNodeRefs.size());
				
				checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs);

				// rename PF
				nodeService.setProperty(pfNodeRef, ContentModel.PROP_NAME, "PF renamed");
				entityReportService.generateReport(pfNodeRef);
				
				// check reports in generated, its name
				Date generatedDate2 = (Date)nodeService.getProperty(pfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
				generatedDate.before(generatedDate2);
				List<NodeRef> reportNodeRefs2 = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
				assertEquals(3, reportNodeRefs2.size());
				
				checkReportNames(defaultReportTplNodeRef, otherReportTplNodeRef, reportNodeRefs2);							
				return null;
			}
		});
				
		// Test datalist modified
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				FinishedProductData pfData = (FinishedProductData) alfrescoRepository.findOne(pfNodeRef);
				NodeRef nodeRef = pfData.getAllergenList().get(0).getNodeRef();
				
				// change nothing
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, true);				
				
				
				return null;
			}
		});
		
		assertFalse(entityReportService.shouldGenerateReport(pfNodeRef));
		
		// Test datalist modified
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				FinishedProductData pfData = (FinishedProductData) alfrescoRepository.findOne(pfNodeRef);
				NodeRef nodeRef = pfData.getAllergenList().get(0).getNodeRef();
				// change something
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, false);				
				
				return null;

			}
		});
		
		assertTrue(entityReportService.shouldGenerateReport(pfNodeRef));
		
		// Delete report tpl -> report should be deleted
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				logger.info("Delete report Tpl");
				nodeService.setProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);				
				
				entityReportService.generateReport(pfNodeRef);
				
				// check report Tpl
				List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT);
				assertEquals("check system templates", 2, reportTplNodeRefs.size());
				
				// check other report is deleted
				List<NodeRef> reportNodeRefs = associationService.getTargetAssocs(pfNodeRef, ReportModel.ASSOC_REPORTS);
				assertEquals(2, reportNodeRefs.size());
				
				boolean hasReportPF2Name = false;
				for(NodeRef n : reportNodeRefs){
					String reportName = (String)nodeService.getProperty(n, ContentModel.PROP_NAME);
					if(reportName.contains("report PF 2")){
						hasReportPF2Name = true;
					}
				}
				assertFalse(hasReportPF2Name);
				
				nodeService.setProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, false);
				
				return null;

			}
		});
		
		

	}
	
	private void checkReportNames(NodeRef defaultReportTplNodeRef, NodeRef otherReportTplNodeRef, List<NodeRef>reportNodeRefs){
		
		String defaultReportName = String.format("%s - %s", 
				nodeService.getProperty(pfNodeRef, ContentModel.PROP_NAME), 
				nodeService.getProperty(defaultReportTplNodeRef, ContentModel.PROP_NAME))
				.replace(RepoConsts.REPORT_EXTENSION_BIRT, ((String)nodeService.getProperty(defaultReportTplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT)).toLowerCase());
		
		String otherReportName = String.format("%s - %s", 
						nodeService.getProperty(pfNodeRef, ContentModel.PROP_NAME), 
						nodeService.getProperty(otherReportTplNodeRef, ContentModel.PROP_NAME))
						.replace(RepoConsts.REPORT_EXTENSION_BIRT, ((String)nodeService.getProperty(otherReportTplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT)).toLowerCase());
		
		int checks=0;
		for(NodeRef reportNodeRef : reportNodeRefs){
			String reportName = (String)nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);
			if(reportName.equals(defaultReportName)){
				checks++;
			}
			else if(reportName.equals(otherReportName)){
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
	public void testGetProductSystemReportTemplates() throws InterruptedException {

		initReports();
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef systemFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
				NodeRef productReportTplFolder = repoService.getOrCreateFolderByPath(reportsFolder, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));

				// create PF
				FinishedProductData pfData = new FinishedProductData();
				pfData.setName("PF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3), false));
				pfData.setAllergenList(allergenList);

				pfNodeRef = alfrescoRepository.create(testFolderNodeRef, pfData).getNodeRef();

				QName typeQName = nodeService.getType(pfNodeRef);
				assertEquals("check system templates", 3, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());

				assertEquals("check user templates", 0, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "user").size());

				// add a user template
				reportTplService.createTplRptDesign(productReportTplFolder, "user tpl", "beCPG/birt/document/product/default/ProductReport.rptdesign", ReportType.Document,
						ReportFormat.PDF, BeCPGModel.TYPE_FINISHEDPRODUCT, false, true, true);

				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "user").size());

				// add a user template
				NodeRef userTpl2NodeRef = reportTplService.createTplRptDesign(productReportTplFolder, "user tpl 2", "beCPG/birt/document/product/default/ProductReport.rptdesign",
						ReportType.Document, ReportFormat.PDF, BeCPGModel.TYPE_FINISHEDPRODUCT, false, false, true);

				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "u*").size());
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "user*").size());
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "user tpl 2").size());
				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "\"user tpl 2\"")
						.size());
				assertEquals("check user templates", userTpl2NodeRef,
						reportTplService.getUserReportTemplate(ReportType.Document, BeCPGModel.TYPE_FINISHEDPRODUCT, "user tpl 2"));

				return null;

			}
		}, false, true);

	}
}
