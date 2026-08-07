/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.project.formulation.ScoreRangeConverter;
import fr.becpg.repo.regulatory.RegulatoryEntity;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
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

	private final NodeService nodeService;

	private final NamespaceService namespaceService;

	/**
	 * <p>Constructor for ScoreResultWriter.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 */
	@Autowired
	public ScoreResultWriter(ScoreDefinitionService scoreDefinitionService, @Qualifier("nodeService") NodeService nodeService,
			NamespaceService namespaceService) {
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
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

		if (!isApplicable(entity, definition.get())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Score " + context.getCode() + " does not apply to the markets of the entity");
			}
			return;
		}

		context.computeShares();

		RegulatoryScoreListDataItem item = findOrCreateItem(entity, definition.get());

		item.keepPreviousValue();
		item.setValue(context.getValue());
		item.setScoreClass(scoreClass(context, definition.get()));
		item.setDetails(context.toJSON().toString());
		item.setVersion(context.getVersion());
		item.setComputedDate(new Date());

		// the published score carries the scope of its definition, so it can be read by market
		item.setCountries(new ArrayList<>(definition.get().getCountries()));
		item.setUsages(new ArrayList<>(definition.get().getUsages()));

		item.setCategory(category(entity, definition.get()));

		attachToParent(entity, item, definition.get());
	}

	/**
	 * An entity that carries no regulatory information is served by every score, so a
	 * repository not using the market filtering keeps every score it used to get.
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a boolean
	 */
	private boolean isApplicable(ScoredEntity entity, ScoreDefinitionItem definition) {
		if (entity instanceof RegulatoryEntity regulatoryEntity) {
			return scoreDefinitionService.isApplicable(definition, regulatoryEntity);
		}
		return true;
	}

	/**
	 * <p>findOrCreateItem.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link fr.becpg.repo.score.data.RegulatoryScoreListDataItem} object
	 */
	private RegulatoryScoreListDataItem findOrCreateItem(ScoredEntity entity, ScoreDefinitionItem definition) {
		if (entity.getRegulatoryScoreList() == null) {
			entity.setRegulatoryScoreList(new ArrayList<>());
		}

		for (RegulatoryScoreListDataItem item : entity.getRegulatoryScoreList()) {
			if (Objects.equals(item.getScoreDef(), definition.getNodeRef())) {
				return item;
			}
		}

		RegulatoryScoreListDataItem item = new RegulatoryScoreListDataItem();
		item.setScoreDef(definition.getNodeRef());
		entity.getRegulatoryScoreList().add(item);

		return item;
	}


	/**
	 * Files the score under the one it details, so a mark and its axes read as a tree rather
	 * than as unrelated rows.
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param item the score being published
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void attachToParent(ScoredEntity entity, RegulatoryScoreListDataItem item, ScoreDefinitionItem definition) {
		if (definition.getParent() == null) {
			item.setDepthLevel(0);
			item.setParentLevel(null);
			return;
		}

		for (RegulatoryScoreListDataItem candidate : entity.getRegulatoryScoreList()) {
			if (Objects.equals(candidate.getScoreDef(), definition.getParent())) {
				item.setParentLevel(candidate.getNodeRef());
				item.setDepthLevel((candidate.getDepthLevel() == null ? 0 : candidate.getDepthLevel()) + 1);
				return;
			}
		}

		// the parent has not been published yet, the score stands at the root until it is
		item.setDepthLevel(0);
		item.setParentLevel(null);
	}

	/**
	 * Category the score applies to. A definition names the product property holding it, so a
	 * new score needing a category is a row of reference data rather than a class.
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.String} object, null when the score needs no category
	 */
	private String category(ScoredEntity entity, ScoreDefinitionItem definition) {
		String property = definition.getCategoryProperty();

		if ((property == null) || property.isBlank() || !(entity instanceof RepositoryEntity repositoryEntity)
				|| (repositoryEntity.getNodeRef() == null)) {
			return null;
		}

		Serializable value = nodeService.getProperty(repositoryEntity.getNodeRef(), QName.createQName(property, namespaceService));

		return value != null ? value.toString() : null;
	}

	/**
	 * Class of the score. A plugin that only computes a value gets it converted by the range
	 * of its definition, so stating a verdict stays a matter of reference data.
	 *
	 * @param context the breakdown of the computed score
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.String} object
	 */
	private String scoreClass(ScoreContext context, ScoreDefinitionItem definition) {
		if (context.getScoreClass() != null) {
			return context.getScoreClass();
		}

		if ((context.getValue() == null) || (definition.getRange() == null) || definition.getRange().isBlank()) {
			return null;
		}

		return new ScoreRangeConverter(definition.getRange()).getScoreLetter(context.getValue());
	}
}
