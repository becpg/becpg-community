/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * The Interface DataListModel.
 *
 * @author querephi
 * @version $Id: $Id
 */
public final class DataListModel {

	private DataListModel() {
		//Do Nothing
	}

	/** Constant <code>MODEL_1_0_URI="http://www.alfresco.org/model/datalist/"{trunked}</code> */
	public static final String MODEL_1_0_URI = "http://www.alfresco.org/model/datalist/1.0";

	/** Constant <code>MODEL_PREFIX="dl"</code> */
	public static final String MODEL_PREFIX = "dl";

	/** Constant <code>TYPE_DATALIST</code> */
	public static final QName TYPE_DATALIST = QName.createQName(MODEL_1_0_URI, "dataList");

	/** Constant <code>PROP_DATALISTITEMTYPE</code> */
	public static final QName PROP_DATALISTITEMTYPE = QName.createQName(MODEL_1_0_URI, "dataListItemType");

}
