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
 * <p>GS1Model interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface GS1Model {

	/** Constant <code>GS1_URI="http://www.bcpg.fr/model/gs1/1.0"</code> */
	String GS1_URI = "http://www.bcpg.fr/model/gs1/1.0";

	/** Constant <code>BECPG_PREFIX="gs1"</code> */
	String BECPG_PREFIX = "gs1";

	/** Constant <code>MODEL</code> */
	QName MODEL = QName.createQName(GS1_URI, "gs1Model");

	/** Constant <code>ASPECT_MEASURES_ASPECT</code> */
	QName ASPECT_MEASURES_ASPECT = QName.createQName(GS1_URI, "measuresAspect");

	/** Constant <code>ASPECT_GS1_ASPECT</code> */
	QName ASPECT_GS1_ASPECT = QName.createQName(GS1_URI, "gs1Aspect");
	

	/** Constant <code>ASPECT_PACK_SORTING</code> */
	QName ASPECT_PACK_SORTING = QName.createQName(GS1_URI, "packSortingAspect");

	/** Constant <code>PROP_WEIGHT</code> */
	QName PROP_WEIGHT = QName.createQName(GS1_URI, "weight");
	/** Constant <code>PROP_SECONDARY_WEIGHT</code> */
	QName PROP_SECONDARY_WEIGHT = QName.createQName(GS1_URI, "secondaryWeight");

	/** Constant <code>PROP_INNERPACK_WEIGHT</code> */
	QName PROP_INNERPACK_WEIGHT = QName.createQName(GS1_URI, "innerPackWeight");

	/** Constant <code>PROP_INNERPACK_NET_WEIGHT</code> */
	QName PROP_INNERPACK_NET_WEIGHT = QName.createQName(GS1_URI, "innerPackNetWeight");
	/** Constant <code>PROP_TERTIARY_WEIGHT</code> */
	QName PROP_TERTIARY_WEIGHT = QName.createQName(GS1_URI, "tertiaryWeight");
	/** Constant <code>PROP_SECONDARY_NET_WEIGHT</code> */
	QName PROP_SECONDARY_NET_WEIGHT = QName.createQName(GS1_URI, "secondaryNetWeight");
	/** Constant <code>PROP_TERTIARY_NET_WEIGHT</code> */
	QName PROP_TERTIARY_NET_WEIGHT = QName.createQName(GS1_URI, "tertiaryNetWeight");

	/** Constant <code>PROP_NUTRIENT_TYPE_CODE</code> */
	QName PROP_NUTRIENT_TYPE_CODE = QName.createQName(GS1_URI, "nutrientTypeCode");

	/** Constant <code>PROP_WIDTH</code> */
	QName PROP_WIDTH = QName.createQName(GS1_URI, "width");

	/** Constant <code>PROP_DEPTH</code> */
	QName PROP_DEPTH = QName.createQName(GS1_URI, "depth");

	/** Constant <code>PROP_HEIGHT</code> */
	QName PROP_HEIGHT = QName.createQName(GS1_URI, "height");

	/** Constant <code>PROP_PACKAGING_TYPE_CODE</code> */
	QName PROP_PACKAGING_TYPE_CODE = QName.createQName(GS1_URI, "packagingTypeCode");

	/** Constant <code>PROP_SECONDARY_WIDTH</code> */
	QName PROP_SECONDARY_WIDTH = QName.createQName(GS1_URI, "secondaryWidth");

	/** Constant <code>PROP_SECONDARY_DEPTH</code> */
	QName PROP_SECONDARY_DEPTH = QName.createQName(GS1_URI, "secondaryDepth");

	/** Constant <code>PROP_SECONDARY_HEIGHT</code> */
	QName PROP_SECONDARY_HEIGHT = QName.createQName(GS1_URI, "secondaryHeight");

	/** Constant <code>PROP_PRODUCT_PER_INNER_PACK</code> */
	QName PROP_PRODUCT_PER_INNER_PACK = QName.createQName(GS1_URI, "productPerInnerPack");

	/** Constant <code>PROP_TERTIARY_WIDTH</code> */
	QName PROP_TERTIARY_WIDTH = QName.createQName(GS1_URI, "tertiaryWidth");

	/** Constant <code>PROP_TERTIARY_DEPTH</code> */
	QName PROP_TERTIARY_DEPTH = QName.createQName(GS1_URI, "tertiaryDepth");

	/** Constant <code>PROP_PALLET_TYPE_CODE</code> */
	QName PROP_PALLET_TYPE_CODE = QName.createQName(GS1_URI, "palletTypeCode");

	/** Constant <code>PROP_PLATFORMTERMSANSCONDITION_CODE</code> */
	QName PROP_PLATFORMTERMSANSCONDITION_CODE = QName.createQName(GS1_URI, "platformTermsAndConditionsCode");

	/** Constant <code>TYPE_TARGET_MARKET</code> */
	QName TYPE_TARGET_MARKET = QName.createQName(GS1_URI, "targetMarket");
	
	/** Constant <code>ASSOC_TARGET_MARKET_COUNTRIES</code> */
	QName ASSOC_TARGET_MARKET_COUNTRIES = QName.createQName(GS1_URI, "targetMarketCountries");

	/** Constant <code>TYPE_DUTY_FEE_TAX</code> */
	QName TYPE_DUTY_FEE_TAX = QName.createQName(GS1_URI, "dutyFeeTax");
	/** Constant <code>PROP_PALLET_TYPE_CODE</code> */
	QName PROP_DUTY_FEE_TAX_RATE = QName.createQName(GS1_URI, "dutyFeeTaxRate");

	/** Constant <code>PROP_PRODUCT_ACTIVITY_TYPE_CODE</code> */
	QName PROP_PRODUCT_ACTIVITY_TYPE_CODE = QName.createQName(GS1_URI, "productActivityTypeCode");

	/** Constant <code>TYPE_TRADEITEM_PRICE_LIST</code> */
	QName TYPE_TRADEITEM_PRICE_LIST = QName.createQName(GS1_URI, "tradeItemPriceList");

	/** Constant <code>TYPE_COLLECTION_PRICE_LIST</code> */
	QName TYPE_COLLECTION_PRICE_LIST = QName.createQName(GS1_URI, "collectionPriceList");

	/** Constant <code>PROP_PACKAGINGTERMSANSCONDITION_CODE</code> */
	QName PROP_PACKAGINGTERMSANSCONDITION_CODE = QName.createQName(GS1_URI, "packagingTermsAndConditionsCode");

	/** Constant <code>PROP_SECONDARY_PACKAGING_TYPE_CODE</code> */
	QName PROP_SECONDARY_PACKAGING_TYPE_CODE = QName.createQName(GS1_URI, "secondaryPackagingTypeCode");

	/** Constant <code>TYPE_ALCOHOL_BEVERAGE_CONTAINER</code> */
	QName TYPE_ALCOHOL_BEVERAGE_CONTAINER = QName.createQName(GS1_URI, "alcoholBeverageContainer");

}
