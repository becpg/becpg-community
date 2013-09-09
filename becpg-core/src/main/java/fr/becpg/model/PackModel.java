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
	public static final QName PROP_PALLET_HEIGHT = QName.createQName(PACK_URI,
			"palletHeight");	
	
	public static final QName ASPECT_TARE =   QName.createQName(PACK_URI,
			"tareAspect");
	
	public static final QName PROP_TARE = QName.createQName(PACK_URI,
			"tare");	
	
	public static final QName PROP_TARE_UNIT = QName.createQName(PACK_URI,
			"tareUnit");	
	
	public static final QName ASPECT_SIZE =   QName.createQName(PACK_URI,
			"sizeAspect");
	
	public static final QName PROP_LENGTH = QName.createQName(PACK_URI,
			"length");	
	
	public static final QName PROP_WIDTH = QName.createQName(PACK_URI,
			"width");	
	
	public static final QName PROP_HEIGHT = QName.createQName(PACK_URI,
			"height");	
	
	public static final QName TYPE_LABELING_TEMPLATE = QName.createQName(PACK_URI, "labelingTemplate");
	public static final QName ASPECT_LABELING =   QName.createQName(PACK_URI,
			"labelingAspect");
	public static final QName ASSOC_LABELING_TEMPLATE = QName.createQName(PACK_URI, "labelingTemplate");
	public static final QName PROP_LABELING_POSITION = QName.createQName(PACK_URI, "labelingPosition");
	
	public static final QName TYPE_LABEL = QName.createQName(PACK_URI, "label");
	public static final QName PROP_LABEL_TYPE = QName.createQName(PACK_URI, "labelType");
	
	public static final QName TYPE_LABELING_LIST = QName.createQName(PACK_URI, "labelingList");
	public static final QName ASSOC_LL_LABEL = QName.createQName(PACK_URI, "llLabel");
	public static final QName PROP_LL_TYPE = QName.createQName(PACK_URI, "llType");
}
