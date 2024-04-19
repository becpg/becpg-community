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
package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.activity.remote.JsonActivityVisitor;
import fr.becpg.repo.activity.remote.RemoteActivityVisitor;
import fr.becpg.repo.activity.remote.XmlActivityVisitor;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;

/**
 * Get activities as XML or JSON
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GetActivitiesWebScript extends AbstractEntityWebScript {

	/** Constant <code>PARAM_FEED_ID="feedDBID"</code> */
	protected static final String PARAM_FEED_ID = "feedDBID";

	private ActivityService activityService;

	private SiteService siteService;

	private ContentService contentService;

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>activityService</code>.</p>
	 *
	 * @param activityService a {@link org.alfresco.service.cmr.activities.ActivityService} object.
	 */
	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	/**
	 * <p>Setter for the field <code>siteService</code>.</p>
	 *
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object.
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private static Log logger = LogFactory.getLog(GetActivitiesWebScript.class);

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		String feedUserId = AuthenticationUtil.getFullyAuthenticatedUser();

		String feedId = req.getParameter(PARAM_FEED_ID);

		// where did we get up to ?
		Long feedDBID = -1L;

		if (feedId != null) {
			feedDBID = Long.parseLong(feedId);
		}

		// own + others (note: template can be changed to filter out user's own
		// activities if needed)
		if (logger.isDebugEnabled()) {
			logger.debug("Get user feed entries: " + feedUserId + ", " + feedDBID);
		}

		try (OutputStream out = resp.getOutputStream()) {
			resp.setContentEncoding("UTF-8");

			RemoteActivityVisitor remoteActivityVisitor = null;

			RemoteEntityFormat format = getFormat(req);

			switch (format) {
			case xml, xml_all, xml_light:
				remoteActivityVisitor = new XmlActivityVisitor(siteService, nodeService, namespaceService, contentService);
				break;

			case json, json_all:
				remoteActivityVisitor = new JsonActivityVisitor(siteService, nodeService, namespaceService, contentService);
				break;
			default:
				throw new BeCPGException("Unknown format " + format.toString());
			}

			PagingResults<ActivityFeedEntity> feedEntries = activityService.getPagedUserFeedEntries(feedUserId, null, false, false, feedDBID,
					new PagingRequest(1000));

			resp.setContentType(remoteActivityVisitor.getContentType());
			remoteActivityVisitor.visit(feedEntries.getPage(), out);

			resp.setStatus(Status.STATUS_OK);

		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}
		} catch (BeCPGException e) {
			logger.error(e, e);
		}

	}

}
