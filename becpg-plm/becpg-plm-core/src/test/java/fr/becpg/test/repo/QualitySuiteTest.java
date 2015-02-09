package fr.becpg.test.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.repo.quality.QualityControlTest;
import fr.becpg.test.repo.workflow.ClaimWorkflowTest;
import fr.becpg.test.repo.workflow.NCWorkflowTest;


@RunWith(Suite.class)
@SuiteClasses(value={
	QualityControlTest.class,
	ClaimWorkflowTest.class,
	NCWorkflowTest.class
})
public class QualitySuiteTest {

}
