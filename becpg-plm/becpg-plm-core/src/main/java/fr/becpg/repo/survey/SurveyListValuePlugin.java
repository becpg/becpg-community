package fr.becpg.repo.survey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service
public class SurveyListValuePlugin extends EntityListValuePlugin {

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { "survey" };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef itemId = null;

		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
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

		String parent = (String) props.get(ListValueService.PROP_PARENT);
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

		return new ListValuePage(queryBuilder.list(), pageNum, pageSize, new NodeRefListValueExtractor(SurveyModel.PROP_SURVEY_QUESTION_LABEL, nodeService));

	}

}
