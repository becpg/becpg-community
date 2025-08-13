package fr.becpg.test.repo.license;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.junit.Test;

import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.license.BeCPGLicense;
import fr.becpg.repo.license.BeCPGLicenseManager;

public class LicenseManagerTest {

	String DELIMITER = ";";
	String licenseFilePath = "./src/test/java/fr/becpg/test/repo/license/LicenseList.csv";
	String targetPath = "./target/test-classes/fr/becpg/test/repo/license/output/";

	@SuppressWarnings("unchecked")
	@Test
	public void generateLicenseFile(){

		//Get license file
		File file = new File(licenseFilePath);

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			JSONObject licenseJson = new JSONObject();
			
			String line = reader.readLine();
			File targetDir = new File(targetPath);
			targetDir.mkdirs();

			while ((line = reader.readLine()) != null) {
				String[] licenses = line.split(DELIMITER, -1);
				ArrayList<Long> parsedLicenses = new ArrayList<>();
				String licenseName = licenses[0] + " LICENSE";
				for (String license : licenses) {
					try {  
						parsedLicenses.add(Long.parseLong(license));  
					} catch(NumberFormatException e){  
						parsedLicenses.add(0L);
					}  
				}          
				
				//Get license key
				BeCPGLicense license = new BeCPGLicense(licenseName, parsedLicenses.get(4), 
						parsedLicenses.get(3),parsedLicenses.get(5), parsedLicenses.get(1), parsedLicenses.get(2));
				String licenseKey  = BeCPGLicenseManager.computeLicenseKey(license);    			

				assertTrue(BeCPGLicenseManager.isValid(licenseKey, license));
				/*System.out.println("Sample license key:"+licenseKey);
				System.out.println(license);*/

				//Create JSON file
				licenseJson.put("LicenseName", licenseName);
				licenseJson.put("LicenseWriteNamed",parsedLicenses.get(1));
				licenseJson.put("LicenseReadNamed", parsedLicenses.get(2));
				licenseJson.put("LicenseWriteConcurrent", parsedLicenses.get(3));
				licenseJson.put("LicenseReadConcurrent", parsedLicenses.get(4));
				licenseJson.put("LicenseSupplierConcurrent", parsedLicenses.get(5));
				licenseJson.put("LicenseKey", licenseKey);

				String json = JsonHelper.MAPPER.copy().writerWithDefaultPrettyPrinter().writeValueAsString(licenseJson);

				try (FileWriter licenseFile = new FileWriter(targetPath + licenseName + ".json")) {
					licenseFile.write(json);
					licenseFile.flush(); 
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//TODO test LisenceManager and sample json file
	@Test
	public void testLienseKey() {

		BeCPGLicense license = new BeCPGLicense("beCPG Sample LICENSE", 1, 1, 10, 1, 10);

		String licenseKey  = BeCPGLicenseManager.computeLicenseKey(license);

		//Used to get the key System.out.println("Sample license key:"+licenseKey);

		assertTrue(BeCPGLicenseManager.isValid(licenseKey, license));
	}

}
