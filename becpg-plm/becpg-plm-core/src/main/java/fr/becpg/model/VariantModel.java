package fr.becpg.model;

import org.alfresco.service.namespace.QName;

@Deprecated
public class VariantModel {

	public static final String VARIANT_URI = "http://www.bcpg.fr/model/var/1.0";

	public static final String VARIANT_PREFIX = "var";

	public static final QName MODEL = QName.createQName(VARIANT_URI, "varmodel");
	
	//charact
	public static final QName TYPE_CHARACT = QName.createQName(VARIANT_URI,
			"charact");
		
}
