package fr.becpg.repo.web.scripts.report;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.thumbnail.script.ScriptThumbnailService;
import org.alfresco.repo.web.scripts.quickshare.QuickShareContentGet;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.jscript.Thumbnail;

/**
 * <p>
 * ReportQuickShareThumbnailContentGet class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportQuickShareThumbnailContentGet extends QuickShareContentGet {
	private static final Log logger = LogFactory.getLog(ReportQuickShareThumbnailContentGet.class);

	private ThumbnailService thumbnailService;
	private ScriptThumbnailService scriptThumbnailService;
	private ServiceRegistry serviceRegistry;
	private Thumbnail thumbnail;
	private EntityDictionaryService entityDictionaryService;
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	public void setNodeService(NodeService nodeService) {
		super.setNodeService(nodeService);
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>thumbnail</code>.
	 * </p>
	 *
	 * @param thumbnail
	 *            a {@link fr.becpg.repo.jscript.Thumbnail} object.
	 */
	public void setThumbnail(Thumbnail thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * <p>
	 * Setter for the field <code>entityDictionaryService</code>.
	 * </p>
	 *
	 * @param entityDictionaryService
	 *            a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>
	 * Setter for the field <code>thumbnailService</code>.
	 * </p>
	 *
	 * @param thumbnailService
	 *            a {@link org.alfresco.service.cmr.thumbnail.ThumbnailService}
	 *            object.
	 */
	public void setThumbnailService(ThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	/**
	 * <p>
	 * Setter for the field <code>scriptThumbnailService</code>.
	 * </p>
	 *
	 * @param scriptThumbnailService
	 *            a
	 *            {@link org.alfresco.repo.thumbnail.script.ScriptThumbnailService}
	 *            object.
	 */
	public void setScriptThumbnailService(ScriptThumbnailService scriptThumbnailService) {
		this.scriptThumbnailService = scriptThumbnailService;
	}

	/**
	 * <p>
	 * Setter for the field <code>serviceRegistry</code>.
	 * </p>
	 *
	 * @param services
	 *            a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry services) {
		this.serviceRegistry = services;
	}

	/** {@inheritDoc} */
	@Override
	protected void executeImpl(NodeRef nodeRef, Map<String, String> templateVars, WebScriptRequest req, WebScriptResponse res, Map<String, Object> model, boolean attach) throws IOException
	{

		String thumbnailName = templateVars.get("thumbnailname");
		if (thumbnailName == null) {
			logger.error("Thumbnail name was not provided: " + nodeRef);
			throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + nodeRef);
		}

		// Indicate whether or not the thumbnail can be cached by the browser.
		// Caching is allowed if the lastModified
		// argument is provided as this is an indication of request uniqueness
		// and therefore the browser will have
		// the latest thumbnail image.
		if (model == null) {
			model = new HashMap<>(1);
		}

		if (req.getParameter("lastModified") != null) {
			model.put("allowBrowserToCache", "true"); // note: must be String
														// not boolean
		} else {
			model.put("allowBrowserToCache", "false"); // note: must be String
														// not boolean
		}
		
		try {
			Context.enter();

			Scriptable scope = Context.getCurrentContext().initSafeStandardObjects(); // note:
			// required
			// for
			// ValueConverter
			// (collection)
			ScriptNode node = new ScriptNode(nodeRef, serviceRegistry, scope);

			if ((nodeRef != null) && entityDictionaryService.isSubClass(nodeService.getType(nodeRef), ReportModel.TYPE_REPORT)) {
				
				node = new ScriptNode(thumbnail.refreshReport(node).getNodeRef(), serviceRegistry, scope);
			}

			NodeRef thumbnailNodeRef = thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, thumbnailName);

			if (thumbnailNodeRef == null) {

				// Get the queue/force create setting
				boolean qc = false;
				boolean fc = false;
				String c = req.getParameter("c");
				if (c != null) {
					if (c.equals("queue")) {
						qc = true;
					} else if (c.equals("force")) {
						fc = true;
					}
				}

				// Get the place holder flag
				boolean ph = false;
				String phString = req.getParameter("ph");
				if (phString != null) {
					ph =  Boolean.valueOf(phString);
				}

				// Queue the creation of the thumbnail if appropriate

				
				if (fc) {
					ScriptNode thumbnailNode = node.createThumbnail(thumbnailName, false, true);
					
					if (thumbnailNode != null) {
						thumbnailNodeRef = thumbnailNode.getNodeRef();
					}
				} else {
					if (qc) {
						node.createThumbnail(thumbnailName, true);
					}
				}

				if (thumbnailNodeRef == null) {
					if (ph == true) {
						// Try and get the place holder resource. We use a
						// method in
						// the thumbnail service
						// that by default gives us a resource based on the
						// content's mime type.
						String phPath = null;
						ContentData contentData = (ContentData) this.serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
						if (contentData != null) {
							phPath = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath(thumbnailName, contentData.getMimetype());
						}

						if (phPath == null) {
							// 404 since no thumbnail was found
							throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
									"Thumbnail was not found and no place holder resource set for '" + thumbnailName + "'");
						} else {
							// Set the resouce path in the model ready for the
							// content stream to send back to the client
							model.put("contentPath", phPath);
						}
					} else {
						// 404 since no thumbnail was found
						throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Thumbnail was not found");
					}
				}
			}

	        // determine attachment
	        attach = Boolean.valueOf(req.getParameter("a"));

			super.executeImpl(thumbnailNodeRef, templateVars, req, res, model, attach);

			if (logger.isDebugEnabled()) {
				logger.debug("QuickShare - retrieved thumbnail content: " + thumbnailNodeRef + " [" + nodeRef + "," + thumbnailName + "]");
			}
		} finally {
			Context.exit();
		}
	}
}
