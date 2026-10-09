package fr.becpg.repo.web.scripts.regulatory;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import fr.becpg.repo.regulatory.becpg.regulatory.RegulatoryComplianceViewService;

/**
 * Unit tests of the mapping of the compliance view failures to HTTP answers.
 */
public class RegulatoryViewWebScriptTest {

	private static final String NODE_REF = "workspace://SpacesStore/0e3b5e0e-6b4a-4c79-9d53-b2d2f7d6a1c4";

	private RegulatoryComplianceViewService service;
	private RegulatoryViewWebScript webScript;
	private WebScriptRequest request;
	private WebScriptResponse response;
	private StringWriter body;

	@Before
	public void setUp() throws Exception {
		service = mock(RegulatoryComplianceViewService.class);
		webScript = new RegulatoryViewWebScript();
		webScript.setRegulatoryComplianceViewService(service);
		request = mock(WebScriptRequest.class);
		response = mock(WebScriptResponse.class);
		body = new StringWriter();
		when(response.getWriter()).thenReturn(body);
		when(request.getParameter("nodeRef")).thenReturn(NODE_REF);
		when(service.uiUrl()).thenReturn(Optional.of("https://regulatory.becpg.fr"));
	}

	@Test
	public void viewIsWrittenAsIs() throws Exception {
		when(request.getParameter("refresh")).thenReturn("true");
		when(service.fetchView(new NodeRef(NODE_REF), true)).thenReturn("{\"markets\":[]}");

		webScript.execute(request, response);

		assertEquals("{\"markets\":[]}", body.toString());
	}

	@Test
	public void disabledViewAnswersNotConfiguredWithoutCallingTheService() throws Exception {
		when(service.uiUrl()).thenReturn(Optional.empty());

		webScript.execute(request, response);

		verify(service, never()).fetchView(any(), anyBoolean());
		verify(response).setStatus(Status.STATUS_SERVICE_UNAVAILABLE);
		assertEquals("{\"error\":\"" + RegulatoryViewWebScript.ERROR_NOT_CONFIGURED + "\"}", body.toString());
	}

	@Test
	public void rejectedProductAnswersInvalidProduct() throws Exception {
		when(service.fetchView(any(), anyBoolean())).thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

		webScript.execute(request, response);

		verify(response).setStatus(Status.STATUS_BAD_REQUEST);
		assertEquals("{\"error\":\"" + RegulatoryViewWebScript.ERROR_INVALID_PRODUCT + "\"}", body.toString());
	}

	@Test
	public void refusedAuthenticationAnswersUnavailable() throws Exception {
		when(service.fetchView(any(), anyBoolean())).thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

		webScript.execute(request, response);

		verify(response).setStatus(Status.STATUS_BAD_GATEWAY);
	}

	@Test
	public void unreachableServiceAnswersUnavailable() throws Exception {
		when(service.fetchView(any(), anyBoolean())).thenThrow(new ResourceAccessException("timeout"));

		webScript.execute(request, response);

		verify(response).setStatus(Status.STATUS_BAD_GATEWAY);
		assertEquals("{\"error\":\"" + RegulatoryViewWebScript.ERROR_UNAVAILABLE + "\"}", body.toString());
	}

	@Test(expected = WebScriptException.class)
	public void malformedNodeRefIsRejected() throws Exception {
		when(request.getParameter("nodeRef")).thenReturn("javascript:alert(1)");

		webScript.execute(request, response);
	}
}
