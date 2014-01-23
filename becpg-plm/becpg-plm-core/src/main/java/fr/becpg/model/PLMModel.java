/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * beCPG model definition.
 * 
 * @author querephi
 */
public interface PLMModel {

	
	// product
	/** The Constant TYPE_PRODUCT. */
	QName TYPE_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "product");

	// productSpecification
	QName TYPE_PRODUCT_SPECIFICATION = QName.createQName(BeCPGModel.BECPG_URI, "productSpecification");

	// productMicrobioCriteria
	/** The Constant TYPE_PRODUCT_MICROBIO_CRITERIA. */
	QName TYPE_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteria");

	/** The Constant ASSOC_PRODUCT_MICROBIO_CRITERIA. */
	QName ASSOC_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteria");

	QName ASPECT_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteriaAspect");

	/** The Constant PROP_PRODUCT_HIERARCHY1. */
	QName PROP_PRODUCT_HIERARCHY1 = QName.createQName(BeCPGModel.BECPG_URI, "productHierarchy1");

	/** The Constant PROP_PRODUCT_HIERARCHY2. */
	QName PROP_PRODUCT_HIERARCHY2 = QName.createQName(BeCPGModel.BECPG_URI, "productHierarchy2");

	/** The Constant PROP_PRODUCT_STATE. */
	QName PROP_PRODUCT_STATE = QName.createQName(BeCPGModel.BECPG_URI, "productState");

	/** The Constant PROP_PRODUCT_UNIT. */
	QName PROP_PRODUCT_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "productUnit");

	// finishedProduct
	/** The Constant TYPE_FINISHEDPRODUCT. */
	QName TYPE_FINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");
	// semiFinishedProduct
	/** The Constant TYPE_SEMIFINISHEDPRODUCT. */
	QName TYPE_SEMIFINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "semiFinishedProduct");
	// localSemiFinishedProduct
	/** The Constant TYPE_LOCALSEMIFINISHEDPRODUCT. */
	QName TYPE_LOCALSEMIFINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "localSemiFinishedProduct");
	// rawMaterial
	/** The Constant TYPE_RAWMATERIAL. */
	QName TYPE_RAWMATERIAL = QName.createQName(BeCPGModel.BECPG_URI, "rawMaterial");
	// packagingKit
	/** The Constant TYPE_PACKAGINGKIT. */
	QName TYPE_PACKAGINGKIT = QName.createQName(BeCPGModel.BECPG_URI, "packagingKit");
	// packagingMaterial
	/** The Constant TYPE_PACKAGINGMATERIAL. */
	QName TYPE_PACKAGINGMATERIAL = QName.createQName(BeCPGModel.BECPG_URI, "packagingMaterial");
	QName TYPE_RESOURCEPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "resourceProduct");

	QName TYPE_PRODUCTLIST_ITEM = QName.createQName(BeCPGModel.BECPG_URI, "productListItem");

	/** The Constant TYPE_CHARACT. */
	QName TYPE_CHARACT = QName.createQName(BeCPGModel.BECPG_URI, "charact");

	// allergenList
	/** The Constant TYPE_ALLERGENLIST. */
	QName TYPE_ALLERGENLIST = QName.createQName(BeCPGModel.BECPG_URI, "allergenList");

	/** The Constant PROP_ALLERGENLIST_VOLUNTARY. */
	QName PROP_ALLERGENLIST_VOLUNTARY = QName.createQName(BeCPGModel.BECPG_URI, "allergenListVoluntary");

	/** The Constant PROP_ALLERGENLIST_ALLERGEN. */
	QName ASSOC_ALLERGENLIST_ALLERGEN = QName.createQName(BeCPGModel.BECPG_URI, "allergenListAllergen");

	/** The Constant PROP_ALLERGENLIST_INVOLUNTARY. */
	QName PROP_ALLERGENLIST_INVOLUNTARY = QName.createQName(BeCPGModel.BECPG_URI, "allergenListInVoluntary");

	/** The Constant PROP_ALLERGENLIST_VOLUNTARY_SOURCES. */
	QName ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "allergenListVolSources");

	/** The Constant PROP_ALLERGENLIST_INVOLUNTARY_SOURCES. */
	QName ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "allergenListInVolSources");

	// compoList
	/** The Constant TYPE_COMPOLIST. */
	QName TYPE_COMPOLIST = QName.createQName(BeCPGModel.BECPG_URI, "compoList");

	/** The Constant ASSOC_COMPOLIST_PRODUCT. */
	QName ASSOC_COMPOLIST_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "compoListProduct");

	/** The Constant PROP_COMPOLIST_QTY. */
	QName PROP_COMPOLIST_QTY = QName.createQName(BeCPGModel.BECPG_URI, "compoListQty");

	QName PROP_COMPOLIST_QTY_SUB_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "compoListQtySubFormula");

	QName PROP_COMPOLIST_QTY_AFTER_PROCESS = QName.createQName(BeCPGModel.BECPG_URI, "compoListQtyAfterProcess");

	/** The Constant PROP_COMPOLIST_UNIT. */
	QName PROP_COMPOLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "compoListUnit");

	QName PROP_COMPOLIST_LOSS_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListLossPerc");

	QName PROP_COMPOLIST_YIELD_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListYieldPerc");

	/** The Constant PROP_COMPOLIST_DECL_TYPE. */
	QName PROP_COMPOLIST_DECL_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "compoListDeclType");

	QName PROP_COMPOLIST_OVERRUN_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListOverrunPerc");

	QName PROP_COMPOLIST_VOLUME = QName.createQName(BeCPGModel.BECPG_URI, "compoListVolume");

	// packagingList
	QName TYPE_PACKAGINGLIST = QName.createQName(BeCPGModel.BECPG_URI, "packagingList");

	QName ASSOC_PACKAGINGLIST_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "packagingListProduct");

	QName PROP_PACKAGINGLIST_QTY = QName.createQName(BeCPGModel.BECPG_URI, "packagingListQty");

	QName PROP_PACKAGINGLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "packagingListUnit");

	QName PROP_PACKAGINGLIST_PKG_LEVEL = QName.createQName(BeCPGModel.BECPG_URI, "packagingListPkgLevel");

	QName PROP_PACKAGINGLIST_ISMASTER = QName.createQName(BeCPGModel.BECPG_URI, "packagingListIsMaster");

	// costList
	/** The Constant TYPE_COSTLIST. */
	QName TYPE_COSTLIST = QName.createQName(BeCPGModel.BECPG_URI, "costList");

	/** The Constant PROP_COSTLIST_VALUE. */
	QName PROP_COSTLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "costListValue");

	/** The Constant PROP_COSTLIST_UNIT. */
	QName PROP_COSTLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "costListUnit");

	QName PROP_COSTLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "costListMaxi");

	/** The Constant ASSOC_COSTLIST_COST. */
	QName ASSOC_COSTLIST_COST = QName.createQName(BeCPGModel.BECPG_URI, "costListCost");

	/** The Constant PROP_COSTDETAILSLIST_VALUE. */
	QName PROP_COSTDETAILSLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "costDetailsListValue");

	/** The Constant PROP_COSTDETAILSLIST_UNIT. */
	QName PROP_COSTDETAILSLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "costDetailsListUnit");

	QName PROP_COSTDETAILSLIST_PERC = QName.createQName(BeCPGModel.BECPG_URI, "costDetailsListPerc");

	/** The Constant ASSOC_COSTDETAILSLIST_COST. */
	QName ASSOC_COSTDETAILSLIST_COST = QName.createQName(BeCPGModel.BECPG_URI, "costDetailsListCost");

	// priceList
	QName TYPE_PRICELIST = QName.createQName(BeCPGModel.BECPG_URI, "priceList");

	QName ASSOC_PRICELIST_COST = QName.createQName(BeCPGModel.BECPG_URI, "priceListCost");

	QName PROP_PRICELIST_PREF_RANK = QName.createQName(BeCPGModel.BECPG_URI, "priceListPrefRank");

	QName PROP_PRICELIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "priceListValue");

	QName PROP_PRICELIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "priceListUnit");

	QName PROP_PRICELIST_PURCHASE_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "priceListPurchaseQty");

	QName PROP_PRICELIST_PURCHASE_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "priceListPurchaseUnit");

	// ingList
	/** The Constant TYPE_INGLIST. */
	QName TYPE_INGLIST = QName.createQName(BeCPGModel.BECPG_URI, "ingList");

	/** The Constant PROP_INGLIST_QTY_PERC. */
	QName PROP_INGLIST_QTY_PERC = QName.createQName(BeCPGModel.BECPG_URI, "ingListQtyPerc");

	/** The Constant PROP_INGLIST_IS_GMO. */
	QName PROP_INGLIST_IS_GMO = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsGMO");

	/** The Constant PROP_INGLIST_IS_IONIZED. */
	QName PROP_INGLIST_IS_IONIZED = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsIonized");

	QName PROP_ING_LIST_IS_PROCESSING_AID = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsProcessingAid");

	/** The Constant ASSOC_INGLIST_GEO_ORIGIN. */
	QName ASSOC_INGLIST_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "ingListGeoOrigin");

	/** The Constant ASSOC_INGLIST_BIO_ORIGIN. */
	QName ASSOC_INGLIST_BIO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "ingListBioOrigin");

	/** The Constant ASSOC_INGLIST_ING. */
	QName ASSOC_INGLIST_ING = QName.createQName(BeCPGModel.BECPG_URI, "ingListIng");

	// nutList
	/** The Constant TYPE_NUTLIST. */
	QName TYPE_NUTLIST = QName.createQName(BeCPGModel.BECPG_URI, "nutList");

	/** The Constant ASSOC_NUTLIST_NUT. */
	QName ASSOC_NUTLIST_NUT = QName.createQName(BeCPGModel.BECPG_URI, "nutListNut");

	/** The Constant PROP_NUTLIST_VALUE. */
	QName PROP_NUTLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "nutListValue");

	/** The Constant PROP_NUTLIST_UNIT. */
	QName PROP_NUTLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "nutListUnit");
	QName PROP_NUTLIST_MINI = QName.createQName(BeCPGModel.BECPG_URI, "nutListMini");
	QName PROP_NUTLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "nutListMaxi");
	/** The Constant PROP_NUTLIST_GROUP. */
	QName PROP_NUTLIST_GROUP = QName.createQName(BeCPGModel.BECPG_URI, "nutListGroup");

	// organoList
	/** The Constant TYPE_ORGANOLIST. */
	QName TYPE_ORGANOLIST = QName.createQName(BeCPGModel.BECPG_URI, "organoList");

	/** The Constant PROP_ORGANOLIST_VALUE. */
	QName PROP_ORGANOLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "organoListValue");

	/** The Constant PROP_ORGANOLIST_ORGANO. */
	QName ASSOC_ORGANOLIST_ORGANO = QName.createQName(BeCPGModel.BECPG_URI, "organoListOrgano");

	// ingLabelingList
	/** The Constant TYPE_INGLABELINGLIST. */
	QName TYPE_INGLABELINGLIST = QName.createQName(BeCPGModel.BECPG_URI, "ingLabelingList");

	QName TYPE_LABELING_RULE_LIST = QName.createQName(BeCPGModel.BECPG_URI, "labelingRuleList");
	QName PROP_LABELING_RULE_LABEL = QName.createQName(BeCPGModel.BECPG_URI, "lrLabel");

	/** The Constant ASSOC_ILL_GRP. */
	QName ASSOC_ILL_GRP = QName.createQName(BeCPGModel.BECPG_URI, "illGrp");

	/** The Constant PROP_ILL_VALUE. */
	QName PROP_ILL_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "illValue");
	QName PROP_ILL_MANUAL_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "illManualValue");

	// microbioList
	/** The Constant TYPE_MICROBIOLIST. */
	QName TYPE_MICROBIOLIST = QName.createQName(BeCPGModel.BECPG_URI, "microbioList");

	/** The Constant PROP_MICROBIOLIST_VALUE. */
	QName PROP_MICROBIOLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "mblValue");

	/** The Constant PROP_MICROBIOLIST_UNIT. */
	QName PROP_MICROBIOLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "mblUnit");

	/** The Constant PROP_MICROBIOLIST_MAXI. */
	QName PROP_MICROBIOLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "mblMaxi");

	QName PROP_MICROBIOLIST_TEXT_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "mblTextCriteria");

	/** The Constant ASSOC_MICROBIOLIST_MICROBIO. */
	QName ASSOC_MICROBIOLIST_MICROBIO = QName.createQName(BeCPGModel.BECPG_URI, "mblMicrobio");

	// physicoChemList
	/** The Constant TYPE_PHYSICOCHEMLIST. */
	QName TYPE_PHYSICOCHEMLIST = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemList");

	/** The Constant PROP_PHYSICOCHEMLIST_VALUE. */
	QName PROP_PHYSICOCHEMLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "pclValue");

	/** The Constant PROP_PHYSICOCHEMLIST_UNIT. */
	QName PROP_PHYSICOCHEMLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "pclUnit");

	/** The Constant PROP_PHYSICOCHEMLIST_MINI. */
	QName PROP_PHYSICOCHEMLIST_MINI = QName.createQName(BeCPGModel.BECPG_URI, "pclMini");

	/** The Constant PROP_PHYSICOCHEMLIST_MAXI. */
	QName PROP_PHYSICOCHEMLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "pclMaxi");

	/** The Constant ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM. */
	QName ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM = QName.createQName(BeCPGModel.BECPG_URI, "pclPhysicoChem");

	// forbiddenIngList
	QName TYPE_FORBIDDENINGLIST = QName.createQName(BeCPGModel.BECPG_URI, "forbiddenIngList");
	QName PROP_FIL_REQ_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "filReqType");
	QName PROP_FIL_REQ_MESSAGE = QName.createQName(BeCPGModel.BECPG_URI, "filReqMessage");
	QName PROP_FIL_QTY_PERC_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "filQtyPercMaxi");
	QName PROP_FIL_IS_GMO = QName.createQName(BeCPGModel.BECPG_URI, "filIsGMO");
	QName PROP_FIL_IS_IONIZED = QName.createQName(BeCPGModel.BECPG_URI, "filIsIonized");
	QName ASSOC_FIL_INGS = QName.createQName(BeCPGModel.BECPG_URI, "filIngs");
	QName ASSOC_FIL_GEO_ORIGINS = QName.createQName(BeCPGModel.BECPG_URI, "filGeoOrigins");
	QName ASSOC_FIL_BIO_ORIGINS = QName.createQName(BeCPGModel.BECPG_URI, "filBioOrigins");

	// reqCtrlList
	QName TYPE_REQCTRLLIST = QName.createQName(BeCPGModel.BECPG_URI, "reqCtrlList");
	QName PROP_RCL_REQ_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "rclReqType");
	QName PROP_RCL_REQ_MESSAGE = QName.createQName(BeCPGModel.BECPG_URI, "rclReqMessage");
	QName ASSOC_RCL_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "rclSources");

	QName TYPE_DYNAMICCHARACTLIST = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactList");

	QName PROP_DYNAMICCHARACT_TITLE = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactTitle");

	QName PROP_DYNAMICCHARACT_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactFormula");

	QName PROP_DYNAMICCHARACT_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactValue");

	QName PROP_DYNAMICCHARACT_GROUP_COLOR = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactGroupColor");

	// contactList
	QName TYPE_CONTACTLIST = QName.createQName(BeCPGModel.BECPG_URI, "contactList");

	// labelClaimList
	QName TYPE_LABELCLAIMLIST = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimList");
	QName PROP_LCL_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "lclType");
	QName PROP_LCL_IS_CLAIMED = QName.createQName(BeCPGModel.BECPG_URI, "lclIsClaimed");
	QName ASSOC_LCL_LABELCLAIM = QName.createQName(BeCPGModel.BECPG_URI, "lclLabelClaim");

	// allergen
	/** The Constant TYPE_ALLERGEN. */
	QName TYPE_ALLERGEN = QName.createQName(BeCPGModel.BECPG_URI, "allergen");

	/** The Constant PROP_ALLERGEN_TYPE. */
	QName PROP_ALLERGEN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "allergenType");

	// cost
	/** The Constant TYPE_COST. */
	QName TYPE_COST = QName.createQName(BeCPGModel.BECPG_URI, "cost");

	/** The Constant PROP_COSTCURRENCY. */
	QName PROP_COSTCURRENCY = QName.createQName(BeCPGModel.BECPG_URI, "costCurrency");
	QName PROP_COSTFIXED = QName.createQName(BeCPGModel.BECPG_URI, "costFixed");

	// ing
	/** The Constant TYPE_ING. */
	QName TYPE_ING = QName.createQName(BeCPGModel.BECPG_URI, "ing");

	/** The Constant PROP_ING_CEECODE. */
	QName PROP_ING_CEECODE = QName.createQName(BeCPGModel.BECPG_URI, "ingCEECode");

	/** The Constant PROP_ING_TYPE. */

	QName PROP_ING_TYPE_V2 = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeV2");

	QName TYPE_ING_TYPE_ITEM = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeItem");

	// microbio
	/** The Constant TYPE_MICROBIO. */
	QName TYPE_MICROBIO = QName.createQName(BeCPGModel.BECPG_URI, "microbio");

	// geoOrigin
	/** The Constant TYPE_GEO_ORIGIN. */
	QName TYPE_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "geoOrigin");

	/** The Constant PROP_GEO_ORIGIN_ISOCODE. */
	QName PROP_GEO_ORIGIN_ISOCODE = QName.createQName(BeCPGModel.BECPG_URI, "bioOriginISOCode");

	// bioOrigin
	/** The Constant TYPE_BIO_ORIGIN. */
	QName TYPE_BIO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "bioOrigin");

	/** The Constant PROP_BIO_ORIGIN_TYPE. */
	QName PROP_BIO_ORIGIN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "bioOriginType");

	// nut
	/** The Constant TYPE_NUT. */
	QName TYPE_NUT = QName.createQName(BeCPGModel.BECPG_URI, "nut");

	/** The Constant PROP_NUTGROUP. */
	QName PROP_NUTGROUP = QName.createQName(BeCPGModel.BECPG_URI, "nutGroup");

	/** The Constant PROP_NUTTYPE. */
	QName PROP_NUTTYPE = QName.createQName(BeCPGModel.BECPG_URI, "nutType");

	/** The Constant PROP_NUTUNIT. */
	QName PROP_NUTUNIT = QName.createQName(BeCPGModel.BECPG_URI, "nutUnit");

	QName PROP_NUTGDA = QName.createQName(BeCPGModel.BECPG_URI, "nutGDA");

	// organo
	/** The Constant TYPE_ORGANO. */
	QName TYPE_ORGANO = QName.createQName(BeCPGModel.BECPG_URI, "organo");

	// physicoChem
	/** The Constant TYPE_PHYSICO_CHEM. */
	QName TYPE_PHYSICO_CHEM = QName.createQName(BeCPGModel.BECPG_URI, "physicoChem");

	QName PROP_PHYSICO_CHEM_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemUnit");
	QName PROP_PHYSICO_CHEM_FORMULATED = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormulated");

	// supplier
	/** The Constant ASPECT_SUPPLIERS. */
	QName ASPECT_SUPPLIERS = QName.createQName(BeCPGModel.BECPG_URI, "suppliersAspect");

	/** The Constant ASSOC_SUPPLIERS. */
	QName ASSOC_SUPPLIERS = QName.createQName(BeCPGModel.BECPG_URI, "suppliers");

	/** The Constant TYPE_SUPPLIER. */
	QName TYPE_SUPPLIER = QName.createQName(BeCPGModel.BECPG_URI, "supplier");

	// client
	/** The Constant ASPECT_CLIENTS. */
	QName ASPECT_CLIENTS = QName.createQName(BeCPGModel.BECPG_URI, "clientsAspect");

	/** The Constant ASSOC_CLIENTS. */
	QName ASSOC_CLIENTS = QName.createQName(BeCPGModel.BECPG_URI, "clients");

	/** The Constant TYPE_CLIENT. */
	QName TYPE_CLIENT = QName.createQName(BeCPGModel.BECPG_URI, "client");

	// product aspect
	/** The Constant ASPECT_PRODUCT. */
	QName ASPECT_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "productAspect");

	/** The Constant ASPECT_TRANSFORMATION. */
	QName ASPECT_TRANSFORMATION = QName.createQName(BeCPGModel.BECPG_URI, "transformationAspect");
	QName PROP_PRODUCT_QTY = QName.createQName(BeCPGModel.BECPG_URI, "productQty");
	QName PROP_PRODUCT_DENSITY = QName.createQName(BeCPGModel.BECPG_URI, "productDensity");
	QName PROP_PRODUCT_NET_WEIGHT = QName.createQName(BeCPGModel.BECPG_URI, "netWeight");

	QName ASSOC_PRODUCT_SPECIFICATIONS = QName.createQName(BeCPGModel.BECPG_URI, "productSpecifications");
	QName PROP_PRODUCT_COMMENTS = QName.createQName(BeCPGModel.BECPG_URI, "productComments");

	// ean aspect
	/** The Constant ASPECT_EAN. */
	QName ASPECT_EAN = QName.createQName(BeCPGModel.BECPG_URI, "eanAspect");

	/** The Constant PROP_EAN_CODE. */
	QName PROP_EAN_CODE = QName.createQName(BeCPGModel.BECPG_URI, "eanCode");

	// code aspect
	QName ASPECT_ERP_CODE = QName.createQName(BeCPGModel.BECPG_URI, "erpCodeAspect");
	QName PROP_ERP_CODE = QName.createQName(BeCPGModel.BECPG_URI, "erpCode");

	/**
	 * Variants
	 */
	QName TYPE_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "variant");
	QName ASPECT_ENTITY_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "entityVariantAspect");
	QName ASPECT_ENTITYLIST_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "entityListVariantAspect");

	QName ASSOC_VARIANTS = QName.createQName(BeCPGModel.BECPG_URI, "variants");
	QName PROP_VARIANTIDS = QName.createQName(BeCPGModel.BECPG_URI, "variantIds");
	QName PROP_IS_DEFAULT_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "isDefaultVariant");

	/**
	 * Profitability
	 */
	QName ASPECT_PROFITABILITY = QName.createQName(BeCPGModel.BECPG_URI, "profitabilityAspect");

	QName PROP_UNIT_PRICE = QName.createQName(BeCPGModel.BECPG_URI, "unitPrice");

	QName PROP_BREAK_EVEN = QName.createQName(BeCPGModel.BECPG_URI, "breakEven");

	QName PROP_PROJECTED_QTY = QName.createQName(BeCPGModel.BECPG_URI, "projectedQty");

	QName PROP_PROFITABILITY = QName.createQName(BeCPGModel.BECPG_URI, "profitability");

	QName PROP_UNIT_TOTAL_COST = QName.createQName(BeCPGModel.BECPG_URI, "unitTotalCost");

	QName PROP_PRICE_CURRENCY = QName.createQName(BeCPGModel.BECPG_URI, "priceCurrency");

	/**
	 * manufacturingAspect
	 */
	QName ASPECT_MANUFACTURING = QName.createQName(BeCPGModel.BECPG_URI, "manufacturingAspect");
	QName ASSOC_SUBSIDIARY = QName.createQName(BeCPGModel.BECPG_URI, "subsidiary");
	QName ASSOC_PLANTS = QName.createQName(BeCPGModel.BECPG_URI, "plants");
	QName ASSOC_TRADEMARK = QName.createQName(BeCPGModel.BECPG_URI, "trademark");

	/**
	 * subsidiary
	 */
	QName TYPE_SUBSIDIARY = QName.createQName(BeCPGModel.BECPG_URI, "subsidiary");

	/**
	 * plant
	 */
	QName TYPE_PLANT = QName.createQName(BeCPGModel.BECPG_URI, "plant");
	QName ASSOC_PLANT_CERTIFICATIONS = QName.createQName(BeCPGModel.BECPG_URI, "plantCertifications");
	QName ASSOC_PLANT_APPROVAL_NUMBERS = QName.createQName(BeCPGModel.BECPG_URI, "plantApprovalNumbers");

	/**
	 * trademark
	 */
	QName TYPE_TRADEMARK = QName.createQName(BeCPGModel.BECPG_URI, "trademark");

	/**
	 * certification
	 */
	QName TYPE_CERTIFICATION = QName.createQName(BeCPGModel.BECPG_URI, "certification");

	/**
	 * approvalNumber
	 */
	QName TYPE_APPROVAL_NUMBER = QName.createQName(BeCPGModel.BECPG_URI, "approvalNumber");

	QName TYPE_LABEL_CLAIM = QName.createQName(BeCPGModel.BECPG_URI, "labelClaim");
	QName PROP_LABEL_CLAIM_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimType");
	QName PROP_LABEL_CLAIM_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimFormula");

	QName TYPE_STORAGE_CONDITIONS = QName.createQName(BeCPGModel.BECPG_URI, "storageConditions");
	QName ASSOC_STORAGE_CONDITIONS = QName.createQName(BeCPGModel.BECPG_URI, "storageConditions");
	QName TYPE_PRECAUTION_OF_USE = QName.createQName(BeCPGModel.BECPG_URI, "precautionOfUse");
	QName ASSOC_PRECAUTION_OF_USE = QName.createQName(BeCPGModel.BECPG_URI, "precautionOfUse");


}
