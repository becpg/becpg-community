package fr.becpg.repo.formulation.spel;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>CustomSpelFunctions interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface CustomSpelFunctions {

	/**
	 * <p>match.</p>
	 *
	 * @param beanName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean match(String beanName);

	/**
	 * <p>create.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @return a {@link java.lang.Object} object.
	 */
	Object create(RepositoryEntity entity);

}
	
