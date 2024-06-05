package fr.becpg.repo.formulation;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ReportableEntityService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ReportableEntityService {

	/**
	 * <p>postEntityErrors.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param formulationChainId a {@link java.lang.String} object
	 * @param errors a {@link java.util.Set} object
	 */
	void postEntityErrors(NodeRef entityNodeRef, String formulationChainId, Set<ReportableError> errors);
	
}
