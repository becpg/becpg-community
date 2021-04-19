package fr.becpg.repo.web.scripts.document;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.version.EntityVersionService;

public class CompareDocumentWebScript extends AbstractWebScript {

	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";
	private static final String PARAM_VERSION_LABEL = "versionLabel";
	private static final String PARAM_ENTITIES = "entities";


	private static final Log logger = LogFactory.getLog(CompareDocumentWebScript.class);

	@Autowired
	private VersionService versionService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private CompareDocumentService compareDocumentService;
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef actualNode = null;

		String versionLabel = null;

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		// retrieve the actual node
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if (storeType != null && storeId != null && nodeId != null) {
				actualNode = new NodeRef(storeType, storeId, nodeId);
			}
			versionLabel = templateArgs.get(PARAM_VERSION_LABEL);
		}

		NodeRef versionNode = null;

		// retrieve the version node
		if (versionLabel != null) {

			VersionHistory versionHistory = versionService.getVersionHistory(actualNode);

			if (versionHistory != null) {
				Version version = versionHistory.getVersion(versionLabel);
				versionNode = entityVersionService.getEntityVersion(version);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("actualNode: " + actualNode + " - versionLabel: " + versionLabel + " - versionNode: " + versionNode);
			}
		} else {
			String entities = req.getParameter(PARAM_ENTITIES);

			versionNode = new NodeRef(entities);
		}

		if (versionNode == null) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "missing parameters. versionNode");
		}
		
		compareDocumentService.compare(actualNode, versionNode, req, res);

	}

}
