package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.ecm.data.ChangeOrderData;

/**
 * <p>AutomaticECOService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface AutomaticECOService {

	/**
	 * <p>addAutomaticChangeEntry.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param currentChangeOrder a {@link fr.becpg.repo.ecm.data.ChangeOrderData} object.
	 * @return a boolean.
	 */
	boolean addAutomaticChangeEntry(NodeRef entityNodeRef, ChangeOrderData currentChangeOrder);

	/**
	 * <p>applyAutomaticEco.</p>
	 *
	 * @return a boolean.
	 */
	boolean applyAutomaticEco();

	/**
	 * <p>createAutomaticEcoForUser.</p>
	 *
	 * @param ecoName a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.ecm.data.ChangeOrderData} object.
	 */
	ChangeOrderData createAutomaticEcoForUser(String ecoName);

	/**
	 * <p>getCurrentUserChangeOrderData.</p>
	 *
	 * @return a {@link fr.becpg.repo.ecm.data.ChangeOrderData} object.
	 */
	ChangeOrderData getCurrentUserChangeOrderData();


}
