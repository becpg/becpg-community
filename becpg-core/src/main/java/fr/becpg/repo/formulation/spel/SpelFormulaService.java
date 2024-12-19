package fr.becpg.repo.formulation.spel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;
import fr.becpg.repo.system.SystemConfigurationService;

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
	private SystemConfigurationService systemConfigurationService;
	
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
	 * @return a T object.
	 * @param <T> a T class
	 */
	@SuppressWarnings("unchecked")
	public <T extends RepositoryEntity> T createSecurityProxy(T entity) {
		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(entity);
		factory.addAdvice(securityMethodBeforeAdvice);
		return ((T) factory.getProxy());
	}

	
	/**
	 * <p>getSpelParser.</p>
	 *
	 * @return a {@link org.springframework.expression.ExpressionParser} object
	 */
	public ExpressionParser getSpelParser() {
		
		//  https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions-spel-compilation
		if(parser == null) {
			SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.MIXED,
				    this.getClass().getClassLoader());
			
			 parser = new BeCPGSpelExpressionParser(config);
		}
		return parser;
	}

	/**
	 * <p>createSpelContext.</p>
	 *
	 * @param rootObject a {@link java.lang.Object} object
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object
	 */
	public StandardEvaluationContext createSpelContext(@Nullable Object rootObject) {
		StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
		String authorizedTypes = systemConfigurationService.confValue("beCPG.spel.security.authorizedTypes");
		context.setTypeLocator(new BecpgSpelSecurityTypeLocator(authorizedTypes));
		return context;
	}
	
	/**
	 * <p>createEntitySpelContext.</p>
	 *
	 * @param entity a T object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 * @param <T> a T class
	 */
	public <T extends RepositoryEntity> StandardEvaluationContext createEntitySpelContext(T entity) {
		StandardEvaluationContext context = createSpelContext(createSecurityProxy(entity));
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
		StandardEvaluationContext context = createSpelContext(formulaContext);
		registerCustomFunctions(entity, context);
		return context;
	}

	/**
	 * <p>createDataListItemSpelContext.</p>
	 *
	 * @param entity a T object.
	 * @param dataListItem a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 * @param <T> a T class
	 */
	public <T extends RepositoryEntity> StandardEvaluationContext createDataListItemSpelContext(T entity, RepositoryEntity dataListItem) {
		return createDataListItemSpelContext(entity, dataListItem, true);
	}

	/**
	 * <p>createCustomSpelContext.</p>
	 *
	 * @param entity a T object.
	 * @param formulaContext a {@link fr.becpg.repo.formulation.spel.SpelFormulaContext} object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 * @param applySecurity a boolean
	 * @param <T> a T class
	 */
	public <T extends RepositoryEntity> StandardEvaluationContext createCustomSpelContext(T entity, SpelFormulaContext<T> formulaContext, boolean applySecurity) {
		StandardEvaluationContext context = createSpelContext(formulaContext);
		if(applySecurity) {
			formulaContext.setEntity(createSecurityProxy(entity));
		} else {
			formulaContext.setEntity(entity);
		}
		registerCustomFunctions(entity, context);
		return context;
	}
	
	
	/**
	 * <p>createCustomSpelContext.</p>
	 *
	 * @param entity a T object
	 * @param formulaContext a {@link fr.becpg.repo.formulation.spel.SpelFormulaContext} object
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object
	 * @param <T> a T class
	 */
	public <T extends RepositoryEntity> StandardEvaluationContext createCustomSpelContext(T entity, SpelFormulaContext<T> formulaContext) {
		return createCustomSpelContext(entity, formulaContext,true);
	}

	/**
	 * <p>createItemSpelContext.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param item a T object.
	 * @return a {@link org.springframework.expression.spel.support.StandardEvaluationContext} object.
	 * @param <T> a T class
	 */
	public <T> StandardEvaluationContext createItemSpelContext(RepositoryEntity entity, T item) {
		StandardEvaluationContext dataContext = createSpelContext(item);
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
	
	private class BecpgSpelSecurityTypeLocator extends StandardTypeLocator {
		
		private List<String> authorizedTypes = new ArrayList<>();
		
		private BecpgSpelSecurityTypeLocator(String authorizedTypes) {
			if (authorizedTypes != null && !authorizedTypes.isBlank()) {
				this.authorizedTypes = Arrays.asList(authorizedTypes.split(","));
			}
		}
		
		@Override
		public Class<?> findType(String typeName) throws EvaluationException {
			if (!isTypeAuthorized(typeName, authorizedTypes)) {
				logger.error(TYPE_NOT_AUTHORIZED + typeName);
				throw new EvaluationException(TYPE_NOT_AUTHORIZED + typeName);
			}
			return super.findType(typeName);
		}
		
		private boolean isTypeAuthorized(String typeName, List<String> authorizedTypes) {
			if (FORBIDDEN_TYPES.contains(typeName)) {
				return false;
			}
			return authorizedTypes.stream().anyMatch(clazz -> clazz.equals(typeName) || clazz.endsWith("*") && typeName.startsWith(clazz.replace("*", "")));
		}
	}
	
	private static final String TYPE_NOT_AUTHORIZED = "Type is not authorized: ";
	
	private static final List<String> FORBIDDEN_TYPES = List.of("java.lang.System", "java.lang.Runtime", "java.lang.ProcessBuilder",
			"java.lang.Class", "java.lang.ClassLoader", "java.lang.Thread", "java.lang.ThreadGroup", "java.lang.reflect.Method",
			"java.lang.reflect.Field", "java.lang.reflect.Constructor", "java.lang.reflect.Proxy", "javax.script.ScriptEngine",
			"javax.script.ScriptEngineManager", "java.util.concurrent.Executors", "java.util.concurrent.ExecutorService", "java.io.File",
			"java.io.FileInputStream", "java.io.FileOutputStream", "java.io.RandomAccessFile", "java.io.FileReader", "java.io.FileWriter",
			"java.net.URL", "java.net.Socket", "java.sql.Connection", "java.sql.Statement", "java.sql.ResultSet",
			"org.springframework.beans.factory.config.BeanFactory", "org.springframework.beans.factory.config.ConfigurableListableBeanFactory",
			"org.springframework.context.ApplicationContext", "org.springframework.context.support.AbstractApplicationContext", "sun.misc.Unsafe",
			"org.apache.commons.io.FileUtils");
}
