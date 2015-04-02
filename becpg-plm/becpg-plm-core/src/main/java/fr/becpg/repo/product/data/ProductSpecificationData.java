/*
 * 
 */
package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;

@AlfType
@AlfQname(qname = "bcpg:productSpecification")
public class ProductSpecificationData extends ProductData   {

	private List<ForbiddenIngListDataItem> forbiddenIngList;
	
	private List<LabelingRuleListDataItem> labelingRuleList;
	
	private List<ResourceParamDataItem> resourceParamList;
	
	@DataList
	@AlfQname(qname = "bcpg:labelingRuleList")
	public List<LabelingRuleListDataItem> getLabelingRuleList() {
		return labelingRuleList;
	}

	@Override
	public String toString() {
		return "ProductSpecificationData [forbiddenIngList=" + forbiddenIngList + ", labelingRuleList=" + labelingRuleList + "]";
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
	@AlfQname(qname = "mpm:resourceParam")
	public List<ResourceParamDataItem> getResourceParamList() {
		return resourceParamList;
	}

	public void setResourceParamList(List<ResourceParamDataItem> resourceParamList) {
		this.resourceParamList = resourceParamList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((forbiddenIngList == null) ? 0 : forbiddenIngList.hashCode());
		result = prime * result + ((labelingRuleList == null) ? 0 : labelingRuleList.hashCode());
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
		if (forbiddenIngList == null) {
			if (other.forbiddenIngList != null)
				return false;
		} else if (!forbiddenIngList.equals(other.forbiddenIngList))
			return false;
		if (labelingRuleList == null) {
			if (other.labelingRuleList != null)
				return false;
		} else if (!labelingRuleList.equals(other.labelingRuleList))
			return false;
		return true;
	}
	
	
	
}
