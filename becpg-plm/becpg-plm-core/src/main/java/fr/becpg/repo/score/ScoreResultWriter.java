/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.score.data.EntityScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Publishes a computed score into the score list of an entity.
 *
 * <p>Writing is a no-op when no score definition matches the computed score, so the
 * framework stays dormant until definitions are created: the historical properties such
 * as {@code bcpg:nutrientProfilingScore} keep being the only output.</p>
 *
 * @author matthieu
 */
@Service("scoreResultWriter")
public class ScoreResultWriter {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ScoreResultWriter.class);

	private final ScoreDefinitionService scoreDefinitionService;

	/**
	 * <p>Constructor for ScoreResultWriter.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 */
	@Autowired
	public ScoreResultWriter(ScoreDefinitionService scoreDefinitionService) {
		this.scoreDefinitionService = scoreDefinitionService;
	}

	/**
	 * <p>Writes a computed score into the score list of an entity.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param context the breakdown of the computed score
	 */
	public void write(ScoredEntity entity, ScoreContext context) {
		Optional<ScoreDefinitionItem> definition = scoreDefinitionService.findByCode(context.getCode(), context.getVersion());

		if (definition.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("No score definition for code " + context.getCode() + ", skipping score list");
			}
			return;
		}

		context.computeShares();

		EntityScoreListDataItem item = findOrCreateItem(entity, definition.get());

		item.keepPreviousValue();
		item.setValue(context.getValue());
		item.setScoreClass(context.getScoreClass());
		item.setDetails(context.toJSON().toString());
		item.setVersion(context.getVersion());
		item.setComputedDate(new Date());
	}

	/**
	 * <p>findOrCreateItem.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.data.EntityScoreListDataItem} object
	 */
	private EntityScoreListDataItem findOrCreateItem(ScoredEntity entity, ScoreDefinitionItem definition) {
		if (entity.getEntityScoreList() == null) {
			entity.setEntityScoreList(new ArrayList<>());
		}

		for (EntityScoreListDataItem item : entity.getEntityScoreList()) {
			if (Objects.equals(item.getScoreDef(), definition.getNodeRef())) {
				return item;
			}
		}

		EntityScoreListDataItem item = new EntityScoreListDataItem();
		item.setScoreDef(definition.getNodeRef());
		entity.getEntityScoreList().add(item);

		return item;
	}

}
