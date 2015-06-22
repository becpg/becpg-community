/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
	QName MODEL = QName.createQName(QUALITY_URI, "qamodel");

	/** non conformity **/
	QName TYPE_NC = QName.createQName(QUALITY_URI, "nc");
	QName PROP_NC_STATE = QName.createQName(QUALITY_URI, "ncState");
	QName PROP_NC_TYPE = QName.createQName(QUALITY_URI, "ncType");
	QName PROP_NC_COMMENT = QName.createQName(QUALITY_URI, "ncComment");
	QName PROP_NC_PRIORITY = QName.createQName(QUALITY_URI, "ncPriority");

	/** Control plan **/
	QName TYPE_CONTROL_PLAN = QName.createQName(QUALITY_URI, "controlPlan");

	/** Quality control **/
	QName TYPE_QUALITY_CONTROL = QName.createQName(QUALITY_URI, "qualityControl");

	QName PROP_QC_SAMPLES_COUNTER = QName.createQName(QUALITY_URI, "qcSamplesCounter");
	QName PROP_QC_STATE = QName.createQName(QUALITY_URI, "qcState");
	QName ASSOC_QC_CONTROL_PLANS = QName.createQName(QUALITY_URI, "qcControlPlans");

	/** Work item analysis **/
	QName TYPE_WORK_ITEM_ANALYSIS = QName.createQName(QUALITY_URI, "workItemAnalysis");

	/** control point **/
	QName TYPE_CONTROL_POINT = QName.createQName(QUALITY_URI, "controlPoint");

	/** control step **/
	QName TYPE_CONTROL_STEP = QName.createQName(QUALITY_URI, "controlStep");

	/** control method **/
	QName TYPE_CONTROL_METHOD = QName.createQName(QUALITY_URI, "controlMethod");
	
	QName TYPE_CONTROL_CHARACT = QName.createQName(QUALITY_URI, "controlCharact");

	/** qualityListItem */
	QName TYPE_QUALITYLIST_ITEM = QName.createQName(QUALITY_URI, "qualityListItem");

	/** Work Log **/
	QName TYPE_WORK_LOG = QName.createQName(QUALITY_URI, "workLog");
	QName PROP_WL_STATE = QName.createQName(QUALITY_URI, "wlState");
	QName PROP_WL_COMMENT = QName.createQName(QUALITY_URI, "wlComment");

	/** controlDefList **/
	QName TYPE_CONTROLDEF_LIST = QName.createQName(QUALITY_URI, "controlDefList");
	QName PROP_CDL_TYPE = QName.createQName(QUALITY_URI, "clType");
	QName PROP_CDL_MINI = QName.createQName(QUALITY_URI, "clMini");
	QName PROP_CDL_MAXI = QName.createQName(QUALITY_URI, "clMaxi");
	QName PROP_CDL_REQUIRED = QName.createQName(QUALITY_URI, "clRequired");
	QName ASSOC_CDL_METHOD = QName.createQName(QUALITY_URI, "clMethod");
	QName ASSOC_CDL_CHARACTS = QName.createQName(QUALITY_URI, "clCharacts");

	/** control list **/
	QName TYPE_CONTROL_LIST = QName.createQName(QUALITY_URI, "controlList");
	QName PROP_CL_TYPE = QName.createQName(QUALITY_URI, "clType");
	QName PROP_CL_MINI = QName.createQName(QUALITY_URI, "clMini");
	QName PROP_CL_MAXI = QName.createQName(QUALITY_URI, "clMaxi");
	QName PROP_CL_REQUIRED = QName.createQName(QUALITY_URI, "clRequired");
	QName PROP_CL_SAMPLE_ID = QName.createQName(QUALITY_URI, "clSampleId");
	QName PROP_CL_VALUE = QName.createQName(QUALITY_URI, "clValue");
	QName PROP_CL_TARGET = QName.createQName(QUALITY_URI, "clTarget");
	QName PROP_CL_UNIT = QName.createQName(QUALITY_URI, "clUnit");
	QName PROP_CL_STATE = QName.createQName(QUALITY_URI, "clState");
	QName ASSOC_CL_METHOD = QName.createQName(QUALITY_URI, "clMethod");
	QName ASSOC_CL_CHARACTS = QName.createQName(QUALITY_URI, "clCharacts");

	/** samplingDefList **/
	QName TYPE_SAMPLINGDEF_LIST = QName.createQName(QUALITY_URI, "samplingDefList");
	QName PROP_SDL_QTY = QName.createQName(QUALITY_URI, "sdlQty");
	QName PROP_SDL_FREQ = QName.createQName(QUALITY_URI, "sdlFreq");
	QName PROP_SDL_FREQUNIT = QName.createQName(QUALITY_URI, "sdlFreqUnit");

	/** sampling list **/
	QName TYPE_SAMPLING_LIST = QName.createQName(QUALITY_URI, "samplingList");
	QName PROP_SL_DATETIME = QName.createQName(QUALITY_URI, "slDateTime");
	QName PROP_SL_SAMPLE_ID = QName.createQName(QUALITY_URI, "slSampleId");
	QName PROP_SL_SAMPLE_STATE = QName.createQName(QUALITY_URI, "slSampleState");
	QName ASSOC_SL_CONTROL_POINT = QName.createQName(QUALITY_URI, "slControlPoint");
	QName ASSOC_SL_CONTROL_STEP = QName.createQName(QUALITY_URI, "slControlStep");

	QName ASPECT_BATCH = QName.createQName(QUALITY_URI, "batchAspect");
	QName PROP_BATCH_ID = QName.createQName(QUALITY_URI, "batchId");
	QName PROP_ORDER_ID = QName.createQName(QUALITY_URI, "orderId");
	QName PROP_BATCH_START = QName.createQName(QUALITY_URI, "batchStart");
	QName PROP_BATCH_DURATION = QName.createQName(QUALITY_URI, "batchDuration");
	QName ASSOC_PRODUCT = QName.createQName(QUALITY_URI, "product");

	QName ASPECT_CLAIM_TREATEMENT = QName.createQName(QUALITY_URI, "claimTreatementAspect");

	QName PROP_CLAIM_TREATEMENT_DATE = QName.createQName(QUALITY_URI, "claimTreatementDate");
	QName PROP_CLAIM_TREATEMENT_DETAILS = QName.createQName(QUALITY_URI, "claimTreatementDetails");
	QName ASSOC_CLAIM_TREATEMENT_ACTOR = QName.createQName(QUALITY_URI, "claimTreatmentActor");

	QName ASPECT_CLAIM_RESPONSE = QName.createQName(QUALITY_URI, "claimResponseAspect");

	QName PROP_CLAIM_RESPONSE_DATE = QName.createQName(QUALITY_URI, "claimResponseDate");
	QName PROP_CLAIM_RESPONSE_DETAILS = QName.createQName(QUALITY_URI, "claimResponseDetails");
	QName PROP_CLAIM_RESPONSE_STATE = QName.createQName(QUALITY_URI, "claimResponseState");
	QName ASSOC_CLAIM_RESPONSE_ACTOR = QName.createQName(QUALITY_URI, "claimResponseActor");

	String NC_TYPE_NONCONFORMITY = "NonConformity";
	String NC_TYPE_CLAIM = "Claim";

	QName ASPECT_CLAIM_CLOSING = QName.createQName(QUALITY_URI, "claimClosingAspect");
	
	QName ASPECT_CLAIM = QName.createQName(QUALITY_URI, "claimAspect");


}
