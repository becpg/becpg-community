package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>AsyncECOService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface AsyncECOService {

    /**
     * <p>applyAsync.</p>
     *
     * @param ecoNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
     */
    void applyAsync(NodeRef ecoNodeRef);
	
	/**
	 * <p>doSimulationAsync.</p>
	 *
	 * @param ecoNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void doSimulationAsync(NodeRef ecoNodeRef);
	
}
