package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.formulation.FormulateException;

public interface CharactDetailsVisitor {

	CharactDetails visit(ProductData productData, List<NodeRef> elements) throws FormulateException;

	void setDataListType(QName dataType);

}
