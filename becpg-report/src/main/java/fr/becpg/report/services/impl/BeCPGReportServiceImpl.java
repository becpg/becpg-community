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
package fr.becpg.report.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;

import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.services.BeCPGReportService;
import fr.becpg.report.services.BirtPlatformListener;
import fr.becpg.report.services.TemplateCacheService;

public class BeCPGReportServiceImpl implements BeCPGReportService {

	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	

	private static Log logger = LogFactory.getLog(BeCPGReportServiceImpl.class);
	
	/** The report engine. */
	private IReportEngine reportEngine = BirtPlatformListener.getReportEngine();

	private TemplateCacheService templateCacheService = new TemplateCacheServiceImpl();
	

	@SuppressWarnings("unchecked")
	@Override
	public void generateReport(String templateId, String format,
			InputStream dataSource, OutputStream out, Map<String, byte[]> images ) throws IOException {
		
		IRunAndRenderTask task = null;
		InputStream in = null;
		
		try {
			in = templateCacheService.getTemplate(templateId);
		
			IReportRunnable design = reportEngine.openReportDesign(in);							
			
			
			// Create task to run and render the report,
			task  = reportEngine.createRunAndRenderTask(design);
			
			
			IRenderOption options = null;
			
			if(format.equals(ReportFormat.PDF.toString())){
			
				options = new RenderOption();
				options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
			}			
			else if(format.equals(ReportFormat.DOC.toString())){
				
				options = new RenderOption();
				options.setOutputFormat(ReportFormat.DOC.toString());
			}
			else{
				// default format excel
				options = new EXCELRenderOption();
				options.setOutputFormat(ReportFormat.XLS.toString());
			}
			
			options.setOutputStream(out);							
			task.setRenderOption(options);
			
			// xml data
			task.getAppContext().put(KEY_XML_INPUTSTREAM, dataSource);
			
			// images
			if(images != null){
				for(Map.Entry<String, byte[]> entry : images.entrySet()){
					task.getAppContext().put(entry.getKey(), entry.getValue());
				}
			}					
			
	//		IGetParameterDefinitionTask paramTask = reportEngine.createGetParameterDefinitionTask(design);											
	//		
	//		 hide all datalists and display visible ones
	//		for(Object key : paramTask.getDefaultValues().keySet()){
	//			if(((String)key).endsWith(PARAM_VALUE_HIDE_CHAPTER_SUFFIX)){
	//				task.setParameterValue((String)key, Boolean.TRUE);
	//			}
	//		}							
	//		
	//		for(QName existingList : existingLists){
	//			task.setParameterValue(existingList.getLocalName() + PARAM_VALUE_HIDE_CHAPTER_SUFFIX, Boolean.FALSE);
	//		}
	
			
			task.run();
		} catch (Exception e) {
			logger.error(e,e);
		} finally {
			if(in!=null){
				in.close();
			}
			
			if(dataSource!=null){
				dataSource.close();
			}
			
			if(task!=null){
				task.close();
			}
			
		}
		
		
		
		
		
	}
	
	
	
	

	

}
