package fr.becpg.repo.formulation.spel;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
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
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.annotation.AlfProp;

/**
 *
 * Register custom beCPG SPEL helper accessible with @beCPG.
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

		RepositoryEntity entity;

		public BeCPGSpelFunctionsWrapper(RepositoryEntity entity) {
			super();
			this.entity = entity;
		}

		/**
		 * Helper @beCPG.findOne($nodeRef)
		 *
		 * <code>
		 * 	Example : @beCPG.findOne(nodeRef).qty
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
		 * Helper @beCPG.propValue($nodeRef, $qname) <code>
		 * Example : @beCPG.propValue(nodeRef,'bcpg:productQty')
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
		 * Helper @beCPG.propValue($entity, $qname)
		 *
		 * @param item
		 * @param qname
		 * @return entity property value
		 */
		public Serializable propValue(RepositoryEntity item, String qname) {
			if (item != null) {
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
		 * Helper @beCPG.propValue( $qname)
		 *
		 * @param qname
		 * @return property value in current entity
		 */
		public Serializable propValue(String qname) {
			return propValue(entity, qname);

		}

		/**
		 * Helper @beCPG.setValue($entity, $qname, $value)
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
				item.getExtraProperties().put(getQName(qname), value);
				return value;
			}
			return null;
		}

		/**
		 * Helper @beCPG.setValue( $qname, $value)
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
		 * Helper @beCPG.setAssocs($nodeRef, $qname, $assocNodeRefs)
		 * 
		 * @param nodeRef
		 * @param qname
		 * @param assocNodeRefs
		 */
		public void setAssocs(NodeRef nodeRef, String qname, List<NodeRef> assocNodeRefs) {
			associationService.update(nodeRef, getQName(qname), assocNodeRefs);
		}

		public void setAssocs(RepositoryEntity entity, String qname, List<NodeRef> assocNodeRefs) {
			associationService.update(entity.getNodeRef(), getQName(qname), assocNodeRefs);
		}

		public void setAssocs(String qname, List<NodeRef> assocNodeRefs) {
			associationService.update(entity.getNodeRef(), getQName(qname), assocNodeRefs);
		}

		/**
		 * Helper @beCPG.setAssoc($nodeRef, $qname, $assocNodeRef)
		 * 
		 * @param nodeRef
		 * @param qname
		 * @param assocNodeRef
		 */
		public void setAssoc(NodeRef nodeRef, String qname, NodeRef assocNodeRef) {
			associationService.update(nodeRef, getQName(qname), assocNodeRef);
		}

		public void setAssoc(String qname, NodeRef assocNodeRef) {
			associationService.update(entity.getNodeRef(), getQName(qname), assocNodeRef);
		}

		public void setAssoc(RepositoryEntity entity, String qname, NodeRef assocNodeRef) {
			associationService.update(entity.getNodeRef(), getQName(qname), assocNodeRef);
		}

		/**
		 * Helper @beCPG.assocValue($nodeRef, $qname)
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
			return assocValue(entity.getNodeRef(), qname);
		}

		public NodeRef assocValue(RepositoryEntity entity, String qname) {
			return assocValue(entity.getNodeRef(), qname);
		}

		
		/**
		 * Helper @beCPG.assocValues($entity, $qname)
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
			return assocValues(entity.getNodeRef(), qname);
		}

		public List<NodeRef> assocValues(String qname) {
			return assocValues(entity.getNodeRef(), qname);
		}
		
		
		/**
		 * Helper @beCPG.sourcesAssocValues($nodeRef, $qname)
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
		 * Helper @beCPG.assocPropValues($nodeRef, $assocQname, $propQName)
		 *
		 * @param nodeRef
		 * @param assocQname
		 * @param propQName
		 * @return collection of association property values
		 */
		public List<Serializable> assocPropValues(NodeRef nodeRef, String assocQname, String propQName) {
			if (nodeRef != null) {
				return associationService.getTargetAssocs(nodeRef, getQName(assocQname)).stream().map(o -> propValue(o, propQName))
						.filter(o -> o != null).collect(Collectors.toList());
			}
			return null;
		}
		
		public List<Serializable> assocPropValues(RepositoryEntity entity ,String assocQname, String propQName) {
			return assocPropValues(entity.getNodeRef(), assocQname, propQName);
		}

		public List<Serializable> assocPropValues(String assocQname, String propQName) {
			return assocPropValues(entity.getNodeRef(), assocQname, propQName);
		}

		
		
		/**
		 * Helper @beCPG.assocAssocValues($nodeRef, $assocQname, $assocAssocQName)
		 *
		 * @param nodeRef
		 * @param assocAssocQName
		 * @param propQName
		 * @return collection of association association values
		 */
		public List<NodeRef> assocAssocValues(NodeRef nodeRef, String assocQname, String assocAssocQName) {
			if (nodeRef != null) {
				return associationService.getTargetAssocs(nodeRef, getQName(assocQname)).stream().map(o -> assocValue(o, assocAssocQName))
						.filter(o -> o != null).collect(Collectors.toList());
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
		 * Helper @beCPG.assocPropValue($nodeRef, $assocQname, $propQName)
		 *
		 * Example : var val = @beCPG.assocPropValue(nodeRef, "bcpg:geoOrigin",
		 * "bcpg:isoCode"); #val!=null ? @beCPG.setValue($nodeRef,
		 * "cm:title", @beCPG.assocPropValue("bcpg:geoOrigin", "bcpg:isoCode"))
		 * : "";
		 *
		 * or
		 *
		 * @beCPG.setValue("cm:title", @beCPG.assocPropValue("bcpg:geoOrigin",
		 * "bcpg:isoCode"))
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
		 * Helper @beCPG.findDuplicates($collection)
		 *
		 * @return Set of duplicates
		 */
		public <T> Set<T> findDuplicates(Collection<? extends T> collection) {
			    Set<T> uniques = new HashSet<>();
			    return collection.stream()
			        .filter(e -> !uniques.add(e))
			        .collect(Collectors.toSet());
			  }
		
		
		/**
		 * Helper @beCPG.getQName($qname)
		 *
		 * @param qName
		 * @return QName from string
		 */
		public QName getQName(String qName) {
			return QName.createQName(qName, namespaceService);
		}

		/**
		 * Helper @beCPG.updateMLText($mltext, $locale, $value)
		 *
		 * Update mlText locale value
		 *
		 * @param mlText
		 * @param locale
		 * @param value
		 * @return value beeing set
		 */
		public MLText updateMLText(MLText mlText, String locale, String value) {

			if (mlText == null) {
				mlText = new MLText();
			}

			if ((value != null) && !value.isEmpty()) {
				mlText.addValue(MLTextHelper.parseLocale(locale), value);
			} else {
				mlText.removeValue(MLTextHelper.parseLocale(locale));
			}

			return mlText;
		}

		/**
		 * @beCPG.runScript($nodeRef)
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
		 * @beCPG.sum($range, $formula)
		 *
		 *                    Example :
		 * @beCPG.sum(compoListView.compoList.?[parent ==
		 *                                             null],"entity.costList[0].value
		 *                                             + dataListItem.qty")
		 * @beCPG.sum(compoListView.compoList.?[parent ==
		 *                                             null],"@beCPG.propValue(dataListItem.nodeRef,'bcpg:compoListQty')")
		 *
		 * @param range
		 * @param formula
		 * @return sum of formula results apply on range
		 */
		public Double sum(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.SUM);
		}

		/**
		 * @beCPG.sum($range)
		 *
		 * @param range
		 * @return sum range of double
		 */
		public Double sum(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).sum();
		}

		/**
		 * @beCPG.avg($range, $formula)
		 *
		 * @param range
		 * @param formula
		 * @return average of formula results apply on range
		 */
		public Double avg(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.AVG);
		}

		/**
		 * @beCPG.avg($range)
		 *
		 *                    @param range
		 * @return average range of double
		 */
		public Double avg(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		}

		/**
		 * @beCPG.max($range, $formula)
		 *
		 * @param range
		 * @param formula
		 * @return get max of formula results apply on range
		 */
		public Double max(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.MAX);
		}

		/**
		 * @beCPG.max($range)
		 *
		 * @param range
		 * @return get max of range of double
		 */
		public Double max(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
		}

		/**
		 * @beCPG.min($range, $formula)
		 *
		 * @param range
		 * @param formula
		 * @return get min of formula results apply on range
		 */
		public Double min(Collection<RepositoryEntity> range, String formula) {
			return formulaService.aggreate(entity, range, formula, SpelFormulaContext.Operator.MIN);
		}

		/**
		 * @beCPG.min($range)
		 *
		 * @param range
		 * @return get min of range of double
		 */
		public Double min(Collection<Double> range) {
			return range.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
		}

		/**
		 * @beCPG.extractCustomList($nodeRef, $listType) @param listType
		 * @return list
		 */
		public Collection<RepositoryEntity> extractCustomList(NodeRef nodeRef, String listType) {
			return alfrescoRepository.loadDataList(entity.getNodeRef(), getQName(listType), getQName(listType));
		}

		/**
		 * @beCPG.extractCustomList($listType) @param listType
		 * @return list
		 */
		public Collection<RepositoryEntity> extractCustomList(String listType) {
			return extractCustomList(entity.getNodeRef(), listType);
		}

		/**
		 * @beCPG.saveCustomList($range)
		 *
		 * @param range
		 */
		public void saveCustomList(Collection<RepositoryEntity> range) {
			alfrescoRepository.save(range);
		}

		/**
		 * @beCPG.applyFormulaToList($range, $formula)
		 *
		 * @param range
		 * @param formula
		 */
		public void applyFormulaToList(Collection<RepositoryEntity> range, String formula) {
			formulaService.applyToList(entity, range, formula);
		}

		/**
		 * Helper @beCPG.filter($range, formula)
		 *
		 * @param <T>
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
				}).collect(Collectors.toList());
			}
			return null;
		}
		
		/**
		 * Helper @beCPG.filterByAssoc($range, $assocQname, $values)
		 *
		 * @param <T>
		 * @param range
		 * @param assocName
		 * @param values
		 * @return filter nodeRef collection by assoc values
		 */
		public List<NodeRef> filterByAssoc(Collection<NodeRef> range, String assocName, Collection<NodeRef> values ){
			if(range!=null) {
				return range.stream().filter(nodeRef -> {
					List<NodeRef> assocs = associationService.getTargetAssocs(nodeRef, getQName(assocName));
					return assocs.containsAll(values);
				}).collect(Collectors.toList());
			}
			return null;
		}
		
		/**
		 * Helper @beCPG.getOrDefault($range, $index, $defaultValue)
		 * 
		 * @param <T>
		 * @param range
		 * @param index
		 * @param defaultValue
		 * @return defaultValue id list index doesn't exists
		 */
		public <T> T getOrDefault(List<T> range,int index, T defaultValue) {
			if(range!=null && !range.isEmpty() &&  index >= 0 && index < range.size()) {
				return range.get(index);
			}
			return defaultValue;
		}
		

		/**
		 * Helper  @beCPG.children($parent, $compositeList)
		 *
		 * @param <T>
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
		 * Helper @beCPG.formatNumber($number)
		 *
		 * @param number
		 * @return standard becpg number format
		 */
		public String formatNumber(Number number) {
			return attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDecimal(number);
		}

		/**
		 * Helper @beCPG.formatNumber($number, $format )
		 *
		 *    Example: @beCPG.formatNumber(10,00005,
		 *                              "0.##")
		 *
		 * @param number
		 * @param format
		 * @return formated number to provided format
		 */
		public String formatNumber(Number number, String format) {
			return new java.text.DecimalFormat(format).format(number);
		}

		/**
		 * Helper  @beCPG.formatDate($date )
		 *
		 *  Example: @beCPG.formatNumber(10,00005,
		 *                         "0.##")
		 *
		 * @param date
		 * @return standard becpg date format
		 */
		public String formatDate(Date date) {
			return attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDate(date);
		}

		/**
		 * @beCPG.formatDate($date, $format )
		 *
		 *  Example: @beCPG.formatDate(new
		 *                          java.util.Date(),"dd/mm/YYYY" )
		 *
		 * @param date
		 * @return standard becpg number format
		 */
		public String formatDate(Date date, String format) {
			return new java.text.SimpleDateFormat(format).format(date);
		}

		/**
		 *
		 * Helper  @beCPG.copy($fromNodeRef, $propQNames, $listQNames)
		 *
		 *                           Copy properties from an entity to current
		 *                           entity
		 *
		 *                           Example: @beCPG.copy(compoListView.compoList[0].product,{"bcpg:suppliers","bcpg:legalName"},{"bcpg:costList"});
		 *
		 * @param fromNodeRef
		 * @param propQNames
		 * @param listQNames
		 */
		public void copy(NodeRef fromNodeRef, Collection<String> propQNames, Collection<String> listQNames) {
			try {
				Set<QName> treatedProp = new HashSet<>();
				Set<QName> treatedList = new HashSet<>();

				RepositoryEntity from = alfrescoRepository.findOne(fromNodeRef);

				if (from != null) {
					BeanWrapper beanWrapper = new BeanWrapperImpl(entity);

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

								for (String listQName1 : listQNames) {
									QName listQName = QName.createQName(listQName1, namespaceService);
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
							PropertyDefinition propertyDef = entityDictionaryService.getProperty(propQName);
							if (propertyDef != null) {
								if (extraPropToCopy.containsKey(propQName)) {
									logger.debug("Setting property : " + propQName + " from nodeRef");
									nodeService.setProperty(entity.getNodeRef(), propQName, extraPropToCopy.get(propQName));
								} else {
									logger.debug("Removing property : " + propQName);
									nodeService.removeProperty(entity.getNodeRef(), propQName);
								}

							} else {
								logger.debug("Setting association : " + propQName + " from nodeRef");
								associationService.update(entity.getNodeRef(), propQName,
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
							entityListDAO.copyDataList(entityListDAO.getList(listContainerNodeRef, listQName), entity.getNodeRef(), true);

							treatedList.add(listQName);
						}
					}
				}
				
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				logger.error(e, e);
			}
		}

	}

}
