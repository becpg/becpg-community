/*
 *
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
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

import fr.becpg.repo.report.entity.EntityReportService;

/**
 * <p>ReportWebScript class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class ReportWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ReportWebScript.class);

	private static final String ACTION_CHECK = "check";
	private static final String ACTION_FORCE = "force";

	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private NodeService nodeService;

	private LockService lockService;

	private EntityReportService entityReportService;

	/**
	 * <p>Setter for the field <code>entityReportService</code>.</p>
	 *
	 * @param entityReportService a {@link fr.becpg.repo.report.entity.EntityReportService} object.
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
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
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object.
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		logger.debug("start report webscript");
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String action = templateArgs.get(PARAM_ACTION);
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);

		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
		boolean generateReport = false;

		if (nodeService.exists(nodeRef) && (lockService.getLockStatus(nodeRef) == LockStatus.NO_LOCK)) {

			if (ACTION_CHECK.equals(action)) {
				generateReport = entityReportService.shouldGenerateReport(nodeRef, null);
			} else if (ACTION_FORCE.equals(action)) {
				generateReport = true;
			} else {
				String error = "Unsupported action: " + action;
				logger.error(error);
				throw new WebScriptException(error);
			}

			if (generateReport) {
				entityReportService.generateReports(nodeRef);
			}

		}

		try {

			JSONObject ret = new JSONObject();

			ret.put("generateReport", generateReport);
			ret.put("status", "SUCCESS");

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}
}
