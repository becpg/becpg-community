package fr.becpg.test.repo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem.SignalWord;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.formulation.clp.HazardClassificationFormulaContext;
import fr.becpg.test.utils.CharactTestHelper;

public class StandardSoapTestProduct extends StandardProductBuilder {

	public static final String H226_FORBIDDEN = "Product should not contain H226";
	public static final String H290_DANGER_FORBIDDEN = "Product should not contain H290 with Danger";
	public static final String DANGER_FORBIDDEN = "Product should not contain Danger";
	public static final String GHS07_FORBIDDEN = "Product should not contain Pictogram GHS07";

	private boolean isWithCompo = true;
	private boolean isWithPhysico = true;
	private boolean isWithSpecification = false;

	protected StandardSoapTestProduct(Builder builder) {
		super(builder);
		this.isWithCompo = builder.isWithCompo;
		this.isWithPhysico = builder.isWithPhysico;
		this.isWithSpecification = builder.isWithSpecification;
	}

	// Static inner Builder class
	public static class Builder extends StandardProductBuilder.Builder<Builder> {
		private boolean isWithCompo = true;
		private boolean isWithPhysico = true;
		private boolean isWithSpecification = false;

		public Builder withCompo(boolean isWithCompo) {
			this.isWithCompo = isWithCompo;
			return this;
		}

		public Builder withPhysico(boolean isWithPhysico) {
			this.isWithPhysico = isWithPhysico;
			return this;
		}

		public Builder withSpecification(boolean isWithSpecification) {
			this.isWithSpecification = isWithSpecification;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public StandardSoapTestProduct build() {
			return new StandardSoapTestProduct(this);
		}

	}

	// Raw Material Names
	public static final String SODIUM_HYDROXIDE = "Sodium Hydroxide";
	public static final String OLIVE_OIL = "Olive Oil";
	public static final String ESSENTIAL_OILS = "Essential Oils Mix";

	protected NodeRef sodiumHydroxideNodeRef;
	protected NodeRef oliveOilNodeRef;
	protected NodeRef essentialOilsNodeRef;

