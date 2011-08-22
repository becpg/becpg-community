/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

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
import fr.becpg.repo.product.comparison.CompareProductReportService;

// TODO: Auto-generated Javadoc
/**
 * The Class CompareProductReportWebScript.
 *
 * @author querephi
 */
public class CompareProductReportWebScript extends AbstractWebScript  {
	
	/** The Constant PARAM_PRODUCT_1. */
	private static final String PARAM_PRODUCT_1 = "product1";
	
	/** The Constant PARAM_PRODUCT_2. */
	private static final String PARAM_PRODUCT_2 = "product2";
	
	/** The Constant PARAM_PRODUCT_3. */
	private static final String PARAM_PRODUCT_3 = "product3";
	
	/** The Constant PARAM_PRODUCT_4. */
	private static final String PARAM_PRODUCT_4 = "product4";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareProductReportWebScript.class);
	
	/** The compare product report service. */
	private CompareProductReportService compareProductReportService;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;		

	/**
	 * Sets the compare product report service.
	 *
	 * @param compareProductReportService the new compare product report service
	 */
	public void setCompareProductReportService(
			CompareProductReportService compareProductReportService) {
		this.compareProductReportService = compareProductReportService;
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
	 * Compare products.
	 *
	 * @param req the req
	 * @param res the res
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException{
		
		logger.debug("CompareWebScript executeImpl()");
		
		String product1 = req.getParameter(PARAM_PRODUCT_1);
		String product2 = req.getParameter(PARAM_PRODUCT_2);
		String product3 = req.getParameter(PARAM_PRODUCT_3);
		String product4 = req.getParameter(PARAM_PRODUCT_4);
		
		if(product1.isEmpty() && product2.isEmpty()){
			logger.error("missing parameters. product1= '' or product2=''");
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "missing parameters. product1= '' or product2=''");
		}
		
		NodeRef product1NodeRef = new NodeRef(product1);
		List<NodeRef> products = new ArrayList<NodeRef>();
		products.add(new NodeRef(product2));
		
		if(product3 != null)
			products.add(new NodeRef(product3));
		if(product4 != null)
			products.add(new NodeRef(product4));
		
		
		// get the content and stream directly to the response output stream
        // assuming the repository is capable of streaming in chunks, this should allow large files
        // to be streamed directly to the browser response stream.
        try
        {        	
        	compareProductReportService.getComparisonReport(product1NodeRef, products, res.getOutputStream());
    		
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
