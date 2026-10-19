package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.decernis.RegulatoryMode;

/**
 * Products checking the IFRA side of the beCPG regulatory compliance check, one per {@link IfraScenario}.
 * <p>
 * Every product is a raw material holding its ingredients directly, in percent of the product, sold worldwide under
 * the cosmetic legal status for one or more IFRA categories. Its ingredients are created for the product alone, in a
 * folder next to it, so that the identifiers of each line (name, CAS, EC and regulatory code) are exactly the ones of
 * the scenario: the shared ingredients of the other samples carry other identifiers under the same names. Each
 * ingredient also carries its ingredient type, its function in the recipe, which the regulatory check requires.
 */
public class IfraTestProduct extends StandardSoapTestProduct {

	/** The IFRA situations a product illustrates. */
	public enum IfraScenario {
		/** Category 4: two ingredients above their limit, one exactly at its limit. */
		FINE_FRAGRANCE,
		/** Categories 5A and 5D on one line: the same recipe passes in 5A and fails in 5D. */
		BODY_LOTION_TWO_CATEGORIES,
		/** Category 9: two ingredients above their limit, AHMI with no requirement in this category. */
		RINSE_OFF_SHAMPOO,
		/** Category 12: enforced prohibitions, prohibitions not enforced and a listed ingredient. */
		SCENTED_CANDLE,
		/** Category 4: ingredients prohibited on every IFRA category, with an unregulated solvent. */
		PROHIBITED_IN_CATEGORY_4,
		/** Category 2: the ways a recipe line matches, misses or fails to resolve the IFRA ingredient. */
		IDENTIFICATION_MATRIX
	}

	public static final String USAGE_CODE_CATEGORY_2 = "IFRA_CATEGORY_2";
	public static final String USAGE_CODE_CATEGORY_4 = "IFRA_CATEGORY_4";
	public static final String USAGE_CODE_CATEGORY_5A = "IFRA_CATEGORY_5A";
	public static final String USAGE_CODE_CATEGORY_5D = "IFRA_CATEGORY_5D";
	public static final String USAGE_CODE_CATEGORY_9 = "IFRA_CATEGORY_9";
	public static final String USAGE_CODE_CATEGORY_12 = "IFRA_CATEGORY_12";

	public static final String ALCOHOL_DENAT = "ALCOHOL DENAT.";
	public static final String AQUA = "AQUA";
	public static final String PARFUM = "PARFUM";
	public static final String GLYCERIN = "GLYCERIN";
	public static final String CELLULOSE_GUM = "CELLULOSE GUM";
	public static final String MICROCRYSTALLINE_WAX = "MICROCRYSTALLINE WAX";
	public static final String BEESWAX = "BEESWAX";
	public static final String AMYL_CINNAMIC_ALDEHYDE = "alpha-Amyl cinnamic aldehyde";
	public static final String BENZYL_SALICYLATE = "Benzyl salicylate";
	public static final String CITRAL = "Citral";
	public static final String CINNAMIC_ALDEHYDE = "Cinnamic aldehyde";
	public static final String AHMI = "Acetyl hexamethyl indan (AHMI)";
	public static final String ALLYL_ISOTHIOCYANATE = "Allyl isothiocyanate";
	public static final String MINTLACTONE = "Mintlactone";
	public static final String ACETYL_DIMETHYLFURAN = "3-Acetyl-2,5-dimethylfuran";
	public static final String CYCLAMEN_ALCOHOL = "Cyclamen alcohol";
	public static final String ISOPHORONE = "Isophorone";
	public static final String LIMONENE = "Limonene";
	public static final String QUINOLINE = "Quinoline";
	public static final String MUSK_XYLENE = "Musk xylene";
	public static final String FURFURYL_ALCOHOL = "Furfuryl alcohol";
	public static final String MASSOIA_LACTONE = "Massoia lactone";
	public static final String BOLDO_OIL = "Boldo oil";
	public static final String HEXYL_BASE = "Hexyl base";
	public static final String ACCORD_17 = "Accord 17";
	public static final String ALLYL_PHENOXYACETATE = "Allyl phenoxyacetate";
	public static final String PROPYLIDENEPHTHALIDE = "3-Propylidenephthalide";
	public static final String AMYL_CINNAMAL = "AMYL CINNAMAL";
	public static final String SENSATE_ACCORD = "Sensate accord";
	public static final String HOUSE_WOODY_ACCORD = "Accord boisé maison";

