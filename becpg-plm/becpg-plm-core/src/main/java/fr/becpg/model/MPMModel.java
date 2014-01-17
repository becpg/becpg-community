package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class MPMModel {

	public static final String MPM_URI = "http://www.bcpg.fr/model/mpm/1.0";

	public static final String MPM_PREFIX = "mpm";

	public static final QName MODEL = QName.createQName(MPM_URI, "mpmmodel");
	
	//processStep
	public static final QName TYPE_PROCESSSTEP = QName.createQName(MPM_URI,
			"processStep");
	
	//processList
	public static final QName TYPE_PROCESSLIST = QName.createQName(MPM_URI,
			"processList");
	public static final QName PROP_PL_QTY = QName.createQName(MPM_URI,
			"plQty");
	public static final QName PROP_PL_QTY_RESOURCE = QName.createQName(MPM_URI,
			"plQtyResource");
	public static final QName PROP_PL_RATE_RESOURCE = QName.createQName(MPM_URI,
			"plRateResource");
	public static final QName PROP_PL_YIELD = QName.createQName(MPM_URI,
			"plYield");
	public static final QName PROP_PL_RATE_PROCESS = QName.createQName(MPM_URI,
			"plRateProcess");
	public static final QName PROP_PL_RATE_PRODUCT = QName.createQName(MPM_URI,
			"plRateProduct");
	public static final QName ASSOC_PL_STEP = QName.createQName(MPM_URI,
			"plStep");
	public static final QName ASSOC_PL_PRODUCT = QName.createQName(MPM_URI,
			"plProduct");
	public static final QName ASSOC_PL_RESOURCE = QName.createQName(MPM_URI,
			"plResource");	
}
