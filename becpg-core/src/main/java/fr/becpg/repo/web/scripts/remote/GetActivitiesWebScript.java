/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.util.JSONtoFmModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * Get activities as XML
 *
 * @author matthieu
 *
 */
public class GetActivitiesWebScript extends AbstractWebScript {

	
	protected static final String PARAM_FEED_ID = "feedDBID";
	
	private ActivityService activityService;
	

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	private static Log logger = LogFactory.getLog(GetActivitiesWebScript.class);

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		String feedUserId = AuthenticationUtil.getFullyAuthenticatedUser();

		String feedId = req.getParameter(PARAM_FEED_ID);
		
		// where did we get up to ?
		Long feedDBID = -1L;
		
		if(feedId!=null ) {
			feedDBID = Long.parseLong(feedId);
		}

		// own + others (note: template can be changed to filter out user's own
		// activities if needed)
		if (logger.isDebugEnabled()) {
			logger.debug("Get user feed entries: " + feedUserId + ", " + feedDBID);
		}
		
		try {
			
		 PagingResults<ActivityFeedEntity> feedEntries = 	activityService.getPagedUserFeedEntries(feedUserId, null, false, false, feedDBID, new PagingRequest(1000));
			
			visit(feedEntries, resp.getOutputStream());

			// set mimetype for the content and the character encoding + length
			// for the stream
			resp.setContentType("application/xml");
			resp.setContentEncoding("UTF-8");
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}

		} catch (Exception e) {
			logger.error(e,e);
		} 

	}
	
	

	public void visit(PagingResults<ActivityFeedEntity> feedEntries, OutputStream result) throws XMLStreamException {

		// Create an output factory
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		// Create an XML stream writer
		XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(result);

		if (logger.isDebugEnabled()) {
			logger.debug("Indent xml formater ON");
			xmlw = new IndentingXMLStreamWriter(xmlw);
		}

		// Write XML prologue
		xmlw.writeStartDocument();
		// Visit node

		xmlw.writeStartElement("activities");
		
		if (feedEntries.getPage().size() > 0) {

			for (ActivityFeedEntity feedEntry : feedEntries.getPage()) {
				try {
					String type = feedEntry.getActivityType();
					
					xmlw.writeStartElement("activity");
					xmlw.writeAttribute("id", feedEntry.getId().toString());
					xmlw.writeAttribute("site", feedEntry.getSiteNetwork());
					xmlw.writeAttribute("type",type.substring(type.lastIndexOf(".") + 1) );
					xmlw.writeAttribute("user", feedEntry.getPostUserId());
					xmlw.writeAttribute("date", feedEntry.getPostDate().toString());
					
					Map<String,Object>  summary = JSONtoFmModel.convertJSONObjectToMap(feedEntry.getActivitySummary());
					
					xmlw.writeAttribute("nodeRef",toString(summary.get("nodeRef")));
					xmlw.writeAttribute("title", toString(summary.get("title")));
					
					xmlw.writeCData(feedEntry.getActivitySummary());
					
					xmlw.writeEndElement();

				} catch (JSONException je) {
					// skip this feed entry
					logger.warn("Skip feed entry : " + je.getMessage());
					continue;
				}
			}

		}
		xmlw.writeEndElement();
		
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	private String toString(Object val) {
		if(val!=null) {
			return val.toString();
		}
		return "";
	}

}