	public static final String CAS_AMYL_CINNAMIC_ALDEHYDE = "122-40-7";
	public static final String CAS_BENZYL_SALICYLATE = "118-58-1";
	public static final String CAS_CINNAMIC_ALDEHYDE = "104-55-2";
	public static final String CAS_GERANIAL = "141-27-5";
	/** The BECPG regulatory code of alpha-hexyl cinnamic aldehyde. */
	private static final String HEXYL_CINNAMIC_ALDEHYDE_CODE = "BECPG_33052";

	private static final String CAS_ALCOHOL = "64-17-5";
	private static final String CAS_WATER = "7732-18-5";
	private static final String CAS_GLYCERIN = "56-81-5";
	private static final String CAS_CELLULOSE_GUM = "9004-32-4";
	private static final String CAS_CITRAL = "106-26-3";
	private static final String CAS_AHMI = "15323-35-0";

	private static final String MODULE_IFRA = "IFRA";
	private static final String WORLD_CODE = "WORLD";
	private static final String WORLD = "World";
	private static final String LEGAL_STATUS_COSMETIC = "COSMETIC";
	private static final String INGREDIENTS_FOLDER_SUFFIX = " - ingredients";
	private static final String LINE_FOLDER_PREFIX = "Line ";

	private static final String LEAVE_ON = "LEAVE_ON";
	private static final String RINSE_OFF = "RINSE_OFF";
	private static final String BODY = "BODY";
	private static final String HANDS = "HANDS";
	private static final String HAIR = "HAIR";
	private static final String SCALP = "SCALP";
	private static final String PUMP_SPRAY = "PUMP_SPRAY";
	private static final String AEROSOL = "AEROSOL";
	private static final String INHALABLE = "INHALABLE";
	private static final String ADULT = "ADULT";
	/** The target population is single-valued in beCPG: from birth stands for the adults and the under-3 of 5D. */
	private static final String FROM_BIRTH = "FROM_BIRTH";

	/* The usages and ingredient types below are the values of the cosmetics reference data, column for column. */
	private static final IfraUsage CATEGORY_2 = new IfraUsage("Category 2", "Catégorie 2", USAGE_CODE_CATEGORY_2);
	private static final IfraUsage CATEGORY_4 = new IfraUsage("Category 4", "Catégorie 4", USAGE_CODE_CATEGORY_4);
	private static final IfraUsage CATEGORY_5A = new IfraUsage("Category 5A", "Catégorie 5A", USAGE_CODE_CATEGORY_5A);
	private static final IfraUsage CATEGORY_5D = new IfraUsage("Category 5D", "Catégorie 5D", USAGE_CODE_CATEGORY_5D);
	private static final IfraUsage CATEGORY_9 = new IfraUsage("Category 9", "Catégorie 9", USAGE_CODE_CATEGORY_9);
	private static final IfraUsage CATEGORY_12 = new IfraUsage("Category 12", "Catégorie 12", USAGE_CODE_CATEGORY_12);

	private static final String SOLVENT = "SOLVENT";
	private static final String HUMECTANT = "HUMECTANT";
	private static final String VISCOSITY_CONTROLLING = "VISCOSITY CONTROLLING";
	private static final String EMOLLIENT = "SKIN_CONDITIONING - EMOLLIENT";
	private static final String PERFUMING = "PERFUMING";

