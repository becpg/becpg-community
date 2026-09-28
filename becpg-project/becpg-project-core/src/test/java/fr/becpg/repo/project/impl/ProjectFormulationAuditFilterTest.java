package fr.becpg.repo.project.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.junit.Before;
import org.junit.Test;

public class ProjectFormulationAuditFilterTest {

    private final AtomicInteger auditableDisableCount = new AtomicInteger();

    private ProjectFormulationAuditFilter auditFilter;

    @Before
    public void setUp() {
        BehaviourFilter behaviourFilter = mock(BehaviourFilter.class);
        doAnswer(invocation -> auditableDisableCount.incrementAndGet()).when(behaviourFilter)
                .disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        doAnswer(invocation -> auditableDisableCount.updateAndGet(count -> Math.max(0, count - 1))).when(behaviourFilter)
                .enableBehaviour(ContentModel.ASPECT_AUDITABLE);
        when(behaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)).thenAnswer(invocation -> auditableDisableCount.get() == 0);
        auditFilter = new ProjectFormulationAuditFilter(behaviourFilter);
    }

    @Test
    public void testAuditIsDisabledDuringFormulation() {
        auditFilter.disableAuditForFormulation();

        assertEquals(1, auditableDisableCount.get());

        auditFilter.enableAuditAfterFormulation();

        assertEquals(0, auditableDisableCount.get());
    }

    @Test
    public void testScriptRunsAuditedDuringFormulation() {
        AtomicInteger countDuringScript = new AtomicInteger(-1);
        auditFilter.disableAuditForFormulation();

        auditFilter.runAudited(() -> countDuringScript.set(auditableDisableCount.get()));

        assertEquals(0, countDuringScript.get());
        assertEquals(1, auditableDisableCount.get());
        auditFilter.enableAuditAfterFormulation();
    }

    @Test
    public void testScriptRunsAuditedDuringNestedParentFormulation() {
        AtomicInteger countDuringScript = new AtomicInteger(-1);
        auditFilter.disableAuditForFormulation();
        auditFilter.disableAuditForFormulation();

        auditFilter.runAudited(() -> countDuringScript.set(auditableDisableCount.get()));

        assertEquals(0, countDuringScript.get());
        assertEquals(2, auditableDisableCount.get());
        auditFilter.enableAuditAfterFormulation();
        auditFilter.enableAuditAfterFormulation();
        assertEquals(0, auditableDisableCount.get());
    }

    @Test
    public void testCallerDisableIsKeptDuringScript() {
        AtomicInteger countDuringScript = new AtomicInteger(-1);
        auditableDisableCount.set(1);
        auditFilter.disableAuditForFormulation();

        auditFilter.runAudited(() -> countDuringScript.set(auditableDisableCount.get()));

        assertEquals(1, countDuringScript.get());
        auditFilter.enableAuditAfterFormulation();
        assertEquals(1, auditableDisableCount.get());
    }

    @Test
    public void testFormulationDisableIsRestoredWhenScriptFails() {
        auditFilter.disableAuditForFormulation();

        assertThrows(IllegalStateException.class, () -> auditFilter.runAudited(() -> {
            throw new IllegalStateException("Script failure");
        }));

        assertEquals(1, auditableDisableCount.get());
        auditFilter.enableAuditAfterFormulation();
        assertEquals(0, auditableDisableCount.get());
    }

    @Test
    public void testFormulationTriggeredByScriptIsBalanced() {
        AtomicInteger countInNestedScript = new AtomicInteger(-1);
        auditFilter.disableAuditForFormulation();

        auditFilter.runAudited(() -> {
            auditFilter.disableAuditForFormulation();
            auditFilter.runAudited(() -> countInNestedScript.set(auditableDisableCount.get()));
            auditFilter.enableAuditAfterFormulation();
        });

        assertEquals(0, countInNestedScript.get());
        assertEquals(1, auditableDisableCount.get());
        auditFilter.enableAuditAfterFormulation();
    }

    @Test
    public void testScriptInAnotherTransactionGetsNoExtraDisable() {
        auditFilter.disableAuditForFormulation();
        auditableDisableCount.set(0);

        auditFilter.runAudited(() -> {
        });

        assertEquals(0, auditableDisableCount.get());
    }

    @Test
    public void testRunAuditedOutsideFormulationLeavesFilterUntouched() {
        AtomicInteger countDuringScript = new AtomicInteger(-1);

        auditFilter.runAudited(() -> countDuringScript.set(auditableDisableCount.get()));

        assertEquals(0, countDuringScript.get());
        assertEquals(0, auditableDisableCount.get());
    }
}
