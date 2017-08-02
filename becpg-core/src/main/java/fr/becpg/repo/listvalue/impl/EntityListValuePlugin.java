/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
package fr.becpg.repo.listvalue.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.listvalue.ListValueExtractor;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValuePlugin;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service
public class EntityListValuePlugin implements ListValuePlugin {

	private static final Log logger = LogFactory.getLog(EntityListValuePlugin.class);

	private static final String PROP_FILTER_BY_ASSOC = "filterByAssoc";
	protected static final String SOURCE_TYPE_TARGET_ASSOC = "targetassoc";
	protected static final String SOURCE_TYPE_LINKED_VALUE = "linkedvalue";
	protected static final String SOURCE_TYPE_LINKED_VALUE_ALL = "allLinkedvalue";
	protected static final String SOURCE_TYPE_LIST_VALUE = "listvalue";
	protected static final String searchTemplate = "%(cm:name  bcpg:erpCode bcpg:code bcpg:legalName)";
	protected static final String mixedSearchTemplate = "%(cm:name  bcpg:erpCode bcpg:code bcpg:charactName bcpg:legalName bcpg:lvValue)";
	protected static final String charactSearchTemplate = "%(bcpg:charactName bcpg:legalName)";
	protected static final String listValueSearchTemplate = "%(bcpg:lvValue bcpg:legalName)";

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
	private HierarchyService hierarchyService;
	@Autowired
	private HierarchyValueExtractor hierarchyValueExtractor;
	@Autowired
	private EntityListDAO entityListDAO;
	@Autowired
	protected TargetAssocValueExtractor targetAssocValueExtractor;


	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_TARGET_ASSOC, SOURCE_TYPE_LINKED_VALUE, SOURCE_TYPE_LINKED_VALUE_ALL, SOURCE_TYPE_LIST_VALUE };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String path = (String) props.get(ListValueService.PROP_PATH);
		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		String classNames = (String) props.get(ListValueService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;

		switch (sourceType) {
		case SOURCE_TYPE_TARGET_ASSOC:
			QName type = QName.createQName(className, namespaceService);
			return suggestTargetAssoc(path, type, query, pageNum, pageSize, arrClassNames, props);
		case SOURCE_TYPE_LINKED_VALUE:
			return suggestLinkedValue(path, query, pageNum, pageSize, props, false);
		case SOURCE_TYPE_LINKED_VALUE_ALL:
			return suggestLinkedValue(path, query, pageNum, pageSize, props, true);
		case SOURCE_TYPE_LIST_VALUE:
			return suggestListValue(path, query, pageNum, pageSize);
		}

		return null;
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
	 * @param props
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	public ListValuePage suggestTargetAssoc(String path, QName type, String query, Integer pageNum, Integer pageSize, String[] arrClassNames,
			Map<String, Serializable> props) {

		if (logger.isDebugEnabled()) {
			if (arrClassNames != null) {
				logger.debug("suggestTargetAssoc with arrClassNames : " + Arrays.toString(arrClassNames));
			}
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();

		String template = searchTemplate;
		if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)) {
			template = charactSearchTemplate;
			if(isAllQuery(query)){
				queryBuilder.addSort(BeCPGModel.PROP_CHARACT_NAME, true);
			}
		} else if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_LIST_VALUE)) {
			template = listValueSearchTemplate;
			if(isAllQuery(query)){
				queryBuilder.addSort(BeCPGModel.PROP_LV_VALUE, true);
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
					template = mixedSearchTemplate;
					break;
				}
			}
		} else {
			if(isAllQuery(query)){
				queryBuilder.addSort(ContentModel.PROP_NAME, true);
			}
		}

		queryBuilder.ofType(type).excludeDefaults().inSearchTemplate(template).locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

		if (!isAllQuery(query)) {
			if (query.length() > 2) {
				queryBuilder.andFTSQuery(prepareQuery(query.trim() + BeCPGQueryHelper.SUFFIX_ALL));
			}
			queryBuilder.andFTSQuery(query);
		}

		if ((path != null) && !path.isEmpty()) {
			queryBuilder.inPath(path);
		}

		// filter by classNames
		filterByClass(queryBuilder, arrClassNames);

		List<NodeRef> ret = null;

		if (props != null) {

			// exclude class
			String excludeClassNames = (String) props.get(ListValueService.PROP_EXCLUDE_CLASS_NAMES);
			String[] arrExcludeClassNames = excludeClassNames != null ? excludeClassNames.split(PARAM_VALUES_SEPARATOR) : null;
			excludeByClass(queryBuilder, arrExcludeClassNames);

			String excluseProps = (String) props.get(ListValueService.PROP_EXCLUDE_PROPS);
			String[] arrExcluseProps = excluseProps != null ? excluseProps.split(PARAM_VALUES_SEPARATOR) : null;
			excludeByProp(queryBuilder, arrExcluseProps);

			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				String filterByAssoc = extras.get(PROP_FILTER_BY_ASSOC);
				String strAssocNodeRef = (String) props.get(ListValueService.PROP_PARENT);
				if ((filterByAssoc != null) && (filterByAssoc.length() > 0) && (strAssocNodeRef != null) && (strAssocNodeRef.length() > 0)) {
					QName assocQName = QName.createQName(filterByAssoc, namespaceService);

					NodeRef nodeRef = new NodeRef(strAssocNodeRef);

					if (nodeService.exists(nodeRef)) {

						List<NodeRef> tmp = queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();

						List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, assocQName);

						List<NodeRef> nodesToKeep = new ArrayList<>();
						for (AssociationRef assocRef : assocRefs) {
							nodesToKeep.add(assocRef.getSourceRef());
						}
						tmp.retainAll(nodesToKeep);

						ret = tmp.subList(0, Math.min(RepoConsts.MAX_SUGGESTIONS, tmp.size()));
					}
				}
			}
		}
		queryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS);

		if (ret == null) {
			ret = queryBuilder.list();
		}

		return new ListValuePage(ret, pageNum, pageSize, getTargetAssocValueExtractor());

	}



	protected ListValueExtractor<NodeRef> getTargetAssocValueExtractor() {
		return targetAssocValueExtractor;
	}

	/**
	 * Suggest linked value according to query
	 *
	 * Query path : +PATH:
	 * "/app:company_home/cm:System/cm:LinkedLists/cm:Hierarchy/cm:Hierarchy1_Hierarchy2*"
	 * +TYPE:"bcpg:LinkedValue" +@cm\:lkvPrevValue:"hierar*".
	 *
	 * @param path
	 *            the path
	 * @param parent
	 *            the parent
	 * @param query
	 *            the query
	 * @param b
	 * @return the map
	 */
	private ListValuePage suggestLinkedValue(String path, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props,
			boolean all) {

		NodeRef itemIdNodeRef = null;

		if (path == null) {
			NodeRef entityNodeRef = null;
			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("destination") != null) {
					entityNodeRef = new NodeRef(extras.get("destination"));
				} else if (extras.get("itemId") != null) {
					itemIdNodeRef = new NodeRef(extras.get("itemId"));
					entityNodeRef = nodeService.getPrimaryParent(itemIdNodeRef).getParentRef();
				} else if (extras.get("list") != null) {

					QName dataListQName = QName.createQName(extras.get("list"), namespaceService);

					entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

					if (listContainerNodeRef != null) {
						entityNodeRef = entityListDAO.getList(listContainerNodeRef, dataListQName);
					}

				}
				if (entityNodeRef != null) {
					path = nodeService.getPath(entityNodeRef).toPrefixString(namespaceService);
				}
			}
		}

		query = prepareQuery(query);
		List<NodeRef> ret;

		if (!all) {
			String parent = (String) props.get(ListValueService.PROP_PARENT);
			NodeRef parentNodeRef = (parent != null) && NodeRef.isNodeRef(parent) ? new NodeRef(parent) : null;
			ret = hierarchyService.getHierarchiesByPath(path, parentNodeRef, query);
		} else {

			ret = hierarchyService.getAllHierarchiesByPath(path, query);
		}

		// avoid cycle: when editing an item, cannot select itself as parent
		if ((itemIdNodeRef != null) && ret.contains(itemIdNodeRef)) {
			ret.remove(itemIdNodeRef);
		}

		return new ListValuePage(ret, pageNum, pageSize,
				all ? hierarchyValueExtractor : new NodeRefListValueExtractor(BeCPGModel.PROP_LKV_VALUE, nodeService));
	}

	/**
	 * Suggest list value according to query
	 *
	 * Query path : +PATH:"/app:company_home/cm:System/cm:Lists/cm:Nuts/*"
	 * +TYPE:"bcpg:nut" +@cm\:name:"Nut1*".
	 *
	 * @param path
	 *            the path
	 * @param query
	 *            the query
	 * @return the map
	 */
	private ListValuePage suggestListValue(String path, String query, Integer pageNum, Integer pageSize) {

		logger.debug("suggestListValue");

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();

		queryBuilder.inPath(path);
		queryBuilder.ofType(BeCPGModel.TYPE_LIST_VALUE);

		if (!isAllQuery(query)) {
			queryBuilder.andPropQuery(BeCPGModel.PROP_LV_VALUE, prepareQuery(query));
			queryBuilder.andOperator();
		} else {
			queryBuilder.addSort(BeCPGModel.PROP_LV_VALUE, true);
		}

		

		List<NodeRef> ret = queryBuilder.list();

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(BeCPGModel.PROP_LV_VALUE, nodeService));

	}


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

	private BeCPGQueryBuilder excludeByProp(BeCPGQueryBuilder queryBuilder, String[] arrExcluseProps) {
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

	private BeCPGQueryBuilder excludeByClass(BeCPGQueryBuilder queryBuilder, String[] arrClassNames) {

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

	// TODO duplicate in AbstractExprNameExtractor
	protected String extractExpr(NodeRef nodeRef, String exprFormat) {
		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(exprFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(nodeRef, propQnameAlt);
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(nodeRef, propQname);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	protected String extractPropText(NodeRef nodeRef, String propQname) {
		if (nodeService.getProperty(nodeRef, QName.createQName(propQname, namespaceService)) instanceof List) {
			return ((List<String>) nodeService.getProperty(nodeRef, QName.createQName(propQname, namespaceService))).stream()
					.collect(Collectors.joining(","));
		}
		return (String) nodeService.getProperty(nodeRef, QName.createQName(propQname, namespaceService));
	}

	protected boolean isAllQuery(String query) {
		return BeCPGQueryHelper.isAllQuery(query);
	}
	
	protected String prepareQuery(String query) {
		return BeCPGQueryHelper.prepareQuery(dictionaryService, query);
	}

	public boolean isQueryMatch(String query, String entityName) {

		return BeCPGQueryHelper.isQueryMatch(query, entityName, dictionaryService);
	}

	/**
	 * Suggest a dalist item
	 *
	 * @param entityNodeRef
	 * @param datalistType
	 * @param propertyQName
	 * @param query
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	protected ListValuePage suggestDatalistItem(NodeRef entityNodeRef, QName datalistType, QName propertyQName, String query, Integer pageNum,
			Integer pageSize) {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(datalistType).andPropQuery(propertyQName, prepareQuery(query))
				.inPath(nodeService.getPath(entityNodeRef).toPrefixString(namespaceService) + "/*/*").maxResults(RepoConsts.MAX_SUGGESTIONS);

		PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
		// Tokenised "false" or "both"
		if (propertyDef != null) {

			if (IndexTokenisationMode.BOTH.equals(propertyDef.getIndexTokenisationMode())
					|| IndexTokenisationMode.FALSE.equals(propertyDef.getIndexTokenisationMode()) && isAllQuery(query)) {
				queryBuilder.addSort(propertyQName, true);
			}
		}

		return new ListValuePage(queryBuilder.list(), pageNum, pageSize, new NodeRefListValueExtractor(propertyQName, nodeService));
	}

}
