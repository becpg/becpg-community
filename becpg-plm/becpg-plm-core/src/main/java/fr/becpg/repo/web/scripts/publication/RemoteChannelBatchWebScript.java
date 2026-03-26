package fr.becpg.repo.web.scripts.publication;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.PublicationModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.json.JsonData;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.publication.ChannelData;
import fr.becpg.repo.publication.PublicationChannelService;

/**
 * <p>RemoteChannelBatchWebScript class.</p>
 *
 * @author matthieu
 */
public class RemoteChannelBatchWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(RemoteChannelBatchWebScript.class);

	private static final String ENTITY = "entity";
	private static final String ATTRIBUTES = "attributes";
	private static final String BATCH_ACK_ENDPOINT = "/becpg/remote/channel/batch/ack";
	private static final String CHANNEL_NODEREF = "channelNodeRef";
	private static final String SUCCESS = "SUCCESS";
	private static final String STATUS = "status";
	private static final String ACTION = "action";
	private static final String CHANNEL_ID = "channelId";
	private static final String NODEREF = "nodeRef";
	private static final String START = "start";
	private static final String END = "end";

	private NamespaceService namespaceService;
	private NodeService nodeService;
	private AssociationService associationService;
	private PublicationChannelService publicationChannelService;

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>publicationChannelService</code>.</p>
	 *
	 * @param publicationChannelService a {@link fr.becpg.repo.publication.PublicationChannelService} object
	 */
	public void setPublicationChannelService(PublicationChannelService publicationChannelService) {
		this.publicationChannelService = publicationChannelService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		String channelId = req.getParameter(CHANNEL_ID);
		if (channelId == null || channelId.isEmpty()) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing required parameter: channelId");
		}
		
		NodeRef channelNodeRef = publicationChannelService.getChannelById(channelId);
		if (channelNodeRef == null || !nodeService.exists(channelNodeRef)) {
			throw new WebScriptException(Status.STATUS_NOT_FOUND, "This channel does not exist, or you have no right to access it");
		}
		
		if (!hasChannelPermission(channelNodeRef)) {
			throw new WebScriptException(Status.STATUS_FORBIDDEN, "You are not allowed to update this channel");
		}
		
		JsonData jsonData;
		try {
			jsonData = JsonHelper.read(req.getContent().getContent());
		} catch (Exception e) {
			logger.error("Error parsing JSON request body", e);
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON in request body");
		}
		
		if (!jsonData.has(ENTITY)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "JSON body request must contain 'entity'");
		}
		
		JsonData entityData = jsonData.get(ENTITY);
		if (!entityData.has(ATTRIBUTES)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'entity' must contain 'attributes'");
		}
		JsonData entityAttributes = entityData.get(ATTRIBUTES);
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String action = templateArgs.get(ACTION);
		
		JsonData jsonResponse = JsonHelper.createJsonObject();
		
		try {
			if (START.equals(action)) {
				String batchIdAttr = PublicationModel.PROP_PUBCHANNEL_BATCHID.toPrefixString(namespaceService);
				checkAttributePresence(entityAttributes, batchIdAttr);
				String batchId = getStringAttribute(entityAttributes, batchIdAttr);
				AuthenticationUtil.runAsSystem(() -> {
					publicationChannelService.startChannel(channelNodeRef, batchId);
					return null;
				});
			} else if (END.equals(action)) {
				String statusAttr = PublicationModel.PROP_PUBCHANNEL_STATUS.toPrefixString(namespaceService);
				checkAttributePresence(entityAttributes, statusAttr);
				String status = getStringAttribute(entityAttributes, statusAttr);
				
				String failCountAttr = PublicationModel.PROP_PUBCHANNEL_FAILCOUNT.toPrefixString(namespaceService);
				checkAttributePresence(entityAttributes, failCountAttr);
				Integer failCount = getIntAttribute(entityAttributes, failCountAttr);

				String readCountAttr = PublicationModel.PROP_PUBCHANNEL_READCOUNT.toPrefixString(namespaceService);
				checkAttributePresence(entityAttributes, readCountAttr);
				Integer readCount = getIntAttribute(entityAttributes, readCountAttr);
				
				String errorAttr = PublicationModel.PROP_PUBCHANNEL_ERROR.toPrefixString(namespaceService);
				String error = entityAttributes.has(errorAttr) ? getStringAttribute(entityAttributes, errorAttr) : null;
				
				String lastSuccessBatchIdAttr = PublicationModel.PROP_PUBCHANNEL_LASTSUCCESSBATCHID.toPrefixString(namespaceService);
				String lastSuccessBatchId = entityAttributes.has(lastSuccessBatchIdAttr)
						? getStringAttribute(entityAttributes, lastSuccessBatchIdAttr)
								: null;
				
				String lastDateAttr = PublicationModel.PROP_PUBCHANNEL_LASTDATE.toPrefixString(namespaceService);
				Date lastDate = entityAttributes.has(lastDateAttr) ? parseDateAttribute(entityAttributes, lastDateAttr) : null;
				
				ChannelData channelData = ChannelData.builder().status(status).failCount(failCount).readCount(readCount).error(error)
						.lastSuccessBatchId(lastSuccessBatchId).lastDate(lastDate).build();
				AuthenticationUtil.runAsSystem(() -> {
					publicationChannelService.completeChannel(channelNodeRef, channelData);
					return null;
				});
			} else if (BATCH_ACK_ENDPOINT.equals(req.getServiceMatch().getPath())) {
				String nodeRefParam = req.getParameter(NODEREF);
				if (nodeRefParam == null || nodeRefParam.isEmpty()) {
					throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing required parameter: nodeRef");
				}
				
				NodeRef nodeRef = new NodeRef(nodeRefParam);
				if (!nodeService.exists(nodeRef)) {
					throw new WebScriptException(Status.STATUS_NOT_FOUND, "This entity does not exist, or you have no right to access it");
				}
				String statusAttr = PublicationModel.PROP_PUBCHANNELLIST_STATUS.toPrefixString(namespaceService);
				String batchIdAttr = PublicationModel.PROP_PUBCHANNELLIST_BATCHID.toPrefixString(namespaceService);
				
				checkAttributePresence(entityAttributes, statusAttr);
				checkAttributePresence(entityAttributes, batchIdAttr);
				
				String status = getStringAttribute(entityAttributes, statusAttr);
				String batchId = getStringAttribute(entityAttributes, batchIdAttr);

				String errorAttr = PublicationModel.PROP_PUBCHANNELLIST_ERROR.toPrefixString(namespaceService);
				String error = entityAttributes.has(errorAttr) ? getStringAttribute(entityAttributes, errorAttr) : null;

				String actionAttr = PublicationModel.PROP_PUBCHANNELLIST_ACTION.toPrefixString(namespaceService);
				String channelListAction = entityAttributes.has(actionAttr) ? getStringAttribute(entityAttributes, actionAttr) : null;

				ChannelData channelData = ChannelData.builder().status(status).batchId(batchId).error(error).action(channelListAction).build();
				AuthenticationUtil.runAsSystem(() -> {
					publicationChannelService.publishEntityChannel(nodeRef, channelId, channelData);
					return null;
				});
				jsonResponse.put(NODEREF, nodeRef.toString());
			} else {
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unknown 'action' parameter");
			}
		} catch (WebScriptException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error processing request", e);
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
		}
		
		jsonResponse.put(STATUS, SUCCESS);
		jsonResponse.put(CHANNEL_ID, channelId);
		jsonResponse.put(CHANNEL_NODEREF, channelNodeRef.toString());
		
		res.setStatus(Status.STATUS_OK);
		res.setContentEncoding("UTF-8");
		res.setContentType("application/json");
		res.getWriter().write(jsonResponse.toString());
	}
	
	private boolean hasChannelPermission(NodeRef channelNodeRef) {
		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
		List<NodeRef> channelAccounts = associationService.getTargetAssocs(channelNodeRef, PublicationModel.ASSOC_PUBCHANNEL_ACCOUNTS);
		boolean isChannelAccount = channelAccounts.stream()
				.anyMatch(a -> currentUser.equals(nodeService.getProperty(a, ContentModel.PROP_USERNAME)));
		
		if (isChannelAccount) {
			return true;
		}

		return AuthorityHelper.extractPeople(PermissionService.GROUP_PREFIX + SystemGroup.ApiConnector).stream()
				.anyMatch(u -> u.equals(currentUser));
	}
	
	private void checkAttributePresence(JsonData entityAttributes, String attribute) {
		if (!entityAttributes.has(attribute)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'attributes' must contain '" + attribute + "'");
		}
	}
	
	private String getStringAttribute(JsonData entityAttributes, String attribute) {
		try {
			return entityAttributes.get(attribute).getString();
		} catch (Exception e) {
			logger.error("Error getting string attribute: " + attribute, e);
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid value for attribute '" + attribute + "'");
		}
	}
	
	private Integer getIntAttribute(JsonData entityAttributes, String attribute) {
		try {
			return entityAttributes.get(attribute).getInt();
		} catch (Exception e) {
			logger.error("Error getting integer attribute: " + attribute, e);
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid integer value for attribute '" + attribute + "'");
		}
	}
	
	private Date parseDateAttribute(JsonData entityAttributes, String attribute) {
		try {
			String dateString = entityAttributes.get(attribute).getString();
			return ISO8601DateFormat.parse(dateString);
		} catch (DateTimeParseException e) {
			logger.error("Error parsing date attribute: " + attribute, e);
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid date format for attribute '" + attribute + "'. Expected ISO-8601 format (e.g., 2025-11-25T10:05:39.746Z)");
		} catch (Exception e) {
			logger.error("Error getting date attribute: " + attribute, e);
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid value for date attribute '" + attribute + "'");
		}
	}
}
