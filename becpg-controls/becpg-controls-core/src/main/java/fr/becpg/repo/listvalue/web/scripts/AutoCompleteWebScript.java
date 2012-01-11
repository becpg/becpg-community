/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue.web.scripts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
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

// TODO: Auto-generated Javadoc
/**
 * The Class AutoCompleteWebScript.
 *
 * @author querephi
 */
public class AutoCompleteWebScript extends DeclarativeWebScript {
	

	

	// request parameter names
	/** The Constant PARAM_SOURCE_TYPE. */
	private static final String PARAM_SOURCE_TYPE = "sourcetype";
	
	/** The Constant PARAM_CLASS_NAME. */
	private static final String PARAM_CLASS_NAME = "className";
	
	private static final String PARAM_CLASS_NAMES = "classNames";
	
	/** The Constant PARAM_PATH. */
	private static final String PARAM_PATH = "path";
	
	/** The Constant PARAM_PARENT. */
	private static final String PARAM_PARENT = "parent";
	
	/** The Constant PARAM_NODEREF. */
	private static final String PARAM_NODEREF = "entityNodeRef";
	
	/** The Constant PARAM_QUERY. */
	private static final String PARAM_QUERY = "q";
	
	/** The Constant PARAM_PRODUCT_TYPE. */
	private static final String PARAM_PRODUCT_TYPE = "productType";
	
	// model key names
	/** The Constant MODEL_KEY_NAME_SUGGESTIONS. */
	private static final String MODEL_KEY_NAME_SUGGESTIONS = "suggestions";
	
	//values


	/** The Constant PARAM_QUERY. */
	private static final String PARAM_PAGE = "page";

	private static final String MODEL_PAGE_SIZE = "pageSize";

	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AutoCompleteWebScript.class);	
	
	/** The list value service. */
	private ListValueService listValueService;
	
		
	/**
	 * Sets the list value service.
	 *
	 * @param listValueService the new list value service
	 */
	public void setListValueService(ListValueService listValueService) {
		this.listValueService = listValueService;
	}		
	
	
	/**
	 * Suggest values according to query
	 * 
	 * url : becpg/autocomplete/{sourcetype}/{path}?q=&parent=.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		ListValuePage suggestions;
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String sourceType = templateArgs.get(PARAM_SOURCE_TYPE);
		String className = templateArgs.get(PARAM_CLASS_NAME);
		String path = templateArgs.get(PARAM_PATH);
		String page = req.getParameter(PARAM_PAGE);
		String query = req.getParameter(PARAM_QUERY);
		String parent = req.getParameter(PARAM_PARENT);
		String nodeRef = req.getParameter(PARAM_NODEREF);
		String productType = templateArgs.get(PARAM_PRODUCT_TYPE);
		String classNames = req.getParameter(PARAM_CLASS_NAMES);		
		
		Integer pageNum = null;
		if(page!=null){
			try {
				pageNum = Integer.parseInt(page);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse page argument",e);
			}
		}
		
		
		Locale locale = I18NUtil.getLocale();
		
		logger.debug("exec webscript");
		

	
			Map<String,Serializable> props = new HashMap<String, Serializable>();
			props.put(ListValueService.PROP_LOCALE,locale);
			props.put(ListValueService.PROP_NODEREF,nodeRef);
			props.put(ListValueService.PROP_PATH,path);
			props.put(ListValueService.PROP_CLASS_NAME, className);
			props.put(ListValueService.PROP_CLASS_NAMES, classNames);
			props.put(ListValueService.PROP_PARENT, parent);
			props.put(ListValueService.PROP_PRODUCT_TYPE, productType);
		
			
			suggestions = listValueService.suggestBySourceType(sourceType , query,  pageNum, props);
		
		
		if(suggestions==null){
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'sourcetype'. sourcetype = " + sourceType);
		}
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_SUGGESTIONS, suggestions);
		model.put(MODEL_PAGE_SIZE, ListValueService.SUGGEST_PAGE_SIZE);
		logger.debug("return model");
		return model;		
	}
	
	}
