package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Sample cosmetic finished product holding every piece of data a Product Information File (PIF) needs, apart from the
 * annexes the customer provides (safety report, stability and challenge tests, claim support, GMP certificates).
 * <p>
 * The product is a leave-on body milk made of raw materials, each one with its supplier, supplier plant, origin, safety
 * data sheet reference and INCI ingredients. The finished product carries its identity, consumer information, regulatory
 * use, physico-chemical and microbiological specifications, manufacturing process and plant, primary and secondary
 * packaging, claims, labeling rules and exposure scenario.
 * <p>
 * The model has no field for the responsible person: the subsidiary stands for it and holds its address through the
 * location aspect. The animal testing statement, the physical form and the CPNP number have no field and are left out.
 * <p>
 * The values are realistic test data, not a real formula: they must not be used for an actual safety assessment.
 */
public class ProductInformationFileTestProduct extends StandardBodyMilkTestProduct {

	private static final String CHARACTS_PATH = "/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:";
	private static final String LISTS_PATH = "/app:company_home/cm:System/cm:Lists/bcpg:entityLists/cm:";
	private static final String PACK_MATERIALS_LIST = "pmMaterials";
	private static final String PHYSICO_UNITS_LIST = "PhysicoUnits";
	private static final String MICROBIO_UNITS_LIST = "MicrobioUnits";
	private static final String ALLERGENS_LIST = "Allergens";
	private static final String MICROBIOS_LIST = "Microbios";
	private static final String PLANTS_LIST = "Plants";
	private static final String PROCESS_STEPS_LIST = "ProcessSteps";
	private static final String PRECAUTION_OF_USE_LIST = "PrecautionOfUse";
	private static final String STORAGE_CONDITIONS_LIST = "StorageConditions";
	private static final String SUBSIDIARIES_LIST = "Subsidiaries";
	private static final String TRADEMARKS_LIST = "Trademarks";
	private static final String SDS_NUMBER_PREFIX = "SDS-";
	private static final String SDS_NUMBER_SEPARATORS = "[^A-Z0-9]+";
	private static final String SDS_NUMBER_SEPARATOR = "-";
	private static final String RUN_SEPARATOR = " - ";
	private static final String PLANT_SUFFIX = " plant";
	private static final String INCI_LIST_RULE = "INCI list";
	private static final String RENDER_FORMULA = "render()";
	private static final String CLAIM_MOISTURIZING_CODE = "MOISTURIZING_24H";
	private static final String CLAIM_NATURAL_ORIGIN_CODE = "ISO_16128";
	private static final String CLAIM_VEGAN_CODE = "VEGAN";

	private static final QName ASPECT_CONSUMER = QName.createQName(BeCPGModel.BECPG_URI, "consumerAspect");
	private static final QName ASPECT_LOCATION = QName.createQName(BeCPGModel.BECPG_URI, "location");
	private static final QName PROP_LOCATION_ADDRESS1 = QName.createQName(BeCPGModel.BECPG_URI, "locationAddress1");
	private static final QName PROP_PREPARATION_TIPS = QName.createQName(BeCPGModel.BECPG_URI, "preparationTips");
	private static final QName PROP_PACKAGING_DESCRIPTION = QName.createQName(BeCPGModel.BECPG_URI, "packagingDescription");
	private static final QName ASPECT_SDS = QName.createQName(GHSModel.GHS_URI, "sdsAspect");
	private static final QName PROP_SDS_SHEET_NUMBER = QName.createQName(GHSModel.GHS_URI, "sdsSheetNumber");
	private static final QName PROP_SDS_SHEET_EFFECTIVE_DATE = QName.createQName(GHSModel.GHS_URI, "sdsSheetEffectiveDate");

	public static final String PRODUCT_NAME = "🧴 Moisturizing Body Milk - PIF sample";
	public static final String LEGAL_NAME = "Moisturizing body milk";
	public static final String PRODUCT_DESCRIPTION = "Light moisturizing body milk with sunflower oil and shea butter, for all skin types.";
	public static final String ERP_CODE = "PIF-BM-200";
	public static final String EAN_CODE = "3760123456784";
	public static final String DIRECTIONS_OF_USE = "Apply daily to clean, dry skin of the body and massage gently until fully absorbed.";
	public static final String PACKAGING_DESCRIPTION = "200 mL HDPE bottle with PP flip-top cap, packed by 12 in a corrugated case.";
	public static final String STORAGE_CONDITIONS = "Store between 5 °C and 25 °C, away from light and heat";
	public static final String PRECAUTIONS_OF_USE = "For external use only. Avoid contact with eyes; in case of contact, rinse immediately with water. "
			+ "Keep out of reach of children.";

	public static final String RESPONSIBLE_PERSON = "Pure Skin Cosmetics SAS";
	public static final String RESPONSIBLE_PERSON_ADDRESS = "12 rue de la Republique, 69002 Lyon, France";
	public static final String TRADEMARK = "Pure Skin";
	public static final String MANUFACTURING_PLANT = "Pure Skin Lyon plant";
	public static final String MANUFACTURING_PLANT_ADDRESS = "ZI des Pres, 69330 Meyzieu, France";
	public static final String CERT_ISO_22716 = "ISO 22716";
	public static final String CERT_ISO_22716_DESCRIPTION = "ISO 22716 Cosmetics - Good Manufacturing Practices";

