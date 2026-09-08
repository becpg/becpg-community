package fr.becpg.test.repo.autocomplete;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.survey.SurveyAutoCompletePlugin;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.helper.SurveyableEntityHelper;

/**
 * Checks that the question suggestions of a survey list keep the questions assigned to that list
 * and the questions assigned to no list at all, whatever the storage form of the empty list name.
 */
public class SurveyAutoCompletePluginIT extends AbstractAutoCompletePluginTest {

	private static final String SOURCE_TYPE = "survey";

	@Autowired
	private SurveyAutoCompletePlugin surveyAutoCompletePlugin;

	@Autowired
	private AlfrescoRepository<SurveyQuestion> surveyQuestionRepository;

	@Test
	public void testUnassignedQuestionsBelongToEverySurveyList() {

		final String token = "q" + UUID.randomUUID().toString().replace("-", "");
		final String defaultListName = SurveyableEntityHelper.surveyListsNames().get(0);
		final String secondListName = SurveyableEntityHelper.surveyListsNames().get(1);

		final NodeRef unsetQuestionNodeRef = createQuestion(token + " unset", null);
		final NodeRef emptyQuestionNodeRef = createQuestion(token + " empty", "");
		final NodeRef secondListQuestionNodeRef = createQuestion(token + " second", secondListName);

		waitForSolr();

		inReadTx(() -> {
			List<AutoCompleteEntry> secondListSuggestions = suggest(token, secondListName);
			assertEquals("Unassigned and second list questions", 3, secondListSuggestions.size());
			assertTrue(contains(unsetQuestionNodeRef, secondListSuggestions));
			assertTrue(contains(emptyQuestionNodeRef, secondListSuggestions));
			assertTrue(contains(secondListQuestionNodeRef, secondListSuggestions));

			List<AutoCompleteEntry> defaultListSuggestions = suggest(token, defaultListName);
			assertEquals("Unassigned questions only", 2, defaultListSuggestions.size());
			assertTrue(contains(unsetQuestionNodeRef, defaultListSuggestions));
			assertTrue(contains(emptyQuestionNodeRef, defaultListSuggestions));
			assertFalse(contains(secondListQuestionNodeRef, defaultListSuggestions));

			List<AutoCompleteEntry> noListSuggestions = suggest(token, null);
			assertEquals("Every question without a list filter", 3, noListSuggestions.size());
			return null;
		});
	}

	private NodeRef createQuestion(String label, String surveyListName) {
		return inWriteTx(() -> {
			SurveyQuestion surveyQuestion = new SurveyQuestion();
			surveyQuestion.setName(label);
			surveyQuestion.setLabel(label);
			surveyQuestion.setFsSurveyListName(surveyListName);
			return surveyQuestionRepository.create(getTestFolderNodeRef(), surveyQuestion).getNodeRef();
		});
	}

	private List<AutoCompleteEntry> suggest(String query, String listName) {
		Map<String, Serializable> props = new HashMap<>();
		if (listName != null) {
			HashMap<String, String> extras = new HashMap<>();
			extras.put(AutoCompleteService.EXTRA_PARAM_LIST, listName);
			props.put(AutoCompleteService.EXTRA_PARAM, extras);
		}
		return surveyAutoCompletePlugin.suggest(SOURCE_TYPE, query, 0, 10, props).getResults();
	}

	private boolean contains(NodeRef questionNodeRef, List<AutoCompleteEntry> suggestions) {
		for (AutoCompleteEntry suggestion : suggestions) {
			if (questionNodeRef.toString().equals(suggestion.getValue())) {
				return true;
			}
		}
		return false;
	}
}
