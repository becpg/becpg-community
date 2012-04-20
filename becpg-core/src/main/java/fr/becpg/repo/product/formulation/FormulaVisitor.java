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
import fr.becpg.repo.product.data.productList.DynamicCharachListItem;

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


		if (productData.getDynamicCharachList() != null) {
			for (DynamicCharachListItem dynamicCharachListItem : productData.getDynamicCharachList()) {
				try {
					logger.debug("Parse formula : " + dynamicCharachListItem.getDynamicCharachFormula() + " (" + dynamicCharachListItem.getDynamicCharachTitle() + ")");
					Expression exp = parser.parseExpression(dynamicCharachListItem.getDynamicCharachFormula());
					dynamicCharachListItem.setDynamicCharachValue(exp.getValue(context));		
					logger.debug("Value :" + dynamicCharachListItem.getDynamicCharachValue());
				} catch (Exception e) {
					dynamicCharachListItem.setDynamicCharachValue("#Error");
					logger.warn("Error in formula :" + dynamicCharachListItem.getDynamicCharachFormula() + " (" + dynamicCharachListItem.getDynamicCharachTitle() + ")", e);
				}
			}
		}
		return productData;
	}
	
}
