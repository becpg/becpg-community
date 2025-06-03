package fr.becpg.repo.entity.catalog.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.entity.catalog.CataloguableEntity;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.formulation.FormulationBaseHandler;

public class EntityCatalogFormulationHandler extends FormulationBaseHandler<CataloguableEntity> {

	private static final Log logger = LogFactory.getLog(EntityCatalogFormulationHandler.class);

	/** Constant <code>MESSAGE_NON_VALIDATED_STATE="message.formulate.nonValidatedState"</code> */
	public static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";
	/** Constant <code>MESSAGE_MANDATORY_FIELD_MISSING="message.formulate.mandatory_property"</code> */
	public static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory_property";
	/** Constant <code>MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED="message.formulate.mandatory_property_lo"{trunked}</code> */
	public static final String MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED = "message.formulate.mandatory_property_localized";

	/** Constant <code>MESSAGE_NON_UNIQUE_FIELD="message.formulate.non-unique-field"</code> */
	public static final String MESSAGE_NON_UNIQUE_FIELD = "message.formulate.non-unique-field";

	private EntityCatalogService entityCatalogService;


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
	public boolean process(CataloguableEntity scorableEntity) {
		JSONObject scores = new JSONObject();

		try {
			scores.put(EntityCatalogService.PROP_CATALOGS, entityCatalogService.formulateCatalogs(scorableEntity, scorableEntity.getReportLocales()));
		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		scorableEntity.setEntityScore(scores.toString());

		return true;
	}

}