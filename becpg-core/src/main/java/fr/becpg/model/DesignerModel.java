package fr.becpg.model;

import org.alfresco.service.namespace.QName;
/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class DesignerModel {


	public static final String M2_URI = "http://www.bcpg.fr/model/m2/1.0";
	
	public static final String DESIGNER_URI = "http://www.bcpg.fr/model/designer/1.0";

	/**
	 * Types
	 */
	
	public static final QName TYPE_M2_MODEL = QName.createQName(M2_URI,
			"model");
	
	public static final QName TYPE_M2_NAMESPACE =  QName.createQName(M2_URI,"namespace");

	public static final QName TYPE_M2_TYPE = QName.createQName(M2_URI,"type");
	

	public static final QName TYPE_M2_CHILD_ASSOCIATION = QName.createQName(M2_URI,"childAssociation");
	

	public static final QName TYPE_M2_PROPERTY = QName.createQName(M2_URI,"property");
	
	public static final QName TYPE_M2_PROPERTY_OVERRIDE = QName.createQName(M2_URI,"propertyOverride");
	
	
	/**
	 * Aspects
	 */
	
	public static final QName ASPECT_MODEL =  QName.createQName(DESIGNER_URI,
			"modelAspect");

	
	/**
	 * Props
	 */
	
	public static final QName PROP_M2_NAME =  QName.createQName(M2_URI,
			"name");


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


	public static final QName PROP_M2_PARENT_NAME = QName.createQName(M2_URI,
			"parentName");

	public static final QName PROP_M2_INDEX_MODE  = QName.createQName(M2_URI,
			"indexTokenisationMode");


	/**
	 * Assoc
	 */


	public static final QName ASSOC_MODEL = QName.createQName(DESIGNER_URI,
			"model");


	public static final QName ASSOC_M2_NAMESPACES = QName.createQName(M2_URI,"namespaces");

	public static final QName ASSOC_M2_TYPES = QName.createQName(M2_URI,"types");
	
	public static final QName ASSOC_M2_IMPORTS = QName.createQName(M2_URI,"imports");
	
	public static final QName ASSOC_M2_ASPECTS = QName.createQName(M2_URI,"aspects");

	public static final QName ASSOC_M2_CONSTRAINTS = QName.createQName(M2_URI,"constraints");

	public static final QName ASSOC_M2_DATA_TYPE = QName.createQName(M2_URI,"dataTypes");

	
}
