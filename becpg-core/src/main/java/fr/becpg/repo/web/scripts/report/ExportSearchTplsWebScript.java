/*
 * 
 */
package fr.becpg.repo.web.scripts.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchTplsWebScript.
 *
 * @author querephi
 */
public class ExportSearchTplsWebScript extends DeclarativeWebScript  {

	private static final String PARAM_DATATYPE = "datatype";
	
	// model key names
	/** The Constant MODEL_KEY_NAME_REPORT_TEMPLATES. */
	private static final String MODEL_KEY_NAME_REPORT_TEMPLATES = "reportTpls";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ExportSearchTplsWebScript.class);
	
	private NamespaceService namespaceService;
	
	private ReportTplService reportTplService;
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	/**
	 * Get the report templates to export a search
	 * 
	 * url : /becpg/productlists/node/{store_type}/{store_id}/{id}.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
		
		logger.debug("ExportSearchTplsWebScript");
		
		// get datatype
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String datatype = templateArgs.get(PARAM_DATATYPE);
		QName datatypeQName = QName.createQName(datatype, namespaceService);
		
		List<NodeRef> reportTpls = reportTplService.suggestUserReportTemplates(ReportType.ExportSearch, datatypeQName, "*");					
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_REPORT_TEMPLATES, reportTpls);
		
		return model;
	}
}
