package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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
	
	FormulationPluginPriority getMatchPriority(QName type);

	void runFormulation(NodeRef entityNodeRef) throws FormulateException;

}
