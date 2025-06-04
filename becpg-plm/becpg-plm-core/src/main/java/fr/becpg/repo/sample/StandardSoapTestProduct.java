package fr.becpg.repo.sample;

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
import fr.becpg.model.ProjectModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.product.data.ClientData;
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
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.product.formulation.clp.HazardClassificationFormulaContext;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;

/**
 * <p>StandardSoapTestProduct class.</p>
 *
 * @author matthieu
 */
public class StandardSoapTestProduct extends SampleProductBuilder {

	/** Constant <code>H226_FORBIDDEN="Product should not contain H226"</code> */
	public static final String H226_FORBIDDEN = "Product should not contain H226";
	/** Constant <code>H290_DANGER_FORBIDDEN="Product should not contain H290 with Da"{trunked}</code> */
	public static final String H290_DANGER_FORBIDDEN = "Product should not contain H290 with Danger";
	/** Constant <code>DANGER_FORBIDDEN="Product should not contain Danger"</code> */
	public static final String DANGER_FORBIDDEN = "Product should not contain Danger";
	/** Constant <code>GHS07_FORBIDDEN="Product should not contain Pictogram GH"{trunked}</code> */
	public static final String GHS07_FORBIDDEN = "Product should not contain Pictogram GHS07";
	/** Constant <code>CLIMATE_CHANGE="Climate change"</code> */
	public static final String CLIMATE_CHANGE = "Climate change";

	// Transport subcategories
	/** Constant <code>CLIENT_TRANSPORT_IMPACTS="Client transport carbon impact (kg CO2/"{trunked}</code> */
	public static final String CLIENT_TRANSPORT_IMPACTS = "Client transport carbon impact (kg CO2/kg/km)";
	/** Constant <code>SUPPLIER_TRANSPORT_IMPACT="Supplier transport carbon impact (kCO2/"{trunked}</code> */
	public static final String SUPPLIER_TRANSPORT_IMPACT = "Supplier transport carbon impact (kCO2/kg/km)";

	/** Constant <code>EPI_SCORE="EPI Score"</code> */
	public static final String EPI_SCORE = "EPI Score";
	/** Constant <code>SPI_SCORE="SPI Score"</code> */
	public static final String SPI_SCORE = "SPI Score";

	/** Constant <code>SUPPLIER_ECOVADIS_CERTIFICATION="Supplier Ecovadis Certification Score"</code> */
	public static final String SUPPLIER_ECOVADIS_CERTIFICATION = "Supplier Ecovadis Certification Score";

	private boolean isWithCompo = true;
	private boolean isWithScore = false;
	private boolean isWithPhysico = true;
	private boolean isWithSpecification = false;
	private boolean isWithToxicology = false;

	/**
	 * <p>Constructor for StandardSoapTestProduct.</p>
	 *
	 * @param builder a {@link fr.becpg.repo.sample.StandardSoapTestProduct.Builder} object
	 */
	protected StandardSoapTestProduct(Builder builder) {
		super(builder);
		this.isWithCompo = builder.isWithCompo;
		this.isWithPhysico = builder.isWithPhysico;
		this.isWithSpecification = builder.isWithSpecification;
		this.isWithScore = builder.isWithScore;
		this.isWithToxicology = builder.isWithToxicology;
	}

	// Static inner Builder class
	public static class Builder extends SampleProductBuilder.Builder<Builder> {
		private boolean isWithCompo = true;
		private boolean isWithPhysico = true;
		private boolean isWithSpecification = false;
		private boolean isWithScore = false;
		private boolean isWithToxicology = false;

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

		public Builder withScore(boolean isWithScore) {
			this.isWithScore = isWithScore;
			return this;
		}
		
