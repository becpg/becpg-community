package fr.becpg.repo.regulatory.becpg.regulatory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.helper.RestTemplateHelper;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * HTTP client of the becpg-regulatory service: builds the recipe projection sent for a
 * check, and posts it with the configured authentication (oauth2 bearer or delegated
 * Alfresco ticket, see {@link BecpgRegulatoryAuthenticationService}).
 * <p>
 * Shared by the formulation ({@code /check}, persisted by {@link BecpgRegulatoryService})
 * and the embedded compliance view ({@code /check/view}, read only), so that both send
 * exactly the same projection.
 *
 * @author matthieu
 */
@Service
public class BecpgRegulatoryClient {

	private static final Log logger = LogFactory.getLog(BecpgRegulatoryClient.class);

	static final String PROP_SERVER_URL = "beCPG.regulatory.serverUrl";
	static final String API_PATH = "/api";
	static final String PROP_ENABLED = "beCPG.regulatory.enabled";
	static final String HEADER_BECPG_TICKET = "BECPG_TICKET";
	static final String CHECK_PATH = "/v1/regulatory/check";
	static final String CHECK_VIEW_PATH = "/v1/regulatory/check/view?refresh=";

	private final SystemConfigurationService systemConfigurationService;
	private final RemoteEntityService remoteEntityService;
	private final BecpgRegulatoryAuthenticationService authenticationService;

	/**
	 * @param systemConfigurationService the system configuration
	 * @param remoteEntityService the remote entity serializer
	 * @param authenticationService the regulatory authentication
	 */
	public BecpgRegulatoryClient(SystemConfigurationService systemConfigurationService, RemoteEntityService remoteEntityService,
			BecpgRegulatoryAuthenticationService authenticationService) {
		this.systemConfigurationService = systemConfigurationService;
		this.remoteEntityService = remoteEntityService;
		this.authenticationService = authenticationService;
	}

	/**
	 * @return the base URL of the regulatory API, or null when not configured
	 */
	public String serverUrl() {
		return systemConfigurationService.confValue(PROP_SERVER_URL);
	}

	/**
	 * The web application is served by the same origin as the API (the regulatory nginx routes
	 * {@code /api} to the API and everything else to the UI), so its URL is the server URL
	 * without the {@code /api} suffix. The view follows the regulatory service: it is enabled
	 * whenever the service is.
	 *
	 * @return the public URL of the regulatory web application serving the embedded view,
	 *         or empty when the regulatory service is disabled on this instance
	 */
	public Optional<String> uiUrl() {
		String serverUrl = serverUrl();
		if (!Boolean.parseBoolean(systemConfigurationService.confValue(PROP_ENABLED)) || serverUrl == null || serverUrl.isBlank()) {
			return Optional.empty();
		}
		String url = stripTrailingSlash(serverUrl.trim());
		return Optional.of(stripTrailingSlash(url.endsWith(API_PATH) ? url.substring(0, url.length() - API_PATH.length()) : url));
	}

	private static String stripTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	/**
	 * Reads the recipe projection of a product, with the rights of the current user.
	 *
	 * @param productNodeRef the product
	 * @return the projection, ready to be posted
	 * @throws JSONException if the serialized entity is not valid JSON
	 */
	public JSONObject fetchRecipe(NodeRef productNodeRef) throws JSONException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		remoteEntityService.getEntity(productNodeRef, out, recipeParams());
		return new JSONObject(out.toString(StandardCharsets.UTF_8));
	}

	/**
	 * Posts a recipe to the check consumed by the formulation.
	 *
	 * @param recipe the recipe projection
	 * @return the response body, possibly null
	 */
	public String check(JSONObject recipe) {
		return post(RestTemplateHelper.getRestTemplateLongTimeout(), serverUrl() + CHECK_PATH, recipe);
	}

	/**
	 * Posts a recipe to the check consumed by the embedded compliance view. Nothing is persisted.
	 *
	 * @param recipe the recipe projection
	 * @param refresh true to bypass the result cache of the regulatory service
	 * @return the compliance view, as JSON
	 */
	public String checkView(JSONObject recipe, boolean refresh) {
		return post(RestTemplateHelper.getRestTemplate(), serverUrl() + CHECK_VIEW_PATH + refresh, recipe);
	}

	private String post(RestTemplate restTemplate, String url, JSONObject recipe) {
		if (logger.isTraceEnabled()) {
			logger.trace("POST " + url + " body: " + recipe);
		}
		return restTemplate.postForObject(url, createRequest(recipe.toString()), String.class, new HashMap<>());
	}

	/**
	 * @param body the JSON body
	 * @return the request, with the authentication header of the configured mode
	 */
	HttpEntity<String> createRequest(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		authenticationService.getOauth2Token().ifPresent(headers::setBearerAuth);
		authenticationService.getBecpgTicket().ifPresent(ticket -> headers.set(HEADER_BECPG_TICKET, ticket));
		return new HttpEntity<>(body, headers);
	}

	/**
	 * @return the filtered projection sent to the regulatory service: recipe quantities,
	 *         ingredient identifiers, the jurisdictions / usages to check, and the ingredient
	 *         hierarchy: a line ticked as support is an impurity of the parent line it sits under
	 */
	static RemoteParams recipeParams() {
		RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
		params.setFilteredProperties(Set.of(ContentModel.PROP_SYS_NAME, PLMModel.PROP_INGLIST_QTY_PERC, PLMModel.ASSOC_INGLIST_ING,
				PLMModel.PROP_INGLIST_IS_SUPPORT, BeCPGModel.PROP_DEPTH_LEVEL, BeCPGModel.PROP_PARENT_LEVEL,
				PLMModel.ASSOC_REGULATORY_USAGE_REF, PLMModel.ASSOC_REGULATORY_COUNTRIES, PLMModel.PROP_REGULATORY_CODE));
		params.setFilteredAssocProperties(Map.of(
				PLMModel.ASSOC_INGLIST_ING,
				Set.of(PLMModel.PROP_CAS_NUMBER, PLMModel.PROP_CE_NUMBER, PLMModel.PROP_EC_NUMBER, PLMModel.PROP_FDA_NUMBER,
						PLMModel.PROP_FEMA_NUMBER, PLMModel.PROP_FL_NUMBER, PLMModel.PROP_ING_TYPE_V2, PLMModel.PROP_ING_TYPE_DEC_THRESHOLD,
						PLMModel.PROP_PLURAL_LEGAL_NAME),
				PLMModel.TYPE_ING_TYPE_ITEM, Set.of(PLMModel.PROP_REGULATORY_CODE),
				PLMModel.ASSOC_REGULATORY_USAGE_REF, Set.of(PLMModel.PROP_REGULATORY_CODE),
				PLMModel.ASSOC_REGULATORY_COUNTRIES, Set.of(PLMModel.PROP_REGULATORY_CODE, PLMModel.PROP_GEO_ORIGIN_ISOCODE)));
		return params;
	}
}
