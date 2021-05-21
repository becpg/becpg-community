package fr.becpg.repo.product.formulation.ecoscore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValuePlugin;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;

@Service
public class FrenchEcoScore implements ListValuePlugin {

	private static Log logger = LogFactory.getLog(FrenchEcoScore.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	private String agribaliseDBPath;
	private String countryScoreDBPath;
	private final String ECO_SCORE_SOURCE_TYPE = "ecoscore";

	private FrenchEcoScore() {
		agribaliseDBPath = "beCPG/databases/ecoscore/agribalise_3_0.csv";
		countryScoreDBPath = "beCPG/databases/ecoscore/country_score_2021.csv";
	}

	private Map<String, EnvironmentalFootprintValue> environmentalFootprints = null;
	private Map<String, Pair<Integer, Integer>> countryScores = null;

	private class EnvironmentalFootprintValue {
		private String id;
		private String value;
		private Double score;

		public EnvironmentalFootprintValue(String id, String value, Double score) {
			super();
			this.id = id;
			this.value = value;
			this.score = score;
		}

		public String getId() {
			return id;
		}

		public Double getScore() {
			return score;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return id + " - " + value;
		}
	}

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { ECO_SCORE_SOURCE_TYPE };
	}

	/** {@inheritDoc} */
	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		List<EnvironmentalFootprintValue> matches = new ArrayList<>();
		
		if (environmentalFootprints == null) {
			loadEFs();
		}

		String preparedQuery = BeCPGQueryHelper.prepareQuery(entityDictionaryService, query).replace("*", "");

		matches.addAll(
				environmentalFootprints.values().stream().filter(res -> BeCPGQueryHelper.isQueryMatch(query, res.toString(), entityDictionaryService))
						.limit(100).collect(Collectors.toList()));

		matches.sort((o1, o2) -> {

			if (BeCPGQueryHelper.isAllQuery(query)) {
				return o1.getValue().compareTo(o2.getValue());
			}

			String value = BeCPGQueryHelper.prepareQuery(entityDictionaryService, o1.getValue()).replace("*", "").replace(preparedQuery, "A");
			String value2 = BeCPGQueryHelper.prepareQuery(entityDictionaryService, o2.getValue()).replace("*", "").replace(preparedQuery, "A");

			return value.compareTo(value2);

		});

		logger.debug("suggestion for " + query + ", found " + matches.size() + " results");

