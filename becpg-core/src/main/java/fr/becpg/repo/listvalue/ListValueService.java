/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.SystemProductType;

// TODO: Auto-generated Javadoc
/**
 * The Interface ListValueService.
 *
 * @author querephi
 */
public interface ListValueService {

	/**
	 * Gets the allowed values.
	 *
	 * @param path the path
	 * @param constraintType the constraint type
	 * @param constraintProp the constraint prop
	 * @return the allowed values
	 */
	public List<String> getAllowedValues(String path, QName constraintType, QName constraintProp);
	
	/**
	 * Suggest target assoc.
	 *
	 * @param type the type
	 * @param query the query
	 * @return the map
	 */
	public Map<String, String> suggestTargetAssoc(QName type, String query);
	
	/**
	 * Suggest linked value.
	 *
	 * @param path the path
	 * @param parent the parent
	 * @param query the query
	 * @return the map
	 */
	public Map<String, String> suggestLinkedValue(String path, String parent, String query);
	
	/**
	 * Suggest list value.
	 *
	 * @param path the path
	 * @param query the query
	 * @return the map
	 */
	public Map<String, String> suggestListValue(String path, String query);
	
	/**
	 * Suggest product.
	 *
	 * @param query the query
	 * @return the map
	 */
	public Map<String, String> suggestProduct(String query);
	
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
	public Map<String, String> suggestProductReportTemplates(QName nodeType, String query);
}
