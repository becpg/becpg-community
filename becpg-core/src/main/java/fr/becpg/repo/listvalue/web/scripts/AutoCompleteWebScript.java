/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.listvalue.web.scripts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;

/**
 * The Class AutoCompleteWebScript.
 * 
 * @author querephi
 */
public class AutoCompleteWebScript extends DeclarativeWebScript {

	private static final String PARAM_SOURCE_TYPE = "sourcetype";
	private static final String PARAM_CLASS_NAME = "className";
	private static final String PARAM_CLASS_NAMES = "classNames";
	private static final String PARAM_ATTRIBUTE_NAME = "attributeName";
	private static final String PARAM_FILTER = "filter";
	private static final String PARAM_EXCLUDE_CLASS_NAMES = "excludeClassNames";
	private static final String PARAM_PAGE_SIZE = "pageSize";
	private static final String PARAM_PATH = "path";
	private static final String PARAM_PARENT = "parent";
	private static final String PARAM_NODEREF = "entityNodeRef";
	private static final String PARAM_QUERY = "q";
	private static final String PARAM_PRODUCT_TYPE = "productType";
	private static final String MODEL_KEY_NAME_SUGGESTIONS = "suggestions";
	private static final String PARAM_PAGE = "page";
	private static final String MODEL_PAGE_SIZE = "pageSize";

	private static final Log logger = LogFactory.getLog(AutoCompleteWebScript.class);

	private ListValueService listValueService;


	public void setListValueService(ListValueService listValueService) {
		this.listValueService = listValueService;
	}

	/**
	 * Suggest values according to query
	 * 
	 * url : becpg/autocomplete/{sourcetype}/{path}?q=&parent=.
	 * 
	 * @param req
	 *            the req
	 * @param status
	 *            the status
	 * @param cache
	 *            the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		ListValuePage suggestions;
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String sourceType = templateArgs.get(PARAM_SOURCE_TYPE);

		// ClassName can be in template or in param list
		String className = templateArgs.get(PARAM_CLASS_NAME);
		if (className == null) {
			className = req.getParameter(PARAM_CLASS_NAME);
		}
		String classNames = req.getParameter(PARAM_CLASS_NAMES);
		String excludeClassNames = req.getParameter(PARAM_EXCLUDE_CLASS_NAMES);

		// Pagination
		String page = req.getParameter(PARAM_PAGE);
		String pageSizeParam = req.getParameter(PARAM_PAGE_SIZE);

		// Filters
		String query = req.getParameter(PARAM_QUERY);
		String parent = req.getParameter(PARAM_PARENT);
		String nodeRef = req.getParameter(PARAM_NODEREF);

		String path = templateArgs.get(PARAM_PATH);
		if(path==null){
			path = req.getParameter(PARAM_PATH);
		}
		
		String productType = templateArgs.get(PARAM_PRODUCT_TYPE);

		Integer pageNum = null;
		if (page != null) {
			try {
				pageNum = Integer.parseInt(page);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse page argument", e);
			}
		}

		Integer pageSize = ListValueService.SUGGEST_PAGE_SIZE;
		if (pageSizeParam != null) {
			try {
				pageSize = Integer.parseInt(pageSizeParam);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse pageSize argument", e);
			}
		}

		Locale locale = I18NUtil.getLocale();

		logger.debug("exec webscript");

		Map<String, Serializable> props = new HashMap<String, Serializable>();
		props.put(ListValueService.PROP_LOCALE, locale);
		props.put(ListValueService.PROP_NODEREF, nodeRef);
		props.put(ListValueService.PROP_PATH, path);
		props.put(ListValueService.PROP_CLASS_NAME, className);
		props.put(ListValueService.PROP_CLASS_NAMES, classNames);
		props.put(ListValueService.PROP_ATTRIBUTE_NAME, req.getParameter(PARAM_ATTRIBUTE_NAME));
		props.put(ListValueService.PROP_FILTER, req.getParameter(PARAM_FILTER));
		props.put(ListValueService.PROP_EXCLUDE_CLASS_NAMES, excludeClassNames);
		props.put(ListValueService.PROP_PARENT, parent);
		props.put(ListValueService.PROP_PRODUCT_TYPE, productType);
		props.put(ListValueService.EXTRA_PARAM, getExtraParams(req));
		

		suggestions = listValueService.suggestBySourceType(sourceType, query, pageNum, pageSize, props);

		if (suggestions == null) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'sourcetype'. sourcetype = " + sourceType);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_SUGGESTIONS, suggestions);
		model.put(MODEL_PAGE_SIZE, pageSize);
		logger.debug("return model");
		return model;
	}

	private HashMap<String, String> getExtraParams(WebScriptRequest req) {
		HashMap<String, String> ret = new HashMap<String, String>();

		for (String name : req.getParameterNames()) {
			if (name.startsWith(ListValueService.EXTRA_PARAM + ".")) {
				ret.put(name.replace(ListValueService.EXTRA_PARAM + ".", ""), req.getParameter(name));
			}
		}

		return ret;
	}

}
