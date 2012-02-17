package fr.becpg.report.services.impl;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import fr.becpg.report.services.BeCPGReportService;
import fr.becpg.report.services.BirtPlatformListener;

public class BeCPGReportServiceImpl implements BeCPGReportService {

	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	

	/** The report engine. */
	private IReportEngine reportEngine = BirtPlatformListener.getReportEngine();
	
	public void test(){
		IRunAndRenderTask task = null;
		
		
	}
	
	
	
	
//	reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);
//	//in = new BufferedInputStream(reader.getContentInputStream());
//	in = reader.getContentInputStream();
//	IReportRunnable design = reportEngine.openReportDesign(in);							
	
//	String mimetype = mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF);
//	writer.setMimetype(mimetype);
//	
//	//Create task to run and render the report,
//	task = reportEngine.createRunAndRenderTask(design);
//	
//	IRenderOption options = new RenderOption();
//	out = writer.getContentOutputStream();
//	options.setOutputStream(out);							
//	options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
//	task.setRenderOption(options);
//	
//	// xml data
//	buffer = new ByteArrayInputStream( nodeElt.asXML().getBytes());
//	task.getAppContext().put(KEY_XML_INPUTSTREAM, buffer);
//	
//	// images
//	if(images != null){
//		for(Map.Entry<String, byte[]> entry : images.entrySet()){
//			task.getAppContext().put(entry.getKey(), entry.getValue());
//		}
//	}					
//	
//	IGetParameterDefinitionTask paramTask = reportEngine.createGetParameterDefinitionTask(design);											
//	
//	// hide all datalists and display visible ones
//	for(Object key : paramTask.getDefaultValues().keySet()){
//		if(((String)key).endsWith(PARAM_VALUE_HIDE_CHAPTER_SUFFIX)){
//			task.setParameterValue((String)key, Boolean.TRUE);
//		}
//	}							
//	
//	for(QName existingList : existingLists){
//		task.setParameterValue(existingList.getLocalName() + PARAM_VALUE_HIDE_CHAPTER_SUFFIX, Boolean.FALSE);
//	}
//	
//	task.run();
	
//	
//
//	//Create task to run and render the report,
//	logger.debug("Create task to run and render the report");
//	IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
//	
//	IRenderOption options = null;
//				
//	if(reportFormat.equals(ReportFormat.PDF)){
//	
//		options = new RenderOption();
//		options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
//	}			
//	else if(reportFormat.equals(ReportFormat.DOC)){
//		
//		options = new RenderOption();
//		options.setOutputFormat(ReportFormat.DOC.toString());
//	}
//	else{
//		// default format excel
//		options = new EXCELRenderOption();
//		options.setOutputFormat(ReportFormat.XLS.toString());
//	}
//	
	

}
