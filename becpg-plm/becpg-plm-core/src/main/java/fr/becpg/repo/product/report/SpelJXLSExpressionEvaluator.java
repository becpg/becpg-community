package fr.becpg.repo.product.report;

import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxls.expression.ExpressionEvaluator;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.product.formulation.FormulaService;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 *
 * @author matthieu
 *
 */
public class SpelJXLSExpressionEvaluator implements ExpressionEvaluator {

	private FormulaService formulaService;

	private static Log logger = LogFactory.getLog(SpelJXLSExpressionEvaluator.class);

	Expression expression;

	@Override
	public String getExpression() {
		return expression.getExpressionString();
	}

	public SpelJXLSExpressionEvaluator(FormulaService formulaService) {
		super();
		this.formulaService = formulaService;
	}

	public SpelJXLSExpressionEvaluator(FormulaService formulaService, Expression expression) {
		super();
		this.formulaService = formulaService;
		this.expression = expression;
	}

	
	
	@Override
	public Object evaluate(String expression, Map<String, Object> data) {
		try {

			if(expression!=null && expression.startsWith("IMG_")) {
					return data.get(expression);
			}
			
			StandardEvaluationContext context = null;

			 if (data.containsKey("dataListItem")) {
				context = formulaService.createEvaluationContext((ProductData) data.get("entity"), (RepositoryEntity) data.get("dataListItem"));
			} else if (data.containsKey("entity")) {
				context = formulaService.createEvaluationContext((ProductData) data.get("entity"));
			} else {
				context = new StandardEvaluationContext(data);
			}
			ExpressionParser parser = new SpelExpressionParser();

			String[] formulas = SpelHelper.formatMTFormulas(expression);
			for (String formula : formulas) {

				Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
				if (varFormulaMatcher.matches()) {
					logger.debug("Variable formula : " + varFormulaMatcher.group(2) + " (" + varFormulaMatcher.group(1) + ")");
					Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
					context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
				} else {
					logger.debug("Parse formula : " + formula);
					Expression exp = parser.parseExpression(formula);
					return exp.getValue(context);
				}
			}
		} catch (Exception e) {
			if (e instanceof ConcurrencyFailureException) {
				throw (ConcurrencyFailureException) e;
			}
			logger.error("wrong expression: "+expression ,e);
		}
		

		return "";

	}

	@Override
	public Object evaluate(Map<String, Object> data) {
		StandardEvaluationContext context = formulaService.createEvaluationContext((ProductData) data.get("entity"));

		return expression.getValue(context);
	}

}
