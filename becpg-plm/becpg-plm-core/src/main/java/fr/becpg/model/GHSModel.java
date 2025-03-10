package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>GHSModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GHSModel {

	/** Constant <code>GHS_URI="http://www.bcpg.fr/model/ghs/1.0"</code> */
	public static final String GHS_URI = "http://www.bcpg.fr/model/ghs/1.0";

	/** Constant <code>GHS_PREFIX="gsh"</code> */
	public static final String GHS_PREFIX = "gsh";

	/** Constant <code>MODEL</code> */
	public static final  QName MODEL = QName.createQName(GHS_URI, "ghsModel");

	/** Constant <code>TYPE_PERSONAL_PROTECTION</code> */
	public static final QName TYPE_PERSONAL_PROTECTION = QName.createQName(GHS_URI, "personalProtection");
	/** Constant <code>TYPE_PICTOGRAM</code> */
	public static final QName TYPE_PICTOGRAM = QName.createQName(GHS_URI, "pictogram");
	/** Constant <code>TYPE_HAZARD_STATEMENT</code> */
	public static final QName TYPE_HAZARD_STATEMENT = QName.createQName(GHS_URI, "hazardStatement");
	/** Constant <code>TYPE_PRECAUTIONARY_STATEMENT</code> */
	public static final QName TYPE_PRECAUTIONARY_STATEMENT = QName.createQName(GHS_URI, "precautionaryStatement");
	/** Constant <code>TYPE_ONU_CODE</code> */
	public static final QName TYPE_ONU_CODE = QName.createQName(GHS_URI, "onuCode");
	/** Constant <code>TYPE_CLASS_CODE</code> */
	public static final QName TYPE_CLASS_CODE = QName.createQName(GHS_URI, "classCode");
	/** Constant <code>TYPE_PACKAGING_GROUP_CODE</code> */
	public static final QName TYPE_PACKAGING_GROUP_CODE = QName.createQName(GHS_URI, "packagingGroupCode");

	/** Constant <code>PROP_SDS_HAZARD_CLASSIFICATIONS</code> */
	public static final QName PROP_SDS_HAZARD_CLASSIFICATIONS = QName.createQName(GHS_URI, "sdsHazardClassifications");

	/** Constant <code>TYPE_HAZARD_CLASSIFICATION_LIST</code> */
	public static final QName TYPE_HAZARD_CLASSIFICATION_LIST = QName.createQName(GHS_URI, "hazardClassificationList");

	/** Constant <code>PROP_HAZARD_CODE</code> */
	public static final QName PROP_HAZARD_CODE = QName.createQName(GHS_URI, "hazardCode");
	/** Constant <code>PROP_PICTOGRAM_CODE</code> */
	public static final QName PROP_PICTOGRAM_CODE = QName.createQName(GHS_URI, "pictogramCode");

}
