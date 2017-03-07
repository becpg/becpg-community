/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.product;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;

public class FormulateRecipeWebScript extends AbstractProductWebscript {

	private static final String PARAM_RECIPE = "recipe";
	private static final String PARAM_NAME = "name";

	private static final String PARAM_METADATA = "metadata";

	private static final Log logger = LogFactory.getLog(FormulateRecipeWebScript.class);

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)  throws IOException {
		logger.debug("start formulate webscript");

		
		ProductData productData = new FinishedProductData();

		try 
		{
			
			JSONObject json;

			if (req.getParameter(PARAM_METADATA) != null) {
				json = new JSONObject(req.getParameter(PARAM_METADATA));
			} else {
				json = (JSONObject) req.parseContent();
			}
			
			String recipe = "";
			if (json != null && json.has(PARAM_RECIPE)) {
				recipe = (String) json.get(PARAM_RECIPE);
			}
			
			if (json != null && json.has(PARAM_NAME)) {
				productData.setName((String) json.get(PARAM_NAME));
			}
			
			JSONObject ret = new JSONObject();

			
			if (recipe != null && recipe.length()>0) {
				ret.put("productData", productService.formulateText(recipe,productData));
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException | FormulateException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} 
	}

}
