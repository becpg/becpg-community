package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BaseObject;

public class LabelingListView  extends BaseObject {


	private List<IngLabelingListDataItem> ingLabelingList;
	private List<LabelingRuleListDataItem> labelingRuleList;
	
	
	@DataList
	@AlfQname(qname = "bcpg:ingLabelingList")
	public List<IngLabelingListDataItem> getIngLabelingList() {
		return ingLabelingList;
	}

	public void setIngLabelingList(List<IngLabelingListDataItem> ingLabelingList) {
		this.ingLabelingList = ingLabelingList;
	}
	

	@DataList
	@AlfQname(qname = "bcpg:labelingRuleList")
	public List<LabelingRuleListDataItem> getLabelingRuleList() {
		return labelingRuleList;
	}

	public void setLabelingRuleList(List<LabelingRuleListDataItem> labelingRuleList) {
		this.labelingRuleList = labelingRuleList;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ingLabelingList == null) ? 0 : ingLabelingList.hashCode());
		result = prime * result + ((labelingRuleList == null) ? 0 : labelingRuleList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabelingListView other = (LabelingListView) obj;
		if (ingLabelingList == null) {
			if (other.ingLabelingList != null)
				return false;
		} else if (!ingLabelingList.equals(other.ingLabelingList))
			return false;
		if (labelingRuleList == null) {
			if (other.labelingRuleList != null)
				return false;
		} else if (!labelingRuleList.equals(other.labelingRuleList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LabelingListView [ingLabelingList=" + ingLabelingList + ", labelingRuleList=" + labelingRuleList + "]";
	}
	
	
}
