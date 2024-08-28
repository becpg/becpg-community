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

@Service("searchRuleService")
public class SearchRuleServiceImpl implements SearchRuleService {

	private static Log logger = LogFactory.getLog(SearchRuleServiceImpl.class);

	private static final String SEPARATOR = "\\-";

	private final ThreadLocal<SimpleDateFormat> formatter = ThreadLocal.withInitial(() -> {
		return new SimpleDateFormat("yyyy" + SEPARATOR + "MM" + SEPARATOR + "dd");
	});

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

	@Override
	public SearchRuleResult search(SearchRuleFilter filter) {
		StopWatch watch = new StopWatch();
		watch.start();
		SearchRuleResult searchRuleResult = new SearchRuleResult();
		try {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().excludeDefaults();

			if (filter.getNodeType() != null) {
				queryBuilder.ofType(filter.getNodeType());
			}
			Date from = null;
			Date to = null;
			
			Calendar date = Calendar.getInstance();
			
			if(filter.getCurrentDate()!=null) {
				date.setTime(filter.getCurrentDate());
			} 
			

			String fromQuery = null;
			String toQuery = null;

			switch (filter.getDateFilterType()) {
			case After: //[(NOW+DATE) , MAX]
				if( filter.getDateFilterDelay()!=null) {
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
				}
				fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				toQuery = "MAX";
				from = date.getTime();
				to = new Date(Long.MAX_VALUE);
				break;
			case To: //[NOW , (NOW+DATE)]
				if( filter.getDateFilterDelay()!=null) {
				date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
				}
				fromQuery = "NOW";
				toQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				from = new Date();
				to = date.getTime();
				break;
			case Before: //[MIN , (NOW-DATE)]
				if( filter.getDateFilterDelay()!=null) {
				date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), -filter.getDateFilterDelay());
				}
				fromQuery = "MIN";
				toQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				from = new Date(0L);
				to = new Date();
				break;
			case From: //[(NOW-DATE) , NOW]
				if( filter.getDateFilterDelay()!=null) {
				date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), -filter.getDateFilterDelay());
				}
				fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				toQuery = "NOW";
				from = date.getTime();
				to = new Date();
				break;
			case Equals: // date = NOW + X
				if( filter.getDateFilterDelay()!=null) {
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
				}
				fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
				toQuery = fromQuery;
				from = date.getTime();
				to = date.getTime();
				break;
			}


			if ( filter.getDateField() != null) {
				
				if (DateFilterType.Equals.equals(filter.getDateFilterType())) {
					queryBuilder.andPropQuery(filter.getDateField(), fromQuery);
				} else {
					queryBuilder.andBetween(filter.getDateField(), fromQuery, toQuery);
				}

			}

			if (filter.getNodePath() != null) {
				queryBuilder.inSubPath(filter.getNodePath().toPrefixString(namespaceService));
			}

			if ((filter != null) && !filter.getQuery().isEmpty()) {
				queryBuilder.andFTSQuery( String.format(filter.getQuery(), fromQuery, fromQuery, fromQuery));
			}
			
			if(Boolean.TRUE.equals(filter.getEnsureDbQuery())) {
				queryBuilder.inDB();
			}

			List<NodeRef> ret = advSearchService.queryAdvSearch(filter.getNodeType(), queryBuilder, filter.getNodeCriteria(),
					RepoConsts.MAX_RESULTS_5000);

			if ((filter.getEntityCriteria() != null) && (filter.getEntityType() != null)) {
				ret = filterByEntityCriteria(ret, filter);
			}

			//Versions history filter
			Map<NodeRef, Map<String, NodeRef>> itemVersions = new HashMap<>();
			if (from != null && to != null) {

				if (!VersionFilterType.NONE.equals(filter.getVersionFilterType()) && filter.getDateField().isMatch(ContentModel.PROP_MODIFIED)) {
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

	private int getDateFilterDelayUnit(DateFilterDelayUnit dateFilterDelayUnit) {
		switch (dateFilterDelayUnit) {
		case HOUR:
			return Calendar.HOUR;
		case MINUTE:
			return Calendar.MINUTE;
		default:
			return Calendar.DATE;
		}

	}

	private List<NodeRef> filterByEntityCriteria(List<NodeRef> nodes, SearchRuleFilter filter) {
		if (logger.isDebugEnabled()) {
			logger.debug("Filter by entity criteria, size before: " + nodes.size());
		}

		List<NodeRef> ret = new ArrayList<>();
		if (filter.getEntityCriteria() != null && !filter.getEntityCriteria().isEmpty()) {

			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(filter.getEntityType()).excludeDefaults();
			List<NodeRef> entities = advSearchService.queryAdvSearch(filter.getEntityType(), queryBuilder, filter.getEntityCriteria(),
					RepoConsts.MAX_RESULTS_5000);
			for (NodeRef nodeRef : nodes) {
				NodeRef entityRef = entityService.getEntityNodeRef(nodeRef, nodeService.getType(nodeRef));
				if (entities.contains(entityRef)) {
					ret.add(nodeRef);
				}
			}

		} else {
			for (NodeRef nodeRef : nodes) {
				NodeRef entityRef = entityService.getEntityNodeRef(nodeRef, nodeService.getType(nodeRef));
				if (entityRef != null && matchEntityType(entityRef, filter.getEntityType())) {
					ret.add(nodeRef);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(" - new size: " + ret.size());
		}
		return ret;
	}

	private Map<String, NodeRef> getOnlyAssociatedVersions(NodeRef item, VersionFilterType versionType, Date from, Date to) {
		Map<String, NodeRef> ret = new HashMap<>();
		final VersionHistory versionHistory;
		if (from != null && to != null && (versionHistory = versionService.getVersionHistory(item)) != null) {
			versionHistory.getAllVersions().forEach(version -> {
				Date createDate = version.getFrozenModifiedDate();
				// if versionType is MINOR, versionLabel must not match an integer
				// if versionType is MAJOR, versionLabel must match an integer
				// versionType = MINOR XOR versionLabel matches INT
				final boolean versionsMatch = versionType != null && (versionType == VersionFilterType.MINOR != (Double.parseDouble(version.getVersionLabel()) % 1 == 0));
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
