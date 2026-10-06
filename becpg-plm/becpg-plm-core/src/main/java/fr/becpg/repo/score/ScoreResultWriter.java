/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.project.formulation.ScoreRangeConverter;
import fr.becpg.repo.regulatory.RegulatoryEntity;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Publishes a computed score into the score list of an entity.
 *
 * <p>Like the other characteristic lists, the score list follows the entity template: a
 * score is published only when the entity, or its template, lists it. A line deleted from
 * a product therefore stays deleted unless the template carries it, and the markets of the
 * entity filter the lines the template brings. Writing is a no-op when no score definition
 * matches the computed score, so the framework stays dormant until definitions are
 * created: the historical properties such as {@code bcpg:nutrientProfilingScore} keep
 * being written whatever the score list holds.</p>
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
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
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

		synchronizeTemplate(entity);

		Optional<RegulatoryScoreListDataItem> listedItem = findItem(entity, definition.get().getNodeRef());

		if (listedItem.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Score " + context.getCode() + " " + context.getVersion() + " is listed neither on the entity nor on its template");
			}
			return;
		}

		context.computeShares();

		RegulatoryScoreListDataItem item = listedItem.get();

		// a score entered by hand is an audited figure, the formulation must not overwrite it
		if (Boolean.TRUE.equals(item.getIsManual())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Score " + context.getCode() + " is entered by hand, keeping the value of the entity");
			}
			writeManualDetails(item, context, definition.get());
			return;
		}

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
	 * Rewrites the breakdown of a score entered by hand around the verdict entered, the value
	 * and class of the item being left untouched.
	 *
	 * <p>The marking and its tooltip are drawn from the breakdown: left as it was, it would
	 * show the grade computed before the entry rather than the grade entered. The computed
	 * verdict stays beside it, so the gap between the two remains readable.</p>
	 *
	 * @param item the score entered by hand
	 * @param context the breakdown of the computed score
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void writeManualDetails(RegulatoryScoreListDataItem item, ScoreContext context, ScoreDefinitionItem definition) {
		context.setScoreClass(scoreClass(context, definition));

		String manualClass = item.getScoreClass();
		if ((manualClass == null) || manualClass.isBlank()) {
			manualClass = classOf(item.getValue(), definition);
		}

		context.overrideVerdict(item.getValue(), manualClass);
		item.setDetails(context.toJSON().toString());
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
	 * Copies onto the entity the score lines of its template it does not list yet.
	 *
	 * <p>A line is copied only when its definition is still effective and applies to the
	 * markets of the entity, so one template can serve several markets. Running it again
	 * adds nothing: the lines already listed are left as they are.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 */
	public void synchronizeTemplate(ScoredEntity entity) {
		List<RegulatoryScoreListDataItem> templateItems = templateItems(entity);

		if (templateItems.isEmpty()) {
			return;
		}

		if (entity.getRegulatoryScoreList() == null) {
			entity.setRegulatoryScoreList(new ArrayList<>());
		}

		for (RegulatoryScoreListDataItem templateItem : templateItems) {
			NodeRef scoreDef = templateItem.getScoreDef();
			if ((scoreDef != null) && findItem(entity, scoreDef).isEmpty() && isApplicable(entity, scoreDef)) {
				entity.getRegulatoryScoreList().add(copyOf(templateItem));
			}
		}
	}

	/**
	 * Versions of a score the entity lists, for a plugin able to compute several versions of
	 * its code.
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param code the score code, as held by {@code bcpg:scoreDefCode}
	 * @return a {@link java.util.List} object, never null
	 */
	public List<String> listedVersions(ScoredEntity entity, String code) {
		List<String> versions = new ArrayList<>();

		if (entity.getRegulatoryScoreList() != null) {
			for (RegulatoryScoreListDataItem item : entity.getRegulatoryScoreList()) {
				Optional<ScoreDefinitionItem> definition = scoreDefinitionService.findByNodeRef(item.getScoreDef());
				if (definition.isPresent() && Objects.equals(code, definition.get().getCode()) && (definition.get().getVersion() != null)) {
					versions.add(definition.get().getVersion());
				}
			}
		}

		return versions;
	}

	/**
	 * Score lines of the template of an entity. A template formulated on its own has no
	 * template to follow.
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @return a {@link java.util.List} object, never null
	 */
	private static List<RegulatoryScoreListDataItem> templateItems(ScoredEntity entity) {
		if ((entity instanceof ProductData product) && (product.getEntityTpl() != null) && !product.getEntityTpl().equals(product)
				&& (product.getEntityTpl().getRegulatoryScoreList() != null)) {
			return product.getEntityTpl().getRegulatoryScoreList();
		}
		return List.of();
	}

	/**
	 * A copied line carries what the template sets: the score, and for a score entered by
	 * hand, its default entry. The computed fields are left to the formulation.
	 *
	 * @param templateItem a {@link fr.becpg.repo.score.data.RegulatoryScoreListDataItem} object
	 * @return a {@link fr.becpg.repo.score.data.RegulatoryScoreListDataItem} object
	 */
	private static RegulatoryScoreListDataItem copyOf(RegulatoryScoreListDataItem templateItem) {
		RegulatoryScoreListDataItem item = new RegulatoryScoreListDataItem();
		item.setScoreDef(templateItem.getScoreDef());

		if (Boolean.TRUE.equals(templateItem.getIsManual())) {
			item.setIsManual(true);
			item.setValue(templateItem.getValue());
			item.setScoreClass(templateItem.getScoreClass());
		}

		return item;
	}

	/**
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param scoreDef the node reference of a score definition
	 * @return whether the definition is effective and applies to the markets of the entity
	 */
	private boolean isApplicable(ScoredEntity entity, NodeRef scoreDef) {
		Optional<ScoreDefinitionItem> definition = scoreDefinitionService.findByNodeRef(scoreDef);
		return definition.isPresent() && isApplicable(entity, definition.get());
	}

	/**
	 * <p>findItem.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.score.ScoredEntity} object
	 * @param scoreDef the node reference of a score definition
	 * @return the line of the entity holding this score, if listed
	 */
	private static Optional<RegulatoryScoreListDataItem> findItem(ScoredEntity entity, NodeRef scoreDef) {
		if (entity.getRegulatoryScoreList() != null) {
			for (RegulatoryScoreListDataItem item : entity.getRegulatoryScoreList()) {
				if (Objects.equals(item.getScoreDef(), scoreDef)) {
					return Optional.of(item);
				}
			}
		}
		return Optional.empty();
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
			// a parent still held in memory carries no reference to point at: the score stays
			// at the root and joins its parent on the next formulation
			if (Objects.equals(candidate.getScoreDef(), definition.getParent()) && (candidate.getNodeRef() != null)) {
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

		return classOf(context.getValue(), definition);
	}

	/**
	 * <p>Class of a value, read from the range of the definition.</p>
	 *
	 * @param value the value of the score
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.lang.String} object, null when the value or the range is missing
	 */
	private String classOf(Double value, ScoreDefinitionItem definition) {
		if ((value == null) || (definition.getRange() == null) || definition.getRange().isBlank()) {
			return null;
		}

		return new ScoreRangeConverter(definition.getRange()).getScoreLetter(value);
	}
}
