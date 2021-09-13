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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.telemetry.OpenCensusConfiguration;
import io.opencensus.common.Scope;
import io.opencensus.trace.samplers.Samplers;

/**
 * Get entity as XML
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GetEntityWebScript extends AbstractEntityWebScript {

	private static final String PARAM_PARAMS = "params";

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		try (Scope scope = tracer.spanBuilder("/remote/get").setSampler(Samplers.probabilitySampler(OpenCensusConfiguration.REMOTE_GET_SAMPLING_PROBABILITY)).startScopedSpan()) {
			NodeRef entityNodeRef = findEntity(req);

			logger.debug("Get entity: " + entityNodeRef);

			try (OutputStream out = resp.getOutputStream()) {

				RemoteParams params = new RemoteParams(getFormat(req));
				params.setFilteredFields(extractFields(req), namespaceService);
				params.setFilteredLists(extractLists(req));
				params.setJsonParams(extractParams(req));
				resp.setContentType(getContentType(req));
				resp.setContentEncoding("UTF-8");

				remoteEntityService.getEntity(entityNodeRef, out, params);

			
				resp.setStatus(Status.STATUS_OK);
			} catch (BeCPGException e) {
				logger.error("Cannot export entity", e);
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

	private JSONObject extractParams(WebScriptRequest req) {

		String params = req.getParameter(PARAM_PARAMS);
		if ((params != null) && !params.isEmpty()) {

			try {
				return new JSONObject(params);
			} catch (JSONException e) {
				logger.error("Cannot parse params:" + params);
			}
		}
		return null;
	}

}
