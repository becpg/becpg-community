package fr.becpg.repo.product;

import java.util.Map;

import org.alfresco.service.namespace.QName;

import fr.becpg.repo.product.formulation.FormulateException;

public class CharactDetailsVisitorFactoryImpl implements CharactDetailsVisitorFactory{
	
	
	Map<String,CharactDetailsVisitor> visitorRegistry;
	
	public void setVisitorRegistry(Map<String, CharactDetailsVisitor> visitorRegistry) {
		this.visitorRegistry = visitorRegistry;
	}

	@Override
	public CharactDetailsVisitor getCharactDetailsVisitor(QName dataType, String dataListName) throws FormulateException {
		
		CharactDetailsVisitor visitor = visitorRegistry.get(dataListName);
		if(visitor!=null){
			visitor.setDataListType(dataType);
			return visitor;
		}
		
		throw new FormulateException("No visitor found");
	}

}
