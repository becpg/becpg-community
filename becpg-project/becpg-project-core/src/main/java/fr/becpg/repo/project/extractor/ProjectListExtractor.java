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
package fr.becpg.repo.project.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityExtractorService;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>ProjectListExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProjectListExtractor extends SimpleExtractor {

	private static final String PREF_FOLDER_FAVOURITES = "org.alfresco.share.folders.favourites";
	private static final String PROP_IS_FAVOURITE = "isFavourite";
	private static final String FILTER_DATA = "filterData";
	private static final String PAGINATION = "pagination";

	private static final String VIEW_TASKS = "tasks";
	private static final String VIEW_RESOURCES = "resources";
	private static final String FILTER_ENTITY_PROJECTS = "entity-projects";
	private static final String FILTER_FAVOURITES = "favourites";
	private static final String FILTER_TASKS = "tasks";
	private static final String FILTER_MY_TASKS = "my-tasks";
	private static final String FILTER_MY_PROJECTS = "my-projects";
	private static final String FILTER_PROJECTS = "projects";

	private static final String PROJECT_LIST = "projectList";

	private static final String PROP_PROJECT_STATE = "prop_pjt_projectState";

	private static final String PROP_PROJECT_LEGEND = "prop_pjt_projectLegends";

	private static final String PROP_SORT = "sort";

	private String projectSearchTemplate = "%(cm:name bcpg:code cm:title)";

	private ProjectService projectService;

	private PersonService personService;

	private PreferenceService preferenceService;

	private SecurityService securityService;


	private EntityActivityExtractorService entityActivityExtractorService;
	
	private static final Log logger = LogFactory.getLog(ProjectListExtractor.class);
	
	/**
	 * <p>Setter for the field <code>entityActivityExtractorService</code>.</p>
	 *
	 * @param entityActivityExtractorService a {@link fr.becpg.repo.activity.EntityActivityExtractorService} object
	 */
	public void setEntityActivityExtractorService(EntityActivityExtractorService entityActivityExtractorService) {
		this.entityActivityExtractorService = entityActivityExtractorService;
	}
	
	/**
	 * <p>Setter for the field <code>projectService</code>.</p>
	 *
	 * @param projectService a {@link fr.becpg.repo.project.ProjectService} object.
	 */
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object.
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>Setter for the field <code>preferenceService</code>.</p>
	 *
	 * @param preferenceService a {@link org.alfresco.service.cmr.preference.PreferenceService} object.
	 */
	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}


	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		List<NodeRef> favorites = getFavorites();

		List<NodeRef> results = getListNodeRef(dataListFilter, dataListFilter.getPagination(), favorites);

		Map<String, Object> props = new HashMap<>();
		if (FILTER_MY_TASKS.equals(dataListFilter.getFilterId()) || FILTER_TASKS.equals(dataListFilter.getFilterId())
				|| VIEW_RESOURCES.equals(dataListFilter.getExtraParams()) || VIEW_TASKS.equals(dataListFilter.getExtraParams())) {
			props.put(PROP_ACCESSRIGHT, dataListFilter.hasWriteAccess() && (securityService.computeAccessMode(dataListFilter.getEntityNodeRef(),
					ProjectModel.TYPE_PROJECT, ProjectModel.TYPE_TASK_LIST) == SecurityService.WRITE_ACCESS));
		} else {
			props.put(PROP_ACCESSRIGHT, dataListFilter.hasWriteAccess());
		}

		props.put(FILTER_DATA, dataListFilter);
		props.put(PAGINATION, dataListFilter.getPagination());

		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

		for (NodeRef nodeRef : results) {
			if (permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				if (!nodeService.exists(nodeRef)) {
					logger.error("NodeRef doesn't exist ? " + nodeRef.toString());
				} else {
					if (ret.getComputedFields() == null) {
						ret.setComputedFields(attributeExtractorService.readExtractStructure(nodeService.getType(nodeRef), metadataFields));
					}
					if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
						ret.addItem(extractExport(RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat()) ? FormatMode.XLSX : FormatMode.CSV,
								nodeRef, ret.getComputedFields(), props, cache));
					} else {
						Map<String, Object> extracted = extractJSON(nodeRef, ret.getComputedFields(), props, cache);
						extracted.put(PROP_IS_FAVOURITE, favorites.contains(nodeRef));
						ret.addItem(extracted);
					}

				}
			}
		}

		ret.setFullListSize(dataListFilter.getPagination().getFullListSize());

		return ret;
	}

	private List<NodeRef> getFavorites() {

		Map<String, Serializable> preferences = preferenceService.getPreferences(AuthenticationUtil.getFullyAuthenticatedUser());

		String favorites = (String) preferences.get(PREF_FOLDER_FAVOURITES);

		List<NodeRef> ret = new ArrayList<>();

		if (logger.isDebugEnabled()) {
			logger.debug("Favourites: " + favorites);
		}

		if (favorites != null) {
			for (String favorite : favorites.split(",")) {
				if (favorite != null && !favorite.isBlank()) {
					try {
						ret.add(new NodeRef(favorite));
					} catch (MalformedNodeRefException e) {
						logger.warn("Favorite nodeRef is malformed : " + favorite);
					}
				}
			}
		}
		return ret;
	}

	private List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination, List<NodeRef> favorites) {

		List<NodeRef> results = paginatedSearchCache.getSearchResults(pagination.getQueryExecutionId());

		if (results == null) {

			results = new LinkedList<>();
			// pjt:project
			if (dataListFilter.isDefaultSort()) {
				Map<String, Boolean> sortMap = new LinkedHashMap<>();
				sortMap.put("@cm:created", false);
				dataListFilter.setSortMap(sortMap);
			}

			BeCPGQueryBuilder beCPGQueryBuilder = dataListFilter.getSearchQuery().excludeDefaults().andOperator()
					.inSearchTemplate(projectSearchTemplate);

			if (FILTER_ENTITY_PROJECTS.equals(dataListFilter.getFilterId())) {
				results = associationService.getSourcesAssocs(dataListFilter.getEntityNodeRef(), ProjectModel.ASSOC_PROJECT_ENTITY);
			} else {

				if (dataListFilter.isSimpleItem()) {
					results.add(dataListFilter.getNodeRef());
				} else {
					results = extractProjectResults(dataListFilter, pagination, favorites, beCPGQueryBuilder);
				}
			}
			
			if (dataListFilter.getSortId() != null) {
				DataListSortPlugin plugin = dataListSortRegistry.getPluginById(dataListFilter.getSortId());
				if (plugin != null) {
					plugin.sort(results, dataListFilter.getSortMap());
				}
			}
			pagination.setQueryExecutionId(paginatedSearchCache.storeSearchResults(results));

		}

		return pagination.paginate(results);
	}

	private List<NodeRef> extractProjectResults(DataListFilter dataListFilter, DataListPagination pagination, List<NodeRef> favorites,
			BeCPGQueryBuilder beCPGQueryBuilder) {
		List<NodeRef> results;
		if (dataListFilter.getCriteriaMap() == null) {
			dataListFilter.setCriteriaMap(new HashMap<>());
		}

		List<NodeRef> projectResults = null;
		
		boolean isTaskOrViewResources = FILTER_TASKS.equals(dataListFilter.getFilterId()) || FILTER_MY_TASKS.equals(dataListFilter.getFilterId())
				|| (VIEW_RESOURCES.equals(dataListFilter.getExtraParams()) || VIEW_TASKS.equals(dataListFilter.getExtraParams()));

		if (isTaskOrViewResources && securityService.computeAccessMode(dataListFilter.getEntityNodeRef(), ProjectModel.TYPE_PROJECT,
					ProjectModel.TYPE_TASK_LIST) == SecurityService.NONE_ACCESS) {
			return new ArrayList<>();
		}
		
		if (FILTER_MY_PROJECTS.equals(dataListFilter.getFilterId()) || !isTaskOrViewResources) {
			projectResults = getProjectResults(dataListFilter, beCPGQueryBuilder, pagination);
		}
		
		if (isTaskOrViewResources) {
			if (FILTER_PROJECTS.equals(dataListFilter.getFilterId())) {
				beCPGQueryBuilder.clearFTSQuery();
			}

			QName dataType = ProjectModel.TYPE_TASK_LIST;
			beCPGQueryBuilder.ofType(dataType);

			beCPGQueryBuilder.excludeProp(ProjectModel.PROP_TL_IS_EXCLUDE_FROM_SEARCH, Boolean.TRUE.toString());
			beCPGQueryBuilder.excludeProp(ProjectModel.PROP_TL_IS_GROUP, Boolean.TRUE.toString());

			if ((dataListFilter.getCriteriaMap() != null) && !dataListFilter.getCriteriaMap().containsKey("prop_pjt_tlState")
					&& ((dataListFilter.getFilterParams() == null) || !dataListFilter.getFilterParams().contains("tlState"))) {
				dataListFilter.getCriteriaMap().put("prop_pjt_tlState", TaskState.InProgress.toString());
			}

			if ((dataListFilter.getCriteriaMap() != null)) {
				if (dataListFilter.getCriteriaMap().containsKey(PROP_PROJECT_STATE)) {
					dataListFilter.getCriteriaMap().remove(PROP_PROJECT_STATE);
				}

				if (dataListFilter.getCriteriaMap().containsKey(PROP_PROJECT_LEGEND)) {
					dataListFilter.getCriteriaMap().put("assoc_pjt_tlTaskLegend_added", dataListFilter.getCriteriaMap().get(PROP_PROJECT_LEGEND));
					dataListFilter.getCriteriaMap().remove(PROP_PROJECT_LEGEND);
				}
			}
				
			if (FILTER_MY_TASKS.equals(dataListFilter.getFilterId())) {
				NodeRef currentUserNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
				dataListFilter.getCriteriaMap().put("assoc_pjt_tlResources_added", currentUserNodeRef.toString());
				results = advSearchService.queryAdvSearch(dataType, beCPGQueryBuilder, dataListFilter.getCriteriaMap(), pagination.getMaxResults());
			} else {
				results = advSearchService.queryAdvSearch(dataType, beCPGQueryBuilder, dataListFilter.getCriteriaMap(), pagination.getMaxResults());
				
				if (VIEW_RESOURCES.equals(dataListFilter.getExtraParams())) {
					for (Iterator<NodeRef> iterator = results.iterator(); iterator.hasNext();) {
						NodeRef nodeRef = iterator.next();
						if ((associationService.getTargetAssoc(nodeRef, ProjectModel.ASSOC_TL_RESOURCES) == null)) {
							iterator.remove();
						}
					}
				}
				
				if (projectResults != null) {
					List<NodeRef> tempResults = new ArrayList<>();
					for (Iterator<NodeRef> iterator = results.iterator(); iterator.hasNext() && tempResults.size() < RepoConsts.MAX_RESULTS_1000;) {
						NodeRef nodeRef = iterator.next();
						NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
						if (!projectResults.contains(entityNodeRef)) {
							iterator.remove();
						} else {
							tempResults.add(nodeRef);
						}
					}
					results = tempResults;
				}
			}

		} else {
			results = projectResults;
		}

		if (FILTER_FAVOURITES.equals(dataListFilter.getFilterId()) && results != null) {
			logger.debug("Keep only favorites");
			results.retainAll(favorites);
		}
		return results;
	}

	private List<NodeRef> getProjectResults(DataListFilter dataListFilter, BeCPGQueryBuilder beCPGQueryBuilder, DataListPagination pagination) {

		List<NodeRef> results = null;
		List<NodeRef> unionResults = new LinkedList<>();
		QName dataType = ProjectModel.TYPE_PROJECT;
		beCPGQueryBuilder.ofType(ProjectModel.TYPE_PROJECT);

		Map<String, String> criteriaMap = new HashMap<>();

		Integer maxResults = pagination.getMaxResults();

		if (!VIEW_RESOURCES.equals(dataListFilter.getExtraParams()) && !VIEW_TASKS.equals(dataListFilter.getExtraParams())) {
			criteriaMap.putAll(dataListFilter.getCriteriaMap());
		}

		if (FILTER_MY_PROJECTS.equals(dataListFilter.getFilterId())) {
			String userName = AuthenticationUtil.getFullyAuthenticatedUser();

			NodeRef currentUserNodeRef = personService.getPerson(userName);
			BeCPGQueryBuilder creatorQuery = dataListFilter.getSearchQuery().excludeDefaults().clone().ofType(ProjectModel.TYPE_PROJECT);

			if (!criteriaMap.containsKey(PROP_PROJECT_STATE)
					&& ((dataListFilter.getFilterParams() == null) || !dataListFilter.getFilterParams().contains("projectState"))) {
				creatorQuery.andPropQuery(ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress.toString());
			} else {
				if (criteriaMap.containsKey(PROP_PROJECT_STATE)) {
					creatorQuery.andPropQuery(ProjectModel.PROP_PROJECT_STATE, criteriaMap.get(PROP_PROJECT_STATE));
				} else {
					creatorQuery.andFTSQuery(dataListFilter.getFilterParams());
				}
			}

			creatorQuery.andPropEquals(ProjectModel.PROP_PROJECT_OWNERS, currentUserNodeRef.toString());

			unionResults.addAll(creatorQuery.list());

			if (!criteriaMap.containsKey(PROP_PROJECT_STATE)
					&& ((dataListFilter.getFilterParams() == null) || !dataListFilter.getFilterParams().contains("projectState"))) {
				criteriaMap.put(PROP_PROJECT_STATE, ProjectState.InProgress.toString());
			}

		}

		results = advSearchService.queryAdvSearch(dataType, beCPGQueryBuilder.clone(), criteriaMap, maxResults);

		if (unionResults != null) {
			for (NodeRef tmp : unionResults) {
				if ((tmp != null) && !results.contains(tmp)) {
					results.add(tmp);
				}
			}
		}

		return results;

	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields, FormatMode mode,
			Map<QName, Serializable> properties, final Map<String, Object> props, final Map<NodeRef, Map<String, Object>> cache) {

		Map<String, Object> ret = attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, mode,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field, FormatMode mode) {
						DataListPagination pagination = (DataListPagination) props.get(PAGINATION);
						List<Map<String, Object>> ret = new ArrayList<>();
						if (field.isDataListItems()) {
							if ((ProjectModel.TYPE_TASK_LIST.equals(field.getFieldQname()) && (pagination.getPageSize() > 10))
									|| BeCPGModel.TYPE_ACTIVITY_LIST.equals(field.getFieldQname())
									|| (ProjectModel.TYPE_DELIVERABLE_LIST.equals(field.getFieldQname())
											&& ProjectModel.TYPE_TASK_LIST.equals(itemType))) {
								// Only in progress tasks
								if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(field.getFieldQname())) {

									Map<String, Object> tmp = new HashMap<>(4);

									Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
									Map<String, Boolean> userAccess = new HashMap<>(1);

									permissions.put("userAccess", userAccess);
									userAccess.put("edit", false);

									tmp.put(PROP_TYPE, BeCPGModel.TYPE_ACTIVITY_LIST);
									
									tmp.put(PROP_PERMISSIONS, permissions);
									if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(field.getFieldQname())) {
										String currentComment = (String) nodeService.getProperty(nodeRef, ProjectModel.PROP_PROJECT_CUR_COMMENT);
										
										if (currentComment != null && !currentComment.isBlank()) {
											tmp.put(PROP_NODEDATA, entityActivityExtractorService.extractAuditActivityData(new JSONObject(currentComment), metadataFields, FormatMode.JSON));
										}
									}
									ret.add(tmp);
								} else {
									List<NodeRef> assocRefs = new ArrayList<>();
									if ((ProjectModel.TYPE_TASK_LIST.equals(field.getFieldQname()))) {
										assocRefs = associationService.getTargetAssocs(nodeRef, ProjectModel.ASSOC_PROJECT_CUR_TASKS);
									} else {
										assocRefs = associationService.getSourcesAssocs(nodeRef, ProjectModel.ASSOC_DL_TASK);
									}
									
									for (NodeRef itemNodeRef : assocRefs) {
										if ((permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED)
												&& (securityService.computeAccessMode(nodeRef, itemType, field.getFieldQname()) >= SecurityService.READ_ACCESS)) {
											
											Map<String, Object> tmp = new HashMap<>(4);
											
											Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
											Map<String, Boolean> userAccess = new HashMap<>(1);
											
											permissions.put("userAccess", userAccess);
											userAccess.put("edit",
													(permissionService.hasPermission(itemNodeRef, "Write") == AccessStatus.ALLOWED) && (securityService
															.computeAccessMode(nodeRef,itemType, field.getFieldQname()) == SecurityService.WRITE_ACCESS));
											
											QName itemType = nodeService.getType(itemNodeRef);
											Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
											tmp.put(PROP_TYPE, itemType.toPrefixString(services.getNamespaceService()));
											tmp.put(PROP_NODE, itemNodeRef.toString());
											
											tmp.put(PROP_SORT, nodeService.getProperty(itemNodeRef, BeCPGModel.PROP_SORT));
											
											tmp.put(PROP_PERMISSIONS, permissions);
											
											Map<String, Object> metadata = doExtract(itemNodeRef, itemType, field.getChildrens(), mode, properties,
													props, cache);
											if ((ProjectModel.TYPE_TASK_LIST.equals(field.getFieldQname()))) {
												NodeRef subProjectNoderef = associationService.getTargetAssoc(itemNodeRef,
														ProjectModel.ASSOC_SUB_PROJECT);
												if (subProjectNoderef != null) {
													HashMap<String, Object> commentCount = new HashMap<>(6);
													Integer count = (Integer) nodeService.getProperty(subProjectNoderef,
															ForumModel.PROP_COMMENT_COUNT);
													commentCount.put("displayValue", count);
													commentCount.put("value", count);
													commentCount.put("metadata", "int");
													metadata.put("prop_fm_commentCount", commentCount);
												}
											}
											tmp.put(PROP_NODEDATA, metadata);
											ret.add(tmp);
										}
									}
								}
							} else {

								NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
								NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());
								if (listNodeRef != null) {
									List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

									for (NodeRef itemNodeRef : results) {
										if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {

											Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
											Map<String, Boolean> userAccess = new HashMap<>(1);

											permissions.put("userAccess", userAccess);
											userAccess.put("edit", permissionService.hasPermission(itemNodeRef, "Write") == AccessStatus.ALLOWED);

											Map<String, Object> tmp = new HashMap<>(3);
											QName itemType = nodeService.getType(itemNodeRef);
											Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
											tmp.put(PROP_TYPE, itemType.toPrefixString(services.getNamespaceService()));
											tmp.put(PROP_NODE, itemNodeRef.toString());
											tmp.put(PROP_PERMISSIONS, permissions);
											tmp.put(PROP_NODEDATA,
													doExtract(itemNodeRef, itemType, field.getChildrens(), mode, properties, props, cache));
											ret.add(tmp);
										}
									}
								}
							}
						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, cache, mode, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs;
								if (((AssociationDefinition) field.getFieldDef()).isChild()) {
									assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
								}
								for (NodeRef itemNodeRef : assocRefs) {
									addExtracted(itemNodeRef, field, cache, mode, ret);
								}

							}
						}

						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, Map<NodeRef, Map<String, Object>> cache,
							FormatMode mode, List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								if (FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) {
									ret.add(extractExport(mode, itemNodeRef, field.getChildrens(), props, cache));
								} else {
									ret.add(extractJSON(itemNodeRef, field.getChildrens(), props, cache));
								}
							}
						}
					}

				});

		extractTaskListResources(nodeRef, mode, itemType, ret);

		return ret;
	}

	private void extractTaskListResources(NodeRef nodeRef, FormatMode mode, QName itemType, Map<String, Object> ret) {

		if (FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) {
			String resources = "";
			if (entityDictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				for (NodeRef resourceNodeRef : projectService.extractResources(entityListDAO.getEntity(nodeRef),
						associationService.getTargetAssocs(nodeRef, ProjectModel.ASSOC_TL_RESOURCES))) {
					if (!resources.isEmpty()) {
						resources += ",";
					}
					resources += extractResourcePropName(resourceNodeRef);
				}
			}

			if (!resources.isEmpty()) {
				ret.put("assoc_pjt_tlResources", resources);
			}
		} else {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> resources = (List<Map<String, Object>>) ret.get("assoc_pjt_tlResources");
			if ((resources == null) || resources.isEmpty()) {
				return;
			}
			for (Map<String, Object> resource : resources) {
				NodeRef resourceRef = new NodeRef((String) resource.get("value"));
				for (NodeRef extractedResourceRef : projectService.extractResources(entityListDAO.getEntity(nodeRef), Arrays.asList(resourceRef))) {
					resource.put("value", extractedResourceRef.toString());
					resource.put("metadata", extractResourcePropName(extractedResourceRef));
					resource.put("displayValue", resource.get("metadata"));
				}
			}

		}

	}

	private String extractResourcePropName(NodeRef resourceRef) {
		return (String) nodeService.getProperty(resourceRef, nodeService.getType(resourceRef).equals(ContentModel.TYPE_AUTHORITY_CONTAINER)
				? (nodeService.getProperty(resourceRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME) != null ? ContentModel.PROP_AUTHORITY_DISPLAY_NAME
						: ContentModel.PROP_AUTHORITY_NAME)
				: ContentModel.PROP_USERNAME);
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().equals(PROJECT_LIST);
	}

	/** {@inheritDoc} */
	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

}
