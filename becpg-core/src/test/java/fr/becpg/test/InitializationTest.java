package fr.becpg.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.becpg.model.BeCPGModel;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
		"classpath:alfresco/web-scripts-application-context-test.xml" })
public class InitializationTest {

	@Autowired
	@Qualifier("qnameDAO")
	QNameDAO qNameDAO;
	
	@Autowired
	DictionaryDAO dictionaryDAO;
	
	@Autowired
	Repository repositoryHelper;
	
	@Autowired
	NodeService nodeService;
	
	@Autowired
	TransactionService transactionService;
	
	@Test
	public void testInitWithoutCreateNode(){
		
		dictionaryDAO.reset();
		
		//Will throw an assert false when corrected
		Assert.assertNull(qNameDAO.getQName(BeCPGModel.PROP_SORT));
	}
	
	@Test
	public void testInitWithCreateNode(){
		
		dictionaryDAO.reset();
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Throwable {

				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "name");
				properties.put(BeCPGModel.PROP_SORT, 1);
				nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
						ContentModel.TYPE_CONTENT, properties);
				return false;

			}
		}, false, true);
		
		//Works
		Assert.assertNotNull(qNameDAO.getQName(BeCPGModel.PROP_SORT));
	}
	
}
