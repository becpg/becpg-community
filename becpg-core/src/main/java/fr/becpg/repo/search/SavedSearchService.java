package fr.becpg.repo.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.search.data.SavedSearch;

/**
 * author Matthieu
 *
 * @author matthieu
 */
public interface SavedSearchService {

	/**
	 * <p>getSavedSearchContent.</p>
	 *
	 * @param savedSearch a {@link fr.becpg.repo.search.data.SavedSearch} object
	 * @return a {@link java.lang.String} object
	 */
	String getSavedSearchContent(SavedSearch savedSearch);

	/**
	 * <p>createOrUpdate.</p>
	 *
	 * @param savedSearch a {@link fr.becpg.repo.search.data.SavedSearch} object
	 * @param jsonString a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef createOrUpdate(SavedSearch savedSearch, String jsonString);

	/**
	 * <p>findSavedSearch.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.search.data.SavedSearch} object
	 * @return a {@link java.util.List} object
	 */
	List<SavedSearch> findSavedSearch(SavedSearch filter);

	/**
	 * <p>getSaveSearchFolder.</p>
	 *
	 * @param savedSearch a {@link fr.becpg.repo.search.data.SavedSearch} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getSaveSearchFolder(SavedSearch savedSearch);

}
