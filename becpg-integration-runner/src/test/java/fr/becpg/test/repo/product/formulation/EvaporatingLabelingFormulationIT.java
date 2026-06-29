package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.sample.CharactTestHelper;
import fr.becpg.repo.sample.StandardChocolateEclairTestProduct;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class EvaporatingLabelingFormulationIT extends AbstractFinishedProductTest {

	@Autowired
	protected AssociationService associationService;

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
					"lait 53,6%, oeuf 46,4%", Locale.FRENCH);

			// Verify ingredient list quantities match labeling
			inReadTx(() -> {
				FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
				Assert.assertEquals("Should have 3 ingredients", 3, formulatedProduct.getIngList().size());

				// Expected: lait 54.2%, oeuf 45.8%
				Assert.assertEquals("Milk qtyPercWithYield should be 53,6%",53.6, formulatedProduct.getIngList().get(0).getQtyPercWithYield(), 0.1);
				Assert.assertEquals("Egg qtyPercWithYield should be 46,4%", 46.4, formulatedProduct.getIngList().get(1).getQtyPercWithYield(), 0.1);

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
	 *  Water evaporation distribution based on available water
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
				product.setName("testEvaporationWithLimitedAvailableWater - BisCuiCui 🐦");
				product.withQty(70d);

				// Set rates on raw materials
				nodeService.setProperty(testProduct.getMilkNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 20d);
				nodeService.setProperty(testProduct.getIngMilkNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 20d);

				product.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getFlourNodeRef()), 
						CompoListDataItem.build().withQtyUsed(32d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getMilkNodeRef()),
						CompoListDataItem.build().withQtyUsed(5d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getEggNodeRef()), 
						CompoListDataItem.build().withQtyUsed(15d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getSugarNodeRef()), 
						CompoListDataItem.build().withQtyUsed(45d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(testProduct.getWaterNodeRef())));
				

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
					"lait 43,1%, farine 28,6%, sucre 21,4%, oeuf 6,9%", Locale.FRENCH);

			inReadTx(() -> {
				FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				Assert.assertNotNull("Ingredient list should not be null", formulatedProduct.getIngList());
				Assert.assertEquals("Should have 5 ingredients", 5, formulatedProduct.getIngList().size());

				Assert.assertEquals("Milk qtyPercWithYield should be 43.1%", 43.1, formulatedProduct.getIngList().get(1).getQtyPercWithYield(), 0.1);
				Assert.assertEquals("Egg qtyPercWithYield should be 6.94%",6.94d, formulatedProduct.getIngList().get(4).getQtyPercWithYield(), 0.1);

				return null;
			});
		} finally {
			// Revert properties
			inWriteTx(() -> {
				nodeService.setProperty(testProduct.getIngMilkNodeRef(), PLMModel.PROP_EVAPORATED_RATE, 90d);
				return null;
			});
		}
	}

	/**
	 * Reproduces #34702: a composite ingredient ("Tomate purée") whose sub-ingredient ("Tomate")
	 * carries an evaporation rate, used in a product that also contains free water which fully
	 * evaporates. The sum of the top-level "Qty with yield" percentages must stay at 100 %.
	 */
	@Test
	public void testSubIngredientEvaporation() {

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			NodeRef ingFlour = CharactTestHelper.getOrCreateIng(nodeService, "ING Flour 34702");
			NodeRef ingWater = CharactTestHelper.getOrCreateIng(nodeService, "ING Water 34702");
			NodeRef ingTomatoPuree = CharactTestHelper.getOrCreateIng(nodeService, "ING Tomato puree 34702");
			NodeRef ingTomato = CharactTestHelper.getOrCreateIng(nodeService, "ING Tomato 34702");

			// Free water fully evaporates; the tomato sub-ingredient partially evaporates
			nodeService.setProperty(ingWater, PLMModel.PROP_EVAPORATED_RATE, 100d);
			nodeService.setProperty(ingTomato, PLMModel.PROP_EVAPORATED_RATE, 50d);

			// Sub-product detailing "Tomato puree" -> "Tomato" (the evaporating child)
			RawMaterialData tomatoPureeSub = RawMaterialData.build().withName("RM Tomato puree sub 34702").withQty(100d)
					.withUnit(ProductUnit.kg).withIngList(List.of(buildIng(ingTomato, 100d)));
			NodeRef tomatoPureeSubNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), tomatoPureeSub).getNodeRef();

			RawMaterialData tomatoPuree = RawMaterialData.build().withName("RM Tomato puree 34702").withQty(100d).withUnit(ProductUnit.kg)
					.withIngList(List.of(buildIng(ingTomatoPuree, 100d)));
			NodeRef tomatoPureeNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), tomatoPuree).getNodeRef();
			associationService.update(ingTomatoPuree, BeCPGModel.ASSOC_PARENT_ENTITY, List.of(tomatoPureeSubNodeRef));

			RawMaterialData flour = RawMaterialData.build().withName("RM Flour 34702").withQty(100d).withUnit(ProductUnit.kg)
					.withIngList(List.of(buildIng(ingFlour, 100d)));
			NodeRef flourNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), flour).getNodeRef();

			RawMaterialData water = RawMaterialData.build().withName("RM Water 34702").withQty(100d).withUnit(ProductUnit.kg)
					.withIngList(List.of(buildIng(ingWater, 100d)));
			NodeRef waterNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), water).getNodeRef();

			// Finished product: 100 kg in, 80 kg net -> yield 80 %, 20 kg evaporated
			// (15 kg free water + 5 kg from the tomato sub-ingredient)
			FinishedProductData fp = FinishedProductData.build().withName("FP Pizza 34702").withUnit(ProductUnit.kg).withQty(80d)
					.withCompoList(List.of(
							CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
									.withProduct(flourNodeRef),
							CompoListDataItem.build().withQtyUsed(45d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
									.withProduct(tomatoPureeNodeRef),
							CompoListDataItem.build().withQtyUsed(15d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
									.withProduct(waterNodeRef)));

			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(finishedProductNodeRef);
			return null;
		});

		inReadTx(() -> {
			FinishedProductData formulatedProduct = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			List<IngListDataItem> ingList = formulatedProduct.getIngList();
			Assert.assertNotNull("Ingredient list should not be null", ingList);

			double topLevelWithYield = 0d;
			for (IngListDataItem item : ingList) {
				if ((item.getParent() == null) && (item.getQtyPercWithYield() != null)) {
					topLevelWithYield += item.getQtyPercWithYield();
				}
			}

			IngListDataItem tomatoPuree = findIngByName(ingList, "ING Tomato puree 34702");
			IngListDataItem tomato = findIngByName(ingList, "ING Tomato 34702");
			Assert.assertNotNull("Tomato puree ingredient missing", tomatoPuree);
			Assert.assertNotNull("Tomato sub-ingredient missing", tomato);
			Assert.assertNotNull("Tomato should be a child of Tomato puree", tomato.getParent());

			// 100 kg in, 80 kg net; 15 kg free water + 5 kg tomato water evaporate -> 40 kg flour, 40 kg tomato out of 80 kg
			Assert.assertEquals("Sum of top-level Qty with yield should be 100%", 100d, topLevelWithYield, 0.1);
			Assert.assertEquals("Tomato puree Qty with yield should be 50%", 50d, tomatoPuree.getQtyPercWithYield(), 0.2);
			Assert.assertEquals("Tomato (child) Qty with yield should be 50%", 50d, tomato.getQtyPercWithYield(), 0.2);
			Assert.assertEquals("Parent Qty with yield should equal the sum of its children", tomatoPuree.getQtyPercWithYield(),
					tomato.getQtyPercWithYield(), 0.1);
			return null;
		});
	}

	private IngListDataItem findIngByName(List<IngListDataItem> ingList, String charactName) {
		for (IngListDataItem item : ingList) {
			if ((item.getIng() != null) && charactName.equals(nodeService.getProperty(item.getIng(), BeCPGModel.PROP_CHARACT_NAME))) {
				return item;
			}
		}
		return null;
	}

	private IngListDataItem buildIng(NodeRef ingNodeRef, double qtyPerc) {
		IngListDataItem item = IngListDataItem.build().withQtyPerc(qtyPerc).withIngredient(ingNodeRef);
		item.setQtyPercWithYield(qtyPerc);
		return item;
	}

}
