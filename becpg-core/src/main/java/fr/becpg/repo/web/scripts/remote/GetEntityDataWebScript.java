package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.net.SocketException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.output.CountingOutputStream;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;

/**
 * Get entity as XML
 * 
 * @author matthieu
 * 
 */
public class GetEntityDataWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		NodeRef entityNodeRef = findEntity(req);

		logger.debug("Get entity data: " + entityNodeRef);

		try {
				
			remoteEntityService.getEntityData(entityNodeRef, resp.getOutputStream(), getFormat(req));

			// set mimetype for the content and the character encoding + length
			// for the stream
			resp.setContentType(mimetypeService.guessMimetype(getFormat(req).toString()));
			CountingOutputStream c = new CountingOutputStream(resp.getOutputStream());
			resp.setHeader("Content-Length", Long.toString(c.getByteCount()));

		} catch (BeCPGException e) {
			logger.error("Cannot export entity data", e);
			throw new WebScriptException(e.getMessage());
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled())
				logger.info("Client aborted stream read:\n\tcontent", e1);

		} 

	}

}
