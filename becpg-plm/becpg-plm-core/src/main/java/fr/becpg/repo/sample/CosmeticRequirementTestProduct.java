package fr.becpg.repo.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
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
 * Products checking how the beCPG regulatory compliance check picks the cosmetic requirements that apply to a product,
 * one per {@link CosmeticScenario}: by age, by use mode, by application zone, by exposure route, by professional use
 * and by closest product type.
 * <p>
 * Every product is a raw material sold in France, under the cosmetic legal status unless the scenario names another
 * one, for a single product type. Its recipe holds one regulated ingredient and water for the rest. The product
 * attributes the scenario does not name are left empty, not set to their default value, as the check runs an empty
 * attribute once per possible value and keeps the worst result. The ingredients are created for the product alone, in a folder next to it, so that each one
 * carries exactly the identifiers and the ingredient type of the scenario.
 */
public class CosmeticRequirementTestProduct extends StandardSoapTestProduct {

	/** The cosmetic situations a product illustrates. */
	public enum CosmeticScenario {
		/** Zinc toothpaste from 3 years: the clause above 1 year applies. */
		ZINC_TOOTHPASTE_FROM_3_YEARS,
		/** Zinc toothpaste from 6 months: the clause from 6 months to 1 year applies. */
		ZINC_TOOTHPASTE_FROM_6_MONTHS,
		/** Zinc toothpaste from birth: no clause covers birth, the youngest one above it applies. */
		ZINC_TOOTHPASTE_FROM_BIRTH,
		/** Zinc toothpaste with no target population: checked for every age. */
		ZINC_TOOTHPASTE_NO_TARGET_POPULATION,
		/** Zinc mouth wash for adults, above the mouth wash limit. */
		ZINC_MOUTH_WASH_ADULT,
		/** Zinc mouth wash from 3 years: the mouth wash clause from 6 years still binds younger users. */
		ZINC_MOUTH_WASH_FROM_3_YEARS,
		/** Zinc mouth wash for adults, below the mouth wash limit. */
		ZINC_MOUTH_WASH_ADULT_LOW_DOSE,
		/** Hexyl salicylate perfume for adults: the clause for children below 3 does not match. */
		HEXYL_SALICYLATE_PERFUME_ADULT,
		/** Hexyl salicylate perfume from 6 months: the clause for children below 3 survives the closer perfume clause. */
		HEXYL_SALICYLATE_PERFUME_FROM_6_MONTHS,
		/** Hexyl salicylate perfume with no target population: checked for every age. */
		HEXYL_SALICYLATE_PERFUME_NO_TARGET_POPULATION,
		/** Hexyl salicylate perfume from 3 years: the clause for children below 3 ends where the product starts. */
		HEXYL_SALICYLATE_PERFUME_FROM_3_YEARS,
		/** Hexyl salicylate perfume from 6 months, below every limit. */
		HEXYL_SALICYLATE_PERFUME_FROM_6_MONTHS_LOW_DOSE,
		/** Acetylated vetiver face mask, leave-on. */
		VETIVER_FACE_MASK_LEAVE_ON,
		/** Acetylated vetiver face mask, rinse-off. */
		VETIVER_FACE_MASK_RINSE_OFF,
		/** Acetylated vetiver face mask, brief contact: neither the leave-on nor the rinse-off clause reaches it. */
		VETIVER_FACE_MASK_BRIEF_CONTACT,
		/** Acetylated vetiver face mask with no use mode: checked for every use mode. */
		VETIVER_FACE_MASK_NO_USE_MODE,
		/** Propylparaben cleansing wipe for the nappy area, brief contact, from birth. */
		PROPYLPARABEN_WIPE_FROM_BIRTH,
		/** Propylparaben cleansing wipe for the nappy area, above the preservative limit. */
		PROPYLPARABEN_WIPE_FROM_BIRTH_HIGH_DOSE,
		/** Propylparaben cleansing wipe for the nappy area with no use mode: checked for every use mode. */
		PROPYLPARABEN_WIPE_NO_USE_MODE,
		/** Propylparaben leave-on cream for the nappy area, from birth: the ban for children under 3 replaces the limit. */
		PROPYLPARABEN_NAPPY_CREAM_FROM_BIRTH,
		/** Propylparaben leave-on cream for the nappy area, from 3 years: the ban ends where the product starts. */
		PROPYLPARABEN_NAPPY_CREAM_FROM_3_YEARS,
		/** Propylparaben leave-on lotion for the body, from birth: the ban is for the nappy area only. */
		PROPYLPARABEN_BODY_LOTION_FROM_BIRTH,
		/** Propylparaben leave-on intimate cream for adults: the ban is for children under 3 only. */
		PROPYLPARABEN_INTIMATE_CREAM_ADULT,
		/** Acetylated vetiver face cream: the leave-on clause applies. */
		VETIVER_FACE_CREAM,
		/** Acetylated vetiver foundation: the make-up clause applies. */
		VETIVER_FOUNDATION,
		/** Acetylated vetiver deodorant: the deodorant clause applies. */
		VETIVER_DEODORANT,
		/** Acetylated vetiver perfume: the fragrance clause applies. */
		VETIVER_PERFUME,
		/** Acetylated vetiver perfume with no use mode: the fragrance clause replaces the rinse-off one. */
		VETIVER_PERFUME_NO_USE_MODE,
		/** Salicylic acid leave-in hair spray for adults, within both lines. */
		SALICYLIC_ACID_SPRAY_ADULT,
		/** Salicylic acid leave-in hair spray for adults, above the preservative line only. */
		SALICYLIC_ACID_SPRAY_ADULT_HIGH_DOSE,
		/** Salicylic acid leave-in hair spray from 6 months: the bans for children under 3 of both lines apply. */
		SALICYLIC_ACID_SPRAY_FROM_6_MONTHS,
		/** Salicylic acid leave-in hair spray with no target population: checked for every age. */
		SALICYLIC_ACID_SPRAY_NO_TARGET_POPULATION,
		/** Salicylic acid leave-in hair spray from 3 years: both bans for children under 3 end where it starts. */
		SALICYLIC_ACID_SPRAY_FROM_3_YEARS,
		/** Sodium hydroxide hair relaxer for professional use. */
		SODIUM_HYDROXIDE_RELAXER_PROFESSIONAL,
		/** Sodium hydroxide hair relaxer for general use. */
		SODIUM_HYDROXIDE_RELAXER_GENERAL_USE,
		/** Sodium hydroxide hair relaxer with no professional use: checked for both. */
		SODIUM_HYDROXIDE_RELAXER_NO_PROFESSIONAL_USE,
		/** Sodium hydroxide hair relaxer for general use, typed as pH adjuster: the pH clause does not lift the general limit. */
		SODIUM_HYDROXIDE_RELAXER_GENERAL_USE_PH_ADJUSTER,
		/** Benzyl salicylate aerosol deodorant, within the spray clause. */
		BENZYL_SALICYLATE_AEROSOL_DEODORANT,
		/** Benzyl salicylate aerosol deodorant, above the spray clause. */
		BENZYL_SALICYLATE_AEROSOL_DEODORANT_HIGH_DOSE,
		/**
		 * Benzyl salicylate roll-on deodorant with no exposure route: checked for every route. The data stores the
		 * leave-on non-spray clause with the powder or inhalable route, so only the run over that route refuses it.
		 */
		BENZYL_SALICYLATE_ROLL_ON,
		/** Benzyl salicylate roll-on deodorant with no exposure route, below every limit. */
		BENZYL_SALICYLATE_ROLL_ON_LOW_DOSE,
		/** Basic Blue 26 colorant body lotion with no mucous contact: the ban on mucous membranes does not apply. */
		CI_44045_BODY_LOTION_NO_MUCOUS_CONTACT,
		/** Basic Blue 26 colorant body lotion with no mucous contact declared: checked with and without. */
		CI_44045_BODY_LOTION_NO_DECLARED_MUCOUS_CONTACT,
		/** Basic Blue 26 colorant body lotion in contact with mucous membranes: the ban applies. */
		CI_44045_BODY_LOTION_MUCOUS_CONTACT,
		/** Safrole toothpaste for adults: the oral hygiene limit, in ppm, applies. */
		SAFROLE_TOOTHPASTE_ADULT,
		/** Safrole toothpaste with no target population: the ban on toothpaste for children applies. */
		SAFROLE_TOOTHPASTE_NO_TARGET_POPULATION,
		/** Safrole body lotion for adults: the limit on every cosmetic, in ppm, applies. */
		SAFROLE_BODY_LOTION_ADULT,
		/** Sodium hydroxide body lotion typed as pH adjuster: the pH clause alone applies, and pH is not compared. */
		SODIUM_HYDROXIDE_BODY_LOTION_PH_ADJUSTER,
		/** Sodium hydroxide body lotion with no ingredient type: the clause on pH adjusters reaches it all the same. */
		SODIUM_HYDROXIDE_BODY_LOTION_UNTYPED,
		/** Propylparaben leave-on cream from birth with no application zone: checked for every zone. */
		PROPYLPARABEN_NAPPY_CREAM_NO_APPLICATION_ZONE,
		/** Propylparaben leave-on cream from birth for the body and the nappy area: one shared zone is enough for the ban. */
		PROPYLPARABEN_BODY_AND_NAPPY_CREAM_FROM_BIRTH,
		/** Propylparaben leave-on cream for the nappy area, from birth, at 0 %: a prohibited ingredient that is absent. */
		PROPYLPARABEN_NAPPY_CREAM_FROM_BIRTH_ZERO_DOSE,
		/** Benzyl salicylate body lotion typed as preservative: it is not listed among the authorized preservatives. */
		BENZYL_SALICYLATE_BODY_LOTION_PRESERVATIVE,
		/** Body lotion with an unidentified trade name typed as preservative: the preservative whitelist refuses it. */
		UNIDENTIFIED_PRESERVATIVE_BODY_LOTION,
		/** Body lotion with an unidentified trade name typed as perfuming: no whitelist applies to it. */
		UNIDENTIFIED_PERFUMING_BODY_LOTION,
		/** Propylparaben cleansing wipe above the preservative limit, with no ingredient type. */
		PROPYLPARABEN_WIPE_UNTYPED_HIGH_DOSE,
		/** Propylparaben cleansing wipe above the preservative limit, with an ingredient type the service does not know. */
		PROPYLPARABEN_WIPE_UNKNOWN_TYPE_HIGH_DOSE,
		/** Zinc toothpaste declared for a product type the service does not know. */
		ZINC_TOOTHPASTE_UNKNOWN_USAGE,
		/** Zinc toothpaste sold under a legal status no regulation source of the service carries. */
		ZINC_TOOTHPASTE_OTC_DRUG
	}

