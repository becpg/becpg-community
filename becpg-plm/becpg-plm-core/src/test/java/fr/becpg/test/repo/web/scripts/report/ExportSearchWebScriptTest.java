/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.web.scripts.report;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchWebScriptTest.
 *
 * @author querephi
 */
public class ExportSearchWebScriptTest extends fr.becpg.test.PLMBaseWebScriptTest{

	/** The logger. */
	private static Log logger = LogFactory.getLog(ExportSearchWebScriptTest.class);
	
	
	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearchQuery.xml";
	
	@Resource
    private ReportTplService reportTplService;
 
	@Resource 
	private EntityService entityService;
	
	
	/** The local s f1 node ref. */
	private NodeRef  localSF1NodeRef;
    
    /** The raw material1 node ref. */
    private NodeRef  rawMaterial1NodeRef;
    
    /** The raw material2 node ref. */
    private NodeRef  rawMaterial2NodeRef;
    
    /** The local s f2 node ref. */
    private NodeRef  localSF2NodeRef;
    
    /** The raw material3 node ref. */
    private NodeRef  rawMaterial3NodeRef;
    
    /** The raw material4 node ref. */
    private NodeRef  rawMaterial4NodeRef;
    

    
    /** The export product report tpl. */
    private NodeRef exportProductReportTpl;
    
