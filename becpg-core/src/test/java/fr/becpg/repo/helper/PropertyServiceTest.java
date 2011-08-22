/*
 * 
 */
package fr.becpg.repo.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.config.format.PropertyFormats;


// TODO: Auto-generated Javadoc
/**
 * The Class ProductDAOTest.
 *
 * @author querephi
 */
public class PropertyServiceTest  extends BaseAlfrescoTestCase  {
	  
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(PropertyServiceTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The repository helper. */
	private PropertyService propertyService;   
	
	private DictionaryService dictionaryService;
	
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	propertyService = (PropertyService)appCtx.getBean("propertyService");
    	dictionaryService = (DictionaryService)appCtx.getBean("dictionaryService");
    	
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			@Override
			public NodeRef execute() throws Throwable {
 		        
 				return null;

 			}},false,true);  
                        
    }
    
    
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {
		try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Don't let this mask any previous exceptions
        }
        super.tearDown();

    }	  
   
   /**
    * Test date property
    */
   public void testGetDate() throws ParseException{
	   
	   PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modified"));
	   SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	   Date date = dateFormat.parse("07/10/2009");
	   
	   assertNotNull(propertyDef);
	   assertNotNull(date);
	   
	   PropertyFormats propertyFormats = new PropertyFormats(true);
	   String stringDate = propertyService.getStringValue(propertyDef, date, propertyFormats);	   
	   assertEquals("check date", "mer. 7 oct. 2009 00:00:00", stringDate);
   }
   
 	
}
