package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * Unit tests of the level given to the missing mandatory fields of a catalog.
 */
public class CompletionReqCtrlCalculatingFormulationHandlerTest {

	private static final String SUPPORTED_LOCALES = "fr,en";

	private static final String CATALOG_LABEL = "GS1";

	private static final String MISSING_FIELD = "GTIN/EAN";

	private final CompletionReqCtrlCalculatingFormulationHandler handler = new CompletionReqCtrlCalculatingFormulationHandler();

	@Before
	public void setUp() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales(SUPPORTED_LOCALES);
	}

	@After
	public void tearDown() {
		MLTextHelper.flushCache();
	}

	@Test
	public void missingFieldIsToleratedInSimulation() {
		assertEquals(RequirementType.Tolerated, formulateMissingField(SystemState.Simulation).getReqType());
	}

	@Test
	public void missingFieldIsForbiddenOnceSubmittedForValidation() {
		assertEquals(RequirementType.Forbidden, formulateMissingField(SystemState.ToValidate).getReqType());
	}

	@Test
	public void missingFieldIsForbiddenOnValidProduct() {
		assertEquals(RequirementType.Forbidden, formulateMissingField(SystemState.Valid).getReqType());
	}

	@Test
	public void missingFieldKeepsTheCompletionDataType() {
		assertEquals(RequirementDataType.Completion, formulateMissingField(SystemState.Simulation).getReqDataType());
	}

	private RequirementListDataItem formulateMissingField(SystemState state) {
		FinishedProductData product = new FinishedProductData();
		product.setState(state);
		product.setReqCtrlList(new ArrayList<>());
		product.setEntityScore(scoreWithMissingField().toString());

		handler.process(product);

		List<RequirementListDataItem> requirements = product.getReqCtrlList();
		assertEquals(1, requirements.size());
		return requirements.get(0);
	}

	private static JSONObject scoreWithMissingField() {
		JSONObject missingField = new JSONObject();
		missingField.put(EntityCatalogService.PROP_DISPLAY_NAME, MISSING_FIELD);

		JSONObject catalog = new JSONObject();
		catalog.put(EntityCatalogService.PROP_LABEL, CATALOG_LABEL);
		catalog.put(EntityCatalogService.PROP_MISSING_FIELDS, new JSONArray().put(missingField));

		JSONObject score = new JSONObject();
		score.put(EntityCatalogService.PROP_CATALOGS, new JSONArray().put(catalog));
		return score;
	}
}