	public static final String WATER = "Water";
	public static final String ZINC_GLUCONATE = "Zinc gluconate";
	public static final String HEXYL_SALICYLATE = "Hexyl salicylate";
	/** A name matching no database record, so that the CAS number keeps every record carrying it. */
	public static final String ACETYLATED_VETIVER = "Acetylated vetiver test";
	public static final String PROPYLPARABEN = "Propylparaben";
	public static final String SALICYLIC_ACID = "Salicylic acid";
	public static final String BENZYL_SALICYLATE = "Benzyl salicylate";
	/** The name of one of the Basic Blue 26 records. */
	public static final String CI_44045 = "CI 44045";
	public static final String SAFROLE = "Safrole";
	/** A trade name matching no database record, with no CAS number. */
	public static final String PRESERVATIVE_BLEND = "Preservative blend test";
	/** A product type code the service does not know. */
	public static final String UNKNOWN_USAGE_CODE = "COSMETIC_UNKNOWN_USAGE_TEST";
	/** An ingredient type code the service does not know; it knows SKIN_CONDITIONING_-_EMOLLIENT. */
	public static final String UNKNOWN_INGREDIENT_TYPE = "EMOLLIENT";
	/** A legal status beCPG offers and no regulation source of the service carries. */
	public static final String LEGAL_STATUS_OTC_DRUG = "OTC_DRUG";

