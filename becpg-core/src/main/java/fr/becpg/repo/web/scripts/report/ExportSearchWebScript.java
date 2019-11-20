/*
 *
 */
package fr.becpg.repo.web.scripts.report;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.web.scripts.search.AbstractSearchWebScript;
import fr.becpg.report.client.ReportFormat;

/**
 * Webscript that send the result of a search in a report
 *
 * @author querephi, matthieu
 */
public class ExportSearchWebScript extends AbstractSearchWebScript {

	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private static final Log logger = LogFactory.getLog(ExportSearchWebScript.class);

	private ExportSearchService exportSearchService;

	private MimetypeService mimetypeService;

	private ReportTplService reportTplService;

	public void setExportSearchService(ExportSearchService exportSearchService) {
		this.exportSearchService = exportSearchService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	/**
	 * Export search in a report.
	 *
	 * @param req
	 *            the req
	 * @param res
	 *            the res
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);

		NodeRef templateNodeRef = new NodeRef(storeType, storeId, nodeId);
		String query = req.getParameter(PARAM_QUERY);

		Boolean async = "true".equals(req.getParameter("async"));

		if ((query == null) || query.isEmpty()) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'query' argument cannot be null or empty");
		}

		try {
			JSONObject jsonObject = new JSONObject(query);

			QName datatype = QName.createQName(jsonObject.getString("datatype"), namespaceService);

			List<NodeRef> resultNodeRefs = null;
			String aftsQuery = (String) nodeService.getProperty(templateNodeRef, ReportModel.PROP_REPORT_TPL_SEARCH_QUERY);
			if ((aftsQuery != null) && !aftsQuery.isEmpty()) {
				resultNodeRefs = BeCPGQueryBuilder.createQuery().andFTSQuery(aftsQuery).maxResults(RepoConsts.MAX_RESULTS_5000).list();
			} else {
				resultNodeRefs = doSearch(req, RepoConsts.MAX_RESULTS_5000);
			}

			ReportFormat reportFormat = reportTplService.getReportFormat(templateNodeRef);

			if (async) {

				NodeRef downloadNodeRef = exportSearchService.createReport(datatype, templateNodeRef, resultNodeRefs, reportFormat);

				JSONObject ret = new JSONObject();

				ret.put("nodeRef", downloadNodeRef);

				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				res.getWriter().write(ret.toString(3));

			} else {

				String name = (String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME);

				String format = reportFormat.toString();
				if(ReportFormat.XLSX.equals(reportFormat) && name.endsWith(ReportTplService.PARAM_VALUE_XLSMREPORT_EXTENSION)) {
					format = "xlsm";
				}
				
				String mimeType = mimetypeService.getMimetype(format);

				name = FilenameUtils.removeExtension(name) + FilenameUtils.EXTENSION_SEPARATOR_STR + mimetypeService.getExtension(mimeType);

				logger.debug("Rendering report at format :" + reportFormat.toString() + " mimetype: " + mimeType + " name " + name);

				res.setContentType(mimeType);
				AttachmentHelper.setAttachment(req, res, name);

				exportSearchService.createReport(datatype, templateNodeRef, resultNodeRefs, reportFormat, res.getOutputStream());
			}

		} catch (SocketException | ContentIOException e1) {

			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}

		} catch (JSONException e3) {

			if (logger.isInfoEnabled()) {
				logger.info("Failed to parse the JSON query", e3);
			}

		}

	}

}
