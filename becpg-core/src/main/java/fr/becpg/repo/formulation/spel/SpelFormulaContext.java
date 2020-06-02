package fr.becpg.repo.formulation.spel;

import fr.becpg.repo.repository.RepositoryEntity;

public interface  SpelFormulaContext<T extends RepositoryEntity> {
	

	public enum Operator {
		SUM,AVG,PERC
	}

	void setEntity(T repositoryEntity);
	
	public T getEntity();

	
}
