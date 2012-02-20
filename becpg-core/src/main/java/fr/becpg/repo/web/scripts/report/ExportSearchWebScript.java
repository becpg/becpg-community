/*
 * 
 */
package fr.becpg.repo.web.scripts.report;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.web.scripts.search.AbstractSearchWebSrcipt;
import fr.becpg.report.client.ReportFormat;

/**
 * Webscript that send the result of a search in a report
 *
 * @author querephi
 */
public class ExportSearchWebScript extends AbstractSearchWebSrcipt  {
	
	

	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ExportSearchWebScript.class);
	
	/** The export search service. */
	private ExportSearchService exportSearchService;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;

	private ReportTplService reportTplService;
	
	/**
	 * Sets the export search service.
	 *
	 * @param exportSearchService the new export search service
	 */
	public void setExportSearchService(ExportSearchService exportSearchService) {
		this.exportSearchService = exportSearchService;
	}
	
	/**
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}
	

	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	/**
	 * Export search in a report.
	 *
	 * @param req the req
	 * @param res the res
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException{
		
		logger.debug("ExportSearchWebScript executeImpl()");
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();		
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
    	
		NodeRef templateNodeRef = new NodeRef(storeType, storeId, nodeId);		
		String query = req.getParameter(PARAM_QUERY);
		
		if (query == null || query.isEmpty()) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'query' argument cannot be null or empty");
		}
		
		// get the content and stream directly to the response output stream
        // assuming the repository is capable of streaming in chunks, this should allow large files
        // to be streamed directly to the browser response stream.
        try
        {      
    		JSONObject jsonObject = new JSONObject(query);    		
    	
        	QName datatype = QName.createQName(jsonObject.getString("datatype"), namespaceService);
        	
        	List<NodeRef> resultNodeRefs = doSearch(req,null);

        	// report format
			ReportFormat reportFormat = reportTplService.getReportFormat(templateNodeRef);
        	
        	exportSearchService.getReport(datatype, templateNodeRef, resultNodeRefs, reportFormat, res.getOutputStream());    		        	
        	
    		// set mimetype for the content and the character encoding + length for the stream
            res.setContentType(mimetypeService.guessMimetype(reportFormat.toString()));
            //res.setContentEncoding(reader.getEncoding());
            //res.setHeader("Content-Length", Long.toString(reader.getSize()));
            CountingOutputStream c = new CountingOutputStream(res.getOutputStream());
            res.setHeader("Content-Length", Long.toString(c.getByteCount()));
            
    		
        }
        catch (SocketException e1){
        	
            // the client cut the connection - our mission was accomplished apart from a little error message
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tcontent",  e1);
            
        }
        catch (ContentIOException e2){
        	
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tcontent", e2);
            
        } catch (JSONException e3) {
			
        	if (logger.isInfoEnabled())
                logger.info("Failed to parse the JSON query", e3);
        	
		}
		
	}	

}
