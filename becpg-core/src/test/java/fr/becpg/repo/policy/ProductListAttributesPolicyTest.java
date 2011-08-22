/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.policy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductListPoliciesTest.
 *
 * @author querephi
 */
public class ProductListAttributesPolicyTest  extends RepoBaseTestCase  {
	
	/** The PAT h_ testfolder. */
	private static String PATH_TESTFOLDER = "TestFolder";       
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductListAttributesPolicyTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;	
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/** The repository helper. */
	private Repository repositoryHelper;    
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");  
    	productDAO = (ProductDAO)appCtx.getBean("productDAO");
        authenticationComponent = (AuthenticationComponent)appCtx.getBean("authenticationComponent");
        repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
                        
    }
    
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {
		try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Don't let this mask any previous exceptions
        }
        super.tearDown();

    }	
	
	/**
	 * Create a cost list and nut list without filling units
	 */
	   public void testCreateProductLists(){
		   
		   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {
				
					deleteCharacteristics();
					initCharacteristics();
					
					/*-- Create test folder --*/
					NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
					if(folderNodeRef != null)
					{
						fileFolderService.delete(folderNodeRef);    		
					}			
					folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
									
					/*
					 * Create raw material
					 */
					
					RawMaterialData rawMaterialData = new RawMaterialData();
					rawMaterialData.setUnit(ProductUnit.kg);
					rawMaterialData.setName("RM");
					
					NodeRef cost1 = costs.get(0);
					nodeService.setProperty(cost1, BeCPGModel.PROP_COSTCURRENCY, "€");
					NodeRef cost2 = costs.get(1);
					nodeService.setProperty(cost2, BeCPGModel.PROP_COSTCURRENCY, "$");
					
					NodeRef nut1 = nuts.get(0);
					nodeService.setProperty(nut1, BeCPGModel.PROP_NUTUNIT, "kcal");
					NodeRef nut2 = nuts.get(1);
					nodeService.setProperty(nut2, BeCPGModel.PROP_NUTUNIT, "kJ");
					
					List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
					costList.add(new CostListDataItem(null, 12f, "", cost1));
					costList.add(new CostListDataItem(null, 11f, "", cost2));
					rawMaterialData.setCostList(costList);
					
					List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
					nutList.add(new NutListDataItem(null, 12.4f, "", "Groupe 1", nut1));
					nutList.add(new NutListDataItem(null, 12.5f, "", "Groupe 1", nut2));
					rawMaterialData.setNutList(nutList);
					
					Set<QName> dataLists = new HashSet<QName>();
					dataLists.add(BeCPGModel.TYPE_COSTLIST);
					dataLists.add(BeCPGModel.TYPE_NUTLIST);
					NodeRef rawMaterialNodeRef = productDAO.create(folderNodeRef, rawMaterialData, dataLists);											
					
					RawMaterialData rawMaterialDBData = (RawMaterialData)productDAO.find(rawMaterialNodeRef, dataLists);
					
					for(CostListDataItem c : rawMaterialDBData.getCostList()){
						
						logger.debug("costList unit: " + c.getUnit());
						if(c.getCost().equals(cost1)){
							assertEquals("Check 1st costList", "€/kg", c.getUnit());
						}
						else if(c.getCost().equals(cost2)){
							assertEquals("Check 2nd costList", "$/kg", c.getUnit());
						}
						else{
							assertTrue(false);
						}
					}
					
					for(NutListDataItem n : rawMaterialDBData.getNutList()){
						
						logger.debug("nutList unit: " + n.getUnit());
						if(n.getNut().equals(nut1)){
							assertEquals("Check 1st nutList", "kcal/100g", n.getUnit());
						}
						else if(n.getNut().equals(nut2)){
							assertEquals("Check 2nd nutList", "kJ/100g", n.getUnit());
						}
						else{
							assertTrue(false);
						}
					}
					
					/*
					 * Change product unit
					 */
					nodeService.setProperty(rawMaterialNodeRef, BeCPGModel.PROP_PRODUCT_UNIT, ProductUnit.L);
					
					rawMaterialDBData = (RawMaterialData)productDAO.find(rawMaterialNodeRef, dataLists);
					
					for(CostListDataItem c : rawMaterialDBData.getCostList()){
						
						logger.debug("costList unit: " + c.getUnit());
						if(c.getCost().equals(cost1)){
							assertEquals("Check 1st costList", "€/L", c.getUnit());
						}
						else if(c.getCost().equals(cost2)){
							assertEquals("Check 2nd costList", "$/L", c.getUnit());
						}
						else{
							assertTrue(false);
						}
					}
					
					for(NutListDataItem n : rawMaterialDBData.getNutList()){
						
						logger.debug("nutList unit: " + n.getUnit());
						if(n.getNut().equals(nut1)){
							assertEquals("Check 1st nutList", "kcal/100mL", n.getUnit());
						}
						else if(n.getNut().equals(nut2)){
							assertEquals("Check 2nd nutList", "kJ/100mL", n.getUnit());
						}
						else{
							assertTrue(false);
						}
					}
					
					/*
					 *  Change cost, nut
					 */
					NodeRef costListItem1NodeRef = null;
					NodeRef costListItem2NodeRef = null;
					for(CostListDataItem c : rawMaterialDBData.getCostList()){
						
						if(c.getCost().equals(cost1)){
							costListItem1NodeRef = c.getNodeRef();
						}
						else if(c.getCost().equals(cost2)){
							costListItem2NodeRef = c.getNodeRef();
						}						
					}
					
					nodeService.removeAssociation(costListItem1NodeRef, cost1, BeCPGModel.ASSOC_COSTLIST_COST);
					nodeService.removeAssociation(costListItem2NodeRef, cost2, BeCPGModel.ASSOC_COSTLIST_COST);					
					nodeService.createAssociation(costListItem1NodeRef, cost2, BeCPGModel.ASSOC_COSTLIST_COST);
					nodeService.createAssociation(costListItem2NodeRef, cost1, BeCPGModel.ASSOC_COSTLIST_COST);
					
					NodeRef nutListItem1NodeRef = null;
					NodeRef nutListItem2NodeRef = null;
					for(NutListDataItem n : rawMaterialDBData.getNutList()){
						
						if(n.getNut().equals(nut1)){
							nutListItem1NodeRef = n.getNodeRef();
						}
						else if(n.getNut().equals(nut2)){
							nutListItem2NodeRef = n.getNodeRef();
						}						
					}
					
					nodeService.removeAssociation(nutListItem1NodeRef, nut1, BeCPGModel.ASSOC_NUTLIST_NUT);
					nodeService.removeAssociation(nutListItem2NodeRef, nut2, BeCPGModel.ASSOC_NUTLIST_NUT);		
					nodeService.createAssociation(nutListItem1NodeRef, nut2, BeCPGModel.ASSOC_NUTLIST_NUT);
					nodeService.createAssociation(nutListItem2NodeRef, nut1, BeCPGModel.ASSOC_NUTLIST_NUT);
					
					rawMaterialDBData = (RawMaterialData)productDAO.find(rawMaterialNodeRef, dataLists);
					
					for(CostListDataItem c : rawMaterialDBData.getCostList()){
						
						logger.debug("costList unit: " + c.getUnit());
						if(c.getNodeRef().equals(costListItem1NodeRef)){
							assertEquals("Check 1st costList", "$/L", c.getUnit());
						}
						else if(c.getNodeRef().equals(costListItem2NodeRef)){
							assertEquals("Check 2nd costList", "€/L", c.getUnit());
						}
						else{
							assertTrue(false);
						}
					}
					
					for(NutListDataItem n : rawMaterialDBData.getNutList()){
						
						logger.debug("nutList unit: " + n.getUnit());
						if(n.getNodeRef().equals(nutListItem1NodeRef)){
							assertEquals("Check 1st nutList", "kJ/100mL", n.getUnit());
						}
						else if(n.getNodeRef().equals(nutListItem2NodeRef)){
							assertEquals("Check 2nd nutList", "kcal/100mL", n.getUnit());
						}
						else{
							assertTrue(false);
						}
					}
					
					return null;
					
				}},false,true);
		   
	   }  
 	
}
