package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
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
	/** http://localhost:8080/alfresco/services/becpg/remote/entity **/
	protected static final String PARAM_CALLBACK = "callback";

	/**
	 * Callbach auth 
	 * admin:becpg
	 */
	protected static final String PARAM_CALLBACK_USER = "callbackUser";
	
	protected static final String PARAM_CALLBACK_PASSWORD = "callbackPassword";
	
	/** Services **/

	protected NodeService nodeService;

	protected RemoteEntityService remoteEntityService;

	protected BeCPGSearchService beCPGSearchService;

	protected MimetypeService mimetypeService;

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	

	public void setRemoteEntityService(RemoteEntityService remoteEntityService) {
		this.remoteEntityService = remoteEntityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	protected List<NodeRef> findEntities(WebScriptRequest req) {
	
		
		String path = req.getParameter(PARAM_PATH);
		String query = req.getParameter(PARAM_QUERY);
		String runnedQuery = "+TYPE:\"bcpg:entity\" -TYPE:\"cm:systemfolder\""
				+ " -@cm\\:lockType:READ_ONLY_LOCK"
				+ " -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\"";
		
		
		if (path != null && path.length() > 0) {
			runnedQuery+= " +PATH:\"" + path + "//*\"";
		}
		
		
		if (query != null && query.length() > 0) {
			runnedQuery += " "+query;
		
		}
		
		List<NodeRef> refs = beCPGSearchService.luceneSearch(runnedQuery,250);
		if (refs!=null && refs.isEmpty()) {
			return refs;
		}
		throw new WebScriptException("No entities found for query " + query);
		
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
		
		return findEntities(req).get(0);
	}

	protected void sendOKStatus(NodeRef entityNodeRef, WebScriptResponse resp) throws IOException {
		resp.getWriter().write(entityNodeRef.toString());
	}

	protected EntityProviderCallBack getEntityProviderCallback(WebScriptRequest req) {
		final String callBack = req.getParameter(PARAM_CALLBACK);
		final String user = req.getParameter(PARAM_CALLBACK_USER);
		final String password = req.getParameter(PARAM_CALLBACK_PASSWORD);
		
		if (callBack != null && callBack.length() > 0) {
			return new EntityProviderCallBack() {

				@Override
				public NodeRef provideNode(NodeRef nodeRef) throws BeCPGException {
					try {
						logger.debug("EntityProviderCallBack call : " + callBack + "?nodeRef=" + nodeRef.toString());

						if (user != null && user.length() > 0
								&& password != null && password.length() > 0){
							logger.debug("Set authentication for callback");
							Authenticator.setDefault (new Authenticator() {
							    protected PasswordAuthentication getPasswordAuthentication() {
							        return new PasswordAuthentication (user, password.toCharArray());
							    }
							});

						}  else {
							logger.debug("Set default authentication for callback");
							Authenticator.setDefault (new Authenticator() {
							    protected PasswordAuthentication getPasswordAuthentication() {
							        return new PasswordAuthentication ("admin", "becpg".toCharArray());
							    }
							});
						}
						
						URL  entityUrl =  new URL(callBack + "?nodeRef=" + nodeRef.toString());
						URL  dataUrl  =  new URL(callBack + "/data?nodeRef=" + nodeRef.toString());
						InputStream entityStream = null;
						InputStream dataStream = null;
						
						try {
							entityStream = entityUrl.openStream();
							 
							NodeRef entityNodeRef =  remoteEntityService.createOrUpdateEntity(nodeRef, entityStream, RemoteEntityFormat.xml, this);
							
							if(remoteEntityService.containsData(entityNodeRef)){
							
								dataStream = dataUrl.openStream();
								remoteEntityService.addOrUpdateEntityData(entityNodeRef, dataStream, RemoteEntityFormat.xml);
							
							}
							return entityNodeRef;
							
						} finally {
							IOUtils.closeQuietly(entityStream);
							IOUtils.closeQuietly(dataStream);
						}
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

	protected RemoteEntityFormat getFormat(WebScriptRequest req) {
		String format = req.getParameter(PARAM_FORMAT);
		if (format != null && RemoteEntityFormat.csv.toString().equals(format)) {
			return RemoteEntityFormat.csv;
		}
		return RemoteEntityFormat.xml;
	}
}
