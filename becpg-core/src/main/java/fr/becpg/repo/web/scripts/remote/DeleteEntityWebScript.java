package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Delete corresponding entity
 * @author matthieu
 *
 */
public class DeleteEntityWebScript extends AbstractEntityWebScript {
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		
		NodeRef entityNodeRef = findEntity(req);
		
		logger.debug("Deleting entity: "+entityNodeRef);
		nodeService.deleteNode(entityNodeRef);
		
		sendOKStatus(entityNodeRef,resp);
		
	}

	
}
