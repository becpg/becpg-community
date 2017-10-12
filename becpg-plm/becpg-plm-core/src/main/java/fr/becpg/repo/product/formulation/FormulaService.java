package fr.becpg.repo.product.formulation;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext.Operator;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;

public class FormulaService {

	private static Log logger = LogFactory.getLog(FormulaService.class);

	private SecurityMethodBeforeAdvice securityMethodBeforeAdvice;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private RepositoryEntityDefReader<ProductData> repositoryEntityDefReader;

	private DictionaryService dictionaryService;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private AssociationService associationService;

	private EntityListDAO entityListDAO;

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

	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<ProductData> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
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

		public QName getQName(String qName) {
			return QName.createQName(qName, namespaceService);
		}

		public Serializable setValue(RepositoryEntity item, String qname, Serializable value) {
			item.getExtraProperties().put(QName.createQName(qname, namespaceService), value);
			return value;
		}

		public Double sum(Collection<CompositionDataItem> range, String formula) {
			return aggreate(productData, range, formula, Operator.SUM);
		}

		public Double sum(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).sum();
		}

		public Double avg(Collection<CompositionDataItem> range, String formula) {
			return aggreate(productData, range, formula, Operator.AVG);
		}

		public void copy(NodeRef fromNodeRef, String[] propQNames, String[] listQNames) {
			try {
				Set<QName> treatedProp = new HashSet<>();
				Set<QName> treatedList = new HashSet<>();

				ProductData from = alfrescoRepository.findOne(fromNodeRef);

				if (from != null) {
					BeanWrapper beanWrapper = new BeanWrapperImpl(productData);

					for (final PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {

						Method readMethod = pd.getReadMethod();

						if (readMethod != null) {
							if (readMethod.isAnnotationPresent(AlfProp.class)) {
								QName qname = repositoryEntityDefReader.readQName(readMethod);
								for (String propQName2 : propQNames) {
									QName propQName = QName.createQName(propQName2, namespaceService);
									if (qname.equals(propQName)) {
										logger.debug("Setting property : " + propQName + " from repository entity");

										PropertyUtils.setProperty(from, pd.getName(), PropertyUtils.getProperty(from, pd.getName()));

										treatedProp.add(propQName);
									}

								}

								for (int i = 0; i < listQNames.length; i++) {
									QName listQName = QName.createQName(propQNames[i], namespaceService);
									if (qname.equals(listQName)) {
										logger.debug("Setting list : " + listQName + " from repository entity");
										PropertyUtils.setProperty(from, pd.getName(), PropertyUtils.getProperty(from, pd.getName()));
										treatedList.add(listQName);
									}

								}

							}
						}
					}

					Map<QName, Serializable> extraPropToCopy = nodeService.getProperties(from.getNodeRef());

					for (String propQName2 : propQNames) {
						QName propQName = QName.createQName(propQName2, namespaceService);
						if (!treatedProp.contains(propQName)) {
							PropertyDefinition propertyDef = dictionaryService.getProperty(propQName);
							if (propertyDef != null) {
								if (extraPropToCopy.containsKey(propQName)) {
									logger.debug("Setting property : " + propQName + " from nodeRef");
									nodeService.setProperty(productData.getNodeRef(), propQName, extraPropToCopy.get(propQName));
								} else {
									logger.debug("Removing property : " + propQName);
									nodeService.removeProperty(productData.getNodeRef(), propQName);
								}

							} else {
								logger.debug("Setting association : " + propQName + " from nodeRef");
								associationService.update(productData.getNodeRef(), propQName,
										associationService.getTargetAssocs(from.getNodeRef(), propQName));
							}
							treatedProp.add(propQName);

						}

					}

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(from.getNodeRef());

					for (String listQName2 : listQNames) {
						QName listQName = QName.createQName(listQName2, namespaceService);

						if (!treatedList.contains(listQName)) {
							logger.debug("Copy list : " + listQName + " from nodeRef");
							entityListDAO.copyDataList(entityListDAO.getList(listContainerNodeRef, listQName), productData.getNodeRef(), true);

							treatedList.add(listQName);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e, e);
			}
		}

		public <T> Collection<T> filter(Collection<T> range, String formula) {

			ExpressionParser parser = new SpelExpressionParser();
			Expression exp = parser.parseExpression(formula);

			return range.stream().filter(p -> {
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
		StandardEvaluationContext dataContext = new StandardEvaluationContext(
				new FormulaFormulationContext(this, createSecurityProxy(productData), dataListItem));

		registerCustomFunctions(productData, dataContext);

		return dataContext;
	}

	public Double aggreate(ProductData entity, Collection<CompositionDataItem> range, String formula, Operator operator) {

		if (logger.isDebugEnabled()) {
			logger.debug("Running aggregate fonction [" + formula + "] on range (" + range.size() + ") for operator " + operator);
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
				logger.debug("Value is null for [" + formula + "] on " + item.toString());
			}
		}
		if (Operator.AVG.equals(operator)) {
			sum /= count;
		}

		return sum;
	}

	public ProductData findOne(NodeRef nodeRef) {
		return createSecurityProxy(alfrescoRepository.findOne(nodeRef));
	}

}
