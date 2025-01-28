/*
 *
 */
package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamDataItem;
import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;

/**
 * <p>ProductSpecificationData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:productSpecification")
@AlfCacheable
public class ProductSpecificationData extends ProductData {

	private static final long serialVersionUID = -3890483893356522048L;
	
	private String specCompatibilityLog;
	
	private String regulatoryCode;

	private List<ForbiddenIngListDataItem> forbiddenIngList;

	private List<LabelingRuleListDataItem> labelingRuleList;

	private List<ResourceParamDataItem> resourceParams;

	private List<SpecCompatibilityDataItem> specCompatibilityList;
	
	private List<DynamicCharactListItem> dynamicCharactList;
	

	private List<NodeRef> specCompatibilityTpls = new ArrayList<>();
	
	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public static ProductSpecificationData build() {
		return new ProductSpecificationData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public ProductSpecificationData withName(String name) {
		setName(name);
		return this;
	}

	

	/**
	 * <p>withNutList.</p>
	 *
	 * @param nutList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.ProductSpecificationData} object
	 */
	public ProductSpecificationData withNutList(List<NutListDataItem> nutList) {
		setNutList(nutList);
		return this;
	}
	
	/**
	 * <p>withHcList.</p>
	 *
	 * @param hList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.ProductSpecificationData} object
	 */
	public ProductSpecificationData withHcList(List<HazardClassificationListDataItem> hList) {
		setHcList(hList);
		return this;
	}
	
	/**
	 * <p>withForbiddenIngList.</p>
	 *
	 * @param forbiddenIngList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.ProductSpecificationData} object
	 */
	public ProductSpecificationData withForbiddenIngList(List<ForbiddenIngListDataItem> forbiddenIngList) {
		setForbiddenIngList(forbiddenIngList);
		return this;
	}

	
	/**
	 * <p>Getter for the field <code>regulatoryCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:regulatoryCode")
	public String getRegulatoryCode() {
		return regulatoryCode;
	}

	/**
	 * <p>Setter for the field <code>regulatoryCode</code>.</p>
	 *
	 * @param regulatoryCode a {@link java.lang.String} object.
	 */
	public void setRegulatoryCode(String regulatoryCode) {
		this.regulatoryCode = regulatoryCode;
	}
	
	/**
	 * <p>Getter for the field <code>specCompatibilityLog</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:specCompatibilityLog")
	public String getSpecCompatibilityLog() {
		return specCompatibilityLog;
	}

	/**
	 * <p>Setter for the field <code>specCompatibilityLog</code>.</p>
	 *
	 * @param specCompatibilityLog a {@link java.lang.String} object.
	 */
	public void setSpecCompatibilityLog(String specCompatibilityLog) {
		this.specCompatibilityLog = specCompatibilityLog;
	}

	/**
	 * <p>Getter for the field <code>specCompatibilityTpls</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:specCompatibilityTpls")
	@AlfReadOnly
	public List<NodeRef> getSpecCompatibilityTpls() {
		return specCompatibilityTpls;
	}

	/**
	 * <p>Setter for the field <code>specCompatibilityTpls</code>.</p>
	 *
	 * @param specCompatibilityTpls a {@link java.util.List} object.
	 */
	public void setSpecCompatibilityTpls(List<NodeRef> specCompatibilityTpls) {
		this.specCompatibilityTpls = specCompatibilityTpls;
	}

	/**
	 * <p>Getter for the field <code>specCompatibilityList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:productSpecCompatibilityList")
	public List<SpecCompatibilityDataItem> getSpecCompatibilityList() {
		return specCompatibilityList;
	}

	/**
	 * <p>Setter for the field <code>specCompatibilityList</code>.</p>
	 *
	 * @param specCompatibilityList a {@link java.util.List} object.
	 */
	public void setSpecCompatibilityList(List<SpecCompatibilityDataItem> specCompatibilityList) {
		this.specCompatibilityList = specCompatibilityList;
	}

	/**
	 * <p>Getter for the field <code>labelingRuleList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:labelingRuleList")
	public List<LabelingRuleListDataItem> getLabelingRuleList() {
		return labelingRuleList;
	}

	/**
	 * <p>Setter for the field <code>labelingRuleList</code>.</p>
	 *
	 * @param labelingRuleList a {@link java.util.List} object.
	 */
	public void setLabelingRuleList(List<LabelingRuleListDataItem> labelingRuleList) {
		this.labelingRuleList = labelingRuleList;
	}

	/**
	 * <p>Getter for the field <code>forbiddenIngList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:forbiddenIngList")
	public List<ForbiddenIngListDataItem> getForbiddenIngList() {
		return forbiddenIngList;
	}

	/**
	 * <p>Setter for the field <code>forbiddenIngList</code>.</p>
	 *
	 * @param forbiddenIngList a {@link java.util.List} object.
	 */
	public void setForbiddenIngList(List<ForbiddenIngListDataItem> forbiddenIngList) {
		this.forbiddenIngList = forbiddenIngList;
	}
	
	
	/**
	 * <p>Getter for the field <code>dynamicCharactList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:dynamicCharactList")
	public List<DynamicCharactListItem> getDynamicCharactList() {
		return dynamicCharactList;
	}

	/**
	 * <p>Setter for the field <code>dynamicCharactList</code>.</p>
	 *
	 * @param dynamicCharactList a {@link java.util.List} object.
	 */
	public void setDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList) {
		this.dynamicCharactList = dynamicCharactList;
	}

	/**
	 * <p>Getter for the field <code>resourceParams</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "mpm:resourceParam")
	public List<ResourceParamDataItem> getResourceParams() {
		return resourceParams;
	}

	/**
	 * <p>Setter for the field <code>resourceParams</code>.</p>
	 *
	 * @param resourceParams a {@link java.util.List} object.
	 */
	public void setResourceParams(List<ResourceParamDataItem> resourceParams) {
		this.resourceParams = resourceParams;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(dynamicCharactList, forbiddenIngList, labelingRuleList, regulatoryCode, resourceParams,
				specCompatibilityList, specCompatibilityLog, specCompatibilityTpls);
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
		ProductSpecificationData other = (ProductSpecificationData) obj;
		return Objects.equals(dynamicCharactList, other.dynamicCharactList) && Objects.equals(forbiddenIngList, other.forbiddenIngList)
				&& Objects.equals(labelingRuleList, other.labelingRuleList) && Objects.equals(regulatoryCode, other.regulatoryCode)
				&& Objects.equals(resourceParams, other.resourceParams) && Objects.equals(specCompatibilityList, other.specCompatibilityList)
				&& Objects.equals(specCompatibilityLog, other.specCompatibilityLog)
				&& Objects.equals(specCompatibilityTpls, other.specCompatibilityTpls);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ProductSpecificationData [specCompatibilityLog=" + specCompatibilityLog + ", regulatoryCode=" + regulatoryCode + ", forbiddenIngList="
				+ forbiddenIngList + ", labelingRuleList=" + labelingRuleList + ", resourceParams=" + resourceParams + ", specCompatibilityList="
				+ specCompatibilityList + ", dynamicCharactList=" + dynamicCharactList + ", specCompatibilityTpls=" + specCompatibilityTpls + "]";
	}

}
