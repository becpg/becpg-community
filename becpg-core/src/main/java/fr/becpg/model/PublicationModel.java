package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public interface PublicationModel {


	public static final String PUBLICATION_URI = "http://www.bcpg.fr/model/publication/1.0";

	public static final String PUBLICATION_PREFIX = "bp";


	/** The Constant MODEL. */
	public static final QName MODEL = QName.createQName(PUBLICATION_URI, "publicationModel");
	
	public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(PUBLICATION_URI,"MailingListChannel");
	
	public static final QName PROP_MAILLING_MEMBERS = QName.createQName(PUBLICATION_URI,"mailingMembers");
	
}
