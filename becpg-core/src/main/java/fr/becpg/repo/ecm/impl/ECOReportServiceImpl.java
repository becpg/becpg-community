package fr.becpg.repo.ecm.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.ECOReportService;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

public class ECOReportServiceImpl implements ECOReportService {

	private static final String TAG_ECO = "eco";
	private static final String TAG_CALCULATED_CHARACTS = "calculatedCharacts";
	private static final String TAG_CALCULATED_CHARACT = "calculatedCharact";
	private static final String ATTR_SOURCEITEM_HIERARCHY1 = "sourceItemHierarchy1";
	private static final String ATTR_SOURCEITEM_HIERARCHY2 = "sourceItemHierarchy2";
	private static final String ATTR_SOURCEITEM_NAME = "sourceItemName";
	private static final String ATTR_SOURCEITEM_CODE = "sourceItemCode";
	private static final String ATTR_CHARACT_NAME = "charactName";
	private static final String ATTR_SOURCE_VALUE = "sourceValue";
	private static final String ATTR_TARGET_VALUE = "targetValue";
	private static final String ATTR_IS_COST = "isCost";
	
	private static final Integer DEFAULT_PROJECTED_QTY = 1;
	
	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	
	private static Log logger = LogFactory.getLog(ECOServiceImpl.class);
	
	private ReportTplService reportTplService;
	private NodeService nodeService;
	private ContentService contentService;
	private MimetypeService mimetypeService;	
	private IReportEngine reportEngine;
	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setReportEngine(IReportEngine reportEngine) {
		this.reportEngine = reportEngine;
	}

	@SuppressWarnings("unchecked")
	@Override
	//TODO move that to entityReport Service
	public void generateReport(ChangeOrderData ecoData) {
				
		NodeRef tplNodeRef = reportTplService.getSystemReportTemplate(ReportType.System, 
										null, 
										TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_ECO));
		
		if(tplNodeRef != null){
			
			//Prepare data source
			Element ecoXmlDataElt = extractXml(ecoData);						
			InputStream in = null;
			InputStream bais = null;
			OutputStream out = null;
			try{
				
				ContentWriter contentWriter = contentService.getWriter(ecoData.getNodeRef(), ContentModel.PROP_CONTENT, true);			
				String mimetype = mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF);
				contentWriter.setMimetype(mimetype);
				out = contentWriter.getContentOutputStream();
				
				ContentReader reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);
				in = reader.getContentInputStream();
				IReportRunnable design = reportEngine.openReportDesign(in);
									
				//Create task to run and render the report,
				logger.debug("Create task to run and render the report");
				IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
				
				//Update data source
				logger.debug("Update data source");
			    bais = new ByteArrayInputStream( ecoXmlDataElt.asXML().getBytes());
				task.getAppContext().put(KEY_XML_INPUTSTREAM, bais);
								
				IRenderOption options = new RenderOption();
				options.setOutputStream(out);							
				options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
				task.setRenderOption(options);					
				task.run();
				task.close();							

					
			}
			catch(Exception e){
				logger.error("Failed to run comparison report: ",  e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(bais);
				IOUtils.closeQuietly(out);
			}
			
		}
		
	}
	
	private Element extractXml(ChangeOrderData ecoData){
		
		Document document = DocumentHelper.createDocument();		
		Element ecoElt = document.addElement(TAG_ECO);		
		ecoElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), ecoData.getName());
		ecoElt.addAttribute(BeCPGModel.PROP_CODE.getLocalName(), ecoData.getCode());		
		Element calculatedCharactsElt = ecoElt.addElement(TAG_CALCULATED_CHARACTS);
		
		for(SimulationListDataItem sl : ecoData.getSimulationList()){
		
			Element calculatedCharactElt = calculatedCharactsElt.addElement(TAG_CALCULATED_CHARACT);
			
			Boolean isCost = Boolean.FALSE;
			QName charactQName = nodeService.getType(sl.getCharact());
			if(charactQName != null && charactQName.isMatch(BeCPGModel.TYPE_COST)){
				isCost = Boolean.TRUE;
			}
			
			Integer projectedQty = (Integer)nodeService.getProperty(sl.getSourceItem(), BeCPGModel.PROP_PROJECTED_QTY);
			if(projectedQty == null){
				projectedQty = DEFAULT_PROJECTED_QTY;
			}
			
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_HIERARCHY1, (String)nodeService.getProperty(sl.getSourceItem(), BeCPGModel.PROP_PRODUCT_HIERARCHY1));
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_HIERARCHY2, (String)nodeService.getProperty(sl.getSourceItem(), BeCPGModel.PROP_PRODUCT_HIERARCHY2));
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_CODE, (String)nodeService.getProperty(sl.getSourceItem(), BeCPGModel.PROP_CODE));
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_NAME, (String)nodeService.getProperty(sl.getSourceItem(), ContentModel.PROP_NAME));
			calculatedCharactElt.addAttribute(ATTR_CHARACT_NAME, (String)nodeService.getProperty(sl.getCharact(), ContentModel.PROP_NAME));
			calculatedCharactElt.addAttribute(ATTR_SOURCE_VALUE, sl.getSourceValue() ==null ? "" : sl.getSourceValue().toString());
			calculatedCharactElt.addAttribute(ATTR_TARGET_VALUE, sl.getTargetValue() ==null ? "" : sl.getTargetValue().toString());
			calculatedCharactElt.addAttribute(BeCPGModel.PROP_PROJECTED_QTY.getLocalName(), projectedQty.toString());
			calculatedCharactElt.addAttribute(ATTR_IS_COST, isCost.toString());
			
		}
		
		return ecoElt;
	}

}
