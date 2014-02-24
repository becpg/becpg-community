/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

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

	private static final String PARAM_MAX_RESULTS = "maxResults";
	
	/** Services **/

	protected NodeService nodeService;

	protected RemoteEntityService remoteEntityService;


	protected MimetypeService mimetypeService;

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
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
		String maxResultsString = req.getParameter(PARAM_MAX_RESULTS);
		
		Integer maxResults = null;
		if (maxResultsString != null) {
			try {
				maxResults = Integer.parseInt(maxResultsString);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse page argument", e);
			}
		}
		
		
		
		
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();
		
		queryBuilder.ofType(BeCPGModel.TYPE_ENTITY_V2).excludeDefaults();
		
		if(maxResults==null){
			queryBuilder.maxResults(RepoConsts.MAX_RESULTS_256);
		} else {
			queryBuilder.maxResults(maxResults);
		}
		
		if (path != null && path.length() > 0) {
			queryBuilder.inPath(path);
		}
		
		
		if (query != null && query.length() > 0) {
			queryBuilder.andFTSQuery(query);
		
		}
		
		List<NodeRef> refs = queryBuilder.list();
		
	
		
		
		if (refs!=null && !refs.isEmpty()) {
			logger.info("Returning "+refs.size()+" entities");
			
			return refs;
		}
		
		logger.info("No entities found for query " + queryBuilder.toString());
		return new ArrayList<NodeRef>();
		
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
		final String user = req.getParameter(PARAM_CALLBACK_USER)!=null ?  req.getParameter(PARAM_CALLBACK_USER) : "admin";
		final String password = req.getParameter(PARAM_CALLBACK_PASSWORD)!=null ?  req.getParameter(PARAM_CALLBACK_PASSWORD) : "becpg";
		
		if (callBack != null && callBack.length() > 0) {
			return new EntityProviderCallBack() {

				@Override
				public NodeRef provideNode(NodeRef nodeRef) throws BeCPGException {
					try {
						

						String url = callBack + "?nodeRef=" + nodeRef.toString();
						
						HttpClient httpClient = new DefaultHttpClient();
						
						Header authHeader  = BasicScheme.authenticate(
									 new UsernamePasswordCredentials(user, password),
									 "UTF-8", false);

						HttpGet entityUrl = new HttpGet(url);
						
						entityUrl.addHeader(authHeader);
			
						
						InputStream entityStream = null;
						InputStream dataStream = null;
						
						try {
							logger.debug("Try getting nodeRef  from : " +url); 
							
							HttpResponse httpResponse = httpClient.execute(entityUrl);
							HttpEntity responseEntity = httpResponse.getEntity();
							
							
							entityStream = responseEntity.getContent();
							 
							NodeRef entityNodeRef =  remoteEntityService.createOrUpdateEntity(nodeRef, entityStream, RemoteEntityFormat.xml, this);
							
							if(remoteEntityService.containsData(entityNodeRef)){
							
								
								HttpGet dataUrl = new HttpGet(callBack + "/data?nodeRef=" + nodeRef.toString());
								dataUrl.addHeader(authHeader);
								httpResponse = httpClient.execute(dataUrl);
								responseEntity = httpResponse.getEntity();
								dataStream = responseEntity.getContent();
								remoteEntityService.addOrUpdateEntityData(entityNodeRef, dataStream, RemoteEntityFormat.xml);
							
							}
							
							logger.debug("Getting node success for :"+url);
							
							return entityNodeRef;
							
						} finally {
							IOUtils.closeQuietly(entityStream);
							IOUtils.closeQuietly(dataStream);
						}
					
						
					} catch (MalformedURLException e) {
						throw new BeCPGException(e);
					} catch (IOException e) {
						throw new BeCPGException(e);
					}catch (Exception e) {
						logger.error("Cannot import nodeRef: "+nodeRef,e);
						throw e;
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
