/*
Copyright (C) 2010-2021 beCPG.

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.model.filefolder.GetChildrenCannedQueryFactory;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionCheckedValue.PermissionCheckedValueMixin;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.model.Repository;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;
import fr.becpg.repo.system.SystemConfigurationService;
import jakarta.annotation.Nonnull;

/**
 * <p>
 * BeCPGQueryBuilder class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("beCPGQueryBuilder")
public class BeCPGQueryBuilder extends AbstractBeCPGQueryBuilder implements InitializingBean {

	private static final Log logger = LogFactory.getLog(BeCPGQueryBuilder.class);

	private static final String DEFAULT_FIELD_NAME = "keywords";

	private static final String CANNED_QUERY_FILEFOLDER_LIST = "fileFolderGetChildrenCannedQueryFactory";
	
	private static final String ENABLE_INDEX_TYPES_KEY = "beCPG.solr.enableIndexForTypes";

	private static BeCPGQueryBuilder INSTANCE = null;

	@Autowired
	@Qualifier("SearchService")
	private SearchService searchService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	@Qualifier("fileFolderCannedQueryRegistry")
	private NamedObjectRegistry<CannedQueryFactory<NodeRef>> cannedQueryRegistry;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	private Boolean includeReportInSearch() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.report.includeReportInSearch"));
	}

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private Repository repository;

	private Integer maxResults = RepoConsts.MAX_RESULTS_256;
	private Integer page = -1;
	private NodeRef parentNodeRef;
	private final Set<NodeRef> parentNodeRefs = new HashSet<>();
	private QName type = null;
	private final Set<QName> types = new HashSet<>();
	private final Set<Pair<QName, Integer>> boostedTypes = new HashSet<>();
	
 	private Set<QName> typesToExcludeFromIndex = new HashSet<>();

	private final Set<QName> aspects = new HashSet<>();
	private String subPath = null;
	private String path = null;
	private String inSite = null;
	private String excludePath = null;
	private String membersPath = null;
	private final Set<NodeRef> ids = new HashSet<>();
	private final Set<NodeRef> notIds = new HashSet<>();
	private final Set<QName> notNullProps = new HashSet<>();
	private final Set<QName> nullProps = new HashSet<>();
	private final Set<QName> nullOrUnsetProps = new HashSet<>();

	private final Map<QName, String> propQueriesMap = new HashMap<>();
	private final Map<QName, Pair<String, String>> propBetweenQueriesMap = new HashMap<>();
	private final Map<QName, Pair<String, String>> propOrBetweenQueriesMap = new HashMap<>();
	private final Map<QName, Pair<String, String>> propBetweenOrNullQueriesMap = new HashMap<>();
	private final Map<QName, String> propQueriesEqualMap = new HashMap<>();
	private final Set<String> ftsQueries = new HashSet<>();
	private final Set<QName> excludedAspects = new HashSet<>();
	private final Set<QName> excludedTypes = new HashSet<>();
	private final Map<QName, List<String>> excludedPropQueriesMap = new HashMap<>();
	private QueryConsistency queryConsistancy = QueryConsistency.DEFAULT;
	private boolean isExactType = false;
	private String searchTemplate = null;
	private SearchParameters.Operator operator = null;
	private Locale locale = Locale.getDefault();
	private StoreRef store = RepoConsts.SPACES_STORE;

	private String defaultSearchTemplate() {
		return systemConfigurationService.confValue("beCPG.defaultSearchTemplate");
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

	}

	/**
	 * <p>
	 * isInit.
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean isInit() {
		return INSTANCE != null;
	}

	private BeCPGQueryBuilder() {
		// Make creation private

	}
	
	
	public void setTypesToExcludeFromIndex(Set<QName> typesToExcludeFromIndex) {
		this.typesToExcludeFromIndex = typesToExcludeFromIndex;
	}

	public static Set<QName> getTypesExcludedFromIndex() {
		return INSTANCE.typesToExcludeFromIndex.stream()
	            .filter(nodeType -> !INSTANCE.systemConfigurationService.confValue(ENABLE_INDEX_TYPES_KEY)
	                    .contains(nodeType.toPrefixString(INSTANCE.namespaceService)))
	            .collect(Collectors.toSet());
	}
	
	public static boolean isExcludedFromIndex(QName type) {
		return getTypesExcludedFromIndex().contains(type);
	}

	/**
	 * <p>
	 * createQuery.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public static BeCPGQueryBuilder createQuery() {
		BeCPGQueryBuilder builder = new BeCPGQueryBuilder();
		if (INSTANCE != null) {
			builder.searchService = INSTANCE.searchService;
			builder.namespaceService = INSTANCE.namespaceService;
			builder.cannedQueryRegistry = INSTANCE.cannedQueryRegistry;
			builder.nodeService = INSTANCE.nodeService;
			builder.entityDictionaryService = INSTANCE.entityDictionaryService;
			builder.tenantService = INSTANCE.tenantService;
			builder.systemConfigurationService = INSTANCE.systemConfigurationService;
			builder.repository = INSTANCE.repository;
		}
		return builder;
	}

	public BeCPGQueryBuilder inStore(StoreRef store) {
		this.store = store;
		return this;
	}
	
	/**
	 * <p>
	 * ofType.
	 * </p>
	 *
	 * @param typeQname
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder ofType(QName typeQname) {
		this.type = typeQname;
		return this;
	}

	/**
	 * <p>
	 * ofExactType.
	 * </p>
	 *
	 * @param typeQname
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder ofExactType(QName typeQname) {
		this.isExactType = true;
		return ofType(typeQname);
	}

	/**
	 * <p>
	 * inType.
	 * </p>
	 *
	 * @param typeQname
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inType(QName typeQname) {
		if (!typeQname.equals(type)) {
			types.add(typeQname);
		}

		return this;
	}

	/**
	 * <p>
	 * inBoostedType.
	 * </p>
	 *
	 * @param typeQname
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param boostFactor
	 *            a {@link java.lang.Integer} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inBoostedType(QName typeQname, Integer boostFactor) {
		if (!typeQname.equals(type)) {
			type = null;
		}
		boostedTypes.add(new Pair<>(typeQname, boostFactor));
		return this;
	}

	/**
	 * <p>
	 * withAspect.
	 * </p>
	 *
	 * @param aspect
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder withAspect(QName aspect) {
		excludedAspects.remove(aspect);
		aspects.add(aspect);
		return this;
	}

	/**
	 * <p>
	 * includeAspect.
	 * </p>
	 *
	 * @param aspect
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder includeAspect(QName aspect) {
		excludedAspects.remove(aspect);
		return this;
	}

	/**
	 * <p>
	 * inParent.
	 * </p>
	 *
	 * @param parentNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inParent(NodeRef parentNodeRef) {
		parentNodeRefs.add(parentNodeRef);
		return this;
	}

	/**
	 * <p>
	 * maxResults.
	 * </p>
	 *
	 * @param maxResults
	 *            a int.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder maxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	/**
	 * <p>
	 * skipCount.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 * @param page a int
	 */
	public BeCPGQueryBuilder page(int page) {
		this.page = page;
		return this;
	}
	
	/**
	 * <p>page.</p>
	 *
	 * @param pagingRequest a {@link org.alfresco.query.PagingRequest} object
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object
	 */
	public BeCPGQueryBuilder page(PagingRequest pagingRequest) {
		this.maxResults = pagingRequest.getMaxItems();
		this.page = pagingRequest.getSkipCount()/pagingRequest.getMaxItems();
		return this;
	}


	/**
	 * <p>
	 * andOperator.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andOperator() {
		this.operator = SearchParameters.Operator.AND;
		return this;
	}

	/**
	 * <p>
	 * locale.
	 * </p>
	 *
	 * @param locale
	 *            a {@link java.util.Locale} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder locale(Locale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * <p>
	 * parent.
	 * </p>
	 *
	 * @param parentNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder parent(NodeRef parentNodeRef) {
		if (this.parentNodeRef != null) {
			logger.warn("ParentNodeRef is already set for this query.( old:" + this.parentNodeRef + " -  new: " + parentNodeRef + ")");
		}

		this.parentNodeRef = parentNodeRef;
		return this;
	}

	/**
	 * <p>
	 * members.
	 * </p>
	 *
	 * @param path
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder members(String path) {
		if (this.membersPath != null) {
			logger.warn("Path is already set for this query.( old:" + this.membersPath + " -  new: " + path + ")");
		}
		this.membersPath = path;

		return this;
	}

	/**
	 * <p>
	 * inPath.
	 * </p>
	 *
	 * @param path
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inPath(String path) {
		if (this.path != null) {
			logger.warn("Path is already set for this query.( old:" + this.path + " -  new: " + path + ")");
		}
		this.path = path;

		return this;
	}

	/**
	 * <p>
	 * inSubPath.
	 * </p>
	 *
	 * @param subPath
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inSubPath(String subPath) {
		if (this.path != null) {
			logger.warn("Path is already set for this query.( old:" + this.path + " -  new: " + path + ")");
		}
		if (this.subPath != null) {
			logger.warn("SubPath is already set for this query.( old:" + this.subPath + " -  new: " + subPath + ")");
		}
		this.subPath = subPath;

		return this;
	}

	/**
	 * <p>
	 * excludePath.
	 * </p>
	 *
	 * @param excludePath
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
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

	/**
	 * <p>
	 * inDB.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inDB() {
		queryConsistancy = QueryConsistency.TRANSACTIONAL;
		cmisLanguage();
		return this;
	}

	/**
	 * <p>
	 * inDBIfPossible.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inDBIfPossible() {
		queryConsistancy = QueryConsistency.TRANSACTIONAL_IF_POSSIBLE;
		ftsLanguage();
		return this;
	}

	/**
	 * <p>
	 * cmisLanguage.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder cmisLanguage() {
		this.language = SearchService.LANGUAGE_CMIS_ALFRESCO;
		return this;
	}

	/**
	 * <p>
	 * inSite.
	 * </p>
	 *
	 * @param siteId
	 *            a {@link java.lang.String} object.
	 * @param containerId
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inSite(String siteId, String containerId) {

		if (siteId != null && !siteId.isBlank() && ((containerId == null) || containerId.isBlank() || "documentLibrary".equals(containerId))) {
			if (this.inSite != null) {
				logger.warn("Site is already set for this query.( old:" + this.inSite + " -  new: " + siteId + ")");
			}
			inSite = siteId;
		} else {

			String sitePath = SiteHelper.SITES_SPACE_QNAME_PATH;

			if ((siteId != null) && (siteId.length() > 0)) {
				sitePath += "cm:" + ISO9075.encode(siteId);
			} else {
				sitePath += "*";
			}

			if ((containerId != null) && (containerId.length() > 0)) {
				sitePath += "/cm:" + ISO9075.encode(containerId);
			}

			// recursive //*
			sitePath += "/";
			inPath(sitePath);

		}
		return this;
	}

	/**
	 * <p>
	 * andID.
	 * </p>
	 *
	 * @param nodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andID(NodeRef nodeRef) {
		this.ids.add(nodeRef);
		return this;
	}

	/**
	 * <p>
	 * andNotID.
	 * </p>
	 *
	 * @param nodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andNotID(NodeRef nodeRef) {
		if (!ids.contains(nodeRef)) {
			this.notIds.add(nodeRef);
		} else {
			logger.warn("Unconsistent search id already in ids : " + nodeRef);
		}
		return this;
	}

	/**
	 * <p>
	 * andNotIDs.
	 * </p>
	 *
	 * @param nodeRefs
	 *            a {@link java.util.Set} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andNotIDs(Set<NodeRef> nodeRefs) {
		this.notIds.addAll(nodeRefs);
		return this;
	}

	Map<String, Boolean> sortProps = new TreeMap<>();

	/**
	 * <p>
	 * addSort.
	 * </p>
	 *
	 * @param sortMap
	 *            a {@link java.util.Map} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder addSort(Map<String, Boolean> sortMap) {
		this.sortProps = sortMap;
		return this;
	}

	/**
	 * <p>
	 * addSort.
	 * </p>
	 *
	 * @param propToSort
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param sortOrder
	 *            a boolean.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder addSort(QName propToSort, boolean sortOrder) {
		sortProps.put(getSortProp(propToSort), sortOrder);
		return this;
	}

	/**
	 * <p>
	 * isNotNull.
	 * </p>
	 *
	 * @param propQName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder isNotNull(QName propQName) {
		notNullProps.add(propQName);
		return this;
	}

	/**
	 * <p>
	 * isNull.
	 * </p>
	 *
	 * @param propQName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder isNull(QName propQName) {
		if (!notNullProps.contains(propQName)) {
			nullProps.add(propQName);
		} else {
			logger.warn("Unconsistent search null prop already in notNullProps : " + propQName);
		}
		return this;
	}

	/**
	 * <p>
	 * isNullOrUnset.
	 * </p>
	 *
	 * @param propQName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder isNullOrUnset(QName propQName) {
		if (!notNullProps.contains(propQName)) {
			nullOrUnsetProps.add(propQName);
		} else {
			logger.warn("Unconsistent search null prop already in notNullProps : " + propQName);
		}
		return this;
	}

	/**
	 * <p>
	 * andFTSQuery.
	 * </p>
	 *
	 * @param ftsQuery
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andFTSQuery(String ftsQuery) {
		ftsQueries.add(ftsQuery);
		return this;
	}

	/**
	 * <p>
	 * clearFTSQuery.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder clearFTSQuery() {
		ftsQueries.clear();
		return this;
	}

	/**
	 * <p>
	 * andPropEquals.
	 * </p>
	 *
	 * @param propQName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andPropEquals(QName propQName, String value) {
		if (value == null) {
			isNull(propQName);
		} else if (isPropQueryNull(value)) {
			isNullOrUnset(propQName);
		} else if (isPropQueryNotNull(value)) {
			isNotNull(propQName);
		} else {
			propQueriesEqualMap.put(propQName, value);
		}
		return this;
	}

	/**
	 * <p>
	 * andPropQuery.
	 * </p>
	 *
	 * @param propQName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param propQuery
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andPropQuery(QName propQName, String propQuery) {
		if (propQuery == null) {
			isNull(propQName);
		} else if (isPropQueryNull(propQuery)) {
			isNullOrUnset(propQName);
		} else if (isPropQueryNotNull(propQuery)) {
			isNotNull(propQName);
		} else {
			propQueriesMap.put(propQName, propQuery);
		}
		return this;
	}

	private boolean isPropQueryNull(String value) {
		return (value == null) || "ISUNSET".equalsIgnoreCase(value) || "ISNULL".equalsIgnoreCase(value) || "NULL".equalsIgnoreCase(value)
				|| "EMPTY".equalsIgnoreCase(value);
	}

	private boolean isPropQueryNotNull(String value) {
		return (value == null) || "ISSET".equalsIgnoreCase(value) || "ISNOTNULL".equalsIgnoreCase(value) || "NOTNULL".equalsIgnoreCase(value)
				|| "NOTEMPTY".equalsIgnoreCase(value);
	}

	/**
	 * <p>
	 * andBetween.
	 * </p>
	 *
	 * @param propQName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param start
	 *            a {@link java.lang.String} object.
	 * @param end
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andBetween(QName propQName, String start, String end) {
		propBetweenQueriesMap.put(propQName, new Pair<>(start, end));
		return this;
	}

	/**
	 * <p>orBetween.</p>
	 *
	 * @param propQName a {@link org.alfresco.service.namespace.QName} object
	 * @param start a {@link java.lang.String} object
	 * @param end a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object
	 */
	public BeCPGQueryBuilder orBetween(QName propQName, String start, String end) {
		propOrBetweenQueriesMap.put(propQName, new Pair<>(start, end));
		return this;
	}

	/**
	 * <p>
	 * andBetweenOrNull.
	 * </p>
	 *
	 * @param propQName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param start
	 *            a {@link java.lang.String} object.
	 * @param end
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder andBetweenOrNull(QName propQName, String start, String end) {
		propBetweenOrNullQueriesMap.put(propQName, new Pair<>(start, end));
		return this;
	}

	/**
	 * <p>
	 * excludeProp.
	 * </p>
	 *
	 * @param propName
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @param query
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder excludeProp(QName propName, String query) {
		List<String> queries = excludedPropQueriesMap.computeIfAbsent(propName, a -> {
			return new ArrayList<>();
		});
		queries.add(query);
		excludedPropQueriesMap.put(propName, queries);
		return this;
	}

	/**
	 * <p>
	 * excludeType.
	 * </p>
	 *
	 * @param type
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder excludeType(QName type) {
		if (!types.contains(type) && !type.equals(this.type)) {
			excludedTypes.add(type);
		}
		return this;
	}

	/**
	 * <p>
	 * excludeAspect.
	 * </p>
	 *
	 * @param aspect
	 *            a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder excludeAspect(QName aspect) {
		if (!aspects.contains(aspect)) {
			excludedAspects.add(aspect);
		} else {
			logger.warn("Unconsistent search aspect already in withAspect : " + aspect);
		}

		return this;
	}

	/**
	 * <p>
	 * excludeVersions.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder excludeVersions() {
		excludeAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION);
		return this;
	}

	/**
	 * <p>
	 * excludeDefaults.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder excludeDefaults() {
		excludeVersions();
		excludeSystems();
		excludeArchivedEntities();
		return this;
	}

	/**
	 * <p>
	 * excludeSystems.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder excludeSystems() {
		excludeAspect(BeCPGModel.ASPECT_ENTITY_TPL);
		excludeAspect(BeCPGModel.ASPECT_HIDDEN_FOLDER);
		excludeType(BeCPGModel.TYPE_SYSTEM_ENTITY);
		excludeAspect(ContentModel.ASPECT_WORKING_COPY);
		return this;
	}

	/**
	 * <p>
	 * excludeSearch.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder excludeSearch() {

		excludeDefaults();
		excludeType(ContentModel.TYPE_THUMBNAIL);
		excludeType(ContentModel.TYPE_FAILED_THUMBNAIL);
		excludeType(ContentModel.TYPE_RATING);
		excludeType(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		if (!Boolean.TRUE.equals(includeReportInSearch())) {
			excludeType(ReportModel.TYPE_REPORT);
		}
		excludeType(ForumModel.TYPE_FORUM);
		excludeType(RuleModel.TYPE_RULE);
		excludeType(ForumModel.TYPE_POST);
		excludeType(ForumModel.TYPE_FORUMS);
		excludeType(ApplicationModel.TYPE_FILELINK);
		excludeAspect(ContentModel.ASPECT_HIDDEN);

		return this;
	}

	/**
	 * <p>excludeArchivedEntities.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object
	 */
	public BeCPGQueryBuilder excludeArchivedEntities() {
		excludeAspect(BeCPGModel.ASPECT_ARCHIVED_ENTITY);
		return this;
	}

	
	public NodeRef selectNodeByPath(String xPath) {
	
		return selectNodeByPath(repository.getRootHome(),xPath);
	}
	
	/**
	 * <p>
	 * selectNodeByPath.
	 * </p>
	 *
	 * @param parentNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param xPath
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef selectNodeByPath(NodeRef parentNodeRef, String xPath) {
		this.maxResults = RepoConsts.MAX_RESULTS_SINGLE_VALUE;
		List<NodeRef> ret = selectNodesByPath(parentNodeRef, xPath);
		return (ret != null) && !ret.isEmpty() ? ret.get(0) : null;
	}

	/**
	 * <p>
	 * selectNodesByPath.
	 * </p>
	 *
	 * @param parentNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param xPath
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> selectNodesByPath(NodeRef parentNodeRef, String xPath) {
		List<NodeRef> ret = null;
		StopWatch watch = new StopWatch();
		watch.start();

		try {

			if (logger.isDebugEnabled()) {
				logger.debug("selectNodesByPath, parent: " + parentNodeRef + " xpath: " + xPath);
			}

			ret = searchService.selectNodes(parentNodeRef, xPath, null, namespaceService, false);
		} finally {
			watch.stop();
			if ((ret != null) && (watch.getTotalTimeSeconds() > 1)) {
				logger.warn("Slow query [" + xPath + "] executed in  " + watch.getTotalTimeSeconds() + " seconds - size results " + ret.size());
			}
		}

		return ret;
	}

	/**
	 * <p>
	 * list.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> list() {
		PagingResults<NodeRef> ret = pagingResults();

		return ret != null ? ret.getPage() : new ArrayList<>();
	}

	/**
	 * <p>
	 * pagingResults.
	 * </p>
	 *
	 * @return a {@link org.alfresco.query.PagingResults} object
	 */
	public PagingResults<NodeRef> pagingResults() {

		StopWatch watch = new StopWatch();
		watch.start();

		PagingResults<NodeRef> ret = null;

		String runnedQuery = buildQuery();

		try {

			if (RepoConsts.MAX_RESULTS_UNLIMITED.equals(maxResults) && logger.isDebugEnabled()) {
				logger.debug("Unlimited results ask");
			}

			ret = search(runnedQuery, sortProps, page, maxResults);

		} finally {

			int resultSize = ret != null ? ret.getTotalResultCount().getFirst() : 0;

			watch.stop();
			if (watch.getTotalTimeSeconds() > 1) {
				logger.warn("Slow query [" + runnedQuery + "] executed in  " + watch.getTotalTimeSeconds() + " seconds - size results " + resultSize);

			}

			if (logger.isDebugEnabled()) {
				int tmpIndex = (RepoConsts.MAX_RESULTS_SINGLE_VALUE.equals(maxResults) ? 4 : 3);

				logger.debug("[" + Thread.currentThread().getStackTrace()[tmpIndex].getClassName() + " "
						+ Thread.currentThread().getStackTrace()[tmpIndex].getLineNumber() + "] " + runnedQuery + " executed in  "
						+ watch.getTotalTimeSeconds() + " seconds - size results " + resultSize);
			}
		}

		return ret;
	}

	/**
	 * <p>
	 * singleValue.
	 * </p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef singleValue() {

		this.maxResults = RepoConsts.MAX_RESULTS_SINGLE_VALUE;
		List<NodeRef> ret = list();

		return (ret != null) && !ret.isEmpty() ? ret.get(0) : null;
	}

	/*
	 * 1) Lucene's QueryParser class does not parse boolean expressions -- it might look like it, but it does not. 2) Lucene's BooleanQuery clause does not model Boolean Queries ... it models
	 * aggregate queries. 3) the most native way to represent the options available in a lucene "BooleanQuery" as a string is with the +/- prefixes, where... +foo ... means foo is a required clause
	 * and docs must match it -foo ... means foo is prohibited clause and docs must not match it foo ... means foo is an optional clause and docs that match it will get score benefits for doing so. 4)
	 * in an attempt to make things easier for people who have simple needs, QueryParser "fakes" that it parses boolean expressions by interpreting "A AND B" as "+A +B"; "A OR B" as "A B" and "NOT A"
	 * as "-A" 5) if you change the default operator on QueryParser to be AND then things get more complicated, mainly because then QueryParser treats "A B" the same as "+A +B" 6) you should avoid
	 * thinking in terms of AND, OR, and NOT ... think in terms of OPTIONAL, REQUIRED, and PROHIBITED ... your life will be much easier: documentation will make more sense, conversations on the email
	 * list will be more synergistastic, wine will be sweeter, and food will taste better.
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
		} else if (subPath != null) {
			runnedQuery.append(mandatory(getCondSubPath(subPath)));
		}

		if (inSite != null) {
			runnedQuery.append(mandatory(getCondSite(inSite)));
		}

		if (!parentNodeRefs.isEmpty()) {
			if (parentNodeRefs.size() == 1) {
				runnedQuery.append(mandatory(getCondParent(parentNodeRefs.iterator().next())));
			} else {
				runnedQuery.append(mandatory(startGroup()));
				boolean first = true;
				for (NodeRef tmp : parentNodeRefs) {
					if (first) {
						runnedQuery.append(getCondParent(tmp));
					} else {
						runnedQuery.append(or(getCondParent(tmp)));
					}
					first = false;
				}
				runnedQuery.append(endGroup());
			}
		}

		if (type != null) {
			if (isExactType) {
				runnedQuery.append(mandatory(getCondExactType(type)));
			} else {
				runnedQuery.append(mandatory(getCondType(type)));
			}
		}

		if (!types.isEmpty() || !boostedTypes.isEmpty()) {
			if ((types.size() == 1) && boostedTypes.isEmpty()) {
				runnedQuery.append(mandatory(getCondType(types.iterator().next())));
			} else if ((boostedTypes.size() == 1) && types.isEmpty()) {
				runnedQuery
						.append(mandatory(boost(getCondType(boostedTypes.iterator().next().getFirst()), boostedTypes.iterator().next().getSecond())));
			} else {
				runnedQuery.append(mandatory(startGroup()));
				boolean first = true;
				for (QName tmpQName : types) {
					if (first) {
						runnedQuery.append(getCondType(tmpQName));
					} else {
						runnedQuery.append(or(getCondType(tmpQName)));
					}
					first = false;
				}
				for (Pair<QName, Integer> typePair : boostedTypes) {
					if (first) {
						runnedQuery.append(boost(getCondType(typePair.getFirst()), typePair.getSecond()));
					} else {
						runnedQuery.append(or(boost(getCondType(typePair.getFirst()), typePair.getSecond())));
					}
					first = false;
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

		if (!nullOrUnsetProps.isEmpty()) {
			for (QName tmpQName : nullOrUnsetProps) {
				runnedQuery.append(mandatory(getCondIsNullOrIsUnsetValue(tmpQName)));
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
				runnedQuery.append(mandatory(getCondContainsValue(propQueryEntry.getKey(), "(" + propQueryEntry.getValue() + ")")));
			}
		}

		if (!propQueriesEqualMap.isEmpty()) {
			for (Map.Entry<QName, String> propQueryEntry : propQueriesEqualMap.entrySet()) {
				runnedQuery.append(equalsQuery(getCondContainsValue(propQueryEntry.getKey(), "\"" + propQueryEntry.getValue() + "\"")));
			}
		}

		if (!excludedPropQueriesMap.isEmpty()) {
			for (Map.Entry<QName, List<String>> propQueryEntry : excludedPropQueriesMap.entrySet()) {
				for (String query : propQueryEntry.getValue()) {
					runnedQuery.append(prohibided(getCondContainsValue(propQueryEntry.getKey(), query)));
				}
			}
		}

		for (Map.Entry<QName, Pair<String, String>> propQueryEntry : propBetweenQueriesMap.entrySet()) {
			runnedQuery.append(mandatory(getCondContainsValue(propQueryEntry.getKey(),
					String.format("[%s TO %s]", propQueryEntry.getValue().getFirst(), propQueryEntry.getValue().getSecond()))));
		}

		if (!propOrBetweenQueriesMap.isEmpty()) {
			StringBuilder orBetweenQuery = new StringBuilder();
			for (Map.Entry<QName, Pair<String, String>> propQueryEntry : propOrBetweenQueriesMap.entrySet()) {
				String propCond = getCondContainsValue(propQueryEntry.getKey(),
						String.format("[%s TO %s]", propQueryEntry.getValue().getFirst(), propQueryEntry.getValue().getSecond()));
				if (orBetweenQuery.toString().isEmpty()) {
					orBetweenQuery.append(startGroup()).append(propCond);
				} else {
					orBetweenQuery.append(or(propCond));
				}
			}
			orBetweenQuery.append(endGroup());
			runnedQuery.append(mandatory(orBetweenQuery.toString()));
		}

		for (Map.Entry<QName, Pair<String, String>> propQueryEntry : propBetweenOrNullQueriesMap.entrySet()) {
			runnedQuery.append(getMandatoryOrGroup(getCondIsNullOrIsUnsetValue(propQueryEntry.getKey()), getCondContainsValue(propQueryEntry.getKey(),
					String.format("[%s TO %s]", propQueryEntry.getValue().getFirst(), propQueryEntry.getValue().getSecond()))));
		}

		if (!ftsQueries.isEmpty()) {
			for (String ftsQuery : ftsQueries) {
				runnedQuery.append(mandatory(startGroup()));
				runnedQuery.append(ftsQuery);
				runnedQuery.append(endGroup());
			}

		}

		String ret = runnedQuery.toString();

		if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(language) && ret.startsWith(" AND")) {
			return ret.replaceFirst(" AND", "");
		}

		return ret;
	}

	private String buildCmisQuery() {
		StringBuilder runnedQuery = new StringBuilder();
		StringBuilder orderBy = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();

		runnedQuery.append("SELECT  * FROM ");
		if ((type == null) || ContentModel.TYPE_CONTENT.equals(type)) {
			runnedQuery.append("cmis:document as D");
		} else if (ContentModel.TYPE_FOLDER.equals(type)) {
			runnedQuery.append("cmis:folder as D");
		} else {
			runnedQuery.append(type.toPrefixString(namespaceService)).append(" as D");
		}

		if (parentNodeRef != null) {
			whereClause.append(" AND IN_FOLDER( D,'").append(parentNodeRef).append("')");
		} else if (membersPath != null) {
			throw new IllegalStateException("members not supported for CMIS search");
		} else if (path != null) {
			throw new IllegalStateException("path not supported for CMIS search");
		} else if (inSite != null) {
			throw new IllegalStateException("site not supported for CMIS search");
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
				whereClause.append(" AND ").append(getCmisPrefix(tmpQName)).append(" IS NOT NULL");
			}
		}

		if (!nullOrUnsetProps.isEmpty()) {
			for (QName tmpQName : nullOrUnsetProps) {
				whereClause.append(" AND ").append(getCmisPrefix(tmpQName)).append(" IS NULL");
			}

		}

		if (!nullProps.isEmpty()) {
			for (QName tmpQName : nullProps) {
				whereClause.append(" AND ").append(getCmisPrefix(tmpQName)).append(" IS NULL");
			}
		}

		if (!ids.isEmpty()) {
			for (NodeRef tmpNodeRef : ids) {
				whereClause.append(" AND D.cmis:objectId = '").append(tmpNodeRef).append("'");
			}
		}

		if (!notIds.isEmpty()) {
			for (NodeRef tmpNodeRef : notIds) {
				whereClause.append(" AND D.cmis:objectId <> '").append(tmpNodeRef).append("'");
			}
		}

		if (!propQueriesMap.isEmpty()) {

			for (Map.Entry<QName, String> propQueryEntry : propQueriesMap.entrySet()) {
				whereClause.append(" AND ").append(getCmisPrefix(propQueryEntry.getKey())).append(" LIKE '%").append(propQueryEntry.getValue())
						.append("%'");
			}
		}

		if (!propQueriesEqualMap.isEmpty()) {
			for (Map.Entry<QName, String> propQueryEntry : propQueriesEqualMap.entrySet()) {
				whereClause.append(" AND ").append(getCmisPrefix(propQueryEntry.getKey())).append(" = '")
						.append(sanitizeProperty(propQueryEntry.getValue())).append("'");
			}
		}

		if (!excludedPropQueriesMap.isEmpty()) {
			for (Map.Entry<QName, List<String>> propQueryEntry : excludedPropQueriesMap.entrySet()) {
				for (String query : propQueryEntry.getValue()) {
					whereClause.append(" AND ").append(getCmisPrefix(propQueryEntry.getKey())).append(" <> '").append(query).append("'");
				}
			}
		}

		for (Map.Entry<QName, Pair<String, String>> propQueryEntry : propBetweenQueriesMap.entrySet()) {

			String first = propQueryEntry.getValue().getFirst();
			String second = propQueryEntry.getValue().getSecond();
			if (!"MIN".equals(first)) {
				whereClause.append(" AND ").append(getCmisPrefix(propQueryEntry.getKey())).append(" >= ").append(first).append("");
			}
			if (!"MAX".equals(second)) {
				whereClause.append(" AND ").append(getCmisPrefix(propQueryEntry.getKey())).append(" <= ").append(second).append("");
			}
		}

		for (Map.Entry<QName, Pair<String, String>> propQueryEntry : propBetweenOrNullQueriesMap.entrySet()) {

			String first = propQueryEntry.getValue().getFirst();
			String second = propQueryEntry.getValue().getSecond();
			if (!"MIN".equals(first)) {
				whereClause.append(" AND ").append(getCmisPrefix(propQueryEntry.getKey())).append(" >= ").append(first).append("");
			}
			if (!"MAX".equals(second)) {
				whereClause.append(" AND ").append(getCmisPrefix(propQueryEntry.getKey())).append(" <= ").append(second).append("");
			}

			whereClause.append(" OR ").append(getCmisPrefix(propQueryEntry.getKey())).append(" IS NULL");
		}

		if (!ftsQueries.isEmpty()) {
			throw new IllegalStateException("fts contains not supported yet");
		}

		if ((sortProps != null) && !sortProps.isEmpty()) {
			orderBy.append(" ORDER BY");
			for (Map.Entry<String, Boolean> kv : sortProps.entrySet()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Add sort :" + kv.getKey() + " " + kv.getValue());
				}
				orderBy.append(" ").append(getCmisPrefix(QName.createQName(kv.getKey().replaceFirst("@", ""))))
						.append(Boolean.TRUE.equals(kv.getValue()) ? " ASC" : " DESC");
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
				runnedQuery.append(" JOIN ").append(tmpQName.toPrefixString(namespaceService)).append(" as ").append(tmpQName.getLocalName())
						.append(" on D.cmis:objectId = ").append(tmpQName.getLocalName()).append(".cmis:objectId");
			}
		}

		return runnedQuery.toString() + ret + orderBy.toString();

	}

	private String sanitizeProperty(String prop) {
		return prop.replaceAll("(?<!\\\\)'", "\\\\'");
	}

	private String getCmisPrefix(QName tmpQName) {
		String ret = tmpQName.toPrefixString(namespaceService);
		PropertyDefinition def = entityDictionaryService.getProperty(tmpQName);

		QName aspect = def != null ? def.getContainerClass().getName() : null;
		if ((def != null) && def.getContainerClass().isAspect() && (aspect != null) && !aspect.isMatch(ContentModel.ASPECT_AUDITABLE)) {
			this.aspects.add(aspect);
			ret = aspect.getLocalName() + "." + ret;
		} else {
			if (tmpQName.equals(ContentModel.PROP_NAME)) {
				ret = "cmis:name";
			} else if (tmpQName.equals(ContentModel.PROP_CREATED)) {
				ret = "cmis:creationDate";
			} else if (tmpQName.equals(ContentModel.PROP_CREATOR)) {
				ret = "cmis:createdBy";
			} else if (tmpQName.equals(ContentModel.PROP_MODIFIED)) {
				ret = "cmis:lastModificationDate";
			} else if (tmpQName.equals(ContentModel.PROP_MODIFIER)) {
				ret = "cmis:lastModifiedBy";
			}
			ret = "D." + ret;
		}

		return ret;
	}

	private boolean isCmis() {
		return SearchService.LANGUAGE_CMIS_ALFRESCO.equals(language);
	}

	/**
	 * <p>
	 * inSearchTemplate.
	 * </p>
	 *
	 * @param searchTemplate
	 *            a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder inSearchTemplate(String searchTemplate) {
		this.searchTemplate = searchTemplate;
		return this;
	}

	/**
	 * <p>
	 * ftsLanguage.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder ftsLanguage() {
		this.language = SearchService.LANGUAGE_FTS_ALFRESCO;
		return this;
	}

	private PagingResults<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int page, int maxResults) {

		List<NodeRef> nodes = new ArrayList<>();

		boolean hasMore = false;
		int totalFirst = 0;
		int totalSecond = 0;
		int skipCount = 0;

		SearchParameters sp = new SearchParameters();
		sp.addStore(store);

		sp.setQuery(runnedQuery);
		sp.addLocale(locale);
		sp.excludeDataInTheCurrentTransaction(true);
		sp.setExcludeTenantFilter(false);

		if (logger.isDebugEnabled() && (language != null)) {
			logger.debug("Use search language:" + language);
		}

		sp.setLanguage(language);

		if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(language)) {
			sp.setDefaultFieldName(DEFAULT_FIELD_NAME);

			if (operator != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Use operator:" + operator.toString());
				}
				sp.setDefaultFTSFieldConnective(operator);
				sp.setDefaultFTSOperator(operator);
				sp.setDefaultOperator(operator);
			}
			if (searchTemplate != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("searchTemplate:" + searchTemplate);
				}
				sp.addQueryTemplate(DEFAULT_FIELD_NAME, searchTemplate);
			} else {
				sp.addQueryTemplate(DEFAULT_FIELD_NAME, defaultSearchTemplate());
			}
		}

		// Force the database use if possible
		// execute queries transactionally, when possible, and fall back to
		// eventual consistency; or
		sp.setQueryConsistency(queryConsistancy);

		if (maxResults == RepoConsts.MAX_RESULTS_UNLIMITED) {
			sp.setLimitBy(LimitBy.UNLIMITED);
			sp.setMaxPermissionChecks(Integer.MAX_VALUE);
			sp.setLimit(Integer.MAX_VALUE);
			sp.setMaxItems(Integer.MAX_VALUE);
		} else {
			sp.setLimit(maxResults);
			sp.setMaxItems(maxResults);
			sp.setLimitBy(LimitBy.FINAL_SIZE);
		}

		if (page > 0 && maxResults != RepoConsts.MAX_RESULTS_UNLIMITED) {
			skipCount = (page - 1) * maxResults;
			sp.setSkipCount(skipCount);
			sp.setMaxPermissionChecks(page * RepoConsts.MAX_RESULTS_1000);
		}

		if ((sort != null) && !isCmis()) {
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
				hasMore = result.hasMore();
				totalFirst = (int) result.getNumberFound();

				totalSecond = !hasMore ? (int) result.getNumberFound() : (int) (skipCount + result.getNumberFound() + 1);

				if (AuthenticationUtil.isMtEnabled()) {
					for (NodeRef node : result.getNodeRefs()) {
						nodes.add(tenantService.getBaseName(node));
					}
				} else {
					nodes = new ArrayList<>(result.getNodeRefs());
				}

				if (maxResults == RepoConsts.MAX_RESULTS_UNLIMITED && hasMore) {
					logger.warn("Unlimited search has more results: " + nodes.size() + " " + result.length());
				}
			}
		} catch (FTSQueryException | QueryModelException e) {
			logger.error("Incorrect query :" + runnedQuery, e);
		} finally {
			if (result != null) {
				result.close();
			}
		}

		return asPagingResults(nodes, hasMore, new Pair<>(totalFirst, totalSecond));

	}

	private PagingResults<NodeRef> asPagingResults(List<NodeRef> nodes, boolean hasMore, Pair<Integer, Integer> total) {

		return new PagingResults<NodeRef>() {

			@Override
			@Nonnull
			public List<NodeRef> getPage() {
				return nodes;
			}

			@Override
			public boolean hasMoreItems() {
				return hasMore;
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return total;
			}

			@Override
			public String getQueryExecutionId() {
				return null;
			}

		};
	}

	/**
	 * <p>
	 * count.
	 * </p>
	 *
	 * This method can be very slow for high result counts and saturate nodeDao cache
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long count() {

		String runnedQuery = buildQuery();

		Long ret = 0L;

		SearchParameters sp = new SearchParameters();
		sp.addStore(store);

		sp.setQuery(runnedQuery);
		sp.addLocale(locale);
		sp.excludeDataInTheCurrentTransaction(true);
		sp.setExcludeTenantFilter(false);
		sp.setLanguage(language);
		sp.setQueryConsistency(queryConsistancy);
		sp.setLimitBy(LimitBy.UNLIMITED);
		sp.setMaxPermissionChecks(Integer.MAX_VALUE);
		sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
		sp.setLimit(Integer.MAX_VALUE);
		sp.setMaxItems(Integer.MAX_VALUE);

		ResultSet result = null;
		try {
			result = searchService.query(sp);
			if (result != null) {
				if (result.hasMore()) {
					logger.warn("Count size was limited by: " + result.getResultSetMetaData().getLimitedBy());
				}

				ret = result.getNumberFound();
			}
		} finally {
			if (result != null) {
				result.close();
			}
		}

		return ret;
	}

	/**
	 * <p>
	 * extractSortQname.
	 * </p>
	 *
	 * @param sortProp
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName extractSortQname(String sortProp) {
		if (sortProp.indexOf(QName.NAMESPACE_BEGIN) != -1) {
			return QName.createQName(sortProp.replace("@", ""));
		} else {
			return QName.createQName(sortProp.replace("@", ""), namespaceService);
		}

	}

	/**
	 * <p>
	 * childFileFolders.
	 * </p>
	 *
	 * @param pageRequest
	 *            a {@link org.alfresco.query.PagingRequest} object.
	 * @return a {@link org.alfresco.query.PagingResults} object.
	 */
	public PagingResults<NodeRef> childFileFolders(PagingRequest pageRequest) {

		StopWatch watch = new StopWatch();
		watch.start();

		PagingResults<NodeRef> pageOfNodeInfos = null;

		List<Pair<QName, Boolean>> tmp = new LinkedList<>();

		for (Map.Entry<String, Boolean> entry : sortProps.entrySet()) {
			if (entry.getKey().indexOf(QName.NAMESPACE_BEGIN) != -1) {
				tmp.add(new Pair<>(QName.createQName(entry.getKey().replace("@", "")), entry.getValue()));
			} else {
				tmp.add(new Pair<>(QName.createQName(entry.getKey().replace("@", ""), namespaceService), entry.getValue()));
			}
		}

		try {

			if (type != null) {

				pageOfNodeInfos = internalList(parentNodeRef, Collections.singleton(type), excludedAspects, tmp, pageRequest);
			} else if (!types.isEmpty()) {
				pageOfNodeInfos = internalList(parentNodeRef, types, excludedAspects, tmp, pageRequest);
			}

		} finally {
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

	private PagingResults<NodeRef> internalList(NodeRef rootNodeRef, Set<QName> searchTypeQNames, Set<QName> ignoreAspectQNames,
			List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest) {

		// get canned query
		GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory) cannedQueryRegistry
				.getNamedObject(CANNED_QUERY_FILEFOLDER_LIST);

		GetChildrenCannedQuery cq = (GetChildrenCannedQuery) getChildrenCannedQueryFactory.getCannedQuery(rootNodeRef, null,
				Collections.singleton(ContentModel.ASSOC_CONTAINS), searchTypeQNames, ignoreAspectQNames, null, sortProps, pagingRequest);

		// execute canned query
		CannedQueryResults<NodeRef> results = cq.execute();

		return getPagingResults(pagingRequest, results);
	}

	private PagingResults<NodeRef> getPagingResults(PagingRequest pagingRequest, final CannedQueryResults<NodeRef> results) {

		final List<NodeRef> nodeRefs;
		if (results.getPageCount() > 0) {
			nodeRefs = results.getPages().get(0);
		} else {
			nodeRefs = Collections.emptyList();
		}

		// set total count
		final Pair<Integer, Integer> totalCount;
		if (pagingRequest.getRequestTotalCountMax() > 0) {
			totalCount = results.getTotalResultCount();
		} else {
			totalCount = null;
		}

		PermissionCheckedValueMixin.create(nodeRefs);

		return new PagingResults<>() {
			@Override
			public String getQueryExecutionId() {
				return null; // TODO use Paginated Cache results
								// //results.getQueryExecutionId();
			}

			@Override
			public List<NodeRef> getPage() {
				if ((type != null) && !BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(type)) {
					return nodeRefs.stream().filter(n -> nodeService.getType(n).equals(type)).toList();
				}
				return nodeRefs;
			}

			@Override
			public boolean hasMoreItems() {
				return results.hasMoreItems();
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				return totalCount;
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return buildQuery();
	}

	/** {@inheritDoc} */
	@Override
	public BeCPGQueryBuilder clone() {

		BeCPGQueryBuilder builder = new BeCPGQueryBuilder();

		if (INSTANCE != null) {
			builder.searchService = INSTANCE.searchService;
			builder.namespaceService = INSTANCE.namespaceService;
			builder.cannedQueryRegistry = INSTANCE.cannedQueryRegistry;
			builder.nodeService = INSTANCE.nodeService;
			builder.entityDictionaryService = INSTANCE.entityDictionaryService;
			builder.tenantService = INSTANCE.tenantService;
			builder.systemConfigurationService = INSTANCE.systemConfigurationService;
		}

		builder.maxResults = this.maxResults;
		builder.parentNodeRef = this.parentNodeRef;
		builder.type = this.type;
		builder.subPath = this.subPath;
		builder.path = this.path;
		builder.inSite = this.inSite;
		builder.excludePath = this.excludePath;
		builder.membersPath = this.membersPath;
		builder.queryConsistancy = this.queryConsistancy;
		builder.isExactType = this.isExactType;
		builder.searchTemplate = this.searchTemplate;
		builder.operator = this.operator;
		builder.locale = this.locale;
		builder.sortProps = this.sortProps;
		builder.parentNodeRefs.addAll(parentNodeRefs);
		builder.types.addAll(types);
		builder.boostedTypes.addAll(boostedTypes);
		builder.aspects.addAll(aspects);
		builder.ids.addAll(ids);
		builder.notIds.addAll(notIds);
		builder.notNullProps.addAll(notNullProps);
		builder.nullProps.addAll(nullProps);
		builder.nullOrUnsetProps.addAll(nullOrUnsetProps);
		builder.propQueriesMap.putAll(propQueriesMap);
		builder.propBetweenQueriesMap.putAll(propBetweenQueriesMap);
		builder.propBetweenOrNullQueriesMap.putAll(propBetweenOrNullQueriesMap);
		builder.propQueriesEqualMap.putAll(propQueriesEqualMap);
		builder.ftsQueries.addAll(ftsQueries);
		builder.excludedAspects.addAll(excludedAspects);
		builder.excludedTypes.addAll(excludedTypes);
		builder.excludedPropQueriesMap.putAll(excludedPropQueriesMap);

		return builder;

	}

	
}
