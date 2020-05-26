package fr.becpg.repo.formulation.spel;

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

import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;

@Service("formulaService")
public class SpelFormulaService {

	private static final Log logger = LogFactory.getLog(SpelFormulaService.class);

	@Autowired
	private SecurityMethodBeforeAdvice securityMethodBeforeAdvice;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private CustomSpelFunctions[] customSpelFunctions;

	private <T extends RepositoryEntity> void registerCustomFunctions(T entity, StandardEvaluationContext context) {

		context.setBeanResolver((context1, beanName) -> {

			for (CustomSpelFunctions customSpelFunction : customSpelFunctions) {
				if (customSpelFunction.match(beanName)) {
					return customSpelFunction.create(entity);
				}
			}
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends RepositoryEntity> T createSecurityProxy(T entity) {
		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(entity);
		factory.addAdvice(securityMethodBeforeAdvice);
		return ((T) factory.getProxy());
	}

	public <T extends RepositoryEntity> StandardEvaluationContext createEntitySpelContext(T entity) {
		StandardEvaluationContext context = new StandardEvaluationContext(createSecurityProxy(entity));
		registerCustomFunctions(entity, context);
		return context;
	}
	
	public <T extends RepositoryEntity> StandardEvaluationContext createDataListItemSpelContext(T entity, RepositoryEntity dataListItem) {
		DataListItemSpelContext formulaContext = new DataListItemSpelContext(this); 
		formulaContext.setEntity(createSecurityProxy(entity));
		formulaContext.setDataListItem(dataListItem);
		StandardEvaluationContext context = new StandardEvaluationContext(formulaContext);
		
		registerCustomFunctions(entity, context);
		return context;
	}

	public <T extends RepositoryEntity> StandardEvaluationContext createCustomSpelContext(T entity, SpelFormulaContext<T> formulaContext) {
		StandardEvaluationContext context = new StandardEvaluationContext(formulaContext);
		formulaContext.setEntity(createSecurityProxy(entity));
		registerCustomFunctions(entity, context);
		return context;
	}



	public <T> StandardEvaluationContext createItemSpelContext(RepositoryEntity entity, T item) {
		StandardEvaluationContext dataContext = new StandardEvaluationContext(item);
		registerCustomFunctions(entity, dataContext);
		return dataContext;
	}

	public Double aggreate(RepositoryEntity entity, Collection<RepositoryEntity> range, String formula, SpelFormulaContext.Operator operator) {

		if (logger.isDebugEnabled()) {
			logger.debug("Running aggregate fonction [" + formula + "] on range (" + range.size() + ") for operator " + operator);
		}

		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(formula);
		Double sum = 0d;
		int count = 0;
		for (RepositoryEntity item : range) {
			StandardEvaluationContext context = createDataListItemSpelContext(entity, item);
			Double value = exp.getValue(context, Double.class);
			if (value != null) {
				sum += value;
				count++;
			} else {
				logger.debug("Value is null for [" + formula + "] on " + item.toString());
			}
		}
		if (SpelFormulaContext.Operator.AVG.equals(operator) && (count != 0)) {
			sum /= count;
		}

		return sum;
	}

	public void applyToList(RepositoryEntity entity, Collection<RepositoryEntity> range, String formula) {

		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(formula);

		for (RepositoryEntity item : range) {
			StandardEvaluationContext context = createDataListItemSpelContext(entity, item);
			exp.getValue(context, Double.class);

		}

	}

	public RepositoryEntity findOne(NodeRef nodeRef) {
		return createSecurityProxy(alfrescoRepository.findOne(nodeRef));
	}

}
