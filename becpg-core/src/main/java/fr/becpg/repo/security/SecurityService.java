/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
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
package fr.becpg.repo.security;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.security.data.PermissionContext;

/**
 * <p>SecurityService interface.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public interface SecurityService {

	/**
	 * Access status
	 */
	int NONE_ACCESS = 0;
	/** Constant <code>READ_ACCESS=1</code> */
	int READ_ACCESS = 1;
	/** Constant <code>WRITE_ACCESS=2</code> */
	int WRITE_ACCESS = 2;
	
	/**
	 * Compute access mode for the given field name on a specific type
	 *
	 * @return Access Mode status
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 * @param name a {@link java.lang.String} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	int computeAccessMode(NodeRef nodeRef, QName nodeType, String name);
	
	/**
	 * Compute access mode for the given field name on a specific type
	 *
	 * @return Access Mode status
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 * @param name a {@link org.alfresco.service.namespace.QName} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	int computeAccessMode(NodeRef nodeRef, QName nodeType, QName name);
	
	/**
	 * The fields of a type a security rule is set on, among the rules that apply to the given node
	 * (its local rules, or the global ones).
	 * <p>
	 * A field outside this set is readable whoever asks, which lets a caller reading many fields
	 * evaluate {@link #computeAccessMode(NodeRef, QName, String)} for the ruled ones only.
	 *
	 * @param nodeRef the node whose rules apply, or null for the global rules
	 * @param nodeType the type the rules are set on
	 * @return the names the rules of the type are set on (prefixed fields and lists, or entries such
	 *         as <code>View-documents</code>), empty when the type has no rule
	 */
	Set<String> getFieldsWithRules(NodeRef nodeRef, QName nodeType);

	/**
	 * Refresh ACLS cache per tenant
	 */
	void refreshAcls();

	/**
	 * Extract props list based on existing ACL_GROUPS
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<String> getAvailablePropNames();

	/**
	 * Check user is in currentSecurityGroup or isAdmin
	 *
	 * @param securityGroup a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean isCurrentUserAllowed(String securityGroup);

	/**
	 * List available security roles for user
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<String> getUserSecurityRoles();
	
	/**
	 * Get the permission context for a given node and a given property
	 *
	 * @return a {@link java.util.List} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object
	 * @param propName a {@link java.lang.String} object
	 */
	PermissionContext getPermissionContext(NodeRef nodeRef, QName nodeType, String propName);
}
