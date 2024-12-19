package fr.becpg.repo.entity.version;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>VersionHelper class.</p>
 *
 * @author matthieu
 */
public class VersionHelper {

	private VersionHelper() {
		
	}
	
	/**
	 * <p>isVersion.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public static boolean isVersion(NodeRef nodeRef) {
		return nodeRef.getStoreRef().getProtocol().contains(VersionBaseModel.STORE_PROTOCOL)
				|| nodeRef.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID);
	}
	
}
