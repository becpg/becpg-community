/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.web.scripts.listvalue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

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
	
	/** The Constant PARAM_PATH. */
	private static final String PARAM_PATH = "path";
	
	/** The Constant PARAM_PARENT. */
	private static final String PARAM_PARENT = "parent";
	
	/** The Constant PARAM_QUERY. */
	private static final String PARAM_QUERY = "q";
	
	/** The Constant PARAM_PRODUCT_TYPE. */
	private static final String PARAM_PRODUCT_TYPE = "productType";
	
	// model key names
	/** The Constant MODEL_KEY_NAME_SUGGESTIONS. */
	private static final String MODEL_KEY_NAME_SUGGESTIONS = "suggestions";
	
	//values
	/** The Constant SOURCE_TYPE_TARGET_ASSOC. */
	private static final String SOURCE_TYPE_TARGET_ASSOC = "targetassoc";
	
	/** The Constant SOURCE_TYPE_PRODUCT. */
	private static final String SOURCE_TYPE_PRODUCT = "product";
	
	/** The Constant SOURCE_TYPE_LINKED_VALUE. */
	private static final String SOURCE_TYPE_LINKED_VALUE = "linkedvalue";
	
	/** The Constant SOURCE_TYPE_LIST_VALUE. */
	private static final String SOURCE_TYPE_LIST_VALUE = "listvalue";
	
	/** The Constant SOURCE_TYPE_PRODUCT_REPORT. */
	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AutoCompleteWebScript.class);	
	
	/** The list value service. */
	private ListValueService listValueService;
	
	/** The namespace service. */
	private NamespaceService namespaceService;
		
	/**
	 * Sets the list value service.
	 *
	 * @param listValueService the new list value service
	 */
	public void setListValueService(ListValueService listValueService) {
		this.listValueService = listValueService;
	}		
	
	
	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
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
				
		Map<String, String> suggestions;
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String sourceType = templateArgs.get(PARAM_SOURCE_TYPE);
		String className = templateArgs.get(PARAM_CLASS_NAME);
		String path = templateArgs.get(PARAM_PATH);
		String query = req.getParameter(PARAM_QUERY);
		String parent = req.getParameter(PARAM_PARENT);
		String productType = templateArgs.get(PARAM_PRODUCT_TYPE);
		
		Locale locale = I18NUtil.getLocale();
		
		logger.debug("exec webscript");
		
		if(sourceType.equals(SOURCE_TYPE_TARGET_ASSOC)){
			QName type = QName.createQName(className, namespaceService);
			suggestions = listValueService.suggestTargetAssoc(type, query, locale);
		}
		else if(sourceType.equals(SOURCE_TYPE_PRODUCT)){
			suggestions = listValueService.suggestProduct(query, locale);
		}
		else if(sourceType.equals(SOURCE_TYPE_LINKED_VALUE)){
			suggestions = listValueService.suggestLinkedValue(path, parent, query, locale);
		}
		else if(sourceType.equals(SOURCE_TYPE_LIST_VALUE)){
			suggestions = listValueService.suggestListValue(path, query, locale);
		}
		else if(sourceType.equals(SOURCE_TYPE_PRODUCT_REPORT)){			
			
			QName productTypeQName = QName.createQName(productType, namespaceService);
			suggestions = listValueService.suggestProductReportTemplates(productTypeQName, query);
		}
		else{
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'sourcetype'. sourcetype = " + sourceType);
		}
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_SUGGESTIONS, suggestions);
		logger.debug("return model");
		return model;		
	}
	
	}
