package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.EntityIconService;

public class EntityIconWebScript extends AbstractWebScript{
	
	private static Log logger = LogFactory.getLog(EntityIconWebScript.class);
	
	private EntityIconService entityIconService;
	
	public void setEntityIconService(EntityIconService entityIconService) {
		this.entityIconService = entityIconService;
	}
	
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		try (OutputStream out = resp.getOutputStream()){
			resp.setContentType("text/css;charset=UTF-8");
			resp.setContentEncoding("UTF-8");
			
			entityIconService.writeIconCSS(out);
			
			resp.setStatus(Status.STATUS_OK);
		} catch (BeCPGException e) {
			logger.error("Cannot generate CSS Entity Icons ", e);
			throw new WebScriptException(e.getMessage());
		} catch (SocketException e1) {
			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}
	
		} 
		
	}

}