	@Override
	public FinishedProductData createTestProduct() {

		// Create the soap finished product
		FinishedProductData soapProduct = FinishedProductData.build().withName(uniqueName("🧼 Standard Natural Olive Soap 🫒💧")).withUnit(ProductUnit.kg).withQty(1000d)
				.withDensity(1.2d);

		if (isWithCompo) {
			// Initialize raw materials if not already done
			if (sodiumHydroxideNodeRef == null) {
				initRawMaterialsWithIngredients();
			}

			soapProduct = soapProduct.withCompoList(List.of(
					CompoListDataItem.build().withQtyUsed(130d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(sodiumHydroxideNodeRef),
					CompoListDataItem.build().withQtyUsed(800d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(oliveOilNodeRef),
					CompoListDataItem.build().withQtyUsed(70d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
							.withProduct(essentialOilsNodeRef)));
		}

		if (isWithPhysico) {
			// Add physico-chemical properties
			addPhysicoChemProperty(soapProduct, "Boiling point", HazardClassificationFormulaContext.BOILING_POINT, 78.0); // Boiling point
			addPhysicoChemProperty(soapProduct, "Flash point", HazardClassificationFormulaContext.FLASH_POINT, 23.0); // Flash point
			addPhysicoChemProperty(soapProduct, "Hydrocarbon", HazardClassificationFormulaContext.HYDROCARBON_PERC, 15.0); // Hydrocarbon percentage

		}

		if (isWithSpecification) {

			soapProduct.setProductSpecifications(createProductSpecifications());

		}

		alfrescoRepository.create(destFolder, soapProduct);

		return soapProduct;
	}

	private List<ProductSpecificationData> createProductSpecifications() {

		ProductSpecificationData productSpecification = ProductSpecificationData.build().withName(uniqueName("🧼 Soap products specification 📋"))
				.withHcList(List.of(
						HazardClassificationListDataItem.build().withHazardStatement(CharactTestHelper.getOrCreateH(nodeService, "H226"))
								.withRegulatoryMessage(H226_FORBIDDEN),

						HazardClassificationListDataItem.build().withHazardStatement(CharactTestHelper.getOrCreateH(nodeService, "H290"))
								.withSignalWord(SignalWord.Danger.toString()).withRegulatoryMessage(H290_DANGER_FORBIDDEN),

						HazardClassificationListDataItem.build().withSignalWord(SignalWord.Danger.toString()).withRegulatoryMessage(DANGER_FORBIDDEN),

						HazardClassificationListDataItem.build().withPictogram(CharactTestHelper.getOrCreatePicto(nodeService, "GHS07"))
								.withRegulatoryMessage(GHS07_FORBIDDEN))).withForbiddenIngList(List.of(ForbiddenIngListDataItem
										
										.build().withQtyPercMaxi(2d).withIngs(List.of(CharactTestHelper.getOrCreateIng(nodeService, "Sodium Chloride")))));

		alfrescoRepository.create(destFolder, productSpecification);
		return List.of(productSpecification);
	}

	public void initRawMaterialsWithIngredients() {
		// Create Sodium Hydroxide raw material with ingredients
		RawMaterialData sodiumHydroxide = RawMaterialData.build().withName(uniqueName(SODIUM_HYDROXIDE)).withQty(100d).withUnit(ProductUnit.kg)
				.withIngList(createSodiumHydroxideIngredients());

		sodiumHydroxideNodeRef = alfrescoRepository.create(destFolder, sodiumHydroxide).getNodeRef();

		// Create Olive Oil raw material with ingredients
		RawMaterialData oliveOil = RawMaterialData.build().withName(uniqueName(OLIVE_OIL)).withQty(500d).withUnit(ProductUnit.kg)
				.withIngList(createOliveOilIngredients());

		oliveOilNodeRef = alfrescoRepository.create(destFolder, oliveOil).getNodeRef();

		// Create Essential Oils raw material with ingredients
		RawMaterialData essentialOils = RawMaterialData.build().withName(uniqueName(ESSENTIAL_OILS)).withQty(50d).withUnit(ProductUnit.kg)
				.withIngList(createEssentialOilsIngredients());

		essentialOilsNodeRef = alfrescoRepository.create(destFolder, essentialOils).getNodeRef();
	}



	private List<IngListDataItem> createSodiumHydroxideIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Sodium Hydroxide ingredients
		ingredients.add(createIngListItem("Sodium Hydroxide", 80.0, "1310-73-2", "Skin Corr. 1A:H314, Met. Corr. 1:H290", 500.0, 1000.0, 1.0, true));
		ingredients.add(createIngListItem("Sodium Carbonate", 10.0, "497-19-8", "Eye Irrit. 2:H319", 2800.0, 2000.0, null, false));
		ingredients.add(createIngListItem("Sodium Chloride", 10.0, "7647-14-5", "Eye Irrit. 2:H319", 3000.0, null, null, false));

		return ingredients;
	}

	private List<IngListDataItem> createOliveOilIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Olive Oil ingredients
		ingredients.add(createIngListItem("Oleic Acid", 55.0, "112-80-1", "Skin Irrit. 2:H315", null, null, null, false));
		ingredients.add(createIngListItem("Linoleic Acid", 20.0, "60-33-3", "Skin Sens. 1:H317", null, null, 1.0, true));
		ingredients.add(createIngListItem("Palmitic Acid", 15.0, "57-10-3", "Eye Irrit. 2:H319", null, null, null, false));

		return ingredients;
	}

	private List<IngListDataItem> createEssentialOilsIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Essential Oils ingredients
		ingredients.add(createIngListItem("Lavender Oil", 40.0, "8000-28-0", "Skin Sens. 1:H317, Aquatic Chronic 3:H412", null, null, 1.0, true));
		ingredients.add(createIngListItem("Eucalyptus Oil", 30.0, "8000-48-4", "Flam. Liq. 3:H226, Skin Sens. 1:H317", null, null, 1.0, true));
		ingredients.add(createIngListItem("Tea Tree Oil", 30.0, "68647-73-4", "Flam. Liq. 3:H226, Acute Tox. 4:H302, Skin Sens. 1:H317", 1900.0, null,
				1.0, true));

		return ingredients;
	}

	private IngListDataItem createIngListItem(String ingName, Double percentage, String casNumber, String hazardClass, Double toxicityOral,
			Double toxicityDermal, Double mFactor, Boolean superSensitizing) {

		NodeRef ing = CharactTestHelper.getOrCreateIng(nodeService, ingName);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_CAS_NUMBER, casNumber);
		properties.put(GHSModel.PROP_SDS_HAZARD_CLASSIFICATIONS, hazardClass);
		properties.put(BeCPGModel.PROP_ING_TOX_ACUTE_ORAL, toxicityOral);
		properties.put(BeCPGModel.PROP_ING_TOX_ACUTE_DERMAL, toxicityDermal);
		properties.put(BeCPGModel.PROP_ING_TOX_AQUATIC_MFACTOR, mFactor);
		properties.put(BeCPGModel.PROP_ING_TOX_IS_SUPER_SENSITIZING, superSensitizing);

		nodeService.addProperties(ing, properties);

		return IngListDataItem.build().withQtyPerc(percentage).withIngredient(ing);
	}

	public void addIngredient(ProductData product, String ingName, Double percentage, String hazardClass, Double toxicityOral, Double mFactor,
			Boolean superSensitizing) {
		if (product.getIngList() == null) {
			product.setIngList(new ArrayList<>());
		}

		product.getIngList().add(createIngListItem(ingName, percentage, null, hazardClass, toxicityOral, null, mFactor, superSensitizing));

	}

	public void addPhysicoChemProperty(ProductData product, String name, String code, Double value) {
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

	// Getters
	public NodeRef getSodiumHydroxideNodeRef() {
		return sodiumHydroxideNodeRef;
	}

	public NodeRef getOliveOilNodeRef() {
		return oliveOilNodeRef;
	}

	public NodeRef getEssentialOilsNodeRef() {
		return essentialOilsNodeRef;
	}

}