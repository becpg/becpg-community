package fr.becpg.repo.activity;

import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

/**
 * <p>DefaultEntityActivityPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DefaultEntityActivityPlugin implements EntityActivityPlugin {

	/** {@inheritDoc} */
	@Override
	public boolean isMatchingStateProperty(QName propName) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMatchingEntityType(QName entityName) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isIgnoreStateProperty(QName propName) {
		return false;
	}

	

}
