package fr.becpg.test.repo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LogisticUnitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.impl.SurveyServiceImpl.ResponseType;
import fr.becpg.test.utils.CharactTestHelper;

public class StandardChocolateEclairTestProduct extends StandardProductBuilder {

	public static final String WATER_NAME = "Eau";
	public static final String MILK_NAME = "Lait";
	public static final String SUGAR_NAME = "Sucre";
	public static final String SUGAR_SUPPLIER_1_NAME = "Sucre - Fournisseur 1";
	public static final String SUGAR_SUPPLIER_2_NAME = "Sucre - Fournisseur 2";
	public static final String SUGAR_SUPPLIER_3_NAME = "Sucre - Fournisseur 3";
	public static final String SUGAR_GEN_USINE_1_NAME = "Sucre - GEN - Usine 1";
	public static final String SUGAR_GEN_USINE_2_NAME = "Sucre - GEN - Usine 2";
	public static final String FLOUR_NAME = "Farine";
	public static final String EGG_NAME = "Oeuf";
	public static final String CHOCOLATE_NAME = "Chocolat";
	public static final String PATE_CHOUX_NAME = "Pâte à choux";
	public static final String CREME_PATISSIERE_NAME = "Crème pâtissière";
	public static final String NAPPAGE_NAME = "Nappage";
	public static final String SUPPLIER_1 = "Fournisseur 1";
	public static final String SUPPLIER_2 = "Fournisseur 2";
	public static final String SUPPLIER_3 = "Fournisseur 3";
	public static final String LABORATORY_1 = "Laboratoire 1";
	public static final String LABORATORY_2 = "Laboratoire 2";
	public static final String PLANT_USINE_1 = "Usine 1";
	public static final String PLANT_USINE_2 = "Usine 2";
	public static final String PASTRY_QUALITY = "Pastry quality";
	public static final String CCP_COMPLIANCE = "CCP compliance";
	public static final String FILLING_QUALITY = "Filling quality";

	protected NodeRef pateChouxNodeRef;
	protected NodeRef cremePatissiereNodeRef;
	protected NodeRef nappageNodeRef;

	protected NodeRef sugarPlants1NodeRef;
	protected NodeRef sugarPlants2NodeRef;
	protected NodeRef sugarSupplier1NodeRef;
	protected NodeRef sugarSupplier2NodeRef;
	protected NodeRef sugarSupplier3NodeRef;

	protected NodeRef waterNodeRef;
	protected NodeRef milkNodeRef;
	protected NodeRef sugarNodeRef;
	protected NodeRef flourNodeRef;
	protected NodeRef eggNodeRef;
	protected NodeRef chocolateNodeRef;

	protected NodeRef ingWaterNodeRef;
	protected NodeRef ingMilkNodeRef;
	protected NodeRef ingSugarNodeRef;
	protected NodeRef ingFlourNodeRef;
	protected NodeRef ingEggNodeRef;
	protected NodeRef ingChocolateNodeRef;

	public NodeRef getPateChouxNodeRef() {
		return pateChouxNodeRef;
	}

	public NodeRef getCremePatissiereNodeRef() {
		return cremePatissiereNodeRef;
	}

	public NodeRef getNappageNodeRef() {
		return nappageNodeRef;
	}

	public NodeRef getWaterNodeRef() {
		return waterNodeRef;
	}

	public NodeRef getMilkNodeRef() {
		return milkNodeRef;
	}

	public NodeRef getSugarNodeRef() {
		return sugarNodeRef;
	}

	public NodeRef getSugarPlants1NodeRef() {
		return sugarPlants1NodeRef;
	}

	public NodeRef getSugarPlants2NodeRef() {
		return sugarPlants2NodeRef;
	}

	public NodeRef getSugarSupplier1NodeRef() {
		return sugarSupplier1NodeRef;
	}

	public NodeRef getSugarSupplier2NodeRef() {
		return sugarSupplier2NodeRef;
	}

	public NodeRef getSugarSupplier3NodeRef() {
		return sugarSupplier3NodeRef;
	}

	public NodeRef getFlourNodeRef() {
		return flourNodeRef;
	}

