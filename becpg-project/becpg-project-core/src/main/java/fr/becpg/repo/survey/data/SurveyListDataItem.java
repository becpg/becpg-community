package fr.becpg.repo.survey.data;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>SurveyListDataItem class.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "survey:surveyList")
public class SurveyListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6180023906108178900L;

	private String comment;

	private NodeRef question;

	private List<NodeRef> choices;

	private Integer sort;
	
	private Boolean generated;

	private List<String> reportKinds;
	
	/**
	 * <p>Constructor for SurveyListDataItem.</p>
	 */
	public SurveyListDataItem() {}

	/**
	 * <p>Constructor for SurveyListDataItem.</p>
	 *
	 * @param question a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param generated a boolean
	 */
	public SurveyListDataItem(NodeRef question, boolean generated) {
		this.question = question;
		this.generated = generated;
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
	@DataListIdentifierAttr
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
	
	/**
	 * <p>Getter for the field <code>generated</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "survey:generated")
	@InternalField
	public Boolean getGenerated() {
		return generated;
	}

	/**
	 * <p>Setter for the field <code>generated</code>.</p>
	 *
	 * @param generated a {@link java.lang.Boolean} object
	 */
	public void setGenerated(Boolean generated) {
		this.generated = generated;
	}
	
	/**
	 * <p>Getter for the field <code>reportKinds</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "rep:reportKinds")
	public List<String> getReportKinds() {
		return reportKinds;
	}
	
	/**
	 * <p>Setter for the field <code>reportKinds</code>.</p>
	 *
	 * @param choices a {@link java.util.List} object
	 */
	public void setReportKinds(List<String> reportKinds) {
		this.reportKinds = reportKinds;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(choices, comment, question, generated, reportKinds);
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
		SurveyListDataItem other = (SurveyListDataItem) obj;
		return Objects.equals(choices, other.choices) && Objects.equals(comment, other.comment)
				&& Objects.equals(question, other.question) && Objects.equals(generated, other.generated)
				&& Objects.equals(reportKinds, other.reportKinds);
	}

	@Override
	public String toString() {
		return "SurveyListDataItem [comment=" + comment + ", question=" + question + ", choices=" + choices + ", sort="
				+ sort + ", generated=" + generated + ", reportKinds=" + reportKinds + "]";
	}
}
