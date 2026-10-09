package fr.becpg.web.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * <p>ExpiredSessionAjaxRequestMatcherTest class.</p>
 *
 * Covers which requests the AIMS filter answers with a 401 once the Share session has no user: only the
 * background service and proxy requests, never page requests, script tags or the no-auth endpoints.
 *
 * @author matthieu
 */
public class ExpiredSessionAjaxRequestMatcherTest {

    private static final String SHARE_CONTEXT = "/share";

    private static final String REQUESTED_WITH_HEADER = "X-Requested-With";

    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String USER_ID_ATTRIBUTE = "_alf_USER_ID";

    private static final String PROJECT_DETAILS_URI = "/share/service/modules/project-details/project-details";

    private ExpiredSessionAjaxRequestMatcher matcher;

    @Before
    public void setUp() {
        matcher = new ExpiredSessionAjaxRequestMatcher(SHARE_CONTEXT);
    }

    @Test
    public void matchesBackgroundServiceRequestWithoutUser() {
        assertTrue(matcher.matches(backgroundRequest(PROJECT_DETAILS_URI, null)));
    }

    @Test
    public void matchesBackgroundProxyRequestWithoutUser() {
        assertTrue(matcher.matches(backgroundRequest("/share/proxy/alfresco/becpg/entity/datalists", null)));
    }

    @Test
    public void matchesWhatEverValueAlfrescoPutsInTheHeader() {
        HttpServletRequest request = request(PROJECT_DETAILS_URI, FORM_CONTENT_TYPE, null);

        assertTrue(matcher.matches(request));
    }

    @Test
    public void ignoresRequestOfAnAuthenticatedUser() {
        assertFalse(matcher.matches(backgroundRequest(PROJECT_DETAILS_URI, "admin")));
    }

    @Test
    public void matchesRequestOfTheGuestUser() {
        assertTrue(matcher.matches(backgroundRequest(PROJECT_DETAILS_URI, "guest")));
    }

    @Test
    public void ignoresRequestWithoutTheBackgroundHeader() {
        assertFalse(matcher.matches(request("/share/service/messages.js", null, null)));
    }

    @Test
    public void ignoresPageRequest() {
        assertFalse(matcher.matches(backgroundRequest("/share/page/user/admin/dashboard", null)));
    }

    @Test
    public void ignoresNoAuthEndpoints() {
        assertFalse(matcher.matches(backgroundRequest("/share/proxy/alfresco-noauth/api/people/reset", null)));
        assertFalse(matcher.matches(backgroundRequest("/share/service/noauth/messages", null)));
    }

    @Test
    public void ignoresResources() {
        assertFalse(matcher.matches(backgroundRequest("/share/res/js/alfresco.js", null)));
    }

    @Test
    public void ignoresServicePathOfAnotherContext() {
        assertFalse(matcher.matches(backgroundRequest("/other/service/modules/project-details/project-details", null)));
    }

    private HttpServletRequest backgroundRequest(String requestUri, String userId) {
        return request(requestUri, "application/json", userId);
    }

    private HttpServletRequest request(String requestUri, String requestedWith, String userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getHeader(REQUESTED_WITH_HEADER)).thenReturn(requestedWith);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(USER_ID_ATTRIBUTE)).thenReturn(userId);
        return request;
    }
}
