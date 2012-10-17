package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class PackModel {

	public static final String PACK_URI = "http://www.bcpg.fr/model/pack/1.0";

	public static final String PACK_PREFIX = "pack";

	public static final QName MODEL = QName.createQName(PACK_URI, "packmodel");
	
	public static final QName ASPECT_PALLET = QName.createQName(PACK_URI,
			"palletAspect");
	public static final QName PROP_PALLET_LAYERS = QName.createQName(PACK_URI,
			"palletLayers");
	public static final QName PROP_PALLET_BOXES_PER_LAYER = QName.createQName(PACK_URI,
			"palletBoxesPerLayer");
	public static final QName PROP_PALLET_BOXES_PER_PALLET = QName.createQName(PACK_URI,
			"palletBoxesPerPallet");
	public static final QName PROP_PALLET_DIMENSIONS = QName.createQName(PACK_URI,
			"palletLayers");	
}
