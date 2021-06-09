/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * PLM model definition.
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface PLMModel {
	

	// product
	/** Constant <code>TYPE_PRODUCT</code> */
	QName TYPE_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "product");
	/** Constant <code>TYPE_FINISHEDPRODUCT</code> */
	QName TYPE_FINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");
	/** Constant <code>TYPE_SEMIFINISHEDPRODUCT</code> */
	QName TYPE_SEMIFINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "semiFinishedProduct");
	/** Constant <code>TYPE_LOCALSEMIFINISHEDPRODUCT</code> */
	QName TYPE_LOCALSEMIFINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "localSemiFinishedProduct");
	/** Constant <code>TYPE_RAWMATERIAL</code> */
	QName TYPE_RAWMATERIAL = QName.createQName(BeCPGModel.BECPG_URI, "rawMaterial");
	/** Constant <code>TYPE_PACKAGINGKIT</code> */
	QName TYPE_PACKAGINGKIT = QName.createQName(BeCPGModel.BECPG_URI, "packagingKit");
	/** Constant <code>TYPE_PACKAGINGMATERIAL</code> */
	QName TYPE_PACKAGINGMATERIAL = QName.createQName(BeCPGModel.BECPG_URI, "packagingMaterial");
	/** Constant <code>TYPE_RESOURCEPRODUCT</code> */
	QName TYPE_RESOURCEPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "resourceProduct");

	/** Constant <code>TYPE_PRODUCT_SPECIFICATION</code> */
	QName TYPE_PRODUCT_SPECIFICATION = QName.createQName(BeCPGModel.BECPG_URI, "productSpecification");

	// productMicrobioCriteria
	/** Constant <code>TYPE_PRODUCT_MICROBIO_CRITERIA</code> */
	QName TYPE_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteria");
	/** Constant <code>ASSOC_PRODUCT_MICROBIO_CRITERIA</code> */
	QName ASSOC_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteriaRef");
	/** Constant <code>ASPECT_PRODUCT_MICROBIO_CRITERIA</code> */
	QName ASPECT_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteriaAspect");

	/** Constant <code>TYPE_PRODUCTLIST_ITEM</code> */
	QName TYPE_PRODUCTLIST_ITEM = QName.createQName(BeCPGModel.BECPG_URI, "productListItem");

	// allergenList
	/** Constant <code>TYPE_ALLERGENLIST</code> */
	QName TYPE_ALLERGENLIST = QName.createQName(BeCPGModel.BECPG_URI, "allergenList");
	/** Constant <code>PROP_ALLERGENLIST_VOLUNTARY</code> */
	QName PROP_ALLERGENLIST_VOLUNTARY = QName.createQName(BeCPGModel.BECPG_URI, "allergenListVoluntary");
	/** Constant <code>ASSOC_ALLERGENLIST_ALLERGEN</code> */
	QName ASSOC_ALLERGENLIST_ALLERGEN = QName.createQName(BeCPGModel.BECPG_URI, "allergenListAllergen");
	/** Constant <code>PROP_ALLERGENLIST_INVOLUNTARY</code> */
	QName PROP_ALLERGENLIST_INVOLUNTARY = QName.createQName(BeCPGModel.BECPG_URI, "allergenListInVoluntary");
	/** Constant <code>ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES</code> */
	QName ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "allergenListVolSources");
	/** Constant <code>ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES</code> */
	QName ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "allergenListInVolSources");
	/** Constant <code>PROP_ALLERGEN_DECISION_TREE</code> */
	QName PROP_ALLERGEN_DECISION_TREE = QName.createQName(BeCPGModel.BECPG_URI, "allergenListDecisionTree");
	/** Constant <code>ASSOC_ALLERGENSUBSETS</code> */
	QName ASSOC_ALLERGENSUBSETS = QName.createQName(BeCPGModel.BECPG_URI, "allergenSubset");

	// compoList
	/** Constant <code>TYPE_COMPOLIST</code> */
	QName TYPE_COMPOLIST = QName.createQName(BeCPGModel.BECPG_URI, "compoList");
	/** Constant <code>ASSOC_COMPOLIST_PRODUCT</code> */
	QName ASSOC_COMPOLIST_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "compoListProduct");
	/** Constant <code>PROP_COMPOLIST_QTY</code> */
	QName PROP_COMPOLIST_QTY = QName.createQName(BeCPGModel.BECPG_URI, "compoListQty");
	/** Constant <code>PROP_COMPOLIST_QTY_SUB_FORMULA</code> */
	QName PROP_COMPOLIST_QTY_SUB_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "compoListQtySubFormula");
	/** Constant <code>PROP_COMPOLIST_QTY_AFTER_PROCESS</code> */
	QName PROP_COMPOLIST_QTY_AFTER_PROCESS = QName.createQName(BeCPGModel.BECPG_URI, "compoListQtyAfterProcess");
	/** Constant <code>PROP_COMPOLIST_UNIT</code> */
	QName PROP_COMPOLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "compoListUnit");
	/** Constant <code>PROP_COMPOLIST_LOSS_PERC</code> */
	QName PROP_COMPOLIST_LOSS_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListLossPerc");
	/** Constant <code>PROP_COMPOLIST_YIELD_PERC</code> */
	QName PROP_COMPOLIST_YIELD_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListYieldPerc");
	/** Constant <code>PROP_COMPOLIST_DECL_TYPE</code> */
	QName PROP_COMPOLIST_DECL_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "compoListDeclType");
	/** Constant <code>PROP_COMPOLIST_OVERRUN_PERC</code> */
	QName PROP_COMPOLIST_OVERRUN_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListOverrunPerc");
	/** Constant <code>PROP_COMPOLIST_VOLUME</code> */
	QName PROP_COMPOLIST_VOLUME = QName.createQName(BeCPGModel.BECPG_URI, "compoListVolume");

	// packagingList
	/** Constant <code>TYPE_PACKAGINGLIST</code> */
	QName TYPE_PACKAGINGLIST = QName.createQName(BeCPGModel.BECPG_URI, "packagingList");
	/** Constant <code>ASSOC_PACKAGINGLIST_PRODUCT</code> */
	QName ASSOC_PACKAGINGLIST_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "packagingListProduct");
	/** Constant <code>PROP_PACKAGINGLIST_QTY</code> */
	QName PROP_PACKAGINGLIST_QTY = QName.createQName(BeCPGModel.BECPG_URI, "packagingListQty");
	/** Constant <code>PROP_PACKAGINGLIST_UNIT</code> */
	QName PROP_PACKAGINGLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "packagingListUnit");
	/** Constant <code>PROP_PACKAGINGLIST_PKG_LEVEL</code> */
	QName PROP_PACKAGINGLIST_PKG_LEVEL = QName.createQName(BeCPGModel.BECPG_URI, "packagingListPkgLevel");
	/** Constant <code>PROP_PACKAGINGLIST_ISMASTER</code> */
	QName PROP_PACKAGINGLIST_ISMASTER = QName.createQName(BeCPGModel.BECPG_URI, "packagingListIsMaster");
	/** Constant <code>PROP_PACKAGINGLIST_LOSS_PERC</code> */
	QName PROP_PACKAGINGLIST_LOSS_PERC = QName.createQName(BeCPGModel.BECPG_URI, "packagingListLossPerc");

	// costList
	/** Constant <code>TYPE_COSTLIST</code> */
	QName TYPE_COSTLIST = QName.createQName(BeCPGModel.BECPG_URI, "costList");
	/** Constant <code>PROP_COSTLIST_VALUE</code> */
	QName PROP_COSTLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "costListValue");
	/** Constant <code>PROP_COSTLIST_UNIT</code> */
	QName PROP_COSTLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "costListUnit");
	/** Constant <code>PROP_COSTLIST_MAXI</code> */
	QName PROP_COSTLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "costListMaxi");
	/** Constant <code>ASSOC_COSTLIST_COST</code> */
	QName ASSOC_COSTLIST_COST = QName.createQName(BeCPGModel.BECPG_URI, "costListCost");

	// priceList
	/** Constant <code>TYPE_PRICELIST</code> */
	QName TYPE_PRICELIST = QName.createQName(BeCPGModel.BECPG_URI, "priceList");
	/** Constant <code>ASSOC_PRICELIST_COST</code> */
	QName ASSOC_PRICELIST_COST = QName.createQName(BeCPGModel.BECPG_URI, "priceListCost");
	/** Constant <code>PROP_PRICELIST_PREF_RANK</code> */
	QName PROP_PRICELIST_PREF_RANK = QName.createQName(BeCPGModel.BECPG_URI, "priceListPrefRank");
	/** Constant <code>PROP_PRICELIST_VALUE</code> */
	QName PROP_PRICELIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "priceListValue");
	/** Constant <code>PROP_PRICELIST_UNIT</code> */
	QName PROP_PRICELIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "priceListUnit");
	/** Constant <code>PROP_PRICELIST_PURCHASE_QTY</code> */
	QName PROP_PRICELIST_PURCHASE_QTY = QName.createQName(BeCPGModel.BECPG_URI, "priceListPurchaseQty");
	/** Constant <code>PROP_PRICELIST_PURCHASE_UNIT</code> */
	QName PROP_PRICELIST_PURCHASE_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "priceListPurchaseUnit");

	// ingList
	/** Constant <code>TYPE_INGLIST</code> */
	QName TYPE_INGLIST = QName.createQName(BeCPGModel.BECPG_URI, "ingList");
	/** Constant <code>PROP_INGLIST_QTY_PERC</code> */
	QName PROP_INGLIST_QTY_PERC = QName.createQName(BeCPGModel.BECPG_URI, "ingListQtyPerc");
	
	QName PROP_INGLIST_QTY_PERCWITHYIELD = QName.createQName(BeCPGModel.BECPG_URI, "ingListQtyPercWithYield");
	/** Constant <code>PROP_INGLIST_IS_GMO</code> */
	QName PROP_INGLIST_IS_GMO = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsGMO");
	/** Constant <code>PROP_INGLIST_IS_IONIZED</code> */
	QName PROP_INGLIST_IS_IONIZED = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsIonized");
	/** Constant <code>PROP_INGLIST_IS_PROCESSING_AID</code> */
	QName PROP_INGLIST_IS_PROCESSING_AID = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsProcessingAid");
	/** Constant <code>PROP_INGLIST_DECL_TYPE</code> */
	QName PROP_INGLIST_DECL_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "ingListDeclType");
	/** Constant <code>ASSOC_INGLIST_GEO_ORIGIN</code> */
	QName ASSOC_INGLIST_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "ingListGeoOrigin");
	/** Constant <code>ASSOC_INGLIST_BIO_ORIGIN</code> */
	QName ASSOC_INGLIST_BIO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "ingListBioOrigin");
	/** Constant <code>ASSOC_INGLIST_ING</code> */
	QName ASSOC_INGLIST_ING = QName.createQName(BeCPGModel.BECPG_URI, "ingListIng");
	
	
	// nutList
	/** Constant <code>TYPE_NUTLIST</code> */
	QName TYPE_NUTLIST = QName.createQName(BeCPGModel.BECPG_URI, "nutList");
	/** Constant <code>ASSOC_NUTLIST_NUT</code> */
	QName ASSOC_NUTLIST_NUT = QName.createQName(BeCPGModel.BECPG_URI, "nutListNut");
	/** Constant <code>PROP_NUTLIST_VALUE</code> */
	QName PROP_NUTLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "nutListValue");
	/** Constant <code>PROP_NUTLIST_VALUE_PER_SERVING</code> */
	QName PROP_NUTLIST_VALUE_PER_SERVING = QName.createQName(BeCPGModel.BECPG_URI, "nutListValuePerServing");
	/** Constant <code>PROP_NUTLIST_FORMULATED_VALUE</code> */
	QName PROP_NUTLIST_FORMULATED_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "nutListFormulatedValue");
	/** Constant <code>PROP_NUTLIST_UNIT</code> */
	QName PROP_NUTLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "nutListUnit");
	/** Constant <code>PROP_NUTLIST_MINI</code> */
	QName PROP_NUTLIST_MINI = QName.createQName(BeCPGModel.BECPG_URI, "nutListMini");
	

	QName PROP_NUTLIST_FORMULATED_MINI = QName.createQName(BeCPGModel.BECPG_URI, "nutListFormulatedMini");
	
	QName PROP_NUTLIST_FORMULATED_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "nutListFormulatedMaxi");
	/** Constant <code>PROP_NUTLIST_MAXI</code> */
	QName PROP_NUTLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "nutListMaxi");
	/** Constant <code>PROP_NUTLIST_GROUP</code> */
	QName PROP_NUTLIST_GROUP = QName.createQName(BeCPGModel.BECPG_URI, "nutListGroup");
	/** Constant <code>PROP_NUTLIST_ROUNDED_VALUE</code> */
	QName PROP_NUTLIST_ROUNDED_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "nutListRoundedValue");
	/** Constant <code>PROP_NUTLIST_FORMULA_ERROR</code> */
	QName PROP_NUTLIST_FORMULA_ERROR = QName.createQName(BeCPGModel.BECPG_URI, "nutListFormulaErrorLog");
	/** Constant <code>PROP_NUTLIST_METHOD</code> */
	QName PROP_NUTLIST_METHOD = QName.createQName(BeCPGModel.BECPG_URI, "nutListMethod");
	
	// organoList
	/** Constant <code>TYPE_ORGANOLIST</code> */
	QName TYPE_ORGANOLIST = QName.createQName(BeCPGModel.BECPG_URI, "organoList");
	/** Constant <code>PROP_ORGANOLIST_VALUE</code> */
	QName PROP_ORGANOLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "organoListValue");
	/** Constant <code>ASSOC_ORGANOLIST_ORGANO</code> */
	QName ASSOC_ORGANOLIST_ORGANO = QName.createQName(BeCPGModel.BECPG_URI, "organoListOrgano");

	// ingLabelingList
	/** Constant <code>TYPE_INGLABELINGLIST</code> */
	QName TYPE_INGLABELINGLIST = QName.createQName(BeCPGModel.BECPG_URI, "ingLabelingList");

	// labelingRuleList
	/** Constant <code>TYPE_LABELINGRULELIST</code> */
	QName TYPE_LABELINGRULELIST = QName.createQName(BeCPGModel.BECPG_URI, "labelingRuleList");
	/** Constant <code>PROP_LABELINGRULELIST_LABEL</code> */
	QName PROP_LABELINGRULELIST_LABEL = QName.createQName(BeCPGModel.BECPG_URI, "lrLabel");
	/** Constant <code>PROP_LABELINGRULELIST_TYPE</code> */
	QName PROP_LABELINGRULELIST_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "lrType");
	/** Constant <code>PROP_LABELINGRULELIST_SYNC_STATE</code> */
	QName PROP_LABELINGRULELIST_SYNC_STATE = QName.createQName(BeCPGModel.BECPG_URI, "lrSyncState");

	/** Constant <code>ASSOC_ILL_GRP</code> */
	QName ASSOC_ILL_GRP = QName.createQName(BeCPGModel.BECPG_URI, "illGrp");
	/** Constant <code>PROP_ILL_VALUE</code> */
	QName PROP_ILL_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "illValue");
	/** Constant <code>PROP_ILL_MANUAL_VALUE</code> */
	QName PROP_ILL_MANUAL_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "illManualValue");
	/** Constant <code>PROP_ILL_LOG_VALUE</code> */
	QName PROP_ILL_LOG_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "illLogValue");

	// microbioList
	/** Constant <code>TYPE_MICROBIOLIST</code> */
	QName TYPE_MICROBIOLIST = QName.createQName(BeCPGModel.BECPG_URI, "microbioList");
	/** Constant <code>PROP_MICROBIOLIST_VALUE</code> */
	QName PROP_MICROBIOLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "mblValue");
	/** Constant <code>PROP_MICROBIOLIST_UNIT</code> */
	QName PROP_MICROBIOLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "mblUnit");
	/** Constant <code>PROP_MICROBIOLIST_MAXI</code> */
	QName PROP_MICROBIOLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "mblMaxi");
	/** Constant <code>PROP_MICROBIOLIST_TEXT_CRITERIA</code> */
	QName PROP_MICROBIOLIST_TEXT_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "mblTextCriteria");
	/** Constant <code>ASSOC_MICROBIOLIST_MICROBIO</code> */
	QName ASSOC_MICROBIOLIST_MICROBIO = QName.createQName(BeCPGModel.BECPG_URI, "mblMicrobio");

	// physicoChemList
	/** Constant <code>TYPE_PHYSICOCHEMLIST</code> */
	QName TYPE_PHYSICOCHEMLIST = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemList");
	/** Constant <code>PROP_PHYSICOCHEMLIST_VALUE</code> */
	QName PROP_PHYSICOCHEMLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "pclValue");
	/** Constant <code>PROP_PHYSICOCHEMLIST_UNIT</code> */
	QName PROP_PHYSICOCHEMLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "pclUnit");
	/** Constant <code>PROP_PHYSICOCHEMLIST_TYPE</code> */
	QName PROP_PHYSICOCHEMLIST_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "pclType");
	
	/** Constant <code>PROP_PHYSICOCHEMLIST_MINI</code> */
	QName PROP_PHYSICOCHEMLIST_MINI = QName.createQName(BeCPGModel.BECPG_URI, "pclMini");
	/** Constant <code>PROP_PHYSICOCHEMLIST_MAXI</code> */
	QName PROP_PHYSICOCHEMLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "pclMaxi");
	/** Constant <code>PROP_PHYSICOCHEMFORMULA_ERROR</code> */
	QName PROP_PHYSICOCHEMFORMULA_ERROR = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormulaErrorLog");
	/** Constant <code>ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM</code> */
	QName ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM = QName.createQName(BeCPGModel.BECPG_URI, "pclPhysicoChem");

	// forbiddenIngList
	/** Constant <code>TYPE_FORBIDDENINGLIST</code> */
	QName TYPE_FORBIDDENINGLIST = QName.createQName(BeCPGModel.BECPG_URI, "forbiddenIngList");
	/** Constant <code>PROP_FIL_REQ_TYPE</code> */
	QName PROP_FIL_REQ_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "filReqType");
	/** Constant <code>PROP_FIL_REQ_MESSAGE</code> */
	QName PROP_FIL_REQ_MESSAGE = QName.createQName(BeCPGModel.BECPG_URI, "filReqMessage");
	/** Constant <code>PROP_FIL_QTY_PERC_MAXI</code> */
	QName PROP_FIL_QTY_PERC_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "filQtyPercMaxi");
	/** Constant <code>PROP_FIL_IS_GMO</code> */
	QName PROP_FIL_IS_GMO = QName.createQName(BeCPGModel.BECPG_URI, "filIsGMO");
	/** Constant <code>PROP_FIL_IS_IONIZED</code> */
	QName PROP_FIL_IS_IONIZED = QName.createQName(BeCPGModel.BECPG_URI, "filIsIonized");
	/** Constant <code>ASSOC_FIL_INGS</code> */
	QName ASSOC_FIL_INGS = QName.createQName(BeCPGModel.BECPG_URI, "filIngs");
	/** Constant <code>ASSOC_FIL_GEO_ORIGINS</code> */
	QName ASSOC_FIL_GEO_ORIGINS = QName.createQName(BeCPGModel.BECPG_URI, "filGeoOrigins");
	/** Constant <code>ASSOC_FIL_BIO_ORIGINS</code> */
	QName ASSOC_FIL_BIO_ORIGINS = QName.createQName(BeCPGModel.BECPG_URI, "filBioOrigins");

	// reqCtrlList
	/** Constant <code>TYPE_REQCTRLLIST</code> */
	QName TYPE_REQCTRLLIST = QName.createQName(BeCPGModel.BECPG_URI, "reqCtrlList");
	/** Constant <code>PROP_RCL_REQ_TYPE</code> */
	QName PROP_RCL_REQ_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "rclReqType");
	/** Constant <code>PROP_RCL_REQ_MESSAGE</code> */
	QName PROP_RCL_REQ_MESSAGE = QName.createQName(BeCPGModel.BECPG_URI, "rclReqMessage");
	/** Constant <code>ASSOC_RCL_SOURCES</code> */
	QName ASSOC_RCL_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "rclSources");
	/** Constant <code>ASSOC_RCL_CHARACT</code> */
	QName ASSOC_RCL_CHARACT = QName.createQName(BeCPGModel.BECPG_URI, "rclCharact");
	
	/** Constant <code>PROP_RCL_REQ_DATA_TYPE</code> */
	QName PROP_RCL_REQ_DATA_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "rclDataType");
	
	/** Constant <code>TYPE_SPEC_COMPATIBILTY_LIST</code> */
	QName TYPE_SPEC_COMPATIBILTY_LIST = QName.createQName(BeCPGModel.BECPG_URI, "productSpecCompatibilityList");
	
	/** Constant <code>PROP_SPEC_COMPATIBILITY_JOB_ON</code> */
	QName PROP_SPEC_COMPATIBILITY_JOB_ON = QName.createQName(BeCPGModel.BECPG_URI, "specCompatibilityJobOn");
	/** Constant <code>PROP_SPEC_COMPATIBILITY_LOG</code> */
	QName PROP_SPEC_COMPATIBILITY_LOG = QName.createQName(BeCPGModel.BECPG_URI, "specCompatibilityLog");

	/** Constant <code>ASSOC_PSCL_SOURCE_ITEM</code> */
	QName ASSOC_PSCL_SOURCE_ITEM = QName.createQName(BeCPGModel.BECPG_URI, "psclSourceItem");
	

	/** Constant <code>TYPE_DYNAMICCHARACTLIST</code> */
	QName TYPE_DYNAMICCHARACTLIST = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactList");
	/** Constant <code>PROP_DYNAMICCHARACT_TITLE</code> */
	QName PROP_DYNAMICCHARACT_TITLE = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactTitle");
	/** Constant <code>PROP_DYNAMICCHARACT_FORMULA</code> */
	QName PROP_DYNAMICCHARACT_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactFormula");
	/** Constant <code>PROP_DYNAMICCHARACT_VALUE</code> */
	QName PROP_DYNAMICCHARACT_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactValue");
	/** Constant <code>PROP_DYNAMICCHARACT_GROUP_COLOR</code> */
	QName PROP_DYNAMICCHARACT_GROUP_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactGroupColor");
	/** Constant <code>PROP_DYNAMICCHARACT_COLUMN</code> */
	QName PROP_DYNAMICCHARACT_COLUMN = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactColumn");
	/** Constant <code>PROP_DYNAMICCHARACT_SYNCHRONIZABLE_STATE</code> */
	QName PROP_DYNAMICCHARACT_SYNCHRONIZABLE_STATE = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactSynchronisableState");

	// contactList
	/** Constant <code>TYPE_CONTACTLIST</code> */
	QName TYPE_CONTACTLIST = QName.createQName(BeCPGModel.BECPG_URI, "contactList");
	/** Constant <code>PROP_CONTACT_LIST_FIRST_NAME</code> */
	QName PROP_CONTACT_LIST_FIRST_NAME = QName.createQName(BeCPGModel.BECPG_URI, "contactListFirstName");
	/** Constant <code>PROP_CONTACT_LIST_LAST_NAME</code> */
	QName PROP_CONTACT_LIST_LAST_NAME = QName.createQName(BeCPGModel.BECPG_URI, "contactListLastName");
	
	// labelClaimList
	/** Constant <code>TYPE_LABELCLAIMLIST</code> */
	QName TYPE_LABELCLAIMLIST = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimList");
	/** Constant <code>PROP_LCL_TYPE</code> */
	QName PROP_LCL_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "lclType");
	/** Constant <code>PROP_LCL_CLAIM_VALUE</code> */
	QName PROP_LCL_CLAIM_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "lclClaimValue");
	/** Constant <code>ASSOC_LCL_LABELCLAIM</code> */
	QName ASSOC_LCL_LABELCLAIM = QName.createQName(BeCPGModel.BECPG_URI, "lclLabelClaim");
	/** Constant <code>PROP_LCL_FORMULAERROR</code> */
	QName PROP_LCL_FORMULAERROR = QName.createQName(BeCPGModel.BECPG_URI, "lclFormulaErrorLog");
	/** Constant <code>ASSOC_LCL_MISSING_LABELCLAIMS</code> */
	QName ASSOC_LCL_MISSING_LABELCLAIMS = QName.createQName(BeCPGModel.BECPG_URI, "lclMissingLabelClaims");
	
	// allergen
	/** Constant <code>TYPE_ALLERGEN</code> */
	QName TYPE_ALLERGEN = QName.createQName(BeCPGModel.BECPG_URI, "allergen");
	/** Constant <code>PROP_ALLERGEN_CODE</code> */
	QName PROP_ALLERGEN_CODE = QName.createQName(BeCPGModel.BECPG_URI, "allergenCode");
	/** Constant <code>PROP_ALLERGEN_TYPE</code> */
	QName PROP_ALLERGEN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "allergenType");
	/** Constant <code>PROP_ALLERGEN_REGULATORY_THRESHOLD</code> */
	QName PROP_ALLERGEN_REGULATORY_THRESHOLD = QName.createQName(BeCPGModel.BECPG_URI, "allergenRegulatoryThreshold");

	// cost
	/** Constant <code>TYPE_COST</code> */
	QName TYPE_COST = QName.createQName(BeCPGModel.BECPG_URI, "cost");
	/** Constant <code>PROP_COST_FORMULA</code> */
	QName PROP_COST_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "costFormula");
	/** Constant <code>PROP_COSTCURRENCY</code> */
	QName PROP_COSTCURRENCY = QName.createQName(BeCPGModel.BECPG_URI, "costCurrency");
	/** Constant <code>PROP_COSTFIXED</code> */
	QName PROP_COSTFIXED = QName.createQName(BeCPGModel.BECPG_URI, "costFixed");
	/** Constant <code>PROP_COSTTYPE</code> */
	QName PROP_COSTTYPE = QName.createQName(BeCPGModel.BECPG_URI, "costType");

	// ing
	/** Constant <code>TYPE_ING</code> */
	QName TYPE_ING = QName.createQName(BeCPGModel.BECPG_URI, "ing");
	/** Constant <code>PROP_ING_CEECODE</code> */
	QName PROP_ING_CEECODE = QName.createQName(BeCPGModel.BECPG_URI, "ingCEECode");
	/** Constant <code>PROP_ING_CASCODE</code> */
	QName PROP_ING_CASCODE = QName.createQName(BeCPGModel.BECPG_URI, "ingCASCode");

	/** Constant <code>ASPECT_ING_TYPE</code> */
	QName ASPECT_ING_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeAspect");
	
	/** Constant <code>PROP_ING_TYPE_V2</code> */
	QName PROP_ING_TYPE_V2 = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeV2");
	/** Constant <code>TYPE_ING_TYPE_ITEM</code> */
	QName TYPE_ING_TYPE_ITEM = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeItem");
	/** Constant <code>PROP_ING_TYPE_DEC_THRESHOLD</code> */
	QName PROP_ING_TYPE_DEC_THRESHOLD = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeDecThreshold");
	/** Constant <code>PROP_PLURAL_LEGAL_NAME</code> */
	QName PROP_PLURAL_LEGAL_NAME = QName.createQName(BeCPGModel.BECPG_URI, "pluralLegalName");

	// microbio
	/** Constant <code>TYPE_MICROBIO</code> */
	QName TYPE_MICROBIO = QName.createQName(BeCPGModel.BECPG_URI, "microbio");

	// geoOrigin
	/** Constant <code>TYPE_GEO_ORIGIN</code> */
	QName TYPE_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "geoOrigin");
	/** Constant <code>PROP_GEO_ORIGIN_ISOCODE</code> */
	QName PROP_GEO_ORIGIN_ISOCODE = QName.createQName(BeCPGModel.BECPG_URI, "bioOriginISOCode");

	// bioOrigin
	/** Constant <code>TYPE_BIO_ORIGIN</code> */
	QName TYPE_BIO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "bioOrigin");
	/** Constant <code>PROP_BIO_ORIGIN_TYPE</code> */
	QName PROP_BIO_ORIGIN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "bioOriginType");

	// nut
	/** Constant <code>TYPE_NUT</code> */
	QName TYPE_NUT = QName.createQName(BeCPGModel.BECPG_URI, "nut");
	/** Constant <code>PROP_NUTGROUP</code> */
	QName PROP_NUTGROUP = QName.createQName(BeCPGModel.BECPG_URI, "nutGroup");
	/** Constant <code>PROP_NUTTYPE</code> */
	QName PROP_NUTTYPE = QName.createQName(BeCPGModel.BECPG_URI, "nutType");
	/** Constant <code>PROP_NUTUNIT</code> */
	QName PROP_NUTUNIT = QName.createQName(BeCPGModel.BECPG_URI, "nutUnit");
	/** Constant <code>PROP_NUTGDA</code> */
	QName PROP_NUTGDA = QName.createQName(BeCPGModel.BECPG_URI, "nutGDA");
	/** Constant <code>PROP_NUTUL</code> */
	QName PROP_NUTUL = QName.createQName(BeCPGModel.BECPG_URI, "nutUL");
	/** Constant <code>PROP_NUT_FORMULA</code> */
	QName PROP_NUT_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "nutFormula");	

	// organo
	/** Constant <code>TYPE_ORGANO</code> */
	QName TYPE_ORGANO = QName.createQName(BeCPGModel.BECPG_URI, "organo");

	// physicoChem
	/** Constant <code>TYPE_PHYSICO_CHEM</code> */
	QName TYPE_PHYSICO_CHEM = QName.createQName(BeCPGModel.BECPG_URI, "physicoChem");
	/** Constant <code>PROP_PHYSICO_CHEM_UNIT</code> */
	QName PROP_PHYSICO_CHEM_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemUnit");
	/** Constant <code>PROP_PHYSICO_CHEM_TYPE</code> */
	QName PROP_PHYSICO_CHEM_TYPE  = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemType");
	/** Constant <code>PROP_PHYSICO_CHEM_FORMULATED</code> */
	QName PROP_PHYSICO_CHEM_FORMULATED = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormulated");
	/** Constant <code>PROP_PHYSICO_CHEM_FORMULATED_FROM_VOL</code> */
	QName PROP_PHYSICO_CHEM_FORMULATED_FROM_VOL = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormulatedFromVol");
	/** Constant <code>PROP_PHYSICO_CHEM_FORMULA</code> */
	QName PROP_PHYSICO_CHEM_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormula");

	// supplier aspect
	/** Constant <code>ASPECT_SUPPLIERS</code> */
	QName ASPECT_SUPPLIERS = QName.createQName(BeCPGModel.BECPG_URI, "suppliersAspect");
	/** Constant <code>ASSOC_SUPPLIERS</code> */
	QName ASSOC_SUPPLIERS = QName.createQName(BeCPGModel.BECPG_URI, "suppliers");
	/** Constant <code>ASSOC_SUPPLIER_PLANTS</code> */
	QName ASSOC_SUPPLIER_PLANTS = QName.createQName(BeCPGModel.BECPG_URI, "supplierPlants");
	/** Constant <code>PROP_SUPPLIER_STATE</code> */
	QName PROP_SUPPLIER_STATE = QName.createQName(BeCPGModel.BECPG_URI, "supplierState");

	// supplier
	/** Constant <code>TYPE_SUPPLIER</code> */
	QName TYPE_SUPPLIER = QName.createQName(BeCPGModel.BECPG_URI, "supplier");
	
	// supplierAccountRef aspect
	/** Constant <code>ASPECT_SUPPLIERS_ACCOUNTREF</code> */
	QName ASPECT_SUPPLIERS_ACCOUNTREF = QName.createQName(BeCPGModel.BECPG_URI, "supplierAccountRefAspect");
	/** Constant <code>ASSOC_SUPPLIER_ACCOUNTS</code> */
	QName ASSOC_SUPPLIER_ACCOUNTS = QName.createQName(BeCPGModel.BECPG_URI, "supplierAccountRef");
	


	// client aspect
	/** Constant <code>ASPECT_CLIENTS</code> */
	QName ASPECT_CLIENTS = QName.createQName(BeCPGModel.BECPG_URI, "clientsAspect");
	/** Constant <code>ASSOC_CLIENTS</code> */
	QName ASSOC_CLIENTS = QName.createQName(BeCPGModel.BECPG_URI, "clients");

	// client
	/** Constant <code>TYPE_CLIENT</code> */
	QName TYPE_CLIENT = QName.createQName(BeCPGModel.BECPG_URI, "client");
	/** Constant <code>PROP_CLIENT_STATE</code> */
	QName PROP_CLIENT_STATE = QName.createQName(BeCPGModel.BECPG_URI, "clientState");

	// product aspect
	/** Constant <code>ASPECT_PRODUCT</code> */
	QName ASPECT_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "productAspect");
	/** Constant <code>PROP_PRODUCT_HIERARCHY1</code> */
	QName PROP_PRODUCT_HIERARCHY1 = QName.createQName(BeCPGModel.BECPG_URI, "productHierarchy1");
	/** Constant <code>PROP_PRODUCT_HIERARCHY2</code> */
	QName PROP_PRODUCT_HIERARCHY2 = QName.createQName(BeCPGModel.BECPG_URI, "productHierarchy2");
	/** Constant <code>PROP_PRODUCT_STATE</code> */
	QName PROP_PRODUCT_STATE = QName.createQName(BeCPGModel.BECPG_URI, "productState");
	/** Constant <code>PROP_PRODUCT_UNIT</code> */
	QName PROP_PRODUCT_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "productUnit");
	/** Constant <code>PROP_PRODUCT_QTY</code> */
	QName PROP_PRODUCT_QTY = QName.createQName(BeCPGModel.BECPG_URI, "productQty");
	/** Constant <code>PROP_PRODUCT_DENSITY</code> */
	QName PROP_PRODUCT_DENSITY = QName.createQName(BeCPGModel.BECPG_URI, "productDensity");
	/** Constant <code>PROP_PRODUCT_COMMENTS</code> */
	QName PROP_PRODUCT_COMMENTS = QName.createQName(BeCPGModel.BECPG_URI, "productComments");
	/** Constant <code>PROP_PRODUCT_SCORE</code> */
	QName PROP_PRODUCT_SCORE = QName.createQName(BeCPGModel.BECPG_URI, "productScores");
	/** Constant <code>PROP_PRODUCT_DROP_PACKAGING_OF_COMPONENTS</code> */
	QName PROP_PRODUCT_DROP_PACKAGING_OF_COMPONENTS = QName.createQName(BeCPGModel.BECPG_URI, "dropPackagingOfComponents");
	/** Constant <code>PROP_PRODUCT_SERVING_SIZE</code> */
	QName PROP_PRODUCT_SERVING_SIZE = QName.createQName(BeCPGModel.BECPG_URI, "servingSize");
	
	/** Constant <code>PROP_PRODUCT_SERVING_SIZE_BY_COUNTRY</code> */
	QName PROP_PRODUCT_SERVING_SIZE_BY_COUNTRY = QName.createQName(BeCPGModel.BECPG_URI, "servingSizeByCountry");
	
	/** Constant <code>PROP_PRODUCT_SERVING_SIZE_UNIT</code> */
	QName PROP_PRODUCT_SERVING_SIZE_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "servingSizeUnit");
	
	// transformation
	/** Constant <code>ASPECT_TRANSFORMATION</code> */
	QName ASPECT_TRANSFORMATION = QName.createQName(BeCPGModel.BECPG_URI, "transformationAspect");	
	/** Constant <code>ASSOC_PRODUCT_SPECIFICATIONS</code> */
	QName ASSOC_PRODUCT_SPECIFICATIONS = QName.createQName(BeCPGModel.BECPG_URI, "productSpecifications");
	
	/** Constant <code>PROP_PRODUCT_NET_VOLUME</code> */
	QName PROP_PRODUCT_NET_VOLUME = QName.createQName(BeCPGModel.BECPG_URI, "netVolume");
	/** Constant <code>PROP_PRODUCT_NET_WEIGHT</code> */
	QName PROP_PRODUCT_NET_WEIGHT = QName.createQName(BeCPGModel.BECPG_URI, "netWeight");
	/** Constant <code>PROP_PRODUCT_COMPO_QTY_USED</code> */
	QName PROP_PRODUCT_COMPO_QTY_USED = QName.createQName(BeCPGModel.BECPG_URI, "productCompoQtyUsed");
	/** Constant <code>PROP_PRODUCT_COMPO_VOLUME_USED</code> */
	QName PROP_PRODUCT_COMPO_VOLUME_USED = QName.createQName(BeCPGModel.BECPG_URI, "productCompoVolumeUsed");

	// ean aspect
	/** Constant <code>ASPECT_EAN</code> */
	QName ASPECT_EAN = QName.createQName(BeCPGModel.BECPG_URI, "eanAspect");
	/** Constant <code>PROP_EAN_CODE</code> */
	QName PROP_EAN_CODE = QName.createQName(BeCPGModel.BECPG_URI, "eanCode");

	// profitability
	/** Constant <code>ASPECT_PROFITABILITY</code> */
	QName ASPECT_PROFITABILITY = QName.createQName(BeCPGModel.BECPG_URI, "profitabilityAspect");
	/** Constant <code>PROP_UNIT_PRICE</code> */
	QName PROP_UNIT_PRICE = QName.createQName(BeCPGModel.BECPG_URI, "unitPrice");
	/** Constant <code>PROP_BREAK_EVEN</code> */
	QName PROP_BREAK_EVEN = QName.createQName(BeCPGModel.BECPG_URI, "breakEven");
	/** Constant <code>PROP_PROJECTED_QTY</code> */
	QName PROP_PROJECTED_QTY = QName.createQName(BeCPGModel.BECPG_URI, "projectedQty");
	/** Constant <code>PROP_PROFITABILITY</code> */
	QName PROP_PROFITABILITY = QName.createQName(BeCPGModel.BECPG_URI, "profitability");
	/** Constant <code>PROP_UNIT_TOTAL_COST</code> */
	QName PROP_UNIT_TOTAL_COST = QName.createQName(BeCPGModel.BECPG_URI, "unitTotalCost");
	/** Constant <code>PROP_PRICE_CURRENCY</code> */
	QName PROP_PRICE_CURRENCY = QName.createQName(BeCPGModel.BECPG_URI, "priceCurrency");

	// manufacturingAspect
	/** Constant <code>ASPECT_MANUFACTURING</code> */
	QName ASPECT_MANUFACTURING = QName.createQName(BeCPGModel.BECPG_URI, "manufacturingAspect");
	/** Constant <code>ASSOC_SUBSIDIARY</code> */
	QName ASSOC_SUBSIDIARY = QName.createQName(BeCPGModel.BECPG_URI, "subsidiaryRef");
	/** Constant <code>ASSOC_PLANTS</code> */
	QName ASSOC_PLANTS = QName.createQName(BeCPGModel.BECPG_URI, "plants");
	/** Constant <code>ASSOC_TRADEMARK</code> */
	QName ASSOC_TRADEMARK = QName.createQName(BeCPGModel.BECPG_URI, "trademarkRef");

	// subsidiary
	/** Constant <code>TYPE_SUBSIDIARY</code> */
	QName TYPE_SUBSIDIARY = QName.createQName(BeCPGModel.BECPG_URI, "subsidiary");

	// plant
	/** Constant <code>TYPE_PLANT</code> */
	QName TYPE_PLANT = QName.createQName(BeCPGModel.BECPG_URI, "plant");
	/** Constant <code>ASSOC_PLANT_CERTIFICATIONS</code> */
	QName ASSOC_PLANT_CERTIFICATIONS = QName.createQName(BeCPGModel.BECPG_URI, "plantCertifications");


	// trademark
	/** Constant <code>TYPE_TRADEMARK</code> */
	QName TYPE_TRADEMARK = QName.createQName(BeCPGModel.BECPG_URI, "trademark");
	/** Constant <code>PROP_TRADEMARK_TYPE</code> */
	QName PROP_TRADEMARK_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "trademarkType");
	

	// certification
	/** Constant <code>TYPE_CERTIFICATION</code> */
	QName TYPE_CERTIFICATION = QName.createQName(BeCPGModel.BECPG_URI, "certification");

	// approvalNumber
	/** Constant <code>TYPE_APPROVAL_NUMBER</code> */
	QName TYPE_APPROVAL_NUMBER = QName.createQName(BeCPGModel.BECPG_URI, "approvalNumber");

	// labelClaim
	/** Constant <code>TYPE_LABEL_CLAIM</code> */
	QName TYPE_LABEL_CLAIM = QName.createQName(BeCPGModel.BECPG_URI, "labelClaim");
	/** Constant <code>PROP_LABEL_CLAIM_CODE</code> */
	QName PROP_LABEL_CLAIM_CODE = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimCode");
	/** Constant <code>PROP_LABEL_CLAIM_TYPE</code> */
	QName PROP_LABEL_CLAIM_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimType");
	/** Constant <code>PROP_LABEL_CLAIM_FORMULA</code> */
	QName PROP_LABEL_CLAIM_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimFormula");

	// storageConditions
	/** Constant <code>TYPE_STORAGE_CONDITIONS</code> */
	QName TYPE_STORAGE_CONDITIONS = QName.createQName(BeCPGModel.BECPG_URI, "storageConditions");
	/** Constant <code>ASSOC_STORAGE_CONDITIONS</code> */
	QName ASSOC_STORAGE_CONDITIONS = QName.createQName(BeCPGModel.BECPG_URI, "storageConditionsRef");
	
	// precautionOfUse
	/** Constant <code>TYPE_PRECAUTION_OF_USE</code> */
	QName TYPE_PRECAUTION_OF_USE = QName.createQName(BeCPGModel.BECPG_URI, "precautionOfUse");
	/** Constant <code>ASSOC_PRECAUTION_OF_USE</code> */
	QName ASSOC_PRECAUTION_OF_USE = QName.createQName(BeCPGModel.BECPG_URI, "precautionOfUseRef");

	// instruction
	/** Constant <code>ASPECT_INSTRUCTION</code> */
	QName ASPECT_INSTRUCTION = QName.createQName(BeCPGModel.BECPG_URI, "instruction");
	/** Constant <code>PROP_INSTRUCTION</code> */
	QName PROP_INSTRUCTION = QName.createQName(BeCPGModel.BECPG_URI, "instruction");
	
	@Deprecated
	/** Constant <code>ASPECT_DILUENT</code> */
	QName ASPECT_DILUENT = QName.createQName(BeCPGModel.BECPG_URI, "diluentAspect");
	/** Constant <code>ASPECT_RECONSTITUTABLE</code> */
	QName ASPECT_RECONSTITUTABLE = QName.createQName(BeCPGModel.BECPG_URI, "reconstitutableAspect");
	/** Constant <code>PROP_RECONSTITUTION_RATE</code> */
	QName PROP_RECONSTITUTION_RATE =  QName.createQName(BeCPGModel.BECPG_URI, "reconstitutionRate");
	/** Constant <code>PROP_RECONSTITUTION_PRIORITY</code> */
	QName PROP_RECONSTITUTION_PRIORITY =  QName.createQName(BeCPGModel.BECPG_URI, "reconstitutionPriority");
	/** Constant <code>ASSOC_DILUENT_REF</code> */
	QName ASSOC_DILUENT_REF =  QName.createQName(BeCPGModel.BECPG_URI, "diluentRef");
	/** Constant <code>ASSOC_TARGET_RECONSTITUTION_REF</code> */
	QName ASSOC_TARGET_RECONSTITUTION_REF =  QName.createQName(BeCPGModel.BECPG_URI, "targetReconstitutionRef");
	
	//Compare
	/** Constant <code>ASPECT_COMPARE_WITH_DYN_COLUMN</code> */
	QName ASPECT_COMPARE_WITH_DYN_COLUMN = QName.createQName(BeCPGModel.BECPG_URI, "compareWithDynColumnAspect");
	/** Constant <code>PROP_COMPARE_WITH_DYN_COLUMN</code> */
	QName PROP_COMPARE_WITH_DYN_COLUMN =  QName.createQName(BeCPGModel.BECPG_URI, "compareWithDynColumn");

	//Nutrient profile
	/** Constant <code>ASPECT_NUTRIENT_PROFILING_SCORE</code> */
	QName ASPECT_NUTRIENT_PROFILING_SCORE = QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfilingScoreAspect");
	/** Constant <code>PROP_NUTRIENT_PROFILING_SCORE</code> */
	QName PROP_NUTRIENT_PROFILING_SCORE =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfilingScore");
	/** Constant <code>PROP_NUTRIENT_PROFILING_CLASS</code> */
	QName PROP_NUTRIENT_PROFILING_CLASS =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfilingClass");
	/** Constant <code>PROP_NUTRIENT_PROFILE_SCORE_FORMULA</code> */
	QName PROP_NUTRIENT_PROFILE_SCORE_FORMULA =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileScoreFormula");
	/** Constant <code>PROP_NUTRIENT_PROFILE_CLASS_FORMULA</code> */
	QName PROP_NUTRIENT_PROFILE_CLASS_FORMULA =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileClassFormula");
	/** Constant <code>TYPE_NUTRIENT_PROFILE</code> */
	QName TYPE_NUTRIENT_PROFILE = QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfile");
	
	//Eco score
	QName ASPECT_ECO_SCORE = QName.createQName(BeCPGModel.BECPG_URI, "ecoScoreAspect");
	QName PROP_ECO_SCORE = QName.createQName(BeCPGModel.BECPG_URI, "ecoScore");
	QName PROP_ECO_SCORE_CLASS = QName.createQName(BeCPGModel.BECPG_URI, "ecoScoreClass");
	QName PROP_ECO_SCORE_CATEGORY = QName.createQName(BeCPGModel.BECPG_URI, "ecoScoreCategory");
		
	//Custom codes
	/** Constant <code>TYPE_CUSTOMSCODE</code> */
	QName TYPE_CUSTOMSCODE = QName.createQName(BeCPGModel.BECPG_URI, "customsCode");
	/** Constant <code>PROP_CUSTOMSCODE_CODE</code> */
	QName PROP_CUSTOMSCODE_CODE = QName.createQName(BeCPGModel.BECPG_URI, "cstsCode");
	/** Constant <code>ASPECT_CUSTOMSCODE</code> */
	QName ASPECT_CUSTOMSCODE = QName.createQName(BeCPGModel.BECPG_URI, "customsCodeAspect");
	/** Constant <code>ASSOC_CUSTOMSCODE</code> */
	QName ASSOC_CUSTOMSCODE = QName.createQName(BeCPGModel.BECPG_URI, "customsCodeRef");
	
	// Regulatory code
	/** Constant <code>ASPECT_REGULATORY_CODE</code> */
	QName ASPECT_REGULATORY_CODE= QName.createQName(BeCPGModel.BECPG_URI, "regulatoryCodeAspect");
	/** Constant <code>PROP_REGULATORY_CODE</code> */
	QName PROP_REGULATORY_CODE = QName.createQName(BeCPGModel.BECPG_URI, "regulatoryCode");
	
	//Chemical code
	/** Constant <code>PROP_CAS_NUMBER</code> */
	QName PROP_CAS_NUMBER= QName.createQName(BeCPGModel.BECPG_URI, "casNumber");
	/** Constant <code>PROP_CE_NUMBER</code> */
	QName PROP_CE_NUMBER= QName.createQName(BeCPGModel.BECPG_URI, "ceNumber");
	/** Constant <code>PROP_EC_NUMBER</code> */
	QName PROP_EC_NUMBER= QName.createQName(BeCPGModel.BECPG_URI, "ecNumber");
	/** Constant <code>PROP_FDA_NUMBER</code> */
	QName PROP_FDA_NUMBER= QName.createQName(BeCPGModel.BECPG_URI, "fdaNumber");
	/** Constant <code>PROP_FEMA_NUMBER</code> */
	QName PROP_FEMA_NUMBER= QName.createQName(BeCPGModel.BECPG_URI, "femaNumber");
	/** Constant <code>PROP_FL_NUMBER</code> */
	QName PROP_FL_NUMBER= QName.createQName(BeCPGModel.BECPG_URI, "flNumber");

	
	/** Constant <code>ASPECT_WATER</code> */
	QName ASPECT_WATER = QName.createQName(BeCPGModel.BECPG_URI, "waterAspect");
	/** Constant <code>LABELING_RULE_ASPECT</code> */
	QName LABELING_RULE_ASPECT =  QName.createQName(BeCPGModel.BECPG_URI, "labelingRuleAspect");
	
	/** Constant <code>PROP_MODIFIED_CATALOG1</code> */
	QName PROP_MODIFIED_CATALOG1 =  QName.createQName(BeCPGModel.BECPG_URI, "modifiedCatalog1");
	/** Constant <code>PROP_MODIFIED_CATALOG2</code> */
	QName PROP_MODIFIED_CATALOG2 =  QName.createQName(BeCPGModel.BECPG_URI, "modifiedCatalog2");
	/** Constant <code>PROP_MODIFIED_CATALOG3</code> */
	QName PROP_MODIFIED_CATALOG3 =  QName.createQName(BeCPGModel.BECPG_URI, "modifiedCatalog3");

	
	
}
