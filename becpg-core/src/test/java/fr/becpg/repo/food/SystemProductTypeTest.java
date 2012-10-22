/*
 * 
 */
package fr.becpg.repo.food;

import junit.framework.TestCase;

import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDataTest.
 *
 * @author querephi
 */

public class SystemProductTypeTest extends TestCase{

	/**
	 * Test determine product type.
	 */
	@Test
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
