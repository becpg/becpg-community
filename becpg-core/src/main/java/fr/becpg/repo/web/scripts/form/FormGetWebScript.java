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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.form.BecpgFormService;

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
				NodeRef nodeRef = (entityNodeRef != null && !entityNodeRef.isEmpty()) ? new NodeRef(entityNodeRef) : null;
				ret = becpgFormService.getForm(itemKind, itemId, formId, siteId, nodeRef);

			}
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString(3));

		} catch (Exception e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled() && watch!=null) {
				watch.stop();
				logger.debug("MultilingualFieldWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

}
