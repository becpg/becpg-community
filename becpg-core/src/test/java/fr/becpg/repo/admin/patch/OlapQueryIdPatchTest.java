package fr.becpg.repo.admin.patch;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Checks that the OLAP dashlet preferences are rewritten for real users only: a person node left
 * for the system user must not reach the preference service, which refuses it and makes the patch
 * fail at startup (#36439).
 *
 * @author matthieu
 */
public class OlapQueryIdPatchTest {

	private static final String PREFERENCE_KEY = OlapQueryIdPatch.PREFERENCE_ROOT + ".component-1-1.query";

	private static final String OLD_IDENTITY = "Produits par état";

	private static final String OLAP_QUERY_ID = "productsByState";

	private static final String USER_NAME = "alice";

	private static final NodeRef SYSTEM_PERSON = new NodeRef("workspace://SpacesStore/system");

	private static final NodeRef USER_PERSON = new NodeRef("workspace://SpacesStore/alice");

	private static final NodeRef NAMELESS_PERSON = new NodeRef("workspace://SpacesStore/nameless");

	@Mock
	private PersonService personService;

	@Mock
	private NodeService nodeService;

	@Mock
	private PreferenceService preferenceService;

	private final OlapQueryIdPatch patch = new OlapQueryIdPatch();

	private AutoCloseable mocks;

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		patch.setPersonService(personService);
		patch.setNodeService(nodeService);
		patch.setPreferenceService(preferenceService);

		when(personService.getAllPeople()).thenReturn(Set.of(SYSTEM_PERSON, USER_PERSON, NAMELESS_PERSON));
		when(nodeService.getProperty(SYSTEM_PERSON, ContentModel.PROP_USERNAME)).thenReturn(AuthenticationUtil.getSystemUserName());
		when(nodeService.getProperty(USER_PERSON, ContentModel.PROP_USERNAME)).thenReturn(USER_NAME);
		when(personService.personExists(AuthenticationUtil.getSystemUserName())).thenReturn(false);
		when(personService.personExists(USER_NAME)).thenReturn(true);
		when(preferenceService.getPreferences(USER_NAME, OlapQueryIdPatch.PREFERENCE_ROOT)).thenReturn(Map.of(PREFERENCE_KEY, OLD_IDENTITY));
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	public void skipsTheSystemUser() {
		patch.rewritePreferences(Map.of(OLD_IDENTITY, OLAP_QUERY_ID));

		verify(preferenceService, never()).getPreferences(eq(AuthenticationUtil.getSystemUserName()), anyString());
	}

	@Test
	public void skipsAPersonWithoutUserName() {
		patch.rewritePreferences(Map.of(OLD_IDENTITY, OLAP_QUERY_ID));

		verify(preferenceService, never()).getPreferences(eq(null), anyString());
	}

	@Test
	public void rewritesThePreferencesOfRealUsers() {
		int rewritten = patch.rewritePreferences(Map.of(OLD_IDENTITY, OLAP_QUERY_ID));

		assertEquals(1, rewritten);
		verify(preferenceService).setPreferences(USER_NAME, Map.<String, Serializable> of(PREFERENCE_KEY, OLAP_QUERY_ID));
	}

	@Test
	public void leavesTheUsersAloneWhenNothingWasMigrated() {
		int rewritten = patch.rewritePreferences(Map.of());

		assertEquals(0, rewritten);
		verify(preferenceService, never()).setPreferences(anyString(), anyMap());
	}
}
