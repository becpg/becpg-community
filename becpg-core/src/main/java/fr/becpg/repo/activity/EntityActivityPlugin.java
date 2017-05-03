package fr.becpg.repo.activity;

import org.alfresco.service.namespace.QName;

public interface EntityActivityPlugin {

	boolean isMatchingStateProperty(QName propName);

}
