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

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.RemoteParams;

/**
 * Get entity as XML
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GetEntityDataWebScript extends AbstractEntityWebScript {

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		NodeRef entityNodeRef = findEntity(req);

		logger.debug("Get entity data: " + entityNodeRef);

		try (OutputStream out = resp.getOutputStream()) {

			RemoteParams params = new RemoteParams(getFormat(req));
			params.setFilteredFields(extractFields(req), namespaceService);
			resp.setContentType(getContentType(req));
			resp.setContentEncoding("UTF-8");

			remoteEntityService.getEntityData(entityNodeRef, out, params);

			resp.setStatus(Status.STATUS_OK);
		} catch (BeCPGException e) {
			logger.error("Cannot export entity data", e);
			throw new WebScriptException(e.getMessage());
		} catch (AccessDeniedException e) {
			throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}

		}
	}

}
