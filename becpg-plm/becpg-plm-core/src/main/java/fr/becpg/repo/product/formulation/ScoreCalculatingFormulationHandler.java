package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.score.ScoreCalculatingPlugin;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.repository.model.StateableEntity;

/**
 * Computes product completion score
 *
 * @author steven
 * @version $Id: $Id
 */
public class ScoreCalculatingFormulationHandler extends FormulationBaseHandler<ScorableEntity> {

	private static final Log logger = LogFactory.getLog(ScoreCalculatingFormulationHandler.class);

	private AlfrescoRepository<ScorableEntity> alfrescoRepository;

	private ScoreCalculatingPlugin[] scorePlugins;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ScorableEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>scorePlugins</code>.</p>
	 *
	 * @param scorePlugins an array of {@link fr.becpg.repo.product.formulation.score.ScoreCalculatingPlugin} objects
	 */
	public void setScorePlugins(ScoreCalculatingPlugin[] scorePlugins) {
		this.scorePlugins = scorePlugins;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ScorableEntity scorableEntity) {

		if (scorePlugins != null) {
			for (ScoreCalculatingPlugin plugin : scorePlugins) {
				if (plugin.accept(scorableEntity)) {
					plugin.formulateScore(scorableEntity);
				}
			}
		}

		JSONObject scores = new JSONObject();

		try {

			if (scorableEntity.getEntityScore() != null) {
				scores = new JSONObject(scorableEntity.getEntityScore());
			}
			Integer childScore = 0;
			Integer childrenSize = 0;
			Double specificationScore = 100d;
			Double mandatoryFieldsScore = 100d;

			for (AbstractProductDataView view : scorableEntity.getViews()) {
				if (view.getMainDataList() != null) {
					for (CompositionDataItem dataItem : view.getMainDataList()) {
						if (dataItem.getComponent() != null) {
							if (checkProductValidity(dataItem.getComponent())) {
								childScore += 1;
							}
							childrenSize += 1;
						}
					}
				}
			}

			Double componentsValidationScore = (childrenSize > 0 ? (childScore * 100d) / childrenSize : 100d);

			Map<String, Map<String, Integer>> reqNbByTypeAndKey = new HashMap<>();
			Map<String, Integer> reqNbByRegulatoryCode = new HashMap<>();
			Set<String> visitedRclItems = new HashSet<>();

			if (scorableEntity.getReqCtrlList() != null) {
				for (ReqCtrlListDataItem rclDataItem : scorableEntity.getReqCtrlList()) {
					RequirementDataType key = rclDataItem.getReqDataType();
					RequirementType type = rclDataItem.getReqType();
					// make sure we don't put duplicates rclDataItems
					if (!visitedRclItems.contains(rclDataItem.getKey())) {
						if ((key != null) && (type != null)) {

							if (specificationScore > 10) {
								if ((RequirementDataType.Specification.equals(key)) && RequirementType.Forbidden.equals(type)) {
									specificationScore -= 10;
								}
							}

							if (reqNbByTypeAndKey.containsKey(key.toString())) {
								Map<String, Integer> currentCount = reqNbByTypeAndKey.get(key.toString());
								if (currentCount == null) {
									currentCount = new HashMap<>();
								}

								if (currentCount.containsKey(type.toString())) {
									currentCount.put(type.toString(), currentCount.get(type.toString()) + 1);
								} else {
									currentCount.put(type.toString(), 1);
								}

							} else if (!reqNbByTypeAndKey.containsKey(key.toString())) {
								// this dataType was not found before, adding it
								Map<String, Integer> newMap = new HashMap<>();
								newMap.put(type.toString(), 1);
								reqNbByTypeAndKey.put(key.toString(), newMap);

							}

						}
						if ((rclDataItem.getRegulatoryCode() != null) && !rclDataItem.getRegulatoryCode().isEmpty()) {
							Integer currentCount = reqNbByRegulatoryCode.get(rclDataItem.getRegulatoryCode());
							if (currentCount == null) {
								currentCount = 0;
							}
							currentCount++;
							reqNbByRegulatoryCode.put(rclDataItem.getRegulatoryCode(), currentCount);
						}

					}

					visitedRclItems.add(rclDataItem.getKey());
				}
			}
			Double completionPercent = 100d;

			// checks if mandatory fields are present
			if (scores.has(EntityCatalogService.PROP_CATALOGS)) {
				JSONArray mandatoryFields = scores.getJSONArray(EntityCatalogService.PROP_CATALOGS);
				int total = mandatoryFields.length();
				if (total > 0) {

					mandatoryFieldsScore = 0d;

					for (int j = 0; j < total; j++) {
						JSONObject catalog = (JSONObject) mandatoryFields.get(j);
						mandatoryFieldsScore += catalog.getDouble(EntityCatalogService.PROP_SCORE);
					}

					mandatoryFieldsScore /= total;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Child score: " + childScore + ", childSize: " + childrenSize + "\n\n mandatoryFields: " + mandatoryFields);
				}
				completionPercent = (componentsValidationScore + mandatoryFieldsScore + specificationScore) / 3d;

				

			}
			
			
			JSONObject details = new JSONObject();
			details.put("mandatoryFields", mandatoryFieldsScore);

			details.put("specifications", specificationScore);
			details.put("componentsValidation", componentsValidationScore);

			scores.put("global", completionPercent);
			scores.put("details", details);

			List<JSONObject> ctrlArray = new ArrayList<>();

			for (String ctrlKey : reqNbByTypeAndKey.keySet()) {
				JSONObject currentJSO = new JSONObject();
				currentJSO.put(ctrlKey, reqNbByTypeAndKey.get(ctrlKey));

				ctrlArray.add(currentJSO);
			}

			if (!reqNbByRegulatoryCode.isEmpty()) {

				JSONObject currentJSO = new JSONObject();
				currentJSO.put("RegulatoryCodes", reqNbByRegulatoryCode);

				ctrlArray.add(currentJSO);
			}

			ctrlArray.sort((o1, o2) -> {
				// sort on keys (fbd > all)

				try {
					JSONObject o1Values = o1.getJSONObject((String) o1.keys().next());
					JSONObject o2Values = o2.getJSONObject((String) o2.keys().next());

					String key = RequirementType.Forbidden.toString();

					if ((o1Values.has(key) && !(o2Values.has(key)))
							|| (o1Values.has(key) && o2Values.has(key) && (o1Values.getInt(key) >= o2Values.getInt(key)))) {
						return -1;
					} else {
						return 1;
					}
				} catch (JSONException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("JSONException, returning equals");
					}
					return 0;
				}
			});

			if (logger.isDebugEnabled()) {
				logger.debug("Ctrl sorted array: " + ctrlArray);
			}

			scores.put("ctrlCount", ctrlArray);
			scores.put("totalForbidden", getForbiddenCtrlAmount(reqNbByTypeAndKey));

		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		scorableEntity.setEntityScore(scores.toString());

		return true;
	}

	/**
	 * Returns if node exists and is in valid state
	 *
	 * @param node a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	public boolean checkProductValidity(NodeRef node) {
		ScorableEntity found = alfrescoRepository.findOne(node);
		if (found instanceof StateableEntity) {
			return SystemState.Valid.toString().equals(((StateableEntity) found).getEntityState());
		}
		return true;
	}

	private int getForbiddenCtrlAmount(Map<String, Map<String, Integer>> reqCtrlList) {
		int res = 0;

		for (Map<String, Integer> map : reqCtrlList.values()) {
			if (map.containsKey(RequirementType.Forbidden.toString())) {
				res += map.get(RequirementType.Forbidden.toString());
			}
		}

		return res;
	}
	
	/** {@inheritDoc} */
	@Override
	public void onError(ScorableEntity repositoryEntity) {
		process(repositoryEntity);
		super.onError(repositoryEntity);
	}

}
