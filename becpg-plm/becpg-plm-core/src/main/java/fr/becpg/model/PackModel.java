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
 * <p>PackModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PackModel {

	/** Constant <code>PACK_URI="http://www.bcpg.fr/model/pack/1.0"</code> */
	public static final String PACK_URI = "http://www.bcpg.fr/model/pack/1.0";

	/** Constant <code>PACK_PREFIX="pack"</code> */
	public static final String PACK_PREFIX = "pack";

	/** Constant <code>MODEL</code> */
	public static final QName MODEL = QName.createQName(PACK_URI, "packmodel");

	/** Constant <code>ASPECT_PALLET</code> */
	public static final QName ASPECT_PALLET = QName.createQName(PACK_URI, "palletAspect");
	/** Constant <code>PROP_PALLET_LAYERS</code> */
	public static final QName PROP_PALLET_LAYERS = QName.createQName(PACK_URI, "palletLayers");
	/** Constant <code>PROP_PALLET_BOXES_PER_LAYER</code> */
	public static final QName PROP_PALLET_BOXES_PER_LAYER = QName.createQName(PACK_URI, "palletBoxesPerLayer");
	/** Constant <code>PROP_PALLET_BOXES_PER_PALLET</code> */
	public static final QName PROP_PALLET_BOXES_PER_PALLET = QName.createQName(PACK_URI, "palletBoxesPerPallet");
	/** Constant <code>PROP_PALLET_HEIGHT</code> */
	public static final QName PROP_PALLET_HEIGHT = QName.createQName(PACK_URI, "palletHeight");
	/** Constant <code>PROP_PALLET_NUMBER_ON_GROUND</code> */
	public static final QName PROP_PALLET_NUMBER_ON_GROUND = QName.createQName(PACK_URI, "palletNumberOnGround");

	/** Constant <code>PROP_PALLET_BOXES_PER_LAST_LAYER</code> */
	public static final QName PROP_PALLET_BOXES_PER_LAST_LAYER = QName.createQName(PACK_URI, "palletBoxesPerLastLayer");

	/** Constant <code>PROP_PALLET_STACKING_MAX_WEIGHT</code> */
	public static final QName PROP_PALLET_STACKING_MAX_WEIGHT = QName.createQName(PACK_URI, "palletStackingMaxWeight");

	/** Constant <code>PROP_PALLET_PRODUCTS_PER_BOX</code> */
	public static final QName PROP_PALLET_PRODUCTS_PER_BOX = QName.createQName(PACK_URI, "palletProductsPerBox");

	/** Constant <code>PACK_MATERIAL_LIST_TYPE</code> */
	public static final QName PACK_MATERIAL_LIST_TYPE = QName.createQName(PACK_URI, "packMaterialList");

	/** Constant <code>PROP_PACK_MATERIAL_LIST_WEIGHT</code> */
	public static final QName PROP_PACK_MATERIAL_LIST_WEIGHT = QName.createQName(PACK_URI, "pmlWeight");

	/** Constant <code>ASSOC_PACK_MATERIAL_LIST_MATERIAL</code> */
	public static final QName ASSOC_PACK_MATERIAL_LIST_MATERIAL = QName.createQName(PACK_URI, "pmlMaterial");

	/** Constant <code>ASPECT_TARE</code> */
	public static final QName ASPECT_TARE = QName.createQName(PACK_URI, "tareAspect");

	/** Constant <code>PROP_TARE</code> */
	public static final QName PROP_TARE = QName.createQName(PACK_URI, "tare");

	/** Constant <code>PROP_TARE_UNIT</code> */
	public static final QName PROP_TARE_UNIT = QName.createQName(PACK_URI, "tareUnit");

	/** Constant <code>ASPECT_SIZE</code> */
	public static final QName ASPECT_SIZE = QName.createQName(PACK_URI, "sizeAspect");

	/** Constant <code>PROP_LENGTH</code> */
	public static final QName PROP_LENGTH = QName.createQName(PACK_URI, "length");

	/** Constant <code>PROP_WIDTH</code> */
	public static final QName PROP_WIDTH = QName.createQName(PACK_URI, "width");

	/** Constant <code>PROP_HEIGHT</code> */
	public static final QName PROP_HEIGHT = QName.createQName(PACK_URI, "height");

	/** Constant <code>TYPE_LABELING_TEMPLATE</code> */
	public static final QName TYPE_LABELING_TEMPLATE = QName.createQName(PACK_URI, "labelingTemplate");
	/** Constant <code>ASPECT_LABELING</code> */
	public static final QName ASPECT_LABELING = QName.createQName(PACK_URI, "labelingAspect");
	/** Constant <code>ASSOC_LABELING_TEMPLATE</code> */
	public static final QName ASSOC_LABELING_TEMPLATE = QName.createQName(PACK_URI, "labelingLabelingTemplate");
	/** Constant <code>PROP_LABELING_POSITION</code> */
	public static final QName PROP_LABELING_POSITION = QName.createQName(PACK_URI, "labelingPosition");

	/** Constant <code>TYPE_LABEL</code> */
	public static final QName TYPE_LABEL = QName.createQName(PACK_URI, "label");
	/** Constant <code>PROP_LABEL_TYPE</code> */
	public static final QName PROP_LABEL_TYPE = QName.createQName(PACK_URI, "labelType");

	/** Constant <code>TYPE_LABELING_LIST</code> */
	public static final QName TYPE_LABELING_LIST = QName.createQName(PACK_URI, "labelingList");
	/** Constant <code>ASSOC_LL_LABEL</code> */
	public static final QName ASSOC_LL_LABEL = QName.createQName(PACK_URI, "llLabel");
	/** Constant <code>PROP_LL_TYPE</code> */
	public static final QName PROP_LL_TYPE = QName.createQName(PACK_URI, "llType");
	/** Constant <code>PROP_LL_POSITION</code> */
	public static final QName PROP_LL_POSITION = QName.createQName(PACK_URI, "llPosition");

	/** Constant <code>ASPECT_PM_MATERIAL</code> */
	public static final QName ASPECT_PM_MATERIAL = QName.createQName(PACK_URI, "pmMaterialAspect");

	/** Constant <code>ASSOC_PM_MATERIAL</code> */
	public static final QName ASSOC_PM_MATERIAL = QName.createQName(PACK_URI, "pmMaterialRefs");

	/** Constant <code>TYPE_PACKAGING_MATERIAL</code> */
	public static final QName TYPE_PACKAGING_MATERIAL  = QName.createQName(PACK_URI, "packMaterial");

	/** Constant <code>PROP_PM_ECOSCORE</code> */
	public static final QName PROP_PM_ECOSCORE   = QName.createQName(PACK_URI, "pmEcoScore");

	/** Constant <code>PROP_PM_ISNOTRECYCLABLE</code> */
	public static final QName PROP_PM_ISNOTRECYCLABLE  = QName.createQName(PACK_URI, "pmIsNotRecyclable");

}
