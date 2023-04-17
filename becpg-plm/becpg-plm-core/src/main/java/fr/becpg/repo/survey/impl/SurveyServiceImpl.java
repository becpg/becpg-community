package fr.becpg.repo.survey.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.SurveyService;
import fr.becpg.repo.survey.data.Survey;
import fr.becpg.repo.survey.data.SurveyQuestion;

/**
 *
 * @author matthieu
 *
 */
@Service("surveyService")
public class SurveyServiceImpl implements SurveyService {

	private static Log logger = LogFactory.getLog(SurveyServiceImpl.class);

	public enum CommentType {
		none, text, textarea, file
	}

	public enum ResponseType {
		list, checkboxes, multiChoicelist
	}

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private EntityListDAO entityListDAO;

	/*
	 * data :
	 * [{"qid":"q1","cid":"q1r1"},{"qid":"q2","cid":"q2r1","comment":"Test"},{"qid":"q3a","cid":"q3ar1","listOptions":"1"},{"qid":"q3b","cid":"q3br1","listOptions":"1,3"},{"qid":"q4a","cid":"q4ar2"}]
	 * def : [ { id : "q1", start : true, label : "What is the weather today?", url : "https://www.accuweather.com", lowerNote : "You can check in the attached website.", mandatory : true, choices : [
	 * { id : "q1r1", label : "Outlook", cid : "q2" }, { id : "q1r2", label : "Sunny", cid : "r1" }, { id : "q1r3", label : "Rainy", cid : "q3" } ] }, { id : "q2", label :
	 * "What are the evolution forecast?", upperNote: "It's really important to know what the weather will be like!", note : "You can check in the attached website in the first question.", mandatory :
	 * true, choices : [ { id : "q2r1", label : "It will be sunny soon", cid : "r1" }, { id : "q2r2", label : "It will stay cloudy without rain", cid : "r2" }, { id : "q2r3", label :
	 * "I will be rainy", comment : true, commentLabel: "How long does it would be rainy?", textarea : true, cid : "q3" } ] }, { id : "q3", label : "What do you have to wear against the rain?", note :
	 * "Rain clothes checklist", mandatory : false, choices : [ { id : "q3r1", label : "Rain clothes checklist", list: ["Umbrella","Waterproof cap","Coat","Boots"], multiple : true, checkboxes : true,
	 * cid : "r3" } ] }, { id : "q4", label : "Have you got any remarks?", mandatory : false, choices : [ { id : "q4r1", label : "hidden", comment : true, textarea: true } ] },
	 *
	 * { id : "r1", label : "Enjoy your bright day!" }, { id : "r2", label : "Beware to not take cold!" }, { id : "r3", label : "Try to avoid getting outside!" } ]
	 */
	@Override
	public JSONObject getSurveyData(NodeRef entityNodeRef, String dataListName) throws JSONException {
		JSONObject ret = new JSONObject();
		L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {

			JSONArray data = new JSONArray();
			JSONArray definitions = new JSONArray();

			Set<SurveyQuestion> questions = new HashSet<>();

			for (Survey survey : getSurveys(entityNodeRef, dataListName)) {
				JSONObject value = new JSONObject();

				SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository.findOne(survey.getQuestion());

				appendQuestionDefinition(definitions, surveyQuestion, questions, survey.getSort());
				if ((survey.getComment() != null) || !survey.getChoices().isEmpty()) {
					value.put("qid", survey.getQuestion().getId());
					if (survey.getComment() != null) {
						value.put("comment", survey.getComment());
					}

					if ((surveyQuestion.getResponseType() == null) || surveyQuestion.getResponseType().isEmpty()) {
						for (NodeRef choice : survey.getChoices()) {
							value.put("cid", choice.getId());
						}

					} else {

						value.put("listOptions", getOptions(survey.getChoices()));
						value.put("cid", "sub-" + surveyQuestion.getNodeRef().getId().substring(4));
					}
					data.put(value);
				}

			}

			ret.put("data", data);
			ret.put("def", definitions);

			return ret;
		}), false, true, true);

		return ret;
	}

	@Override
	public void saveSurveyData(NodeRef entityNodeRef, String dataListName, JSONObject data) throws JSONException {
		if (data.has("data")) {
			String strData = data.getString("data");

			JSONArray values = new JSONArray(strData);
			for (Survey survey : getSurveys(entityNodeRef, dataListName)) {
				List<NodeRef> choices = new LinkedList<>();
				survey.setComment(null);

				for (int i = 0; i < values.length(); i++) {

					JSONObject value = values.getJSONObject(i);
					if (value.has("qid") && (value.getString("qid")).equals(survey.getQuestion().getId())) {
						if (value.has("comment")) {
							survey.setComment(value.getString("comment"));
						}

						if (value.has("listOptions")) {

							for (String cid : value.getString("listOptions").split(",")) {
								choices.add(createNodeRef(cid));
							}
						} else if (value.has("cid")) {
							for (String cid : value.getString("cid").split(",")) {
								choices.add(createNodeRef(cid));
							}
						}

						break;
					}

				}

				survey.setChoices(choices);
				if (logger.isDebugEnabled()) {
					logger.debug("Save: " + survey.toString());
				}

				alfrescoRepository.save(survey);

			}
		}

	}

	private NodeRef createNodeRef(String id) {
		return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
	}

	private List<Survey> getSurveys(NodeRef entityNodeRef, String dataListName) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, dataListName);

			if (dataListNodeRef != null) {

				return entityListDAO.getListItems(dataListNodeRef, null).stream().map(el -> {
					Survey s = (Survey) alfrescoRepository.findOne(el);
					s.setParentNodeRef(dataListNodeRef);
					return s;
				}).collect(Collectors.toCollection(LinkedList::new));
			}

		}
		return new ArrayList<>();
	}

	private void appendQuestionDefinition(JSONArray definitions, SurveyQuestion surveyQuestion, Set<SurveyQuestion> questions, Integer sort)
			throws JSONException {

		if (!questions.contains(surveyQuestion)) {

			JSONObject definition = new JSONObject();

			definition.put("id", surveyQuestion.getNodeRef().getId());
			definition.put("sort", sort);
			definition.put("label", surveyQuestion.getLabel());
			definition.put("start", questions.isEmpty() || Boolean.TRUE.equals(surveyQuestion.getIsVisible()));

			if ((surveyQuestion.getQuestionUrl() != null) && !surveyQuestion.getQuestionUrl().isBlank()) {
				definition.put("url", surveyQuestion.getQuestionUrl());
			}
			if ((surveyQuestion.getQuestionLowerNote() != null) && !surveyQuestion.getQuestionLowerNote().isBlank()) {
				definition.put("lowerNote", surveyQuestion.getQuestionLowerNote());
			}
			if ((surveyQuestion.getQuestionUpperNote() != null) && !surveyQuestion.getQuestionUpperNote().isBlank()) {
				definition.put("upperNote", surveyQuestion.getQuestionUpperNote());
			}
			if ((surveyQuestion.getQuestionNote() != null) && !surveyQuestion.getQuestionNote().isBlank()) {
				definition.put("note", surveyQuestion.getQuestionNote());
			}
			if (Boolean.TRUE.equals(surveyQuestion.getIsMandatory())) {
				definition.put("mandatory", true);
			}

			questions.add(surveyQuestion);

			JSONArray choices = new JSONArray();
			List<NodeRef> definitionChoices = getDefinitionChoices(surveyQuestion);
			if (!definitionChoices.isEmpty()) {
				if (ResponseType.list.toString().equals(surveyQuestion.getResponseType())
						|| ResponseType.checkboxes.toString().equals(surveyQuestion.getResponseType())
						|| ResponseType.multiChoicelist.toString().equals(surveyQuestion.getResponseType())) {
					JSONObject choice = new JSONObject();
					choice.put("id", "sub-" + surveyQuestion.getNodeRef().getId().substring(4));
					JSONArray list = new JSONArray();

					for (NodeRef listOption : definitionChoices) {
						SurveyQuestion opt = (SurveyQuestion) alfrescoRepository.findOne(listOption);

						list.put(listOption.getId() + "|" + opt.getLabel());
					}

					choice.put("list", list);
					choice.put("multiple", !ResponseType.list.toString().equals(surveyQuestion.getResponseType()));
					choice.put("label", "hidden");
					if (ResponseType.checkboxes.toString().equals(surveyQuestion.getResponseType())) {
						choice.put("checkboxes", true);
					}

					if (CommentType.text.toString().equals(surveyQuestion.getResponseCommentType())
							|| CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
						choice.put("comment", true);
						choice.put("commentLabel", surveyQuestion.getResponseCommentLabel());
						if (CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
							choice.put("textarea", true);
						}
					}

					appendCids(choice, surveyQuestion, definitions, questions, sort);

					choices.put(choice);

				} else {

					for (NodeRef nodeRef : definitionChoices) {

						SurveyQuestion defChoice = (SurveyQuestion) alfrescoRepository.findOne(nodeRef);
						JSONObject choice = new JSONObject();
						choice.put("id", defChoice.getNodeRef().getId());
						choice.put("label", defChoice.getLabel());
						appendCids(choice, defChoice, definitions, questions, sort);
						
						if (CommentType.text.toString().equals(defChoice.getResponseCommentType())
								|| CommentType.textarea.toString().equals(defChoice.getResponseCommentType())) {
							choice.put("comment", true);
							choice.put("commentLabel",  defChoice.getResponseCommentLabel());
							if (CommentType.textarea.toString().equals(defChoice.getResponseCommentType())) {
								choice.put("textarea", true);
							}
						}

						choices.put(choice);
					}

				}

			} else if (CommentType.text.toString().equals(surveyQuestion.getResponseCommentType())
					|| CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
				JSONObject choice = new JSONObject();
				choice.put("id", "sub-" + surveyQuestion.getNodeRef().getId().substring(4));
				choice.put("label", "hidden");
				choice.put("comment", true);
				choice.put("commentLabel", surveyQuestion.getResponseCommentLabel());
				if (CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
					choice.put("textarea", true);
				}

				appendCids(choice, surveyQuestion, definitions, questions, sort);

				choices.put(choice);
			}
			if (choices.length() > 0) {
				definition.put("choices", choices);
			}
			definitions.put(definition);
		}
	}

	private void appendCids(JSONObject choice, SurveyQuestion surveyQuestion, JSONArray definitions, Set<SurveyQuestion> questions, int sort) {
		if (surveyQuestion.getNextQuestions() != null) {
			JSONArray cids = new JSONArray();
			for (SurveyQuestion question : surveyQuestion.getNextQuestions()) {
				cids.put(question.getNodeRef().getId());
				appendQuestionDefinition(definitions, question, questions, sort + 1);
			}
			if (cids.length() > 0) {
				choice.put("cid", cids);
			}

		}

	}

	private List<NodeRef> getDefinitionChoices(SurveyQuestion surveyQuestion) {
		BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION).andPropEquals(BeCPGModel.PROP_PARENT_LEVEL,
				surveyQuestion.getNodeRef().toString());

		Map<String, Boolean> sortBy = new LinkedHashMap<>();
		sortBy.put("@bcpg:sort", true);
		return query.addSort(sortBy).inDB().list();
	}

	private String getOptions(List<NodeRef> definitionChoices) {
		StringBuilder options = null;
		for (NodeRef choice : definitionChoices) {
			if (options == null) {
				options = new StringBuilder();
			} else {
				options.append(",");
			}
			options.append(choice.getId());
		}
		if (options != null) {
			return options.toString();
		}
		return null;
	}

}
