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
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamDataItem;
import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;

@AlfType
@AlfQname(qname = "bcpg:productSpecification")
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
	
	
	@AlfProp
	@AlfQname(qname="bcpg:regulatoryCode")
	public String getRegulatoryCode() {
		return regulatoryCode;
	}

	public void setRegulatoryCode(String regulatoryCode) {
		this.regulatoryCode = regulatoryCode;
	}
	
	@AlfProp
	@AlfQname(qname = "bcpg:specCompatibilityLog")
	public String getSpecCompatibilityLog() {
		return specCompatibilityLog;
	}

	public void setSpecCompatibilityLog(String specCompatibilityLog) {
		this.specCompatibilityLog = specCompatibilityLog;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:specCompatibilityTpls")
	@AlfReadOnly
	public List<NodeRef> getSpecCompatibilityTpls() {
		return specCompatibilityTpls;
	}

	public void setSpecCompatibilityTpls(List<NodeRef> specCompatibilityTpls) {
		this.specCompatibilityTpls = specCompatibilityTpls;
	}

	@DataList
	@AlfQname(qname = "bcpg:productSpecCompatibilityList")
	public List<SpecCompatibilityDataItem> getSpecCompatibilityList() {
		return specCompatibilityList;
	}

	public void setSpecCompatibilityList(List<SpecCompatibilityDataItem> specCompatibilityList) {
		this.specCompatibilityList = specCompatibilityList;
	}

	@DataList
	@AlfQname(qname = "bcpg:labelingRuleList")
	public List<LabelingRuleListDataItem> getLabelingRuleList() {
		return labelingRuleList;
	}

	public void setLabelingRuleList(List<LabelingRuleListDataItem> labelingRuleList) {
		this.labelingRuleList = labelingRuleList;
	}

	@DataList
	@AlfQname(qname = "bcpg:forbiddenIngList")
	public List<ForbiddenIngListDataItem> getForbiddenIngList() {
		return forbiddenIngList;
	}

	public void setForbiddenIngList(List<ForbiddenIngListDataItem> forbiddenIngList) {
		this.forbiddenIngList = forbiddenIngList;
	}
	
	
	@DataList
	@AlfQname(qname = "bcpg:dynamicCharactList")
	public List<DynamicCharactListItem> getDynamicCharactList() {
		return dynamicCharactList;
	}

	public void setDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList) {
		this.dynamicCharactList = dynamicCharactList;
	}

	@DataList
	@AlfQname(qname = "mpm:resourceParam")
	public List<ResourceParamDataItem> getResourceParams() {
		return resourceParams;
	}

	public void setResourceParams(List<ResourceParamDataItem> resourceParams) {
		this.resourceParams = resourceParams;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(dynamicCharactList, forbiddenIngList, labelingRuleList, regulatoryCode, resourceParams,
				specCompatibilityList, specCompatibilityLog, specCompatibilityTpls);
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
		ProductSpecificationData other = (ProductSpecificationData) obj;
		return Objects.equals(dynamicCharactList, other.dynamicCharactList) && Objects.equals(forbiddenIngList, other.forbiddenIngList)
				&& Objects.equals(labelingRuleList, other.labelingRuleList) && Objects.equals(regulatoryCode, other.regulatoryCode)
				&& Objects.equals(resourceParams, other.resourceParams) && Objects.equals(specCompatibilityList, other.specCompatibilityList)
				&& Objects.equals(specCompatibilityLog, other.specCompatibilityLog)
				&& Objects.equals(specCompatibilityTpls, other.specCompatibilityTpls);
	}

	@Override
	public String toString() {
		return "ProductSpecificationData [specCompatibilityLog=" + specCompatibilityLog + ", regulatoryCode=" + regulatoryCode + ", forbiddenIngList="
				+ forbiddenIngList + ", labelingRuleList=" + labelingRuleList + ", resourceParams=" + resourceParams + ", specCompatibilityList="
				+ specCompatibilityList + ", dynamicCharactList=" + dynamicCharactList + ", specCompatibilityTpls=" + specCompatibilityTpls + "]";
	}

}
