package fr.becpg.web.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * <p>RejectedAlfrescoTicketTrackerTest class.</p>
 *
 * Covers which responses tell the AIMS filter that the repository refused the session ticket, and the session mark
 * that makes the next request renew it.
 *
 * @author matthieu
 */
public class RejectedAlfrescoTicketTrackerTest {

    private static final String SHARE_CONTEXT = "/share";

    private static final String REJECTED_TICKET_ATTRIBUTE = "becpg.aims.rejectedAlfrescoTicket";

    private static final String DATALISTS_PROXY_URI = "/share/proxy/alfresco/becpg/entitylists/node/workspace/SpacesStore/abc";

    private static final String COLUMNS_SERVICE_URI = "/share/service/becpg/entity/datagrid/config/columns";

    private static final int UNAUTHORIZED = 401;

    private RejectedAlfrescoTicketTracker tracker;

    @Before
    public void setUp() {
        tracker = new RejectedAlfrescoTicketTracker(SHARE_CONTEXT);
    }

    @Test
    public void detectsUnauthorizedProxyResponse() {
        assertTrue(tracker.isTicketRejection(request(DATALISTS_PROXY_URI), UNAUTHORIZED));
    }

    @Test
    public void detectsUnauthorizedServiceResponse() {
        assertTrue(tracker.isTicketRejection(request(COLUMNS_SERVICE_URI), UNAUTHORIZED));
    }

    @Test
    public void ignoresOtherStatuses() {
        assertFalse(tracker.isTicketRejection(request(DATALISTS_PROXY_URI), 200));
        assertFalse(tracker.isTicketRejection(request(DATALISTS_PROXY_URI), 403));
        assertFalse(tracker.isTicketRejection(request(DATALISTS_PROXY_URI), 500));
    }

    @Test
    public void ignoresUnauthorizedPageResponse() {
        assertFalse(tracker.isTicketRejection(request("/share/page/entity-data-lists"), UNAUTHORIZED));
    }

    @Test
    public void ignoresUnauthorizedNoAuthResponse() {
        assertFalse(tracker.isTicketRejection(request("/share/proxy/alfresco-noauth/api/people/reset"), UNAUTHORIZED));
    }

    @Test
    public void marksTheSessionOfTheRequest() {
        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = request(DATALISTS_PROXY_URI);
        when(request.getSession(false)).thenReturn(session);

        tracker.markRejected(request);

        verify(session).setAttribute(REJECTED_TICKET_ATTRIBUTE, Boolean.TRUE);
    }

    @Test
    public void markingWithoutValidSessionDoesNothing() {
        HttpServletRequest request = request(DATALISTS_PROXY_URI);
        when(request.getSession(false)).thenReturn(null);

        tracker.markRejected(request);

        verify(request).getSession(false);
    }

    @Test
    public void readsTheMark() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(REJECTED_TICKET_ATTRIBUTE)).thenReturn(Boolean.TRUE);

        assertTrue(tracker.isRejected(session));
    }

    @Test
    public void unmarkedSessionIsNotRejected() {
        assertFalse(tracker.isRejected(mock(HttpSession.class)));
    }

    @Test
    public void clearsTheMark() {
        HttpSession session = mock(HttpSession.class);

        tracker.clearRejected(session);

        verify(session).removeAttribute(REJECTED_TICKET_ATTRIBUTE);
    }

    private HttpServletRequest request(String requestUri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(requestUri);
        return request;
    }
}