		public Builder withToxicology(boolean isWithToxicology) {
			this.isWithToxicology = isWithToxicology;
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
	/** Constant <code>SODIUM_HYDROXIDE="Sodium Hydroxide"</code> */
	public static final String SODIUM_HYDROXIDE = "Sodium Hydroxide";
	/** Constant <code>SODIUM_CARBONATE="Sodium Carbonate"</code> */
	public static final String SODIUM_CARBONATE = "Sodium Carbonate";
	/** Constant <code>SODIUM_CHLORIDE="Sodium Chloride"</code> */
	public static final String SODIUM_CHLORIDE = "Sodium Chloride";
	/** Constant <code>OLIVE_OIL="Olive Oil"</code> */
	public static final String OLIVE_OIL = "Olive Oil";
	/** Constant <code>ESSENTIAL_OILS="Essential Oils Mix"</code> */
	public static final String ESSENTIAL_OILS = "Essential Oils Mix";

	protected NodeRef sodiumHydroxideNodeRef;
	protected NodeRef oliveOilNodeRef;
	protected NodeRef essentialOilsNodeRef;

	/** {@inheritDoc} */
	@Override
	public FinishedProductData createTestProduct() {

		// Create the soap finished product
		FinishedProductData soapProduct = FinishedProductData.build().withName(uniqueName("🧼 Standard Natural Olive Soap 🫒💧"))
				.withUnit(ProductUnit.kg).withQty(1000d).withDensity(1.2d);

		if (isWithScore) {
			
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, CLIENT_TRANSPORT_IMPACTS),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);
			
			//Client
			ClientData clientData = ClientData.build().withName(uniqueName("Natural Soap Dealer Shop")).withScoreList(List.of(ScoreListDataItem
					.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, CLIENT_TRANSPORT_IMPACTS)).withScore(55d)));

