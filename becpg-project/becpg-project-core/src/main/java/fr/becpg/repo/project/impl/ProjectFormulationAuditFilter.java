/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.project.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Controls the <code>cm:auditable</code> behaviour during project formulation.
 * <p>
 * Formulation disables <code>cm:auditable</code> for the whole transaction so that the project and its
 * lists are not flagged as modified each time it is formulated. Deliverable scripts run inside that
 * formulation but deliberately modify other entities, which must keep their <code>cm:modified</code> and
 * <code>cm:modifier</code> up to date. This filter tracks how many times formulation disabled the
 * behaviour on the current thread, so that a script can run with exactly those disables lifted, while a
 * disable set by the caller of the formulation (an import, for instance) is left untouched.
 *
 * @author matthieu
 */
@Component
public class ProjectFormulationAuditFilter {

    private final ThreadLocal<Integer> formulationDisableCount = ThreadLocal.withInitial(() -> 0);

    private final BehaviourFilter policyBehaviourFilter;

    /**
     * Creates the filter.
     *
     * @param policyBehaviourFilter the Alfresco behaviour filter
     */
    @Autowired
    public ProjectFormulationAuditFilter(@Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * Disables <code>cm:auditable</code> for the formulation starting on the current thread.
     * Every call must be paired with {@link #enableAuditAfterFormulation()} in a finally block.
     */
    public void disableAuditForFormulation() {
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        formulationDisableCount.set(formulationDisableCount.get() + 1);
    }

    /**
     * Enables <code>cm:auditable</code> again at the end of a formulation.
     */
    public void enableAuditAfterFormulation() {
        policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
        storeDisableCount(Math.max(0, formulationDisableCount.get() - 1));
    }

    /**
     * Runs an action with the <code>cm:auditable</code> disables set by project formulation lifted,
     * then restores them.
     *
     * @param action the action to run, typically a deliverable script
     */
    public void runAudited(Runnable action) {
        int suspendedCount = formulationDisableCount.get();
        int liftedCount = liftFormulationDisables(suspendedCount);
        try {
            action.run();
        } finally {
            restoreFormulationDisables(liftedCount, suspendedCount);
        }
    }

    /**
     * Lifts at most <code>count</code> disables, and only while the behaviour is actually disabled, so that
     * a script running in a transaction where formulation did not disable it gets no extra disable back.
     */
    private int liftFormulationDisables(int count) {
        int liftedCount = 0;
        while ((liftedCount < count) && !policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)) {
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            liftedCount++;
        }
        storeDisableCount(0);
        return liftedCount;
    }

    private void restoreFormulationDisables(int liftedCount, int suspendedCount) {
        for (int i = 0; i < liftedCount; i++) {
            policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        }
        storeDisableCount(suspendedCount);
    }

    private void storeDisableCount(int count) {
        if (count == 0) {
            formulationDisableCount.remove();
        } else {
            formulationDisableCount.set(count);
        }
    }
}
