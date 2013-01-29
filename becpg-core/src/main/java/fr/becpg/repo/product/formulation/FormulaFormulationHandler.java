package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;

/**
 * Use Spring EL to parse formula and compute value
 * 
 * @author matthieu
 * 
 */
public class FormulaFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(FormulaFormulationHandler.class);

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		ExpressionParser parser = new SpelExpressionParser();
		EvaluationContext context = new StandardEvaluationContext(productData);


		if (productData.getCompoListView()!=null &&  productData.getCompoListView().getDynamicCharactList() != null) {
			for (DynamicCharactListItem dynamicCharactListItem : productData.getCompoListView().getDynamicCharactList()) {
				computeFormula(context, parser, dynamicCharactListItem);
			}
		}
		
		if (productData.getPackagingListView()!=null &&  productData.getPackagingListView().getDynamicCharactList() != null) {
			for (DynamicCharactListItem dynamicCharactListItem : productData.getPackagingListView().getDynamicCharactList()) {
				computeFormula(context, parser, dynamicCharactListItem);
			}
		}
		
		if (productData.getProcessListView()!=null &&  productData.getProcessListView().getDynamicCharactList() != null) {
			for (DynamicCharactListItem dynamicCharactListItem : productData.getProcessListView().getDynamicCharactList()) {
				computeFormula(context, parser, dynamicCharactListItem);
			}
		}
		
		return true;
	}

	private void computeFormula(EvaluationContext context, ExpressionParser parser, DynamicCharactListItem dynamicCharactListItem) {
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
