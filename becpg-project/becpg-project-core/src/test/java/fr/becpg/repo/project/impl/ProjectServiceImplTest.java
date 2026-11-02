package fr.becpg.repo.project.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskManualDate;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

public class ProjectServiceImplTest {

    private static final String SCRIPT_NAME = "/cm:apply-effectivity-date.js";

    private static final String OUTSIDE_SCRIPTS_PATH = "/app:company_home/cm:Documents";

    private final NodeRef scriptNodeRef = new NodeRef("workspace://SpacesStore/deliverable-script");

    private final NodeRef projectNodeRef = new NodeRef("workspace://SpacesStore/project");

    private final Date initialStartDate = ProjectHelper.removeTime(new Date(1793750400000L));

    private final Date newStartDate = ProjectHelper.removeTime(new Date(1794182400000L));

    @Mock
    private NodeService nodeService;

    @Mock
    private NamespaceService namespaceService;

    @Mock
    private ScriptService scriptService;

    @Mock
    private BehaviourFilter policyBehaviourFilter;

    @Mock
    private ProjectFormulationAuditFilter projectFormulationAuditFilter;

    @Mock
    private SysAdminParams sysAdminParams;

    @Mock
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private AutoCloseable mocks;

    private final AtomicBoolean runningAudited = new AtomicBoolean();

    private final AtomicBoolean scriptRunAudited = new AtomicBoolean();

    private DeliverableListDataItem deliverable;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        deliverable = new DeliverableListDataItem();
        deliverable.setContent(scriptNodeRef);
        when(nodeService.exists(scriptNodeRef)).thenReturn(true);
        doAnswer(invocation -> {
            runningAudited.set(true);
            try {
                invocation.getArgument(0, Runnable.class).run();
            } finally {
                runningAudited.set(false);
            }
            return null;
        }).when(projectFormulationAuditFilter).runAudited(any(Runnable.class));
        doAnswer(invocation -> {
            scriptRunAudited.set(runningAudited.get());
            return null;
        }).when(scriptService).executeScript(eq(scriptNodeRef), eq(ContentModel.PROP_CONTENT), anyMap());
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void testDeliverableScriptRunsAudited() {
        givenScriptPath(RepoConsts.SCRIPTS_FULL_PATH + SCRIPT_NAME);

        projectService.runScript(new ProjectData(), new TaskListDataItem(), deliverable);

        verify(scriptService).executeScript(eq(scriptNodeRef), eq(ContentModel.PROP_CONTENT), anyMap());
        assertTrue(scriptRunAudited.get());
    }

    @Test
    public void testScriptOutsideDataDictionaryIsIgnored() {
        givenScriptPath(OUTSIDE_SCRIPTS_PATH + SCRIPT_NAME);

        projectService.runScript(new ProjectData(), new TaskListDataItem(), deliverable);

        verify(projectFormulationAuditFilter, never()).runAudited(any(Runnable.class));
        verify(scriptService, never()).executeScript(any(NodeRef.class), any(), anyMap());
    }

    @Test
    public void testNewStartDateMovesRootTask() {
        TaskListDataItem rootTask = createTask("root", TaskState.Planned);
        givenProject(PlanningMode.Planning, ProjectState.Planned, rootTask);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService).setProperty(rootTask.getNodeRef(), ProjectModel.PROP_TL_START, newStartDate);
    }

    @Test
    public void testNewStartDateKeepsSuccessorTask() {
        TaskListDataItem rootTask = createTask("root", TaskState.Planned);
        TaskListDataItem successorTask = createTask("successor", TaskState.Planned);
        successorTask.getPrevTasks().add(rootTask.getNodeRef());
        givenProject(PlanningMode.Planning, ProjectState.Planned, rootTask, successorTask);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService, never()).setProperty(same(successorTask.getNodeRef()), any(), any());
    }

    @Test
    public void testNewStartDateKeepsManualStartTask() {
        TaskListDataItem manualTask = createTask("manual", TaskState.Planned);
        manualTask.setManualDate(TaskManualDate.Start);
        givenProject(PlanningMode.Planning, ProjectState.Planned, manualTask);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService, never()).setProperty(same(manualTask.getNodeRef()), any(), any());
    }

    @Test
    public void testNewStartDateMovesSubProjectRootTask() {
        TaskListDataItem subProjectTask = createTask("subProject", TaskState.Planned);
        subProjectTask.setSubProject(new NodeRef("workspace://SpacesStore/sub-project"));
        givenProject(PlanningMode.Planning, ProjectState.Planned, subProjectTask);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService).setProperty(subProjectTask.getNodeRef(), ProjectModel.PROP_TL_START, newStartDate);
    }

    @Test
    public void testNewStartDateMovesCancelledRootTask() {
        TaskListDataItem cancelledTask = createTask("cancelled", TaskState.Cancelled);
        givenProject(PlanningMode.Planning, ProjectState.Planned, cancelledTask);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService).setProperty(cancelledTask.getNodeRef(), ProjectModel.PROP_TL_START, newStartDate);
    }

    @Test
    public void testNewStartDateIgnoredOnInProgressProject() {
        TaskListDataItem rootTask = createTask("root", TaskState.Planned);
        givenProject(PlanningMode.Planning, ProjectState.InProgress, rootTask);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService, never()).setProperty(same(rootTask.getNodeRef()), any(), any());
    }

    @Test
    public void testNewStartDateIgnoredInRetroPlanning() {
        TaskListDataItem rootTask = createTask("root", TaskState.Planned);
        givenProject(PlanningMode.RetroPlanning, ProjectState.Planned, rootTask);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService, never()).setProperty(same(rootTask.getNodeRef()), any(), any());
    }

    @Test
    public void testNewStartDateIgnoredOnTemplate() {
        TaskListDataItem rootTask = createTask("root", TaskState.Planned);
        givenProject(PlanningMode.Planning, ProjectState.Planned, rootTask);
        when(nodeService.hasAspect(projectNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)).thenReturn(true);

        projectService.updateProjectStartDate(projectNodeRef, newStartDate);

        verify(nodeService, never()).setProperty(same(rootTask.getNodeRef()), any(), any());
    }

    private void givenProject(PlanningMode planningMode, ProjectState projectState, TaskListDataItem... tasks) {
        ProjectData projectData = new ProjectData();
        projectData.setNodeRef(projectNodeRef);
        projectData.setTaskList(new ArrayList<>(List.of(tasks)));
        when(nodeService.getProperty(projectNodeRef, ProjectModel.PROP_PROJECT_PLANNING_MODE)).thenReturn(planningMode.toString());
        when(nodeService.getProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE)).thenReturn(projectState.toString());
        when(alfrescoRepository.findOne(projectNodeRef)).thenReturn(projectData);
    }

    private TaskListDataItem createTask(String name, TaskState taskState) {
        return TaskListDataItem.build().withNodeRef(new NodeRef("workspace://SpacesStore/" + name)).withTaskName(name)
                .withTaskState(taskState).withStart(initialStartDate).withPrevTasks(new ArrayList<>());
    }

    private void givenScriptPath(String prefixPath) {
        Path path = mock(Path.class);
        when(path.toPrefixString(namespaceService)).thenReturn(prefixPath);
        when(nodeService.getPath(scriptNodeRef)).thenReturn(path);
    }
}
