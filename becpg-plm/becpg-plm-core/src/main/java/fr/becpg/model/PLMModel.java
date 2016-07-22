/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * PLM model definition.
 * 
 * @author querephi
 */
public interface PLMModel {

	// product
	QName TYPE_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "product");
	QName TYPE_FINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");
	QName TYPE_SEMIFINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "semiFinishedProduct");
	QName TYPE_LOCALSEMIFINISHEDPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "localSemiFinishedProduct");
	QName TYPE_RAWMATERIAL = QName.createQName(BeCPGModel.BECPG_URI, "rawMaterial");
	QName TYPE_PACKAGINGKIT = QName.createQName(BeCPGModel.BECPG_URI, "packagingKit");
	QName TYPE_PACKAGINGMATERIAL = QName.createQName(BeCPGModel.BECPG_URI, "packagingMaterial");
	QName TYPE_RESOURCEPRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "resourceProduct");

	QName TYPE_PRODUCT_SPECIFICATION = QName.createQName(BeCPGModel.BECPG_URI, "productSpecification");

	// productMicrobioCriteria
	QName TYPE_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteria");
	QName ASSOC_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteriaRef");
	QName ASPECT_PRODUCT_MICROBIO_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "productMicrobioCriteriaAspect");

	QName TYPE_PRODUCTLIST_ITEM = QName.createQName(BeCPGModel.BECPG_URI, "productListItem");

	// allergenList
	QName TYPE_ALLERGENLIST = QName.createQName(BeCPGModel.BECPG_URI, "allergenList");
	QName PROP_ALLERGENLIST_VOLUNTARY = QName.createQName(BeCPGModel.BECPG_URI, "allergenListVoluntary");
	QName ASSOC_ALLERGENLIST_ALLERGEN = QName.createQName(BeCPGModel.BECPG_URI, "allergenListAllergen");
	QName PROP_ALLERGENLIST_INVOLUNTARY = QName.createQName(BeCPGModel.BECPG_URI, "allergenListInVoluntary");
	QName ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "allergenListVolSources");
	QName ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "allergenListInVolSources");
	QName PROP_ALLERGEN_DECISION_TREE = QName.createQName(BeCPGModel.BECPG_URI, "allergenListDecisionTree");
	QName ASSOC_ALLERGENSUBSETS = QName.createQName(BeCPGModel.BECPG_URI, "allergenSubset");

	// compoList
	QName TYPE_COMPOLIST = QName.createQName(BeCPGModel.BECPG_URI, "compoList");
	QName ASSOC_COMPOLIST_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "compoListProduct");
	QName PROP_COMPOLIST_QTY = QName.createQName(BeCPGModel.BECPG_URI, "compoListQty");
	QName PROP_COMPOLIST_QTY_SUB_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "compoListQtySubFormula");
	QName PROP_COMPOLIST_QTY_AFTER_PROCESS = QName.createQName(BeCPGModel.BECPG_URI, "compoListQtyAfterProcess");
	QName PROP_COMPOLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "compoListUnit");
	QName PROP_COMPOLIST_LOSS_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListLossPerc");
	QName PROP_COMPOLIST_YIELD_PERC = QName.createQName(BeCPGModel.BECPG_URI, "compoListYieldPerc");
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
	QName TYPE_COSTLIST = QName.createQName(BeCPGModel.BECPG_URI, "costList");
	QName PROP_COSTLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "costListValue");
	QName PROP_COSTLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "costListUnit");
	QName PROP_COSTLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "costListMaxi");
	QName ASSOC_COSTLIST_COST = QName.createQName(BeCPGModel.BECPG_URI, "costListCost");

	// priceList
	QName TYPE_PRICELIST = QName.createQName(BeCPGModel.BECPG_URI, "priceList");
	QName ASSOC_PRICELIST_COST = QName.createQName(BeCPGModel.BECPG_URI, "priceListCost");
	QName PROP_PRICELIST_PREF_RANK = QName.createQName(BeCPGModel.BECPG_URI, "priceListPrefRank");
	QName PROP_PRICELIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "priceListValue");
	QName PROP_PRICELIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "priceListUnit");
	QName PROP_PRICELIST_PURCHASE_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "priceListPurchaseQty");
	QName PROP_PRICELIST_PURCHASE_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "priceListPurchaseUnit");

	// ingList
	QName TYPE_INGLIST = QName.createQName(BeCPGModel.BECPG_URI, "ingList");
	QName PROP_INGLIST_QTY_PERC = QName.createQName(BeCPGModel.BECPG_URI, "ingListQtyPerc");
	QName PROP_INGLIST_IS_GMO = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsGMO");
	QName PROP_INGLIST_IS_IONIZED = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsIonized");
	QName PROP_ING_LIST_IS_PROCESSING_AID = QName.createQName(BeCPGModel.BECPG_URI, "ingListIsProcessingAid");
	QName ASSOC_INGLIST_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "ingListGeoOrigin");
	QName ASSOC_INGLIST_BIO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "ingListBioOrigin");
	QName ASSOC_INGLIST_ING = QName.createQName(BeCPGModel.BECPG_URI, "ingListIng");

	// nutList
	QName TYPE_NUTLIST = QName.createQName(BeCPGModel.BECPG_URI, "nutList");
	QName ASSOC_NUTLIST_NUT = QName.createQName(BeCPGModel.BECPG_URI, "nutListNut");
	QName PROP_NUTLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "nutListValue");
	QName PROP_NUTLIST_FORMULATED_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "nutListFormulatedValue");
	QName PROP_NUTLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "nutListUnit");
	QName PROP_NUTLIST_MINI = QName.createQName(BeCPGModel.BECPG_URI, "nutListMini");
	QName PROP_NUTLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "nutListMaxi");
	QName PROP_NUTLIST_GROUP = QName.createQName(BeCPGModel.BECPG_URI, "nutListGroup");

	// organoList
	QName TYPE_ORGANOLIST = QName.createQName(BeCPGModel.BECPG_URI, "organoList");
	QName PROP_ORGANOLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "organoListValue");
	QName ASSOC_ORGANOLIST_ORGANO = QName.createQName(BeCPGModel.BECPG_URI, "organoListOrgano");

	// ingLabelingList
	QName TYPE_INGLABELINGLIST = QName.createQName(BeCPGModel.BECPG_URI, "ingLabelingList");

	// labelingRuleList
	QName TYPE_LABELING_RULE_LIST = QName.createQName(BeCPGModel.BECPG_URI, "labelingRuleList");
	QName PROP_LABELING_RULE_LABEL = QName.createQName(BeCPGModel.BECPG_URI, "lrLabel");

	QName ASSOC_ILL_GRP = QName.createQName(BeCPGModel.BECPG_URI, "illGrp");
	QName PROP_ILL_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "illValue");
	QName PROP_ILL_MANUAL_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "illManualValue");

	// microbioList
	QName TYPE_MICROBIOLIST = QName.createQName(BeCPGModel.BECPG_URI, "microbioList");
	QName PROP_MICROBIOLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "mblValue");
	QName PROP_MICROBIOLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "mblUnit");
	QName PROP_MICROBIOLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "mblMaxi");
	QName PROP_MICROBIOLIST_TEXT_CRITERIA = QName.createQName(BeCPGModel.BECPG_URI, "mblTextCriteria");
	QName ASSOC_MICROBIOLIST_MICROBIO = QName.createQName(BeCPGModel.BECPG_URI, "mblMicrobio");

	// physicoChemList
	QName TYPE_PHYSICOCHEMLIST = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemList");
	QName PROP_PHYSICOCHEMLIST_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "pclValue");
	QName PROP_PHYSICOCHEMLIST_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "pclUnit");
	QName PROP_PHYSICOCHEMLIST_MINI = QName.createQName(BeCPGModel.BECPG_URI, "pclMini");
	QName PROP_PHYSICOCHEMLIST_MAXI = QName.createQName(BeCPGModel.BECPG_URI, "pclMaxi");
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
	QName PROP_DYNAMICCHARACT_COLUMN = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactColumn");
	QName PROP_DYNAMICCHARACT_SYNCHRONIZABLE_STATE = QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactSynchronisableState");

	// contactList
	QName TYPE_CONTACTLIST = QName.createQName(BeCPGModel.BECPG_URI, "contactList");

	// labelClaimList
	QName TYPE_LABELCLAIMLIST = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimList");
	QName PROP_LCL_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "lclType");
	QName PROP_LCL_CLAIM_VALUE = QName.createQName(BeCPGModel.BECPG_URI, "lclClaimValue");
	QName ASSOC_LCL_LABELCLAIM = QName.createQName(BeCPGModel.BECPG_URI, "lclLabelClaim");

	// allergen
	QName TYPE_ALLERGEN = QName.createQName(BeCPGModel.BECPG_URI, "allergen");
	QName PROP_ALLERGEN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "allergenType");
	QName PROP_ALLERGEN_REGULATORY_THRESHOLD = QName.createQName(BeCPGModel.BECPG_URI, "allergenRegulatoryThreshold");

	// cost
	QName TYPE_COST = QName.createQName(BeCPGModel.BECPG_URI, "cost");
	QName PROP_COST_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "costFormula");
	QName PROP_COSTCURRENCY = QName.createQName(BeCPGModel.BECPG_URI, "costCurrency");
	QName PROP_COSTFIXED = QName.createQName(BeCPGModel.BECPG_URI, "costFixed");

	// ing
	QName TYPE_ING = QName.createQName(BeCPGModel.BECPG_URI, "ing");
	QName PROP_ING_CEECODE = QName.createQName(BeCPGModel.BECPG_URI, "ingCEECode");

	QName ASPECT_ING_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeAspect");
	
	QName PROP_ING_TYPE_V2 = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeV2");
	QName TYPE_ING_TYPE_ITEM = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeItem");
	QName PROP_ING_TYPE_DEC_THRESHOLD = QName.createQName(BeCPGModel.BECPG_URI, "ingTypeDecThreshold");
	QName PROP_ING_TYPE_LEGAL_NAME_PLURAL = QName.createQName(BeCPGModel.BECPG_URI, "ingTypePluralLegalName");

	// microbio
	QName TYPE_MICROBIO = QName.createQName(BeCPGModel.BECPG_URI, "microbio");

	// geoOrigin
	QName TYPE_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "geoOrigin");
	QName PROP_GEO_ORIGIN_ISOCODE = QName.createQName(BeCPGModel.BECPG_URI, "bioOriginISOCode");

	// bioOrigin
	QName TYPE_BIO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "bioOrigin");
	QName PROP_BIO_ORIGIN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "bioOriginType");

	// nut
	QName TYPE_NUT = QName.createQName(BeCPGModel.BECPG_URI, "nut");
	QName PROP_NUTGROUP = QName.createQName(BeCPGModel.BECPG_URI, "nutGroup");
	QName PROP_NUTTYPE = QName.createQName(BeCPGModel.BECPG_URI, "nutType");
	QName PROP_NUTUNIT = QName.createQName(BeCPGModel.BECPG_URI, "nutUnit");
	QName PROP_NUTGDA = QName.createQName(BeCPGModel.BECPG_URI, "nutGDA");
	QName PROP_NUTUL = QName.createQName(BeCPGModel.BECPG_URI, "nutUL");
	QName PROP_NUT_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "nutFormula");
	QName PROP_NUT_METHOD = QName.createQName(BeCPGModel.BECPG_URI, "nutListMethod");

	// organo
	QName TYPE_ORGANO = QName.createQName(BeCPGModel.BECPG_URI, "organo");

	// physicoChem
	QName TYPE_PHYSICO_CHEM = QName.createQName(BeCPGModel.BECPG_URI, "physicoChem");
	QName PROP_PHYSICO_CHEM_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemUnit");
	QName PROP_PHYSICO_CHEM_FORMULATED = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormulated");
	QName PROP_PHYSICO_CHEM_FORMULATED_FROM_VOL = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormulatedFromVol");
	QName PROP_PHYSICO_CHEM_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "physicoChemFormula");

	// supplier aspect
	QName ASPECT_SUPPLIERS = QName.createQName(BeCPGModel.BECPG_URI, "suppliersAspect");
	QName ASSOC_SUPPLIERS = QName.createQName(BeCPGModel.BECPG_URI, "suppliers");
	QName PROP_SUPPLIER_STATE = QName.createQName(BeCPGModel.BECPG_URI, "supplierState");

	// supplier
	QName TYPE_SUPPLIER = QName.createQName(BeCPGModel.BECPG_URI, "supplier");
	QName ASSOC_SUPPLIER_ACCOUNT = QName.createQName(BeCPGModel.BECPG_URI, "supplierAccountRef");

	// client aspect
	QName ASPECT_CLIENTS = QName.createQName(BeCPGModel.BECPG_URI, "clientsAspect");
	QName ASSOC_CLIENTS = QName.createQName(BeCPGModel.BECPG_URI, "clients");

	// client
	QName TYPE_CLIENT = QName.createQName(BeCPGModel.BECPG_URI, "client");

	// product aspect
	QName ASPECT_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "productAspect");
	QName PROP_PRODUCT_HIERARCHY1 = QName.createQName(BeCPGModel.BECPG_URI, "productHierarchy1");
	QName PROP_PRODUCT_HIERARCHY2 = QName.createQName(BeCPGModel.BECPG_URI, "productHierarchy2");
	QName PROP_PRODUCT_STATE = QName.createQName(BeCPGModel.BECPG_URI, "productState");
	QName PROP_PRODUCT_UNIT = QName.createQName(BeCPGModel.BECPG_URI, "productUnit");
	QName PROP_PRODUCT_QTY = QName.createQName(BeCPGModel.BECPG_URI, "productQty");
	QName PROP_PRODUCT_DENSITY = QName.createQName(BeCPGModel.BECPG_URI, "productDensity");
	QName PROP_PRODUCT_COMMENTS = QName.createQName(BeCPGModel.BECPG_URI, "productComments");
	QName PROP_PRODUCT_SCORE = QName.createQName(BeCPGModel.BECPG_URI, "productScores");
	
	// transformation
	QName ASPECT_TRANSFORMATION = QName.createQName(BeCPGModel.BECPG_URI, "transformationAspect");	
	QName ASSOC_PRODUCT_SPECIFICATIONS = QName.createQName(BeCPGModel.BECPG_URI, "productSpecifications");
	
	QName PROP_PRODUCT_NET_VOLUME = QName.createQName(BeCPGModel.BECPG_URI, "netVolume");
	QName PROP_PRODUCT_NET_WEIGHT = QName.createQName(BeCPGModel.BECPG_URI, "netWeight");
	QName PROP_PRODUCT_COMPO_QTY_USED = QName.createQName(BeCPGModel.BECPG_URI, "productCompoQtyUsed");
	QName PROP_PRODUCT_COMPO_VOLUME_USED = QName.createQName(BeCPGModel.BECPG_URI, "productCompoVolumeUsed");

	// ean aspect
	QName ASPECT_EAN = QName.createQName(BeCPGModel.BECPG_URI, "eanAspect");
	QName PROP_EAN_CODE = QName.createQName(BeCPGModel.BECPG_URI, "eanCode");

	// code aspect
	QName ASPECT_ERP_CODE = QName.createQName(BeCPGModel.BECPG_URI, "erpCodeAspect");
	QName PROP_ERP_CODE = QName.createQName(BeCPGModel.BECPG_URI, "erpCode");

	// variant
	QName TYPE_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "variant");
	QName ASPECT_ENTITY_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "entityVariantAspect");
	QName ASPECT_ENTITYLIST_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "entityListVariantAspect");

	QName ASSOC_VARIANTS = QName.createQName(BeCPGModel.BECPG_URI, "variants");
	QName PROP_VARIANTIDS = QName.createQName(BeCPGModel.BECPG_URI, "variantIds");
	QName PROP_IS_DEFAULT_VARIANT = QName.createQName(BeCPGModel.BECPG_URI, "isDefaultVariant");

	// profitability
	QName ASPECT_PROFITABILITY = QName.createQName(BeCPGModel.BECPG_URI, "profitabilityAspect");
	QName PROP_UNIT_PRICE = QName.createQName(BeCPGModel.BECPG_URI, "unitPrice");
	QName PROP_BREAK_EVEN = QName.createQName(BeCPGModel.BECPG_URI, "breakEven");
	QName PROP_PROJECTED_QTY = QName.createQName(BeCPGModel.BECPG_URI, "projectedQty");
	QName PROP_PROFITABILITY = QName.createQName(BeCPGModel.BECPG_URI, "profitability");
	QName PROP_UNIT_TOTAL_COST = QName.createQName(BeCPGModel.BECPG_URI, "unitTotalCost");
	QName PROP_PRICE_CURRENCY = QName.createQName(BeCPGModel.BECPG_URI, "priceCurrency");

	// manufacturingAspect
	QName ASPECT_MANUFACTURING = QName.createQName(BeCPGModel.BECPG_URI, "manufacturingAspect");
	QName ASSOC_SUBSIDIARY = QName.createQName(BeCPGModel.BECPG_URI, "subsidiaryRef");
	QName ASSOC_PLANTS = QName.createQName(BeCPGModel.BECPG_URI, "plants");
	QName ASSOC_TRADEMARK = QName.createQName(BeCPGModel.BECPG_URI, "trademarkRef");

	// subsidiary
	QName TYPE_SUBSIDIARY = QName.createQName(BeCPGModel.BECPG_URI, "subsidiary");

	// plant
	QName TYPE_PLANT = QName.createQName(BeCPGModel.BECPG_URI, "plant");
	QName ASSOC_PLANT_CERTIFICATIONS = QName.createQName(BeCPGModel.BECPG_URI, "plantCertifications");
	QName ASSOC_PLANT_APPROVAL_NUMBERS = QName.createQName(BeCPGModel.BECPG_URI, "plantApprovalNumbers");

	// trademark
	QName TYPE_TRADEMARK = QName.createQName(BeCPGModel.BECPG_URI, "trademark");
	QName PROP_TRADEMARK_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "trademarkType");
	

	// certification
	QName TYPE_CERTIFICATION = QName.createQName(BeCPGModel.BECPG_URI, "certification");

	// approvalNumber
	QName TYPE_APPROVAL_NUMBER = QName.createQName(BeCPGModel.BECPG_URI, "approvalNumber");

	// labelClaim
	QName TYPE_LABEL_CLAIM = QName.createQName(BeCPGModel.BECPG_URI, "labelClaim");
	QName PROP_LABEL_CLAIM_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimType");
	QName PROP_LABEL_CLAIM_FORMULA = QName.createQName(BeCPGModel.BECPG_URI, "labelClaimFormula");

	// storageConditions
	QName TYPE_STORAGE_CONDITIONS = QName.createQName(BeCPGModel.BECPG_URI, "storageConditions");
	QName ASSOC_STORAGE_CONDITIONS = QName.createQName(BeCPGModel.BECPG_URI, "storageConditionsRef");
	
	// precautionOfUse
	QName TYPE_PRECAUTION_OF_USE = QName.createQName(BeCPGModel.BECPG_URI, "precautionOfUse");
	QName ASSOC_PRECAUTION_OF_USE = QName.createQName(BeCPGModel.BECPG_URI, "precautionOfUseRef");

	// instruction
	QName ASPECT_INSTRUCTION = QName.createQName(BeCPGModel.BECPG_URI, "instruction");
	QName PROP_INSTRUCTION = QName.createQName(BeCPGModel.BECPG_URI, "instruction");
	
	@Deprecated
	QName ASPECT_DILUENT = QName.createQName(BeCPGModel.BECPG_URI, "diluentAspect");
	QName ASPECT_RECONSTITUTABLE = QName.createQName(BeCPGModel.BECPG_URI, "reconstitutableAspect");
	QName PROP_RECONSTITUTION_RATE =  QName.createQName(BeCPGModel.BECPG_URI, "reconstitutionRate");
	QName PROP_RECONSTITUTION_PRIORITY =  QName.createQName(BeCPGModel.BECPG_URI, "reconstitutionPriority");
	QName ASSOC_DILUENT_REF =  QName.createQName(BeCPGModel.BECPG_URI, "diluentRef");
	QName ASSOC_TARGET_RECONSTITUTION_REF =  QName.createQName(BeCPGModel.BECPG_URI, "targetReconstitutionRef");
	
	//Compare
	QName ASPECT_COMPARE_WITH_DYN_COLUMN = QName.createQName(BeCPGModel.BECPG_URI, "compareWithDynColumnAspect");
	QName PROP_COMPARE_WITH_DYN_COLUMN =  QName.createQName(BeCPGModel.BECPG_URI, "compareWithDynColumn");

	//Nutrient profile
	QName ASPECT_NUTRIENT_PROFILING_SCORE = QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfilingScoreAspect");
	QName PROP_NUTRIENT_PROFILING_SCORE =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfilingScore");
	QName PROP_NUTRIENT_PROFILING_CLASS =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfilingClass");
	QName PROP_NUTRIENT_PROFILE_SCORE_FORMULA =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileScoreFormula");
	QName PROP_NUTRIENT_PROFILE_CLASS_FORMULA =  QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileClassFormula");
	QName TYPE_NUTRIENT_PROFILE = QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfile");
	
	//Custom codes
	QName TYPE_CUSTOMSCODE = QName.createQName(BeCPGModel.BECPG_URI, "customsCode");
	QName PROP_CUSTOMSCODE_CODE = QName.createQName(BeCPGModel.BECPG_URI, "cstsCode");
	QName ASPECT_CUSTOMSCODE = QName.createQName(BeCPGModel.BECPG_URI, "customsCodeAspect");
	QName ASSOC_CUSTOMSCODE = QName.createQName(BeCPGModel.BECPG_URI, "customsCodeRef");
	
	
	QName ASPECT_WATER = QName.createQName(BeCPGModel.BECPG_URI, "waterAspect");
	
	
	
}