	/** The function of the formulation bases; every other ingredient is a fragrance material, hence perfuming. */
	private static final Map<String, String> BASE_INGREDIENT_TYPES = Map.of(ALCOHOL_DENAT, SOLVENT, AQUA, SOLVENT, GLYCERIN, HUMECTANT,
			CELLULOSE_GUM, VISCOSITY_CONTROLLING, MICROCRYSTALLINE_WAX, VISCOSITY_CONTROLLING, BEESWAX, EMOLLIENT);

	private static final RegulatoryAttributes FINE_FRAGRANCE_ATTRIBUTES = new RegulatoryAttributes(LEAVE_ON, List.of(BODY, HANDS),
			List.of(PUMP_SPRAY), ADULT);

	private static final ScenarioDefinition FINE_FRAGRANCE = new ScenarioDefinition("IFRA A - Fine fragrance", List.of(CATEGORY_4),
			FINE_FRAGRANCE_ATTRIBUTES, List.of(
					ingredient(ALCOHOL_DENAT, CAS_ALCOHOL, 78.40),
					ingredient(AQUA, CAS_WATER, 10.00),
					ingredient(PARFUM, null, 3.70),
					ingredient(AMYL_CINNAMIC_ALDEHYDE, CAS_AMYL_CINNAMIC_ALDEHYDE, 1.50),
					ingredient(BENZYL_SALICYLATE, CAS_BENZYL_SALICYLATE, 3.00),
					ingredient(CITRAL, CAS_CITRAL, 0.60),
					ingredient(CINNAMIC_ALDEHYDE, CAS_CINNAMIC_ALDEHYDE, 0.30),
					ingredient(AHMI, CAS_AHMI, 2.50)));

	private static final ScenarioDefinition BODY_LOTION_TWO_CATEGORIES = new ScenarioDefinition("IFRA B - Body lotion",
			List.of(CATEGORY_5A, CATEGORY_5D), new RegulatoryAttributes(LEAVE_ON, List.of(BODY), List.of(), FROM_BIRTH), List.of(
					ingredient(AQUA, CAS_WATER, 90.21),
					ingredient(GLYCERIN, CAS_GLYCERIN, 8.00),
					ingredient(CELLULOSE_GUM, CAS_CELLULOSE_GUM, 0.50),
					ingredient(CINNAMIC_ALDEHYDE, CAS_CINNAMIC_ALDEHYDE, 0.05),
					ingredient(CITRAL, CAS_CITRAL, 0.14),
					ingredient(AMYL_CINNAMIC_ALDEHYDE, CAS_AMYL_CINNAMIC_ALDEHYDE, 0.10),
					ingredient(BENZYL_SALICYLATE, CAS_BENZYL_SALICYLATE, 1.00)));

	private static final ScenarioDefinition RINSE_OFF_SHAMPOO = new ScenarioDefinition("IFRA C - Shampoo", List.of(CATEGORY_9),
			new RegulatoryAttributes(RINSE_OFF, List.of(HAIR, SCALP), List.of(), ADULT), List.of(
					ingredient(AQUA, CAS_WATER, 80.65),
					ingredient(GLYCERIN, CAS_GLYCERIN, 10.00),
					ingredient(CELLULOSE_GUM, CAS_CELLULOSE_GUM, 1.00),
					ingredient(CINNAMIC_ALDEHYDE, CAS_CINNAMIC_ALDEHYDE, 0.45),
					ingredient(CITRAL, CAS_CITRAL, 1.30),
					ingredient(AMYL_CINNAMIC_ALDEHYDE, CAS_AMYL_CINNAMIC_ALDEHYDE, 1.60),
					ingredient(BENZYL_SALICYLATE, CAS_BENZYL_SALICYLATE, 2.00),
					ingredient(AHMI, CAS_AHMI, 3.00)));

