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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.impl.HttpEntityProviderCallback;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Abstract remote entity webscript
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractEntityWebScript extends AbstractWebScript {

	/** Constant <code>logger</code> */
	protected static final Log logger = LogFactory.getLog(AbstractEntityWebScript.class);
	

	/** Constant <code>PARAM_QUERY="query"</code> */
	protected static final String PARAM_QUERY = "query";
	/** Constant <code>PARAM_PATH="path"</code> */
	protected static final String PARAM_PATH = "path";
	/** Constant <code>PARAM_FORMAT="format"</code> */
	protected static final String PARAM_FORMAT = "format";
	/** Constant <code>PARAM_NODEREF="nodeRef"</code> */
	protected static final String PARAM_NODEREF = "nodeRef";
	/** Constant <code>PARAM_ALL_VERSION="allVersion"</code> */
	protected static final String PARAM_ALL_VERSION = "allVersion";
	/** Constant <code>PARAM_FIELDS="fields"</code> */
	protected static final String PARAM_FIELDS = "fields";
	/** Constant <code>PARAM_LISTS="lists"</code> */
	protected static final String PARAM_LISTS = "lists";
	/** Constant <code>PARAM_EXCLUDE_SYSTEMS="excludeSystems"</code> */
	protected static final String PARAM_EXCLUDE_SYSTEMS = "excludeSystems";

	/** http://localhost:8080/alfresco/services/becpg/remote/entity **/
	protected static final String PARAM_CALLBACK = "callback";

	/**
	 * Callback auth admin:becpg
	 */
	protected static final String PARAM_CALLBACK_USER = "callbackUser";

	/** Constant <code>PARAM_CALLBACK_PASSWORD="callbackPassword"</code> */
	protected static final String PARAM_CALLBACK_PASSWORD = "callbackPassword";

	private static final String PARAM_MAX_RESULTS = "maxResults";

	/** Services **/

	protected NodeService nodeService;

	protected RemoteEntityService remoteEntityService;

	protected MimetypeService mimetypeService;

	protected PermissionService permissionService;
	
	protected NamespaceService namespaceService;
	
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>mimetypeService</code>.</p>
	 *
	 * @param mimetypeService a {@link org.alfresco.service.cmr.repository.MimetypeService} object.
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * <p>Setter for the field <code>remoteEntityService</code>.</p>
	 *
	 * @param remoteEntityService a {@link fr.becpg.repo.entity.remote.RemoteEntityService} object.
	 */
	public void setRemoteEntityService(RemoteEntityService remoteEntityService) {
		this.remoteEntityService = remoteEntityService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object.
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * <p>findEntities.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link java.util.List} object.
	 */
	protected List<NodeRef> findEntities(WebScriptRequest req) {

		String path = decodeParam(req.getParameter(PARAM_PATH));
		String query = decodeParam(req.getParameter(PARAM_QUERY));
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

		if ((query != null) && !query.toUpperCase().contains("TYPE")) {
			queryBuilder.ofType(BeCPGModel.TYPE_ENTITY_V2);
		}

		if ((req.getParameter(PARAM_ALL_VERSION) == null) || "false".equalsIgnoreCase(req.getParameter(PARAM_ALL_VERSION))) {
			if((req.getParameter(PARAM_EXCLUDE_SYSTEMS) == null) || "true".equalsIgnoreCase(req.getParameter(PARAM_EXCLUDE_SYSTEMS))){
				queryBuilder.excludeDefaults();
			} else {
				queryBuilder.excludeVersions();
			}
		} else {
			if((req.getParameter(PARAM_EXCLUDE_SYSTEMS) == null) || "true".equalsIgnoreCase(req.getParameter(PARAM_EXCLUDE_SYSTEMS))){
				queryBuilder.excludeSystems();
			}
		}

		if (maxResults == null) {
			queryBuilder.maxResults(RepoConsts.MAX_RESULTS_256);
		} else {
			queryBuilder.maxResults(maxResults);
		}

		if ((path != null) && (path.length() > 0)) {
			queryBuilder.inPath(path);
		}

		if ((query != null) && (query.length() > 0)) {
			queryBuilder.andFTSQuery(query);

		}

		List<NodeRef> refs = queryBuilder.inDBIfPossible().list();

		if ((refs != null) && !refs.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning " + refs.size() + " entities");
			}
			return refs;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("No entities found for query " + queryBuilder.toString());
		}
		return new ArrayList<>();

	}

	/**
	 * <p>findEntity.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected NodeRef findEntity(WebScriptRequest req) {

		String nodeRef = req.getParameter(PARAM_NODEREF);
		if ((nodeRef != null) && !nodeRef.isBlank()) {
			NodeRef node = new NodeRef(nodeRef);
			if (nodeService.exists(node)) {
				if (AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(node))) {
					return node;
				} else {
					throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
				}
			} else {
				throw new WebScriptException(Status.STATUS_NOT_FOUND ,"Node " + nodeRef + " doesn't exist in repository");
			}

		} else if (((req.getParameter(PARAM_PATH) == null) || req.getParameter(PARAM_PATH).isEmpty())
				&& ((req.getParameter(PARAM_QUERY) == null) || req.getParameter(PARAM_QUERY).isEmpty())) {
			throw new WebScriptException(Status.STATUS_NOT_IMPLEMENTED, "One of nodeRef query or path parameter is mandatory");
		}
		List<NodeRef> ret = findEntities(req);
		if ((ret != null) && !ret.isEmpty()) {
			return ret.get(0);
		}

		throw new WebScriptException(Status.STATUS_NOT_FOUND ,"No entity found for this parameters");
	}

	/**
	 * <p>sendOKStatus.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param resp a {@link org.springframework.extensions.webscripts.WebScriptResponse} object.
	 * @param format a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 * @throws java.io.IOException if any.
	 */
	protected void sendOKStatus(NodeRef entityNodeRef, WebScriptResponse resp, RemoteEntityFormat format) throws IOException {
		if ((resp != null) && (resp.getWriter() != null) && (entityNodeRef != null)) {
			if(RemoteEntityFormat.json.equals(format)) {
				JSONObject ret = new JSONObject();
				try {
					ret.put("nodeRef", entityNodeRef);
					ret.put("status", "SUCCESS");
	
					resp.setContentType("application/json");
					resp.setContentEncoding("UTF-8");
					ret.write(resp.getWriter());
				} catch (JSONException e) {
					logger.error(e,e);
				}
			} else {
				resp.getWriter().write(entityNodeRef.toString());
			}
		}
	}

	/**
	 * <p>getEntityProviderCallback.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link fr.becpg.repo.entity.remote.EntityProviderCallBack} object.
	 */
	protected EntityProviderCallBack getEntityProviderCallback(WebScriptRequest req) {

		String callBack = req.getParameter(PARAM_CALLBACK);
		String user = req.getParameter(PARAM_CALLBACK_USER) != null ? req.getParameter(PARAM_CALLBACK_USER) : "admin";
		String password = req.getParameter(PARAM_CALLBACK_PASSWORD) != null ? req.getParameter(PARAM_CALLBACK_PASSWORD) : "becpg";

		if ((callBack != null) && (callBack.length() > 0)) {
			return new HttpEntityProviderCallback(callBack, user, password, remoteEntityService);
		}
		logger.debug("No callback param provided");
		return null;
	}

	/**
	 * <p>getFormat.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link fr.becpg.repo.entity.remote.RemoteEntityFormat} object.
	 */
	protected RemoteEntityFormat getFormat(WebScriptRequest req) {
		String format = req.getParameter(PARAM_FORMAT);
		if ((format != null) && RemoteEntityFormat.csv.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.csv;
		} else if ((format != null) && RemoteEntityFormat.xml_excel.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.xml_excel;
		} else if ((format != null) && RemoteEntityFormat.xml_all.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.xml_all;
		} else if ((format != null) && RemoteEntityFormat.xml_light.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.xml_light;
		} else if ((format != null) && RemoteEntityFormat.json.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.json;
		} else if ((format != null) && RemoteEntityFormat.json_all.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.json_all;
		} else if ((format != null) && RemoteEntityFormat.json_schema.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.json_schema;	
		} else if ((format != null) && RemoteEntityFormat.xsd.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.xsd;
		} else if ((format != null) && RemoteEntityFormat.xsd_excel.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.xsd_excel;
		}
		return RemoteEntityFormat.xml;
	}

	/**
	 * <p>getContentType.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getContentType(WebScriptRequest req) {
		RemoteEntityFormat format = getFormat(req);
		if (RemoteEntityFormat.csv.equals(format)) {
			return "text/csv;charset=UTF-8";
		} else if (RemoteEntityFormat.json.equals(format)) {
			return "application/json;charset=UTF-8";
		 } else if (RemoteEntityFormat.json_schema.equals(format)) {
			return "application/schema+json;charset=UTF-8"; 
		} else {
			return "application/xml";
		}
	}

	/**
	 * <p>extractFields.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<String> extractFields(WebScriptRequest req) {
		Set<String> fields = new HashSet<>();
		String fieldsParams = req.getParameter(PARAM_FIELDS);
		if ((fieldsParams != null) && (fieldsParams.length() > 0)) {

			for (String field : decodeParam(fieldsParams).split(",")) {
				fields.add(field);
				if (field.contains("|")) {
					fields.add(field.split("\\|")[0]);
				}
			}
		}
		return new ArrayList<>(fields);
	}

	/**
	 * <p>extractLists.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<String> extractLists(WebScriptRequest req) {
		List<String> lists = new ArrayList<>();
		String listsParams = req.getParameter(PARAM_LISTS);
		
		if ((listsParams != null) && (!listsParams.isEmpty())) {

			String[] splitted = decodeParam(listsParams).split(",");
			for (String list : splitted) {
				String[] listName = list.split(":");
				if ((listName != null) ) {
					if(listName.length > 1){
						if(listName[0].startsWith("!")) {
							lists.add("!"+listName[1]);
						} else {
							lists.add(listName[1]);
						}
					} else {
						lists.add(listName[0]);
					}
				}
			}
		}
		return lists;
	}


	//TODO move that to CompressParamHelper in becpg-tools
	private static final String BASE_64_PREFIX = "b64-";
	
	private static final Map<String,String> replacementMaps = new HashMap<>();
	{
		replacementMaps.put("bcpg:", "§");
	}
	
	
	protected static  String decodeParam(String param)   {
		if ((param != null) && param.startsWith(BASE_64_PREFIX) ) {
		     try {
				 Inflater decompresser = new Inflater();
				 byte[] compressedData = Base64.decodeBase64(param.replaceFirst(BASE_64_PREFIX, ""));
			     decompresser.setInput(compressedData, 0, compressedData.length);
			     byte[] output = new byte[100000];
			     
			     int decompressedDataLength = decompresser.inflate(output);
			     decompresser.end();
			     String ret = (new String(output, 0, decompressedDataLength, StandardCharsets.UTF_8));
			     for(Entry<String,String> entry: replacementMaps.entrySet()) {
			    	 ret = ret.replaceAll( entry.getValue(), entry.getKey());
					}
			     
		    	 return ret;
			} catch (DataFormatException e) {
				logger.error("Error decoding param: "+ param,e);
			}
		}
		return param;
	}

}
