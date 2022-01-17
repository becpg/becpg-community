/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.repo.search.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.search.AdvSearchPlugin;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * This class do a search on the repository (association, properties and
 * productLists), for the UI so it respects rights.
 *
 * @author querephi
 * @version $Id: $Id
 */

@Service("advSearchService")
public class AdvSearchServiceImpl implements AdvSearchService {

	private static final Log logger = LogFactory.getLog(AdvSearchServiceImpl.class);

	@Autowired
	private NamespaceService namespaceService;

	@Autowired(required = false)
	private AdvSearchPlugin[] advSearchPlugins;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private Repository repository;

	/**
	 * Constant
	 * <code>CONFIG_PATH="/app:company_home/cm:System/cm:Config/c"{trunked}</code>
	 */
	public static final String CONFIG_PATH = "/app:company_home/cm:System/cm:Config/cm:search.json";
	/** Constant <code>SEARCH_CONFIG_CACHE_KEY="SEARCH_CONFIG"</code> */
	public static final String SEARCH_CONFIG_CACHE_KEY = "SEARCH_CONFIG";

	/**
	 * <p>
	 * getSearchConfig.
	 * </p>
	 *
	 * @return a {@link fr.becpg.repo.search.impl.SearchConfig} object.
	 */
	@Override
	public SearchConfig getSearchConfig() {

		return beCPGCacheService.getFromCache(AdvSearchService.class.getName(), SEARCH_CONFIG_CACHE_KEY, () -> {
			SearchConfig searchConfig = new SearchConfig();

			NodeRef configNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), CONFIG_PATH);
			if (configNodeRef != null) {
				ContentReader reader = contentService.getReader(configNodeRef, ContentModel.PROP_CONTENT);
				String content = reader.getContentString();

				searchConfig.parse(content, namespaceService, entityDictionaryService);
			}
			return searchConfig;
		});
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> queryAdvSearch(QName datatype, BeCPGQueryBuilder beCPGQueryBuilder, Map<String, String> criteria, int maxResults) {

		SearchConfig searchConfig = getSearchConfig();

		logger.debug("advSearch, dataType=" + datatype + ", \ncriteria=" + criteria + "\nplugins: " + Arrays.asList(advSearchPlugins));
		if (isAssocSearch(criteria) || (maxResults > RepoConsts.MAX_RESULTS_1000)) {
			maxResults = RepoConsts.MAX_RESULTS_UNLIMITED;
		} else if (maxResults <= 0) {
			maxResults = RepoConsts.MAX_RESULTS_1000;
		}

		Set<String> ignoredFields = new HashSet<>();

		if (advSearchPlugins != null) {
			for (AdvSearchPlugin advSearchPlugin : advSearchPlugins) {
				ignoredFields.addAll(advSearchPlugin.getIgnoredFields(datatype, searchConfig));
			}
		}

		addCriteriaMap(beCPGQueryBuilder, criteria, ignoredFields);

		List<NodeRef> nodes = beCPGQueryBuilder.maxResults(maxResults).ofType(datatype).inDBIfPossible().list();

		if (advSearchPlugins != null) {
			StopWatch watch = null;
			for (AdvSearchPlugin advSearchPlugin : advSearchPlugins) {
				if (logger.isDebugEnabled()) {
					watch = new StopWatch();
					watch.start();
				}
				nodes = advSearchPlugin.filter(nodes, datatype, criteria, searchConfig);
				if (logger.isDebugEnabled() && (watch != null)) {
					watch.stop();
					logger.debug("query filter " + advSearchPlugin.getClass().getName() + " executed in  " + watch.getTotalTimeSeconds()
							+ " seconds, new size: " + nodes.size());
				}
			}
		}

		return nodes;
	}

	/** {@inheritDoc} */
	@Override
	public BeCPGQueryBuilder createSearchQuery(QName datatype, String term, String tag, boolean isRepo, String siteId, String containerId) {
		BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery();
		// Simple keyword search and tag specific search
		if ((term != null) && (term.length() != 0)) {
			beCPGQueryBuilder.andFTSQuery(term);
		} else if ((tag != null) && (tag.length() != 0)) {
			beCPGQueryBuilder.andFTSQuery("TAG:\"" + tag + "\"");
		}

		// we processed the search terms, so suffix the PATH query

		if (!isRepo) {
			beCPGQueryBuilder.inSite(siteId, containerId);
		} else {
			beCPGQueryBuilder.excludePath(RepoConsts.ENTITIES_HISTORY_XPATH + "//*");
		}

		if (datatype != null) {
			beCPGQueryBuilder.ofType(datatype);
		}

		beCPGQueryBuilder.excludeSearch();

		return beCPGQueryBuilder;
	}

	private void addCriteriaMap(BeCPGQueryBuilder queryBuilder, Map<String, String> criteriaMap, Set<String> ignoredFields) {
		if ((criteriaMap != null) && !criteriaMap.isEmpty()) {

			for (Map.Entry<String, String> criterion : criteriaMap.entrySet()) {

				String key = criterion.getKey();
				String propValue = criterion.getValue();

				if (!propValue.isEmpty() && !ignoredFields.contains(key)) {
					// properties
					if (key.startsWith("prop_")) {

						// found a property - is it namespace_propertyname or
						// pseudo property format?
						String propName = key.substring(5);
						if (propName.contains("_")) {

							propName = propName.replace("_", ":");

							// special case for range packed properties
							if (propName.endsWith("-range")) {

								// currently support text based ranges (usually
								// numbers) or date ranges
								// range value is packed with a | character
								// separator

								// if neither value is specified then there is
								// no need to add the term
								if (!propValue.isEmpty()) {

									String from;
									String to;
									int sepindex = propValue.indexOf("|");
									if (propName.endsWith("-date-range")) {
										// date range found
										propName = propName.substring(0, propName.length() - "-date-range".length());

										// work out if "from" and/or "to" are
										// specified - use MIN and MAX
										// otherwise;
										// we only want the "YYYY-MM-DD" part of
										// the ISO date value - so crop the
										// strings
										from = (sepindex == 0 ? "MIN" : propValue.substring(0, 10));
										to = (sepindex == (propValue.length() - 1) ? "MAX" : propValue.substring(sepindex + 1, sepindex + 11));
									} else {
										// simple range found
										propName = propName.substring(0, propName.length() - "-range".length());

										// work out if "min" and/or "max" are
										// specified - use MIN and MAX otherwise
										from = (sepindex == 0 ? "MIN" : propValue.substring(0, sepindex));
										to = (sepindex == (propValue.length() - 1) ? "MAX" : propValue.substring(sepindex + 1));
									}

									queryBuilder.andBetween(QName.createQName(propName, namespaceService), from, to);

								}

							} else if (propName.contains("Hierarchy")) {
								String hierarchyQuery = getHierarchyQuery(propName, propValue);

								if ((hierarchyQuery != null) && (hierarchyQuery.length() > 0)) {
									List<String> hierarchyNodes = new ArrayList<>();
									String[] results = hierarchyQuery.split(",");
									for (String result : results) {
										result = result.replace("\"", "");
										hierarchyNodes.add(result);
									}

									String hierarchyPropName = propName;
									if (!hierarchyNodes.isEmpty()) {
										Integer depthLevel = getHierarchyLevel(new NodeRef(hierarchyNodes.get(0)));
										if ((depthLevel != null) && !hierarchyPropName.contains(depthLevel.toString())) {
											hierarchyPropName = hierarchyPropName.replaceAll("[0-9]", depthLevel.toString());
										}
										if (entityDictionaryService.getProperty(QName.createQName(hierarchyPropName, namespaceService)) == null) {
											hierarchyPropName = propName;
										}

									}

									String hierarchyNodesString = hierarchyNodes.toString().replace(", ", "\" OR @" + hierarchyPropName + ":\"")
											.replaceAll(Pattern.quote("["), "\"").replaceAll(Pattern.quote("]"), "\"");
									StringBuilder hierarchiesQuery = new StringBuilder();
									hierarchiesQuery.append("@");
									hierarchiesQuery.append(hierarchyPropName);
									hierarchiesQuery.append(":");
									hierarchiesQuery.append(hierarchyNodesString);

									queryBuilder.andFTSQuery(hierarchiesQuery.toString());

								}

							} else if (propName.endsWith("depthLevel")) {
								Integer maxLevel = null;
								try {
									maxLevel = Integer.parseInt(propValue);
								} catch (Exception e) {
									// do nothing
								}
								if ((maxLevel != null) && (maxLevel > 0)) {
									queryBuilder.andBetween(QName.createQName(propName, namespaceService), "0", propValue);

								}
							} else if(!propName.endsWith("-entry")) {
								// beCPG - bug fix : pb with operator -,
								// AND, OR
								// poivre AND -noir
								// poivre AND noir
								// sushi AND (saumon OR thon) AND -dorade
								// formQuery += (first ? "" : " AND ") +

								queryBuilder.andPropQuery(QName.createQName(propName, namespaceService), cleanValue(propValue));
							}
						} else {
							// pseudo cm:content property - e.g.
							// mimetype,size
							// or encoding
							queryBuilder.andFTSQuery("cm:content." + propName + ":\"" + propValue + "\"");

						}

					}

				}
			}

		}

	}

	private String cleanValue(String propValue) {
		String cleanQuery = propValue.replace(".", "").replace("#", "");

		if (cleanQuery.contains("\",\"")) {
			cleanQuery = cleanQuery.replace("\",\"", "\" OR \"");
		}

		return escapeValue(cleanQuery);
	}

	/**
	 * <p>
	 * escapeValue.
	 * </p>
	 *
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String escapeValue(String value) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if ((c == '{') || (c == '}') || (c == ':') || (c == '-') || (c == '/')) {
				buf.append('\\');
			}
			buf.append(c);
		}
		return buf.toString();
	}

	private String getHierarchyQuery(String propName, String hierarchyName) {
		List<NodeRef> nodes = null;

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		if (!NodeRef.isNodeRef(hierarchyName)) {

			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery()
					.inSubPath(RepoConsts.PATH_SYSTEM + "/" + RepoConsts.PATH_PRODUCT_HIERARCHY + "/"
							+ BeCPGModel.ASSOC_ENTITYLISTS.toPrefixString(namespaceService))
					.inType(BeCPGModel.TYPE_LINKED_VALUE).andPropQuery(BeCPGModel.PROP_LKV_VALUE, hierarchyName);

			if (propName.endsWith("Hierarchy1")) {
				queryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, "1");
			}

			nodes = queryBuilder.list();
		}

		StringBuilder ret = new StringBuilder();
		if ((nodes != null) && !nodes.isEmpty()) {
			for (NodeRef node : nodes) {
				ret.append(" \"" + node.toString() + "\"");
			}
		} else {
			ret.append("\"" + hierarchyName + "\"");
		}

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("getHierarchyQuery executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}
		return ret.toString();
	}

	private Integer getHierarchyLevel(NodeRef hierarchyNodeRef) {
		return (Integer) nodeService.getProperty(hierarchyNodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
	}

	private boolean isAssocSearch(Map<String, String> criteria) {
		if (criteria != null) {
			for (Map.Entry<String, String> criterion : criteria.entrySet()) {
				String key = criterion.getKey();
				String value = criterion.getValue();
				// association
				if (key.startsWith("assoc_") && (value != null) && !value.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
}