	public NodeRef getEggNodeRef() {
		return eggNodeRef;
	}

	public NodeRef getChocolateNodeRef() {
		return chocolateNodeRef;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public NodeRef getDestFolder() {
		return destFolder;
	}

	private boolean isWithCompo = true;
	private boolean isWithLabeling = true;
	private boolean isWithGenericRawMaterial = true;
	private boolean isWithStocks = true;
	private boolean isWithIngredients = false;
	private boolean isWithSurvey = false;
	private boolean isWithScoreList = false;

	// Private constructor to enforce usage of the builder
	private StandardChocolateEclairTestProduct(Builder builder) {
		super(builder);
		this.isWithCompo = builder.isWithCompo;
		this.isWithLabeling = builder.isWithLabeling;
		this.isWithGenericRawMaterial = builder.isWithGenericRawMaterial;
		this.isWithStocks = builder.isWithStocks;
		this.isWithIngredients = builder.isWithIngredients;
		this.isWithSurvey = builder.isWithSurvey;
		this.isWithScoreList = builder.isWithScoreList;
	}

	// Static inner Builder class
	public static class Builder extends StandardProductBuilder.Builder<Builder> {
		private boolean isWithCompo = true;
		private boolean isWithLabeling = true;
		private boolean isWithGenericRawMaterial = true;
		private boolean isWithStocks = true;
		private boolean isWithIngredients = false;
		private boolean isWithSurvey = false;
		private boolean isWithScoreList = false;
		
		
		public Builder withSurvey(boolean isWithSurvey) {
			this.isWithSurvey = isWithSurvey;
			return this;
		}
		
		public Builder withScoreList(boolean isWithScoreList) {
			this.isWithScoreList = isWithScoreList;
			return this;
		}
		

		public Builder withCompo(boolean isWithCompo) {
			this.isWithCompo = isWithCompo;
			return this;
		}

		public Builder withLabeling(boolean isWithLabeling) {
			this.isWithLabeling = isWithLabeling;
			return this;
		}

		public Builder withGenericRawMaterial(boolean isWithGenericRawMaterial) {
			this.isWithGenericRawMaterial = isWithGenericRawMaterial;
			return this;
		}

		public Builder withStocks(boolean isWithStocks) {
			this.isWithStocks = isWithStocks;
			return this;
		}

		public Builder withIngredients(boolean isWithIngredients) {
			this.isWithIngredients = isWithIngredients;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public StandardChocolateEclairTestProduct build() {
			return new StandardChocolateEclairTestProduct(this);
		}
	}

	@Override
	public FinishedProductData createTestProduct() {
		FinishedProductData finishedProduct = FinishedProductData.build().withName("Éclair au chocolat").withUnit(ProductUnit.kg).withQty(550d)
				.withDensity(1d);

		if (isWithCompo) {

			if (pateChouxNodeRef == null) {
				initCompoProduct();
			}

			finishedProduct = finishedProduct.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(250d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(pateChouxNodeRef),
					CompoListDataItem.build().withQtyUsed(200d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(cremePatissiereNodeRef),
					CompoListDataItem.build().withQtyUsed(100d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(nappageNodeRef)));

		}

		if (isWithLabeling) {

			finishedProduct = finishedProduct.withLabelingRuleList(
					List.of(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("Rendu as HTML").withFormula("renderAsHtmlTable()")
									.withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
									.withLabelingRuleType(LabelingRuleType.Format),
							LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
									.withLabelingRuleType(LabelingRuleType.Prefs)));

		}
		
		if(isWithSurvey) {
			finishedProduct.withSurveyList(createEclairQMSSurveyList());
		}
		
		if(isWithScoreList) {
			finishedProduct.withScoreList(createScoreList());
		}

		alfrescoRepository.create(destFolder, finishedProduct);

		return finishedProduct;

	}

	public LogisticUnitData createCaseLogisticUnit() {
		FinishedProductData eclairAuChocolat = createTestProduct();

		PackagingMaterialData packagingMaterialData = PackagingMaterialData.build().withName("Colis en carton");
		NodeRef packagingMaterialNodeRef = alfrescoRepository.create(destFolder, packagingMaterialData).getNodeRef();

		LogisticUnitData logisticUnitData = LogisticUnitData.build().withName("Colis d'éclairs au chocolat").withSecondaryWidth(200d)
				.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
						.withProduct(eclairAuChocolat.getNodeRef())))
				.withPackagingList(List.of(PackagingListDataItem.build().withQty(500d).withUnit(ProductUnit.g).withPkgLevel(PackagingLevel.Secondary)
						.withProduct(packagingMaterialNodeRef)));

		alfrescoRepository.create(destFolder, logisticUnitData);

		return logisticUnitData;
	}

	public LogisticUnitData createPalletLogisticUnit(NodeRef caseNodeRef) {
		PackagingMaterialData packagingMaterialData = PackagingMaterialData.build().withName("Palette en bois");
		NodeRef packagingMaterialNodeRef = alfrescoRepository.create(destFolder, packagingMaterialData).getNodeRef();
		LogisticUnitData palletLogisticUnit =

				LogisticUnitData.build().withName("Palette d'éclairs au chocolat").withTertiaryWidth(500d)
						.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
								.withDeclarationType(DeclarationType.Detail).withProduct(caseNodeRef)))
						.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.kg)
								.withPkgLevel(PackagingLevel.Tertiary).withProduct(packagingMaterialNodeRef)));

		alfrescoRepository.create(destFolder, palletLogisticUnit);

		return palletLogisticUnit;
	}

	public void initCompoProduct() {
		// Creating raw materials
		RawMaterialData water = RawMaterialData.build().withName(WATER_NAME).withQty(100d).withUnit(ProductUnit.kg);

		if (isWithStocks) {
			addStocks(water, 1000d, new ArrayList<>());
		}

		if (isWithIngredients) {
			initIngredients();
			water.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingWaterNodeRef)));
		}

		waterNodeRef = alfrescoRepository.create(destFolder, water).getNodeRef();
		nodeService.addAspect(waterNodeRef, PLMModel.ASPECT_WATER, new HashMap<>());

		RawMaterialData milk = RawMaterialData.build().withName(MILK_NAME).withQty(20d).withUnit(ProductUnit.kg);

		if (isWithStocks) {
			addStocks(milk, 1000d, new ArrayList<>());
		}

		milkNodeRef = alfrescoRepository.create(destFolder, milk).getNodeRef();

		nodeService.setProperty(milkNodeRef, PLMModel.PROP_EVAPORATED_RATE, 90d);

		RawMaterialData sugar = RawMaterialData.build().withName(SUGAR_NAME).withQty(20d).withUnit(ProductUnit.kg);

		if (isWithGenericRawMaterial) {
			RawMaterialData sugarSupplier1 = RawMaterialData.build().withName(SUGAR_SUPPLIER_1_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_1, PLMModel.TYPE_PLANT)));
			sugarSupplier1.setSuppliers(List.of(getOrCreateCharact(SUPPLIER_1, PLMModel.TYPE_SUPPLIER)));

			if (isWithIngredients) {
				sugarSupplier1.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingSugarNodeRef)));
			}

			RawMaterialData sugarSupplier2 = RawMaterialData.build().withName(SUGAR_SUPPLIER_2_NAME).withUnit(ProductUnit.kg).withPlants(
					List.of(getOrCreateCharact(PLANT_USINE_1, PLMModel.TYPE_PLANT), getOrCreateCharact(PLANT_USINE_2, PLMModel.TYPE_PLANT)));
			sugarSupplier2.setSuppliers(List.of(getOrCreateCharact(SUPPLIER_2, PLMModel.TYPE_SUPPLIER)));

			if (isWithIngredients) {
				sugarSupplier2.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingSugarNodeRef)));
			}

			RawMaterialData sugarSupplier3 = RawMaterialData.build().withName(SUGAR_SUPPLIER_3_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_2, PLMModel.TYPE_PLANT)));
			sugarSupplier3.setSuppliers(List.of(getOrCreateCharact(SUPPLIER_3, PLMModel.TYPE_SUPPLIER)));

			if (isWithIngredients) {
				sugarSupplier3.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingSugarNodeRef)));
			}

			if (isWithStocks) {
				addStocks(sugarSupplier1, 100d, List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY),
						getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
				addStocks(sugarSupplier2, 100d, List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY)));
				addStocks(sugarSupplier3, 100d, List.of(getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
			}

			sugarSupplier1NodeRef = alfrescoRepository.create(destFolder, sugarSupplier1).getNodeRef();
			sugarSupplier2NodeRef = alfrescoRepository.create(destFolder, sugarSupplier2).getNodeRef();
			sugarSupplier3NodeRef = alfrescoRepository.create(destFolder, sugarSupplier3).getNodeRef();

			RawMaterialData sugarGen1 = RawMaterialData.build().withName(SUGAR_GEN_USINE_1_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_1, PLMModel.TYPE_PLANT)));

			sugarGen1.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier1NodeRef),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier2NodeRef)));

			sugarPlants1NodeRef = alfrescoRepository.create(destFolder, sugarGen1).getNodeRef();

			RawMaterialData sugarGen2 = RawMaterialData.build().withName(SUGAR_GEN_USINE_2_NAME).withUnit(ProductUnit.kg)
					.withPlants(List.of(getOrCreateCharact(PLANT_USINE_2, PLMModel.TYPE_PLANT)));

			sugarGen2.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier2NodeRef),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarSupplier3NodeRef)));

			sugarPlants2NodeRef = alfrescoRepository.create(destFolder, sugarGen2).getNodeRef();

			sugar.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarPlants1NodeRef),
					CompoListDataItem.build().withQtyUsed(50d).withUnit(ProductUnit.Perc).withProduct(sugarPlants2NodeRef)));
		}

		sugarNodeRef = alfrescoRepository.create(destFolder, sugar).getNodeRef();

		RawMaterialData flour = RawMaterialData.build().withName(FLOUR_NAME).withQty(30d).withUnit(ProductUnit.kg);

		if (isWithIngredients) {
			flour.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingFlourNodeRef)));
		}

		if (isWithStocks) {
			addStocks(flour, 100d,
					List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY), getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
		}

		flourNodeRef = alfrescoRepository.create(destFolder, flour).getNodeRef();

		RawMaterialData egg = RawMaterialData.build().withName(EGG_NAME).withQty(40d).withUnit(ProductUnit.kg);

		if (isWithIngredients) {
			egg.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingEggNodeRef)));
		}

		if (isWithStocks) {
			addStocks(egg, 100d,
					List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY), getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
		}

		eggNodeRef = alfrescoRepository.create(destFolder, egg).getNodeRef();

		RawMaterialData chocolate = RawMaterialData.build().withName(CHOCOLATE_NAME).withQty(50d).withUnit(ProductUnit.kg);

		if (isWithIngredients) {
			chocolate.withIngList(List.of(IngListDataItem.build().withQtyPerc(100d).withIngredient(ingChocolateNodeRef)));
		}

		if (isWithStocks) {
			addStocks(chocolate, 100d,
					List.of(getOrCreateCharact(LABORATORY_1, PLMModel.TYPE_LABORATORY), getOrCreateCharact(LABORATORY_2, PLMModel.TYPE_LABORATORY)));
		}

		chocolateNodeRef = alfrescoRepository.create(destFolder, chocolate).getNodeRef();

		// Creating semi-finished product (Pâte à choux)
		SemiFinishedProductData pateChoux = SemiFinishedProductData.build().withName(PATE_CHOUX_NAME).withQty(22d).withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(100d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(waterNodeRef),
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(milkNodeRef),
						CompoListDataItem.build().withQtyUsed(20d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef)));

		if (isWithLabeling) {
			pateChoux = pateChoux.withLabelingRuleList(
					List.of(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("Rendu as HTML").withFormula("renderAsHtmlTable()")
									.withLabelingRuleType(LabelingRuleType.Render),
							LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
									.withLabelingRuleType(LabelingRuleType.Format),
							LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
									.withLabelingRuleType(LabelingRuleType.Prefs)));
		}

		pateChouxNodeRef = alfrescoRepository.create(destFolder, pateChoux).getNodeRef();

		// Creating semi-finished product (Crème pâtissière)
		SemiFinishedProductData cremePatissiere = SemiFinishedProductData.build().withName(CREME_PATISSIERE_NAME).withQty(200d)
				.withUnit(ProductUnit.kg)
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

		cremePatissiereNodeRef = alfrescoRepository.create(destFolder, cremePatissiere).getNodeRef();

		// Creating semi-finished product (Nappage)
		SemiFinishedProductData nappage = SemiFinishedProductData.build().withName(NAPPAGE_NAME).withQty(100d).withUnit(ProductUnit.kg)
				.withCompoList(List.of(
						CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(eggNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(sugarNodeRef),
						CompoListDataItem.build().withQtyUsed(30d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
								.withProduct(chocolateNodeRef)));

		nappageNodeRef = alfrescoRepository.create(destFolder, nappage).getNodeRef();
	}

	private void initIngredients() {

		ingWaterNodeRef = CharactTestHelper.getOrCreateIng(nodeService,WATER_NAME);

		nodeService.addAspect(ingWaterNodeRef, PLMModel.ASPECT_WATER, new HashMap<>());

		ingMilkNodeRef = CharactTestHelper.getOrCreateIng(nodeService,MILK_NAME);

		nodeService.setProperty(ingMilkNodeRef, PLMModel.PROP_EVAPORATED_RATE, 90d);

		ingSugarNodeRef = CharactTestHelper.getOrCreateIng(nodeService,SUGAR_NAME);
		ingFlourNodeRef = CharactTestHelper.getOrCreateIng(nodeService,FLOUR_NAME);
		ingEggNodeRef = CharactTestHelper.getOrCreateIng(nodeService,EGG_NAME);

		nodeService.setProperty(ingEggNodeRef, PLMModel.PROP_EVAPORATED_RATE, 10d);

		ingChocolateNodeRef = CharactTestHelper.getOrCreateIng(nodeService,CHOCOLATE_NAME);

	}

	private void addStocks(RawMaterialData rawMaterial, Double qty, List<NodeRef> laboratories) {

		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_WEEK, 10);

		List<StockListDataItem> stocks = new ArrayList<>();

		for (NodeRef laboratory : laboratories) {
			StockListDataItem stockListDataItem = new StockListDataItem();
			stockListDataItem.setBatchId(rawMaterial.getName() + " - " + nodeService.getProperty(laboratory, ContentModel.PROP_NAME) + "-"
					+ now.get(Calendar.DAY_OF_WEEK));
			stockListDataItem.setLaboratories(List.of(laboratory));
			stockListDataItem.setUseByDate(now.getTime());
			stockListDataItem.setBatchQty(qty);
			stocks.add(stockListDataItem);
		}

		now.add(Calendar.DAY_OF_WEEK, 5);

		StockListDataItem stockListDataItem = new StockListDataItem();
		stockListDataItem.setBatchId(rawMaterial.getName() + " - " + now.get(Calendar.DAY_OF_WEEK));
		stockListDataItem.setUseByDate(now.getTime());
		stockListDataItem.setBatchQty(qty / 10);
		stocks.add(stockListDataItem);

		rawMaterial.setStockList(stocks);

	}

	Map<Pair<QName, String>, NodeRef> characts = new HashMap<>();

	public NodeRef getOrCreateCharact(String name, QName type) {

		return characts.computeIfAbsent(new Pair<>(type, name), p -> {
			Map<QName, Serializable> prop = Map.of(ContentModel.PROP_NAME, name);

			return nodeService
					.createNode(destFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) prop.get(ContentModel.PROP_NAME)), type, prop)
					.getChildRef();

		});
	}
	
	private List<SurveyListDataItem> createEclairQMSSurveyList() {
	    // Question 1: Choux Pastry Quality Check
	    final SurveyQuestion question1 = getOrCreateSurveyQuestion(
	        "Evaluate the choux pastry characteristics:",
	        PASTRY_QUALITY,
	        ResponseType.multiChoicelist.name(),
	        null
	    );

	    getOrCreateSurveyAnswer(
	        question1,
	        "Perfect - Golden brown, hollow, crisp exterior (18-20cm length)",
	        100d
	    );

	    final SurveyQuestion q1Answer1 =  getOrCreateSurveyAnswer(
	        question1,
	        "Minor defects - Slight color variation, size within 17-21cm",
	        20d
	    );

	    getOrCreateSurveyAnswer(
	        question1,
	        "Major defects - Improper rise, inconsistent texture",
	        0d
	    );

	    final SurveyListDataItem survey1 = new SurveyListDataItem();
	    survey1.setQuestion(question1.getNodeRef());
	    survey1.setChoices(List.of(q1Answer1.getNodeRef()));

	    // Question 2: Puff Pastry Layering Quality Check
	    final SurveyQuestion question2 = getOrCreateSurveyQuestion(
	        "Evaluate the puff pastry layering quality:",
	        PASTRY_QUALITY,
	        ResponseType.multiChoicelist.name(),
	        null
	    );

	     getOrCreateSurveyAnswer(
	        question2,
	        "Ideal - Even, well-defined layers, crisp and flaky texture",
	        100d
	    );

	     final SurveyQuestion q2Answer1 = getOrCreateSurveyAnswer(
	        question2,
	        "Acceptable - Slight layer compression, moderate flakiness",
	        40d
	    );

	    getOrCreateSurveyAnswer(
	        question2,
	        "Defective - Dense, underbaked, or collapsed layers",
	        0d
	    );

	    final SurveyListDataItem survey2 = new SurveyListDataItem();
	    survey2.setQuestion(question2.getNodeRef());
	    survey2.setChoices(List.of(q2Answer1.getNodeRef()));

	    // Question 3: Chocolate Filling Quality
	    final SurveyQuestion question3 = getOrCreateSurveyQuestion(
	        "Chocolate Filling Quality Parameters:",
	        FILLING_QUALITY,
	        null,
	        100d
	    );

	    getOrCreateSurveyAnswer(
	        question3,
	        "Correct viscosity (65-70% chocolate content), uniform texture",
	        100d
	    );

	    getOrCreateSurveyAnswer(
	        question3,
	        "Slight viscosity deviation (60-75% content), minor texture issues",
	        50d
	    );

	    final SurveyQuestion q3Answer3 = getOrCreateSurveyAnswer(
	        question3,
	        "Out of specification - improper viscosity or crystallization",
	        0d
	    );

	    final SurveyListDataItem survey3 = new SurveyListDataItem();
	    survey3.setQuestion(question3.getNodeRef());
	    survey3.setChoices(List.of(q3Answer3.getNodeRef()));

	    return List.of(survey1, survey2, survey3);
	}


	private List<ScoreListDataItem> createScoreList() {
	    return List.of(
	        ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, PASTRY_QUALITY)),
	        ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, FILLING_QUALITY)),
	        ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, CCP_COMPLIANCE))
	    );
	}
	
	private SurveyQuestion getOrCreateSurveyQuestion( String label, String scoreCriterion, String responseType, Double questionScore) {
		  
	    // Create new question if not found
	    final SurveyQuestion question = (SurveyQuestion) alfrescoRepository.findOne(CharactTestHelper.getOrCreateSurveyQuestion(nodeService,label));
	    question.setLabel(label);
	    question.setScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, scoreCriterion));
	    
	    if (responseType != null) {
	        question.setResponseType(responseType);
	    }
	    
	    if (questionScore != null) {
	        question.setQuestionScore(questionScore);
	    }
	    
	    return (SurveyQuestion) alfrescoRepository.save(question);
	}

	private SurveyQuestion getOrCreateSurveyAnswer(SurveyQuestion parentQuestion, String label, Double score) {
	 final SurveyQuestion answer = (SurveyQuestion) alfrescoRepository.findOne(CharactTestHelper.getOrCreateSurveyQuestion(nodeService,label));
	    answer.setParent(parentQuestion);
	    answer.setLabel(label);
	    
	    if (score != null) {
	        answer.setQuestionScore(score);
	    }

	    return (SurveyQuestion) alfrescoRepository.save(answer);
	}
	    


}
