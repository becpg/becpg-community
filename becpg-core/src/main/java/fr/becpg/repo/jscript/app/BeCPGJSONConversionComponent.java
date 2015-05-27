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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.jscript.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.app.JSONConversionComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import fr.becpg.repo.helper.AssociationService;

public class BeCPGJSONConversionComponent extends JSONConversionComponent {

	private AssociationService associationService;

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	private static Log logger = LogFactory.getLog(BeCPGJSONConversionComponent.class);

	/** Registered decorators */
	protected Map<QName, AssociationDecorator> associationDecorators = new HashMap<QName, AssociationDecorator>(3);

	/**
	 * Thread local cache of namespace prefixes for long QName to short prefix
	 * name conversions
	 */
	protected static ThreadLocal<Map<String, String>> namespacePrefixCache = new ThreadLocal<Map<String, String>>() {
		@Override
		protected Map<String, String> initialValue() {
			return new HashMap<String, String>(8);
		}
	};

	/**
	 * Register a property decorator;
	 * 
	 * @param propertyDecorator
	 */
	public void registerAssociationDecorator(AssociationDecorator associationDecorator) {
		for (QName assocName : associationDecorator.getAssociationNames()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Register decorators for assoc :" + assocName.toPrefixString(namespaceService));
			}

			associationDecorators.put(assocName, associationDecorator);
		}
	}

	/**
	 * Convert a node reference to a JSON string. Selects the correct converter
	 * based on selection implementation.
	 */
	@SuppressWarnings("unchecked")
	public String toJSON(NodeRef nodeRef, boolean useShortQNames) {
		JSONObject json = new JSONObject();

		if (this.nodeService.exists(nodeRef)) {
			if (publicServiceAccessService.hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "getProperties", nodeRef) == AccessStatus.ALLOWED) {
				// init namespace prefix cache
				namespacePrefixCache.get().clear();

				// Get node info
				FileInfo nodeInfo = fileFolderService.getFileInfo(nodeRef);
				if( nodeInfo!=null ){
					// Set root values
					setRootValues(nodeInfo, json, useShortQNames);
	
					// add permissions
					json.put("permissions", permissionsToJSON(nodeRef));
	
					// add properties
					json.put("properties", propertiesToJSON(nodeRef, nodeInfo.getProperties(), useShortQNames));
	
					// add associations
					json.put("associations", associationsToJSON(nodeRef, useShortQNames));
	
					// add aspects
					json.put("aspects", apsectsToJSON(nodeRef, useShortQNames));
				}
			}
		}

		return json.toJSONString();
	}

	/**
	 * 
	 * @param nodeRef
	 * @param useShortQNames
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	protected JSONObject associationsToJSON(NodeRef nodeRef, boolean useShortQNames) {
		JSONObject assocsToJSON = new JSONObject();

		for (QName assoc : associationDecorators.keySet()) {
			if (associationDecorators.get(assoc) != null) {
				if (nodeService.hasAspect(nodeRef, associationDecorators.get(assoc).getAspect())) {
					try {

						String key = nameToString(assoc, useShortQNames);
						if (logger.isDebugEnabled()) {
							logger.debug("look for assoc decorator: " + assoc.toPrefixString(namespaceService));
						}

						assocsToJSON.put(key, associationDecorators.get(assoc).decorate(assoc, nodeRef, associationService.getTargetAssocs(nodeRef, assoc)));
					} catch (NamespaceException ne) {
						// ignore properties that do not have a registered
						// namespace
						if (logger.isDebugEnabled())
							logger.debug("Ignoring assoc '" + assoc + "' as its namespace is not registered");
					}
				}
			}
		}

		return assocsToJSON;
	}

	/**
	 * Convert a qname to a string - either full or short prefixed named.
	 * 
	 * @param qname
	 * @param isShortName
	 * @return qname string.
	 */
	private String nameToString(final QName qname, final boolean isShortName) {
		String result;
		if (isShortName) {
			final Map<String, String> cache = namespacePrefixCache.get();
			String prefix = cache.get(qname.getNamespaceURI());
			if (prefix == null) {
				// first request for this namespace prefix, get and cache result
				Collection<String> prefixes = this.namespaceService.getPrefixes(qname.getNamespaceURI());
				prefix = prefixes.size() != 0 ? prefixes.iterator().next() : "";
				cache.put(qname.getNamespaceURI(), prefix);
			}
			result = prefix + QName.NAMESPACE_PREFIX + qname.getLocalName();
		} else {
			result = qname.toString();
		}
		return result;
	}

	/**
	 * Handles the work of converting values to JSON.
	 * 
	 * @param nodeRef
	 * @param propertyName
	 * @param key
	 * @param value
	 * @return the JSON value
	 */
	@SuppressWarnings({ "unchecked" })
	protected Object propertyToJSON(final NodeRef nodeRef, final QName propertyName, final String key, final Serializable value) {
		if (value != null) {
			// Has a decorator has been registered for this property?
			if (propertyDecorators.containsKey(propertyName)) {
				JSONAware jsonAware = propertyDecorators.get(propertyName).decorate(propertyName, nodeRef, value);
				if (jsonAware != null) {
					return jsonAware;
				}
			} else {
				// Built-in data type processing
				if (value instanceof Date) {
					JSONObject dateObj = new JSONObject();
					dateObj.put("value", JSONObject.escape(value.toString()));
					dateObj.put("iso8601", JSONObject.escape(ISO8601DateFormat.format((Date) value)));
					return dateObj;
				} else if (value instanceof List) {
					// Convert the List to a JSON list by recursively calling
					// propertyToJSON
					List<Object> jsonList = new ArrayList<Object>(((List<Serializable>) value).size());
					for (Serializable listItem : (List<Serializable>) value) {
						jsonList.add(propertyToJSON(nodeRef, propertyName, key, listItem));
					}
					return jsonList;
				} else if (value instanceof Double) {
					return (Double.isInfinite((Double) value) || Double.isNaN((Double) value) ? null : value.toString());
				} else if (value instanceof Float) {
					return (Float.isInfinite((Float) value) || Float.isNaN((Float) value) ? null : value.toString());
				} else {
					return value.toString();
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param nodeRef
	 * @param map
	 * @param useShortQNames
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	protected JSONObject propertiesToJSON(NodeRef nodeRef, Map<QName, Serializable> properties, boolean useShortQNames) {
		JSONObject propertiesJSON = new JSONObject();

		for (QName propertyName : properties.keySet()) {
			try {
				String key = nameToString(propertyName, useShortQNames);
				Serializable value = properties.get(propertyName);

				propertiesJSON.put(key, propertyToJSON(nodeRef, propertyName, key, value));
			} catch (NamespaceException ne) {
				// ignore properties that do not have a registered namespace
				if (logger.isDebugEnabled())
					logger.debug("Ignoring property '" + propertyName + "' as its namespace is not registered");
			}
		}

		return propertiesJSON;
	}

}
