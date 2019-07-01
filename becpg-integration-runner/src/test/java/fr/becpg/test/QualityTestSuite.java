package fr.becpg.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.repo.quality.QualityControlIT;
import fr.becpg.test.repo.workflow.ClaimWorkflowIT;
import fr.becpg.test.repo.workflow.NCWorkflowIT;

@RunWith(Suite.class)
@SuiteClasses(value = { QualityControlIT.class, ClaimWorkflowIT.class, NCWorkflowIT.class })
public class QualityTestSuite {

}
