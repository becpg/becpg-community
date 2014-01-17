package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;

public interface CompositionDataItem extends RepositoryEntity  {

	NodeRef getProduct();

	void setProduct(NodeRef targetItem);

	Double getQty();

	void setQty(Double d);

	//TODO try to remove
	@Deprecated
	CompositionDataItem createCopy();


}
