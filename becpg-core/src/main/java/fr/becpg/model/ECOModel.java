package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class ECOModel {

	public static final String ECO_URI = "http://www.bcpg.fr/model/eco/1.0";

	public static final String ECO_PREFIX = "eco";

	public static final QName MODEL = QName.createQName(ECO_URI, "ecomodel");
	
	//eco
	public static final QName TYPE_ECO = QName.createQName(ECO_URI,
			"changeOrder");
	public static final QName PROP_ECO_STATE = QName.createQName(ECO_URI,
			"ecoState");
	public static final QName PROP_ECO_TYPE = QName.createQName(ECO_URI,
			"ecoType");
	public static final QName ASSOC_CALCULATED_CHARACTS = QName.createQName(ECO_URI,
			"calculatedCharacts");	
	public static final QName ASSOC_CHANGE_UNITS = QName.createQName(ECO_URI,
			"changeUnits");	
	
	//replacementList
	public static final QName TYPE_REPLACEMENTLIST = QName.createQName(ECO_URI,
			"replacementList");
	public static final QName PROP_REVISION = QName.createQName(ECO_URI,
			"revisionType");
	public static final QName ASSOC_SOURCE_ITEM = QName.createQName(ECO_URI,
			"sourceItem");
	public static final QName ASSOC_TARGET_ITEM = QName.createQName(ECO_URI,
			"targetItem");
	
	//wUsedList
	public static final QName TYPE_WUSEDLIST = QName.createQName(ECO_URI,
			"wUsedList");
	public static final QName PROP_WUL_IMPACTED_DATALIST = QName.createQName(ECO_URI,
			"impactedDataList");
	public static final QName PROP_WUL_IS_WUSED_IMPACTED = QName.createQName(ECO_URI,
			"isWUsedImpacted");	
	public static final QName ASSOC_WUL_LINK = QName.createQName(ECO_URI,
			"wulLink");
	public static final QName ASSOC_WUL_CHANGE_UNIT = QName.createQName(ECO_URI,
			"wulChangeUnit");
	
	//simulationList
	public static final QName TYPE_SIMULATIONLIST = QName.createQName(ECO_URI,
			"simulationList");
	public static final QName PROP_SL_SOURCE_VALUE = QName.createQName(ECO_URI,
			"slSourceValue");
	public static final QName PROP_SL_TARGET_VALUE = QName.createQName(ECO_URI,
			"slTargetValue");	
	public static final QName ASSOC_SL_SOURCE_ITEM = QName.createQName(ECO_URI,
			"slSourceItem");
	public static final QName ASSOC_SL_CHARACT = QName.createQName(ECO_URI,
			"slCharact");
	
	//changeUnit
	public static final QName TYPE_CHANGE_UNIT = QName.createQName(ECO_URI,
			"changeUnit");
	public static final QName PROP_CU_REVISION = QName.createQName(ECO_URI,
			"cuRevision");
	public static final QName PROP_CU_REQ_RESPECTED = QName.createQName(ECO_URI,
			"cuReqRespected");
	public static final QName PROP_CU_REQ_DETAILS = QName.createQName(ECO_URI,
			"cuReqDetails");
	public static final QName PROP_CU_TREATED = QName.createQName(ECO_URI,
			"cuTreated");	
	public static final QName ASSOC_CU_ECO = QName.createQName(ECO_URI,
			"cuECO");
	public static final QName ASSOC_CU_SOURCE_ITEM = QName.createQName(ECO_URI,
			"cuSourceItem");
	public static final QName ASSOC_CU_TARGET_ITEM = QName.createQName(ECO_URI,
			"cuTargetItem");	
	
	//simulationEntityAspect
	public static final QName ASPECT_SIMULATION_ENTITY = QName.createQName(ECO_URI,
			"simulationEntityAspect");
	public static final QName ASSOC_SIMULATION_SOURCE_ITEM = QName.createQName(ECO_URI,
			"simulationSourceItem");
	
}