		return new ListValuePage(matches, pageNum, pageSize, (values) -> {
			List<ListValueEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (EnvironmentalFootprintValue value : values) {
					suggestions.add(new ListValueEntry(value.getId(), value.toString(), "rawMaterial"));
				}
			}
			return suggestions;
		});
	}

	private void loadEFs() {
		environmentalFootprints = new LinkedHashMap<>();
		ClassPathResource resource = new ClassPathResource(agribaliseDBPath);
		try (InputStream in = resource.getInputStream()) {
			try (InputStreamReader inReader = new InputStreamReader(resource.getInputStream())) {
				try (CSVReader csvReader = new CSVReader(inReader, ';', '"', 1)) {
					String[] line = null;
					while ((line = csvReader.readNext()) != null) {
						environmentalFootprints.put(line[1], new EnvironmentalFootprintValue(line[1], line[2], parseDouble(line[12])));
					}
				}
			}
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	private void loadCountryScores() {
		countryScores = new LinkedHashMap<>();
		ClassPathResource resource = new ClassPathResource(countryScoreDBPath);
		try (InputStream in = resource.getInputStream()) {
			try (InputStreamReader inReader = new InputStreamReader(resource.getInputStream())) {
				try (CSVReader csvReader = new CSVReader(inReader, ';', '"', 1)) {
					String[] line = null;
					while ((line = csvReader.readNext()) != null) {
						countryScores.put(line[0], new Pair<>(parseInt(line[2]), parseInt(line[3])));
					}
				}
			}
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	private Integer parseInt(String value) {
		if ((value != null) && !value.trim().isEmpty()) {
			return Integer.valueOf(value);
		}

		return null;
	}

	private Double parseDouble(String value) {
		if ((value != null) && !value.trim().isEmpty()) {
			return Double.valueOf(value.trim().replace(",", "."));
		}

		return null;
	}

	public int computeScore(ProductData productData) {

		boolean threatenedSpecies = false;
		boolean notRecyclable = false;

		if (threatenedSpecies) {
			return 19;
		}

		//Tester claim ORIGINE_FRANCE

		int score = threatenedSpecies ? 19 : (notRecyclable ? 79 : 100);

		return Math.min(score, computeEFScore("TODO", false)
				+ Math.min(25, computeClaimScore(productData) + computeTransportScore(productData) + computePackagingScore(productData)));

	}

	public int computeEFScore(String categoryCode, boolean isDrink) {

		//Defers loading
		if (environmentalFootprints == null) {
			loadEFs();
		}

		if (environmentalFootprints.containsKey(categoryCode)) {
			EnvironmentalFootprintValue environmentalFootprintValue = environmentalFootprints.get(categoryCode);

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

	/**
	 *
	 * https://gs1.org/voc/PackagingMarkedLabelAccreditationCode
	 *
	 *
	 * Groupe 1
	 *  Nature & Progrès	20 / NATURE_ET_PROGRES
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

	 * @param productData
	 * @return
	 */

	private static final List<String> GROUP1_CLAIM = Arrays.asList("NATURE_ET_PROGRES", "BIO_COHERANCE", "DEMETER_LABEL");
	private static final List<String> GROUP2_CLAIM = Arrays.asList("EU_ORGANIC");
	private static final List<String> GROUP3_CLAIM = Arrays.asList("HAUTE_VALEUR_ENVIRONNEMENTALE", "UTZ_CERTIFIED", "RAINFOREST_ALLIANCE", "FAIR_TRADE_MARK",
			"BLEU_BLANC_COEUR", "LABEL_ROUGE", "AQUACULTURE_STEWARDSHIP_COUNCIL", "MARINE_STEWARDSHIP_COUNCIL_LABEL");

	public int computeClaimScore(ProductData productData) {
		int score = 0;
		if (productData.getLabelClaimList() != null) {
			boolean isASCorMSC = true;
			for (LabelClaimListDataItem claim : productData.getLabelClaimList()) {
				if (Boolean.TRUE.equals(claim.getIsClaimed())) {

					String code = (String) nodeService.getProperty(claim.getLabelClaim(), PLMModel.PROP_LABEL_CLAIM_CODE);
					if ((code != null) && !code.isEmpty()) {

						if (GROUP1_CLAIM.contains(code)) {
							score += 20;
						} else if (GROUP2_CLAIM.contains(code)) {
							score += 15;
						}
						if (GROUP3_CLAIM.contains(code)) {
							if (code.contains("STEWARDSHIP_COUNCIL")) {
								if (isASCorMSC) {
									score += 10;
									isASCorMSC = false;
								}

							} else {

								score += 10;
							}
						}

					}

				}
			}
		}

		return Math.max(score, 20);

	}

	//	SUSTAINABLE_PALM_OIL_RSPO

	public int computeTransportScore(ProductData productData) {

		//Defers loading
		if (countryScores == null) {
			loadCountryScores();
		}

		Double transportScore = 0d;
		Double politicalScore = 0d;
		Double waterPerc = 0d;

		if (productData.getIngList() != null) {
			for (IngListDataItem ingListDataItem : productData.getIngList()) {
				if (ingListDataItem.getQtyPerc() != null) {
					//L'origine de l'ingrédient "eau" (si présent) n'entre pas en compte dans le calcul du score transport
					if (isWater(ingListDataItem.getIng())) {
						waterPerc = ingListDataItem.getQtyPerc();
					} else {
						// À défaut d'origine disponible, l'origine "Monde" est appliquée pour les ingrédients concernés.
						// L'origine "UE et hors UE" correspond à l'origine "Monde"
						int transportScoreByCountry = 0;
						int politicalScoreByCountry = 0;

						if (ingListDataItem.getGeoOrigin() != null) {
							for (NodeRef geoOrigin : ingListDataItem.getGeoOrigin()) {
								String geoCode = (String) nodeService.getProperty(geoOrigin, PLMModel.PROP_GEO_ORIGIN_ISOCODE);
								if (countryScores.containsKey(geoCode)) {
									transportScoreByCountry = Math.min(transportScoreByCountry, countryScores.get(geoCode).getFirst());
									politicalScoreByCountry = Math.min(transportScoreByCountry, countryScores.get(geoCode).getSecond());
								}
							}
						}

						transportScore += transportScoreByCountry * ingListDataItem.getQtyPerc();
						politicalScore += politicalScoreByCountry * ingListDataItem.getQtyPerc();
					}

				}

			}

		}

		return (int) Math.round((transportScore * 0.15) + ((politicalScore / 10) - 5));
	}

	private boolean isWater(NodeRef ing) {
		return nodeService.hasAspect(ing, PLMModel.ASPECT_WATER);
	}

	/**
	 * Pas de prise en compte du ratio par type d'emballage car nous travaillons
	 * en qté exacte de matériaux
	 * @param productData
	 * @return
	 */
	public int computePackagingScore(ProductData productData) {

		//String packagingFormat = (String) nodeService.getProperty(productData.getNodeRef(),GS1Model.PROP_PACKAGING_TYPE_CODE));

		Double score = 100d;

		boolean notRecyclable = false;

		if (productData.getPackMaterialList() != null) {
			for (PackMaterialListDataItem material : productData.getPackMaterialList()) {
				Integer materialScore = getMaterialScore(material.getCharactNodeRef());

				//notRecyclable ???

				score -= (100 - materialScore) * material.getPmlWeight();
				//TODO diviser par le total ?
			}
		}

		return (int) Math.round((score / 10) - 10);

	}

	private Integer getMaterialScore(NodeRef charactNodeRef) {
		// TODO Auto-generated method stub
		return null;
	}

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
