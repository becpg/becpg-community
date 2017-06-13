package fr.becpg.repo.activity;

import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

@Service
public class DefaultEntityActivityPlugin implements EntityActivityPlugin {

	@Override
	public boolean isMatchingStateProperty(QName propName) {
		return false;
	}

	@Override
	public boolean isMatchingEntityType(QName entityName) {
		return false;
	}

	

}
