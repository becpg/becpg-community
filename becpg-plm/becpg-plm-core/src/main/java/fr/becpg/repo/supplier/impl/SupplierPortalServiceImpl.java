package fr.becpg.repo.supplier.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.BasicPasswordGenerator;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.artworks.signature.SignatureService;
import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.authentication.BeCPGUserAccount;
import fr.becpg.repo.authentication.BeCPGUserAccountService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.jscript.SupplierPortalHelper;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.supplier.SupplierPortalService;

@Service("supplierPortalService")
public class SupplierPortalServiceImpl implements SupplierPortalService {

	private static Log logger = LogFactory.getLog(SupplierPortalServiceImpl.class);

	@Autowired
	private AssociationService associationService;

	@Autowired
	private Repository repository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private SiteService siteService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private BeCPGUserAccountService beCPGUserAccountService;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private EntityService entityService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private PersonService personService;

	@Autowired
	private SignatureService signatureService;

	@Autowired
	private EntityReportService entityReportService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;
	
	@Value("${beCPG.sendToSupplier.projectName.format}")
	private String projectNameTpl = "{entity_cm:name} - {supplier_cm:name} - REFERENCING - {date_YYYY}";

	@Value("${beCPG.sendToSupplier.entityName.format}")
	private String entityNameTpl = "{entity_cm:name} - UPDATE - {date_YYYY}";

	@Override
	public String getProjectNameTpl() {
		return projectNameTpl;
	}

	@Override
	public String getEntityNameTpl() {
		return entityNameTpl;
	}

	@Override
	public NodeRef createSupplierProject(NodeRef entityNodeRef, NodeRef projectTemplateNodeRef, List<NodeRef> supplierAccountNodeRefs) {

		Date currentDate = Calendar.getInstance().getTime();

		boolean checkAccounts = (supplierAccountNodeRefs == null) || supplierAccountNodeRefs.isEmpty();

		NodeRef supplierNodeRef = checkSupplierNodeRef(entityNodeRef, checkAccounts);

		if (checkAccounts) {
			supplierAccountNodeRefs = associationService.getTargetAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
		}

		NodeRef destNodeRef = associationService.getTargetAssoc(projectTemplateNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST);
		if (destNodeRef == null) {
			throw new IllegalStateException(I18NUtil.getMessage("message.project-template.destination.missed"));
		}

		String projectName = repoService.getAvailableName(destNodeRef, createName(entityNodeRef, supplierNodeRef, projectNameTpl, currentDate),
				false);

		ProjectData projectData = new ProjectData();
		projectData.setName(projectName);
		projectData.setParentNodeRef(destNodeRef);
		projectData.setState(ProjectState.InProgress.toString());

		if (entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITY_V2)) {

			NodeRef branchNodeRef = null;

			if (entityNodeRef != supplierNodeRef) {

				NodeRef supplierDestFolder = getOrCreateSupplierDestFolder(supplierNodeRef, supplierAccountNodeRefs);

				String branchName = repoService.getAvailableName(supplierDestFolder,
						createName(entityNodeRef, supplierNodeRef, entityNameTpl, currentDate), false);

				if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH)) {
					branchNodeRef = entityNodeRef;

					List<AssociationRef> assocs = nodeService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);

