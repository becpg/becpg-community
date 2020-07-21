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

	/** Constant <code>MODEL_1_0_URI="http://www.alfresco.org/model/datalist/"{trunked}</code> */
	public final static String MODEL_1_0_URI = "http://www.alfresco.org/model/datalist/1.0";

	/** Constant <code>MODEL_PREFIX="dl"</code> */
	public final static String MODEL_PREFIX = "dl";

	/** Constant <code>TYPE_DATALIST</code> */
	public final static QName TYPE_DATALIST = QName.createQName(MODEL_1_0_URI, "dataList");

	/** Constant <code>PROP_DATALISTITEMTYPE</code> */
	public final static QName PROP_DATALISTITEMTYPE = QName.createQName(MODEL_1_0_URI, "dataListItemType");

}
