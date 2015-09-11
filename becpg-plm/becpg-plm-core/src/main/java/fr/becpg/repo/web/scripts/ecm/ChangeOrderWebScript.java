/*
 * 
 */
package fr.becpg.repo.web.scripts.ecm;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.ecm.AsyncECOService;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * The Class FormulateWebScript.
 * 
 * @author querephi
 */
public class ChangeOrderWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ChangeOrderWebScript.class);

	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_ECO_NAME = "name";
	private static final String ACTION_CALCULATE_WUSED = "calculatewused";
	private static final String ACTION_DO_SIMULATION = "dosimulation";
	private static final String ACTION_APPLY = "apply";
	private static final String ACTION_GET_INFOS = "infos";
	private static final String ACTION_CREATE_AUTOMATIC_ECO = "create";

	private ECOService ecoService;

	private AsyncECOService asyncECOService;

	private AutomaticECOService automaticECOService;

	private NodeService nodeService;

	private AlfrescoRepository<ChangeOrderData> alfrescoRepository;

	public void setAutomaticECOService(AutomaticECOService automaticECOService) {
		this.automaticECOService = automaticECOService;
	}

	public void setAsyncECOService(AsyncECOService asyncECOService) {
		this.asyncECOService = asyncECOService;
	}

	public void setEcoService(ECOService ecoService) {
		this.ecoService = ecoService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ChangeOrderData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {
		logger.debug("start eco webscript");
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String action = templateArgs.get(PARAM_ACTION);

		NodeRef ecoNodeRef = null;
		if (storeType != null && storeId != null && nodeId != null) {
			ecoNodeRef = new NodeRef(storeType, storeId, nodeId);
		}

		if (ACTION_CALCULATE_WUSED.equals(action)) {
			ecoService.calculateWUsedList(ecoNodeRef, false);
			writeInfos(ecoNodeRef, res);
		} else if (ACTION_DO_SIMULATION.equals(action)) {
			asyncECOService.doSimulationAsync(ecoNodeRef);
			writeInfos(ecoNodeRef, res);
		} else if (ACTION_APPLY.equals(action)) {
			asyncECOService.applyAsync(ecoNodeRef);
			writeInfos(ecoNodeRef, res);
		} else if (ACTION_GET_INFOS.equals(action)) {

			writeInfos(ecoNodeRef, res);

		} else if (ACTION_CREATE_AUTOMATIC_ECO.equals(action)) {

			try {
				String name = req.getParameter(PARAM_ECO_NAME);

				JSONObject json;

				if (name == null) {
					json = (JSONObject) req.parseContent();
					if (json != null && json.has(PARAM_ECO_NAME)) {
						name = (String) json.get(PARAM_ECO_NAME);
					}
				}

				if (name != null && name.length() > 0) {
					JSONObject ret = createJSONObject(automaticECOService.createAutomaticEcoForUser(name));

					res.setContentType("application/json");
					res.setContentEncoding("UTF-8");
					ret.write(res.getWriter());
				}
			} catch (JSONException | IOException e) {
				throw new WebScriptException("Unable to serialize JSON", e);
			}

		} else {
			logger.error("Unknown action '" + action + "'.");
		}

	}

	private void writeInfos(NodeRef ecoNodeRef, WebScriptResponse res) {

		try {
			if (nodeService.exists(ecoNodeRef)) {
				JSONObject ret = createJSONObject(alfrescoRepository.findOne(ecoNodeRef));

				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				ret.write(res.getWriter());

			}
		} catch (JSONException | IOException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

	private JSONObject createJSONObject(ChangeOrderData ecm) throws JSONException {

		JSONObject ret = new JSONObject();
		ret.put("nodeRef", ecm.getNodeRef());
		ret.put("name", ecm.getName());
		ret.put("state", ecm.getEcoState());
		return ret;
	}
}
