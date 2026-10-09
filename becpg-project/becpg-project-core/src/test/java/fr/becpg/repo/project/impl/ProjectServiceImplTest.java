package fr.becpg.repo.project.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

public class ProjectServiceImplTest {

    private static final String SCRIPT_NAME = "/cm:apply-effectivity-date.js";

    private static final String OUTSIDE_SCRIPTS_PATH = "/app:company_home/cm:Documents";

    private final NodeRef scriptNodeRef = new NodeRef("workspace://SpacesStore/deliverable-script");

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

    private void givenScriptPath(String prefixPath) {
        Path path = mock(Path.class);
        when(path.toPrefixString(namespaceService)).thenReturn(prefixPath);
        when(nodeService.getPath(scriptNodeRef)).thenReturn(path);
    }
}
