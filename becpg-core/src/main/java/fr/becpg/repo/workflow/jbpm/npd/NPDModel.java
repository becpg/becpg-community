package fr.becpg.repo.workflow.jbpm.npd;

import org.alfresco.service.namespace.QName;

public class NPDModel {

	public static final String NPD_URI = "http://www.bcpg.fr/model/npd/1.0";

	public static final String NPD_PREFIX = "npd";

	public static final QName MODEL = QName.createQName(NPD_URI, "npdmodel");
		
	//npdAspect
	public static final QName ASPECT_NPD = QName.createQName(NPD_URI,
			"npdAspect");
	public static final QName PROP_NPD_NUMBER = QName.createQName(NPD_URI,
			"npdNumber");
	public static final QName PROP_NPD_TYPE = QName.createQName(NPD_URI,
			"npdType");
	public static final QName PROP_NPD_STATUS = QName.createQName(NPD_URI,
			"npdStatus");
	public static final QName PROP_NPD_INITIATOR = QName.createQName(NPD_URI,
			"npdInitiator");
		
}
