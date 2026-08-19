package fr.becpg.repo.survey.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.batch.WorkProviderFactory;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.regulatory.RequirementAwareEntity;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.SurveyService;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.data.SurveyQuestionCache;
import fr.becpg.repo.survey.helper.SurveyableEntityHelper;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>SurveyServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("surveyService")
public class SurveyServiceImpl implements SurveyService {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(SurveyServiceImpl.class);

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
	
	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	/**
	 * Constant <code>CONF_SUBSIDIARY_SCOPE_ENABLED="beCPG.survey.subsidiaryScope.enabled"</code>
	 * <p>
	 * Opt-in, <code>false</code> in the shipped configuration : see
	 * {@link #createSubsidiaryScopeFilter(NodeRef)}.
	 */
	private static final String CONF_SUBSIDIARY_SCOPE_ENABLED = "beCPG.survey.subsidiaryScope.enabled";

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
	public JSONObject getSurveyData(NodeRef entityNodeRef, String dataListName, Boolean disabled) throws JSONException {
		JSONObject ret = new JSONObject();
		L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {

			JSONArray data = new JSONArray();
			JSONArray definitions = new JSONArray();

			Set<SurveyQuestion> questions = new HashSet<>();
			
			// Get requirements if they exist
			List<RequirementListDataItem> requirements = getRequirementsForEntity(entityNodeRef);
			Map<String, List<RequirementListDataItem>> questionRequirements = mapRequirementsToQuestions(requirements);
			
			final Predicate<SurveyQuestion> subsidiaryScope = createSubsidiaryScopeFilter(entityNodeRef);
			final List<SurveyListDataItem> surveyListDataItems = filterSubsidiaryScope(getSurveys(entityNodeRef, dataListName), subsidiaryScope);

			for (SurveyListDataItem survey : surveyListDataItems) {
				JSONObject value = new JSONObject();

				SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository.findOne(survey.getQuestion());

				appendQuestionDefinition(definitions, surveyQuestion, questions, questionRequirements, subsidiaryScope);
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
	@Override
	public SurveyQuestionCache getSurveyQuestionCache() {
		return beCPGCacheService.getFromCache(CACHE_KEY, CACHE_KEY, () -> {
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION)
					.inDB();
			List<SurveyQuestion> allSurveyQuestions = WorkProviderFactory.fromQueryBuilder(queryBuilder).collect().stream()
					.map(alfrescoRepository::findOne).map(SurveyQuestion.class::cast).toList();
			List<SurveyQuestion> generatedSurveyQuestions = allSurveyQuestions.stream()
					.filter(q -> Boolean.TRUE.equals(q.getGenerationEnabled()))
					.filter(q -> q.getFsSurveyListName() != null && q.getFsSurveyListName().startsWith(SurveyableEntityHelper.SURVEY_LIST_BASE_NAME))
					.filter(q -> q.getParent() == null).toList();
			Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef = allSurveyQuestions.stream().collect(Collectors.toMap(q -> q.getNodeRef(), q -> q));
			Map<SurveyQuestion, List<NodeRef>> surveyQuestionsByParent = allSurveyQuestions.stream().collect(Collectors.toMap(q -> q, this::getDefinitionChoices));
			return new SurveyQuestionCache(surveyQuestionByNodeRef, surveyQuestionsByParent, generatedSurveyQuestions);
		});
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
	public void saveSurveyData(NodeRef entityNodeRef, String dataListName, JSONObject data) throws JSONException {
	    if (!data.has("data")) {
	        return;
	    }
	    
	    JSONArray values = new JSONArray(data.getString("data"));
	    Map<String, JSONObject> valueByQid = new HashMap<>();
	    for (int i = 0; i < values.length(); i++) {
	        JSONObject v = values.getJSONObject(i);
	        if (v.has("qid")) {
	            valueByQid.put(v.getString("qid"), v);
	        }
	    }

	    for (SurveyListDataItem survey : filterSubsidiaryScope(getSurveys(entityNodeRef, dataListName), createSubsidiaryScopeFilter(entityNodeRef))) {
	    	final List<NodeRef> choices = survey.getChoices();
	        // Reset survey
	        survey.setComment(null);
	        survey.setNumberComment(null);
	        survey.setDateComment(null);
	        survey.setChoices(new ArrayList<>());

	        JSONObject value = valueByQid.get(survey.getQuestion().getId());
	        if (value != null) {
	            // Handle comment
	            String comment = value.optString("comment", null);
	            if (comment != null) {
	                survey.setComment(comment);

	                if (choices != null && !choices.isEmpty()) {
	                    SurveyQuestion choice = (SurveyQuestion) alfrescoRepository.findOne(choices.get(0));
	                    String type = choice.getResponseCommentType();
	                    try {
	                        switch (type) {
	                            case "number", "int", "percentage":
	                                survey.setNumberComment(Double.parseDouble(comment));
	                                break;
	                            case "date", "dateTime":
	                            	 survey.setDateComment(ISO8601DateFormat.parse(comment));
	                                break;
	                            default:
	                                break;
	                        }
	                    } catch (Exception e) {
	                        logger.error("Cannot convert '"+comment+"' into '"+type+"' for survey "+survey.getName(), e);
	                    }
	                }
	            }

	            // Handle choices
	            String options = value.optString("listOptions", value.optString("cid", null));
	            if (options != null) {
	                for (String cid : options.split(",")) {
	                    survey.getChoices().add(createNodeRef(cid.trim()));
	                }
	            }
	        }

	        alfrescoRepository.save(survey);
	    }
	}


	/**
	 * <p>createNodeRef.</p>
	 *
	 * @param id a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef createNodeRef(String id) {
		return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
	}

	/**
	 * <p>getSurveys.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListName a {@link java.lang.String} object
	 * @return a {@link java.util.List} object
	 */
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

	/**
	 * Builds the subsidiary scope filter applied to the questions of an entity, i.e. the answer to
	 * "is this question one this entity's subsidiary has to answer?" (Redmine #25461 / DEV-813 :
	 * subsidiaries sharing one entity template and one questionnaire, each supplier only seeing the
	 * questions of its own subsidiary).
	 * <p>
	 * The semantics are strictly the ones already implemented by
	 * <code>DocumentFormulationHandler.isDocTypeMatchProduct()</code> for a document type and by
	 * <code>SurveyListFormulationHandler</code> for a generated question, on the very same
	 * association <code>bcpg:subsidiaryRef</code> :
	 * <ul>
	 * <li>a question carrying <b>no</b> subsidiary is answered by everyone — today's behaviour, kept
	 * as is,</li>
	 * <li>a question carrying subsidiaries is kept only when the entity carries at least one of
	 * them.</li>
	 * </ul>
	 * Nothing is written : the filter only hides questions from what is served, the stored
	 * <code>survey:surveyList</code> rows (and the answers of a hidden question) are left untouched,
	 * both when reading and when saving.
	 * <p>
	 * <b>Opt-in</b> : returns "everything is in scope" unless
	 * <code>beCPG.survey.subsidiaryScope.enabled</code> is set to <code>true</code>, which it is not
	 * in the shipped configuration. Left alone, every instance keeps serving exactly the questions it
	 * serves today.
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.function.Predicate} object
	 */
	private Predicate<SurveyQuestion> createSubsidiaryScopeFilter(NodeRef entityNodeRef) {
		if ((entityNodeRef == null) || !Boolean.parseBoolean(systemConfigurationService.confValue(CONF_SUBSIDIARY_SCOPE_ENABLED))) {
			return surveyQuestion -> true;
		}

		List<NodeRef> entitySubsidiaries = associationService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_SUBSIDIARY_REF);

		return surveyQuestion -> {
			if (surveyQuestion == null) {
				return true;
			}
			List<NodeRef> questionSubsidiaries = surveyQuestion.getSubsidiaryRefs();
			if ((questionSubsidiaries == null) || questionSubsidiaries.isEmpty()) {
				return true;
			}
			return (entitySubsidiaries != null) && !entitySubsidiaries.isEmpty() && !Collections.disjoint(questionSubsidiaries, entitySubsidiaries);
		};
	}

	/**
	 * Keeps only the survey rows whose question is in the subsidiary scope of the entity, see
	 * {@link #createSubsidiaryScopeFilter(NodeRef)}. A row whose question cannot be resolved is kept.
	 *
	 * @param surveyListDataItems a {@link java.util.List} object
	 * @param subsidiaryScope a {@link java.util.function.Predicate} object
	 * @return a {@link java.util.List} object
	 */
	private List<SurveyListDataItem> filterSubsidiaryScope(List<SurveyListDataItem> surveyListDataItems,
			Predicate<SurveyQuestion> subsidiaryScope) {
		Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef = getSurveyQuestionCache().getSurveyQuestionByNodeRef();
		return surveyListDataItems.stream().filter(survey -> {
			if (survey.getQuestion() == null) {
				return true;
			}
			SurveyQuestion surveyQuestion = surveyQuestionByNodeRef.get(survey.getQuestion());
			if (surveyQuestion == null) {
				return true;
			}
			return subsidiaryScope.test(surveyQuestion);
		}).toList();
	}

	/**
	 * <p>appendQuestionDefinition.</p>
	 *
	 * @param definitions a {@link org.json.JSONArray} object
	 * @param surveyQuestion a {@link fr.becpg.repo.survey.data.SurveyQuestion} object
	 * @param questions a {@link java.util.Set} object
	 * @param questionRequirements a {@link java.util.Map} object
	 * @param subsidiaryScope a {@link java.util.function.Predicate} object, see
	 *            {@link #createSubsidiaryScopeFilter(NodeRef)} : always true unless the subsidiary
	 *            scope has been enabled
	 * @throws org.json.JSONException if any.
	 */
	private void appendQuestionDefinition(JSONArray definitions, SurveyQuestion surveyQuestion, Set<SurveyQuestion> questions,
			Map<String, List<RequirementListDataItem>> questionRequirements, Predicate<SurveyQuestion> subsidiaryScope) throws JSONException {

		if (!subsidiaryScope.test(surveyQuestion)) {
			return;
		}

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
						reqObj.put("message", req.getContentLocaleReqMessage());
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

					if (!CommentType.none.name().equals(surveyQuestion.getResponseCommentType())) {
						choice.put("comment", true);
						choice.put("commentLabel", getLabelOrHidden(surveyQuestion.getResponseCommentLabel()));
						if (CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
							choice.put("textarea", true);
						}
						choice.put("commentType", surveyQuestion.getResponseCommentType());
					}

					appendCids(choice, surveyQuestion, definitions, questions, questionRequirements, subsidiaryScope);

					choices.put(choice);

				} else {

					for (NodeRef nodeRef : definitionChoices) {

						SurveyQuestion defChoice = (SurveyQuestion) alfrescoRepository.findOne(nodeRef);
						JSONObject choice = new JSONObject();
						choice.put("id", defChoice.getNodeRef().getId());
						choice.put("label", defChoice.getLabel());
						appendCids(choice, defChoice, definitions, questions, questionRequirements, subsidiaryScope);

						if (!CommentType.none.name().equals(defChoice.getResponseCommentType())) {
							choice.put("comment", true);
							choice.put("commentLabel", getLabelOrHidden(defChoice.getResponseCommentLabel()));
							if (CommentType.textarea.toString().equals(defChoice.getResponseCommentType())) {
								choice.put("textarea", true);
							}
							choice.put("commentType", defChoice.getResponseCommentType());
						}

						choices.put(choice);
					}

				}

			} else if (!CommentType.none.name().equals(surveyQuestion.getResponseCommentType())) {
				JSONObject choice = new JSONObject();
				choice.put("id", "sub-" + surveyQuestion.getNodeRef().getId().substring(4));
				choice.put("label", "hidden");
				choice.put("comment", true);
				choice.put("commentLabel", getLabelOrHidden(surveyQuestion.getResponseCommentLabel()));
				if (CommentType.textarea.toString().equals(surveyQuestion.getResponseCommentType())) {
					choice.put("textarea", true);
				}
				choice.put("commentType", surveyQuestion.getResponseCommentType());

				appendCids(choice, surveyQuestion, definitions, questions, questionRequirements, subsidiaryScope);

				choices.put(choice);
			}
			if (choices.length() > 0) {
				definition.put("choices", choices);
			}
			definitions.put(definition);
		}
	}

	/**
	 * <p>getLabelOrHidden.</p>
	 *
	 * @param responseCommentLabel a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private String getLabelOrHidden(String responseCommentLabel) {
		return responseCommentLabel == null ? null : responseCommentLabel.isBlank() ? "hidden" : responseCommentLabel;
	}

	/**
	 * <p>appendCids.</p>
	 *
	 * @param choice a {@link org.json.JSONObject} object
	 * @param surveyQuestion a {@link fr.becpg.repo.survey.data.SurveyQuestion} object
	 * @param definitions a {@link org.json.JSONArray} object
	 * @param questions a {@link java.util.Set} object
	 * @param questionRequirements a {@link java.util.Map} object
	 * @param subsidiaryScope a {@link java.util.function.Predicate} object, see
	 *            {@link #createSubsidiaryScopeFilter(NodeRef)} : always true unless the subsidiary
	 *            scope has been enabled, in which case an out of scope follow-up question is neither
	 *            referenced nor defined
	 */
	private void appendCids(JSONObject choice, SurveyQuestion surveyQuestion, JSONArray definitions,
			Set<SurveyQuestion> questions, Map<String, List<RequirementListDataItem>> questionRequirements,
			Predicate<SurveyQuestion> subsidiaryScope) {
		if (surveyQuestion.getNextQuestions() != null) {
			JSONArray cids = new JSONArray();
			for (SurveyQuestion question : surveyQuestion.getNextQuestions()) {
				if (!subsidiaryScope.test(question)) {
					continue;
				}
				cids.put(question.getNodeRef().getId());
				appendQuestionDefinition(definitions, question, questions, questionRequirements, subsidiaryScope);
			}
			if (cids.length() > 0) {
				choice.put("cid", cids);
			}

		}

	}

	/**
	 * <p>getDefinitionChoices.</p>
	 *
	 * @param surveyQuestion a {@link fr.becpg.repo.survey.data.SurveyQuestion} object
	 * @return a {@link java.util.List} object
	 */
	private List<NodeRef> getDefinitionChoices(SurveyQuestion surveyQuestion) {
		BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION).andPropEquals(BeCPGModel.PROP_PARENT_LEVEL,
				surveyQuestion.getNodeRef().toString());

		Map<String, Boolean> sortBy = new LinkedHashMap<>();
		sortBy.put("@bcpg:sort", true);
		return query.addSort(sortBy).inDB().list();
	}

	/**
	 * <p>getOptions.</p>
	 *
	 * @param definitionChoices a {@link java.util.List} object
	 * @return a {@link java.lang.String} object
	 */
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
		return surveyListDataItems.stream().filter(q -> isVisible(q, surveyListDataItems)).toList();
	}

	/**
	 * <p>isVisible.</p>
	 *
	 * @param surveyListDataItem a {@link fr.becpg.repo.survey.data.SurveyListDataItem} object
	 * @param surveyListDataItems a {@link java.util.List} object
	 * @return a boolean
	 */
	private boolean isVisible(SurveyListDataItem surveyListDataItem, List<SurveyListDataItem> surveyListDataItems) {
		Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef = getSurveyQuestionCache().getSurveyQuestionByNodeRef();
		SurveyQuestion surveyQuestion = surveyQuestionByNodeRef.get(surveyListDataItem.getQuestion());
		if (surveyQuestion == null) {
			return true;
		}
		SurveyQuestion parentQuestion = extractParentQuestion(surveyQuestion);
		if (Boolean.TRUE.equals(parentQuestion.getIsVisible())) {
			return true;
		}
		boolean inNextQuestions = new ArrayList<>(surveyQuestionByNodeRef.values()).stream()
				.map(sQuestion -> getSurveyQuestionCache().getSurveyQuestionsByParent().get(sQuestion)).flatMap(List::stream)
				.map(surveyQuestionByNodeRef::get)
				.filter(sAnswer -> sAnswer.getNextQuestions() != null)
				.anyMatch(q -> q.getNextQuestions().contains(parentQuestion));
		if (!inNextQuestions) {
			return true;
		}
		return surveyListDataItems.stream().map(SurveyListDataItem::getChoices).flatMap(List::stream)
				.map(surveyQuestionByNodeRef::get)
				.filter(Objects::nonNull)
				.map(SurveyQuestion::getNextQuestions)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.anyMatch(parentQuestion::equals);
	}

	/**
	 * <p>extractParentQuestion.</p>
	 *
	 * @param surveyQuestion a {@link fr.becpg.repo.survey.data.SurveyQuestion} object
	 * @return a {@link fr.becpg.repo.survey.data.SurveyQuestion} object
	 */
	private SurveyQuestion extractParentQuestion(SurveyQuestion surveyQuestion){
		if (surveyQuestion.getParent() != null) {
			RepositoryEntity surveyQuestionItem = alfrescoRepository.findOne(surveyQuestion.getParent());
			if (surveyQuestionItem instanceof SurveyQuestion parentSurveyQuestion) {
				return parentSurveyQuestion;
			} else {
				logger.warn("survey question parent is not of type survey:surveyQuestion: " + surveyQuestion.getParent() 
				+ "for surveyQuestion: " + surveyQuestion.getNodeRef());
			}
		}
		return surveyQuestion;
	}

}
