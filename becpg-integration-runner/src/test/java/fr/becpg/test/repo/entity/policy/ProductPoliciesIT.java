/*
 *
 */
package fr.becpg.test.repo.entity.policy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductPoliciesTest.
 *
 * @author querephi
 */
public class ProductPoliciesIT extends PLMBaseTestCase {

	@Autowired
	private AutoNumService autoNumService;

	private String productCode1 = null;
	private String productCode2 = null;

	/**
	 * Test product code.
	 */
	@Test
	public void testProductCode() {

		final NodeRef rawMaterial1NodeRef = inWriteTx(() -> {

			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

		});

		final NodeRef rawMaterial2NodeRef = inWriteTx(() -> {

			assertNotNull("Check product creaed", rawMaterial1NodeRef);
			productCode1 = (String) nodeService.getProperty(rawMaterial1NodeRef, BeCPGModel.PROP_CODE);

			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

		});

		inWriteTx(() -> {

			productCode2 = (String) nodeService.getProperty(rawMaterial2NodeRef, BeCPGModel.PROP_CODE);
			assertNotNull("Check product code 1", productCode1);
			assertNotNull("Check product code 2", productCode2);
			Pattern p = Pattern
					.compile(autoNumService.getAutoNumMatchPattern(PLMModel.TYPE_RAWMATERIAL, BeCPGModel.PROP_CODE));

			Matcher ma1 = p.matcher(productCode1);
			assertTrue(ma1.matches());
			Matcher ma2 = p.matcher(productCode2);
			assertTrue(ma2.matches());
			assertEquals("Compare product codes", Long.parseLong(ma1.group(2)) + 1, Long.parseLong(ma2.group(2)));

			return null;

		});
	}
}
