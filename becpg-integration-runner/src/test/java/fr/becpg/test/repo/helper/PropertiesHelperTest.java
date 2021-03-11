package fr.becpg.test.repo.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.PropertiesHelper;

public class PropertiesHelperTest {

	@Test
	public void test() {

		String name = "FP404 - D'emo * 4x2.5kg Na\ndos O\"live M?ix (up to date) - List of calculated. characteristics - RM cost (€/kg)-a473a92e-8f30-4f56-a97c-f4c22af0b787.";
		String name2 = "FP404 - Demo 4x2.5kg Nandos Olive Mix (up to date) - List of calculated characteristics - Total Pack cost (€/P)-0f7284c1-7a8d-4eb3-82b0-7886db324520";
		String name3 = "Beef, ground, 70% lean meat / 30% fat, raw";
		String ret = "FP404 - D'emo - 4x2.5kg Na dos O-live M-ix (up to date) - List of calculated. characteristics - RM cost (€-kg)-a473a92e-8f30-4f56-a97c-f4c22af0b787";
		String ret2 = "Beef, ground, 70% lean meat - 30% fat, raw";
		
		Assert.assertEquals(PropertiesHelper.cleanName(name), ret);
		Assert.assertFalse(PropertiesHelper.testName(name));
		Assert.assertFalse(PropertiesHelper.testName(name2));
		Assert.assertTrue(PropertiesHelper.testName(PropertiesHelper.cleanName(name2)));
		Assert.assertTrue(PropertiesHelper.testName(ret));
		
		
		Assert.assertFalse(PropertiesHelper.testName(name3));
		Assert.assertEquals(PropertiesHelper.cleanName(name3), ret2);
		Assert.assertTrue(PropertiesHelper.testName(PropertiesHelper.cleanName(name3)));
		
		Assert.assertEquals(PropertiesHelper.cleanFolderName("cm:test_x0020_folder"), "test folder");
		
		Assert.assertEquals("test", PropertiesHelper.cleanValue(" test "));
		
		
	}

}
