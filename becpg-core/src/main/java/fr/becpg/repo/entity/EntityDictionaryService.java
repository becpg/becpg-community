package fr.becpg.repo.entity;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.namespace.QName;

public interface EntityDictionaryService {

	QName getWUsedList(QName entityType);
	
	QName getDefaultPivotAssoc(QName dataListItemType);

	List<AssociationDefinition> getPivotAssocDefs(QName sourceType);

	QName getTargetType(QName createQName);
	
	Collection<QName> getSubTypes(QName typeQname);

	ClassAttributeDefinition getPropDef(QName fieldQname);

	boolean isSubClass(QName fieldQname, QName typeEntitylistItem);
}
