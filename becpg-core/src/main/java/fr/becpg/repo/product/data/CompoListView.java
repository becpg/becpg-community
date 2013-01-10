package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;

public class CompoListView extends AbstractProductDataView {

	private List<CompoListDataItem> compoList;
	
	@DataList
	@AlfQname(qname="bcpg:compoList")
	public List<CompoListDataItem> getCompoList() {
		return compoList;
	}


	public void setCompoList(List<CompoListDataItem> compoList) {
		this.compoList = compoList;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compoList == null) ? 0 : compoList.hashCode());
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
		CompoListView other = (CompoListView) obj;
		if (compoList == null) {
			if (other.compoList != null)
				return false;
		} else if (!compoList.equals(other.compoList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CompoListView [compoList=" + compoList + "]";
	}
	


	
}
