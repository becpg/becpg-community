/*
 *
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.version.EntityVersion;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * The Class VersionHistoryWebScript.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class EntityVersionWebScript extends AbstractWebScript {

	// request parameter names
	private static final String PARAM_NODEREF = "nodeRef";
	private static final String DISPLAY_FORMAT = "dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'";
	private static final String PARAM_MODE = "mode";

	private static final int MAX_DESCRIPTION_LENGTH = 200;

	private static final Log logger = LogFactory.getLog(EntityVersionWebScript.class);

	private EntityVersionService entityVersionService;

	private NodeService nodeService;

	private PersonService personService;

	private AttributeExtractorService attributeExtractorService;

	private ServiceRegistry serviceRegistry;

	private VersionService versionService;

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
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
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object.
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>versionService</code>.</p>
	 *
	 * @param versionService a {@link org.alfresco.service.cmr.version.VersionService} object
	 */
	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get entity version history.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));

		String mode = req.getParameter(PARAM_MODE);
		SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_FORMAT);

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

				Serializable manualVersionLabel = nodeService.getProperty(nodeRef, BeCPGModel.PROP_MANUAL_VERSION_LABEL);

				if (manualVersionLabel instanceof String && !((String) manualVersionLabel).isBlank()) {
					jsonVersion.put("label", manualVersionLabel);
				} else {
					jsonVersion.put("label", RepoConsts.INITIAL_VERSION);
				}

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

					String name = (String) nodeService.getProperty(version.getFrozenStateNodeRef(), ContentModel.PROP_NAME);

					if (name.endsWith(RepoConsts.VERSION_NAME_DELIMITER + version.getVersionLabel())) {
						name = name.replace(RepoConsts.VERSION_NAME_DELIMITER + version.getVersionLabel(), "");
					}

					jsonVersion.put("name", name);

					VersionHistory versionHistory = versionService.getVersionHistory(version.getEntityNodeRef());

					NodeRef headVersionNodeRef = null;

					if (versionHistory != null) {
						Version headVersion = versionService.getVersionHistory(version.getEntityNodeRef()).getHeadVersion();
						headVersionNodeRef = headVersion.getFrozenStateNodeRef();
					}

					boolean isHeadVersion = headVersionNodeRef == null || version.getFrozenStateNodeRef().equals(headVersionNodeRef);

					Serializable manualVersionLabel = null;

					if (isHeadVersion) {
						manualVersionLabel = nodeService.getProperty(version.getEntityNodeRef(), BeCPGModel.PROP_MANUAL_VERSION_LABEL);
					}

					if (manualVersionLabel instanceof String && !((String) manualVersionLabel).isBlank()) {
						jsonVersion.put("label", manualVersionLabel);
					} else {
						jsonVersion.put("label", version.getVersionLabel());
					}

					String description = version.getDescription();

					if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
						description = description.substring(0, MAX_DESCRIPTION_LENGTH) + " ...";
					}

					jsonVersion.put("description", description);

					Date createdDate = version.getFrozenModifiedDate();

					jsonVersion.put("createdDate", displayFormat.format(createdDate));
					jsonVersion.put("createdDateISO", ISO8601DateFormat.format(createdDate));
					jsonVersion.put("creator",
							getPerson((String) nodeService.getProperty(version.getEntityVersionNodeRef(), ContentModel.PROP_CREATOR)));

					QName itemType = nodeService.getType(version.getFrozenStateNodeRef());
					// Branch info
					jsonVersion.put("entityNodeRef", version.getEntityNodeRef());
					jsonVersion.put("entityFromBranch", version.getEntityBranchFromNodeRef());
					jsonVersion.put("metadata", attributeExtractorService.extractMetadata(itemType, version.getEntityNodeRef()));
					jsonVersion.put("siteId", attributeExtractorService.extractSiteId(version.getEntityNodeRef()));
					jsonVersion.put("itemType", itemType.toPrefixString(serviceRegistry.getNamespaceService()));

					String referenceLabel = (String) nodeService.getProperty(version.getEntityNodeRef(), ContentModel.PROP_VERSION_LABEL);
					if (referenceLabel == null || referenceLabel.equals(version.getVersionLabel())) {
						jsonVersion.put("clickableNode", version.getEntityNodeRef());
					}

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

					Serializable manualVersionLabel = nodeService.getProperty(branchNodeRef, BeCPGModel.PROP_MANUAL_VERSION_LABEL);

					if (manualVersionLabel instanceof String && !((String) manualVersionLabel).isBlank()) {
						jsonBranch.put("label", manualVersionLabel);
					} else if (nodeService.hasAspect(branchNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
						jsonBranch.put("label", nodeService.getProperty(branchNodeRef, ContentModel.PROP_VERSION_LABEL));
					} else {
						jsonBranch.put("label", RepoConsts.INITIAL_VERSION);
					}

					String description = (String) nodeService.getProperty(branchNodeRef, ContentModel.PROP_DESCRIPTION);

					if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
						description = description.substring(0, MAX_DESCRIPTION_LENGTH) + " ...";
					}

					jsonBranch.put("description", description);
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

	private JSONObject getPerson(String frozenModifier) throws JSONException {
		JSONObject jsonCreator = new JSONObject();
		if (personService.personExists(frozenModifier)) {
			NodeRef creatorNodeRef = personService.getPerson(frozenModifier);
			jsonCreator.put("userName", nodeService.getProperty(creatorNodeRef, ContentModel.PROP_USERNAME));
			jsonCreator.put("firstName", nodeService.getProperty(creatorNodeRef, ContentModel.PROP_FIRSTNAME));
			jsonCreator.put("lastName", nodeService.getProperty(creatorNodeRef, ContentModel.PROP_LASTNAME));
		} else {
			logger.debug("Person doesn't exist : " + frozenModifier);
			jsonCreator.put("userName", frozenModifier);
			jsonCreator.put("firstName", frozenModifier);
			jsonCreator.put("lastName", frozenModifier);
		}
		return jsonCreator;
	}
}
