package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.net.SocketException;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.report.entity.EntityReportParameters;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.report.client.ReportFormat;

public class EntityReportWebScript extends AbstractEntityWebScript {

	private static final Log logger = LogFactory.getLog(EntityReportWebScript.class);

	private static final String PARAM_LOCALE = "locale";
	private static final String PARAM_TEMPLATE = "tplNodeRef";

	private EntityReportService entityReportService;

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		NodeRef entityNodeRef = findEntity(req);

		NodeRef templateNodeRef = null;
		if (req.getParameter(PARAM_TEMPLATE) != null) {
			templateNodeRef = new NodeRef(req.getParameter(PARAM_TEMPLATE));
		} else {
			throw new WebScriptException("Template nodeRef is mandatory..");
		}

		Locale locale;
		String paramLocale = req.getParameter(PARAM_LOCALE);
		if (paramLocale == null) {
			locale = I18NUtil.getLocale();
		} else {
			locale = MLTextHelper.parseLocale(paramLocale);
		}

		ReportFormat reportFormat;
		String format = req.getParameter(PARAM_FORMAT);
		if (format != null) {
			reportFormat = ReportFormat.valueOf(format.toUpperCase());
		} else {
			reportFormat = ReportFormat.PDF;
		}

		EntityReportParameters reportParameters;
		JSONObject json = (JSONObject) req.parseContent();
		if (json != null) {
			reportParameters = EntityReportParameters.createFromJSON(json.toString());
		} else {
			reportParameters = EntityReportParameters
					.createFromJSON((String) nodeService.getProperty(templateNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Get report for entity: " + entityNodeRef);
		}

		try {
			String mimeType = mimetypeService.getMimetype(format);
	
			resp.setContentType(mimeType);
			entityReportService.generateReport(entityNodeRef, templateNodeRef, reportParameters, locale, reportFormat, resp.getOutputStream());
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled())
				logger.info("Client aborted stream read:\n\tcontent", e1);

		} 
		
	}

}
