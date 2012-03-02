package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;

/**
 * Update entity with POST xml
 * 
 * @author matthieu
 * 
 */
public class UpdateEntityWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		NodeRef entityNodeRef = findEntity(req);

		logger.debug("Update entity: " + entityNodeRef);
		try {
			
			entityService.createOrUpdateEntity(entityNodeRef,req.getContent().getInputStream(), getFormat(req), getEntityProviderCallback(req));

			sendOKStatus(entityNodeRef, resp);
		} catch (BeCPGException e) {
			logger.error("Cannot import entity", e);
			throw new WebScriptException(e.getMessage());
		}

	}
}
