package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.MLText;

public interface ReportableEntity {

	public void addError(String error);
	
	public void addWarning(String warning);
	
	public void addInfo(String information);

	public void addError(MLText i18nMessage);
	
}
