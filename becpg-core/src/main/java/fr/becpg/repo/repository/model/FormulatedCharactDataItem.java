package fr.becpg.repo.repository.model;

public interface FormulatedCharactDataItem extends SimpleCharactDataItem {

	Double getFormulatedValue();

	public String getErrorLog();
	
	public void setErrorLog(String errorLog);
}
