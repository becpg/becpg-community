/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.net.SocketException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
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

		CountingOutputStream out = null;
		try {
				
			remoteEntityService.getEntityData(entityNodeRef, resp.getOutputStream(), getFormat(req));

			// set mimetype for the content and the character encoding + length
			// for the stream
			resp.setContentType(getContentType(req));
			resp.setContentEncoding("UTF-8");
			out = new CountingOutputStream(resp.getOutputStream());
			resp.setHeader("Content-Length", Long.toString(out.getByteCount()));

		} catch (BeCPGException e) {
			logger.error("Cannot export entity data", e);
			throw new WebScriptException(e.getMessage());
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled())
				logger.info("Client aborted stream read:\n\tcontent", e1);

		} finally {
			IOUtils.closeQuietly(out);
		}

	}

	

}
