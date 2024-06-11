package fr.becpg.repo.survey.data;

import java.util.List;
import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>SurveyQuestion class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "survey:surveyQuestion")
public class SurveyQuestion extends BeCPGDataObject {

	/**
	 *
	 */
	private static final long serialVersionUID = -1181753750547793094L;

	private SurveyQuestion parent;

	private String label;

	private String questionNote;
	private String questionUpperNote;

	private String questionLowerNote;
	private String questionUrl;
	private String surveyCriterion;
	private Integer questionScore;
	private Boolean isMandatory;
	private Boolean isVisible;
	private String responseType;
	private String responseCommentType;
	private String responseCommentLabel;
	private List<SurveyQuestion> nextQuestions;

	private Integer sort;

	/**
	 * <p>Getter for the field <code>sort</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	/**
	 * <p>Setter for the field <code>sort</code>.</p>
	 *
	 * @param sort a {@link java.lang.Integer} object
	 */
	public void setSort(Integer sort) {
		this.sort = sort;
	}

	/**
	 * <p>Getter for the field <code>parent</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.survey.data.SurveyQuestion} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	public SurveyQuestion getParent() {
		return parent;
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionLabel")
	public String getLabel() {
		return label;
	}

	/**
	 * <p>Getter for the field <code>questionNote</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionNote")
	public String getQuestionNote() {
		return questionNote;
	}

	/**
	 * <p>Getter for the field <code>questionUpperNote</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionUpperNote")
	public String getQuestionUpperNote() {
		return questionUpperNote;
	}

	/**
	 * <p>Getter for the field <code>questionLowerNote</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionLowerNote")
	public String getQuestionLowerNote() {
		return questionLowerNote;
	}

	/**
	 * <p>Getter for the field <code>questionUrl</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionUrl")
	public String getQuestionUrl() {
		return questionUrl;
	}

	/**
	 * <p>Getter for the field <code>surveyCriterion</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionCriterion")
	public String getSurveyCriterion() {
		return surveyCriterion;
	}

	/**
	 * <p>Getter for the field <code>questionScore</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionScore")
	public Integer getQuestionScore() {
		return questionScore;
	}

	/**
	 * <p>Getter for the field <code>isMandatory</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionIsMandatory")
	public Boolean getIsMandatory() {
		return isMandatory;
	}

	/**
	 * <p>Getter for the field <code>isVisible</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:questionIsVisible")
	public Boolean getIsVisible() {
		return isVisible;
	}

	/**
	 * <p>Setter for the field <code>isVisible</code>.</p>
	 *
	 * @param isVisible a {@link java.lang.Boolean} object
	 */
	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * <p>Getter for the field <code>responseType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:responseType")
	public String getResponseType() {
		return responseType;
	}

