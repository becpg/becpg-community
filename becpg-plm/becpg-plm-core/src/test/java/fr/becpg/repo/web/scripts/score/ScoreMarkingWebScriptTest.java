/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.web.scripts.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;
import fr.becpg.repo.score.marking.FrontOfPackMarkingService;
import fr.becpg.repo.score.marking.RenderedScoreMarking;
import fr.becpg.repo.score.marking.ScoreMarkingRenderer;

/**
 * Unit tests of {@link ScoreMarkingWebScript}: what it streams, and what it refuses to read.
 *
 * @author matthieu
 */
public class ScoreMarkingWebScriptTest {

	private static final NodeRef SCORE_LINE = new NodeRef("workspace://SpacesStore/score-line");

	private static final String DETAILS = "{\"code\":\"MTL\",\"scale\":\"Traffic\"}";

	private static final String SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>";

	private static final String PORTION_DETAILS = "{\"code\":\"MTL_PORTION\",\"scale\":\"Traffic\"}";

	private static final String FRONT_OF_PACK_SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" id=\"fop\"/>";

	private final FrontOfPackMarkingService frontOfPackMarkingService = mock(FrontOfPackMarkingService.class);

	private final EntityListDAO entityListDAO = mock(EntityListDAO.class);

	private NodeService nodeService;

	private PermissionService permissionService;

	private ScoreMarkingRenderer renderer;

	@SuppressWarnings("unchecked")
	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository = mock(AlfrescoRepository.class);

	private ScoreMarkingWebScript webScript;

	private WebScriptRequest request;

	private WebScriptResponse response;

	private ByteArrayOutputStream body;

	@Before
	public void setUp() throws IOException {
		nodeService = mock(NodeService.class);
		permissionService = mock(PermissionService.class);
		renderer = mock(ScoreMarkingRenderer.class);

		webScript = new ScoreMarkingWebScript();
		webScript.setNodeService(nodeService);
		webScript.setPermissionService(permissionService);
		webScript.setScoreMarkingRenderer(renderer);
		webScript.setAlfrescoRepository(alfrescoRepository);
		webScript.setFrontOfPackMarkingService(frontOfPackMarkingService);
		webScript.setEntityListDAO(entityListDAO);

		request = requestFor(SCORE_LINE);
		body = new ByteArrayOutputStream();
		response = mock(WebScriptResponse.class);
		when(response.getOutputStream()).thenReturn(body);

		when(nodeService.exists(SCORE_LINE)).thenReturn(true);
		when(nodeService.getType(SCORE_LINE)).thenReturn(PLMModel.TYPE_ENTITY_SCORE_LIST);
		when(permissionService.hasReadPermission(SCORE_LINE)).thenReturn(AccessStatus.ALLOWED);
		RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
		line.setDetails(DETAILS);
		when(alfrescoRepository.findOne(SCORE_LINE)).thenReturn(line);
		when(renderer.render(any(RegulatoryScoreListDataItem.class), any())).thenReturn(Optional.of(new RenderedScoreMarking("MTL", "High", SVG)));
	}

	private static WebScriptRequest requestFor(NodeRef nodeRef) {
		Match match = mock(Match.class);
		when(match.getTemplateVars()).thenReturn(Map.of("store_type", nodeRef.getStoreRef().getProtocol(), "store_id",
				nodeRef.getStoreRef().getIdentifier(), "id", nodeRef.getId()));
		WebScriptRequest request = mock(WebScriptRequest.class);
		when(request.getServiceMatch()).thenReturn(match);
		return request;
	}

	private int statusOf(WebScriptRequest failingRequest) throws IOException {
		try {
			webScript.execute(failingRequest, response);
			fail("The webscript must refuse this request");
			return 0;
		} catch (WebScriptException e) {
			return e.getStatus();
		}
	}

	@Test
	public void testMarkingIsStreamedAsSvg() throws IOException {
		webScript.execute(request, response);

		verify(response).setContentType("image/svg+xml");
		assertEquals(SVG, body.toString(StandardCharsets.UTF_8));
	}

	@Test
	public void testMarkingOpenedOnItsOwnCannotRunAScript() throws IOException {
		webScript.execute(request, response);

		verify(response).setHeader("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox");
		verify(response).setHeader("X-Content-Type-Options", "nosniff");
	}

	@Test
	public void testUnreadableLineIsForbiddenAndNothingIsRead() throws IOException {
		when(permissionService.hasReadPermission(SCORE_LINE)).thenReturn(AccessStatus.DENIED);

		assertEquals(Status.STATUS_FORBIDDEN, statusOf(request));
		verify(alfrescoRepository, never()).findOne(SCORE_LINE);
	}

	@Test
	public void testNodeOtherThanAScoreLineIsNotRendered() throws IOException {
		when(nodeService.getType(SCORE_LINE)).thenReturn(ContentModel.TYPE_CONTENT);

		assertEquals(Status.STATUS_NOT_FOUND, statusOf(request));
		verify(alfrescoRepository, never()).findOne(SCORE_LINE);
	}

	@Test
	public void testMissingNodeIsNotFound() throws IOException {
		when(nodeService.exists(SCORE_LINE)).thenReturn(false);

		assertEquals(Status.STATUS_NOT_FOUND, statusOf(request));
	}

	@Test
	public void testTrafficLightsPerPortionShowTheFrontOfPackMarking() throws IOException {
		NodeRef productNodeRef = new NodeRef("workspace://SpacesStore/product");
		RegulatoryScoreListDataItem line = new RegulatoryScoreListDataItem();
		line.setDetails(PORTION_DETAILS);
		when(alfrescoRepository.findOne(SCORE_LINE)).thenReturn(line);
		when(entityListDAO.getEntity(SCORE_LINE)).thenReturn(productNodeRef);
		when(alfrescoRepository.findOne(productNodeRef)).thenReturn(new FinishedProductData());
		when(frontOfPackMarkingService.render(any(), any())).thenReturn(Optional.of(new RenderedScoreMarking("MTL_FOP", "High", FRONT_OF_PACK_SVG)));

		webScript.execute(request, response);

		assertEquals(FRONT_OF_PACK_SVG, body.toString(StandardCharsets.UTF_8));
	}

	@Test
	public void testScoreWithoutMarkingIsNotFoundSoThatShareFallsBack() throws IOException {
		when(renderer.render(any(RegulatoryScoreListDataItem.class), any())).thenReturn(Optional.empty());

		assertEquals(Status.STATUS_NOT_FOUND, statusOf(request));
	}

}
