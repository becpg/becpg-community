package fr.becpg.repo.formulation.spel;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.stereotype.Service;

import fr.becpg.config.format.FormatMode;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.annotation.DataListView;
import fr.becpg.repo.repository.impl.LazyLoadingDataList;
import fr.becpg.repo.repository.model.BaseObject;
import fr.becpg.repo.repository.model.CopiableDataItem;

/**
 *
 * Register custom beCPG SPEL helper accessible with {@code @beCPG}.
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class BeCPGSpelFunctions implements CustomSpelFunctions {

	private static final Log logger = LogFactory.getLog(BeCPGSpelFunctions.class);

	@Autowired
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private ScriptService scriptService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private SpelFormulaService formulaService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	/** {@inheritDoc} */
	@Override
	public boolean match(String beanName) {
		return beanName.equals("beCPG");
	}

	/** {@inheritDoc} */
	@Override
	public Object create(RepositoryEntity repositoryEntity) {
		return new BeCPGSpelFunctionsWrapper(repositoryEntity);
	}

	public class BeCPGSpelFunctionsWrapper {

		private static final String SPEL_COPY_FORCE_INDICATOR = "|true";

		RepositoryEntity entity;

		public BeCPGSpelFunctionsWrapper(RepositoryEntity entity) {
			super();
			this.entity = entity;
		}

		/**
		 * Helper {@code @beCPG.findOne($nodeRef)}
		 *
		 * <code>
		 * 	Example : {@code @beCPG.findOne(nodeRef)}.qty
		 *</code>
		 *
		 * @param nodeRef
		 * @return repository entity for nodeRef
		 */
		public RepositoryEntity findOne(NodeRef nodeRef) {
			if (nodeRef != null) {
				return formulaService.createSecurityProxy(alfrescoRepository.findOne(nodeRef));
			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.propValue($nodeRef, $qname)} <code>
		 * Example : {@code @beCPG.propValue(nodeRef,'bcpg:productQty')}
		 *</code>
		 *
		 * @param nodeRef
		 * @param qname
		 * @return node property value
		 */
		public Serializable propValue(NodeRef nodeRef, String qname) {
			if (nodeRef != null) {
				return nodeService.getProperty(nodeRef, getQName(qname));
			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.propValue($entity, $qname)}
		 *
		 * @param item
		 * @param qname
		 * @return entity property value
		 */
		public Serializable propValue(RepositoryEntity item, String qname) {
			if (item != null) {
				assertIsNotMappedQname(item, getQName(qname), false);
				Serializable value = item.getExtraProperties().get(getQName(qname));
				if (value == null) {
					value = nodeService.getProperty(item.getNodeRef(), getQName(qname));
					item.getExtraProperties().put(getQName(qname), value);
				}
				return value;
			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.propMLValue($entity, $qname, $locale)}
		 *
		 * @param item
		 * @param qname
		 * @param locale
		 * @return
		 */
		public Serializable propMLValue(RepositoryEntity item, String qname, String locale) {
			Serializable value;

			boolean isMLAware = MLPropertyInterceptor.setMLAware(true);
			try {
				value = propValue(item, qname);
			} finally {
				MLPropertyInterceptor.setMLAware(isMLAware);
			}

			if (value instanceof MLText mlText) {
				if (locale == null) {
					return value;
				}

				return MLTextHelper.getClosestValue(mlText, MLTextHelper.parseLocale(locale));
			}

			return null;
		}

		/**
		 * Helper {@code @beCPG.propMLValue($nodeRef, $qname, $locale)}
		 *
		 * @param nodeRef
		 * @param qname
		 * @param locale
		 * @return
		 */
		public Serializable propMLValue(NodeRef nodeRef, String qname, String locale) {
			MLText value;

			boolean isMLAware = MLPropertyInterceptor.setMLAware(true);
			try {
				value = (MLText) nodeService.getProperty(nodeRef, getQName(qname));
				if (value != null) {
					if (locale == null) {
						return value;
					}

					return MLTextHelper.getClosestValue(value, MLTextHelper.parseLocale(locale));
				}

			} finally {
				MLPropertyInterceptor.setMLAware(isMLAware);
			}

			return null;
		}

		/**
		 * Helper {@code @beCPG.propValue( $qname)}
		 *
		 * @param qname
		 * @return property value in current entity
		 */
		public Serializable propValue(String qname) {
			return propValue(entity, qname);

		}

		/**
		 * Helper {@code @beCPG.propMLValue($mltext, $locale)}
		 *
		 * get mlText locale value
		 *
		 * @param mlText
		 * @param locale
		 * @return value being set
		 */
		public String propMLValue(MLText mlText, String locale) {
			return MLTextHelper.getClosestValue(mlText, MLTextHelper.parseLocale(locale));
		}

		/**
		 * Helper {@code @beCPG.setValue($entity, $qname, $value)}
		 *
		 * Set property value on entity
		 *
		 * @param item
		 * @param qname
		 * @param value
		 * @return value being set
		 */
		public Serializable setValue(RepositoryEntity item, String qname, Serializable value) {
			if (item != null) {
				assertIsNotMappedQname(item, getQName(qname), true);
				item.getExtraProperties().put(getQName(qname), value);
				return value;
			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.setValue( $qname, $value)}
		 *
		 * Set property value on current entity
		 *
		 * @param qname
		 * @param value
		 * @return value being set
		 */
		public Serializable setValue(String qname, Serializable value) {
			return setValue(entity, qname, value);
		}

		/**
		 * Helper {@code @beCPG.setAssocs($nodeRef, $qname, $assocNodeRefs)}
		 *
		 * @param nodeRef
		 * @param qname
		 * @param assocNodeRefs
		 */
		public void setAssocs(NodeRef nodeRef, String qname, List<NodeRef> assocNodeRefs) {
			associationService.update(nodeRef, getQName(qname), assocNodeRefs);
		}

		public void setAssocs(RepositoryEntity entity, String qname, List<NodeRef> assocNodeRefs) {
			assertIsNotMappedQname(entity, getQName(qname), true);
			setAssocs(entity.getNodeRef(), qname, assocNodeRefs);
		}

		public void setAssocs(String qname, List<NodeRef> assocNodeRefs) {
			assertIsNotMappedQname(entity, getQName(qname), true);
			setAssocs(entity.getNodeRef(), qname, assocNodeRefs);
		}

		/**
		 * Helper {@code {@code @beCPG.setAssoc($nodeRef, $qname, $assocNodeRef)}}
		 *
		 * @param nodeRef
		 * @param qname
		 * @param assocNodeRef
		 */
		public void setAssoc(NodeRef nodeRef, String qname, NodeRef assocNodeRef) {

			associationService.update(nodeRef, getQName(qname), assocNodeRef);
		}

		public void setAssoc(String qname, NodeRef assocNodeRef) {
			assertIsNotMappedQname(entity, getQName(qname), true);
			setAssoc(entity.getNodeRef(), qname, assocNodeRef);
		}

		public void setAssoc(RepositoryEntity entity, String qname, NodeRef assocNodeRef) {
			assertIsNotMappedQname(entity, getQName(qname), true);
			setAssoc(entity.getNodeRef(), qname, assocNodeRef);
		}

		/**
		 * Helper {@code @beCPG.assocValue($nodeRef, $qname)}
		 *
		 * @param nodeRef
		 * @param qname
		 * @return association nodeRef
		 */
		public NodeRef assocValue(NodeRef nodeRef, String qname) {
			if (nodeRef != null) {

				return associationService.getTargetAssoc(nodeRef, getQName(qname));
			}
			return null;
		}

		public NodeRef assocValue(String qname) {
			assertIsNotMappedQname(entity, getQName(qname), false);
			return assocValue(entity.getNodeRef(), qname);
		}

		public NodeRef assocValue(RepositoryEntity entity, String qname) {
			assertIsNotMappedQname(entity, getQName(qname), false);
			return assocValue(entity.getNodeRef(), qname);
		}

		/**
		 * Helper {@code @beCPG.assocValues($entity, $qname)}
		 *
		 * @param nodeRef
		 * @param qname
		 * @return collection of association nodeRefs
		 */
		public List<NodeRef> assocValues(NodeRef nodeRef, String qname) {
			if (nodeRef != null) {
				return associationService.getTargetAssocs(nodeRef, getQName(qname));
			}
			return null;
		}

		public List<NodeRef> assocValues(RepositoryEntity entity, String qname) {
			assertIsNotMappedQname(entity, getQName(qname), false);
			return assocValues(entity.getNodeRef(), qname);
		}

		public List<NodeRef> assocValues(String qname) {
			assertIsNotMappedQname(entity, getQName(qname), false);
			return assocValues(entity.getNodeRef(), qname);
		}

		/**
		 * Helper {@code @beCPG.sourcesAssocValues($nodeRef, $qname)}
		 *
		 * @param nodeRef
		 * @param qname
		 * @return association nodeRef
		 */
		public List<NodeRef> sourcesAssocValues(NodeRef nodeRef, String qname) {
			if (nodeRef != null) {
				return associationService.getSourcesAssocs(nodeRef, getQName(qname));
			}
			return null;
		}

		public List<NodeRef> sourcesAssocValues(RepositoryEntity entity, String qname) {
			return sourcesAssocValues(entity.getNodeRef(), qname);
		}

		public List<NodeRef> sourcesAssocValues(String qname) {
			return sourcesAssocValues(entity.getNodeRef(), qname);
		}

		/**
		 * Helper {@code @beCPG.assocPropValues($nodeRef, $assocQname, $propQName)}
		 *
		 * @param nodeRef
		 * @param assocQname
		 * @param propQName
		 * @return collection of association property values
		 */
		public List<Serializable> assocPropValues(NodeRef nodeRef, String assocQname, String propQName) {
			if (nodeRef != null) {
				return associationService.getTargetAssocs(nodeRef, getQName(assocQname)).stream().map(o -> propValue(o, propQName))
						.filter(Objects::nonNull).toList();
			}
			return null;
		}

		public List<Serializable> assocPropValues(RepositoryEntity entity, String assocQname, String propQName) {
			return assocPropValues(entity.getNodeRef(), assocQname, propQName);
		}

		public List<Serializable> assocPropValues(String assocQname, String propQName) {
			return assocPropValues(entity.getNodeRef(), assocQname, propQName);
		}

		/**
		 * Helper {@code @beCPG.assocAssocValues($nodeRef, $assocQname, $assocAssocQName)}
		 *
		 * @param nodeRef
		 * @param assocAssocQName
		 * @return collection of association association values
		 */
		public List<NodeRef> assocAssocValues(NodeRef nodeRef, String assocQname, String assocAssocQName) {
			if (nodeRef != null) {
				return associationService.getTargetAssocs(nodeRef, getQName(assocQname)).stream()
						.flatMap(o -> assocValues(o, assocAssocQName).stream()) // Flatten the list of NodeRef
						.filter(Objects::nonNull).toList();
			}

			return null;
		}

		public List<NodeRef> assocAssocValues(RepositoryEntity entity, String assocQname, String assocAssocQName) {
			return assocAssocValues(entity.getNodeRef(), assocQname, assocAssocQName);
		}

		public List<NodeRef> assocAssocValues(String assocQname, String assocAssocQName) {
			return assocAssocValues(entity.getNodeRef(), assocQname, assocAssocQName);
		}

		/**
		 * Helper {@code @beCPG.assocPropValue($nodeRef, $assocQname, $propQName)}
		 *
		 * Example :  {@code var val = @beCPG.assocPropValue(nodeRef, "bcpg:geoOrigin",
		 * "bcpg:isoCode"); #val!=null ? @beCPG.setValue($nodeRef,
		 * "cm:title",  @beCPG.assocPropValue("bcpg:geoOrigin", "bcpg:isoCode"))
		 * : ""; 
		 *
		 * or
		 *
		 * @beCPG.setValue("cm:title", @beCPG.assocPropValue("bcpg:geoOrigin",
		 * "bcpg:isoCode"))
		 *}
		 *
		 * @param nodeRef
		 * @param assocQname
		 * @param propQName
		 * @return value of association property
		 */
		public Serializable assocPropValue(NodeRef nodeRef, String assocQname, String propQName) {
			NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
			if (assocNodeRef != null) {
				return propValue(assocNodeRef, propQName);
			}
			return null;
		}

		public Serializable assocPropValue(String assocQname, String propQName) {
			return assocPropValue(entity.getNodeRef(), assocQname, propQName);
		}

		/**
		 * Helper {@code @beCPG.findDuplicates($collection)}
		 *
		 * @return Set of duplicates
		 */
		public <T> Set<T> findDuplicates(Collection<? extends T> collection) {
			Set<T> uniques = new HashSet<>();
			return collection.stream().filter(e -> !uniques.add(e)).collect(Collectors.toSet());
		}

		/**
		 * Helper {@code @beCPG.getQName($qname)}
		 *
		 * @param qName
		 * @return QName from string
		 */
		public QName getQName(String qName) {
			return QName.createQName(qName, namespaceService);
		}

		/**
		 * Helper {@code @beCPG.updateMLText($mltext, $locale, $value)}
		 *
		 * Update mlText locale value
		 *
		 * @param mlText
		 * @param locale
		 * @param value
		 * @return value being set
		 */
		public MLText updateMLText(MLText mlText, String locale, String value) {
			if (mlText == null) {
				mlText = new MLText();
			}

			if ((locale != null) && !locale.isBlank()) {
				Locale loc = MLTextHelper.parseLocale(locale);

				if ((value != null) && !value.isEmpty()) {
					if (MLTextHelper.isSupportedLocale(loc)) {
						mlText.addValue(loc, value);
					} else {
						logger.error("Unsupported locale in updateMLText " + loc);
					}
				} else {
					mlText.removeValue(loc);
				}
			} else {
				logger.error("Null or empty locale in updateMLText ");
			}

			return mlText;
		}

		/**
		 * {@code @beCPG.runScript($nodeRef)}
		 *
		 * @param scriptNode
		 */
		public void runScript(String scriptNode) {
			runScript(new NodeRef(scriptNode));
		}

		public void runScript(NodeRef scriptNode) {
			// Checking script path for security

			if ((scriptNode != null) && nodeService.exists(scriptNode)
					&& nodeService.getPath(scriptNode).toPrefixString(namespaceService).startsWith(RepoConsts.SCRIPTS_FULL_PATH)) {
				String userName = AuthenticationUtil.getFullyAuthenticatedUser();

				Map<String, Object> model = new HashMap<>();
				model.put("currentUser", userName);
				model.put("entity", entity);

				scriptService.executeScript(scriptNode, ContentModel.PROP_CONTENT, model);
			}
		}

		/**
		 * <pre>
		 * {@code @beCPG.sum($range, $formula)}
		 *
		 *                    Example :{@code
		 * @beCPG.sum(compoListView.compoList.?[parent ==
		 *                                             null],"entity.costList[0].value
		 *                                             + dataListItem.qty")
		 * @beCPG.sum(compoListView.compoList.?[parent ==
		 *                                             null],"@beCPG.propValue(dataListItem.nodeRef,'bcpg:compoListQty')")}
		 * </pre>                                            
		 *
		 * @param range
		 * @param formula
		 * @return sum of formula results apply on range
		 */
		public Double sum(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.SUM);
		}

		/**
		 * {@code @beCPG.sum($range)}
		 *
		 * @param range
		 * @return sum range of double
		 */
		public Double sum(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).sum();
		}

		/**
		 * {@code @beCPG.avg($range, $formula)}
		 *
		 * @param range
		 * @param formula
		 * @return average of formula results apply on range
		 */
		public Double avg(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.AVG);
		}

		/**
		 * {@code @beCPG.avg($range)}
		 *
		 *                    @param range
		 * @return average range of double
		 */
		public Double avg(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		}

		/**
		 * {@code @beCPG.max($range, $formula)}
		 *
		 * @param range
		 * @param formula
		 * @return get max of formula results apply on range
		 */
		public Double max(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.MAX);
		}

		/**
		 * {@code @beCPG.max($range)}
		 *
		 * @param range
		 * @return get max of range of double
		 */
		public Double max(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
		}

		/**
		 * {@code @beCPG.join($pattern, $range)}
		 *
		 * @param pattern
		 * @param range
		 * @return get join from pattern and range
		 */
		public String join(String pattern, List<String> range) {
			return String.join(pattern, range);
		}

		/**
		 * {@code @beCPG.join($pattern, $range)}
		 * {@code @beCPG.join($pattern, $range1, $range2)}
		 *
		 * @param pattern
		 * @param range1
		 * @param range2
		 * @return get join from pattern and ranges
		 */
		public String join(String pattern, List<String> range1, List<String> range2) {

			StringBuilder ret = new StringBuilder();
			if ((range1 != null) && (range2 != null) && (range1.size() == range2.size())) {
				for (int i = 0; i < range1.size(); i++) {
					ret.append(String.format(pattern, range1.get(i), range2.get(i)));
				}
			}
			return ret.toString();
		}

		/**
		 * {@code @beCPG.min($range, $formula)}
		 *
		 * @param range
		 * @param formula
		 * @return get min of formula results apply on range
		 */
		public Double min(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.MIN);
		}

		/**
		 * {@code @beCPG.min($range)}
		 *
		 * @param range
		 * @return get min of range of double
		 */
		public Double min(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
		}

		/**
		 * {@code @beCPG.extractCustomList($nodeRef, $listType)} @param listType
		 * @return list
		 */
		public Collection<RepositoryEntity> extractCustomList(NodeRef nodeRef, String listType) {
			return alfrescoRepository.loadDataList(nodeRef, getQName(listType).getLocalName(), getQName(listType));
		}

		/**
		 * {@code @beCPG.extractCustomList($listType)} @param listType
		 * @return list
		 */
		public Collection<RepositoryEntity> extractCustomList(String listType) {
			return extractCustomList(entity.getNodeRef(), listType);
		}

		/**
		 * {@code @beCPG.saveCustomList($range)}
		 *
		 * @param range
		 */
		public void saveCustomList(Collection<RepositoryEntity> range) {
			alfrescoRepository.save(range);
		}

		/**
		 * {@code @beCPG.applyFormulaToList($range, $formula)}
		 *
		 * @param range
		 * @param formula
		 */
		public void applyFormulaToList(Collection<RepositoryEntity> range, String formula) {
			formulaService.applyToList(entity, range, formula);
		}

		/**
		 * Helper {@code @beCPG.filter($range, formula)}
		 *
		 * @param range
		 * @param formula
		 * @return filter collection with spel formula
		 */
		public <T> Collection<T> filter(Collection<T> range, String formula) {
			if (range != null) {
				ExpressionParser parser = formulaService.getSpelParser();
				Expression exp = parser.parseExpression(formula);

				return range.stream().filter(p -> {
					return exp.getValue(formulaService.createItemSpelContext(entity, p), Boolean.class);
				}).toList();
			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.replaceByFormula($rangeR, formula)}
		 *
		 * @param range
		 * @param formula
		 * @return for each item return a new item based on formula
		 */
		@SuppressWarnings("unchecked")
		public <T> Collection<T> replaceByFormula(Collection<T> range, String formula) {
			if (range != null) {
				ExpressionParser parser = formulaService.getSpelParser();
				Expression exp = parser.parseExpression(formula);

				return (Collection<T>) range.stream().map(p -> {
					return exp.getValue(formulaService.createItemSpelContext(entity, p));
				}).toList();
			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.groupingByFormula($range, groupingFormula)}
		 *
		 * @param range
		 * @param groupingFormula
		 * @return group the list by formula
		 */
		public <T> Map<Object, List<T>> groupingByFormula(Collection<T> range, String groupingFormula) {
			if (range != null) {
				ExpressionParser parser = formulaService.getSpelParser();
				Expression exp = parser.parseExpression(groupingFormula);

				return range.stream().collect(Collectors.groupingBy(p -> {
					return exp.getValue(formulaService.createItemSpelContext(entity, p));
				}));

			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.filterByAssoc($range, $assocQname, $values)}
		 *
		 * @param range
		 * @param assocName
		 * @param values
		 * @return filter nodeRef collection by assoc values
		 */
		public List<NodeRef> filterByAssoc(Collection<NodeRef> range, String assocName, Collection<NodeRef> values) {
			if (range != null) {
				return range.stream().filter(nodeRef -> {
					List<NodeRef> assocs = associationService.getTargetAssocs(nodeRef, getQName(assocName));
					return assocs.containsAll(values);
				}).toList();
			}
			return null;
		}

		/**
		 * Helper {@code @beCPG.getOrDefault($range, $index, $defaultValue)}
		 *
		 * @param range
		 * @param index
		 * @param defaultValue
		 * @return defaultValue id list index doesn't exists
		 */
		public <T> T getOrDefault(List<T> range, int index, T defaultValue) {
			if ((range != null) && !range.isEmpty() && (index >= 0) && (index < range.size())) {
				return range.get(index);
			}
			return defaultValue;
		}

		/**
		 * Helper  {@code @beCPG.children($parent, $compositeList)}
		 *
		 * @param parent
		 * @param compositeList
		 * @return children of parent item
		 */
		public <T> Collection<CompositeDataItem<T>> children(CompositeDataItem<T> parent, Collection<CompositeDataItem<T>> compositeList) {
			List<CompositeDataItem<T>> ret = new ArrayList<>();
			for (CompositeDataItem<T> item : compositeList) {
				if (item.getParent() != null) {
					if (parent.equals(item.getParent())) {
						ret.add(item);
					}
				}
			}
			return ret;
		}

		/**
		 * Helper {@code @beCPG.formatNumber($number)}
		 *
		 * @param number
		 * @return standard becpg number format
		 */
		public String formatNumber(Number number) {
			return attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDecimal(number);
		}

		/**
		 * Helper {@code @beCPG.formatNumber($number, $format )}
		 *
		 *    Example:{@code @beCPG.formatNumber(10.00005d,
		 *                              "0.##")}
		 *
		 * @param number
		 * @param format
		 * @return formated number to provided format
		 */
		public String formatNumber(Number number, String format) {
			return new java.text.DecimalFormat(format).format(number);
		}

		/**
		 * Helper  {@code @beCPG.formatDate($date )}
		 *
		 *  Example: {@code @beCPG.formatDate(new
		 *                          java.util.Date() )}
		 *
		 * @param date
		 * @return standard becpg date format
		 */
		public String formatDate(Date date) {
			return attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDate(date);
		}

		/**
		 * {@code @beCPG.formatDate($date, $format )}
		 *
		 *  Example: {@code@beCPG.formatDate(new
		 *                          java.util.Date(),"dd/mm/YYYY" )}
		 *
		 * @param date
		 * @return standard becpg number format
		 */
		public String formatDate(Date date, String format) {
			return new java.text.SimpleDateFormat(format).format(date);
		}

		/**
		 * {@code @beCPG.updateDate($date, $field, $amount )}
		 *
		 *  Example:  {@code @beCPG.updateDate(new
		 *                          java.util.Date(), java.util.Calendar.DAY_OF_MONTH, -5 )}
		 *  cf Calendar JavaDoc
		 *
		 * @param date
		 * @return date
		 */
		public Date updateDate(Date date, int field, int amount) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(field, amount);
			return cal.getTime();
		}

		/**
		 *
		 * Helper  {@code @beCPG.copy($fromNodeRef, $propQNames, $listQNames)}
		 *
		 *  Copy properties from an entity to current
		 *  entity
		 *
		 *  Example: {@code @beCPG.copy(compoListView.compoList[0].product,{"bcpg:suppliers","bcpg:legalName"},{"bcpg:costList"})};
		 *  Example: {@code @beCPG.copy(compoListView.compoList[0].product,{"bcpg:suppliers","bcpg:legalName"},{"bcpg:costList|true"})}; // Force full copy of costList
		 *
		 * @param fromNodeRef
		 * @param propQNames
		 * @param listQNames
		 */
		public void copy(NodeRef fromNodeRef, Collection<String> propQNames, Collection<String> listQNames) {
			if (fromNodeRef != null) {
				copy(alfrescoRepository.findOne(fromNodeRef), propQNames, listQNames);
			}
		}

		public void copy(RepositoryEntity from, Collection<String> propQNames, Collection<String> listQNames) {
			copy(entity, from, propQNames, listQNames);
		}

		public void copy(RepositoryEntity to, RepositoryEntity from, Collection<String> propQNames, Collection<String> listQNames) {
			try {
				Set<QName> treatedProp = new HashSet<>();
				Set<QName> treatedList = new HashSet<>();

				if (from != null) {

					copyLists(to, from, listQNames, treatedList, true);
					copyEntityProperties(to, from, listQNames, propQNames, treatedProp, treatedList);
					copyLists(to, from, listQNames, treatedList, false);
					copyExtraProperties(to, from, propQNames, treatedProp);

				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				logger.error(e, e);
			}
		}

		private void copyLists(RepositoryEntity to, RepositoryEntity from, Collection<String> listQNames, Set<QName> treatedList, boolean force) {
			if ((to.getNodeRef() != null) && (from.getNodeRef() != null)) {

				NodeRef listContainerNodeRef = entityListDAO.getListContainer(from.getNodeRef());

				for (String listQName2 : listQNames) {
					if (!force || listQName2.endsWith(SPEL_COPY_FORCE_INDICATOR)) {

						QName listQName = QName.createQName(listQName2.replace("|true", ""), namespaceService);

						if (!treatedList.contains(listQName)) {
							logger.debug("Copy list : " + listQName);
							entityListDAO.copyDataList(entityListDAO.getList(listContainerNodeRef, listQName), to.getNodeRef(), true);

							treatedList.add(listQName);
						}
					}
				}
			}
		}

		public void copyEntityProperties(RepositoryEntity to, RepositoryEntity from, Collection<String> listQNames, Collection<String> propQNames,
				Set<QName> treatedProp, Set<QName> treatedList) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

			BeanWrapper beanWrapper = new BeanWrapperImpl(to);

			for (final PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {

				Method readMethod = pd.getReadMethod();

				if (readMethod != null) {
					if ((readMethod.isAnnotationPresent(AlfProp.class) || readMethod.isAnnotationPresent(AlfMultiAssoc.class)
							|| readMethod.isAnnotationPresent(AlfSingleAssoc.class))) {
						QName qname = repositoryEntityDefReader.readQName(readMethod);
						for (String propQName2 : propQNames) {
							QName propQName = QName.createQName(propQName2, namespaceService);
							if (qname.equals(propQName)) {
								logger.debug("Setting property/assoc : " + propQName + " from repository entity: " + pd.getName());

								PropertyUtils.setProperty(to, pd.getName(), PropertyUtils.getProperty(from, pd.getName()));
								if (!readMethod.isAnnotationPresent(AlfReadOnly.class)) {
									treatedProp.add(propQName);
								}
							}
						}
					} else if (readMethod.isAnnotationPresent(DataList.class)) {
						QName qname = repositoryEntityDefReader.readQName(readMethod);
						for (String listQName1 : listQNames) {
							QName listQName = QName.createQName(listQName1.replace(SPEL_COPY_FORCE_INDICATOR, ""), namespaceService);
							if (qname.equals(listQName)) {
								handleList(listQName, from, to, pd, treatedList);
							}
						}
					} else if (readMethod.isAnnotationPresent(DataListView.class)) {
						for (String listQName1 : listQNames) {
							QName listQName = QName.createQName(listQName1.replace(SPEL_COPY_FORCE_INDICATOR, ""), namespaceService);
							BaseObject fromView = (BaseObject) PropertyUtils.getProperty(from, pd.getName());
							BaseObject toView = (BaseObject) PropertyUtils.getProperty(to, pd.getName());

							for (final PropertyDescriptor pdView : (new BeanWrapperImpl(toView)).getPropertyDescriptors()) {
								Method viewReadMethod = pdView.getReadMethod();
								if (viewReadMethod.isAnnotationPresent(DataList.class)) {
									QName viewQname = repositoryEntityDefReader.readQName(viewReadMethod);
									if (viewQname.equals(listQName)) {
										handleList(listQName, fromView, toView, pdView, treatedList);
									}
								}
							}
						}
					}
				}
			}

		}

		private void handleList(QName listQName, Object from, Object to, PropertyDescriptor pd, Set<QName> treatedList)
				throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			if (!treatedList.contains(listQName)) {
				logger.debug("Setting list : " + listQName + " from repository entity");
				cloneDataList(PropertyUtils.getProperty(from, pd.getName()), PropertyUtils.getProperty(to, pd.getName()), to, pd);
				treatedList.add(listQName);
			} else {
				logger.debug("Reloading list : " + listQName + " from repository");
				Object oldData = PropertyUtils.getProperty(to, pd.getName());
				if (oldData instanceof LazyLoadingDataList) {
					((LazyLoadingDataList<?>) oldData).refresh();
				}
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void cloneDataList(Object data, Object oldData, Object to, PropertyDescriptor pd)
				throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			if (oldData instanceof LazyLoadingDataList lazyDataList) {
				lazyDataList.clear();
				lazyDataList.addAll(clone((Collection) data));
			} else {
				PropertyUtils.setProperty(to, pd.getName(), clone((Collection) data));
			}
		}

		@SuppressWarnings({ "unchecked" })
		private <T extends RepositoryEntity> List<T> clone(Collection<T> data) {
			List<T> clonedList = new LinkedList<>();
			Map<NodeRef, T> origDataItemMap = new HashMap<>();

			for (T originalData : data) {
				NodeRef origNodeRef = originalData.getNodeRef();
				T clonedItem = originalData instanceof CopiableDataItem ? (T) ((CopiableDataItem) originalData).copy() : originalData;
				clonedItem.setName(originalData.getName());
				clonedItem.setNodeRef(null);
				clonedItem.setParentNodeRef(null);
				origDataItemMap.put(origNodeRef, clonedItem);
				clonedList.add(clonedItem);
			}

			for (T item : clonedList) {
				if (item instanceof CompositeDataItem compositeItem) {
					T parent = (T) compositeItem.getParent();
					if (parent != null) {
						if (parent.getNodeRef() != null) {
							compositeItem.setParent(origDataItemMap.get(parent.getNodeRef()));
						} else {
							parent.setParentNodeRef(null);
						}
					}
				}
			}

			return clonedList;
		}

		private void copyExtraProperties(RepositoryEntity to, RepositoryEntity from, Collection<String> propQNames, Set<QName> treatedProp) {
			if (to.getNodeRef() != null) {

				Map<QName, Serializable> extraPropToCopy = nodeService.getProperties(from.getNodeRef());

				for (String propQName2 : propQNames) {
					QName propQName = QName.createQName(propQName2, namespaceService);
					if (!treatedProp.contains(propQName)) {
						PropertyDefinition propertyDef = entityDictionaryService.getProperty(propQName);
						if (propertyDef != null) {
							if (extraPropToCopy.containsKey(propQName)) {
								logger.debug("Setting property : " + propQName + " from nodeRef");
								to.getExtraProperties().put(propQName, extraPropToCopy.get(propQName));
							} else {
								logger.debug("Removing property : " + propQName);
								nodeService.removeProperty(to.getNodeRef(), propQName);
								to.getExtraProperties().remove(propQName);
							}

						} else {
							logger.debug("Setting association : " + propQName + " from nodeRef");
							associationService.update(to.getNodeRef(), propQName, associationService.getTargetAssocs(from.getNodeRef(), propQName));
						}
						treatedProp.add(propQName);

					}

				}
			}

		}

		private void assertIsNotMappedQname(RepositoryEntity item, QName qName, boolean allowWrite) {
			if (item != null && repositoryEntityDefReader.isRegisteredQName(item, qName, allowWrite)) {
				throw new FormulateException(String.format("QName is %s mapped in entity %s. Please use entity.%s to access it ",
						qName.getPrefixedQName(namespaceService), item.getClass().getName(), qName.getLocalName()));
			}
		}

	}

}
