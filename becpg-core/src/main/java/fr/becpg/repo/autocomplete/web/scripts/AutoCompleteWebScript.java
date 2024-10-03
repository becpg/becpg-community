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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.autocomplete.web.scripts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;

/**
 * The Class AutoCompleteWebScript.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class AutoCompleteWebScript extends DeclarativeWebScript {

	private static final String PARAM_SOURCE_TYPE = "sourcetype";
	private static final String PARAM_CLASS_NAME = "className";
	private static final String PARAM_CLASS_NAMES = "classNames";
	private static final String PARAM_ATTRIBUTE_NAME = "attributeName";
	private static final String PARAM_FILTER = "filter";
	private static final String PARAM_EXCLUDE_CLASS_NAMES = "excludeClassNames";
	private static final String PARAM_EXCLUDE_PROPS = "excludeProps";
	private static final String PROP_EXCLUDE_SOURCES = "excludeSources";
	private static final String PROP_ITEM_ID = "itemId";
	private static final String PROP_FIELD_NAME = "fieldName";
	private static final String PARAM_AND_PROPS = "andProps";
	private static final String PARAM_PAGE_SIZE = "pageSize";
	private static final String PARAM_PATH = "path";
	private static final String PARAM_PARENT = "parent";
	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";
	private static final String PARAM_IS_SEARCH = "isSearch";
	private static final String PARAM_QUERY = "q";
	private static final String PARAM_PRODUCT_TYPE = "productType";
	private static final String MODEL_KEY_NAME_SUGGESTIONS = "suggestions";
	private static final String PARAM_PAGE = "page";
	private static final String MODEL_PAGE_SIZE = "pageSize";

	private static final Log logger = LogFactory.getLog(AutoCompleteWebScript.class);

	private AutoCompleteService autoCompleteService;


	/**
	 * <p>Setter for the field <code>AutoCompleteService</code>.</p>
	 *
	 * @param autoCompleteService a {@link fr.becpg.repo.autocomplete.AutoCompleteService} object
	 */
	public void setAutoCompleteService(AutoCompleteService autoCompleteService) {
		this.autoCompleteService = autoCompleteService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Suggest values according to query
	 *
	 * url : becpg/autocomplete/{sourcetype}/{path}?q=&amp;parent=.
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		AutoCompletePage suggestions;
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String sourceType = templateArgs.get(PARAM_SOURCE_TYPE);

		// ClassName can be in template or in param list
		String className = templateArgs.get(PARAM_CLASS_NAME);
		if (className == null) {
			className = req.getParameter(PARAM_CLASS_NAME);
		}
		// Pagination
		String page = req.getParameter(PARAM_PAGE);
		String pageSizeParam = req.getParameter(PARAM_PAGE_SIZE);

		// Filters
		String query = req.getParameter(PARAM_QUERY);
		String parent = req.getParameter(PARAM_PARENT);
		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		String isSearch = req.getParameter(PARAM_IS_SEARCH);

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

		Integer pageSize = AutoCompleteService.SUGGEST_PAGE_SIZE;
		if (pageSizeParam != null) {
			try {
				pageSize = Integer.parseInt(pageSizeParam);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse pageSize argument", e);
			}
		}

		Locale locale = I18NUtil.getLocale();

		logger.debug("exec webscript");

		Map<String, Serializable> props = new HashMap<>();
		props.put(AutoCompleteService.PROP_LOCALE, locale);
		props.put(AutoCompleteService.PROP_ENTITYNODEREF, entityNodeRef);
		props.put(AutoCompleteService.PROP_PATH, path);
		props.put(AutoCompleteService.PROP_CLASS_NAME, className);
		props.put(AutoCompleteService.PROP_CLASS_NAMES, req.getParameter(PARAM_CLASS_NAMES));
		props.put(AutoCompleteService.PROP_ATTRIBUTE_NAME, req.getParameter(PARAM_ATTRIBUTE_NAME));
		props.put(AutoCompleteService.PROP_FILTER, req.getParameter(PARAM_FILTER));
		props.put(AutoCompleteService.PROP_EXCLUDE_CLASS_NAMES,  req.getParameter(PARAM_EXCLUDE_CLASS_NAMES));
		props.put(AutoCompleteService.PROP_EXCLUDE_PROPS, req.getParameter(PARAM_EXCLUDE_PROPS));
		props.put(AutoCompleteService.PROP_EXCLUDE_SOURCES, req.getParameter(PROP_EXCLUDE_SOURCES));
		props.put(AutoCompleteService.PROP_ITEM_ID, req.getParameter(PROP_ITEM_ID));
		props.put(AutoCompleteService.PROP_FIELD_NAME, req.getParameter(PROP_FIELD_NAME));
		props.put(AutoCompleteService.PROP_AND_PROPS, req.getParameter(PARAM_AND_PROPS));
		props.put(AutoCompleteService.PROP_PARENT, parent);
		props.put(AutoCompleteService.PROP_PRODUCT_TYPE, productType);
		props.put(AutoCompleteService.PROP_INCLUDE_DELETED, isSearch != null || Boolean.TRUE.equals(Boolean.parseBoolean(isSearch)));
		props.put(AutoCompleteService.EXTRA_PARAM, getExtraParams(req));
		
		
		HashMap<String, String> extras = getExtraParams(req);
		if (extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID) != null && NodeRef.isNodeRef(extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID))) {
				props.put(AutoCompleteService.PROP_NODEREF,  extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID));
		}
		
		props.put(AutoCompleteService.EXTRA_PARAM, extras);
		

		suggestions = autoCompleteService.suggestBySourceType(sourceType, query, pageNum, pageSize, props);

		if (suggestions == null) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'sourcetype'. sourcetype = " + sourceType);
		}

		Map<String, Object> model = new HashMap<>();
		model.put(MODEL_KEY_NAME_SUGGESTIONS, suggestions);
		model.put(MODEL_PAGE_SIZE, pageSize);
		logger.debug("return model");
		return model;
	}

	private HashMap<String, String> getExtraParams(WebScriptRequest req) {
		HashMap<String, String> ret = new HashMap<>();

		for (String name : req.getParameterNames()) {
			if (name.startsWith(AutoCompleteService.EXTRA_PARAM + ".")) {
				ret.put(name.replace(AutoCompleteService.EXTRA_PARAM + ".", ""), req.getParameter(name));
			}
		}

		return ret;
	}

}
