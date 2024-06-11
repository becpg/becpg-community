package fr.becpg.repo.product.data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.product.data.productList.ContactListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.product.data.productList.PlantListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.StateableEntity;
import fr.becpg.repo.survey.data.SurveyList;
import fr.becpg.repo.survey.data.SurveyableEntity;

/**
 * <p>SupplierData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:supplier")
public class SupplierData extends BeCPGDataObject implements HierarchicalEntity, StateableEntity, SurveyableEntity {

	private static final long serialVersionUID = -2554133542406623412L;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private SystemState state = SystemState.Simulation;

	private List<CostListDataItem> costList;

	private List<PlantListDataItem> plantList;

	private List<ContactListDataItem> contactList;

	private List<LCAListDataItem> lcaList;

	private SupplierData entityTpl;

	/*
	 * Formulation
	 */
	private Date formulatedDate;
	private Integer reformulateCount;
	private Integer currentReformulateCount;
	private String formulationChainId;
	private Boolean updateFormulatedDate = true;
	private String requirementChecksum;

	// Survey Entity
	private List<ScoreListDataItem> scoreList;
	private List<SurveyList> surveyList;
	private Integer supplierScore;

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.model.SystemState} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:supplierState")
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
	@AlfQname(qname = "bcpg:supplierHierarchy1")
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
	@AlfQname(qname = "bcpg:supplierHierarchy2")
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
	 * <p>Getter for the field <code>plantList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:plant")
	public List<PlantListDataItem> getPlantList() {
		return plantList;
	}

	/**
	 * <p>Setter for the field <code>plantList</code>.</p>
	 *
	 * @param plantList a {@link java.util.List} object.
	 */
	public void setPlantList(List<PlantListDataItem> plantList) {
		this.plantList = plantList;
	}

	/**
	 * <p>Getter for the field <code>contactList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:contactList")
	public List<ContactListDataItem> getContactList() {
		return contactList;
	}

	/**
	 * <p>Setter for the field <code>contactList</code>.</p>
	 *
	 * @param contactList a {@link java.util.List} object.
	 */
	public void setContactList(List<ContactListDataItem> contactList) {
		this.contactList = contactList;
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
	 * <p>getFormulatedEntityTpl.</p>
	 */
	@Override
	public NodeRef getFormulatedEntityTpl() {
		return entityTpl != null ? entityTpl.getNodeRef() : null;
	}

	/**
	 * <p>Setter for the field <code>projectTpl</code>.</p>
	 *
	 * @param entityTpl a {@link fr.becpg.repo.product.data.SupplierData} object
	 */
	public void setEntityTpl(SupplierData entityTpl) {
		this.entityTpl = entityTpl;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>requirementChecksum</code>.</p>
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:requirementChecksum")
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

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>formulatedDate</code>.</p>
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:formulatedDate")
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

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>surveyList</code>.</p>
	 */
	@DataList
	@AlfQname(qname = "survey:surveyList")
	@Override
	public List<SurveyList> getSurveyList() {
		return surveyList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>surveyList</code>.</p>
	 */
	@Override
	public void setSurveyList(List<SurveyList> surveyList) {
		this.surveyList = surveyList;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "pjt:projectScore")
	@Override
	public Integer getScore() {
		return supplierScore;
	}

	/** {@inheritDoc} */
	@Override
	public void setScore(Integer supplierScore) {
		this.supplierScore = supplierScore;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(costList, hierarchy1, hierarchy2, plantList, state);
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
		SupplierData other = (SupplierData) obj;
		return Objects.equals(costList, other.costList) && Objects.equals(lcaList, other.lcaList) && Objects.equals(hierarchy1, other.hierarchy1)
				&& Objects.equals(hierarchy2, other.hierarchy2) && Objects.equals(plantList, other.plantList) && state == other.state;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SupplierData [name=" + name + ", hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", state=" + state + "]";
	}

}
