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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Set;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.RemoteParams;

/**
 * <p>ListEntitiesWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ListEntitiesWebScript extends AbstractEntityWebScript {

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			RemoteParams params = new RemoteParams(getFormat(req));

			Set<String> fields = extractFields(req);
			Set<String> lists = extractLists(req);
			params.setFilteredFields(fields, namespaceService);
			params.setFilteredLists(lists);

			boolean shouldLimit = fields != null && !fields.isEmpty() || lists != null && !lists.isEmpty();

			resp.setContentType(getContentType(req));
			resp.setContentEncoding("UTF-8");

			Integer maxResults = intParam(req, PARAM_MAX_RESULTS);
			if (shouldLimit && maxResults != null) {
				if (maxResults <= 0 || maxResults > maxResultsLimit()) {
					maxResults = maxResultsLimit();
				}
			}

			PagingResults<NodeRef> entities = findEntities(req, maxResults);

			logger.debug("List entities");

			// Write to secondary buffer
			remoteEntityService.listEntities(entities, buffer, params);

			// Write buffer content to response output stream
			buffer.writeTo(resp.getOutputStream());

			resp.setStatus(Status.STATUS_OK);
		} catch (BeCPGException e) {
			logger.error("Cannot export entity", e);
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
