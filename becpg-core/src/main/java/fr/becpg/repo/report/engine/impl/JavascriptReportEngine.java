package fr.becpg.repo.report.engine.impl;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

/**
 * <p>JavascriptReportEngine class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class JavascriptReportEngine implements BeCPGReportEngine {

	/** Constant <code>JS_EXTENSION=".js"</code> */
	public static final String JS_EXTENSION = ".js";
	
	private static Log logger = LogFactory.getLog(JavascriptReportEngine.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ServiceRegistry serviceRegistry;

	@Autowired
	private ScriptService scriptService;

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME)).endsWith(JS_EXTENSION);
	}

	/** {@inheritDoc} */
	@Override
	public void createReport(NodeRef tplNodeRef, EntityReportData reportData, OutputStream out, Map<String, Object> params) throws ReportException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		final ReportFormat format = (ReportFormat) params.get(ReportParams.PARAM_FORMAT);

		if (format == null) {
			throw new IllegalArgumentException("Format is a mandatory param");
		}
		logger.debug("Run javascript report");

		Map<String, Object> model = new HashMap<>();

		model.put("entity", new ScriptNode((NodeRef) params.get(BeCPGReportEngine.PARAM_ENTITY_NODEREF), serviceRegistry));
		model.put("document", new ScriptNode((NodeRef) params.get(BeCPGReportEngine.PARAM_DOCUMENT_NODEREF), serviceRegistry));

		scriptService.executeScript(tplNodeRef, ContentModel.PROP_CONTENT, model);

		if (logger.isDebugEnabled() && watch!=null) {
			watch.stop();
			logger.debug(" Report generated by server in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

	}

	/** {@inheritDoc} */
	@Override
	public boolean isXmlEngine() {
		return false;
	}

}
