package fr.becpg.repo.product.formulation;

import java.util.Collection;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext.Operator;
import fr.becpg.repo.product.formulation.labeling.LabelingFormulaContext;
import fr.becpg.repo.product.formulation.spel.CustomSpelFunctions;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;

@Service("formulaService")
public class FormulaService {

	private static final Log logger = LogFactory.getLog(FormulaService.class);

	@Autowired
	private SecurityMethodBeforeAdvice securityMethodBeforeAdvice;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private CustomSpelFunctions[] customSpelFunctions;

	private void registerCustomFunctions(ProductData productData, StandardEvaluationContext context) {

		context.setBeanResolver((context1, beanName) -> {

			for (CustomSpelFunctions customSpelFunction : customSpelFunctions) {
				if (customSpelFunction.match(beanName)) {
					return customSpelFunction.create(productData);
				}
			}
			return null;
		});
	}

	public RepositoryEntity createSecurityProxy(RepositoryEntity productData) {
		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(productData);
		factory.addAdvice(securityMethodBeforeAdvice);
		return (RepositoryEntity) factory.getProxy();
	}

	public StandardEvaluationContext createEvaluationContext(ProductData productData) {
		StandardEvaluationContext context = new StandardEvaluationContext(createSecurityProxy(productData));
		registerCustomFunctions(productData, context);
		return context;
	}

	public StandardEvaluationContext createEvaluationContext(ProductData productData, LabelingFormulaContext labelingFormulaContext) {
		StandardEvaluationContext context = new StandardEvaluationContext(labelingFormulaContext);
		labelingFormulaContext.setEntity((ProductData) createSecurityProxy(productData));
		registerCustomFunctions(productData, context);
		return context;
	}

	public StandardEvaluationContext createEvaluationContext(ProductData productData, RepositoryEntity dataListItem) {
		StandardEvaluationContext dataContext = new StandardEvaluationContext(
				new FormulaFormulationContext(this, (ProductData) createSecurityProxy(productData), dataListItem));
		registerCustomFunctions(productData, dataContext);
		return dataContext;
	}


	public <T> StandardEvaluationContext createEvaluationContext(ProductData productData, T item) {
		StandardEvaluationContext dataContext = new StandardEvaluationContext(item);
		registerCustomFunctions(productData, dataContext);
		return dataContext;
	}

	
	public Double aggreate(ProductData entity, Collection<RepositoryEntity> range, String formula, Operator operator) {

		if (logger.isDebugEnabled()) {
			logger.debug("Running aggregate fonction [" + formula + "] on range (" + range.size() + ") for operator " + operator);
		}

		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(formula);
		Double sum = 0d;
		int count = 0;
		for (RepositoryEntity item : range) {
			StandardEvaluationContext context = new StandardEvaluationContext(new FormulaFormulationContext(this, entity, item));
			registerCustomFunctions(entity, context);

			Double value = exp.getValue(context, Double.class);
			if (value != null) {
				sum += value;
				count++;
			} else {
				logger.debug("Value is null for [" + formula + "] on " + item.toString());
			}
		}
		if (Operator.AVG.equals(operator) && count!=0) {
			sum /= count;
		}

		return sum;
	}

	public void applyToList(ProductData entity, Collection<RepositoryEntity> range, String formula) {

		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(formula);

		for (RepositoryEntity item : range) {
			StandardEvaluationContext context = new StandardEvaluationContext(new FormulaFormulationContext(this, entity, item));
			registerCustomFunctions(entity, context);

			exp.getValue(context, Double.class);

		}

	}

	public RepositoryEntity findOne(NodeRef nodeRef) {
		return createSecurityProxy(alfrescoRepository.findOne(nodeRef));
	}

}
