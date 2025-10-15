package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class EvaporatingLabelingFormulationIT extends AbstractFinishedProductTest {

	@Test
	public void testEvaporatedRate() {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true)
					.withLabeling(true).withIngredients(true).build();

			return testProduct.createTestProduct().getNodeRef();
		});

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"pâte à choux 45,5% (eau 32,5%, lait 6,5%, sucre 6,5%), crème pâtissière 36,4% (chocolat 9,1%, lait 7,3%, oeuf 7,3%, sucre 7,3%, farine 5,5%), nappage 18,2% (oeuf 7,3%, chocolat 5,5%, sucre 5,5%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"pâte à choux 45,5% (sucre 41,3%, lait 4,1%), crème pâtissière 36,4% (chocolat 9,1%, lait 7,3%, oeuf 7,3%, sucre 7,3%, farine 5,5%), nappage 18,2% (oeuf 7,3%, chocolat 5,5%, sucre 5,5%)",
				Locale.FRENCH);

	}

	@Test
	public void testDoNotPropagateYield() {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false)
					.withLabeling(false).withIngredients(true).build();
			testProduct.initCompoProduct();

			FinishedProductData biscuit = testProduct.createTestProduct();
			biscuit.setName("Crousti-Flow 💧🍪");
			biscuit.withQty(100d);

			SemiFinishedProductData bisCuiCui = SemiFinishedProductData.build().withName("BisCuiCui 🐦🐦🐦").withQty(80d).withUnit(ProductUnit.kg)
					.withCompoList(List.of(
							CompoListDataItem.build().withQtyUsed(15d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
									.withProduct(testProduct.getWaterNodeRef()),
							CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
									.withProduct(testProduct.getFlourNodeRef()),
							CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
									.withProduct(testProduct.getEggNodeRef()),
							CompoListDataItem.build().withQtyUsed(15d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
									.withProduct(testProduct.getSugarNodeRef())));

			NodeRef biscuicuiNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), bisCuiCui).getNodeRef();

			biscuit.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getWaterNodeRef()),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(biscuicuiNodeRef)));

			return alfrescoRepository.save(biscuit).getNodeRef();
		});

		Assert.assertNotNull(finishedProductNodeRef);

		checkILL(finishedProductNodeRef, new ArrayList<>(List.of(
				LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
				LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})").withLabelingRuleType(LabelingRuleType.Format),
				LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
						.withLabelingRuleType(LabelingRuleType.Prefs),
				LabelingRuleListDataItem.build().withName("doNotPropagateYield").withFormula("doNotPropagateYield=true")
						.withLabelingRuleType(LabelingRuleType.Prefs))),
				"eau 50%, oeuf 28,1%, farine 12,5%, sucre 9,4%", Locale.FRENCH);

		// Verify ingredient list quantities match labeling
		inReadTx(() -> {
			FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
			Assert.assertEquals("Should have 4 ingredients", 4, formulatedProduct.getIngList().size());

			// Expected: eau 50%, oeuf 28.1%, farine 12.5%, sucre 9.4%
			Assert.assertEquals("Water qtyPercWithYield should be 50%", 50.0, formulatedProduct.getIngList().get(0).getQtyPercWithYield(), 0.1);
			Assert.assertEquals("Egg qtyPercWithYield should be 28.1%", 28.1, formulatedProduct.getIngList().get(1).getQtyPercWithYield(), 0.1);
			Assert.assertEquals("Flour qtyPercWithYield should be 12.5%", 12.5, formulatedProduct.getIngList().get(2).getQtyPercWithYield(), 0.1);
			Assert.assertEquals("Sugar qtyPercWithYield should be 9.4%", 9.4, formulatedProduct.getIngList().get(3).getQtyPercWithYield(), 0.1);

			return null;
		});

	}

	@Test
	public void testMonoLevelWithoutEvap() {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false)
					.withLabeling(false).withIngredients(true).build();
			testProduct.initCompoProduct();

			FinishedProductData biscuit = testProduct.createTestProduct();
			biscuit.setName("testMonoLevelWithoutEvap - BisCuiCui 🐦");
			biscuit.withQty(80d);

			biscuit.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getWaterNodeRef()),
					CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getFlourNodeRef()),
					CompoListDataItem.build().withQtyUsed(10d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getSugarNodeRef())));

			return alfrescoRepository.save(biscuit).getNodeRef();
		});

		Assert.assertNotNull(finishedProductNodeRef);

		checkILL(finishedProductNodeRef,
				new ArrayList<>(List.of(
						LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
						LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
								.withLabelingRuleType(LabelingRuleType.Format),
						LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
								.withLabelingRuleType(LabelingRuleType.Prefs))),
				"farine 50%, eau 37,5%, sucre 12,5%", Locale.FRENCH);

		// Verify ingredient list quantities match labeling
		inReadTx(() -> {
			FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
			Assert.assertEquals("Should have 3 ingredients", 3, formulatedProduct.getIngList().size());

			// Expected: farine 50%, eau 37.5%, sucre 12.5%
			Assert.assertEquals("Water qtyPercWithYield should be 37.5%", 37.5, formulatedProduct.getIngList().get(0).getQtyPercWithYield(), 0.1);
			Assert.assertEquals("Flour qtyPercWithYield should be 50%", 50.0, formulatedProduct.getIngList().get(1).getQtyPercWithYield(), 0.1);
			Assert.assertEquals("Sugar qtyPercWithYield should be 12.5%", 12.5, formulatedProduct.getIngList().get(2).getQtyPercWithYield(), 0.1);

			return null;
		});

	}

	@Test
	public void testMonoLevelWithoutEvap2() {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false)
					.withLabeling(false).withIngredients(true).build();
			testProduct.initCompoProduct();

			FinishedProductData biscuit = testProduct.createTestProduct();
			biscuit.setName("testMonoLevelWithoutEvap2 - BisCuiCui 🐦");
			biscuit.withQty(80d);

			biscuit.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(10d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getWaterNodeRef()),
					CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getFlourNodeRef()),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getSugarNodeRef())));

			return alfrescoRepository.save(biscuit).getNodeRef();
		});

		Assert.assertNotNull(finishedProductNodeRef);

		checkILL(finishedProductNodeRef,
				new ArrayList<>(List.of(
						LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
						LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
								.withLabelingRuleType(LabelingRuleType.Format),
						LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
								.withLabelingRuleType(LabelingRuleType.Prefs))),
				"sucre 62,5%, farine 50%", Locale.FRENCH);

		// Verify ingredient list quantities match labeling
		inReadTx(() -> {
			FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
			Assert.assertEquals("Should have 3 ingredients", 3, formulatedProduct.getIngList().size());

			// Expected: sucre 62.5%, farine 50%
			Assert.assertEquals("Sugar qtyPercWithYield should be 62.5%", 62.5, formulatedProduct.getIngList().get(0).getQtyPercWithYield(), 0.1);
			Assert.assertEquals("Flour qtyPercWithYield should be 50%", 50.0, formulatedProduct.getIngList().get(1).getQtyPercWithYield(), 0.1);

			return null;
		});

	}

	@Test
	public void testMonoLevelWithoutEvap3() {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false)
					.withLabeling(false).withIngredients(true).build();
			testProduct.initCompoProduct();

			FinishedProductData biscuit = testProduct.createTestProduct();
			biscuit.setName("testMonoLevelWithoutEvap3 - BisCuiCui 🐦");
			biscuit.withQty(80d);

			biscuit.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getFlourNodeRef()),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(testProduct.getSugarNodeRef())));

			return alfrescoRepository.save(biscuit).getNodeRef();
		});

		Assert.assertNotNull(finishedProductNodeRef);

		checkILL(finishedProductNodeRef,
				new ArrayList<>(List.of(
						LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
						LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
								.withLabelingRuleType(LabelingRuleType.Format),
						LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
								.withLabelingRuleType(LabelingRuleType.Prefs))),
				"farine 62,5%, sucre 62,5%", Locale.FRENCH);

		// Verify ingredient list quantities match labeling
		inReadTx(() -> {
			FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
			Assert.assertEquals("Should have 2 ingredients", 2, formulatedProduct.getIngList().size());

			// Expected: farine 62.5%, sucre 62.5%
			Assert.assertEquals("Flour qtyPercWithYield should be 62.5%", 62.5, formulatedProduct.getIngList().get(0).getQtyPercWithYield(), 0.1);
			Assert.assertEquals("Sugar qtyPercWithYield should be 62.5%", 62.5, formulatedProduct.getIngList().get(1).getQtyPercWithYield(), 0.1);

			return null;
		});

	}

	@Test
	public void testMonoLevelEvap() {

		StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false).withLabeling(false).withIngredients(true)
				.build();

		inWriteTx(() -> {
			testProduct.initCompoProduct();
			return null;
		});

		try {
			final NodeRef finishedProductNodeRef = inWriteTx(() -> {

				FinishedProductData biscuit = testProduct.createTestProduct();
				biscuit.setName("testMonoLevelEvap - BisCuiCui 🐦");
				biscuit.withQty(80d);

				nodeService.setProperty(testProduct.getMilkNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 20d);
				nodeService.setProperty(testProduct.getIngMilkNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 20d);

				biscuit.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(10d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getWaterNodeRef()),
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getEggNodeRef()),
						CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getMilkNodeRef())));

				return alfrescoRepository.save(biscuit).getNodeRef();
			});

			Assert.assertNotNull(finishedProductNodeRef);

			checkILL(finishedProductNodeRef,
					new ArrayList<>(List.of(
							LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
									.withLabelingRuleType(LabelingRuleType.Format),
							LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
									.withLabelingRuleType(LabelingRuleType.Prefs))),
					"lait 54,2%, oeuf 45,8%", Locale.FRENCH);

			// Verify ingredient list quantities match labeling
			inReadTx(() -> {
				FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
				Assert.assertEquals("Should have 3 ingredients", 3, formulatedProduct.getIngList().size());

				// Expected: lait 54.2%, oeuf 45.8%
				Assert.assertEquals("Milk qtyPercWithYield should be 54.2%", 54.2, formulatedProduct.getIngList().get(0).getQtyPercWithYield(), 0.1);
				Assert.assertEquals("Egg qtyPercWithYield should be 45.8%", 45.8, formulatedProduct.getIngList().get(1).getQtyPercWithYield(), 0.1);

				return null;
			});
		} finally {
			// Revert properties
			inWriteTx(() -> {
				nodeService.setProperty(testProduct.getIngMilkNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 80d);
				return null;
			});
		}

	}

	/**
	 * Test for issue #21401 - Water evaporation distribution based on available water
	 *
	 * This test reproduces the scenario where:
	 * - Ingredients have different evaporation rates
	 * - Some ingredients have less water available than what the proportional rate would require
	 * - The fix ensures evaporation is distributed based on available water, not just rates
	 */
	@Test
	public void testEvaporationWithLimitedAvailableWater() {
		StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder().withAlfrescoRepository(alfrescoRepository)
				.withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(false).withLabeling(false).withIngredients(true)
				.build();

		inWriteTx(() -> {
			testProduct.initCompoProduct();
			return null;
		});

		try {
			final NodeRef finishedProductNodeRef = inWriteTx(() -> {
				FinishedProductData product = testProduct.createTestProduct();
				product.setName("Test #21401 - Limited Available Water");
				product.withQty(70d);

				// Set rates on raw materials
				nodeService.setProperty(testProduct.getFlourNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 13.5d);
				nodeService.setProperty(testProduct.getEggNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 88d);
				// Set rates on ingredients as well
				nodeService.setProperty(testProduct.getIngFlourNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 13.5d);
				nodeService.setProperty(testProduct.getIngEggNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 88d);

				product.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(9d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getFlourNodeRef()), // 9kg, 13.5% water
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getEggNodeRef()), // 20kg, 88% water
						CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getSugarNodeRef()), // 50kg, no water
						CompoListDataItem.build().withQtyUsed(38.46d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getWaterNodeRef()))); // 38.46kg, 100% water

				return alfrescoRepository.save(product).getNodeRef();
			});

			Assert.assertNotNull(finishedProductNodeRef);

			// New expected label based on corrected scenario
			checkILL(finishedProductNodeRef,
					new ArrayList<>(List.of(
							LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
									.withLabelingRuleType(LabelingRuleType.Format),
							LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
									.withLabelingRuleType(LabelingRuleType.Prefs))),
					"sucre 71,4%, oeuf 17,4%, farine 11,1%", Locale.FRENCH);

			// Verify ingredient list quantities match labeling
			inReadTx(() -> {
				FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
				Assert.assertEquals("Should have 4 ingredients", 4, formulatedProduct.getIngList().size());

				// Assertions for the new corrected scenario
				Assert.assertEquals("Sugar qtyPercWithYield should be 71.4%", 71.4, formulatedProduct.getIngList().get(0).getQtyPercWithYield(), 0.1);
					Assert.assertEquals("Flour qtyPercWithYield should be  17.4%",  17.4, formulatedProduct.getIngList().get(2).getQtyPercWithYield(), 0.1);
				Assert.assertEquals("Egg qtyPercWithYield should be 11,1%",11.1d, formulatedProduct.getIngList().get(3).getQtyPercWithYield(), 0.1);

				return null;
			});
		} finally {
			// Revert properties
			inWriteTx(() -> {
				nodeService.setProperty(testProduct.getIngFlourNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 0d);
				nodeService.setProperty(testProduct.getIngEggNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 10d);
				return null;
			});
		}
	}

}
