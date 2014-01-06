
package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.LabelListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;

@AlfType
@AlfQname(qname = "bcpg:packagingMaterial")
public class PackagingMaterialData extends ProductData   {

	private List<LabelListDataItem> labelingList;

	
	@DataList
	@AlfQname(qname = "pack:labelingList")
	public List<LabelListDataItem> getLabelingList() {
		return labelingList;
	}

	public void setLabelingList(List<LabelListDataItem> labelingList) {
		this.labelingList = labelingList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((labelingList == null) ? 0 : labelingList.hashCode());
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
		PackagingMaterialData other = (PackagingMaterialData) obj;
		if (labelingList == null) {
			if (other.labelingList != null)
				return false;
		} else if (!labelingList.equals(other.labelingList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PackagingMaterialData [labelingList=" + labelingList + "]";
	}
	
	
	
}
