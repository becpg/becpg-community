/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.autocomplete.impl.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.extractors.TargetAssocAutoCompleteExtractor;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>TargetAssocAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 * Default autocomplete used to suggest targetAssociation
 *
 * Example:
 * 
 * <pre>
 * {@code
 * <control template="/org/alfresco/components/form/controls/autocomplete-association.ftl">
 *     <control-param name="ds">becpg/autocomplete/targetassoc/associations/bcpg:product?classNames=bcpg:entityTplAspect&amp;excludeProps=bcpg:entityTplEnabled%7Cfalse</control-param>
 *     <control-param name="pageLinkTemplate">entity-data-lists?list=View-properties&amp;nodeRef={nodeRef}</control-param>
 *  </control>
 * }
 * </pre>
 *
 *  Datasources:
 *
 * ds: /becpg/autocomplete/targetassoc/associations/{className}?classNames={classNames?}&amp;excludeProps={excludeProps?)&amp;path={path?}&amp;filter={}&
 * param: {className} type of item to retrieve
 * param: {classNames} (optional)  comma separated lists of classNames, can be uses to filter by aspect or boost certain types (inc_ or ^)

 * Examples:
 *
 *   With aspect
 *    classNames=bcpg:entityTplAspect
 *   Includes aspect (OR)
 *    classNames=inc_bcpg:entityTplAspect,inc_gs1:gs1Aspect
 *   Boost specific types
 *   classNames=bcpg:rawMaterial^2
 *
 * param: {andProps} (optional/deprecated) comma separated of property|value pair that item should have  (filter=prop_to_filter|value)
 * param: {filter} (optional) same as andProps
 *
 * Examples:
 *
 *  filter=prop_to_filter|value
 *	filter=cm:name|samplename
 *	filter=cm:name|{cm:title}
 *  filter=bcpg:code|{bcpg:code},cm:name|MP* 
 *  filter=au:market|{au:market}
 *  filter=gs1:sortingBonusCriteria_or|{gs1:sortingBonusCriteria}  (when field is multiple default operator is and _or allow to change that)
 *  filter=bcpg:ingTypeV2|{htmlPropValue} use the value of parent or parentAssoc control-param (@Since 4.2)
 *
 * param: {excludeProps} (optional) comma separated of property|value pair that item should not have
 * param: {excludeClassNames} (optional) comma separated lists of classNames that will be excluded
 * param: {path} (optional) retrieve item in specific path if path doesn't contains / path is relative to current entity
 *
 * Example:
 *  path=System/Characts/bcpg:entityLists/Contacts
 *
 * param: {extra.filterByAssoc} return item that has same assoc that is in the current entity
 *
 * If parent is provided use parent as the targetAssoc
 *
 * Example:
 *
 * If itemId is provides use itemId as entity
 * Else use currentEntity
 *
 * Examples:
 *
 * becpg/autocomplete/product?extra.filterByAssoc=bcpg:plant
 * becpg/autocomplete/product?extra.filterByAssoc=bcpg:plant_or
 *
 * <pre>
 * {@code
 *    <control
 *       template="/org/alfresco/components/form/controls/autocomplete-association.ftl">
 *       <control-param name="ds">becpg/autocomplete/targetassoc/associations/bcpg:entityV2?extra.filterByAssoc=bcpg:trademarkRef</control-param>
 *       <control-param name="parentAssoc">sample_plTrademark</control-param>
 *   </control>
 *  }
 * </pre>
 */
@Service("targetAssocAutoCompletePlugin")
@BeCPGPublicApi
public class TargetAssocAutoCompletePlugin implements AutoCompletePlugin {

	private static final Log logger = LogFactory.getLog(TargetAssocAutoCompletePlugin.class);

	protected static final String PROP_FILTER_BY_ASSOC = "filterByAssoc";
	/** Constant <code>SOURCE_TYPE_TARGET_ASSOC="targetassoc"</code> */
	protected static final String SOURCE_TYPE_TARGET_ASSOC = "targetassoc";
	/** Constant <code>searchTemplate="%(cm:name bcpg:erpCode bcpg:code bcpg:l"{trunked}</code> */
	protected static final String DEFAULT_SEARCH_TEMPLATE = "%(cm:name bcpg:erpCode bcpg:code bcpg:legalName)";
	/** Constant <code>mixedSearchTemplate="%(cm:name bcpg:erpCode bcpg:code bcpg:c"{trunked}</code> */
	protected static final String MIXED_SEARCH_TEMPLATE = "%(cm:name bcpg:erpCode bcpg:code bcpg:charactName bcpg:legalName bcpg:lvValue)";
	/** Constant <code>charactSearchTemplate="%(bcpg:charactName bcpg:legalName)"</code> */
	protected static final String CHARACT_SEARCH_TEMPLATE = "%(bcpg:charactName bcpg:erpCode bcpg:legalName)";
	/** Constant <code>listValueSearchTemplate="%(bcpg:lvValue bcpg:legalName)"</code> */
	protected static final String LISTVALUE_SEARCH_TEMPLATE = "%(bcpg:lvValue bcpg:lvCode bcpg:legalName )";
	/** Constant <code>personSearchTemplate="%(cm:userName cm:firstName cm:lastName "{trunked}</code> */
	protected static final String PERSON_SEARH_TEMPLATE = "%(cm:userName cm:firstName cm:lastName cm:email)";

