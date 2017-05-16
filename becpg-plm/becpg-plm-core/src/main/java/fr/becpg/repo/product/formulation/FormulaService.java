package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext.Operator;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;

public class FormulaService {
	
	private static Log logger = LogFactory.getLog(FormulaService.class);
	
	private SecurityMethodBeforeAdvice securityMethodBeforeAdvice;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	public void setSecurityMethodBeforeAdvice(SecurityMethodBeforeAdvice securityMethodBeforeAdvice) {
		this.securityMethodBeforeAdvice = securityMethodBeforeAdvice;
	}


	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	public void registerCustomFunctions(ProductData productData, StandardEvaluationContext context) {
		context.setBeanResolver(new BeanResolver() {

			final SpelHelperFonctions spelHelperFonctions = new SpelHelperFonctions(productData);

			@Override
			public Object resolve(EvaluationContext context, String beanName) throws AccessException {
				if (beanName.equals("beCPG")) {
					return spelHelperFonctions;
				}
				return null;
			}
		});
	}

	private ProductData createSecurityProxy(ProductData productData) {
		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(productData);
		factory.addAdvice(securityMethodBeforeAdvice);
		return (ProductData) factory.getProxy();

	}

	public class SpelHelperFonctions {
		
		ProductData productData;
		
		

		public SpelHelperFonctions(ProductData productData) {
			super();
			this.productData = productData;
		}

		public ProductData findOne(NodeRef nodeRef) {
			return createSecurityProxy(alfrescoRepository.findOne(nodeRef));
		}

		public Serializable propValue(NodeRef nodeRef, String qname) {
			return nodeService.getProperty(nodeRef, QName.createQName(qname, namespaceService));
		}

		public QName getQName(String qName){
			return QName.createQName(qName, namespaceService);
		}
		
		
		public Double sum(Collection<CompositionDataItem> range, String formula) {
			return aggreate(productData, range, formula, Operator.SUM);
		}
		
		public Double sum(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).sum();
		}
		
		
		public Double avg(Collection<CompositionDataItem> range, String formula) {
			return aggreate( productData, range, formula, Operator.AVG);
		}
		
		
		public <T> Collection<T> filter(Collection<T> range, String formula){

			ExpressionParser parser = new SpelExpressionParser();
			Expression exp = parser.parseExpression(formula);
			
			return range.stream().filter(p  -> {
				StandardEvaluationContext dataContext = new StandardEvaluationContext(p);
				registerCustomFunctions(productData, dataContext);
				return exp.getValue(dataContext, Boolean.class);
			}).collect(Collectors.toList());
			
			
		}
		
	}

	public StandardEvaluationContext createEvaluationContext(ProductData productData) {
		StandardEvaluationContext context = new StandardEvaluationContext(createSecurityProxy(productData));

		registerCustomFunctions(productData, context);

		return context;
	}
	
	

	public StandardEvaluationContext createEvaluationContext(ProductData productData, CompositionDataItem dataListItem) {
		StandardEvaluationContext dataContext = new StandardEvaluationContext(new FormulaFormulationContext(this, createSecurityProxy(productData), dataListItem));
		
		registerCustomFunctions(productData, dataContext);
		
		return dataContext;
	}

	
	
	public Double aggreate(ProductData entity , Collection<CompositionDataItem> range, String formula, Operator operator){

		if(logger.isDebugEnabled()){
			logger.debug("Running aggregate fonction ["+formula+"] on range ("+range.size()+") for operator "+operator );
		}
		
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(formula);
		Double sum = 0d;
        int count = 0;
		for (CompositionDataItem item : range) {
			StandardEvaluationContext context = new StandardEvaluationContext(new FormulaFormulationContext(this, entity, item));
			
			registerCustomFunctions(entity, context);
			
			Double value = exp.getValue(context, Double.class);
			if (value != null) {
				sum += value;
				count++;
			} else {
				logger.debug("Value is null for ["+formula+"] on "+item.toString());
			}
		}
		if(Operator.AVG.equals(operator)){
			sum /=count;
		}
		
		return sum;
	}
	

	public ProductData findOne(NodeRef nodeRef) {
		return createSecurityProxy(alfrescoRepository.findOne(nodeRef));
	}



}
