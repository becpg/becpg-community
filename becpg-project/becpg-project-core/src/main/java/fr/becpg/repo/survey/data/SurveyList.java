package fr.becpg.repo.survey.data;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "survey:surveyList")
public class SurveyList extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6180023906108178900L;

	private String comment;

	private NodeRef question;

	private List<NodeRef> choices;

	private Integer sort;
	
	private boolean generated;
	
	public SurveyList() {}

	public SurveyList(NodeRef question, boolean generated) {
		this.question = question;
		this.generated = generated;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	@AlfProp
	@AlfQname(qname = "survey:slComment")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "survey:slQuestion")
	public NodeRef getQuestion() {
		return question;
	}

	public void setQuestion(NodeRef question) {
		this.question = question;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "survey:slChoices")
	public List<NodeRef> getChoices() {
		return choices;
	}

	public void setChoices(List<NodeRef> choices) {
		this.choices = choices;
	}
	
	@AlfProp
	@AlfQname(qname = "survey:generated")
	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(choices, comment, question, generated);
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
		SurveyList other = (SurveyList) obj;
		return Objects.equals(choices, other.choices) && Objects.equals(comment, other.comment)
				&& Objects.equals(question, other.question) && Objects.equals(generated, other.generated);
	}

	@Override
	public String toString() {
		return "SurveyList [comment=" + comment + ", question=" + question + ", choices=" + choices + ", sort=" + sort
				+ ", generated=" + generated + "]";
	}
}
