/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.project.formulation.ScoreRangeConverter;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.survey.data.SurveyableEntity;

/**
 * Produces a score out of the criteria already formulated by
 * {@link fr.becpg.repo.project.formulation.ScoreListFormulationHandler}.
 *
 * <p>This engine adds no computation of its own: the criteria carry their SpEL formulas and
 * their weights, and the score list has already been aggregated. It only groups the top
 * level criteria of a given score definition, converts the weighted result into a class and
 * publishes the breakdown. A criterion without score definition keeps feeding the legacy
 * single entity score, untouched.</p>
 *
 * @author matthieu
 */
@Service("criteriaScoreEngine")
public class CriteriaScoreEngine {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(CriteriaScoreEngine.class);

	/** Constant <code>MAX_SCORE=100d</code> */
	private static final double MAX_SCORE = 100d;

	private final NodeService nodeService;

	private final ScoreDefinitionService scoreDefinitionService;

	private final ScoreResultWriter scoreResultWriter;

	/**
	 * <p>Constructor for CriteriaScoreEngine.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 * @param scoreResultWriter a {@link fr.becpg.repo.score.ScoreResultWriter} object
	 */
	@Autowired
	public CriteriaScoreEngine(@Qualifier("nodeService") NodeService nodeService, ScoreDefinitionService scoreDefinitionService,
			ScoreResultWriter scoreResultWriter) {
		this.nodeService = nodeService;
		this.scoreDefinitionService = scoreDefinitionService;
		this.scoreResultWriter = scoreResultWriter;
	}

	/**
	 * <p>Formulates every criteria driven score applicable to the entity.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.survey.data.SurveyableEntity} object, also
	 *            expected to be a {@link fr.becpg.repo.score.ScoredEntity}
	 */
	public void formulateScores(SurveyableEntity entity) {
		if (!(entity instanceof ScoredEntity scoredEntity) || (entity.getScoreList() == null)) {
			return;
		}

		for (ScoreDefinitionItem definition : scoreDefinitionService.getEffectiveScoreDefinitions(new Date())) {
			if (ScoreEngine.Criteria.equals(definition.getScoreEngine())) {
				formulateScore(scoredEntity, entity.getScoreList(), definition);
			}
		}
	}

	/**
	 * <p>formulateScore.</p>
	 *
	 * @param scoredEntity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param scoreList a {@link java.util.List} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void formulateScore(ScoredEntity scoredEntity, List<ScoreListDataItem> scoreList, ScoreDefinitionItem definition) {
		List<ScoreListDataItem> roots = extractRoots(scoreList, definition.getNodeRef());

		if (roots.isEmpty()) {
			return;
		}

		ScoreContext context = buildContext(definition, roots);

		if (context.getValue() == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No weighted criterion for score " + definition.getCode());
			}
			return;
		}

		scoreResultWriter.write(scoredEntity, context);
	}

	/**
	 * Top level criteria of a score definition: only they carry the weights of the final
	 * aggregation, the children having already been rolled up into their parent.
	 *
	 * @param scoreList a {@link java.util.List} object
	 * @param scoreDefNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.List} object
	 */
	private List<ScoreListDataItem> extractRoots(List<ScoreListDataItem> scoreList, NodeRef scoreDefNodeRef) {
		List<ScoreListDataItem> roots = new ArrayList<>();
		for (ScoreListDataItem item : scoreList) {
			if ((item.getParent() == null) && Objects.equals(item.getScoreDefinition(), scoreDefNodeRef)) {
				roots.add(item);
			}
		}
		return roots;
	}

	/**
	 * <p>buildContext.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @param roots a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.score.ScoreContext} object
	 */
	private ScoreContext buildContext(ScoreDefinitionItem definition, List<ScoreListDataItem> roots) {
		ScoreContext context = new ScoreContext();

		context.setCode(definition.getCode());
		context.setVersion(definition.getVersion());
		context.setUnit(definition.getUnit());
		context.setScale(definition.getScoreScale().name());
		context.setRange(definition.getRange());

		double totalScore = 0d;
		double totalWeight = 0d;

		for (ScoreListDataItem root : roots) {
			if ((root.getWeight() == null) || (root.getScore() == null)) {
				continue;
			}
			totalScore += root.getWeight() * root.getScore();
			totalWeight += root.getWeight();
			context.getParts().add(toScorePart(root));
		}

		if (totalWeight > 0d) {
			context.setValue(Math.min(totalScore / totalWeight, MAX_SCORE));
			applyRange(context, definition.getRange());
		}

		return context;
	}

	/**
	 * <p>Converts the value into a class, when the definition declares a range.</p>
	 *
	 * @param context a {@link fr.becpg.repo.score.ScoreContext} object
	 * @param range the range declared by the score definition
	 */
	private void applyRange(ScoreContext context, String range) {
		if ((range == null) || range.isBlank()) {
			return;
		}
		context.setScoreClass(new ScoreRangeConverter(range).getScoreLetter(context.getValue()));
	}

	/**
	 * <p>toScorePart.</p>
	 *
	 * @param item a {@link fr.becpg.repo.project.data.projectList.ScoreListDataItem} object
	 * @return a {@link fr.becpg.repo.score.ScorePart} object
	 */
	private ScorePart toScorePart(ScoreListDataItem item) {
		String label = (String) nodeService.getProperty(item.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME);

		// the criterion is named by its code, a node reference would say nothing in a breakdown
		String code = (String) nodeService.getProperty(item.getCharactNodeRef(), ProjectModel.PROP_SCORE_CRITERION_TYPE);
		if ((code == null) || code.isBlank()) {
			code = (label != null) ? label : item.getCharactNodeRef().getId();
		}

		return new ScorePart(code).withLabel(label).withValue(item.getScore(), null)
				.withCoefficients(null, item.getWeight()).withContribution((item.getScore() * item.getWeight()) / MAX_SCORE);
	}

}
