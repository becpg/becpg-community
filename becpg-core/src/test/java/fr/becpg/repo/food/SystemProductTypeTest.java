/*
 * 
 */
package fr.becpg.repo.food;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDataTest.
 *
 * @author querephi
 */
public class SystemProductTypeTest extends RepoBaseTestCase{

	/** The logger. */
	private static Log logger = LogFactory.getLog(SystemProductTypeTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
    	
    	logger.debug("ProductTypeTest::setUp");
    	    	
    }
    
	@Override
    public void tearDown() throws Exception
    {	
        super.tearDown();
        
    }	
	
	/**
	 * Test determine product type.
	 */
	public void testDetermineProductType(){
		
		assertEquals(SystemProductType.FinishedProduct, SystemProductType.valueOf(BeCPGModel.TYPE_FINISHEDPRODUCT));
		assertEquals(SystemProductType.LocalSemiFinishedProduct, SystemProductType.valueOf(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT));
		assertEquals(SystemProductType.PackagingKit, SystemProductType.valueOf(BeCPGModel.TYPE_PACKAGINGKIT));
		assertEquals(SystemProductType.PackagingMaterial, SystemProductType.valueOf(BeCPGModel.TYPE_PACKAGINGMATERIAL));
		assertEquals(SystemProductType.RawMaterial, SystemProductType.valueOf(BeCPGModel.TYPE_RAWMATERIAL));
		assertEquals(SystemProductType.SemiFinishedProduct, SystemProductType.valueOf(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT));
		assertEquals(SystemProductType.ResourceProduct, SystemProductType.valueOf(BeCPGModel.TYPE_RESOURCEPRODUCT));
	}
}
