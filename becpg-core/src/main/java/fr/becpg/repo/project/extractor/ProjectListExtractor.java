package fr.becpg.repo.project.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.project.data.projectList.TaskState;

@Service
public class ProjectListExtractor extends SimpleExtractor {

	private static final String PREF_FOLDER_FAVOURITES = "org.alfresco.share.documents.favourites";
	private static final String PROP_IS_FAVOURITE = "isFavourite";
	private static final String FILTER_DATA = "filterData";
	private static final String PAGINATION = "pagination";

	private static final String VIEW_FAVOURITES = "favourites";
	private static final String VIEW_TASKS = "tasks";
	private static final String VIEW_MY_TASKS = "my-tasks";

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
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());

		List<NodeRef> favorites = getFavorites();

		List<NodeRef> results = getListNodeRef(dataListFilter, pagination, favorites);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, hasWriteAccess);
		props.put(FILTER_DATA, dataListFilter);
		props.put(PAGINATION, pagination);

		Map<NodeRef, Map<String, Object>> cache = new HashMap<>();

		List<AttributeExtractorStructure> computedFields = null;

		for (NodeRef nodeRef : results) {
			if (permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				if (!nodeService.exists(nodeRef)) {
					logger.error("NodeRef doesn't exist ? " + nodeRef.toString());
				} else {
					if (computedFields == null) {
						computedFields = attributeExtractorService.readExtractStructure(nodeService.getType(nodeRef), metadataFields);
					}

					Map<String, Object> extracted = extract(nodeRef, computedFields, props, cache);
					if (favorites.contains(nodeRef)) {
						extracted.put(PROP_IS_FAVOURITE, true);
					} else {
						extracted.put(PROP_IS_FAVOURITE, false);
					}

					ret.getItems().add(extracted);
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
				} catch (MalformedNodeRefException e){
					logger.warn("Favorite nodeRef is malformed : "+favorite);
				}
			}
		}
		return ret;
	}

	private List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination, List<NodeRef> favorites) {

		List<NodeRef> results = new ArrayList<NodeRef>();

		// pjt:project
		QName dataType = dataListFilter.getDataType();
		String query = dataListFilter.getSearchQuery();

		if (VIEW_MY_TASKS.equals(dataListFilter.getFilterId()) || VIEW_TASKS.equals(dataListFilter.getFilterId())) {
			dataType = ProjectModel.TYPE_TASK_LIST;
			query = query.replace(LuceneHelper.mandatory(LuceneHelper.getCondType(ProjectModel.TYPE_PROJECT)), LuceneHelper.mandatory(LuceneHelper.getCondType(dataType)));
		}

		results = advSearchService.queryAdvSearch(query, SearchService.LANGUAGE_LUCENE, dataType, dataListFilter.getCriteriaMap(), dataListFilter.getSortMap(),
				pagination.getMaxResults());

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
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields, Map<QName, Serializable> properties,
			final Map<String, Object> props, final Map<NodeRef, Map<String, Object>> cache) {

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, false, new AttributeExtractorService.DataListCallBack() {

			@Override
			public List<Map<String, Object>> extractDataListField(NodeRef entityNodeRef, QName dataListQname, List<AttributeExtractorStructure> metadataFields) {

				List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
				// TODO inneficient join
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, dataListQname);

				if (listNodeRef != null) {
					List<NodeRef> results = entityListDAO.getListItems(listNodeRef, dataListQname);

					DataListPagination pagination = (DataListPagination) props.get(PAGINATION);
					TaskState taskState = null;
					if (pagination.getPageSize() > 10) {
						DataListFilter filter = (DataListFilter) props.get(FILTER_DATA);
						taskState = TaskState.valueOf(filter.getFilterData());
					}

					for (NodeRef nodeRef : results) {

						
						if ((taskState == null
								|| (ProjectModel.TYPE_TASK_LIST.equals(dataListQname) && taskState.toString().equals(nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_STATE))) 
								|| ProjectModel.TYPE_DELIVERABLE_LIST
									.equals(dataListQname)) && permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {

							Map<String, Object> tmp = new HashMap<String, Object>(3);
							QName itemType = nodeService.getType(nodeRef);
							Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
							tmp.put(PROP_TYPE, itemType.toPrefixString(services.getNamespaceService()));
							tmp.put(PROP_NODE, nodeRef);
							tmp.put(PROP_NODEDATA, doExtract(nodeRef, itemType, metadataFields, properties, props, cache));
							ret.add(tmp);
						}
					}
				}

				return ret;
			}

			@Override
			public Map<String, Object> extractEntityField(NodeRef entityListNodeRef, QName entityTypeQname, List<AttributeExtractorStructure> metadataFields) {

				NodeRef entityNodeRef = entityListDAO.getEntity(entityListNodeRef);

				if (cache.containsKey(entityNodeRef)) {
					return cache.get(entityNodeRef);
				} else {
					if (permissionService.hasPermission(entityNodeRef, "Read") == AccessStatus.ALLOWED) {
						return extract(entityNodeRef, metadataFields, props, cache);
					}
				}

				return new HashMap<String, Object>();
			}
		});
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter, String dataListName) {
		return dataListName != null && dataListName.equals("projectList");
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {

		return null;
	}

}
