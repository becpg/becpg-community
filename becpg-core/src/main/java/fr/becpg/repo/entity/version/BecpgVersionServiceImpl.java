package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.version.Version2ServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

public class BecpgVersionServiceImpl extends Version2ServiceImpl {

	private static Log logger = LogFactory.getLog(BecpgVersionServiceImpl.class);

	@Override
	protected void defaultOnCreateVersion(QName classRef, NodeRef nodeRef, Map<String, Serializable> versionProperties, PolicyScope nodeDetails) {
		if (logger.isDebugEnabled()) {
			logger.debug("defaultOnCreateVersion " + nodeService.getType(nodeRef) + " " + classRef + " "
					+ this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
		}

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS)) {
			if ((classRef != null) && classRef.equals(nodeService.getType(nodeRef))) {
				logger.debug("Skipping node properties for becpg version");
				nodeDetails.addProperty(classRef, ContentModel.PROP_NAME, this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
				nodeDetails.addProperty(classRef, BeCPGModel.PROP_CODE, this.nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE));
			}

		} else {

			super.defaultOnCreateVersion(classRef, nodeRef, versionProperties, nodeDetails);
		}
	}
}
