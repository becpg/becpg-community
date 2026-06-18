package fr.becpg.repo.web.scripts.regulatory;

import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.regulatory.ComplianceResult;
import fr.becpg.repo.regulatory.RegulatoryRouterService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

/**
 * <p>RegulatoryWebScript class.</p>
 *
 * @author Valentin
 */
public class RegulatoryWebScript extends AbstractWebScript {

	/** Constant <code>PARAM_NODEREF="nodeRef"</code> */
	private static final String PARAM_NODEREF = "nodeRef";
	/** Constant <code>PARAM_ASYNC="async"</code> */
	private static final String PARAM_ASYNC = "async";

	private RegulatoryRouterService regulatoryRouterService;
	
	/**
	 * <p>Setter for the field <code>regulatoryRouterService</code>.</p>
	 *
	 * @param regulatoryRouterService a {@link fr.becpg.repo.regulatory.RegulatoryRouterService} object
	 */
	public void setRegulatoryRouterService(RegulatoryRouterService regulatoryRouterService) {
		this.regulatoryRouterService = regulatoryRouterService;
	}
	
	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));
		boolean async = Boolean.parseBoolean(req.getParameter(PARAM_ASYNC));
		
		ComplianceResult result = regulatoryRouterService.checkCompliance(nodeRef, async);
		res.setContentType("application/json");
		res.getWriter().write(JsonHelper.serialize(result));
	}

}
