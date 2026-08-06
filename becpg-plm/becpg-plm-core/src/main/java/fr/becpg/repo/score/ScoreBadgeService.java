/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.Optional;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Resolves the badge image of a score class.
 *
 * <p>Badges live in {@code /System/ScoreBadges/&lt;code&gt;/badge-&lt;class&gt;.svg}, so a customer
 * brands its own score by dropping files in the repository, without redeploying Share.
 * When no badge is found, Share falls back on the images shipped with the web
 * application.</p>
 *
 * @author matthieu
 */
@Service("scoreBadgeService")
public class ScoreBadgeService {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ScoreBadgeService.class);

	/** Constant <code>BADGES_FOLDER="/app:company_home/cm:System/cm:ScoreBadges"</code> */
	private static final String BADGES_FOLDER = "/app:company_home/cm:System/cm:ScoreBadges";

	/** Constant <code>BADGE_PREFIX="badge-"</code> */
	private static final String BADGE_PREFIX = "badge-";

	/** Constant <code>DEFAULT_BADGE="badge-default"</code> */
	private static final String DEFAULT_BADGE = "badge-default";

	private final NodeRef companyHome;

	private final FileFolderService fileFolderService;

	/**
	 * <p>Constructor for ScoreBadgeService.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object
	 * @param repositoryHelper a {@link org.alfresco.repo.model.Repository} object
	 */
	@Autowired
	public ScoreBadgeService(@Qualifier("fileFolderService") FileFolderService fileFolderService,
			@Qualifier("repositoryHelper") Repository repositoryHelper) {
		this.fileFolderService = fileFolderService;
		this.companyHome = repositoryHelper.getCompanyHome();
	}

	/**
	 * <p>Finds the badge of a score class, falling back on the default badge of the score.</p>
	 *
	 * @param code the score code, as held by {@code bcpg:scoreDefCode}
	 * @param scoreClass the class of the computed score, may be null for a scale without classes
	 * @return a {@link java.util.Optional} object
	 */
	public Optional<NodeRef> findBadge(String code, String scoreClass) {
		NodeRef scoreFolder = findScoreFolder(code);
		if (scoreFolder == null) {
			return Optional.empty();
		}

		Optional<NodeRef> badge = findFileStartingWith(scoreFolder, badgeName(scoreClass));
		return badge.isPresent() ? badge : findFileStartingWith(scoreFolder, DEFAULT_BADGE);
	}

	/**
	 * <p>badgeName.</p>
	 *
	 * @param scoreClass a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private String badgeName(String scoreClass) {
		if ((scoreClass == null) || scoreClass.isBlank()) {
			return DEFAULT_BADGE;
		}
		return BADGE_PREFIX + scoreClass.trim().toLowerCase();
	}

	/**
	 * <p>findScoreFolder.</p>
	 *
	 * @param code a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef findScoreFolder(String code) {
		if ((code == null) || code.isBlank()) {
			return null;
		}
		return BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(companyHome, BADGES_FOLDER + "/cm:" + code);
	}

	/**
	 * Matches on the file name without its extension, so the customer chooses between SVG
	 * and PNG without the code knowing.
	 *
	 * @param folderNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param baseName the expected file name, extension excluded
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<NodeRef> findFileStartingWith(NodeRef folderNodeRef, String baseName) {
		for (FileInfo file : fileFolderService.listFiles(folderNodeRef)) {
			if (removeExtension(file.getName()).equalsIgnoreCase(baseName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found score badge " + file.getName());
				}
				return Optional.of(file.getNodeRef());
			}
		}
		return Optional.empty();
	}

	/**
	 * <p>removeExtension.</p>
	 *
	 * @param fileName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private String removeExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
	}

}
