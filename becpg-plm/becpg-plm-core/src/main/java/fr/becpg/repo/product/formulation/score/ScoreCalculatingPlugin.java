package fr.becpg.repo.product.formulation.score;

import fr.becpg.repo.product.data.ScorableEntity;

public interface ScoreCalculatingPlugin {

	public boolean accept(ScorableEntity scorableEntity);
	
	public boolean formulateScore(ScorableEntity scorableEntity);
	
}
