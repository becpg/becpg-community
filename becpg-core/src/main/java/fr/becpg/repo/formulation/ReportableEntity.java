package fr.becpg.repo.formulation;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

public interface ReportableEntity extends FormulatedEntity {

	public void addError(String error);
	
	public void addWarning(String warning);
	
	public void addInfo(String information);

	public void addError(MLText i18nMessage);
	
	public void addError(String msg, String formulationChainId, List<NodeRef> sources);
	
	boolean mergeRequirements(boolean removeItemsWithoutFormulationId);

}
