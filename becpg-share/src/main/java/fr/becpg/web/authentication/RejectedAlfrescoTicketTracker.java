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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.web.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Remembers, in the HTTP session, that the repository refused the Alfresco ticket of an authenticated AIMS session.
 *
 * The repository keeps a single ticket per user, shared by every Share session of that user. When one of them logs
 * out, or when the repository restarts, the ticket is gone while the other sessions stay authenticated on the Share
 * side: every background call then gets a 401, the client reloads the page, and the page reload keeps sending the
 * dead ticket. Marking the session on the first 401 lets the AIMS filter fetch a new ticket on the next request.
 *
 * Stateless and thread-safe: the mark lives in the session.
 *
 * @author matthieu
 */
public class RejectedAlfrescoTicketTracker {

    private static final String REJECTED_TICKET_ATTRIBUTE = "becpg.aims.rejectedAlfrescoTicket";

    private final ShareBackgroundEndpoints protectedEndpoints;

    /**
     * <p>Constructor for RejectedAlfrescoTicketTracker.</p>
     *
     * @param shareContext the Share context path, for instance <code>/share</code>
     */
    public RejectedAlfrescoTicketTracker(String shareContext) {
        this.protectedEndpoints = new ShareBackgroundEndpoints(shareContext);
    }

    /**
     * Tells whether a served request shows that the repository refused the session ticket.
     *
     * @param request the served request
     * @param status the status of its response
     * @return true for a 401 answered by a service or proxy endpoint
     */
    public boolean isTicketRejection(HttpServletRequest request, int status) {
        return status == HttpServletResponse.SC_UNAUTHORIZED && protectedEndpoints.isProtected(request.getRequestURI());
    }

    /**
     * Marks the session of the request, if it is still valid, as holding a refused ticket.
     *
     * @param request the request whose response was a ticket rejection
     */
    public void markRejected(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(REJECTED_TICKET_ATTRIBUTE, Boolean.TRUE);
        }
    }

    /**
     * Tells whether the session holds a ticket the repository refused.
     *
     * @param session the HTTP session
     * @return true when the ticket has to be renewed
     */
    public boolean isRejected(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(REJECTED_TICKET_ATTRIBUTE));
    }

    /**
     * Forgets the mark once the ticket has been renewed, or the session is about to be dropped.
     *
     * @param session the HTTP session
     */
    public void clearRejected(HttpSession session) {
        session.removeAttribute(REJECTED_TICKET_ATTRIBUTE);
    }
}
