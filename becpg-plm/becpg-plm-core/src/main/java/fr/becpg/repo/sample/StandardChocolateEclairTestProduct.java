package fr.becpg.repo.sample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LogisticUnitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
<<<<<<< 23.4.1
=======
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.ResourceProductData;
>>>>>>> 743d2b5 Fix #27967 - [Feature] Add composition/packaging multilevel in export search
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
<<<<<<< 23.4.1
=======
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
>>>>>>> 743d2b5 Fix #27967 - [Feature] Add composition/packaging multilevel in export search
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
<<<<<<< 23.4.1
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
=======
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
>>>>>>> 743d2b5 Fix #27967 - [Feature] Add composition/packaging multilevel in export search
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.impl.SurveyServiceImpl.ResponseType;

/**
 * <p>StandardChocolateEclairTestProduct class.</p>
 *
 * @author matthieu
 */
public class StandardChocolateEclairTestProduct extends SampleProductBuilder {

	/** Constant <code>WATER_NAME="Eau"</code> */
	public static final String WATER_NAME = "Eau";
	/** Constant <code>MILK_NAME="Lait"</code> */
	public static final String MILK_NAME = "Lait";
	/** Constant <code>SUGAR_NAME="Sucre"</code> */
	public static final String SUGAR_NAME = "Sucre";
	/** Constant <code>SUGAR_SUPPLIER_1_NAME="Sucre - Fournisseur 1"</code> */
	public static final String SUGAR_SUPPLIER_1_NAME = "Sucre - Fournisseur 1";
	/** Constant <code>SUGAR_SUPPLIER_2_NAME="Sucre - Fournisseur 2"</code> */
	public static final String SUGAR_SUPPLIER_2_NAME = "Sucre - Fournisseur 2";
	/** Constant <code>SUGAR_SUPPLIER_3_NAME="Sucre - Fournisseur 3"</code> */
	public static final String SUGAR_SUPPLIER_3_NAME = "Sucre - Fournisseur 3";
	/** Constant <code>SUGAR_GEN_USINE_1_NAME="Sucre - GEN - Usine 1"</code> */
	public static final String SUGAR_GEN_USINE_1_NAME = "Sucre - GEN - Usine 1";
	/** Constant <code>SUGAR_GEN_USINE_2_NAME="Sucre - GEN - Usine 2"</code> */
	public static final String SUGAR_GEN_USINE_2_NAME = "Sucre - GEN - Usine 2";
	/** Constant <code>FLOUR_NAME="Farine"</code> */
	public static final String FLOUR_NAME = "Farine";
	/** Constant <code>EGG_NAME="Oeuf"</code> */
	public static final String EGG_NAME = "Oeuf";
	/** Constant <code>CHOCOLATE_NAME="Chocolat"</code> */
	public static final String CHOCOLATE_NAME = "Chocolat";
	/** Constant <code>PATE_CHOUX_NAME="P&acirc;te &agrave; choux"</code> */
	public static final String PATE_CHOUX_NAME = "Pâte à choux";
	/** Constant <code>CREME_PATISSIERE_NAME="Cr&egrave;me p&acirc;tissi&egrave;re"</code> */
	public static final String CREME_PATISSIERE_NAME = "Crème pâtissière";
	/** Constant <code>NAPPAGE_NAME="Nappage"</code> */
	public static final String NAPPAGE_NAME = "Nappage";
	/** Constant <code>SUPPLIER_1="Fournisseur 1"</code> */
	public static final String SUPPLIER_1 = "Fournisseur 1";
	/** Constant <code>SUPPLIER_2="Fournisseur 2"</code> */
	public static final String SUPPLIER_2 = "Fournisseur 2";
	/** Constant <code>SUPPLIER_3="Fournisseur 3"</code> */
	public static final String SUPPLIER_3 = "Fournisseur 3";
	/** Constant <code>LABORATORY_1="Laboratoire 1"</code> */
	public static final String LABORATORY_1 = "Laboratoire 1";
	/** Constant <code>LABORATORY_2="Laboratoire 2"</code> */
	public static final String LABORATORY_2 = "Laboratoire 2";
	/** Constant <code>PLANT_USINE_1="Usine 1"</code> */
	public static final String PLANT_USINE_1 = "Usine 1";
	/** Constant <code>PLANT_USINE_2="Usine 2"</code> */
	public static final String PLANT_USINE_2 = "Usine 2";
	/** Constant <code>PASTRY_QUALITY="Pastry quality"</code> */
	public static final String PASTRY_QUALITY = "Pastry quality";
	/** Constant <code>CCP_COMPLIANCE="CCP compliance"</code> */
	public static final String CCP_COMPLIANCE = "CCP compliance";
	/** Constant <code>FILLING_QUALITY="Filling quality"</code> */
	public static final String FILLING_QUALITY = "Filling quality";

