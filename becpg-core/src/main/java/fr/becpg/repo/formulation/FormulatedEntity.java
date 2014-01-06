package fr.becpg.repo.formulation;

import java.util.Date;

import fr.becpg.repo.repository.RepositoryEntity;

public interface FormulatedEntity extends RepositoryEntity {

	public Date getFormulatedDate();
	public void setFormulatedDate(Date formulatedDate);
	
}
