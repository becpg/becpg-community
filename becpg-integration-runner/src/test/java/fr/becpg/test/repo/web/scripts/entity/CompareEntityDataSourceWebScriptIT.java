package fr.becpg.test.repo.web.scripts.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.test.repo.product.AbstractCompareProductTest;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class CompareEntityDataSourceWebscriptIT.
 *
 * @author querephi, kevin
 */
public class CompareEntityDataSourceWebScriptIT extends AbstractCompareProductTest {

	private static final Log logger = LogFactory.getLog(CompareEntityDataSourceWebScriptIT.class);

	@Test
	public void testGetProductsXmlDatasource() throws IOException {
		inWriteTx(() -> {
			FinishedProductData fp1 = new FinishedProductData();
			fp1.setName("FP 1");

			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * compoList.add(new CompoListDataItem(null, null, 1d, 0d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(1d).withQtyUsed(0d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail)
					.withProduct(rawMaterial2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, 1d, 0d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(3d).withQtyUsed(0d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial3NodeRef));

			fp1.getCompoListView().setCompoList(compoList);

			fp1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

			FinishedProductData fp2 = new FinishedProductData();
			fp2.setName("FP 2");

			compoList = new ArrayList<>();
			/*
			 * compoList.add(new CompoListDataItem(null, null, 1d, 0d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial1NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQty(2d).withQtyUsed(0d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail)
					.withProduct(rawMaterial2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, null, 1d, 0d, ProductUnit.kg, 0d,
			 * DeclarationType.Detail, localSF2NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(0d).withUnit(ProductUnit.kg)
					.withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(3), 2d, 0d,
			 * ProductUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(2d).withQtyUsed(0d)
					.withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial3NodeRef));
			/*
			 * compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d,
			 * ProductUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQty(3d).withQtyUsed(0d)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail)
					.withProduct(rawMaterial4NodeRef));

			fp2.getCompoListView().setCompoList(compoList);

			fp2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), fp2).getNodeRef();

			return null;
		});
		String url = String.format("/becpg/entity/compare/datasource?entity=%s&entities=%s", fp1NodeRef.toString(),
				fp2NodeRef.toString());

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		String rep = response.getContentAsString();

		Assert.assertNotNull(response);
		logger.debug("response type: " + response.getContentType());

		inReadTx(() -> {
			FinishedProductData fp1 = (FinishedProductData) alfrescoRepository.findOne(fp1NodeRef);
			FinishedProductData fp2 = (FinishedProductData) alfrescoRepository.findOne(fp2NodeRef);

			Assert.assertTrue(rep.contains("entity1=\"" + fp1.getName() + "\""));
			Assert.assertTrue(rep.contains("entity2=\"" + fp2.getName() + "\""));

			return null;
		});
	}

	@Test
	public void testGetRawMaterialsXmlDatasource() throws IOException {
		String url = String.format("/becpg/entity/compare/datasource?entity=%s&entities=%s",
				rawMaterial1NodeRef.toString(), rawMaterial2NodeRef.toString() + "," + rawMaterial3NodeRef.toString()
						+ "," + rawMaterial4NodeRef.toString());

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		String rep = response.getContentAsString();

		Assert.assertNotNull(response);
		logger.debug("response type: " + response.getContentType());

		inReadTx(() -> {
			RawMaterialData rawMaterial1 = (RawMaterialData) alfrescoRepository.findOne(rawMaterial1NodeRef);
			RawMaterialData rawMaterial2 = (RawMaterialData) alfrescoRepository.findOne(rawMaterial2NodeRef);
			RawMaterialData rawMaterial3 = (RawMaterialData) alfrescoRepository.findOne(rawMaterial3NodeRef);
			RawMaterialData rawMaterial4 = (RawMaterialData) alfrescoRepository.findOne(rawMaterial4NodeRef);

			Assert.assertTrue(rep.contains("entity1=\"" + rawMaterial1.getName() + "\""));
			Assert.assertTrue(rep.contains("entity2=\"" + rawMaterial2.getName() + "\""));
			Assert.assertTrue(rep.contains("entity3=\"" + rawMaterial3.getName() + "\""));
			Assert.assertTrue(rep.contains("entity4=\"" + rawMaterial4.getName() + "\""));

			return null;
		});
	}
}
