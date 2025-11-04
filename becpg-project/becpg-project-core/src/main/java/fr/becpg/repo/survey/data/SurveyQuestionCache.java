package fr.becpg.repo.survey.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>SurveyQuestionCache class.</p>
 *
 * @author matthieu
 */
public class SurveyQuestionCache {

	private Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef = new HashMap<>();
	
	private Map<SurveyQuestion, List<NodeRef>> surveyQuestionsByParent = new HashMap<>();
	
	private List<SurveyQuestion> generatedSurveyQuestions = new ArrayList<>();

	/**
	 * <p>Constructor for SurveyQuestionCache.</p>
	 *
	 * @param surveyQuestionByNodeRef a {@link java.util.Map} object
	 * @param surveyQuestionsByParent a {@link java.util.Map} object
	 * @param generatedSurveyQuestions a {@link java.util.List} object
	 */
	public SurveyQuestionCache(Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef, Map<SurveyQuestion, List<NodeRef>> surveyQuestionsByParent,
			List<SurveyQuestion> generatedSurveyQuestions) {
		super();
		this.surveyQuestionByNodeRef = surveyQuestionByNodeRef;
		this.surveyQuestionsByParent = surveyQuestionsByParent;
		this.generatedSurveyQuestions = generatedSurveyQuestions;
	}

	/**
	 * <p>Getter for the field <code>surveyQuestionByNodeRef</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<NodeRef, SurveyQuestion> getSurveyQuestionByNodeRef() {
		return surveyQuestionByNodeRef;
	}

	/**
	 * <p>Getter for the field <code>surveyQuestionsByParent</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<SurveyQuestion, List<NodeRef>> getSurveyQuestionsByParent() {
		return surveyQuestionsByParent;
	}

	/**
	 * <p>Getter for the field <code>generatedSurveyQuestions</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<SurveyQuestion> getGeneratedSurveyQuestions() {
		return generatedSurveyQuestions;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SurveyQuestionCache [surveyQuestionByNodeRef=" + surveyQuestionByNodeRef + ", surveyQuestionsByParent=" + surveyQuestionsByParent
				+ ", generatedSurveyQuestions=" + generatedSurveyQuestions + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(generatedSurveyQuestions, surveyQuestionByNodeRef, surveyQuestionsByParent);
	}

	/** {@inheritDoc} */
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
