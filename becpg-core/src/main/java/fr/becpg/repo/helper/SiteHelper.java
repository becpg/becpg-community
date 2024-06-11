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
package fr.becpg.repo.helper;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * <p>SiteHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SiteHelper {

	/** Constant <code>SITES_SPACE_QNAME_PATH="/app:company_home/st:sites/"</code> */
	public static final String SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";

	/**
	 * <p>extractContainerId.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String extractContainerId(String path) {

		String containerId = null;
		if (isSitePath(path)) {
			String tmp = path.substring(SITES_SPACE_QNAME_PATH.length());
			int pos = tmp.indexOf('/');
			if (pos >= 1) {
				tmp = tmp.substring(pos + 1);
				pos = tmp.indexOf('/');
				if (pos >= 1) {
					// strip container id from the path
					containerId = tmp.substring(0, pos);
					containerId = containerId.substring(containerId.indexOf(":") + 1);
				}
			}
		}

		return containerId;
	}

	/**
	 * <p>isSitePath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isSitePath(String path) {
		boolean isSitePath = false;

		if (path.startsWith(SITES_SPACE_QNAME_PATH)) {
			isSitePath = true;
		}

		return isSitePath;
	}

	/**
	 * <p>extractSiteId.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String extractSiteId(String path) {
		String siteId = null;
		if (isSitePath(path)) {

			String tmp = path.substring(SITES_SPACE_QNAME_PATH.length());
			int pos = tmp.indexOf(':');
			if (pos > 0) {
				siteId = tmp.split("/")[0];
				siteId = siteId.split(":")[1];
			}
		}
		return siteId;

	}

	/**
	 * <p>extractDisplayPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @param displayPath a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String extractDisplayPath(String path, String displayPath) {
		String ret = "";

		if (isSitePath(path)) {
			String[] splitted = displayPath.split("/");

			for (int i = Math.min(5, splitted.length); i < splitted.length; i++) {
				if (ret.length() > 0) {
					ret += "/";
				}
				ret += splitted[i];
			}

		} else {
			ret = displayPath;
		}

		return ret;
	}
	
	/**
	 * <p>extractSiteDisplayPath.</p>
	 *
	 *
	 * Extracts the display path out of a path but from the site name, and without "documentLibrary".
	 *
	 * If there no site in path, the result is just the display path
	 *
	 * @param path a {@link org.alfresco.service.cmr.repository.Path} object
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 * @return a {@link java.lang.String} object
	 */
	public static String extractSiteDisplayPath(Path path, PermissionService permissionService, NodeService nodeService, NamespaceService namespaceService) {

		StringBuilder buf = new StringBuilder(64);

		boolean isSite = false;
		boolean isSiteChild = false;
		
		for (int i = 0; i < path.size() - 1; i++) {
			String elementString = null;
			Element element = path.get(i);
			
			if (isSite) {
				isSiteChild = true;
				isSite = false;
			}
			
			if (element instanceof ChildAssocElement) {
				ChildAssociationRef elementRef = ((ChildAssocElement) element).getRef();
				if (elementRef.getParentRef() != null) {
					Serializable nameProp = null;
					if (permissionService.hasPermission(elementRef.getChildRef(), PermissionService.READ) == AccessStatus.ALLOWED) {
						
						if (SiteModel.TYPE_SITE.equals(nodeService.getType(elementRef.getChildRef()))) {
							nameProp = nodeService.getProperty(elementRef.getChildRef(), ContentModel.PROP_TITLE);
							isSite = true;
						}

						if (nameProp == null) {
							nameProp = nodeService.getProperty(elementRef.getChildRef(), ContentModel.PROP_NAME);
						}

						// use the name property if we are allowed access to it
						elementString = nameProp.toString();
					} else {
						// revert to using QName if not
						elementString = elementRef.getQName().getLocalName();
					}
				}
			} else {
				elementString = element.getElementString();
			}
			
			if (!isSiteChild && elementString != null) {
				buf.append("/");
				buf.append(elementString);
			} else if (isSiteChild) {
				isSiteChild = !isSiteChild;
			}
		}
		
		String ret = "";
		
		String displayPath = buf.toString();
		
		if (isSitePath(path.toPrefixString(namespaceService))) {
			String[] splitted = displayPath.split("/");

			for (int i = Math.min(3, splitted.length); i < splitted.length; i++) {
				if (ret.length() > 0) {
					ret += "/";
				}
				ret += splitted[i];
			}

		} else {
			ret = displayPath;
		}

		return ret;

	}

}
