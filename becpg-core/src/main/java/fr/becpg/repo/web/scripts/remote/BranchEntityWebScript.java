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

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.version.EntityVersionService;
import io.opencensus.common.Scope;

/**
 * Create entity branch
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BranchEntityWebScript extends AbstractEntityWebScript {

	private static final String PARAM_DEST_NODEREF = "destNodeRef";

	private EntityVersionService entityVersionService;

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		try (Scope scope = tracer.spanBuilder("/remote/branch").startScopedSpan()) {

			NodeRef entityNodeRef = findEntity(req);

			if (logger.isDebugEnabled()) {
				logger.debug("Branch entity: " + entityNodeRef);
			}

			NodeRef destNodeRef = null;
			if (req.getParameter(PARAM_DEST_NODEREF) != null) {
				destNodeRef = new NodeRef(req.getParameter(PARAM_DEST_NODEREF));
			} else {
				destNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
			}

			try {
				sendOKStatus(entityVersionService.createBranch(entityNodeRef, destNodeRef), resp, getFormat(req));

			} catch (BeCPGException e) {
				logger.error("Cannot branch entity", e);
				throw new WebScriptException(e.getMessage());
			}
		}

	}
}
