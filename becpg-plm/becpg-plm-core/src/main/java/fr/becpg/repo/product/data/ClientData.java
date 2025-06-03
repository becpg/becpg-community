package fr.becpg.repo.product.data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.catalog.CataloguableEntity;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.StateableEntity;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyableEntity;

/**
 * <p>ClientData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:client")
public class ClientData extends BeCPGDataObject implements HierarchicalEntity, StateableEntity, SurveyableEntity, CataloguableEntity {

	private static final long serialVersionUID = 5302327031354625757L;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private SystemState state = SystemState.Simulation;
	private NodeRef clientTpl;

	/*
	 * Formulation
	 */
	private Date formulatedDate;

	private Integer reformulateCount;
	private Integer currentReformulateCount;
	private String formulationChainId;
	private Boolean updateFormulatedDate = true;
	private String requirementChecksum;

	/*
	 * Survey Score
	 */
	private Integer clientScore;
	
	/*
	 * Completion scores
	 */
	private String entityScore;
	private List<String> reportLocales;


	private List<CostListDataItem> costList;
	private List<LCAListDataItem> lcaList;
	private List<ScoreListDataItem> scoreList;
	private List<SurveyListDataItem> surveyList;
	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ClientData} object
	 */
	public static ClientData build() {
		return new ClientData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.ClientData} object
	 */
	public ClientData withName(String name) {
		setName(name);
		return this;
	}
	

	/**
	 * <p>withScoreList.</p>
	 *
	 * @param scoreList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.ClientData} object
	 */
	public ClientData withScoreList(List<ScoreListDataItem> scoreList) {
		setScoreList(scoreList);
		return this;
	}
	

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.model.SystemState} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:clientState")
	public SystemState getState() {
		return state;
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param state a {@link fr.becpg.model.SystemState} object.
	 */
	public void setState(SystemState state) {
		this.state = state;
	}

	/** {@inheritDoc} */
	@Override
	public String getEntityState() {
		return state != null ? state.toString() : null;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "bcpg:clientHierarchy1")
	@Override
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	/**
	 * <p>Setter for the field <code>hierarchy1</code>.</p>
	 *
	 * @param hierarchy1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "bcpg:clientHierarchy2")
	@Override
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	/**
	 * <p>Setter for the field <code>hierarchy2</code>.</p>
	 *
	 * @param hierarchy2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}

	/**
	 * <p>Getter for the field <code>costList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:costList")
	public List<CostListDataItem> getCostList() {
		return costList;
	}

	/**
	 * <p>Setter for the field <code>costList</code>.</p>
	 *
	 * @param costList a {@link java.util.List} object.
	 */
	public void setCostList(List<CostListDataItem> costList) {
		this.costList = costList;
	}

	/**
	 * <p>Getter for the field <code>lcaList</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@DataList
	@AlfQname(qname = "bcpg:lcaList")
	public List<LCAListDataItem> getLcaList() {
		return lcaList;
	}

	/**
	 * <p>Setter for the field <code>lcaList</code>.</p>
	 *
	 * @param lcaList a {@link java.util.List} object
	 */
	public void setLcaList(List<LCAListDataItem> lcaList) {
		this.lcaList = lcaList;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "pjt:projectScore")
	@Override
	public Integer getScore() {
		return clientScore;
	}

	/** {@inheritDoc} */
	@Override
	public void setScore(Integer clientScore) {
		this.clientScore = clientScore;
	}
	
	/**
	 * <p>Getter for the field <code>entityScore</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:entityScore")
	public String getEntityScore() {
		return entityScore;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>entityScore</code>.</p>
	 */
	public void setEntityScore(String string) {
		this.entityScore = string;
	}

	/**
	 * <p>Getter for the field <code>reportLocales</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@AlfQname(qname = "rep:reportLocales")
	public List<String> getReportLocales() {
		return reportLocales;
	}

	/**
	 * <p>Setter for the field <code>reportLocales</code>.</p>
	 *
	 * @param reportLocales a {@link java.util.List} object.
	 */
	public void setReportLocales(List<String> reportLocales) {
		this.reportLocales = reportLocales;
	}


	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>formulatedDate</code>.</p>
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:formulatedDate")
	@Override
	public Date getFormulatedDate() {
		return formulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulatedDate(Date formulatedDate) {
		this.formulatedDate = formulatedDate;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>requirementChecksum</code>.</p>
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:requirementChecksum")
	@Override
	public String getRequirementChecksum() {
		return requirementChecksum;
	}

	/**
	 * <p>Setter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @param requirementChecksum a {@link java.lang.String} object.
	 */
	public void setRequirementChecksum(String requirementChecksum) {
		this.requirementChecksum = requirementChecksum;
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldUpdateFormulatedDate() {
		return updateFormulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setUpdateFormulatedDate(boolean updateFormulatedDate) {
		this.updateFormulatedDate = updateFormulatedDate;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>reformulateCount</code>.</p>
	 */
	@Override
	public Integer getReformulateCount() {
		return reformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setReformulateCount(Integer reformulateCount) {
		this.reformulateCount = reformulateCount;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>currentReformulateCount</code>.</p>
	 */
	@Override
	public Integer getCurrentReformulateCount() {
		return currentReformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentReformulateCount(Integer currentReformulateCount) {
		this.currentReformulateCount = currentReformulateCount;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>formulationChainId</code>.</p>
	 */
	@Override
	public String getFormulationChainId() {
		return formulationChainId;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulationChainId(String formulationChainId) {
		this.formulationChainId = formulationChainId;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>getFormulatedEntityTpl.</p>
	 */
	@Override
	public NodeRef getFormulatedEntityTpl() {
		return clientTpl;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>surveyList</code>.</p>
	 */
	@DataList
	@AlfQname(qname = "survey:surveyList")
	@Override
	public List<SurveyListDataItem> getSurveyList() {
		return surveyList;
	}

	/** {@inheritDoc} */
	@Override
	public void setSurveyList(List<SurveyListDataItem> surveyList) {
		this.surveyList = surveyList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>scoreList</code>.</p>
	 */
	@DataList
	@AlfQname(qname = "pjt:scoreList")
	@Override
	public List<ScoreListDataItem> getScoreList() {
		return scoreList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>scoreList</code>.</p>
	 */
	@Override
	public void setScoreList(List<ScoreListDataItem> scoreList) {
		this.scoreList = scoreList;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ClientData [name=" + name + ", hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", state=" + state + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(clientScore, clientTpl, costList, currentReformulateCount, formulatedDate, formulationChainId,
				hierarchy1, hierarchy2, lcaList, reformulateCount, requirementChecksum, scoreList, state, surveyList, updateFormulatedDate);
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
		ClientData other = (ClientData) obj;
		return Objects.equals(clientScore, other.clientScore) && Objects.equals(clientTpl, other.clientTpl)
				&& Objects.equals(costList, other.costList) && Objects.equals(currentReformulateCount, other.currentReformulateCount)
				&& Objects.equals(formulatedDate, other.formulatedDate) && Objects.equals(formulationChainId, other.formulationChainId)
				&& Objects.equals(hierarchy1, other.hierarchy1) && Objects.equals(hierarchy2, other.hierarchy2)
				&& Objects.equals(lcaList, other.lcaList) && Objects.equals(reformulateCount, other.reformulateCount)
				&& Objects.equals(requirementChecksum, other.requirementChecksum) && Objects.equals(scoreList, other.scoreList)
				&& state == other.state && Objects.equals(surveyList, other.surveyList)
				&& Objects.equals(updateFormulatedDate, other.updateFormulatedDate);
	}

}
