package fr.becpg.repo.signature.impl;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.artworks.signature.SignatureService;
import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.signature.SignatureProjectHelper;
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

	private static final Log logger = LogFactory.getLog(SignatureProjectServiceImpl.class);
	
	// Constants
	private static final String TASK_SIGNATURE_NAME_KEY = "signatureWorkflow.task-signature.name";
	private static final String TASK_REJECT_NAME_KEY = "signatureWorkflow.task-reject.name";
	private static final String TASK_REJECT_DESCRIPTION_KEY = "signatureWorkflow.task-reject.description";
	private static final String TASK_CHECKIN_NAME_KEY = "signatureWorkflow.task-checkin.name";
	private static final String PREPARE_SCRIPT = "cm:prepare-signature.js";
	private static final String VALIDATE_SCRIPT = "cm:validate-signature.js";
	private static final String REJECT_SCRIPT = "cm:reject-signature.js";
	private static final String SIGN_SCRIPT = "cm:sign-document.js";
	private static final int NOTIFICATION_FREQUENCY = 7;
	private static final int INITIAL_NOTIFICATION = -1;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

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
	private SignatureProjectHelper signatureProjectHelper;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private SignatureProjectPlugin[] signatureProjectPlugins;

	/** {@inheritDoc} */
	@Override
	public NodeRef prepareSignatureProject(NodeRef projectNodeRef, List<NodeRef> originalDocuments) {
		originalDocuments = signatureProjectHelper.copyReports(originalDocuments);
		List<NodeRef> viewRecipients = associationService.getTargetAssocs(projectNodeRef, SignatureModel.ASSOC_RECIPIENTS);
		viewRecipients = projectService.extractResources(projectNodeRef, viewRecipients);
		associationService.update(projectNodeRef, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);
		List<NodeRef> preparedDocuments = prepareDocuments(projectNodeRef, originalDocuments, viewRecipients);
		ProjectData project = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
		TaskListDataItem rejectTask = createRejectTask(project, preparedDocuments);
		List<NodeRef> recipients = AuthorityHelper.extractPeople(viewRecipients);
		NodeRef lastTask = createOrUpdateSignatureTasks(project, preparedDocuments, recipients, rejectTask.getNodeRef(), rejectTask);
		createValidatingTask(project, originalDocuments, lastTask, rejectTask);
		project.setDirtyTaskTree(true);
		alfrescoRepository.save(project);
		return projectNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createEntitySignatureTasks(NodeRef projectNodeRef, NodeRef previousTask, String projectType) {
		ProjectData project = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
		if (!project.getEntities().isEmpty()) {
			TaskListDataItem firstTask = (TaskListDataItem) alfrescoRepository.findOne(ProjectHelper.findAncestorTask(previousTask, associationService));
			NodeRef entityNodeRef = project.getEntities().get(0);
			NodeRef entitySignatureFolder = null;
			SignatureProjectPlugin signatureProjectPlugin = findSignatureProjectPlugin(projectType);
			if (signatureProjectPlugin != null) {
				entitySignatureFolder = signatureProjectPlugin.prepareEntitySignatureFolder(project, entityNodeRef);
			} else {
				entitySignatureFolder = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
						TranslateHelper.getTranslatedPath("Documents"));
			}
			List<NodeRef> documentsToSign = signatureProjectHelper.findDocumentsToSign(entitySignatureFolder, false);
			List<NodeRef> documentsAlreadySigned = signatureProjectHelper.findDocumentsToSign(entitySignatureFolder, true);

			Map<NodeRef, List<DeliverableListDataItem>> deliverableByDocuments = signatureProjectHelper.getDeliverableByDocuments(project);
			for (NodeRef doc : documentsAlreadySigned) {

				List<DeliverableListDataItem> associatedDeliverables = deliverableByDocuments.getOrDefault(doc, new ArrayList<>());
				associatedDeliverables.forEach(d -> d.setState(DeliverableState.Completed));

				if (logger.isDebugEnabled()) {
					logger.debug("Marked " + associatedDeliverables.size() + " deliverables as completed for document: " + doc);
				}
			}
			List<NodeRef> lastsTasks = new ArrayList<>();
			for (NodeRef documentToSign : documentsToSign) {
				associationService.update(documentToSign, ContentModel.ASSOC_ORIGINAL, List.of());
				List<NodeRef> viewRecipients = associationService.getTargetAssocs(documentToSign, SignatureModel.ASSOC_RECIPIENTS);
				if (viewRecipients.isEmpty() && (signatureProjectPlugin != null)) {
					viewRecipients.addAll(signatureProjectPlugin.extractRecipients(documentToSign));
				}
				viewRecipients = projectService.extractResources(projectNodeRef, viewRecipients);
				associationService.update(documentToSign, SignatureModel.ASSOC_RECIPIENTS, viewRecipients);
				List<NodeRef> recipients = AuthorityHelper.extractPeople(viewRecipients);
				NodeRef lastTask = createOrUpdateSignatureTasks(project, List.of(documentToSign), recipients, previousTask, firstTask);
				lastsTasks.add(lastTask);
			}
			closeSignedTasks(project);
			if (signatureProjectPlugin != null) {
				signatureProjectPlugin.createOrUpdateClosingTask(project, lastsTasks, firstTask);
			}
			project.setDirtyTaskTree(true);
		}
		alfrescoRepository.save(project);
		return project.getNodeRef();
	}

	private void closeSignedTasks(ProjectData project) {
		for (TaskListDataItem task : project.getTaskList()) {
			// Get deliverables associated with this task
			List<DeliverableListDataItem> relatedDeliverables = project.getDeliverableList().stream()
					.filter(d -> (d.getTasks() != null) && d.getTasks().contains(task.getNodeRef())).toList();

			// Check if all related deliverables are signed
			boolean allSigned = !relatedDeliverables.isEmpty() && relatedDeliverables.stream().allMatch(d -> (d.getContent() != null)
					&& SignatureStatus.Signed.toString().equals(nodeService.getProperty(d.getContent(), SignatureModel.PROP_STATUS)));

			if (allSigned) {
				task.setTaskState(TaskState.Completed);
			}
		}
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
					String documentName = repoService.getAvailableName(externalSignatureFolder,
							(String) nodeService.getProperty(document, ContentModel.PROP_NAME), false, true);
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
		TaskListDataItem validatingTask = projectService.insertNewTask(project, List.of(lastTask));
		validatingTask.setRefusedTask(rejectTask);
		validatingTask.setTaskName(I18NUtil.getMessage(TASK_CHECKIN_NAME_KEY));
		validatingTask.setResources(new ArrayList<>());
		NodeRef currentUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
		validatingTask.getResources().add(currentUser);
		String resourceFirstName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_FIRSTNAME);
		String resourceLastName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_LASTNAME);
		for (NodeRef document : documents) {
			String docName = (String) nodeService.getProperty(document, ContentModel.PROP_NAME);
			String validateDeliverableName = SignatureProjectHelper.generateValidateDeliverableName(docName, resourceFirstName, resourceLastName);
			project.getDeliverableList()
					.add(ProjectHelper.createDeliverable(validateDeliverableName, docName, DeliverableScriptOrder.Post, validatingTask,
							BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), RepoConsts.SCRIPTS_FULL_PATH + RepoConsts.PATH_SEPARATOR + VALIDATE_SCRIPT)));
			String validateDocDeliverableName = SignatureProjectHelper.generateValidateDocDeliverableName(docName, resourceFirstName,
					resourceLastName);
			DeliverableListDataItem signViewDeliverable = ProjectHelper.createDeliverable(validateDocDeliverableName, docName, null, validatingTask,
					document);
			signViewDeliverable.setUrl(signatureService.getDocumentView(document, null, validatingTask.getNodeRef()));
			project.getDeliverableList().add(signViewDeliverable);
		}
	}

	private TaskListDataItem createRejectTask(ProjectData project, List<NodeRef> documents) {
		TaskListDataItem rejectTask = projectService.insertNewTask(project, null);
		rejectTask.setTaskName(I18NUtil.getMessage(TASK_REJECT_NAME_KEY));
		rejectTask.setDescription(I18NUtil.getMessage(TASK_REJECT_DESCRIPTION_KEY));
		rejectTask.setState(TaskState.Cancelled.toString());
		rejectTask.setResources(new ArrayList<>());
		NodeRef currentUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
		rejectTask.getResources().add(currentUser);
		String resourceFirstName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_FIRSTNAME);
		String resourceLastName = (String) nodeService.getProperty(currentUser, ContentModel.PROP_LASTNAME);
		for (NodeRef document : documents) {
			String docName = (String) nodeService.getProperty(document, ContentModel.PROP_NAME);
			String rejectDeliverableName = SignatureProjectHelper.generateRejectDeliverableName(docName, resourceFirstName, resourceLastName);
			project.getDeliverableList()
					.add(ProjectHelper.createDeliverable(rejectDeliverableName, docName, DeliverableScriptOrder.Pre, rejectTask,
							BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
									RepoConsts.SCRIPTS_FULL_PATH + RepoConsts.PATH_SEPARATOR + REJECT_SCRIPT)));
			String rejectDocDeliverableName = SignatureProjectHelper.generateRejectDocDeliverableName(docName, resourceFirstName, resourceLastName);
			project.getDeliverableList().add(ProjectHelper.createDeliverable(rejectDocDeliverableName, docName, null, rejectTask, document));
		}
		project.getTaskList().add(rejectTask);
		return rejectTask;
	}

	private NodeRef createOrUpdateSignatureTasks(ProjectData project, List<NodeRef> documents, List<NodeRef> recipients, NodeRef previousTask,
			TaskListDataItem rejectTask) {
		for (NodeRef recipient : recipients) {
			final NodeRef finalPreviousTask = previousTask;
			String resourceFirstName = (String) nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME);
			String resourceLastName = (String) nodeService.getProperty(recipient, ContentModel.PROP_LASTNAME);
			String taskName = I18NUtil.getMessage(TASK_SIGNATURE_NAME_KEY);
			TaskListDataItem newTask = project.getTaskList().stream()
					.filter(task -> (task.getResources() != null) && task.getResources().contains(recipient))
					.filter(task -> (task.getTaskName() != null) && task.getTaskName().equals(taskName)).findFirst()
					.orElseGet(() -> projectService.insertNewTask(project, List.of(finalPreviousTask)));
			newTask.setTaskState(TaskState.Planned);
			newTask.setRefusedTask(rejectTask);
			for (TaskListDataItem otherTask : project.getTaskList()) {
				if (!otherTask.equals(newTask) && otherTask.getPrevTasks().contains(previousTask)) {
					otherTask.getPrevTasks().remove(previousTask);
					otherTask.getPrevTasks().add(newTask.getNodeRef());
					otherTask.setTaskState(TaskState.Planned);
				}
			}
			if (!newTask.getPrevTasks().contains(previousTask)) {
				newTask.getPrevTasks().add(previousTask);
			}
			newTask.setTaskName(taskName);
			if (!newTask.getResources().contains(recipient)) {
				newTask.getResources().add(recipient);
			}
			newTask.setTaskName(I18NUtil.getMessage(TASK_SIGNATURE_NAME_KEY));
			newTask.setNotificationFrequency(NOTIFICATION_FREQUENCY);
			newTask.setInitialNotification(INITIAL_NOTIFICATION);
			newTask.setNotificationAuthorities(List.of(recipient));
			for (NodeRef document : documents) {
				String docName = (String) nodeService.getProperty(document, ContentModel.PROP_NAME);
				String prepareDeliverableName = SignatureProjectHelper.generatePrepareDeliverableName(docName, resourceFirstName, resourceLastName);
				if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(prepareDeliverableName))) {
					project.getDeliverableList()
							.add(ProjectHelper.createDeliverable(prepareDeliverableName, docName, DeliverableScriptOrder.Pre, newTask,
									BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
											RepoConsts.SCRIPTS_FULL_PATH + RepoConsts.PATH_SEPARATOR + PREPARE_SCRIPT)));
				} else {
					project.getDeliverableList().stream().filter(del -> del.getName().equals(prepareDeliverableName))
							.forEach(d -> d.setState(DeliverableState.Planned));
				}
				String urlDeliverableName = SignatureProjectHelper.generateUrlDeliverableName(docName, resourceFirstName, resourceLastName);
				if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(urlDeliverableName))) {
					project.getDeliverableList().add(ProjectHelper.createDeliverable(urlDeliverableName, docName, null, newTask, document));
				} else {
					project.getDeliverableList().stream().filter(del -> del.getName().equals(urlDeliverableName)).forEach(d -> {
						d.setState(DeliverableState.Planned);
						d.setContent(document);
					});
				}
				String signDeliverableName = SignatureProjectHelper.generateSignDeliverableName(docName, resourceFirstName, resourceLastName);

				if (project.getDeliverableList().stream().noneMatch(del -> del.getName().equals(signDeliverableName))) {
					project.getDeliverableList()
							.add(ProjectHelper.createDeliverable(signDeliverableName, docName, DeliverableScriptOrder.Post, newTask,
									BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(),
											RepoConsts.SCRIPTS_FULL_PATH + RepoConsts.PATH_SEPARATOR + SIGN_SCRIPT)));
				} else {
					project.getDeliverableList().stream().filter(del -> del.getName().equals(signDeliverableName))
							.forEach(d -> d.setState(DeliverableState.Planned));
				}
			}
			previousTask = newTask.getNodeRef();
		}
		return previousTask;
	}

}
