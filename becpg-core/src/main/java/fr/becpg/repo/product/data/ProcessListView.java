package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;

public class ProcessListView extends AbstractProductDataView {

	private List<ProcessListDataItem> processList;
	
	
	@DataList
	@AlfQname(qname="mpm:processList")
	public List<ProcessListDataItem> getProcessList() {
		return processList;
	}
	
	public void setProcessList(List<ProcessListDataItem> processList) {
		this.processList = processList;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processList == null) ? 0 : processList.hashCode());
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
		ProcessListView other = (ProcessListView) obj;
		if (processList == null) {
			if (other.processList != null)
				return false;
		} else if (!processList.equals(other.processList))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "ProcessListView [processList=" + processList + "]";
	}			
	

}
