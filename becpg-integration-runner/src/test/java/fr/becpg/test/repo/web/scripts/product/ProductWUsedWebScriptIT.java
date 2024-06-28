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
	 * @throws Exception the exception
	 */
	@Test
	public void testgetProductWused() throws Exception {

		inWriteTx(() -> {

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
			/*
			 * compoList.add( new CompoListDataItem(null, null, 1d, 0d, ProductUnit.kg, 0d,
			 * DeclarationType.Omit, lSFNodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Omit).withProduct(lSFNodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Omit, rawMaterialNodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(3d).withQtyUsed(0d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Omit)
					.withProduct(rawMaterialNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			logger.debug("local semi finished: " + lSFNodeRef);
			logger.debug("finishedProductNodeRef: " + finishedProductNodeRef);

			return null;

		});

		// Call webscript on raw material
		String url = "/becpg/entity/datalists/data/node?entityNodeRef=" + rawMaterialNodeRef.toString()
				+ "&itemType=bcpg%3AcompoList&dataListName=WUsed";
		String data = "{\"fields\":[\"bcpg_costListCost\",\"bcpg_costListValue\",\"bcpg_costListUnit\"],\"filter\":{\"filterId\":\"all\",\"filterData\":\"\"}}";
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200,
				"admin");
		logger.debug("content : " + response.getContentAsString());
		response = TestWebscriptExecuters.sendRequest(new PostRequest(url, data, "application/json"), 200, "admin");
		logger.debug("content : " + response.getContentAsString());

	}

}
