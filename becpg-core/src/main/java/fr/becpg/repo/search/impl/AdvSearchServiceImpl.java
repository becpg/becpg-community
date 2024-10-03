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
package fr.becpg.repo.search.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
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
	 * {@inheritDoc}
	 *
	 * <p>
	 * getSearchConfig.
	 * </p>
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
		if (isSearchFiltered(criteria) || (maxResults > RepoConsts.MAX_RESULTS_1000)) {
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
			beCPGQueryBuilder.andFTSQuery(cleanValue(term, true));
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

	// Advanced search form data search.
	// Supplied as json in the standard Alfresco Forms data structure:
	//    prop_<name>:value|assoc_<name>:value
	//    name = namespace_propertyname|pseudopropertyname
	//    value = string value - comma separated for multi-value, no escaping yet!
	// - underscore represents colon character in name
	// - pseudo property is one of any cm:content url property: mimetype|encoding|size
	// - always string values - interogate DD for type data
	// - an additional "-mode" suffixed parameter for a value is allowed to specify
	//   either an AND or OR join condition for multi-value property searches
	private void addCriteriaMap(BeCPGQueryBuilder queryBuilder, Map<String, String> criteriaMap, Set<String> ignoredFields) {
		if ((criteriaMap != null) && !criteriaMap.isEmpty()) {

			boolean useSubCats = false;
			for (Map.Entry<String, String> criterion : criteriaMap.entrySet()) {

				String key = criterion.getKey();
				String propValue = criterion.getValue();
				String modePropValue = criteriaMap.get(key + "-mode");

				if (!propValue.isEmpty() && !ignoredFields.contains(key)) {
					// properties
					if (key.startsWith("prop_") && !key.endsWith("-mode")) {

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
										// or
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

							} else if (propName.endsWith(":added")) {
								
								List<String> nodes = new ArrayList<>();
								String[] results = propValue.split(",");
								for (String result : results) {
									result = result.replace("\"", "");
									nodes.add(result);
								}
								
								String propNameReplaced = propName.replace(":added", "");
								
								String nodesString = nodes.toString().replace(", ", "\" OR @" + propNameReplaced + ":\"")
										.replaceAll(Pattern.quote("["), "\"").replaceAll(Pattern.quote("]"), "\"");
								
								StringBuilder query = new StringBuilder();
								query.append("@");
								query.append(propNameReplaced);
								query.append(":");
								query.append(nodesString);
								
								queryBuilder.andFTSQuery(query.toString());
								
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
							} else if (isCategoryProperty(criteriaMap, key)) {
								// If there's no suffix it means this property holds the value for categories
								if ((propName.indexOf("usesubcats") == -1) && (propName.indexOf("isCategory") == -1)) {
									// Determines if the checkbox use sub categories was clicked
									if ("true".equals(criteriaMap.get(key + "_usesubcats"))) {
										useSubCats = true;
									}

									// Build list of category terms to search for
									String catQuery = "";
									String[] cats = propValue.split(",");
									if (propName.indexOf("cm:categories") != -1) {
										catQuery = processDefaultCategoryProperty(cats, useSubCats);
									} else if (propName.indexOf("cm:taggable") != -1) {
										catQuery = processDefaultTagProperty(cats);
									}

									if (!catQuery.isBlank()) {

										queryBuilder.andFTSQuery(catQuery);

									}
								}
							} else if (isMultiValueProperty(propValue, modePropValue) || isListProperty(criteriaMap, key)) {
								if (propName.indexOf("isListProperty") == -1) {
									queryBuilder.andFTSQuery(processMultiValue(propName, propValue, modePropValue, false));
								}
							} else if (!propName.endsWith("-entry")) {
								// beCPG - bug fix : pb with operator -,
								// AND, OR
								// poivre AND -noir
								// poivre AND noir
								// sushi AND (saumon OR thon) AND -dorade
								// formQuery += (first ? "" : " AND ") +

								queryBuilder.andPropQuery(QName.createQName(propName, namespaceService), cleanValue(propValue, false));
							}
						} else {

							if (isMultiValueProperty(propValue, modePropValue)) {
								queryBuilder.andFTSQuery(processMultiValue(propName, propValue, modePropValue, true));
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

	}

	private String cleanValue(String propValue, boolean cleanFTS) {
		String cleanQuery = cleanFTS ? propValue.replace(".", "").replace("#", "") : propValue;

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

	private boolean isSearchFiltered(Map<String, String> criteria) {
		for (AdvSearchPlugin advSearchPlugin : advSearchPlugins) {
			if (advSearchPlugin.isSearchFiltered(criteria)) {
				return true;
			}
		}
		return false;
	}

	boolean isMultiValueProperty(String propValue, String modePropValue) {
		return (modePropValue != null) && (propValue.indexOf(",") != -1);
	}

	/**
	 * Helper method used to construct lucene query fragment for a multi-valued property.
	 *
	 * @param propName property name
	 * @param propValue property value (comma separated)
	 * @param operand logical operand that should be used
	 * @param pseudo is it a pseudo property
	 * @return lucene query with multi-valued property
	 */
	String processMultiValue(String propName, String propValue, String operand, boolean pseudo) {

		String[] multiValue = propValue.split(",");
		StringBuilder formQuery = new StringBuilder();
		if(operand == null || operand.isBlank()) {
			operand = "OR";
		}
		
		for (var i = 0; i < multiValue.length; i++) {

			if (i > 0) {
				formQuery.append(' ' + operand + ' ');
			}

			if (pseudo) {
				formQuery.append("(cm:content." + propName + ":\"" + multiValue[i] + "\")");
			} else {
				formQuery.append(  QName.createQName(propName, namespaceService) + ":("
						+ cleanValue(multiValue[i], false) + ")");
			}
		}

		return formQuery.toString();
	}

	/*
	 * @return true if it is tied to a list of properties, false otherwise
	 */
	private boolean isListProperty(Map<String, String> criteriaMap, String prop) {
		return (prop.indexOf("isListProperty") != -1) || criteriaMap.containsKey(prop + "_isListProperty");
	}

	/**
	 * Helper method used to determine whether the property is tied to categories.
	 *
	 * @param formJSON the list of the properties provided to the form
	 * @param prop propertyname
	 * @return true if it is tied to categories, false otherwise
	 */
	private boolean isCategoryProperty(Map<String, String> criteriaMap, String prop) {
		return (prop.indexOf("usesubcats") != -1) || (prop.indexOf("isCategory") != -1) || criteriaMap.containsKey(prop + "_usesubcats")
				|| criteriaMap.containsKey(prop + "_isCategory");
	}

	/**
	 * Helper method used to construct lucene query fragment for a default category property.
	 *
	 * @param cats the selected categories (array of string noderef)
	 * @param useSubCats boolean that indicates if should search also in subcategories
	 * @return lucene query with default category property
	 */
	private String processDefaultCategoryProperty(String[] cats, boolean useSubCats) {
		boolean firstCat = true;
		StringBuilder catQuery = new StringBuilder();
		final Map<String, String> cache = new HashMap<>();
		for (String cat : cats) {

			NodeRef catNode = new NodeRef(cat);

			final StringBuilder buf = new StringBuilder(128);
			final Path path = nodeService.getPath(catNode);
			for (final Path.Element e : path) {
				if (e instanceof Path.ChildAssocElement) {
					final QName qname = ((Path.ChildAssocElement) e).getRef().getQName();
					if (qname != null) {
						String prefix = cache.get(qname.getNamespaceURI());
						if (prefix == null) {
							// first request for this namespace prefix, get and cache result
							Collection<String> prefixes = namespaceService.getPrefixes(qname.getNamespaceURI());
							prefix = !prefixes.isEmpty() ? prefixes.iterator().next() : "";
							cache.put(qname.getNamespaceURI(), prefix);
						}
						buf.append('/');
						if (prefix.length() > 0) {
							buf.append(prefix).append(':');
						}
						buf.append(ISO9075.encode(qname.getLocalName()));
					}
				} else {
					buf.append('/').append(e.toString());
				}
			}

			catQuery.append((firstCat ? "" : " OR ") + "PATH:\"" + buf.toString() + (useSubCats ? "//*\"" : "/member\""));

			firstCat = false;

		}
		return catQuery.toString();
	}

	/**
	 * Helper method used to construct lucene query fragment for a custom category property.
	 *
	 * @param propName property name
	 * @param cats the selected categories (array of string noderef)
	 * @param useSubCats boolean that indicates if should search also in subcategories
	 * @return lucene query with custom category property
	 */
	private String processDefaultTagProperty(String[] cats) {
		StringBuilder catQuery = new StringBuilder();

		boolean first = true;
		for (String cat : cats) {

			NodeRef catNode = new NodeRef(cat);

			catQuery.append((first ? "" : " OR ") + "TAG:\"" + nodeService.getProperty(catNode, ContentModel.PROP_NAME) + "\"");
			first = false;
		}

		return catQuery.toString();
	}

}