			alfrescoRepository.create(destFolder, clientData);
			soapProduct.setClients(List.of(clientData));
			soapProduct.setScoreList(new ArrayList<>());
		}

		if (isWithCompo || isWithToxicology) {
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

			createPhysicoChems(soapProduct);

		}

		if (isWithSpecification) {
			soapProduct.setProductSpecifications(createProductSpecifications());
		}
		
		if (isWithToxicology) {
			soapProduct.setToxList(createToxList());
		}

		alfrescoRepository.create(destFolder, soapProduct);

		saveEntityAssociations(soapProduct);

		return soapProduct;
	}

	private List<ToxListDataItem> createToxList() {
		List<ToxListDataItem> toxList = new ArrayList<>();
		ToxListDataItem toxListDataItem = new ToxListDataItem();
		toxListDataItem.setTox(CharactTestHelper.getOrCreateTox(nodeService, "Adult RO Face", 140.0, true, true,
				List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.OcularIrritation, ToxType.SystemicIngredient)));
		toxList.add(toxListDataItem);
		
		toxListDataItem = new ToxListDataItem();
		toxListDataItem.setTox(CharactTestHelper.getOrCreateTox(nodeService, "Adult RO Hair", 110.0, true, true,
				List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
		toxList.add(toxListDataItem);
		
		toxListDataItem = new ToxListDataItem();
		toxListDataItem.setTox(CharactTestHelper.getOrCreateTox(nodeService, "Adult RO Body", 220.0, true, true,
				List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
		toxList.add(toxListDataItem);
		return toxList;
	}

	private void saveEntityAssociations(FinishedProductData soapProduct) {

		if (soapProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData productSpecificationData : soapProduct.getProductSpecifications()) {
				nodeService.createAssociation(soapProduct.getNodeRef(), productSpecificationData.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			}
		}

		if (soapProduct.getClients() != null) {
			for (ClientData clientData : soapProduct.getClients()) {
				nodeService.createAssociation(soapProduct.getNodeRef(), clientData.getNodeRef(), PLMModel.ASSOC_CLIENTS);
			}
		}

	}

	/**
	 * <p>createPhysicoChems.</p>
	 *
	 * @param soapProduct a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	protected void createPhysicoChems(FinishedProductData soapProduct) {
		// Add physico-chemical properties
		addPhysicoChemProperty(soapProduct, "Boiling point", HazardClassificationFormulaContext.BOILING_POINT, 78.0); // Boiling point
		addPhysicoChemProperty(soapProduct, "Flash point", HazardClassificationFormulaContext.FLASH_POINT, 23.0); // Flash point
		addPhysicoChemProperty(soapProduct, "Hydrocarbon", HazardClassificationFormulaContext.HYDROCARBON_PERC, 15.0); // Hydrocarbon percentage
	}

	/**
	 * <p>createProductSpecifications.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	protected List<ProductSpecificationData> createProductSpecifications() {

		ProductSpecificationData productSpecification = ProductSpecificationData.build().withName(uniqueName("🧼 Soap products specification 📋"))
				.withHcList(List.of(
						HazardClassificationListDataItem.build().withHazardStatement(CharactTestHelper.getOrCreateH(nodeService, "H226"))
								.withRegulatoryMessage(H226_FORBIDDEN),

						HazardClassificationListDataItem.build().withHazardStatement(CharactTestHelper.getOrCreateH(nodeService, "H290"))
								.withSignalWord(SignalWord.Danger.toString()).withRegulatoryMessage(H290_DANGER_FORBIDDEN),

						HazardClassificationListDataItem.build().withSignalWord(SignalWord.Danger.toString()).withRegulatoryMessage(DANGER_FORBIDDEN),

						HazardClassificationListDataItem.build().withPictogram(CharactTestHelper.getOrCreatePicto(nodeService, "GHS07"))
								.withRegulatoryMessage(GHS07_FORBIDDEN)))
				.withForbiddenIngList(List.of(ForbiddenIngListDataItem

						.build().withQtyPercMaxi(2d).withIngs(List.of(CharactTestHelper.getOrCreateIng(nodeService, SODIUM_CHLORIDE)))));

		alfrescoRepository.create(destFolder, productSpecification);
		return List.of(productSpecification);
	}

	/**
	 * <p>initRawMaterialsWithIngredients.</p>
	 */
	public void initRawMaterialsWithIngredients() {
		// Create Sodium Hydroxide raw material with ingredients
		RawMaterialData sodiumHydroxide = RawMaterialData.build().withName(uniqueName(SODIUM_HYDROXIDE)).withQty(100d).withUnit(ProductUnit.kg)
				.withIngList(createSodiumHydroxideIngredients())
				.withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, "DE", "Germany")));

		if (isWithScore) {
			
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);
			nodeService.setProperty(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT),ProjectModel.PROP_SCORE_CRITERION_FORMULATED, true);

			sodiumHydroxide = sodiumHydroxide.withScoreList(List.of(
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE))
							.withScore(62.4d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE))
							.withScore(77.4d),
				 ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT))));
			addLCAProperty(sodiumHydroxide, CLIMATE_CHANGE, "CLIMATE_CHANGE", 2.51d);

		}

		sodiumHydroxideNodeRef = alfrescoRepository.create(destFolder, sodiumHydroxide).getNodeRef();

		// Create Olive Oil raw material with ingredients
		RawMaterialData oliveOil = RawMaterialData.build().withName(uniqueName(OLIVE_OIL)).withQty(500d).withUnit(ProductUnit.kg)
				.withIngList(createOliveOilIngredients()).withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, "France", "FR")));

		if (isWithScore) {

			oliveOil = oliveOil.withScoreList(List.of(
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE))
							.withScore(62.5d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE))
							.withScore(77.5d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT))));
			addLCAProperty(sodiumHydroxide, CLIMATE_CHANGE, "CLIMATE_CHANGE", 109.99d);

		}

		oliveOilNodeRef = alfrescoRepository.create(destFolder, oliveOil).getNodeRef();

		// Create Essential Oils raw material with ingredients
		RawMaterialData essentialOils = RawMaterialData.build().withName(uniqueName(ESSENTIAL_OILS)).withQty(50d).withUnit(ProductUnit.kg)
				.withIngList(createEssentialOilsIngredients()).withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, "China", "CN")));

		if (isWithScore) {

			essentialOils = essentialOils.withScoreList(List.of(
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, EPI_SCORE))
							.withScore(28.4d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SPI_SCORE))
							.withScore(43.4d),
					ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriterion(nodeService, SUPPLIER_TRANSPORT_IMPACT))));
			addLCAProperty(sodiumHydroxide, CLIMATE_CHANGE, "CLIMATE_CHANGE", 300d);

		}

		essentialOilsNodeRef = alfrescoRepository.create(destFolder, essentialOils).getNodeRef();
	}

	private List<IngListDataItem> createSodiumHydroxideIngredients() {
		List<IngListDataItem> ingredients = new ArrayList<>();

		// Sodium Hydroxide ingredients
		ingredients.add(createIngListItem(SODIUM_HYDROXIDE, 80.0, "1310-73-2", "Skin Corr. 1A:H314, Met. Corr. 1:H290", 500.0, 1000.0, 1.0, true));
		ingredients.add(createIngListItem(SODIUM_CARBONATE, 10.0, "497-19-8", "Eye Irrit. 2:H319", 2800.0, 2000.0, null, false));
		ingredients.add(createIngListItem(SODIUM_CHLORIDE, 10.0, "7647-14-5", "Eye Irrit. 2:H319", 3000.0, null, null, false));

		if (isWithToxicology) {
			NodeRef ing = CharactTestHelper.getOrCreateIng(nodeService, SODIUM_HYDROXIDE);
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, 10000);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 18);
			properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 80);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 85);
			nodeService.addProperties(ing, properties);
			
			ing = CharactTestHelper.getOrCreateIng(nodeService, SODIUM_CARBONATE);
			properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, 1200);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 0.2);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 100);
			nodeService.addProperties(ing, properties);
			
			ing = CharactTestHelper.getOrCreateIng(nodeService, SODIUM_CHLORIDE);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 1);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 5);
			nodeService.addProperties(ing, properties);
		}
		
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

	/**
	 * <p>addIngredient.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param ingName a {@link java.lang.String} object
	 * @param percentage a {@link java.lang.Double} object
	 * @param hazardClass a {@link java.lang.String} object
	 * @param toxicityOral a {@link java.lang.Double} object
	 * @param mFactor a {@link java.lang.Double} object
	 * @param superSensitizing a {@link java.lang.Boolean} object
	 */
	public void addIngredient(ProductData product, String ingName, Double percentage, String hazardClass, Double toxicityOral, Double mFactor,
			Boolean superSensitizing) {
		if (product.getIngList() == null) {
			product.setIngList(new ArrayList<>());
		}

		product.getIngList().add(createIngListItem(ingName, percentage, null, hazardClass, toxicityOral, null, mFactor, superSensitizing));

	}

	/**
	 * <p>addPhysicoChemProperty.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param name a {@link java.lang.String} object
	 * @param code a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 */
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

	/**
	 * <p>addLCAProperty.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param name a {@link java.lang.String} object
	 * @param code a {@link java.lang.String} object
	 * @param value a {@link java.lang.Double} object
	 */
	public void addLCAProperty(ProductData product, String name, String code, Double value) {
		LCAListDataItem lcaListDataItem = new LCAListDataItem();

		NodeRef lca = CharactTestHelper.getOrCreateLCA(nodeService, name);

		Map<QName, Serializable> props = new HashMap<>();
		props.put(PLMModel.PROP_LCA_CODE, code);
		props.put(PLMModel.PROP_LCAUNIT, CharactTestHelper.getOrCreateLCAUnit(nodeService, "kg CO2 eq"));
		nodeService.addProperties(lca, props);

		if (product.getLcaList() == null) {
			product.setLcaList(new ArrayList<>());
		}

		lcaListDataItem.setLca(lca);
		lcaListDataItem.setValue(value);
		product.getLcaList().add(lcaListDataItem);

	}

	// Getters
	/**
	 * <p>Getter for the field <code>sodiumHydroxideNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSodiumHydroxideNodeRef() {
		return sodiumHydroxideNodeRef;
	}

	/**
	 * <p>Getter for the field <code>oliveOilNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getOliveOilNodeRef() {
		return oliveOilNodeRef;
	}

	/**
	 * <p>Getter for the field <code>essentialOilsNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getEssentialOilsNodeRef() {
		return essentialOilsNodeRef;
	}

}
