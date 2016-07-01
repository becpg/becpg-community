/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
			resp.setContentType(getContentType(req));
			resp.setContentEncoding("UTF-8");
			try (CountingOutputStream out = new CountingOutputStream(resp.getOutputStream())) {
				resp.setHeader("Content-Length", Long.toString(out.getByteCount()));
			}

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
