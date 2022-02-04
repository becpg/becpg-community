package fr.becpg.repo.survey.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.repository.AlfrescoRepository;
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

	public enum CommentType {
		none, text, textarea, file
	}

	public enum ResponseType {
		list, checkboxes
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
		JSONArray data = new JSONArray();
		JSONArray definitions = new JSONArray();
		

		Set<SurveyQuestion> questions = new HashSet<>();

		for (Survey survey : getSurveys(entityNodeRef, dataListName)) {
			JSONObject value = new JSONObject();
			

			SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository.findOne(survey.getQuestion());

			appendQuestionDefinition(definitions, surveyQuestion, questions);

			value.append("qid", survey.getQuestion());
			if (survey.getComment() != null) {
				value.append("comment", survey.getComment());
			}

			if ((surveyQuestion.getResponseType() == null) || surveyQuestion.getResponseType().isEmpty()) {
				for (NodeRef choice : survey.getChoices()) {
					value.append("cid", choice);
				}

			} else {

				value.append("listOptions", getOptions(survey.getChoices()));
			}

			data.put(value);
		}
		
		ret.put("data", data);
		ret.put("def", definitions);

		return ret;
	}

	@Override
	public void saveSurveyData(NodeRef entityNodeRef, String dataListName, JSONObject data) throws JSONException {
		if (data.has("data")) {

			JSONArray values = data.getJSONArray("data");
			for (Survey survey : getSurveys(entityNodeRef, dataListName)) {
				for (int i = 0; i < values.length(); i++) {
					JSONObject value = values.getJSONObject(i);
					if (value.has("qid") && (new NodeRef(value.getString("qid"))).equals(survey.getQuestion())) {
						if (value.has("comment")) {
							survey.setComment(value.getString("comment"));
						} else {
							survey.setComment(null);
						}

						List<NodeRef> choices = new ArrayList<>();
						if (value.has("listOptions")) {

							for (String cid : value.getString("listOptions").split(",")) {
								choices.add(new NodeRef(cid));
							}
						} else if (value.has("cid")) {
							choices.add(new NodeRef(value.getString("cid")));
						}

						survey.setChoices(choices);
						alfrescoRepository.save(survey);

					}

				}

			}
		}

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

	private void appendQuestionDefinition(JSONArray definitions, SurveyQuestion surveyQuestion, Set<SurveyQuestion> questions) throws JSONException {

		if (!questions.contains(surveyQuestion)) {
			questions.add(surveyQuestion);
			JSONObject definition = new JSONObject();

			definition.append("id", surveyQuestion.getNodeRef());
			definition.append("start", questions.isEmpty());
			definition.append("label", surveyQuestion.getLabel());
			definition.append("url", surveyQuestion.getQuestionUrl());
			definition.append("lowerNote", surveyQuestion.getQuestionLowerNote());
			definition.append("upperNote", surveyQuestion.getQuestionUpperNote());
			definition.append("note", surveyQuestion.getQuestionNote());
			definition.append("mandatory", surveyQuestion.getNodeRef());

			JSONArray choices = new JSONArray();
			List<NodeRef> definitionChoices = getDefinitionChoices(surveyQuestion);
			if (!definitionChoices.isEmpty()) {
				if (ResponseType.list.toString().equals(surveyQuestion.getResponseType())
						|| ResponseType.checkboxes.toString().equals(surveyQuestion.getResponseType())) {
					JSONObject choice = new JSONObject();
					choice.append("id", surveyQuestion.getNodeRef() + "-choice");
					choice.append("list", getOptions(definitionChoices));
					choice.append("multiple", true);
					choice.append("checkboxes", ResponseType.checkboxes.toString().equals(surveyQuestion.getResponseType()));
					if (surveyQuestion.getNextQuestion() != null) {
						choice.append("cid", surveyQuestion.getNextQuestion().getNodeRef());
						appendQuestionDefinition(definitions, surveyQuestion.getNextQuestion(), questions);
					}

					choices.put(choice);

				} else {

					for (NodeRef nodeRef : definitionChoices) {
						
						SurveyQuestion defChoice = (SurveyQuestion) alfrescoRepository.findOne(nodeRef);
						JSONObject choice = new JSONObject();
						choice.append("id", defChoice.getNodeRef());
						choice.append("label", defChoice.getLabel());
						if (defChoice.getNextQuestion() != null) {
							choice.append("cid", defChoice.getNextQuestion().getNodeRef());
							appendQuestionDefinition(definitions, surveyQuestion.getNextQuestion(), questions);
						}
						if (CommentType.text.toString().equals(defChoice.getResponseCommentType())
								|| CommentType.textarea.toString().equals(defChoice.getResponseCommentType())) {
							choice.append("comment", true);
							choice.append("commentLabel", defChoice.getResponseCommentLabel());
							if (CommentType.textarea.toString().equals(defChoice.getResponseCommentType())) {
								choice.append("textarea", true);
							}
						}

						choices.put(choice);
					}

				}

			} else if (CommentType.text.toString().equals(surveyQuestion.getResponseCommentType())
					|| CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
				JSONObject choice = new JSONObject();
				choice.append("id", surveyQuestion.getNodeRef() + "-choice");

				choice.append("comment", true);
				choice.append("commentLabel", surveyQuestion.getResponseCommentLabel());
				if (CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
					choice.append("textarea", true);
				}

				if (surveyQuestion.getNextQuestion() != null) {
					choice.append("cid", surveyQuestion.getNextQuestion().getNodeRef());
					appendQuestionDefinition(definitions, surveyQuestion.getNextQuestion(), questions);
				}

				choices.put(choice);
			}
			definition.append("choices", choices);

			definitions.put(definition);
		}
	}

	private List<NodeRef> getDefinitionChoices(SurveyQuestion surveyQuestion) {
		BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION).andPropEquals(BeCPGModel.PROP_PARENT_LEVEL,
				surveyQuestion.getNodeRef().toString());

		return query.inDB().list();
	}

	private String getOptions(List<NodeRef> definitionChoices) {
		StringBuilder options = null;
		for (NodeRef choice : definitionChoices) {
			if (options == null) {
				options = new StringBuilder();
			} else {
				options.append(",");
			}
			options.append(choice);
		}
		if (options != null) {
			return options.toString();
		}
		return null;
	}

}