	private static final ScenarioDefinition SCENTED_CANDLE = new ScenarioDefinition("IFRA D - Scented candle", List.of(CATEGORY_12),
			new RegulatoryAttributes(null, List.of(), List.of(INHALABLE), ADULT), List.of(
					ingredient(MICROCRYSTALLINE_WAX, "63231-60-7", 91.30),
					ingredient(BEESWAX, "8012-89-3", 5.00),
					ingredient(ALLYL_ISOTHIOCYANATE, "57-06-7", 0.02),
					ingredient(MINTLACTONE, "13341-72-5", 0.05),
					ingredient(ACETYL_DIMETHYLFURAN, "10599-70-9", 0.03),
					ingredient(CYCLAMEN_ALCOHOL, "4756-19-8", 0.50),
					ingredient(ISOPHORONE, "78-59-1", 0.10),
					ingredient(LIMONENE, "138-86-3", 3.00)));

	private static final ScenarioDefinition PROHIBITED_IN_CATEGORY_4 = new ScenarioDefinition("IFRA E - Prohibited in category 4",
			List.of(CATEGORY_4), FINE_FRAGRANCE_ATTRIBUTES, List.of(
					ingredient(ALCOHOL_DENAT, CAS_ALCOHOL, 94.50),
					ingredient(QUINOLINE, "91-22-5", 0.50),
					ingredient(MUSK_XYLENE, "81-15-2", 1.00),
					ingredient(FURFURYL_ALCOHOL, "98-00-0", 2.00),
					ingredient(MASSOIA_LACTONE, "51154-96-2", 1.00),
					ingredient(BOLDO_OIL, "8022-81-9", 1.00)));

	private static final ScenarioDefinition IDENTIFICATION_MATRIX = new ScenarioDefinition("IFRA F - Identification matrix", List.of(CATEGORY_2),
			new RegulatoryAttributes(LEAVE_ON, List.of(BODY), List.of(AEROSOL), ADULT), List.of(
					ingredient(ALCOHOL_DENAT, CAS_ALCOHOL, 78.70),
					ingredient(AQUA, CAS_WATER, 10.00),
					new IfraIngredient(HEXYL_BASE, null, null, HEXYL_CINNAMIC_ALDEHYDE_CODE, 1.00),
					new IfraIngredient(HEXYL_BASE, CAS_BENZYL_SALICYLATE, null, HEXYL_CINNAMIC_ALDEHYDE_CODE, 1.00),
					ingredient(ACCORD_17, CAS_AHMI, 2.50),
					ingredient(CINNAMIC_ALDEHYDE, null, 1.00),
					ingredient(ALLYL_PHENOXYACETATE, "863306-60-9", 1.00),
					ingredient(PROPYLIDENEPHTHALIDE, "17369-59-4", 1.00),
					ingredient(CITRAL, CAS_GERANIAL, 0.50),
					ingredient(CITRAL, CAS_CINNAMIC_ALDEHYDE, 0.10),
					ingredient(AMYL_CINNAMAL, null, 1.00),
					ingredient(AMYL_CINNAMAL, CAS_AMYL_CINNAMIC_ALDEHYDE, 1.00),
					new IfraIngredient(BENZYL_SALICYLATE, CAS_BENZYL_SALICYLATE, "204-262-9", null, 1.00),
					new IfraIngredient(SENSATE_ACCORD, CAS_CINNAMIC_ALDEHYDE, "204-541-5", null, 0.10),
					ingredient(HOUSE_WOODY_ACCORD, null, 0.10)));

	/**
	 * An IFRA category the product is declared for.
	 *
	 * @param englishName the name of the regulatory usage, by default and in English
	 * @param frenchName the name of the regulatory usage in French
	 * @param code the regulatory code of the usage, the code of the IFRA category leaf
	 */
	private record IfraUsage(String englishName, String frenchName, String code) {

		/**
		 * @return the usage name in each locale of the reference data
		 */
		MLText name() {
			MLText name = new MLText(englishName);
			name.addValue(Locale.ENGLISH, englishName);
			name.addValue(Locale.FRENCH, frenchName);
			return name;
		}
	}

	/**
	 * A recipe line, as the user would type it.
	 *
	 * @param name the ingredient name
	 * @param cas the CAS number, null when the line has none
	 * @param ec the EC number, null when the line has none
	 * @param regulatoryCode the BECPG regulatory code, null when the line has none
	 * @param qtyPerc the quantity, in percent of the product
	 */
	private record IfraIngredient(String name, String cas, String ec, String regulatoryCode, double qtyPerc) {
	}

