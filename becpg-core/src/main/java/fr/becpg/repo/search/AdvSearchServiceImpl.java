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
package fr.becpg.repo.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.permission.BeCPGPermissionFilter;
import fr.becpg.repo.search.permission.impl.ReadPermissionFilter;

/**
 * This class do a search on the repository (association, properties and
 * productLists), for the UI so it respects rights.
 * 
 * @author querephi
 * 
 */
public class AdvSearchServiceImpl implements AdvSearchService {

	private static final String CRITERIA_ING = "assoc_bcpg_ingListIng_added";

	private static final String CRITERIA_GEO_ORIGIN = "assoc_bcpg_ingListGeoOrigin_added";

	private static final String CRITERIA_BIO_ORIGIN = "assoc_bcpg_ingListBioOrigin_added";

	private static final String CRITERIA_PACK_LABEL = "assoc_pack_llLabel_added";


	private static Log logger = LogFactory.getLog(AdvSearchServiceImpl.class);

	private NodeService nodeService;


	private NamespaceService namespaceService;

	private BeCPGSearchService beCPGSearchService;

	private PermissionService permissionService;

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	public List<NodeRef> queryAdvSearch(String searchQuery, String language, QName datatype, Map<String, String> criteria, Map<String, Boolean> sortMap, int maxResults) {

		if (maxResults <= 0) {
			maxResults = RepoConsts.MAX_RESULTS_1000;
		}

		searchQuery = appendCriteria(searchQuery, language, criteria);

		boolean isAssocSearch = isAssocSearch(criteria);

		List<NodeRef> nodes = beCPGSearchService.search(searchQuery, sortMap, isAssocSearch ? RepoConsts.MAX_RESULTS_UNLIMITED : maxResults, language);

		if (isAssocSearch) {
			nodes = filterByAssociations(nodes, criteria);

//		TODO search plugin	if (datatype != null && dictionaryService.isSubClass(datatype, BeCPGModel.TYPE_PRODUCT)) {
//				nodes = getSearchNodesByIngListCriteria(nodes, criteria);
//				nodes = getSearchNodesByLabelingCriteria(nodes, criteria);
//			}
		}

		nodes = filterWithPermissions(nodes, new ReadPermissionFilter(), maxResults);

		return nodes;
	}

	@Override
	public String getSearchQueryByProperties(QName datatype, String term, String tag, boolean isRepo, String siteId, String containerId) {
		String ftsQuery = "";
		// Simple keyword search and tag specific search
		if (term != null && term.length() != 0) {
			ftsQuery = term + " ";
		} else if (tag != null && tag.length() != 0) {
			ftsQuery = "TAG:" + tag;
		}

		// we processed the search terms, so suffix the PATH query

		if (!isRepo) {
			ftsQuery = LuceneHelper.getSiteSearchPath(siteId, containerId) + (ftsQuery.length() > 0 ? " AND (" + ftsQuery + ")" : "");
		}

		if (datatype != null) {
			ftsQuery = "+TYPE:\"" + datatype + "\"" + (ftsQuery.length() > 0 ? " AND (" + ftsQuery + ")" : "");
		}

		ftsQuery += " AND -TYPE:\"cm:thumbnail\" " + "AND -TYPE:\"cm:failedThumbnail\" " + "AND -TYPE:\"cm:rating\" " + "AND -TYPE:\"bcpg:entityListItem\" "
				+ "AND -TYPE:\"systemfolder\" " + "AND -TYPE:\"rep:report\"";

		// extract data type for this search - advanced search query is type
		// specific
		ftsQuery += " AND -ASPECT:\"bcpg:hiddenFolder\"" + " AND -ASPECT:\"bcpg:compositeVersion\""
				+ " AND -ASPECT:\"bcpg:entityTplAspect\"";

		if (logger.isDebugEnabled()) {
			logger.debug(" build searchQueryByProperties :" + ftsQuery);
		}

		return ftsQuery;
	}

