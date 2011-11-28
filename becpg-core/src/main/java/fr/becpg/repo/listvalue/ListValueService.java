/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Interface ListValueService.
 *
 * @author querephi
 */
public interface ListValueService {

	/** Prop Constants **/
	
	
	public static final String PROP_SOURCE_TYPE = "sourcetype";
	
	public static final String PROP_CLASS_NAME = "className";
	
	public static final String PROP_PATH = "path";
	
	public static final String PROP_PARENT = "parent";
	
	public static final String PROP_NODEREF = "nodeRef";
	
	public static final String PROP_LOCALE = "locale";
	
	
	
	/**
	 * Suggest target assoc.
	 *
	 * @param type the type
	 * @param query the query
	 * @return the map
	 */
	public ListValuePage suggestTargetAssoc(QName type, String query, Integer pageNum, Locale locale);
	
	/**
	 * Suggest linked value.
	 *
	 * @param path the path
	 * @param parent the parent
	 * @param query the query
	 * @return the map
	 */
	public ListValuePage suggestLinkedValue(String path, String parent, String query, Integer pageNum, Locale locale);
	
	/**
	 * Suggest list value.
	 *
	 * @param path the path
	 * @param query the query
	 * @return the map
	 */
	public ListValuePage suggestListValue(String path, String query, Integer pageNum, Locale locale);
	
	/**
	 * Suggest product.
	 *
	 * @param query the query
	 * @return the map
	 */
	public ListValuePage suggestProduct(String query, Integer pageNum, Locale locale);
	
	/**
	 * Gets the item by type and name.
	 *
	 * @param type the type
	 * @param name the name
	 * @return the item by type and name
	 */
	public NodeRef getItemByTypeAndName(QName type, String name);
	
	/**
	 * Suggest product report templates.
	 *
	 * @param systemProductType the system product type
	 * @param query the query
	 * @return the map
	 */
	public ListValuePage suggestProductReportTemplates(QName nodeType, String query, Integer pageNum);

	
	
	/**
	 * 
	 * @param sourceType
	 * @param query
	 * @param pageNum
	 * @param props
	 * @return
	 */
	public ListValuePage suggestBySourceType(String sourceType, String query, Integer pageNum,
			Map<String, Serializable> props);
}
