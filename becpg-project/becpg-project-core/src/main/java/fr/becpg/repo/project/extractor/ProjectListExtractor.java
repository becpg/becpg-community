/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
import java.util.Iterator;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.extractor.ActivityListExtractor;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class ProjectListExtractor extends ActivityListExtractor {

	private static final String PREF_FOLDER_FAVOURITES = "org.alfresco.share.folders.favourites";
	private static final String PROP_IS_FAVOURITE = "isFavourite";
	private static final String FILTER_DATA = "filterData";
	private static final String PAGINATION = "pagination";

	private static final String VIEW_FAVOURITES = "favourites";
	private static final String VIEW_TASKS = "tasks";
	private static final String VIEW_MY_TASKS = "my-tasks";
	private static final String VIEW_MY_PROJECTS = "my-projects";
	private static final String VIEW_PROJECTS = "projects";
	private static final String VIEW_RESOURCES = "resources";
	private static final String VIEW_ENTITY_PROJECTS = "entity-projects";
	private static final String PROJECT_LIST = "projectList";

	private PersonService personService;

	private AssociationService associationService;

	private PreferenceService preferenceService;

	private static final Log logger = LogFactory.getLog(ProjectListExtractor.class);

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
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		List<NodeRef> favorites = getFavorites();

		List<NodeRef> results = getListNodeRef(dataListFilter, dataListFilter.getPagination(), favorites);

		Map<String, Object> props = new HashMap<>();
		props.put(PROP_ACCESSRIGHT, dataListFilter.hasWriteAccess());
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
						ret.addItem(extractExport(RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat()) ? AttributeExtractorMode.XLSX
								: AttributeExtractorMode.CSV, nodeRef, ret.getComputedFields(), props, cache));
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

		List<NodeRef> results = new ArrayList<>();

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
					beCPGQueryBuilder.excludeProp(ProjectModel.PROP_TL_IS_EXCLUDE_FROM_SEARCH, Boolean.TRUE.toString());
				}

				if (VIEW_RESOURCES.equals(dataListFilter.getExtraParams())) {
					if (VIEW_PROJECTS.equals(dataListFilter.getFilterId())) {
						beCPGQueryBuilder.clearFTSQuery();

					}
					
					beCPGQueryBuilder.excludeProp(ProjectModel.PROP_TL_IS_EXCLUDE_FROM_SEARCH, Boolean.TRUE.toString());
					
					if(dataListFilter.getCriteriaMap()==null){
						dataListFilter.setCriteriaMap(new HashMap<String, String>());
					}

					if (dataListFilter.getCriteriaMap() != null && !dataListFilter.getCriteriaMap().containsKey("prop_pjt_tlState")) {
						dataListFilter.getCriteriaMap().put("prop_pjt_tlState", "\"Planned\",\"InProgress\"");
					} 

				}
				
				if(VIEW_MY_PROJECTS.equals(dataListFilter.getFilterId())){
					NodeRef currentUserNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
					if (dataListFilter.getCriteriaMap() == null) {
						dataListFilter.setCriteriaMap(new HashMap<>());	
					}
						
					dataListFilter.getCriteriaMap().put("assoc_pjt_projectManager_added", currentUserNodeRef.toString());
				}
				
				
				results = advSearchService.queryAdvSearch(dataType, beCPGQueryBuilder, dataListFilter.getCriteriaMap(), pagination.getMaxResults());

				if (VIEW_RESOURCES.equals(dataListFilter.getExtraParams())) {

					for (Iterator<NodeRef> iterator = results.iterator(); iterator.hasNext();) {
						NodeRef nodeRef = iterator.next();
						if (associationService.getTargetAssoc(nodeRef, ProjectModel.ASSOC_TL_RESOURCES) == null) {
							iterator.remove();
						}
					}

					if (VIEW_PROJECTS.equals(dataListFilter.getFilterId())) {
						BeCPGQueryBuilder projectQueryBuilder = dataListFilter.getSearchQuery();
						projectQueryBuilder.ofType(ProjectModel.TYPE_PROJECT);
						List<NodeRef> projectList = projectQueryBuilder.ftsLanguage().list();
						for (Iterator<NodeRef> iterator = results.iterator(); iterator.hasNext();) {
							NodeRef nodeRef = iterator.next();
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							if (!projectList.contains(entityNodeRef)) {
								iterator.remove();
							}
						}
					}

				}

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
				}else if (VIEW_FAVOURITES.equals(dataListFilter.getFilterId())) {
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
						List<Map<String, Object>> ret = new ArrayList<>();
						if (field.isDataListItems()) {

							DataListPagination pagination = (DataListPagination) props.get(PAGINATION);
						
						
							if ((ProjectModel.TYPE_TASK_LIST.equals(field.getFieldQname()) && pagination.getPageSize()>10)
									|| BeCPGModel.TYPE_ACTIVITY_LIST.equals(field.getFieldQname())) {
								// Only in progress tasks
								List<NodeRef> assocRefs;
								if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(field.getFieldQname())) {
									assocRefs = associationService.getTargetAssocs(nodeRef, ProjectModel.ASSOC_PROJECT_CUR_COMMENTS);
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, ProjectModel.ASSOC_PROJECT_CUR_TASKS);
								}

								for (NodeRef itemNodeRef : assocRefs) {

									if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {

										Map<String, Object> tmp = new HashMap<>(4);
										
										
										Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
										Map<String, Boolean> userAccess = new HashMap<>(1);

										permissions.put("userAccess", userAccess);
										userAccess.put("edit", permissionService.hasPermission(itemNodeRef, "Write") == AccessStatus.ALLOWED);

										QName itemType = nodeService.getType(itemNodeRef);
										Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
										tmp.put(PROP_TYPE, itemType.toPrefixString(services.getNamespaceService()));
										tmp.put(PROP_NODE, itemNodeRef);
										tmp.put(PROP_PERMISSIONS, permissions);
										if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(field.getFieldQname())) {
											Map<String, Object> tmp2 = doExtract(itemNodeRef, itemType, field.getChildrens(), mode, properties,
													props, cache);
											postLookupActivity(itemNodeRef, tmp2, properties, mode);
											tmp.put(PROP_NODEDATA, tmp2);
										} else {
											tmp.put(PROP_NODEDATA,
													doExtract(itemNodeRef, itemType, field.getChildrens(), mode, properties, props, cache));
										}

										ret.add(tmp);
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
											tmp.put(PROP_NODE, itemNodeRef);
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
							AttributeExtractorMode mode, List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								if (AttributeExtractorMode.CSV.equals(mode) || AttributeExtractorMode.XLSX.equals(mode)) {
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
