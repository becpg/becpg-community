package fr.becpg.repo.listvalue;

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
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.listvalue.impl.AbstractBaseListValuePlugin;
import fr.becpg.repo.listvalue.impl.ListValueServiceImpl;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.search.BeCPGSearchService;
import fr.becpg.repo.search.lucene.analysis.FrenchSnowballAnalyserThatRemovesAccents;

@Service
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

	/** The Constant SOURCE_TYPE_PRODUCT. */
	private static final String SOURCE_TYPE_PRODUCT = "product";

	/** The Constant SOURCE_TYPE_LINKED_VALUE. */
	private static final String SOURCE_TYPE_LINKED_VALUE = "linkedvalue";

	/** The Constant SOURCE_TYPE_LIST_VALUE. */
	private static final String SOURCE_TYPE_LIST_VALUE = "listvalue";

	/** The Constant SOURCE_TYPE_PRODUCT_REPORT. */
	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";

	protected static final String PARAM_VALUES_SEPARATOR = ",";

	/** The node service. */
	protected NodeService nodeService;

	/** The namespace service. */
	protected NamespaceService namespaceService;

	/** The product report service. */
	private ReportTplService reportTplService;

	protected BeCPGSearchService beCPGSearchService;

	private DictionaryService dictionaryService;

	private DictionaryDAO dictionaryDAO;

	private AutoNumService autoNumService;

	private Analyzer luceneAnaLyzer = null;

	/**
	 * Sets the namespace service.
	 * 
	 * @param namespaceService
	 *            the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * Sets the node service.
	 * 
	 * @param nodeService
	 *            the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_TARGET_ASSOC, SOURCE_TYPE_PRODUCT, SOURCE_TYPE_LINKED_VALUE,
				SOURCE_TYPE_PRODUCT_REPORT, SOURCE_TYPE_LIST_VALUE };
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

		String path = (String) props.get(ListValueService.PROP_PATH);
		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		String classNames = (String) props.get(ListValueService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;
		String productType = (String) props.get(ListValueService.PROP_PRODUCT_TYPE);

		if (sourceType.equals(SOURCE_TYPE_TARGET_ASSOC)) {
			QName type = QName.createQName(className, namespaceService);
			return suggestTargetAssoc(type, query, pageNum, pageSize, arrClassNames, props);
		} else if (sourceType.equals(SOURCE_TYPE_PRODUCT)) {
			return suggestTargetAssoc(BeCPGModel.TYPE_PRODUCT, query, pageNum, pageSize, arrClassNames, props);
		} else if (sourceType.equals(SOURCE_TYPE_LINKED_VALUE)) {
			return suggestLinkedValue(path, query, pageNum, pageSize, props);
		} else if (sourceType.equals(SOURCE_TYPE_LIST_VALUE)) {
			return suggestListValue(path, query, pageNum, pageSize);
		} else if (sourceType.equals(SOURCE_TYPE_PRODUCT_REPORT)) {
			QName productTypeQName = QName.createQName(productType, namespaceService);
			return suggestProductReportTemplates(productTypeQName, query, pageNum, pageSize);

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
	protected ListValuePage suggestTargetAssoc(QName type, String query, Integer pageNum, Integer pageSize,
			String[] arrClassNames, Map<String, Serializable> props) {

		if (logger.isDebugEnabled()) {
			if (arrClassNames != null) {
				logger.debug("suggestTargetAssoc with arrClassNames : " + Arrays.toString(arrClassNames));
			}
		}

		String queryPath = "";

		// Is code or name search
		if (isQueryCode(query, type, arrClassNames)) {
			query = prepareQueryCode(query, type, arrClassNames);
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_BY_CODE, type, query);
		} else if (isAllQuery(query)) {
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_ALL, type);
		} else {
			query = prepareQuery(query);
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_BY_NAME, type, query);
		}

		// filter by classNames
		queryPath = filterByClass(queryPath, arrClassNames);

		// filter product state
		queryPath += String.format(RepoConsts.QUERY_FILTER_PRODUCT_STATE, SystemState.Archived, SystemState.Refused);

		List<NodeRef> ret = null;

		if (props != null) {
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				String filterByAssoc = (String) extras.get(PROP_FILTER_BY_ASSOC);
				String strAssocNodeRef = (String) props.get(ListValueService.PROP_PARENT);
				if (filterByAssoc != null && filterByAssoc.length() > 0 && strAssocNodeRef != null
						&& strAssocNodeRef.length() > 0) {
					QName assocQName = QName.createQName(filterByAssoc, namespaceService);

					NodeRef nodeRef = new NodeRef(strAssocNodeRef);

					if (nodeService.exists(nodeRef)) {

						List<NodeRef> tmp = beCPGSearchService
								.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_UNLIMITED);

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

		if (ret == null) {
			ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_SUGGESTIONS);
		}

		return new ListValuePage(ret, pageNum, pageSize, new TargetAssocValueExtractor(ContentModel.PROP_NAME,
				nodeService, namespaceService));

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
	 * @return the map
	 */
	private ListValuePage suggestLinkedValue(String path, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

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
					path = path.replace("/app:company_home/", "");
				}
			}
		}

		logger.debug("suggestLinkedValue for path:" + path);

		String queryPath = "";

		String parent = (String) props.get(ListValueService.PROP_PARENT);
		path = LuceneHelper.encodePath(path);
		if (!isAllQuery(query)) {
			query = prepareQuery(query);

			if (parent == null) {
				queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ROOT, path, query);
			} else {
				queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, path, parent, query);
			}
		} else {
			if (parent == null) {
				queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ALL_ROOT, path);
			} else {
				queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ALL, path, parent);
			}
		}

		// avoid cycle: when editing an item, cannot select itself as parent
		if (itemIdNodeRef != null) {
			queryPath += LuceneHelper.getCondEqualID(itemIdNodeRef, Operator.NOT);
		}

		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_SUGGESTIONS);

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

		String queryPath = "";
		path = LuceneHelper.encodePath(path);
		if (!isAllQuery(query)) {
			query = prepareQuery(query);
			queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE, path, query);
		} else {
			queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE_ALL, path);
		}

		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_SUGGESTIONS);

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME,
				nodeService));

	}

	private String prepareQueryCode(String query, QName type, String[] arrClassNames) {
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
					if (BeCPGModel.TYPE_PRODUCT.equals(typeTmp)) {
						for (QName subType : dictionaryService.getSubTypes(typeTmp, true)) {
							if (ret.length() > 0) {
								ret.append(" OR ");
							}
							ret.append(autoNumService.getPrefixedCode(subType, BeCPGModel.PROP_CODE, codeNumber));
						}
					} else {
						if (ret.length() > 0) {
							ret.append(" OR ");
						}
						ret.append(autoNumService.getPrefixedCode(typeTmp, BeCPGModel.PROP_CODE, codeNumber));
					}
				}
				return "(" + ret.toString() + ")";
			}
		}
		return query;
	}

	private boolean isQueryCode(String query, QName type, String[] arrClassNames) {
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
	 * Get the nodeRef of the item by type and name.
	 * 
	 * @param type
	 *            the type
	 * @param name
	 *            the name
	 * @return the item by type and name
	 */

	/**
	 * Get the report templates of the product type that user can choose from
	 * UI.
	 * 
	 * @param systemProductType
	 *            the system product type
	 * @param query
	 *            the query
	 * @return the map
	 */

	private ListValuePage suggestProductReportTemplates(QName nodeType, String query, Integer pageNum, Integer pageSize) {

		query = prepareQuery(query);
		List<NodeRef> tplsNodeRef = reportTplService.suggestUserReportTemplates(ReportType.Document, nodeType, query);

		return new ListValuePage(tplsNodeRef, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME,
				nodeService));
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

	private String filterByClass(String query, String[] arrClassNames) {

		if (arrClassNames != null) {

			String queryClassNames = "";
			boolean isFirst = true;

			for (String className : arrClassNames) {				
				
				QName classQName = QName.createQName(className, namespaceService);
				ClassDefinition classDef = dictionaryService.getClass(classQName);
				LuceneHelper.Operator op = isFirst ? null : LuceneHelper.Operator.OR;
				isFirst = false;

				if(classDef.isAspect()){
					queryClassNames += LuceneHelper.mandatory(LuceneHelper.getCondAspect(classQName));
				}
				else{
					queryClassNames += LuceneHelper.getCond(LuceneHelper.getCondType(classQName),op);
				}				
			}

			query += " AND (" + queryClassNames + ")";
		}

		return query;
	}

	protected boolean isAllQuery(String query) {
		return query != null && query.trim().equals(SUFFIX_ALL);
	}

	boolean isQueryMatch(String query, String entityName) {

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
		String queryPath = "";

		query = prepareQuery(query);
		
		queryPath += LuceneHelper.mandatory(LuceneHelper.getCondType(datalistType));
		queryPath += LuceneHelper.getCondContainsValue(propertyQName, query, Operator.AND);
		queryPath += LuceneHelper.getCond(String.format(" +PATH:\"%s/*/*/*\"", nodeService.getPath(entityNodeRef).toPrefixString(namespaceService)), Operator.AND);		
		
		logger.debug("suggestDatalistItem for query : " + queryPath);

		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, LuceneHelper.getSort(propertyQName), RepoConsts.MAX_SUGGESTIONS);

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(propertyQName, nodeService));
	}
}
