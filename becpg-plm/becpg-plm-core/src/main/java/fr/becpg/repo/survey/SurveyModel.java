package fr.becpg.repo.survey;

import org.alfresco.service.namespace.QName;

public final class SurveyModel {

	private SurveyModel() {
	}

	public static final String SURVEY_URI = "http://www.bcpg.fr/model/survey/1.0";

	public static final String SURVEY_PREFIX = "survey";

	public static final QName MODEL = QName.createQName(SURVEY_URI, "surveymodel");

	public static final QName TYPE_SURVEY_QUESTION = QName.createQName(SURVEY_URI, "surveyQuestion");

	public static final QName PROP_SURVEY_QUESTION_LABEL = QName.createQName(SURVEY_URI, "questionLabel");

	public static final QName TYPE_SURVEY_LIST = QName.createQName(SURVEY_URI, "surveyList");

	public static final QName PROP_QUESTION_LABEL = QName.createQName(SURVEY_URI, "questionLabel");

	public static final QName PROP_QUESTION_SCORE = QName.createQName(SURVEY_URI, "questionScore");

}
