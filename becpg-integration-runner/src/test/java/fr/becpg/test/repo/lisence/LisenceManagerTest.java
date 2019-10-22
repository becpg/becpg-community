package fr.becpg.test.repo.lisence;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.becpg.repo.license.BeCPGLicense;
import fr.becpg.repo.license.BeCPGLicenseManager;

public class LisenceManagerTest {

	//TODO test LisenceManager and sample json file
	
	@Test
	public void testLisenceKey() {
		
		BeCPGLicense license = new BeCPGLicense("beCPG Sample LICENSE", 1, 1, 10, 1, 10);
 
		String lisenceKey  = BeCPGLicenseManager.computeLicenseKey(license);
		
		//Used to get the key System.out.println("Sample license key:"+lisenceKey);
		
		assertTrue(BeCPGLicenseManager.isValid(lisenceKey, license));
	}

}
