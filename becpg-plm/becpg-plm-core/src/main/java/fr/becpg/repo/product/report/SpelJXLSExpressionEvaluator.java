package fr.becpg.repo.product.report;

import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxls.expression.ExpressionEvaluator;
import org.springframework.expression.Expression;
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

	private final SpelFormulaService formulaService;

	/** Constant <code>logger</code> */
	private static Log logger = LogFactory.getLog(SpelJXLSExpressionEvaluator.class);

	private final String expression;

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	/**
	 * <p>
	 * Constructor for SpelJXLSExpressionEvaluator.
	 * </p>
	 *
	 * JXLS 3 commands (jx:each items, jx:if condition) ask the factory for an
	 * evaluator bound to their expression and then call
	 * {@link #evaluate(Map)}, so the expression must be kept here.
	 *
	 * @param formulaService
	 *            a {@link fr.becpg.repo.formulation.spel.SpelFormulaService}
	 *            object.
	 * @param expression
	 *            the expression this evaluator is bound to, null for cell
	 *            expressions which are passed to
	 *            {@link #evaluate(String, Map)}
	 */
	public SpelJXLSExpressionEvaluator(SpelFormulaService formulaService, String expression) {
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

			String[] formulas = SpelHelper.formatMTFormulas(expression);
			for (String formula : formulas) {

				Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
				if (varFormulaMatcher.matches()) {
					logger.debug("Variable formula : " + varFormulaMatcher.group(2) + " (" + varFormulaMatcher.group(1) + ")");
					Expression exp = formulaService.parseExpression(varFormulaMatcher.group(2));
					context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
				} else {
					logger.debug("Parse formula : " + formula);
					Expression exp = formulaService.parseExpression(formula);
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
		return evaluate(expression, data);
	}

}