	/**
	 * <p>Getter for the field <code>responseCommentType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:responseCommentType")
	public String getResponseCommentType() {
		return responseCommentType;
	}

	/**
	 * <p>Getter for the field <code>responseCommentLabel</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:responseCommentLabel")
	public String getResponseCommentLabel() {
		return responseCommentLabel;
	}

	/**
	 * <p>Getter for the field <code>nextQuestions</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc(isEntity = true)
	@AlfReadOnly
	@AlfQname(qname = "survey:nextQuestion")
	public List<SurveyQuestion> getNextQuestions() {
		return nextQuestions;
	}

	/**
	 * <p>Setter for the field <code>parent</code>.</p>
	 *
	 * @param parent a {@link fr.becpg.repo.survey.data.SurveyQuestion} object
	 */
	public void setParent(SurveyQuestion parent) {
		this.parent = parent;
	}

	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link java.lang.String} object
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * <p>Setter for the field <code>questionNote</code>.</p>
	 *
	 * @param questionNote a {@link java.lang.String} object
	 */
	public void setQuestionNote(String questionNote) {
		this.questionNote = questionNote;
	}

	/**
	 * <p>Setter for the field <code>questionUpperNote</code>.</p>
	 *
	 * @param questionUpperNote a {@link java.lang.String} object
	 */
	public void setQuestionUpperNote(String questionUpperNote) {
		this.questionUpperNote = questionUpperNote;
	}

	/**
	 * <p>Setter for the field <code>questionLowerNote</code>.</p>
	 *
	 * @param questionLowerNote a {@link java.lang.String} object
	 */
	public void setQuestionLowerNote(String questionLowerNote) {
		this.questionLowerNote = questionLowerNote;
	}

	/**
	 * <p>Setter for the field <code>questionUrl</code>.</p>
	 *
	 * @param questionUrl a {@link java.lang.String} object
	 */
	public void setQuestionUrl(String questionUrl) {
		this.questionUrl = questionUrl;
	}

	/**
	 * <p>Setter for the field <code>surveyCriterion</code>.</p>
	 *
	 * @param surveyCriterion a {@link java.lang.String} object
	 */
	public void setSurveyCriterion(String surveyCriterion) {
		this.surveyCriterion = surveyCriterion;
	}

	/**
	 * <p>Setter for the field <code>questionScore</code>.</p>
	 *
	 * @param questionScore a {@link java.lang.Integer} object
	 */
	public void setQuestionScore(Integer questionScore) {
		this.questionScore = questionScore;
	}

	/**
	 * <p>Setter for the field <code>isMandatory</code>.</p>
	 *
	 * @param isMandatory a {@link java.lang.Boolean} object
	 */
	public void setIsMandatory(Boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	/**
	 * <p>Setter for the field <code>responseType</code>.</p>
	 *
	 * @param responseType a {@link java.lang.String} object
	 */
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	/**
	 * <p>Setter for the field <code>responseCommentType</code>.</p>
	 *
	 * @param responseCommentType a {@link java.lang.String} object
	 */
	public void setResponseCommentType(String responseCommentType) {
		this.responseCommentType = responseCommentType;
	}

	/**
	 * <p>Setter for the field <code>responseCommentLabel</code>.</p>
	 *
	 * @param responseCommentLabel a {@link java.lang.String} object
	 */
	public void setResponseCommentLabel(String responseCommentLabel) {
		this.responseCommentLabel = responseCommentLabel;
	}

	/**
	 * <p>Setter for the field <code>nextQuestions</code>.</p>
	 *
	 * @param nextQuestions a {@link java.util.List} object
	 */
	public void setNextQuestions(List<SurveyQuestion> nextQuestions) {
		this.nextQuestions = nextQuestions;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(isMandatory, isVisible, label, nextQuestions, parent, questionLowerNote, questionNote, questionScore,
				questionUpperNote, questionUrl, surveyCriterion, responseCommentLabel, responseCommentType, responseType, sort);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyQuestion other = (SurveyQuestion) obj;
		return Objects.equals(isMandatory, other.isMandatory) && Objects.equals(isVisible, other.isVisible) && Objects.equals(label, other.label)
				&& Objects.equals(nextQuestions, other.nextQuestions) && Objects.equals(parent, other.parent)
				&& Objects.equals(questionLowerNote, other.questionLowerNote) && Objects.equals(questionNote, other.questionNote)
				&& Objects.equals(questionScore, other.questionScore) && Objects.equals(questionUpperNote, other.questionUpperNote)
				&& Objects.equals(questionUrl, other.questionUrl) && Objects.equals(surveyCriterion, other.surveyCriterion)
				&& Objects.equals(responseCommentLabel, other.responseCommentLabel) && Objects.equals(responseCommentType, other.responseCommentType)
				&& Objects.equals(responseType, other.responseType) && Objects.equals(sort, other.sort);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SurveyQuestion [parent=" + parent + ", label=" + label + ", questionNote=" + questionNote + ", questionUpperNote=" + questionUpperNote
				+ ", questionLowerNote=" + questionLowerNote + ", questionUrl=" + questionUrl + ", surveyCriterion=" + surveyCriterion
				+ ", questionScore=" + questionScore + ", isMandatory=" + isMandatory + ", responseType=" + responseType + ", responseCommentType="
				+ responseCommentType + ", responseCommentLabel=" + responseCommentLabel + ", nextQuestion=" + nextQuestions + ", sort=" + sort + "]";
	}

}
