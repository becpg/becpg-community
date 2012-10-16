package fr.becpg.repo.entity;

import org.alfresco.service.namespace.QName;

public interface EntityDictionaryService {

	public QName getWUsedList(QName entityType);
	
	public QName getDefaultPivotAssoc(QName dataListItemType);
}
