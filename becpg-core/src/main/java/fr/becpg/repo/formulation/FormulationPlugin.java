package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>FormulationPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface FormulationPlugin {
	
	enum FormulationPluginPriority {
		HIGHT, NORMAL, LOW, NONE;

		public boolean isHigherPriority(FormulationPluginPriority compareTo) {
			if (LOW.equals(compareTo) && (NORMAL.equals(this) || HIGHT.equals(this)))
				return true;

			if (NORMAL.equals(compareTo) && HIGHT.equals(this))
				return true;

			return false;
		}
	}
	
	/**
	 * <p>getMatchPriority.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link fr.becpg.repo.formulation.FormulationPlugin.FormulationPluginPriority} object.
	 */
	FormulationPluginPriority getMatchPriority(QName type);

	/**
	 * <p>runFormulation.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 * @param chainId a {@link java.lang.String} object
	 */
	void runFormulation(NodeRef entityNodeRef, String chainId);

}
