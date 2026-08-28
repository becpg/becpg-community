/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

	private static final String ACCESS_DENIED_MESSAGE = "You have no right to see this node";

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		NodeRef entityNodeRef = findEntity(req);

		/*
		 * Once the response output stream has been taken, the container can no longer
		 * render an error into it: renderErrorResponse asks for the writer and fails
		 * on "getOutputStream() has already been called", which replaces the real
		 * cause with a message about the response. Past that point the cause is
		 * logged here instead, and the request ends on the truncated body the caller
		 * already holds.
		 */
		boolean streaming = false;

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
		
			streaming = true;
			try (OutputStream out = resp.getOutputStream()) {
				remoteEntityService.getEntity(entityNodeRef, out, params);
				resp.setStatus(Status.STATUS_OK);
			}

		} catch (BeCPGException e) {
			if (isBrokenPipe(e)) {
				logger.info("Client aborted connection for entity: " + entityNodeRef);
			} else if (isAccessDenied(e)) {
				refuseExport(entityNodeRef, resp, streaming);
			} else {
				logger.error("Cannot export entity " + entityNodeRef + " for user " + AuthenticationUtil.getFullyAuthenticatedUser(), e);
				endOnError(resp, streaming, Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
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
			if (streaming) {
				logger.error("Cannot export entity " + entityNodeRef + ", the response is already committed", e);
				return;
			}
			throw e;
		} catch (RuntimeException e) {
			if (isAccessDenied(e)) {
				refuseExport(entityNodeRef, resp, streaming);
			} else if (streaming) {
				logger.error("Cannot export entity " + entityNodeRef + ", the response is already committed", e);
			} else {
				throw e;
			}
		}

	}

	/**
	 * <p>refuseExport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param resp a {@link org.springframework.extensions.webscripts.WebScriptResponse} object
	 * @param streaming whether the response output stream has already been taken
	 */
	private void refuseExport(NodeRef entityNodeRef, WebScriptResponse resp, boolean streaming) {
		logger.warn("User " + AuthenticationUtil.getFullyAuthenticatedUser() + " is not allowed to export entity " + entityNodeRef);
		endOnError(resp, streaming, Status.STATUS_FORBIDDEN, ACCESS_DENIED_MESSAGE);
	}

	/**
	 * <p>endOnError.</p>
	 *
	 * Reports the error to the caller, unless the response output stream has already been taken:
	 * the container then renders the error by asking the response for its writer, which fails on
	 * "getOutputStream() has already been called" and replaces the real cause with a message about
	 * the response. Past that point the cause has been logged and the caller keeps the truncated
	 * body it already holds.
	 *
	 * @param resp a {@link org.springframework.extensions.webscripts.WebScriptResponse} object
	 * @param streaming whether the response output stream has already been taken
	 * @param status the status to report
	 * @param message the message to report
	 */
	private void endOnError(WebScriptResponse resp, boolean streaming, int status, String message) {
		if (streaming) {
			return;
		}
		resp.reset();
		throw new WebScriptException(status, message);
	}

}
