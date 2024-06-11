/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.repo.web.scripts.publication;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.web.scripts.remote.ListEntitiesWebScript;

/**
 * <p>ListEntitiesByChannelWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ListEntitiesByChannelWebScript extends ListEntitiesWebScript {

	private static final String PARAM_CHANNELID = "channelId";
	private static final String PARAM_CHANNELNODEREF = "channelNodeRef";

	PublicationChannelService publicationChannelService;

	/**
	 * <p>Setter for the field <code>publicationChannelService</code>.</p>
	 *
	 * @param publicationChannelService a {@link fr.becpg.repo.publication.PublicationChannelService} object
	 */
	public void setPublicationChannelService(PublicationChannelService publicationChannelService) {
		this.publicationChannelService = publicationChannelService;
	}

	/** {@inheritDoc} */
	@Override
	protected PagingResults<NodeRef> findEntities(WebScriptRequest req, Boolean limit) {

		String channelId = req.getParameter(PARAM_CHANNELID);
		String channelNodeRefStr = req.getParameter(PARAM_CHANNELNODEREF);

		Integer maxResults = intParam(req, PARAM_MAX_RESULTS);
		Integer page = intParam(req, PARAM_PAGE);

		int skipCount = 0;

		if (maxResults == null || Boolean.TRUE.equals(limit)) {
			maxResults = RepoConsts.MAX_RESULTS_256;
		}

		if (page != null && page > 0 && !RepoConsts.MAX_RESULTS_UNLIMITED.equals(maxResults)) {
			skipCount = (page - 1) * maxResults;
		}

		NodeRef channelNodeRef = null;
		if ((channelNodeRefStr != null) && !channelNodeRefStr.isBlank()) {
			channelNodeRef = new NodeRef(channelNodeRefStr);

		} else if (channelId != null && !channelId.isBlank()) {
			channelNodeRef = publicationChannelService.getChannelById(channelId);
		}

		if (channelNodeRef != null && nodeService.exists(channelNodeRef)) {
			if (!AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(channelNodeRef))) {
				throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
			}
			return publicationChannelService.getEntitiesByChannel(channelNodeRef, new PagingRequest(skipCount, maxResults));
		}

		throw new WebScriptException(Status.STATUS_NOT_FOUND, "Channel not found " + channelNodeRef);

	}

}
