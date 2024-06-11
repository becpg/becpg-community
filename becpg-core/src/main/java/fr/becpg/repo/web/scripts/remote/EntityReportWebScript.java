package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.net.SocketException;
import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
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

/**
 * <p>EntityReportWebScript class.</p>
 *
 * @author rabah, matthieu
 * @version $Id: $Id
 */
public class EntityReportWebScript extends AbstractEntityWebScript {

	private static final Log logger = LogFactory.getLog(EntityReportWebScript.class);

	private static final String PARAM_LOCALE = "locale";
	private static final String PARAM_TEMPLATE = "tplNodeRef";

	private EntityReportService entityReportService;

	private ContentService contentService;

	/**
	 * <p>Setter for the field <code>entityReportService</code>.</p>
	 *
	 * @param entityReportService a {@link fr.becpg.repo.report.entity.EntityReportService} object.
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/** {@inheritDoc} */
	@Override
	public void executeInternal(WebScriptRequest req, WebScriptResponse resp) throws IOException {
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

		String format = req.getFormat();
		if (req.getParameter(PARAM_FORMAT) != null) {
			format = req.getParameter(PARAM_FORMAT);
		}

		if ((format == null) || format.isEmpty() || !ReportFormat.isBirtSupported(format.toUpperCase())) {
			format = ReportFormat.PDF.toString().toLowerCase();
		}

		String mimetype = mimetypeService.getMimetype(format);
		if (mimetype == null) {
			throw new WebScriptException("Web Script format '" + format + "' is not registered");
		}
		resp.setContentType(mimetype);

		EntityReportParameters reportParameters;
		JSONObject json = (JSONObject) req.parseContent();
		if (json != null) {
			reportParameters = EntityReportParameters.createFromJSON(json.toString());
		} else {
			reportParameters = EntityReportParameters
					.createFromJSON((String) nodeService.getProperty(templateNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS));
		}

		try {

			NodeRef documentNodeRef = entityReportService.getAssociatedDocumentNodeRef(entityNodeRef, templateNodeRef, reportParameters, locale,
					ReportFormat.valueOf(format.toUpperCase()));
			if (documentNodeRef != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Get existing report for entity: " + entityNodeRef + " doc " + documentNodeRef);
				}
				// get the content reader
				ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);
				if ((reader == null) || !reader.exists()) {
					throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to locate content for node ref " + documentNodeRef);
				}

				// get the content and stream directly to the response output stream
				// assuming the repository is capable of streaming in chunks, this should allow large files
				// to be streamed directly to the browser response stream.
				reader.getContent(resp.getOutputStream());

			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Generate report for entity: " + entityNodeRef);
				}
				entityReportService.generateReport(entityNodeRef, templateNodeRef, reportParameters, locale,
						ReportFormat.valueOf(format.toUpperCase()), resp.getOutputStream());
			}
		} catch (SocketException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}

		}

	}

}
