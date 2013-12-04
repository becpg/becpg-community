package fr.becpg.repo.report.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.google.common.util.concurrent.Striped;

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

	private static final String PREF_REPORT_PREFIX = "fr.becpg.repo.report.";
	private static final String PREF_REPORT_SUFFIX = ".view";

	private static Log logger = LogFactory.getLog(EntityReportServiceImpl.class);

	private NamespaceService namespaceService;

	private PreferenceService preferenceService;

	private NodeService nodeService;

	private ContentService contentService;

	private FileFolderService fileFolderService;

	private BehaviourFilter policyBehaviourFilter;

	private ReportTplService reportTplService;

	private BeCPGReportEngine beCPGReportEngine;

	private MimetypeService mimetypeService;

	private AssociationService associationService;

	private PermissionService permissionService;

	private TransactionService transactionService;

	private Map<String, EntityReportExtractor> entityExtractors = new HashMap<String, EntityReportExtractor>();

	private Striped<Lock> stripedLocs = Striped.lazyWeakLock(20);

	@Override
	public void registerExtractor(String typeName, EntityReportExtractor extractor) {
		logger.debug("Register report extractor :" + typeName + " - " + extractor.getClass().getSimpleName());
		entityExtractors.put(typeName, extractor);
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
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

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void generateReport(final NodeRef entityNodeRef) {
		Lock lock = stripedLocs.get(entityNodeRef);
		if (logger.isDebugEnabled()) {
			logger.debug("Acquire lock for: " + entityNodeRef + " - " + Thread.currentThread().getName());
		}
		lock.lock();
		try {
			RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
				@Override
				public Object doWork() throws Exception {
					RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
						@Override
						public Object execute() {
							try {
								policyBehaviourFilter.disableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
								policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);

								if (logger.isDebugEnabled()) {
									logger.debug("Generate report: " + entityNodeRef + " - " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
								}

								if (nodeService.exists(entityNodeRef)) {
									generateReportImpl(entityNodeRef);
								}

							} finally {
								policyBehaviourFilter.enableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
								policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);
							}
							return null;
						}
					};
					return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, false);
				}
			};
			AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
		} finally {
			lock.unlock();
			if (logger.isDebugEnabled()) {
				logger.debug("Release lock for: " + entityNodeRef + " - " + Thread.currentThread().getName());
			}
		}
	}

	private void generateReportImpl(NodeRef entityNodeRef) {

		// #366 : force to use server locale for mlText fields
		I18NUtil.setLocale(Locale.getDefault());

		List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);
		// TODO here plug a template filter base on entityNodeRef
		tplsNodeRef = reportTplService.cleanDefaultTpls(tplsNodeRef);
		EntityReportData reportData = null;

		if (!tplsNodeRef.isEmpty()) {
			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);

			generateReports(entityNodeRef, tplsNodeRef, reportData.getXmlDataSource(), reportData.getDataObjects());
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("Reports generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
			}
		} else {
			logger.debug("No report tpls found, delete existing ones");
			updateReportsAssoc(entityNodeRef, new ArrayList<NodeRef>());
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

	private String getReportDocumentName(NodeRef entityNodeRef, NodeRef tplNodeRef, String reportFormat) {

		String documentName = String.format(REPORT_NAME, (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME),
				(String) nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME));
		String extension = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
		if (documentName.endsWith(RepoConsts.REPORT_EXTENSION_BIRT) && extension != null) {
			documentName = documentName.replace(RepoConsts.REPORT_EXTENSION_BIRT, extension.toLowerCase());
		}
		return documentName;
	}

	private NodeRef getReportDocumenNodeRef(NodeRef entityNodeRef, NodeRef tplNodeRef, String documentName) {

		String documentsFolderName = TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS);
		NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, documentsFolderName);
		if (documentsFolderNodeRef == null) {
			logger.warn("No folder: " + documentsFolderName + " found ");
			return null;
			// documentsFolderNodeRef = fileFolderService.create(entityNodeRef,
			// documentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
		}

		NodeRef documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, documentName);
		if (documentNodeRef == null) {
			documentNodeRef = fileFolderService.create(documentsFolderNodeRef, documentName, ReportModel.TYPE_REPORT).getNodeRef();
			// We don't update permissions. If permissions are modified -> admin
			// should use the action update-permissions from the reportTpl
			setPermissions(tplNodeRef, documentNodeRef);
			associationService.update(documentNodeRef, ReportModel.ASSOC_REPORT_TPL, tplNodeRef);
		}

		return documentNodeRef;
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
	public void generateReports(final NodeRef entityNodeRef, List<NodeRef> tplsNodeRef, final Element nodeElt, final Map<String, byte[]> images) {

		if (entityNodeRef == null) {
			throw new IllegalArgumentException("nodeRef is null");
		}

		if (tplsNodeRef.isEmpty()) {
			throw new IllegalArgumentException("tplsNodeRef is empty");
		}

		List<NodeRef> newReports = new ArrayList<NodeRef>();

		// generate reports
		for (final NodeRef tplNodeRef : tplsNodeRef) {

			if (nodeElt == null) {
				throw new IllegalArgumentException("nodeElt is null");
			}

			// prepare
			String reportFormat = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
			String documentName = getReportDocumentName(entityNodeRef, tplNodeRef, reportFormat);

			NodeRef documentNodeRef = getReportDocumenNodeRef(entityNodeRef, tplNodeRef, documentName);
			if (documentNodeRef != null) {
				// Run report
				try {
					ContentWriter writer = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);

					if (writer != null) {
						String mimetype = mimetypeService.guessMimetype(documentName);
						writer.setMimetype(mimetype);
						Map<String, Object> params = new HashMap<String, Object>();

						params.put(ReportParams.PARAM_IMAGES, images);
						params.put(ReportParams.PARAM_FORMAT, ReportFormat.valueOf(reportFormat));

						logger.debug("beCPGReportEngine createReport: " + entityNodeRef);
						beCPGReportEngine.createReport(tplNodeRef, nodeElt, writer.getContentOutputStream(), params);

					}
				} catch (ReportException e) {
					logger.error("Failed to execute report for template : " + tplNodeRef, e);
				}

				// Set Assoc
				newReports.add(documentNodeRef);
			}
		}

		updateReportsAssoc(entityNodeRef, newReports);
	}

	private void updateReportsAssoc(NodeRef entityNodeRef, List<NodeRef> newReports) {

		// #417 : refresh reports assoc (delete obsolete reports if we rename
		// entity)
		List<NodeRef> dbReports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS, false);
		for (NodeRef dbReport : dbReports) {
			if (!newReports.contains(dbReport)) {
				logger.debug("delete old report: " + dbReport);
				nodeService.addAspect(dbReport, ContentModel.ASPECT_TEMPORARY, null);
				nodeService.deleteNode(dbReport);
			}
		}
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

	public void setPermissions(NodeRef tplNodeRef, NodeRef documentNodeRef) {

		Set<AccessPermission> tplAccessPermissions = permissionService.getAllSetPermissions(tplNodeRef);
		permissionService.deletePermissions(documentNodeRef);
		boolean inheritParentPermissions = true;

		if (!tplAccessPermissions.isEmpty()) {
			logger.debug("set permissions size " + tplAccessPermissions.size());
			if (logger.isDebugEnabled()) {
				for (AccessPermission a : tplAccessPermissions) {
					logger.debug("Authority: " + a.getAuthority() + " status " + a.getAccessStatus() + " " + a.getPermission());
				}
			}
			for (AccessPermission tplAccessPermission : tplAccessPermissions) {
				if (!tplAccessPermission.isInherited()) {
					permissionService.setPermission(documentNodeRef, tplAccessPermission.getAuthority(), tplAccessPermission.getPermission(), true);
					inheritParentPermissions = false;
				}
			}
		}

		permissionService.setInheritParentPermissions(documentNodeRef, inheritParentPermissions);
	}

	@Override
	public boolean shouldGenerateReport(NodeRef entityNodeRef) {
		StopWatch watch = new StopWatch();
		if (logger.isDebugEnabled()) {
			watch.start();
		}
		try {
			Date modified = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
			Date generatedReportDate = (Date) nodeService.getProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);

			if (modified == null || generatedReportDate == null || modified.getTime() > generatedReportDate.getTime()) {
				return true;
			}

			List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);

			for (NodeRef tplNodeRef : tplsNodeRef) {
				modified = (Date) nodeService.getProperty(tplNodeRef, ContentModel.PROP_MODIFIED);
				if (modified == null || generatedReportDate == null || modified.getTime() > generatedReportDate.getTime()) {
					return true;
				}
			}

		
			return retrieveExtractor(entityNodeRef).shouldGenerateReport(entityNodeRef);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("ShouldGenerateReport executed in  " + watch.getTotalTimeSeconds() + " seconds ");
			}
		}
	}

	@Override
	public NodeRef getSelectedReport(NodeRef entityNodeRef) {

		String reportName = getSelectedReportName(entityNodeRef);

		List<NodeRef> dbReports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS, false);

		NodeRef ret = null;

		for (NodeRef reportNodeRef : dbReports) {
			if (permissionService.hasPermission(reportNodeRef, "Read") == AccessStatus.ALLOWED) {

				NodeRef reportTemplateNodeRef = reportTplService.getAssociatedReportTemplate(reportNodeRef);
				if (reportTemplateNodeRef != null) {
					String templateName = (String) this.nodeService.getProperty(reportTemplateNodeRef, ContentModel.PROP_NAME);
					if (templateName.endsWith(RepoConsts.REPORT_EXTENSION_BIRT)) {
						templateName = templateName.replace("." + RepoConsts.REPORT_EXTENSION_BIRT, "");
					}

					if ((Boolean) this.nodeService.getProperty(reportTemplateNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT)) {
						ret = reportNodeRef;
					}
					if (templateName.equalsIgnoreCase(reportName)) {
						return reportNodeRef;
					}
				}

			}
		}
		return ret;
	}

	@Override
	public String getSelectedReportName(NodeRef entityNodeRef) {

		String username = AuthenticationUtil.getFullyAuthenticatedUser();
		String typeName = nodeService.getType(entityNodeRef).toPrefixString(namespaceService).replace(":", "_");

		Map<String, Serializable> preferences = preferenceService.getPreferences(username);

		String reportName = (String) preferences.get(PREF_REPORT_PREFIX + typeName + PREF_REPORT_SUFFIX);

		if (logger.isDebugEnabled()) {
			logger.debug("Getting: " + reportName + " from preference for: " + username + " and type: " + typeName);
		}

		return reportName;
	}
}