	private boolean isAssocSearch(Map<String, String> criteria) {
		if (criteria != null) {
			for (Map.Entry<String, String> criterion : criteria.entrySet()) {
				String key = criterion.getKey();
				// association
				if (key.startsWith("assoc_")) {
					return true;
				}
			}
		}
		return false;
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

	private String appendCriteria(String query, String language, Map<String, String> criteria) {
		if (criteria != null && !criteria.isEmpty()) {

			String formQuery = "";

			for (Map.Entry<String, String> criterion : criteria.entrySet()) {

				String key = criterion.getKey();
				String propValue = criterion.getValue();
				String operator = (language == SearchService.LANGUAGE_FTS_ALFRESCO) ? (formQuery.length() < 1 ? "" : " AND ") : " +";

				if (!propValue.isEmpty()) {
					// properties
					if (key.startsWith("prop_")) {

						// found a property - is it namespace_propertyname or
						// pseudo property format?
						String propName = key.substring(5);
						if (propName.contains("_")) {

							// property name - convert to DD property name
							// format
							if (language == SearchService.LANGUAGE_FTS_ALFRESCO) {
								propName = propName.replace("_", ":");
							} else {
								propName = "@" + propName.replace("_", "\\:");
							}

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
									formQuery += operator + propName + ":\"" + from + "\"..\"" + to + "\"";
								}
							} else if (propName.contains("productHierarchy")) {
								String hierarchyQuery = getHierarchyQuery(propName, propValue);
								if (hierarchyQuery != null && hierarchyQuery.length() > 0) {
									if (language == SearchService.LANGUAGE_FTS_ALFRESCO) {
										formQuery += operator + propName + ":(" + hierarchyQuery + ")";
									} else {
										formQuery += operator + propName + ":\"" + hierarchyQuery + "\"";
									}
								}

							} else if (propName.endsWith("depthLevel")) {
								Integer maxLevel = null;
								try {
									maxLevel = Integer.parseInt(propValue);
								} catch (Exception e) {
									// do nothing
								}
								if (maxLevel != null) {
									formQuery += operator + propName + ":[0 TO " + propValue + "]";
								}
							} else if (!propName.contains("llPosition") ){

								// beCPG - bug fix : pb with operator -, AND, OR
								// poivre AND -noir
								// poivre AND noir
								// sushi AND (saumon OR thon) AND -dorade
								// formQuery += (first ? "" : " AND ") +
								// propName + ":\"" + propValue + "\"";

								if (language == SearchService.LANGUAGE_FTS_ALFRESCO) {
									formQuery += operator + propName + ":(" + propValue + ")";
								} else {
									formQuery += operator + propName + ":\"" + propValue + "\"";
								}
							}
						} else {
							// pseudo cm:content property - e.g. mimetype, size
							// or encoding
							formQuery += operator + "cm:content." + propName + ":\"" + propValue + "\"";
						}
					}
				}
			}

			if (query != null && query.length() > 0 && formQuery.length() > 0) {
				query += " AND (" + formQuery + ")";
			} else if (formQuery.length() > 0) {
				query = formQuery;
			}

		}

		return query;
	}

	private String getHierarchyQuery(String propName, String hierachyName) {
		List<NodeRef> nodes = null;

		if (!NodeRef.isNodeRef(hierachyName)) {

			// TODO use HierarchyService, not generic
			String searchQuery = String
					.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_BY_NAME,
							LuceneHelper.encodePath(RepoConsts.PATH_SYSTEM + "/" + RepoConsts.PATH_PRODUCT_HIERARCHY + "/"
									+ BeCPGModel.ASSOC_ENTITYLISTS.toPrefixString(namespaceService)), hierachyName);

			if (propName.endsWith("productHierarchy1")) {
				searchQuery += " +@bcpg\\:depthLevel:1";
			}
			nodes = beCPGSearchService.luceneSearch(searchQuery, -1);
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

	/**
	 * Take in account criteria on associations (ie :
	 * assoc_bcpg_supplierAssoc_added)
	 * 
	 * @return filtered list of nodes by associations
	 */
	private List<NodeRef> filterByAssociations(List<NodeRef> nodes, Map<String, String> criteria) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		for (Map.Entry<String, String> criterion : criteria.entrySet()) {

			String key = criterion.getKey();
			String propValue = criterion.getValue();

			// association
			if (key.startsWith("assoc_") && !propValue.isEmpty()) {

				String assocName = key.substring(6);
				if (assocName.endsWith("_added")) {
					// TODO : should be generic
					if (!key.equals(CRITERIA_ING) && !key.equals(CRITERIA_GEO_ORIGIN) && !key.equals(CRITERIA_BIO_ORIGIN) && !key.equals(CRITERIA_PACK_LABEL)) {

						assocName = assocName.substring(0, assocName.length() - 6);
						assocName = assocName.replace("_", ":");
						QName assocQName = QName.createQName(assocName, namespaceService);

						String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
						for (String strNodeRef : arrValues) {

							NodeRef nodeRef = new NodeRef(strNodeRef);

							if (nodeService.exists(nodeRef)) {

								List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, assocQName);

								// remove nodes that don't respect the
								// assoc_ criteria
								List<NodeRef> nodesToKeep = new ArrayList<NodeRef>();

								for (AssociationRef assocRef : assocRefs) {

									nodesToKeep.add(assocRef.getSourceRef());
								}

								nodes.retainAll(nodesToKeep);
							}

						}
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("filterByAssociations executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

		return nodes;
	}


}
