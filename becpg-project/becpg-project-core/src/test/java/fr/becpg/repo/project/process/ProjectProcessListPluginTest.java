package fr.becpg.repo.project.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.becpg.repo.project.data.ProjectState;

/**
 * <p>ProjectProcessListPluginTest class.</p>
 *
 * @author matthieu
 */
public class ProjectProcessListPluginTest {

	@Test
	public void projectWithoutStateIsActive() {
		assertTrue(ProjectProcessListPlugin.isActive(null));
	}

	@Test
	public void completedProjectIsNotActive() {
		assertFalse(ProjectProcessListPlugin.isActive(ProjectState.Completed));
	}

	@Test
	public void inProgressProjectIsActive() {
		assertTrue(ProjectProcessListPlugin.isActive(ProjectState.InProgress));
	}
}
