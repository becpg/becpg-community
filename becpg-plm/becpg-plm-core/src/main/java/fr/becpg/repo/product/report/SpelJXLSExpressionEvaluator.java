package fr.becpg.repo.product.report;

import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxls.expression.ExpressionEvaluator;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>
 * SpelJXLSExpressionEvaluator class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SpelJXLSExpressionEvaluator implements ExpressionEvaluator {

	private SpelFormulaService formulaService;

	private static Log logger = LogFactory.getLog(SpelJXLSExpressionEvaluator.class);

	Expression expression;

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression.getExpressionString();
	}

	/**
	 * <p>
	 * Constructor for SpelJXLSExpressionEvaluator.
	 * </p>
	 *
	 * @param formulaService
	 *            a {@link fr.becpg.repo.formulation.spel.SpelFormulaService}
	 *            object.
	 */
	public SpelJXLSExpressionEvaluator(SpelFormulaService formulaService) {
		super();
		this.formulaService = formulaService;
	}

	/**
	 * <p>
	 * Constructor for SpelJXLSExpressionEvaluator.
	 * </p>
	 *
	 * @param formulaService
	 *            a {@link fr.becpg.repo.formulation.spel.SpelFormulaService}
	 *            object.
	 * @param expression
	 *            a {@link org.springframework.expression.Expression} object.
	 */
	public SpelJXLSExpressionEvaluator(SpelFormulaService formulaService, Expression expression) {
		super();
		this.formulaService = formulaService;
		this.expression = expression;
	}

	/** {@inheritDoc} */
	@Override
	public Object evaluate(String expression, Map<String, Object> data) {
		try {

			if ((expression != null) && expression.startsWith("IMG_")) {
				return data.get(expression);
			}

			StandardEvaluationContext context = null;

			if (data.containsKey("dataListItem")) {
				context = formulaService.createDataListItemSpelContext((RepositoryEntity) data.get("entity"),
						(RepositoryEntity) data.get("dataListItem"));
			} else if (data.containsKey("entity")) {
				context = formulaService.createEntitySpelContext((RepositoryEntity) data.get("entity"));
			} else {
				context = formulaService.createSpelContext(data);
			}
			ExpressionParser parser = formulaService.getSpelParser();

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
			if (RetryingTransactionHelper.extractRetryCause(e) != null) {
				throw e;
			}
			if (logger.isDebugEnabled()) {
				logger.debug(e, e);
			}

			return "Wrong expression: " + expression + " - " + e.getMessage();
		}

		return "";

	}

	/** {@inheritDoc} */
	@Override
	public Object evaluate(Map<String, Object> data) {

		StandardEvaluationContext context = formulaService.createEntitySpelContext((RepositoryEntity) data.get("entity"));

		return expression.getValue(context);

	}

}
