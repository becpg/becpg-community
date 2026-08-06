/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.List;

import fr.becpg.repo.score.data.EntityScoreListDataItem;

/**
 * Entity able to carry several computed scores at once.
 *
 * <p>Implementing this interface is what makes an entity eligible to the score
 * framework: the orchestrator writes one {@link fr.becpg.repo.score.data.EntityScoreListDataItem}
 * per applicable score definition.</p>
 *
 * @author matthieu
 */
public interface ScoredEntity {

	/**
	 * <p>getEntityScoreList.</p>
	 *
	 * @return the computed scores of the entity, may be null when the list is not loaded
	 */
	List<EntityScoreListDataItem> getEntityScoreList();

	/**
	 * <p>setEntityScoreList.</p>
	 *
	 * @param entityScoreList the computed scores of the entity
	 */
	void setEntityScoreList(List<EntityScoreListDataItem> entityScoreList);

}
