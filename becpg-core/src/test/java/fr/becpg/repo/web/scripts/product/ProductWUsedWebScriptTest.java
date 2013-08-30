/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductWUsedWebScriptTest.
 *
 * @author querephi
 */
public class ProductWUsedWebScriptTest extends fr.becpg.test.BaseWebScriptTest{

	private static Log logger = LogFactory.getLog(ProductWUsedWebScriptTest.class);
	
	private NodeRef rawMaterialNodeRef = null;
	private NodeRef finishedProductNodeRef = null;
	
		
	/**
	 * Testget product wused.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testgetProductWused() throws Exception {
		
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
				@Override
				public NodeRef execute() throws Throwable {					   
			
			    	/*-- Create raw material --*/
	 				logger.debug("/*-- Create raw material --*/");
	 				RawMaterialData rawMaterial = new RawMaterialData();
	 				rawMaterial.setName("Raw material");
	 				rawMaterialNodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial).getNodeRef();
	 				LocalSemiFinishedProductData lSF = new LocalSemiFinishedProductData();
	 				lSF.setName("Local semi finished");
	 				NodeRef lSFNodeRef = alfrescoRepository.create(testFolderNodeRef, lSF).getNodeRef();
	 				 				 			
	 				/*-- Create finished product --*/
	 				logger.debug("/*-- Create finished product --*/");
	 				FinishedProductData finishedProduct = new FinishedProductData();
	 				finishedProduct.setName("Finished Product");
	 				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>(); 				
	 				compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, lSFNodeRef));
	 				compoList.add(new CompoListDataItem(null, compoList.get(0), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterialNodeRef));
					finishedProduct.getCompoListView().setCompoList(compoList); 				
					Collection<QName> dataLists = new ArrayList<QName>();		
					dataLists.add(BeCPGModel.TYPE_COMPOLIST);
	 				finishedProductNodeRef = alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();
	 				
	 				logger.debug("local semi finished: " + lSFNodeRef);
	 				logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material
			String url = "/becpg/entity/datalists/data/node?entityNodeRef="+rawMaterialNodeRef.toString()+"&itemType=bcpg%3AcompoList&dataListName=WUsed";
			String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"all\",\"filterData\":\"\"}}";
			logger.debug("url : " + url);				

			Response response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			response = sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content : " + response.getContentAsString());			
		

    }
    	
}
