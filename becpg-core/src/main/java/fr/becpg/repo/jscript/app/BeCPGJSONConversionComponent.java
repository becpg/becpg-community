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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.jscript.app;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.app.JSONConversionComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.URLEncoder;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>BeCPGJSONConversionComponent class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGJSONConversionComponent extends JSONConversionComponent {

	private static final String CREATE_CHILDREN = "CreateChildren";

	private AssociationService associationService;

	private SecurityService securityService;
	
	private static final String REPORT_DOWNLOAD_API_URL = "becpg/report/node/{0}/{1}/{2}/content/{3}";


	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	private static final Log logger = LogFactory.getLog(BeCPGJSONConversionComponent.class);

	/** Registered decorators */
	protected final Map<QName, AssociationDecorator> associationDecorators = new HashMap<>(3);

	/**
	 * Register a property decorator;
	 *
	 * @param associationDecorator a {@link fr.becpg.repo.jscript.app.AssociationDecorator} object.
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
	 * {@inheritDoc}
	 *
	 * Convert a node reference to a JSON string. Selects the correct converter
	 * based on selection implementation.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String toJSON(NodeRef nodeRef, boolean useShortQNames) {
		JSONObject json = new JSONObject();

		if (this.nodeService.exists(nodeRef)) {
			if (publicServiceAccessService.hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "getProperties", nodeRef) == AccessStatus.ALLOWED) {
				// init namespace prefix cache
				namespacePrefixCache.get().clear();

				// Get node info
				FileInfo nodeInfo = this.fileFolderService.getFileInfo(nodeRef);

				// Set root values
				setRootValues(nodeInfo, json, useShortQNames);
				
				if (ReportModel.TYPE_REPORT.equals(nodeService.getType(nodeRef))) {
					json.put("contentURL", MessageFormat.format(
							REPORT_DOWNLOAD_API_URL,
	                                nodeRef.getStoreRef().getProtocol(),
	                                nodeRef.getStoreRef().getIdentifier(),
	                                nodeRef.getId(),
	                                URLEncoder.encode(nodeInfo.getName())));
				}

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

		return json.toJSONString();
	}

	/**
	 * <p>associationsToJSON.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param useShortQNames a boolean.
	 * @return a {@link org.json.simple.JSONObject} object.
	 */
	@SuppressWarnings("unchecked")
	protected JSONObject associationsToJSON(NodeRef nodeRef, boolean useShortQNames) {
		JSONObject assocsToJSON = new JSONObject();

		for (QName assoc : associationDecorators.keySet()) {
			if (associationDecorators.get(assoc) != null) {
				if (nodeService.hasAspect(nodeRef, associationDecorators.get(assoc).getAspect())) {
					try {

						String key = qnameToString(assoc, useShortQNames);
						if (logger.isDebugEnabled()) {
							logger.debug("look for assoc decorator: " + assoc.toPrefixString(namespaceService));
						}

						assocsToJSON.put(key,
								associationDecorators.get(assoc).decorate(assoc, nodeRef, associationService.getTargetAssocs(nodeRef, assoc)));
					} catch (NamespaceException ne) {
						// ignore properties that do not have a registered
						// namespace
						if (logger.isDebugEnabled()) {
							logger.debug("Ignoring assoc '" + assoc + "' as its namespace is not registered");
						}
					}
				}
			}
		}

		return assocsToJSON;
	}

	/**
	 * Convert a qname to a string - either full or short prefixed named.
	 *
	 */
	private String qnameToString(final QName qname, final boolean isShortName) {
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

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject userPermissionsToJSON(final NodeRef nodeRef) {

		final JSONObject userPermissionJSON = super.userPermissionsToJSON(nodeRef);
		if (userPermissionJSON.containsKey(CREATE_CHILDREN) && "true".equals(userPermissionJSON.get(CREATE_CHILDREN).toString())) {
			userPermissionJSON.put(CREATE_CHILDREN, securityService.computeAccessMode(nodeRef, nodeService.getType(nodeRef), "View-documents") == SecurityService.WRITE_ACCESS);
		}
		for (String userPermission : securityService.getUserSecurityRoles()) {
			userPermissionJSON.put(userPermission, true);
		}
		return userPermissionJSON;
	}

}
