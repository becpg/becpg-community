package fr.becpg.repo.search.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import fr.becpg.repo.search.AdvSearchPlugin;
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

	@Autowired(required = false)
	private AdvSearchPlugin[] advSearchPlugins;

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
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();

			if (filter.getNodeType() != null) {
				queryBuilder.ofType(filter.getNodeType());
			}
			Date from = null;
			Date to = null;

			if (filter.getDateFilterType() != null && filter.getDateFilterDelay() != null && filter.getDateField() != null) {
				Calendar date = Calendar.getInstance();

				String fromQuery = null;
				String toQuery = null;

				switch (filter.getDateFilterType()) {
				case After: //[(NOW+DATE) , MAX]
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
					fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
					toQuery = "MAX";
					from = date.getTime();
					to = new Date(Long.MAX_VALUE);
					break;
				case To: //[NOW , (NOW+DATE)]
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
					fromQuery = "NOW";
					toQuery = formatDate(filter.getDateFilterDelayUnit(), date);
					from = new Date();
					to = date.getTime();
					break;
				case Before: //[MIN , (NOW-DATE)]
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), -filter.getDateFilterDelay());
					fromQuery = "MIN";
					toQuery = formatDate(filter.getDateFilterDelayUnit(), date);
					from = new Date(0L);
					to = new Date();
					break;
				case From: //[(NOW-DATE) , NOW]
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), -filter.getDateFilterDelay());
					fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
					toQuery = "NOW";
					from = date.getTime();
					to = new Date();
					break;
				case Equals: // date = NOW + X
					date.add(getDateFilterDelayUnit(filter.getDateFilterDelayUnit()), filter.getDateFilterDelay());
					fromQuery = formatDate(filter.getDateFilterDelayUnit(), date);
					toQuery = fromQuery;
					from = date.getTime();
					to = date.getTime();
					break;
				}

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
				queryBuilder.andFTSQuery(filter.getQuery());
			}

			List<NodeRef> ret = advSearchService.queryAdvSearch(filter.getNodeType(), queryBuilder, filter.getNodeCriteria(),
					RepoConsts.MAX_RESULTS_UNLIMITED);

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
		List<NodeRef> ret = new ArrayList<>();
		if (advSearchPlugins != null) {
			for (NodeRef nodeRef : nodes) {
				NodeRef entityRef = entityService.getEntityNodeRef(nodeRef, nodeService.getType(nodeRef));
				if (entityRef!=null &&  matchEntityType(entityRef, filter.getEntityType())) {
					List<NodeRef> entityList = new ArrayList<>(Collections.singletonList(entityRef));
					for (AdvSearchPlugin advSearchPlugin : advSearchPlugins) {
						entityList = advSearchPlugin.filter(entityList, filter.getEntityType(), filter.getEntityCriteria(),
								advSearchService.getSearchConfig());
					}

					if ((entityList != null) && !entityList.isEmpty()) {
						ret.add(nodeRef);
					}
				}
			}
		}
		return ret;
	}

	private Map<String, NodeRef> getOnlyAssociatedVersions(NodeRef item, VersionFilterType versionType, Date from, Date to) {
		Map<String, NodeRef> ret = new HashMap<>();
		if ((from != null) && (to != null) && (versionService.getVersionHistory(item) != null)) {
			versionService.getVersionHistory(item).getAllVersions().forEach(version -> {
				Date createDate = version.getFrozenModifiedDate();
				if (((versionType != null) && versionType.match(version.getVersionType()))
						&& !RepoConsts.INITIAL_VERSION.equals(version.getVersionLabel())
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
