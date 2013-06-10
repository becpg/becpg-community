package fr.becpg.repo.project.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
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
import fr.becpg.repo.helper.LuceneHelper;

@Service
public class ProjectListExtractor extends SimpleExtractor {

	private static final String PREF_FOLDER_FAVOURITES = "org.alfresco.share.documents.favourites";
	private static final String PROP_IS_FAVOURITE = "isFavourite";
	
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
		
		List<NodeRef> results = getListNodeRef(dataListFilter, pagination,favorites);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(PROP_ACCESSRIGHT, hasWriteAccess);
		
		

		for (NodeRef nodeRef : results) {
			if (permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				
				Map<String, Object> extracted = extract(nodeRef, metadataFields, props);
				if(favorites.contains(nodeRef)){
					extracted.put(PROP_IS_FAVOURITE,true);
				} else {
					extracted.put(PROP_IS_FAVOURITE,false);
				}
				
				ret.getItems().add(extracted);
			}
		}

		ret.setFullListSize(pagination.getFullListSize());

		return ret;
	}

	private List<NodeRef> getFavorites() {

		Map<String, Serializable> preferences =  preferenceService.getPreferences(AuthenticationUtil.getFullyAuthenticatedUser());
        
        String favorites = (String) preferences.get(PREF_FOLDER_FAVOURITES);

		List<NodeRef> ret = new ArrayList<>();
        
		if(logger.isDebugEnabled()){
			logger.debug("Favourites: "+favorites);
		}
		
		if(favorites!=null){
			for(String favorite : favorites.split(",")){
				ret.add(new NodeRef(favorite));
			}
		}
		return ret;
	}

	private List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination, List<NodeRef> favorites) {

		List<NodeRef> results = new ArrayList<NodeRef>();		
		
		//pjt:project
		QName dataType = dataListFilter.getDataType();
		String query = dataListFilter.getSearchQuery();
		Map<String, Boolean> sortMap = dataListFilter.getSortMap();
		
		if(VIEW_MY_TASKS.equals(dataListFilter.getFilterId()) || VIEW_TASKS.equals(dataListFilter.getFilterId())){
			dataType = ProjectModel.TYPE_TASK_LIST;
			query = query.replace(LuceneHelper.mandatory(LuceneHelper.getCondType(ProjectModel.TYPE_PROJECT)), LuceneHelper.mandatory(LuceneHelper.getCondType(dataType)));
			sortMap = new HashMap<>();
			sortMap.put(ProjectModel.PROP_TL_START.toString(), true);
			sortMap.put(ProjectModel.PROP_TL_END.toString(), true);
			sortMap.put(ProjectModel.PROP_TL_TASK_NAME.toString(), true);
			
		}
		
		results = advSearchService.queryAdvSearch(query, SearchService.LANGUAGE_LUCENE, dataType, dataListFilter.getCriteriaMap(),
				sortMap , pagination.getMaxResults());
		
		//Always should return project
        if(VIEW_MY_TASKS.equals(dataListFilter.getFilterId()) || VIEW_TASKS.equals(dataListFilter.getFilterId())){
        	 if(VIEW_MY_TASKS.equals(dataListFilter.getFilterId())){
        		 logger.debug("Keep only tasks for  "+AuthenticationUtil.getFullyAuthenticatedUser());
        		 NodeRef currentUserNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
        		 if(logger.isDebugEnabled()){
        			 logger.debug("Retain : "+associationService.getSourcesAssocs(currentUserNodeRef, ProjectModel.ASSOC_TL_RESOURCES));
        		 }
        		 results.retainAll(associationService.getSourcesAssocs(currentUserNodeRef, ProjectModel.ASSOC_TL_RESOURCES));
        	 }
		}
		
        if(VIEW_FAVOURITES.equals(dataListFilter.getFilterId()) ){
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
	public boolean applyTo(DataListFilter dataListFilter, String dataListName) {
		return dataListName != null && dataListName.equals("projectList");
	}

}
