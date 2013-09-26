package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class ClaimWorkflowModel {

	public static final String NC_URI = "http://www.bcpg.fr/model/nc-workflow/1.0";
	public static final QName PROP_REJECTED_STATE = QName.createQName(NC_URI, "claimRejectedState");
	public static final QName PROP_REJECTED_CAUSE = QName.createQName(NC_URI, "claimRejectedCause");
	
	
}
