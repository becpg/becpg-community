package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

/**
 * Test product builder for creating a cake with local semi-finished products
 * to test hierarchical composition in batch formulation.
 *
 * This test product creates:
 * - Raw materials: Flour, Sugar, Butter
 * - Local semi-finished product: Dough (contains Flour + Sugar)
 * - Finished product: Cake (contains Dough as parent + Butter as child of Dough)
 *
 * Used specifically for testing Redmine #25868: Local semi-finished products
 * and their children should be properly copied to batch composition with
 * correct parent-child hierarchy.
 *
 * @author matthieu
 */
public class StandardCakeWithLocalSemiFinishedTestProduct extends SampleProductBuilder {

	/** Constant <code>FLOUR_NAME="Wheat Flour T55"</code> */
	public static final String FLOUR_NAME = "Wheat Flour T55";
	/** Constant <code>SUGAR_NAME="Caster Sugar"</code> */
	public static final String SUGAR_NAME = "Caster Sugar";
	/** Constant <code>BUTTER_NAME="Unsalted Butter"</code> */
	public static final String BUTTER_NAME = "Unsalted Butter";
	/** Constant <code>EGG_NAME="Fresh Eggs"</code> */
	public static final String EGG_NAME = "Fresh Eggs";
	/** Constant <code>BAKING_POWDER_NAME="Baking Powder"</code> */
	public static final String BAKING_POWDER_NAME = "Baking Powder";
	/** Constant <code>VANILLA_NAME="Vanilla Extract"</code> */
	public static final String VANILLA_NAME = "Vanilla Extract";
	/** Constant <code>MILK_NAME="Whole Milk"</code> */
	public static final String MILK_NAME = "Whole Milk";
	/** Constant <code>CAKE_BATTER_NAME="Vanilla Cake Batter"</code> */
	public static final String CAKE_BATTER_NAME = "Vanilla Cake Batter";
	/** Constant <code>BUTTERCREAM_NAME="Vanilla Buttercream"</code> */
	public static final String BUTTERCREAM_NAME = "Vanilla Buttercream";
	/** Constant <code>CAKE_NAME="Vanilla Layer Cake"</code> */
	public static final String CAKE_NAME = "Vanilla Layer Cake";

	/** Constant <code>PLANT_USINE_1="Usine 1"</code> */
	public static final String PLANT_USINE_1 = "Usine 1";
	/** Constant <code>LABORATORY_1="Laboratoire 1"</code> */
	public static final String LABORATORY_1 = "Laboratoire 1";

	protected FinishedProductData product;

	protected NodeRef flourNodeRef;
	protected NodeRef sugarNodeRef;
	protected NodeRef butterNodeRef;
	protected NodeRef eggNodeRef;
	protected NodeRef bakingPowderNodeRef;
	protected NodeRef vanillaNodeRef;
	protected NodeRef milkNodeRef;
	protected NodeRef cakeBatterNodeRef;
	protected NodeRef buttercreamNodeRef;

	private boolean isWithCompo = true;

	// Private constructor to enforce usage of the builder
	private StandardCakeWithLocalSemiFinishedTestProduct(Builder builder) {
		super(builder);
		this.isWithCompo = builder.isWithCompo;
	}

	// Static inner Builder class
	public static class Builder extends SampleProductBuilder.Builder<Builder> {

		private boolean isWithCompo = true;

		public Builder withCompo(boolean isWithCompo) {
			this.isWithCompo = isWithCompo;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public StandardCakeWithLocalSemiFinishedTestProduct build() {
			return new StandardCakeWithLocalSemiFinishedTestProduct(this);
		}
	}

