
package fr.becpg.repo.web.scripts.product;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;

@Service
public class FormulateWebScript extends AbstractProductWebscript {

	protected static final String PARAM_FAST = "fast";

	private static Log logger = LogFactory.getLog(FormulateWebScript.class);

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {
		logger.debug("start formulate webscript");

		String fast = req.getParameter(PARAM_FAST);

		boolean isFast = false;
		if (fast != null && fast.equals("true")) {
			isFast = true;
		}

		NodeRef productNodeRef = getProductNodeRef(req);
		try {
			productService.formulate(productNodeRef, isFast);
		} catch (FormulateException e) {
			handleFormulationError(e);
		}
	}

}
