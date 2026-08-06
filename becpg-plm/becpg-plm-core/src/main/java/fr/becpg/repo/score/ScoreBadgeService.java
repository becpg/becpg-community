/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.score.data.ScoreBadgeListDataItem;
import fr.becpg.repo.score.data.ScoreDefinitionItem;

/**
 * Resolves the badge image of a score class.
 *
 * <p>Badges are held by the score definition itself, as the content of its badge list, so
 * they are imported like any other reference data and a customer brands its own score
 * without redeploying Share. When no badge is found, Share falls back on the images
 * shipped with the web application.</p>
 *
 * @author matthieu
 */
@Service("scoreBadgeService")
public class ScoreBadgeService {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ScoreBadgeService.class);

	/** Constant <code>DEFAULT_CLASS="default"</code> */
	private static final String DEFAULT_CLASS = "default";

	private final ScoreDefinitionService scoreDefinitionService;

	/**
	 * <p>Constructor for ScoreBadgeService.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 */
	@Autowired
	public ScoreBadgeService(ScoreDefinitionService scoreDefinitionService) {
		this.scoreDefinitionService = scoreDefinitionService;
	}

	/**
	 * <p>Finds the badge of a score class, falling back on the default badge of the score.</p>
	 *
	 * @param code the score code, as held by {@code bcpg:scoreDefCode}
	 * @param scoreClass the class of the computed score, may be null for a scale without classes
	 * @return a {@link java.util.Optional} object
	 */
	public Optional<NodeRef> findBadge(String code, String scoreClass) {
		Optional<ScoreDefinitionItem> definition = scoreDefinitionService.findByCode(code, null);

		if (definition.isEmpty()) {
			return Optional.empty();
		}

		List<ScoreBadgeListDataItem> badges = definition.get().getBadgeList();

		if (badges == null) {
			return Optional.empty();
		}

		Optional<NodeRef> badge = findByClass(badges, scoreClass);

		return badge.isPresent() ? badge : findByClass(badges, DEFAULT_CLASS);
	}

	/**
	 * <p>findByClass.</p>
	 *
	 * @param badges the badge list of the definition
	 * @param scoreClass the class to look for
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<NodeRef> findByClass(List<ScoreBadgeListDataItem> badges, String scoreClass) {
		if ((scoreClass == null) || scoreClass.isBlank()) {
			return Optional.empty();
		}

		for (ScoreBadgeListDataItem badge : badges) {
			if (scoreClass.equalsIgnoreCase(badge.getScoreClass())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found score badge for class " + scoreClass);
				}
				return Optional.ofNullable(badge.getNodeRef());
			}
		}

		return Optional.empty();
	}

}
