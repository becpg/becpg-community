package fr.becpg.repo.formulation.spel;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>SpelFormulaContext interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SpelFormulaContext<T extends RepositoryEntity> {
	

	public enum Operator {
		SUM,AVG,PERC,MIN,MAX
	}

	/**
	 * <p>setEntity.</p>
	 *
	 * @param repositoryEntity a T object.
	 */
	void setEntity(T repositoryEntity);
	
	/**
	 * <p>getEntity.</p>
	 *
	 * @return a T object.
	 */
	public T getEntity();

	
}
