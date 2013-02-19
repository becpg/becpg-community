package fr.becpg.repo.workflow;

import javax.annotation.Resource;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.subethamail.wiser.Wiser;

import fr.becpg.test.RepoBaseTestCase;

@Ignore
public abstract class AbstractWorkflowTest extends RepoBaseTestCase {

	private static Log logger = LogFactory.getLog(AbstractWorkflowTest.class);
	
	protected Wiser wiser = new Wiser(2500);

	
	@Resource(name="WorkflowService")
	protected WorkflowService workflowService;

	
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		// First start wiser
		try {
			wiser.start();
		} catch (Exception e) {
			logger.warn("cannot open wiser!");
		}

	}
	
	@Override
	@After
	public void tearDown() throws Exception {
		
		super.tearDown();
		try {
			wiser.stop();
		} catch (Exception e) {
			logger.warn("cannot stop wiser!");
		}
		
	}
	
}
