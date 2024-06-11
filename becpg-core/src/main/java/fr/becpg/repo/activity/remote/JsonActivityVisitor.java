package fr.becpg.repo.activity.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import fr.becpg.common.BeCPGException;

/**
 * Visitor for retrieving Activities in Json format
 * 
 * @author frederic
 */
public class JsonActivityVisitor implements RemoteActivityVisitor {

	private static Log logger = LogFactory.getLog(JsonActivityVisitor.class);

	public static final String NODE_REF = "nodeRef";
	public static final String TITLE = "title";
	public static final String LAST_NAME = "lastName";
	public static final String FIRST_NAME = "firstName";

	private SiteService siteService;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private ContentService contentService;

	/**
	 * @param siteService
	 * @param nodeService
	 * @param namespaceService
	 * @param contentService
	 */
	public JsonActivityVisitor(SiteService siteService, NodeService nodeService, NamespaceService namespaceService, ContentService contentService) {
		super();
		this.siteService = siteService;
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
		this.contentService = contentService;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param feedEntries a {@link org.alfresco.query.PagingResults} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	@Override
	public void visit(List<ActivityFeedEntity> feedEntries, OutputStream result) throws BeCPGException {

		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {

			JSONObject jsonResp = new JSONObject();

			logger.debug("Writing activities to outputStream...");

			if (!feedEntries.isEmpty()) {
				JSONArray activitiesJson = new JSONArray();
				for (ActivityFeedEntity feedEntry : feedEntries) {
					activitiesJson.put(createFeedEntityJson(feedEntry));
				}

				jsonResp.put("activities", activitiesJson);
			}

			jsonResp.write(out);

		} catch (IOException e) {
			logger.debug("Exception while writing JSON to response", e);
			throw new BeCPGException("Error while writing JSON to response : ", e);
		}
	}

	/**
	 * @param generator
	 * @param feedEntry
	 * @throws IOException
	 */
	private JSONObject createFeedEntityJson(ActivityFeedEntity feedEntry) throws BeCPGException {
		try {

			JSONObject feedEntityJson = new JSONObject();

			String type = feedEntry.getActivityType();

			feedEntityJson.put("id", feedEntry.getId().toString());
			feedEntityJson.put("site", feedEntry.getSiteNetwork());
			feedEntityJson.put("type", type.substring(type.lastIndexOf(".") + 1));
			feedEntityJson.put("user", feedEntry.getPostUserId());
			feedEntityJson.put("date", ISO8601DateFormat.format(feedEntry.getPostDate()));

			JSONObject summary = new JSONObject(feedEntry.getActivitySummary());

			NodeRef nodeRef = null;

			if (summary.keySet().contains(NODE_REF)) {
				nodeRef = new NodeRef(toString(summary.get(NODE_REF)));
			} else if (summary.keySet().contains("entityNodeRef")) {
				nodeRef = new NodeRef(toString(summary.get("entityNodeRef")));
			}
			if (nodeRef != null) {
				feedEntityJson.put(NODE_REF, nodeRef.toString());

				if (nodeService.exists(nodeRef)) {
					if (nodeService.hasAspect(nodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
						nodeRef = new NodeRef((String) nodeService.getProperty(nodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
					}

					feedEntityJson.put("nodeType", nodeService.getType(nodeRef).toPrefixString(namespaceService));
					ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
					if ((contentReader != null) && (contentReader.getMimetype() != null)) {
						feedEntityJson.put("mimeType", contentReader.getMimetype());
					}
				}
			}
			feedEntityJson.put(TITLE, toString(summary.opt(TITLE)));
			feedEntityJson.put(LAST_NAME, toString(summary.opt(LAST_NAME)));
			feedEntityJson.put(FIRST_NAME, toString(summary.opt(FIRST_NAME)));

			if (feedEntry.getSiteNetwork() != null) {
				SiteInfo siteInfo = siteService.getSite(feedEntry.getSiteNetwork());
				if (siteInfo != null) {
					feedEntityJson.put("siteTitle", siteInfo.getTitle());
				}
			}
			// Writing activitySummary
			feedEntityJson.put("activitySummary", summary);

			return feedEntityJson;

		} catch (JSONException je) {
			// skip this feed entry
			logger.warn("An error occured while creating the Json : " + je.getMessage());
			throw new BeCPGException("An error occured while creating the Json : ", je);

		}
	}

	private String toString(Object val) {
		if (val != null) {
			return val.toString();
		}
		return "";
	}

	@Override
	public String getContentType() {
		return "application/json";
	}
}