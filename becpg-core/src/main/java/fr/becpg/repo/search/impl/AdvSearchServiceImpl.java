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
package fr.becpg.repo.search.impl;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.search.AdvSearchPlugin;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.permission.BeCPGPermissionFilter;
import fr.becpg.repo.search.permission.impl.ReadPermissionFilter;

/**
 * This class do a search on the repository (association, properties and
 * productLists), for the UI so it respects rights.
 * 
 * @author querephi
 * 
 */

@Service("advSearchService")
public class AdvSearchServiceImpl implements AdvSearchService {

	private static Log logger = LogFactory.getLog(AdvSearchServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private PermissionService permissionService;

	@Autowired(required = false)
	private AdvSearchPlugin[] advSearchPlugins;

	@Autowired
	private HierarchyService hierarchyService;

	@Override
	public List<NodeRef> queryAdvSearch(QName datatype, BeCPGQueryBuilder beCPGQueryBuilder, Map<String, String> criteria, int maxResults) {

		if (maxResults <= 0) {
			maxResults = RepoConsts.MAX_RESULTS_1000;
		}

		addCriteriaMap(beCPGQueryBuilder, criteria);

		List<NodeRef> nodes = beCPGQueryBuilder.ftsLanguage().list();

		if (advSearchPlugins != null) {
			for (AdvSearchPlugin advSearchPlugin : advSearchPlugins) {
				nodes = advSearchPlugin.filter(nodes, datatype, criteria);
			}
		}

		nodes = filterWithPermissions(nodes, new ReadPermissionFilter(), maxResults);

		return nodes;
	}

	@Override
	public BeCPGQueryBuilder createSearchQuery(QName datatype, String term, String tag, boolean isRepo, String siteId, String containerId) {
		BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery();
		// Simple keyword search and tag specific search
		if (term != null && term.length() != 0) {
			beCPGQueryBuilder.andFTSQuery(term);
		} else if (tag != null && tag.length() != 0) {
			beCPGQueryBuilder.andFTSQuery("TAG:" + tag);
		}

		// we processed the search terms, so suffix the PATH query

		if (!isRepo) {
			beCPGQueryBuilder.inSite(siteId, containerId);
		}

		if (datatype != null) {
			beCPGQueryBuilder.ofType(datatype);
		}


		beCPGQueryBuilder.excludeSearch();

		return beCPGQueryBuilder;
	}

	private void addCriteriaMap(BeCPGQueryBuilder queryBuilder, Map<String, String> criteriaMap) {
		if (criteriaMap != null && !criteriaMap.isEmpty()) {

			for (Map.Entry<String, String> criterion : criteriaMap.entrySet()) {

				String key = criterion.getKey();
				String propValue = criterion.getValue();

				if (!propValue.isEmpty()) {
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

									String from = "", to = "";
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
										to = (sepindex == propValue.length() - 1 ? "MAX" : propValue.substring(sepindex + 1, sepindex + 11));
									} else {
										// simple range found
										propName = propName.substring(0, propName.length() - "-range".length());

										// work out if "min" and/or "max" are
										// specified - use MIN and MAX otherwise
										from = (sepindex == 0 ? "MIN" : propValue.substring(0, sepindex));
										to = (sepindex == propValue.length() - 1 ? "MAX" : propValue.substring(sepindex + 1));
									}

									queryBuilder.andBetween(QName.createQName(propName, namespaceService), from, to);

								}

							} else if (propName.contains("productHierarchy")) {
								String hierarchyQuery = getHierarchyQuery(propName, propValue);
								if (hierarchyQuery != null && hierarchyQuery.length() > 0) {

									queryBuilder.andPropQuery(QName.createQName(propName, namespaceService), hierarchyQuery);

								}

							} else if (propName.endsWith("depthLevel")) {
								Integer maxLevel = null;
								try {
									maxLevel = Integer.parseInt(propValue);
								} catch (Exception e) {
									// do nothing
								}
								if (maxLevel != null) {
									queryBuilder.andBetween(QName.createQName(propName, namespaceService), "0", propValue);

								}
							} else if (!propName.contains("llPosition")) {
								// beCPG - bug fix : pb with operator -,
								// AND, OR
								// poivre AND -noir
								// poivre AND noir
								// sushi AND (saumon OR thon) AND -dorade
								// formQuery += (first ? "" : " AND ") +
								// propName + ":\"" + propValue + "\"";
								queryBuilder.andPropQuery(QName.createQName(propName, namespaceService), propValue);
								// TODO
							}  else {
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

	private List<NodeRef> filterWithPermissions(List<NodeRef> nodes, BeCPGPermissionFilter filter, int maxResults) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		nodes = filter.filter(nodes, permissionService, maxResults);

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("filterWithPermissions executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}

	private String getHierarchyQuery(String propName, String hierachyName) {
		List<NodeRef> nodes = null;

		if (!NodeRef.isNodeRef(hierachyName)) {

			// TODO use HierarchyService, not generic
			// " +PATH:\"/app:company_home/%s//*\" +TYPE:\"bcpg:linkedValue\" +@bcpg\\:lkvValue:\"%s\" ";

			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder
					.createQuery()
					.inPath(RepoConsts.PATH_SYSTEM + "/" + RepoConsts.PATH_PRODUCT_HIERARCHY + "/"
							+ BeCPGModel.ASSOC_ENTITYLISTS.toPrefixString(namespaceService)).inType(BeCPGModel.TYPE_LINKED_VALUE)
					.andPropEquals(BeCPGModel.PROP_LKV_VALUE, hierachyName);

			if (propName.endsWith("productHierarchy1")) {
				queryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, "1");
			}
			nodes = queryBuilder.list();
		}
		String ret = "";
		if (nodes != null && !nodes.isEmpty()) {
			for (NodeRef node : nodes) {
				ret += " \"" + node.toString() + "\"";
			}
		} else {
			ret += "\"" + hierachyName + "\"";
		}

		return ret;
	}

}
