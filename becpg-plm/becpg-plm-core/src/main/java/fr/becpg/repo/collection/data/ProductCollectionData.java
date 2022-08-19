package fr.becpg.repo.collection.data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.collection.data.list.CollectionPriceListDataItem;
import fr.becpg.repo.collection.data.list.ProductListDataItem;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.StateableEntity;

/**
 * <p>ProductCollectionData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:productCollection")
public class ProductCollectionData extends BeCPGDataObject implements HierarchicalEntity, StateableEntity, FormulatedEntity {

	private static final long serialVersionUID = -2554133542406623412L;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private SystemState state = SystemState.Simulation;
	private List<CollectionPriceListDataItem> priceList;
	private List<ProductListDataItem> productList;
	private ProductCollectionData entityTpl;

	/*
	 * Formulation
	 */
	private Date formulatedDate;
	private Integer reformulateCount;
	private Integer currentReformulateCount;
	private String formulationChainId;
	private Boolean updateFormulatedDate = true;
	private String requirementChecksum;

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.model.SystemState} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productCollectionState")
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
	@AlfQname(qname = "bcpg:productCollectionHierarchy1")
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
	@AlfQname(qname = "bcpg:productCollectionHierarchy2")
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

	@DataList
	@AlfQname(qname = "gs1:collectionPriceList")
	public List<CollectionPriceListDataItem> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<CollectionPriceListDataItem> priceList) {
		this.priceList = priceList;
	}

	@DataList
	@AlfQname(qname = "bcpg:productList")
	public List<ProductListDataItem> getProductList() {
		return productList;
	}

	public void setProductList(List<ProductListDataItem> productList) {
		this.productList = productList;
	}

	@AlfSingleAssoc(isEntity = true, isCacheable = true)
	@AlfQname(qname = "bcpg:entityTplRef")
	public ProductCollectionData getEntityTpl() {
		return entityTpl;
	}

	@Override
	public NodeRef getFormulatedEntityTpl() {
		return entityTpl != null ? entityTpl.getNodeRef() : null;
	}

	public void setEntityTpl(ProductCollectionData entityTpl) {
		this.entityTpl = entityTpl;
	}

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
	 * <p>Getter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
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
	 * <p>Getter for the field <code>reformulateCount</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
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
	 * <p>Getter for the field <code>currentReformulateCount</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
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
	 * <p>Getter for the field <code>formulationChainId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
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

	
	//Javascript helpers do not remove
	
	public CollectionPriceListDataItem createCollectionPriceListDataItem() {
		return new CollectionPriceListDataItem();
	}
	
	public ProductListDataItem createProductListDataItemDataItem() {
		return new ProductListDataItem();
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(currentReformulateCount, formulatedDate, formulationChainId, hierarchy1, hierarchy2, priceList,
				productList, reformulateCount, requirementChecksum, state, updateFormulatedDate);
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
		ProductCollectionData other = (ProductCollectionData) obj;
		return Objects.equals(currentReformulateCount, other.currentReformulateCount) && Objects.equals(formulatedDate, other.formulatedDate)
				&& Objects.equals(formulationChainId, other.formulationChainId) && Objects.equals(hierarchy1, other.hierarchy1)
				&& Objects.equals(hierarchy2, other.hierarchy2) && Objects.equals(priceList, other.priceList)
				&& Objects.equals(productList, other.productList) && Objects.equals(reformulateCount, other.reformulateCount)
				&& Objects.equals(requirementChecksum, other.requirementChecksum) && state == other.state
				&& Objects.equals(updateFormulatedDate, other.updateFormulatedDate);
	}

	@Override
	public String toString() {
		return "ProductCollectionData [hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", state=" + state + "]";
	}

}
