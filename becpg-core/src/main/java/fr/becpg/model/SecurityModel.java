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
	
		
	public static final QName PROP_ACL_GROUP_TYPE_NAME = QName.createQName(SECURITY_URI,
			"typeName");


   public static final QName ASSOC_GROUPS_ASSIGNEE = QName.createQName(SECURITY_URI,
			"groupsAssignee");

	public static final QName PROP_ACL_PROPNAME = QName.createQName(SECURITY_URI,
			"propName");

	public static final QName PROP_ACL_PERMISSION = QName.createQName(SECURITY_URI,
			"aclPermission");
	
}
