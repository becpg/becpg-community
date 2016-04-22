/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.DocxRenderOption;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.ModuleHandle;

import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.services.BeCPGReportService;
import fr.becpg.report.services.BirtPlatformListener;
import fr.becpg.report.services.TemplateCacheService;

public class BeCPGReportServiceImpl implements BeCPGReportService {

	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	

	private final static Log logger = LogFactory.getLog(BeCPGReportServiceImpl.class);
	
	/** The report engine. */
	private final IReportEngine reportEngine = BirtPlatformListener.getReportEngine();

	private final TemplateCacheService templateCacheService =  TemplateCacheServiceImpl.getInstance();
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void generateReport(String templateId, String format, String lang,
			InputStream dataSource, OutputStream out, Map<String, byte[]> images ) throws IOException {
		
		IRunAndRenderTask task = null;
		InputStream in = null;
		
		try {
			in = templateCacheService.getTemplate(templateId);
		
			IResourceLocator iResourceLocator = createRessourceLocator(templateId);
		
			IReportRunnable design = reportEngine.openReportDesign(templateId, in ,iResourceLocator);							
			
			// Create task to run and render the report,
			task  = reportEngine.createRunAndRenderTask(design);
			
			
			IRenderOption options;
			
			if(format.equals(ReportFormat.PDF.toString())){
			
				options = new RenderOption();
				options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
			}			
			else if(format.equals(ReportFormat.DOCX.toString())){
				
				options = new DocxRenderOption();
				options.setOutputFormat(ReportFormat.DOCX.toString());
			}
			else{
				// default format excel
				options = new EXCELRenderOption();
				options.setOutputFormat(ReportFormat.XLSX.toString());
			}
			
			options.setOutputStream(out);							
			task.setRenderOption(options);
			
			//Set the provided locale
			if(lang!=null && !lang.isEmpty()){
				task.setLocale(new Locale(lang));
			}
			
			// xml data
			task.getAppContext().put(KEY_XML_INPUTSTREAM, dataSource);
			
			// images
			if(images != null){
				for(Map.Entry<String, byte[]> entry : images.entrySet()){
					task.getAppContext().put(entry.getKey(), entry.getValue());
				}
			}					
			
			
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


	private IResourceLocator createRessourceLocator(final String templateId) {
		return new IResourceLocator() {
			
			@Override
			@SuppressWarnings("rawtypes")
			public URL findResource(ModuleHandle moduleHandle, String fileName, int type, Map appContext) {
				return findResource(fileName);
			}
			
			private URL findResource(String fileName) {
				try {
					return templateCacheService.getTemplateURL(buildFileName(templateId,fileName));
				} catch (Exception e) {
					logger.warn(e.getMessage());
					if(logger.isDebugEnabled()){
						logger.debug(e,e);
					}
				}
				return null;
			}

			private String buildFileName(String templateId, String fileName) {
				logger.debug("Build fileName for : "+templateId+" "+fileName);
				
				return templateId + "-" + (new File(fileName)).getName();
			}

			@Override
			public URL findResource(ModuleHandle moduleHandle, String fileName, int type) {
				return findResource(fileName);
			}
		};
	}
	
	
	
	

	

}
