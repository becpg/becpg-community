/*
 *
 */
package fr.becpg.test.repo.web.scripts.product;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
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
public class ProductWUsedWebScriptIT extends fr.becpg.test.PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(ProductWUsedWebScriptIT.class);

	private NodeRef rawMaterialNodeRef = null;
	private NodeRef finishedProductNodeRef = null;

	/**
	 * Testget product wused.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testgetProductWused() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
			compoList.add(new CompoListDataItem(null, null, 1d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, lSFNodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			logger.debug("local semi finished: " + lSFNodeRef);
			logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);

			return null;

		}, false, true);

		// Call webscript on raw material
		String url = "/becpg/entity/datalists/data/node?entityNodeRef=" + rawMaterialNodeRef.toString()
				+ "&itemType=bcpg%3AcompoList&dataListName=WUsed";
		String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"all\",\"filterData\":\"\"}}";
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
		response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content : " + response.getContentAsString());

	}

	/**
	 * Test get product wused with model filter.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testgetProductWusedWithModelFilter() throws Exception {

		final List<NodeRef> tplNodeRefContainer = new ArrayList<>();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			logger.debug("/*-- Create raw material 2 --*/");
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw material 2");
			rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();
			LocalSemiFinishedProductData lSF = new LocalSemiFinishedProductData();
			lSF.setName("Local semi finished 2");
			NodeRef lSFNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), lSF).getNodeRef();

			/*-- Create finished product model --*/
			FinishedProductData finishedProductTpl = new FinishedProductData();
			finishedProductTpl.setName("My Product Model");
			NodeRef tplNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProductTpl).getNodeRef();
			tplNodeRefContainer.add(tplNodeRef);

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product 2 --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product 2");
			finishedProduct.setEntityTpl(finishedProductTpl);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, 1d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, lSFNodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), 3d, 0d, ProductUnit.kg, 0d, DeclarationType.Omit, rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			return null;

		}, false, true);

		NodeRef tplNodeRef = tplNodeRefContainer.get(0);

		// Call webscript on raw material with model filter
		String url = "/becpg/entity/datalists/data/node?entityNodeRef=" + rawMaterialNodeRef.toString()
				+ "&itemType=bcpg%3AcompoList&dataListName=WUsed";
		String filterDataStr = "{\\\"nested_bcpg_compoListProduct_assoc_bcpg_entityTplRef_added\\\":\\\"" + tplNodeRef.toString() + "\\\"}";
		String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"filterform\",\"filterData\":\"" + filterDataStr + "\"}}";
		logger.debug("url : " + url);
		logger.debug("data : " + data);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content : " + response.getContentAsString());
		org.junit.Assert.assertTrue("Should return matches using the model filter", response.getContentAsString().contains("Finished Product 2"));

		// Try with a non-matching model filter
		String nonMatchingFilterDataStr = "{\\\"nested_bcpg_compoListProduct_assoc_bcpg_entityTplRef_added\\\":\\\"workspace://SpacesStore/non-existent-node\\\"}";
		String dataNonMatching = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"filterform\",\"filterData\":\"" + nonMatchingFilterDataStr + "\"}}";
		Response responseNonMatching = TestWebscriptExecuters.sendRequest(new PostRequest(url, dataNonMatching, "application/json"), 200, "admin");
		org.junit.Assert.assertFalse("Should not return matches with non-matching model filter", responseNonMatching.getContentAsString().contains("Finished Product 2"));

	}

}
