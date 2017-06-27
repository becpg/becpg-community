package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.JsonScoreHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Computes product completion score
 *
 * @author steven
 *
 */
public class ScoreCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(ScoreCalculatingFormulationHandler.class);

	public static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory_property";
	public static final String MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED = "message.formulate.mandatory_property_localized";
	public static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";
	public static final String MESSAGE_OR = "message.formulate.or";
	public static final String MESSAGE_NON_UNIQUE_FIELD = "message.formulate.non-unique-field";
	public static final String CATALOGS_PATH = "/app:company_home/cm:System/cm:PropertyCatalogs";
	public static final String CATALOG_DEFS = "CATALOG_DEFS";
	private AlfrescoRepository<ProductData> alfrescoRepository;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public boolean process(ProductData product) {

		if (logger.isDebugEnabled()) {
			logger.debug("===== Calculating score of product " + product.getName() + " =====");
		}
		
		JSONObject scores = new JSONObject();

		try {

			if (product.getEntityScore() != null) {
				scores = new JSONObject(product.getEntityScore());
			}
			Integer childScore = 0;
			Integer childrenSize = 0;
			Double specificationScore = 100d;
			Double mandatoryFieldsScore = 100d;

			for (AbstractProductDataView view : product.getViews()) {
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

			// pre merge, ctrlDataItems might be duplicated, visit them only
			// once
			List<String> visitedCtrlDataItems = new ArrayList<>();
			if (product.getReqCtrlList() != null) {
				for (ReqCtrlListDataItem ctrl : product.getReqCtrlList()) {
					logger.debug("Checking rclDataItem " + ctrl.getReqType() + " - " + ctrl.getReqDataType() + " - " + ctrl.getReqMessage());
					if (specificationScore > 10) {
						if ((ctrl.getReqDataType() == RequirementDataType.Specification) && (ctrl.getReqType() == RequirementType.Forbidden)
								&& !visitedCtrlDataItems.contains(ctrl.getKey())) {
							if (logger.isDebugEnabled()) {
								logger.debug("Visiting specification rclDataItem: " + ctrl.getReqMessage() + ", s=" + ctrl.getSources());
							}
							specificationScore -= 10;
							visitedCtrlDataItems.add(ctrl.getKey());
						}
					} else {
						break;
					}

				}
			}

			// checks if mandatory fields are present
			if (scores.has(JsonScoreHelper.PROP_CATALOGS)) {
				JSONArray mandatoryFields = scores.getJSONArray(JsonScoreHelper.PROP_CATALOGS);

				if (mandatoryFields.length() > 0) {

					mandatoryFieldsScore = 0d;

					for (int j = 0; j < mandatoryFields.length(); j++) {
						JSONObject catalog = (JSONObject) mandatoryFields.get(j);
						mandatoryFieldsScore += catalog.getDouble(JsonScoreHelper.PROP_SCORE);
					}

					mandatoryFieldsScore /= mandatoryFields.length();
				}

				logger.debug("Child score: " + childScore + ", childSize: " + childrenSize + "\n\n mandatoryFields: " + mandatoryFields);
				Double completionPercent = (componentsValidationScore + mandatoryFieldsScore + specificationScore) / 3d;

				JSONObject details = new JSONObject();
				details.put("mandatoryFields", mandatoryFieldsScore);

				details.put("specifications", specificationScore);
				details.put("componentsValidation", componentsValidationScore);

				scores.put("global", completionPercent);
				scores.put("details", details);

			}
			Map<String, Map<String, Integer>> ctrlCount = getCtrlCount(product);

			List<JSONObject> ctrlArray = new ArrayList<>();

			for (String ctrlKey : ctrlCount.keySet()) {
				JSONObject currentJSO = new JSONObject();
				currentJSO.put(ctrlKey, ctrlCount.get(ctrlKey));

				ctrlArray.add(currentJSO);
			}

			ctrlArray.sort((o1, o2) -> {
				// sort on keys (fbd > all)

				try {
					JSONObject o1Values = o1.getJSONObject((String) o1.keys().next());
					JSONObject o2Values = o2.getJSONObject((String) o2.keys().next());

					if ((o1Values.has("Forbidden") && !(o2Values.has("Forbidden"))) || (o1Values.has("Forbidden") && o2Values.has("Forbidden")
							&& (o1Values.getInt("Forbidden") >= o2Values.getInt("Forbidden")))) {
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

			logger.debug("Ctrl sorted array: " + ctrlArray);

			scores.put("ctrlCount", ctrlArray);
			scores.put("totalForbidden", getForbiddenCtrlAmount(ctrlCount));

		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		product.setEntityScore(scores.toString());

		return true;
	}

	/**
	 * Returns if node exists and is in valid state
	 *
	 * @param node
	 * @return
	 */
	public boolean checkProductValidity(NodeRef node) {
		ProductData found = alfrescoRepository.findOne(node);
		if (found != null) {
			return SystemState.Valid.equals(found.getState());
		}
		return false;
	}

	private Map<String, Map<String, Integer>> getCtrlCount(ProductData product) {

		Map<String, Map<String, Integer>> counts = new HashMap<>();
		List<ReqCtrlListDataItem> visitedRclItems = new ArrayList<>();

		for (ReqCtrlListDataItem rclDataItem : product.getReqCtrlList()) {
			RequirementDataType key = rclDataItem.getReqDataType();

			// make sure we don't put duplicates rclDataItems
			if (counts.containsKey(key.toString()) && !visitedRclItems.contains(rclDataItem)) {
				Map<String, Integer> currentCount = counts.get(key.toString());
				if (currentCount == null) {
					currentCount = new HashMap<String, Integer>();
				}

				if (currentCount.containsKey(rclDataItem.getReqType().toString())) {
					currentCount.put(rclDataItem.getReqType().toString(), currentCount.get(rclDataItem.getReqType().toString()) + 1);
				} else {
					currentCount.put(rclDataItem.getReqType().toString(), 1);
				}
				visitedRclItems.add(rclDataItem);

			} else if (!counts.containsKey(key.toString())) {
				// this dataType was not found before, adding it
				Map<String, Integer> newMap = new HashMap<String, Integer>();
				newMap.put(rclDataItem.getReqType().toString(), 1);
				counts.put(key.toString(), newMap);

				visitedRclItems.add(rclDataItem);
			}
		}

		return counts;
	}

	private int getForbiddenCtrlAmount(Map<String, Map<String, Integer>> reqCtrlList) {
		int res = 0;

		for (Map<String, Integer> map : reqCtrlList.values()) {
			if (map.containsKey("Forbidden")) {
				res += map.get("Forbidden");
			}
		}

		return res;
	}

}