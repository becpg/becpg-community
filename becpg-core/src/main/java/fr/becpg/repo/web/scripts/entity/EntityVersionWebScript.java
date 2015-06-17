/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.version.EntityVersion;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.web.scripts.AbstractCachingWebscript;

/**
 * The Class VersionHistoryWebScript.
 *
 * @author querephi
 */
public class EntityVersionWebScript extends AbstractCachingWebscript {

	// request parameter names
	private static final String PARAM_NODEREF = "nodeRef";
	private static final String DISPLAY_FORMAT = "dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'";
	private static final String PARAM_MODE = "mode";

	private static final Log logger = LogFactory.getLog(EntityVersionWebScript.class);

	private EntityVersionService entityVersionService;

	private NodeService nodeService;

	private PersonService personService;

	private AttributeExtractorService attributeExtractorService;

	private ServiceRegistry serviceRegistry;

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * Get entity version history.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		logger.debug("VersionWebScript executeImpl()");
		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));
		String mode = req.getParameter(PARAM_MODE);
		SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_FORMAT);

		Date lastModified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);

		if (lastModified != null && shouldReturnNotModified(req, lastModified)) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			if (logger.isDebugEnabled()) {
				logger.debug("Send Not_MODIFIED status");
			}
			return;
		}

		if (lastModified == null) {
			lastModified = new Date();
		}

		Cache cache = new Cache(getDescription().getRequiredCache());
		cache.setIsPublic(false);
		cache.setMustRevalidate(true);
		cache.setNeverCache(false);
		cache.setMaxAge(0L);
		cache.setLastModified(lastModified);
		res.setCache(cache);

		List<EntityVersion> versions;
		if ("graph".equals(mode)) {
			versions = entityVersionService.getAllVersionAndBranches(nodeRef);
		} else {
			versions = entityVersionService.getAllVersions(nodeRef);
		}

		try {
			JSONArray jsonVersions = new JSONArray();

			if (versions.isEmpty()) {

				JSONObject jsonVersion = new JSONObject();
				jsonVersion.put("nodeRef", nodeRef);
				jsonVersion.put("name", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
				jsonVersion.put("label", RepoConsts.INITIAL_VERSION);
				jsonVersion.put("description", "");
				jsonVersion.put("createdDate", displayFormat.format((Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED)));
				jsonVersion.put("createdDateISO", ISO8601DateFormat.format((Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED)));
				jsonVersion.put("creator", getPerson((String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR)));
				jsonVersions.put(jsonVersion);

			} else {
				for (EntityVersion version : versions) {

					JSONObject jsonVersion = new JSONObject();
					// Version info
					jsonVersion.put("nodeRef", version.getEntityVersionNodeRef());
					jsonVersion.put("name", nodeService.getProperty(version.getFrozenStateNodeRef(), ContentModel.PROP_NAME));
					jsonVersion.put("label", version.getVersionLabel());
					jsonVersion.put("description", version.getDescription());

					Date createdDate = version.getFrozenModifiedDate();

					jsonVersion.put("createdDate", displayFormat.format(createdDate));
					jsonVersion.put("createdDateISO", ISO8601DateFormat.format(createdDate));
					jsonVersion.put("creator", getPerson(version.getFrozenModifier()));

					QName itemType = nodeService.getType(version.getFrozenStateNodeRef());
					// Branch info
					jsonVersion.put("entityNodeRef", version.getEntityNodeRef());
					jsonVersion.put("entityFromBranch", version.getEntityBranchFromNodeRef());
					jsonVersion.put("metadata", attributeExtractorService.extractMetadata(itemType, version.getEntityNodeRef()));
					jsonVersion.put("siteId", attributeExtractorService.extractSiteId(version.getEntityNodeRef()));
					jsonVersion.put("itemType", itemType.toPrefixString(serviceRegistry.getNamespaceService()));

					jsonVersions.put(jsonVersion);
				}
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");

			if ("branches".equals(mode)) {

				JSONArray jsonBranches = new JSONArray();
				List<NodeRef> branches = entityVersionService.getAllVersionBranches(nodeRef);
				for (NodeRef branchNodeRef : branches) {
					JSONObject jsonBranch = new JSONObject();
					jsonBranch.put("nodeRef", branchNodeRef);
					jsonBranch.put("name", nodeService.getProperty(branchNodeRef, ContentModel.PROP_NAME));
					if (nodeService.hasAspect(branchNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
						jsonBranch.put("label", nodeService.getProperty(branchNodeRef, ContentModel.PROP_VERSION_LABEL));
					} else {
						jsonBranch.put("label", RepoConsts.INITIAL_VERSION);
					}

					jsonBranch.put("description", nodeService.getProperty(branchNodeRef, ContentModel.PROP_DESCRIPTION));
					jsonBranch.put("createdDate", displayFormat.format((Date) nodeService.getProperty(branchNodeRef, ContentModel.PROP_CREATED)));
					jsonBranch.put("createdDateISO",
							ISO8601DateFormat.format((Date) nodeService.getProperty(branchNodeRef, ContentModel.PROP_CREATED)));
					jsonBranch.put("metadata", attributeExtractorService.extractMetadata(nodeService.getType(branchNodeRef), branchNodeRef));
					jsonBranch.put("siteId", attributeExtractorService.extractSiteId(branchNodeRef));
					jsonBranch.put("itemType", nodeService.getType(branchNodeRef).toPrefixString(serviceRegistry.getNamespaceService()));
					jsonBranches.put(jsonBranch);

					jsonBranch.put("creator", getPerson((String) nodeService.getProperty(branchNodeRef, ContentModel.PROP_CREATOR)));

				}

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("versions", jsonVersions);
				jsonObject.put("branches", jsonBranches);
				res.getWriter().write(jsonObject.toString(3));

			} else {
				res.getWriter().write(jsonVersions.toString(3));
			}

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON");
		}

	}

	private JSONObject getPerson(String frozenModifier) throws InvalidNodeRefException, JSONException {
		JSONObject jsonCreator = new JSONObject();
		try {
			NodeRef creatorNodeRef = personService.getPerson(frozenModifier);
			jsonCreator.put("userName", nodeService.getProperty(creatorNodeRef, ContentModel.PROP_USERNAME));
			jsonCreator.put("firstName", nodeService.getProperty(creatorNodeRef, ContentModel.PROP_FIRSTNAME));
			jsonCreator.put("lastName", nodeService.getProperty(creatorNodeRef, ContentModel.PROP_LASTNAME));
		} catch (NoSuchPersonException e) {
			logger.debug("Person doesn't exist : " + frozenModifier);
			jsonCreator.put("userName", frozenModifier);
			jsonCreator.put("firstName", frozenModifier);
			jsonCreator.put("lastName", frozenModifier);
		}
		return jsonCreator;
	}
}
