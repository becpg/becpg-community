package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.test.repo.StandardChocolateEclairTestProduct;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class EvaporatingLabelingFormulationIT extends AbstractFinishedProductTest {

	@Test
	public void testEvaporatedRate() {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true)
					.withLabeling(true).build();

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
				LabelingRuleListDataItem.build().withName("Rendu as HTML").withFormula("renderAsHtmlTable()")
						.withLabelingRuleType(LabelingRuleType.Render),
				LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})").withLabelingRuleType(LabelingRuleType.Format),
				LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
						.withLabelingRuleType(LabelingRuleType.Prefs),
				LabelingRuleListDataItem.build().withName("doNotPropagateYield").withFormula("doNotPropagateYield=true")
						.withLabelingRuleType(LabelingRuleType.Prefs))),
				" Eau 50%, Oeuf 28,1%, Farine 12,5%, Sucre 9,4%", Locale.FRENCH);

	}

}
