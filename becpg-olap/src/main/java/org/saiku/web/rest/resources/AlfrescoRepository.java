/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package org.saiku.web.rest.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.saiku.web.rest.objects.acl.AclEntry;
import org.saiku.web.rest.objects.acl.enumeration.AclMethod;
import org.saiku.web.rest.objects.repository.IRepositoryObject;
import org.saiku.web.rest.objects.repository.RepositoryFileObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import fr.becpg.olap.authentication.AlfrescoUserDetails;
import fr.becpg.olap.http.DeleteQueryCommand;
import fr.becpg.olap.http.DownloadQueryCommand;
import fr.becpg.olap.http.ListQueriesCommand;
import fr.becpg.olap.http.UploadQueryCommand;
import fr.becpg.tools.InstanceManager;
import fr.becpg.tools.InstanceManager.Instance;

/**
 * 
 * @author matthieu
 * 
 */
@Component
@Path("/saiku/{username}/alfrepository")
@XmlAccessorType(XmlAccessType.NONE)
public class AlfrescoRepository {

	private static final Log log = LogFactory.getLog(AlfrescoRepository.class);

	
	InstanceManager instanceManager;

	public void setInstanceManager(InstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

	public interface AlfrescoSessionCallBack<T> {
		public T execute(Instance instance, HttpClient httpClient);
	}

	private <T> T runInAlfrescoSession(AlfrescoSessionCallBack<T> alfrescoSessionCallBack) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.getPrincipal() != null && (auth.getPrincipal() instanceof AlfrescoUserDetails)) {
			Instance instance = ((AlfrescoUserDetails) auth.getPrincipal()).getInstance();
			HttpClient httpClient = instanceManager.createInstanceSession(instance);
			try {
				return alfrescoSessionCallBack.execute(instance, httpClient);
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
			
		}

		return null;
	}

	public class QueryList {

		private String parentNodeRef;
		Map<String, String> queries = new HashMap<String, String>();

		public String getParentNodeRef() {
			return parentNodeRef;
		}

		public String getQueryNodeRef(String fileName) {
			return queries.get(fileName);
		}

		public List<IRepositoryObject> getRepositoryObjects() {	
			List<AclMethod> aclMethods = new ArrayList<AclMethod>();
				aclMethods.add(AclMethod.READ);
				aclMethods.add(AclMethod.WRITE);
			List<IRepositoryObject> objects = new ArrayList<IRepositoryObject>();
			for (String filename : queries.keySet()) {

				objects.add(new RepositoryFileObject(filename, "#"+filename, "saiku", filename+".saiku", aclMethods));
			}

			return objects;
		}

		public void load(InputStream in) throws JsonParseException, IOException {
			queries.clear();
			JsonFactory jsonFactory = new JsonFactory();
			JsonParser jp = jsonFactory.createJsonParser(in);

			ObjectMapper mapper = new ObjectMapper();

			JsonNode rootNode = mapper.readTree(jp);

			this.parentNodeRef = rootNode.path("metadata").path("olapQueriesFolder").getTextValue();

			for (JsonNode query : rootNode.path("queries")) {
				String filename = query.path("queryName").getTextValue();
				String nodeRef = query.path("noderef").getTextValue();
				queries.put(filename, nodeRef);

			}
		}
	}

	/**
	 * Get Saved Queries.
	 * 
	 * @return A list of SavedQuery Objects.
	 */
	@GET
	@Produces({ "application/json" })
	public List<IRepositoryObject> getRepository(@QueryParam("path") String path, @QueryParam("type") String type) {

		checkFileName(path);

		return runInAlfrescoSession(new AlfrescoSessionCallBack<List<IRepositoryObject>>() {

			public List<IRepositoryObject> execute(Instance instance, HttpClient httpClient) {

				try {

					QueryList queryList = retrieveQueries(instance, httpClient);
					if (queryList != null) {
						return queryList.getRepositoryObjects();
					}

				} catch (Exception e) {
					log.error(this.getClass().getName(), e);
				}
				return new ArrayList<IRepositoryObject>();

			}
		});

	}

	private QueryList retrieveQueries(Instance instance, HttpClient httpClient) throws IOException {
		QueryList queryList = null;
		ListQueriesCommand listQueriesCommand = new ListQueriesCommand(instance.getInstanceUrl());
		try (InputStream in = listQueriesCommand.runCommand(httpClient)) {
			queryList = new QueryList();
			queryList.load(in);

		}
		return queryList;
	}

	private void checkFileName(String path) {
		if (path != null && (path.startsWith("/") || path.startsWith("."))) {
			throw new IllegalArgumentException("Path cannot be null or start with \"/\" or \".\" - Illegal Path: " + path);
		}

	}

