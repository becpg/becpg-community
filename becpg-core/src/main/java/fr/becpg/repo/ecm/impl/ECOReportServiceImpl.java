package fr.becpg.repo.ecm.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.ECOReportService;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.helper.HierarchyHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

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
	
	private static Log logger = LogFactory.getLog(ECOServiceImpl.class);
	
	private ReportTplService reportTplService;
	private NodeService nodeService;
	private ContentService contentService;
	private MimetypeService mimetypeService;	
	private BeCPGReportEngine beCPGReportEngine;
	
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

	public void setBeCPGReportEngine(BeCPGReportEngine beCPGReportEngine) {
		this.beCPGReportEngine = beCPGReportEngine;
	}

	@Override
	//TODO move that to entityReport Service
	public void generateReport(ChangeOrderData ecoData) {
				
		NodeRef tplNodeRef = reportTplService.getSystemReportTemplate(ReportType.System, 
										null, 
										TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_ECO));
		
		if(tplNodeRef != null){
			
			//Prepare data source
			Element ecoXmlDataElt = extractXml(ecoData);						
			
			try{
				
				ContentWriter contentWriter = contentService.getWriter(ecoData.getNodeRef(), ContentModel.PROP_CONTENT, true);			
				String mimetype = mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF);
				contentWriter.setMimetype(mimetype);
				
				Map<String,Object> params = new HashMap<String, Object>();
				params.put(ReportParams.PARAM_FORMAT,ReportFormat.PDF);
				
				beCPGReportEngine.createReport(tplNodeRef, ecoXmlDataElt, contentWriter.getContentOutputStream(), params);
					
			}
			catch(Exception e){
				logger.error("Failed to run comparison report: ",  e);
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
			
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_HIERARCHY1, HierarchyHelper.getHierachyName((NodeRef)nodeService.getProperty(sl.getSourceItem(), BeCPGModel.PROP_PRODUCT_HIERARCHY1),nodeService));
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_HIERARCHY2, HierarchyHelper.getHierachyName((NodeRef)nodeService.getProperty(sl.getSourceItem(), BeCPGModel.PROP_PRODUCT_HIERARCHY2),nodeService));
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
