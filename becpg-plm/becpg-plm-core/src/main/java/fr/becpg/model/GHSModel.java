package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class GHSModel {

	public static  String GHS_URI = "http://www.bcpg.fr/model/ghs/1.0";

	public static  String GHS_PREFIX = "gsh";

	public static final  QName MODEL = QName.createQName(GHS_URI, "ghsModel");

	public static final QName TYPE_PERSONAL_PROTECTION = QName.createQName(GHS_URI, "personalProtection");
	public static final QName TYPE_PICTOGRAM = QName.createQName(GHS_URI, "pictogram");
	public static final QName TYPE_HAZARD_STATEMENT = QName.createQName(GHS_URI, "hazardStatement");
	public static final QName TYPE_PRECAUTIONARY_STATEMENT = QName.createQName(GHS_URI, "precautionaryStatement");

}