	private static final String PRODUCT_NAME_PREFIX = "Cosmetic requirement - ";
	private static final String INGREDIENTS_FOLDER_SUFFIX = " - ingredients";
	private static final String LEGAL_STATUS_COSMETIC = "COSMETIC";
	private static final String FRANCE_CODE = "FR";
	private static final String FRANCE = "France";
	private static final double TOTAL_PERC = 100d;

	private static final String LEAVE_ON = "LEAVE_ON";
	private static final String RINSE_OFF = "RINSE_OFF";
	private static final String BRIEF_CONTACT = "BRIEF_CONTACT";
	private static final String HAIR = "HAIR";
	private static final String SCALP = "SCALP";
	private static final String FACE = "FACE";
	private static final String ORAL_CAVITY = "ORAL_CAVITY";
	private static final String BODY = "BODY";
	private static final String ANOGENITAL = "ANOGENITAL";
	private static final String PUMP_SPRAY = "PUMP_SPRAY";
	private static final String AEROSOL = "AEROSOL";
	private static final String FROM_BIRTH = "FROM_BIRTH";
	private static final String FROM_6_MONTHS = "FROM_6M";
	private static final String FROM_3_YEARS = "FROM_3Y";
	private static final String ADULT = "ADULT";

	private static final String SOLVENT = "SOLVENT";
	private static final String ORAL_CARE = "ORAL_CARE";
	private static final String PERFUMING = "PERFUMING";
	private static final String PRESERVATIVE = "PRESERVATIVE";
	private static final String HAIR_WAVING_OR_STRAIGHTENING = "HAIR_WAVING_OR_STRAIGHTENING";
	private static final String PH_ADJUSTERS = "pH_ADJUSTERS";
	private static final String COLORANT = "COLORANT";

	private static final ProductType TOOTHPASTE = new ProductType("Toothpaste", "COSMETIC_TOOTHPASTE");
	private static final ProductType MOUTH_WASH = new ProductType("Mouth wash", "COSMETIC_MOUTH_WASH");
	private static final ProductType HYDROALCOHOLIC_PERFUMES = new ProductType("Hydroalcoholic perfumes", "COSMETIC_HYDROALCOHOLIC_PERFUMES");
	private static final ProductType FACE_MASK = new ProductType("Face mask", "COSMETIC_FACE_MASK");
	private static final ProductType OTHER_SKIN_CLEANSING = new ProductType("Other skin cleansing products",
			"COSMETIC_OTHER_SKIN_CLEANSING_PRODUCTS");
	/** The code the body milk sample already gives this usage, kept so that neither sample rewrites the other. */
	private static final ProductType BODY_CARE = new ProductType("Body care products", "COSMETIC_BODY_CARE_PRODUCTS,DECERNIS_Body Cream/Lotion");
	private static final ProductType EXTERNAL_INTIMATE_CARE = new ProductType("External intimate care products",
			"COSMETIC_EXTERNAL_INTIMATE_CARE_PRODUCTS");
	private static final ProductType FACE_CARE = new ProductType("Face care products other than face mask",
			"COSMETIC_FACE_CARE_PRODUCTS_OTHER_THAN_FACE_MASK");
	private static final ProductType FOUNDATION = new ProductType("Foundation", "COSMETIC_FOUNDATION");
	private static final ProductType DEODORANT = new ProductType("Products without antiperspirant activity",
			"COSMETIC_PRODUCTS_WITHOUT_ANTIPERSPIRANT_ACTIVITY");
	private static final ProductType HAIR_CONDITIONER = new ProductType("Hair conditioner", "COSMETIC_HAIR_CONDITIONER");
	private static final ProductType HAIR_RELAXER = new ProductType("Hair relaxer / straightener products",
			"COSMETIC_HAIR_RELAXER_STRAIGHTENER_PRODUCTS");
	private static final ProductType UNKNOWN_USAGE = new ProductType("Unknown usage test", UNKNOWN_USAGE_CODE);

	private static final CosmeticIngredient WATER_INGREDIENT = new CosmeticIngredient(WATER, "7732-18-5", SOLVENT);
	private static final CosmeticIngredient ZINC_GLUCONATE_INGREDIENT = new CosmeticIngredient(ZINC_GLUCONATE, "4468-02-4", ORAL_CARE);
	private static final CosmeticIngredient HEXYL_SALICYLATE_INGREDIENT = new CosmeticIngredient(HEXYL_SALICYLATE, "6259-76-3", PERFUMING);
	private static final CosmeticIngredient ACETYLATED_VETIVER_INGREDIENT = new CosmeticIngredient(ACETYLATED_VETIVER, "84082-84-8", PERFUMING);
	private static final CosmeticIngredient PROPYLPARABEN_INGREDIENT = new CosmeticIngredient(PROPYLPARABEN, "94-13-3", PRESERVATIVE);
	private static final CosmeticIngredient SALICYLIC_ACID_INGREDIENT = new CosmeticIngredient(SALICYLIC_ACID, "69-72-7", PRESERVATIVE);
	private static final CosmeticIngredient SODIUM_HYDROXIDE_INGREDIENT = new CosmeticIngredient(SODIUM_HYDROXIDE, "1310-73-2",
			HAIR_WAVING_OR_STRAIGHTENING);
	private static final CosmeticIngredient BENZYL_SALICYLATE_INGREDIENT = new CosmeticIngredient(BENZYL_SALICYLATE, "118-58-1", PERFUMING);
	private static final CosmeticIngredient CI_44045_INGREDIENT = new CosmeticIngredient(CI_44045, "2580-56-5", COLORANT);
	private static final CosmeticIngredient SAFROLE_INGREDIENT = new CosmeticIngredient(SAFROLE, "94-59-7", PERFUMING);
	private static final CosmeticIngredient PRESERVATIVE_BLEND_INGREDIENT = new CosmeticIngredient(PRESERVATIVE_BLEND, null, PRESERVATIVE);

