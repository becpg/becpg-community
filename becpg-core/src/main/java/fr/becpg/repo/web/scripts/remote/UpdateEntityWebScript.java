/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * Update entity with POST xml
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UpdateEntityWebScript extends AbstractEntityWebScript {

	private static final String PARAM_CREATE_VERSION = "createVersion";
	private static final String PARAM_MAJOR_VERSION = "majorVersion";
	private static final String PARAM_DESCRIPTION = "versionDescription";

	private EntityVersionService entityVersionService;

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		NodeRef entityNodeRef = findEntity(req);

		if ("true".equals(req.getParameter(PARAM_CREATE_VERSION))) {
			String description = "";

			if (req.getParameter(PARAM_DESCRIPTION) != null) {
				description = req.getParameter(PARAM_DESCRIPTION);
			}

			VersionType versionType = VersionType.MINOR;

			if (req.getParameter(PARAM_MAJOR_VERSION) != null) {
				versionType = "true".equals(req.getParameter(PARAM_MAJOR_VERSION)) ? VersionType.MAJOR : VersionType.MINOR;
			}

			Map<String, Serializable> properties = new HashMap<>();
			properties.put(VersionBaseModel.PROP_VERSION_TYPE, versionType);
			properties.put(Version.PROP_DESCRIPTION, description);

			// Create first version if needed
			entityVersionService.createInitialVersion(entityNodeRef);

			entityVersionService.createVersion(entityNodeRef, properties);

		}
		logger.debug("Update entity: " + entityNodeRef);
		RemoteEntityFormat format = getFormat(req);
		NodeRef newNodeRef = remoteEntityService.createOrUpdateEntity(entityNodeRef, req.getContent().getInputStream(), format,
				getEntityProviderCallback(req));
		sendOKStatus(newNodeRef, resp, format);

	}
}
