package fr.becpg.test.repo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.test.utils.CharactTestHelper;

public class GreenScoreSpecificationTestProduct extends StandardSoapTestProduct {

	private static final String DEFAULT_RANGE = "A: [8), B: [6;8), C: [4;6), D: [2;4), E: [0;2)";

	// Main categories
	public static final String FORMULATION = "1- FORMULATION";
	public static final String MANUFACTURING = "2- MANUFACTURING";
	public static final String TRANSPORT = "3- TRANSPORT";
	public static final String USAGE = "4- USAGE";
	public static final String END_OF_LIFE = "5- END OF LIFE";

	// Formulation subcategories
	public static final String CARBON_SCORE_INGREDIENTS = "INGREDIENTS CARBON SCORE";
	public static final String CARBON_SCORE_INGREDIENTS_WITHOUT_TRANSPORT = "Ingredients carbon score (excluding transport)";
	public static final String CARBON_TRANSPORT_IMPACT = "Transport carbon impact (kCO2/kg/km)";
	public static final String BIODIVERSITY = "BIODIVERSITY";
	public static final String BIOTECH_RAW_MATERIALS_WEIGHT = "Weight of biotech raw materials";
	public static final String UPCYCLED_RAW_MATERIALS_WEIGHT = "Weight of upcycled raw materials";
	public static final String ENVIRONMENTAL_PERFORMANCE = "ENVIRONMENTAL PERFORMANCE";

	public static final String ISO_16128_PERCENTAGE = "Raw materials percentage according to ISO 16128";
	public static final String SOCIETAL_PERFORMANCE = "SOCIETAL PERFORMANCE";

	// Manufacturing subcategories
	public static final String RESOURCE_CONSUMPTION = "RESOURCE CONSUMPTION";
	public static final String RAW_MATERIALS_COUNT = "Raw materials count";
	public static final String ENERGY_INTENSIVE_MATERIALS = "Weight of energy-intensive materials";
	public static final String FACTORY_CARBON_SCORE = "FACTORY CARBON SCORE (excluding transport)";
	public static final String FACTORY_CARBON_INTENSITY = "Factory Carbon Score (intensity)";

	protected GreenScoreSpecificationTestProduct(Builder builder) {
		super(builder);
	}

	public static class Builder extends StandardSoapTestProduct.Builder {

		@Override
		public StandardSoapTestProduct build() {
			return new GreenScoreSpecificationTestProduct((Builder) this.withScore(true).withSpecification(true).withPhysico(true).withCompo(true));
		}

		@Override
		protected Builder self() {
			return this;
		}
	}

	@Override
	protected List<ProductSpecificationData> createProductSpecifications() {

		ProductSpecificationData productSpecification = ProductSpecificationData.build().withName(uniqueName("Green score specification 📋"))
				.withScoreList(createGreenScoreList());

		alfrescoRepository.create(destFolder, productSpecification);
		return List.of(productSpecification);
	}

	@Override
	protected void createPhysicoChems(FinishedProductData soapProduct) {
		super.createPhysicoChems(soapProduct);

		// Add physico-chemical properties
		addPhysicoChemProperty(soapProduct, "Poids des MP issues Biotech", null, 2.7d);
		addPhysicoChemProperty(soapProduct, "Poids des MP upcyclées", null, 0d);
		addPhysicoChemProperty(soapProduct, "Poids des MP énergivores", null, 0d); //--> Claim
		addPhysicoChemProperty(soapProduct, "% de MP selon ISO 16128", null, 0d); //--> Claim

		addLCAProperty(soapProduct, CLIMATE_CHANGE, "CLIMATE_CHANGE", 5.45d);
	}

