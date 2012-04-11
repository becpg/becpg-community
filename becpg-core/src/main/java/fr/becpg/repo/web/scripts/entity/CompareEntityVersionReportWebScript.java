/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.comparison.CompareEntityReportService;
import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * The Class CompareEntityVersionReportWebScript.
 *
 * @author querephi
 */
public class CompareEntityVersionReportWebScript extends AbstractWebScript  {
	
	private static final String PARAM_STORE_TYPE = "store_type";	
	private static final String PARAM_STORE_ID = "store_id";	
	private static final String PARAM_ID = "id";	
	private static final String PARAM_VERSION_LABEL = "versionLabel";
	
	private static Log logger = LogFactory.getLog(CompareEntityVersionReportWebScript.class);
	
	private CompareEntityReportService compareEntityReportService;
	
	private MimetypeService mimetypeService;		

	private VersionService versionService;
	
	private EntityVersionService entityVersionService;
	
	public void setCompareEntityReportService(
			CompareEntityReportService compareEntityReportService) {
		this.compareEntityReportService = compareEntityReportService;
	}
	
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * Compare entitys.
	 *
	 * @param req the req
	 * @param res the res
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException{
		
		logger.debug("CompareEntityVersionReportWebScript executeImpl()");
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String versionLabel = templateArgs.get(PARAM_VERSION_LABEL);
		
		NodeRef entityNodeRef = new NodeRef(storeType, storeId, nodeId);
				
		if(entityNodeRef != null && versionLabel.isEmpty()){
			logger.error("missing parameters. entity= '' or versionLabel=''");
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "missing parameters. entity= '' or versionLabel=''");
		}
		
		VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
		Version version = versionHistory.getVersion(versionLabel);				
		NodeRef entityVersionNodeRef = entityVersionService.getEntityVersion(version);
		
		if (logger.isDebugEnabled())
			logger.debug("entityNodeRef: " + entityNodeRef + " - versionLabel: " + versionLabel + " - entityVersionNodeRef: " + entityVersionNodeRef);
				
		List<NodeRef> entities = new ArrayList<NodeRef>();
		entities.add(entityNodeRef);
		
		// get the content and stream directly to the response output stream
        // assuming the repository is capable of streaming in chunks, this should allow large files
        // to be streamed directly to the browser response stream.
        try
        {        	
        	compareEntityReportService.getComparisonReport(entityVersionNodeRef, entities, res.getOutputStream());
    		
    		// set mimetype for the content and the character encoding + length for the stream
            res.setContentType(mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF));
            //res.setContentEncoding(reader.getEncoding());
            //res.setHeader("Content-Length", Long.toString(reader.getSize()));
    		
        }
        catch (SocketException e1)
        {
            // the client cut the connection - our mission was accomplished apart from a little error message
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tcontent",  e1);
        }
        catch (ContentIOException e2)
        {
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tcontent", e2);
        }
		
	}
	

}
