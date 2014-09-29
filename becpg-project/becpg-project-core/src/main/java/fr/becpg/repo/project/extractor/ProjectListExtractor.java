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
package fr.becpg.repo.project.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class ProjectListExtractor extends SimpleExtractor {

	private static final String PREF_FOLDER_FAVOURITES = "org.alfresco.share.documents.favourites";
	private static final String PROP_IS_FAVOURITE = "isFavourite";
	private static final String FILTER_DATA = "filterData";
	private static final String PAGINATION = "pagination";

	private static final String VIEW_FAVOURITES = "favourites";
	private static final String VIEW_TASKS = "tasks";
	private static final String VIEW_MY_TASKS = "my-tasks";
	private static final String VIEW_ENTITY_PROJECTS = "entity-projects";
	private static final String PROJECT_LIST = "projectList";

	private PersonService personService;

	private AssociationService associationService;

	private PreferenceService preferenceService;

	private static Log logger = LogFactory.getLog(ProjectListExtractor.class);

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination,
			boolean hasWriteAccess) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());

		List<NodeRef> favorites = getFavorites();

		List<NodeRef> results = getListNodeRef(dataListFilter, pagination, favorites);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, hasWriteAccess);
		props.put(FILTER_DATA, dataListFilter);
		props.put(PAGINATION, pagination);

		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

		for (NodeRef nodeRef : results) {
			if (permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				if (!nodeService.exists(nodeRef)) {
					logger.error("NodeRef doesn't exist ? " + nodeRef.toString());
				} else {
					if (ret.getComputedFields() == null) {
						ret.setComputedFields(attributeExtractorService.readExtractStructure(nodeService.getType(nodeRef), metadataFields));
					}
					if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat())
							|| RepoConsts.FORMAT_XLS.equals(dataListFilter.getFormat())) {
						ret.addItem(extractExport( RepoConsts.FORMAT_XLS.equals(dataListFilter.getFormat())? AttributeExtractorMode.XLS: AttributeExtractorMode.CSV, nodeRef, ret.getComputedFields(), props, cache));
					} else {
						Map<String, Object> extracted = extractJSON(nodeRef, ret.getComputedFields(), props, cache);
						if (favorites.contains(nodeRef)) {
							extracted.put(PROP_IS_FAVOURITE, true);
						} else {
							extracted.put(PROP_IS_FAVOURITE, false);
						}

						ret.addItem(extracted);
					}

				}
			}
		}

		ret.setFullListSize(pagination.getFullListSize());

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
				try {
					ret.add(new NodeRef(favorite));
				} catch (MalformedNodeRefException e) {
					logger.warn("Favorite nodeRef is malformed : " + favorite);
				}
			}
		}
		return ret;
	}

	private List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination, List<NodeRef> favorites) {

		List<NodeRef> results = new ArrayList<NodeRef>();

		// pjt:project
		QName dataType = dataListFilter.getDataType();
		BeCPGQueryBuilder beCPGQueryBuilder = dataListFilter.getSearchQuery();

		if (VIEW_ENTITY_PROJECTS.equals(dataListFilter.getFilterId())) {
			results = associationService.getSourcesAssocs(dataListFilter.getEntityNodeRef(), ProjectModel.ASSOC_PROJECT_ENTITY);
		} else {

			if (dataListFilter.isSimpleItem()) {
				results.add(dataListFilter.getNodeRef());
			} else {

				if (VIEW_MY_TASKS.equals(dataListFilter.getFilterId()) || VIEW_TASKS.equals(dataListFilter.getFilterId())) {
					dataType = ProjectModel.TYPE_TASK_LIST;
					beCPGQueryBuilder.ofType(dataType);

				}

				results = advSearchService.queryAdvSearch(dataType, beCPGQueryBuilder, dataListFilter.getCriteriaMap(), pagination.getMaxResults());

				// Always should return project
				if (VIEW_MY_TASKS.equals(dataListFilter.getFilterId()) || VIEW_TASKS.equals(dataListFilter.getFilterId())) {
					if (VIEW_MY_TASKS.equals(dataListFilter.getFilterId())) {
						logger.debug("Keep only tasks for  " + AuthenticationUtil.getFullyAuthenticatedUser());
						NodeRef currentUserNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
						if (logger.isDebugEnabled()) {
							logger.debug("Retain : " + associationService.getSourcesAssocs(currentUserNodeRef, ProjectModel.ASSOC_TL_RESOURCES));
						}
						results.retainAll(associationService.getSourcesAssocs(currentUserNodeRef, ProjectModel.ASSOC_TL_RESOURCES));
					}
				}

				if (VIEW_FAVOURITES.equals(dataListFilter.getFilterId())) {
					logger.debug("Keep only favorites");
					results.retainAll(favorites);
				}
			}

		}

		if (dataListFilter.getSortId() != null) {
			DataListSortPlugin plugin = dataListSortRegistry.getPluginById(dataListFilter.getSortId());
			if (plugin != null) {
				plugin.sort(results);
			}
		}

		results = pagination.paginate(results);

		return results;
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			final AttributeExtractorMode mode, Map<QName, Serializable> properties, final Map<String, Object> props,
			final Map<NodeRef, Map<String, Object>> cache) {

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, mode,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field) {
						List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
						if (field.isDataListItems()) {
							NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
							NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());
							if (listNodeRef != null) {
								List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

								DataListPagination pagination = (DataListPagination) props.get(PAGINATION);
								TaskState taskState = null;
								if (pagination.getPageSize() > 10) {
									DataListFilter filter = (DataListFilter) props.get(FILTER_DATA);
									if (filter.getFilterData() != null) {
										try {
											taskState = TaskState.valueOf(filter.getFilterData());
										} catch (Exception e) {
											// Case filter data is incorrect
										}
									}
								}

								for (NodeRef itemNodeRef : results) {

									if ((taskState == null
											|| (ProjectModel.TYPE_TASK_LIST.equals(field.getFieldQname()) && taskState.toString().equals(
													nodeService.getProperty(itemNodeRef, ProjectModel.PROP_TL_STATE))) || ProjectModel.TYPE_DELIVERABLE_LIST
												.equals(field.getFieldQname()))
											&& permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {

										Map<String, Object> tmp = new HashMap<String, Object>(3);
										QName itemType = nodeService.getType(itemNodeRef);
										Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
										tmp.put(PROP_TYPE, itemType.toPrefixString(services.getNamespaceService()));
										tmp.put(PROP_NODE, itemNodeRef);
										tmp.put(PROP_NODEDATA, doExtract(itemNodeRef, itemType, field.getChildrens(), mode, properties, props, cache));
										ret.add(tmp);
									}
								}
							}
						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, cache, mode, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs = null;
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
							AttributeExtractorMode mode, List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								if (AttributeExtractorMode.CSV.equals(mode) || AttributeExtractorMode.XLS.equals(mode)) {
									ret.add(extractExport(mode, itemNodeRef, field.getChildrens(), props, cache));
								} else {
									ret.add(extractJSON(itemNodeRef, field.getChildrens(), props, cache));
								}
							}
						}
					}

				});
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return dataListFilter.getDataListName() != null && dataListFilter.getDataListName().equals(PROJECT_LIST);
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

}
