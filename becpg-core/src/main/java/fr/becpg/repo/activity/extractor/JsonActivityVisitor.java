package fr.becpg.repo.activity.extractor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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
import org.alfresco.util.JSONtoFmModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Visitor for retrieving Activities in Json format
 * 
 * @author frederic
 */
public class JsonActivityVisitor implements RemoteActivityVisitor {

	private static Log logger = LogFactory.getLog(JsonActivityVisitor.class);

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
	public void visit(List<ActivityFeedEntity> feedEntries, OutputStream result) throws IOException {

		JsonFactory factory = new JsonFactory();
		try (JsonGenerator generator = factory.createGenerator(result, JsonEncoding.UTF8);) {
			logger.debug("Writing activities to outputStream...");

			generator.setCodec(new ObjectMapper());
			generator.writeStartObject();
			generator.writeArrayFieldStart("activities");

			if (!feedEntries.isEmpty()) {

				for (ActivityFeedEntity feedEntry : feedEntries) {
					writeFeedEntry(generator, feedEntry);
				}
			}

			generator.writeEndArray();
			generator.writeEndObject();
		}
	}

	/**
	 * @param generator
	 * @param feedEntry
	 * @throws IOException
	 */
	private void writeFeedEntry(JsonGenerator generator, ActivityFeedEntity feedEntry) throws IOException {
		try {

			generator.writeStartObject();

			String type = feedEntry.getActivityType();

			generator.writeStringField("id", feedEntry.getId().toString());
			generator.writeStringField("site", feedEntry.getSiteNetwork());
			generator.writeStringField("type", type.substring(type.lastIndexOf(".") + 1));
			generator.writeStringField("user", feedEntry.getPostUserId());
			generator.writeStringField("date", ISO8601DateFormat.format(feedEntry.getPostDate()));

			Map<String, Object> summary = JSONtoFmModel.convertJSONObjectToMap(feedEntry.getActivitySummary());

			NodeRef nodeRef = null;

			if (summary.containsKey("nodeRef")) {
				nodeRef = new NodeRef(toString(summary.get("nodeRef")));
			} else if (summary.containsKey("entityNodeRef")) {
				nodeRef = new NodeRef(toString(summary.get("entityNodeRef")));
			}
			if (nodeRef != null && (nodeService.exists(nodeRef))) {
				if (nodeService.hasAspect(nodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
					nodeRef = new NodeRef((String) nodeService.getProperty(nodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
				}

				generator.writeStringField("nodeRef", nodeRef.toString());

				generator.writeStringField("nodeType", nodeService.getType(nodeRef).toPrefixString(namespaceService));
				ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
				if ((contentReader != null) && (contentReader.getMimetype() != null)) {
					generator.writeStringField("mimeType", contentReader.getMimetype());
				}
			}

			generator.writeStringField("title", toString(summary.get("title")));
			generator.writeStringField("lastName", toString(summary.get("lastName")));
			generator.writeStringField("firstName", toString(summary.get("firstName")));
			if (feedEntry.getSiteNetwork() != null) {
				SiteInfo siteInfo = siteService.getSite(feedEntry.getSiteNetwork());
				if (siteInfo != null) {
					generator.writeStringField("siteTitle", siteInfo.getTitle());
				}
			}
			// Writing activitySummary
			generator.writeObjectField("activitySummary", summary);

			generator.writeEndObject(); // End of feedEntry

		} catch (JSONException je) {
			// skip this feed entry
			logger.warn("Skip feed entry : " + je.getMessage());
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
