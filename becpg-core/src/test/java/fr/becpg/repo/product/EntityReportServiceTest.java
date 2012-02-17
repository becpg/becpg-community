/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.report.template.ReportFormat;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductReportServiceTest.
 *
 * @author querephi
 */
public class EntityReportServiceTest extends RepoBaseTestCase {

	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";    
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityReportServiceTest.class);
	

	/** The product dao. */
	private ProductDAO productDAO;
	
	private ReportTplService reportTplService;
	
	
	/** The policy behaviour filter. */
	private BehaviourFilter policyBehaviourFilter;
	
	/** The sf node ref. */
	private NodeRef sfNodeRef;
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	productDAO = (ProductDAO)ctx.getBean("productDAO");
    	
    	reportTplService = (ReportTplService)ctx.getBean("reportTplService");
    	policyBehaviourFilter = (BehaviourFilter)ctx.getBean("policyBehaviourFilter");
    	
    	transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {

 				deleteReportTpls();
 				deleteCharacteristics();
 				initCharacteristics();
 				 		        
 				return null;

 			}},false,true); 
    }
    
    
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }		
	
	/**
	 * Reset the property report modified.
	 */
	private void resetReportModified(){
				
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				policyBehaviourFilter.disableBehaviour(sfNodeRef, ContentModel.ASPECT_AUDITABLE);
				nodeService.setProperty(sfNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, new Date());
				policyBehaviourFilter.enableBehaviour(sfNodeRef, ContentModel.ASPECT_AUDITABLE);
				return null;
				
			}});
		
	}
	
	
	/**
	 * Check if node has changed, so the report is out of date.
	 *
	 * @param nodeRef the node ref
	 * @return true, if is report up to date
	 */	
	private  boolean isReportUpToDate(NodeRef nodeRef) {
					
		Date reportModified = (Date)nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);
		
		// report not generated
		if(reportModified == null){
			logger.debug("report not generated");
			return false;
		}
		
		// check modified date (modified is always bigger than reportModified so a delta is defined)
		Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);		
		logger.debug("modified: " + ISO8601DateFormat.format(modified) + " - reportModified: " + ISO8601DateFormat.format(reportModified));
		if(modified.after(reportModified)){			
			logger.debug("node has been modified");
			return false;
		}		
		
		return true;
	}	
	
	/**
	 * Test is report up to date.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	public void testIsReportUpToDate() throws InterruptedException{		   		
	   	
		logger.debug("testIsReportUpToDate()");
		
		// Add report tpl
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
							   	
				
				/*-- Add report tpl --*/
				NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
			   	NodeRef reportsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
			   	NodeRef productReportTplFolder = repoService.createFolderByPath(reportsFolder, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));		   			   		   
		   		
			   	NodeRef tplNodeRef = reportTplService.createTplRptDesign(productReportTplFolder, 
			   										"report SF", 
			   										"beCPG/birt/document/product/default/ProductReport.rptdesign", 
		   											ReportType.Document, 
		   											ReportFormat.PDF,
		   											BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, 
		   											true, 
		   											true,
		   											true);
			   	
			   	logger.debug("###tplNodeRef: " + tplNodeRef);
			   	
				return null;
				
			}});
		
		assertEquals("check system templates", 1, reportTplService.getSystemReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT).size());
		
		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
							   	
				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
				
				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);
				
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
				
				assertEquals("check system templates", 1, reportTplService.getSystemReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT).size());
				
				return null;
				
			}});	   				
		
		// load SF and test it
		Collection<QName> dataLists = productDictionaryService.getDataLists();
		final SemiFinishedProductData sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
		
		// wait since it is done in a threadpool
		Thread.sleep(6000);
		
		// product report should be update to date due to policy		
		assertEquals("check if report is up to date", true, isReportUpToDate(sfNodeRef));
		
		// change product property, should be still up to date due to policy
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_NAME, "SF");				
				return null;
				
			}});
		
		assertEquals("check if report is up to date", true, isReportUpToDate(sfNodeRef));
		
		//change product property, should be out of date (policy disabled)
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				policyBehaviourFilter.disableBehaviour(sfNodeRef, BeCPGModel.TYPE_PRODUCT);
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_NAME, "SF1");	
				policyBehaviourFilter.enableBehaviour(sfNodeRef, BeCPGModel.TYPE_PRODUCT);
				return null;
				
			}});
		
		
		assertEquals("check if report is up to date", false, isReportUpToDate(sfNodeRef));
		
		// reset
		resetReportModified();
		assertEquals("check if report is up to date", true, isReportUpToDate(sfNodeRef));
		
		//change product property, should be out of date (policy enabled)		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_NAME, "SF1");				
				return null;
				
			}});
							
		assertEquals("check if report is up to date", true, isReportUpToDate(sfNodeRef));
		
		
		// reset
		resetReportModified();		
		assertEquals("check if report is up to date", true, isReportUpToDate(sfNodeRef));
		
		// setProperty of allergen without changing anything => should be up to date
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				NodeRef nodeRef = sfData.getAllergenList().get(0).getAllergen();
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, true);				
				return null;
				
			}});
		
		assertEquals("check if report is up to date", true, isReportUpToDate(sfNodeRef));															   
	}
	
	/**
	 * Test get product system report templates.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	public void testGetProductSystemReportTemplates() throws InterruptedException{
		   		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {				
				
				NodeRef systemFolder = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
			   	NodeRef reportsFolder = repoService.createFolderByPath(systemFolder, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
			   	NodeRef productReportTplFolder = repoService.createFolderByPath(reportsFolder, RepoConsts.PATH_PRODUCT_REPORTTEMPLATES, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_REPORTTEMPLATES));		   			   		   
		   				   	
			   	
				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
									
				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);
				
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
				
				QName typeQName = nodeService.getType(sfNodeRef);
				assertEquals("check system templates", 0, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());						   			   		  
		   		
				// add a system template
				reportTplService.createTplRptDesign(productReportTplFolder, 
													"report MP", 
													"beCPG/birt/document/product/default/ProductReport.rptdesign", 
													ReportType.Document,
													ReportFormat.PDF,
													BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, 
													true, 
													true,
													true);
				
				assertEquals("check system templates", 1, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());
				
				// add a system template
				reportTplService.createTplRptDesign(productReportTplFolder, 
													"report MP 2", 
													"beCPG/birt/document/product/default/ProductReport.rptdesign", 
													ReportType.Document, 
													ReportFormat.PDF,
													BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, 
													true, 
													false,
													true);
				assertEquals("check system templates", 2, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());				
				
				assertEquals("check user templates", 0, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user").size());

				// add a user template
				reportTplService.createTplRptDesign(productReportTplFolder, 
													"user tpl", 
													"beCPG/birt/document/product/default/ProductReport.rptdesign", 
													ReportType.Document, 
													ReportFormat.PDF,
													BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, 
													false, 
													true,
													true);				

				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user").size());
				
				// add a user template
				NodeRef userTpl2NodeRef = reportTplService.createTplRptDesign(productReportTplFolder, 
													"user tpl 2", 
													"beCPG/birt/document/product/default/ProductReport.rptdesign", 
													ReportType.Document,
													ReportFormat.PDF,
													BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, 
													false, 
													false,
													true);	
				
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "u*").size());
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user*").size());								
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user tpl 2").size());
				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "\"user tpl 2\"").size());
				assertEquals("check user templates", userTpl2NodeRef, reportTplService.getUserReportTemplate(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user tpl 2"));
				
				return null;
				
			}},false,true);	   		
		
		
	}	
}
