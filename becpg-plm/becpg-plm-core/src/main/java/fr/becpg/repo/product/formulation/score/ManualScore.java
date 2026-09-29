/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.score;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoreDefinitionService;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ScoreResultWriter;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * <p>Publishes the breakdown of the scores entered by hand, EcoVadis or EcoBeautyScore being the
 * case: nothing computes them, so without it their line would carry a verdict but no breakdown,
 * and no marking could be drawn from it on screen or in a report.</p>
 *
 * <p>A computed score entered by hand whose engine publishes nothing, a Green Impact Index without
 * its criteria being the case, gets its breakdown the same way; once its engine computes it, the
 * breakdown is rewritten around the entry by {@link fr.becpg.repo.score.ScoreResultWriter}.</p>
 *
 * <p>Only the lines the user filled are published: a score that was never entered gets no line.</p>
 *
 * @author matthieu
 */
@Service("manualScore")
public class ManualScore implements ScoreCalculatingPlugin {

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Constructor for ManualScore.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	@Autowired
	public ManualScore(ScoreDefinitionService scoreDefinitionService, ScoreResultWriter scoreResultWriter) {
		this.scoreDefinitionService = scoreDefinitionService;
		this.scoreResultWriter = scoreResultWriter;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(ScorableEntity scorableEntity) {
		return (scorableEntity instanceof ScoredEntity scoredEntity) && (scoredEntity.getRegulatoryScoreList() != null)
				&& !scoredEntity.getRegulatoryScoreList().isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean formulateScore(ScorableEntity scorableEntity) {
		if (!(scorableEntity instanceof ScoredEntity scoredEntity)) {
			return true;
		}
		for (ScoreDefinitionItem definition : scoreDefinitionService.getEffectiveScoreDefinitions(new Date())) {
			enteredLine(scoredEntity, definition).ifPresent(line -> scoreResultWriter.write(scoredEntity, verdict(definition, line)));
		}
		return true;
	}

	private static Optional<RegulatoryScoreListDataItem> enteredLine(ScoredEntity scoredEntity, ScoreDefinitionItem definition) {
		for (RegulatoryScoreListDataItem line : scoredEntity.getRegulatoryScoreList()) {
			if ((definition.getNodeRef() != null) && definition.getNodeRef().equals(line.getScoreDef()) && isEntered(line)
					&& (ScoreEngine.Manual.equals(definition.getScoreEngine()) || lacksBreakdown(line))) {
				return Optional.of(line);
			}
		}
		return Optional.empty();
	}

	/** A computed score entered by hand that its engine did not publish. */
	private static boolean lacksBreakdown(RegulatoryScoreListDataItem line) {
		return Boolean.TRUE.equals(line.getIsManual()) && ((line.getDetails() == null) || line.getDetails().isBlank());
	}

	private static boolean isEntered(RegulatoryScoreListDataItem line) {
		return (line.getValue() != null) || ((line.getScoreClass() != null) && !line.getScoreClass().isBlank());
	}

	/**
	 * <p>The score as entered, in the format every marking is drawn from.</p>
	 *
	 * @param definition the definition of the score
	 * @param line the line the user filled
	 * @return the score context
	 */
	static ScoreContext verdict(ScoreDefinitionItem definition, RegulatoryScoreListDataItem line) {
		ScoreContext context = new ScoreContext();
		context.setCode(definition.getCode());
		context.setVersion(definition.getVersion());
		context.setUnit(definition.getUnit());
		context.setScale(definition.getScoreScale().name());
		context.setScoreClass(line.getScoreClass());
		context.setValue(line.getValue());
		return context;
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return ScoreEngine.Manual.name();
	}

	/**
	 * {@inheritDoc}
	 *
	 * The scores are published by the plugin itself, definition by definition.
	 */
	@Override
	public Optional<ScoreContext> getScoreContext(ScorableEntity scorableEntity) {
		return Optional.empty();
	}

}
