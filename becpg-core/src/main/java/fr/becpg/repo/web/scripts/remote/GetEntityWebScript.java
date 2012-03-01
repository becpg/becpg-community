package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;

/**
 * Get entity as XML
 * @author matthieu
 *
 */
public class GetEntityWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		
		NodeRef entityNodeRef = findEntity(req);
		
		logger.debug("Get entity: "+entityNodeRef);
		
		try {
			entityService.exportEntity(entityNodeRef,resp.getOutputStream(), getFormat(req));
		} catch (BeCPGException e) {
			logger.error("Cannot export entity",e);
			throw new WebScriptException(e.getMessage());
		}

	}

}