	private static final RegulatoryAttributes NO_ATTRIBUTES = new RegulatoryAttributes(null, List.of(), List.of(), null, null, null);
	private static final RegulatoryAttributes BODY_LOTION_FOR_ADULTS = NO_ATTRIBUTES.withUseMode(LEAVE_ON).withApplicationZones(BODY)
			.withTargetPopulation(ADULT);

	private static final ScenarioDefinition ZINC_TOOTHPASTE = new ScenarioDefinition(TOOTHPASTE,
			NO_ATTRIBUTES.withUseMode(RINSE_OFF).withApplicationZones(ORAL_CAVITY), ZINC_GLUCONATE_INGREDIENT, 0.9);
	private static final ScenarioDefinition ZINC_MOUTH_WASH = new ScenarioDefinition(MOUTH_WASH,
			NO_ATTRIBUTES.withUseMode(RINSE_OFF).withApplicationZones(ORAL_CAVITY), ZINC_GLUCONATE_INGREDIENT, 0.5);
	private static final ScenarioDefinition HEXYL_SALICYLATE_PERFUME = new ScenarioDefinition(HYDROALCOHOLIC_PERFUMES,
			NO_ATTRIBUTES.withUseMode(LEAVE_ON), HEXYL_SALICYLATE_INGREDIENT, 1);
	private static final ScenarioDefinition VETIVER_FACE_MASK = new ScenarioDefinition(FACE_MASK,
			NO_ATTRIBUTES.withApplicationZones(FACE).withTargetPopulation(ADULT), ACETYLATED_VETIVER_INGREDIENT, 0.15);
	private static final ScenarioDefinition PROPYLPARABEN_WIPE = new ScenarioDefinition(OTHER_SKIN_CLEANSING,
			NO_ATTRIBUTES.withUseMode(BRIEF_CONTACT).withApplicationZones(ANOGENITAL).withTargetPopulation(FROM_BIRTH), PROPYLPARABEN_INGREDIENT, 0.1);
	private static final ScenarioDefinition PROPYLPARABEN_NAPPY_CREAM = new ScenarioDefinition(BODY_CARE,
			NO_ATTRIBUTES.withUseMode(LEAVE_ON).withApplicationZones(ANOGENITAL).withTargetPopulation(FROM_BIRTH), PROPYLPARABEN_INGREDIENT, 0.1);
	private static final ScenarioDefinition VETIVER_LEAVE_ON = new ScenarioDefinition(FACE_CARE,
			NO_ATTRIBUTES.withUseMode(LEAVE_ON).withTargetPopulation(ADULT), ACETYLATED_VETIVER_INGREDIENT, 0.08);
	private static final ScenarioDefinition SALICYLIC_ACID_SPRAY = new ScenarioDefinition(HAIR_CONDITIONER,
			NO_ATTRIBUTES.withUseMode(LEAVE_ON).withApplicationZones(HAIR).withExposureRoutes(PUMP_SPRAY).withTargetPopulation(ADULT),
			SALICYLIC_ACID_INGREDIENT, 0.4);
	private static final ScenarioDefinition SODIUM_HYDROXIDE_RELAXER = new ScenarioDefinition(HAIR_RELAXER,
			NO_ATTRIBUTES.withUseMode(RINSE_OFF).withApplicationZones(HAIR, SCALP).withProfessionalUse(true), SODIUM_HYDROXIDE_INGREDIENT, 3);
	private static final ScenarioDefinition BENZYL_SALICYLATE_ROLL_ON = new ScenarioDefinition(DEODORANT,
			NO_ATTRIBUTES.withUseMode(LEAVE_ON).withApplicationZones(BODY), BENZYL_SALICYLATE_INGREDIENT, 0.7);
	private static final ScenarioDefinition BENZYL_SALICYLATE_AEROSOL = BENZYL_SALICYLATE_ROLL_ON.withAttributes(
			BENZYL_SALICYLATE_ROLL_ON.attributes().withExposureRoutes(AEROSOL));
	private static final ScenarioDefinition SAFROLE_TOOTHPASTE = ZINC_TOOTHPASTE.withIngredient(SAFROLE_INGREDIENT).withQtyPerc(0.008);
	private static final ScenarioDefinition CI_44045_BODY_LOTION = bodyLotion(CI_44045_INGREDIENT, 0.01);
	private static final ScenarioDefinition PRESERVATIVE_BLEND_BODY_LOTION = bodyLotion(PRESERVATIVE_BLEND_INGREDIENT, 0.5);

	/**
	 * The product type the product is declared for, as a regulatory usage.
	 *
	 * @param name the name of the regulatory usage
	 * @param code the regulatory code of the usage, the code of the product type leaf
	 */
	private record ProductType(String name, String code) {
	}

	/**
	 * An ingredient of the recipe.
	 *
	 * @param name the ingredient name
	 * @param cas the CAS number, null when the ingredient has none
	 * @param ingredientType the regulatory code of its ingredient type, its function in the recipe, null when it has none
	 */
	private record CosmeticIngredient(String name, String cas, String ingredientType) {

