/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Interface DataListModel.
 *
 * @author querephi
 */
public interface DataListModel {
	
	/** DataList Model URI. */
	static final String MODEL_1_0_URI = "http://www.alfresco.org/model/datalist/1.0";	
	
	/** DataList Prefix. */
	static final String MODEL_PREFIX = "dl";
	
	/** The Constant TYPE_DATALIST. */
	static final QName TYPE_DATALIST = QName.createQName(MODEL_1_0_URI, "dataList");
	
	/** The Constant PROP_DATALISTITEMTYPE. */
	static final QName PROP_DATALISTITEMTYPE = QName.createQName(MODEL_1_0_URI, "dataListItemType");

}
