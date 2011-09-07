/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.RepoConsts;
import fr.becpg.repo.entity.comparison.CompareEntityReportService;

// TODO: Auto-generated Javadoc
/**
 * The Class CompareEntityReportWebScript.
 *
 * @author querephi
 */
public class CompareEntityReportWebScript extends AbstractWebScript  {
	
	/** The Constant PARAM_ENTITY_1. */
	private static final String PARAM_ENTITY_1 = "entity1";
	
	/** The Constant PARAM_ENTITY_2. */
	private static final String PARAM_ENTITY_2 = "entity2";
	
	/** The Constant PARAM_ENTITY_3. */
	private static final String PARAM_ENTITY_3 = "entity3";
	
	/** The Constant PARAM_ENTITY_4. */
	private static final String PARAM_ENTITY_4 = "entity4";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareEntityReportWebScript.class);
	
	/** The compare entity report service. */
	private CompareEntityReportService compareEntityReportService;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;		

	/**
	 * Sets the compare entity report service.
	 *
	 * @param compareEntityReportService the new compare entity report service
	 */
	public void setCompareEntityReportService(
			CompareEntityReportService compareEntityReportService) {
		this.compareEntityReportService = compareEntityReportService;
	}
	
	/**
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
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
		
		logger.debug("CompareWebScript executeImpl()");
		
		String entity1 = req.getParameter(PARAM_ENTITY_1);
		String entity2 = req.getParameter(PARAM_ENTITY_2);
		String entity3 = req.getParameter(PARAM_ENTITY_3);
		String entity4 = req.getParameter(PARAM_ENTITY_4);
		
		if(entity1.isEmpty() && entity2.isEmpty()){
			logger.error("missing parameters. entity1= '' or entity2=''");
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "missing parameters. entity1= '' or entity2=''");
		}
		
		NodeRef entity1NodeRef = new NodeRef(entity1);
		List<NodeRef> entities = new ArrayList<NodeRef>();
		entities.add(new NodeRef(entity2));
		
		if(entity3 != null)
			entities.add(new NodeRef(entity3));
		if(entity4 != null)
			entities.add(new NodeRef(entity4));
		
		
		// get the content and stream directly to the response output stream
        // assuming the repository is capable of streaming in chunks, this should allow large files
        // to be streamed directly to the browser response stream.
        try
        {        	
        	compareEntityReportService.getComparisonReport(entity1NodeRef, entities, res.getOutputStream());
    		
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
