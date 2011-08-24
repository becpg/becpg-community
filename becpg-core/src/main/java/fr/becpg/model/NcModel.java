/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * beCPG Non conformity model definition.
 *
 * @author Matthieu Laborie
 */
public interface NcModel {

	//
	// Namespace
	//

	// Non conformity Model URI
	/** The Constant NC_URI. */
	public static final String NC_URI = "http://www.bcpg.fr/model/nc/1.0";

	// Non conformity Model Prefix
	/** The Constant NC_PREFIX. */
	public static final String NC_PREFIX = "nc";

	//
	// Product Model Definitions
	//
	/** The Constant MODEL. */
	static final QName MODEL = QName.createQName(NC_URI, "ncmodel");

	// productTemplate
	/** The Constant TYPE_NC. */
	static final QName TYPE_NC = QName.createQName(NC_URI,
			"nc");
	
}
