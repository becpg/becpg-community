package fr.becpg.repo.product.data.productList;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;

@AlfType
@AlfQname(qname = "mpm:resourceParam")
public class ResourceParamDataItem extends AbstractManualDataItem {
	
	private String title;
	private String description;
	
	@AlfProp
	@AlfQname(qname="cm:title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@AlfProp
	@AlfQname(qname="cm:description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public ResourceParamDataItem() {
		super();
	}
	
	public ResourceParamDataItem(String name, String title, String description) {
		super();
		this.name = name;
		this.title = title;
		this.description = description;
	}
		
	@Override
	public String toString() {
		return "ResourceParamDataItem [title=" + title + ", description="
				+ description + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		ResourceParamDataItem other = (ResourceParamDataItem) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
	
	
	
}
