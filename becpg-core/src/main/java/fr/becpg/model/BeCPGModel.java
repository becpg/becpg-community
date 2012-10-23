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
	String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";

	// Food Model Prefix
	/** The Constant BECPG_PREFIX. */
	String BECPG_PREFIX = "bcpg";

	//
	// Product Model Definitions
	//
	/** The Constant MODEL. */
	QName MODEL = QName.createQName(BECPG_URI, "bcpgmodel");

	// productSpecification
	QName TYPE_PRODUCT_SPECIFICATION = QName.createQName(BECPG_URI, "productSpecification");

	// productMicrobioCriteria
	/** The Constant TYPE_PRODUCT_MICROBIO_CRITERIA. */
	QName TYPE_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BECPG_URI, "productMicrobioCriteria");

	/** The Constant ASSOC_PRODUCT_MICROBIO_CRITERIA. */
	QName ASSOC_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BECPG_URI, "productMicrobioCriteria");

	QName ASPECT_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BECPG_URI, "productMicrobioCriteriaAspect");

	QName ASPECT_ENTITYLISTS = QName.createQName(BECPG_URI, "entityListsAspect");

	// product
	/** The Constant TYPE_PRODUCT. */
	QName TYPE_PRODUCT = QName.createQName(BECPG_URI, "product");

	/** The Constant PROP_PRODUCT_LEGALNAME. */
	QName PROP_PRODUCT_LEGALNAME = QName.createQName(BECPG_URI, "legalName");

	/** The Constant PROP_PRODUCT_HIERARCHY1. */
	QName PROP_PRODUCT_HIERARCHY1 = QName.createQName(BECPG_URI, "productHierarchy1");

	/** The Constant PROP_PRODUCT_HIERARCHY2. */
	QName PROP_PRODUCT_HIERARCHY2 = QName.createQName(BECPG_URI, "productHierarchy2");

	/** The Constant PROP_PRODUCT_STATE. */
	QName PROP_PRODUCT_STATE = QName.createQName(BECPG_URI, "productState");

	/** The Constant PROP_PRODUCT_UNIT. */
	QName PROP_PRODUCT_UNIT = QName.createQName(BECPG_URI, "productUnit");

	// finishedProduct
	/** The Constant TYPE_FINISHEDPRODUCT. */
	QName TYPE_FINISHEDPRODUCT = QName.createQName(BECPG_URI, "finishedProduct");
	// semiFinishedProduct
	/** The Constant TYPE_SEMIFINISHEDPRODUCT. */
	QName TYPE_SEMIFINISHEDPRODUCT = QName.createQName(BECPG_URI, "semiFinishedProduct");
	// localSemiFinishedProduct
	/** The Constant TYPE_LOCALSEMIFINISHEDPRODUCT. */
	QName TYPE_LOCALSEMIFINISHEDPRODUCT = QName.createQName(BECPG_URI, "localSemiFinishedProduct");
	// rawMaterial
	/** The Constant TYPE_RAWMATERIAL. */
	QName TYPE_RAWMATERIAL = QName.createQName(BECPG_URI, "rawMaterial");
	// packagingKit
	/** The Constant TYPE_PACKAGINGKIT. */
	QName TYPE_PACKAGINGKIT = QName.createQName(BECPG_URI, "packagingKit");
	// packagingMaterial
	/** The Constant TYPE_PACKAGINGMATERIAL. */
	QName TYPE_PACKAGINGMATERIAL = QName.createQName(BECPG_URI, "packagingMaterial");
	QName TYPE_RESOURCEPRODUCT = QName.createQName(BECPG_URI, "resourceProduct");

	/** The Constant ASSOC_ENTITYLISTS. */
	QName ASSOC_ENTITYLISTS = QName.createQName(BECPG_URI, "entityLists");

	QName TYPE_ENTITYLIST_ITEM = QName.createQName(BECPG_URI, "entityListItem");

	QName TYPE_PRODUCTLIST_ITEM = QName.createQName(BECPG_URI, "productListItem");

	/** The Constant TYPE_CHARACT. */
	QName TYPE_CHARACT = QName.createQName(BECPG_URI, "charact");

	// allergenList
	/** The Constant TYPE_ALLERGENLIST. */
	QName TYPE_ALLERGENLIST = QName.createQName(BECPG_URI, "allergenList");

	/** The Constant PROP_ALLERGENLIST_VOLUNTARY. */
	QName PROP_ALLERGENLIST_VOLUNTARY = QName.createQName(BECPG_URI, "allergenListVoluntary");

	/** The Constant PROP_ALLERGENLIST_ALLERGEN. */
	QName ASSOC_ALLERGENLIST_ALLERGEN = QName.createQName(BECPG_URI, "allergenListAllergen");

	/** The Constant PROP_ALLERGENLIST_INVOLUNTARY. */
	QName PROP_ALLERGENLIST_INVOLUNTARY = QName.createQName(BECPG_URI, "allergenListInVoluntary");

	/** The Constant PROP_ALLERGENLIST_VOLUNTARY_SOURCES. */
	QName ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES = QName.createQName(BECPG_URI, "allergenListVolSources");

	/** The Constant PROP_ALLERGENLIST_INVOLUNTARY_SOURCES. */
	QName ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES = QName.createQName(BECPG_URI, "allergenListInVolSources");

	// compoList
	/** The Constant TYPE_COMPOLIST. */
	QName TYPE_COMPOLIST = QName.createQName(BECPG_URI, "compoList");

	/** The Constant ASSOC_COMPOLIST_PRODUCT. */
	QName ASSOC_COMPOLIST_PRODUCT = QName.createQName(BECPG_URI, "compoListProduct");

	/** The Constant PROP_COMPOLIST_QTY. */
	QName PROP_COMPOLIST_QTY = QName.createQName(BECPG_URI, "compoListQty");

	QName PROP_COMPOLIST_QTY_SUB_FORMULA = QName.createQName(BECPG_URI, "compoListQtySubFormula");

	QName PROP_COMPOLIST_QTY_AFTER_PROCESS = QName.createQName(BECPG_URI, "compoListQtyAfterProcess");

	/** The Constant PROP_COMPOLIST_UNIT. */
	QName PROP_COMPOLIST_UNIT = QName.createQName(BECPG_URI, "compoListUnit");

	QName PROP_COMPOLIST_LOSS_PERC = QName.createQName(BECPG_URI, "compoListLossPerc");

	QName PROP_COMPOLIST_YIELD_PERC = QName.createQName(BECPG_URI, "compoListYieldPerc");

	/** The Constant PROP_COMPOLIST_DECL_TYPE. */
	QName PROP_COMPOLIST_DECL_TYPE = QName.createQName(BECPG_URI, "compoListDeclType");

	// packagingList
	QName TYPE_PACKAGINGLIST = QName.createQName(BECPG_URI, "packagingList");

	QName ASSOC_PACKAGINGLIST_PRODUCT = QName.createQName(BECPG_URI, "packagingListProduct");

	QName PROP_PACKAGINGLIST_QTY = QName.createQName(BECPG_URI, "packagingListQty");

	QName PROP_PACKAGINGLIST_UNIT = QName.createQName(BECPG_URI, "packagingListUnit");

	QName PROP_PACKAGINGLIST_PKG_LEVEL = QName.createQName(BECPG_URI, "packagingListPkgLevel");

	QName PROP_PACKAGINGLIST_ISMASTER = QName.createQName(BECPG_URI, "packagingListIsMaster");

	// costList
	/** The Constant TYPE_COSTLIST. */
	QName TYPE_COSTLIST = QName.createQName(BECPG_URI, "costList");

	/** The Constant PROP_COSTLIST_VALUE. */
	QName PROP_COSTLIST_VALUE = QName.createQName(BECPG_URI, "costListValue");

	/** The Constant PROP_COSTLIST_UNIT. */
	QName PROP_COSTLIST_UNIT = QName.createQName(BECPG_URI, "costListUnit");

	QName PROP_COSTLIST_MAXI = QName.createQName(BECPG_URI, "costListMaxi");

	/** The Constant ASSOC_COSTLIST_COST. */
	QName ASSOC_COSTLIST_COST = QName.createQName(BECPG_URI, "costListCost");

	/** The Constant PROP_COSTDETAILSLIST_VALUE. */
	QName PROP_COSTDETAILSLIST_VALUE = QName.createQName(BECPG_URI, "costDetailsListValue");

	/** The Constant PROP_COSTDETAILSLIST_UNIT. */
	QName PROP_COSTDETAILSLIST_UNIT = QName.createQName(BECPG_URI, "costDetailsListUnit");

	QName PROP_COSTDETAILSLIST_PERC = QName.createQName(BECPG_URI, "costDetailsListPerc");

	/** The Constant ASSOC_COSTDETAILSLIST_COST. */
	QName ASSOC_COSTDETAILSLIST_COST = QName.createQName(BECPG_URI, "costDetailsListCost");

	/** The Constant ASSOC_COSTDETAILSLIST_SOURCE. */
	QName ASSOC_COSTDETAILSLIST_SOURCE = QName.createQName(BECPG_URI, "costDetailsListSource");

	// priceList
	QName TYPE_PRICELIST = QName.createQName(BECPG_URI, "priceList");

	QName ASSOC_PRICELIST_COST = QName.createQName(BECPG_URI, "priceListCost");

	QName PROP_PRICELIST_PREF_RANK = QName.createQName(BECPG_URI, "priceListPrefRank");

	QName PROP_PRICELIST_VALUE = QName.createQName(BECPG_URI, "priceListValue");

	QName PROP_PRICELIST_UNIT = QName.createQName(BECPG_URI, "priceListUnit");

	QName PROP_PRICELIST_PURCHASE_VALUE = QName.createQName(BECPG_URI, "priceListPurchaseQty");

	QName PROP_PRICELIST_PURCHASE_UNIT = QName.createQName(BECPG_URI, "priceListPurchaseUnit");

	// ingList
	/** The Constant TYPE_INGLIST. */
	QName TYPE_INGLIST = QName.createQName(BECPG_URI, "ingList");

	/** The Constant PROP_INGLIST_QTY_PERC. */
	QName PROP_INGLIST_QTY_PERC = QName.createQName(BECPG_URI, "ingListQtyPerc");

	/** The Constant PROP_INGLIST_IS_GMO. */
	QName PROP_INGLIST_IS_GMO = QName.createQName(BECPG_URI, "ingListIsGMO");

	/** The Constant PROP_INGLIST_IS_IONIZED. */
	QName PROP_INGLIST_IS_IONIZED = QName.createQName(BECPG_URI, "ingListIsIonized");

	/** The Constant ASSOC_INGLIST_GEO_ORIGIN. */
	QName ASSOC_INGLIST_GEO_ORIGIN = QName.createQName(BECPG_URI, "ingListGeoOrigin");

	/** The Constant ASSOC_INGLIST_BIO_ORIGIN. */
	QName ASSOC_INGLIST_BIO_ORIGIN = QName.createQName(BECPG_URI, "ingListBioOrigin");

	/** The Constant ASSOC_INGLIST_ING. */
	QName ASSOC_INGLIST_ING = QName.createQName(BECPG_URI, "ingListIng");

	// nutList
	/** The Constant TYPE_NUTLIST. */
	QName TYPE_NUTLIST = QName.createQName(BECPG_URI, "nutList");

	/** The Constant ASSOC_NUTLIST_NUT. */
	QName ASSOC_NUTLIST_NUT = QName.createQName(BECPG_URI, "nutListNut");

	/** The Constant PROP_NUTLIST_VALUE. */
	QName PROP_NUTLIST_VALUE = QName.createQName(BECPG_URI, "nutListValue");

	/** The Constant PROP_NUTLIST_UNIT. */
	QName PROP_NUTLIST_UNIT = QName.createQName(BECPG_URI, "nutListUnit");
	QName PROP_NUTLIST_MINI = QName.createQName(BECPG_URI, "nutListMini");
	QName PROP_NUTLIST_MAXI = QName.createQName(BECPG_URI, "nutListMaxi");
	/** The Constant PROP_NUTLIST_GROUP. */
	QName PROP_NUTLIST_GROUP = QName.createQName(BECPG_URI, "nutListGroup");

	// organoList
	/** The Constant TYPE_ORGANOLIST. */
	QName TYPE_ORGANOLIST = QName.createQName(BECPG_URI, "organoList");

	/** The Constant PROP_ORGANOLIST_VALUE. */
	QName PROP_ORGANOLIST_VALUE = QName.createQName(BECPG_URI, "organoListValue");

	/** The Constant PROP_ORGANOLIST_ORGANO. */
	QName ASSOC_ORGANOLIST_ORGANO = QName.createQName(BECPG_URI, "organoListOrgano");

	// ingLabelingList
	/** The Constant TYPE_INGLABELINGLIST. */
	QName TYPE_INGLABELINGLIST = QName.createQName(BECPG_URI, "ingLabelingList");

	/** The Constant ASSOC_ILL_GRP. */
	QName ASSOC_ILL_GRP = QName.createQName(BECPG_URI, "illGrp");

	/** The Constant PROP_ILL_VALUE. */
	QName PROP_ILL_VALUE = QName.createQName(BECPG_URI, "illValue");

	// microbioList
	/** The Constant TYPE_MICROBIOLIST. */
	QName TYPE_MICROBIOLIST = QName.createQName(BECPG_URI, "microbioList");

	/** The Constant PROP_MICROBIOLIST_VALUE. */
	QName PROP_MICROBIOLIST_VALUE = QName.createQName(BECPG_URI, "mblValue");

	/** The Constant PROP_MICROBIOLIST_UNIT. */
	QName PROP_MICROBIOLIST_UNIT = QName.createQName(BECPG_URI, "mblUnit");

	/** The Constant PROP_MICROBIOLIST_MAXI. */
	QName PROP_MICROBIOLIST_MAXI = QName.createQName(BECPG_URI, "mblMaxi");

	QName PROP_MICROBIOLIST_TEXT_CRITERIA = QName.createQName(BECPG_URI, "mblTextCriteria");

	/** The Constant ASSOC_MICROBIOLIST_MICROBIO. */
	QName ASSOC_MICROBIOLIST_MICROBIO = QName.createQName(BECPG_URI, "mblMicrobio");

	// physicoChemList
	/** The Constant TYPE_PHYSICOCHEMLIST. */
	QName TYPE_PHYSICOCHEMLIST = QName.createQName(BECPG_URI, "physicoChemList");

	/** The Constant PROP_PHYSICOCHEMLIST_VALUE. */
	QName PROP_PHYSICOCHEMLIST_VALUE = QName.createQName(BECPG_URI, "pclValue");

	/** The Constant PROP_PHYSICOCHEMLIST_UNIT. */
	QName PROP_PHYSICOCHEMLIST_UNIT = QName.createQName(BECPG_URI, "pclUnit");

	/** The Constant PROP_PHYSICOCHEMLIST_MINI. */
	QName PROP_PHYSICOCHEMLIST_MINI = QName.createQName(BECPG_URI, "pclMini");

	/** The Constant PROP_PHYSICOCHEMLIST_MAXI. */
	QName PROP_PHYSICOCHEMLIST_MAXI = QName.createQName(BECPG_URI, "pclMaxi");

	/** The Constant ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM. */
	QName ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM = QName.createQName(BECPG_URI, "pclPhysicoChem");

	// forbiddenIngList
	QName TYPE_FORBIDDENINGLIST = QName.createQName(BECPG_URI, "forbiddenIngList");
	QName PROP_FIL_REQ_TYPE = QName.createQName(BECPG_URI, "filReqType");
	QName PROP_FIL_REQ_MESSAGE = QName.createQName(BECPG_URI, "filReqMessage");
	QName PROP_FIL_QTY_PERC_MAXI = QName.createQName(BECPG_URI, "filQtyPercMaxi");
	QName PROP_FIL_IS_GMO = QName.createQName(BECPG_URI, "filIsGMO");
	QName PROP_FIL_IS_IONIZED = QName.createQName(BECPG_URI, "filIsIonized");
	QName ASSOC_FIL_INGS = QName.createQName(BECPG_URI, "filIngs");
	QName ASSOC_FIL_GEO_ORIGINS = QName.createQName(BECPG_URI, "filGeoOrigins");
	QName ASSOC_FIL_BIO_ORIGINS = QName.createQName(BECPG_URI, "filBioOrigins");

	// reqCtrlList
	QName TYPE_REQCTRLLIST = QName.createQName(BECPG_URI, "reqCtrlList");
	QName PROP_RCL_REQ_TYPE = QName.createQName(BECPG_URI, "rclReqType");
	QName PROP_RCL_REQ_MESSAGE = QName.createQName(BECPG_URI, "rclReqMessage");
	QName ASSOC_RCL_SOURCES = QName.createQName(BECPG_URI, "rclSources");

	QName TYPE_DYNAMICCHARACTLIST = QName.createQName(BECPG_URI, "dynamicCharactList");

	QName PROP_DYNAMICCHARACT_TITLE = QName.createQName(BECPG_URI, "dynamicCharactTitle");

	QName PROP_DYNAMICCHARACT_FORMULA = QName.createQName(BECPG_URI, "dynamicCharactFormula");

	QName PROP_DYNAMICCHARACT_VALUE = QName.createQName(BECPG_URI, "dynamicCharactValue");

	QName PROP_DYNAMICCHARACT_GROUP_COLOR = QName.createQName(BECPG_URI, "dynamicCharactGroupColor");

	// contactList
	QName TYPE_CONTACTLIST = QName.createQName(BECPG_URI, "contactList");

	// allergen
	/** The Constant TYPE_ALLERGEN. */
	QName TYPE_ALLERGEN = QName.createQName(BECPG_URI, "allergen");

	/** The Constant PROP_ALLERGEN_TYPE. */
	QName PROP_ALLERGEN_TYPE = QName.createQName(BECPG_URI, "allergenType");

	// cost
	/** The Constant TYPE_COST. */
	QName TYPE_COST = QName.createQName(BECPG_URI, "cost");

	/** The Constant PROP_COSTCURRENCY. */
	QName PROP_COSTCURRENCY = QName.createQName(BECPG_URI, "costCurrency");
	QName PROP_COSTFIXED = QName.createQName(BECPG_URI, "costFixed");

	// ing
	/** The Constant TYPE_ING. */
	QName TYPE_ING = QName.createQName(BECPG_URI, "ing");

	/** The Constant PROP_ING_CEECODE. */
	QName PROP_ING_CEECODE = QName.createQName(BECPG_URI, "ingCEECode");

	/** The Constant PROP_ING_TYPE. */
	QName PROP_ING_TYPE = QName.createQName(BECPG_URI, "ingType");

	// microbio
	/** The Constant TYPE_MICROBIO. */
	QName TYPE_MICROBIO = QName.createQName(BECPG_URI, "microbio");

	// geoOrigin
	/** The Constant TYPE_GEO_ORIGIN. */
	QName TYPE_GEO_ORIGIN = QName.createQName(BECPG_URI, "geoOrigin");

	/** The Constant PROP_GEO_ORIGIN_ISOCODE. */
	QName PROP_GEO_ORIGIN_ISOCODE = QName.createQName(BECPG_URI, "bioOriginISOCode");

	// bioOrigin
	/** The Constant TYPE_BIO_ORIGIN. */
	QName TYPE_BIO_ORIGIN = QName.createQName(BECPG_URI, "bioOrigin");

	/** The Constant PROP_BIO_ORIGIN_TYPE. */
	QName PROP_BIO_ORIGIN_TYPE = QName.createQName(BECPG_URI, "bioOriginType");

	// nut
	/** The Constant TYPE_NUT. */
	QName TYPE_NUT = QName.createQName(BECPG_URI, "nut");

	/** The Constant PROP_NUTGROUP. */
	QName PROP_NUTGROUP = QName.createQName(BECPG_URI, "nutGroup");

	/** The Constant PROP_NUTTYPE. */
	QName PROP_NUTTYPE = QName.createQName(BECPG_URI, "nutType");

	/** The Constant PROP_NUTUNIT. */
	QName PROP_NUTUNIT = QName.createQName(BECPG_URI, "nutUnit");

	// organo
	/** The Constant TYPE_ORGANO. */
	QName TYPE_ORGANO = QName.createQName(BECPG_URI, "organo");

	// physicoChem
	/** The Constant TYPE_PHYSICO_CHEM. */
	QName TYPE_PHYSICO_CHEM = QName.createQName(BECPG_URI, "physicoChem");

	QName PROP_PHYSICO_CHEM_FORMULATED = QName.createQName(BECPG_URI, "physicoChemFormulated");

	// linkedValue
	/** The Constant TYPE_LINKED_VALUE. */
	QName TYPE_LINKED_VALUE = QName.createQName(BECPG_URI, "linkedValue");

	// lkvValue
	/** The Constant PROP_LNK_VALUE. */
	QName PROP_LKV_VALUE = QName.createQName(BECPG_URI, "lkvValue");

	// listValue
	/** The Constant TYPE_LIST_VALUE. */
	QName TYPE_LIST_VALUE = QName.createQName(BECPG_URI, "listValue");

	/** entityFolder */
	QName TYPE_ENTITY_FOLDER = QName.createQName(BECPG_URI, "entityFolder");

	QName PROP_ENTITY_FOLDER_CLASS_NAME = QName.createQName(BECPG_URI, "entityFolderClassName");

	/** entity */
	QName TYPE_ENTITY = QName.createQName(BECPG_URI, "entity");

	QName TYPE_SYSTEM_ENTITY = QName.createQName(BECPG_URI, "systemEntity");

	// autoNum
	/** The Constant TYPE_AUTO_NUM. */
	QName TYPE_AUTO_NUM = QName.createQName(BECPG_URI, "autoNum");

	/** The Constant PROP_AUTO_NUM_CLASS_NAME. */
	QName PROP_AUTO_NUM_CLASS_NAME = QName.createQName(BECPG_URI, "autoNumClassName");

	/** The Constant PROP_AUTO_NUM_PROPERTY_NAME. */
	QName PROP_AUTO_NUM_PROPERTY_NAME = QName.createQName(BECPG_URI, "autoNumPropertyName");

	/** The Constant PROP_AUTO_NUM_VALUE. */
	QName PROP_AUTO_NUM_VALUE = QName.createQName(BECPG_URI, "autoNumValue");

	/** The Constant PROP_AUTO_NUM_VALUE. */
	QName PROP_AUTO_NUM_PREFIX = QName.createQName(BECPG_URI, "autoNumPrefix");

	// entityTpl aspect
	QName ASPECT_ENTITY_TPL = QName.createQName(BECPG_URI, "entityTplAspect");
	QName PROP_ENTITY_TPL_CLASS_NAME = QName.createQName(BECPG_URI, "entityTplClassName");
	QName PROP_ENTITY_TPL_ENABLED = QName.createQName(BECPG_URI, "entityTplEnabled");

	// supplier
	/** The Constant ASPECT_SUPPLIERS. */
	QName ASPECT_SUPPLIERS = QName.createQName(BECPG_URI, "suppliersAspect");

	/** The Constant ASSOC_SUPPLIERS. */
	QName ASSOC_SUPPLIERS = QName.createQName(BECPG_URI, "suppliers");

	/** The Constant TYPE_SUPPLIER. */
	QName TYPE_SUPPLIER = QName.createQName(BECPG_URI, "supplier");

	// client
	/** The Constant ASPECT_CLIENTS. */
	QName ASPECT_CLIENTS = QName.createQName(BECPG_URI, "clientsAspect");

	/** The Constant ASSOC_CLIENTS. */
	QName ASSOC_CLIENTS = QName.createQName(BECPG_URI, "clients");

	/** The Constant TYPE_CLIENT. */
	QName TYPE_CLIENT = QName.createQName(BECPG_URI, "client");

	// product aspect
	/** The Constant ASPECT_PRODUCT. */
	QName ASPECT_PRODUCT = QName.createQName(BECPG_URI, "productAspect");

	/** The Constant ASPECT_TRANSFORMATION. */
	QName ASPECT_TRANSFORMATION = QName.createQName(BECPG_URI, "transformationAspect");
	QName PROP_PRODUCT_QTY = QName.createQName(BECPG_URI, "productQty");
	QName PROP_PRODUCT_DENSITY = QName.createQName(BECPG_URI, "productDensity");
	QName ASSOC_PRODUCT_SPECIFICATION = QName.createQName(BECPG_URI, "productSpecification");
	QName PROP_PRODUCT_COMMENTS = QName.createQName(BECPG_URI, "productComments");

	// ean aspect
	/** The Constant ASPECT_EAN. */
	QName ASPECT_EAN = QName.createQName(BECPG_URI, "eanAspect");

	/** The Constant PROP_EAN_CODE. */
	QName PROP_EAN_CODE = QName.createQName(BECPG_URI, "eanCode");

	// depthLevel aspect
	/** The Constant ASPECT_DEPTH_LEVEL. */
	QName ASPECT_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevelAspect");

	/** The Constant PROP_DEPTH_LEVEL. */
	QName PROP_DEPTH_LEVEL = QName.createQName(BECPG_URI, "depthLevel");

	QName PROP_PARENT_LEVEL = QName.createQName(BECPG_URI, "parentLevel");

	// depthLevel aspect
	QName ASPECT_SORTABLE_LIST = QName.createQName(BECPG_URI, "sortableListAspect");
	QName PROP_SORT = QName.createQName(BECPG_URI, "sort");

	// detaillableListItem aspect
	QName ASPECT_DETAILLABLE_LIST_ITEM = QName.createQName(BECPG_URI, "detaillableListItemAspect");

	// manual aspect
	QName ASPECT_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItemAspect");
	QName PROP_IS_MANUAL_LISTITEM = QName.createQName(BECPG_URI, "isManualListItem");

	// versionable aspect
	/** The Constant ASPECT_COMPOSITE_VERSIONABLE. */
	@Deprecated
	QName ASPECT_COMPOSITE_VERSIONABLE = QName.createQName(BECPG_URI, "compositeVersionable");

	/** The Constant PROP_VERSION_LABEL. */
	@Deprecated
	QName PROP_VERSION_LABEL = QName.createQName(BECPG_URI, "versionLabel");

	/** The Constant PROP_INITIAL_VERSION. */
	@Deprecated
	QName PROP_INITIAL_VERSION = QName.createQName(BECPG_URI, "initialVersion");

	// version aspect
	/** The Constant ASPECT_COMPOSITE_VERSION. */
	QName ASPECT_COMPOSITE_VERSION = QName.createQName(BECPG_URI, "compositeVersion");

	/** The Constant PROP_VERSION_DESCRIPTION. */
	@Deprecated
	QName PROP_VERSION_DESCRIPTION = QName.createQName(BECPG_URI, "versionDescription");

	/** The Constant PROP_FROZEN_CREATOR. */
	@Deprecated
	QName PROP_FROZEN_CREATOR = QName.createQName(BECPG_URI, "frozenCreator");

	/** The Constant PROP_FROZEN_CREATED. */
	@Deprecated
	QName PROP_FROZEN_CREATED = QName.createQName(BECPG_URI, "frozenCreated");

	/** The Constant PROP_FROZEN_MODIFIER. */
	@Deprecated
	QName PROP_FROZEN_MODIFIER = QName.createQName(BECPG_URI, "frozenModifier");

	/** The Constant PROP_FROZEN_MODIFIED. */
	@Deprecated
	QName PROP_FROZEN_MODIFIED = QName.createQName(BECPG_URI, "frozenModified");

	/** The Constant PROP_FROZEN_ACCESSED. */
	@Deprecated
	QName PROP_FROZEN_ACCESSED = QName.createQName(BECPG_URI, "frozenAccessed");

	/** The Constant PROP_FROZEN_NODE_REF. */
	@Deprecated
	QName PROP_FROZEN_NODE_REF = QName.createQName(BECPG_URI, "frozenNodeRef");

	/** The Constant PROP_FROZEN_NODE_DBID. */
	@Deprecated
	QName PROP_FROZEN_NODE_DBID = QName.createQName(BECPG_URI, "frozenNodeDbId");

	// version aspect
	/** The Constant ASPECT_PERMISSIONS_TPL. */
	QName ASPECT_PERMISSIONS_TPL = QName.createQName(BECPG_URI, "permissionsTpl");

	/** The Constant ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS. */
	QName ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS = QName.createQName(BECPG_URI, "consumerGroups");

	/** The Constant ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS. */
	QName ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS = QName.createQName(BECPG_URI, "editorGroups");

	/** The Constant ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS. */
	QName ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS = QName.createQName(BECPG_URI, "contributorGroups");

	/** The Constant ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS. */
	QName ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS = QName.createQName(BECPG_URI, "collaboratorGroups");

	// code aspect
	/** The Constant ASPECT_CODE. */
	QName ASPECT_CODE = QName.createQName(BECPG_URI, "codeAspect");

	/** The Constant PROP_CODE. */
	QName PROP_CODE = QName.createQName(BECPG_URI, "code");

	// code aspect
	QName ASPECT_ERP_CODE = QName.createQName(BECPG_URI, "erpCodeAspect");
	QName PROP_ERP_CODE = QName.createQName(BECPG_URI, "erpCode");

	/**
	 * Effectivity
	 */

	QName ASPECT_EFFECTIVITY = QName.createQName(BECPG_URI, "effectivityAspect");

	QName PROP_START_EFFECTIVITY = QName.createQName(BECPG_URI, "startEffectivity");

	QName PROP_END_EFFECTIVITY = QName.createQName(BECPG_URI, "endEffectivity");

	/**
	 * Profitability
	 */
	QName ASPECT_PROFITABILITY = QName.createQName(BECPG_URI, "profitabilityAspect");

	QName PROP_UNIT_PRICE = QName.createQName(BECPG_URI, "unitPrice");

	QName PROP_BREAK_EVEN = QName.createQName(BECPG_URI, "breakEven");

	QName PROP_PROJECTED_QTY = QName.createQName(BECPG_URI, "projectedQty");

	QName PROP_PROFITABILITY = QName.createQName(BECPG_URI, "profitability");

	QName PROP_UNIT_TOTAL_COST = QName.createQName(BECPG_URI, "unitTotalCost");

	QName PROP_PRICE_CURRENCY = QName.createQName(BECPG_URI, "priceCurrency");

	/**
	 * manufacturingAspect
	 */
	QName ASPECT_MANUFACTURING = QName.createQName(BECPG_URI, "manufacturingAspect");
	QName ASSOC_SUBSIDIARY = QName.createQName(BECPG_URI, "subsidiary");
	QName ASSOC_PLANTS = QName.createQName(BECPG_URI, "plants");
	QName ASSOC_TRADEMARK = QName.createQName(BECPG_URI, "trademark");

	/**
	 * subsidiary
	 */
	QName TYPE_SUBSIDIARY = QName.createQName(BECPG_URI, "subsidiary");

	/**
	 * plant
	 */
	QName TYPE_PLANT = QName.createQName(BECPG_URI, "plant");
	QName ASSOC_PLANT_CERTIFICATIONS = QName.createQName(BECPG_URI, "plantCertifications");
	QName ASSOC_PLANT_APPROVAL_NUMBERS = QName.createQName(BECPG_URI, "plantApprovalNumbers");

	/**
	 * trademark
	 */
	QName TYPE_TRADEMARK = QName.createQName(BECPG_URI, "trademark");

	/**
	 * certification
	 */
	QName TYPE_CERTIFICATION = QName.createQName(BECPG_URI, "certification");

	/**
	 * approvalNumber
	 */
	QName TYPE_APPROVAL_NUMBER = QName.createQName(BECPG_URI, "approvalNumber");

	/**
	 * legalName aspect
	 */
	QName ASPECT_LEGAL_NAME = QName.createQName(BECPG_URI, "legalNameAspect");
	QName PROP_LEGAL_NAME = QName.createQName(BECPG_URI, "legalName");

	QName ASPECT_DELETED = QName.createQName(BECPG_URI, "isDeletedAspect");
	QName PROP_IS_DELETED = QName.createQName(BECPG_URI, "isDeleted");

}
