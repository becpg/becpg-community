/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.lucene.analysis.FrenchSnowballAnalyserThatRemovesAccents;

public class EntityListValuePlugin extends AbstractBaseListValuePlugin {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceImpl.class);

	/** The Constant SUFFIX_ALL. */
	protected static final String SUFFIX_ALL = "*";

	/** The Constant SUFFIX_SPACE. */
	private static final String SUFFIX_SPACE = " ";

	/** The Constant SUFFIX_DOUBLE_QUOTE. */
	private static final String SUFFIX_DOUBLE_QUOTE = "\"";

	/** The Constant SUFFIX_SIMPLE_QUOTE. */
	private static final String SUFFIX_SIMPLE_QUOTE = "'";

	private static final String PROP_FILTER_BY_ASSOC = "filterByAssoc";

	/** The Constant SOURCE_TYPE_TARGET_ASSOC. */
	private static final String SOURCE_TYPE_TARGET_ASSOC = "targetassoc";


	/** The Constant SOURCE_TYPE_LINKED_VALUE. */
	private static final String SOURCE_TYPE_LINKED_VALUE = "linkedvalue";
	
	private static final String SOURCE_TYPE_LINKED_VALUE_ALL = "allLinkedvalue";

	/** The Constant SOURCE_TYPE_LIST_VALUE. */
	private static final String SOURCE_TYPE_LIST_VALUE = "listvalue";


	protected static final String PARAM_VALUES_SEPARATOR = ",";

	/** The node service. */
	protected NodeService nodeService;

	/** The namespace service. */
	protected NamespaceService namespaceService;


	protected DictionaryService dictionaryService;

	private DictionaryDAO dictionaryDAO;

	protected AutoNumService autoNumService;

	private Analyzer luceneAnaLyzer = null;
	
	private HierarchyService hierarchyService;
	
	private TargetAssocValueExtractor targetAssocValueExtractor;

	
	public void setTargetAssocValueExtractor(TargetAssocValueExtractor targetAssocValueExtractor) {
		this.targetAssocValueExtractor = targetAssocValueExtractor;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}


	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_TARGET_ASSOC, SOURCE_TYPE_LINKED_VALUE, SOURCE_TYPE_LINKED_VALUE_ALL,SOURCE_TYPE_LIST_VALUE };
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

		String path = (String) props.get(ListValueService.PROP_PATH);
		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		String classNames = (String) props.get(ListValueService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;

		if (sourceType.equals(SOURCE_TYPE_TARGET_ASSOC)) {
			QName type = QName.createQName(className, namespaceService);
			return suggestTargetAssoc(type, query, pageNum, pageSize, arrClassNames, props);
		} else if (sourceType.equals(SOURCE_TYPE_LINKED_VALUE)) {
			return suggestLinkedValue(path, query, pageNum, pageSize, props, false);
		} else if (sourceType.equals(SOURCE_TYPE_LINKED_VALUE_ALL)) {
			return suggestLinkedValue(path, query, pageNum, pageSize, props, true);
		} else if (sourceType.equals(SOURCE_TYPE_LIST_VALUE)) {
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
	public ListValuePage suggestTargetAssoc(QName type, String query, Integer pageNum, Integer pageSize,
			String[] arrClassNames, Map<String, Serializable> props) {

		if (logger.isDebugEnabled()) {
			if (arrClassNames != null) {
				logger.debug("suggestTargetAssoc with arrClassNames : " + Arrays.toString(arrClassNames));
			}
		}
		

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();
		queryBuilder.ofType(type);
		queryBuilder.excludeVersions();
		
		// Is code or name search
		if (isQueryCode(query, type, arrClassNames)) {
			String codeQuery = prepareQueryCode(query, type, arrClassNames);
			
			//TODO erpCOde and eanCode not in COre
			queryBuilder.andFTSQuery(String.format("(@bcpg\\:code:%s OR @bcpg\\:erpCode:%s OR @bcpg\\:eanCode:%s)", codeQuery,codeQuery,codeQuery));
			
			
		} else if (!isAllQuery(query)) { 
			queryBuilder.andPropQuery(ContentModel.PROP_NAME, query);
		}

		// filter by classNames
		filterByClass(queryBuilder, arrClassNames);

		// filter product state
		//TODO queryPath += String.format(RepoConsts.QUERY_FILTER_PRODUCT_STATE, SystemState.Archived, SystemState.Refused);

		List<NodeRef> ret = null;

		if (props != null) {
			
			// exclude class
			String excludeClassNames = (String) props.get(ListValueService.PROP_EXCLUDE_CLASS_NAMES);
			String[] arrExcludeClassNames = excludeClassNames != null ? excludeClassNames.split(PARAM_VALUES_SEPARATOR) : null;
			excludeByClass(queryBuilder, arrExcludeClassNames);
			
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				String filterByAssoc = (String) extras.get(PROP_FILTER_BY_ASSOC);
				String strAssocNodeRef = (String) props.get(ListValueService.PROP_PARENT);
				if (filterByAssoc != null && filterByAssoc.length() > 0 && strAssocNodeRef != null
						&& strAssocNodeRef.length() > 0) {
					QName assocQName = QName.createQName(filterByAssoc, namespaceService);

					NodeRef nodeRef = new NodeRef(strAssocNodeRef);

					if (nodeService.exists(nodeRef)) {

						List<NodeRef> tmp = queryBuilder.list();

						List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, assocQName);

						List<NodeRef> nodesToKeep = new ArrayList<NodeRef>();
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

		return new ListValuePage(ret, pageNum, pageSize, targetAssocValueExtractor);

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
	private ListValuePage suggestLinkedValue(String path, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props, boolean all) {

		NodeRef itemIdNodeRef = null;

		if (path == null) {
			NodeRef entityNodeRef = null;
			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("destination") != null) {
					entityNodeRef = new NodeRef((String) extras.get("destination"));
				} else if (extras.get("itemId") != null) {
					itemIdNodeRef = new NodeRef((String) extras.get("itemId"));
					entityNodeRef = nodeService.getPrimaryParent(itemIdNodeRef).getParentRef();
				}
				if (entityNodeRef != null) {
					path = nodeService.getPath(entityNodeRef).toPrefixString(namespaceService);
				}
			}
		}

		query = prepareQuery(query);	
		List<NodeRef> ret = null;
		
		if(!all){
			String parent = (String) props.get(ListValueService.PROP_PARENT);
			NodeRef parentNodeRef = parent != null && NodeRef.isNodeRef(parent) ? new NodeRef(parent) : null;
			ret = hierarchyService.getHierarchiesByPath(path, parentNodeRef, query);
		} else {
			ret = hierarchyService.getAllHierarchiesByPath(path, query);
		}

		// avoid cycle: when editing an item, cannot select itself as parent
		if (itemIdNodeRef != null && ret.contains(itemIdNodeRef)) {
			ret.remove(itemIdNodeRef);
		}

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(BeCPGModel.PROP_LKV_VALUE,
				nodeService));
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
			//TODO merge with philippe use value !!!
			queryBuilder.andPropQuery(ContentModel.PROP_NAME, prepareQuery(query));
			
		}

		List<NodeRef> ret = queryBuilder.list();

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME,
				nodeService));

	}

	protected String prepareQueryCode(String query, QName type, String[] arrClassNames) {
		if (Pattern.matches(RepoConsts.REGEX_NON_NEGATIVE_INTEGER_FIELD, query)) {
			Long codeNumber = null;
			try {
				codeNumber = Long.parseLong(query);
			} catch (NumberFormatException e) {
				logger.debug(e, e);
			}

			if (codeNumber != null) {
				List<QName> types = new ArrayList<QName>();
				if (arrClassNames != null && arrClassNames.length > 0) {
					for (int i = 0; i < arrClassNames.length; i++) {
						types.add(QName.createQName(arrClassNames[i], namespaceService));
					}
				} else {
					types.add(type);
				}

				StringBuffer ret = new StringBuffer();
				for (QName typeTmp : types) {
					
						if (ret.length() > 0) {
							ret.append(" OR ");
						}
						ret.append(autoNumService.getPrefixedCode(typeTmp, BeCPGModel.PROP_CODE, codeNumber));
				}
				return "(" + ret.toString() + ")";
			}
		}
		return query;
	}

	protected boolean isQueryCode(String query, QName type, String[] arrClassNames) {
		boolean ret = Pattern.matches(RepoConsts.REGEX_NON_NEGATIVE_INTEGER_FIELD, query);
		if (arrClassNames != null) {
			for (int i = 0; i < arrClassNames.length; i++) {
				QName filteredType = QName.createQName(arrClassNames[i], namespaceService);
				ret = ret
						|| Pattern.matches(autoNumService.getAutoNumMatchPattern(filteredType, BeCPGModel.PROP_CODE),
								query);			
			}
		} else {
			ret = ret || Pattern.matches(autoNumService.getAutoNumMatchPattern(type, BeCPGModel.PROP_CODE), query);
		}

		return ret;
	}

	



	/**
	 * Prepare query. //TODO escape + - && || ! ( ) { } [ ] ^ " ~ * ? : \
	 * 
	 * @param query
	 *            the query
	 * @return the string
	 * @throws IOException
	 */
	protected String prepareQuery(String query) {

		logger.debug("Query before prepare:" + query);
		if (query != null
				&& !(query.endsWith(SUFFIX_ALL) || query.endsWith(SUFFIX_SPACE) || query.endsWith(SUFFIX_DOUBLE_QUOTE) || query
						.endsWith(SUFFIX_SIMPLE_QUOTE))) {
			// Query with wildcard are not getting analyzed by stemmers
			// so do it manually
			Analyzer analyzer = getTextAnalyzer();

			if (logger.isDebugEnabled()) {
				logger.debug("Using analyzer : " + analyzer.getClass().getName());
			}
			TokenStream source = null;
			Reader reader = null;
			try {

				reader = new StringReader(query);

				if (analyzer instanceof FrenchSnowballAnalyserThatRemovesAccents) {
					source = ((FrenchSnowballAnalyserThatRemovesAccents) analyzer).tokenStream(null, reader, true);
				} else {
					source = analyzer.tokenStream(null, reader);
				}

				StringBuffer buff = new StringBuffer();
				Token reusableToken = new Token();
				while ((reusableToken = source.next(reusableToken)) != null) {
					if (buff.length() > 0) {
						buff.append(' ');
					}
					buff.append(reusableToken.term());
				}
				source.reset();

				buff.append(SUFFIX_ALL);
				query = buff.toString();
			} catch (Exception e) {
				logger.error(e, e);
			} finally {

				try {
					if (source != null) {
						source.close();
					}

				} catch (IOException e) {
					// Nothing todo here
					logger.error(e, e);
				}

			}

		}

		logger.debug("Query after prepare:" + query);

		return query;
	}

	protected Analyzer getTextAnalyzer() {
		if (luceneAnaLyzer == null) {
			DataTypeDefinition def = dictionaryDAO.getDataType(DataTypeDefinition.TEXT);

			try {
				return (Analyzer) Class.forName(def.resolveAnalyserClassName()).newInstance();
			} catch (Exception e) {
				logger.error(e, e);
				return new fr.becpg.repo.search.lucene.analysis.FrenchSnowballAnalyserThatRemovesAccents();
			}
		}
		return luceneAnaLyzer;
	}

	private BeCPGQueryBuilder filterByClass(BeCPGQueryBuilder queryBuilder, String[] arrClassNames) {

		if (arrClassNames != null) {

			for (String className : arrClassNames) {				
				
				QName classQName = QName.createQName(className, namespaceService);
				ClassDefinition classDef = dictionaryService.getClass(classQName);
				if(classDef.isAspect()){
					
					queryBuilder.withAspect(classQName);
				}
				else{
					queryBuilder.inType(classQName);
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

				if(classDef.isAspect()) {
					queryBuilder.excludeAspect(classQName);
				} else {
					queryBuilder.excludeType(classQName);
				}
						
			}
		}

		return queryBuilder;
	}

	protected boolean isAllQuery(String query) {
		return query != null && query.trim().equals(SUFFIX_ALL);
	}

	public boolean isQueryMatch(String query, String entityName) {

		if (query != null) {

			if (SUFFIX_ALL.equals(query)) {
				return true;
			}

			Analyzer analyzer = getTextAnalyzer();

			if (logger.isDebugEnabled()) {
				logger.debug("Using analyzer : " + analyzer.getClass().getName());
			}
			TokenStream querySource = null;
			Reader queryReader = null;
			TokenStream productNameSource = null;
			Reader productNameReader = null;
			try {

				queryReader = new StringReader(query);
				productNameReader = new StringReader(entityName);
				querySource = analyzer.tokenStream(null, queryReader);
				productNameSource = analyzer.tokenStream(null, productNameReader);

				Token reusableToken = new Token();
				boolean match = true;
				while ((reusableToken = querySource.next(reusableToken)) != null) {
					Token tmpToken = new Token();
					while ((tmpToken = productNameSource.next(tmpToken)) != null) {
						match = false;
						if (logger.isDebugEnabled()) {
							logger.debug("Test StartWith : " + reusableToken.term() + " with " + tmpToken.term());
						}

						if (tmpToken.term().startsWith(reusableToken.term())) {
							match = true;
							break;
						}
					}
					if (!match) {
						break;
					}
				}
				querySource.reset();
				productNameSource.reset();
				return match;
			} catch (Exception e) {
				logger.error(e, e);
			} finally {

				try {
					if (querySource != null) {
						querySource.close();
					}
					if (productNameSource != null) {
						productNameSource.close();
					}

				} catch (IOException e) {
					// Nothing todo here
					logger.error(e, e);
				}

			}

		}

		return false;
	}
	
	/**
	 * Suggest a dalist item
	 * @param entityNodeRef
	 * @param datalistType
	 * @param propertyQName
	 * @param query
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	protected ListValuePage suggestDatalistItem(NodeRef entityNodeRef, QName datalistType, QName propertyQName, String query, Integer pageNum, Integer pageSize) {
		
		List<NodeRef> ret =	BeCPGQueryBuilder.createQuery().ofType(datalistType)
				   .andPropQuery(propertyQName,prepareQuery(query))
				   .inPath(nodeService.getPath(entityNodeRef).toPrefixString(namespaceService))
				   .addSort(propertyQName,true)
				   .maxResults(RepoConsts.MAX_SUGGESTIONS).list();

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(propertyQName, nodeService));
	}
}
