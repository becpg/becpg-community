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
import org.alfresco.service.cmr.version.VersionType;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * Create entity branch
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MergeEntityWebScript extends AbstractEntityWebScript {

	private static final String PARAM_MAJOR_VERSION = "majorVersion";
	private static final String PARAM_DESCRIPTION = "description";
	private static final String PARAM_BRANCH_TO_NODEREF = "branchToNodeRef";
	private static final String PARAM_IMPACT_WUSED = "impactWused";
	private static final String PARAM_RENAME_ON_MERGE = "renameOnMerge";

	private static final String VALUE_TRUE = "true";

	private EntityVersionService entityVersionService;

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {
			NodeRef entityNodeRef = findEntity(req);

			if (logger.isDebugEnabled()) {
				logger.debug("Merge entity: " + entityNodeRef);
			}

			NodeRef branchToNodeRef = null;

			if (req.getParameter(PARAM_BRANCH_TO_NODEREF) != null) {
				branchToNodeRef = new NodeRef(req.getParameter(PARAM_BRANCH_TO_NODEREF));
			}

			String description = "";

			if (req.getParameter(PARAM_DESCRIPTION) != null) {
				description = req.getParameter(PARAM_DESCRIPTION);
			}

			VersionType versionType = VersionType.MINOR;

			if (req.getParameter(PARAM_MAJOR_VERSION) != null) {
				versionType = VALUE_TRUE.equals(req.getParameter(PARAM_MAJOR_VERSION)) ? VersionType.MAJOR : VersionType.MINOR;
			}

			boolean impactWused = false;
			if ((req.getParameter(PARAM_IMPACT_WUSED) != null) && VALUE_TRUE.equals(req.getParameter((PARAM_IMPACT_WUSED)))) {
				impactWused = true;
			}

			boolean rename = false;
			if ((req.getParameter(PARAM_RENAME_ON_MERGE) != null) && VALUE_TRUE.equals(req.getParameter((PARAM_RENAME_ON_MERGE)))) {
				rename = true;
			}

			JSONObject json = (JSONObject) req.parseContent();
			try {

				if (json != null) {
					if (json.has(PARAM_DESCRIPTION)) {
						description = (String) json.get(PARAM_DESCRIPTION);
					}
					if (json.has(PARAM_MAJOR_VERSION)) {
						versionType = json.get(PARAM_MAJOR_VERSION).equals(VALUE_TRUE) ? VersionType.MAJOR : VersionType.MINOR;
					}
					if (json.has(PARAM_BRANCH_TO_NODEREF)) {
						branchToNodeRef = new NodeRef((String) json.get(PARAM_BRANCH_TO_NODEREF));
					}

					if (json.has(PARAM_IMPACT_WUSED) && VALUE_TRUE.equals(json.get(PARAM_IMPACT_WUSED))) {
						impactWused = true;
					}
				}
				if (branchToNodeRef == null) {
					throw new WebScriptException("Branch nodeRef is mandatory..");
				}

				if (logger.isDebugEnabled()) {
					logger.debug("branchToNodeRef: " + branchToNodeRef);
					logger.debug("entityNodeRef: " + entityNodeRef);
					logger.debug("description: " + description);
					logger.debug("versionType: " + versionType);
					logger.debug("impactWused: " + impactWused);
				}
				NodeRef newEntityNodeRef = entityVersionService.mergeBranch(entityNodeRef, branchToNodeRef, versionType, description, impactWused,
						rename);

				if (impactWused) {
					entityVersionService.impactWUsed(newEntityNodeRef, versionType, description, null);
				}

				sendOKStatus(newEntityNodeRef, resp, getFormat(req));

			} catch (JSONException e) {
				logger.error("Cannot merge entity", e);
				throw new WebScriptException(e.getMessage());
			}
	}
}
