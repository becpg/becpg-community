/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.web.scripts.score;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.marking.RenderedScoreMarking;
import fr.becpg.repo.score.marking.ScoreMarkingRenderer;

/**
 * <p>Streams the regulatory marking of a line of the score list, rendered on the server as SVG.</p>
 *
 * <p>Share draws a score in the browser; for a scale a marking template covers, it asks this
 * webscript instead, so that the screen shows exactly what the report prints. A 404 tells Share
 * to fall back on its own drawing.</p>
 *
 * @author matthieu
 */
public class ScoreMarkingWebScript extends AbstractWebScript {

	private static final String PARAM_STORE_TYPE = "store_type";

	private static final String PARAM_STORE_ID = "store_id";

	private static final String PARAM_ID = "id";

	private static final String SVG_CONTENT_TYPE = "image/svg+xml";

	private static final String HEADER_CONTENT_LENGTH = "Content-Length";

	private static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";

	private static final String HEADER_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

	/**
	 * A marking template is editable content. Shown as an image it can run nothing, but opened
	 * on its own an SVG is a document: the policy keeps it from running a script or loading
	 * anything, inline styles aside.
	 */
	private static final String SVG_CONTENT_SECURITY_POLICY = "default-src 'none'; style-src 'unsafe-inline'; sandbox";

	private static final String NO_SNIFF = "nosniff";

	private static final String NOT_A_SCORE_LINE = "Not a score line: ";

	private static final String ACCESS_DENIED = "You are not allowed to read this score line";

	private static final String NO_MARKING = "No marking for score line ";

	private NodeService nodeService;

	private PermissionService permissionService;

	private ScoreMarkingRenderer scoreMarkingRenderer;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * <p>Setter for the field <code>scoreMarkingRenderer</code>.</p>
	 *
	 * @param scoreMarkingRenderer a {@link fr.becpg.repo.score.marking.ScoreMarkingRenderer} object
	 */
	public void setScoreMarkingRenderer(ScoreMarkingRenderer scoreMarkingRenderer) {
		this.scoreMarkingRenderer = scoreMarkingRenderer;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef scoreLineNodeRef = readableScoreLine(req.getServiceMatch().getTemplateVars());

		// the whole line, not its breakdown alone: a score entered by hand has no breakdown
		RepositoryEntity scoreLine = alfrescoRepository.findOne(scoreLineNodeRef);
		Optional<RenderedScoreMarking> marking = scoreLine instanceof RegulatoryScoreListDataItem line
				? scoreMarkingRenderer.render(line, I18NUtil.getLocale())
				: Optional.empty();

		if (marking.isEmpty()) {
			throw new WebScriptException(Status.STATUS_NOT_FOUND, NO_MARKING + scoreLineNodeRef);
		}

		writeSvg(marking.get().svg(), res);
	}

	/**
	 * The node is checked before anything is read from it: it must be a line of a score list the
	 * current user may read, so that the webscript never renders the detail of another node.
	 */
	private NodeRef readableScoreLine(Map<String, String> templateVars) {
		NodeRef nodeRef = new NodeRef(templateVars.get(PARAM_STORE_TYPE), templateVars.get(PARAM_STORE_ID), templateVars.get(PARAM_ID));

		if (!nodeService.exists(nodeRef)) {
			throw new WebScriptException(Status.STATUS_NOT_FOUND, NOT_A_SCORE_LINE + nodeRef);
		}
		if (!AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(nodeRef))) {
			throw new WebScriptException(Status.STATUS_FORBIDDEN, ACCESS_DENIED);
		}
		if (!PLMModel.TYPE_ENTITY_SCORE_LIST.equals(nodeService.getType(nodeRef))) {
			throw new WebScriptException(Status.STATUS_NOT_FOUND, NOT_A_SCORE_LINE + nodeRef);
		}
		return nodeRef;
	}

	private void writeSvg(String svg, WebScriptResponse res) throws IOException {
		byte[] bytes = svg.getBytes(StandardCharsets.UTF_8);
		res.setContentType(SVG_CONTENT_TYPE);
		res.setContentEncoding(StandardCharsets.UTF_8.name());
		res.setHeader(HEADER_CONTENT_SECURITY_POLICY, SVG_CONTENT_SECURITY_POLICY);
		res.setHeader(HEADER_CONTENT_TYPE_OPTIONS, NO_SNIFF);
		res.setHeader(HEADER_CONTENT_LENGTH, String.valueOf(bytes.length));
		res.getOutputStream().write(bytes);
	}

}
