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
 * <p>QualityModel interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface QualityModel {

	//
	// Namespace
	//

	/** Quality Model URI */
	String QUALITY_URI = "http://www.bcpg.fr/model/quality/1.0";

	/** Quality Model Prefix */
	String QUALITY_PREFIX = "qa";

	//
	// Quality Model Definitions
	//
	/** Constant <code>MODEL</code> */
	QName MODEL = QName.createQName(QUALITY_URI, "qamodel");

	/** non conformity **/
	QName TYPE_NC = QName.createQName(QUALITY_URI, "nc");
	/** Constant <code>PROP_NC_STATE</code> */
	QName PROP_NC_STATE = QName.createQName(QUALITY_URI, "ncState");
	/** Constant <code>PROP_NC_TYPE</code> */
	QName PROP_NC_TYPE = QName.createQName(QUALITY_URI, "ncType");
	/** Constant <code>PROP_NC_COMMENT</code> */
	QName PROP_NC_COMMENT = QName.createQName(QUALITY_URI, "ncComment");
	/** Constant <code>PROP_NC_PRIORITY</code> */
	QName PROP_NC_PRIORITY = QName.createQName(QUALITY_URI, "ncPriority");

	/** Control plan **/
	QName TYPE_CONTROL_PLAN = QName.createQName(QUALITY_URI, "controlPlan");

	/** Quality control **/
	QName TYPE_QUALITY_CONTROL = QName.createQName(QUALITY_URI, "qualityControl");
	

	/** Constant <code>TYPE_BATCH</code> */
	QName TYPE_BATCH = QName.createQName(QUALITY_URI, "batch");

	/** Constant <code>PROP_QC_SAMPLES_COUNTER</code> */
	QName PROP_QC_SAMPLES_COUNTER = QName.createQName(QUALITY_URI, "qcSamplesCounter");
	/** Constant <code>PROP_QC_STATE</code> */
	QName PROP_QC_STATE = QName.createQName(QUALITY_URI, "qcState");
	/** Constant <code>ASSOC_QC_CONTROL_PLANS</code> */
	QName ASSOC_QC_CONTROL_PLANS = QName.createQName(QUALITY_URI, "qcControlPlans");

	/** Work item analysis **/
	QName TYPE_WORK_ITEM_ANALYSIS = QName.createQName(QUALITY_URI, "workItemAnalysis");

	/** control point **/
	QName TYPE_CONTROL_POINT = QName.createQName(QUALITY_URI, "controlPoint");

	/** control step **/
	QName TYPE_CONTROL_STEP = QName.createQName(QUALITY_URI, "controlStep");

	/** control method **/
	QName TYPE_CONTROL_METHOD = QName.createQName(QUALITY_URI, "controlMethod");
	
	/** Constant <code>TYPE_CONTROL_CHARACT</code> */
	QName TYPE_CONTROL_CHARACT = QName.createQName(QUALITY_URI, "controlCharact");
	
	/** Constant <code>PROP_CONTROL_CHARACT_TYPE</code> */
	QName PROP_CONTROL_CHARACT_TYPE = QName.createQName(QUALITY_URI, "controlCharactType");

	/** qualityListItem */
	QName TYPE_QUALITYLIST_ITEM = QName.createQName(QUALITY_URI, "qualityListItem");

	/** controlDefList **/
	QName TYPE_CONTROLDEF_LIST = QName.createQName(QUALITY_URI, "controlDefList");
	/** Constant <code>PROP_CDL_TYPE</code> */
	QName PROP_CDL_TYPE = QName.createQName(QUALITY_URI, "clType");
	/** Constant <code>PROP_CDL_MINI</code> */
	QName PROP_CDL_MINI = QName.createQName(QUALITY_URI, "clMini");
	/** Constant <code>PROP_CDL_MAXI</code> */
	QName PROP_CDL_MAXI = QName.createQName(QUALITY_URI, "clMaxi");
	/** Constant <code>PROP_CDL_REQUIRED</code> */
	QName PROP_CDL_REQUIRED = QName.createQName(QUALITY_URI, "clRequired");
	/** Constant <code>ASSOC_CDL_METHOD</code> */
	QName ASSOC_CDL_METHOD = QName.createQName(QUALITY_URI, "clMethod");
	/** Constant <code>ASSOC_CDL_CHARACTS</code> */
	QName ASSOC_CDL_CHARACTS = QName.createQName(QUALITY_URI, "clCharacts");

	/** control list **/
	QName TYPE_CONTROL_LIST = QName.createQName(QUALITY_URI, "controlList");
	/** Constant <code>PROP_CL_TYPE</code> */
	QName PROP_CL_TYPE = QName.createQName(QUALITY_URI, "clType");
	/** Constant <code>PROP_CL_MINI</code> */
	QName PROP_CL_MINI = QName.createQName(QUALITY_URI, "clMini");
	/** Constant <code>PROP_CL_MAXI</code> */
	QName PROP_CL_MAXI = QName.createQName(QUALITY_URI, "clMaxi");
	/** Constant <code>PROP_CL_REQUIRED</code> */
	QName PROP_CL_REQUIRED = QName.createQName(QUALITY_URI, "clRequired");
	/** Constant <code>PROP_CL_SAMPLE_ID</code> */
	QName PROP_CL_SAMPLE_ID = QName.createQName(QUALITY_URI, "clSampleId");
	/** Constant <code>PROP_CL_VALUE</code> */
	QName PROP_CL_VALUE = QName.createQName(QUALITY_URI, "clValue");
	/** Constant <code>PROP_CL_TARGET</code> */
	QName PROP_CL_TARGET = QName.createQName(QUALITY_URI, "clTarget");
	/** Constant <code>PROP_CL_UNIT</code> */
	QName PROP_CL_UNIT = QName.createQName(QUALITY_URI, "clUnit");
	/** Constant <code>PROP_CL_STATE</code> */
	QName PROP_CL_STATE = QName.createQName(QUALITY_URI, "clState");
	/** Constant <code>ASSOC_CL_METHOD</code> */
	QName ASSOC_CL_METHOD = QName.createQName(QUALITY_URI, "clMethod");
	/** Constant <code>ASSOC_CL_CHARACTS</code> */
	QName ASSOC_CL_CHARACTS = QName.createQName(QUALITY_URI, "clCharacts");

	/** samplingDefList **/
	QName TYPE_SAMPLINGDEF_LIST = QName.createQName(QUALITY_URI, "samplingDefList");
	/** Constant <code>PROP_SDL_QTY</code> */
	QName PROP_SDL_QTY = QName.createQName(QUALITY_URI, "sdlQty");
	/** Constant <code>PROP_SDL_FREQ</code> */
	QName PROP_SDL_FREQ = QName.createQName(QUALITY_URI, "sdlFreq");
	/** Constant <code>PROP_SDL_FREQUNIT</code> */
	QName PROP_SDL_FREQUNIT = QName.createQName(QUALITY_URI, "sdlFreqUnit");

	/** Constant <code>TYPE_BATCH_ALLOCATION_LIST</code> */
	QName TYPE_BATCH_ALLOCATION_LIST = QName.createQName(QUALITY_URI, "batchAllocationList");
	
	/** Constant <code>TYPE_STOCK_LIST</code> */
	QName TYPE_STOCK_LIST = QName.createQName(QUALITY_URI, "stockList");
	/** sampling list **/
	QName TYPE_SAMPLING_LIST = QName.createQName(QUALITY_URI, "samplingList");
	/** Constant <code>PROP_SL_DATETIME</code> */
	QName PROP_SL_DATETIME = QName.createQName(QUALITY_URI, "slDateTime");
	/** Constant <code>PROP_SL_SAMPLE_ID</code> */
	QName PROP_SL_SAMPLE_ID = QName.createQName(QUALITY_URI, "slSampleId");
	/** Constant <code>PROP_SL_SAMPLE_STATE</code> */
	QName PROP_SL_SAMPLE_STATE = QName.createQName(QUALITY_URI, "slSampleState");
	/** Constant <code>ASSOC_SL_CONTROL_POINT</code> */
	QName ASSOC_SL_CONTROL_POINT = QName.createQName(QUALITY_URI, "slControlPoint");
	/** Constant <code>ASSOC_SL_CONTROL_STEP</code> */
	QName ASSOC_SL_CONTROL_STEP = QName.createQName(QUALITY_URI, "slControlStep");

	/** Constant <code>ASPECT_BATCH</code> */
	QName ASPECT_BATCH = QName.createQName(QUALITY_URI, "batchAspect");
	/** Constant <code>PROP_BATCH_ID</code> */
	QName PROP_BATCH_ID = QName.createQName(QUALITY_URI, "batchId");
	/** Constant <code>PROP_ORDER_ID</code> */
	QName PROP_ORDER_ID = QName.createQName(QUALITY_URI, "orderId");
	/** Constant <code>PROP_BATCH_START</code> */
	QName PROP_BATCH_START = QName.createQName(QUALITY_URI, "batchStart");
	/** Constant <code>PROP_BATCH_DURATION</code> */
	QName PROP_BATCH_DURATION = QName.createQName(QUALITY_URI, "batchDuration");
	/** Constant <code>ASSOC_PRODUCT</code> */
	QName ASSOC_PRODUCT = QName.createQName(QUALITY_URI, "product");

	/** Constant <code>ASPECT_CLAIM_TREATEMENT</code> */
	QName ASPECT_CLAIM_TREATEMENT = QName.createQName(QUALITY_URI, "claimTreatementAspect");

	/** Constant <code>PROP_CLAIM_TREATEMENT_DATE</code> */
	QName PROP_CLAIM_TREATEMENT_DATE = QName.createQName(QUALITY_URI, "claimTreatementDate");
	/** Constant <code>PROP_CLAIM_TREATEMENT_DETAILS</code> */
	QName PROP_CLAIM_TREATEMENT_DETAILS = QName.createQName(QUALITY_URI, "claimTreatementDetails");
	/** Constant <code>ASSOC_CLAIM_TREATEMENT_ACTOR</code> */
	QName ASSOC_CLAIM_TREATEMENT_ACTOR = QName.createQName(QUALITY_URI, "claimTreatmentActor");

	/** Constant <code>ASPECT_CLAIM_RESPONSE</code> */
	QName ASPECT_CLAIM_RESPONSE = QName.createQName(QUALITY_URI, "claimResponseAspect");

	/** Constant <code>PROP_CLAIM_RESPONSE_DATE</code> */
	QName PROP_CLAIM_RESPONSE_DATE = QName.createQName(QUALITY_URI, "claimResponseDate");
	/** Constant <code>PROP_CLAIM_RESPONSE_DETAILS</code> */
	QName PROP_CLAIM_RESPONSE_DETAILS = QName.createQName(QUALITY_URI, "claimResponseDetails");
	/** Constant <code>PROP_CLAIM_RESPONSE_STATE</code> */
	QName PROP_CLAIM_RESPONSE_STATE = QName.createQName(QUALITY_URI, "claimResponseState");
	/** Constant <code>ASSOC_CLAIM_RESPONSE_ACTOR</code> */
	QName ASSOC_CLAIM_RESPONSE_ACTOR = QName.createQName(QUALITY_URI, "claimResponseActor");

	/** Constant <code>NC_TYPE_NONCONFORMITY="NonConformity"</code> */
	String NC_TYPE_NONCONFORMITY = "NonConformity";
	/** Constant <code>NC_TYPE_CLAIM="Claim"</code> */
	String NC_TYPE_CLAIM = "Claim";

	/** Constant <code>ASPECT_CLAIM_CLOSING</code> */
	QName ASPECT_CLAIM_CLOSING = QName.createQName(QUALITY_URI, "claimClosingAspect");
	
	/** Constant <code>ASPECT_CLAIM</code> */
	QName ASPECT_CLAIM = QName.createQName(QUALITY_URI, "claimAspect");

	/** Constant <code>ASPECT_CONTROL_LIST</code> */
	QName ASPECT_CONTROL_LIST = QName.createQName(QUALITY_URI, "controlListAspect");



}
