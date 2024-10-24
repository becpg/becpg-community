package fr.becpg.repo.search;

import org.alfresco.service.cmr.repository.NodeRef;

public interface SavedSearchService {

	String getSavedSearchContent(NodeRef nodeRef);

	NodeRef createOrUpdate(NodeRef savedSearchNodeRef, String searchType, String siteId, String jsonString, boolean isGlobalSavedSearch);

//	NodeRef saveSearch(String searchType, String siteId, String content, Boolean isUserSavedSearch);
//	
//	
//	SavedSearch getSavedSearch(NodeRef nodeRef);
//	
//	
//	List<SavedSearch> listSavedSearchByType(String type);
//	
//	
//	
}
