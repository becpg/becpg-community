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
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.PublicationModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.json.JsonData;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelStatus;

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
	private static final String ENTITY_NODEREF = "entityNodeRef";
	private static final String START = "start";
	private static final String END = "end";

	private NamespaceService namespaceService;
	private NodeService nodeService;
	private AssociationService associationService;
	private PublicationChannelService publicationChannelService;
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setPublicationChannelService(PublicationChannelService publicationChannelService) {
		this.publicationChannelService = publicationChannelService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		String channelId = req.getParameter(CHANNEL_ID);
		if (channelId == null || channelId.isEmpty()) {
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Missing required parameter: channelId");
			return;
		}

		NodeRef channelNodeRef = publicationChannelService.getChannelById(channelId);
		if (channelNodeRef == null || !nodeService.exists(channelNodeRef)) {
			sendErrorResponse(res, Status.STATUS_NOT_FOUND, "This channel does not exist, or you have no right to access it");
			return;
		}

		if (!hasChannelPermission(channelNodeRef)) {
			sendErrorResponse(res, Status.STATUS_FORBIDDEN, "You are not allowed to update this channel");
			return;
		}

		JsonData jsonData;
		try {
			jsonData = JsonHelper.read(req.getContent().getContent());
		} catch (Exception e) {
			logger.error("Error parsing JSON request body", e);
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Invalid JSON in request body");
			return;
		}

		if (!jsonData.has(ENTITY)) {
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "JSON body request must contain 'entity'");
			return;
		}

		JsonData entityData = jsonData.get(ENTITY);
		if (!entityData.has(ATTRIBUTES)) {
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "'entity' must contain 'attributes'");
			return;
		}
		JsonData entityAttributes = entityData.get(ATTRIBUTES);

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String action = templateArgs.get(ACTION);

		JsonData jsonResponse = JsonHelper.createJsonData();
		
		if (START.equals(action)) {
			if (!processBatchStart(res, channelNodeRef, entityAttributes)) {
				return;
			}
		} else if (END.equals(action)) {
			if (!processBatchEnd(res, channelNodeRef, entityAttributes)) {
				return;
			}
		} else if (BATCH_ACK_ENDPOINT.equals(req.getServiceMatch().getPath())) {
			String entity = req.getParameter(ENTITY_NODEREF);
			if (entity == null || entity.isEmpty()) {
				sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Missing required parameter: entityNodeRef");
				return;
			}
			
			NodeRef entityNodeRef = new NodeRef(entity);
			if (!nodeService.exists(entityNodeRef)) {
				sendErrorResponse(res, Status.STATUS_NOT_FOUND, "This entity does not exist, or you have no right to access it");
				return;
			}
			
			if (!processBatchAck(res, channelId, entityNodeRef, entityAttributes)) {
				return;
			}
			jsonResponse.put(ENTITY_NODEREF, entityNodeRef.toString());
		} else {
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Unknown 'action' parameter");
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
	
	private void sendErrorResponse(WebScriptResponse res, int statusCode, String message) {
		try {
			JsonData errorResponse = JsonHelper.createJsonData();
			errorResponse.put(STATUS, "error");
			errorResponse.put("message", message);
			
			res.setStatus(statusCode);
			res.setContentEncoding("UTF-8");
			res.setContentType("application/json");
			res.getWriter().write(errorResponse.toString());
		} catch (Exception e) {
			logger.error("Error sending error response", e);
		}
	}

	private boolean processBatchStart(WebScriptResponse res, NodeRef channelNodeRef, JsonData entityAttributes) {
		try {
			String statusAttr = PublicationModel.PROP_PUBCHANNEL_STATUS.toPrefixString(namespaceService);
			String batchIdAttr = PublicationModel.PROP_PUBCHANNEL_BATCHID.toPrefixString(namespaceService);
			
			if (!checkAttributePresence(res, entityAttributes, batchIdAttr)) {
				return false;
			}
			
			String batchId = getStringAttribute(res, entityAttributes, batchIdAttr);
			if (batchId == null) {
				return false;
			}
			
			AuthenticationUtil.runAsSystem(() -> {
				if (entityAttributes.has(statusAttr)) {
					String status = getStringAttribute(res, entityAttributes, statusAttr);
					nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_STATUS, status);
				} else {
					nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_STATUS, PublicationChannelStatus.STARTED.toString());
				}
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHSTARTTIME, new Date());
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME, null);
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHDURATION, null);
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHID, batchId);
				return null;
			});
			
			return true;
		} catch (Exception e) {
			logger.error("Error processing batch start", e);
			sendErrorResponse(res, Status.STATUS_INTERNAL_SERVER_ERROR, "Error processing batch start: " + e.getMessage());
			return false;
		}
	}

	private boolean processBatchEnd(WebScriptResponse res, NodeRef channelNodeRef, JsonData entityAttributes) {
		try {
			String statusAttr = PublicationModel.PROP_PUBCHANNEL_STATUS.toPrefixString(namespaceService);
			String failCountAttr = PublicationModel.PROP_PUBCHANNEL_FAILCOUNT.toPrefixString(namespaceService);
			String readCountAttr = PublicationModel.PROP_PUBCHANNEL_READCOUNT.toPrefixString(namespaceService);
			
			if (!checkAttributePresence(res, entityAttributes, statusAttr) ||
				!checkAttributePresence(res, entityAttributes, failCountAttr) ||
				!checkAttributePresence(res, entityAttributes, readCountAttr)) {
				return false;
			}
			
			String status = getStringAttribute(res, entityAttributes, statusAttr);
			Integer failCount = getIntAttribute(res, entityAttributes, failCountAttr);
			Integer readCount = getIntAttribute(res, entityAttributes, readCountAttr);
			
			if (status == null || failCount == null || readCount == null) {
				return false;
			}
			
			AuthenticationUtil.runAsSystem(() -> {
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_STATUS, status);
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_FAILCOUNT, failCount);
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_READCOUNT, readCount);
				
				String batchEndTimeAttr = PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME.toPrefixString(namespaceService);
				if (entityAttributes.has(batchEndTimeAttr)) {
					Date date = parseDateAttribute(res, entityAttributes, batchEndTimeAttr);
					if (date != null) {
						nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME, date);
					}
				} else {
					nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHENDTIME, new Date());
				}
				
				String batchDurationAttr = PublicationModel.PROP_PUBCHANNEL_BATCHDURATION.toPrefixString(namespaceService);
				if (entityAttributes.has(batchDurationAttr)) {
					Long duration = getLongAttribute(res, entityAttributes, batchDurationAttr);
					if (duration != null) {
						nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_BATCHDURATION, duration);
					}
				}
				
				String errorAttr = PublicationModel.PROP_PUBCHANNEL_ERROR.toPrefixString(namespaceService);
				if (entityAttributes.has(errorAttr)) {
					String error = getStringAttribute(res, entityAttributes, errorAttr);
					if (error != null) {
						nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ERROR, error);
					}
				}
				
				String lastSuccessBatchIdAttr = PublicationModel.PROP_PUBCHANNEL_LASTSUCCESSBATCHID.toPrefixString(namespaceService);
				if (entityAttributes.has(lastSuccessBatchIdAttr)) {
					String lastSuccessBatchId = getStringAttribute(res, entityAttributes, lastSuccessBatchIdAttr);
					if (lastSuccessBatchId != null) {
						nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTSUCCESSBATCHID, lastSuccessBatchId);
					}
				}
				
				String lastDateAttr = PublicationModel.PROP_PUBCHANNEL_LASTDATE.toPrefixString(namespaceService);
				if (entityAttributes.has(lastDateAttr)) {
					Date date = parseDateAttribute(res, entityAttributes, lastDateAttr);
					if (date != null) {
						nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_LASTDATE, date);
					}
				}
				
				nodeService.setProperty(channelNodeRef, PublicationModel.PROP_PUBCHANNEL_ACTION, null);
				return null;
			});
			
			return true;
		} catch (Exception e) {
			logger.error("Error processing batch end", e);
			sendErrorResponse(res, Status.STATUS_INTERNAL_SERVER_ERROR, "Error processing batch end: " + e.getMessage());
			return false;
		}
	}

	private boolean processBatchAck(WebScriptResponse res, String channelId, NodeRef entityNodeRef, JsonData entityAttributes) {
		try {
			String statusAttr = PublicationModel.PROP_PUBCHANNELLIST_STATUS.toPrefixString(namespaceService);
			String batchIdAttr = PublicationModel.PROP_PUBCHANNELLIST_BATCHID.toPrefixString(namespaceService);
			
			if (!checkAttributePresence(res, entityAttributes, statusAttr) ||
				!checkAttributePresence(res, entityAttributes, batchIdAttr)) {
				return false;
			}
			
			String status = getStringAttribute(res, entityAttributes, statusAttr);
			String batchId = getStringAttribute(res, entityAttributes, batchIdAttr);
			
			if (status == null || batchId == null) {
				return false;
			}
			
			AuthenticationUtil.runAsSystem(() -> {
				
				NodeRef channelListNodeRef = publicationChannelService.getOrCreateChannelListNodeRef(entityNodeRef, channelId);
				
				nodeService.setProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_STATUS, status);
				nodeService.setProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_BATCHID, batchId);
				
				String errorAttr = PublicationModel.PROP_PUBCHANNELLIST_ERROR.toPrefixString(namespaceService);
				if (entityAttributes.has(errorAttr)) {
					String error = getStringAttribute(res, entityAttributes, errorAttr);
					if (error != null) {
						nodeService.setProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ERROR, error);
					}
				}
				
				String actionAttr = PublicationModel.PROP_PUBCHANNELLIST_ACTION.toPrefixString(namespaceService);
				if (entityAttributes.has(actionAttr)) {
					String action = getStringAttribute(res, entityAttributes, actionAttr);
					if (action != null) {
						nodeService.setProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_ACTION, action);
					}
				}
				
				String publishedDateAttr = PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE.toPrefixString(namespaceService);
				if (entityAttributes.has(publishedDateAttr)) {
					Date date = parseDateAttribute(res, entityAttributes, publishedDateAttr);
					if (date != null) {
						nodeService.setProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE, date);
					}
				}
				
				return null;
			});
			
			return true;
		} catch (Exception e) {
			logger.error("Error processing batch acknowledgment", e);
			sendErrorResponse(res, Status.STATUS_INTERNAL_SERVER_ERROR, "Error processing batch acknowledgment: " + e.getMessage());
			return false;
		}
	}

	private boolean checkAttributePresence(WebScriptResponse res, JsonData entityAttributes, String attribute) {
		if (!entityAttributes.has(attribute)) {
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "'attributes' must contain '" + attribute + "'");
			return false;
		}
		return true;
	}
	
	private String getStringAttribute(WebScriptResponse res, JsonData entityAttributes, String attribute) {
		try {
			return entityAttributes.get(attribute).getString();
		} catch (Exception e) {
			logger.error("Error getting string attribute: " + attribute, e);
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Invalid value for attribute '" + attribute + "'");
			return null;
		}
	}
	
	private Integer getIntAttribute(WebScriptResponse res, JsonData entityAttributes, String attribute) {
		try {
			return entityAttributes.get(attribute).getInt();
		} catch (Exception e) {
			logger.error("Error getting integer attribute: " + attribute, e);
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Invalid integer value for attribute '" + attribute + "'");
			return null;
		}
	}
	
	private Long getLongAttribute(WebScriptResponse res, JsonData entityAttributes, String attribute) {
		try {
			return entityAttributes.get(attribute).getLong();
		} catch (Exception e) {
			logger.error("Error getting long attribute: " + attribute, e);
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Invalid long value for attribute '" + attribute + "'");
			return null;
		}
	}
	
	private Date parseDateAttribute(WebScriptResponse res, JsonData entityAttributes, String attribute) {
		try {
			String dateString = entityAttributes.get(attribute).getString();
			return ISO8601DateFormat.parse(dateString);
		} catch (DateTimeParseException e) {
			logger.error("Error parsing date attribute: " + attribute, e);
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Invalid date format for attribute '" + attribute + "'. Expected ISO-8601 format (e.g., 2025-11-25T10:05:39.746Z)");
			return null;
		} catch (Exception e) {
			logger.error("Error getting date attribute: " + attribute, e);
			sendErrorResponse(res, Status.STATUS_BAD_REQUEST, "Invalid value for date attribute '" + attribute + "'");
			return null;
		}
	}
}