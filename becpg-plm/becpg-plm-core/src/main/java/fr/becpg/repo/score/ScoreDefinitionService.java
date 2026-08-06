/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
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
			if (!Boolean.TRUE.equals(nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_DELETED))) {
				ScoreDefinitionItem definition = (ScoreDefinitionItem) alfrescoRepository.findOne(nodeRef);
				attachSubLists(definition);
				definitions.add(definition);
			}
		}
		return definitions;
	}

	/**
	 * <p>Reads the thresholds, the badges and the coefficients of a definition.</p>
	 *
	 * <p>A charact cannot own nested lists, so they live in their own system datalists and
	 * point back to their definition.</p>
	 *
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 */
	private void attachSubLists(ScoreDefinitionItem definition) {
		definition.setThresholdList(readSubList(PLMModel.TYPE_SCORE_THRESHOLD_LIST, PLMModel.ASSOC_STL_SCORE_DEF, definition));
		definition.setBadgeList(readSubList(PLMModel.TYPE_SCORE_BADGE_LIST, PLMModel.ASSOC_SBL_SCORE_DEF, definition));
		definition.setCoeffList(readSubList(PLMModel.TYPE_SCORE_DEF_COEFF_LIST, PLMModel.ASSOC_SDCL_SCORE_DEF, definition));
	}

	/**
	 * <p>Reads the lines of one system datalist that point to a definition.</p>
	 *
	 * @param type the type of the datalist items
	 * @param assocQName the association carrying the definition
	 * @param definition a {@link fr.becpg.repo.score.data.ScoreDefinitionItem} object
	 * @return a {@link java.util.List} object, never null
	 */
	@SuppressWarnings("unchecked")
	private <T extends RepositoryEntity> List<T> readSubList(QName type, QName assocQName, ScoreDefinitionItem definition) {
		List<T> items = new ArrayList<>();
		for (NodeRef nodeRef : getSubListNodeRefs(type, assocQName).getOrDefault(definition.getNodeRef(), Collections.emptyList())) {
			items.add((T) alfrescoRepository.findOne(nodeRef));
		}
		return items;
	}

	/**
	 * <p>Indexes the lines of one system datalist by the definition they belong to.</p>
	 *
	 * @param type the type of the datalist items
	 * @param assocQName the association carrying the definition
	 * @return a {@link java.util.Map} object, never null
	 */
	private Map<NodeRef, List<NodeRef>> getSubListNodeRefs(QName type, QName assocQName) {
		return beCPGCacheService.getFromCache(CACHE_KEY, type.getLocalName(), () -> {
			Map<NodeRef, List<NodeRef>> byDefinition = new HashMap<>();

			for (NodeRef nodeRef : BeCPGQueryBuilder.createQuery().inDB().ofType(type).list()) {
				if (Boolean.TRUE.equals(nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_DELETED))) {
					continue;
				}

				for (AssociationRef assocRef : nodeService.getTargetAssocs(nodeRef, assocQName)) {
					byDefinition.computeIfAbsent(assocRef.getTargetRef(), key -> new ArrayList<>()).add(nodeRef);
				}
			}

			return byDefinition;
		});
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
			// the deleted ones are filtered when the definitions are read: bcpg:isDeletedAspect
			// is mandatory on the characts, only its flag tells a definition apart
			List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_SCORE_DEFINITION).list();

			if (logger.isDebugEnabled()) {
				logger.debug("Found " + nodeRefs.size() + " score definitions");
			}

			return nodeRefs;
		});
	}

}
