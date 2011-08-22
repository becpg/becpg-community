/*
 * 
 */
package fr.becpg.repo.web.scripts.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.report.search.ExportSearchService;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchTplsWebScript.
 *
 * @author querephi
 */
public class ExportSearchTplsWebScript extends DeclarativeWebScript  {

	// model key names
	/** The Constant MODEL_KEY_NAME_REPORT_TEMPLATES. */
	private static final String MODEL_KEY_NAME_REPORT_TEMPLATES = "reportTpls";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ExportSearchTplsWebScript.class);
	
	/** The export search service. */
	private ExportSearchService exportSearchService;
	
	/**
	 * Sets the export search service.
	 *
	 * @param exportSearchService the new export search service
	 */
	public void setExportSearchService(ExportSearchService exportSearchService) {
		this.exportSearchService = exportSearchService;
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
		
		List<NodeRef> reportTpls = exportSearchService.getReportTpls();
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_REPORT_TEMPLATES, reportTpls);
		return model;
	}
}
