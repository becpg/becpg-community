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
public interface BeCPGModel {

	//
	// Namespace
	//

	// Food Model URI
	/** The Constant BECPG_URI. */
	static final String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";

	// Food Model Prefix
	/** The Constant BECPG_PREFIX. */
	static final String BECPG_PREFIX = "bcpg";

	//
	// Product Model Definitions
	//
	/** The Constant MODEL. */
	static final QName MODEL = QName.createQName(BECPG_URI, "bcpgmodel");
	
	// productSpecification
	static final QName TYPE_PRODUCT_SPECIFICATION = QName.createQName(BECPG_URI,
	"productSpecification");
	
	// productMicrobioCriteria
	/** The Constant TYPE_PRODUCT_MICROBIO_CRITERIA. */
	static final QName TYPE_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BECPG_URI,
	"productMicrobioCriteria");
	
	/** The Constant ASSOC_PRODUCT_MICROBIO_CRITERIA. */
	static final QName ASSOC_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BECPG_URI,
	"productMicrobioCriteria");
	
	static final QName ASPECT_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BECPG_URI,
	"productMicrobioCriteriaAspect");
	
	static final QName ASPECT_ENTITYLISTS = QName.createQName(
			BECPG_URI, "entityListsAspect");

	// product
	/** The Constant TYPE_PRODUCT. */
	static final QName TYPE_PRODUCT = QName.createQName(BECPG_URI, "product");
	
	/** The Constant PROP_PRODUCT_LEGALNAME. */
	static final QName PROP_PRODUCT_LEGALNAME = QName.createQName(BECPG_URI,
			"legalName");
	
	/** The Constant PROP_PRODUCT_HIERARCHY1. */
	static final QName PROP_PRODUCT_HIERARCHY1 = QName.createQName(BECPG_URI,
			"productHierarchy1");
	
	/** The Constant PROP_PRODUCT_HIERARCHY2. */
	static final QName PROP_PRODUCT_HIERARCHY2 = QName.createQName(BECPG_URI,
			"productHierarchy2");
	
	/** The Constant PROP_PRODUCT_STATE. */
	static final QName PROP_PRODUCT_STATE = QName.createQName(BECPG_URI,
			"productState");
	
	/** The Constant PROP_PRODUCT_UNIT. */
	static final QName PROP_PRODUCT_UNIT = QName.createQName(BECPG_URI,
	"productUnit");	
		
	// finishedProduct
	/** The Constant TYPE_FINISHEDPRODUCT. */
	static final QName TYPE_FINISHEDPRODUCT = QName.createQName(BECPG_URI,
			"finishedProduct");
	// semiFinishedProduct
	/** The Constant TYPE_SEMIFINISHEDPRODUCT. */
	static final QName TYPE_SEMIFINISHEDPRODUCT = QName.createQName(BECPG_URI,
			"semiFinishedProduct");
	// localSemiFinishedProduct
	/** The Constant TYPE_LOCALSEMIFINISHEDPRODUCT. */
	static final QName TYPE_LOCALSEMIFINISHEDPRODUCT = QName.createQName(
			BECPG_URI, "localSemiFinishedProduct");
	// rawMaterial
	/** The Constant TYPE_RAWMATERIAL. */
	static final QName TYPE_RAWMATERIAL = QName.createQName(BECPG_URI,
			"rawMaterial");
	// packagingKit
	/** The Constant TYPE_PACKAGINGKIT. */
	static final QName TYPE_PACKAGINGKIT = QName.createQName(BECPG_URI,
			"packagingKit");
	// packagingMaterial
	/** The Constant TYPE_PACKAGINGMATERIAL. */
	static final QName TYPE_PACKAGINGMATERIAL = QName.createQName(BECPG_URI,
			"packagingMaterial");
	static final QName TYPE_RESOURCEPRODUCT = QName.createQName(BECPG_URI,
			"resourceProduct");

	/** The Constant ASSOC_ENTITYLISTS. */
	static final QName ASSOC_ENTITYLISTS = QName.createQName(BECPG_URI,
			"entityLists");
	
	static final QName TYPE_ENTITYLIST_ITEM = QName.createQName(BECPG_URI,
	"entityListItem");
	
	static final QName TYPE_PRODUCTLIST_ITEM = QName.createQName(BECPG_URI,
	"productListItem");
	
	/** The Constant TYPE_CHARACT. */
	static final QName TYPE_CHARACT = QName.createQName(BECPG_URI,
			"charact");
	
	// allergenList
	/** The Constant TYPE_ALLERGENLIST. */
	static final QName TYPE_ALLERGENLIST = QName.createQName(BECPG_URI,
			"allergenList");
	
	/** The Constant PROP_ALLERGENLIST_VOLUNTARY. */
	static final QName PROP_ALLERGENLIST_VOLUNTARY = QName.createQName(
			BECPG_URI, "allergenListVoluntary");
	
	/** The Constant PROP_ALLERGENLIST_ALLERGEN. */
	static final QName ASSOC_ALLERGENLIST_ALLERGEN = QName.createQName(
			BECPG_URI, "allergenListAllergen");
	
	/** The Constant PROP_ALLERGENLIST_INVOLUNTARY. */
	static final QName PROP_ALLERGENLIST_INVOLUNTARY = QName
			.createQName(BECPG_URI, "allergenListInVoluntary");
	
	/** The Constant PROP_ALLERGENLIST_VOLUNTARY_SOURCES. */
	static final QName ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES = QName.createQName(
			BECPG_URI, "allergenListVolSources");
	
	/** The Constant PROP_ALLERGENLIST_INVOLUNTARY_SOURCES. */
	static final QName ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES = QName.createQName(
			BECPG_URI, "allergenListInVolSources");

	// compoList
	/** The Constant TYPE_COMPOLIST. */
	static final QName TYPE_COMPOLIST = QName.createQName(BECPG_URI,
			"compoList");
	
	/** The Constant ASSOC_COMPOLIST_PRODUCT. */
	static final QName ASSOC_COMPOLIST_PRODUCT = QName.createQName(BECPG_URI,
			"compoListProduct");
	
	/** The Constant PROP_COMPOLIST_QTY. */
	static final QName PROP_COMPOLIST_QTY = QName.createQName(BECPG_URI,
			"compoListQty");
	
	static final QName PROP_COMPOLIST_QTY_SUB_FORMULA = QName.createQName(BECPG_URI,
	"compoListQtySubFormula");
	
	static final QName PROP_COMPOLIST_QTY_AFTER_PROCESS = QName.createQName(BECPG_URI,
	"compoListQtyAfterProcess");
	
	/** The Constant PROP_COMPOLIST_UNIT. */
	static final QName PROP_COMPOLIST_UNIT = QName.createQName(BECPG_URI,
	"compoListUnit");
	
	static final QName PROP_COMPOLIST_LOSS_PERC = QName.createQName(BECPG_URI,
	"compoListLossPerc");
	
	static final QName PROP_COMPOLIST_YIELD_PERC = QName.createQName(BECPG_URI,
			"compoListYieldPerc");
	
	/** The Constant PROP_COMPOLIST_DECL_TYPE. */
	static final QName PROP_COMPOLIST_DECL_TYPE = QName.createQName(
			BECPG_URI, "compoListDeclType");	
	
	// packagingList
	static final QName TYPE_PACKAGINGLIST = QName.createQName(BECPG_URI,
			"packagingList");
	
	static final QName ASSOC_PACKAGINGLIST_PRODUCT = QName.createQName(BECPG_URI,
			"packagingListProduct");
		
	static final QName PROP_PACKAGINGLIST_QTY = QName.createQName(BECPG_URI,
			"packagingListQty");
		
	static final QName PROP_PACKAGINGLIST_UNIT = QName.createQName(BECPG_URI,
			"packagingListUnit");
	
	static final QName PROP_PACKAGINGLIST_PKG_LEVEL = QName.createQName(
			BECPG_URI, "packagingListPkgLevel");	
	
	static final QName PROP_PACKAGINGLIST_ISMASTER = QName.createQName(
			BECPG_URI, "packagingListIsMaster");

	// costList
	/** The Constant TYPE_COSTLIST. */
	static final QName TYPE_COSTLIST = QName.createQName(BECPG_URI, "costList");
	
	/** The Constant PROP_COSTLIST_VALUE. */
	static final QName PROP_COSTLIST_VALUE = QName.createQName(BECPG_URI,
			"costListValue");
	
	/** The Constant PROP_COSTLIST_UNIT. */
	static final QName PROP_COSTLIST_UNIT = QName.createQName(BECPG_URI,
			"costListUnit");
	
	static final QName PROP_COSTLIST_MAXI = QName.createQName(BECPG_URI,
			"costListMaxi");
	
	/** The Constant ASSOC_COSTLIST_COST. */
	static final QName ASSOC_COSTLIST_COST = QName.createQName(BECPG_URI,
			"costListCost");
	
	/** The Constant PROP_COSTDETAILSLIST_VALUE. */
	static final QName PROP_COSTDETAILSLIST_VALUE = QName.createQName(BECPG_URI,
			"costDetailsListValue");
	
	/** The Constant PROP_COSTDETAILSLIST_UNIT. */
	static final QName PROP_COSTDETAILSLIST_UNIT = QName.createQName(BECPG_URI,
			"costDetailsListUnit");
	
	static final QName PROP_COSTDETAILSLIST_PERC = QName.createQName(BECPG_URI,
			"costDetailsListPerc");
	
	/** The Constant ASSOC_COSTDETAILSLIST_COST. */
	static final QName ASSOC_COSTDETAILSLIST_COST = QName.createQName(BECPG_URI,
			"costDetailsListCost");
	
	/** The Constant ASSOC_COSTDETAILSLIST_SOURCE. */
	static final QName ASSOC_COSTDETAILSLIST_SOURCE = QName.createQName(BECPG_URI,
			"costDetailsListSource");
	
	// priceList
	static final QName TYPE_PRICELIST = QName.createQName(BECPG_URI, "priceList");
	
	static final QName ASSOC_PRICELIST_COST = QName.createQName(BECPG_URI,
			"priceListCost");
	
	static final QName PROP_PRICELIST_PREF_RANK = QName.createQName(BECPG_URI,
			"priceListPrefRank");
	
	static final QName PROP_PRICELIST_VALUE = QName.createQName(BECPG_URI,
			"priceListValue");
	
	static final QName PROP_PRICELIST_UNIT = QName.createQName(BECPG_URI,
			"priceListUnit");
	
	static final QName PROP_PRICELIST_PURCHASE_VALUE = QName.createQName(BECPG_URI,
			"priceListPurchaseQty");
	
	static final QName PROP_PRICELIST_PURCHASE_UNIT = QName.createQName(BECPG_URI,
			"priceListPurchaseUnit");
			
	// ingList
	/** The Constant TYPE_INGLIST. */
	static final QName TYPE_INGLIST = QName.createQName(BECPG_URI, "ingList");
	
	/** The Constant PROP_INGLIST_QTY_PERC. */
	static final QName PROP_INGLIST_QTY_PERC = QName.createQName(BECPG_URI,
			"ingListQtyPerc");
	
	/** The Constant PROP_INGLIST_IS_GMO. */
	static final QName PROP_INGLIST_IS_GMO = QName.createQName(BECPG_URI,
			"ingListIsGMO");
	
	/** The Constant PROP_INGLIST_IS_IONIZED. */
	static final QName PROP_INGLIST_IS_IONIZED = QName.createQName(BECPG_URI,
	"ingListIsIonized");
	
	/** The Constant ASSOC_INGLIST_GEO_ORIGIN. */
	static final QName ASSOC_INGLIST_GEO_ORIGIN = QName.createQName(BECPG_URI,
			"ingListGeoOrigin");
	
	/** The Constant ASSOC_INGLIST_BIO_ORIGIN. */
	static final QName ASSOC_INGLIST_BIO_ORIGIN = QName.createQName(BECPG_URI,
			"ingListBioOrigin");
	
	/** The Constant ASSOC_INGLIST_ING. */
	static final QName ASSOC_INGLIST_ING = QName.createQName(BECPG_URI,
			"ingListIng");

	// nutList
	/** The Constant TYPE_NUTLIST. */
	static final QName TYPE_NUTLIST = QName.createQName(BECPG_URI, "nutList");
	
	/** The Constant ASSOC_NUTLIST_NUT. */
	static final QName ASSOC_NUTLIST_NUT = QName.createQName(BECPG_URI,
			"nutListNut");
	
	/** The Constant PROP_NUTLIST_VALUE. */
	static final QName PROP_NUTLIST_VALUE = QName.createQName(BECPG_URI,
			"nutListValue");
	
	/** The Constant PROP_NUTLIST_UNIT. */
	static final QName PROP_NUTLIST_UNIT = QName.createQName(BECPG_URI,
			"nutListUnit");
	static final QName PROP_NUTLIST_MINI = QName.createQName(BECPG_URI,
	"nutListMini");
	static final QName PROP_NUTLIST_MAXI = QName.createQName(BECPG_URI,
	"nutListMaxi");
	/** The Constant PROP_NUTLIST_GROUP. */
	static final QName PROP_NUTLIST_GROUP = QName.createQName(BECPG_URI,
			"nutListGroup");

	// organoList
	/** The Constant TYPE_ORGANOLIST. */
	static final QName TYPE_ORGANOLIST = QName.createQName(BECPG_URI,
			"organoList");
	
	/** The Constant PROP_ORGANOLIST_VALUE. */
	static final QName PROP_ORGANOLIST_VALUE = QName.createQName(BECPG_URI,
			"organoListValue");
	
	/** The Constant PROP_ORGANOLIST_ORGANO. */
	static final QName ASSOC_ORGANOLIST_ORGANO = QName.createQName(BECPG_URI,
			"organoListOrgano");

	// ingLabelingList
	/** The Constant TYPE_INGLABELINGLIST. */
	static final QName TYPE_INGLABELINGLIST = QName.createQName(BECPG_URI,
			"ingLabelingList");
	
	/** The Constant ASSOC_ILL_GRP. */
	static final QName ASSOC_ILL_GRP = QName.createQName(BECPG_URI, "illGrp");
	
	/** The Constant PROP_ILL_VALUE. */
	static final QName PROP_ILL_VALUE = QName
			.createQName(BECPG_URI, "illValue");

	// microbioList
	/** The Constant TYPE_MICROBIOLIST. */
	static final QName TYPE_MICROBIOLIST = QName.createQName(BECPG_URI,
			"microbioList");
	
	/** The Constant PROP_MICROBIOLIST_VALUE. */
	static final QName PROP_MICROBIOLIST_VALUE = QName.createQName(BECPG_URI,
			"mblValue");
	
	/** The Constant PROP_MICROBIOLIST_UNIT. */
	static final QName PROP_MICROBIOLIST_UNIT = QName.createQName(BECPG_URI,
			"mblUnit");
	
	/** The Constant PROP_MICROBIOLIST_MAXI. */
	static final QName PROP_MICROBIOLIST_MAXI = QName.createQName(BECPG_URI,
			"mblMaxi");
	
	static final QName PROP_MICROBIOLIST_TEXT_CRITERIA = QName.createQName(BECPG_URI,
			"mblTextCriteria");
	
	/** The Constant ASSOC_MICROBIOLIST_MICROBIO. */
	static final QName ASSOC_MICROBIOLIST_MICROBIO = QName.createQName(
			BECPG_URI, "mblMicrobio");

	// physicoChemList
	/** The Constant TYPE_PHYSICOCHEMLIST. */
	static final QName TYPE_PHYSICOCHEMLIST = QName.createQName(BECPG_URI,
			"physicoChemList");
	
	/** The Constant PROP_PHYSICOCHEMLIST_VALUE. */
	static final QName PROP_PHYSICOCHEMLIST_VALUE = QName.createQName(
			BECPG_URI, "pclValue");
	
	/** The Constant PROP_PHYSICOCHEMLIST_UNIT. */
	static final QName PROP_PHYSICOCHEMLIST_UNIT = QName.createQName(BECPG_URI,
			"pclUnit");
	
	/** The Constant PROP_PHYSICOCHEMLIST_MINI. */
	static final QName PROP_PHYSICOCHEMLIST_MINI = QName.createQName(BECPG_URI,
			"pclMini");
	
	/** The Constant PROP_PHYSICOCHEMLIST_MAXI. */
	static final QName PROP_PHYSICOCHEMLIST_MAXI = QName.createQName(BECPG_URI,
			"pclMaxi");
	
	/** The Constant ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM. */
	static final QName ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM = QName.createQName(
			BECPG_URI, "pclPhysicoChem");
	
	// forbiddenIngList
	static final QName TYPE_FORBIDDENINGLIST = QName.createQName(BECPG_URI,
			"forbiddenIngList");
	static final QName PROP_FIL_REQ_TYPE = QName.createQName(BECPG_URI,
			"filReqType");
	static final QName PROP_FIL_REQ_MESSAGE = QName.createQName(BECPG_URI,
			"filReqMessage");
	static final QName PROP_FIL_QTY_PERC_MAXI = QName.createQName(BECPG_URI,
			"filQtyPercMaxi");
	static final QName PROP_FIL_IS_GMO = QName.createQName(BECPG_URI,
			"filIsGMO");
	static final QName PROP_FIL_IS_IONIZED = QName.createQName(BECPG_URI,
			"filIsIonized");
	static final QName ASSOC_FIL_INGS = QName.createQName(BECPG_URI,
			"filIngs");
	static final QName ASSOC_FIL_GEO_ORIGINS = QName.createQName(BECPG_URI,
			"filGeoOrigins");
	static final QName ASSOC_FIL_BIO_ORIGINS = QName.createQName(BECPG_URI,
			"filBioOrigins");		
	
	// reqCtrlList
	static final QName TYPE_REQCTRLLIST = QName.createQName(BECPG_URI,
			"reqCtrlList");
	static final QName PROP_RCL_REQ_TYPE = QName.createQName(BECPG_URI,
			"rclReqType");
	static final QName PROP_RCL_REQ_MESSAGE = QName.createQName(BECPG_URI,
			"rclReqMessage");	
	static final QName ASSOC_RCL_SOURCES = QName.createQName(BECPG_URI,
			"rclSources");
	

	static final QName TYPE_DYNAMICCHARACTLIST = QName.createQName(BECPG_URI,
			"dynamicCharactList");
	
	static final QName PROP_DYNAMICCHARACT_TITLE = QName.createQName(BECPG_URI,
			"dynamicCharactTitle");
	
	static final QName PROP_DYNAMICCHARACT_FORMULA = QName.createQName(BECPG_URI,
			"dynamicCharactFormula");
	
	static final QName PROP_DYNAMICCHARACT_VALUE = QName.createQName(BECPG_URI,
			"dynamicCharactValue");
	
	static final QName PROP_DYNAMICCHARACT_GROUP_COLOR = QName.createQName(BECPG_URI,
			"dynamicCharactGroupColor");

	
	// contactList
	static final QName TYPE_CONTACTLIST = QName.createQName(BECPG_URI,
			"contactList");
		
	// allergen
	/** The Constant TYPE_ALLERGEN. */
	static final QName TYPE_ALLERGEN = QName.createQName(BECPG_URI, "allergen");
	
	/** The Constant PROP_ALLERGEN_TYPE. */
	static final QName PROP_ALLERGEN_TYPE = QName.createQName(BECPG_URI, "allergenType");

	// cost
	/** The Constant TYPE_COST. */
	static final QName TYPE_COST = QName.createQName(BECPG_URI, "cost");
	
	/** The Constant PROP_COSTCURRENCY. */
	static final QName PROP_COSTCURRENCY = QName.createQName(BECPG_URI, "costCurrency");
	static final QName PROP_COSTFIXED = QName.createQName(BECPG_URI, "costFixed");

	// ing
	/** The Constant TYPE_ING. */
	static final QName TYPE_ING = QName.createQName(BECPG_URI, "ing");
	
	/** The Constant PROP_ING_CEECODE. */
	static final QName PROP_ING_CEECODE = QName.createQName(BECPG_URI,
			"ingCEECode");
	
	/** The Constant PROP_ING_TYPE. */
	static final QName PROP_ING_TYPE = QName.createQName(BECPG_URI, "ingType");

	// microbio
	/** The Constant TYPE_MICROBIO. */
	static final QName TYPE_MICROBIO = QName.createQName(BECPG_URI, "microbio");
	
	// geoOrigin
	/** The Constant TYPE_GEO_ORIGIN. */
	static final QName TYPE_GEO_ORIGIN = QName.createQName(BECPG_URI, "geoOrigin");
	
	/** The Constant PROP_GEO_ORIGIN_ISOCODE. */
	static final QName PROP_GEO_ORIGIN_ISOCODE = QName.createQName(BECPG_URI, "bioOriginISOCode");
	
	// bioOrigin
	/** The Constant TYPE_BIO_ORIGIN. */
	static final QName TYPE_BIO_ORIGIN = QName.createQName(BECPG_URI, "bioOrigin");
	
	/** The Constant PROP_BIO_ORIGIN_TYPE. */
	static final QName PROP_BIO_ORIGIN_TYPE = QName.createQName(BECPG_URI, "bioOriginType");

	// nut
	/** The Constant TYPE_NUT. */
	static final QName TYPE_NUT = QName.createQName(BECPG_URI, "nut");
	
	/** The Constant PROP_NUTGROUP. */
	static final QName PROP_NUTGROUP = QName.createQName(BECPG_URI, "nutGroup");
	
	/** The Constant PROP_NUTTYPE. */
	static final QName PROP_NUTTYPE = QName.createQName(BECPG_URI, "nutType");
	
	/** The Constant PROP_NUTUNIT. */
	static final QName PROP_NUTUNIT = QName.createQName(BECPG_URI, "nutUnit");

	// organo
	/** The Constant TYPE_ORGANO. */
	static final QName TYPE_ORGANO = QName.createQName(BECPG_URI, "organo");

	// physicoChem
	/** The Constant TYPE_PHYSICO_CHEM. */
	static final QName TYPE_PHYSICO_CHEM = QName.createQName(BECPG_URI,
			"physicoChem");
	
	static final QName PROP_PHYSICO_CHEM_FORMULATED = QName.createQName(BECPG_URI, "physicoChemFormulated");

	// linkedValue
	/** The Constant TYPE_LINKED_VALUE. */
	static final QName TYPE_LINKED_VALUE = QName.createQName(BECPG_URI,
			"linkedValue");
	
	// lkvValue
	/** The Constant PROP_LNK_VALUE. */
	static final QName PROP_LKV_VALUE = QName.createQName(BECPG_URI,
			"lkvValue");

	// listValue
	/** The Constant TYPE_LIST_VALUE. */
	static final QName TYPE_LIST_VALUE = QName.createQName(BECPG_URI,
			"listValue");

	/** entityFolder */
	static final QName TYPE_ENTITY_FOLDER = QName.createQName(BECPG_URI,
			"entityFolder");
	
	static final QName PROP_ENTITY_FOLDER_CLASS_NAME = QName.createQName(
			BECPG_URI, "entityFolderClassName");
	
	/** entity */
	static final QName TYPE_ENTITY = QName.createQName(BECPG_URI,
	"entity");


	static final QName TYPE_SYSTEM_ENTITY = QName.createQName(BECPG_URI,
			"systemEntity");
	
	// autoNum
	/** The Constant TYPE_AUTO_NUM. */
	static final QName TYPE_AUTO_NUM = QName.createQName(BECPG_URI,
			"autoNum");
	
	/** The Constant PROP_AUTO_NUM_CLASS_NAME. */
	static final QName PROP_AUTO_NUM_CLASS_NAME = QName.createQName(
			BECPG_URI, "autoNumClassName");
	
	/** The Constant PROP_AUTO_NUM_PROPERTY_NAME. */
	static final QName PROP_AUTO_NUM_PROPERTY_NAME = QName.createQName(
			BECPG_URI, "autoNumPropertyName");
	
	/** The Constant PROP_AUTO_NUM_VALUE. */
	static final QName PROP_AUTO_NUM_VALUE = QName.createQName(
			BECPG_URI, "autoNumValue");
	
	/** The Constant PROP_AUTO_NUM_VALUE. */
	static final QName PROP_AUTO_NUM_PREFIX = QName.createQName(
			BECPG_URI, "autoNumPrefix");
	
	// entityTpl aspect
	static final QName ASPECT_ENTITY_TPL = QName.createQName(BECPG_URI,
	"entityTplAspect");	
	static final QName PROP_ENTITY_TPL_CLASS_NAME = QName.createQName(BECPG_URI,
	"entityTplClassName");
	static final QName PROP_ENTITY_TPL_ENABLED = QName.createQName(BECPG_URI,
	"entityTplEnabled");
	
	// supplier
	/** The Constant ASPECT_SUPPLIERS. */
	static final QName ASPECT_SUPPLIERS = QName.createQName(BECPG_URI, "suppliersAspect");
	
	/** The Constant ASSOC_SUPPLIERS. */
	static final QName ASSOC_SUPPLIERS = QName.createQName(BECPG_URI, "suppliers");
	
	/** The Constant TYPE_SUPPLIER. */
	static final QName TYPE_SUPPLIER = QName.createQName(BECPG_URI, "supplier");	
	
	// client
	/** The Constant ASPECT_CLIENTS. */
	static final QName ASPECT_CLIENTS = QName.createQName(BECPG_URI, "clientsAspect");
	
	/** The Constant ASSOC_CLIENTS. */
	static final QName ASSOC_CLIENTS = QName.createQName(BECPG_URI, "clients");
	
	/** The Constant TYPE_CLIENT. */
	static final QName TYPE_CLIENT = QName.createQName(BECPG_URI, "client");	
	
	// product aspect
	/** The Constant ASPECT_PRODUCT. */
	static final QName ASPECT_PRODUCT = QName.createQName(BECPG_URI,	"productAspect");
	
	/** The Constant ASPECT_TRANSFORMATION. */
	static final QName ASPECT_TRANSFORMATION = QName.createQName(BECPG_URI,	"transformationAspect");
	static final QName PROP_PRODUCT_QTY = QName.createQName(BECPG_URI,
			"productQty");		
	static final QName PROP_PRODUCT_DENSITY = QName.createQName(BECPG_URI,
			"productDensity");	
	static final QName ASSOC_PRODUCT_SPECIFICATION = QName.createQName(BECPG_URI,
			"productSpecification");
	static final QName PROP_PRODUCT_COMMENTS = QName.createQName(BECPG_URI,
			"productComments");
	
	// ean aspect
	/** The Constant ASPECT_EAN. */
	static final QName ASPECT_EAN = QName.createQName(BECPG_URI,	"eanAspect");
	
	/** The Constant PROP_EAN_CODE. */
	static final QName PROP_EAN_CODE = QName.createQName(BECPG_URI,	"eanCode");
	
	// depthLevel aspect
	/** The Constant ASPECT_DEPTH_LEVEL. */
	static final QName ASPECT_DEPTH_LEVEL = QName.createQName(BECPG_URI,	"depthLevelAspect");
	
	/** The Constant PROP_DEPTH_LEVEL. */
	static final QName PROP_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevel");
	
	static final QName PROP_PARENT_LEVEL = QName.createQName(BECPG_URI,
			"parentLevel");
	
	// depthLevel aspect
	static final QName ASPECT_SORTABLE_LIST = QName.createQName(BECPG_URI,	"sortableListAspect");
	static final QName PROP_SORT = QName.createQName(BECPG_URI, "sort");
	

	// detaillableListItem aspect
	static final QName ASPECT_DETAILLABLE_LIST_ITEM = QName.createQName(BECPG_URI,	"detaillableListItemAspect");

	
	// manual aspect
	static final QName ASPECT_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI,	"isManualListItemAspect");
	static final QName PROP_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItem");
	
	// versionable aspect
	/** The Constant ASPECT_COMPOSITE_VERSIONABLE. */
	@Deprecated
	static final QName ASPECT_COMPOSITE_VERSIONABLE = QName.createQName(BECPG_URI,	"compositeVersionable");
	
	/** The Constant PROP_VERSION_LABEL. */
	@Deprecated
	static final QName PROP_VERSION_LABEL = QName.createQName(BECPG_URI, "versionLabel");
	
	/** The Constant PROP_INITIAL_VERSION. */
	@Deprecated
	static final QName PROP_INITIAL_VERSION = QName.createQName(BECPG_URI, "initialVersion");
	
	// version aspect
	/** The Constant ASPECT_COMPOSITE_VERSION. */
	static final QName ASPECT_COMPOSITE_VERSION = QName.createQName(BECPG_URI,	"compositeVersion");
	
	/** The Constant PROP_VERSION_DESCRIPTION. */
	@Deprecated
	static final QName PROP_VERSION_DESCRIPTION = QName.createQName(BECPG_URI, "versionDescription");
	
	/** The Constant PROP_FROZEN_CREATOR. */
	@Deprecated
	static final QName PROP_FROZEN_CREATOR = QName.createQName(BECPG_URI, "frozenCreator");
	
	/** The Constant PROP_FROZEN_CREATED. */
	@Deprecated
	static final QName PROP_FROZEN_CREATED = QName.createQName(BECPG_URI, "frozenCreated");
	
	/** The Constant PROP_FROZEN_MODIFIER. */
	@Deprecated
	static final QName PROP_FROZEN_MODIFIER = QName.createQName(BECPG_URI, "frozenModifier");
	
	/** The Constant PROP_FROZEN_MODIFIED. */
	@Deprecated
	static final QName PROP_FROZEN_MODIFIED = QName.createQName(BECPG_URI, "frozenModified");
	
	/** The Constant PROP_FROZEN_ACCESSED. */
	@Deprecated
	static final QName PROP_FROZEN_ACCESSED = QName.createQName(BECPG_URI, "frozenAccessed");
	
	/** The Constant PROP_FROZEN_NODE_REF. */
	@Deprecated
	static final QName PROP_FROZEN_NODE_REF = QName.createQName(BECPG_URI, "frozenNodeRef");
	
	/** The Constant PROP_FROZEN_NODE_DBID. */
	@Deprecated
	static final QName PROP_FROZEN_NODE_DBID = QName.createQName(BECPG_URI, "frozenNodeDbId");
	
	// version aspect
	/** The Constant ASPECT_PERMISSIONS_TPL. */
	static final QName ASPECT_PERMISSIONS_TPL = QName.createQName(BECPG_URI,"permissionsTpl");
	
	/** The Constant ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS. */
	static final QName ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS = QName.createQName(BECPG_URI, "consumerGroups");
	
	/** The Constant ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS. */
	static final QName ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS = QName.createQName(BECPG_URI, "editorGroups");
	
	/** The Constant ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS. */
	static final QName ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS = QName.createQName(BECPG_URI, "contributorGroups");
	
	/** The Constant ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS. */
	static final QName ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS = QName.createQName(BECPG_URI, "collaboratorGroups");
	
	// code aspect
	/** The Constant ASPECT_CODE. */
	static final QName ASPECT_CODE = QName.createQName(BECPG_URI,	"codeAspect");
	
	/** The Constant PROP_CODE. */
	static final QName PROP_CODE = QName.createQName(BECPG_URI, "code");
	
	// code aspect
	static final QName ASPECT_ERP_CODE = QName.createQName(BECPG_URI,	"erpCodeAspect");
	static final QName PROP_ERP_CODE = QName.createQName(BECPG_URI, "erpCode");
	
	
	/**
	 * Effectivity
	 */
	
	
	static final QName ASPECT_EFFECTIVITY = QName.createQName(BECPG_URI, "effectivityAspect");		
	
	static final QName PROP_START_EFFECTIVITY = QName.createQName(BECPG_URI, "startEffectivity");		
	
	static final QName PROP_END_EFFECTIVITY = QName.createQName(BECPG_URI, "endEffectivity");

	
	/**
	 * Profitability
	 */
	static final QName ASPECT_PROFITABILITY = QName.createQName(BECPG_URI, "profitabilityAspect");
	
	static final QName PROP_UNIT_PRICE = QName.createQName(BECPG_URI, "unitPrice");
	
	static final QName PROP_BREAK_EVEN = QName.createQName(BECPG_URI, "breakEven");
	
	static final QName PROP_PROJECTED_QTY = QName.createQName(BECPG_URI, "projectedQty");	

	static final QName PROP_PROFITABILITY = QName.createQName(BECPG_URI, "profitability");
	
	static final QName PROP_UNIT_TOTAL_COST = QName.createQName(BECPG_URI, "unitTotalCost");

	static final QName PROP_PRICE_CURRENCY =  QName.createQName(BECPG_URI, "priceCurrency");	
	
	/**
	 * manufacturingAspect
	 */
	static final QName ASPECT_MANUFACTURING = QName.createQName(BECPG_URI, "manufacturingAspect");	
	static final QName ASSOC_SUBSIDIARY = QName.createQName(BECPG_URI, "subsidiary");
	static final QName ASSOC_PLANTS = QName.createQName(BECPG_URI, "plants");
	static final QName ASSOC_TRADEMARK = QName.createQName(BECPG_URI, "trademark");

	/**
	 * subsidiary
	 */
	static final QName TYPE_SUBSIDIARY = QName.createQName(BECPG_URI, "subsidiary");
	
	/**
	 * plant
	 */
	static final QName TYPE_PLANT = QName.createQName(BECPG_URI, "plant");
	static final QName ASSOC_PLANT_CERTIFICATIONS = QName.createQName(BECPG_URI, "plantCertifications");
	static final QName ASSOC_PLANT_APPROVAL_NUMBERS = QName.createQName(BECPG_URI, "plantApprovalNumbers");
	
	/**
	 * trademark
	 */
	static final QName TYPE_TRADEMARK = QName.createQName(BECPG_URI, "trademark");
	
	/**
	 * certification
	 */
	static final QName TYPE_CERTIFICATION = QName.createQName(BECPG_URI, "certification");
	
	/**
	 * approvalNumber
	 */
	static final QName TYPE_APPROVAL_NUMBER = QName.createQName(BECPG_URI, "approvalNumber");
	
	/**
	 * legalName aspect
	 */
	static final QName ASPECT_LEGAL_NAME = QName.createQName(BECPG_URI, "legalNameAspect");
	static final QName PROP_LEGAL_NAME = QName.createQName(BECPG_URI, "legalName");

	static final QName ASPECT_DELETED = QName.createQName(BECPG_URI, "isDeletedAspect");
	static final QName PROP_IS_DELETED = QName.createQName(
			BECPG_URI, "isDeleted");



}
