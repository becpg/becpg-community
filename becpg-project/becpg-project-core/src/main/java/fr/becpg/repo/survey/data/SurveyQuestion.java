package fr.becpg.repo.survey.data;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
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
	private String surveyCriterion;
	private Integer questionScore;
	private Boolean isMandatory;
	private Boolean isVisible;
	private String responseType;
	private String responseCommentType;
	private String responseCommentLabel;
	private List<SurveyQuestion> nextQuestions;

	private Integer sort;
	

	private List<NodeRef> fsLinkedCharactRefs;
	private List<QName> fsLinkedTypes;
	private List<NodeRef> fsLinkedHierarchy;

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
	@AlfQname(qname = "survey:questionLabel")
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
	@AlfQname(qname = "survey:questionCriterion")
	public String getSurveyCriterion() {
		return surveyCriterion;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionScore")
	public Integer getQuestionScore() {
		return questionScore;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionIsMandatory")
	public Boolean getIsMandatory() {
		return isMandatory;
	}

	@AlfProp
	@AlfQname(qname = "survey:questionIsVisible")
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

	@AlfMultiAssoc(isEntity = true)
	@AlfReadOnly
	@AlfQname(qname = "survey:nextQuestion")
	public List<SurveyQuestion> getNextQuestions() {
		return nextQuestions;
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

	public void setSurveyCriterion(String surveyCriterion) {
		this.surveyCriterion = surveyCriterion;
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

	public void setNextQuestions(List<SurveyQuestion> nextQuestions) {
		this.nextQuestions = nextQuestions;
	}

	@AlfMultiAssoc
	@AlfReadOnly
	@AlfQname(qname = "survey:fsLinkedCharactRefs")
	public List<NodeRef> getFsLinkedCharactRefs() {
		return fsLinkedCharactRefs;
	}

	public void setFsLinkedCharactRefs(List<NodeRef> fsLinkedCharactRefs) {
		this.fsLinkedCharactRefs = fsLinkedCharactRefs;
	}

	@AlfProp
	@AlfQname(qname = "survey:fsLinkedTypes")
	public List<QName> getFsLinkedTypes() {
		return fsLinkedTypes;
	}

	public void setFsLinkedTypes(List<QName> fsLinkedTypes) {
		this.fsLinkedTypes = fsLinkedTypes;
	}

	@AlfMultiAssoc
	@AlfReadOnly
	@AlfQname(qname = "survey:fsLinkedHierarchy")
	public List<NodeRef> getFsLinkedHierarchy() {
		return fsLinkedHierarchy;
	}

	public void setFsLinkedHierarchy(List<NodeRef> fsLinkedHierarchy) {
		this.fsLinkedHierarchy = fsLinkedHierarchy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(fsLinkedCharactRefs, fsLinkedHierarchy, fsLinkedTypes, isMandatory, isVisible, label,
						nextQuestions, parent, questionLowerNote, questionNote, questionScore, questionUpperNote,
						questionUrl, responseCommentLabel, responseCommentType, responseType, sort, surveyCriterion);
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
		return Objects.equals(fsLinkedCharactRefs, other.fsLinkedCharactRefs)
				&& Objects.equals(fsLinkedHierarchy, other.fsLinkedHierarchy)
				&& Objects.equals(fsLinkedTypes, other.fsLinkedTypes) && Objects.equals(isMandatory, other.isMandatory)
				&& Objects.equals(isVisible, other.isVisible) && Objects.equals(label, other.label)
				&& Objects.equals(nextQuestions, other.nextQuestions) && Objects.equals(parent, other.parent)
				&& Objects.equals(questionLowerNote, other.questionLowerNote)
				&& Objects.equals(questionNote, other.questionNote)
				&& Objects.equals(questionScore, other.questionScore)
				&& Objects.equals(questionUpperNote, other.questionUpperNote)
				&& Objects.equals(questionUrl, other.questionUrl)
				&& Objects.equals(responseCommentLabel, other.responseCommentLabel)
				&& Objects.equals(responseCommentType, other.responseCommentType)
				&& Objects.equals(responseType, other.responseType) && Objects.equals(sort, other.sort)
				&& Objects.equals(surveyCriterion, other.surveyCriterion);
	}

	@Override
	public String toString() {
		return "SurveyQuestion [parent=" + parent + ", label=" + label + ", questionNote=" + questionNote
				+ ", questionUpperNote=" + questionUpperNote + ", questionLowerNote=" + questionLowerNote
				+ ", questionUrl=" + questionUrl + ", surveyCriterion=" + surveyCriterion + ", questionScore="
				+ questionScore + ", isMandatory=" + isMandatory + ", isVisible=" + isVisible + ", responseType="
				+ responseType + ", responseCommentType=" + responseCommentType + ", responseCommentLabel="
				+ responseCommentLabel + ", nextQuestions=" + nextQuestions + ", sort=" + sort
				+ ", fsLinkedCharactRefs=" + fsLinkedCharactRefs + ", fsLinkedTypes=" + fsLinkedTypes
				+ ", fsLinkedHierarchy=" + fsLinkedHierarchy + "]";
	}
}
