package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ExportFormat;

/**
 * Create entity with POST xml 
 * @author matthieu
 *
 */
public class CreateEntityWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		
		logger.debug("Create entity");
		
		NodeRef entityNodeRef = 
				entityService.createOrUpdateEntity(null,req.getContent().getInputStream(), ExportFormat.xml);
		
		sendOKStatus(entityNodeRef,resp);
	}
	
}