	/**
	 * Inits the objects.
	 */
	private void initObjects(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
									
		
				//costs
				NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
				NodeRef costFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_COSTS));				
				if(costFolder != null){
					fileFolderService.delete(costFolder);
				}				
				costFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_COSTS), ContentModel.TYPE_FOLDER).getNodeRef();
				List<FileInfo> costsFileInfo = fileFolderService.listFiles(costFolder);
				if(costsFileInfo.size() == 0){
					
					String [] costNames = {"Coût MP","Coût prév MP","Coût Emb","Coût prév emb"};
					for(String costName : costNames)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, costName);
			    		properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			    		ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_COST, properties);
			    		costs.add(childAssocRef.getChildRef());
			    	}
				}
				
				//nuts
				NodeRef nutFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_NUTS));				
				if(nutFolder != null){
					fileFolderService.delete(nutFolder);
				}				
				nutFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_NUTS), ContentModel.TYPE_FOLDER).getNodeRef();
				List<FileInfo> nutsFileInfo = fileFolderService.listFiles(nutFolder);
				if(nutsFileInfo.size() == 0){
					
					String [] nutNames = {"Protéines","Lipides","Glucides",};
					for(String nutName : nutNames)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, nutName);
			    		properties.put(PLMModel.PROP_NUTGROUP, "Groupe 1");
			    		properties.put(PLMModel.PROP_NUTUNIT, "g");
			    		ChildAssociationRef childAssocRef = nodeService.createNode(nutFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_NUT, properties);
			    		nuts.add(childAssocRef.getChildRef());
			    	}
				}				
				
				//allergens
				NodeRef allergensFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_ALLERGENS));
				if(allergensFolder == null){
					allergensFolder = fileFolderService.create(systemFolder, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_ALLERGENS), ContentModel.TYPE_FOLDER).getNodeRef();
				}
				List<FileInfo> allergensFileInfo = fileFolderService.listFiles(allergensFolder);
				if(allergensFileInfo.size() == 0){
					for(int i=0 ; i<10 ; i++)
			    	{    		
			    		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			    		properties.put(ContentModel.PROP_NAME, "Allergen " + i);
			    		ChildAssociationRef childAssocRef = nodeService.createNode(allergensFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_ALLERGEN, properties);
			    		allergens.add(childAssocRef.getChildRef());
			    	}
				}
				else{
					for(FileInfo fileInfo : allergensFileInfo){
						allergens.add(fileInfo.getNodeRef());
					}
				}
			
				/*-- Create raw materials --*/
				logger.debug("/*-- Create raw materials --*/");
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");				
				rawMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial1).getNodeRef();
				
				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");					
				rawMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial2).getNodeRef();
				
				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");				
				rawMaterial3NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial3).getNodeRef();
				
				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");					
				rawMaterial4NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial4).getNodeRef();
				
				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");				
				 alfrescoRepository.create(testFolderNodeRef, rawMaterial5).getNodeRef();
				
				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF1).getNodeRef();
				
				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");							
				localSF2NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF2).getNodeRef();	
		
		return null;
		
			}},false,true);
		
	}

	/**
	 * Inits the tests.
	 * @throws IOException 
	 */
	private void initTests() throws IOException{
		
		logger.debug("look for report template");
	   	
		
	   	
	   	// reports folder
	   	NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolderNodeRef, RepoConsts.PATH_REPORTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));    	
	   	assertNotNull("Check reports folder", reportsFolder);
	   	
	   	// export search report
		NodeRef exportSearchNodeRef = repoService.getOrCreateFolderByPath(reportsFolder, RepoConsts.PATH_REPORTS_EXPORT_SEARCH, TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH));
		NodeRef exportSearchProductsNodeRef = repoService.getOrCreateFolderByPath(exportSearchNodeRef, PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS));
		
		exportProductReportTpl = reportTplService.createTplRptDesign(exportSearchProductsNodeRef, 
											TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS), 
											EXPORT_PRODUCTS_REPORT_RPTFILE_PATH, 
											ReportType.ExportSearch, 	
											ReportFormat.XLS,
											PLMModel.TYPE_PRODUCT, 
											false, 
											true, 
											true);
		
		reportTplService.createTplRessource(exportSearchProductsNodeRef, 												
											EXPORT_PRODUCTS_REPORT_XMLFILE_PATH, 												
											false);						   		  
	}
	
	/**
	 * Adds the product image.
	 *
	 * @param parentNodeRef the parent node ref
	 * @throws IOException 
	 * @throws ContentIOException 
	 */
	@Deprecated 
	//Use writeImages of entityService instead
	//merge with productserviceTest or inside helper
	private void addProductImage(NodeRef parentNodeRef) throws ContentIOException, IOException{
		
		
		/*-- add product image--*/
		logger.debug("/*-- add product image--*/");
		String imageName = I18NUtil.getMessage(RepoConsts.PATH_LOGO_IMAGE) + ".jpg";			
		logger.debug("image name: " + imageName);	
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, imageName);
		NodeRef imageNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)properties.get(ContentModel.PROP_NAME)), ContentModel.TYPE_CONTENT, properties).getChildRef();
		
		ContentWriter writer = contentService.getWriter(imageNodeRef, ContentModel.PROP_CONTENT, true);

		ClassPathResource img  = new ClassPathResource("beCPG/birt/productImage.jpg");
		

		String mimetype = mimetypeService.guessMimetype(img.getFilename());
		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
		Charset charset = charsetFinder.getCharset(img.getInputStream(), mimetype);
		String encoding = charset.name();
		
		
		logger.debug("mimetype : " + mimetype);
		logger.debug("encoding : " + encoding);
		writer.setMimetype(mimetype);
		writer.setEncoding(encoding);
		writer.putContent(img.getInputStream());		
   }
		
	/**
	 * Test export search.
	 */
	@Test
	public void testExportSearch(){
	
		//TODO : merge CompareProductReportWebScript avec CompareProductServiceTest => beaucoup de code en commun !
		// init objects
		initObjects();
	
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
									
				//Create comparison product report
				initTests();
				
				logger.debug("createRawMaterial 1");
				
				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");			
		
				//Costs
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();		    		
				for(int j=0 ; j<costs.size() ; j++)
				{		    			
					CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, costs.get(j), false);
					costList.add(costListItemData);
				}		
				fp1.setCostList(costList);
				
				// create an MP for the allergens
				RawMaterialData allergenRawMaterial = new RawMaterialData();
				allergenRawMaterial.setName("MP allergen");
				NodeRef allergenRawMaterialNodeRef = alfrescoRepository.create(testFolderNodeRef, allergenRawMaterial).getNodeRef();
				
				//Allergens
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();		    		
				for(int j=0 ; j<allergens.size() ; j++)
				{		    			
					List<NodeRef> voluntarySources = new ArrayList<NodeRef>();
					voluntarySources.add(allergenRawMaterialNodeRef);
					
					AllergenListDataItem allergenListItemData = new AllergenListDataItem(null,null, true, false, voluntarySources, null, allergens.get(j), false);
					allergenList.add(allergenListItemData);
				}		
				fp1.setAllergenList(allergenList);
					
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				fp1.getCompoListView().setCompoList(compoList);
				
				 alfrescoRepository.create(testFolderNodeRef, fp1).getNodeRef();
				
				logger.debug("create FP 2");
				
				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");			
		
				//Costs
				costList = new ArrayList<CostListDataItem>();		    		
				for(int j=0 ; j<costs.size() ; j++)
				{		    			
					CostListDataItem costListItemData = new CostListDataItem(null, 12.4d, "$/kg", null, costs.get(j), false);
					costList.add(costListItemData);
				}		
				fp2.setCostList(costList);
					
				//Allergens
				allergenList = new ArrayList<AllergenListDataItem>();		    		
				for(int j=0 ; j<allergens.size() ; j++)
				{		    			
					List<NodeRef> allSources = new ArrayList<NodeRef>();
					allSources.add(allergenRawMaterialNodeRef);
					AllergenListDataItem allergenListItemData = null;
					
					if(j < 5){
						allergenListItemData = new AllergenListDataItem(null,null, true, false, allSources, null, allergens.get(j), false);
					}
					else{
						allergenListItemData = new AllergenListDataItem(null,null, false, true, null, allSources, allergens.get(j), false);
					}						
					
					allergenList.add(allergenListItemData);
				}		
				fp2.setAllergenList(allergenList);
				
				compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 2d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
				fp2.getCompoListView().setCompoList(compoList);
				
				alfrescoRepository.create(testFolderNodeRef, fp2).getNodeRef();
				
				/*-- Create images folder --*/					
				NodeRef imagesNodeRef = fileFolderService.create(testFolderNodeRef, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES), ContentModel.TYPE_FOLDER).getNodeRef();					
				addProductImage(imagesNodeRef);
				
				
				
				return null;
				
			}},false,true);						
		
		// search on date range
		try{
						
			String url = "/becpg/report/exportsearch/" + exportProductReportTpl.toString().replace("://", "/") + "/Excel.xls?repo=true&term=&query={\"prop_cm_name\"%3A\"\"%2C\"prop_bcpg_legalName\"%3A\"\"%2C\"prop_bcpg_productHierarchy1\"%3A\"\"%2C\"prop_bcpg_productHierarchy2\"%3A\"\"%2C\"prop_bcpg_productState\"%3A\"\"%2C\"prop_bcpg_productCode\"%3A\"\"%2C\"prop_bcpg_eanCode\"%3A\"\"%2C\"assoc_bcpg_supplierAssoc\"%3A\"\"%2C\"assoc_bcpg_supplierAssoc_added\"%3A\"\"%2C\"assoc_bcpg_supplierAssoc_removed\"%3A\"\"%2C\"prop_cm_modified-date-range\"%3A\"2011-04-17T00%3A00%3A00%2B02%3A00|2011-05-23T00%3A00%3A00%2B02%3A00\"%2C\"prop_cm_modifier\"%3A\"\"%2C\"assoc_bcpg_ingListIng\"%3A\"\"%2C\"assoc_bcpg_ingListIng_added\"%3A\"\"%2C\"assoc_bcpg_ingListIng_removed\"%3A\"\"%2C\"assoc_bcpg_ingListGeoOrigin\"%3A\"\"%2C\"assoc_bcpg_ingListGeoOrigin_added\"%3A\"\"%2C\"assoc_bcpg_ingListGeoOrigin_removed\"%3A\"\"%2C\"assoc_bcpg_ingListBioOrigin\"%3A\"\"%2C\"assoc_bcpg_ingListBioOrigin_added\"%3A\"\"%2C\"assoc_bcpg_ingListBioOrigin_removed\"%3A\"\"%2C\"datatype\"%3A\"bcpg%3Aproduct\"}";
							
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("Response: "+response.getContentAsString() );
		}
		catch(Exception e){				
			logger.error("Failed to execute webscript", e);
			assertNull("Should not throw an exception", e);
		}
		
		// search on cm:name
		try{
			
			String url = "/becpg/report/exportsearch/" + exportProductReportTpl.toString().replace("://", "/") + "/Excel.xls?repo=true&term=&query={\"prop_cm_name\"%3A\"FP\"%2C\"prop_cm_title\"%3A\"\"%2C\"prop_cm_description\"%3A\"\"%2C\"prop_mimetype\"%3A\"\"%2C\"prop_cm_modified-date-range\"%3A\"\"%2C\"prop_cm_modifier\"%3A\"\"%2C\"datatype\"%3A\"cm%3Acontent\"}";				
							
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			logger.debug("Response: "+response.getContentAsString() );
		}
		catch(Exception e){				
			logger.error("Failed to execute webscript", e);
			assertNull("Should not throw an exception", e);
			}
			
	   }
		
		/**
		 * Test get export search tpls.
		 */
		public void testGetExportSearchTpls(){
		
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {
										
				//Create product report
				initTests();
							
				//List<NodeRef> reportTpls = exportSearchService.getReportTpls();
				List<NodeRef> reportTpls = reportTplService.getUserReportTemplates(ReportType.ExportSearch, PLMModel.TYPE_PRODUCT, "*");
				
				for(NodeRef n : reportTpls){
					logger.debug("report name: " + nodeService.getProperty(n, ContentModel.PROP_NAME));
				}
				
				assertEquals("There is one report", 1, reportTpls.size());
				assertEquals("Check report nodeRef", exportProductReportTpl, reportTpls.get(0));
				
				return null;
				
			}},false,true);
		
		try{
		
			String url = "/becpg/report/exportsearch/templates/bcpg:product";
			
			Response response = sendRequest(new GetRequest(url), 200, "admin");
			
			logger.debug("response: " + response.getContentAsString());
		}
		catch(Exception e){
			logger.error("Failed to execute webscript", e);
			}
			
	   }
	
	
    	
}
