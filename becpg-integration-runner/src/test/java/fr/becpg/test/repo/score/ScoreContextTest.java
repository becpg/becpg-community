package fr.becpg.test.repo.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScorePart;
import fr.becpg.repo.score.ScoreScale;

/**
 * Checks the normalized format every score publishes its breakdown in.
 *
 * @author matthieu
 */
public class ScoreContextTest {

	private static final double PRECISION = 0.01d;

	@Test
	public void testSerializationRoundTrip() {
		ScoreContext context = new ScoreContext();
		context.setCode("ECOBALYSE");
		context.setVersion("3.1");
		context.setValue(123.4d);
		context.setUnit("µPt/kg");
		context.setScoreClass("B");
		context.setScale(ScoreScale.Numeric.name());
		context.getParts().add(new ScorePart("cch").withLabel("Changement climatique").withValue(1.2d, "kgCO2e")
				.withCoefficients(7553.08d, 0.2106d).withContribution(33.4d));
		context.getSteps().add(new ScorePart("complements").withLabel("Compléments hors-ACV").withContribution(-12d));

		ScoreContext parsed = ScoreContext.parse(context.toJSON().toString());

		assertEquals("ECOBALYSE", parsed.getCode());
		assertEquals("3.1", parsed.getVersion());
		assertEquals(123.4d, parsed.getValue(), PRECISION);
		assertEquals("µPt/kg", parsed.getUnit());
		assertEquals("B", parsed.getScoreClass());
		assertEquals(ScoreScale.Numeric.name(), parsed.getScale());

		assertEquals(1, parsed.getParts().size());
		ScorePart part = parsed.getParts().get(0);
		assertEquals("cch", part.getCode());
		assertEquals("Changement climatique", part.getLabel());
		assertEquals(1.2d, part.getValue(), PRECISION);
		assertEquals("kgCO2e", part.getUnit());
		assertEquals(7553.08d, part.getNormalization(), PRECISION);
		assertEquals(0.2106d, part.getWeight(), PRECISION);
		assertEquals(33.4d, part.getContribution(), PRECISION);

		assertEquals(1, parsed.getSteps().size());
		assertEquals(-12d, parsed.getSteps().get(0).getContribution(), PRECISION);
	}

	@Test
	public void testSharesUseAbsoluteContributions() {
		ScoreContext context = new ScoreContext();
		context.getParts().add(new ScorePart("cch").withContribution(60d));
		context.getParts().add(new ScorePart("wtu").withContribution(-20d));
		context.getParts().add(new ScorePart("ldu").withContribution(20d));

		context.computeShares();

		assertEquals(60d, context.getParts().get(0).getShare(), PRECISION);
		assertEquals(20d, context.getParts().get(1).getShare(), PRECISION);
		assertEquals(20d, context.getParts().get(2).getShare(), PRECISION);
	}

	@Test
	public void testSharesAreLeftAloneWhenNothingContributes() {
		ScoreContext context = new ScoreContext();
		context.getParts().add(new ScorePart("cch").withContribution(0d));

		context.computeShares();

		assertNull(context.getParts().get(0).getShare());
	}

	@Test
	public void testMissingValuesSurviveTheRoundTrip() {
		ScoreContext context = new ScoreContext();
		context.setCode("NUTRISCORE");
		context.getParts().add(new ScorePart("PRO-"));

		ScoreContext parsed = ScoreContext.parse(context.toJSON().toString());

		assertNull(parsed.getValue());
		assertNull(parsed.getParts().get(0).getValue());
		assertNull(parsed.getParts().get(0).getContribution());
	}

}
