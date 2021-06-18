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

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteParams;
import io.opencensus.common.Scope;

/**
 * Update entity with POST xml
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UpdateEntityDataWebScript extends AbstractEntityWebScript {

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		try (Scope scope = tracer.spanBuilder("/remote/post/data").startScopedSpan()) {
			NodeRef entityNodeRef = findEntity(req);

			logger.debug("Update entity: " + entityNodeRef);

			RemoteEntityFormat format = getFormat(req);
			remoteEntityService.addOrUpdateEntityData(entityNodeRef, req.getContent().getInputStream(), new RemoteParams(format));
			sendOKStatus(entityNodeRef, resp, format);
		}
	}
}
