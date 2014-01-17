package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public interface GS1Model {


	public static final String GS1_URI = "http://www.bcpg.fr/model/gs1/1.0";

	public static final String BECPG_PREFIX = "gs1";


	/** The Constant MODEL. */
	public static final QName MODEL = QName.createQName(GS1_URI, "gs1Model");
	
	public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(GS1_URI,"DeliveryChannel");
	
	
}