		/**
		 * @param newIngredientType the regulatory code of another ingredient type, null for none
		 * @return the same ingredient with that type
		 */
		CosmeticIngredient withIngredientType(String newIngredientType) {
			return new CosmeticIngredient(name, cas, newIngredientType);
		}
	}

	/**
	 * The product attributes selecting the requirements that apply. A null or empty attribute is left off the product.
	 *
	 * @param useMode the use mode
	 * @param applicationZones the application zones
	 * @param exposureRoutes the exposure routes
	 * @param targetPopulation the target population
	 * @param professionalUse whether the product is for professional use
	 * @param mucousContact whether the product comes into contact with mucous membranes
	 */
	private record RegulatoryAttributes(String useMode, List<String> applicationZones, List<String> exposureRoutes, String targetPopulation,
			Boolean professionalUse, Boolean mucousContact) {

		RegulatoryAttributes withUseMode(String newUseMode) {
			return new RegulatoryAttributes(newUseMode, applicationZones, exposureRoutes, targetPopulation, professionalUse, mucousContact);
		}

		RegulatoryAttributes withApplicationZones(String... newApplicationZones) {
			return new RegulatoryAttributes(useMode, List.of(newApplicationZones), exposureRoutes, targetPopulation, professionalUse, mucousContact);
		}

		RegulatoryAttributes withExposureRoutes(String... newExposureRoutes) {
			return new RegulatoryAttributes(useMode, applicationZones, List.of(newExposureRoutes), targetPopulation, professionalUse, mucousContact);
		}

		RegulatoryAttributes withTargetPopulation(String newTargetPopulation) {
			return new RegulatoryAttributes(useMode, applicationZones, exposureRoutes, newTargetPopulation, professionalUse, mucousContact);
		}

		RegulatoryAttributes withProfessionalUse(Boolean newProfessionalUse) {
			return new RegulatoryAttributes(useMode, applicationZones, exposureRoutes, targetPopulation, newProfessionalUse, mucousContact);
		}

		RegulatoryAttributes withMucousContact(Boolean newMucousContact) {
			return new RegulatoryAttributes(useMode, applicationZones, exposureRoutes, targetPopulation, professionalUse, newMucousContact);
		}

		/**
		 * @return every attribute of the regulatory product aspect, null for the ones left off the product
		 */
		Map<QName, Serializable> toProperties() {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PLMModel.PROP_REGULATORY_USE_MODE, useMode);
			properties.put(PLMModel.PROP_REGULATORY_APPLICATION_ZONES, applicationZones.isEmpty() ? null : new ArrayList<>(applicationZones));
			properties.put(PLMModel.PROP_REGULATORY_EXPOSURE_ROUTES, exposureRoutes.isEmpty() ? null : new ArrayList<>(exposureRoutes));
			properties.put(PLMModel.PROP_REGULATORY_TARGET_POPULATION, targetPopulation);
			properties.put(PLMModel.PROP_REGULATORY_PROFESSIONAL_USE, professionalUse);
			properties.put(PLMModel.PROP_REGULATORY_MUCOUS_CONTACT, mucousContact);
			return properties;
		}
	}

	/**
	 * Everything a scenario declares.
	 *
	 * @param productType the product type of the single regulatory line
	 * @param attributes the product attributes
	 * @param ingredient the regulated ingredient
	 * @param qtyPerc the quantity of the regulated ingredient, in percent of the product; water makes up the rest
	 * @param legalStatus the legal status of the single regulatory line
	 */
	private record ScenarioDefinition(ProductType productType, RegulatoryAttributes attributes, CosmeticIngredient ingredient, double qtyPerc,
			String legalStatus) {

		/**
		 * A scenario sold under the cosmetic legal status.
		 *
		 * @param productType the product type of the single regulatory line
		 * @param attributes the product attributes
		 * @param ingredient the regulated ingredient
		 * @param qtyPerc the quantity of the regulated ingredient, in percent of the product
		 */
		ScenarioDefinition(ProductType productType, RegulatoryAttributes attributes, CosmeticIngredient ingredient, double qtyPerc) {
			this(productType, attributes, ingredient, qtyPerc, LEGAL_STATUS_COSMETIC);
		}

		ScenarioDefinition withProductType(ProductType newProductType) {
			return new ScenarioDefinition(newProductType, attributes, ingredient, qtyPerc, legalStatus);
		}

		ScenarioDefinition withAttributes(RegulatoryAttributes newAttributes) {
			return new ScenarioDefinition(productType, newAttributes, ingredient, qtyPerc, legalStatus);
		}

		ScenarioDefinition withIngredient(CosmeticIngredient newIngredient) {
			return new ScenarioDefinition(productType, attributes, newIngredient, qtyPerc, legalStatus);
		}

		ScenarioDefinition withQtyPerc(double newQtyPerc) {
			return new ScenarioDefinition(productType, attributes, ingredient, newQtyPerc, legalStatus);
		}

		ScenarioDefinition withLegalStatus(String newLegalStatus) {
			return new ScenarioDefinition(productType, attributes, ingredient, qtyPerc, newLegalStatus);
		}

		ScenarioDefinition withUseMode(String useMode) {
			return withAttributes(attributes.withUseMode(useMode));
		}

		ScenarioDefinition withApplicationZones(String... applicationZones) {
			return withAttributes(attributes.withApplicationZones(applicationZones));
		}

		ScenarioDefinition withTargetPopulation(String targetPopulation) {
			return withAttributes(attributes.withTargetPopulation(targetPopulation));
		}

		ScenarioDefinition withProfessionalUse(Boolean professionalUse) {
			return withAttributes(attributes.withProfessionalUse(professionalUse));
		}

		ScenarioDefinition withMucousContact(Boolean mucousContact) {
			return withAttributes(attributes.withMucousContact(mucousContact));
		}

		ScenarioDefinition withIngredientType(String ingredientType) {
			return withIngredient(ingredient.withIngredientType(ingredientType));
		}
	}

	private final CosmeticScenario scenario;

	private final ScenarioDefinition definition;

	/**
	 * @param builder the builder
	 */
	protected CosmeticRequirementTestProduct(Builder builder) {
		super(builder);
		this.scenario = builder.scenario;
		this.definition = definitionOf(builder.scenario);
	}

	/** Builds a cosmetic requirement product, the zinc toothpaste from 3 years unless another scenario is chosen. */
	public static class Builder extends StandardSoapTestProduct.Builder {
		private CosmeticScenario scenario = CosmeticScenario.ZINC_TOOTHPASTE_FROM_3_YEARS;

		/**
		 * Creates a builder whose product holds its regulatory list.
		 */
		public Builder() {
			withRegulatory(true);
		}

		/**
		 * @param scenario the cosmetic situation the product illustrates
		 * @return this builder
		 */
		public Builder withScenario(CosmeticScenario scenario) {
			this.scenario = scenario;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public CosmeticRequirementTestProduct build() {
			return new CosmeticRequirementTestProduct(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ProductData createTestProduct() {
		String productName = uniqueName(PRODUCT_NAME_PREFIX + scenario.name());
		RawMaterialData product = RawMaterialData.build()
				.withName(productName)
				.withUnit(ProductUnit.kg)
				.withQty(100d)
				.withIngList(createIngredients(createIngredientsFolder(productName)));
		addOptionalLists(product);
		product.setRegulatoryMode(RegulatoryMode.REGULATORY);

		alfrescoRepository.create(destFolder, product);

		setRegulatoryAttributes(product.getNodeRef());
		setMarketsLegalStatus(product);
		return product;
	}

	/**
	 * Creates the single market of the product: France, for the product type of the scenario.
	 *
	 * @return the regulatory list
	 */
	@Override
	protected List<RegulatoryListDataItem> createMarketList() {
		ProductType productType = definition.productType();
		return buildMarketList(List.of(getOrCreateCountryRef(FRANCE_CODE, FRANCE)),
				List.of(getOrCreateUsageRef(productType.name(), productType.code())));
	}

	/**
	 * @param ingredient the regulated ingredient
	 * @param qtyPerc its quantity, in percent of the product
	 * @return a leave-on body lotion for adults holding that ingredient
	 */
	private static ScenarioDefinition bodyLotion(CosmeticIngredient ingredient, double qtyPerc) {
		return new ScenarioDefinition(BODY_CARE, BODY_LOTION_FOR_ADULTS, ingredient, qtyPerc);
	}

	private static ScenarioDefinition definitionOf(CosmeticScenario scenario) {
		return switch (scenario) {
		case ZINC_TOOTHPASTE_FROM_3_YEARS -> ZINC_TOOTHPASTE.withTargetPopulation(FROM_3_YEARS);
		case ZINC_TOOTHPASTE_FROM_6_MONTHS -> ZINC_TOOTHPASTE.withTargetPopulation(FROM_6_MONTHS);
		case ZINC_TOOTHPASTE_FROM_BIRTH -> ZINC_TOOTHPASTE.withTargetPopulation(FROM_BIRTH);
		case ZINC_TOOTHPASTE_NO_TARGET_POPULATION -> ZINC_TOOTHPASTE;
		case ZINC_MOUTH_WASH_ADULT -> ZINC_MOUTH_WASH.withTargetPopulation(ADULT);
		case ZINC_MOUTH_WASH_FROM_3_YEARS -> ZINC_MOUTH_WASH.withTargetPopulation(FROM_3_YEARS);
		case ZINC_MOUTH_WASH_ADULT_LOW_DOSE -> ZINC_MOUTH_WASH.withTargetPopulation(ADULT).withQtyPerc(0.08);
		case HEXYL_SALICYLATE_PERFUME_ADULT -> HEXYL_SALICYLATE_PERFUME.withTargetPopulation(ADULT);
		case HEXYL_SALICYLATE_PERFUME_FROM_6_MONTHS -> HEXYL_SALICYLATE_PERFUME.withTargetPopulation(FROM_6_MONTHS);
		case HEXYL_SALICYLATE_PERFUME_NO_TARGET_POPULATION -> HEXYL_SALICYLATE_PERFUME;
		case HEXYL_SALICYLATE_PERFUME_FROM_3_YEARS -> HEXYL_SALICYLATE_PERFUME.withTargetPopulation(FROM_3_YEARS);
		case HEXYL_SALICYLATE_PERFUME_FROM_6_MONTHS_LOW_DOSE -> HEXYL_SALICYLATE_PERFUME.withTargetPopulation(FROM_6_MONTHS).withQtyPerc(0.08);
		default -> useModeOrZoneDefinitionOf(scenario);
		};
	}

	private static ScenarioDefinition useModeOrZoneDefinitionOf(CosmeticScenario scenario) {
		return switch (scenario) {
		case VETIVER_FACE_MASK_LEAVE_ON -> VETIVER_FACE_MASK.withUseMode(LEAVE_ON);
		case VETIVER_FACE_MASK_RINSE_OFF -> VETIVER_FACE_MASK.withUseMode(RINSE_OFF);
		case VETIVER_FACE_MASK_BRIEF_CONTACT -> VETIVER_FACE_MASK.withUseMode(BRIEF_CONTACT);
		case VETIVER_FACE_MASK_NO_USE_MODE -> VETIVER_FACE_MASK;
		case PROPYLPARABEN_WIPE_FROM_BIRTH -> PROPYLPARABEN_WIPE;
		case PROPYLPARABEN_WIPE_FROM_BIRTH_HIGH_DOSE -> PROPYLPARABEN_WIPE.withQtyPerc(0.2);
		case PROPYLPARABEN_WIPE_NO_USE_MODE -> PROPYLPARABEN_WIPE.withUseMode(null);
		case PROPYLPARABEN_NAPPY_CREAM_FROM_BIRTH -> PROPYLPARABEN_NAPPY_CREAM;
		case PROPYLPARABEN_NAPPY_CREAM_FROM_3_YEARS -> PROPYLPARABEN_NAPPY_CREAM.withTargetPopulation(FROM_3_YEARS);
		case PROPYLPARABEN_BODY_LOTION_FROM_BIRTH -> PROPYLPARABEN_NAPPY_CREAM.withApplicationZones(BODY);
		case PROPYLPARABEN_INTIMATE_CREAM_ADULT -> PROPYLPARABEN_NAPPY_CREAM.withProductType(EXTERNAL_INTIMATE_CARE).withTargetPopulation(ADULT);
		default -> precedenceDefinitionOf(scenario);
		};
	}

	private static ScenarioDefinition precedenceDefinitionOf(CosmeticScenario scenario) {
		return switch (scenario) {
		case VETIVER_FACE_CREAM -> VETIVER_LEAVE_ON;
		case VETIVER_FOUNDATION -> VETIVER_LEAVE_ON.withProductType(FOUNDATION);
		case VETIVER_DEODORANT -> VETIVER_LEAVE_ON.withProductType(DEODORANT);
		case VETIVER_PERFUME -> VETIVER_LEAVE_ON.withProductType(HYDROALCOHOLIC_PERFUMES).withQtyPerc(0.5);
		case VETIVER_PERFUME_NO_USE_MODE -> VETIVER_LEAVE_ON.withProductType(HYDROALCOHOLIC_PERFUMES).withQtyPerc(0.5).withUseMode(null);
		case SALICYLIC_ACID_SPRAY_ADULT -> SALICYLIC_ACID_SPRAY;
		case SALICYLIC_ACID_SPRAY_ADULT_HIGH_DOSE -> SALICYLIC_ACID_SPRAY.withQtyPerc(1);
		case SALICYLIC_ACID_SPRAY_FROM_6_MONTHS -> SALICYLIC_ACID_SPRAY.withTargetPopulation(FROM_6_MONTHS);
		case SALICYLIC_ACID_SPRAY_NO_TARGET_POPULATION -> SALICYLIC_ACID_SPRAY.withTargetPopulation(null);
		case SALICYLIC_ACID_SPRAY_FROM_3_YEARS -> SALICYLIC_ACID_SPRAY.withTargetPopulation(FROM_3_YEARS);
		default -> conditionDefinitionOf(scenario);
		};
	}

	private static ScenarioDefinition conditionDefinitionOf(CosmeticScenario scenario) {
		return switch (scenario) {
		case SODIUM_HYDROXIDE_RELAXER_PROFESSIONAL -> SODIUM_HYDROXIDE_RELAXER;
		case SODIUM_HYDROXIDE_RELAXER_GENERAL_USE -> SODIUM_HYDROXIDE_RELAXER.withProfessionalUse(false);
		case SODIUM_HYDROXIDE_RELAXER_NO_PROFESSIONAL_USE -> SODIUM_HYDROXIDE_RELAXER.withProfessionalUse(null);
		case SODIUM_HYDROXIDE_RELAXER_GENERAL_USE_PH_ADJUSTER -> SODIUM_HYDROXIDE_RELAXER.withProfessionalUse(false)
				.withIngredientType(PH_ADJUSTERS);
		case BENZYL_SALICYLATE_AEROSOL_DEODORANT -> BENZYL_SALICYLATE_AEROSOL;
		case BENZYL_SALICYLATE_AEROSOL_DEODORANT_HIGH_DOSE -> BENZYL_SALICYLATE_AEROSOL.withQtyPerc(1);
		case BENZYL_SALICYLATE_ROLL_ON -> BENZYL_SALICYLATE_ROLL_ON;
		case BENZYL_SALICYLATE_ROLL_ON_LOW_DOSE -> BENZYL_SALICYLATE_ROLL_ON.withQtyPerc(0.4);
		default -> mucousContactOrLimitDefinitionOf(scenario);
		};
	}

	private static ScenarioDefinition mucousContactOrLimitDefinitionOf(CosmeticScenario scenario) {
		return switch (scenario) {
		case CI_44045_BODY_LOTION_NO_MUCOUS_CONTACT -> CI_44045_BODY_LOTION.withMucousContact(false);
		case CI_44045_BODY_LOTION_NO_DECLARED_MUCOUS_CONTACT -> CI_44045_BODY_LOTION;
		case CI_44045_BODY_LOTION_MUCOUS_CONTACT -> CI_44045_BODY_LOTION.withMucousContact(true);
		case SAFROLE_TOOTHPASTE_ADULT -> SAFROLE_TOOTHPASTE.withTargetPopulation(ADULT);
		case SAFROLE_TOOTHPASTE_NO_TARGET_POPULATION -> SAFROLE_TOOTHPASTE;
		case SAFROLE_BODY_LOTION_ADULT -> bodyLotion(SAFROLE_INGREDIENT, 0.008);
		default -> zoneOrIngredientTypeDefinitionOf(scenario);
		};
	}

	private static ScenarioDefinition zoneOrIngredientTypeDefinitionOf(CosmeticScenario scenario) {
		return switch (scenario) {
		case SODIUM_HYDROXIDE_BODY_LOTION_PH_ADJUSTER -> bodyLotion(SODIUM_HYDROXIDE_INGREDIENT.withIngredientType(PH_ADJUSTERS), 1);
		case SODIUM_HYDROXIDE_BODY_LOTION_UNTYPED -> bodyLotion(SODIUM_HYDROXIDE_INGREDIENT.withIngredientType(null), 1);
		case PROPYLPARABEN_NAPPY_CREAM_NO_APPLICATION_ZONE -> PROPYLPARABEN_NAPPY_CREAM.withApplicationZones();
		case PROPYLPARABEN_BODY_AND_NAPPY_CREAM_FROM_BIRTH -> PROPYLPARABEN_NAPPY_CREAM.withApplicationZones(BODY, ANOGENITAL);
		case PROPYLPARABEN_NAPPY_CREAM_FROM_BIRTH_ZERO_DOSE -> PROPYLPARABEN_NAPPY_CREAM.withQtyPerc(0);
		default -> identificationDefinitionOf(scenario);
		};
	}

	private static ScenarioDefinition identificationDefinitionOf(CosmeticScenario scenario) {
		return switch (scenario) {
		case BENZYL_SALICYLATE_BODY_LOTION_PRESERVATIVE -> bodyLotion(BENZYL_SALICYLATE_INGREDIENT.withIngredientType(PRESERVATIVE), 0.1);
		case UNIDENTIFIED_PRESERVATIVE_BODY_LOTION -> PRESERVATIVE_BLEND_BODY_LOTION;
		case UNIDENTIFIED_PERFUMING_BODY_LOTION -> PRESERVATIVE_BLEND_BODY_LOTION.withIngredientType(PERFUMING);
		case PROPYLPARABEN_WIPE_UNTYPED_HIGH_DOSE -> PROPYLPARABEN_WIPE.withQtyPerc(0.2).withIngredientType(null);
		case PROPYLPARABEN_WIPE_UNKNOWN_TYPE_HIGH_DOSE -> PROPYLPARABEN_WIPE.withQtyPerc(0.2).withIngredientType(UNKNOWN_INGREDIENT_TYPE);
		case ZINC_TOOTHPASTE_UNKNOWN_USAGE -> ZINC_TOOTHPASTE.withTargetPopulation(FROM_3_YEARS).withProductType(UNKNOWN_USAGE);
		case ZINC_TOOTHPASTE_OTC_DRUG -> ZINC_TOOTHPASTE.withTargetPopulation(FROM_3_YEARS).withLegalStatus(LEGAL_STATUS_OTC_DRUG);
		default -> throw new IllegalArgumentException("No definition for the cosmetic scenario " + scenario);
		};
	}

	/**
	 * Sets the attributes the scenario names and removes the others, which adding the aspect sets to their default.
	 *
	 * @param productNodeRef the created product
	 */
	private void setRegulatoryAttributes(NodeRef productNodeRef) {
		Map<QName, Serializable> properties = definition.attributes().toProperties();
		nodeService.addAspect(productNodeRef, PLMModel.ASPECT_REGULATORY_PRODUCT, new HashMap<>());
		for (Map.Entry<QName, Serializable> property : properties.entrySet()) {
			if (property.getValue() == null) {
				nodeService.removeProperty(productNodeRef, property.getKey());
			} else {
				nodeService.setProperty(productNodeRef, property.getKey(), property.getValue());
			}
		}
	}

	/**
	 * Sets the legal status of the scenario, the cosmetic one by default, on the market, through the node service, as
	 * the repository mapping of a regulatory line does not hold it.
	 *
	 * @param product the created product
	 */
	private void setMarketsLegalStatus(RawMaterialData product) {
		for (RegulatoryListDataItem market : product.getRegulatoryList()) {
			nodeService.setProperty(market.getNodeRef(), PLMModel.PROP_REGULATORY_LEGAL_STATUS, definition.legalStatus());
		}
	}

	private List<IngListDataItem> createIngredients(NodeRef ingredientsFolder) {
		return new ArrayList<>(List.of(ingListItem(ingredientsFolder, definition.ingredient(), definition.qtyPerc()),
				ingListItem(ingredientsFolder, WATER_INGREDIENT, TOTAL_PERC - definition.qtyPerc())));
	}

	private IngListDataItem ingListItem(NodeRef ingredientsFolder, CosmeticIngredient ingredient, double qtyPerc) {
		return IngListDataItem.build().withQtyPerc(qtyPerc).withIngredient(createIngredient(ingredientsFolder, ingredient));
	}

	private NodeRef createIngredientsFolder(String productName) {
		String folderName = PropertiesHelper.cleanName(productName + INGREDIENTS_FOLDER_SUFFIX);
		return createNode(destFolder, folderName, ContentModel.TYPE_FOLDER, new HashMap<>());
	}

	/**
	 * Creates the ingredient of a recipe line, with no CAS number or no ingredient type when it has none.
	 *
	 * @param parent the folder of the ingredients
	 * @param ingredient the ingredient to create
	 * @return the created ingredient
	 */
	private NodeRef createIngredient(NodeRef parent, CosmeticIngredient ingredient) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, ingredient.name());
		if (ingredient.cas() != null) {
			properties.put(PLMModel.PROP_CAS_NUMBER, ingredient.cas());
		}
		if (ingredient.ingredientType() != null) {
			properties.put(PLMModel.PROP_ING_TYPE_V2, getOrCreateIngredientType(ingredient.ingredientType()));
		}
		return createNode(parent, PropertiesHelper.cleanName(ingredient.name()), PLMModel.TYPE_ING, properties);
	}

	private NodeRef createNode(NodeRef parent, String name, QName type, Map<QName, Serializable> properties) {
		properties.put(ContentModel.PROP_NAME, name);
		return nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), type,
				properties).getChildRef();
	}
}
