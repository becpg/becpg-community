package fr.becpg.repo.supplier.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.signature.SignatureProjectPlugin;
import fr.becpg.repo.supplier.SupplierPortalService;

@Service
public class SupplierSignatureProjectPlugin implements SignatureProjectPlugin {

	private static final Log logger = LogFactory.getLog(SupplierSignatureProjectPlugin.class);
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private ContentService contentService;
	
	@Autowired
	private RepoService repoService;

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

	private static final String SUPPLIER_REFERENCING_PROJECT_TYPE = "supplierReferencing";
	
	private static final String SUPPLIER_REPORT_KIND = "SupplierSheet";
	
	@Override
	public boolean applyTo(String projectType) {
		return SUPPLIER_REFERENCING_PROJECT_TYPE.equals(projectType);
	}

	@Override
	public NodeRef prepareEntitySignatureFolder(NodeRef projectNodeRef, NodeRef entityNodeRef) {
		
		NodeRef supplierDocumentsFolder = supplierPortalService.getOrCreateSupplierDocumentsFolder(entityNodeRef);

		List<NodeRef> reports = entityReportService.getOrRefreshReportsOfKind(entityNodeRef, SUPPLIER_REPORT_KIND);
		
		if (reports.isEmpty()) {
			logger.warn("No report with 'Supplier Sheet' type was found for entity: " + entityNodeRef);
		}
		
		List<NodeRef> suppliers = associationService.getTargetAssocs(projectNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);

		for (NodeRef reportNodeRef : reports) {

			if (reportNodeRef != null) {
				NodeRef reportCopy = copyReport(supplierDocumentsFolder, reportNodeRef);
				associationService.update(reportCopy, SignatureModel.ASSOC_RECIPIENTS, suppliers);
			}
		}
		return supplierDocumentsFolder;
	}
	
	@Override
	public NodeRef getExternalSignatureFolder(NodeRef projectNodeRef, List<NodeRef> documents, List<NodeRef> recipients) {
		
		NodeRef signatureFolder = null;
		
		NodeRef supplierNodeRef = findSupplierAccount(documents, recipients);
		
		if (supplierNodeRef != null) {
			signatureFolder = supplierPortalService.getOrCreateSupplierDestFolder(supplierNodeRef, recipients);
			nodeService.moveNode(projectNodeRef, signatureFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
		}
		
		return signatureFolder;
	}

	@Override
	public void createOrUpdateClosingTask(ProjectData project, List<NodeRef> lastTasks) {
		
		String taskName = I18NUtil.getMessage("plm.supplier.portal.task.closing.name");
		
		TaskListDataItem closingTask = project.getTaskList().stream().filter(task -> task.getTaskName().equals(taskName)).findFirst().orElseGet(() -> projectService.createNewTask(project));

		closingTask.setTaskName(taskName);
	
		NodeRef creator = personService.getPerson(project.getCreator());
	
		if (!closingTask.getResources().contains(creator)) {
			closingTask.getResources().add(creator);
		}
		
		lastTasks.stream().filter(Predicate.not(closingTask.getPrevTasks()::contains)).forEach(closingTask.getPrevTasks()::add);
	
		String closingTaskDeliverableName = project.getName();
		
		if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(closingTaskDeliverableName))) {
			project.getDeliverableList().add(ProjectHelper.createDeliverable(closingTaskDeliverableName, I18NUtil.getMessage("plm.supplier.portal.task.closing.name"),
					DeliverableScriptOrder.Post, closingTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
							"/app:company_home/app:dictionary/app:scripts/cm:validateProjectEntity.js")));
		}
	
	}

	@Override
	public List<NodeRef> extractRecipients(NodeRef document) {
		return supplierPortalService.extractSupplierAccountRefs(document);
	}

	private NodeRef findSupplierAccount(List<NodeRef> documents, List<NodeRef> recipients) {
		for (NodeRef document : documents) {
	
			NodeRef entity = entityService.getEntityNodeRef(document, nodeService.getType(document));
	
			if (entity != null) {
				NodeRef supplierNodeRef = supplierPortalService.getSupplierNodeRef(entity);
	
				if (supplierNodeRef != null) {
					return supplierNodeRef;
				}
			}
		}
	
		for (NodeRef recipient : recipients) {
	
			List<NodeRef> sourceSupplierAccountAssocs = associationService.getSourcesAssocs(recipient, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
	
			if ((sourceSupplierAccountAssocs != null) && !sourceSupplierAccountAssocs.isEmpty()) {
				for (NodeRef sourceSupplierAccountAssoc : sourceSupplierAccountAssocs) {
					if (PLMModel.TYPE_SUPPLIER.equals(nodeService.getType(sourceSupplierAccountAssoc))) {
						return sourceSupplierAccountAssoc;
					}
				}
			}
		}
	
		return null;
	}

	private NodeRef copyReport(NodeRef parentFolder, NodeRef reportNodeRef) {
		String reportName = extractReportName(reportNodeRef);
		
		NodeRef existingReportCopy = nodeService.getChildByName(parentFolder, ContentModel.ASSOC_CONTAINS, reportName);
		
		if (existingReportCopy != null && SignatureStatus.Initialized.toString().equals(nodeService.getProperty(existingReportCopy, SignatureModel.PROP_STATUS))) {
			return existingReportCopy;
		}
		
		reportName = repoService.getAvailableName(parentFolder, reportName, false, true);
		
		Map<QName, Serializable> props = new HashMap<>();

		props.put(ContentModel.PROP_NAME, reportName);

		NodeRef reportCopy = nodeService
				.createNode(parentFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT, props)
				.getChildRef();
		
		nodeService.setProperty(reportCopy, ContentModel.PROP_OWNER, AuthenticationUtil.SYSTEM_USER_NAME);

		ContentReader reader = contentService.getReader(reportNodeRef, ContentModel.PROP_CONTENT);
		ContentWriter writer = contentService.getWriter(reportCopy, ContentModel.PROP_CONTENT, true);
		writer.setEncoding(reader.getEncoding());
		writer.setMimetype(reader.getMimetype());

		writer.putContent(reader);
		
		return reportCopy;
	}

	private String extractReportName(NodeRef reportNodeRef) {
		String reportName = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);

		int lastDotIndex = reportName.lastIndexOf(".");
		
		if (lastDotIndex != -1) {
			String nameWithoutExtension = reportName.substring(0, lastDotIndex);
			String extension = reportName.substring(lastDotIndex);
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).substring(0, 10);
			reportName = nameWithoutExtension + " - " + date + extension;
		}
		return reportName;
	}
}