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

public class ECMModel {

	public static final String ECM_URI = "http://www.bcpg.fr/model/ecm/1.0";

	public static final String ECM_PREFIX = "ecm";

	public static final QName MODEL = QName.createQName(ECM_URI, "ecmmodel");
	
	//eco
	public static final QName TYPE_ECO = QName.createQName(ECM_URI,
			"changeOrder");
	public static final QName PROP_ECO_STATE = QName.createQName(ECM_URI,
			"ecoState");
	public static final QName PROP_ECO_TYPE = QName.createQName(ECM_URI,
			"ecoType");
	public static final QName ASSOC_CALCULATED_CHARACTS = QName.createQName(ECM_URI,
			"calculatedCharacts");	

	//replacementList
	public static final QName TYPE_REPLACEMENTLIST = QName.createQName(ECM_URI,
			"replacementList");
	public static final QName PROP_REVISION = QName.createQName(ECM_URI,
			"rlRevisionType");
	public static final QName ASSOC_SOURCE_ITEMS = QName.createQName(ECM_URI,
			"rlSourceItems");
	public static final QName ASSOC_TARGET_ITEM = QName.createQName(ECM_URI,
			"rlTargetItem");
	
	public static final QName PROP_QTY_PERCENT = QName.createQName(ECM_URI,
			"rlQtyPerc");
	
	//wUsedList
	public static final QName TYPE_WUSEDLIST = QName.createQName(ECM_URI,
			"wUsedList");
	public static final QName PROP_WUL_IMPACTED_DATALIST = QName.createQName(ECM_URI,
			"impactedDataList");
	public static final QName PROP_WUL_IS_WUSED_IMPACTED = QName.createQName(ECM_URI,
			"isWUsedImpacted");	
	public static final QName ASSOC_WUL_LINK = QName.createQName(ECM_URI,
			"wulLink");
	public static final QName ASSOC_WUL_SOURCE_ITEMS = QName.createQName(ECM_URI,
			"wulSourceItems");
	
	//simulationList
	public static final QName TYPE_CALCULATEDCHARACTLIST = QName.createQName(ECM_URI,
			"calculatedCharactList");
	public static final QName PROP_CCL_SOURCE_VALUE = QName.createQName(ECM_URI,
			"cclSourceValue");
	public static final QName PROP_CCL_TARGET_VALUE = QName.createQName(ECM_URI,
			"cclTargetValue");	
	public static final QName ASSOC_CCL_SOURCE_ITEM = QName.createQName(ECM_URI,
			"cclSourceItem");
	public static final QName ASSOC_CCL_CHARACT = QName.createQName(ECM_URI,
			"cclCharact");
	
	//changeUnitList
	public static final QName TYPE_CHANGEUNITLIST = QName.createQName(ECM_URI,
			"changeUnitList");
	public static final QName PROP_CUL_REVISION = QName.createQName(ECM_URI,
			"culRevision");
	public static final QName PROP_CUL_REQ_TYPE = QName.createQName(ECM_URI,
			"culReqType");
	public static final QName PROP_CUL_REQ_DETAILS = QName.createQName(ECM_URI,
			"culReqDetails");
	public static final QName PROP_CUL_TREATED = QName.createQName(ECM_URI,
			"culTreated");	
	public static final QName ASSOC_CUL_SOURCE_ITEMS = QName.createQName(ECM_URI,
			"culSourceItems");
	public static final QName ASSOC_CUL_TARGET_ITEM = QName.createQName(ECM_URI,
			"culTargetItem");	
	public static final QName ASSOC_CUL_SIMULATION_ITEM = QName.createQName(ECM_URI,
			"culSimulationItem");	
	
	
	
}