	@GET
	@Produces({ "application/json" })
	@Path("/resource/acl")
	public AclEntry getResourceAcl(@QueryParam("file") String file) {
		throw new IllegalStateException("Not implemented");
	}

	@POST
	@Produces({ "application/json" })
	@Path("/resource/acl")
	public Response setResourceAcl(@FormParam("file") String file, @FormParam("acl") String aclEntry) {
		throw new IllegalStateException("Not implemented");
	}

	/**
	 * Load a resource.
	 * 
	 * @param file
	 *            - The name of the repository file to load.
	 * @param path
	 *            - The path of the given file to load.
	 * @return A Repository File Object.
	 */ 
	@GET
	@Produces({ "text/plain" })
	@Path("/resource")
	public Response getResource(@QueryParam("file") final String file) {

		checkFileName(file);

		return runInAlfrescoSession(new AlfrescoSessionCallBack<Response>() {

			public Response execute(Instance instance, HttpClient httpClient) {
				try {
					QueryList queryList = retrieveQueries(instance, httpClient);
					if (queryList != null) {

						String nodeRef = queryList.getQueryNodeRef(file.replace(".saiku", ""));
						if (nodeRef != null) {

							DownloadQueryCommand downloadQueryCommand = new DownloadQueryCommand(instance.getInstanceUrl());

							try (InputStreamReader reader = new InputStreamReader(downloadQueryCommand.runCommand(httpClient, nodeRef,file))) {

								BufferedReader br = new BufferedReader(reader);
								String chunk = "", content = "";
								while ((chunk = br.readLine()) != null) {
									content += chunk + "\n";
								}
								byte[] doc = content.getBytes("UTF-8");
								return Response.ok(doc, MediaType.TEXT_PLAIN).header("content-length", doc.length).build();

							}

						}
					}

				} catch (Exception e) {
					log.error("Cannot load query (" + file + ")", e);
				}
				return Response.serverError().build();
			}
		});

	}

	/**
	 * Save a resource.
	 * 
	 * @param file
	 *            - The name of the repository file to load.
	 * @param path
	 *            - The path of the given file to load.
	 * @param content
	 *            - The content to save.
	 * @return Status
	 */
	@POST
	@Path("/resource")
	public Response saveResource(@FormParam("file") final String file, @FormParam("content") final String content) {

		checkFileName(file);

		return runInAlfrescoSession(new AlfrescoSessionCallBack<Response>() {

			public Response execute(Instance instance, HttpClient httpClient) {
				try {
					QueryList queryList = retrieveQueries(instance, httpClient);
					if (queryList != null) {

						String nodeRef = queryList.getQueryNodeRef(file.replace(".saiku", ""));
						UploadQueryCommand uploadQueryCommand = new UploadQueryCommand(instance.getInstanceUrl());
						if (nodeRef != null) {

							try (InputStream in = uploadQueryCommand.runCommand(httpClient, nodeRef, content)) {
								
								if(log.isDebugEnabled()){
									IOUtils.copy(in, System.out);
								}
								
								return Response.ok().build();
							}

						} else {
							try (InputStream in = uploadQueryCommand.runCommand(httpClient, queryList.getParentNodeRef(), file.replace(".saiku", "")+".saiku", content)) {
								
								
								if(log.isDebugEnabled()){
									IOUtils.copy(in, System.out);
								}
								
								return Response.ok().build();
							}

						}
					}

				} catch (Exception e) {
					log.error("Cannot save resource to ( file: " + file + ")", e);
				}
				return Response.serverError().entity("Cannot save resource to ( file: " + file + ")").type("text/plain").build();
			}
		});

	}

	/**
	 * Delete a resource.
	 * 
	 * @param file
	 *            - The name of the repository file to load.
	 * @param path
	 *            - The path of the given file to load.
	 * @return Status
	 */
	@DELETE
	@Path("/resource")
	public Response deleteResource(@QueryParam("file") final String file) {

		checkFileName(file);

		return runInAlfrescoSession(new AlfrescoSessionCallBack<Response>() {

			public Response execute(Instance instance, HttpClient httpClient) {
				try {
					QueryList queryList = retrieveQueries(instance, httpClient);
					if (queryList != null) {

						String nodeRef = queryList.getQueryNodeRef(file.replace(".saiku", ""));

						DeleteQueryCommand deleteQueryCommand = new DeleteQueryCommand(instance.getInstanceUrl());
						if (nodeRef != null) {
							try (InputStream in = deleteQueryCommand.runCommand(httpClient, nodeRef)) {
								return Response.ok().build();
							}
						}
					}

				} catch (Exception e) {
					log.error("Cannot save resource to ( file: " + file + ")", e);
				}
				return Response.serverError().build();
			}
		});

	}

}
