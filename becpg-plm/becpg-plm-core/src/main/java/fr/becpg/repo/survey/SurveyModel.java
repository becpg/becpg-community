package fr.becpg.repo.survey;

import org.alfresco.service.namespace.QName;

public final class SurveyModel {

	private SurveyModel() {
	}

	public static final String SURVEY_URI = "http://www.bcpg.fr/model/survey/1.0";

	public static final String SURVEY_PREFIX = "survey";

	public static final QName MODEL = QName.createQName(SURVEY_URI, "surveymodel");

	public static final QName TYPE_SURVEY_QUESTION = QName.createQName(SURVEY_URI, "surveyQuestion");
	
	public static final QName PROP_SURVEY_LABEL = QName.createQName(SURVEY_URI, "label");

	public static final QName TYPE_SURVEY =  QName.createQName(SURVEY_URI, "survey");

}
