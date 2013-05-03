package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.ContentHelper;

public abstract class AbstractBeCPGPatch extends AbstractPatch {

	private static Log logger = LogFactory.getLog(EmailTemplatesPatch.class);

	protected Repository repository;

	protected ContentHelper contentHelper;

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setContentHelper(ContentHelper contentHelper) {
		this.contentHelper = contentHelper;
	}

	protected NodeRef searchFolder(String xpath) {
		List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(), xpath, null, namespaceService,
				false);
		if (nodeRefs.size() > 1) {
			throw new PatchException("XPath returned too many results: \n" + "   xpath: " + xpath + "\n"
					+ "   results: " + nodeRefs);
		} else if (nodeRefs.size() == 0) {
			// the node does not exist
			return null;
		} else {
			return nodeRefs.get(0);
		}
	}

	protected void updateResource(String xPath, String resourcePath) {
		NodeRef nodeRef = searchFolder(xPath);
		if (nodeRef != null) {
			logger.info("Update resource xPath: " + xPath + " with resourcePath: " + resourcePath);
			contentHelper.addFilesResources(nodeRef, resourcePath, true);
		}
	}

}