	protected NodeRef pateChouxNodeRef;
	protected NodeRef cremePatissiereNodeRef;
	protected NodeRef nappageNodeRef;

	protected NodeRef mixingProcessNodeRef;
	protected NodeRef bakingProcessNodeRef;

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

	/**
	 * <p>Getter for the field <code>pateChouxNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getPateChouxNodeRef() {
		return pateChouxNodeRef;
	}

	/**
	 * <p>Getter for the field <code>cremePatissiereNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getCremePatissiereNodeRef() {
		return cremePatissiereNodeRef;
	}

	/**
	 * <p>Getter for the field <code>nappageNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getNappageNodeRef() {
		return nappageNodeRef;
	}

	/**
	 * <p>Getter for the field <code>waterNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getWaterNodeRef() {
		return waterNodeRef;
	}

	/**
	 * <p>Getter for the field <code>milkNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getMilkNodeRef() {
		return milkNodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarNodeRef() {
		return sugarNodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarPlants1NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarPlants1NodeRef() {
		return sugarPlants1NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarPlants2NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarPlants2NodeRef() {
		return sugarPlants2NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarSupplier1NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarSupplier1NodeRef() {
		return sugarSupplier1NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarSupplier2NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarSupplier2NodeRef() {
		return sugarSupplier2NodeRef;
	}

	/**
	 * <p>Getter for the field <code>sugarSupplier3NodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarSupplier3NodeRef() {
		return sugarSupplier3NodeRef;
	}

	/**
	 * <p>Getter for the field <code>flourNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getFlourNodeRef() {
		return flourNodeRef;
	}

	/**
	 * <p>Getter for the field <code>eggNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getEggNodeRef() {
		return eggNodeRef;
	}

	/**
	 * <p>Getter for the field <code>chocolateNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getChocolateNodeRef() {
		return chocolateNodeRef;
	}

	/**
	 * <p>getNodeService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * <p>getDestFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
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

	private boolean isWithClaim = false;

	private boolean isWithSpecification = false;
	private boolean isWithNuts = false;
	private boolean isWithProcess = false;

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

		this.isWithClaim = builder.isWithClaim;
		this.isWithSpecification = builder.isWithSpecification;
		this.isWithNuts = builder.isWithNuts;
		this.isWithProcess = builder.isWithProcess;
	}

	// Static inner Builder class
	public static class Builder extends SampleProductBuilder.Builder<Builder> {
		private boolean isWithCompo = true;
		private boolean isWithLabeling = true;
		private boolean isWithGenericRawMaterial = true;
		private boolean isWithStocks = true;
		private boolean isWithIngredients = false;
		private boolean isWithSurvey = false;
		private boolean isWithScoreList = false;

		private boolean isWithClaim = false;
		private boolean isWithSpecification = false;
		private boolean isWithNuts = false;
		private boolean isWithProcess = false;

		public Builder withClaim(boolean isWithClaim) {
			this.isWithClaim = isWithClaim;
			return this;
		}

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


		public Builder withNuts(boolean isWithNuts) {
			this.isWithNuts = isWithNuts;
			return this;
		}

		public Builder withProcess(boolean isWithProcess) {
			this.isWithProcess = isWithProcess;
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

	/** {@inheritDoc} */
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


		if (isWithSurvey) {
			product.withSurveyList(new ArrayList<>(createEclairQMSSurveyList()));
		}

		if (isWithScoreList) {
			product.withScoreList(createScoreList());
		}

		if (isWithProcess) {
			initProcessProducts();
			product.withProcessList(createProcessList());
		}

		if (isWithSpecification) {
			product.setProductSpecifications(createProductSpecifications());
		}

		alfrescoRepository.create(destFolder, product);

		
		if(isWithSurvey) {
			finishedProduct.withSurveyList(createEclairQMSSurveyList());
		}
		
		if(isWithScoreList) {
			finishedProduct.withScoreList(createScoreList());
		}

		alfrescoRepository.create(destFolder, finishedProduct);

