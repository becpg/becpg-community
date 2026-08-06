/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.regulatory.RegulatoryEntity;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.score.data.ScoreDefCoeffListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Reads the score definitions of the repository and their coefficients.
 *
 * <p>Definitions rarely change, so the resolved node references are cached. The cache is
 * declared under {@code fr.becpg.repo.score.ScoreDefinitionService} in
 * {@code cache-context.xml}.</p>
 *
 * @author matthieu
 */
@Service("scoreDefinitionService")
public class ScoreDefinitionService {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ScoreDefinitionService.class);

	/** Constant <code>CACHE_KEY="fr.becpg.repo.score.ScoreDefinitionServ"{trunked}</code> */
	private static final String CACHE_KEY = ScoreDefinitionService.class.getName();

	/** Constant <code>DEFINITIONS_CACHE_ENTRY="definitions"</code> */
	private static final String DEFINITIONS_CACHE_ENTRY = "definitions";

	private final BeCPGCacheService beCPGCacheService;

	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private final NodeService nodeService;

	/**
	 * <p>Constructor for ScoreDefinitionService.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public ScoreDefinitionService(BeCPGCacheService beCPGCacheService, AlfrescoRepository<RepositoryEntity> alfrescoRepository,
			@Qualifier("nodeService") NodeService nodeService) {
		this.beCPGCacheService = beCPGCacheService;
		this.alfrescoRepository = alfrescoRepository;
		this.nodeService = nodeService;
	}

	/**
	 * <p>Returns every score definition of the repository.</p>
	 *
	 * @return a {@link java.util.List} object, never null
	 */
	public List<ScoreDefinitionItem> getScoreDefinitions() {
		List<ScoreDefinitionItem> definitions = new ArrayList<>();
		for (NodeRef nodeRef : getScoreDefinitionNodeRefs()) {
			if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DELETED)) {
				definitions.add((ScoreDefinitionItem) alfrescoRepository.findOne(nodeRef));
			}
		}
		return definitions;
	}

	/**
	 * <p>Returns the definitions applicable at the given date.</p>
	 *
	 * @param date the date to check, typically the formulation date
	 * @return a {@link java.util.List} object, never null
	 */
	public List<ScoreDefinitionItem> getEffectiveScoreDefinitions(Date date) {
		List<ScoreDefinitionItem> effectives = new ArrayList<>();
		for (ScoreDefinitionItem definition : getScoreDefinitions()) {
			if (definition.isEffective(date)) {
				effectives.add(definition);
			}
		}
		return effectives;
	}

	/**
	 * <p>Finds the definition matching a code and a version.</p>
	 *
	 * @param code the score code, as held by {@code bcpg:scoreDefCode}
	 * @param version the score version, null or blank to match the first definition of the code
	 * @return a {@link java.util.Optional} object
	 */
	public Optional<ScoreDefinitionItem> findByCode(String code, String version) {
		for (ScoreDefinitionItem definition : getScoreDefinitions()) {
			if (matches(definition, code, version)) {
				return Optional.of(definition);
			}
		}
		return Optional.empty();
	}

	/**
	 * <p>matches.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @param code a {@link java.lang.String} object
	 * @param version a {@link java.lang.String} object
	 * @return a boolean
	 */
	private boolean matches(ScoreDefinitionItem definition, String code, String version) {
		if (!Objects.equals(definition.getCode(), code)) {
			return false;
		}
		return (version == null) || version.isBlank() || Objects.equals(definition.getVersion(), version);
	}

	/**
	 * Checks whether a score applies to an entity, on the markets and the usages it targets.
	 *
	 * <p>Same convention as the regulatory lists: a definition declaring no country applies
	 * everywhere, and an entity declaring no market is served by every definition. The score
	 * is filtered out only when both sides are set and disjoint.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @param entity the entity being formulated
	 * @return a boolean
	 */
	public boolean isApplicable(ScoreDefinitionItem definition, RegulatoryEntity entity) {
		return matches(definition.getCountries(), entity.getRegulatoryCountriesRef())
				&& matches(definition.getUsages(), entity.getRegulatoryUsagesRef());
	}

	/**
	 * <p>matches.</p>
	 *
	 * @param definitionRefs the references declared by the score definition
	 * @param entityRefs the references declared by the entity
	 * @return a boolean
	 */
	private boolean matches(List<NodeRef> definitionRefs, List<NodeRef> entityRefs) {
		if ((definitionRefs == null) || definitionRefs.isEmpty() || (entityRefs == null) || entityRefs.isEmpty()) {
			return true;
		}

		for (NodeRef entityRef : entityRefs) {
			if (definitionRefs.contains(entityRef)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>Returns the normalization and weighting factors of a definition.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.util.List} object, never null
	 */
	public List<ScoreDefCoeffListDataItem> getCoefficients(ScoreDefinitionItem definition) {
		List<ScoreDefCoeffListDataItem> coefficients = definition.getCoeffList();
		return coefficients != null ? coefficients : Collections.emptyList();
	}

	/**
	 * <p>Finds the coefficients of a definition for a given LCA indicator.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @param lcaNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Optional} object
	 */
	public Optional<ScoreDefCoeffListDataItem> findCoefficient(ScoreDefinitionItem definition, NodeRef lcaNodeRef) {
		for (ScoreDefCoeffListDataItem coefficient : getCoefficients(definition)) {
			if (Objects.equals(coefficient.getLca(), lcaNodeRef)) {
				return Optional.of(coefficient);
			}
		}
		return Optional.empty();
	}

	/**
	 * <p>Clears the cached definitions, to be called when a definition is created or updated.</p>
	 */
	public void clearCache() {
		beCPGCacheService.clearCache(CACHE_KEY);
	}

	/**
	 * <p>getScoreDefinitionNodeRefs.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	private List<NodeRef> getScoreDefinitionNodeRefs() {
		return beCPGCacheService.getFromCache(CACHE_KEY, DEFINITIONS_CACHE_ENTRY, () -> {
			// the database query does not support excluding an aspect, the deleted ones are
			// filtered when the definitions are read
			List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_SCORE_DEFINITION).list();

			if (logger.isDebugEnabled()) {
				logger.debug("Found " + nodeRefs.size() + " score definitions");
			}

			return nodeRefs;
		});
	}

}
