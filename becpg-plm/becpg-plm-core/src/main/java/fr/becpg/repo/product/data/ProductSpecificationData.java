/*
 *
 */
package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamDataItem;
import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;

@AlfType
@AlfQname(qname = "bcpg:productSpecification")
public class ProductSpecificationData extends ProductData {

	private static final long serialVersionUID = -3890483893356522048L;

	private List<ForbiddenIngListDataItem> forbiddenIngList;

	private List<LabelingRuleListDataItem> labelingRuleList;

	private List<ResourceParamDataItem> resourceParams;

	private List<SpecCompatibilityDataItem> specCompatibilityList;

	private List<NodeRef> specCompatibilityTpls = new ArrayList<>();

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
		result = (prime * result) + ((forbiddenIngList == null) ? 0 : forbiddenIngList.hashCode());
		result = (prime * result) + ((labelingRuleList == null) ? 0 : labelingRuleList.hashCode());
		result = (prime * result) + ((resourceParams == null) ? 0 : resourceParams.hashCode());
		result = (prime * result) + ((specCompatibilityList == null) ? 0 : specCompatibilityList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProductSpecificationData other = (ProductSpecificationData) obj;
		if (forbiddenIngList == null) {
			if (other.forbiddenIngList != null) {
				return false;
			}
		} else if (!forbiddenIngList.equals(other.forbiddenIngList)) {
			return false;
		}
		if (labelingRuleList == null) {
			if (other.labelingRuleList != null) {
				return false;
			}
		} else if (!labelingRuleList.equals(other.labelingRuleList)) {
			return false;
		}
		if (resourceParams == null) {
			if (other.resourceParams != null) {
				return false;
			}
		} else if (!resourceParams.equals(other.resourceParams)) {
			return false;
		}
		if (specCompatibilityList == null) {
			if (other.specCompatibilityList != null) {
				return false;
			}
		} else if (!specCompatibilityList.equals(other.specCompatibilityList)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ProductSpecificationData [forbiddenIngList=" + forbiddenIngList + ", labelingRuleList=" + labelingRuleList + "]";
	}

}
