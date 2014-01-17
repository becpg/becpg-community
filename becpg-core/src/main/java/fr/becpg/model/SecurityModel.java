/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

public interface SecurityModel {

	//
	// Namespace
	//
	
	/** Security Model URI */
	public static final String SECURITY_URI = "http://www.bcpg.fr/model/security/1.0";

	/** Security Model Prefix */
	public static final String SECURITY_PREFIX = "sec";
	

	//
	// Security Model Definitions
	//
	public static final QName MODEL = QName.createQName(SECURITY_URI, "secmodel");
	
	
	public static final QName TYPE_ACL_ENTRY = QName.createQName(SECURITY_URI,
	"aclEntry");
	
	public static final QName TYPE_ACL_GROUP = QName.createQName(SECURITY_URI,
			"aclGroup");
	
		
	public static final QName PROP_ACL_GROUP_NODE_TYPE = QName.createQName(SECURITY_URI,
			"nodeType");


   public static final QName ASSOC_GROUPS_ASSIGNEE = QName.createQName(SECURITY_URI,
			"groupsAssignee");

	public static final QName PROP_ACL_PROPNAME = QName.createQName(SECURITY_URI,
			"propName");

	public static final QName PROP_ACL_PERMISSION = QName.createQName(SECURITY_URI,
			"aclPermission");

}
