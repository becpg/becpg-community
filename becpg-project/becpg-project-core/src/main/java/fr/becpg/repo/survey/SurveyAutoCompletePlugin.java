package fr.becpg.repo.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.helper.SurveyableEntityHelper;

/**
 * <p>SurveyListValuePlugin class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 *
 * Autocomplete plugin that allows to get survey questions
 *
 * Example:
 *<pre>
 * {@code
 * 	<control template="/org/alfresco/components/form/controls/autocomplete-association.ftl" >
 *		<control-param name="ds">becpg/autocomplete/survey</control-param>
 *		<control-param name="parentAssoc">survey_slQuestion</control-param>
 *	</control>
 *}
 *</pre>
 *
 *  Datasources available:
 *
 * Return all survey questions by code or questionLabel, if parentAssoc is provided filter by parent question
 *
 *  becpg/autocomplete/survey
 */
@Service("surveyAutoCompletePlugin")
public class SurveyAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { "survey" };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef itemId = null;
		String listName = null;

		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
		if (extras != null) {
			if (extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID) != null) {
				itemId = new NodeRef(extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID));
			}
			if (extras.get(AutoCompleteService.EXTRA_PARAM_LIST) != null) {
				listName = extras.get(AutoCompleteService.EXTRA_PARAM_LIST);
			}
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION).excludeDefaults()
				.excludeProp(BeCPGModel.PROP_IS_DELETED, "true")
				.inSearchTemplate("%(bcpg:code survey:questionLabel)").locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

		if (listName != null) {
			String otherListsClause = excludeOtherSurveyListsClause(listName);
			if (!otherListsClause.isEmpty()) {
				queryBuilder.andFTSQuery(otherListsClause);
			}
		}

		if (!isAllQuery(query)) {
			StringBuilder ftsQuery = new StringBuilder();
			if (query.length() > 2) {
				ftsQuery.append("(" + prepareQuery(query.trim()) + ") OR ");
			}
			ftsQuery.append("(" + query + ")");
			queryBuilder.andFTSQuery(ftsQuery.toString());
		}

		String parent = (String) props.get(AutoCompleteService.PROP_PARENT);
		if ((parent != null) && NodeRef.isNodeRef(parent)) {
			queryBuilder.andPropEquals(BeCPGModel.PROP_PARENT_LEVEL, parent);
		} else {
			queryBuilder.andPropEquals(BeCPGModel.PROP_DEPTH_LEVEL, "1");
		}

		if (RepoConsts.MAX_RESULTS_UNLIMITED.equals(pageSize)) {
			queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED);
		} else {
			queryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS);
		}

		if (itemId != null) {
			queryBuilder.andNotID(itemId);
		}

		return new AutoCompletePage(queryBuilder.list(), pageNum, pageSize, new NodeRefAutoCompleteExtractor(SurveyModel.PROP_SURVEY_QUESTION_LABEL, nodeService));

	}

	/**
	 * A question keeps showing up in a survey list unless it is assigned to another one. Testing the
	 * other names rather than the absence of a name is deliberate: an empty list name may be unset,
	 * null or an empty string depending on how the question was saved, and Solr indexes the empty
	 * string as a value that no FTS operator can single out.
	 *
	 * @param listName the survey list name the suggestions are requested for
	 * @return the FTS clause excluding the questions assigned to another list, empty when there is none
	 */
	private String excludeOtherSurveyListsClause(String listName) {
		String property = SurveyModel.PROP_SURVEY_FS_SURVEY_LIST_NAME.toString();
		List<String> otherListsConditions = new ArrayList<>();
		for (String surveyListName : SurveyableEntityHelper.surveyListsNames()) {
			if (!surveyListName.equals(listName)) {
				otherListsConditions.add("=" + property + ":\"" + surveyListName + "\"");
			}
		}
		if (otherListsConditions.isEmpty()) {
			return "";
		}
		return "NOT (" + String.join(" OR ", otherListsConditions) + ")";
	}

}