					if (!assocs.isEmpty()) {
						entityNodeRef = assocs.get(0).getTargetRef();
					}
				} else {
					branchNodeRef = entityVersionService.createBranch(entityNodeRef, supplierDestFolder);
				}

				associationService.update(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO, entityNodeRef);
				nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_AUTO_MERGE_VERSIONTYPE, VersionType.MAJOR.toString());
				nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_AUTO_MERGE_COMMENTS, projectName);
				nodeService.setProperty(branchNodeRef, ContentModel.PROP_NAME, branchName);

				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SUPPLIER_DOCUMENTS));
				NodeRef documentsFolderNodeRef = nodeService.getChildByName(branchNodeRef, ContentModel.ASSOC_CONTAINS,
						(String) properties.get(ContentModel.PROP_NAME));
				if (documentsFolderNodeRef == null) {
					nodeService
							.createNode(branchNodeRef, ContentModel.ASSOC_CONTAINS,
									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
											QName.createValidLocalName(RepoConsts.PATH_SUPPLIER_DOCUMENTS)),
									ContentModel.TYPE_FOLDER, properties)
							.getChildRef();
				}
			} else {
				branchNodeRef = supplierNodeRef;
			}

			projectData.setEntities(Arrays.asList(branchNodeRef));

		}

		projectData.setProjectTpl(projectTemplateNodeRef);

		if (logger.isDebugEnabled()) {
			logger.debug("Creating supplier portal project : " + projectData.getName());
		}

		NodeRef projectNodeRef = alfrescoRepository.save(projectData).getNodeRef();

		associationService.update(projectNodeRef, PLMModel.ASSOC_SUPPLIERS, Collections.singletonList(supplierNodeRef));
		associationService.update(projectNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS, supplierAccountNodeRefs);

		return projectNodeRef;

	}

	@Override
	public NodeRef prepareSignatureProject(NodeRef projectNodeRef, List<NodeRef> originalDocuments) {

		originalDocuments = copyReports(originalDocuments);
		
		List<NodeRef> viewRecipients = associationService.getTargetAssocs(projectNodeRef, SignatureModel.ASSOC_RECIPIENTS);

		viewRecipients = projectService.extractResources(projectNodeRef, viewRecipients);

		associationService.update(projectNodeRef, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);

		List<NodeRef> recipients = extractPeople(viewRecipients);

		NodeRef supplierNodeRef = findSupplierAccount(originalDocuments, recipients);

		NodeRef supplierDestinationFolder = null;

		if (supplierNodeRef != null) {
			supplierDestinationFolder = getOrCreateSupplierDestFolder(supplierNodeRef, recipients);
			nodeService.moveNode(projectNodeRef, supplierDestinationFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
		}

		List<NodeRef> signedDocuments = prepareSignedDocuments(originalDocuments, viewRecipients, supplierDestinationFolder);

		ProjectData project = alfrescoRepository.findOne(projectNodeRef);

		TaskListDataItem rejectTask = createRejectTask(project, signedDocuments);

		NodeRef lastTask = createSignatureTasks(project, signedDocuments, recipients, rejectTask.getNodeRef(), rejectTask);

		createValidatingTask(project, originalDocuments, lastTask, rejectTask);

		alfrescoRepository.save(project);

		return projectNodeRef;
	}

	private List<NodeRef> prepareSignedDocuments(List<NodeRef> documents, List<NodeRef> viewRecipients, NodeRef supplierDestinationFolder) {
		List<NodeRef> signedDocuments = new ArrayList<>();

		NodeRef currentUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
			
		for (NodeRef document : documents) {
			try {
				policyBehaviourFilter.disableBehaviour(SignatureModel.ASPECT_SIGNATURE);

				associationService.update(document, SignatureModel.ASSOC_VALIDATOR, currentUser);

				associationService.update(document, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);

				if (supplierDestinationFolder != null) {
					Map<QName, Serializable> properties = new HashMap<>();

					String documentName = repoService.getAvailableName(supplierDestinationFolder, (String) nodeService.getProperty(document, ContentModel.PROP_NAME), false, true);

					properties.put(ContentModel.PROP_NAME, documentName);

					NodeRef documentCopy = nodeService.createNode(supplierDestinationFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
							ContentModel.TYPE_CONTENT, properties).getChildRef();

					ContentReader reader = contentService.getReader(document, ContentModel.PROP_CONTENT);
					ContentWriter writer = contentService.getWriter(documentCopy, ContentModel.PROP_CONTENT, true);
					writer.setEncoding(reader.getEncoding());
					writer.setMimetype(reader.getMimetype());

					nodeService.createAssociation(documentCopy, document, ContentModel.ASSOC_ORIGINAL);

					associationService.update(documentCopy, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);

					writer.putContent(reader);

					signedDocuments.add(documentCopy);
				} else {
					signedDocuments.add(document);
				}
			} finally {
				policyBehaviourFilter.enableBehaviour(SignatureModel.ASPECT_SIGNATURE);
			}
		}
		return signedDocuments;
	}

	private List<NodeRef> copyReports(List<NodeRef> originalDocuments) {
		List<NodeRef> documents = new ArrayList<>();
		
		for (NodeRef originalDocument : originalDocuments) {
			
			if (ReportModel.TYPE_REPORT.equals(nodeService.getType(originalDocument))) {
				
				NodeRef entity = entityService.getEntityNodeRef(originalDocument, ReportModel.TYPE_REPORT);
				
				String signedReportsName = TranslateHelper.getTranslatedPath("SignedReports");
				
				NodeRef signedReportsFolder = nodeService.getChildByName(entity, ContentModel.ASSOC_CONTAINS, signedReportsName);
				
				if (signedReportsFolder == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, signedReportsName);
					
					signedReportsFolder = nodeService.createNode(entity, ContentModel.ASSOC_CONTAINS,
									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(RepoConsts.PATH_SUPPLIER_DOCUMENTS)),
									ContentModel.TYPE_FOLDER, properties)
							.getChildRef();
				}
				
				documents.add(copyReport(signedReportsFolder, originalDocument));
			} else {
				documents.add(originalDocument);
			}
		}
		return documents;
	}

	private List<NodeRef> extractPeople(List<NodeRef> viewRecipients) {
		List<NodeRef> recipients = new ArrayList<>();

		for (NodeRef viewRecipient : viewRecipients) {
			QName type = nodeService.getType(viewRecipient);

			if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)) {
				List<NodeRef> members = associationService.getChildAssocs(viewRecipient, ContentModel.ASSOC_MEMBER);
				recipients.addAll(members);
			} else {
				recipients.add(viewRecipient);
			}
		}
		return recipients;
	}

	@Override
	public NodeRef prepareSupplierSignatures(NodeRef projectNodeRef, NodeRef taskNodeRef) {

		ProjectData project = alfrescoRepository.findOne(projectNodeRef);

		if (!project.getEntities().isEmpty()) {

			NodeRef entity = project.getEntities().get(0);

			String supplierFolderName = TranslateHelper.getTranslatedPath("SupplierDocuments");

			NodeRef supplierFolder = nodeService.getChildByName(entity, ContentModel.ASSOC_CONTAINS, supplierFolderName);

			if (supplierFolder == null) {

				Map<QName, Serializable> props = new HashMap<>();

				props.put(ContentModel.PROP_NAME, supplierFolderName);

				supplierFolder = nodeService
						.createNode(supplierFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER, props)
						.getChildRef();
			}

			copySupplierSheets(projectNodeRef, entity, supplierFolder);

			List<NodeRef> documentsToSign = findDocumentsToSign(supplierFolder);

			List<NodeRef> lastsTasks = new ArrayList<>();

			for (NodeRef documentToSign : documentsToSign) {

				associationService.update(documentToSign, ContentModel.ASSOC_ORIGINAL, List.of());

				List<NodeRef> viewRecipients = associationService.getTargetAssocs(documentToSign, SignatureModel.ASSOC_RECIPIENTS);

				viewRecipients = projectService.extractResources(projectNodeRef, viewRecipients);

				associationService.update(documentToSign, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);

				List<NodeRef> recipients = extractPeople(viewRecipients);

				NodeRef lastTask = createSignatureTasks(project, List.of(documentToSign), recipients, taskNodeRef, null);

				lastsTasks.add(lastTask);
			}

			createClosingTask(project, lastsTasks);

		}

		alfrescoRepository.save(project);

		return project.getNodeRef();
	}

	private void createClosingTask(ProjectData project, List<NodeRef> lastTasks) {
		TaskListDataItem closingTask = projectService.createNewTask(project);

		closingTask.setTaskName(I18NUtil.getMessage("plm.supplier.portal.task.closing.name"));

		NodeRef creator = personService.getPerson(project.getCreator());

		closingTask.getResources().add(creator);

		closingTask.getPrevTasks().addAll(lastTasks);

		project.getDeliverableList()
				.add(ProjectHelper.createDeliverable(projectNameTpl, I18NUtil.getMessage("plm.supplier.portal.task.closing.name"),
						DeliverableScriptOrder.Post, closingTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
								"/app:company_home/app:dictionary/app:scripts/cm:validateProjectEntity.js")));

	}

	private List<NodeRef> findDocumentsToSign(NodeRef folder) {

		List<NodeRef> docs = new ArrayList<>();

		List<NodeRef> children = associationService.getChildAssocs(folder, ContentModel.ASSOC_CONTAINS);

		for (NodeRef child : children) {

			QName type = nodeService.getType(child);

			if (ContentModel.TYPE_CONTENT.equals(type)) {

				List<NodeRef> recipients = associationService.getTargetAssocs(child, SignatureModel.ASSOC_RECIPIENTS);

				if ((recipients != null) && !recipients.isEmpty()) {

					String status = (String) nodeService.getProperty(child, SignatureModel.PROP_STATUS);

					if (!SignatureStatus.Signed.toString().equals(status)) {
						docs.add(child);
					}
				}
			} else if (ContentModel.TYPE_FOLDER.equals(type)) {
				docs.addAll(findDocumentsToSign(child));
			}
		}

		return docs;
	}

	private void copySupplierSheets(NodeRef projectNodeRef, NodeRef entity, NodeRef supplierFolder) {

		List<NodeRef> reports = entityReportService.getOrRefreshReportsOfKind(entity, "SupplierSheet");
		
		List<NodeRef> suppliers = associationService.getTargetAssocs(projectNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);

		for (NodeRef reportNodeRef : reports) {

			if (reportNodeRef != null) {
				NodeRef reportCopy = copyReport(supplierFolder, reportNodeRef);
				associationService.update(reportCopy, SignatureModel.ASSOC_RECIPIENTS, suppliers);
			}
		}
	}

	private NodeRef copyReport(NodeRef parentFolder, NodeRef reportNodeRef) {
		String reportName = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);

		int lastDotIndex = reportName.lastIndexOf(".");

		if (lastDotIndex != -1) {
			String nameWithoutExtension = reportName.substring(0, lastDotIndex);
			String extension = reportName.substring(lastDotIndex);
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).substring(0, 10);
			reportName = nameWithoutExtension + " - " + date + extension;
		}

		reportName = repoService.getAvailableName(parentFolder, reportName, false, true);

		Map<QName, Serializable> props = new HashMap<>();

		props.put(ContentModel.PROP_NAME, reportName);

		NodeRef reportCopy = nodeService
				.createNode(parentFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT, props)
				.getChildRef();

		ContentReader reader = contentService.getReader(reportNodeRef, ContentModel.PROP_CONTENT);
		ContentWriter writer = contentService.getWriter(reportCopy, ContentModel.PROP_CONTENT, true);
		writer.setEncoding(reader.getEncoding());
		writer.setMimetype(reader.getMimetype());

		writer.putContent(reader);
		
		return reportCopy;
	}

	private NodeRef findSupplierAccount(List<NodeRef> documents, List<NodeRef> recipients) {
		for (NodeRef document : documents) {

			NodeRef entity = entityService.getEntityNodeRef(document, nodeService.getType(document));

			if (entity != null) {
				NodeRef supplierNodeRef = getSupplierNodeRef(entity);

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

	private void createValidatingTask(ProjectData project, List<NodeRef> documents, NodeRef lastTask, TaskListDataItem rejectTask) {

		TaskListDataItem validatingTask = projectService.createNewTask(project);

		validatingTask.setRefusedTask(rejectTask);

		validatingTask.getPrevTasks().add(lastTask);

		validatingTask.setTaskName(I18NUtil.getMessage("signatureWorkflow.task-checkin.name"));

		validatingTask.setResources(new ArrayList<>());

		NodeRef currentUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());

		validatingTask.getResources().add(currentUser);

		String resourceFirstName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_FIRSTNAME);
		String resourceLastName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_LASTNAME);

		for (NodeRef document : documents) {

			String docName = (String) nodeService.getProperty(document, ContentModel.PROP_NAME);

			project.getDeliverableList()
					.add(ProjectHelper.createDeliverable(docName + resourceFirstName + resourceLastName + " - validate", docName,
							DeliverableScriptOrder.Post, validatingTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
									"/app:company_home/app:dictionary/app:scripts/cm:validate-signature.js")));

			DeliverableListDataItem signViewDeliverable = ProjectHelper
					.createDeliverable(docName + resourceFirstName + resourceLastName + " - validate - doc", docName, null, validatingTask, document);

			signViewDeliverable.setUrl(signatureService.getDocumentView(document, null, validatingTask.getNodeRef()));

			project.getDeliverableList().add(signViewDeliverable);
		}

	}

	private TaskListDataItem createRejectTask(ProjectData project, List<NodeRef> documents) {

		TaskListDataItem rejectTask = projectService.createNewTask(project);

		rejectTask.setTaskName(I18NUtil.getMessage("signatureWorkflow.task-reject.name"));

		rejectTask.setDescription(I18NUtil.getMessage("signatureWorkflow.task-reject.description"));

		rejectTask.setState(TaskState.Cancelled.toString());

		rejectTask.setResources(new ArrayList<>());

		NodeRef currentUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());

		rejectTask.getResources().add(currentUser);

		String resourceFirstName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_FIRSTNAME);
		String resourceLastName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_LASTNAME);

		for (NodeRef document : documents) {

			String docName = (String) nodeService.getProperty(document, ContentModel.PROP_NAME);

			project.getDeliverableList()
					.add(ProjectHelper.createDeliverable(docName + resourceFirstName + resourceLastName + " - reject", docName,
							DeliverableScriptOrder.Pre, rejectTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
									"/app:company_home/app:dictionary/app:scripts/cm:reject-signature.js")));
			project.getDeliverableList().add(ProjectHelper.createDeliverable(docName + resourceFirstName + resourceLastName + " - reject - doc",
					docName, null, rejectTask, document));
		}

		project.getTaskList().add(rejectTask);

		return rejectTask;

	}

	private NodeRef createSignatureTasks(ProjectData project, List<NodeRef> documents, List<NodeRef> recipients, NodeRef firstTask,
			TaskListDataItem rejectTask) {

		NodeRef previousTask = firstTask;

		for (NodeRef recipient : recipients) {

			String resourceFirstName = (String) nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME);
			String resourceLastName = (String) nodeService.getProperty(recipient, ContentModel.PROP_LASTNAME);

			TaskListDataItem newTask = projectService.createNewTask(project);

			newTask.setRefusedTask(rejectTask);

			newTask.getPrevTasks().add(previousTask);

			newTask.setTaskName(nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME) + " "
					+ nodeService.getProperty(recipient, ContentModel.PROP_LASTNAME) + " - SIGNATURE");

			newTask.getResources().add(recipient);

			newTask.setTaskName(I18NUtil.getMessage("signatureWorkflow.task-signature.name"));
			newTask.setNotificationFrequency(7);
			newTask.setInitialNotification(-1);
			newTask.setNotificationAuthorities(List.of(recipient));

			for (NodeRef doc : documents) {

				String docName = (String) nodeService.getProperty(doc, ContentModel.PROP_NAME);

				project.getDeliverableList()
						.add(ProjectHelper.createDeliverable(docName + resourceFirstName + resourceLastName + " - prepare", docName,
								DeliverableScriptOrder.Pre, newTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
										"/app:company_home/app:dictionary/app:scripts/cm:prepare-signature.js")));
				project.getDeliverableList()
						.add(ProjectHelper.createDeliverable(docName + resourceFirstName + resourceLastName + " - url", docName, null, newTask, doc));
				project.getDeliverableList()
						.add(ProjectHelper.createDeliverable(docName + resourceFirstName + resourceLastName + " - sign", docName,
								DeliverableScriptOrder.Post, newTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
										"/app:company_home/app:dictionary/app:scripts/cm:sign-document.js")));
			}

			previousTask = newTask.getNodeRef();
		}

		return previousTask;
	}

	@Override
	public String createName(NodeRef entityNodeRef, NodeRef supplierNodeRef, String nameFormat, Date currentDate) {

		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(nameFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(propQnameAlt, entityNodeRef, supplierNodeRef, currentDate);
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(propQname, entityNodeRef, supplierNodeRef, currentDate);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);

		return sb.toString().replace("-  -", "-").replace("- -", "-").trim().replaceAll("\\-$|\\(\\)", "").trim().replaceAll("\\-$|\\(\\)", "")
				.trim();

	}

	private String extractPropText(String propQname, NodeRef entityNodeRef, NodeRef supplierNodeRef, Date currentDate) {
		if (propQname != null) {
			if ((propQname.indexOf("supplier_") == 0) && (supplierNodeRef != null) && !supplierNodeRef.equals(entityNodeRef)) {

				QName prop = QName.createQName(propQname.replace("supplier_", ""), namespaceService);

				String entityProp = (String) nodeService.getProperty(entityNodeRef, prop);
				String supplierProp = (String) nodeService.getProperty(supplierNodeRef, prop);

				// case of supplier name already contained in entity name
				if ((entityProp != null) && (supplierProp != null) && entityProp.toLowerCase().contains(supplierProp.toLowerCase())) {
					return null;
				}

				return supplierProp;
			} else if (propQname.indexOf("entity_") == 0) {
				return (String) nodeService.getProperty(entityNodeRef, QName.createQName(propQname.replace("entity_", ""), namespaceService));
			} else if (propQname.indexOf("date_") == 0) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(propQname.replace("date_", ""));
				return dateFormat.format(currentDate);
			}

		}
		return "";
	}

	@Override
	public NodeRef getSupplierNodeRef(NodeRef entityNodeRef) {
		NodeRef supplierNodeRef = null;
		if (PLMModel.TYPE_SUPPLIER.equals(nodeService.getType(entityNodeRef))) {
			if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_SUB_ENTITY)) {
				supplierNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_PARENT_ENTITY);
			}

			if (supplierNodeRef == null) {
				supplierNodeRef = entityNodeRef;
			}

		} else {
			supplierNodeRef = associationService.getTargetAssoc(entityNodeRef, PLMModel.ASSOC_SUPPLIERS);
		}
		return supplierNodeRef;
	}

	private NodeRef checkSupplierNodeRef(NodeRef entityNodeRef, boolean checkAccounts) {
		NodeRef supplierNodeRef = null;

		if (entityNodeRef != null) {
			supplierNodeRef = getSupplierNodeRef(entityNodeRef);

			if (supplierNodeRef != null) {
				if (checkAccounts) {
					List<NodeRef> accountNodeRefs = associationService.getTargetAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
					if ((accountNodeRefs == null) || accountNodeRefs.isEmpty()) {
						throw new IllegalStateException(I18NUtil.getMessage("message.supplier-account.missed"));
					}
				}

			} else {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.missed"));
			}
		}

		return supplierNodeRef;
	}

	@Override
	public NodeRef getOrCreateSupplierDestFolder(NodeRef supplierNodeRef, List<NodeRef> resources) {

		if (supplierNodeRef != null) {

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SUPPLIER_DOCUMENTS));
			NodeRef documentsFolderNodeRef = nodeService.getChildByName(supplierNodeRef, ContentModel.ASSOC_CONTAINS,
					(String) properties.get(ContentModel.PROP_NAME));
			if (documentsFolderNodeRef == null) {
				documentsFolderNodeRef = nodeService
						.createNode(supplierNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
										QName.createValidLocalName(RepoConsts.PATH_SUPPLIER_DOCUMENTS)),
								ContentModel.TYPE_FOLDER, properties)
						.getChildRef();
			}

			SiteInfo siteInfo = siteService.getSite(SupplierPortalHelper.SUPPLIER_SITE_ID);

			if (siteInfo != null) {

				NodeRef documentLibraryNodeRef = siteService.getContainer(SupplierPortalHelper.SUPPLIER_SITE_ID, SiteService.DOCUMENT_LIBRARY);
				if (documentLibraryNodeRef != null) {
					Locale currentLocal = I18NUtil.getLocale();
					Locale currentContentLocal = I18NUtil.getContentLocale();

					try {
						I18NUtil.setLocale(Locale.getDefault());
						I18NUtil.setContentLocale(null);

						migrateOldSupplierDestFolder(supplierNodeRef, documentLibraryNodeRef, documentsFolderNodeRef);

						if (!siteInfo.equals(siteService.getSite(supplierNodeRef))) {
							repoService.moveNode(supplierNodeRef, documentLibraryNodeRef);
						}

						for (NodeRef resourceRef : resources) {
							permissionService.setPermission(supplierNodeRef,
									(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME), PermissionService.COORDINATOR, true);
						}
					} finally {
						I18NUtil.setLocale(currentLocal);
						I18NUtil.setContentLocale(currentContentLocal);
					}

				}

			} else {

				NodeRef resourceRef = resources.get(0);
				NodeRef destFolder = repository.getUserHome(resourceRef);

				repoService.moveNode(supplierNodeRef, destFolder);

				permissionService.setPermission(destFolder, PermissionService.GROUP_PREFIX + PLMGroup.ReferencingMgr.toString(),
						PermissionService.COORDINATOR, true);

			}

			return documentsFolderNodeRef;

		}

		return null;
	}

	private void migrateOldSupplierDestFolder(NodeRef supplierNodeRef, NodeRef documentLibraryNodeRef, NodeRef documentsFolderNodeRef) {

		NodeRef destFolder =  nodeService.getChildByName(documentLibraryNodeRef, ContentModel.ASSOC_CONTAINS,
				PropertiesHelper.cleanFolderName(I18NUtil.getMessage("path.referencing")));
			
		if (destFolder != null) {

			NodeRef oldSupplierDestNodeRef = repoService.getFolderByPath(destFolder, supplierNodeRef.getId());

			if (oldSupplierDestNodeRef == null) {

				oldSupplierDestNodeRef = nodeService.getChildByName(destFolder, ContentModel.ASSOC_CONTAINS,
						PropertiesHelper.cleanFolderName((String) nodeService.getProperty(supplierNodeRef, ContentModel.PROP_NAME)));
			}

			if (oldSupplierDestNodeRef != null) {

				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(oldSupplierDestNodeRef);

				for (ChildAssociationRef childAssoc : childAssocs) {
					NodeRef childNode = childAssoc.getChildRef();
					nodeService.moveNode(childNode, documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, childAssoc.getQName());
				}

				nodeService.deleteNode(oldSupplierDestNodeRef);
			}
		}

	}

	@Override
	public NodeRef createExternalUser(String email, String firstName, String lastName, boolean notify, Map<QName, Serializable> extraProps) {

		final boolean isAdminOrSystem = AuthenticationUtil.isRunAsUserTheSystemUser() || authorityService.hasAdminAuthority();

		boolean hasAccess = AuthenticationUtil
				.runAsSystem(() -> isAdminOrSystem || authorityService.getAuthoritiesForUser(AuthenticationUtil.getFullyAuthenticatedUser())
						.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUserMgr.toString()));

		if (hasAccess) {

			BasicPasswordGenerator pwdGen = new BasicPasswordGenerator();
			pwdGen.setPasswordLength(10);

			if ((email == null) || email.isBlank()) {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.missing-email"));
			}

			if ((firstName == null) || firstName.isBlank()) {
				firstName = email;
			}

			if ((lastName == null) || lastName.isBlank()) {
				firstName = email;
			}

			BeCPGUserAccount userAccount = new BeCPGUserAccount();
			userAccount.setEmail(email);
			userAccount.setUserName(email);
			userAccount.setFirstName(firstName);
			userAccount.setLastName(lastName);
			userAccount.setPassword(pwdGen.generatePassword());
			userAccount.setNotify(notify);
			userAccount.getAuthorities().add(SystemGroup.ExternalUser.toString());

			if(extraProps == null) {
				extraProps = new HashMap<>();
			}
			extraProps.put(	ContentModel.PROP_EMAIL_FEED_DISABLED, true);
			
			userAccount.getExtraProps().putAll(extraProps);

			return beCPGUserAccountService.getOrCreateUser(userAccount);

		}

		throw new IllegalAccessError("You should be member of ExternalUserMgr");
	}

}