	public static final String CLAIM_MOISTURIZING = "Moisturizing 24h";
	public static final String CLAIM_NATURAL_ORIGIN = "Natural origin content (ISO 16128)";
	public static final String CLAIM_VEGAN = "Vegan formula";

	public static final String EXPOSURE_ADULT_LEAVE_ON_BODY = "Adult LO Body lotion";
	/** SCCS Notes of Guidance (12th revision): body lotion, 7.82 g/day, retention factor 1. */
	public static final double BODY_LOTION_DAILY_AMOUNT_MG = 7820d;

	public static final String WATER = "Purified Water";
	public static final String GLYCERIN = "Vegetable Glycerin";
	public static final String SUNFLOWER_OIL = "Sunflower Seed Oil";
	public static final String TRIGLYCERIDES = "Caprylic/Capric Triglyceride";
	public static final String CETYL_PALMITATE = "Cetyl Palmitate";
	public static final String SHEA_BUTTER = "Refined Shea Butter";
	public static final String EMULSIFIER = "Olive and Glucoside Emulsifier";
	public static final String XANTHAN = "Xanthan Gum";
	public static final String PRESERVATIVE = "Preservative Blend";
	public static final String FRAGRANCE = "Fresh Floral Fragrance";
	public static final String CITRIC_ACID = "Citric Acid";

	private static final String SUPPLIER_AQUA = "Aqua Process Supplies";
	private static final String SUPPLIER_OLEO = "Provence Oleochemicals";
	private static final String SUPPLIER_ACTIVES = "Alpine Cosmetic Ingredients";
	private static final String SUPPLIER_FRAGRANCE = "Grasse Fragrance House";

	private static final String HEAVY_METALS = "Heavy metals (as Pb)";
	private static final String PPM = "ppm";
	private static final String CFU_PER_G = "CFU/g";
	private static final String ABSENCE_IN_1_G = "Absence in 1 g";
	private static final String LEAVE_ON = "LEAVE_ON";
	private static final String ZONE_BODY = "BODY";
	private static final String ZONE_HANDS = "HANDS";
	private static final String LEGAL_STATUS_COSMETIC = "COSMETIC";
	private static final String FRANCE_CODE = "FR";
	private static final String FRANCE = "France";
	private static final String EU_CODE = "EU";
	private static final String EUROPEAN_UNION = "European Union";
	private static final String USAGE_BODY_LOTION = "Body Cream/Lotion";
	private static final String USAGE_BODY_LOTION_CODE = "COSMETIC_BODY_CREAM_LOTION,DECERNIS_Body Cream/Lotion";
	private static final int PERIOD_AFTER_OPENING_DAYS = 365;
	private static final String REPORT_LOCALE_ENGLISH = "en";
	private static final String REPORT_KIND_COMPO_QUALI_QUANTI = "CompoQualiQuanti";
	private static final String PIF_AGGREGATE_DEFINITION = "PIFReport.agg.json";
	private static final int PIF_REPORT_TEMPLATE_COUNT = 2;

	private static final Log logger = LogFactory.getLog(ProductInformationFileTestProduct.class);

	private record IngredientShare(String inciName, double qtyPerc) {
	}

	private record RawMaterialSpec(String name, double qtyUsed, String supplier, String originCode, String originName,
			List<IngredientShare> ingredients) {
	}

	private record PhysicoChemSpec(String name, String unit, double value, Double mini, Double maxi) {
	}

	private record MicrobioSpec(String name, Double maxi, String textCriteria) {
	}

	private record ProcessStepSpec(String name, String description) {
	}

	private record PackagingSpec(String name, String material, double tare, PackagingLevel level) {
	}

	private static final List<RawMaterialSpec> FORMULA = List.of(
			new RawMaterialSpec(WATER, 77.45, SUPPLIER_AQUA, "FR", "France", List.of(new IngredientShare("AQUA", 100d))),
			new RawMaterialSpec(GLYCERIN, 5d, SUPPLIER_OLEO, "MY", "Malaysia",
					List.of(new IngredientShare("GLYCERIN", 99.5), new IngredientShare("AQUA", 0.5))),
			new RawMaterialSpec(SUNFLOWER_OIL, 4d, SUPPLIER_OLEO, "FR", "France",
					List.of(new IngredientShare("HELIANTHUS ANNUUS SEED OIL", 99.5), new IngredientShare("TOCOPHEROL", 0.5))),
			new RawMaterialSpec(TRIGLYCERIDES, 3.5, SUPPLIER_OLEO, "DE", "Germany",
					List.of(new IngredientShare("CAPRYLIC/CAPRIC TRIGLYCERIDE", 100d))),
			new RawMaterialSpec(CETYL_PALMITATE, 2.5, SUPPLIER_OLEO, "DE", "Germany", List.of(new IngredientShare("CETYL PALMITATE", 100d))),
			new RawMaterialSpec(SHEA_BUTTER, 2d, SUPPLIER_ACTIVES, "BF", "Burkina Faso",
					List.of(new IngredientShare("BUTYROSPERMUM PARKII BUTTER", 100d))),
			new RawMaterialSpec(EMULSIFIER, 3d, SUPPLIER_ACTIVES, "FR", "France", List.of(new IngredientShare("CETEARYL GLUCOSIDE", 60d),
					new IngredientShare("SORBITAN OLIVATE", 30d), new IngredientShare("CETEARYL ALCOHOL", 10d))),
			new RawMaterialSpec(XANTHAN, 0.7, SUPPLIER_ACTIVES, "AT", "Austria", List.of(new IngredientShare("XANTHAN GUM", 100d))),
			new RawMaterialSpec(PRESERVATIVE, 0.8, SUPPLIER_ACTIVES, "DE", "Germany",
					List.of(new IngredientShare("CAPRYLYL GLYCOL", 75d), new IngredientShare("ETHYLHEXYLGLYCERIN", 25d))),
			new RawMaterialSpec(FRAGRANCE, 1d, SUPPLIER_FRAGRANCE, "FR", "France",
					List.of(new IngredientShare("PARFUM", 96.5), new IngredientShare("LINALOOL", 1.5), new IngredientShare("D-LIMONENE", 1d),
							new IngredientShare("GERANIOL", 0.5), new IngredientShare("CITRONELLOL", 0.5))),
			new RawMaterialSpec(CITRIC_ACID, 0.05, SUPPLIER_ACTIVES, "CN", "China", List.of(new IngredientShare("CITRIC ACID", 100d))));

