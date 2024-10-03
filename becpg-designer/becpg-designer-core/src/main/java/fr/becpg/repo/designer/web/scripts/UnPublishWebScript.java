/*
 * 
 */
package fr.becpg.repo.designer.web.scripts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;

/**
 * The Class UnPublishWebScript.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UnPublishWebScript extends AbstractWebScript {

	private static final String PARAM_NODEREF = "nodeRef";

	/** The logger. */
	private static final Log logger = LogFactory.getLog(UnPublishWebScript.class);

	/** The node service. */
	private DesignerService designerService;

	private NodeService nodeService;

	/**
	 * <p>
	 * Setter for the field <code>designerService</code>.
	 * </p>
	 *
	 * @param designerService the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		logger.debug("UnpublishWebScript execute()");

		NodeRef parentNodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));

		designerService.unpublish(parentNodeRef);

		JSONObject jsonResponse = new JSONObject();

		if (nodeService.hasAspect(parentNodeRef, DesignerModel.ASPECT_MODEL)) {
			jsonResponse.put("type", "model");
		} else if (nodeService.hasAspect(parentNodeRef, DesignerModel.ASPECT_CONFIG)) {
			jsonResponse.put("type", "config");
		}

		try (InputStream in = new ByteArrayInputStream(jsonResponse.toString().getBytes())) {
			IOUtils.copy(in, res.getOutputStream());
		} catch (IOException e) {
			logger.error(e, e);
		}

	}

}
