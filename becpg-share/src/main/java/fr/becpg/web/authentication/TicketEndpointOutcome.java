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

/**
 * What the repository ticket endpoint answered when asked for an Alfresco ticket with an access token.
 *
 * Only a refused access token means the session can no longer be used. Any other failure, such as a repository
 * still starting after a restart, is transient and must not cost the user its session.
 *
 * @author matthieu
 */
public enum TicketEndpointOutcome {

    /** A ticket was issued for the access token. */
    ISSUED,

    /** The repository refused the access token itself. */
    TOKEN_REFUSED,

    /** The repository could not answer, the request can be retried later. */
    UNAVAILABLE;

    private static final int STATUS_OK = 200;

    private static final int STATUS_UNAUTHORIZED = 401;

    private static final int STATUS_FORBIDDEN = 403;

    /**
     * Maps the HTTP status of the ticket endpoint.
     *
     * @param status the HTTP status answered by the repository
     * @return the outcome matching that status
     */
    public static TicketEndpointOutcome fromStatus(int status) {
        return switch (status) {
            case STATUS_OK -> ISSUED;
            case STATUS_UNAUTHORIZED, STATUS_FORBIDDEN -> TOKEN_REFUSED;
            default -> UNAVAILABLE;
        };
    }
}