	/** Fragrance allergens of Regulation (EC) No 1223/2009 Annex III, as shares of the fragrance raw material. */
	private static final List<IngredientShare> FRAGRANCE_ALLERGENS = List.of(new IngredientShare("Linalool", 1.5),
			new IngredientShare("Limonene", 1d), new IngredientShare("Geraniol", 0.5), new IngredientShare("Citronellol", 0.5));

	/** Labeling threshold of fragrance allergens in leave-on products, in percent. */
	private static final double LEAVE_ON_ALLERGEN_THRESHOLD = 0.001d;

	private static final List<PhysicoChemSpec> FINISHED_PRODUCT_SPECIFICATIONS = List.of(new PhysicoChemSpec("pH", "pH", 5.5, 5d, 6d),
			new PhysicoChemSpec("Viscosity (Brookfield, 20 °C)", "mPa.s", 8000d, 6000d, 10000d),
			new PhysicoChemSpec("Density (20 °C)", "g/mL", 0.98, 0.96, 1d));

	private static final List<MicrobioSpec> MICROBIOLOGICAL_LIMITS = List.of(new MicrobioSpec("Total aerobic mesophilic count", 1000d, null),
			new MicrobioSpec("Yeasts and moulds", 100d, null), new MicrobioSpec("Pseudomonas aeruginosa", null, ABSENCE_IN_1_G),
			new MicrobioSpec("Staphylococcus aureus", null, ABSENCE_IN_1_G), new MicrobioSpec("Candida albicans", null, ABSENCE_IN_1_G),
			new MicrobioSpec("Escherichia coli", null, ABSENCE_IN_1_G));

	private static final List<ProcessStepSpec> MANUFACTURING_PROCESS = List.of(
			new ProcessStepSpec("Water phase preparation", "Disperse the xanthan gum in glycerin, add it to the water and heat to 75 °C."),
			new ProcessStepSpec("Oil phase preparation", "Melt the oils, butters and emulsifiers together at 75 °C."),
			new ProcessStepSpec("Emulsification", "Pour the oil phase into the water phase under high shear mixing for 10 minutes."),
			new ProcessStepSpec("Cooling", "Cool down to 40 °C under slow stirring."),
			new ProcessStepSpec("Cool down additions", "Add the preservative blend and the fragrance below 40 °C."),
			new ProcessStepSpec("pH adjustment", "Adjust the pH between 5.0 and 6.0 with citric acid."),
			new ProcessStepSpec("Filling", "Fill the bulk into the bottles and cap them after release by quality control."));

	/** Suffix of the suppliers and supplier plants created in the destination folder, unique for each run. */
	private final String runId;

	private static final List<PackagingSpec> PACKAGING = List.of(new PackagingSpec("Bottle HDPE 200 mL", "HDPE", 22d, PackagingLevel.Primary),
			new PackagingSpec("Flip-top cap PP 24-410", "PP", 4d, PackagingLevel.Primary),
			new PackagingSpec("Self-adhesive label PP 120x60", "PP", 0.5, PackagingLevel.Primary),
			new PackagingSpec("Corrugated case x12", "Corrugated cardboard", 180d, PackagingLevel.Secondary));

	/**
	 * <p>Constructor for ProductInformationFileTestProduct.</p>
	 *
	 * @param builder the builder holding the repository dependencies
	 */
	protected ProductInformationFileTestProduct(Builder builder) {
		super(builder);
		this.runId = String.valueOf(System.currentTimeMillis());
	}

	/**
	 * Builder of the PIF sample product.
	 */
	public static class Builder extends StandardBodyMilkTestProduct.Builder {

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public ProductInformationFileTestProduct build() {
			return new ProductInformationFileTestProduct(this);
		}
	}

