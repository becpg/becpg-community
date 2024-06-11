package fr.becpg.repo.activity.remote;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import fr.becpg.common.BeCPGException;

/**
 * 
 */
public class XmlActivityVisitor implements RemoteActivityVisitor {

	private static Log logger = LogFactory.getLog(XmlActivityVisitor.class);

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
	public XmlActivityVisitor(SiteService siteService, NodeService nodeService, NamespaceService namespaceService, ContentService contentService) {
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
	 * @throws javax.xml.stream.XMLStreamException if any.
	 */
	@Override
	public void visit(List<ActivityFeedEntity> feedEntries, OutputStream result) throws BeCPGException {

		try {

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

			if (!feedEntries.isEmpty()) {

				for (ActivityFeedEntity feedEntry : feedEntries) {
					writeFeedEntry(xmlw, feedEntry);
				}

			}
			xmlw.writeEndElement();

			// Write document end. This closes all open structures
			xmlw.writeEndDocument();
			// Close the writer to flush the output
			xmlw.close();
		} catch (XMLStreamException e) {
			logger.debug("Exception while writing XML response", e);
			throw new BeCPGException("Error while writing XML response : ", e);
		}

	}

	/**
	 * @param xmlw
	 * @param feedEntry
	 * @throws XMLStreamException
	 */
	private void writeFeedEntry(XMLStreamWriter xmlw, ActivityFeedEntity feedEntry) throws XMLStreamException {
		try {
			String type = feedEntry.getActivityType();

			xmlw.writeStartElement("activity");
			xmlw.writeAttribute("id", feedEntry.getId().toString());
			xmlw.writeAttribute("site", feedEntry.getSiteNetwork());
			xmlw.writeAttribute("type", type.substring(type.lastIndexOf(".") + 1));
			xmlw.writeAttribute("user", feedEntry.getPostUserId());
			xmlw.writeAttribute("date", ISO8601DateFormat.format(feedEntry.getPostDate()));

			Map<String, Object> summary = JSONtoFmModel.convertJSONObjectToMap(feedEntry.getActivitySummary());

			NodeRef nodeRef = null;

			if (summary.containsKey("nodeRef")) {
				nodeRef = new NodeRef(toString(summary.get("nodeRef")));
			} else if (summary.containsKey("entityNodeRef")) {
				nodeRef = new NodeRef(toString(summary.get("entityNodeRef")));
			}
			if (nodeRef != null) {
				xmlw.writeAttribute("nodeRef", nodeRef.toString());

				if (nodeService.exists(nodeRef)) {
					if (nodeService.hasAspect(nodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
						nodeRef = new NodeRef((String) nodeService.getProperty(nodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
					}

					xmlw.writeAttribute("nodeType", nodeService.getType(nodeRef).toPrefixString(namespaceService));
					ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
					if ((contentReader != null) && (contentReader.getMimetype() != null)) {
						xmlw.writeAttribute("mimeType", contentReader.getMimetype());
					}
				}
			}

			xmlw.writeAttribute("title", toString(summary.get("title")));
			xmlw.writeAttribute("lastName", toString(summary.get("lastName")));
			xmlw.writeAttribute("firstName", toString(summary.get("firstName")));
			if (feedEntry.getSiteNetwork() != null) {
				SiteInfo siteInfo = siteService.getSite(feedEntry.getSiteNetwork());
				if (siteInfo != null) {
					xmlw.writeAttribute("siteTitle", siteInfo.getTitle());
				}
			}

			xmlw.writeCData(feedEntry.getActivitySummary());

			xmlw.writeEndElement();

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
		return "application/xml";
	}

}
