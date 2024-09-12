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

/**
 * <p>SurveyList class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
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
	
	public SurveyList() {}

	public SurveyList(NodeRef question) {
		this.question = question;
	}

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
	 * <p>Getter for the field <code>comment</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:slComment")
	public String getComment() {
		return comment;
	}

	/**
	 * <p>Setter for the field <code>comment</code>.</p>
	 *
	 * @param comment a {@link java.lang.String} object
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * <p>Getter for the field <code>question</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "survey:slQuestion")
	public NodeRef getQuestion() {
		return question;
	}

	/**
	 * <p>Setter for the field <code>question</code>.</p>
	 *
	 * @param question a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setQuestion(NodeRef question) {
		this.question = question;
	}

	/**
	 * <p>Getter for the field <code>choices</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "survey:slChoices")
	public List<NodeRef> getChoices() {
		return choices;
	}

	/**
	 * <p>Setter for the field <code>choices</code>.</p>
	 *
	 * @param choices a {@link java.util.List} object
	 */
	public void setChoices(List<NodeRef> choices) {
		this.choices = choices;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(choices, comment, question);
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
		SurveyList other = (SurveyList) obj;
		return Objects.equals(choices, other.choices) && Objects.equals(comment, other.comment) && Objects.equals(question, other.question);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Survey [comment=" + comment + ", question=" + question + ", choices=" + choices + "]";
	}

}
