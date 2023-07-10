package fr.becpg.repo.formulation.spel;

import java.util.Collection;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;

/**
 * <p>SpelFormulaService class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("formulaService")
public class SpelFormulaService {

	private static final Log logger = LogFactory.getLog(SpelFormulaService.class);

	@Autowired
	private SecurityMethodBeforeAdvice securityMethodBeforeAdvice;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private CustomSpelFunctions[] customSpelFunctions;
	
	private ExpressionParser parser;

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

	/**
	 * <p>createSecurityProxy.</p>
	 *
	 * @param entity a T object.
	 * @param <T> a T object.
	 * @return a T object.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RepositoryEntity> T createSecurityProxy(T entity) {
		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(entity);
		factory.addAdvice(securityMethodBeforeAdvice);
		return ((T) factory.getProxy());
	}

	
	public ExpressionParser getSpelParser() {
		
		//  https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions-spel-compilation
		if(parser == null) {
			SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.MIXED,
				    this.getClass().getClassLoader());
			
			 parser = new SpelExpressionParser(config);
		}
		return parser;
	}

	
	/**
	 * <p>createEntitySpelContext.</p>
	 *
	 * @param entity a T object.
	 * @param <T> a T object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 */
	public <T extends RepositoryEntity> StandardEvaluationContext createEntitySpelContext(T entity) {
		StandardEvaluationContext context = new StandardEvaluationContext(createSecurityProxy(entity));
		registerCustomFunctions(entity, context);
		return context;
	}

	private <T extends RepositoryEntity> StandardEvaluationContext createDataListItemSpelContext(T entity, RepositoryEntity dataListItem,
			boolean applySecurity) {
		DataListItemSpelContext<T> formulaContext = new DataListItemSpelContext<>(this);
		if (applySecurity) {
			formulaContext.setEntity(createSecurityProxy(entity));
		} else {
			formulaContext.setEntity(entity);
		}
		formulaContext.setDataListItem(dataListItem);
		StandardEvaluationContext context = new StandardEvaluationContext(formulaContext);
		registerCustomFunctions(entity, context);
		return context;
	}

	/**
	 * <p>createDataListItemSpelContext.</p>
	 *
	 * @param entity a T object.
	 * @param dataListItem a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param <T> a T object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 */
	public <T extends RepositoryEntity> StandardEvaluationContext createDataListItemSpelContext(T entity, RepositoryEntity dataListItem) {
		return createDataListItemSpelContext(entity, dataListItem, true);
	}

	/**
	 * <p>createCustomSpelContext.</p>
	 *
	 * @param entity a T object.
	 * @param formulaContext a {@link fr.becpg.repo.formulation.spel.SpelFormulaContext} object.
	 * @param <T> a T object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 */
	public <T extends RepositoryEntity> StandardEvaluationContext createCustomSpelContext(T entity, SpelFormulaContext<T> formulaContext, boolean applySecurity) {
		StandardEvaluationContext context = new StandardEvaluationContext(formulaContext);
		if(applySecurity) {
			formulaContext.setEntity(createSecurityProxy(entity));
		} else {
			formulaContext.setEntity(entity);
		}
		registerCustomFunctions(entity, context);
		return context;
	}
	
	
	public <T extends RepositoryEntity> StandardEvaluationContext createCustomSpelContext(T entity, SpelFormulaContext<T> formulaContext) {
		return createCustomSpelContext(entity, formulaContext,true);
	}

	/**
	 * <p>createItemSpelContext.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param item a T object.
	 * @param <T> a T object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 */
	public <T> StandardEvaluationContext createItemSpelContext(RepositoryEntity entity, T item) {
		StandardEvaluationContext dataContext = new StandardEvaluationContext(item);
		registerCustomFunctions(entity, dataContext);
		return dataContext;
	}

	/**
	 * <p>aggreate.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param range a {@link java.util.Collection} object.
	 * @param formula a {@link java.lang.String} object.
	 * @param operator a {@link fr.becpg.repo.formulation.spel.SpelFormulaContext.Operator} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public Double aggreate(RepositoryEntity entity, Collection<RepositoryEntity> range, String formula, SpelFormulaContext.Operator operator) {

		if (logger.isDebugEnabled()) {
			logger.debug("Running aggregate fonction [" + formula + "] on range (" + range.size() + ") for operator " + operator);
		}

		Expression exp = getSpelParser().parseExpression(formula);
		Double ref = 0d;
		if(SpelFormulaContext.Operator.MIN.equals(operator)) {
			ref = Double.POSITIVE_INFINITY;
		} else if(SpelFormulaContext.Operator.MAX.equals(operator)) {
			ref = Double.NEGATIVE_INFINITY;
		}
		int count = 0;
		for (RepositoryEntity item : range) {
			StandardEvaluationContext context = createDataListItemSpelContext(entity, item, false);
			Double value = exp.getValue(context, Double.class);
			if (value != null) {
				if(SpelFormulaContext.Operator.MIN.equals(operator)) {
					ref = Math.min(ref, value);
				} else if(SpelFormulaContext.Operator.MAX.equals(operator)) {
					ref = Math.max(ref, value);
				} else {
					ref += value;
				}
				count++;
			} else {
				logger.debug("Value is null for [" + formula + "] on " + item.toString());
			}
		}
		if (SpelFormulaContext.Operator.AVG.equals(operator) && (count != 0)) {
			ref /= count;
		}
		
		if (ref.isInfinite()) {
			ref = 0d;
		}

		return ref;
	}

	/**
	 * <p>applyToList.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param range a {@link java.util.Collection} object.
	 * @param formula a {@link java.lang.String} object.
	 */
	public void applyToList(RepositoryEntity entity, Collection<RepositoryEntity> range, String formula) {
		Expression exp = getSpelParser().parseExpression(formula);

		for (RepositoryEntity item : range) {
			StandardEvaluationContext context = createDataListItemSpelContext(entity, item, false);
			exp.getValue(context);
		}

	}

	/**
	 * <p>findOne.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 */
	public RepositoryEntity findOne(NodeRef nodeRef) {
		return createSecurityProxy(alfrescoRepository.findOne(nodeRef));
	}

}
