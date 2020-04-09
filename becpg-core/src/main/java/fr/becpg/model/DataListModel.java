/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * The Interface DataListModel.
 *
 * @author querephi
 */
public final class DataListModel {

	public final static String MODEL_1_0_URI = "http://www.alfresco.org/model/datalist/1.0";

	public final static String MODEL_PREFIX = "dl";

	public final static QName TYPE_DATALIST = QName.createQName(MODEL_1_0_URI, "dataList");

	public final static QName PROP_DATALISTITEMTYPE = QName.createQName(MODEL_1_0_URI, "dataListItemType");

}
