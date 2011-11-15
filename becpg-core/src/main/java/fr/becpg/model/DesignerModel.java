package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class DesignerModel {


	public static final String M2_URI = "http://www.bcpg.fr/model/m2/1.0";
	
	public static final String DESIGNER_URI = "http://www.bcpg.fr/model/designer/1.0";

	
	
	public static final QName TYPE_M2_MODEL = QName.createQName(M2_URI,
			"model");
	public static final QName ASPECT_MODEL =  QName.createQName(DESIGNER_URI,
			"modelAspect");

	public static final QName PROP_M2_NAME =  QName.createQName(M2_URI,
			"name");

	public static final QName ASSOC_MODEL = QName.createQName(DESIGNER_URI,
			"model");

	public static final QName PROP_M2_TITLE =  QName.createQName(M2_URI,
			"title");
	public static final QName PROP_M2_DESCRIPTION =  QName.createQName(M2_URI,
			"description");
	
	public static final QName PROP_M2_URI =  QName.createQName(M2_URI,
			"uri");
	public static final QName PROP_M2_PREFIX =  QName.createQName(M2_URI,
			"prefix");

	public static final QName PROP_M2_REF =  QName.createQName(M2_URI,
			"ref");

}
