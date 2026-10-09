package fr.becpg.web.authentication;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <p>TicketEndpointOutcomeTest class.</p>
 *
 * Covers which answers of the repository ticket endpoint cost the user its session: only a refused access token,
 * never a repository that is restarting or failing.
 *
 * @author matthieu
 */
public class TicketEndpointOutcomeTest {

    @Test
    public void okMeansIssued() {
        assertEquals(TicketEndpointOutcome.ISSUED, TicketEndpointOutcome.fromStatus(200));
    }

    @Test
    public void unauthorizedMeansTokenRefused() {
        assertEquals(TicketEndpointOutcome.TOKEN_REFUSED, TicketEndpointOutcome.fromStatus(401));
    }

    @Test
    public void forbiddenMeansTokenRefused() {
        assertEquals(TicketEndpointOutcome.TOKEN_REFUSED, TicketEndpointOutcome.fromStatus(403));
    }

    @Test
    public void serverErrorsAreTransient() {
        assertEquals(TicketEndpointOutcome.UNAVAILABLE, TicketEndpointOutcome.fromStatus(500));
        assertEquals(TicketEndpointOutcome.UNAVAILABLE, TicketEndpointOutcome.fromStatus(502));
        assertEquals(TicketEndpointOutcome.UNAVAILABLE, TicketEndpointOutcome.fromStatus(503));
    }

    @Test
    public void unreachableRepositoryIsTransient() {
        assertEquals(TicketEndpointOutcome.UNAVAILABLE, TicketEndpointOutcome.fromStatus(0));
    }
}
