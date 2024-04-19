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
import java.io.InputStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteParams;

/**
 * Create entity with POST xml
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CreateEntityWebScript extends AbstractEntityWebScript {

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		logger.debug("Create entity");
		if (logger.isTraceEnabled()) {
			logger.trace("Request details: " + req.getContentType() + " " + req.getFormat());
			logger.trace("XML request DUMP: ");
			InputStream in = req.getContent().getInputStream();
			IOUtils.copy(in, System.out);
			if (in.markSupported()) {
				in.reset();
			}
		}

		RemoteEntityFormat format = getFormat(req);

		NodeRef entityNodeRef = remoteEntityService.createOrUpdateEntity(null, req.getContent().getInputStream(), new RemoteParams(format),
				getEntityProviderCallback(req));

		sendOKStatus(entityNodeRef, resp, format);

	}

}
