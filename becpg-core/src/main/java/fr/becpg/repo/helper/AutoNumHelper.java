package fr.becpg.repo.helper;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.AutoNumService;

/**
 * <p>AutoNumHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class AutoNumHelper implements InitializingBean {

	@Autowired
	@Qualifier("autoNumService")
	private AutoNumService autoNumService;

	@Autowired
	private NamespaceService namespaceService;

	private static AutoNumHelper instance = null;

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	/**
	 * SPEL HELPER
	 * T(fr.becpg.repo.helper.AutoNumHelper).getAutoNumValue("bcpg:finishedProduct","bcpg:eanCode")
	 * T(fr.becpg.repo.helper.AutoNumHelper).getOrCreateCode(nodeRef,"bcpg:eanCode")
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getAutoNumValue(String className, String propertyName) {
		return instance.autoNumService.getAutoNumValue(QName.createQName(className, instance.namespaceService),
				QName.createQName(propertyName, instance.namespaceService));
	}

	/**
	 * <p>getOrCreateCode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getOrCreateCode(NodeRef nodeRef, String propertyName) {
		return instance.autoNumService.getOrCreateCode(nodeRef, QName.createQName(propertyName, instance.namespaceService));
	}

}
