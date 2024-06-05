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
 * <p>ECMModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ECMModel {

	/** Constant <code>ECM_URI="http://www.bcpg.fr/model/ecm/1.0"</code> */
	public static final String ECM_URI = "http://www.bcpg.fr/model/ecm/1.0";

	/** Constant <code>ECM_PREFIX="ecm"</code> */
	public static final String ECM_PREFIX = "ecm";

	/** Constant <code>MODEL</code> */
	public static final QName MODEL = QName.createQName(ECM_URI, "ecmmodel");
	
	//eco
	/** Constant <code>TYPE_ECO</code> */
	public static final QName TYPE_ECO = QName.createQName(ECM_URI,
			"changeOrder");
	/** Constant <code>PROP_ECO_STATE</code> */
	public static final QName PROP_ECO_STATE = QName.createQName(ECM_URI,
			"ecoState");
	/** Constant <code>PROP_ECO_TYPE</code> */
	public static final QName PROP_ECO_TYPE = QName.createQName(ECM_URI,
			"ecoType");
	/** Constant <code>ASSOC_CALCULATED_CHARACTS</code> */
	public static final QName ASSOC_CALCULATED_CHARACTS = QName.createQName(ECM_URI,
			"calculatedCharacts");	

	//replacementList
	/** Constant <code>TYPE_REPLACEMENTLIST</code> */
	public static final QName TYPE_REPLACEMENTLIST = QName.createQName(ECM_URI,
			"replacementList");
	/** Constant <code>PROP_REVISION</code> */
	public static final QName PROP_REVISION = QName.createQName(ECM_URI,
			"rlRevisionType");
	/** Constant <code>ASSOC_SOURCE_ITEMS</code> */
	public static final QName ASSOC_SOURCE_ITEMS = QName.createQName(ECM_URI,
			"rlSourceItems");
	/** Constant <code>ASSOC_TARGET_ITEM</code> */
	public static final QName ASSOC_TARGET_ITEM = QName.createQName(ECM_URI,
			"rlTargetItem");
	
	/** Constant <code>PROP_QTY_PERCENT</code> */
	public static final QName PROP_QTY_PERCENT = QName.createQName(ECM_URI,
			"rlQtyPerc");
	
	//wUsedList
	/** Constant <code>TYPE_WUSEDLIST</code> */
	public static final QName TYPE_WUSEDLIST = QName.createQName(ECM_URI,
			"wUsedList");
	/** Constant <code>PROP_WUL_IMPACTED_DATALIST</code> */
	public static final QName PROP_WUL_IMPACTED_DATALIST = QName.createQName(ECM_URI,
			"impactedDataList");
	/** Constant <code>PROP_WUL_IS_WUSED_IMPACTED</code> */
	public static final QName PROP_WUL_IS_WUSED_IMPACTED = QName.createQName(ECM_URI,
			"isWUsedImpacted");	
	/** Constant <code>ASSOC_WUL_LINK</code> */
	public static final QName ASSOC_WUL_LINK = QName.createQName(ECM_URI,
			"wulLink");
	/** Constant <code>ASSOC_WUL_SOURCE_ITEMS</code> */
	public static final QName ASSOC_WUL_SOURCE_ITEMS = QName.createQName(ECM_URI,
			"wulSourceItems");
	
	//simulationList
	/** Constant <code>TYPE_CALCULATEDCHARACTLIST</code> */
	public static final QName TYPE_CALCULATEDCHARACTLIST = QName.createQName(ECM_URI,
			"calculatedCharactList");
	/** Constant <code>PROP_CCL_SOURCE_VALUE</code> */
	public static final QName PROP_CCL_SOURCE_VALUE = QName.createQName(ECM_URI,
			"cclSourceValue");
	/** Constant <code>PROP_CCL_TARGET_VALUE</code> */
	public static final QName PROP_CCL_TARGET_VALUE = QName.createQName(ECM_URI,
			"cclTargetValue");	
	/** Constant <code>ASSOC_CCL_SOURCE_ITEM</code> */
	public static final QName ASSOC_CCL_SOURCE_ITEM = QName.createQName(ECM_URI,
			"cclSourceItem");
	/** Constant <code>ASSOC_CCL_CHARACT</code> */
	public static final QName ASSOC_CCL_CHARACT = QName.createQName(ECM_URI,
			"cclCharact");
	
	//changeUnitList
	/** Constant <code>TYPE_CHANGEUNITLIST</code> */
	public static final QName TYPE_CHANGEUNITLIST = QName.createQName(ECM_URI,
			"changeUnitList");
	/** Constant <code>PROP_CUL_REVISION</code> */
	public static final QName PROP_CUL_REVISION = QName.createQName(ECM_URI,
			"culRevision");
	/** Constant <code>PROP_CUL_REQ_TYPE</code> */
	public static final QName PROP_CUL_REQ_TYPE = QName.createQName(ECM_URI,
			"culReqType");
	/** Constant <code>PROP_CUL_REQ_DETAILS</code> */
	public static final QName PROP_CUL_REQ_DETAILS = QName.createQName(ECM_URI,
			"culReqDetails");
	/** Constant <code>PROP_CUL_TREATED</code> */
	public static final QName PROP_CUL_TREATED = QName.createQName(ECM_URI,
			"culTreated");	
	/** Constant <code>ASSOC_CUL_TARGET_ITEM</code> */
	public static final QName ASSOC_CUL_TARGET_ITEM = QName.createQName(ECM_URI,
			"culTargetItem");	
	
	/** Constant <code>ASPECT_CHANGE_ORDER</code> */
	public static final QName ASPECT_CHANGE_ORDER = QName.createQName(ECM_URI, "changeOrderAspect");
	
	/** Constant <code>ASSOC_CHANGE_ORDER_REF</code> */
	public static final QName ASSOC_CHANGE_ORDER_REF = QName.createQName(ECM_URI, "changeOrderRef");
	
	
	
}
