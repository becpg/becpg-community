package fr.becpg.repo.product.formulation.score;

import fr.becpg.repo.product.data.ScorableEntity;

/**
 * <p>ScoreCalculatingPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ScoreCalculatingPlugin {

	/**
	 * <p>accept.</p>
	 *
	 * @param scorableEntity a {@link fr.becpg.repo.product.data.ScorableEntity} object
	 * @return a boolean
	 */
	public boolean accept(ScorableEntity scorableEntity);
	
	/**
	 * <p>formulateScore.</p>
	 *
	 * @param scorableEntity a {@link fr.becpg.repo.product.data.ScorableEntity} object
	 * @return a boolean
	 */
	public boolean formulateScore(ScorableEntity scorableEntity);
	
}