	/**
	 * Creates the body milk finished product, its raw materials, packaging materials and characteristics.
	 *
	 * @return the created finished product
	 */
	@Override
	public FinishedProductData createTestProduct() {
		registerIngredients();

		FinishedProductData bodyMilk = FinishedProductData.build().withName(uniqueName(PRODUCT_NAME)).withLegalName(LEGAL_NAME)
				.withUnit(ProductUnit.L).withQty(0.2d).withDensity(0.98d).withCompoList(createCompoList())
				.withPackagingList(createPackagingList()).withProcessList(createProcessList()).withLabelClaimList(createClaimList())
				.withLabelingRuleList(createLabelingRules());

		fillIdentity(bodyMilk);
		bodyMilk.setReportLocales(List.of(REPORT_LOCALE_ENGLISH));
		bodyMilk.setRegulatoryList(createMarketList());
		fillSpecifications(bodyMilk);
		alfrescoRepository.create(destFolder, bodyMilk);
		fillConsumerInformation(bodyMilk.getNodeRef());
		fillRegulatoryUse(bodyMilk);
		requestIngredientToxicityComputation();
		attachReportTemplates(bodyMilk.getNodeRef());

		return bodyMilk;
	}

	/**
	 * Builds a unique node name, cleaned of the characters a node name cannot hold, such as the slash of
	 * "Caprylic/Capric Triglyceride". The legal name keeps the original spelling.
	 *
	 * @param name the display name
	 * @return the unique, valid node name
	 */
	@Override
	protected String uniqueName(String name) {
		return super.uniqueName(PropertiesHelper.cleanName(name));
	}

	/**
	 * Registers every INCI ingredient with its function, CAS and EC numbers, hazard classification and toxicological
	 * data, so the raw materials can point to them.
	 */
	private void registerIngredients() {
		createBodyMilkIngredients();
		setIngredientToxicology("GLYCERIN", 1000d, 10d);
		setIngredientToxicology("CAPRYLYL GLYCOL", 300d, 50d);
		setIngredientToxicology("ETHYLHEXYLGLYCERIN", 50d, 50d);
		setIngredientToxicology("PARFUM", 100d, 100d);
	}

	/**
	 * Asks the toxicology service to compute the maximum values of every ingredient of the formula for each exposure
	 * scenario. The computation runs before the transaction commits and resets the flag.
	 */
	private void requestIngredientToxicityComputation() {
		Set<String> inciNames = new LinkedHashSet<>();
		for (RawMaterialSpec spec : FORMULA) {
			for (IngredientShare ingredient : spec.ingredients()) {
				inciNames.add(ingredient.inciName());
			}
		}
		for (String inciName : inciNames) {
			nodeService.setProperty(CharactTestHelper.getOrCreateIng(nodeService, inciName), PLMModel.PROP_ING_TOX_DATA, true);
		}
	}

