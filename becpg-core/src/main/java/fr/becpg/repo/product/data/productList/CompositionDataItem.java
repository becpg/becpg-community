package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;

public interface CompositionDataItem extends RepositoryEntity, Cloneable  {

	NodeRef getProduct();

	void setProduct(NodeRef targetItem);

	Double getQty();

	void setQty(Double d);

	CompositionDataItem createCopy();


}
