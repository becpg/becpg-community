package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import fr.becpg.common.BeCPGException;

/**
 * Create entity with POST xml
 * 
 * @author matthieu
 * 
 */
public class CreateEntityWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		if (!(req instanceof WebScriptServletRequest))
        {
            throw new WebScriptException("Content retrieval must be executed in HTTP Servlet environment");
        }
        HttpServletRequest httpReq = ((WebScriptServletRequest)req).getHttpServletRequest();
		
		
		logger.debug("Create entity");
		try {
			
			NodeRef entityNodeRef = entityService.createOrUpdateEntity(null, httpReq.getInputStream(), getFormat(req),getEntityProviderCallback(req));
			sendOKStatus(entityNodeRef, resp);
		} catch (BeCPGException e) {
			logger.error("Cannot import entity", e);
			throw new WebScriptException(e.getMessage());
		}
	
	}

}
