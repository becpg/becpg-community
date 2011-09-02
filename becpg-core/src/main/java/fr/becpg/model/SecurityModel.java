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
	static final QName MODEL = QName.createQName(SECURITY_URI, "secmodel");
	
	static final QName TYPE_ACL_ENTRY = QName.createQName(SECURITY_URI,
	"aclEntry");
	
	static final QName TYPE_ACL_GROUP = QName.createQName(SECURITY_URI,
			"aclGroup");
	
		
}
