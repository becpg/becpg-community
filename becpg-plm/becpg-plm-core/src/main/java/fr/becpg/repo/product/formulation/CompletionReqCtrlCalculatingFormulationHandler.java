package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

public class CompletionReqCtrlCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompletionReqCtrlCalculatingFormulationHandler.class);

	public static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";
	public static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory_property";
	public static final String MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED = "message.formulate.mandatory_property_localized";

	public static final String MESSAGE_NON_UNIQUE_FIELD = "message.formulate.non-unique-field";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private FormulaService formulaService;
	private EntityCatalogService entityCatalogService;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setFormulaService(FormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setEntityCatalogService(EntityCatalogService entityCatalogService) {
		this.entityCatalogService = entityCatalogService;
	}

	@Override
	public boolean process(ProductData product) throws FormulateException {
		logger.debug("===== Calculating score of " + product.getName() + " (" + product.getState() + ") =====");
		if (logger.isDebugEnabled()) {
			logger.debug("===== Calculating score of product " + product.getName() + " =====");
		}

		JSONObject scores = new JSONObject();

		try {

			// checks if mandatory fields are present
			JSONArray catalogs = entityCatalogService.formulateCatalogs(product.getNodeRef(), product.getReportLocales(), formula -> {
				boolean res = true;

				StandardEvaluationContext context = formulaService.createEvaluationContext(product);

				if (context != null) {
					ExpressionParser parser = new SpelExpressionParser();
					Expression expression = parser.parseExpression(formula);

					try {
						Boolean result = (Boolean) (expression.getValue(context));
						logger.debug("Expression " + expression + " returned " + result);
						res = result.booleanValue();
					} catch (Exception e) {
						logger.error("Unable to parse expression " + expression, e);
						logger.debug("Creating new CtrlListDataItem (method productMatchesOnFormula...)");
						ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated,
								"Unable to parse formula " + formula, null, new ArrayList<NodeRef>(), RequirementDataType.Completion);
						product.getReqCtrlList().add(rclDataItem);
						res = false;
					}
				}

				return res;
			});

			// Unique fields :

			extractReqCtrl(product, catalogs);

			ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated,
					I18NUtil.getMessage(MESSAGE_NON_VALIDATED_STATE), null, new ArrayList<NodeRef>(), RequirementDataType.Validation);

			boolean shouldAdd = false;

			// visits all refs and adds rclDataItem to them if required
			for (AbstractProductDataView view : product.getViews()) {
				if (view.getMainDataList() != null) {
					for (CompositionDataItem dataItem : view.getMainDataList()) {
						if (dataItem.getComponent() != null) {
							if (!checkProductValidity(dataItem.getComponent())) {
								rclDataItem.getSources().add(dataItem.getComponent());
								shouldAdd = true;
							}
						}
					}
				}
			}

			if (shouldAdd) {
				product.getReqCtrlList().add(rclDataItem);
			}

			scores.put(EntityCatalogService.PROP_CATALOGS, catalogs);

		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		product.setEntityScore(scores.toString());
		return true;
	}

	private void extractReqCtrl(ProductData productData, JSONArray catalogs) throws JSONException {

		for (int i = 0; i < catalogs.length(); i++) {
			JSONObject catalog = catalogs.getJSONObject(i);
			String catalogName = catalog.getString(EntityCatalogService.PROP_LABEL);

			if (catalog.has(EntityCatalogService.PROP_MISSING_FIELDS)) {
				JSONArray missingFields = catalog.getJSONArray(EntityCatalogService.PROP_MISSING_FIELDS);
				if (missingFields != null) {
					for (int j = 0; j < missingFields.length(); j++) {
						JSONObject missingField = missingFields.getJSONObject(j);

						String lang = missingField.has(EntityCatalogService.PROP_LOCALE) ? missingField.getString(EntityCatalogService.PROP_LOCALE)
								: null;
						String displayName = missingField.has(EntityCatalogService.PROP_DISPLAY_NAME)
								? missingField.getString(EntityCatalogService.PROP_DISPLAY_NAME)
								: null;

						// Mandatory fields
						final String message = (lang != null
								? I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED, displayName, catalogName, "(" + lang + ")")
								: I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING, displayName, catalogName));

						ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null,
								new ArrayList<NodeRef>(), RequirementDataType.Completion);
						rclDataItem.getSources().add(productData.getNodeRef());

						productData.getReqCtrlList().add(rclDataItem);

					}
				}
			}

			if (catalog.has(EntityCatalogService.PROP_UNIQUE_FIELDS)) {
				JSONArray uniqueFields = catalog.getJSONArray(EntityCatalogService.PROP_UNIQUE_FIELDS);
				if (uniqueFields != null) {
					for (int j = 0; j < uniqueFields.length(); j++) {
						JSONObject uniqueField = uniqueFields.getJSONObject(j);

						String displayName = uniqueField.has(EntityCatalogService.PROP_DISPLAY_NAME)
								? uniqueField.getString(EntityCatalogService.PROP_DISPLAY_NAME)
								: null;
						String value = uniqueField.has(EntityCatalogService.PROP_VALUE) ? uniqueField.getString(EntityCatalogService.PROP_VALUE)
								: null;
						List<NodeRef> propDuplicates = uniqueField.has(EntityCatalogService.PROP_ENTITIES)
								? fromJsonArray(uniqueField.getJSONArray(EntityCatalogService.PROP_ENTITIES))
								: null;

						// Uniquefields
						String message = I18NUtil.getMessage(MESSAGE_NON_UNIQUE_FIELD, displayName, value);

						ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, propDuplicates,
								RequirementDataType.Completion);
						productData.getReqCtrlList().add(rclDataItem);

					}
				}

			}

		}

	}

	private List<NodeRef> fromJsonArray(JSONArray jsonArray) throws JSONException {
		List<NodeRef> ret = new ArrayList<>();
		for (int j = 0; j < jsonArray.length(); j++) {
			ret.add(new NodeRef(jsonArray.getString(j)));

		}
		return ret;
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

}