	private List<ScoreListDataItem> createGreenScoreList() {

		List<ScoreListDataItem> scoreList = new ArrayList<>();

		// 1 - FORMULATION
		ScoreListDataItem formulation = ScoreListDataItem.build().withScoreCriterion(crit(FORMULATION, DEFAULT_RANGE, 50d));
		scoreList.add(formulation);

		// 1.1 - Carbon Score Ingredients
		ScoreListDataItem scoreCarboneIngredients = ScoreListDataItem.build().withParent(formulation)
				.withScoreCriterion(crit(CARBON_SCORE_INGREDIENTS, DEFAULT_RANGE, 18d));
		scoreList.add(scoreCarboneIngredients);

		scoreList.add(ScoreListDataItem.build().withParent(scoreCarboneIngredients)
				.withScoreCriterion(crit(CARBON_SCORE_INGREDIENTS_WITHOUT_TRANSPORT, null, 90d, createLcaFormula(CLIMATE_CHANGE))));

		scoreList.add(ScoreListDataItem.build().withParent(scoreCarboneIngredients).withScoreCriterion(crit(CARBON_TRANSPORT_IMPACT, null, 10d,
				"entity.isRawMaterial() ? @ecoScore.distance(entity.plant.geoOrigins, entity.geoOrigins) * 0,135d : value")));

		// 1.2 - Biodiversity
		ScoreListDataItem biodiversite = ScoreListDataItem.build().withParent(formulation).withScoreCriterion(crit(BIODIVERSITY, DEFAULT_RANGE, 9d));
		scoreList.add(biodiversite);

		scoreList.add(ScoreListDataItem.build().withParent(biodiversite)
				.withScoreCriterion(crit(BIOTECH_RAW_MATERIALS_WEIGHT, null, 50d, createPhysicoFormula("Poids des MP issues Biotech"))));

		scoreList.add(ScoreListDataItem.build().withParent(biodiversite)
				.withScoreCriterion(crit(UPCYCLED_RAW_MATERIALS_WEIGHT, null, 50d, createPhysicoFormula("Poids des MP upcyclées"))));

		// 1.3 - Environmental Performance
		ScoreListDataItem performanceEnvironnementale = ScoreListDataItem.build().withParent(formulation)
				.withScoreCriterion(crit(ENVIRONMENTAL_PERFORMANCE, DEFAULT_RANGE, 36d));
		scoreList.add(performanceEnvironnementale);

		scoreList.add(ScoreListDataItem.build().withParent(performanceEnvironnementale).withScoreCriterion(
				crit(EPI_SCORE, null, 9.1d, "entity.isRawMaterial() ? @ecoScore.countryEPI(entity.geoOrigins): dataListItem.value")));

		scoreList.add(ScoreListDataItem.build().withParent(performanceEnvironnementale)
				.withScoreCriterion(crit(ISO_16128_PERCENTAGE, null, 9.1d, createPhysicoFormula("% de MP selon ISO 16128"))));

		// 1.4 - Societal Performance
		ScoreListDataItem impactsSocietaux = ScoreListDataItem.build().withParent(formulation)
				.withScoreCriterion(crit(SOCIETAL_PERFORMANCE, DEFAULT_RANGE, 36d));
		scoreList.add(impactsSocietaux);

		scoreList.add(ScoreListDataItem.build().withParent(impactsSocietaux).withScoreCriterion(
				crit(SPI_SCORE, null, 66.7d, "entity.isRawMaterial() ? @ecoScore.countrySPI(entity.geoOrigins): dataListItem.value")));

		scoreList.add(ScoreListDataItem.build().withParent(impactsSocietaux).withScoreCriterion(crit(SUPPLIER_ECOVADIS_CERTIFICATION, null, 33.3d)));

		// 2 - MANUFACTURING
		ScoreListDataItem fabrication = ScoreListDataItem.build().withScoreCriterion(crit(MANUFACTURING, DEFAULT_RANGE, 10.71d));
		scoreList.add(fabrication);

		// 2.1 - Resource Consumption
		ScoreListDataItem consommationRessources = ScoreListDataItem.build().withParent(fabrication)
				.withScoreCriterion(crit(RESOURCE_CONSUMPTION, DEFAULT_RANGE, 50d));
		scoreList.add(consommationRessources);

		scoreList.add(ScoreListDataItem.build().withParent(consommationRessources)
				.withScoreCriterion(crit(RAW_MATERIALS_COUNT, null, 42.9d, "entity.compoList.length")));

		scoreList.add(ScoreListDataItem.build().withParent(consommationRessources)
				.withScoreCriterion(crit(ENERGY_INTENSIVE_MATERIALS, null, 57.1d, createPhysicoFormula("Poids des MP énergivores"))));

		// 2.2 - Factory Carbon Score
		ScoreListDataItem scoreCarboneUsine = ScoreListDataItem.build().withParent(fabrication)
				.withScoreCriterion(crit(FACTORY_CARBON_SCORE, DEFAULT_RANGE, 50d));
		scoreList.add(scoreCarboneUsine);

		scoreList.add(ScoreListDataItem.build().withParent(scoreCarboneUsine).withScoreCriterion(crit(FACTORY_CARBON_INTENSITY, null, 100d, "2.5d")));

		// 3 - TRANSPORT
		ScoreListDataItem transport = ScoreListDataItem.build().withScoreCriterion(crit(TRANSPORT, DEFAULT_RANGE, 10.71d));
		scoreList.add(transport);

		// Transport Impacts
		ScoreListDataItem impactsTransport = ScoreListDataItem.build().withParent(transport)
				.withScoreCriterion(crit(TRANSPORT_IMPACTS, DEFAULT_RANGE, 100d));
		scoreList.add(impactsTransport);

		ScoreListDataItem usage = ScoreListDataItem.build().withScoreCriterion(crit(USAGE, DEFAULT_RANGE, 10.7d));
		scoreList.add(usage);

		// 2.4 - Toxicité santé
		ScoreListDataItem toxiciteSante = ScoreListDataItem.build().withParent(usage).withScoreCriterion(crit("TOXICITE SANTE", DEFAULT_RANGE, 100d));
		scoreList.add(toxiciteSante);
		addCriterion(scoreList, toxiciteSante, "ATO (mg/kg)", 1.2d, "entity.hazards.etaVo", new Double[] { 500000.0, 2000.0, 300.0, 50.0, 5.0, 0.0 });
		addCriterion(scoreList, toxiciteSante, "ATD (mg/kg)", 1.2d, "entity.hazards.etaVc",
				new Double[] { 500000.0, 2000.0, 1000.0, 200.0, 50.0, 0.0 });
		addCriterion(scoreList, toxiciteSante, "ATIV (ppm)", 1.2d, "entity.hazards.etaInGas",
				new Double[] { 500000.0, 20000.0, 2500.0, 500.0, 100.0, 0.0 });
		addCriterion(scoreList, toxiciteSante, "AH1", 0.6d, "entity.hazards.hSum(\"AH1\")", new Double[] { 0.0, 0.1, 0.5, 1.0, 3.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "STOT SE 1", 1.2d, "entity.hazards.hSum(\"STOT SE 1\")",
				new Double[] { 0.0, 0.01, 0.1, 1.0, 3.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "STOT SE 2", 1.2d, "entity.hazards.hSum(\"STOT SE 2\")",
				new Double[] { 0.0, 0.01, 0.1, 1.0, 3.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "STOT RE 1", 1.2d, "entity.hazards.hSum(\"STOT RE 1\")",
				new Double[] { 0.0, 0.01, 0.1, 1.0, 3.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "STOT RE 2", 1.2d, "entity.hazards.hSum(\"STOT RE 2\")",
				new Double[] { 0.0, 0.01, 0.1, 1.0, 3.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "Skin Sens. 1A", 6d, "entity.hazards.hSum(\"Skin Sens. 1A\")",
				new Double[] { 0.0, 0.01, 0.1, 1.0, 5.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "Skin Sens. 1B", 6d, "entity.hazards.hSum(\"Skin Sens. 1B\")",
				new Double[] { 0.0, 0.0001, 0.001, 0.01, 0.1, 1.0 });
		addCriterion(scoreList, toxiciteSante, "Skin Sens. 1", 6d, "entity.hazards.hSum(\"Skin Sens. 1\")",
				new Double[] { 0.0, 0.01, 0.1, 1.0, 5.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "S-SS1A", 6d, "entity.hazards.hSum(\"S-SS1A\")", new Double[] { 0.0, 0.01, 0.1, 1.0, 5.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "EDHH1", 6d, "entity.hazards.hSum(\"EDHH1\")", new Double[] { 0.0, 0.01, 0.1, 1.0, 5.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "EDHH2", 6d, "entity.hazards.hSum(\"EDHH2\")", new Double[] { 0.0, 0.01, 0.1, 1.0, 5.0, 10.0 });
		addCriterion(scoreList, toxiciteSante, "Carc. 1A", 6d, "entity.hazards.hSum(\"Carc. 1A\")",
				new Double[] { 0.0, 0.001, 0.005, 0.01, 0.1, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Carc. 1B", 6d, "entity.hazards.hSum(\"Carc. 1B\")",
				new Double[] { 0.0, 0.001, 0.005, 0.01, 0.1, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Carc. 2", 6d, "entity.hazards.hSum(\"Carc. 2\")", new Double[] { 0.0, 0.001, 0.05, 0.1, 1.0, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Muta. 1A", 6d, "entity.hazards.hSum(\"Muta. 1A\")",
				new Double[] { 0.0, 0.001, 0.005, 0.01, 0.1, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Muta. 1B", 6d, "entity.hazards.hSum(\"Muta. 1B\")",
				new Double[] { 0.0, 0.001, 0.005, 0.01, 0.1, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Muta. 2", 6d, "entity.hazards.hSum(\"Muta. 2\")", new Double[] { 0.0, 0.001, 0.05, 0.1, 1.0, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Repr. 1A", 6d, "entity.hazards.hSum(\"Repr. 1A\")",
				new Double[] { 0.0, 0.001, 0.005, 0.01, 0.3, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Repr. 1B", 6d, "entity.hazards.hSum(\"Repr. 1B\")",
				new Double[] { 0.0, 0.001, 0.005, 0.01, 0.3, 100.0 });
		addCriterion(scoreList, toxiciteSante, "Repr. 2", 6d, "entity.hazards.hSum(\"Repr. 2\")", new Double[] { 0.0, 0.001, 0.05, 0.3, 3.0, 100.0 });
		addCriterion(scoreList, toxiciteSante, "REP LACT", 1.2d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.001, 0.005, 0.01, 0.3, 100.0 });

		// 3 - FIN DE VIE
		ScoreListDataItem finDeVie = ScoreListDataItem.build().withScoreCriterion(crit(END_OF_LIFE, DEFAULT_RANGE, 14.3d));
		scoreList.add(finDeVie);

		// 3.1 - Biodégradabilité
		ScoreListDataItem biodegradabilite = ScoreListDataItem.build().withParent(finDeVie)
				.withScoreCriterion(crit("BIODEGRADABILITE", DEFAULT_RANGE, 40d));
		scoreList.add(biodegradabilite);
		scoreList.add(ScoreListDataItem.build().withParent(biodegradabilite)
				.withScoreCriterion(crit("BioDscent", null, 100d, createPhysicoFormula("BioDscent"))));

		// 3.2 - Toxicité environnement
		ScoreListDataItem toxiciteEnvironnement = ScoreListDataItem.build().withParent(finDeVie)
				.withScoreCriterion(crit("TOXICITE ENVIRONNEMENT", DEFAULT_RANGE, 60d));
		scoreList.add(toxiciteEnvironnement);

		scoreList.add(toxiciteEnvironnement);
		addCriterion(scoreList, toxiciteEnvironnement, "EHA1 XMFactor", 26.3d, "entity.hazards.hSum(\"EHA1\",\"M\")",
				new Double[] { 0.0, 0.001, 2.5, 25d, 50d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "EHC1 XMFactor", 13.2d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.001, 2.5, 25d, 50d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "EHC2 XMFactor", 13.2d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.001, 2.5, 25d, 50d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "EHC3 XMFactor", 13.2d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.001, 2.5, 25d, 50d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "EHC4 XMFactor", 2.6d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.001, 2.5, 25d, 50d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "ED ENV1", 13.2d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.010, 0.1, 1d, 10d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "ED ENV2", 13.2d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.001, 2.5, 25d, 50d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "PMT/vPvM", 2.6d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.010, 0.1, 1d, 10d, 100d });
		addCriterion(scoreList, toxiciteEnvironnement, "PBT/vPvB", 2.6d, "entity.hazards.hSum(\"REP LACT\")",
				new Double[] { 0.0, 0.010, 0.1, 1d, 10d, 100d });

		return scoreList;
	}

	private void addCriterion(List<ScoreListDataItem> scoreList, ScoreListDataItem parent, String key, Double weight, String field, Double[] ds) {
		scoreList.add(ScoreListDataItem.build().withParent(parent).withScoreCriterion(
				crit(key, null, weight, String.format("@beCPG.interpolate(%s, [10, 8, 6, 4, 2, 0], %s)", field, Arrays.toString(ds)))));
	}

	private NodeRef crit(String label, String range, Double weight, String formula) {
		NodeRef crit = crit(label);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ProjectModel.PROP_SCORE_CRITERION_WEIGHT, weight);
		properties.put(ProjectModel.PROP_SCORE_CRITERION_RANGE, range);
		properties.put(ProjectModel.PROP_SCORE_CRITERION_FORMULA, formula);

		nodeService.addProperties(crit, properties);

		return crit;
	}

	private String createLcaFormula(String lca) {
		return String.format("lca['%s']?.value", CharactTestHelper.getOrCreateLCA(nodeService, lca));
	}

	private String createPhysicoFormula(String physico) {
		return String.format("physico['%s']?.value", CharactTestHelper.getOrCreatePhysico(nodeService, physico));
	}

	private NodeRef crit(String label, String range, Double weight) {
		NodeRef crit = crit(label);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ProjectModel.PROP_SCORE_CRITERION_WEIGHT, weight);
		properties.put(ProjectModel.PROP_SCORE_CRITERION_RANGE, range);

		nodeService.addProperties(crit, properties);

		return crit;
	}

	private NodeRef crit(String label) {
		return CharactTestHelper.getOrCreateScoreCriterion(nodeService, label);
	}

}