	/**
	 * The product attributes selecting the limits that apply. Mucous contact and professional use are always off.
	 *
	 * @param useMode the use mode, null when the product has none
	 * @param applicationZones the application zones
	 * @param exposureRoutes the exposure routes
	 * @param targetPopulation the target population
	 */
	private record RegulatoryAttributes(String useMode, List<String> applicationZones, List<String> exposureRoutes, String targetPopulation) {

		/**
		 * @return the properties of the regulatory product aspect
		 */
		Map<QName, Serializable> toProperties() {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PLMModel.PROP_REGULATORY_PROFESSIONAL_USE, false);
			properties.put(PLMModel.PROP_REGULATORY_MUCOUS_CONTACT, false);
			properties.put(PLMModel.PROP_REGULATORY_TARGET_POPULATION, targetPopulation);
			properties.put(PLMModel.PROP_REGULATORY_APPLICATION_ZONES, new ArrayList<>(applicationZones));
			properties.put(PLMModel.PROP_REGULATORY_EXPOSURE_ROUTES, new ArrayList<>(exposureRoutes));
			if (useMode != null) {
				properties.put(PLMModel.PROP_REGULATORY_USE_MODE, useMode);
			}
			return properties;
		}
	}

	/**
	 * Everything a scenario declares.
	 *
	 * @param productName the product name, made unique on creation
	 * @param usages the IFRA categories of the single regulatory line
	 * @param attributes the product attributes
	 * @param recipe the recipe lines, in order
	 */
	private record ScenarioDefinition(String productName, List<IfraUsage> usages, RegulatoryAttributes attributes, List<IfraIngredient> recipe) {
	}

	private final ScenarioDefinition definition;

	/**
	 * @param builder the builder
	 */
	protected IfraTestProduct(Builder builder) {
		super(builder);
		this.definition = definitionOf(builder.scenario);
	}

	/** Builds an IFRA product, the fine fragrance unless another scenario is chosen. */
	public static class Builder extends StandardSoapTestProduct.Builder {
		private IfraScenario scenario = IfraScenario.FINE_FRAGRANCE;

		/**
		 * Creates a builder whose product holds its regulatory list.
		 */
		public Builder() {
			withRegulatory(true);
		}

		/**
		 * @param scenario the IFRA situation the product illustrates
		 * @return this builder
		 */
		public Builder withScenario(IfraScenario scenario) {
			this.scenario = scenario;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public IfraTestProduct build() {
			return new IfraTestProduct(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ProductData createTestProduct() {
		String productName = uniqueName(definition.productName());
		RawMaterialData product = RawMaterialData.build()
				.withName(productName)
				.withUnit(ProductUnit.kg)
				.withQty(100d)
				.withIngList(createIngredients(createIngredientsFolder(productName)));
		addOptionalLists(product);
		product.setRegulatoryMode(RegulatoryMode.REGULATORY);

		alfrescoRepository.create(destFolder, product);

		nodeService.addAspect(product.getNodeRef(), PLMModel.ASPECT_REGULATORY_PRODUCT, definition.attributes().toProperties());
		setMarketsLegalStatus(product);
		return product;
	}

	/**
	 * Creates the single market of the product: worldwide, for the IFRA categories of the scenario.
	 *
	 * @return the regulatory list
	 */
	@Override
	protected List<RegulatoryListDataItem> createMarketList() {
		List<NodeRef> usages = new ArrayList<>();
		for (IfraUsage usage : definition.usages()) {
			usages.add(getOrCreateUsageRef(usage.name(), usage.code(), MODULE_IFRA));
		}
		return buildMarketList(List.of(getOrCreateCountryRef(WORLD_CODE, WORLD)), usages);
	}

	private static IfraIngredient ingredient(String name, String cas, double qtyPerc) {
		return new IfraIngredient(name, cas, null, null, qtyPerc);
	}

	private static ScenarioDefinition definitionOf(IfraScenario scenario) {
		return switch (scenario) {
		case FINE_FRAGRANCE -> FINE_FRAGRANCE;
		case BODY_LOTION_TWO_CATEGORIES -> BODY_LOTION_TWO_CATEGORIES;
		case RINSE_OFF_SHAMPOO -> RINSE_OFF_SHAMPOO;
		case SCENTED_CANDLE -> SCENTED_CANDLE;
		case PROHIBITED_IN_CATEGORY_4 -> PROHIBITED_IN_CATEGORY_4;
		case IDENTIFICATION_MATRIX -> IDENTIFICATION_MATRIX;
		};
	}

	/**
	 * Sets the cosmetic legal status on the market, through the node service, as the repository mapping of a
	 * regulatory line does not hold it.
	 *
	 * @param product the created product
	 */
	private void setMarketsLegalStatus(RawMaterialData product) {
		for (RegulatoryListDataItem market : product.getRegulatoryList()) {
			nodeService.setProperty(market.getNodeRef(), PLMModel.PROP_REGULATORY_LEGAL_STATUS, LEGAL_STATUS_COSMETIC);
		}
	}

	private List<IngListDataItem> createIngredients(NodeRef ingredientsFolder) {
		List<IngListDataItem> ingList = new ArrayList<>();
		for (IfraIngredient ingredient : definition.recipe()) {
			NodeRef parent = folderForLine(ingredientsFolder, ingredient, ingList.size() + 1);
			ingList.add(IngListDataItem.build().withQtyPerc(ingredient.qtyPerc()).withIngredient(createIngredient(parent, ingredient)));
		}
		return ingList;
	}

	/**
	 * Two lines of a recipe may name the same ingredient with other identifiers: the second one gets a folder of its
	 * own, as two nodes of a folder cannot share a name.
	 */
	private NodeRef folderForLine(NodeRef ingredientsFolder, IfraIngredient ingredient, int lineNumber) {
		String name = PropertiesHelper.cleanName(ingredient.name());
		if (nodeService.getChildByName(ingredientsFolder, ContentModel.ASSOC_CONTAINS, name) == null) {
			return ingredientsFolder;
		}
		return createFolder(ingredientsFolder, LINE_FOLDER_PREFIX + lineNumber);
	}

	private NodeRef createIngredientsFolder(String productName) {
		return createFolder(destFolder, PropertiesHelper.cleanName(productName + INGREDIENTS_FOLDER_SUFFIX));
	}

	private NodeRef createFolder(NodeRef parent, String name) {
		return createNode(parent, name, ContentModel.TYPE_FOLDER, new HashMap<>());
	}

	/**
	 * Creates the ingredient of a recipe line, holding its ingredient type and only the identifiers the line declares.
	 */
	private NodeRef createIngredient(NodeRef parent, IfraIngredient ingredient) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, ingredient.name());
		properties.put(PLMModel.PROP_ING_TYPE_V2, getOrCreateIngredientType(BASE_INGREDIENT_TYPES.getOrDefault(ingredient.name(), PERFUMING)));
		putIfDeclared(properties, PLMModel.PROP_CAS_NUMBER, ingredient.cas());
		putIfDeclared(properties, PLMModel.PROP_EC_NUMBER, ingredient.ec());
		putIfDeclared(properties, PLMModel.PROP_REGULATORY_CODE, ingredient.regulatoryCode());
		return createNode(parent, PropertiesHelper.cleanName(ingredient.name()), PLMModel.TYPE_ING, properties);
	}

	private static void putIfDeclared(Map<QName, Serializable> properties, QName property, String value) {
		if (value != null) {
			properties.put(property, value);
		}
	}

	private NodeRef createNode(NodeRef parent, String name, QName type, Map<QName, Serializable> properties) {
		properties.put(ContentModel.PROP_NAME, name);
		return nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), type,
				properties).getChildRef();
	}
}
