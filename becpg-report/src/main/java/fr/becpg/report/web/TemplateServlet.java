package fr.becpg.report.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.becpg.report.services.TemplateCacheService;
import fr.becpg.report.services.impl.TemplateCacheServiceImpl;


public class TemplateServlet extends HttpServlet {

	TemplateCacheService templateCacheService = new TemplateCacheServiceImpl();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 609805146293149060L;

	private String TEMPLATE_ID_PARAM = "nodeRef";
	
	@Override
	/**
	 * Return template cache timestamp or -1
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		
		String templateId = req.getParameter(TEMPLATE_ID_PARAM);
	    
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
		super.doPost(req, resp);
		
		
		String templateId = req.getParameter(TEMPLATE_ID_PARAM);
		
		Long time = templateCacheService.saveTemplate(templateId,req.getInputStream());
		
		resp.getWriter().write(""+time);
		resp.getWriter().close();
	}
	
}
