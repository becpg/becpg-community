package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public interface QualityModel {

	//
	// Namespace
	//
	
	/** Quality Model URI */
	public static final String QUALITY_URI = "http://www.bcpg.fr/model/quality/1.0";

	/** Quality Model Prefix */
	public static final String QUALITY_PREFIX = "qa";
	
	//
	// Quality Model Definitions
	//
	static final QName MODEL = QName.createQName(QUALITY_URI, "qamodel");
	
	/** non conformity **/
	static final QName TYPE_NC = QName.createQName(QUALITY_URI,
	"nc");
	static final QName PROP_NC_DETECTED = QName.createQName(QUALITY_URI,
	"ncDetected");	
		
	/** Control plan **/
	static final QName TYPE_CONTROL_PLAN = QName.createQName(QUALITY_URI,
	"controlPlan");
	
	/** Quality control **/
	static final QName TYPE_QUALITY_CONTROL = QName.createQName(QUALITY_URI,
	"qualityControl");
	static final QName PROP_QC_BATCH_START = QName.createQName(QUALITY_URI,
	"batchStart");
	static final QName PROP_QC_BATCH_DURATION = QName.createQName(QUALITY_URI,
	"batchDuration");
	static final QName PROP_QC_SAMPLES_COUNTER = QName.createQName(QUALITY_URI,
	"qcSamplesCounter");
	static final QName PROP_QC_STATE = QName.createQName(QUALITY_URI,
	"qcState");
	static final QName PROP_QC_BATCH_ID = QName.createQName(QUALITY_URI,
	"batchId");
	static final QName PROP_QC_ORDER_ID = QName.createQName(QUALITY_URI,
	"orderId");	
	static final QName ASSOC_QC_PRODUCT = QName.createQName(QUALITY_URI,
	"product");
	static final QName ASSOC_QC_CONTROL_PLANS = QName.createQName(QUALITY_URI,
	"qcControlPlans");
	
	/** Corrective action **/
	static final QName ASPECT_CORRECTIVE_ACTION = QName.createQName(QUALITY_URI,
			"acAspect");
	static final QName PROP_AC_EXPECTED_DATE = QName.createQName(QUALITY_URI,
			"acExpectedDate");
	static final QName PROP_AC_EFFECTIVE_DATE = QName.createQName(QUALITY_URI,
			"acEffectiveDate");
	static final QName PROP_AC_ASSIGNED_TO = QName.createQName(QUALITY_URI,
			"acAssignedTo");
	static final QName PROP_AC_CHECKER = QName.createQName(QUALITY_URI,
			"acChecker");
	
	
	/** Work item analysis **/
	static final QName TYPE_WORK_ITEM_ANALYSIS = QName.createQName(QUALITY_URI,
	"workItemAnalysis");
	
	/** control point **/
	static final QName TYPE_CONTROL_POINT = QName.createQName(QUALITY_URI,
	"controlPoint");
	
	/** control step**/
	static final QName TYPE_CONTROL_STEP = QName.createQName(QUALITY_URI,
	"controlStep");
	
	/** control method **/
	static final QName TYPE_CONTROL_METHOD = QName.createQName(QUALITY_URI,
	"controlMethod");
	
	/** qualityListItem */
	static final QName TYPE_QUALITYLIST_ITEM = QName.createQName(QUALITY_URI,
	"qualityListItem");
	
	/** controlDefList **/
	static final QName TYPE_CONTROLDEF_LIST = QName.createQName(QUALITY_URI,
	"controlDefList");
	static final QName PROP_CDL_TYPE = QName.createQName(QUALITY_URI,
	"clType");
	static final QName PROP_CDL_MINI = QName.createQName(QUALITY_URI,
	"clMini");
	static final QName PROP_CDL_MAXI = QName.createQName(QUALITY_URI,
	"clMaxi");
	static final QName PROP_CDL_REQUIRED = QName.createQName(QUALITY_URI,
	"clRequired");
	static final QName ASSOC_CDL_METHOD = QName.createQName(QUALITY_URI,
	"clMethod");
	static final QName ASSOC_CDL_CHARACTS = QName.createQName(QUALITY_URI,
	"clCharacts");	
	
	/** control list **/
	static final QName TYPE_CONTROL_LIST = QName.createQName(QUALITY_URI,
	"controlList");
	static final QName PROP_CL_TYPE = QName.createQName(QUALITY_URI,
	"clType");
	static final QName PROP_CL_MINI = QName.createQName(QUALITY_URI,
	"clMini");
	static final QName PROP_CL_MAXI = QName.createQName(QUALITY_URI,
	"clMaxi");
	static final QName PROP_CL_REQUIRED = QName.createQName(QUALITY_URI,
	"clRequired");
	static final QName PROP_CL_SAMPLE_ID = QName.createQName(QUALITY_URI,
	"clSampleId");
	static final QName PROP_CL_VALUE = QName.createQName(QUALITY_URI,
	"clValue");
	static final QName PROP_CL_TARGET = QName.createQName(QUALITY_URI,
	"clTarget");
	static final QName PROP_CL_UNIT = QName.createQName(QUALITY_URI,
	"clUnit");
	static final QName PROP_CL_STATE = QName.createQName(QUALITY_URI,
	"clState");		
	static final QName ASSOC_CL_METHOD = QName.createQName(QUALITY_URI,
	"clMethod");
	static final QName ASSOC_CL_CHARACTS = QName.createQName(QUALITY_URI,
	"clCharacts");
	
	/** samplingDefList **/
	static final QName TYPE_SAMPLINGDEF_LIST = QName.createQName(QUALITY_URI,
	"samplingDefList");
	static final QName PROP_SDL_QTY = QName.createQName(QUALITY_URI,
	"sdlQty");
	static final QName PROP_SDL_FREQ = QName.createQName(QUALITY_URI,
	"sdlFreq");
	static final QName PROP_SDL_FREQUNIT = QName.createQName(QUALITY_URI,
	"sdlFreqUnit");
	static final QName ASSOC_SDL_CONTROL_POINT = QName.createQName(QUALITY_URI,
	"sdlControlPoint");
	static final QName ASSOC_SDL_CONTROL_STEP = QName.createQName(QUALITY_URI,
	"sdlControlStep");
	static final QName ASSOC_SDL_CONTROLING_GROUP = QName.createQName(QUALITY_URI,
	"sdlControlingGroup");	
	
	/** sampling list **/
	static final QName TYPE_SAMPLING_LIST = QName.createQName(QUALITY_URI,
	"samplingList");
	static final QName PROP_SL_DATETIME = QName.createQName(QUALITY_URI,
	"slDateTime");
	static final QName PROP_SL_SAMPLE_ID = QName.createQName(QUALITY_URI,
	"slSampleId");
	static final QName PROP_SL_SAMPLE_STATE = QName.createQName(QUALITY_URI,
	"slSampleState");
	static final QName ASSOC_SL_CONTROL_POINT = QName.createQName(QUALITY_URI,
	"slControlPoint");
	static final QName ASSOC_SL_CONTROL_STEP = QName.createQName(QUALITY_URI,
	"slControlStep");		
}
