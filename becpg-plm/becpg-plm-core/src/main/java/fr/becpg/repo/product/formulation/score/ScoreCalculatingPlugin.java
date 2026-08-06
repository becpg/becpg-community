package fr.becpg.repo.product.formulation.score;

import java.util.Optional;

import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.score.ScoreContext;

/**
 * <p>ScoreCalculatingPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ScoreCalculatingPlugin {

	/** Constant <code>ANY_VERSION=""</code> */
	String ANY_VERSION = "";

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

	/**
	 * Code of the score produced, matching the {@code bcpg:scoreDefCode} of its definition.
	 *
	 * @return a {@link java.lang.String} object
	 */
	default String getCode() {
		return getClass().getSimpleName();
	}

	/**
	 * Version of the method implemented, matching the {@code bcpg:scoreDefVersion} of its
	 * definition. {@link #ANY_VERSION} means the plugin serves every version of the code,
	 * typically because it delegates to versioned helpers.
	 *
	 * @return a {@link java.lang.String} object
	 */
	default String getVersion() {
		return ANY_VERSION;
	}

	/**
	 * Breakdown of the last computed score, in the normalized format shared by all scores.
	 * An empty result means the plugin does not publish a breakdown yet.
	 *
	 * @param scorableEntity a {@link fr.becpg.repo.product.data.ScorableEntity} object
	 * @return a {@link java.util.Optional} object
	 */
	default Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
