package fr.becpg.repo.search.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.SearchRuleService;
import fr.becpg.repo.search.data.DateFilterDelayUnit;
import fr.becpg.repo.search.data.DateFilterType;
import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.repo.search.data.SearchRuleResult;
import fr.becpg.repo.search.data.VersionFilterType;

/**
 * <p>SearchRuleServiceImpl class.</p>
 *
 * @author matthieu
 */
@Service("searchRuleService")
public class SearchRuleServiceImpl implements SearchRuleService {

	private static Log logger = LogFactory.getLog(SearchRuleServiceImpl.class);

	private static final String SEPARATOR = "\\-";

	private static final int MAX_ENTITY_NODE_REF_CALLS = RepoConsts.MAX_RESULTS_1000;
	private static final int MAX_RET_SIZE = RepoConsts.MAX_RESULTS_256;
	
	private final ThreadLocal<SimpleDateFormat> formatter = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy" + SEPARATOR + "MM" + SEPARATOR + "dd"));

	
	
	/**
	 * <p>cleanupThreadLocal.</p>
	 */
	public void cleanupThreadLocal() {
		formatter.remove();
	}

	@Autowired
	private VersionService versionService;

	@Autowired
	private EntityService entityService;

	@Autowired
	private AdvSearchService advSearchService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private DictionaryService dictionaryService;

