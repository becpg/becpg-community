package fr.becpg.repo.product.formulation.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.formulation.ecoscore.EcoScoreContext;
import fr.becpg.repo.product.formulation.ecoscore.EcoScoreService;
import fr.becpg.repo.product.formulation.ecoscore.EcoScoreService.EnvironmentalFootprintValue;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>FrenchEcoScore class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("ecoScore")
public class FrenchEcoScore implements AutoCompletePlugin, ScoreCalculatingPlugin {

	private static Log logger = LogFactory.getLog(FrenchEcoScore.class);

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private EcoScoreService ecoScoreService;


	/** Constant <code>ECO_SCORE_SOURCE_TYPE="ecoscore"</code> */
	public static final String ECO_SCORE_SOURCE_TYPE = "ecoscore";

	/**
	 *
	 * https://gs1.org/voc/PackagingMarkedLabelAccreditationCode
	 *
	 *
	 * Groupe 1
	 *  Nature et Progrès	20 / NATURE_ET_PROGRES
		Bio Cohérence	20 / BIO_COHERANCE
		Demeter	20    / DEMETER_LABEL
	
	
		Groupe 2
		Bio (EU)	15 /  EU_ORGANIC
	
		Groupe 3
	
		HVE	10  / HAUTE_VALEUR_ENVIRONNEMENTALE
		UTZ	10  / UTZ_CERTIFIED
		Rainforest	10 / RAINFOREST_ALLIANCE
		Fairtrade	10 / FAIR_TRADE_MARK
		BBC	10 / BLEU_BLANC_COEUR
		Label Rouge	10 / LABEL_ROUGE
		ASC	10 / AQUACULTURE_STEWARDSHIP_COUNCIL
		MSC	10 / MARINE_STEWARDSHIP_COUNCIL_LABEL
	
	 */

	private static final List<String> GROUP1_CLAIM = Arrays.asList("NATURE_ET_PROGRES", "BIO_COHERANCE", "DEMETER_LABEL");
	private static final List<String> GROUP2_CLAIM = Arrays.asList("EU_ORGANIC", "ORGANIC");
	private static final List<String> GROUP3_CLAIM = Arrays.asList("HAUTE_VALEUR_ENVIRONNEMENTALE", "UTZ_CERTIFIED", "RAINFOREST_ALLIANCE",
			"FAIR_TRADE_MARK", "BLEU_BLANC_COEUR", "LABEL_ROUGE", "AQUACULTURE_STEWARDSHIP_COUNCIL", "MARINE_STEWARDSHIP_COUNCIL_LABEL");

	private FrenchEcoScore() {
	
	}


	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { ECO_SCORE_SOURCE_TYPE };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		List<EnvironmentalFootprintValue> matches = new ArrayList<>();


		String preparedQuery = BeCPGQueryHelper.prepareQuery(query).replace("*", "");

		matches.addAll(ecoScoreService.getEnvironmentalFootprints().values().stream().filter(res -> BeCPGQueryHelper.isQueryMatch(query, res.getValue())).limit(100)
				.toList());

		matches.sort((o1, o2) -> {

			if (BeCPGQueryHelper.isAllQuery(query)) {
				return o1.getValue().compareTo(o2.getValue());
			}

			String value = BeCPGQueryHelper.prepareQueryForSorting(o1.getValue()).replace("*", "").replace(preparedQuery, "A");
			String value2 = BeCPGQueryHelper.prepareQueryForSorting(o2.getValue()).replace("*", "").replace(preparedQuery, "A");

			return value.compareTo(value2);

		});

		logger.debug("suggestion for " + query + ", found " + matches.size() + " results");

