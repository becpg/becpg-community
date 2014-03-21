package fr.becpg.test;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.becpg.model.BeCPGModel;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
		"classpath:alfresco/web-scripts-application-context-test.xml" })
public class InitializationTest {

	@Autowired
	QNameDAO qNameDAO;
	
	@Autowired
	DictionaryDAO dictionaryDAO;
	
	@Test
	public void testInit(){
		
		dictionaryDAO.reset();
		//Patch for H2 ????
		Assert.assertNotNull(qNameDAO.getQName(BeCPGModel.PROP_SORT));
	}
	
}
