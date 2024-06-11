package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class EvaporatingLabelingFormulationIT extends AbstractFinishedProductTest {

	protected NodeRef pateChouxNodeRef;
	protected NodeRef cremePatissiereNodeRef;
	protected NodeRef nappageNodeRef;
	protected NodeRef waterNodeRef;
	protected NodeRef milkNodeRef;
	protected NodeRef sugarNodeRef;
	protected NodeRef flourNodeRef;
	protected NodeRef eggNodeRef;
	protected NodeRef chocolateNodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initPart();
	}

	private void initPart() {
		logger.info("Initializing test data");
		inWriteTx(() -> {
			createCompoProducts();
			return null;
		});
	}

	private FinishedProductData createFinishedProduct() {
		return FinishedProductData.build().withName("Éclair au chocolat").withUnit(ProductUnit.kg).withQty(550d).withDensity(1d)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(250d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
								.withProduct(pateChouxNodeRef),
						CompoListDataItem.build().withQtyUsed(200d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
								.withProduct(cremePatissiereNodeRef),
						CompoListDataItem.build().withQtyUsed(100d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
								.withProduct(nappageNodeRef)))
				.withLabelingRuleList(List.of(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render),
						new LabelingRuleListDataItem("Rendu as HTML", "renderAsHtmlTable()", LabelingRuleType.Render),
						new LabelingRuleListDataItem("%", "{0} {1,number,0.#%} ({2})", LabelingRuleType.Format, null, null),
						new LabelingRuleListDataItem("Param1", "ingsLabelingWithYield=true", LabelingRuleType.Prefs, null, null)));
	}

	private void createCompoProducts() {
		// Creating raw materials
		RawMaterialData water = RawMaterialData.build().withName("Eau").withQty(100d).withUnit(ProductUnit.kg);
		waterNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), water).getNodeRef();

		nodeService.addAspect(waterNodeRef, PLMModel.ASPECT_WATER, new HashMap<>());

		RawMaterialData milk = RawMaterialData.build().withName("Lait").withQty(20d).withUnit(ProductUnit.kg);
		milkNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), milk).getNodeRef();

		nodeService.setProperty(milkNodeRef, PLMModel.PROP_EVAPORATED_RATE, 90d);

		RawMaterialData sugar = RawMaterialData.build().withName("Sucre").withQty(20d).withUnit(ProductUnit.kg);
		sugarNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sugar).getNodeRef();

		RawMaterialData flour = RawMaterialData.build().withName("Farine").withQty(30d).withUnit(ProductUnit.kg);
		flourNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), flour).getNodeRef();

		RawMaterialData egg = RawMaterialData.build().withName("Oeuf").withQty(40d).withUnit(ProductUnit.kg);
		eggNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), egg).getNodeRef();

		RawMaterialData chocolate = RawMaterialData.build().withName("Chocolat").withQty(50d).withUnit(ProductUnit.kg);
		chocolateNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), chocolate).getNodeRef();

		// Creating semi-finished product (Pâte à choux)
		SemiFinishedProductData pateChoux = SemiFinishedProductData.build().withName("Pâte à choux").withQty(22d).withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(100d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(waterNodeRef),
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(milkNodeRef),
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef))
						).withLabelingRuleList(List.of(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render),
								new LabelingRuleListDataItem("Rendu as HTML", "renderAsHtmlTable()", LabelingRuleType.Render),
								new LabelingRuleListDataItem("%", "{0} {1,number,0.#%} ({2})", LabelingRuleType.Format, null, null),
								new LabelingRuleListDataItem("Param1", "ingsLabelingWithYield=true", LabelingRuleType.Prefs, null, null)));

		pateChouxNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), pateChoux).getNodeRef();

		// Creating semi-finished product (Crème pâtissière)
		SemiFinishedProductData cremePatissiere = SemiFinishedProductData.build().withName("Crème pâtissière").withQty(200d).withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef),
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(milkNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(flourNodeRef),
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(eggNodeRef),
						CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(chocolateNodeRef)));

		cremePatissiereNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), cremePatissiere).getNodeRef();

		// Creating semi-finished product (Nappage)
		SemiFinishedProductData nappage = SemiFinishedProductData.build().withName("Nappage").withQty(100d).withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(eggNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(chocolateNodeRef)));

		nappageNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), nappage).getNodeRef();

	}

	@Test
	public void testEvaporatedRate() throws Exception {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.info("Creating finished product");
			FinishedProductData finishedProduct = createFinishedProduct();
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%} ({2})", LabelingRuleType.Format, null, null));

		checkILL(finishedProductNodeRef, labelingRuleList, "pâte à choux 45,5% (eau 32,5%, lait 6,5%, sucre 6,5%), crème pâtissière 36,4% (chocolat 9,1%, lait 7,3%, oeuf 7,3%, sucre 7,3%, farine 5,5%), nappage 18,2% (oeuf 7,3%, chocolat 5,5%, sucre 5,5%)", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%} ({2})", LabelingRuleType.Format, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "ingsLabelingWithYield=true", LabelingRuleType.Prefs, null, null));
		
		checkILL(finishedProductNodeRef, labelingRuleList, "pâte à choux 45,5% (sucre 41,3%, lait 4,1%), crème pâtissière 36,4% (chocolat 9,1%, lait 7,3%, oeuf 7,3%, sucre 7,3%, farine 5,5%), nappage 18,2% (oeuf 7,3%, chocolat 5,5%, sucre 5,5%)", Locale.FRENCH);

		
	}
}
