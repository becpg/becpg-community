package fr.becpg.test.repo.search;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.search.SavedSearchService;
import fr.becpg.repo.search.data.SavedSearch;
import fr.becpg.test.RepoBaseTestCase;

public class SavedSearchIT extends RepoBaseTestCase {

	@Autowired
	private SavedSearchService savedSearchService;

	private static final String SEARCH_TYPE = "product-list-bcpg:product";
	private static final String SITE_ID = "simulation";
	private static final String JSON_CONTENT = "{\"key\":\"value\"}";
	private static final String NAME_PREFIX = "TestSearch_";

	@Test
	public void testCreateRetrieveListSavedSearch() {

		String name = NAME_PREFIX + System.currentTimeMillis();

		inWriteTx(() -> {

			AuthenticationUtil.runAsSystem(() -> {
				SavedSearch globalSavedSearch = createSavedSearch(name, SEARCH_TYPE, SITE_ID, true);
				NodeRef savedNodeRef = savedSearchService.createOrUpdate(globalSavedSearch, JSON_CONTENT);

				assertNotNull(savedNodeRef);
				globalSavedSearch.setNodeRef(savedNodeRef);

				String retrievedContent = savedSearchService.getSavedSearchContent(globalSavedSearch);
				assertNotNull(retrievedContent);
				assertEquals(JSON_CONTENT, retrievedContent);
				return globalSavedSearch;
			});

			SavedSearch nonGlobalSavedSearch = createSavedSearch(name, SEARCH_TYPE, null, false);
			savedSearchService.createOrUpdate(nonGlobalSavedSearch, JSON_CONTENT);

			SavedSearch searchFilter = new SavedSearch();
			searchFilter.setSearchType(SEARCH_TYPE);
			List<SavedSearch> savedSearches = savedSearchService.findSavedSearch(searchFilter).stream().filter(s -> s.getName().equals(name))
					.toList();

			assertEquals(1, savedSearches.size());

			searchFilter = new SavedSearch();
			searchFilter.setSearchType(SEARCH_TYPE);
			searchFilter.setSiteId(SITE_ID);

			savedSearches = savedSearchService.findSavedSearch(searchFilter).stream().filter(s -> s.getName().equals(name))
					.toList();

			assertEquals(2, savedSearches.size());
			return null;
		});
	}

	@Test
	public void testObsoleteDocumentsSampleSavedSearch() {
		assertSampleSavedSearch("product-list-bcpg:document", "Obsolete documents", "\\\"prop_cm_to-date-range\\\":\\\"|NOW\\\"");
	}

	@Test
	public void testUpcomingQualityControlsSampleSavedSearch() {
		assertSampleSavedSearch("product-list-qa:qualityControl", "Quality controls to perform",
				"\\\"prop_qa_qcNextAnalysisDate-date-range\\\":\\\"NOW|NOW+7DAY\\\"");
	}

	private void assertSampleSavedSearch(String searchType, String name, String expectedFilter) {

		inReadTx(() -> {

			SavedSearch filter = new SavedSearch();
			filter.setSearchType(searchType);

			List<SavedSearch> savedSearches = savedSearchService.findSavedSearch(filter).stream().filter(s -> name.equals(s.getName())).toList();

			assertEquals(1, savedSearches.size());

			SavedSearch sampleSavedSearch = savedSearches.get(0);
			assertTrue(Boolean.TRUE.equals(sampleSavedSearch.getIsGlobal()));
			assertNotNull(sampleSavedSearch.getTitle());

			String content = savedSearchService.getSavedSearchContent(sampleSavedSearch);
			assertNotNull(content);
			assertTrue(content.contains(expectedFilter));

			return null;
		});
	}

	private SavedSearch createSavedSearch(String name, String searchType, String siteId, boolean isGlobal) {
		SavedSearch savedSearch = new SavedSearch();
		savedSearch.setName(name);
		savedSearch.setSearchType(searchType);
		savedSearch.setSiteId(siteId);
		savedSearch.setIsGlobal(isGlobal);
		return savedSearch;
	}

}
