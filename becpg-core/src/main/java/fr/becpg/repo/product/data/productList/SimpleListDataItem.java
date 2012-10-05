package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

public interface SimpleListDataItem extends IManualDataItem {

	public NodeRef getCharactNodeRef();

	public void setCharactNodeRef(NodeRef nodeRef);

	public Double getValue();

	public void setValue(Double value);

	public Double getMini();

	public void setMini(Double value);

	public Double getMaxi();

	public void setMaxi(Double value);	
}
