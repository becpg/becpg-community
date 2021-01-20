package fr.becpg.test.project;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.project.formulation.ProjectBudgetListIT;
import fr.becpg.test.project.formulation.ProjectBudgetTasksIT;
import fr.becpg.test.project.formulation.ProjectCalculatePlanningDatesIT;
import fr.becpg.test.project.formulation.ProjectMultiLevelPlanningIT;
import fr.becpg.test.project.formulation.ProjectOverdueIT;
import fr.becpg.test.project.formulation.ProjectRefusedTaskIT;
import fr.becpg.test.project.formulation.ProjectStartByStartingTaskIT;
import fr.becpg.test.project.formulation.ProjectSubmitTaskIT;
import fr.becpg.test.project.formulation.ProjectTaskDelegationIT;

@RunWith(Suite.class)
@SuiteClasses(value = { ProjectServiceIT.class, ProjectNotificationIT.class, ProjectCOCIIT.class,
		ProjectTaskDelegationIT.class, ProjectSubmitTaskIT.class, ProjectStartByStartingTaskIT.class, ProjectRefusedTaskIT.class,
		ProjectOverdueIT.class, ProjectMultiLevelPlanningIT.class, ProjectCalculatePlanningDatesIT.class, ProjectBudgetTasksIT.class,
		ProjectBudgetListIT.class })
public class ProjectTestSuite {

}
