package fr.becpg.repo.product.formulation.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONObject;
import org.junit.Test;

import fr.becpg.repo.score.ScoreContext;

/**
 * Unit tests of the Nutri-Score detail carried by the normalized score detail (#37232).
 */
public class NutriScoreContextTest {

	private static final String CLASS_UPPER_VALUE = "0.0";

	private static final String ENERGY_UPPER_VALUE = "335";

	@Test
	public void normalizedDetailCarriesTheClassBoundsOfTheNutriScore() {
		JSONObject source = serializedScoreContext().getSource();

		assertEquals(CLASS_UPPER_VALUE, source.getString("classUpperValue"));
	}

	@Test
	public void normalizedDetailCarriesTheThresholdsOfEachComponent() {
		JSONObject source = serializedScoreContext().getSource();

		assertEquals(ENERGY_UPPER_VALUE, source.getJSONObject("parts").getJSONObject(NutriScoreContext.ENERGY_CODE).getString(NutriScoreContext.UPPER_VALUE));
	}

	@Test
	public void scoreWithoutEngineDetailHasNoSource() {
		ScoreContext context = new ScoreContext();
		context.setCode("MANUAL");

		assertNull(ScoreContext.parse(context.toJSON().toString()).getSource());
	}

	private static ScoreContext serializedScoreContext() {
		return ScoreContext.parse(nutriScoreContext().toScoreContext().toJSON().toString());
	}

	private static NutriScoreContext nutriScoreContext() {
		NutriScoreContext context = new NutriScoreContext();
		context.setNutriScore(0);
		context.setAScore(0);
		context.setCScore(0);
		context.setCategory("Others");
		context.setNutrientClass("A");
		context.setClassLowerValue("-Inf");
		context.setClassUpperValue(CLASS_UPPER_VALUE);
		context.getParts().put(NutriScoreContext.ENERGY_CODE, new JSONObject().put(NutriScoreContext.VALUE, 180d)
				.put(NutriScoreContext.SCORE, 0).put(NutriScoreContext.LOWER_VALUE, "-Inf").put(NutriScoreContext.UPPER_VALUE, ENERGY_UPPER_VALUE));
		return context;
	}
}
