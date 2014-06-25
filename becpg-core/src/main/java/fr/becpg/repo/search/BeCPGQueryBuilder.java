/*
Copyright (C) 2010-2014 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * @author matthieu
 * 
 */
@Service("beCPGQueryBuilder")
public class BeCPGQueryBuilder extends AbstractBeCPGQueryBuilder implements InitializingBean {

	private static Log logger = LogFactory.getLog(BeCPGQueryBuilder.class);

	private static final String DEFAULT_FIELD_NAME = "keywords";

	private static BeCPGQueryBuilder INSTANCE = null;

	@Autowired
	@Qualifier("SearchService")
	private SearchService searchService;
	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	@Qualifier("FileFolderService")
	private FileFolderService fileFolderService;

	@Autowired
	private DictionaryService dictionaryService;

	@Value("${beCPG.defaultSearchTemplate}")
	private String defaultSearchTemplate;

	@Autowired
	private NodeService nodeService;

	private Integer maxResults = RepoConsts.MAX_RESULTS_256;
	private NodeRef parentNodeRef;
	private QName type = null;
	private Set<QName> types = new HashSet<>();
	private Set<QName> aspects = new HashSet<>();
	private String path = null;
	private String excludePath = null;
	private String membersPath = null;
	private Set<NodeRef> ids = new HashSet<>();
	private Set<NodeRef> notIds = new HashSet<>();
	private Set<QName> notNullProps = new HashSet<>();
	private Set<QName> nullProps = new HashSet<>();
	private Map<QName, String> propQueriesMap = new HashMap<QName, String>();
	private Map<QName, Pair<String, String>> propBetweenQueriesMap = new HashMap<QName, Pair<String, String>>();
	private Map<QName, String> propQueriesEqualMap = new HashMap<QName, String>();
	private Set<String> ftsQueries = new HashSet<>();
	private Set<QName> excludedAspects = new HashSet<>();
	private Set<QName> excludedTypes = new HashSet<>();
	private Map<QName, String> excludedPropQueriesMap = new HashMap<QName, String>();
	private QueryConsistency queryConsistancy = QueryConsistency.EVENTUAL;
	private boolean isExactType = false;
	private String searchTemplate = null;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

	}

	private BeCPGQueryBuilder() {
		// Make creation private

	}

	public static BeCPGQueryBuilder createQuery() {
		BeCPGQueryBuilder builder = new BeCPGQueryBuilder();
		if (INSTANCE != null) {
			builder.searchService = INSTANCE.searchService;
			builder.namespaceService = INSTANCE.namespaceService;
			builder.defaultSearchTemplate = INSTANCE.defaultSearchTemplate;
			builder.fileFolderService = INSTANCE.fileFolderService;
			builder.nodeService = INSTANCE.nodeService;
			builder.dictionaryService = INSTANCE.dictionaryService;
		}
		return builder;
	}

	public BeCPGQueryBuilder ofType(QName typeQname) {
		if (this.type != null && !type.equals(typeQname)) {
			logger.warn("Type is already set for this query.( old:" + type + " -  new: " + typeQname + ")");
		}
		this.type = typeQname;

		return this;
	}
	

	public BeCPGQueryBuilder ofExactType(QName typeQname) {
		this.isExactType = true;
		return ofType(typeQname);
	}
	

	public BeCPGQueryBuilder inType(QName typeQname) {
		types.add(typeQname);
		return this;
	}

	public BeCPGQueryBuilder withAspect(QName aspect) {
		excludedAspects.remove(aspect);
		aspects.add(aspect);
		return this;
	}

	public BeCPGQueryBuilder maxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	public BeCPGQueryBuilder parent(NodeRef parentNodeRef) {
		if (this.parentNodeRef != null) {
			logger.warn("ParentNodeRef is already set for this query.( old:" + this.parentNodeRef + " -  new: " + parentNodeRef + ")");
		}

		this.parentNodeRef = parentNodeRef;
		return this;
	}

	public BeCPGQueryBuilder members(String path) {
		if (this.membersPath != null) {
			logger.warn("Path is already set for this query.( old:" + this.membersPath + " -  new: " + path + ")");
		}
		this.membersPath = path;

		return this;
	}

	public BeCPGQueryBuilder inPath(String path) {
		if (this.path != null) {
			logger.warn("Path is already set for this query.( old:" + this.path + " -  new: " + path + ")");
		}
		this.path = path;

		return this;
	}
	
	public BeCPGQueryBuilder excludePath(String excludePath) {
		if (this.path != null) {
			logger.warn("Path is already set for this query.( old:" + this.path + " -  new: " + path + ")");
		}
		if (this.excludePath != null) {
			logger.warn("Exclude Path is already set for this query.( old:" + this.excludePath + " -  new: " + excludePath + ")");
		}
		this.excludePath = excludePath;

		return this;
	}


	public BeCPGQueryBuilder inDB() {
		queryConsistancy = QueryConsistency.TRANSACTIONAL;
		cmisLanguage();
		return this;
	}
	

	public BeCPGQueryBuilder cmisLanguage() {
		this.language = SearchService.LANGUAGE_CMIS_ALFRESCO;
		return this;
	}

	public BeCPGQueryBuilder inSite(String siteId, String containerId) {
		String path = SiteHelper.SITES_SPACE_QNAME_PATH;
		
		if (siteId != null && siteId.length() > 0) {
			path += "cm:" + ISO9075.encode(siteId);
		} else  {
			path += "*";
		}
		if (containerId != null && containerId.length() > 0) {
			path += "/cm:" + ISO9075.encode(containerId);
		}
		// recursive //*
		path += "/";
		inPath(path);

		return this;
	}

	public BeCPGQueryBuilder andID(NodeRef nodeRef) {
		this.ids.add(nodeRef);
		return this;
	}

	public BeCPGQueryBuilder andNotID(NodeRef nodeRef) {
		if (!ids.contains(nodeRef)) {
			this.notIds.add(nodeRef);
		} else {
			logger.warn("Unconsistent search id already in ids : " + nodeRef);
		}
		return this;
	}

	Map<String, Boolean> sortProps = new TreeMap<>();

	public BeCPGQueryBuilder addSort(Map<String, Boolean> sortMap) {
		this.sortProps = sortMap;
		return this;
	}

	public BeCPGQueryBuilder addSort(QName propToSort, boolean sortOrder) {
		sortProps.put(getSortProp(propToSort), sortOrder);
		return this;
	}

	public BeCPGQueryBuilder isNotNull(QName propQName) {
		notNullProps.add(propQName);
		return this;
	}

	public BeCPGQueryBuilder isNull(QName propQName) {
		if (!notNullProps.contains(propQName)) {
			nullProps.add(propQName);
		} else {
			logger.warn("Unconsistent search null prop already in notNullProps : " + propQName);
		}
		return this;
	}

	public BeCPGQueryBuilder andFTSQuery(String ftsQuery) {
		ftsQueries.add(ftsQuery);
		return this;
	}

	public BeCPGQueryBuilder andPropEquals(QName propQName, String value) {
		if (value == null) {
			isNull(propQName);
		} else {
			propQueriesEqualMap.put(propQName, value);
		}
		return this;
	}

	public BeCPGQueryBuilder andPropQuery(QName propQName, String propQuery) {
		if (propQuery == null) {
			isNull(propQName);
		} else {
			propQueriesMap.put(propQName, "(" + propQuery + ")");
		}
		return this;
	}

	public BeCPGQueryBuilder andBetween(QName propQName, String start, String end) {
		propBetweenQueriesMap.put(propQName, new Pair<String, String>(start, end));
		return this;
	}

	public BeCPGQueryBuilder excludeProp(QName propName, String query) {
		excludedPropQueriesMap.put(propName, query);
		return this;
	}

	public BeCPGQueryBuilder excludeType(QName type) {
		if (!types.contains(type) && !type.equals(this.type)) {
			excludedTypes.add(type);
		} else {
			logger.warn("Unconsistent search type already in inType : " + type);
		}
		return this;
	}

	public BeCPGQueryBuilder excludeAspect(QName aspect) {
		if (!aspects.contains(aspect)) {
			excludedAspects.add(aspect);
		} else {
			logger.warn("Unconsistent search aspect already in withAspect : " + aspect);
		}

		return this;
	}

	public BeCPGQueryBuilder excludeVersions() {
		excludeAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION);				
		return this;
	}

	public BeCPGQueryBuilder excludeDefaults() {		
		excludeVersions();
		excludeAspect(BeCPGModel.ASPECT_ENTITY_TPL);
		excludeAspect(BeCPGModel.ASPECT_HIDDEN_FOLDER);
		excludeType(BeCPGModel.TYPE_SYSTEM_ENTITY);			
		excludeAspect(ContentModel.ASPECT_CHECKED_OUT);
		return this;
	}
	
	public BeCPGQueryBuilder excludeSearch() {

		excludeDefaults();
		excludeType(ContentModel.TYPE_THUMBNAIL);
		excludeType(ContentModel.TYPE_FAILED_THUMBNAIL);
		excludeType(ContentModel.TYPE_RATING);
		excludeType(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		excludeType(ReportModel.TYPE_REPORT);
		excludeType(ForumModel.TYPE_FORUM);
		excludeType(RuleModel.TYPE_RULE);
		excludeType(ForumModel.TYPE_POST);
		excludeType(ForumModel.TYPE_FORUMS);
		excludeType(ApplicationModel.TYPE_FILELINK);
		excludeAspect(ContentModel.ASPECT_HIDDEN);

		return this;
	}

	public NodeRef selectNodeByPath(NodeRef parentNodeRef, String xPath) {
		this.maxResults = RepoConsts.MAX_RESULTS_SINGLE_VALUE;
		List<NodeRef> ret = selectNodesByPath(parentNodeRef, xPath);
		return ret != null && !ret.isEmpty() ? ret.get(0) : null;
	}

	public List<NodeRef> selectNodesByPath(NodeRef parentNodeRef, String xPath) {
		List<NodeRef> ret = null;
		StopWatch watch = new StopWatch();
		watch.start();

		try {
			ret = searchService.selectNodes(parentNodeRef, xPath, null, namespaceService, false);
		} finally {
			watch.stop();
			if (watch.getTotalTimeSeconds() > 1) {
				logger.warn("Slow query [" + xPath + "] executed in  " + watch.getTotalTimeSeconds() + " seconds - size results " + ret.size());
			}


		}

		return ret;
	}

	public List<NodeRef> list() {

		StopWatch watch = new StopWatch();
		watch.start();

		List<NodeRef> refs = new LinkedList<NodeRef>();

		String runnedQuery = buildQuery();

		try {

			if (RepoConsts.MAX_RESULTS_UNLIMITED == maxResults) {
				int page = 1;

				logger.info("Unlimited results ask -  start pagination");
				List<NodeRef> tmp = search(runnedQuery, getSort(ContentModel.PROP_MODIFIED, false), page, RepoConsts.MAX_RESULTS_256);

				if (tmp != null && !tmp.isEmpty()) {
					logger.info(" - Page 1:" + tmp.size());
					refs = tmp;
				}
				while (tmp != null && tmp.size() == RepoConsts.MAX_RESULTS_256) {
					page++;
					tmp = search(runnedQuery, getSort(ContentModel.PROP_MODIFIED, false), page, RepoConsts.MAX_RESULTS_256);
					if (tmp != null && !tmp.isEmpty()) {
						logger.info(" - Page " + page + ":" + tmp.size());
						refs.addAll(tmp);
					}
				}

			} else {
				refs = search(runnedQuery, sortProps, -1, maxResults);
			}

		} finally {

			watch.stop();
			if (watch.getTotalTimeSeconds() > 1) {
				logger.warn("Slow query [" + runnedQuery + "] executed in  " + watch.getTotalTimeSeconds() + " seconds - size results " + refs.size());
			}

			if (logger.isDebugEnabled()) {
				int tmpIndex = (RepoConsts.MAX_RESULTS_SINGLE_VALUE == maxResults ? 4 : 3);

				logger.debug("[" + Thread.currentThread().getStackTrace()[tmpIndex].getClassName() + " "
						+ Thread.currentThread().getStackTrace()[tmpIndex].getLineNumber() + "] " + runnedQuery + " executed in  "
						+ watch.getTotalTimeSeconds() + " seconds - size results " + refs.size());
			}
		}

		return refs;
	}

	public NodeRef singleValue() {

		this.maxResults = RepoConsts.MAX_RESULTS_SINGLE_VALUE;
		List<NodeRef> ret = list();

		return ret != null && !ret.isEmpty() ? ret.get(0) : null;
	}

	/*
	 * 1) Lucene's QueryParser class does not parse boolean expressions -- it
	 * might look like it, but it does not. 2) Lucene's BooleanQuery clause does
	 * not model Boolean Queries ... it models aggregate queries. 3) the most
	 * native way to represent the options available in a lucene "BooleanQuery"
	 * as a string is with the +/- prefixes, where... +foo ... means foo is a
	 * required clause and docs must match it -foo ... means foo is prohibited
	 * clause and docs must not match it foo ... means foo is an optional clause
	 * and docs that match it will get score benefits for doing so. 4) in an
	 * attempt to make things easier for people who have simple needs,
	 * QueryParser "fakes" that it parses boolean expressions by interpreting
	 * "A AND B" as "+A +B"; "A OR B" as "A B" and "NOT A" as "-A" 5) if you
	 * change the default operator on QueryParser to be AND then things get more
	 * complicated, mainly because then QueryParser treats "A B" the same as
	 * "+A +B" 6) you should avoid thinking in terms of AND, OR, and NOT ...
	 * think in terms of OPTIONAL, REQUIRED, and PROHIBITED ... your life will
	 * be much easier: documentation will make more sense, conversations on the
	 * email list will be more synergistastic, wine will be sweeter, and food
	 * will taste better.
	 */
	private String buildQuery() {

		StringBuilder runnedQuery = new StringBuilder();

		if (isCmis()) {
			return buildCmisQuery();
		}

		if (parentNodeRef != null) {
			runnedQuery.append(mandatory(getCondParent(parentNodeRef)));
		} else if (membersPath != null) {
			runnedQuery.append(mandatory(getCondMembers(membersPath)));
		} else if (path != null) {
			runnedQuery.append(mandatory(getCondPath(path)));
		} else if (excludePath != null) {
			runnedQuery.append(prohibided(getCondExactPath(excludePath)));
		}

		if (type != null) {
			if(isExactType){
				runnedQuery.append(mandatory(getCondExactType(type)));
			} else {
				runnedQuery.append(mandatory(getCondType(type)));
			}
		}

		if (!types.isEmpty()) {
			if (types.size() == 1) {
				runnedQuery.append(mandatory(getCondType(types.iterator().next())));
			} else {
				runnedQuery.append(mandatory(startGroup()));
				for (QName tmpQName : types) {
					runnedQuery.append(optional(getCondType(tmpQName)));
				}
				runnedQuery.append(endGroup());
			}
		}

		if (!excludedTypes.isEmpty()) {
			for (QName tmpQName : excludedTypes) {
				runnedQuery.append(prohibided(getCondType(tmpQName)));
			}
		}

		if (!aspects.isEmpty()) {
			for (QName tmpQName : aspects) {
				runnedQuery.append(mandatory(getCondAspect(tmpQName)));
			}
		}

		if (!excludedAspects.isEmpty()) {
			for (QName tmpQName : excludedAspects) {
				runnedQuery.append(prohibided(getCondAspect(tmpQName)));
			}
		}

		if (!notNullProps.isEmpty()) {
			for (QName tmpQName : notNullProps) {
				runnedQuery.append(prohibided(getCondIsNullValue(tmpQName)));
			}
		}

		if (!nullProps.isEmpty()) {
			for (QName tmpQName : nullProps) {
				runnedQuery.append(mandatory(getCondIsNullValue(tmpQName)));
			}
		}

		if (!ids.isEmpty()) {
			for (NodeRef tmpNodeRef : ids) {
				runnedQuery.append(mandatory(getCondEqualID(tmpNodeRef)));
			}
		}

		if (!notIds.isEmpty()) {
			for (NodeRef tmpNodeRef : notIds) {
				runnedQuery.append(prohibided(getCondEqualID(tmpNodeRef)));
			}
		}

		if (!propQueriesMap.isEmpty()) {
			for (Map.Entry<QName, String> propQueryEntry : propQueriesMap.entrySet()) {
				runnedQuery.append(mandatory(getCondContainsValue(propQueryEntry.getKey(), propQueryEntry.getValue())));
			}
		}

		if (!propQueriesEqualMap.isEmpty()) {
			for (Map.Entry<QName, String> propQueryEntry : propQueriesEqualMap.entrySet()) {
				runnedQuery.append(equalsQuery(getCondContainsValue(propQueryEntry.getKey(), "\"" + propQueryEntry.getValue() + "\"")));
			}
		}

		if (!excludedPropQueriesMap.isEmpty()) {
			for (Map.Entry<QName, String> propQueryEntry : excludedPropQueriesMap.entrySet()) {
				runnedQuery.append(prohibided(getCondContainsValue(propQueryEntry.getKey(), propQueryEntry.getValue())));
			}
		}

		for (Map.Entry<QName, Pair<String, String>> propQueryEntry : propBetweenQueriesMap.entrySet()) {
			runnedQuery.append(mandatory(getCondContainsValue(propQueryEntry.getKey(),
					String.format("[%s TO %s]", propQueryEntry.getValue().getFirst(), propQueryEntry.getValue().getSecond()))));
		}

		if (!ftsQueries.isEmpty()) {
			runnedQuery.append(mandatory(startGroup()));
			for (String ftsQuery : ftsQueries) {
				runnedQuery.append(optional(ftsQuery));
			}
			runnedQuery.append(endGroup());
		}

		String ret = runnedQuery.toString();

		if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(language)) {
			if (ret.startsWith(" AND")) {
				return ret.replaceFirst(" AND", "");
			}
		}

		return ret;
	}

	private String buildCmisQuery() {
		StringBuilder runnedQuery = new StringBuilder();
		StringBuilder orderBy = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		
		runnedQuery.append("SELECT  * FROM ");
		if (type == null || ContentModel.TYPE_CONTENT.equals(type)) {
			runnedQuery.append("cmis:document as D");
		} else {
			runnedQuery.append(type.toPrefixString(namespaceService) + " as D");
		}

		

		if (parentNodeRef != null) {
			whereClause.append(" AND IN_FOLDER( D,'" + parentNodeRef + "')");
		} else if (membersPath != null) {
			throw new IllegalStateException("members not supported for CMIS search");
		} else if (path != null) {
			throw new IllegalStateException("path not supported for CMIS search");
		}

		if (!types.isEmpty()) {
			throw new IllegalStateException("only one type supported for CMIS search");
		}

		if (!excludedTypes.isEmpty()) {
			throw new IllegalStateException("only one type supported for CMIS search");
		}

		if (!excludedAspects.isEmpty()) {
			throw new IllegalStateException("excludedAspects supported not for CMIS search");
		}

		if (!notNullProps.isEmpty()) {
			for (QName tmpQName : notNullProps) {
				whereClause.append(" AND " + getCmisPrefix(tmpQName) + " IS NOT NULL");
			}

		}

		if (!nullProps.isEmpty()) {
			for (QName tmpQName : nullProps) {
				whereClause.append(" AND " + getCmisPrefix(tmpQName) + " IS NULL");
			}
		}

		if (!ids.isEmpty()) {
			for (NodeRef tmpNodeRef : ids) {
				whereClause.append(" AND D.cmis:objectId = '" + tmpNodeRef + "'");
			}
		}

		if (!notIds.isEmpty()) {
			for (NodeRef tmpNodeRef : notIds) {
				whereClause.append(" AND D.cmis:objectId <> '" + tmpNodeRef + "'");
			}
		}

		if (!propQueriesMap.isEmpty()) {
			throw new IllegalStateException("property contains not supported yet");
		}

		if (!propQueriesEqualMap.isEmpty()) {
			for (Map.Entry<QName, String> propQueryEntry : propQueriesEqualMap.entrySet()) {
				whereClause.append(" AND " + getCmisPrefix(propQueryEntry.getKey()) + " = '" + propQueryEntry.getValue() + "'");
			}
		}

		if (!excludedPropQueriesMap.isEmpty()) {
			for (Map.Entry<QName, String> propQueryEntry : excludedPropQueriesMap.entrySet()) {
				whereClause.append(" AND " + getCmisPrefix(propQueryEntry.getKey()) + " <> '" + propQueryEntry.getValue() + "'");
			}
		}

		for (Map.Entry<QName, Pair<String, String>> propQueryEntry : propBetweenQueriesMap.entrySet()) {

			String first = propQueryEntry.getValue().getFirst();
			String second = propQueryEntry.getValue().getSecond();
			if (!"MIN".equals(first)) {
				whereClause.append(" AND " + getCmisPrefix(propQueryEntry.getKey()) + " >= " + first + "");
			}
			if (!"MAX".equals(second)) {
				whereClause.append(" AND " + getCmisPrefix(propQueryEntry.getKey()) + " <= " + second + "");
			}
		}

		if (!ftsQueries.isEmpty()) {
			throw new IllegalStateException("fts contains not supported yet");
		}

		if (sortProps != null && ! sortProps.isEmpty()) {
			orderBy.append(" ORDER BY");
			for (Map.Entry<String, Boolean> kv : sortProps.entrySet()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Add sort :" + kv.getKey() + " " + kv.getValue());
				}
				orderBy.append(" "+getCmisPrefix(QName.createQName(kv.getKey().replaceFirst("@", ""))) +(kv.getValue()?" ASC": " DESC"));
			}
		}
		
		String ret = whereClause.toString();

		if (ret.startsWith(" AND")) {
			ret = ret.replaceFirst(" AND", "");
		}

		if (ret.length() > 0) {
			ret = " WHERE " + ret;
		}

		if (!aspects.isEmpty()) {
			for (QName tmpQName : aspects) {
				runnedQuery.append(" JOIN " + tmpQName.toPrefixString(namespaceService) + " as " + tmpQName.getLocalName() + " on D.cmis:objectId = "
						+ tmpQName.getLocalName() + ".cmis:objectId");
			}
		}
		
	
		
		return runnedQuery.toString() + ret+ orderBy.toString();

	}

	private String getCmisPrefix(QName tmpQName) {
		String ret = tmpQName.toPrefixString(namespaceService);
		if (dictionaryService.getProperty(tmpQName).getContainerClass().isAspect()) {
			QName aspect = dictionaryService.getProperty(tmpQName).getContainerClass().getName();
			this.aspects.add(aspect);
			ret = aspect.getLocalName() + "." + ret;
		} else {
			if(tmpQName.equals(ContentModel.PROP_NAME)){
				ret = "cmis:name";
			} else if(tmpQName.equals(ContentModel.PROP_CREATED)){
				ret = "cmis:createdDate";
			} else if(tmpQName.equals(ContentModel.PROP_CREATOR)){
				ret = "cmis:createdBy";
			} else if(tmpQName.equals(ContentModel.PROP_MODIFIED)){
				ret = "cmis:lastModifiedDate";
			} else if(tmpQName.equals(ContentModel.PROP_MODIFIER)){
				ret = "cmis:lastModifiedBy";
			}
			ret = "D." + ret;
		}

		return ret;
	}

	private boolean isCmis() {
		return SearchService.LANGUAGE_CMIS_ALFRESCO.equals(language);
	}


	public BeCPGQueryBuilder inSearchTemplate(String searchTemplate) {
		this.searchTemplate = searchTemplate;
		return this;
	}
	
	public BeCPGQueryBuilder ftsLanguage() {
		this.language = SearchService.LANGUAGE_FTS_ALFRESCO;
		return this;
	}

	private List<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int page, int maxResults) {

		List<NodeRef> nodes = new LinkedList<NodeRef>();

		SearchParameters sp = new SearchParameters();
		sp.addStore(RepoConsts.SPACES_STORE);

		sp.setQuery(runnedQuery);
		sp.addLocale(Locale.getDefault());
		sp.excludeDataInTheCurrentTransaction(true);

		sp.setLanguage(language);

		if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(language)) {
			sp.setDefaultFieldName(DEFAULT_FIELD_NAME);
			if(searchTemplate!=null){
				sp.addQueryTemplate(DEFAULT_FIELD_NAME, searchTemplate);
			} else {
				sp.addQueryTemplate(DEFAULT_FIELD_NAME, defaultSearchTemplate);
			}
		}

		// Force the database use if possible
		// execute queries transactionally, when possible, and fall back to
		// eventual consistency; or
		sp.setQueryConsistency(queryConsistancy);

		if (QueryConsistency.TRANSACTIONAL.equals(queryConsistancy)) {
			logger.trace("Transactionnal Search");
			// Will ensure coherency between solr and lucene
			sp.excludeDataInTheCurrentTransaction(false);
		}

		if (maxResults == RepoConsts.MAX_RESULTS_UNLIMITED) {
			sp.setLimitBy(LimitBy.UNLIMITED);
		} else {
			// if (isDBSearch() && notIds.size() > 0) {
			// maxResults = maxResults + notIds.size();
			// }
			sp.setLimit(maxResults);
			sp.setMaxItems(maxResults);
			sp.setLimitBy(LimitBy.FINAL_SIZE);
		}

		if (page > 0) {
			sp.setSkipCount((page - 1) * maxResults);
			sp.setMaxPermissionChecks(page * 1000);
		}

		if (sort != null && !isCmis()) {
			for (Map.Entry<String, Boolean> kv : sort.entrySet()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Add sort :" + kv.getKey() + " " + kv.getValue());
				}
				sp.addSort(kv.getKey(), kv.getValue());
			}
		}

		ResultSet result = null;
		try {
			result = searchService.query(sp);
			if (result != null) {
				nodes = new LinkedList<NodeRef>(result.getNodeRefs());
			}
		} catch (FTSQueryException e) {
			logger.error("Incorrect query :" + runnedQuery, e);
		} catch (QueryModelException e) {
			logger.error("Incorrect query :" + runnedQuery, e);

		} finally {
			if (result != null) {
				result.close();
			}
		}
		//
		// if (isDBSearch() && notIds.size() > 0) {
		// logger.trace("Exclude notIds from results");
		// nodes.removeAll(notIds);
		// if (maxResults != RepoConsts.MAX_RESULTS_UNLIMITED) {
		// return nodes.subList(0, Math.min(nodes.size(), maxResults -
		// notIds.size()));
		// }
		//
		// if (!propBetweenQueriesMap.isEmpty()) {
		// logger.trace("Exclude between query from search");
		// for (Iterator<NodeRef> iterator = nodes.iterator();
		// iterator.hasNext();) {
		// NodeRef nodeRef = (NodeRef) iterator.next();
		// boolean toRemove = false;
		// for (Map.Entry<QName, Pair<String, String>> propQueryEntry :
		// propBetweenQueriesMap.entrySet()) {
		// Object prop = nodeService.getProperty(nodeRef,
		// propQueryEntry.getKey());
		//
		// toRemove = true;
		// if (prop != null) {
		// if (prop instanceof Integer) {
		// String first = propQueryEntry.getValue().getFirst();
		// String second = propQueryEntry.getValue().getSecond();
		// if (("MIN".equals(first) || Integer.parseInt(first) < (Integer) prop)
		// && ("MAX".equals(second) || Integer.parseInt(second) > (Integer)
		// prop)) {
		// toRemove = false;
		// } else {
		// logger.debug("Prop " + prop + " not match range " +
		// String.format("[%s TO %s]", first, second));
		// }
		// } else {
		// logger.info("Not supported between type :" +
		// prop.getClass().getSimpleName());
		// toRemove = false;
		// }
		//
		// } else if ("MIN".equals(propQueryEntry.getValue().getFirst())) {
		// toRemove = false;
		// }
		//
		// }
		// if (toRemove) {
		// iterator.remove();
		// }
		// }
		//
		// }
		//
		// }

		return nodes;
	}

	public PagingResults<FileInfo> childFileFolders(PagingRequest pageRequest) {

		StopWatch watch = new StopWatch();
		watch.start();

		PagingResults<FileInfo> pageOfNodeInfos = null;

		FileFilterMode.setClient(Client.script);

		List<Pair<QName, Boolean>> tmp = new LinkedList<Pair<QName, Boolean>>();

		for (Map.Entry<String, Boolean> entry : sortProps.entrySet()) {
			tmp.add(new Pair<QName, Boolean>(QName.createQName(entry.getKey().replace("@", ""), namespaceService), entry.getValue()));
		}

		try {
			pageOfNodeInfos = fileFolderService.list(parentNodeRef, true, false, null, excludedTypes, tmp, pageRequest);

		} finally {
			FileFilterMode.clearClient();
			watch.stop();

			if (pageOfNodeInfos != null) {
				if (watch.getTotalTimeSeconds() > 1) {
					logger.warn("Slow childFileFolders [" + parentNodeRef + "] executed in  " + watch.getTotalTimeSeconds()
							+ " seconds - size results " + pageOfNodeInfos.getTotalResultCount());
				}

				if (logger.isDebugEnabled()) {

					logger.debug("[" + Thread.currentThread().getStackTrace()[3].getClassName() + " "
							+ Thread.currentThread().getStackTrace()[3].getLineNumber() + "] childFileFolders executed in  "
							+ watch.getTotalTimeSeconds() + " seconds - size results " + pageOfNodeInfos.getTotalResultCount());
				}
			}

		}
		return pageOfNodeInfos;
	}

	@Override
	public String toString() {
		return buildQuery();
	}

	

}
