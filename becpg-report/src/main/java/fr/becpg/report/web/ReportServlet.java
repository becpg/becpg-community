/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;
import fr.becpg.report.services.BeCPGReportService;
import fr.becpg.report.services.impl.BeCPGReportServiceImpl;


public class ReportServlet extends AbstractReportServlet {

	BeCPGReportService beCPGReportService = new BeCPGReportServiceImpl();

	private final static int BUFFER_SIZE = 2048;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 609805146293149060L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		String templateId = req.getParameter(ReportParams.TEMPLATE_ID_PARAM);
		String format = req.getParameter(ReportParams.PARAM_FORMAT);
		String lang = req.getParameter(ReportParams.PARAM_LANG);
		HttpSession session = req.getSession();
		
		@SuppressWarnings("unchecked")
		Map<String,byte[]> images = (Map<String, byte[]>) session.getAttribute(ReportParams.PARAM_IMAGES);
		if(images==null){
			images = new HashMap<String, byte[]>();
		}
		
		if(format.equals(ReportParams.PARAM_IMAGES)){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = req.getInputStream();
			try {
				byte[] buffer = new byte[BUFFER_SIZE];
				int l;
				// consume until EOF
				while ((l = in.read(buffer)) != -1) {
				
					out.write(buffer, 0, l);
				}
				images.put(templateId,out.toByteArray());
				
				
			} finally {
				in.close();
				out.close();
			}
			
			session.setAttribute(ReportParams.PARAM_IMAGES, images);
			resp.getWriter().write(ReportParams.RESP_OK);
			resp.getWriter().close();
			
		} else {
		
		
			OutputStream out =  resp.getOutputStream();
	
			
			beCPGReportService.generateReport(templateId, format , lang , req.getInputStream(), out, images);
			
			//Invalidate session to avoid keeping inMemory images
			session.invalidate();
			
			if(format.equals(ReportFormat.PDF.toString())){
			      resp.setContentType("application/pdf");
			      resp.addHeader("Content-Disposition", "attachment; filename=report.pdf" );
			}			
			else if(format.equals(ReportFormat.DOC.toString())){
				 resp.setContentType("application/ms-word");
			     resp.addHeader("Content-Disposition", "attachment; filename=report.doc" );
			}
			else{
				 resp.setContentType("application/vnd.xls");
			     resp.addHeader("Content-Disposition", "attachment; filename=report.xls" );
			}
			
		    
			out.flush();
			out.close();
		
		}
	}
	

}
