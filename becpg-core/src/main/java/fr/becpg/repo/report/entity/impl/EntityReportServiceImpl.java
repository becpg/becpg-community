package fr.becpg.repo.report.entity.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractor;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

@Service
public class EntityReportServiceImpl implements EntityReportService {

	private static final String DEFAULT_EXTRACTOR = "default";
	private static final String REPORT_NAME = "%s - %s";

	private static Log logger = LogFactory.getLog(EntityReportServiceImpl.class);

	/** The node service. */
	private NodeService nodeService;

	/** The content service. */
	private ContentService contentService;

	/** The file folder service. */
	private FileFolderService fileFolderService;

	private ReportTplService reportTplService;

	private BeCPGReportEngine beCPGReportEngine;

	private MimetypeService mimetypeService;

	private AssociationService associationService;

	private Map<String, EntityReportExtractor> entityExtractors = new HashMap<String, EntityReportExtractor>();

	@Override
	public void registerExtractor(String typeName, EntityReportExtractor extractor) {
		logger.debug("Register report extractor :" + typeName + " - " + extractor.getClass().getSimpleName());
		entityExtractors.put(typeName, extractor);
	}

	/**
	 * Sets the node service.
	 * 
	 * @param nodeService
	 *            the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Sets the content service.
	 * 
	 * @param contentService
	 *            the new content service
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * Sets the file folder service.
	 * 
	 * @param fileFolderService
	 *            the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setBeCPGReportEngine(BeCPGReportEngine beCPGReportEngine) {
		this.beCPGReportEngine = beCPGReportEngine;
	}

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public void generateReport(NodeRef entityNodeRef) {

		List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);
		// TODO here plug a template filter base on entityNodeRef
		tplsNodeRef = reportTplService.cleanDefaultTpls(tplsNodeRef);

		if (!tplsNodeRef.isEmpty()) {
			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);

			generateReports(entityNodeRef, tplsNodeRef, reportData.getXmlDataSource(), reportData.getDataObjects());
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("Reports generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
			}
		} else {
			logger.debug("No report tpls found");
		}

		// set reportNodeGenerated property to now
		nodeService.setProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, Calendar.getInstance().getTime());
	}

	@Override
	public String getXmlReportDataSource(NodeRef entityNodeRef) {
		EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);

		return reportData.getXmlDataSource().asXML();
	}

	private EntityReportExtractor retrieveExtractor(NodeRef entityNodeRef) {
		QName type = nodeService.getType(entityNodeRef);

		EntityReportExtractor ret = entityExtractors.get(type.getLocalName());
		if (ret == null) {
			logger.debug("extractor :" + type.getLocalName() + " not found returning " + DEFAULT_EXTRACTOR);
			ret = entityExtractors.get(DEFAULT_EXTRACTOR);
		}

		return ret;
	}

	/**
	 * Get the node where the document will we stored.
	 * 
	 * @param entityNodeRef
	 *            the node ref
	 * @param tplNodeRef
	 *            the tpl node ref
	 * @return the document content writer
	 */
	public ContentWriter getDocumentContentWriter(NodeRef entityNodeRef, NodeRef tplNodeRef, List<NodeRef> newReports) {

		ContentWriter contentWriter = null;
		
		//Entity V2 is a container

		String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
		NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);
		if (documentsFolderNodeRef == null) {

			documentsFolderNodeRef = fileFolderService.create(entityNodeRef, documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
		}

		String documentName = String.format(REPORT_NAME, (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME),
				(String) nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME));
		NodeRef documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, documentName);
		if (documentNodeRef == null) {

			documentNodeRef = fileFolderService.create(documentsFolderNodeRef, documentName, ReportModel.TYPE_REPORT).getNodeRef();
			associationService.update(documentNodeRef, ReportModel.ASSOC_REPORT_TPL, tplNodeRef);						
		}

		newReports.add(documentNodeRef);
		contentWriter = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);

		return contentWriter;
	}

	/**
	 * Method that generates reports.
	 * 
	 * @param entityNodeRef
	 *            the node ref
	 * @param tplsNodeRef
	 *            the tpls node ref
	 * @param nodeElt
	 *            the node elt
	 * @param images
	 *            the images
	 */
	public void generateReports(NodeRef entityNodeRef, List<NodeRef> tplsNodeRef, Element nodeElt, Map<String, byte[]> images) {

		if (entityNodeRef == null) {
			throw new IllegalArgumentException("nodeRef is null");
		}

		if (tplsNodeRef.isEmpty()) {
			throw new IllegalArgumentException("tplsNodeRef is empty");
		}

		if (nodeElt == null) {
			throw new IllegalArgumentException("nodeElt is null");
		}

		List<NodeRef> newReports = new ArrayList<NodeRef>();

		// generate reports
		for (NodeRef tplNodeRef : tplsNodeRef) {

			// prepare
			try {

				// Run report
				ContentWriter writer = getDocumentContentWriter(entityNodeRef, tplNodeRef, newReports);

				if (writer != null) {
					String mimetype = mimetypeService.guessMimetype(RepoConsts.REPORT_EXTENSION_PDF);
					writer.setMimetype(mimetype);
					Map<String, Object> params = new HashMap<String, Object>();

					params.put(ReportParams.PARAM_IMAGES, images);
					params.put(ReportParams.PARAM_FORMAT, ReportFormat.PDF);

					logger.debug("beCPGReportEngine createReport: " + entityNodeRef);
					beCPGReportEngine.createReport(tplNodeRef, nodeElt, writer.getContentOutputStream(), params);
				}
			} catch (ReportException e) {
				logger.error("Failed to execute report for template : " + tplNodeRef, e);
			}
		}

//		// refresh reports assoc
//		List<NodeRef> dbReports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS);
//		for (NodeRef dbReport : dbReports) {
//			if (!newReports.contains(dbReport)) {
//				logger.debug("delete old report: " + dbReport);
//				nodeService.deleteNode(dbReport);
//			}
//		}
		associationService.update(entityNodeRef, ReportModel.ASSOC_REPORTS, newReports);
	}

	/**
	 * Get the report templates to generate.
	 * 
	 * @param nodeRef
	 *            the product node ref
	 * @return the report tpls to generate
	 */
	public List<NodeRef> getReportTplsToGenerate(NodeRef nodeRef) {

		List<NodeRef> tplsToReturnNodeRef = new ArrayList<NodeRef>();

		// system reports
		QName nodeType = nodeService.getType(nodeRef);
		List<NodeRef> tplsNodeRef = reportTplService.getSystemReportTemplates(ReportType.Document, nodeType);

		for (NodeRef tplNodeRef : tplsNodeRef) {

			tplsToReturnNodeRef.add(tplNodeRef);
		}

		// selected user reports
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, ReportModel.ASSOC_REPORT_TEMPLATES);

		for (AssociationRef assocRef : assocRefs) {

			NodeRef tplNodeRef = assocRef.getTargetRef();
			tplsToReturnNodeRef.add(tplNodeRef);
		}

		return tplsToReturnNodeRef;
	}

}
