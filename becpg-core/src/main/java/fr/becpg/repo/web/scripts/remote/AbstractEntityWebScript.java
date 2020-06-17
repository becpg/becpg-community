/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
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
 *
 */
public abstract class AbstractEntityWebScript extends AbstractWebScript {

	protected static final Log logger = LogFactory.getLog(AbstractEntityWebScript.class);

	protected static final String PARAM_QUERY = "query";
	protected static final String PARAM_PATH = "path";
	protected static final String PARAM_FORMAT = "format";
	protected static final String PARAM_NODEREF = "nodeRef";
	protected static final String PARAM_ALL_VERSION = "allVersion";
	protected static final String PARAM_FIELDS = "fields";
	protected static final String PARAM_LISTS = "lists";

	/** http://localhost:8080/alfresco/services/becpg/remote/entity **/
	protected static final String PARAM_CALLBACK = "callback";

	/**
	 * Callback auth admin:becpg
	 */
	protected static final String PARAM_CALLBACK_USER = "callbackUser";

	protected static final String PARAM_CALLBACK_PASSWORD = "callbackPassword";

	private static final String PARAM_MAX_RESULTS = "maxResults";

	/** Services **/

	protected NodeService nodeService;

	protected RemoteEntityService remoteEntityService;

	protected MimetypeService mimetypeService;

	protected PermissionService permissionService;

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setRemoteEntityService(RemoteEntityService remoteEntityService) {
		this.remoteEntityService = remoteEntityService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

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
			queryBuilder.excludeDefaults();
		} else {
			queryBuilder.excludeSystems();
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
			logger.info("Returning " + refs.size() + " entities");

			return refs;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("No entities found for query " + queryBuilder.toString());
		}
		return new ArrayList<>();

	}

	protected NodeRef findEntity(WebScriptRequest req) {

		String nodeRef = req.getParameter(PARAM_NODEREF);
		if ((nodeRef != null) && (nodeRef.length() > 0)) {
			NodeRef node = new NodeRef(nodeRef);
			if (nodeService.exists(node)) {
				if (AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(node))) {
					return node;
				} else {
					throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
				}
			} else {
				throw new WebScriptException("Node " + nodeRef + " doesn't exist in repository");
			}

		} else if (((req.getParameter(PARAM_PATH) == null) || req.getParameter(PARAM_PATH).isEmpty())
				&& ((req.getParameter(PARAM_QUERY) == null) || req.getParameter(PARAM_QUERY).isEmpty())) {
			throw new IllegalStateException("One of nodeRef query or path parameter is mandatory");
		}
		List<NodeRef> ret = findEntities(req);
		if ((ret != null) && !ret.isEmpty()) {
			return ret.get(0);
		}

		throw new IllegalStateException("No entity found for this parameters");
	}

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
		} else if ((format != null) && RemoteEntityFormat.xsd.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.xsd;
		} else if ((format != null) && RemoteEntityFormat.xsd_excel.toString().equalsIgnoreCase(format)) {
			return RemoteEntityFormat.xsd_excel;
		}
		return RemoteEntityFormat.xml;
	}

	protected String getContentType(WebScriptRequest req) {
		RemoteEntityFormat format = getFormat(req);
		if (RemoteEntityFormat.csv.equals(format)) {
			return "text/csv;charset=UTF-8";
		} else if (RemoteEntityFormat.json.equals(format)) {
			return "application/json;charset=UTF-8";

		} else {
			return "application/xml";
		}
	}

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

	public List<String> extractLists(WebScriptRequest req) {
		List<String> lists = new ArrayList<>();
		String listsParams = req.getParameter(PARAM_LISTS);
		if ((listsParams != null) && (listsParams.length() > 0)) {

			String[] splitted = decodeParam(listsParams).split(",");
			for (String list : splitted) {
				String[] listName = list.split(":");
				if ((listName != null) && (listName.length > 1)) {
					lists.add(listName[1]);
				}
			}
		}
		return lists;
	}

	private String decodeParam(String param)   {
		if ((param != null) && Base64.isBase64(param)) {
		     try {
				 Inflater decompresser = new Inflater();
				 byte[] compressedData = Base64.decodeBase64(param);
			     decompresser.setInput(compressedData, 0, compressedData.length);
			     byte[] output = new byte[100000];
			     
			     int decompressedDataLength = decompresser.inflate(output);
			     decompresser.end();

		    	 return (new String(output, 0, decompressedDataLength, "UTF-8")).replaceAll("=", "bcpg:");
			} catch (DataFormatException | UnsupportedEncodingException e) {
				logger.error(e,e);
			}
		}
		return param;
	}

}
