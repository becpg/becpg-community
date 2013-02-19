package fr.becpg.repo.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.data.AbstractProjectData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

public abstract class  AbstractProjectTestCase extends RepoBaseTestCase {


	@Resource
	protected AlfrescoRepository<AbstractProjectData> alfrescoRepository;

	@Resource(name = "WorkflowService")
	protected WorkflowService workflowService;
	@Resource
	protected AssociationService associationService;
	@Resource
	protected ProjectService projectService;
	@Resource
	protected EntityTplService entityTplService;

	protected NodeRef userOne;
	protected NodeRef userTwo;
	protected List<NodeRef> assigneesOne;
	protected List<NodeRef> assigneesTwo;
	protected NodeRef projectTplNodeRef;
	
	protected void initTest() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				
				userOne = BeCPGTestHelper.createUser(BeCPGTestHelper.USER_ONE,repoBaseTestCase);
				userTwo = BeCPGTestHelper.createUser(BeCPGTestHelper.USER_TWO,repoBaseTestCase);

				assigneesOne = new ArrayList<NodeRef>();
				assigneesOne.add(userOne);
				assigneesTwo = new ArrayList<NodeRef>();
				assigneesTwo.add(userTwo);
								
				// create project Tpl
				ProjectData projectTplData = new ProjectData(null, "Pjt Tpl", PROJECT_HIERARCHY1_PAIN_REF, null,
								null, null, null, null, null, 0, null);
				projectTplData.setParentNodeRef(testFolderNodeRef);
				projectTplData = (ProjectData) alfrescoRepository.save(projectTplData);
				projectTplNodeRef = projectTplData.getNodeRef();
				
				// create documents in tpl folder
				assertNotNull(projectTplData.getNodeRef());
				NodeRef subFolder = nodeService.getChildByName(projectTplNodeRef, ContentModel.ASSOC_CONTAINS,
						"SubFolder");
				if(subFolder == null){
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "SubFolder");
					subFolder = nodeService.createNode(projectTplNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "SubFolder"),
							ContentModel.TYPE_FOLDER, properties).getChildRef();
				}
				
				NodeRef doc1NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS,
						"Doc1");
				if(doc1NodeRef == null){					
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Doc1");
					doc1NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc1"), ContentModel.TYPE_CONTENT, properties)
							.getChildRef();
				}

				NodeRef doc2NodeRef = nodeService.getChildByName(subFolder, ContentModel.ASSOC_CONTAINS,
						"Doc2");
				if(doc2NodeRef == null){
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, "Doc2");
					doc2NodeRef = nodeService.createNode(subFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Doc2"), ContentModel.TYPE_CONTENT, properties)
							.getChildRef();
				}
				
				// create datalists
				List<TaskListDataItem> taskList = new LinkedList<TaskListDataItem>();

				taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task2", false, 2, null, assigneesOne, taskLegends.get(0),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task3", false, 2, null, assigneesOne, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task4", false, 2, null, assigneesTwo, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task5", false, 2, null, assigneesTwo, taskLegends.get(1),
						"activiti$projectAdhoc"));
				taskList.add(new TaskListDataItem(null, "task6", false, 2, null, assigneesTwo, taskLegends.get(2),
						"activiti$projectAdhoc"));
				projectTplData.setTaskList(taskList);
				
				alfrescoRepository.save(projectTplData);


//				Project:
//					Task1	-> Task2	-> Task3	->	Task5	-> Task6
//													-> 	Task4	
						
				// update a second time to manage prevTask
				// TODO : should avoid to save twice
				projectTplData = (ProjectData) alfrescoRepository.findOne(projectTplNodeRef);
				List<NodeRef> prevTasks = new ArrayList<NodeRef>();

				prevTasks.add(projectTplData.getTaskList().get(0).getNodeRef());
				projectTplData.getTaskList().get(1).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(1).getNodeRef());
				projectTplData.getTaskList().get(2).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(3).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(2).getNodeRef());
				projectTplData.getTaskList().get(4).setPrevTasks(prevTasks);

				prevTasks = new ArrayList<NodeRef>();
				prevTasks.add(projectTplData.getTaskList().get(3).getNodeRef());
				prevTasks.add(projectTplData.getTaskList().get(4).getNodeRef());
				projectTplData.getTaskList().get(5).setPrevTasks(prevTasks);

				List<DeliverableListDataItem> deliverableList = new LinkedList<DeliverableListDataItem>();
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(0).getNodeRef(),
						null, "Deliveray descr 1", 100, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(1).getNodeRef(),
						null, "Deliveray descr 2.1", 30, doc2NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(1).getNodeRef(),
						null, "Deliveray descr 2.2", 70, doc1NodeRef));
				deliverableList.add(new DeliverableListDataItem(null, projectTplData.getTaskList().get(2).getNodeRef(),
						null, "Deliveray descr 3", 100, null));
				projectTplData.setDeliverableList(deliverableList);

				alfrescoRepository.save(projectTplData);

				return null;
			}
		}, false, true);
	}
}
