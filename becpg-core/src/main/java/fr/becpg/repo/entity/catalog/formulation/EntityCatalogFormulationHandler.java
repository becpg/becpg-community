package fr.becpg.repo.entity.catalog.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.entity.catalog.CataloguableEntity;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.LargeTextHelper;

/**
 * <p>EntityCatalogFormulationHandler class.</p>
 *
 * @author matthieu
 */
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
			scores.put(EntityCatalogService.PROP_CATALOGS,
					entityCatalogService.formulateCatalogs(scorableEntity, scorableEntity.getReportLocales()));
		} catch (JSONException e) {
			logger.error("Cannot create Json Score", e);
		}

		String entityScore = scores.toString();
		if (entityScore != null && entityScore.length() > LargeTextHelper.TEXT_SIZE_LIMIT) {
			try {
				JSONArray catalogs = scores.optJSONArray(EntityCatalogService.PROP_CATALOGS);
				JSONArray summarizedCatalogs = new JSONArray();
				if (catalogs != null) {
					for (int i = 0; i < catalogs.length(); i++) {
						JSONObject catalog = catalogs.optJSONObject(i);
						if (catalog != null) {
							JSONObject summaryCatalog = new JSONObject();
							Object id = catalog.opt("id");
							if (id != null) {
								summaryCatalog.put("id", id);
							}
							Object label = catalog.opt("label");
							if (label != null) {
								summaryCatalog.put("label", label);
							}
							Object score = catalog.opt("score");
							if (score != null) {
								summaryCatalog.put("score", score);
							}
							summarizedCatalogs.put(summaryCatalog);
						}
					}
				}
				JSONObject summarizedScores = new JSONObject();
				summarizedScores.put(EntityCatalogService.PROP_CATALOGS, summarizedCatalogs);
				entityScore = summarizedScores.toString();
			} catch (JSONException e) {
				logger.error("Cannot create summarized Json Score", e);
				try {
					JSONObject errorScores = new JSONObject();
					errorScores.put("error", "entityScore too large");
					entityScore = errorScores.toString();
				} catch (JSONException jsonException) {
					logger.error("Cannot create error Json Score", jsonException);
					entityScore = null;
				}
			}
		}

		scorableEntity.setEntityScore(entityScore);

		return true;
	}

}
