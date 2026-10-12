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
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
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

	private static final String PHYSICO_UNITS_LIST = "PhysicoUnits";
	private static final String PLANTS_LIST = "Plants";
	private static final String PRECAUTION_OF_USE_LIST = "PrecautionOfUse";
	private static final String STORAGE_CONDITIONS_LIST = "StorageConditions";
	private static final String SUBSIDIARIES_LIST = "Subsidiaries";
	private static final String TRADEMARKS_LIST = "Trademarks";
	private static final String SDS_NUMBER_PREFIX = "SDS-";
	private static final String SDS_NUMBER_SEPARATORS = "[^A-Z0-9]+";
	private static final String SDS_NUMBER_SEPARATOR = "-";
	private static final String RUN_SEPARATOR = " - ";
	private static final String PLANT_SUFFIX = " plant";
	private static final String HIDE_INCI_FUNCTIONS_RULE = "Do not declare INCI functions";
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
	private static final QName PROP_SDS_SIGNAL_WORD = QName.createQName(GHSModel.GHS_URI, "sdsSignalWord");
	private static final QName ASSOC_SDS_HAZARD_STATEMENTS = QName.createQName(GHSModel.GHS_URI, "sdsHazardStatements");
	private static final QName ASSOC_SDS_PICTOGRAMS = QName.createQName(GHSModel.GHS_URI, "sdsPictograms");
	private static final QName ASSOC_SDS_PRECAUTIONARY_STATEMENTS = QName.createQName(GHSModel.GHS_URI, "sdsPrecautionaryStatements");
	private static final QName ASSOC_SDS_PERSONAL_PROTECTIONS = QName.createQName(GHSModel.GHS_URI, "sdsPersonalProtections");
	private static final QName ASSOC_SDS_ONU_CODE = QName.createQName(GHSModel.GHS_URI, "sdsOnuCode");
	private static final QName ASSOC_SDS_CLASS_CODE = QName.createQName(GHSModel.GHS_URI, "sdsClassCode");
	private static final QName ASSOC_SDS_PACKAGING_GROUP_CODE = QName.createQName(GHSModel.GHS_URI, "sdsPackagingGroupCode");
	private static final QName PROP_SDS_SAFETY_INSTRUCTIONS = QName.createQName(GHSModel.GHS_URI, "sdsSafetyInstructions");
	private static final QName PROP_PRECAUTIONARY_CODE = QName.createQName(GHSModel.GHS_URI, "precautionaryCode");
	private static final QName PROP_PERSONAL_PROTECTION_CODE = QName.createQName(GHSModel.GHS_URI, "personalProtectionCode");
	private static final QName PROP_ONU_CODE_NUMBER = QName.createQName(GHSModel.GHS_URI, "onuCodeNumber");
	private static final QName PROP_CLASS_CODE_NUMBER = QName.createQName(GHSModel.GHS_URI, "classCodeNumber");
	private static final QName PROP_PACKAGING_GROUP_CODE_NUMBER = QName.createQName(GHSModel.GHS_URI, "packagingGroupCodeNumber");
	private static final String PHRASE_SEPARATOR = " ";
	private static final String SECURITY_LISTS_PATH = "/app:company_home/cm:System/cm:SecurityLists/bcpg:entityLists/cm:";

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

	/** Postal address of each supplier and of its plant, independent from the origin of the raw materials it sells. */
	private static final Map<String, String> SUPPLIER_ADDRESSES = Map.of(
			SUPPLIER_AQUA, "8 avenue de l'Industrie, 67000 Strasbourg, France",
			SUPPLIER_OLEO, "145 chemin des Oliviers, 13100 Aix-en-Provence, France",
			SUPPLIER_ACTIVES, "Industriestrasse 21, 6020 Innsbruck, Austria",
			SUPPLIER_FRAGRANCE, "52 route de Cannes, 06130 Grasse, France");

	private static final String HEAVY_METALS = "Heavy metals (as Pb)";
	private static final String PPM = "ppm";
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

	/** An INCI ingredient and its share in a raw material, in percent. */
	private record IngredientShare(String inciName, double qtyPerc) {
	}

	/** A raw material of the formula: its share in the bulk, its supplier, its origin and its INCI ingredients. */
	private record RawMaterialSpec(String name, double qtyUsed, String supplier, String originCode, String originName,
			List<IngredientShare> ingredients) {
	}

	/** A physico-chemical characteristic with its unit, value and tolerance. */
	private record PhysicoChemSpec(String name, String unit, double value, Double mini, Double maxi) {
	}

	/** A coded phrase of a safety data sheet, such as a precautionary statement or a personal protection. */
	private record CodedPhrase(String code, String phrase) {
	}

	/** The transport classification of a dangerous good: UN number, hazard class and packing group. */
	private record TransportSpec(String unNumber, String hazardClass, String packingGroup) {
	}

	/** The classification of a safety data sheet: signal word, hazard statements and pictogram codes. */
	private record HazardSpec(String signalWord, List<CodedPhrase> hazardStatements, List<String> pictograms) {
	}

	/** The handling section of a safety data sheet: precautionary statements, protections, instructions and transport. */
	private record HandlingSpec(List<CodedPhrase> precautions, List<CodedPhrase> protections, String safetyInstructions,
			TransportSpec transport) {
	}

	private static final String WARNING = "Warning";
	private static final String GHS02_FLAME = "GHS02";
	private static final String GHS07_EXCLAMATION_MARK = "GHS07";

	/** Classification of the safety data sheets of the hazardous raw materials, consistent with their ingredients. */
	private static final CodedPhrase H226 = new CodedPhrase("H226", "Flammable liquid and vapour.");
	private static final CodedPhrase H317 = new CodedPhrase("H317", "May cause an allergic skin reaction.");
	private static final CodedPhrase H319 = new CodedPhrase("H319", "Causes serious eye irritation.");
	private static final CodedPhrase P210 = new CodedPhrase("P210",
			"Keep away from heat, hot surfaces, sparks, open flames and other ignition sources. No smoking.");
	private static final CodedPhrase P261 = new CodedPhrase("P261", "Avoid breathing mist/vapours/spray.");
	private static final CodedPhrase P264 = new CodedPhrase("P264", "Wash hands thoroughly after handling.");
	private static final CodedPhrase P280 = new CodedPhrase("P280", "Wear protective gloves/eye protection.");
	private static final CodedPhrase P302_P352 = new CodedPhrase("P302+P352", "IF ON SKIN: Wash with plenty of water.");
	private static final CodedPhrase P305_P351_P338 = new CodedPhrase("P305+P351+P338",
			"IF IN EYES: Rinse cautiously with water for several minutes. Remove contact lenses, if present and easy to do. Continue rinsing.");
	private static final CodedPhrase P333_P313 = new CodedPhrase("P333+P313", "If skin irritation or rash occurs: Get medical advice/attention.");
	private static final CodedPhrase P337_P313 = new CodedPhrase("P337+P313", "If eye irritation persists: Get medical advice/attention.");
	private static final CodedPhrase PROTECTIVE_GLOVES = new CodedPhrase("GLOVES", "Protective gloves");
	private static final CodedPhrase EYE_PROTECTION = new CodedPhrase("EYES", "Safety glasses with side shields");

	/** Handling of the hazardous raw materials, consistent with their classification. */
	private static final Map<String, HandlingSpec> RAW_MATERIAL_HANDLING = Map.of(
			FRAGRANCE, new HandlingSpec(List.of(P210, P261, P280, P302_P352, P333_P313), List.of(PROTECTIVE_GLOVES, EYE_PROTECTION),
					"Store in a cool, well-ventilated place away from ignition sources. Ground the containers when transferring.",
					new TransportSpec("UN1266", "3", "III")),
			PRESERVATIVE, new HandlingSpec(List.of(P264, P280, P305_P351_P338, P337_P313), List.of(EYE_PROTECTION, PROTECTIVE_GLOVES),
					"Avoid contact with eyes. Keep the container tightly closed.", null),
			CITRIC_ACID, new HandlingSpec(List.of(P264, P280, P305_P351_P338, P337_P313), List.of(EYE_PROTECTION),
					"Avoid dust formation. Keep the container dry and tightly closed.", null));

	private static final Map<String, HazardSpec> RAW_MATERIAL_HAZARDS = Map.of(
			FRAGRANCE, new HazardSpec(WARNING, List.of(H226, H317), List.of(GHS02_FLAME, GHS07_EXCLAMATION_MARK)),
			PRESERVATIVE, new HazardSpec(WARNING, List.of(H319), List.of(GHS07_EXCLAMATION_MARK)),
			CITRIC_ACID, new HazardSpec(WARNING, List.of(H319), List.of(GHS07_EXCLAMATION_MARK)));

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
	private static final List<AllergenShare> FRAGRANCE_ALLERGENS = List.of(new AllergenShare("Linalool", 1.5),
			new AllergenShare("Limonene", 1d), new AllergenShare("Geraniol", 0.5), new AllergenShare("Citronellol", 0.5));

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

	private static final List<PackagingSpec> PACKAGING = List.of(new PackagingSpec("Bottle HDPE 200 mL", "HDPE", 22d, PackagingLevel.Primary, 1d),
			new PackagingSpec("Flip-top cap PP 24-410", "PP", 4d, PackagingLevel.Primary, 1d),
			new PackagingSpec("Self-adhesive label PP 120x60", "PP", 0.5, PackagingLevel.Primary, 1d),
			new PackagingSpec("Corrugated case x12", "Corrugated cardboard", 180d, PackagingLevel.Secondary, 1d / 12d));

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
	 * Creates the body milk finished product, its raw materials, packaging materials and characteristics. Each data list
	 * is only created when the builder enables it; the identity, consumer information, manufacturer and safety data
	 * sheets are always set. The score and specification options are not used.
	 *
	 * @return the created finished product
	 */
	@Override
	public FinishedProductData createTestProduct() {
		registerIngredients();

		FinishedProductData bodyMilk = FinishedProductData.build().withName(uniqueName(PRODUCT_NAME)).withLegalName(LEGAL_NAME)
				.withUnit(ProductUnit.L).withQty(0.2d).withDensity(0.98d);
		if (isWithCompo) {
			bodyMilk.withCompoList(createCompoList());
		}
		addProductLists(bodyMilk);
		fillIdentity(bodyMilk);
		bodyMilk.setReportLocales(List.of(REPORT_LOCALE_ENGLISH));
		alfrescoRepository.create(destFolder, bodyMilk);
		fillConsumerInformation(bodyMilk.getNodeRef());
		fillRegulatoryUse(bodyMilk);
		if (isWithToxicology) {
			requestIngredientToxicityComputation();
		}
		attachReportTemplates(bodyMilk.getNodeRef());
		attachCosmeticRawMaterialSheet(bodyMilk);

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
		setIngredientToxicology("PARFUM", 500d, 100d);
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
	 * Creates the raw materials and the composition lines of the formula, in percent of the bulk. The lines are
	 * declared, so the label lists the INCI ingredients of the raw materials, not the raw materials themselves.
	 *
	 * @return the composition list
	 */
	private List<CompoListDataItem> createCompoList() {
		List<CompoListDataItem> compoList = new ArrayList<>();
		for (RawMaterialSpec spec : FORMULA) {
			compoList.add(CompoListDataItem.build().withQtyUsed(spec.qtyUsed()).withUnit(ProductUnit.Perc)
					.withDeclarationType(DeclarationType.Declare).withProduct(createRawMaterial(spec)));
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
		NodeRef supplierPlant = getOrCreateLocatedCharact(spec.supplier() + PLANT_SUFFIX, PLMModel.TYPE_PLANT, SUPPLIER_ADDRESSES.get(spec.supplier()));
		RawMaterialData rawMaterial = RawMaterialData.build().withName(uniqueName(spec.name())).withLegalName(spec.name()).withQty(100d)
				.withUnit(ProductUnit.kg).withIngList(createIngList(spec.ingredients()))
				.withGeoOrigins(List.of(CharactTestHelper.getOrCreateGeo(nodeService, spec.originCode(), spec.originName())));
		rawMaterial.setSuppliers(List.of(getOrCreateLocatedCharact(spec.supplier(), PLMModel.TYPE_SUPPLIER, SUPPLIER_ADDRESSES.get(spec.supplier()))));
		rawMaterial.setSupplierPlants(List.of(supplierPlant));
		rawMaterial.setReportLocales(List.of(REPORT_LOCALE_ENGLISH));
		addPhysicoChem(rawMaterial, new PhysicoChemSpec(HEAVY_METALS, PPM, 2d, null, 10d));
		if (isWithAllergens && FRAGRANCE.equals(spec.name())) {
			rawMaterial.setAllergenList(createAllergenList());
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
	 * Adds the supplier safety data sheet to a raw material: its reference, and its classification when the raw
	 * material is hazardous.
	 *
	 * @param rawMaterialNodeRef the raw material
	 * @param name the raw material name the sheet number derives from
	 */
	private void addSafetyDataSheet(NodeRef rawMaterialNodeRef, String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PROP_SDS_SHEET_NUMBER, SDS_NUMBER_PREFIX + name.toUpperCase(Locale.ROOT).replaceAll(SDS_NUMBER_SEPARATORS, SDS_NUMBER_SEPARATOR));
		properties.put(PROP_SDS_SHEET_EFFECTIVE_DATE, new Date());
		nodeService.addAspect(rawMaterialNodeRef, ASPECT_SDS, properties);
		if (RAW_MATERIAL_HAZARDS.containsKey(name)) {
			addHazardClassification(rawMaterialNodeRef, RAW_MATERIAL_HAZARDS.get(name));
			addHandling(rawMaterialNodeRef, RAW_MATERIAL_HANDLING.get(name));
		}
	}

	/**
	 * Sets the handling section of a safety data sheet: precautionary statements, personal protections, safety
	 * instructions and, for a dangerous good, its transport classification.
	 *
	 * @param rawMaterialNodeRef the raw material holding the safety data sheet
	 * @param handling the handling section of the sheet
	 */
	private void addHandling(NodeRef rawMaterialNodeRef, HandlingSpec handling) {
		nodeService.setAssociations(rawMaterialNodeRef, ASSOC_SDS_PRECAUTIONARY_STATEMENTS, getOrCreateCodedPhrases(handling.precautions(),
				PlmRepoConsts.PATH_PRECAUTIONARY_STATEMENTS, GHSModel.TYPE_PRECAUTIONARY_STATEMENT, PROP_PRECAUTIONARY_CODE));
		nodeService.setAssociations(rawMaterialNodeRef, ASSOC_SDS_PERSONAL_PROTECTIONS, getOrCreateProtections(handling.protections()));
		nodeService.setProperty(rawMaterialNodeRef, PROP_SDS_SAFETY_INSTRUCTIONS, handling.safetyInstructions());
		if (handling.transport() != null) {
			addTransport(rawMaterialNodeRef, handling.transport());
		}
	}

	/**
	 * Sets the transport classification of a dangerous good.
	 *
	 * @param rawMaterialNodeRef the raw material holding the safety data sheet
	 * @param transport the UN number, hazard class and packing group
	 */
	private void addTransport(NodeRef rawMaterialNodeRef, TransportSpec transport) {
		nodeService.setAssociations(rawMaterialNodeRef, ASSOC_SDS_ONU_CODE, List.of(
				getOrCreateSecurityValue(PlmRepoConsts.PATH_ONU_CODES, GHSModel.TYPE_ONU_CODE, PROP_ONU_CODE_NUMBER, transport.unNumber())));
		nodeService.setAssociations(rawMaterialNodeRef, ASSOC_SDS_CLASS_CODE, List.of(
				getOrCreateSecurityValue(PlmRepoConsts.PATH_CLASS_CODES, GHSModel.TYPE_CLASS_CODE, PROP_CLASS_CODE_NUMBER, transport.hazardClass())));
		nodeService.setAssociations(rawMaterialNodeRef, ASSOC_SDS_PACKAGING_GROUP_CODE, List.of(getOrCreateSecurityValue(
				PlmRepoConsts.PATH_PACKAGING_GROUP_CODES, GHSModel.TYPE_PACKAGING_GROUP_CODE, PROP_PACKAGING_GROUP_CODE_NUMBER, transport.packingGroup())));
	}

	/**
	 * Gets or creates coded phrases of a security list, each one named by its code followed by its phrase so the
	 * reports print the phrase. The node name stays the code, so a value created by the other sample products is
	 * reused.
	 *
	 * @param phrases the coded phrases
	 * @param listName the name of the security list
	 * @param type the type of the list values
	 * @param codeProperty the property holding the code
	 * @return the list values
	 */
	private List<NodeRef> getOrCreateCodedPhrases(List<CodedPhrase> phrases, String listName, QName type, QName codeProperty) {
		List<NodeRef> values = new ArrayList<>();
		for (CodedPhrase phrase : phrases) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, phrase.code() + PHRASE_SEPARATOR + phrase.phrase());
			properties.put(codeProperty, phrase.code());
			values.add(CharactTestHelper.getOrCreateNode(nodeService, SECURITY_LISTS_PATH + listName, phrase.code(), type, properties));
		}
		return values;
	}

	/**
	 * Gets or creates personal protections, each one named by its description.
	 *
	 * @param protections the coded personal protections
	 * @return the personal protections
	 */
	private List<NodeRef> getOrCreateProtections(List<CodedPhrase> protections) {
		List<NodeRef> values = new ArrayList<>();
		for (CodedPhrase protection : protections) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, protection.phrase());
			properties.put(PROP_PERSONAL_PROTECTION_CODE, protection.code());
			values.add(CharactTestHelper.getOrCreateNode(nodeService, SECURITY_LISTS_PATH + PlmRepoConsts.PATH_PERSONAL_PROTECTIONS, protection.code(),
					GHSModel.TYPE_PERSONAL_PROTECTION, properties));
		}
		return values;
	}

	/**
	 * Gets or creates a coded value of a security list, named by its code.
	 *
	 * @param listName the name of the security list
	 * @param type the type of the list value
	 * @param codeProperty the property holding the code
	 * @param code the code
	 * @return the list value
	 */
	private NodeRef getOrCreateSecurityValue(String listName, QName type, QName codeProperty, String code) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, code);
		properties.put(codeProperty, code);
		return CharactTestHelper.getOrCreateNode(nodeService, SECURITY_LISTS_PATH + listName, code, type, properties);
	}

	/**
	 * Sets the signal word, the hazard statements and the pictograms of a safety data sheet.
	 *
	 * @param rawMaterialNodeRef the raw material holding the safety data sheet
	 * @param hazard the classification of the sheet
	 */
	private void addHazardClassification(NodeRef rawMaterialNodeRef, HazardSpec hazard) {
		nodeService.setProperty(rawMaterialNodeRef, PROP_SDS_SIGNAL_WORD, hazard.signalWord());
		nodeService.setAssociations(rawMaterialNodeRef, ASSOC_SDS_HAZARD_STATEMENTS, getOrCreateCodedPhrases(hazard.hazardStatements(),
				PlmRepoConsts.PATH_HAZARD_STATEMENTS, GHSModel.TYPE_HAZARD_STATEMENT, GHSModel.PROP_HAZARD_CODE));
		List<NodeRef> pictograms = new ArrayList<>();
		for (String code : hazard.pictograms()) {
			pictograms.add(CharactTestHelper.getOrCreatePicto(nodeService, code));
		}
		nodeService.setAssociations(rawMaterialNodeRef, ASSOC_SDS_PICTOGRAMS, pictograms);
	}

	/**
	 * Finds the INCI functions of the ingredients of the formula.
	 *
	 * @return the ingredient types, without duplicates
	 */
	private List<NodeRef> findFormulaIngredientTypes() {
		Set<NodeRef> ingredientTypes = new LinkedHashSet<>();
		for (RawMaterialSpec spec : FORMULA) {
			for (IngredientShare ingredient : spec.ingredients()) {
				Serializable ingredientType = nodeService.getProperty(CharactTestHelper.getOrCreateIng(nodeService, ingredient.inciName()),
						PLMModel.PROP_ING_TYPE_V2);
				if (ingredientType instanceof NodeRef ingredientTypeNodeRef) {
					ingredientTypes.add(ingredientTypeNodeRef);
				}
			}
		}
		return new ArrayList<>(ingredientTypes);
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
	 * Adds the lists the builder enables: physico-chemical specifications, exposure scenario, and the optional lists
	 * whose creation methods this product overrides.
	 *
	 * @param bodyMilk the finished product, not created yet
	 */
	private void addProductLists(FinishedProductData bodyMilk) {
		if (isWithPhysico) {
			createPhysicoChems(bodyMilk);
		}
		if (isWithToxicology) {
			bodyMilk.setToxList(createToxList());
		}
		addOptionalLists(bodyMilk);
	}

	/**
	 * Adds the physico-chemical specifications of the finished product, with their tolerances.
	 *
	 * @param product the finished product
	 */
	@Override
	protected void createPhysicoChems(ProductData product) {
		FINISHED_PRODUCT_SPECIFICATIONS.forEach(spec -> addPhysicoChem(product, spec));
	}

	/**
	 * Creates the primary packaging (bottle, cap and label) and the secondary packaging (case of 12).
	 *
	 * @return the packaging list
	 */
	@Override
	protected List<PackagingListDataItem> createPackagingList() {
		return buildPackagingList(PACKAGING);
	}

	/**
	 * Creates the manufacturing process steps of the emulsion.
	 *
	 * @return the process list
	 */
	@Override
	protected List<ProcessListDataItem> createProcessList() {
		return buildProcessList(MANUFACTURING_PROCESS);
	}

	/**
	 * Creates the labeling rules of the INCI list: a flat list of INCI names by decreasing concentration. The INCI
	 * functions stay on the ingredients but are not declared, so they do not group the list as food additive classes
	 * would.
	 *
	 * @return the labeling rules
	 */
	@Override
	protected List<LabelingRuleListDataItem> createLabelingRules() {
		LabelingRuleListDataItem hideFunctions = LabelingRuleListDataItem.build().withName(HIDE_INCI_FUNCTIONS_RULE)
				.withLabelingRuleType(LabelingRuleType.DoNotDeclare).withComponents(findFormulaIngredientTypes());
		return new ArrayList<>(List.of(createInciRenderRule(), hideFunctions));
	}

	/**
	 * Creates the claims the customer has to support in the claim annex.
	 *
	 * @return the claim list
	 */
	@Override
	protected List<LabelClaimListDataItem> createClaimList() {
		return new ArrayList<>(List.of(createClaim(CLAIM_MOISTURIZING_CODE, CLAIM_MOISTURIZING, null),
				createClaim(CLAIM_NATURAL_ORIGIN_CODE, CLAIM_NATURAL_ORIGIN, 97d), createClaim(CLAIM_VEGAN_CODE, CLAIM_VEGAN, null)));
	}

	/**
	 * Creates the microbiological limits of ISO 17516 for products other than those for children under 3, eye area
	 * and mucous membranes.
	 *
	 * @return the microbiological list
	 */
	@Override
	protected List<MicrobioListDataItem> createMicrobioList() {
		return buildMicrobioList(MICROBIOLOGICAL_LIMITS);
	}

	/**
	 * Creates the fragrance allergens declared by the fragrance supplier.
	 *
	 * @return the allergen list of the fragrance raw material
	 */
	@Override
	protected List<AllergenListDataItem> createAllergenList() {
		return buildAllergenList(FRAGRANCE_ALLERGENS);
	}

	/**
	 * Creates the market of the product: body lotion sold in France and in the European Union.
	 *
	 * @return the regulatory list
	 */
	@Override
	protected List<RegulatoryListDataItem> createMarketList() {
		return buildMarketList(List.of(getOrCreateCountryRef(FRANCE_CODE, FRANCE), getOrCreateCountryRef(EU_CODE, EUROPEAN_UNION)),
				List.of(getOrCreateUsageRef(USAGE_BODY_LOTION, USAGE_BODY_LOTION_CODE)));
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
	 * Creates the exposure scenario of an adult applying a leave-on body lotion.
	 *
	 * @return the toxicology list
	 */
	@Override
	protected List<ToxListDataItem> createToxList() {
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

		if (bodyMilk.getRegulatoryList() != null) {
			for (RegulatoryListDataItem market : bodyMilk.getRegulatoryList()) {
				nodeService.setProperty(market.getNodeRef(), PLMModel.PROP_REGULATORY_LEGAL_STATUS, LEGAL_STATUS_COSMETIC);
			}
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
			if (isEnabledTemplateOf(template, PLMModel.TYPE_FINISHEDPRODUCT)
					&& (holdsReportKind(template, REPORT_KIND_COMPO_QUALI_QUANTI) || isPifTemplate(template))) {
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
	 * Associates each raw material of the composition with the cosmetic raw material sheet, which the raw material
	 * annex of the PIF embeds. The sheet is not a system template, so a raw material only gets it when associated.
	 *
	 * @param bodyMilk the created finished product
	 */
	private void attachCosmeticRawMaterialSheet(FinishedProductData bodyMilk) {
		if (bodyMilk.getCompoList() == null) {
			return;
		}
		List<NodeRef> sheets = findEnabledTemplates(PLMModel.TYPE_RAWMATERIAL, PlmRepoConsts.REPORT_KIND_COSMETIC_SUPPLIER_SHEET);
		if (sheets.isEmpty()) {
			logger.warn("No cosmetic raw material sheet found: the raw material annex of the PIF stays empty");
			return;
		}
		for (CompoListDataItem compoItem : bodyMilk.getCompoList()) {
			nodeService.addAspect(compoItem.getProduct(), ReportModel.ASPECT_REPORT_TEMPLATES, new HashMap<>());
			nodeService.setAssociations(compoItem.getProduct(), ReportModel.ASSOC_REPORT_TEMPLATES, sheets);
		}
	}

	/**
	 * Finds the enabled report templates of a type holding a report kind.
	 *
	 * @param type the type the templates apply to
	 * @param reportKind the report kind
	 * @return the templates
	 */
	private List<NodeRef> findEnabledTemplates(QName type, String reportKind) {
		List<NodeRef> templates = new ArrayList<>();
		for (NodeRef template : BeCPGQueryBuilder.createQuery().ofType(ReportModel.TYPE_REPORT_TPL).inDB()
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list()) {
			if (isEnabledTemplateOf(template, type) && holdsReportKind(template, reportKind)) {
				templates.add(template);
			}
		}
		return templates;
	}

	/**
	 * Tells whether a report template applies to a type and is enabled.
	 *
	 * @param template the report template
	 * @param type the product type
	 * @return true if the template is an enabled template of the type
	 */
	private boolean isEnabledTemplateOf(NodeRef template, QName type) {
		return type.equals(nodeService.getProperty(template, ReportModel.PROP_REPORT_TPL_CLASS_NAME))
				&& !Boolean.TRUE.equals(nodeService.getProperty(template, ReportModel.PROP_REPORT_TPL_IS_DISABLED));
	}

	/**
	 * Tells whether a report template holds a report kind.
	 *
	 * @param template the report template
	 * @param reportKind the report kind
	 * @return true if the template holds the report kind
	 */
	private boolean holdsReportKind(NodeRef template, String reportKind) {
		Serializable reportKinds = nodeService.getProperty(template, ReportModel.PROP_REPORT_KINDS);
		return reportKinds instanceof List<?> kinds && kinds.contains(reportKind);
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
	 * Gets or creates a supplier or a plant with its address, in the destination folder.
	 *
	 * @param name the supplier or plant name
	 * @param type the supplier or plant type
	 * @param address the postal address
	 * @return the supplier or plant
	 */
	private NodeRef getOrCreateLocatedCharact(String name, QName type, String address) {
		NodeRef charact = getOrCreateCharact(name + RUN_SEPARATOR + runId, type);
		nodeService.setProperty(charact, PROP_LOCATION_ADDRESS1, address);
		return charact;
	}
}
