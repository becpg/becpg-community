package fr.becpg.repo.formulation;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

public interface ReportableEntity extends FormulatedEntity {

	void addError(String error);

	void addWarning(String warning);

	void addInfo(String information);

	void addError(MLText i18nMessage);

	void addError(MLText msg, String formulationChainId, List<NodeRef> sources);

	boolean merge();

}
