/*
 *
 */
package fr.becpg.test.repo.project;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectServiceTest.
 *
 * @author quere
 */
public class NPDServiceIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(NPDServiceIT.class);

	private static final String NPDWF_URI = "http://www.bcpg.fr/model/npd-workflow/1.0";

	@Test
	public void testNPDProjectTask() {

		final NodeRef projectNodeRef = inWriteTx(() -> {

			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

			// create project Tpl
			ProjectData projectData = new ProjectData(null, "Pjt", PROJECT_HIERARCHY1_SEA_FOOD_REF,
					PROJECT_HIERARCHY2_CRUSTACEAN_REF, null, null, null, PlanningMode.Planning, null, null, null, 0,
					null);

			// create datalists
			List<TaskListDataItem> taskList = new LinkedList<>();
			taskList.add(new TaskListDataItem(null, "task1", false, 2, null, assigneesOne, taskLegends.get(0),
					"activiti$projectNewProduct"));
			projectData.setTaskList(taskList);

			projectData.setParentNodeRef(getTestFolderNodeRef());
			projectData = (ProjectData) alfrescoRepository.save(projectData);

			// start
			projectData.setProjectState(ProjectState.InProgress);
			projectData = (ProjectData) alfrescoRepository.save(projectData);

			return projectData.getNodeRef();
		});

		String workflowInstance = inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			assertEquals(ProjectState.InProgress, projectData.getProjectState());

			logger.info("workflow instance " + projectData.getTaskList().get(0).getWorkflowInstance());
			assertNotNull(projectData.getTaskList().get(0).getWorkflowInstance());

			return projectData.getTaskList().get(0).getWorkflowInstance();
		});

		testNPDWorkflow(workflowInstance);

	}

	private void testNPDWorkflow(final String workflowInstanceId) {
		assertNotNull("The workflow instance is null!", workflowInstanceId);

		List<WorkflowTask> tasks = inWriteTx(() -> {
			List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
			assertEquals(1, paths.size());
			WorkflowPath path = paths.get(0);

			return workflowService.getTasksForWorkflowPath(path.getId());
		});
		assertEquals(1, tasks.size());

		final WorkflowTask task1 = tasks.get(0);

		assertEquals("npdwf:newProductTask", task1.getName());

		final WorkflowTask task2 = inWriteTx(() -> {

			logger.info("Set npd task information " + task1.getName());
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(QName.createQName(NPDWF_URI, "npdProductName"), "Test NPD");
			properties.put(QName.createQName(NPDWF_URI, "npdAction"), "createNewProduct");
			java.util.Map<QName, List<NodeRef>> assocs = new HashMap<>();

			workflowService.updateTask(task1.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());

			workflowService.endTask(task1.getId(), null);

			List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
			assertEquals(1, paths.size());
			WorkflowPath path = paths.get(0);

			List<WorkflowTask> tasks1 = workflowService.getTasksForWorkflowPath(path.getId());
			assertEquals(1, tasks1.size());

			WorkflowTask task = tasks1.get(0);

			workflowService.updateTask(task.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
			workflowService.endTask(task.getId(), null);

			paths = workflowService.getWorkflowPaths(workflowInstanceId);
			assertEquals(1, paths.size());
			path = paths.get(0);

			tasks1 = workflowService.getTasksForWorkflowPath(path.getId());
			assertEquals(1, tasks1.size());

			return tasks1.get(0);
		});

		final NodeRef productNoderef = inWriteTx(() -> {
			int count = 0;
			NodeRef ret = null;
			NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef,
					WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {
				if (PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(childAssoc.getChildRef()))) {
					count++;
					ret = childAssoc.getChildRef();
					logger.info("NPD created product" + nodeService.getProperty(ret, ContentModel.PROP_NAME) + " "
							+ nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
					assertEquals(SystemState.Simulation.toString(),
							nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
				}
			}
			assertEquals(2, count);
			return ret;
		});

		inWriteTx(() -> {

			logger.info("Set npd task information " + task2.getName());
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(QName.createQName(NPDWF_URI, "npdAction"), "submitTask");
			java.util.Map<QName, List<NodeRef>> assocs = new HashMap<>();
			assocs.put(QName.createQName(NPDWF_URI, "npdSelectedProducts"), Collections.singletonList(productNoderef));

			workflowService.updateTask(task2.getId(), properties, assocs, new HashMap<QName, List<NodeRef>>());
			return workflowService.endTask(task2.getId(), null);
		});

		inWriteTx(() -> {
			int count = 0;
			NodeRef ret = null;
			NodeRef pkgNodeRef = workflowService.getWorkflowById(workflowInstanceId).getWorkflowPackage();
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef,
					WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {
				if (PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(childAssoc.getChildRef()))) {
					count++;
					ret = childAssoc.getChildRef();
					logger.info("NPD updated product" + nodeService.getProperty(ret, ContentModel.PROP_NAME) + " "
							+ nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
					assertEquals(SystemState.ToValidate.toString(),
							nodeService.getProperty(ret, PLMModel.PROP_PRODUCT_STATE));
				}
			}
			assertEquals(1, count);
			return ret;
		});
	}
}
