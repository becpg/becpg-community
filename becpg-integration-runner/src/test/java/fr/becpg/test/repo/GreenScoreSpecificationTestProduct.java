package fr.becpg.test.repo;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.test.utils.CharactTestHelper;

public class GreenScoreSpecificationTestProduct extends StandardSoapTestProduct {

	protected GreenScoreSpecificationTestProduct(Builder builder) {
		super(builder);
	}

	protected List<ProductSpecificationData> createProductSpecifications() {

		ProductSpecificationData productSpecification = ProductSpecificationData.build().withName(uniqueName("Green score specification 📋"))
				.withScoreList(createGreenScoreList());

		alfrescoRepository.create(destFolder, productSpecification);
		return List.of(productSpecification);
	}

	private List<ScoreListDataItem> createGreenScoreList() {
	    List<ScoreListDataItem> scoreList = new ArrayList<>();

	    // 1 - FORMULATION
	    ScoreListDataItem formulation = ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Formulation"));
	    scoreList.add(formulation);
	    
	    // 1.1 - Score carbone Ingrédients
	    ScoreListDataItem scoreCarboneIngredients = ScoreListDataItem.build().withParent(formulation)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Score carbone Ingrédients"));
	    scoreList.add(scoreCarboneIngredients);
	    scoreList.add(ScoreListDataItem.build().withParent(scoreCarboneIngredients)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Score carbone Ingrédients (hors transport)")));
	    scoreList.add(ScoreListDataItem.build().withParent(scoreCarboneIngredients)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Impact carbone transport (kCo2 / kg / km)")));

	    // 1.2 - Biodiversité
	    ScoreListDataItem biodiversite = ScoreListDataItem.build().withParent(formulation)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Biodiversité"));
	    scoreList.add(biodiversite);
	    scoreList.add(ScoreListDataItem.build().withParent(biodiversite)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Poids des MP issues Biotech")));
	    scoreList.add(ScoreListDataItem.build().withParent(biodiversite)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Poids des MP upcyclées")));

	    // 1.3 - Performance environnementale
	    ScoreListDataItem performanceEnvironnementale = ScoreListDataItem.build().withParent(formulation)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Performance environnementale"));
	    scoreList.add(performanceEnvironnementale);
	    scoreList.add(ScoreListDataItem.build().withParent(performanceEnvironnementale)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Note EPI")));
	    scoreList.add(ScoreListDataItem.build().withParent(performanceEnvironnementale)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "% de MP selon ISO 16128")));

	    // 1.4 - Impacts sociétaux ingrédients
	    ScoreListDataItem impactsSocietaux = ScoreListDataItem.build().withParent(formulation)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Impacts sociétaux ingrédients"));
	    scoreList.add(impactsSocietaux);
	    scoreList.add(ScoreListDataItem.build().withParent(impactsSocietaux)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Note SPI")));
	    scoreList.add(ScoreListDataItem.build().withParent(impactsSocietaux)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Score Certification Ecovadis Fournisseurs")));

	    // 2 - FABRICATION
	    ScoreListDataItem fabrication = ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Fabrication"));
	    scoreList.add(fabrication);
	    
	    // 2.1 - Consommation ressources
	    ScoreListDataItem consommationRessources = ScoreListDataItem.build().withParent(fabrication)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Consommation ressources"));
	    scoreList.add(consommationRessources);
	    scoreList.add(ScoreListDataItem.build().withParent(consommationRessources)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Nombre de MP")));
	    scoreList.add(ScoreListDataItem.build().withParent(consommationRessources)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Poids des MP énergivores")));

	    // 2.2 - Score carbone usine (hors transports)
	    ScoreListDataItem scoreCarboneUsine = ScoreListDataItem.build().withParent(fabrication)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Score carbone usine (hors transports)"));
	    scoreList.add(scoreCarboneUsine);
	    scoreList.add(ScoreListDataItem.build().withParent(scoreCarboneUsine)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Score Carbone usine (intensité)")));

	    // 2.3 - Impacts du transport (kg CO2 / kg / km)
	    ScoreListDataItem impactsTransport = ScoreListDataItem.build().withParent(fabrication)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Impacts du transport (kg CO2 / kg / km)"));
	    scoreList.add(impactsTransport);
	    scoreList.add(ScoreListDataItem.build().withParent(impactsTransport)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Ville de livraison")));
	    scoreList.add(ScoreListDataItem.build().withParent(impactsTransport)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Mode de transp")));

	    // 2.4 - Toxicité santé
	    ScoreListDataItem toxiciteSante = ScoreListDataItem.build().withParent(fabrication)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Toxicité santé"));
	    scoreList.add(toxiciteSante);
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "ATO (mg/kg)")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "ATD (mg/kg)")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "ATIV (ppm)")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "AH1")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "STO-SE 1")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "STO-SE 2")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "STOT- RE 1")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "STOT- RE 2")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "SS 1A")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "SS 1B")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "SS 1")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "S-SS1A")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "EDHH1")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "EDHH2")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "CAR 1A")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "CAR 1B")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "CAR2")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "MUT 1A")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "MUT 1B")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "MUT 2")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "REP 1A")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "REP 1B")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "REP 2")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteSante)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "REP LACT")));

	    // 3 - FIN DE VIE
	    ScoreListDataItem finDeVie = ScoreListDataItem.build().withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Fin de Vie"));
	    scoreList.add(finDeVie);

	    // 3.1 - Biodégradabilité
	    ScoreListDataItem biodegradabilite = ScoreListDataItem.build().withParent(finDeVie)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Biodégradabilité"));
	    scoreList.add(biodegradabilite);
	    scoreList.add(ScoreListDataItem.build().withParent(biodegradabilite)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "BioDscent")));

	    // 3.2 - Toxicité environnement
	    ScoreListDataItem toxiciteEnvironnement = ScoreListDataItem.build().withParent(finDeVie)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "Toxicité environnement"));
	    scoreList.add(toxiciteEnvironnement);
	
	    scoreList.add(toxiciteEnvironnement);
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "EHA1 XMFactor")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "EHC1 XMFactor")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "EHC2 XMFactor")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "EHC3 XMFactor")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "EHC4 XMFactor")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "ED ENV1")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "ED ENV2")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "PMT/vPvM")));
	    scoreList.add(ScoreListDataItem.build().withParent(toxiciteEnvironnement)
	            .withScoreCriterion(CharactTestHelper.getOrCreateScoreCriteriom(nodeService, "PBT/vPvB")));

	    return scoreList;
	}

}