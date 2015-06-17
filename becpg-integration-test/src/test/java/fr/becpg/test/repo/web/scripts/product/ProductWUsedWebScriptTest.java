/*
 * 
 */
package fr.becpg.test.repo.web.scripts.product;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.PostRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductWUsedWebScriptTest.
 *
 * @author querephi
 */
@SuppressWarnings("unused")
public class ProductWUsedWebScriptTest extends fr.becpg.test.PLMBaseTestCase{

	private static final Log logger = LogFactory.getLog(ProductWUsedWebScriptTest.class);
	
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
	 				rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
	 				LocalSemiFinishedProductData lSF = new LocalSemiFinishedProductData();
	 				lSF.setName("Local semi finished");
	 				NodeRef lSFNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF).getNodeRef();
	 				 				 			
	 				/*-- Create finished product --*/
	 				logger.debug("/*-- Create finished product --*/");
	 				FinishedProductData finishedProduct = new FinishedProductData();
	 				finishedProduct.setName("Finished Product");
	 				List<CompoListDataItem> compoList = new ArrayList<>();
	 				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, lSFNodeRef));
	 				compoList.add(new CompoListDataItem(null, compoList.get(0), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterialNodeRef));
					finishedProduct.getCompoListView().setCompoList(compoList); 				

	 				finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	 				
	 				logger.debug("local semi finished: " + lSFNodeRef);
	 				logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);
	 				
					return null;

				}},false,true);
		 
			//Call webscript on raw material
			String url = "/becpg/entity/datalists/data/node?entityNodeRef="+rawMaterialNodeRef.toString()+"&itemType=bcpg%3AcompoList&dataListName=WUsed";
			String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"all\",\"filterData\":\"\"}}";
			logger.debug("url : " + url);				

			Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content : " + response.getContentAsString());
			response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
			logger.debug("content : " + response.getContentAsString());			
		

    }
    	
}
