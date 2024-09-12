package fr.becpg.repo.signature.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.artworks.signature.SignatureService;
import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.signature.SignatureProjectPlugin;
import fr.becpg.repo.signature.SignatureProjectService;

/**
 * <p>SignatureProjectServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("signatureProjectService")
public class SignatureProjectServiceImpl implements SignatureProjectService {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;
	
	@Autowired
	private ProjectService projectService;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private ContentService contentService;
	
	@Autowired
	private RepoService repoService;

	@Autowired
	private PersonService personService;

	@Autowired
	private SignatureService signatureService;

	@Autowired
	private Repository repository;
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private SignatureProjectPlugin[] signatureProjectPlugins;

	/** {@inheritDoc} */
	@Override
	public NodeRef prepareSignatureProject(NodeRef projectNodeRef, List<NodeRef> originalDocuments) {

		originalDocuments = copyReports(originalDocuments);
		
		List<NodeRef> viewRecipients = associationService.getTargetAssocs(projectNodeRef, SignatureModel.ASSOC_RECIPIENTS);

		viewRecipients = projectService.extractResources(projectNodeRef, viewRecipients);

		associationService.update(projectNodeRef, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);

		List<NodeRef> preparedDocuments = prepareDocuments(projectNodeRef, originalDocuments, viewRecipients);

		ProjectData project = alfrescoRepository.findOne(projectNodeRef);

		TaskListDataItem rejectTask = createRejectTask(project, preparedDocuments);
		
		List<NodeRef> recipients = AuthorityHelper.extractPeople(viewRecipients);

		NodeRef lastTask = createOrUpdateSignatureTasks(project, preparedDocuments, recipients, rejectTask.getNodeRef(), rejectTask);

		createValidatingTask(project, originalDocuments, lastTask, rejectTask);

		alfrescoRepository.save(project);

		return projectNodeRef;
	}
	
	/** {@inheritDoc} */
	@Override
	public NodeRef createEntitySignatureTasks(NodeRef projectNodeRef, NodeRef previousTask, String projectType) {
	
		ProjectData project = alfrescoRepository.findOne(projectNodeRef);
	
		if (!project.getEntities().isEmpty()) {
	
			NodeRef entityNodeRef = project.getEntities().get(0);
	
			NodeRef entitySignatureFolder = null;
			
			SignatureProjectPlugin signatureProjectPlugin = findSignatureProjectPlugin(projectType);
			
			if (signatureProjectPlugin != null) {
				entitySignatureFolder = signatureProjectPlugin.prepareEntitySignatureFolder(projectNodeRef, entityNodeRef);
			} else {
				entitySignatureFolder = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath("Documents"));
			}
			
			List<NodeRef> documentsToSign = findDocumentsToSign(entitySignatureFolder);
	
			List<NodeRef> lastsTasks = new ArrayList<>();
	
			for (NodeRef documentToSign : documentsToSign) {
	
				associationService.update(documentToSign, ContentModel.ASSOC_ORIGINAL, List.of());
	
				List<NodeRef> viewRecipients = associationService.getTargetAssocs(documentToSign, SignatureModel.ASSOC_RECIPIENTS);
	
				viewRecipients = projectService.extractResources(projectNodeRef, viewRecipients);
	
				associationService.update(documentToSign, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);
	
				List<NodeRef> recipients = AuthorityHelper.extractPeople(viewRecipients);
	
				NodeRef lastTask = createOrUpdateSignatureTasks(project, List.of(documentToSign), recipients, previousTask, null);
	
				lastsTasks.add(lastTask);
			}
	
			if (signatureProjectPlugin != null) {
				signatureProjectPlugin.createOrUpdateClosingTask(project, lastsTasks);
			}
	
		}
	
		alfrescoRepository.save(project);
	
		return project.getNodeRef();
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> extractRecipients(NodeRef document) {
		
		if (signatureProjectPlugins != null) {
			for (SignatureProjectPlugin signatureProjectPlugin : signatureProjectPlugins) {
				List<NodeRef> recipients = signatureProjectPlugin.extractRecipients(document);
				if (!recipients.isEmpty()) {
					return recipients;
				}
			}
		}
		
		return new ArrayList<>();
	}

	private SignatureProjectPlugin findSignatureProjectPlugin(String projectType) {
		
		if (signatureProjectPlugins != null) {
			for (SignatureProjectPlugin signatureProjectPlugin : signatureProjectPlugins) {
				if (signatureProjectPlugin.applyTo(projectType)) {
					return signatureProjectPlugin;
				}
			}
		}
		
		return null;
	}

	private List<NodeRef> prepareDocuments(NodeRef projectNodeRef, List<NodeRef> documents, List<NodeRef> viewRecipients) {

		List<NodeRef> preparedDocuments = new ArrayList<>();
		
		NodeRef externalSignatureFolder = getExternalSignatureFolder(projectNodeRef, documents, viewRecipients);
		
		NodeRef currentUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
			
		for (NodeRef document : documents) {
			try {
				policyBehaviourFilter.disableBehaviour(SignatureModel.ASPECT_SIGNATURE);
	
				associationService.update(document, SignatureModel.ASSOC_VALIDATOR, currentUser);
	
				associationService.update(document, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);
						
				if (externalSignatureFolder != null) {
					Map<QName, Serializable> properties = new HashMap<>();
	
					String documentName = repoService.getAvailableName(externalSignatureFolder, (String) nodeService.getProperty(document, ContentModel.PROP_NAME), false, true);
	
					properties.put(ContentModel.PROP_NAME, documentName);
	
					NodeRef documentCopy = nodeService.createNode(externalSignatureFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
							ContentModel.TYPE_CONTENT, properties).getChildRef();
	
					ContentReader reader = contentService.getReader(document, ContentModel.PROP_CONTENT);
					ContentWriter writer = contentService.getWriter(documentCopy, ContentModel.PROP_CONTENT, true);
					writer.setEncoding(reader.getEncoding());
					writer.setMimetype(reader.getMimetype());
	
					nodeService.createAssociation(documentCopy, document, ContentModel.ASSOC_ORIGINAL);
	
					associationService.update(documentCopy, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);
	
					writer.putContent(reader);
	
					preparedDocuments.add(documentCopy);
				} else {
					preparedDocuments.add(document);
				}
			} finally {
				policyBehaviourFilter.enableBehaviour(SignatureModel.ASPECT_SIGNATURE);
			}
		}
		
		return preparedDocuments;
		
	}

	private NodeRef getExternalSignatureFolder(NodeRef projectNodeRef, List<NodeRef> documents, List<NodeRef> viewRecipients) {
		if (signatureProjectPlugins != null) {
			for (SignatureProjectPlugin signatureProjectPlugin : signatureProjectPlugins) {
				NodeRef externalSignatureFolder = signatureProjectPlugin.getExternalSignatureFolder(projectNodeRef, documents, viewRecipients);
				if (externalSignatureFolder != null) {
					return externalSignatureFolder;
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

	private NodeRef createOrUpdateSignatureTasks(ProjectData project, List<NodeRef> documents, List<NodeRef> recipients, NodeRef previousTask,
			TaskListDataItem rejectTask) {

		for (NodeRef recipient : recipients) {

			String resourceFirstName = (String) nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME);
			String resourceLastName = (String) nodeService.getProperty(recipient, ContentModel.PROP_LASTNAME);

			String taskName = I18NUtil.getMessage("signatureWorkflow.task-signature.name");
			
			TaskListDataItem newTask = project.getTaskList().stream()
					.filter(task -> task.getResources() != null && task.getResources().contains(recipient))
					.filter(task -> task.getTaskName() != null && task.getTaskName().equals(taskName))
					.findFirst()
					.orElseGet(() -> projectService.createNewTask(project));
			
			newTask.setRefusedTask(rejectTask);

			if (!newTask.getPrevTasks().contains(previousTask)) {
				newTask.getPrevTasks().add(previousTask);
			}
			
			newTask.setTaskName(taskName);
			
			if (!newTask.getResources().contains(recipient)) {
				newTask.getResources().add(recipient);
			}

			newTask.setTaskName(I18NUtil.getMessage("signatureWorkflow.task-signature.name"));
			newTask.setNotificationFrequency(7);
			newTask.setInitialNotification(-1);
			newTask.setNotificationAuthorities(List.of(recipient));

			for (NodeRef doc : documents) {

				String docName = (String) nodeService.getProperty(doc, ContentModel.PROP_NAME);

				String prepareDeliverableName = docName + resourceFirstName + resourceLastName + " - prepare";
				
				if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(prepareDeliverableName))) {
					project.getDeliverableList().add(ProjectHelper.createDeliverable(prepareDeliverableName, docName,
							DeliverableScriptOrder.Pre, newTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
									"/app:company_home/app:dictionary/app:scripts/cm:prepare-signature.js")));
				}
				
				String urlDeliverableName = docName + resourceFirstName + resourceLastName + " - url";
				
				if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(urlDeliverableName))) {
					project.getDeliverableList().add(ProjectHelper.createDeliverable(urlDeliverableName, docName, null, newTask, doc));
				}
				
				String signDeliverableName = docName + resourceFirstName + resourceLastName + " - sign";
				
				if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(signDeliverableName))) {
					project.getDeliverableList().add(ProjectHelper.createDeliverable(signDeliverableName, docName,
							DeliverableScriptOrder.Post, newTask, BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
									"/app:company_home/app:dictionary/app:scripts/cm:sign-document.js")));
				}
			}

			previousTask = newTask.getNodeRef();
		}

		return previousTask;
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
							ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER, properties).getChildRef();
				}
				
				documents.add(copyReport(signedReportsFolder, originalDocument));
			} else {
				documents.add(originalDocument);
			}
		}
		return documents;
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
}