		return finishedProduct;

	}

	/**
	 * <p>createCaseLogisticUnit.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
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

	/**
	 * <p>createPalletLogisticUnit.</p>
	 *
	 * @param caseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
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

	/**
	 * <p>initCompoProduct.</p>
	 */
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


	/**
	 * Initialize process products for testing.
	 */
	public void initProcessProducts() {
		// Create process steps
		ResourceProductData mixingProcess = ResourceProductData.build().withName("Mixing Process").withUnit(ProductUnit.P).withQty(1d);
		mixingProcessNodeRef = alfrescoRepository.create(destFolder, mixingProcess).getNodeRef();

		ResourceProductData bakingProcess = ResourceProductData.build().withName("Baking Process").withUnit(ProductUnit.P).withQty(1d);
		bakingProcessNodeRef = alfrescoRepository.create(destFolder, bakingProcess).getNodeRef();
	}

	/**
	 * Create process list for the chocolate éclair.
	 *
	 * @return List of process list data items
	 */
	private List<ProcessListDataItem> createProcessList() {
		return List.of(
			ProcessListDataItem.build().withProduct(mixingProcessNodeRef),
			ProcessListDataItem.build().withProduct(bakingProcessNodeRef)
		);
	}

	private void configureNutrientProfile(FinishedProductData finishedProduct) {
		finishedProduct.getAspects().add(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE);
		finishedProduct.setNutrientProfileCategory(NutrientProfileCategory.Others.toString());
		finishedProduct.setNutrientProfileVersion(NutrientProfileVersion.VERSION_2023.toString());
		finishedProduct.withNutList(createNutList(1250d, 8d, 16d, 30d, 0.2d, 0.5d, 1.2d, 1.8d, 5.5d));
		addPhysicoChemProperty(finishedProduct, "Fruit and vegetable content", NutriScoreContext.FRUIT_VEGETABLE_CODE, 0d);
	}

	private void addPhysicoChemProperty(ProductData product, String name, String code, Double value) {
		PhysicoChemListDataItem physicoChemList = new PhysicoChemListDataItem();

		NodeRef physicoChem = CharactTestHelper.getOrCreatePhysico(nodeService, name);

		Map<QName, Serializable> props = new HashMap<>();
		props.put(PLMModel.PROP_PHYSICO_CHEM_CODE, code);
		props.put(PLMModel.PROP_PHYSICO_CHEM_UNIT, "%");
		nodeService.addProperties(physicoChem, props);

		if (product.getPhysicoChemList() == null) {
			product.setPhysicoChemList(new ArrayList<>());
		}

		physicoChemList.setPhysicoChem(physicoChem);
		physicoChemList.setValue(value);
		product.getPhysicoChemList().add(physicoChemList);
	}

	private List<NutListDataItem> createNutList(Double energyKj, Double satFat, Double totalFat, Double sugar, Double sodium, Double salt,
			Double nspFiber, Double aoacFiber, Double protein) {
		List<NutListDataItem> nutList = new ArrayList<>();
		addNutEntry(nutList, NutriScoreContext.ENERGY_CODE, "kJ", energyKj);
		addNutEntry(nutList, NutriScoreContext.SATFAT_CODE, "g", satFat);
		addNutEntry(nutList, NutriScoreContext.FAT_CODE, "g", totalFat);
		addNutEntry(nutList, NutriScoreContext.SUGAR_CODE, "g", sugar);
		addNutEntry(nutList, NutriScoreContext.SODIUM_CODE, "g", sodium);
		addNutEntry(nutList, NutriScoreContext.SALT_CODE, "g", salt);
		addNutEntry(nutList, NutriScoreContext.NSP_CODE, "g", nspFiber);
		addNutEntry(nutList, NutriScoreContext.AOAC_CODE, "g", aoacFiber);
		addNutEntry(nutList, NutriScoreContext.PROTEIN_CODE, "g", protein);
		return nutList;
	}

	private void addNutEntry(List<NutListDataItem> nutList, String code, String unit, Double value) {
		if (value == null) {
			return;
		}
		NodeRef nutRef = CharactTestHelper.getOrCreateNutrient(nodeService, code, unit);
		nutList.add(NutListDataItem.build().withNut(nutRef).withUnit(unit).withValue(value).withIsManual(true));
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
	        0d
	    );

	    final SurveyQuestion q3Answer3 = getOrCreateSurveyAnswer(
	        question3,
	        "Out of specification - improper viscosity or crystallization",
	        50d
	    );

	    final SurveyListDataItem survey3 = new SurveyListDataItem();
	    survey3.setQuestion(question3.getNodeRef());
	    survey3.setChoices(List.of(q3Answer3.getNodeRef()));

	    return List.of(survey1, survey2, survey3);
	}


	private List<ScoreListDataItem> createScoreList() {
	    return new ArrayList<>(List.of(
	        ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, PASTRY_QUALITY)),
	        ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, FILLING_QUALITY)),
	        ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, CCP_COMPLIANCE)).withScore(80d)
	    ));
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
