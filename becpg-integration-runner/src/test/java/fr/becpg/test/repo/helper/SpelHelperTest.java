package fr.becpg.test.repo.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.formulation.spel.BeCPGSpelExpressionParser;
import fr.becpg.repo.product.formulation.FormulaFormulationHandler;
import org.springframework.expression.EvaluationException;

public class SpelHelperTest {


	
	
	@Test
	public void test() {

		//Test register
		new FormulaFormulationHandler();
		
		
		Assert.assertEquals(SpelHelper.formatFormula("cost['workspace://SpacesStore/c558bfcf-e996-4df4-9eb6-5061b1f7a8d0'][0].valuePerProduct"),
				"costList.^[cost.toString() == 'workspace://SpacesStore/c558bfcf-e996-4df4-9eb6-5061b1f7a8d0'][0].valuePerProduct");

		Assert.assertEquals(SpelHelper.formatFormula("physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value"),
				"physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value");

		Assert.assertEquals(SpelHelper.formatFormula(
				"!dataListItemEntity.physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'].isEmpty() and dataListItemEntity.physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value != null ? dataListItemEntity.physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value * dataListItem.qty / entity.recipeQtyUsed : \"\""),
				"!dataListItemEntity.physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'].isEmpty() and dataListItemEntity.physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value != null ? dataListItemEntity.physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value * dataListItem.qty / entity.recipeQtyUsed : \"\"");
	}

	@Test
	public void testUnsafeSpel() {
		BeCPGSpelExpressionParser parser = new BeCPGSpelExpressionParser();

		String[] unsafeExpressions = {
			"class",
			"classLoader",
			"exec('ls')",
			"forName('java.lang.Runtime')",
			"getClass()",
			"getClass().getClassLoader()",
			"getClass().getConstructor()",
			"getClass().getConstructors()",
			"getClass().getDeclaredField('x')",
			"getClass().getDeclaredFields()",
			"getClass().getDeclaredMethods()",
			"getClass().getMethods()",
			"class.forName('java.lang.Runtime')",
			"classLoader.loadClass('java.lang.Runtime')",
			"declaredFields",
			"declaredMethods",
			"declaredConstructors",
			"declaredClasses"
		};

		for (String expr : unsafeExpressions) {
			try {
				parser.parseExpression(expr);
				Assert.fail("Expression should have been rejected as unsafe: " + expr);
			} catch (EvaluationException e) {
				System.out.println("Successfully blocked expression: " + expr + " with error: " + e.getMessage());
			}
		}
	}

	@Test
	public void testDeepSpelExpression() {
		BeCPGSpelExpressionParser parser = new BeCPGSpelExpressionParser();
		StringBuilder sb = new StringBuilder("'x'");
		for (int i = 0; i < 2000; i++) {
			sb.append("+'x'");
		}
		try {
			parser.parseExpression(sb.toString());
			System.out.println("Deep expression successfully parsed without StackOverflowError");
		} catch (Exception e) {
			System.out.println("Deep expression parsing threw expected exception: " + e.getMessage());
		}
	}

}
