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

	/** Constant <code>TYPE_SURVEY_LIST</code> */
	public static final QName TYPE_SURVEY_LIST = QName.createQName(SURVEY_URI, "surveyList");


}
