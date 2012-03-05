package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.output.CountingOutputStream;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;

public class ListEntitiesWebScript extends AbstractEntityWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		List<NodeRef> entities = findEntities(req);
		

		logger.debug("List entities");

		try {
				
			remoteEntityService.listEntities(entities, resp.getOutputStream(), getFormat(req));

			// set mimetype for the content and the character encoding + length
			// for the stream
			resp.setContentType(mimetypeService.guessMimetype(getFormat(req).toString()));
			CountingOutputStream c = new CountingOutputStream(resp.getOutputStream());
			resp.setHeader("Content-Length", Long.toString(c.getByteCount()));

		} catch (BeCPGException e) {
			logger.error("Cannot export entity", e);
			throw new WebScriptException(e.getMessage());
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled())
				logger.info("Client aborted stream read:\n\tcontent", e1);

		} 
	}


}
