package fr.becpg.repo.survey.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

public class SurveyQuestionCache {

	private Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef = new HashMap<>();
	
	private Map<SurveyQuestion, List<NodeRef>> surveyQuestionsByParent = new HashMap<>();
	
	private List<SurveyQuestion> generatedSurveyQuestions = new ArrayList<>();

	public SurveyQuestionCache(Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef, Map<SurveyQuestion, List<NodeRef>> surveyQuestionsByParent,
			List<SurveyQuestion> generatedSurveyQuestions) {
		super();
		this.surveyQuestionByNodeRef = surveyQuestionByNodeRef;
		this.surveyQuestionsByParent = surveyQuestionsByParent;
		this.generatedSurveyQuestions = generatedSurveyQuestions;
	}

	public Map<NodeRef, SurveyQuestion> getSurveyQuestionByNodeRef() {
		return surveyQuestionByNodeRef;
	}

	public Map<SurveyQuestion, List<NodeRef>> getSurveyQuestionsByParent() {
		return surveyQuestionsByParent;
	}

	public List<SurveyQuestion> getGeneratedSurveyQuestions() {
		return generatedSurveyQuestions;
	}

	@Override
	public String toString() {
		return "SurveyQuestionCache [surveyQuestionByNodeRef=" + surveyQuestionByNodeRef + ", surveyQuestionsByParent=" + surveyQuestionsByParent
				+ ", generatedSurveyQuestions=" + generatedSurveyQuestions + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(generatedSurveyQuestions, surveyQuestionByNodeRef, surveyQuestionsByParent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyQuestionCache other = (SurveyQuestionCache) obj;
		return Objects.equals(generatedSurveyQuestions, other.generatedSurveyQuestions)
				&& Objects.equals(surveyQuestionByNodeRef, other.surveyQuestionByNodeRef)
				&& Objects.equals(surveyQuestionsByParent, other.surveyQuestionsByParent);
	}
	
}