		return new AutoCompletePage(matches, pageNum, pageSize, values -> {
			List<AutoCompleteEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (EnvironmentalFootprintValue value : values) {
					suggestions.add(new AutoCompleteEntry(value.getId(), value.toString(), "category"));
				}
			}
			return suggestions;
		});
	}



	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity productData) {
		return (productData instanceof ProductData) && ((BeCPGDataObject) productData).getAspects().contains(PLMModel.ASPECT_ECO_SCORE);
	}

	/** {@inheritDoc} */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {

		ProductData productData = (ProductData) scorableEntity;

		if ((productData.getEcoScoreCategory() != null) && !productData.getEcoScoreCategory().isEmpty()) {
			Boolean hasThreatenedSpecies = false;
			Boolean notRecyclable = false;
			Boolean isDrink = false;

			int packagingMalus = 0;
			int claimBonus = 0;
			int acvScore = 0;
			int transportScore = 0;
			int politicalScore = 0;
			int ecoScore = 0;

			List<LabelClaimListDataItem> labelClaimList = productData.getLabelClaimList();

			claimBonus = computeClaimBonus(labelClaimList);

			hasThreatenedSpecies = hasThreatenedSpecies(labelClaimList);

			if (Boolean.TRUE.equals(hasThreatenedSpecies)) {
				ecoScore = 19;
			} else {

				List<PackMaterialListDataItem> packMaterialList = productData.getPackMaterialList();

				packagingMalus = computePackagingMalus(packMaterialList);

				notRecyclable = isNotRecyclable(packMaterialList);

				if (Boolean.TRUE.equals(notRecyclable)) {
					ecoScore = 79;
				} else {
					ecoScore = 100;
				}

				acvScore = computeEFScore(productData.getEcoScoreCategory(), isDrink);

				int[] result = computeTransportAndPoliticalScore(productData);

				transportScore = result[0];
				politicalScore = result[1];

				if (logger.isDebugEnabled()) {
					logger.info("Ecoscore details: ");
					logger.info(" - base: " + ecoScore);
					logger.info(" - acvScore: " + acvScore);
					logger.info(" - claimBonus: " + claimBonus);
					logger.info(" - transportScore: " + transportScore);
					logger.info(" - politicalScore: " + politicalScore);
					logger.info(" - packagingMalus: " + packagingMalus);
				}

				ecoScore = Math.min(ecoScore, acvScore + Math.min(25, claimBonus + transportScore + politicalScore + packagingMalus));

			}

			productData.setEcoScore(ecoScore * 1d);

			String scoreClass = computeScoreClass(ecoScore);

			productData.setEcoScoreClass(scoreClass);

			EcoScoreContext ecoScoreContext = new EcoScoreContext();

			ecoScoreContext.setEcoScore(ecoScore);
			ecoScoreContext.setScoreClass(scoreClass);
			ecoScoreContext.setAcvScore(acvScore);
			ecoScoreContext.setClaimBonus(claimBonus);
			ecoScoreContext.setTransportScore(transportScore);
			ecoScoreContext.setPoliticalScore(politicalScore);
			ecoScoreContext.setPackagingMalus(packagingMalus);

			productData.setEcoScoreDetails(ecoScoreContext.toJSON().toString());

		} else {
			productData.setEcoScore(null);
			productData.setEcoScoreClass(null);
			productData.setEcoScoreDetails(null);
		}

		return true;

	}

	private Boolean hasThreatenedSpecies(List<LabelClaimListDataItem> labelClaimList) {

		Boolean hasThreatenedSpecies = false;

		if (labelClaimList != null) {
			for (LabelClaimListDataItem claim : labelClaimList) {
				if (Boolean.TRUE.equals(claim.getIsClaimed())) {

					String code = (String) nodeService.getProperty(claim.getLabelClaim(), PLMModel.PROP_LABEL_CLAIM_CODE);
					if ((code != null) && !code.isEmpty() && "THREATENED_SPECIES".equals(code)) {
						hasThreatenedSpecies = Boolean.TRUE;
						break;
					}
				}
			}
		}

		return hasThreatenedSpecies;
	}

	private Boolean isNotRecyclable(List<PackMaterialListDataItem> packMaterialList) {

		Boolean notRecyclable = false;

		if (packMaterialList != null) {
			for (PackMaterialListDataItem material : packMaterialList) {
				if (!Boolean.TRUE.equals(notRecyclable)) {
					notRecyclable = Boolean.TRUE.equals(nodeService.getProperty(material.getPmlMaterial(), PackModel.PROP_PM_ISNOTRECYCLABLE));
				}
			}

		}
		return notRecyclable;
	}

	private int computeClaimBonus(List<LabelClaimListDataItem> labelClaimList) {

		int claimBonus = 0;

		if (labelClaimList != null) {
			boolean isASCorMSC = true;
			for (LabelClaimListDataItem claim : labelClaimList) {
				if (Boolean.TRUE.equals(claim.getIsClaimed())) {

					String code = (String) nodeService.getProperty(claim.getLabelClaim(), PLMModel.PROP_LABEL_CLAIM_CODE);
					if ((code != null) && !code.isEmpty()) {

						if (GROUP1_CLAIM.contains(code)) {
							claimBonus += 20;
						} else if (GROUP2_CLAIM.contains(code)) {
							claimBonus += 15;
						} else if (GROUP3_CLAIM.contains(code)) {
							if (code.contains("STEWARDSHIP_COUNCIL")) {
								if (isASCorMSC) {
									claimBonus += 10;
									isASCorMSC = false;
								}

							} else {
								claimBonus += 10;
							}
						}
					}
				}
			}
		}

		if (claimBonus > 20) {
			claimBonus = 20;
		}

		return claimBonus;
	}

	private int computePackagingMalus(List<PackMaterialListDataItem> packMaterialList) {

		int packagingMalus = 0;

		if (packMaterialList != null) {

			Double totalWeight = 0d;

			for (PackMaterialListDataItem material : packMaterialList) {
				totalWeight += material.getPmlWeight();
			}

			if (totalWeight == 0d) {
				totalWeight = 1d;
			}

			Double score = 100d;
			for (PackMaterialListDataItem material : packMaterialList) {

				Integer materialScore = (Integer) nodeService.getProperty(material.getPmlMaterial(), PackModel.PROP_PM_ECOSCORE);
				if (materialScore == null) {
					materialScore = 0;
				}

				/**
				 * Pas de prise en compte du ratio par type d'emballage car nous travaillons
				 * en qté exacte de matériaux
				 */
				score -= (100 - materialScore) * material.getPmlWeight() / totalWeight;

			}

			packagingMalus = (int) Math.round((score / 10) - 10);
		}

		return packagingMalus;
	}

	private int computeEFScore(String categoryCode, boolean isDrink) {

		
		if (ecoScoreService.getEnvironmentalFootprints().containsKey(categoryCode)) {
			EnvironmentalFootprintValue environmentalFootprintValue = ecoScoreService.getEnvironmentalFootprints().get(categoryCode);

			Double score;
			if (isDrink) {
				score = (-36 * Math.log((environmentalFootprintValue.getScore() * 100) + 1)) + 150;
			} else {
				score = 100 - ((20 * Math.log((10 * environmentalFootprintValue.getScore()) + 1))
						/ Math.log(2 + (1 / (100 * Math.pow(environmentalFootprintValue.getScore(), 4)))));
			}

			return (int) Math.round(Math.max(0d, Math.min(100d, score)));
		}

		return 0;
	}

	//	SUSTAINABLE_PALM_OIL_RSPO

	private int[] computeTransportAndPoliticalScore(ProductData productData) {

		int[] result = new int[2];

		

		Double transportScore = 0d;
		Double politicalScore = 0d;
		if (productData.getIngList() != null) {
			for (IngListDataItem ingListDataItem : productData.getIngList()) {
				if (ingListDataItem.getQtyPerc() != null) {
					//L'origine de l'ingrédient "eau" (si présent) n'entre pas en compte dans le calcul du score transport
					if (isWater(ingListDataItem.getIng())) {
						ingListDataItem.getQtyPerc();
					} else {
						// À défaut d'origine disponible, l'origine "Monde" est appliquée pour les ingrédients concernés.
						// L'origine "UE et hors UE" correspond à l'origine "Monde"
						Double transportScoreByCountry = 100d;
						Double politicalScoreByCountry = 100d;

						if ((ingListDataItem.getGeoOrigin() != null) && !ingListDataItem.getGeoOrigin().isEmpty()) {
							for (NodeRef geoOrigin : ingListDataItem.getGeoOrigin()) {
								String geoCode = (String) nodeService.getProperty(geoOrigin, PLMModel.PROP_GEO_ORIGIN_ISOCODE);
								if (ecoScoreService.getCountryScores().containsKey(geoCode)) {
									transportScoreByCountry = Math.min(transportScoreByCountry, ecoScoreService.getCountryScores().get(geoCode).getFirst());

									politicalScoreByCountry = Math.min(politicalScoreByCountry, ecoScoreService.getCountryScores().get(geoCode).getSecond());

									logger.debug("Found transportScoreByCountry: " + transportScoreByCountry + " for " + geoCode);
									logger.debug("Found politicalScoreByCountry: " + politicalScoreByCountry + " for " + geoCode);
								} else {
									transportScoreByCountry = 0d;
									politicalScoreByCountry = 0d;
								}
							}
						} else {
							transportScoreByCountry = 0d;
							politicalScoreByCountry = 0d;
						}

						transportScore += (transportScoreByCountry * ingListDataItem.getQtyPerc()) / 100d;
						politicalScore += (politicalScoreByCountry * ingListDataItem.getQtyPerc()) / 100d;
					}

				}

			}

		}

		result[0] = (int) Math.round((transportScore * 0.15d));
		result[1] = (int) Math.round(((politicalScore / 10d) - 5));

		return result;
	}

	private boolean isWater(NodeRef ing) {
		return nodeService.hasAspect(ing, PLMModel.ASPECT_WATER) 
				|| (nodeService.hasAspect(ing, PLMModel.ASPECT_EVAPORABLE)
				&& nodeService.getProperty(ing, PLMModel.PROP_EVAPORATED_RATE) != null
				&& (Double) nodeService.getProperty(ing, PLMModel.PROP_EVAPORATED_RATE) == 100d);
	}

	/**
	 * <p>computeScoreClass.</p>
	 *
	 * @param score a int
	 * @return a {@link java.lang.String} object
	 */
	public String computeScoreClass(int score) {

		if (score >= 80) {
			return "A";
		} else if (score >= 60) {
			return "B";
		} else if (score >= 40) {
			return "C";
		} else if (score >= 20) {
			return "D";
		}

		return "E";

	}

}
