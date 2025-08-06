package fr.becpg.repo.survey.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementAwareEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.SurveyService;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;

/**
 * <p>SurveyServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
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
	/** {@inheritDoc} */
	@Override
	public JSONObject getSurveyData(NodeRef entityNodeRef, String dataListName,Boolean disabled) throws JSONException {
		JSONObject ret = new JSONObject();
		L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {

			JSONArray data = new JSONArray();
			JSONArray definitions = new JSONArray();

			Set<SurveyQuestion> questions = new HashSet<>();
			
			// Get requirements if they exist
			List<RequirementListDataItem> requirements = getRequirementsForEntity(entityNodeRef);
			Map<String, List<RequirementListDataItem>> questionRequirements = mapRequirementsToQuestions(requirements);
			
			final List<SurveyListDataItem> surveyListDataItems = getSurveys(entityNodeRef, dataListName);

			for (SurveyListDataItem survey : surveyListDataItems) {
				JSONObject value = new JSONObject();

				SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository.findOne(survey.getQuestion());

				appendQuestionDefinition(definitions, surveyQuestion, questions, questionRequirements);
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
			ret.put("disabled", disabled);
			return ret;
		}), false, true, true);

		return ret;
	}

	/** {@inheritDoc} */
	/**
	 * Get requirements for an entity if they exist
	 * 
	 * @param entityNodeRef the entity node reference
	 * @return list of requirement control items or empty list
	 */
	private List<RequirementListDataItem> getRequirementsForEntity(NodeRef entityNodeRef) {
		RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
	
		if(entity instanceof RequirementAwareEntity scorableEntity){
			return scorableEntity.getReqCtrlList();
		}
		return new ArrayList<>();
	}
	
	/**
	 * Maps requirements to their associated questions by NodeRef
	 * 
	 * @param requirements list of requirement items
	 * @return map of question ID to list of requirements
	 */
	private Map<String, List<RequirementListDataItem>> mapRequirementsToQuestions(List<RequirementListDataItem> requirements) {
		Map<String, List<RequirementListDataItem>> result = new HashMap<>();
		for (RequirementListDataItem req : requirements) {
			if (req.getCharact() != null) {
				String questionId = req.getCharact().getId();
				if (!result.containsKey(questionId)) {
					result.put(questionId, new ArrayList<>());
				}
				result.get(questionId).add(req);
			}
		}
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public void saveSurveyData(NodeRef entityNodeRef, String dataListName, JSONObject data) throws JSONException {
		if (data.has("data")) {
			String strData = data.getString("data");

			JSONArray values = new JSONArray(strData);
			for (SurveyListDataItem survey : getSurveys(entityNodeRef, dataListName)) {
				List<NodeRef> choices = new ArrayList<>();
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

	private List<SurveyListDataItem> getSurveys(NodeRef entityNodeRef, String dataListName) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, dataListName);

			if (dataListNodeRef != null) {

				return entityListDAO.getListItems(dataListNodeRef, null).stream().map(el -> {
					SurveyListDataItem s = (SurveyListDataItem) alfrescoRepository.findOne(el);
					s.setParentNodeRef(dataListNodeRef);
					return s;
				}).toList();
			}

		}
		return new ArrayList<>();
	}

	private void appendQuestionDefinition(JSONArray definitions, SurveyQuestion surveyQuestion, Set<SurveyQuestion> questions, 
			Map<String, List<RequirementListDataItem>> questionRequirements) throws JSONException {

		if (!questions.contains(surveyQuestion)) {

			JSONObject definition = new JSONObject();

			definition.put("id", surveyQuestion.getNodeRef().getId());
			definition.put("sort", surveyQuestion.getSort());
			definition.put("label", surveyQuestion.getLabel());
			definition.put("start", questions.isEmpty() || Boolean.TRUE.equals(surveyQuestion.getIsVisible()));
			
			// Add requirements information if available for this question
			String questionId = surveyQuestion.getNodeRef().getId();
			if (questionRequirements.containsKey(questionId)) {
				List<RequirementListDataItem> reqs = questionRequirements.get(questionId);
				if (!reqs.isEmpty()) {
					JSONArray requirementsArray = new JSONArray();
					for (RequirementListDataItem req : reqs) {
						JSONObject reqObj = new JSONObject();
						reqObj.put("type", req.getReqType() != null ? req.getReqType().toString() : "Info");
						reqObj.put("message", req.getReqMessage());
						if (req.getRegulatoryCode() != null) {
							reqObj.put("code", req.getRegulatoryCode());
						}
						requirementsArray.put(reqObj);
					}
					definition.put("requirements", requirementsArray);
				}
			}

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
						choice.put("commentLabel", getLabelOrHidden(surveyQuestion.getResponseCommentLabel()));
						if (CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
							choice.put("textarea", true);
						}
					}

					appendCids(choice, surveyQuestion, definitions, questions, questionRequirements);

					choices.put(choice);

				} else {

					for (NodeRef nodeRef : definitionChoices) {

						SurveyQuestion defChoice = (SurveyQuestion) alfrescoRepository.findOne(nodeRef);
						JSONObject choice = new JSONObject();
						choice.put("id", defChoice.getNodeRef().getId());
						choice.put("label", defChoice.getLabel());
						appendCids(choice, defChoice, definitions, questions, questionRequirements);

						if (CommentType.text.toString().equals(defChoice.getResponseCommentType())
								|| CommentType.textarea.toString().equals(defChoice.getResponseCommentType())) {
							choice.put("comment", true);
							choice.put("commentLabel", getLabelOrHidden(defChoice.getResponseCommentLabel()));
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
				choice.put("commentLabel", getLabelOrHidden(surveyQuestion.getResponseCommentLabel()));
				if (CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
					choice.put("textarea", true);
				}

				appendCids(choice, surveyQuestion, definitions, questions, questionRequirements);

				choices.put(choice);
			}
			if (choices.length() > 0) {
				definition.put("choices", choices);
			}
			definitions.put(definition);
		}
	}

	private String getLabelOrHidden(String responseCommentLabel) {
		return responseCommentLabel == null ? null : responseCommentLabel.isBlank() ? "hidden" : responseCommentLabel;
	}

	private void appendCids(JSONObject choice, SurveyQuestion surveyQuestion, JSONArray definitions, Set<SurveyQuestion> questions, Map<String, List<RequirementListDataItem>> questionRequirements) {
		if (surveyQuestion.getNextQuestions() != null) {
			JSONArray cids = new JSONArray();
			for (SurveyQuestion question : surveyQuestion.getNextQuestions()) {
				cids.put(question.getNodeRef().getId());
				appendQuestionDefinition(definitions, question, questions, questionRequirements);
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
	
	/** {@inheritDoc} */
	@Override
	public List<SurveyListDataItem> getVisibles(List<SurveyListDataItem> surveyListDataItems) {
		final List<SurveyListDataItem> visibleSurveyListDataItems = new ArrayList<>(surveyListDataItems.size());
		final Map<NodeRef, SurveyQuestion> nodeRefSurveyQuestions = surveyListDataItems.stream()
				.map(SurveyListDataItem::getQuestion).distinct().map(alfrescoRepository::findOne)
				.map(SurveyQuestion.class::cast)
				.collect(Collectors.toMap(SurveyQuestion::getNodeRef, Function.identity()));
		for (final SurveyListDataItem surveyListDataItem : surveyListDataItems) {
			final SurveyQuestion surveyQuestion = nodeRefSurveyQuestions.get(surveyListDataItem.getQuestion());
			final SurveyQuestion parent = surveyQuestion.getParent() != null ? surveyQuestion.getParent()
					: surveyQuestion;
			final boolean inNextQuestions = new ArrayList<>(nodeRefSurveyQuestions.values()).stream()
					.map(this::getDefinitionChoices).flatMap(List::stream)
					.map(nodeRef -> nodeRefSurveyQuestions.computeIfAbsent(nodeRef,
							unused -> (SurveyQuestion) alfrescoRepository.findOne(nodeRef)))
					.map(SurveyQuestion.class::cast).filter(question -> question.getNextQuestions() != null)
					.anyMatch(question -> question.getNextQuestions().contains(parent));
			final boolean inChoices = inNextQuestions
					&& surveyListDataItems.stream().map(SurveyListDataItem::getChoices).flatMap(List::stream)
							.map(choice -> nodeRefSurveyQuestions.get(choice).getNextQuestions()).flatMap(List::stream)
							.anyMatch(parent::equals);
			if (!inNextQuestions || inChoices || Boolean.TRUE.equals(parent.getIsVisible())) {
				visibleSurveyListDataItems.add(surveyListDataItem);
			}
		}
		return visibleSurveyListDataItems;
	}

}
