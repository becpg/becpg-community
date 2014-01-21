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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.comparison.CompareEntityReportService;

/**
 * The Class CompareEntityVersionReportWebScript.
 * 
 * @author querephi
 */
public class CompareEntityReportWebScript extends AbstractWebScript {

	private static final int MAX_ENTITIES = 10;
	private static final String PARAM_ENTITY = "entity";

	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private static final String PARAM_ENTITIES = "entities";

	private static Log logger = LogFactory.getLog(CompareEntityVersionReportWebScript.class);

	private CompareEntityReportService compareEntityReportService;

	private MimetypeService mimetypeService;

	public void setCompareEntityReportService(CompareEntityReportService compareEntityReportService) {
		this.compareEntityReportService = compareEntityReportService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * Compare entitys.
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
		logger.debug("CompareEntityVersionReportWebScript executeImpl()");
		List<NodeRef> entityNodeRefs = new ArrayList<NodeRef>();

		NodeRef entity1NodeRef = null;
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if (storeType != null && storeId != null && nodeId != null) {
				entity1NodeRef = new NodeRef(storeType, storeId, nodeId);
			}
		}

		String entities = req.getParameter(PARAM_ENTITIES);
		if (entities != null) {
			for (String entity : entities.split(",")) {
				entityNodeRefs.add(new NodeRef(entity));
			}
		}

		if (entityNodeRefs.isEmpty()) {
			for (int i = 1; i <= MAX_ENTITIES; i++) {
				String entity = req.getParameter(PARAM_ENTITY + i);
				if (entity != null) {
					if (entity1NodeRef == null) {
						entity1NodeRef = new NodeRef(entity);
					} else {
						entityNodeRefs.add(new NodeRef(entity));
					}
				}
			}
		}

		if (entity1NodeRef == null || entityNodeRefs.isEmpty()) {
			logger.error("missing parameters. entity1= '' or entity2=''");
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "missing parameters. entity1= '' or entity2=''");
		}

		// get the content and stream directly to the response output stream
		// assuming the repository is capable of streaming in chunks, this
		// should allow large files
		// to be streamed directly to the browser response stream.
		try {
			compareEntityReportService.getComparisonReport(entity1NodeRef, entityNodeRefs, res.getOutputStream());

			// set mimetype for the content and the character encoding + length
			// for the stream
			res.setContentType(mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF));
			res.setHeader("Content-disposition", "attachment; filename=compare.pdf");

			// res.setContentEncoding(reader.getEncoding());
			// res.setHeader("Content-Length", Long.toString(reader.getSize()));

		} catch (SocketException e1) {
			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled())
				logger.info("Client aborted stream read:\n\tcontent", e1);
		} catch (ContentIOException e2) {
			if (logger.isInfoEnabled())
				logger.info("Client aborted stream read:\n\tcontent", e2);
		}

	}

}
