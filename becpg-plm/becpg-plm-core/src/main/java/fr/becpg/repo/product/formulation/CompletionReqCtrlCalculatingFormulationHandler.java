package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.repository.model.EffectiveDataItem;

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

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData product) {
		JSONObject scores = new JSONObject();

		try {

			if (product.getEntityScore() != null) {
				scores = new JSONObject(product.getEntityScore());
				if (scores.has(EntityCatalogService.PROP_CATALOGS)) {
					JSONArray catalogs = scores.getJSONArray(EntityCatalogService.PROP_CATALOGS);
					extractReqCtrl(product, catalogs);

					ReqCtrlListDataItem rclDataItem = ReqCtrlListDataItem.tolerated()
							.withMessage(MLTextHelper.getI18NMessage(MESSAGE_NON_VALIDATED_STATE)).ofDataType(RequirementDataType.Validation);

					boolean shouldAdd = false;

					Predicate<EffectiveDataItem> predicate = new EffectiveFilters<>(EffectiveFilters.EFFECTIVE).createPredicate(product);

					// visits all refs and adds rclDataItem to them if required
					for (AbstractProductDataView view : product.getViews()) {
						if (view.getMainDataList() != null) {
							for (CompositionDataItem dataItem : view.getMainDataList()) {
								if ((dataItem.getComponent() != null) && !checkProductValidity(dataItem.getComponent()) && predicate.test(dataItem)) {
									rclDataItem.addSource(dataItem.getComponent());
									shouldAdd = true;

								}
							}
						}
					}

					if (shouldAdd) {
						product.getReqCtrlList().add(rclDataItem);
					}

					scores.put(EntityCatalogService.PROP_CATALOGS, catalogs);

				}
			}

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

						ReqCtrlListDataItem rclDataItem = ReqCtrlListDataItem.forbidden().withMessage(message)
								.ofDataType(RequirementDataType.Completion);

						productData.getReqCtrlList().add(rclDataItem);

					}
				}
			}

			if (catalog.has(EntityCatalogService.PROP_NON_UNIQUE_FIELDS)) {
				JSONArray uniqueFields = catalog.getJSONArray(EntityCatalogService.PROP_NON_UNIQUE_FIELDS);
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

						ReqCtrlListDataItem rclDataItem = ReqCtrlListDataItem.forbidden()
								.withMessage(MLTextHelper.getI18NMessage(MESSAGE_NON_UNIQUE_FIELD, displayName, value)).withSources(propDuplicates)

								.ofDataType(RequirementDataType.Completion);

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
			return SystemState.Valid.equals(found.getState()) || SystemState.Stopped.equals(found.getState())
					|| SystemState.Archived.equals(found.getState());
		}
		return false;
	}

}
