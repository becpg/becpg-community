package fr.becpg.repo.product;

import org.alfresco.service.namespace.QName;

import fr.becpg.repo.product.formulation.FormulateException;

public interface CharactDetailsVisitorFactory {

	CharactDetailsVisitor getCharactDetailsVisitor(QName dataType, String dataListName) throws FormulateException;

}
