package fr.becpg.repo.web.scripts.report;

import java.io.IOException;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.report.entity.EntityReportService;

public class ReportDataSourceWebscript extends AbstractWebScript {


	private static final String PARAM_NODEREF = "nodeRef";
	
	
	EntityReportService entityReportService;

	/**
	 * @param entityReportService the entityReportService to set
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}
	
	
	/**
	 * @param req
	 *            the req
	 * @param res
	 *            the res
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		    NodeRef entityNodeRef = new NodeRef( req.getParameter(PARAM_NODEREF));	
	    	
		    // #366 : force to use server locale for mlText fields 
		 	I18NUtil.setLocale(Locale.getDefault());
			
			res.setContentType("application/xml");
            res.setContentEncoding("UTF-8");
			res.getWriter().write(entityReportService.getXmlReportDataSource(entityNodeRef));


	}

	
}
