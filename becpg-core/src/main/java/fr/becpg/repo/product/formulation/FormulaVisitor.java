package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;

/**
 * Use Spring EL to parse formula and compute value
 * 
 * @author matthieu
 * 
 */
public class FormulaVisitor implements ProductVisitor {

	private static Log logger = LogFactory.getLog(FormulaVisitor.class);

	@Override
	public ProductData visit(ProductData productData) throws FormulateException {

		ExpressionParser parser = new SpelExpressionParser();
		EvaluationContext context = new StandardEvaluationContext(productData);


		if (productData.getDynamicCharactList() != null) {
			for (DynamicCharactListItem dynamicCharactListItem : productData.getDynamicCharactList()) {
				try {
					logger.debug("Parse formula : " + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")");
					Expression exp = parser.parseExpression(dynamicCharactListItem.getFormula());
					dynamicCharactListItem.setValue(exp.getValue(context));		
					logger.debug("Value :" + dynamicCharactListItem.getValue());
				} catch (Exception e) {
					dynamicCharactListItem.setValue("#Error");
					logger.warn("Error in formula :" + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")", e);
				}
			}
		}
		return productData;
	}
	
}
