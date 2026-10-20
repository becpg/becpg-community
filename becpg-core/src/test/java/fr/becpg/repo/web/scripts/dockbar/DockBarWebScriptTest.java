package fr.becpg.repo.web.scripts.dockbar;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Unit tests of the <code>clear</code> parameter of {@link DockBarWebScript}.
 */
public class DockBarWebScriptTest {

	private static final String USER_NAME = "jdoe";

	private static final String PREF_DOCKBAR_HISTORY = "fr.becpg.DockBarHistory";

	private final PreferenceService preferenceService = mock(PreferenceService.class);

	private final WebScriptRequest req = mock(WebScriptRequest.class);

	private final WebScriptResponse res = mock(WebScriptResponse.class);

	private DockBarWebScript webScript;

	@BeforeClass
	public static void initAuthenticationUtil() throws Exception {
		new AuthenticationUtil().afterPropertiesSet();
	}

	@Before
	public void setUp() throws IOException {
		AuthenticationUtil.setFullyAuthenticatedUser(USER_NAME);
		Description description = mock(Description.class);
		when(description.getRequiredCache()).thenReturn(mock(Description.RequiredCache.class));
		when(res.getWriter()).thenReturn(new StringWriter());

		webScript = spy(new DockBarWebScript());
		webScript.setPreferenceService(preferenceService);
		doReturn(description).when(webScript).getDescription();
	}

	@After
	public void tearDown() {
		AuthenticationUtil.clearCurrentSecurityContext();
	}

	@Test
	public void clearRemovesOnlyTheDockBarHistory() throws IOException {
		when(req.getParameter("clear")).thenReturn("true");

		webScript.execute(req, res);

		verify(preferenceService).clearPreferences(USER_NAME, PREF_DOCKBAR_HISTORY);
		verify(preferenceService, never()).clearPreferences(anyString());
	}

}