	/**
	 * Getter for the field <code>flourNodeRef</code>.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getFlourNodeRef() {
		return flourNodeRef;
	}

	/**
	 * Getter for the field <code>sugarNodeRef</code>.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSugarNodeRef() {
		return sugarNodeRef;
	}

	/**
	 * Getter for the field <code>butterNodeRef</code>.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getButterNodeRef() {
		return butterNodeRef;
	}

	/**
	 * Getter for the field <code>cakeBatterNodeRef</code>.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getCakeBatterNodeRef() {
		return cakeBatterNodeRef;
	}

	/**
	 * Getter for the field <code>buttercreamNodeRef</code>.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getButtercreamNodeRef() {
		return buttercreamNodeRef;
	}

	/**
	 * Getter for the field <code>product</code>.
	 *
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public FinishedProductData getProduct() {
		return product;
	}

	/**
	 * getNodeService.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * getDestFolder.
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getDestFolder() {
		return destFolder;
	}

	/** {@inheritDoc} */
	@Override
	public FinishedProductData createTestProduct() {
		product = FinishedProductData.build().withName(CAKE_NAME).withUnit(ProductUnit.kg).withQty(1.2d)
				.withDensity(0.85d);
		product.setNetWeight(1.2d);

		if (isWithCompo) {
			initCompoProduct();

			// Create hierarchical composition: Local semi-finished products with their raw material children
			CompoListDataItem cakeBatterCompo = CompoListDataItem.build().withParent(null).withQty(null).withQtyUsed(800d)
					.withUnit(ProductUnit.g).withLossPerc(2d).withDeclarationType(DeclarationType.Detail).withProduct(cakeBatterNodeRef);

			CompoListDataItem buttercreamCompo = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(400d)
					.withUnit(ProductUnit.g).withLossPerc(1d).withDeclarationType(DeclarationType.Detail).withProduct(buttercreamNodeRef);

			// Raw materials for cake batter (children of cake batter)
			CompoListDataItem flourCompo = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(250d)
					.withUnit(ProductUnit.g).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(flourNodeRef);
			CompoListDataItem sugarCompo1 = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(200d)
					.withUnit(ProductUnit.g).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(sugarNodeRef);
			CompoListDataItem butterCompo1 = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(200d)
					.withUnit(ProductUnit.g).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(butterNodeRef);
			CompoListDataItem eggCompo = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(200d)
					.withUnit(ProductUnit.g).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(eggNodeRef);
			CompoListDataItem bakingPowderCompo = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(10d)
					.withUnit(ProductUnit.g).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(bakingPowderNodeRef);
			CompoListDataItem vanillaCompo1 = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(5d)
					.withUnit(ProductUnit.mL).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(vanillaNodeRef);
			CompoListDataItem milkCompo1 = CompoListDataItem.build().withParent(cakeBatterCompo).withQty(null).withQtyUsed(120d)
					.withUnit(ProductUnit.mL).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(milkNodeRef);

			// Raw materials for buttercream (children of buttercream)
			CompoListDataItem butterCompo2 = CompoListDataItem.build().withParent(buttercreamCompo).withQty(null).withQtyUsed(250d)
					.withUnit(ProductUnit.g).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(butterNodeRef);
			CompoListDataItem sugarCompo2 = CompoListDataItem.build().withParent(buttercreamCompo).withQty(null).withQtyUsed(200d)
					.withUnit(ProductUnit.g).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(sugarNodeRef);
			CompoListDataItem vanillaCompo2 = CompoListDataItem.build().withParent(buttercreamCompo).withQty(null).withQtyUsed(3d)
					.withUnit(ProductUnit.mL).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(vanillaNodeRef);
			CompoListDataItem milkCompo2 = CompoListDataItem.build().withParent(buttercreamCompo).withQty(null).withQtyUsed(30d)
					.withUnit(ProductUnit.mL).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(milkNodeRef);

			product = product.withCompoList(List.of(
				cakeBatterCompo, buttercreamCompo,
				flourCompo, sugarCompo1, butterCompo1, eggCompo, bakingPowderCompo, vanillaCompo1, milkCompo1,
				butterCompo2, sugarCompo2, vanillaCompo2, milkCompo2
			));
		}

		alfrescoRepository.create(destFolder, product);

		return product;
	}

	/**
	 * Initialize composition products (raw materials and local semi-finished products).
	 * Creates realistic cake formulation with proper ingredient ratios.
	 */
	public void initCompoProduct() {
		// Create raw materials with realistic properties
		RawMaterialData flour = RawMaterialData.build().withName(FLOUR_NAME).withQty(100d).withUnit(ProductUnit.kg).withDensity(0.59d);
		flourNodeRef = alfrescoRepository.create(destFolder, flour).getNodeRef();

		RawMaterialData sugar = RawMaterialData.build().withName(SUGAR_NAME).withQty(50d).withUnit(ProductUnit.kg).withDensity(1.59d);
		sugarNodeRef = alfrescoRepository.create(destFolder, sugar).getNodeRef();

		RawMaterialData butter = RawMaterialData.build().withName(BUTTER_NAME).withQty(25d).withUnit(ProductUnit.kg).withDensity(0.91d);
		butterNodeRef = alfrescoRepository.create(destFolder, butter).getNodeRef();

		RawMaterialData eggs = RawMaterialData.build().withName(EGG_NAME).withQty(30d).withUnit(ProductUnit.kg).withDensity(1.03d);
		eggNodeRef = alfrescoRepository.create(destFolder, eggs).getNodeRef();

		RawMaterialData bakingPowder = RawMaterialData.build().withName(BAKING_POWDER_NAME).withQty(2d).withUnit(ProductUnit.kg).withDensity(0.9d);
		bakingPowderNodeRef = alfrescoRepository.create(destFolder, bakingPowder).getNodeRef();

		RawMaterialData vanilla = RawMaterialData.build().withName(VANILLA_NAME).withQty(1d).withUnit(ProductUnit.L).withDensity(0.87d);
		vanillaNodeRef = alfrescoRepository.create(destFolder, vanilla).getNodeRef();

		RawMaterialData milk = RawMaterialData.build().withName(MILK_NAME).withQty(20d).withUnit(ProductUnit.L).withDensity(1.03d);
		milkNodeRef = alfrescoRepository.create(destFolder, milk).getNodeRef();

		// Create local semi-finished product: Cake Batter
		LocalSemiFinishedProductData cakeBatter = new LocalSemiFinishedProductData();
		cakeBatter.setName(CAKE_BATTER_NAME);
		cakeBatter.setQty(0.8d);
		cakeBatter.setUnit(ProductUnit.kg);
		cakeBatter.setNetWeight(0.8d);
		cakeBatter.setDensity(0.8d);

		cakeBatterNodeRef = alfrescoRepository.create(destFolder, cakeBatter).getNodeRef();

		// Create local semi-finished product: Buttercream
		LocalSemiFinishedProductData buttercream = new LocalSemiFinishedProductData();
		buttercream.setName(BUTTERCREAM_NAME);
		buttercream.setQty(0.4d);
		buttercream.setUnit(ProductUnit.kg);
		buttercream.setNetWeight(0.4d);
		buttercream.setDensity(0.95d);

		buttercreamNodeRef = alfrescoRepository.create(destFolder, buttercream).getNodeRef();
	}



}
