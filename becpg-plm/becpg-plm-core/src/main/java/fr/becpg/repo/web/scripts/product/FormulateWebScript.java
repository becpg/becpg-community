/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
package fr.becpg.repo.web.scripts.product;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.formulation.FormulateException;

public class FormulateWebScript extends AbstractProductWebscript {

	protected static final String PARAM_FAST = "fast";

	private static final Log logger = LogFactory.getLog(FormulateWebScript.class);

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException, IOException {
		logger.debug("start formulate webscript");

		String fast = req.getParameter(PARAM_FAST);

		boolean isFast = false;
		if (fast != null && fast.equals("true")) {
			isFast = true;
		}

		NodeRef productNodeRef = getProductNodeRef(req);
		try {
			productService.formulate(productNodeRef, isFast);
			
			JSONObject ret = new JSONObject();

			ret.put("productNodeRef", productNodeRef);
			ret.put("status", "SUCCESS");

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
			
		} catch (FormulateException e) {
			handleFormulationError(e);
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}
		
		
	}

}
