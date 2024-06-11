package fr.becpg.repo.formulation;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ReportableEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ReportableEntity extends FormulatedEntity {

	/**
	 * <p>addError.</p>
	 *
	 * @param error a {@link java.lang.String} object
	 */
	void addError(String error);

	/**
	 * <p>addWarning.</p>
	 *
	 * @param warning a {@link java.lang.String} object
	 */
	void addWarning(String warning);

	/**
	 * <p>addInfo.</p>
	 *
	 * @param information a {@link java.lang.String} object
	 */
	void addInfo(String information);

	/**
	 * <p>addError.</p>
	 *
	 * @param i18nMessage a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	void addError(MLText i18nMessage);

	/**
	 * <p>addError.</p>
	 *
	 * @param msg a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @param formulationChainId a {@link java.lang.String} object
	 * @param sources a {@link java.util.List} object
	 */
	void addError(MLText msg, String formulationChainId, List<NodeRef> sources);

	/**
	 * <p>merge.</p>
	 *
	 * @return a boolean
	 */
	boolean merge();
	
	/**
	 * <p>merge.</p>
	 *
	 * @param inactiveChainIds a {@link java.util.List} object
	 * @return a boolean
	 */
	boolean merge(List<String> inactiveChainIds);
	
}
