package fr.becpg.repo.ecm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * Unit tests of the scores and requirements stored on a change unit.
 */
public class ChangeUnitScoreBuilderTest {

	private static final String SUPPORTED_LOCALES = "fr,en";

	private static final String MISSING_FIELD = "Mandatory field 'GTIN' is missing";

	private static final String CADMIUM_ABOVE_LIMIT = "Cadmium above 0.30 mg/kg";

	private static final String ALLERGEN_NOT_VALIDATED = "Allergens not validated";

	private static final String PREVIOUS_SCORE = "{\"global\":60,\"totalForbidden\":36,\"catalogs\":[]}";

	private static final int MAX_PROPERTY_BYTES = 65535;

	private static final Locale[] TRANSLATED_LOCALES = { Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN, new Locale("es"),
			new Locale("pt"), new Locale("nl"), new Locale("sv"), new Locale("fi"), new Locale("tr"), new Locale("ru"), Locale.JAPANESE,
			Locale.CHINESE };

	private static final String LONG_TEXT = "Erreur lors de la g\u00e9n\u00e9ration du rapport : \u00e9chec sans d\u00e9tail ".repeat(4);

	private static final String SIMULATED_SCORE = "{\"global\":67,\"totalForbidden\":1,\"catalogs\":[],\"ctrlCount\":[]}";

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
	public void simulatedScoresAreKeptWithoutTheCatalogDetails() {
		JSONObject result = build(product(PREVIOUS_SCORE), product(SIMULATED_SCORE));

		assertEquals(67, result.getInt("global"));
		assertEquals(1, result.getInt("totalForbidden"));
		assertFalse(result.has("catalogs"));
	}

	@Test
	public void previousScoresAreThoseBeforeTheChangeOrder() {
		JSONObject previous = build(product(PREVIOUS_SCORE), product(SIMULATED_SCORE)).getJSONObject(ChangeUnitScoreBuilder.PROP_PREVIOUS);

		assertEquals(60, previous.getInt("global"));
		assertEquals(36, previous.getInt("totalForbidden"));
	}

	@Test
	public void noPreviousScoresWhenTheProductWasNeverFormulated() {
		assertFalse(build(product(null), product(SIMULATED_SCORE)).has(ChangeUnitScoreBuilder.PROP_PREVIOUS));
	}

	@Test
	public void specificationNonConformityComesBeforeIncompleteField() {
		ProductData simulated = product(SIMULATED_SCORE, requirement(RequirementType.Forbidden, RequirementDataType.Completion, MISSING_FIELD),
				requirement(RequirementType.Forbidden, RequirementDataType.Specification, CADMIUM_ABOVE_LIMIT));

		JSONArray requirements = build(product(PREVIOUS_SCORE), simulated).getJSONArray(ChangeUnitScoreBuilder.PROP_REQUIREMENTS);

		assertEquals(CADMIUM_ABOVE_LIMIT, messageOf(requirements.getJSONObject(0)));
	}

	@Test
	public void forbiddenComesBeforeTolerated() {
		ProductData simulated = product(SIMULATED_SCORE, requirement(RequirementType.Tolerated, RequirementDataType.Allergen, ALLERGEN_NOT_VALIDATED),
				requirement(RequirementType.Forbidden, RequirementDataType.Completion, MISSING_FIELD));

		JSONArray requirements = build(product(PREVIOUS_SCORE), simulated).getJSONArray(ChangeUnitScoreBuilder.PROP_REQUIREMENTS);

		assertEquals(MISSING_FIELD, messageOf(requirements.getJSONObject(0)));
	}

	@Test
	public void requirementIntroducedByTheChangeIsFlaggedNew() {
		ProductData before = product(PREVIOUS_SCORE, requirement(RequirementType.Tolerated, RequirementDataType.Allergen, ALLERGEN_NOT_VALIDATED));
		ProductData simulated = product(SIMULATED_SCORE, requirement(RequirementType.Tolerated, RequirementDataType.Allergen, ALLERGEN_NOT_VALIDATED),
				requirement(RequirementType.Forbidden, RequirementDataType.Specification, CADMIUM_ABOVE_LIMIT));

		JSONArray requirements = build(before, simulated).getJSONArray(ChangeUnitScoreBuilder.PROP_REQUIREMENTS);

		assertTrue(requirements.getJSONObject(0).getBoolean(ChangeUnitScoreBuilder.PROP_IS_NEW));
		assertFalse(requirements.getJSONObject(1).getBoolean(ChangeUnitScoreBuilder.PROP_IS_NEW));
	}

	@Test
	public void requirementThatDisappearsIsResolved() {
		ProductData before = product(PREVIOUS_SCORE, requirement(RequirementType.Forbidden, RequirementDataType.Specification, CADMIUM_ABOVE_LIMIT));

		JSONObject result = build(before, product(SIMULATED_SCORE));

		assertEquals(1, result.getInt(ChangeUnitScoreBuilder.PROP_RESOLVED_COUNT));
		assertEquals(CADMIUM_ABOVE_LIMIT, messageOf(result.getJSONArray(ChangeUnitScoreBuilder.PROP_RESOLVED).getJSONObject(0)));
	}

