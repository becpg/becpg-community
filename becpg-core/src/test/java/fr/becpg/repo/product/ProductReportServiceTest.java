/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductReportServiceTest.
 *
 * @author querephi
 */
public class ProductReportServiceTest extends RepoBaseTestCase {

	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";    
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductReportServiceTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;	
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The repository helper. */
	private Repository repositoryHelper;
	
	/** The product report service. */
	private EntityReportService entityReportService;
	
	private ReportTplService reportTplService;
	
	private RepoService repoService;
	
	/** The policy behaviour filter. */
	private BehaviourFilter policyBehaviourFilter;
	
	private EntityListDAO entityListDAO;
	
	/** The sf node ref. */
	private NodeRef sfNodeRef;
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)appCtx.getBean("nodeService");    	
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");
    	productDAO = (ProductDAO)appCtx.getBean("productDAO");
    	productDictionaryService = (ProductDictionaryService)appCtx.getBean("productDictionaryService");
    	repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
    	entityReportService = (EntityReportService)appCtx.getBean("entityReportService");
    	reportTplService = (ReportTplService)appCtx.getBean("reportTplService");
    	policyBehaviourFilter = (BehaviourFilter)appCtx.getBean("policyBehaviourFilter");
    	repoService = (RepoService)appCtx.getBean("repoService");
    	entityListDAO = (EntityListDAO)appCtx.getBean("entityListDAO");
    	
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
				nodeService.setProperty(sfNodeRef, ReportModel.PROP_REPORT_NODE_GENERATED, new Date());
				policyBehaviourFilter.enableBehaviour(sfNodeRef, ContentModel.ASPECT_AUDITABLE);
				return null;
				
			}},false,true);
		
	}
	
	/**
	 * Test is report up to date.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	public void xtestIsReportUpToDate() throws InterruptedException{
		   
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
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0)));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1)));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2)));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3)));
				sfData.setAllergenList(allergenList);
				
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
				
				return null;
				
			}},false,true);	   		
		
		// load SF and test it
		Collection<QName> dataLists = productDictionaryService.getDataLists();
		final SemiFinishedProductData sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
		
		// product report should be update to date due to policy		
		assertEquals("check if report is up to date", true, entityReportService.isReportUpToDate(sfNodeRef));
		
		// change product property, should be still up to date due to policy
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_NAME, "SF");				
				return null;
				
			}},false,true);
		
		assertEquals("check if report is up to date", true, entityReportService.isReportUpToDate(sfNodeRef));
		
		//change product property, should be out of date (policy disabled)
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				policyBehaviourFilter.disableBehaviour(sfNodeRef, BeCPGModel.TYPE_PRODUCT);
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_NAME, "SF1");	
				policyBehaviourFilter.enableBehaviour(sfNodeRef, BeCPGModel.TYPE_PRODUCT);
				return null;
				
			}},false,true);
		
		
		assertEquals("check if report is up to date", false, entityReportService.isReportUpToDate(sfNodeRef));
		
		// reset
		resetReportModified();
		assertEquals("check if report is up to date", true, entityReportService.isReportUpToDate(sfNodeRef));
		
		//change product property, should be out of date (policy enabled)		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_NAME, "SF1");				
				return null;
				
			}},false,true);
							
		assertEquals("check if report is up to date", true, entityReportService.isReportUpToDate(sfNodeRef));
		
		
		// reset
		resetReportModified();		
		assertEquals("check if report is up to date", true, entityReportService.isReportUpToDate(sfNodeRef));
		
		// setProperty of allergen without changing anything => should be up to date
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				NodeRef nodeRef = sfData.getAllergenList().get(0).getAllergen();
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, true);				
				return null;
				
			}},false,true);
		
		assertEquals("check if report is up to date", true, entityReportService.isReportUpToDate(sfNodeRef));													
		
		// setProperty of allergen and change smth => should be out of date
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				NodeRef nodeRef = sfData.getAllergenList().get(0).getNodeRef();
				logger.debug("allergen prev value " + nodeService.getProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY));
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, false);	
								
				return null;
				
			}},false,true);						
		
		assertEquals("check if report is up to date", false, entityReportService.isReportUpToDate(sfNodeRef));
		
		// reset
		resetReportModified();
		Thread.sleep(1000);
		
		// add an allergen		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);
				NodeRef allergen = allergens.get(5);
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    			properties.put(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY, true);
	    		properties.put(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, false);
	    		ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, allergen.getId()), BeCPGModel.TYPE_ALLERGENLIST, properties);	
				NodeRef linkNodeRef = childAssocRef.getChildRef();
    			nodeService.createAssociation(linkNodeRef, allergen, BeCPGModel.PROP_ALLERGENLIST_ALLERGEN);
    			
    			logger.debug("listNodeRef: " + listNodeRef);
    			logger.debug("added allergen modified: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_MODIFIED));
    			logger.debug("added allergen created: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_CREATED));
    			
				return null;
				
			}},false,true);	
		
		assertEquals("check if report is up to date", false, entityReportService.isReportUpToDate(sfNodeRef));
		
		// reset
		resetReportModified();
		Thread.sleep(1000);		
		
		// remove an allergen
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {												
				
				NodeRef nodeRef = sfData.getAllergenList().get(1).getNodeRef();
				nodeService.deleteNode(nodeRef);	
								
				return null;
				
			}},false,true);	
		
		assertEquals("check if report is up to date", false, entityReportService.isReportUpToDate(sfNodeRef));
		
	   
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
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0)));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1)));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2)));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3)));
				sfData.setAllergenList(allergenList);
				
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);				
				
				QName typeQName = nodeService.getType(sfNodeRef);
				assertEquals("check system templates", 0, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());
				
				// add a system template
				reportTplService.createTpl(productReportTplFolder, "report MP", "beCPG/birt/ProductReport.rptdesign", ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, true, true);
				assertEquals("check system templates", 1, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());
				
				// add a system template
				reportTplService.createTpl(productReportTplFolder, "report MP 2", "beCPG/birt/ProductReport.rptdesign", ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, true, false);
				assertEquals("check system templates", 2, reportTplService.getSystemReportTemplates(ReportType.Document, typeQName).size());				
				
				assertEquals("check user templates", 0, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user").size());

				// add a user template
				reportTplService.createTpl(productReportTplFolder, "user tpl", "beCPG/birt/ProductReport.rptdesign", ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, false, true);

				reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user");
				reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user*");
				reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user tpl");
				reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "(user)");
				
				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user").size());
				
				// add a user template
				reportTplService.createTpl(productReportTplFolder, "user tpl 2", "beCPG/birt/ProductReport.rptdesign", ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, false, false);
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "u*").size());
				assertEquals("check user templates", 2, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user*").size());								
				assertEquals("check user templates", 1, reportTplService.suggestUserReportTemplates(ReportType.Document, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, "user tpl 2").size());
				
				return null;
				
			}},false,true);	   		
		
		
	}	
}
