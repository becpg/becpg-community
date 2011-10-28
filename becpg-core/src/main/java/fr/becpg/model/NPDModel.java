package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public interface NPDModel {

	//
	// Namespace
	//

	/** Security Model URI */
	public static final String NPD_URI = "http://www.bcpg.fr/model/npd-workflow/1.0";

	/** Security Model Prefix */
	public static final String NPD_PREFIX = "npdModel";

	//
	// NPD Model Definitions
	//
	public static final QName MODEL = QName.createQName(NPD_URI, "secmodel");

	public static final QName ASPECT_NPD = QName.createQName(NPD_URI, "npdAspect");

	public static final QName PROP_NPD_PRODUCT_NAME = QName.createQName(NPD_URI, "npdProductName");

	public static final QName PROP_NPD_NUMBER = QName.createQName(NPD_URI, "npdNumber");

	public static final QName PROP_NPD_TYPE = QName.createQName(NPD_URI, "npdType");

	public static final QName PROP_NPD_STATUS = QName.createQName(NPD_URI, "npdStatus");

	public static final QName ASSOC_NPD_FOLDER = QName.createQName(NPD_URI, "npdFolder");

	
	//CFT
	
	public static final QName PROP_CFT_TRANSMITTER = QName.createQName(NPD_URI, "cftTransmitter");
	public static final QName PROP_CFT_COMPANY = QName.createQName(NPD_URI, "cftCompany");
	public static final QName PROP_CFT_OPENING_DATE = QName.createQName(NPD_URI, "cftOpeningDate");
	public static final QName PROP_CFT_SAMPLING_DATE = QName.createQName(NPD_URI, "cftSamplingDate");
	public static final QName PROP_CFT_LAUNCH_DATE_DESIRED = QName.createQName(NPD_URI, "cftLaunchDateDesired");
	public static final QName PROP_CFT_RESPONSE_DATE_DESIRED = QName.createQName(NPD_URI, "cftResponseDateDesired");
	public static final QName PROP_CFT_REPONSE_DATEREALIZED = QName.createQName(NPD_URI, "cftResponseDateRealized");
	public static final QName ASSOC_CFT_CLIENT = QName.createQName(NPD_URI, "cftClient");

	public static final QName PROP_RECIPE_DESCRIPTION = QName.createQName(NPD_URI, "recipeDescription");;

	public static final QName PROP_PACKAGING_DESCRIPTION = QName.createQName(NPD_URI, "packagingDescription");;

	public static final QName ASSOC_NEED_DEFINITION_PRODUCT = QName.createQName(NPD_URI, "needDefinitionProduct");;

	public static final QName ASSOC_NEED_DEFINITION_RECIPE = QName.createQName(NPD_URI, "needDefinitionRecipeProduct");;

	public static final QName ASSOC_NEED_DEFINITION_PACKAGING = QName.createQName(NPD_URI, "needDefinitionPackagingKit");

	public static final QName PROP_UNIT_PRICE = QName.createQName(NPD_URI, "unitPrice");

}
