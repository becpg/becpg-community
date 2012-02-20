package fr.becpg.report.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportParams;
import fr.becpg.report.services.TemplateCacheService;
import fr.becpg.report.services.impl.TemplateCacheServiceImpl;


public class TemplateServlet extends AbstractReportServlet {

	TemplateCacheService templateCacheService = new TemplateCacheServiceImpl();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 609805146293149060L;


	
	@Override
	/**
	 * Return template cache timestamp or -1
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
				String templateId = req.getParameter(ReportParams.TEMPLATE_ID_PARAM);
	    
		Long time = templateCacheService.getTemplateTimeStamp(templateId);
		
		resp.getWriter().write(""+time);
		resp.getWriter().close();
		
	}
	
	/**
	 * Add or replace template in cache
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
				String templateId = req.getParameter(ReportParams.TEMPLATE_ID_PARAM);
		
		Long time = -1L;
		try {			
			time = templateCacheService.saveTemplate(templateId,req.getInputStream());
		} catch (ReportException e) {
			logger.error(e,e);
		}
		
		resp.getWriter().write(""+time);
		resp.getWriter().close();
	}
	
}
