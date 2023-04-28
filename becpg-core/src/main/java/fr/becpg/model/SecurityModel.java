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
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>SecurityModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class SecurityModel {

	//
	// Namespace
	//

	/** Security Model URI */
	public final static String SECURITY_URI = "http://www.bcpg.fr/model/security/1.0";

	/** Security Model Prefix */
	public final static String SECURITY_PREFIX = "sec";

	//
	// Security Model Definitions
	//
	/** Constant <code>MODEL</code> */
	public final static QName MODEL = QName.createQName(SECURITY_URI, "secmodel");

	/** Constant <code>TYPE_ACL_ENTRY</code> */
	public final static QName TYPE_ACL_ENTRY = QName.createQName(SECURITY_URI, "aclEntry");

	/** Constant <code>TYPE_ACL_GROUP</code> */
	public final static QName TYPE_ACL_GROUP = QName.createQName(SECURITY_URI, "aclGroup");

	/** Constant <code>PROP_ACL_GROUP_NODE_TYPE</code> */
	public final static QName PROP_ACL_GROUP_NODE_TYPE = QName.createQName(SECURITY_URI, "nodeType");
	
	/** Constant <code>PROP_ACL_GROUP_NODE_TYPE</code> */
	public final static QName PROP_ACL_GROUP_IS_LOCAL_PERMISSION= QName.createQName(SECURITY_URI, "isLocalPermission");

	/** Constant <code>ASSOC_GROUPS_ASSIGNEE</code> */
	public final static QName ASSOC_GROUPS_ASSIGNEE = QName.createQName(SECURITY_URI, "groupsAssignee");

	/** Constant <code>PROP_ACL_PROPNAME</code> */
	public final static QName PROP_ACL_PROPNAME = QName.createQName(SECURITY_URI, "propName");

	/** Constant <code>PROP_ACL_PERMISSION</code> */
	public final static QName PROP_ACL_PERMISSION = QName.createQName(SECURITY_URI, "aclPermission");

	/** Constant <code>ASPECT_SECURITY</code> */
	public static final QName ASPECT_SECURITY= QName.createQName(SECURITY_URI, "securityAspect");
	
	/** Constant <code>ASSOC_SECURITY_REF</code> */
	public static final QName ASSOC_SECURITY_REF= QName.createQName(SECURITY_URI, "securityRef");
	
	public static final QName ASSOC_READ_GROUPS = QName.createQName(SECURITY_URI, "readGroups");
	public static final QName ASSOC_LIST_ITEM_READ_GROUPS = QName.createQName(SECURITY_URI, "listItemReadGroups");
	public static final QName ASSOC_LIST_ITEM_WRITE_GROUPS = QName.createQName(SECURITY_URI, "listItemWriteGroups");
}
