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

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.authentication.BeCPGTicketService;

/**
 * <p>Fresh AI auth-token endpoint ({@code /becpg/remote/ai/token}).</p>
 * <p>
 * The beCPG AI token embeds a mortal Alfresco ticket; when baked once into the Watson iframe URL it expires
 * and every WebSocket reconnect then fails with HTTP 401. This endpoint lets the UI mint a <b>fresh</b> token
 * on demand: it runs as the authenticated caller (Share session), so {@link BeCPGTicketService#getCurrentAuthToken()}
 * returns a token built from the current, valid ticket. The response is never cached.
 *
 * @author matthieu
 */
public class AiTokenWebScript extends AbstractWebScript {

	private BeCPGTicketService beCPGTicketService;

	/**
	 * <p>Setter for the field <code>beCPGTicketService</code>.</p>
	 *
	 * @param beCPGTicketService a {@link fr.becpg.repo.authentication.BeCPGTicketService} object
	 */
	public void setBeCPGTicketService(BeCPGTicketService beCPGTicketService) {
		this.beCPGTicketService = beCPGTicketService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		try {
			JSONObject json = new JSONObject();
			json.put("token", beCPGTicketService.getCurrentAuthToken());

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.setHeader("Cache-Control", "no-store");
			res.getWriter().write(json.toString());
		} catch (JSONException e) {
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Failed to build AI token response", e);
		}
	}
}
