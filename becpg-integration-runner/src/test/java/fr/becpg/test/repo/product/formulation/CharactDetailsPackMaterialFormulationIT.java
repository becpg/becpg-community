/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.icu.text.DecimalFormat;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.web.scripts.product.CharactDetailsHelper;

/**
 * The Class FormulationTest.
 *
 */
public class CharactDetailsPackMaterialFormulationIT extends  FormulationPackMaterialIT {

	protected static final Log logger = LogFactory.getLog(CharactDetailsPackMaterialFormulationIT.class);

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test formulate product and check pack material details
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulateCharactDetailsPackMaterial() throws Exception {

		logger.info("testFormulateCharactDetailsPackMaterial");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = createFinishedProduct();
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(finishedProductNodeRef);
			if(entityListDAO.getList(listContainerNodeRef, PackModel.PACK_MATERIAL_LIST_TYPE) == null) {
				entityListDAO.createList(listContainerNodeRef, PackModel.PACK_MATERIAL_LIST_TYPE);
			}

			// formulate Details
			List<NodeRef> packMaterialNodeRefs = new ArrayList<>();
			productService.formulate(finishedProductNodeRef);
			CharactDetails ret = productService.formulateDetails(finishedProductNodeRef, PackModel.PACK_MATERIAL_LIST_TYPE, "packMaterialList",
					packMaterialNodeRefs, null);

			Assert.assertNotNull(ret);
			logger.info(CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService).toString(3));

			// pack material
			int checks = 0;
			DecimalFormat df = new DecimalFormat("0.###");
			for (Map.Entry<NodeRef, List<CharactDetailsValue>> kv : ret.getData().entrySet()) {

				for (CharactDetailsValue kv2 : kv.getValue()) {

					String trace = "material: " + nodeService.getProperty(kv.getKey(), BeCPGModel.PROP_LV_VALUE) + " - source: " + kv2.getKeyNodeRef()
							+ " - value: " + kv2.getValue();
					logger.info(trace);

					// material 1 (Alluminium)
					if (kv.getKey().equals(packMaterial1NodeRef)) {

						if (kv2.getKeyNodeRef().equals(PF1NodeRef)) {
							checks++;
							assertEquals(df.format(20d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging1NodeRef)) {
							checks++;
							assertEquals(df.format(3d), df.format(kv2.getValue()));
						}
					}

					// material 2 (Carton)
					else if (kv.getKey().equals(packMaterial2NodeRef)) {

						if (kv2.getKeyNodeRef().equals(PF1NodeRef)) {
							checks++;
							assertEquals(df.format(40d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging2NodeRef)) {
							checks++;
							assertEquals(df.format(28.349523125d), df.format(kv2.getValue()));
						}
					}

					// material 3 (Fer)
					else if (kv.getKey().equals(packMaterial3NodeRef)) {

						if (kv2.getKeyNodeRef().equals(SF1NodeRef)) {
							checks++;
							assertEquals(df.format(60d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging3NodeRef)) {
							checks++;
							assertEquals(df.format(226.796d), df.format(kv2.getValue()));
						}
					}

					// material 4 (Plastique)
					else if (kv.getKey().equals(packMaterial4NodeRef)) {

						if (kv2.getKeyNodeRef().equals(SF1NodeRef)) {
							checks++;
							assertEquals(df.format(80d), df.format(kv2.getValue()));
						} else if (kv2.getKeyNodeRef().equals(packaging3NodeRef)) {
							checks++;
							assertEquals(df.format(226.796d), df.format(kv2.getValue()));
						}
					}
					// material 5 (Verre)
					else if (kv.getKey().equals(packMaterial5NodeRef)) {

						if (kv2.getKeyNodeRef().equals(rawMaterial1NodeRef)) {
							checks++;
							assertEquals(df.format(56.699d), df.format(kv2.getValue()));
						}
					}
					// material 6 (Papier)
					else if (kv.getKey().equals(packMaterial6NodeRef)) {
						
						if (kv2.getKeyNodeRef().equals(rawMaterial3NodeRef)) {
							checks++;
							assertEquals(df.format(20d), df.format(kv2.getValue()));
						}
					}

				}

			}

			assertEquals("Verify checks done", 10, checks);

			return null;

		}, false, true);

	}


}