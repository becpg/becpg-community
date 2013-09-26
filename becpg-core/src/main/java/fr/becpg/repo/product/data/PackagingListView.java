package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;

public class PackagingListView extends AbstractProductDataView {

	private List<PackagingListDataItem> packagingList;
	
	
	@DataList
	@AlfQname(qname="bcpg:packagingList")
	public List<PackagingListDataItem> getPackagingList() {
		return packagingList;
	}
	
	@Override
	public List<? extends CompositionDataItem> getMainDataList() {
		return getPackagingList();
	}


	public void setPackagingList(List<PackagingListDataItem> packagingList) {
		this.packagingList = packagingList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packagingList == null) ? 0 : packagingList.hashCode());
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
		PackagingListView other = (PackagingListView) obj;
		if (packagingList == null) {
			if (other.packagingList != null)
				return false;
		} else if (!packagingList.equals(other.packagingList))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "PackagingListView [packagingList=" + packagingList + "]";
	}
	
	
	
}
