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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingResults;
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
import fr.becpg.repo.entity.remote.RemoteRateLimiter;
import fr.becpg.repo.entity.remote.impl.HttpEntityProviderCallback;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Abstract remote entity webscript
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractEntityWebScript extends AbstractWebScript {

	/** Constant <code>logger</code> */
	protected static final Log logger = LogFactory.getLog(AbstractEntityWebScript.class);
	


	/** Constant <code>JSON_PARAM="jsonParam"</code> */
	protected static final String JSON_PARAM = "jsonParam";
	
	/** Constant <code>PARAM_TYPE="type"</code> */
	protected static final String PARAM_TYPE = "type";
	
	/** Constant <code>PARAM_PARAMS="params"</code> */
	protected static final String PARAM_PARAMS = "params";
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

	/** Constant <code>PARAM_MAX_RESULTS="maxResults"</code> */
	protected static final String PARAM_MAX_RESULTS = "maxResults";
	
	/** Constant <code>PARAM_PAGE="page"</code> */
	protected static final String PARAM_PAGE = "page";

	/** Services **/

	protected NodeService nodeService;

	protected RemoteEntityService remoteEntityService;

	protected MimetypeService mimetypeService;

	protected PermissionService permissionService;
	
	protected NamespaceService namespaceService;
	
	protected RemoteRateLimiter remoteRateLimiter;
	
	protected SystemConfigurationService systemConfigurationService;

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
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
	 * <p>Setter for the field <code>remoteRateLimiter</code>.</p>
	 *
	 * @param remoteRateLimiter a {@link fr.becpg.repo.entity.remote.RemoteRateLimiter} object
	 */
	public void setRemoteRateLimiter(RemoteRateLimiter remoteRateLimiter) {
		this.remoteRateLimiter = remoteRateLimiter;
	}

	

	private  Integer maxResultsLimit() {
		return Integer.parseInt(systemConfigurationService.confValue("beCPG.remote.maxResults.limit"));
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		 if (!remoteRateLimiter.allowRequest()) {
			 throw new WebScriptException("beCPG Remote API Call RATE limit reached");
	      }
		 executeInternal(req,resp);
	}
	
	/**
	 * <p>executeInternal.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @param resp a {@link org.springframework.extensions.webscripts.WebScriptResponse} object
	 * @throws java.io.IOException if any.
	 */
	protected abstract void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException;

	/**
	 * <p>findEntities.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link java.util.List} object.
	 * @param limit a {@link java.lang.Boolean} object
	 */
	protected PagingResults<NodeRef> findEntities(WebScriptRequest req, Boolean limit) {

		String path = decodeParam(req.getParameter(PARAM_PATH));
		String query = decodeParam(req.getParameter(PARAM_QUERY));

		Integer maxResults = intParam(req, PARAM_MAX_RESULTS);
		Integer page = intParam(req, PARAM_PAGE);
		
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

		if (maxResults == null || Boolean.TRUE.equals(limit)) {
			queryBuilder.maxResults(Boolean.TRUE.equals(limit) ?  maxResultsLimit() :  RepoConsts.MAX_RESULTS_256);
		} else {
			queryBuilder.maxResults(maxResults);
		}
		
		if (page != null ) {
			queryBuilder.page(page);
		}

		
		if ((path != null) && (path.length() > 0)) {
			queryBuilder.inPath(path);
		}

		if ((query != null) && (query.length() > 0)) {
			queryBuilder.andFTSQuery(query);

		}

		PagingResults<NodeRef> refs = queryBuilder.inDBIfPossible().pagingResults();

		if ((refs != null) ) {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning " + refs.getTotalResultCount() + " entities");
			}
			return refs;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("No entities found for query " + queryBuilder.toString());
		}
		return new EmptyPagingResults<>();

	}

	/**
	 * <p>intParam.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @param paramName a {@link java.lang.String} object
	 * @return a {@link java.lang.Integer} object
	 */
	protected Integer intParam(WebScriptRequest req,  String paramName) {
		String paramString = req.getParameter(paramName);

		Integer ret = null;
		if (paramString != null) {
			try {
				ret = Integer.parseInt(paramString);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse "+paramName+" argument", e);
			}
		}
		
		return ret;
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
		PagingResults<NodeRef> ret = findEntities(req, true);
		if ((ret != null) && !ret.getPage().isEmpty()) {
			return ret.getPage().get(0);
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
					ret.put(PARAM_NODEREF, entityNodeRef);
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
		} else if (RemoteEntityFormat.json.equals(format) || RemoteEntityFormat.json_all.equals(format)) {
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
	public Set<String> extractFields(WebScriptRequest req) {
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
		return fields;
	}

	/**
	 * <p>extractLists.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @return a {@link java.util.List} object.
	 */
	public Set<String> extractLists(WebScriptRequest req) {
		Set<String> lists = new HashSet<>();
		String listsParams = req.getParameter(PARAM_LISTS);
		
		if ((listsParams != null) && (!listsParams.isEmpty())) {

			String[] splitted = decodeParam(listsParams).split(",");
			for (String list : splitted) {
				
				String listToAdd = "";
				if (list.contains("@")) {
					String firstPart = list.split("@")[0];
					String secondPart = list.split("@")[1];
					listToAdd = formatListName(firstPart) + "@" + secondPart;
				} else {
					listToAdd = formatListName(list);
				}
				lists.add(listToAdd);
			}
		}
		return lists;
	}

	private String formatListName(String list) {
		String[] listName = list.split(":");
		if (listName.length > 1) {
			if (listName[0].startsWith("!")) {
				return "!" + listName[1];
			}
			return listName[1];
		}
		return listName[0];
	}

	private static final String BASE_64_PREFIX = "b64-";
	
	private static final Map<String,String> replacementMaps = new HashMap<>();
	{
		replacementMaps.put("bcpg:", "§");
	}
	
	
	/**
	 * <p>decodeParam.</p>
	 *
	 * @param param a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
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
	
	/**
	 * <p>extractParams.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @return a {@link org.json.JSONObject} object
	 */
	protected JSONObject extractParams(WebScriptRequest req) {

		JSONObject jsonParams = null;
		
		String params = req.getParameter(PARAM_PARAMS);
		if ((params != null) && !params.isEmpty()) {

			try {
				jsonParams = new JSONObject(params);
			} catch (JSONException e) {
				logger.error("Cannot parse params:" + params);
			}
		}
		
		for (String parameterName : req.getParameterNames()) {
			
			String jsonParamName = extractJsonParamName(parameterName);
			if (jsonParamName != null && !jsonParamName.isBlank()) {
				if (jsonParams == null) {
					jsonParams = new JSONObject();
				}
				jsonParams.put(jsonParamName, JSONObject.stringToValue(req.getParameter(parameterName)));
			}
		}
		
		return jsonParams;
	}

	/**
	 * <p>extractJsonParamName.</p>
	 *
	 * @param parameterName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	protected String extractJsonParamName(String parameterName) {
		if (parameterName.startsWith(JSON_PARAM)) {
			String[] split = parameterName.split(JSON_PARAM);
			
			if (split.length > 1) {
				String jsonParamName = split[1];
				char[] c = jsonParamName.toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				return new String(c);
			}
		}
		
		return null;
	}
	

}
