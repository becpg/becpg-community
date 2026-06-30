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
package fr.becpg.repo.web.scripts.remote.ai;

import java.io.IOException;
import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>Capabilities &amp; bootstrap probe for the AI remote API ({@code /becpg/remote/ai/capabilities}).</p>
 * <p>
 * becpg-ai calls this once per instance/session to (1) detect whether the tenant exposes the new
 * {@code /becpg/remote/ai/*} endpoints (HTTP 200 = new mode, 404 = fall back to the generic remote API), and
 * (2) bootstrap a small context so it can avoid repeated round-trips: the current user (+ home folder), the
 * available entity types/models, and the beCPG version. Runs as the authenticated user, so the returned
 * person/home reflect the caller.
 *
 * @author matthieu
 */
public class AiCapabilitiesWebScript extends AbstractWebScript {

	/** Bump when the AI remote API contract changes in a way clients must detect. */
	private static final String API_VERSION = "1";

	private NodeService nodeService;
	private NamespaceService namespaceService;
	private PersonService personService;
	private EntityDictionaryService entityDictionaryService;
	private ModuleService moduleService;

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		try {
			JSONObject out = new JSONObject();
			out.put("apiVersion", API_VERSION);
			out.put("endpoints", new JSONArray().put("capabilities").put("search").put("dictionary"));
			out.put("becpgVersion", resolveBecpgVersion());
			out.put("user", buildUser());
			out.put("entityTypes", buildEntityTypes());

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			resp.getWriter().write(out.toString());
			resp.setStatus(Status.STATUS_OK);
		} catch (JSONException e) {
			throw new WebScriptException("Cannot serialize AI capabilities: " + e.getMessage());
		}
	}

	private JSONObject buildUser() throws JSONException {
		JSONObject user = new JSONObject();
		String userName = AuthenticationUtil.getFullyAuthenticatedUser();
		user.put("userName", userName);
		if ((userName != null) && personService.personExists(userName)) {
			NodeRef personRef = personService.getPerson(userName, false);
			user.put("nodeRef", personRef.toString());
			putIfPresent(user, "firstName", nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME));
			putIfPresent(user, "lastName", nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME));
			putIfPresent(user, "email", nodeService.getProperty(personRef, ContentModel.PROP_EMAIL));
			Object homeFolder = nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
			if (homeFolder != null) {
				user.put("homeFolder", homeFolder.toString());
			}
		}
		return user;
	}

	private JSONArray buildEntityTypes() throws JSONException {
		JSONArray types = new JSONArray();
		Collection<QName> subTypes = entityDictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITY_V2);
		if (subTypes != null) {
			for (QName type : subTypes) {
				types.put(type.toPrefixString(namespaceService));
			}
		}
		return types;
	}

	private String resolveBecpgVersion() {
		try {
			ModuleDetails best = null;
			for (ModuleDetails module : moduleService.getAllModules()) {
				if ((module.getId() != null) && module.getId().startsWith("becpg")) {
					if ("becpg-core".equals(module.getId()) || "becpg".equals(module.getId())) {
						return module.getModuleVersionNumber().toString();
					}
					if (best == null) {
						best = module;
					}
				}
			}
			return (best != null) ? best.getModuleVersionNumber().toString() : null;
		} catch (Exception e) {
			// best-effort: version is informational, never fail the capabilities probe over it
			return null;
		}
	}

	private static void putIfPresent(JSONObject obj, String key, Object value) throws JSONException {
		if (value != null) {
			obj.put(key, value.toString());
		}
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>moduleService</code>.</p>
	 *
	 * @param moduleService a {@link org.alfresco.service.cmr.module.ModuleService} object
	 */
	public void setModuleService(ModuleService moduleService) {
		this.moduleService = moduleService;
	}

}
