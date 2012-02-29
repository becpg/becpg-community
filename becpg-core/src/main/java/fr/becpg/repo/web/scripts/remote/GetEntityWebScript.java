package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ExportFormat;

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
		
		entityService.exportEntity(entityNodeRef,resp.getOutputStream(), ExportFormat.xml);
		
		
	}

}
