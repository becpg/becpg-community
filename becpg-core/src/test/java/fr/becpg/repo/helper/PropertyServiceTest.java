/*
 * 
 */
package fr.becpg.repo.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.test.RepoBaseTestCase;


// TODO: Auto-generated Javadoc
/**
 * The Class ProductDAOTest.
 *
 * @author querephi
 */
public class PropertyServiceTest  extends RepoBaseTestCase  {
	  
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(PropertyServiceTest.class);
	
	/** The repository helper. */
	private AttributeExtractorService attributeExtractorService;   
	
	private DictionaryService dictionaryService;
	
	
	/* (non-Javadoc)
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    	
    	attributeExtractorService = (AttributeExtractorService)ctx.getBean("attributeExtractorService");
    	dictionaryService = (DictionaryService)ctx.getBean("dictionaryService");                        
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
	   String stringDate = attributeExtractorService.getStringValue(propertyDef, date, propertyFormats);	   
	   assertEquals("check date", "mer. 7 oct. 2009 00:00:00", stringDate);
   }
   
 	
}
