package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ExportFormat;

/**
 * Update entity with POST xml 
 * @author matthieu
 *
 */
public class UpdateEntityWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
	
		NodeRef entityNodeRef = findEntity(req);
		
		logger.debug("Update entity: "+entityNodeRef);
		
		entityService.createOrUpdateEntity(null,req.getContent().getInputStream(), ExportFormat.xml);
		
		sendOKStatus(entityNodeRef,resp);
		
	}
}
