package fr.becpg.repo.web.scripts.regulatory;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.regulatory.RegulatoryService;
import fr.becpg.repo.regulatory.ComplianceResult;

/**
 * <p>RegulatoryWebScript class.</p>
 *
 * @author Valentin
 */
public class RegulatoryWebScript extends AbstractWebScript {

	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_ASYNC = "async";

	private RegulatoryService regulatoryService;
	
	/**
	 * <p>Setter for the field <code>regulatoryService</code>.</p>
	 *
	 * @param regulatoryService a {@link fr.becpg.repo.regulatory.RegulatoryService} object
	 */
	public void setRegulatoryService(RegulatoryService regulatoryService) {
		this.regulatoryService = regulatoryService;
	}
	
	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));
		boolean async = Boolean.parseBoolean(req.getParameter(PARAM_ASYNC));
		
		ComplianceResult result = regulatoryService.checkCompliance(nodeRef, async);
		res.setContentType("application/json");
		res.getWriter().write(JsonHelper.serialize(result));
	}

}
