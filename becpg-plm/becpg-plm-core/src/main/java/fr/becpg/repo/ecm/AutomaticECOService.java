package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.ChangeOrderData;

public interface AutomaticECOService {

	boolean addAutomaticChangeEntry(NodeRef entityNodeRef, ChangeOrderData currentChangeOrder);

	boolean applyAutomaticEco();

	ChangeOrderData createAutomaticEcoForUser(String ecoName);

	ChangeOrderData getCurrentUserChangeOrderData();
	

}
