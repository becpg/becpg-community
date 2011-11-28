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
	
	//replacementList
	public static final QName TYPE_REPLACEMENTLIST = QName.createQName(ECO_URI,
			"replacementList");
	public static final QName PROP_REVISION = QName.createQName(ECO_URI,
			"rlRevisionType");
	public static final QName ASSOC_SOURCE_ITEM = QName.createQName(ECO_URI,
			"rlSourceItem");
	public static final QName ASSOC_TARGET_ITEM = QName.createQName(ECO_URI,
			"rlTargetItem");
	
	//wUsedList
	public static final QName TYPE_WUSEDLIST = QName.createQName(ECO_URI,
			"wUsedList");
	public static final QName PROP_WUL_IMPACTED_DATALIST = QName.createQName(ECO_URI,
			"impactedDataList");
	public static final QName PROP_WUL_IS_WUSED_IMPACTED = QName.createQName(ECO_URI,
			"isWUsedImpacted");	
	public static final QName ASSOC_WUL_LINK = QName.createQName(ECO_URI,
			"wulLink");
	public static final QName ASSOC_WUL_SOURCE_ITEM = QName.createQName(ECO_URI,
			"wulSourceItem");
	
	//simulationList
	public static final QName TYPE_CALCULATEDCHARACTLIST = QName.createQName(ECO_URI,
			"calculatedCharactList");
	public static final QName PROP_CCL_SOURCE_VALUE = QName.createQName(ECO_URI,
			"cclSourceValue");
	public static final QName PROP_CCL_TARGET_VALUE = QName.createQName(ECO_URI,
			"cclTargetValue");	
	public static final QName ASSOC_CCL_SOURCE_ITEM = QName.createQName(ECO_URI,
			"cclSourceItem");
	public static final QName ASSOC_CCL_CHARACT = QName.createQName(ECO_URI,
			"cclCharact");
	
	//changeUnitList
	public static final QName TYPE_CHANGEUNITLIST = QName.createQName(ECO_URI,
			"changeUnitList");
	public static final QName PROP_CUL_REVISION = QName.createQName(ECO_URI,
			"culRevision");
	public static final QName PROP_CUL_REQ_TYPE = QName.createQName(ECO_URI,
			"culReqType");
	public static final QName PROP_CUL_REQ_DETAILS = QName.createQName(ECO_URI,
			"culReqDetails");
	public static final QName PROP_CUL_TREATED = QName.createQName(ECO_URI,
			"culTreated");	
	public static final QName ASSOC_CUL_SOURCE_ITEM = QName.createQName(ECO_URI,
			"culSourceItem");
	public static final QName ASSOC_CUL_TARGET_ITEM = QName.createQName(ECO_URI,
			"culTargetItem");	
	public static final QName ASSOC_CUL_SIMULATION_ITEM = QName.createQName(ECO_URI,
			"culSimulationItem");	
	
	//simulationEntityAspect
	public static final QName ASPECT_SIMULATION_ENTITY = QName.createQName(ECO_URI,
			"simulationEntityAspect");
	public static final QName ASSOC_SIMULATION_SOURCE_ITEM = QName.createQName(ECO_URI,
			"simulationSourceItem");
	
}
