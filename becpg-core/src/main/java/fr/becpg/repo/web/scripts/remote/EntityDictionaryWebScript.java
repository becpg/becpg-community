/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.file.AccessDeniedException;

import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.RemoteParams;

/**
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityDictionaryWebScript extends AbstractEntityWebScript {

	

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		String type = req.getParameter(PARAM_TYPE);
		
		if (type == null || type.isEmpty()) {
			throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "Type parameter is mandatory");
		}

		logger.debug("Get dictionary schema:  for type" + type);

		try (OutputStream out = resp.getOutputStream()) {

			RemoteParams params = new RemoteParams(getFormat(req));
			params.setFilteredFields(extractFields(req), namespaceService);
			params.setFilteredLists(extractLists(req));
			params.setJsonParams(extractParams(req));
			resp.setContentType(getContentType(req));
			resp.setContentEncoding("UTF-8");

			remoteEntityService.getEntitySchema(QName.createQName(type, namespaceService), out, params);

			resp.setStatus(Status.STATUS_OK);
		} catch (BeCPGException e) {
			logger.error("Cannot export entity schema", e);
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
