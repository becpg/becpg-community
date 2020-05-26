package fr.becpg.repo.formulation.spel;

import fr.becpg.repo.repository.RepositoryEntity;

public interface CustomSpelFunctions {

	boolean match(String beanName);

	Object create(RepositoryEntity entity);

}
	