	/** {@inheritDoc} */
	@Override
	public SearchRuleResult search(SearchRuleFilter filter) {
		StopWatch watch = new StopWatch();
		watch.start();
		SearchRuleResult searchRuleResult = new SearchRuleResult();
		try {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().excludeDefaults();

			if (filter.getNodeType() != null) {
				if (ContentModel.TYPE_CONTENT.equals(filter.getNodeType())) {
					queryBuilder.ofExactType(filter.getNodeType());
				} else {
					queryBuilder.ofType(filter.getNodeType());
				}
			}
			Date from = null;
			Date to = null;

			Calendar date = Calendar.getInstance();

			if (filter.getCurrentDate() != null) {
				date.setTime(filter.getCurrentDate());
			}

			String fromQuery = null;
			String toQuery = null;

			switch (filter.getDateFilterType()) {
			case After: //[(NOW+DATE) , MAX]
				if (filter.getDateFilterDelay() != null) {
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
				}
				fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				toQuery = "MAX";
				from = date.getTime();
				to = new Date(Long.MAX_VALUE);
				break;
			case To: //[NOW , (NOW+DATE)]
				if (filter.getDateFilterDelay() != null) {
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
				}
				fromQuery = "NOW";
				toQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				from = new Date();
				to = date.getTime();
				break;
			case Before: //[MIN , (NOW-DATE)]
				if (filter.getDateFilterDelay() != null) {
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), -filter.getDateFilterDelay());
				}
				fromQuery = "MIN";
				toQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				from = new Date(0L);
				to = new Date();
				break;
			case From: //[(NOW-DATE) , NOW]
				if (filter.getDateFilterDelay() != null) {
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), -filter.getDateFilterDelay());
				}
				fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				toQuery = "NOW";
				from = date.getTime();
				to = new Date();
				break;
			case Equals: // date = NOW + X
				if (filter.getDateFilterDelay() != null) {
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
				}
				fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				toQuery = fromQuery;
				from = date.getTime();
				to = date.getTime();
				break;
			}

			if (filter.getDateField() != null) {

				if (DateFilterType.Equals.equals(filter.getDateFilterType())) {
					queryBuilder.andPropQuery(filter.getDateField(), fromQuery);
				} else {
					queryBuilder.andBetween(filter.getDateField(), fromQuery, toQuery);
				}

			}
			boolean isNotIndexedType = isNotIndexedType(filter.getNodeType());
			boolean shouldFilterByPath = (filter.getNodePath() != null) && isNotIndexedType(filter.getNodeType());

			if ((filter.getNodePath() != null) && !shouldFilterByPath) {
				queryBuilder.inSubPath(filter.getNodePath().toPrefixString(namespaceService));
			}

			if ((filter != null) && !filter.getQuery().isEmpty()) {
				queryBuilder.andFTSQuery(String.format(filter.getQuery(), fromQuery, fromQuery, fromQuery));
			}

			if (Boolean.TRUE.equals(filter.getEnsureDbQuery()) || isNotIndexedType) {
				queryBuilder.inDB();
			}

			List<NodeRef> ret = advSearchService.queryAdvSearch(filter.getNodeType(), queryBuilder, filter.getNodeCriteria(),
					getMaxResults(filter.getMaxResults(), shouldFilterByPath));

			if ((filter.getEntityCriteria() != null) && (filter.getEntityType() != null)) {
				ret = filterByEntityCriteria(ret, filter);
			}

			if (shouldFilterByPath) {
				
				ret = filterByPath(ret, filter);
			}

			//Versions history filter
			Map<NodeRef, Map<String, NodeRef>> itemVersions = new HashMap<>();
			if (((from != null) && (to != null))
					&& (!VersionFilterType.NONE.equals(filter.getVersionFilterType()) && filter.getDateField().isMatch(ContentModel.PROP_MODIFIED))) {
				Iterator<NodeRef> iter = ret.iterator();
				while (iter.hasNext()) {
					NodeRef item = iter.next();
					Map<String, NodeRef> temp = getOnlyAssociatedVersions(item, filter.getVersionFilterType(), from, to);
					if (!temp.isEmpty()) {
						itemVersions.put(item, temp);
					} else {
						iter.remove();
					}
				}
			}

			searchRuleResult.setResults(ret);
			searchRuleResult.setItemVersions(itemVersions);

		} finally {
			watch.stop();
			if ((watch.getTotalTimeSeconds() > 1)) {
				logger.warn("Slow searchRuleFilter [" + filter.toString() + "] executed in  " + watch.getTotalTimeSeconds()
						+ " seconds - size results " + searchRuleResult.getResults().size());
			}
		}

		return searchRuleResult;
	}

	private int getMaxResults(Integer maxResults, boolean shouldFilterByPath) {
		if ((maxResults != null) && !shouldFilterByPath) {
			return maxResults;
		}
		return RepoConsts.MAX_RESULTS_5000;
	}

	/**
	 * <p>filterByPath.</p>
	 *
	 * @param ret a {@link java.util.List} object
	 * @param nodePath a {@link org.alfresco.service.cmr.repository.Path} object
	 * @return a {@link java.util.List} object
	 */
	@Deprecated
	private List<NodeRef> filterByPath(List<NodeRef> ret, SearchRuleFilter filter) {
		
		logger.info("Filter by path for type : " + filter.getNodeType());
		
		if (ret.size() > MAX_RET_SIZE) {
			logger.warn("filterByPath is not optimized for size > " + MAX_RET_SIZE + " consider filtering on initial query");
			logger.info(" - filter: " + filter.toString());
		}
		
		List<NodeRef> filtered = new ArrayList<>();
		for (NodeRef node : ret) {
			Path refPath = nodeService.getPath(node);
			if (isPathContained(refPath, filter.getNodePath())) {
				filtered.add(node);
			}
		}
		return filtered;
	}

	/**
	 * Checks if a path contains another path
	 *
	 * @param containerPath The path to check
	 * @param containedPath The path that might be contained
	 * @return true if containerPath contains containedPath
	 */
	private boolean isPathContained(Path containerPath, Path containedPath) {
		if ((containerPath == null) || (containedPath == null)) {
			return false;
		}

		// Convert paths to string for comparison
		String containerPathStr = containerPath.toPrefixString(namespaceService);
		String containedPathStr = containedPath.toPrefixString(namespaceService);

		// Check if container path starts with the contained path
		return containerPathStr.startsWith(containedPathStr);
	}

	private boolean isNotIndexedType(QName nodeType) {
		return (nodeType != null) && BeCPGQueryBuilder.isExcludedFromIndex(nodeType);
	}

	private int getDateFilterDelayUnit(DateFilterDelayUnit dateFilterDelayUnit) {
		return switch (dateFilterDelayUnit) {
		case HOUR -> Calendar.HOUR;
		case MINUTE -> Calendar.MINUTE;
		default -> Calendar.DATE;
		};

	}

	@Deprecated
	private List<NodeRef> filterByEntityCriteria(List<NodeRef> nodes, SearchRuleFilter filter) {
	

		// Log initial state
		if (logger.isDebugEnabled()) {
			logger.debug("Filter by entity criteria, size before: " + nodes.size());
		}
		if (nodes.size() > MAX_RET_SIZE) {
			logger.warn("filterByEntityCriteria is not optimized for size > " + MAX_RET_SIZE + " consider filtering on initial query");
			logger.info(" - filter: " + filter.toString());
		}

		if (nodes.isEmpty()) {
			return new ArrayList<>();
		}

		List<NodeRef> result = new ArrayList<>();
		int entityNodeRefCallCount = 0;

		List<NodeRef> entities = null;
		if ((filter.getEntityCriteria() != null) && !filter.getEntityCriteria().isEmpty()) {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(filter.getEntityType()).excludeDefaults();
			entities = advSearchService.queryAdvSearch(filter.getEntityType(), queryBuilder, filter.getEntityCriteria(), RepoConsts.MAX_RESULTS_5000);
		}

		for (NodeRef nodeRef : nodes) {
			if ((entityNodeRefCallCount >= MAX_ENTITY_NODE_REF_CALLS) || (result.size() >= MAX_RET_SIZE)) {
				logger.warn("Maximum number of getEntityNodeRef calls (" + MAX_ENTITY_NODE_REF_CALLS + ") reached. Processing stopped.");
				break;
			}

			NodeRef entityRef = entityService.getEntityNodeRef(nodeRef, nodeService.getType(nodeRef));
			entityNodeRefCallCount++;

			boolean shouldInclude = entities != null ? entities.contains(entityRef)
					: ((entityRef != null) && matchEntityType(entityRef, filter.getEntityType()));

			if (shouldInclude) {
				result.add(nodeRef);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(" - new size: " + result.size());
			logger.debug(" - getEntityNodeRef calls: " + entityNodeRefCallCount);
		}

		return result;
	}

	private Map<String, NodeRef> getOnlyAssociatedVersions(NodeRef item, VersionFilterType versionType, Date from, Date to) {
		Map<String, NodeRef> ret = new HashMap<>();
		final VersionHistory versionHistory;
		if ((from != null) && (to != null) && ((versionHistory = versionService.getVersionHistory(item)) != null)) {
			versionHistory.getAllVersions().forEach(version -> {
				Date createDate = version.getFrozenModifiedDate();
				// if versionType is MINOR, versionLabel must not match an integer
				// if versionType is MAJOR, versionLabel must match an integer
				// versionType = MINOR XOR versionLabel matches INT
				final boolean versionsMatch = (versionType != null)
						&& ((versionType == VersionFilterType.MINOR) != ((Double.parseDouble(version.getVersionLabel()) % 1) == 0));
				if (versionsMatch && !RepoConsts.INITIAL_VERSION.equals(version.getVersionLabel())
						&& (from.equals(to) ? formatter.get().format(createDate).equals(formatter.get().format(from))
								: (createDate.after(from) && createDate.before(to)))) {
					ret.put(version.getVersionLabel() + "|" + version.getDescription(), version.getFrozenStateNodeRef());
				}
			});
		}
		return ret;
	}

	private String formatDate(DateFilterDelayUnit dateFilterDelayUnit, Calendar date) {
		if (DateFilterDelayUnit.DATE.equals(dateFilterDelayUnit)) {
			return date.get(Calendar.YEAR) + "\\-" + (date.get(Calendar.MONTH) + 1) + "\\-" + date.get(Calendar.DAY_OF_MONTH);
		}
		return ISO8601DateFormat.format(date.getTime());
	}

	private boolean matchEntityType(NodeRef entityRef, QName entityType) {
		return dictionaryService.isSubClass(nodeService.getType(entityRef), entityType);
	}

}
