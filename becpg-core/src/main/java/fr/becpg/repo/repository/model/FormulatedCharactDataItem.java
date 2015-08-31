package fr.becpg.repo.repository.model;

public interface FormulatedCharactDataItem extends SimpleCharactDataItem, ManualDataItem {

	Double getFormulatedValue();
	
	Boolean getIsFormulated();
	
	void setIsFormulated(Boolean isFormulated);

	String getErrorLog();
	
	void setErrorLog(String errorLog);
}
