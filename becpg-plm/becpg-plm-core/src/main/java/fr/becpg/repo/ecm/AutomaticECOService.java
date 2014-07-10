package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.ChangeOrderData;

public interface AutomaticECOService {

	public ChangeOrderData addAutomaticChangeEntry(NodeRef entityNodeRef);

	public boolean applyAutomaticEco();

	public ChangeOrderData createAutomaticEcoForUser(String ecoName);
	

}
