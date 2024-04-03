package fr.becpg.repo.survey.web.scripts;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.survey.SurveyService;

/**
 *
 * @author matthieu
 *
 */
public class SurveyWebScript extends AbstractWebScript {

	protected static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	protected static final String PARAM_DATA_LIST_NAME = "dataListName";

	private NodeService nodeService;

	private SurveyService surveyService;

	private PermissionService permissionService;

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setSurveyService(SurveyService surveyService) {
		this.surveyService = surveyService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef entityNodeRef = findEntity(req);

		String dataListName = req.getParameter(PARAM_DATA_LIST_NAME);

		try {

			JSONObject ret = new JSONObject();
			if ((JSONObject) req.parseContent() != null) {

				surveyService.saveSurveyData(entityNodeRef, dataListName, (JSONObject) req.parseContent());

				ret.put("persistedObject", entityNodeRef);
				ret.put("message", "Success");
			} else {

				ret = surveyService.getSurveyData(entityNodeRef, dataListName);
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

	protected NodeRef findEntity(WebScriptRequest req) {

		String nodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		if ((nodeRef != null) && (nodeRef.length() > 0)) {
			NodeRef node = new NodeRef(nodeRef);
			if (nodeService.exists(node)) {
				if (AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(node))) {
					return node;
				} else {
					throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You have no right to see this node");
				}
			} else {
				throw new WebScriptException(Status.STATUS_NOT_FOUND, "Node " + nodeRef + " doesn't exist in repository");
			}

		}

		throw new WebScriptException(Status.STATUS_NOT_FOUND, "No entity found for this parameters");
	}

}
