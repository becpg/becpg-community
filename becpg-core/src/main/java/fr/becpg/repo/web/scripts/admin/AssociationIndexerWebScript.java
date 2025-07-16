/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.admin.AssociationIndexerService;

/**
 * <p>AssociationIndexerWebScript class.</p>
 *
 * @author matthieu
 */
public class AssociationIndexerWebScript extends AbstractWebScript {

	private AssociationIndexerService associationIndexerService;

	private NamespaceService namespaceService;

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>associationIndexerService</code>.</p>
	 *
	 * @param associationIndexerService a {@link fr.becpg.repo.admin.AssociationIndexerService} object
	 */
	public void setAssociationIndexerService(AssociationIndexerService associationIndexerService) {
		this.associationIndexerService = associationIndexerService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		try {
			QName sourceName = QName.createQName(req.getParameter("sourceName"), namespaceService);
			QName assocName = QName.createQName(req.getParameter("assocName"), namespaceService);
			associationIndexerService.reindexAssocs(sourceName, assocName);
			res.setStatus(200);
			res.setContentType("application/json");
			res.getWriter().write("{\"status\": \"success\", \"message\": \"Reindexing ongoing.\"}");
		} catch (Exception e) {
			res.setStatus(500);
			res.setContentType("application/json");
			res.getWriter().write("{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
		}
	}

}
