/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.remote.ai;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.authentication.BeCPGTicketService;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Proxies the becpg-ai suggestion API, so that no client has to know where becpg-ai lives.
 *
 * <p>Same arrangement as {@code becpg/olap/chart}: the repository holds the address of the
 * companion service ({@code beCPG.ai.serverUrl}) and knows how to authenticate the current user
 * against it through {@link BeCPGTicketService#getCurrentAuthToken()}. A front end calling
 * becpg-ai directly would need the AI base URL, an AI credential and the format of that
 * credential — none of which it should hold, the format being owned by beCPG alone.</p>
 *
 * <p>There is deliberately no free-form path parameter: a proxy forwarding an arbitrary path is a
 * generic tunnel into becpg-ai. Three operations are exposed, one descriptor each:</p>
 *
 * <ul>
 *   <li>{@code GET  /becpg/ai/suggestions?nodeRef=…&amp;listId=…&amp;locale=…}</li>
 *   <li>{@code POST /becpg/ai/suggestions/apply}</li>
 *   <li>{@code POST /becpg/ai/suggestions/apply-plan}</li>
 * </ul>
 *
 * <p>The upstream status is forwarded as-is rather than collapsed, so that a caller can tell
 * "becpg-ai refused you" (401/403) from "becpg-ai is not configured" (503) from "becpg-ai broke"
 * (502). An unset {@code beCPG.ai.serverUrl} answers 503.</p>
 *
 * @author matthieu
 */
public class AiSuggestionsProxyWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(AiSuggestionsProxyWebScript.class);

	/** Configuration key holding the internal becpg-ai base URL. Empty = becpg-ai not deployed. */
	private static final String CONF_AI_SERVER_URL = "beCPG.ai.serverUrl";

	/** The becpg-ai API root, appended to the configured base URL. */
	private static final String AI_API_BASE = "/api/";

	/** The operation this instance of the bean serves; injected by the Spring context. */
	private String operation;

	private BeCPGTicketService beCPGTicketService;

	private SystemConfigurationService systemConfigurationService;

	/**
	 * <p>Setter for the field <code>operation</code>.</p>
	 *
	 * @param operation {@code suggestions}, {@code apply} or {@code applyPlan}
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * <p>Setter for the field <code>beCPGTicketService</code>.</p>
	 *
	 * @param beCPGTicketService a {@link fr.becpg.repo.authentication.BeCPGTicketService} object
	 */
	public void setBeCPGTicketService(BeCPGTicketService beCPGTicketService) {
		this.beCPGTicketService = beCPGTicketService;
	}

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String baseUrl = systemConfigurationService.confValue(CONF_AI_SERVER_URL);

		if ((baseUrl == null) || baseUrl.trim().isEmpty()) {
			// Not an error: an instance without becpg-ai says so, and the caller turns its AI
			// features off rather than showing a failure.
			res.setStatus(Status.STATUS_SERVICE_UNAVAILABLE);
			res.setContentType(MediaType.APPLICATION_JSON_VALUE);
			res.setContentEncoding("UTF-8");
			res.getWriter().write("{\"error\":\"aiNotConfigured\"}");
			return;
		}

		try {
			URI uri = buildUri(baseUrl.trim(), req);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			// The caller's own identity, in the format becpg-ai expects — built here, by the
			// service that owns that format, and never handed to a front end.
			headers.set("BECPG_TICKET", beCPGTicketService.getCurrentAuthToken());

			boolean isPost = !"suggestions".equals(operation);
			String body = isPost ? req.getContent().getContent() : null;

			if (logger.isDebugEnabled()) {
				logger.debug("Proxying " + operation + " to " + uri);
			}

			ResponseEntity<String> response = RestTemplateHelper.getRestTemplate().exchange(uri,
					isPost ? HttpMethod.POST : HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

			// A 200 is not proof of an answer: an unauthenticated becpg-ai serves its OAuth login
			// page with HTTP 200 and text/html. Relayed as-is, that HTML reaches the caller
			// labelled application/json and fails much later as a non-JSON payload. The content
			// type is the only thing telling the two apart.
			MediaType upstreamType = response.getHeaders().getContentType();
			if ((upstreamType != null) && !MediaType.APPLICATION_JSON.isCompatibleWith(upstreamType)) {
				logger.warn("becpg-ai answered " + upstreamType + " for " + operation
						+ " — most likely an unauthenticated call served its login page");
				write(res, Status.STATUS_BAD_GATEWAY,
						"{\"error\":\"aiNotJson\",\"contentType\":\"" + upstreamType + "\"}");
				return;
			}

			write(res, response.getStatusCode().value(), response.getBody());

		} catch (RestClientResponseException e) {
			// becpg-ai answered, and its answer is the useful one: forward it rather than turn
			// every upstream refusal into an opaque 500.
			logger.warn("becpg-ai returned " + e.getStatusCode().value() + " for " + operation);
			write(res, e.getStatusCode().value(), e.getResponseBodyAsString());
		} catch (URISyntaxException e) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Cannot reach becpg-ai for " + operation, e);
			write(res, Status.STATUS_BAD_GATEWAY, "{\"error\":\"aiUnreachable\"}");
		}
	}

	/**
	 * Builds the upstream URI of the operation this bean serves.
	 *
	 * <p>The path is chosen from {@link #operation}, never from the request: that is what keeps
	 * the proxy closed. Only the parameters a given operation declares are forwarded.</p>
	 *
	 * @param baseUrl the configured becpg-ai base URL
	 * @param req the incoming request
	 * @return the upstream URI
	 * @throws URISyntaxException when the configured URL is not a URI
	 */
	private URI buildUri(String baseUrl, WebScriptRequest req) throws URISyntaxException {

		if ("apply".equals(operation)) {
			return new URI(baseUrl + AI_API_BASE + "suggestions/apply");
		}
		if ("applyPlan".equals(operation)) {
			return new URI(baseUrl + AI_API_BASE + "suggestions/apply-plan");
		}

		String nodeRef = req.getParameter("nodeRef");
		if ((nodeRef == null) || nodeRef.isEmpty()) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "nodeRef is required");
		}
		// Parsed, not concatenated: a value that is not a nodeRef must not reach the upstream
		// path, and `new NodeRef(...)` is the repository's own validation of that.
		NodeRef entityNodeRef = new NodeRef(nodeRef);

		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(baseUrl + AI_API_BASE + "suggestions/" + entityNodeRef.getId());

		String listId = req.getParameter("listId");
		if ((listId != null) && !listId.isEmpty()) {
			builder.queryParam("listId", listId);
		}
		String locale = req.getParameter("locale");
		if ((locale != null) && !locale.isEmpty()) {
			builder.queryParam("locale", locale);
		}

		return builder.build().encode().toUri();
	}

	/**
	 * Writes an upstream answer back to the caller, status included.
	 *
	 * @param res the response
	 * @param status the HTTP status to forward
	 * @param body the body, may be null
	 * @throws IOException if any
	 */
	private void write(WebScriptResponse res, int status, String body) throws IOException {
		res.setStatus(status);
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setContentEncoding("UTF-8");
		res.getWriter().write(body != null ? body : "");
	}

}
