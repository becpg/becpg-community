package fr.becpg.test.repo.product.rounding;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

import fr.becpg.repo.product.formulation.rounding.NutrientRoundingRules;
import fr.becpg.repo.product.formulation.rounding.NutrientRoundingRules.NutrientRoundingRuleType;

//TODO
public class NutrientRoundingRulesTest {

	@Test
	public void testRoundingRules() {
		assertEquals(NutrientRoundingRules.round(1.55d, NutrientRoundingRuleType.NRJ.toString(), Locale.FRENCH), 1.5d,0);
		assertEquals(NutrientRoundingRules.round(1.55d, NutrientRoundingRuleType.NRJ.toString(), Locale.US), 1.5d,0);
	}

}