	/** Constant <code>PARAM_VALUES_SEPARATOR=","</code> */
	protected static final String PARAM_VALUES_SEPARATOR = ",";

	@Autowired
	@Qualifier("NodeService")
	protected NodeService nodeService;
	@Autowired
	protected NamespaceService namespaceService;
	@Autowired
	protected DictionaryService dictionaryService;
	@Autowired
	protected EntityDictionaryService entityDictionaryService;
	@Autowired
	protected AutoNumService autoNumService;
	@Autowired
	protected AttributeExtractorService attributeExtractorService;

	@Autowired
	protected TargetAssocAutoCompleteExtractor targetAssocValueExtractor;
	@Autowired
	protected AssociationService associationService;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_TARGET_ASSOC };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String path = (String) props.get(AutoCompleteService.PROP_PATH);
		String className = (String) props.get(AutoCompleteService.PROP_CLASS_NAME);
		String classNames = (String) props.get(AutoCompleteService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;

		return suggestTargetAssoc(path, QName.createQName(className, namespaceService), query, pageNum, pageSize, arrClassNames, props);

	}

	/**
	 * Suggest target class according to query
	 *
	 * Query path : +PATH:"/app:company_home/cm:System/cm:Lists/cm:Nuts/*"
	 * +TYPE:"bcpg:nut" +@cm\:name:"Nut1*".
	 *
	 * @param type
	 *            the type
	 * @param query
	 *            the query
	 * @param props a {@link java.util.Map} object.
	 * @return the map
	 * @param path a {@link java.lang.String} object.
	 * @param pageNum a {@link java.lang.Integer} object.
	 * @param pageSize a {@link java.lang.Integer} object.
	 * @param arrClassNames an array of {@link java.lang.String} objects.
	 */
	public AutoCompletePage suggestTargetAssoc(String path, QName type, String query, Integer pageNum, Integer pageSize, String[] arrClassNames,
			Map<String, Serializable> props) {

		if (logger.isDebugEnabled()) {
			if (arrClassNames != null) {
				logger.debug("suggestTargetAssoc with arrClassNames : " + Arrays.toString(arrClassNames));
			}
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();
		
		queryBuilder.excludeProp(BeCPGModel.PROP_IS_DELETED, "true");

		String template = DEFAULT_SEARCH_TEMPLATE;
		if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)) {
			template = CHARACT_SEARCH_TEMPLATE;
			if (isAllQuery(query)) {
				queryBuilder.addSort(BeCPGModel.PROP_CHARACT_NAME, true);
			}
		} else if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_LIST_VALUE)) {
			template = LISTVALUE_SEARCH_TEMPLATE;
			if (isAllQuery(query)) {
				queryBuilder.addSort(BeCPGModel.PROP_LV_VALUE, true);
			}
		} else if (entityDictionaryService.isSubClass(type, ContentModel.TYPE_PERSON)) {
			template = PERSON_SEARH_TEMPLATE;
			if (isAllQuery(query)) {
				queryBuilder.addSort(ContentModel.PROP_LASTNAME, true);
			}
		} else if (arrClassNames != null) {
			for (String className : arrClassNames) {
				QName classQName;
				if (className.contains("^")) {
					String[] splitted = className.split("\\^");
					classQName = QName.createQName(splitted[0], namespaceService);
				} else {
					classQName = QName.createQName(className.replace("inc_", ""), namespaceService);
				}
				if (entityDictionaryService.isSubClass(classQName, BeCPGModel.TYPE_CHARACT)) {
					template = MIXED_SEARCH_TEMPLATE;
					break;
				}
			}
		} else {
			if (isAllQuery(query)) {
				queryBuilder.addSort(ContentModel.PROP_NAME, true);
			}
		}

		queryBuilder.ofType(type).excludeDefaults().inSearchTemplate(template).locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

		if (!isAllQuery(query)) {
			StringBuilder ftsQuery = new StringBuilder();
			if (query.length() > 2) {
				ftsQuery.append("(" + prepareQuery(query.trim()) + ") OR ");
			}
			ftsQuery.append("(" + query + ")");
			queryBuilder.andFTSQuery(ftsQuery.toString());
		}

		return new AutoCompletePage(filter(queryBuilder, path, arrClassNames, pageSize, props), pageNum, pageSize, getTargetAssocValueExtractor());

	}

	@SuppressWarnings("unchecked")
	protected List<NodeRef> filter(BeCPGQueryBuilder queryBuilder, String path, String[] arrClassNames, Integer pageSize,
			Map<String, Serializable> props) {
		NodeRef entityNodeRef = null;
		if (props != null) {

			String strNodeRef = (String) props.get(AutoCompleteService.PROP_NODEREF); //itemId
			if ((strNodeRef == null) || strNodeRef.isBlank()) {
				strNodeRef = (String) props.get(AutoCompleteService.PROP_ENTITYNODEREF);
			}

			if ((strNodeRef != null) && NodeRef.isNodeRef(strNodeRef)) {
				entityNodeRef = new NodeRef(strNodeRef);
			}

		}

		filterByPath(queryBuilder, path, entityNodeRef);

		// filter by classNames
		filterByClass(queryBuilder, arrClassNames);

		List<NodeRef> ret = null;

		if (props != null) {

			String propParent = (String) props.get(AutoCompleteService.PROP_PARENT);

			// exclude class
			String excludeClassNames = (String) props.get(AutoCompleteService.PROP_EXCLUDE_CLASS_NAMES);
			String[] arrExcludeClassNames = excludeClassNames != null ? excludeClassNames.split(PARAM_VALUES_SEPARATOR) : null;
			excludeByClass(queryBuilder, arrExcludeClassNames);

			String excluseProps = (String) props.get(AutoCompleteService.PROP_EXCLUDE_PROPS);
			String[] arrExcluseProps = excluseProps != null ? excluseProps.split(PARAM_VALUES_SEPARATOR) : null;
			excludeByProp(queryBuilder, arrExcluseProps);

			String andProps = (String) props.get(AutoCompleteService.PROP_AND_PROPS);
			filterByQueryFilter(queryBuilder, andProps, entityNodeRef, propParent);

			String queryFilter = (String) props.get(AutoCompleteService.PROP_FILTER);
			filterByQueryFilter(queryBuilder, queryFilter, entityNodeRef, propParent);

			Map<String, String> extras = (HashMap<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
			if (extras != null) {
				String filterByAssoc = extras.get(PROP_FILTER_BY_ASSOC);
				NodeRef targetNodeRef = null;
				if ((propParent != null) && !propParent.isBlank() && NodeRef.isNodeRef(propParent)) {
					targetNodeRef = new NodeRef(propParent);
				}
				if ((filterByAssoc != null) && (filterByAssoc.length() > 0)) {
					ret = filterByAssoc(queryBuilder, pageSize, entityNodeRef, filterByAssoc, targetNodeRef);
				}
			}
		}


		if (ret == null) {

			if (RepoConsts.MAX_RESULTS_UNLIMITED.equals(pageSize)) {
				queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
			} else {
				queryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS);
			}
			
			ret = queryBuilder.list();
		}
		
		if (props != null && props.containsKey(AutoCompleteService.PROP_EXCLUDE_SOURCES) && "true".equals(props.get(AutoCompleteService.PROP_EXCLUDE_SOURCES))) {
			String itemId = (String) props.get(AutoCompleteService.PROP_ITEM_ID);
			String fieldName = (String) props.get(AutoCompleteService.PROP_FIELD_NAME);
			excludeSources(ret, itemId, fieldName);
		}

		return ret;
	}

	private void excludeSources(List<NodeRef> ret, String itemId, String fieldName) {
		if (itemId != null && NodeRef.isNodeRef(itemId) && fieldName != null && fieldName.startsWith("assoc_")) {
			QName fieldQName = QName.createQName(fieldName.split("assoc_")[1].replace("_", ":"), namespaceService);
			NodeRef rootNodeRef = new NodeRef(itemId);
			ret.removeAll(extractAllSources(rootNodeRef, fieldQName, new ArrayList<>()));
		}
	}

	private List<NodeRef> extractAllSources(NodeRef source, QName fieldQname, List<NodeRef> allSources) {
		if (!allSources.contains(source)) {
			allSources.add(source);
			List<NodeRef> sourceSources = associationService.getSourcesAssocs(source, fieldQname);
			for (NodeRef sourceSource : sourceSources) {
				extractAllSources(sourceSource, fieldQname, allSources);
			}
		}
		return allSources;
	}

	private List<NodeRef> filterByAssoc(BeCPGQueryBuilder queryBuilder, Integer pageSize, NodeRef entityNodeRef,
			String filterByAssoc, NodeRef targetNodeRef) {
		
		List<NodeRef> ret = null;
		boolean isOrOperand = false;
		if (filterByAssoc.endsWith("_or")) {
			isOrOperand = true;
			filterByAssoc = filterByAssoc.replace("_or", "");
		}

		QName assocQName = QName.createQName(filterByAssoc, namespaceService);


		List<NodeRef> targetNodeRefs = null;

		if (targetNodeRef != null) {
			targetNodeRefs = Arrays.asList(targetNodeRef);
		} else if (entityNodeRef != null) {
			targetNodeRefs = associationService.getTargetAssocs(entityNodeRef, assocQName);
		}

		if ((targetNodeRefs != null) && !targetNodeRefs.isEmpty()) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("Filter by assoc: " +filterByAssoc+ " " +targetNodeRefs.toString() );
			}
			
			List<NodeRef> tmp = queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();
			List<NodeRef> nodesToKeep = new ArrayList<>();

			if (isOrOperand) {
				for (NodeRef assocNodeRef : targetNodeRefs) {
					nodesToKeep.addAll(associationService.getSourcesAssocs(assocNodeRef, assocQName));
				}
			} else if (!targetNodeRefs.isEmpty()) {
				nodesToKeep.addAll(associationService.getSourcesAssocs(targetNodeRefs.get(0), assocQName));
			}

			tmp.retainAll(nodesToKeep);
			if (!RepoConsts.MAX_RESULTS_UNLIMITED.equals(pageSize)) {
				ret = tmp.subList(0, Math.min(RepoConsts.MAX_SUGGESTIONS, tmp.size()));
			} else {
				ret = tmp;
			}
		}
		return ret;
	}

	private void filterByPath(BeCPGQueryBuilder queryBuilder, String path, NodeRef entityNodeRef) {

		if ((path != null) && !path.isEmpty()) {
			if (!path.contains("/")) {
				try {
					QName assocQname = QName.createQName(path, namespaceService);

					if (entityNodeRef != null) {
						NodeRef targetAssocNodeRef = associationService.getTargetAssoc(entityNodeRef, assocQname);
						if (targetAssocNodeRef != null) {

							String targetAssocPath = nodeService.getPath(targetAssocNodeRef).toPrefixString(namespaceService);
							if (logger.isDebugEnabled()) {
								logger.debug("Filtering by node  path:" + targetAssocPath);
							}

							queryBuilder.inPath(targetAssocPath + "/");
						}
					}
				} catch (NamespaceException e) {
					queryBuilder.inPath(path);
				}
			} else {
				queryBuilder.inPath(path);
			}
		}

	}

	/**
	 * <p>Getter for the field <code>targetAssocValueExtractor</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.listvalue.ListValueExtractor} object.
	 */
	protected AutoCompleteExtractor<NodeRef> getTargetAssocValueExtractor() {
		return targetAssocValueExtractor;
	}

	/**
	 * <p>filterByClass.</p>
	 *
	 * @param queryBuilder a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 * @param arrClassNames an array of {@link java.lang.String} objects.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	protected BeCPGQueryBuilder filterByClass(BeCPGQueryBuilder queryBuilder, String[] arrClassNames) {

		if (arrClassNames != null) {

			for (String className : arrClassNames) {
				boolean include = false;
				QName classQName;
				Integer boost = null;
				if (className.contains("^")) {
					String[] splitted = className.split("\\^");
					classQName = QName.createQName(splitted[0], namespaceService);
					boost = Integer.valueOf(splitted[1]);
				} else {
					if (className.startsWith("inc_")) {
						include = true;
						classQName = QName.createQName(className.replace("inc_", ""), namespaceService);
					} else {
						classQName = QName.createQName(className, namespaceService);
					}
				}
				ClassDefinition classDef = dictionaryService.getClass(classQName);
				if (classDef.isAspect()) {
					if (include) {
						queryBuilder.includeAspect(classQName);
					} else {
						queryBuilder.withAspect(classQName);
					}
				} else {
					if (boost != null) {
						queryBuilder.inBoostedType(classQName, boost);
					} else {
						queryBuilder.inType(classQName);
					}
				}
			}
		}

		return queryBuilder;
	}

	protected BeCPGQueryBuilder filterByQueryFilter(BeCPGQueryBuilder queryBuilder, String queryFilters, NodeRef entityNodeRef, String propParent) {

		if ((queryFilters != null) && (!queryFilters.isEmpty())) {

			String[] arrQueryFilters = queryFilters.split(PARAM_VALUES_SEPARATOR);

			if (arrQueryFilters != null) {

				for (String queryFilter : arrQueryFilters) {
					String[] splitted = queryFilter.split("\\|");

					String filterValue = splitted[1];
					String propQName = splitted[0];
					if ((filterValue != null) && !filterValue.isEmpty()) {
						if (filterValue.contains("{")) {
							if (propParent != null) {
								filterValue = filterValue.replace("{htmlPropValue}", propParent);
								if (NodeRef.isNodeRef(propParent)) {
									filterValue = attributeExtractorService.extractExpr(filterValue, new NodeRef(propParent));
								}
							} else if (entityNodeRef != null) {
								filterValue = attributeExtractorService.extractExpr(filterValue, entityNodeRef);
							}
						}
						if ((filterValue != null) && !filterValue.isEmpty() && !filterValue.contains("{")) {
							boolean isOrOperand = false;
							if (propQName.endsWith("_or")) {
								isOrOperand = true;
								propQName = propQName.replace("_or", "");
							}

							if (filterValue.contains(",")) {
								if (isOrOperand) {
									queryBuilder.andPropQuery(QName.createQName(propQName, namespaceService), filterValue.replace(",", " or "));
								} else {
									queryBuilder.andPropQuery(QName.createQName(propQName, namespaceService), filterValue.replace(",", " and "));
								}
							} else {
								queryBuilder.andPropEquals(QName.createQName(propQName, namespaceService), filterValue);
							}
						}
					}
				}
			}
		}
		return queryBuilder;

	}

	protected BeCPGQueryBuilder excludeByProp(BeCPGQueryBuilder queryBuilder, String[] arrExcluseProps) {
		if (arrExcluseProps != null) {
			for (String excludeProp : arrExcluseProps) {
				if (excludeProp.contains("|")) {
					String[] splitted = excludeProp.split("\\|");
					QName propName = QName.createQName(splitted[0], namespaceService);
					queryBuilder.excludeProp(propName, splitted[1]);
				}
			}
		}
		return queryBuilder;
	}

	protected BeCPGQueryBuilder excludeByClass(BeCPGQueryBuilder queryBuilder, String[] arrClassNames) {

		if (arrClassNames != null) {

			for (String className : arrClassNames) {

				QName classQName = QName.createQName(className, namespaceService);
				ClassDefinition classDef = dictionaryService.getClass(classQName);

				if (classDef.isAspect()) {
					queryBuilder.excludeAspect(classQName);
				} else {
					queryBuilder.excludeType(classQName);
				}

			}
		}

		return queryBuilder;
	}

	/**
	 * <p>isAllQuery.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	protected boolean isAllQuery(String query) {
		return BeCPGQueryHelper.isAllQuery(query);
	}

	/**
	 * <p>prepareQuery.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String prepareQuery(String query) {
		return BeCPGQueryHelper.prepareQuery(query);
	}

	/**
	 * <p>isQueryMatch.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @param entityName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isQueryMatch(String query, String entityName) {
		return BeCPGQueryHelper.isQueryMatch(query, entityName);
	}

	/**
	 * Suggest a dalist item
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param datalistType a {@link org.alfresco.service.namespace.QName} object.
	 * @param propertyQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param query a {@link java.lang.String} object.
	 * @param pageNum a {@link java.lang.Integer} object.
	 * @param pageSize a {@link java.lang.Integer} object.
	 * @return a {@link fr.becpg.repo.listvalue.AutoCompletePage} object.
	 */
	protected AutoCompletePage suggestDatalistItem(NodeRef entityNodeRef, QName datalistType, QName propertyQName, String query, Integer pageNum,
			Integer pageSize) {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(datalistType).andPropQuery(propertyQName, prepareQuery(query))
				.inPath(nodeService.getPath(entityNodeRef).toPrefixString(namespaceService) + "/*/*").maxResults(RepoConsts.MAX_SUGGESTIONS);

		PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
		// Tokenised "false" or "both"
		if (propertyDef != null) {

			if (IndexTokenisationMode.BOTH.equals(propertyDef.getIndexTokenisationMode())
					|| (IndexTokenisationMode.FALSE.equals(propertyDef.getIndexTokenisationMode()) && isAllQuery(query))) {
				queryBuilder.addSort(propertyQName, true);
			}
		}

		return new AutoCompletePage(queryBuilder.list(), pageNum, pageSize, new NodeRefAutoCompleteExtractor(propertyQName, nodeService));
	}

}
