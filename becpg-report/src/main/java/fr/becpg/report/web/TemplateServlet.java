/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

	final TemplateCacheService templateCacheService = TemplateCacheServiceImpl.getInstance();
	
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