	@Test
	public void snapshotIsNotAlteredByTheFormulation() {
		RequirementListDataItem requirement = requirement(RequirementType.Forbidden, RequirementDataType.Specification, CADMIUM_ABOVE_LIMIT);
		ProductData product = product(PREVIOUS_SCORE, requirement);
		ChangeUnitScoreBuilder builder = ChangeUnitScoreBuilder.before(product);

		requirement.setReqType(RequirementType.Info);
		product.getReqCtrlList().clear();
		JSONObject resolved = new JSONObject(builder.buildFor(product(SIMULATED_SCORE))).getJSONArray(ChangeUnitScoreBuilder.PROP_RESOLVED)
				.getJSONObject(0);

		assertEquals(RequirementType.Forbidden.toString(), resolved.getString(ChangeUnitScoreBuilder.PROP_REQ_TYPE));
	}

	@Test
	public void requirementsAreCappedButCounted() {
		List<RequirementListDataItem> requirements = new ArrayList<>();
		for (int i = 0; i <= ChangeUnitScoreBuilder.MAX_REQUIREMENTS; i++) {
			requirements.add(requirement(RequirementType.Tolerated, RequirementDataType.Completion, MISSING_FIELD + i));
		}

		JSONObject result = build(product(PREVIOUS_SCORE), product(SIMULATED_SCORE, requirements.toArray(new RequirementListDataItem[0])));

		assertEquals(ChangeUnitScoreBuilder.MAX_REQUIREMENTS, result.getJSONArray(ChangeUnitScoreBuilder.PROP_REQUIREMENTS).length());
		assertEquals(ChangeUnitScoreBuilder.MAX_REQUIREMENTS + 1, result.getInt(ChangeUnitScoreBuilder.PROP_REQUIREMENTS_COUNT));
	}

	@Test
	public void identicalTranslationsAreStoredOnce() {
		MLText message = new MLText();
		for (Locale locale : TRANSLATED_LOCALES) {
			message.addValue(locale, MISSING_FIELD);
		}
		ProductData simulated = product(SIMULATED_SCORE, RequirementListDataItem.build().ofType(RequirementType.Forbidden)
				.ofDataType(RequirementDataType.Completion).withMessage(message));

		JSONObject storedMessage = build(product(PREVIOUS_SCORE), simulated).getJSONArray(ChangeUnitScoreBuilder.PROP_REQUIREMENTS)
				.getJSONObject(0).getJSONObject(ChangeUnitScoreBuilder.PROP_MESSAGE);

		assertEquals(1, storedMessage.length());
	}

	@Test
	public void multilingualRequirementsFitInOneProperty() {
		List<RequirementListDataItem> requirements = new ArrayList<>();
		for (int i = 0; i < ChangeUnitScoreBuilder.MAX_REQUIREMENTS; i++) {
			MLText message = new MLText();
			for (Locale locale : TRANSLATED_LOCALES) {
				message.addValue(locale, locale + " " + i + " " + LONG_TEXT);
			}
			requirements.add(RequirementListDataItem.build().ofType(RequirementType.Tolerated).ofDataType(RequirementDataType.Completion)
					.withMessage(message));
		}

		String json = ChangeUnitScoreBuilder.before(product(PREVIOUS_SCORE, requirements.toArray(new RequirementListDataItem[0])))
				.buildFor(product(SIMULATED_SCORE, requirements.toArray(new RequirementListDataItem[0])));

		assertTrue(json.getBytes(StandardCharsets.UTF_8).length < MAX_PROPERTY_BYTES);
		assertEquals(ChangeUnitScoreBuilder.MAX_REQUIREMENTS, new JSONObject(json).getInt(ChangeUnitScoreBuilder.PROP_REQUIREMENTS_COUNT));
	}

	private static JSONObject build(ProductData before, ProductData simulated) {
		return new JSONObject(ChangeUnitScoreBuilder.before(before).buildFor(simulated));
	}

	private static ProductData product(String entityScore, RequirementListDataItem... requirements) {
		FinishedProductData product = new FinishedProductData();
		product.setEntityScore(entityScore);
		product.setReqCtrlList(new ArrayList<>(List.of(requirements)));
		return product;
	}

	private static RequirementListDataItem requirement(RequirementType reqType, RequirementDataType reqDataType, String message) {
		return RequirementListDataItem.build().ofType(reqType).ofDataType(reqDataType).withMessage(new MLText(Locale.ENGLISH, message));
	}

	private static String messageOf(JSONObject requirement) {
		JSONObject message = requirement.getJSONObject(ChangeUnitScoreBuilder.PROP_MESSAGE);
		return message.has(Locale.ENGLISH.toString()) ? message.getString(Locale.ENGLISH.toString())
				: message.getString(ChangeUnitScoreBuilder.DEFAULT_LOCALE_KEY);
	}
}
