package fr.becpg.repo.survey.web.scripts;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.SurveyService;
import fr.becpg.repo.web.scripts.entity.datalist.AbstractEntityDataListWebScript;

/**
 * <p>SurveyWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SurveyWebScript extends AbstractEntityDataListWebScript {

	/** Constant <code>PARAM_ENTITY_NODEREF="entityNodeRef"</code> */
	protected static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	/** Constant <code>PARAM_DATA_LIST_NAME="dataListName"</code> */
	protected static final String PARAM_DATA_LIST_NAME = "dataListName";

	private SurveyService surveyService;

	protected PermissionService permissionService;

	/**
	 * <p>Setter for the field <code>surveyService</code>.</p>
	 *
	 * @param surveyService a {@link fr.becpg.repo.survey.SurveyService} object
	 */
	public void setSurveyService(SurveyService surveyService) {
		this.surveyService = surveyService;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
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

				final Access access = getAccess(SurveyModel.TYPE_SURVEY_LIST, entityNodeRef, false, null, dataListName, null);
				ret = surveyService.getSurveyData(entityNodeRef, dataListName, !access.canWrite());
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

	/**
	 * <p>findEntity.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
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
