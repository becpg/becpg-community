package fr.becpg.repo.entity.catalog;

import java.util.List;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>ScorableEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface CataloguableEntity extends RepositoryEntity  {

	/**
	 * <p>getEntityScore.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getEntityScore();

	/**
	 * <p>setEntityScore.</p>
	 *
	 * @param string a {@link java.lang.String} object
	 */
	void setEntityScore(String string);

	List<String> getReportLocales();


}
