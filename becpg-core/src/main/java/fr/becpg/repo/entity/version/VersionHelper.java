package fr.becpg.repo.entity.version;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.NodeRef;

public class VersionHelper {

	private VersionHelper() {
		
	}
	
	public static boolean isVersion(NodeRef nodeRef) {
		return nodeRef.getStoreRef().getProtocol().contains(VersionBaseModel.STORE_PROTOCOL)
				|| nodeRef.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID);
	}
	
}
