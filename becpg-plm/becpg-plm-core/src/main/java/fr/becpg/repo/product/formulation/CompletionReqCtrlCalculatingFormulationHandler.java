package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
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

import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.CompositionDataItem;

/**
 * <p>CompletionReqCtrlCalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompletionReqCtrlCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompletionReqCtrlCalculatingFormulationHandler.class);

	/** Constant <code>MESSAGE_NON_VALIDATED_STATE="message.formulate.nonValidatedState"</code> */
	public static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";
	/** Constant <code>MESSAGE_MANDATORY_FIELD_MISSING="message.formulate.mandatory_property"</code> */
	public static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory_property";
	/** Constant <code>MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED="message.formulate.mandatory_property_lo"{trunked}</code> */
	public static final String MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED = "message.formulate.mandatory_property_localized";

	/** Constant <code>MESSAGE_NON_UNIQUE_FIELD="message.formulate.non-unique-field"</code> */
	public static final String MESSAGE_NON_UNIQUE_FIELD = "message.formulate.non-unique-field";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private SpelFormulaService formulaService;
	private EntityCatalogService entityCatalogService;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object.
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/**
	 * <p>Setter for the field <code>entityCatalogService</code>.</p>
	 *
	 * @param entityCatalogService a {@link fr.becpg.repo.entity.catalog.EntityCatalogService} object.
	 */
	public void setEntityCatalogService(EntityCatalogService entityCatalogService) {
		this.entityCatalogService = entityCatalogService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData product) throws FormulateException {
		if (logger.isDebugEnabled()) {
			logger.debug("===== Calculating score of product " + product.getName() + " =====");
		}

		JSONObject scores = new JSONObject();

		try {

			// checks if mandatory fields are present
			JSONArray catalogs = entityCatalogService.formulateCatalogs(product.getNodeRef(), product.getReportLocales(), formula -> {
				boolean res = true;

				StandardEvaluationContext context = formulaService.createEntitySpelContext(product);

				if (context != null) {
					ExpressionParser parser = new SpelExpressionParser();
					Expression expression = parser.parseExpression(formula);

					try {
						Boolean result = (Boolean) (expression.getValue(context));
						if (logger.isDebugEnabled()) {
							logger.debug("Expression " + expression + " returned " + result);
						}
						res = result.booleanValue();
					} catch (Exception e) {
						logger.error("Unable to parse expression " + formula, e);
						logger.debug("Creating new CtrlListDataItem (method productMatchesOnFormula...)");
						ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated,
								new MLText("Unable to parse formula " + formula), null, new ArrayList<NodeRef>(), RequirementDataType.Completion);
						product.getReqCtrlList().add(rclDataItem);
						res = false;
					}
				}

				return res;
			});

			// Unique fields :

			extractReqCtrl(product, catalogs);

			ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated,
					MLTextHelper.getI18NMessage(MESSAGE_NON_VALIDATED_STATE), null, new ArrayList<NodeRef>(), RequirementDataType.Validation);

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
						MLText displayName = extractDisplayName(missingField);

						// Mandatory fields
						final MLText message = (lang != null
								? MLTextHelper.getI18NMessage(MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED, displayName, catalogName, "(" + lang + ")")
								: MLTextHelper.getI18NMessage(MESSAGE_MANDATORY_FIELD_MISSING, displayName, catalogName));

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

						MLText displayName = extractDisplayName(uniqueField);

						String value = uniqueField.has(EntityCatalogService.PROP_VALUE) ? uniqueField.getString(EntityCatalogService.PROP_VALUE)
								: null;
						List<NodeRef> propDuplicates = uniqueField.has(EntityCatalogService.PROP_ENTITIES)
								? fromJsonArray(uniqueField.getJSONArray(EntityCatalogService.PROP_ENTITIES))
								: null;

						// Uniquefields

						ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden,
								MLTextHelper.getI18NMessage(MESSAGE_NON_UNIQUE_FIELD, displayName, value), null, propDuplicates,
								RequirementDataType.Completion);
						productData.getReqCtrlList().add(rclDataItem);

					}
				}

			}

		}

	}

	private MLText extractDisplayName(JSONObject missingField) {

		return MLTextHelper.createMLTextI18N(loc -> {
			try {
				if (missingField.has(EntityCatalogService.PROP_DISPLAY_NAME + "_" + MLTextHelper.localeKey(loc))) {
					return missingField.getString(EntityCatalogService.PROP_DISPLAY_NAME + "_" + MLTextHelper.localeKey(loc));
				} else if (missingField.has(EntityCatalogService.PROP_DISPLAY_NAME)) {
					return missingField.getString(EntityCatalogService.PROP_DISPLAY_NAME);
				}
				return "";
			} catch (JSONException e) {
				return "";
			}

		});
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
	 * @param node a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	public boolean checkProductValidity(NodeRef node) {
		ProductData found = alfrescoRepository.findOne(node);
		if (found != null) {
			return SystemState.Valid.equals(found.getState());
		}
		return false;
	}

}
