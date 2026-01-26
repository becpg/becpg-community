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
public class GetEntityWebScript extends AbstractEntityWebScript {

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		NodeRef entityNodeRef = findEntity(req);

		try {
			if(logger.isDebugEnabled()) {
				logger.debug("Get entity: " + entityNodeRef);
				logger.debug(" - with fields: " +  extractFields(req));
				logger.debug(" - with datalists: " + extractLists(req));
			}

			RemoteParams params = new RemoteParams(getFormat(req));
			params.setFilteredFields(extractFields(req), namespaceService);
			params.setFilteredLists(extractLists(req));
			params.setJsonParams(extractParams(req));
			
			resp.setContentType(getContentType(req));
			resp.setContentEncoding("UTF-8");
		
			try (OutputStream out = resp.getOutputStream()) {
				remoteEntityService.getEntity(entityNodeRef, out, params);
				resp.setStatus(Status.STATUS_OK);
			}

		} catch (BeCPGException e) {
			if (isBrokenPipe(e)) {
				logger.info("Client aborted connection for entity: " + entityNodeRef);
			} else {
				logger.error("Cannot export entity " + entityNodeRef + " for user " + org.alfresco.repo.security.authentication.AuthenticationUtil.getFullyAuthenticatedUser(), e);
				
				try {
					resp.reset();
					throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
				} catch (IllegalStateException ex) {
					logger.warn("Cannot reset response for error, already committed: " + ex.getMessage());
				}
			}
		} catch (AccessDeniedException e) {
			try {
				resp.reset();
				throw new WebScriptException(Status.STATUS_FORBIDDEN, "You have no right to see this node");
			} catch (IllegalStateException ex) {
				logger.warn("Cannot reset response for access denied, already committed: " + ex.getMessage());
			}
		} catch (SocketException e1) {
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read for entity: " + entityNodeRef, e1);
			}
		} catch (IOException e) {
			if (isBrokenPipe(e)) {
				logger.info("Client aborted connection due to network issue for entity: " + entityNodeRef);
				return;
			}
			throw e;
		}

	}

	
}
