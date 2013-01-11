package fr.becpg.test;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

public class Alfresco42Test extends RepoBaseTestCase {
	
	private static Log logger = LogFactory.getLog(Alfresco42Test.class);
	
	@Test
	public void initTest(){
		//DoNothing
		
		NodeRef sfNodeRef  = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				logger.error("//Start creating -----------");
				NodeRef ret =   BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
				logger.error("//End creating -----------");
				return ret;
			}
		}, false, true);
		
		Assert.assertNotNull(sfNodeRef);
	}

}
