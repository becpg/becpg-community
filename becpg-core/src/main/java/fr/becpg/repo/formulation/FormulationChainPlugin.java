package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>FormulationChainPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface FormulationChainPlugin {

	/**
	 * <p>getChainId.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getChainId();
	
	/**
	 * <p>isChainActiveOnEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	boolean isChainActiveOnEntity(NodeRef entityNodeRef);
}
