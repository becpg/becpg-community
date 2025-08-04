/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
package fr.becpg.repo.web.scripts.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.form.BecpgFormService;
import fr.becpg.repo.form.impl.BecpgFormDefinition;

/**
 * Return or save MLText field
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FormGetWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(FormGetWebScript.class);

	private static final String PARAM_SITEID = "siteId";

	private static final String PARAM_ITEMKIND = "itemKind";

	private static final String PARAM_ITEMID = "itemId";

	private static final String PARAM_FORMID = "formId";

	private static final String PARAM_RELOAD = "reload";

	private static final String PARAM_NODE_REF = "entityNodeRef";

	private static final String PARAM_FIELDS = "fields";

	private static final String PARAM_FORCEDFIELDS = "force";

	private BecpgFormService becpgFormService;

	/**
	 * <p>Setter for the field <code>becpgFormService</code>.</p>
	 *
	 * @param becpgFormService a {@link fr.becpg.repo.form.BecpgFormService} object.
	 */
	public void setBecpgFormService(BecpgFormService becpgFormService) {
		this.becpgFormService = becpgFormService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String itemKind = req.getParameter(PARAM_ITEMKIND);
		String itemId = req.getParameter(PARAM_ITEMID);
		String formId = req.getParameter(PARAM_FORMID);
		String siteId = req.getParameter(PARAM_SITEID);
		String entityNodeRef = req.getParameter(PARAM_NODE_REF);
		List<String> fields = new ArrayList<>();
		List<String> forcedFields = new ArrayList<>();

		/* Parse the JSON content */

		String contentType = req.getContentType();
		if ((contentType != null) && (contentType.indexOf(';') != -1)) {
			contentType = contentType.substring(0, contentType.indexOf(';'));
		}
		try {

			JSONObject json = new JSONObject(req.getContent().getContent());

			if (json.has(PARAM_ITEMKIND)) {
				itemKind = (String) json.get(PARAM_ITEMKIND);

			}

			if (json.has(PARAM_ITEMID)) {
				itemId = (String) json.get(PARAM_ITEMID);

			}
			
			if (json.has(PARAM_FORMID) && !JSONObject.NULL.equals(json.get(PARAM_FORMID))) {
				formId = (String) json.get(PARAM_FORMID);
				
			}

			if (json.has(PARAM_SITEID) && !JSONObject.NULL.equals(json.get(PARAM_SITEID))) {
				siteId = (String) json.get(PARAM_SITEID);
			}

			if (json.has(PARAM_NODE_REF) && !JSONObject.NULL.equals(json.get(PARAM_NODE_REF))) {
				entityNodeRef = (String) json.get(PARAM_NODE_REF);
			}

			if (json.has(PARAM_FIELDS)) {
				org.json.JSONArray tmp = json.getJSONArray(PARAM_FIELDS);
				for (int i = 0; i < tmp.length(); i++) {
					fields.add(tmp.getString(i));
				}
			}

			if (json.has(PARAM_FORCEDFIELDS)) {
				org.json.JSONArray tmp = json.getJSONArray(PARAM_FORCEDFIELDS);
				for (int i = 0; i < tmp.length(); i++) {
					forcedFields.add(tmp.getString(i));
				}
			}
			
		

		} catch (IOException | JSONException io) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
		}

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		try {

			JSONObject ret = new JSONObject();

			if ("true".equals(req.getParameter(PARAM_RELOAD))) {
				becpgFormService.reloadConfig();
				ret.put("SUCCESS", true);
			} else {
				NodeRef nodeRef = ((entityNodeRef != null) && !entityNodeRef.isEmpty() ) ? new NodeRef(entityNodeRef) : null;
				BecpgFormDefinition def = becpgFormService.getForm(itemKind, itemId, formId, siteId, fields, forcedFields, nodeRef);
				ret = def.getMergeDef();
				
				if(logger.isDebugEnabled()) {
					logger.debug(itemKind+"/"+itemId+"/"+siteId+"/"+formId+"/"+entityNodeRef);
					logger.debug(ret.toString(3));
				}
			}
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString(3));

		} catch (Exception e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug("MultilingualFieldWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

}
