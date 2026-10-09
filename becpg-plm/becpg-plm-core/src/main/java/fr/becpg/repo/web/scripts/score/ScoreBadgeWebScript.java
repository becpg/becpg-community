/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.web.scripts.score;

import java.io.IOException;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.score.ScoreBadgeService;

/**
 * Streams the badge image of a score class.
 *
 * <p>A missing badge answers a 404 on purpose: Share then falls back on the images
 * shipped with the web application, so a repository without custom badges keeps
 * rendering the standard scores.</p>
 *
 * @author matthieu
 */
public class ScoreBadgeWebScript extends StreamContent {

	/** Constant <code>PARAM_CODE="code"</code> */
	private static final String PARAM_CODE = "code";

	/** Constant <code>PARAM_SCORE_CLASS="scoreClass"</code> */
	private static final String PARAM_SCORE_CLASS = "scoreClass";

	/** Constant <code>NO_BADGE_FOUND="No badge found for score "</code> */
	private static final String NO_BADGE_FOUND = "No badge found for score ";

	private ScoreBadgeService scoreBadgeService;

	/**
	 * <p>Setter for the field <code>scoreBadgeService</code>.</p>
	 *
	 * @param scoreBadgeService a {@link fr.becpg.repo.score.ScoreBadgeService} object
	 */
	public void setScoreBadgeService(ScoreBadgeService scoreBadgeService) {
		this.scoreBadgeService = scoreBadgeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		String code = req.getServiceMatch().getTemplateVars().get(PARAM_CODE);
		String scoreClass = req.getServiceMatch().getTemplateVars().get(PARAM_SCORE_CLASS);

		Optional<NodeRef> badge = scoreBadgeService.findBadge(code, scoreClass);

		if (badge.isEmpty()) {
			throw new WebScriptException(Status.STATUS_NOT_FOUND, NO_BADGE_FOUND + code);
		}

		streamContent(req, res, badge.get(), ContentModel.PROP_CONTENT, false, null, null);
	}

}
