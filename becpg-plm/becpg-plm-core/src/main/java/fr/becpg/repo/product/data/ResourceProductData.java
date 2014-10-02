/*
 * 
 */
package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;

@AlfType
@AlfQname(qname = "bcpg:resourceProduct")
public class ResourceProductData extends ProductData  {

    private List<ResourceParamListItem> resourceParamList;

    
    @DataList
	@AlfQname(qname="mpm:resourceParamList")
	public List<ResourceParamListItem> getResourceParamList() {
		return resourceParamList;
	}

	public void setResourceParamList(List<ResourceParamListItem> resourceParamList) {
		this.resourceParamList = resourceParamList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((resourceParamList == null) ? 0 : resourceParamList.hashCode());
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
		ResourceProductData other = (ResourceProductData) obj;
		if (resourceParamList == null) {
			if (other.resourceParamList != null)
				return false;
		} else if (!resourceParamList.equals(other.resourceParamList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResourceProductData [resourceParamList=" + resourceParamList + "]";
	}
	
	
}
