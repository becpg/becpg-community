package fr.becpg.repo.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.search.data.SavedSearch;

/**
 * author Matthieu
 */
public interface SavedSearchService {

	String getSavedSearchContent(SavedSearch savedSearch);

	NodeRef createOrUpdate(SavedSearch savedSearch, String jsonString);

	List<SavedSearch> findSavedSearch(SavedSearch filter);

	NodeRef getSaveSearchFolder(SavedSearch savedSearch);

}
