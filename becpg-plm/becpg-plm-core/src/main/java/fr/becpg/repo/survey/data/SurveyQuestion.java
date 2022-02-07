package fr.becpg.repo.survey.data;

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

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
	private Integer questionScore;
	private Boolean isMandatory;
	private Boolean isVisible;
	private String responseType;
	private String responseCommentType;
	private String responseCommentLabel;
	private SurveyQuestion nextQuestion;
	
    private Integer sort;
    
    
    @AlfProp
	@AlfQname(qname = "bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	public SurveyQuestion getParent() {
		return parent;
	}

	@AlfProp
	@AlfQname(qname = "survey:label")
	public String getLabel() {
		return label;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionNote")
	public String getQuestionNote() {
		return questionNote;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionUpperNote")
	public String getQuestionUpperNote() {
		return questionUpperNote;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionLowerNote")
	public String getQuestionLowerNote() {
		return questionLowerNote;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionUrl")
	public String getQuestionUrl() {
		return questionUrl;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionScore")
	public Integer getQuestionScore() {
		return questionScore;
	}

	@AlfProp
	@AlfQname(qname = "survey:isMandatory")
	public Boolean getIsMandatory() {
		return isMandatory;
	}

	@AlfProp
	@AlfQname(qname = "survey:isVisible")
	public Boolean getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}

	@AlfProp
	@AlfQname(qname = "survey:responseType")
	public String getResponseType() {
		return responseType;
	}

	@AlfProp
	@AlfQname(qname = "survey:responseCommentType")
	public String getResponseCommentType() {
		return responseCommentType;
	}

	@AlfProp
	@AlfQname(qname = "survey:responseCommentLabel")
	public String getResponseCommentLabel() {
		return responseCommentLabel;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "survey:nextQuestion")
	public SurveyQuestion getNextQuestion() {
		return nextQuestion;
	}
	
	

	public void setParent(SurveyQuestion parent) {
		this.parent = parent;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setQuestionNote(String questionNote) {
		this.questionNote = questionNote;
	}

	public void setQuestionUpperNote(String questionUpperNote) {
		this.questionUpperNote = questionUpperNote;
	}

	public void setQuestionLowerNote(String questionLowerNote) {
		this.questionLowerNote = questionLowerNote;
	}

	public void setQuestionUrl(String questionUrl) {
		this.questionUrl = questionUrl;
	}

	public void setQuestionScore(Integer questionScore) {
		this.questionScore = questionScore;
	}

	public void setIsMandatory(Boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public void setResponseCommentType(String responseCommentType) {
		this.responseCommentType = responseCommentType;
	}

	public void setResponseCommentLabel(String responseCommentLabel) {
		this.responseCommentLabel = responseCommentLabel;
	}

	public void setNextQuestion(SurveyQuestion nextQuestion) {
		this.nextQuestion = nextQuestion;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(isMandatory, isVisible, label, nextQuestion, parent, questionLowerNote, questionNote, questionScore,
				questionUpperNote, questionUrl, responseCommentLabel, responseCommentType, responseType, sort);
		return result;
	}

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
				&& Objects.equals(nextQuestion, other.nextQuestion) && Objects.equals(parent, other.parent)
				&& Objects.equals(questionLowerNote, other.questionLowerNote) && Objects.equals(questionNote, other.questionNote)
				&& Objects.equals(questionScore, other.questionScore) && Objects.equals(questionUpperNote, other.questionUpperNote)
				&& Objects.equals(questionUrl, other.questionUrl) && Objects.equals(responseCommentLabel, other.responseCommentLabel)
				&& Objects.equals(responseCommentType, other.responseCommentType) && Objects.equals(responseType, other.responseType)
				&& Objects.equals(sort, other.sort);
	}

	@Override
	public String toString() {
		return "SurveyQuestion [parent=" + parent + ", label=" + label + ", questionNote=" + questionNote + ", questionUpperNote=" + questionUpperNote
				+ ", questionLowerNote=" + questionLowerNote + ", questionUrl=" + questionUrl + ", questionScore=" + questionScore + ", isMandatory="
				+ isMandatory + ", responseType=" + responseType + ", responseCommentType=" + responseCommentType + ", responseCommentLabel="
				+ responseCommentLabel + ", nextQuestion=" + nextQuestion + ", sort=" + sort + "]";
	}
	
	

}