	/**
	 * Sets the systemic toxicology data the margin of safety is computed from.
	 *
	 * @param inciName the INCI name of the ingredient
	 * @param podSystemic the systemic point of departure (NOAEL), in mg/kg bw/day
	 * @param dermalAbsorption the dermal absorption, in percent
	 */
	private void setIngredientToxicology(String inciName, double podSystemic, double dermalAbsorption) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, podSystemic);
		properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, dermalAbsorption);
		properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100d);
		nodeService.addProperties(CharactTestHelper.getOrCreateIng(nodeService, inciName), properties);
	}

	/**
	 * Creates the raw materials and the composition lines of the formula, in percent of the bulk.
	 *
	 * @return the composition list
	 */
	private List<CompoListDataItem> createCompoList() {
		List<CompoListDataItem> compoList = new ArrayList<>();
		for (RawMaterialSpec spec : FORMULA) {
			compoList.add(CompoListDataItem.build().withQtyUsed(spec.qtyUsed()).withUnit(ProductUnit.Perc)
					.withDeclarationType(DeclarationType.Detail).withProduct(createRawMaterial(spec)));
		}
		return compoList;
	}

	/**
	 * Creates a raw material with its INCI ingredients, supplier, supplier plant, origin, impurity specification and
	 * safety data sheet reference.
	 *
	 * @param spec the raw material description
	 * @return the created raw material
	 */
	private NodeRef createRawMaterial(RawMaterialSpec spec) {
		NodeRef supplierPlant = getOrCreateLocatedCharact(spec.supplier() + PLANT_SUFFIX, PLMModel.TYPE_PLANT, spec.originName());
		RawMaterialData rawMaterial = RawMaterialData.build().withName(uniqueName(spec.name())).withLegalName(spec.name()).withQty(100d)
				.withUnit(ProductUnit.kg).withIngList(createIngList(spec.ingredients()))
				.withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, spec.originCode(), spec.originName())));
		rawMaterial.setSuppliers(List.of(getOrCreateLocatedCharact(spec.supplier(), PLMModel.TYPE_SUPPLIER, spec.originName())));
		rawMaterial.setSupplierPlants(List.of(supplierPlant));
		rawMaterial.setReportLocales(List.of(REPORT_LOCALE_ENGLISH));
		addPhysicoChem(rawMaterial, new PhysicoChemSpec(HEAVY_METALS, PPM, 2d, null, 10d));
		if (FRAGRANCE.equals(spec.name())) {
			rawMaterial.setAllergenList(createFragranceAllergens());
		}

		NodeRef rawMaterialNodeRef = alfrescoRepository.create(destFolder, rawMaterial).getNodeRef();
		addSafetyDataSheet(rawMaterialNodeRef, spec.name());
		return rawMaterialNodeRef;
	}

	/**
	 * Creates the ingredient lines of a raw material.
	 *
	 * @param ingredients the INCI names and their shares
	 * @return the ingredient list
	 */
	private List<IngListDataItem> createIngList(List<IngredientShare> ingredients) {
		List<IngListDataItem> ingList = new ArrayList<>();
		for (IngredientShare ingredient : ingredients) {
			ingList.add(IngListDataItem.build().withQtyPerc(ingredient.qtyPerc())
					.withIngredient(CharactTestHelper.getOrCreateIng(nodeService, ingredient.inciName())));
		}
		return ingList;
	}

	/**
	 * Creates the fragrance allergens declared by the fragrance supplier.
	 *
	 * @return the allergen list of the fragrance raw material
	 */
	private List<AllergenListDataItem> createFragranceAllergens() {
		List<AllergenListDataItem> allergenList = new ArrayList<>();
		for (IngredientShare allergen : FRAGRANCE_ALLERGENS) {
			allergenList.add(AllergenListDataItem.build().withAllergen(getOrCreateFragranceAllergen(allergen.inciName()))
					.withQtyPerc(allergen.qtyPerc()).withVoluntary(true).withInVoluntary(false));
		}
		return allergenList;
	}

	/**
	 * Gets or creates a fragrance allergen with its leave-on labeling threshold.
	 *
	 * @param name the allergen name
	 * @return the allergen
	 */
	private NodeRef getOrCreateFragranceAllergen(String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_ALLERGEN_CODE, name.toUpperCase(Locale.ROOT));
		properties.put(PLMModel.PROP_ALLERGEN_TYPE, AllergenType.Minor.toString());
		properties.put(PLMModel.PROP_ALLERGEN_REGULATORY_THRESHOLD, LEAVE_ON_ALLERGEN_THRESHOLD);
		return CharactTestHelper.getOrCreateNode(nodeService, CHARACTS_PATH + ALLERGENS_LIST, name, PLMModel.TYPE_ALLERGEN, properties);
	}

	/**
	 * Adds the reference of the supplier safety data sheet to a raw material.
	 *
	 * @param rawMaterialNodeRef the raw material
	 * @param name the raw material name the sheet number derives from
	 */
	private void addSafetyDataSheet(NodeRef rawMaterialNodeRef, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PROP_SDS_SHEET_NUMBER, SDS_NUMBER_PREFIX + name.toUpperCase(Locale.ROOT).replaceAll(SDS_NUMBER_SEPARATORS, SDS_NUMBER_SEPARATOR));
		properties.put(PROP_SDS_SHEET_EFFECTIVE_DATE, new Date());
		nodeService.addAspect(rawMaterialNodeRef, ASPECT_SDS, properties);
	}

	/**
	 * Creates the packaging materials and the packaging lines: bottle, cap and label as primary packaging, case as
	 * secondary packaging.
	 *
	 * @return the packaging list
	 */
	private List<PackagingListDataItem> createPackagingList() {
		List<PackagingListDataItem> packagingList = new ArrayList<>();
		for (PackagingSpec spec : PACKAGING) {
			double qty = spec.level() == PackagingLevel.Secondary ? 1d / 12d : 1d;
			packagingList.add(PackagingListDataItem.build().withQty(qty).withUnit(ProductUnit.P).withPkgLevel(spec.level())
					.withProduct(createPackagingMaterial(spec)));
		}
		return packagingList;
	}

	/**
	 * Creates a packaging material with its tare and constitutive material.
	 *
	 * @param spec the packaging material description
	 * @return the created packaging material
	 */
	private NodeRef createPackagingMaterial(PackagingSpec spec) {
		PackagingMaterialData packagingMaterial = PackagingMaterialData.build().withName(uniqueName(spec.name())).withQty(1d)
				.withUnit(ProductUnit.P).withTare(spec.tare(), TareUnit.g).withPackMaterialList(List.of(PackMaterialListDataItem.build()
						.withMaterial(getOrCreatePackMaterial(spec.material())).withPerc(100d).withPkgLevel(spec.level())));
		packagingMaterial.setReportLocales(List.of(REPORT_LOCALE_ENGLISH));
		return alfrescoRepository.create(destFolder, packagingMaterial).getNodeRef();
	}

	/**
	 * Creates the manufacturing process steps, each one described.
	 *
	 * @return the process list
	 */
	private List<ProcessListDataItem> createProcessList() {
		List<ProcessListDataItem> processList = new ArrayList<>();
		for (ProcessStepSpec spec : MANUFACTURING_PROCESS) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, spec.name());
			properties.put(ContentModel.PROP_DESCRIPTION, spec.description());
			processList.add(ProcessListDataItem.build().withStep(
					CharactTestHelper.getOrCreateNode(nodeService, CHARACTS_PATH + PROCESS_STEPS_LIST, spec.name(), MPMModel.TYPE_PROCESSSTEP, properties)));
		}
		return processList;
	}

	/**
	 * Creates the claims the customer has to support in the claim annex.
	 *
	 * @return the claim list
	 */
	private List<LabelClaimListDataItem> createClaimList() {
		return new ArrayList<>(List.of(
				LabelClaimListDataItem.build().withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_MOISTURIZING_CODE, CLAIM_MOISTURIZING))
						.withIsClaimed(true),
				LabelClaimListDataItem.build().withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_NATURAL_ORIGIN_CODE, CLAIM_NATURAL_ORIGIN))
						.withIsClaimed(true).withPercentClaim(97d),
				LabelClaimListDataItem.build().withLabelClaim(CharactTestHelper.getOrCreateClaim(nodeService, CLAIM_VEGAN_CODE, CLAIM_VEGAN))
						.withIsClaimed(true)));
	}

	/**
	 * Creates the labeling rules that render the INCI list of the label.
	 *
	 * @return the labeling rules
	 */
	private List<LabelingRuleListDataItem> createLabelingRules() {
		return List.of(LabelingRuleListDataItem.build().withName(INCI_LIST_RULE).withFormula(RENDER_FORMULA).withLabelingRuleType(LabelingRuleType.Render));
	}

	/**
	 * Sets the identity of the product: codes, net content and manufacturing plant.
	 *
	 * @param bodyMilk the finished product
	 */
	private void fillIdentity(FinishedProductData bodyMilk) {
		bodyMilk.setErpCode(ERP_CODE);
		bodyMilk.setNetVolume(0.2d);
		bodyMilk.setNetWeight(0.196d);
		bodyMilk.setPlants(List.of(createManufacturingPlant()));
	}

	/**
	 * Creates the subsidiary standing for the responsible person, with its address.
	 *
	 * @return the subsidiary
	 */
	private NodeRef createResponsiblePerson() {
		NodeRef subsidiary = getOrCreateListedCharact(RESPONSIBLE_PERSON, PLMModel.TYPE_SUBSIDIARY, SUBSIDIARIES_LIST);
		nodeService.addAspect(subsidiary, ASPECT_LOCATION, new HashMap<>());
		nodeService.setProperty(subsidiary, PROP_LOCATION_ADDRESS1, RESPONSIBLE_PERSON_ADDRESS);
		return subsidiary;
	}

	/**
	 * Creates the manufacturing plant with its address and its GMP certification.
	 *
	 * @return the manufacturing plant
	 */
	private NodeRef createManufacturingPlant() {
		NodeRef plant = getOrCreateListedCharact(MANUFACTURING_PLANT, PLMModel.TYPE_PLANT, PLANTS_LIST);
		nodeService.setProperty(plant, PROP_LOCATION_ADDRESS1, MANUFACTURING_PLANT_ADDRESS);
		nodeService.setAssociations(plant, PLMModel.ASSOC_PLANT_CERTIFICATIONS,
				List.of(CharactTestHelper.getOrCreateCertification(nodeService, CERT_ISO_22716, CERT_ISO_22716_DESCRIPTION)));
		return plant;
	}

	/**
	 * Sets the specifications of the finished product and its exposure scenario.
	 *
	 * @param bodyMilk the finished product
	 */
	private void fillSpecifications(FinishedProductData bodyMilk) {
		for (PhysicoChemSpec spec : FINISHED_PRODUCT_SPECIFICATIONS) {
			addPhysicoChem(bodyMilk, spec);
		}
		bodyMilk.setMicrobioList(createMicrobioList());
		bodyMilk.setToxList(createLeaveOnToxList());
	}

	/**
	 * Adds a physico-chemical value with its unit and tolerance to a product.
	 *
	 * @param product the product
	 * @param spec the physico-chemical value
	 */
	private void addPhysicoChem(ProductData product, PhysicoChemSpec spec) {
		NodeRef physicoChem = CharactTestHelper.getOrCreatePhysico(nodeService, spec.name());
		String unit = getOrCreateUnit(PHYSICO_UNITS_LIST, spec.unit());
		nodeService.setProperty(physicoChem, PLMModel.PROP_PHYSICO_CHEM_UNIT, unit);

		PhysicoChemListDataItem item = new PhysicoChemListDataItem();
		item.setPhysicoChem(physicoChem);
		item.setValue(spec.value());
		item.setUnit(unit);
		item.setMini(spec.mini());
		item.setMaxi(spec.maxi());
		if (product.getPhysicoChemList() == null) {
			product.setPhysicoChemList(new ArrayList<>());
		}
		product.getPhysicoChemList().add(item);
	}

	/**
	 * Creates the microbiological limits of ISO 17516 for products other than those for children under 3, eye area
	 * and mucous membranes.
	 *
	 * @return the microbiological list
	 */
	private List<MicrobioListDataItem> createMicrobioList() {
		List<MicrobioListDataItem> microbioList = new ArrayList<>();
		for (MicrobioSpec spec : MICROBIOLOGICAL_LIMITS) {
			MicrobioListDataItem item = new MicrobioListDataItem();
			item.setMicrobio(getOrCreateListedCharact(spec.name(), PLMModel.TYPE_MICROBIO, MICROBIOS_LIST));
			item.setUnit(getOrCreateUnit(MICROBIO_UNITS_LIST, CFU_PER_G));
			item.setMaxi(spec.maxi());
			if (spec.textCriteria() != null) {
				item.setTextCriteria(new MLText(spec.textCriteria()));
			}
			microbioList.add(item);
		}
		return microbioList;
	}

	/**
	 * Creates the exposure scenario of an adult applying a leave-on body lotion.
	 *
	 * @return the toxicology list
	 */
	private List<ToxListDataItem> createLeaveOnToxList() {
		ToxListDataItem item = new ToxListDataItem();
		item.setTox(CharactTestHelper.getOrCreateTox(nodeService, EXPOSURE_ADULT_LEAVE_ON_BODY, BODY_LOTION_DAILY_AMOUNT_MG, true, true,
				List.of(ToxType.SkinIrritationLeaveOn, ToxType.Sensitization, ToxType.OcularIrritation, ToxType.SystemicIngredient)));
		return new ArrayList<>(List.of(item));
	}

	/**
	 * Sets the consumer information: description, EAN, period after opening, directions and precautions of use,
	 * storage conditions and packaging description, then the trademark and the responsible person.
	 *
	 * @param productNodeRef the finished product
	 */
	private void fillConsumerInformation(NodeRef productNodeRef) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_PERIOD_AFTER_OPENING, PERIOD_AFTER_OPENING_DAYS);
		properties.put(PROP_PREPARATION_TIPS, new MLText(DIRECTIONS_OF_USE));
		properties.put(PROP_PACKAGING_DESCRIPTION, new MLText(PACKAGING_DESCRIPTION));
		nodeService.addAspect(productNodeRef, ASPECT_CONSUMER, properties);
		nodeService.setProperty(productNodeRef, ContentModel.PROP_DESCRIPTION, PRODUCT_DESCRIPTION);
		nodeService.setProperty(productNodeRef, PLMModel.PROP_EAN_CODE, EAN_CODE);

		nodeService.setAssociations(productNodeRef, PLMModel.ASSOC_STORAGE_CONDITIONS,
				List.of(getOrCreateDescribedCharact(STORAGE_CONDITIONS, PLMModel.TYPE_STORAGE_CONDITIONS, STORAGE_CONDITIONS_LIST)));
		nodeService.setAssociations(productNodeRef, PLMModel.ASSOC_PRECAUTION_OF_USE,
				List.of(getOrCreateDescribedCharact(PRECAUTIONS_OF_USE, PLMModel.TYPE_PRECAUTION_OF_USE, PRECAUTION_OF_USE_LIST)));
		fillManufacturer(productNodeRef);
	}

	/**
	 * Sets the trademark and the responsible person. Both are written through the node service, as the repository
	 * mapping of the subsidiary is read only.
	 *
	 * @param productNodeRef the finished product
	 */
	private void fillManufacturer(NodeRef productNodeRef) {
		nodeService.addAspect(productNodeRef, PLMModel.ASPECT_MANUFACTURING, new HashMap<>());
		nodeService.setAssociations(productNodeRef, PLMModel.ASSOC_TRADEMARK,
				List.of(getOrCreateListedCharact(TRADEMARK, PLMModel.TYPE_TRADEMARK, TRADEMARKS_LIST)));
		nodeService.setAssociations(productNodeRef, PLMModel.ASSOC_SUBSIDIARY, List.of(createResponsiblePerson()));
	}

	/**
	 * Creates the market of the product: body lotion sold in France and in the European Union.
	 *
	 * @return the regulatory list
	 */
	private List<RegulatoryListDataItem> createMarketList() {
		RegulatoryListDataItem market = new RegulatoryListDataItem();
		market.setRegulatoryCountriesRef(List.of(getOrCreateCountryRef(FRANCE_CODE, FRANCE), getOrCreateCountryRef(EU_CODE, EUROPEAN_UNION)));
		market.setRegulatoryUsagesRef(List.of(getOrCreateUsageRef(USAGE_BODY_LOTION, USAGE_BODY_LOTION_CODE)));
		return new ArrayList<>(List.of(market));
	}

	/**
	 * Sets the regulatory use of the product (leave-on, body and hands) and its legal status on its market.
	 *
	 * @param bodyMilk the created finished product
	 */
	private void fillRegulatoryUse(FinishedProductData bodyMilk) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_REGULATORY_USE_MODE, LEAVE_ON);
		properties.put(PLMModel.PROP_REGULATORY_APPLICATION_ZONES, new ArrayList<>(List.of(ZONE_BODY, ZONE_HANDS)));
		properties.put(PLMModel.PROP_REGULATORY_MUCOUS_CONTACT, false);
		properties.put(PLMModel.PROP_REGULATORY_PROFESSIONAL_USE, false);
		nodeService.addAspect(bodyMilk.getNodeRef(), PLMModel.ASPECT_REGULATORY_PRODUCT, properties);

		for (RegulatoryListDataItem market : bodyMilk.getRegulatoryList()) {
			nodeService.setProperty(market.getNodeRef(), PLMModel.PROP_REGULATORY_LEGAL_STATUS, LEGAL_STATUS_COSMETIC);
		}
	}

	/**
	 * Associates the finished product with the report templates the PIF needs: the quali-quanti composition its first
	 * annex embeds, and the PIF aggregate itself. Their names are translated, so they are recognised by their content.
	 *
	 * @param productNodeRef the finished product
	 */
	private void attachReportTemplates(NodeRef productNodeRef) {
		List<NodeRef> templates = new ArrayList<>();
		for (NodeRef template : BeCPGQueryBuilder.createQuery().ofType(ReportModel.TYPE_REPORT_TPL).inDB()
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list()) {
			if (isFinishedProductTemplate(template) && (isCompoQualiQuantiTemplate(template) || isPifTemplate(template))) {
				templates.add(template);
			}
		}
		if (templates.size() != PIF_REPORT_TEMPLATE_COUNT) {
			logger.warn("Expected " + PIF_REPORT_TEMPLATE_COUNT + " PIF report templates for finished products, found " + templates.size());
		}
		nodeService.addAspect(productNodeRef, ReportModel.ASPECT_REPORT_TEMPLATES, new HashMap<>());
		nodeService.setAssociations(productNodeRef, ReportModel.ASSOC_REPORT_TEMPLATES, templates);
	}

	/**
	 * Tells whether a report template applies to finished products and is enabled.
	 *
	 * @param template the report template
	 * @return true if the template is an enabled finished product template
	 */
	private boolean isFinishedProductTemplate(NodeRef template) {
		return PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getProperty(template, ReportModel.PROP_REPORT_TPL_CLASS_NAME))
				&& !Boolean.TRUE.equals(nodeService.getProperty(template, ReportModel.PROP_REPORT_TPL_IS_DISABLED));
	}

	/**
	 * Tells whether a report template is the quali-quanti composition, recognised by its report kind.
	 *
	 * @param template the report template
	 * @return true if the template holds the quali-quanti composition report kind
	 */
	private boolean isCompoQualiQuantiTemplate(NodeRef template) {
		Serializable reportKinds = nodeService.getProperty(template, ReportModel.PROP_REPORT_KINDS);
		return reportKinds instanceof List<?> kinds && kinds.contains(REPORT_KIND_COMPO_QUALI_QUANTI);
	}

	/**
	 * Tells whether a report template is the PIF aggregate, recognised by its aggregate definition file.
	 *
	 * @param template the report template
	 * @return true if the template is an aggregate holding the PIF definition
	 */
	private boolean isPifTemplate(NodeRef template) {
		if (!Boolean.TRUE.equals(nodeService.getProperty(template, ReportModel.PROP_REPORT_TPL_IS_AGGREGATE))) {
			return false;
		}
		for (AssociationRef file : nodeService.getTargetAssocs(template, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES)) {
			if (PIF_AGGREGATE_DEFINITION.equals(nodeService.getProperty(file.getTargetRef(), ContentModel.PROP_NAME))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets or creates a characteristic of a system list.
	 *
	 * @param name the characteristic name
	 * @param type the characteristic type
	 * @param listName the name of the system list
	 * @return the characteristic
	 */
	private NodeRef getOrCreateListedCharact(String name, QName type, String listName) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		return CharactTestHelper.getOrCreateNode(nodeService, CHARACTS_PATH + listName, name, type, properties);
	}

	/**
	 * Gets or creates a characteristic of a system list whose description is the text printed in the reports.
	 *
	 * @param text the characteristic name and description
	 * @param type the characteristic type
	 * @param listName the name of the system list
	 * @return the characteristic
	 */
	private NodeRef getOrCreateDescribedCharact(String text, QName type, String listName) {
		NodeRef charact = getOrCreateListedCharact(text, type, listName);
		nodeService.setProperty(charact, ContentModel.PROP_DESCRIPTION, text);
		return charact;
	}

	/**
	 * Gets or creates a unit of a system list, as the unit properties only accept the values of their list. No code is
	 * set, so that an existing value is never given a code another value already holds.
	 *
	 * @param listName the name of the unit list
	 * @param unit the unit
	 * @return the unit, ready to be set on a property
	 */
	private String getOrCreateUnit(String listName, String unit) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, unit);
		CharactTestHelper.getOrCreateNode(nodeService, LISTS_PATH + listName, unit, BeCPGModel.TYPE_LIST_VALUE, properties);
		return unit;
	}

	/**
	 * Gets or creates a packaging material of the system list.
	 *
	 * @param name the material name
	 * @return the packaging material
	 */
	private NodeRef getOrCreatePackMaterial(String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, name);
		return CharactTestHelper.getOrCreateNode(nodeService, LISTS_PATH + PACK_MATERIALS_LIST, name, PackModel.TYPE_PACKAGING_MATERIAL, properties);
	}

	/**
	 * Gets or creates a supplier or a plant located in a country, in the destination folder.
	 *
	 * @param name the supplier or plant name
	 * @param type the supplier or plant type
	 * @param country the country, used as address
	 * @return the supplier or plant
	 */
	private NodeRef getOrCreateLocatedCharact(String name, QName type, String country) {
		NodeRef charact = getOrCreateCharact(name + RUN_SEPARATOR + runId, type);
		nodeService.setProperty(charact, PROP_LOCATION_ADDRESS1, country);
		return charact;
	}
}
