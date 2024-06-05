package fr.becpg.repo.survey;

import java.io.Serializable;
import java.util.HashMap;
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

/**
 * <p>SurveyListValuePlugin class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 *
 * Autocomplete plugin that allows to get survey questions
 *
 * Example:
 * 	<control template="/org/alfresco/components/form/controls/autocomplete-association.ftl" >
 *		<control-param name="ds">becpg/autocomplete/survey</control-param>
 *		<control-param name="parentAssoc">survey_slQuestion</control-param>
 *	</control>
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

		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
		if (extras != null) {
			if (extras.get("itemId") != null) {
				itemId = new NodeRef(extras.get("itemId"));
			}
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION).excludeDefaults()
				.inSearchTemplate("%(bcpg:code survey:questionLabel)").locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

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

}
