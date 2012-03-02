package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.ExportFormat;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Abstract remote entity webscript
 * 
 * @author matthieu
 * 
 */
public abstract class AbstractEntityWebScript extends AbstractWebScript {

	protected static Log logger = LogFactory.getLog(AbstractEntityWebScript.class);

	/** The Constant PARAM_QUERY. */
	protected static final String PARAM_QUERY = "query";

	/** The Constant PARAM_PATH. */
	protected static final String PARAM_PATH = "path";

	/** The Constant PARAM_PATH. */
	protected static final String PARAM_FORMAT = "format";

	/** The Constant PARAM_NODEREF. */
	protected static final String PARAM_NODEREF = "nodeRef";

	/** The Constant PARAM_CALLBACK. */
	/** http://admin:becpg@localhost:8080/alfresco/services/becpg/remote/entity **/
	protected static final String PARAM_CALLBACK = "callback";

	/** Services **/

	protected NodeService nodeService;

	protected EntityService entityService;

	protected BeCPGSearchService beCPGSearchService;

	protected MimetypeService mimetypeService;

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	protected NodeRef findEntity(WebScriptRequest req) {
		String nodeRef = req.getParameter(PARAM_NODEREF);
		if (nodeRef != null && nodeRef.length() > 0) {
			NodeRef node = new NodeRef(nodeRef);
			if (nodeService.exists(node)) {
				return node;
			} else {
				throw new WebScriptException("Node " + nodeRef + " doesn't exist in repository");
			}

		}
		String path = req.getParameter(PARAM_PATH);
		if (path != null && path.length() > 0) {

		}
		String query = req.getParameter(PARAM_QUERY);
		if (query != null && query.length() > 0) {
			query += " +TYPE:\"bcpg:entity\"";
			List<NodeRef> refs = beCPGSearchService.luceneSearch(query, 1);
			if (refs.size() > 0) {
				return refs.get(0);
			}
			throw new WebScriptException("No entity node found for query " + query);
		}

		throw new WebScriptException("No entity node found");
	}

	protected void sendOKStatus(NodeRef entityNodeRef, WebScriptResponse resp) throws IOException {
		resp.getWriter().write("IMPORT OK ("+entityNodeRef+")");
	}

	protected EntityProviderCallBack getEntityProviderCallback(WebScriptRequest req) {
		final String callBack = req.getParameter(PARAM_CALLBACK);
		if (callBack != null && callBack.length() > 0) {
			return new EntityProviderCallBack() {

				@Override
				public NodeRef provideNode(NodeRef nodeRef) throws BeCPGException {
					try {
						logger.debug("EntityProviderCallBack call : " + callBack + "?nodeRef=" + nodeRef.toString());
						URLConnection url = new URL(callBack + "?nodeRef=" + nodeRef.toString()).openConnection();
						return entityService.createOrUpdateEntity(nodeRef, url.getInputStream(), ExportFormat.xml, this);
					} catch (MalformedURLException e) {
						throw new BeCPGException(e);
					} catch (IOException e) {
						throw new BeCPGException(e);
					}

				}
			};
		}
		logger.debug("No callback param provided");
		return null;
	}

	protected ExportFormat getFormat(WebScriptRequest req) {
		String format = req.getParameter(PARAM_FORMAT);
		if (format != null && ExportFormat.csv.toString().equals(format)) {
			return ExportFormat.csv;
		}
		return ExportFormat.xml;
	}
}
