package fr.becpg.repo.regulatory.becpg.regulatory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Unit tests of the configuration, authentication headers and projection of the
 * becpg-regulatory client.
 */
public class BecpgRegulatoryClientTest {

	private SystemConfigurationService systemConfigurationService;
	private BecpgRegulatoryAuthenticationService authenticationService;
	private BecpgRegulatoryClient client;

	@Before
	public void setUp() {
		systemConfigurationService = mock(SystemConfigurationService.class);
		authenticationService = mock(BecpgRegulatoryAuthenticationService.class);
		when(authenticationService.getOauth2Token()).thenReturn(Optional.empty());
		when(authenticationService.getBecpgTicket()).thenReturn(Optional.empty());
		client = new BecpgRegulatoryClient(systemConfigurationService, mock(RemoteEntityService.class), authenticationService);
	}

	@Test
	public void uiUrlIsTheServerUrlWithoutItsApiPath() {
		enableRegulatory(" https://regulatory.becpg.fr/api/ ");

		assertEquals("https://regulatory.becpg.fr", client.uiUrl().orElseThrow());
	}

	@Test
	public void plainHttpServerUrlEnablesTheViewForLocalTests() {
		enableRegulatory("http://localhost:8080/api");

		assertEquals("http://localhost:8080", client.uiUrl().orElseThrow());
	}

	@Test
	public void disabledRegulatoryDisablesTheView() {
		when(systemConfigurationService.confValue(BecpgRegulatoryClient.PROP_SERVER_URL)).thenReturn("https://regulatory.becpg.fr/api");
		when(systemConfigurationService.confValue(BecpgRegulatoryClient.PROP_ENABLED)).thenReturn("false");

		assertFalse(client.uiUrl().isPresent());
	}

	@Test
	public void missingServerUrlDisablesTheView() {
		when(systemConfigurationService.confValue(BecpgRegulatoryClient.PROP_ENABLED)).thenReturn("true");

		assertFalse(client.uiUrl().isPresent());
	}

	private void enableRegulatory(String serverUrl) {
		when(systemConfigurationService.confValue(BecpgRegulatoryClient.PROP_SERVER_URL)).thenReturn(serverUrl);
		when(systemConfigurationService.confValue(BecpgRegulatoryClient.PROP_ENABLED)).thenReturn("true");
	}

	@Test
	public void oauth2ModeSendsABearerToken() {
		when(authenticationService.getOauth2Token()).thenReturn(Optional.of("token"));

		HttpHeaders headers = client.createRequest("{}").getHeaders();

		assertEquals("Bearer token", headers.getFirst(HttpHeaders.AUTHORIZATION));
		assertNull(headers.getFirst(BecpgRegulatoryClient.HEADER_BECPG_TICKET));
	}

	@Test
	public void ticketModeSendsTheDelegatedTicket() {
		when(authenticationService.getBecpgTicket()).thenReturn(Optional.of("ticket"));

		HttpEntity<String> request = client.createRequest("{}");

		assertEquals("ticket", request.getHeaders().getFirst(BecpgRegulatoryClient.HEADER_BECPG_TICKET));
		assertNull(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
		assertEquals("{}", request.getBody());
	}

	@Test
	public void projectionCarriesTheRecipeAndTheRegulatoryContext() {
		RemoteParams params = BecpgRegulatoryClient.recipeParams();

		assertTrue(params.getFilteredProperties().contains(PLMModel.PROP_INGLIST_QTY_PERC));
		assertTrue(params.getFilteredProperties().contains(PLMModel.ASSOC_REGULATORY_COUNTRIES));
		assertTrue(params.getFilteredAssocProperties().get(PLMModel.ASSOC_INGLIST_ING).contains(PLMModel.PROP_CAS_NUMBER));
		assertTrue(params.getFilteredAssocProperties().get(PLMModel.ASSOC_REGULATORY_COUNTRIES).contains(PLMModel.PROP_GEO_ORIGIN_ISOCODE));
	}

	@Test
	public void projectionTellsAnImpurityFromTheIngredientThatCarriesIt() {
		RemoteParams params = BecpgRegulatoryClient.recipeParams();

		assertTrue(params.getFilteredProperties().contains(PLMModel.PROP_INGLIST_FLAGS));
		assertTrue(params.getFilteredProperties().contains(BeCPGModel.PROP_DEPTH_LEVEL));
		assertTrue(params.getFilteredProperties().contains(BeCPGModel.PROP_PARENT_LEVEL));
	}
}
