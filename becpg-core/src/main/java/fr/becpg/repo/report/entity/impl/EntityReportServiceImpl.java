package fr.becpg.repo.report.entity.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractor;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

public class EntityReportServiceImpl implements EntityReportService{

	
	private static final String KEY_XML_INPUTSTREAM = "org.eclipse.datatools.enablement.oda.xml.inputStream";
	
	private static final String PARAM_VALUE_HIDE_CHAPTER_SUFFIX = "HideChapter";
		
	private static final String DEFAULT_EXTRACTOR = "default";
	
	private static Log logger = LogFactory.getLog(EntityReportServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The content service. */
	private ContentService contentService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;	
	
	private EntityListDAO entityListDAO;
	
	/** The report engine. */
	private IReportEngine reportEngine;
	
	/** The mimetype service. */
	private MimetypeService mimetypeService;
	
	
	private ReportTplService reportTplService;
	
	
	private Map<String, EntityReportExtractor>  entityExtractors = new HashMap<String, EntityReportExtractor>();
	
	
			
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the content service.
	 *
	 * @param contentService the new content service
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	
	
	/**
	 * Sets the report engine.
	 *
	 * @param reportEngine the new report engine
	 */
	public void setReportEngine(IReportEngine reportEngine){
		this.reportEngine = reportEngine;
	}
	
	/**
	 * Sets the mimetype service.
	 *
	 * @param mimetypeService the new mimetype service
	 */
	public void setMimetypeService(MimetypeService mimetypeService){
		this.mimetypeService = mimetypeService;
	}		


	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}
	
	
	
	/**
	 * @param entityExtractors the entityExtractors to set
	 */
	public void setEntityExtractors(Map<String, EntityReportExtractor> entityExtractors) {
		this.entityExtractors = entityExtractors;
	}
	

	@Override
	public void generateReport(NodeRef entityNodeRef) {
		List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);			
		//TODO here plug a template filter base on entityNodeRef
		tplsNodeRef = reportTplService.cleanDefaultTpls(tplsNodeRef);		
		
	
		if(!tplsNodeRef.isEmpty()){
			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}
	
			EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);
			
			generateReports(entityNodeRef, tplsNodeRef, reportData.getXmlDataSource() , reportData.getDataObjects());	
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug( "Reports generated in  "
						+ watch.getTotalTimeSeconds() + " seconds");
			}
    	}			
		
	}
	
	
	
	private EntityReportExtractor retrieveExtractor(NodeRef entityNodeRef) {
		QName type = nodeService.getType(entityNodeRef);
		
		EntityReportExtractor ret = entityExtractors.get(type.getLocalName());
		if(ret==null){
			ret = entityExtractors.get(DEFAULT_EXTRACTOR);
		}

		return ret;
	}

	/**
	 * Get the node where the document will we stored.
	 *
	 * @param nodeRef the node ref
	 * @param tplNodeRef the tpl node ref
	 * @return the document content writer
	 */
	public ContentWriter getDocumentContentWriter(NodeRef nodeRef, NodeRef tplNodeRef){
		
		ContentWriter contentWriter = null;
		
		if((Boolean)nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT)){
			contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		}
		else{
			// the doc will be stored in the documents folder of the node			
			NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			
			if(nodeService.getType(parentNodeRef).isMatch(BeCPGModel.TYPE_ENTITY_FOLDER)){
			
				String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
				NodeRef documentsFolderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);		
				if(documentsFolderNodeRef == null){
					
					documentsFolderNodeRef = fileFolderService.create(parentNodeRef, documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
				}
				
				String documentName = (String)nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME);
				NodeRef documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, documentName);
				if(documentNodeRef == null){
					
					documentNodeRef = fileFolderService.create(documentsFolderNodeRef, documentName, ContentModel.TYPE_CONTENT).getNodeRef();
				}
				
				contentWriter = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);
			}
			else{
				logger.debug("The node does not have a entityFolder so we cannot store doc in the folder 'Documents'.");
			}			
		}
		
		return contentWriter;
	}
	

	/**
	 * Method that generates reports.
	 *
	 * @param nodeRef the node ref
	 * @param tplsNodeRef the tpls node ref
	 * @param nodeElt the node elt
	 * @param images the images
	 */
	@SuppressWarnings("unchecked")
	public void generateReports(NodeRef nodeRef, List<NodeRef> tplsNodeRef, Element nodeElt, Map<String, byte[]> images) {
		
		if(nodeRef == null){
			throw new IllegalArgumentException("nodeRef is null");
		}
		
		if(tplsNodeRef.isEmpty()){
			throw new IllegalArgumentException("tplsNodeRef is empty");
		}
		
		if(nodeElt == null){
			throw new IllegalArgumentException("nodeElt is null");
		}		
		
		// calculate the visible datalists
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
		List<QName> existingLists = entityListDAO.getExistingListsQName(listContainerNodeRef);		
		
		// generate reports
		for(NodeRef tplNodeRef : tplsNodeRef){        			
			InputStream in = null;
			OutputStream out =null;
			InputStream buffer = null;
			IRunAndRenderTask task = null;
			//prepare
			try{							
				ContentReader reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);
				in = reader.getContentInputStream();
				IReportRunnable design = reportEngine.openReportDesign(in);							
				
				//Run report		
				ContentWriter writer = getDocumentContentWriter(nodeRef, tplNodeRef);
			
				if(writer != null){
				
					String mimetype = mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF);
					writer.setMimetype(mimetype);
					
					//Create task to run and render the report,
					task = reportEngine.createRunAndRenderTask(design);
					
					IRenderOption options = new RenderOption();
					out = writer.getContentOutputStream();
					options.setOutputStream(out);							
					options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
					task.setRenderOption(options);
					
					// xml data
					buffer = new ByteArrayInputStream( nodeElt.asXML().getBytes());
					task.getAppContext().put(KEY_XML_INPUTSTREAM, buffer);
					
					// images
					if(images != null){
						for(Map.Entry<String, byte[]> entry : images.entrySet()){
							task.getAppContext().put(entry.getKey(), entry.getValue());
						}
					}					
					
					IGetParameterDefinitionTask paramTask = reportEngine.createGetParameterDefinitionTask(design);											
					
					// hide all datalists and display visible ones
					for(Object key : paramTask.getDefaultValues().keySet()){
						if(((String)key).endsWith(PARAM_VALUE_HIDE_CHAPTER_SUFFIX)){
							task.setParameterValue((String)key, Boolean.TRUE);
						}
					}							
					
					for(QName existingList : existingLists){
						task.setParameterValue(existingList.getLocalName() + PARAM_VALUE_HIDE_CHAPTER_SUFFIX, Boolean.FALSE);
					}
					
					task.run();
					// set reportNodeGenerated property to now
			        nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, new Date());
				}  				
			}
			catch(Exception e){
				logger.error("Failed to execute report: ",  e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(buffer);
				IOUtils.closeQuietly(out);
				if(task!=null){
					task.close();
				}
			}
		}
	}
	
	
	
	/**
	 * Get the report templates to generate.
	 *
	 * @param nodeRef the product node ref
	 * @return the report tpls to generate
	 */
	public List<NodeRef> getReportTplsToGenerate(NodeRef nodeRef) {
		
		List<NodeRef> tplsToReturnNodeRef = new ArrayList<NodeRef>();
		
		// system reports
		QName nodeType = nodeService.getType(nodeRef);
		List<NodeRef> tplsNodeRef = reportTplService.getSystemReportTemplates(ReportType.Document, nodeType);										
		
		for(NodeRef tplNodeRef : tplsNodeRef){			
			
			tplsToReturnNodeRef.add(tplNodeRef);
		}
		
		// selected user reports
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, ReportModel.ASSOC_REPORT_TEMPLATES);
		
		for(AssociationRef assocRef : assocRefs){
			
			NodeRef tplNodeRef = assocRef.getTargetRef();			
			tplsToReturnNodeRef.add(tplNodeRef);
		}		
		
		return tplsToReturnNodeRef;		
	}
	
	

	

	@Override
	public boolean isReportUpToDate(NodeRef entityNodeRef) {

        Date reportModified = (Date)nodeService.getProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);

        // report not generated
        if(reportModified == null){
        	logger.debug("report not up to date");
        	return false;
        }

        // check modified date (modified is always bigger than reportModified so a delta is defined)
        Date modified = (Date)nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);                
     
        //Test if report is older than 15s
        if(reportModified.getTime()+1000*15-modified.getTime()<0){
        	if(logger.isDebugEnabled()){
	        	logger.debug("report not up to date :");
	        	logger.debug("modified: " + ISO8601DateFormat.format(modified) + " - reportModified: " + ISO8601DateFormat.format(reportModified));
        	}
        	return false;
        }
        

        return true;

		
	}


	
}
