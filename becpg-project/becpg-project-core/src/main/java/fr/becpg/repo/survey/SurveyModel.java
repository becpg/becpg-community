package fr.becpg.repo.survey;

import org.alfresco.service.namespace.QName;

/**
 * <p>SurveyModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class SurveyModel {

	private SurveyModel() {
		//Do Nothing
	}

	/** Constant <code>SURVEY_URI="http://www.bcpg.fr/model/survey/1.0"</code> */
	public static final String SURVEY_URI = "http://www.bcpg.fr/model/survey/1.0";

	/** Constant <code>SURVEY_PREFIX="survey"</code> */
	public static final String SURVEY_PREFIX = "survey";

	/** Constant <code>MODEL</code> */
	public static final QName MODEL = QName.createQName(SURVEY_URI, "surveymodel");

	/** Constant <code>TYPE_SURVEY_QUESTION</code> */
	public static final QName TYPE_SURVEY_QUESTION = QName.createQName(SURVEY_URI, "surveyQuestion");

	/** Constant <code>PROP_SURVEY_QUESTION_LABEL</code> */
	public static final QName PROP_SURVEY_QUESTION_LABEL = QName.createQName(SURVEY_URI, "questionLabel");
	
	/** Constant <code>PROP_SURVEY_QUESTION_SCORE</code> */
	public static final QName PROP_SURVEY_QUESTION_SCORE = QName.createQName(SURVEY_URI, "questionScore");

	/** Constant <code>PROP_SURVEY_QUESTION_SCORE</code> */
	public static final QName PROP_SURVEY_FS_LINKED_TYPE = QName.createQName(SURVEY_URI, "fsLinkedTypes");
	
	/** Constant <code>TYPE_SURVEY_LIST</code> */
	public static final QName TYPE_SURVEY_LIST = QName.createQName(SURVEY_URI, "surveyList");
	
	/** Constant <code>ASSOC_SURVEY_FS_LINKED_CHARACT_REFS</code> */
	public static final QName ASSOC_SURVEY_FS_LINKED_CHARACT_REFS = QName.createQName(SURVEY_URI, "fsLinkedCharactRefs");

	/** Constant <code>ASSOC_SURVEY_FS_LINKED_HIERARCHY</code> */
	public static final QName ASSOC_SURVEY_FS_LINKED_HIERARCHY = QName.createQName(SURVEY_URI, "fsLinkedHierarchy");

	/** Constant <code>PROP_RESPONSE_TYPE</code> */
	public static final QName PROP_RESPONSE_TYPE = QName.createQName(SURVEY_URI, "responseType");

	/** Constant <code>ASSOC_SCORE_CRITERION</code> */
	public static final QName ASSOC_SCORE_CRITERION =  QName.createQName(SURVEY_URI, "scoreCriterion");
	
}
