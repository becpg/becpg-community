package fr.becpg.repo.supplier.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.artworks.signature.SignatureService;
import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.VersionHelper;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.signature.SignatureProjectHelper;
import fr.becpg.repo.signature.SignatureProjectPlugin;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * <p>SupplierSignatureProjectPlugin class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@Service
public class SupplierSignatureProjectPlugin implements SignatureProjectPlugin {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(SupplierSignatureProjectPlugin.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private PersonService personService;

	@Autowired
	private EntityService entityService;

	@Autowired
	private SupplierPortalService supplierPortalService;

	@Autowired
	private EntityReportService entityReportService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private Repository repository;

	@Autowired
	private SignatureService signatureService;

	@Autowired
	private SignatureProjectHelper signatureProjectHelper;

	/** Constant <code>SUPPLIER_REFERENCING_PROJECT_TYPE="supplierReferencing"</code> */
	private static final String SUPPLIER_REFERENCING_PROJECT_TYPE = "supplierReferencing";

	/** Constant <code>SUPPLIER_REPORT_KIND="SupplierSheet"</code> */
	private static final String SUPPLIER_REPORT_KIND = "SupplierSheet";

	/** Constant <code>CLOSING_TASK_NAME_KEY="plm.supplier.portal.task.closing.name"</code> */
	private static final String CLOSING_TASK_NAME_KEY = "plm.supplier.portal.task.closing.name";

	/** Constant <code>VALIDATE_PROJECT_SCRIPT="cm:validateProjectEntity.js"</code> */
	private static final String VALIDATE_PROJECT_SCRIPT = "cm:validateProjectEntity.js";

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(String projectType) {
		return SUPPLIER_REFERENCING_PROJECT_TYPE.equals(projectType);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef prepareEntitySignatureFolder(ProjectData project, NodeRef entityNodeRef) {

		NodeRef supplierDocumentsFolder = supplierPortalService.getOrCreateSupplierDocumentsFolder(entityNodeRef);
		if (logger.isDebugEnabled()) {
			logger.debug("prepareEntitySignatureFolder - project: " + project.getNodeRef() + ", entity: " + entityNodeRef
					+ ", supplier documents folder: " + supplierDocumentsFolder);
		}

		Map<NodeRef, List<DeliverableListDataItem>> deliverableByDocuments = signatureProjectHelper.getDeliverableByDocuments(project);

		List<DeliverableListDataItem> toRemove = new ArrayList<>();

		for (ChildAssociationRef existingDocumentAssoc : nodeService.getChildAssocs(supplierDocumentsFolder, ContentModel.ASSOC_CONTAINS,
				RegexQNamePattern.MATCH_ALL)) {
			NodeRef existingDocument = existingDocumentAssoc.getChildRef();
			if (nodeService.exists(existingDocument)) {

				if ((nodeService.hasAspect(existingDocument, ReportModel.ASPECT_REPORT_TEMPLATES)
						&& deliverableByDocuments.containsKey(existingDocument))) {
					
					if (logger.isDebugEnabled()) {
						logger.debug("prepareEntitySignatureFolder - deleting outdated report document and its deliverables: " + existingDocument);
					}
					signatureService.cancelDocument(existingDocument);
					
					for (DeliverableListDataItem deliverable : deliverableByDocuments.get(existingDocument)) {
						if (deliverable.getNodeRef() != null) {
							toRemove.add(deliverable);
						}
					}
					nodeService.deleteNode(existingDocument);
					deliverableByDocuments.remove(existingDocument);
				}  else if (SignatureStatus.Prepared.toString().equals(nodeService.getProperty(existingDocument, SignatureModel.PROP_STATUS))) {
					if (logger.isDebugEnabled()) {
						logger.debug("prepareEntitySignatureFolder - resetting prepared document to Initialized: " + existingDocument);
					}
					List<NodeRef> recipients = associationService.getTargetAssocs(existingDocument, SignatureModel.ASSOC_RECIPIENTS);
					existingDocument = signatureService.cancelDocument(existingDocument);
					nodeService.setProperty(existingDocument, SignatureModel.PROP_STATUS, SignatureStatus.Initialized);
					associationService.update(existingDocument, SignatureModel.ASSOC_RECIPIENTS, recipients);
				}
				
				
			}
		}

		project.getDeliverableList().removeAll(toRemove);

		List<NodeRef> reports = entityReportService.getOrRefreshReportsOfKind(entityNodeRef, SUPPLIER_REPORT_KIND);

		if (reports.isEmpty()) {
			logger.error("No report with 'Supplier Sheet' type was found for entity: " + entityNodeRef);
		}

		List<NodeRef> suppliers = associationService.getTargetAssocs(project.getNodeRef(), PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
		if (logger.isDebugEnabled()) {
			logger.debug("prepareEntitySignatureFolder - reports of kind " + SUPPLIER_REPORT_KIND + ": " + reports + ", supplier accounts: " + suppliers);
		}

		for (NodeRef reportNodeRef : reports) {
			if (reportNodeRef != null) {
				NodeRef reportCopy = signatureProjectHelper.copyReport(supplierDocumentsFolder, reportNodeRef);
				associationService.update(reportCopy, SignatureModel.ASSOC_RECIPIENTS, suppliers);
				nodeService.addAspect(reportCopy, ReportModel.ASPECT_REPORT_TEMPLATES, new HashMap<>());
				if (logger.isDebugEnabled()) {
					logger.debug("prepareEntitySignatureFolder - copied report " + reportNodeRef + " to " + reportCopy + " with recipients: " + suppliers);
				}
			}
		}
		return supplierDocumentsFolder;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getExternalSignatureFolder(NodeRef projectNodeRef, List<NodeRef> documents, List<NodeRef> recipients) {

		NodeRef signatureFolder = null;

		NodeRef supplierNodeRef = findSupplierAccount(documents, recipients);
		if (logger.isDebugEnabled()) {
			logger.debug("getExternalSignatureFolder - project: " + projectNodeRef + ", documents: " + documents + ", recipients: " + recipients
					+ ", supplier account: " + supplierNodeRef);
		}

		if (supplierNodeRef != null) {
			signatureFolder = supplierPortalService.getOrCreateSupplierDestFolder(supplierNodeRef, recipients);
			nodeService.moveNode(projectNodeRef, signatureFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
			if (logger.isDebugEnabled()) {
				logger.debug("getExternalSignatureFolder - moved project " + projectNodeRef + " to supplier signature folder: " + signatureFolder);
			}
		}

		return signatureFolder;
	}

	/** {@inheritDoc} */
	@Override
	public void createOrUpdateClosingTask(ProjectData project, List<NodeRef> lastTasks, TaskListDataItem firstTask) {

		// do not create closing task if there are other tasks after
		for (NodeRef lastTask : lastTasks) {
			if (ProjectHelper.isPreviousTask(project, lastTask)) {
				if (logger.isDebugEnabled()) {
					logger.debug("createOrUpdateClosingTask - task " + lastTask + " is followed by another task, no closing task created");
				}
				return;
			}
		}

		String taskName = I18NUtil.getMessage(CLOSING_TASK_NAME_KEY);

		TaskListDataItem closingTask = project.getTaskList().stream().filter(task -> Objects.equals(task.getTaskName(), taskName)).findFirst()
				.orElseGet(() -> projectService.insertNewTask(project, lastTasks));

		closingTask.setTaskName(taskName);
		closingTask.setRefusedTask(firstTask);
		if (logger.isDebugEnabled()) {
			logger.debug("createOrUpdateClosingTask - closing task: " + closingTask.getNodeRef() + ", previous tasks: " + lastTasks
					+ ", refused task: " + (firstTask != null ? firstTask.getNodeRef() : null));
		}

		if (project.getCreator() != null && !project.getCreator().isBlank() && personService.personExists(project.getCreator())) {
			NodeRef creator = personService.getPerson(project.getCreator());
			if (!closingTask.getResources().contains(creator)) {
				closingTask.getResources().add(creator);
			}
		}
		

		lastTasks.stream().filter(Predicate.not(closingTask.getPrevTasks()::contains)).forEach(closingTask.getPrevTasks()::add);

		String closingTaskDeliverableName = project.getName();

		if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(closingTaskDeliverableName))) {
			project.getDeliverableList()
					.add(ProjectHelper.createDeliverable(closingTaskDeliverableName, I18NUtil.getMessage(CLOSING_TASK_NAME_KEY),
							DeliverableScriptOrder.Post, closingTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
									RepoConsts.SCRIPTS_FULL_PATH + RepoConsts.PATH_SEPARATOR + VALIDATE_PROJECT_SCRIPT)));
		}

	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> extractRecipients(NodeRef document) {
		List<NodeRef> supplierAccounts = supplierPortalService.extractSupplierAccountRefs(document);
		if (logger.isDebugEnabled()) {
			logger.debug("extractRecipients - document: " + document + ", supplier accounts: " + supplierAccounts);
		}
		return supplierAccounts;
	}

	/**
	 * <p>findSupplierAccount.</p>
	 *
	 * @param documents a {@link java.util.List} object
	 * @param recipients a {@link java.util.List} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef findSupplierAccount(List<NodeRef> documents, List<NodeRef> recipients) {
		for (NodeRef document : documents) {

			NodeRef entity = entityService.getEntityNodeRef(document, nodeService.getType(document));
			if (logger.isDebugEnabled()) {
				logger.debug("findSupplierAccount - document: " + document + ", entity: " + entity);
			}

			if (entity != null) {
				NodeRef supplierNodeRef = supplierPortalService.getSupplierNodeRef(entity);
				if (logger.isDebugEnabled()) {
					logger.debug("findSupplierAccount - entity " + entity + " is attached to supplier: " + supplierNodeRef);
				}

				if (supplierNodeRef != null) {
					return supplierNodeRef;
				}
			}
		}

		for (NodeRef recipient : recipients) {

			List<NodeRef> sourceSupplierAccountAssocs = associationService.getSourcesAssocs(recipient, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
			if (logger.isDebugEnabled()) {
				logger.debug("findSupplierAccount - recipient: " + recipient + ", supplier accounts sources: " + sourceSupplierAccountAssocs);
			}

			if ((sourceSupplierAccountAssocs != null) && !sourceSupplierAccountAssocs.isEmpty()) {
				for (NodeRef sourceSupplierAccountAssoc : sourceSupplierAccountAssocs) {
					if (!VersionHelper.isVersion(sourceSupplierAccountAssoc)
							&& PLMModel.TYPE_SUPPLIER.equals(nodeService.getType(sourceSupplierAccountAssoc))) {
						return sourceSupplierAccountAssoc;
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("findSupplierAccount - no supplier entity found from documents " + documents + " nor from recipients " + recipients
					+ ": the project will not be moved to a supplier signature folder");
		}
		return null;
	}

}
