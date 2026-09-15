/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product.policy.productListUnits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Checks that the type of a microbiological characteristic reaches the lines that already
 * reference it, and not only the lines created afterwards.
 *
 * @author matthieu
 */
public class MicrobioTypePropagationIT extends PLMBaseTestCase {

	private static final String MICROBIO_TYPE = "Pathogène";

	private static final String MICROBIO_NAME = "Listeria monocytogenes";

	@Test
	public void testCharactTypeReachesExistingLines() {

		final NodeRef microbioNodeRef = inWriteTx(this::createMicrobioCharact);

		final NodeRef productNodeRef = inWriteTx(() -> createProductWithMicrobioLine(microbioNodeRef));

		inReadTx(() -> {
			Assert.assertNull("The line has no type as long as the charact has none", readListType(productNodeRef));
			return null;
		});

		inWriteTx(() -> {
			nodeService.setProperty(microbioNodeRef, PLMModel.PROP_MICROBIO_TYPE, MICROBIO_TYPE);
			return null;
		});

		inReadTx(() -> {
			Assert.assertEquals("The existing line follows the type of its charact", MICROBIO_TYPE, readListType(productNodeRef));
			return null;
		});
	}

	private NodeRef createMicrobioCharact() {

		declareMicrobioType();

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, MICROBIO_NAME);

		return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, MICROBIO_NAME), PLMModel.TYPE_MICROBIO, properties).getChildRef();
	}

	/*
	 * bcpg:microbioType is bound to a dynamic list constraint, its values have to exist before
	 * they can be assigned.
	 */
	private void declareMicrobioType() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_LISTS);
		NodeRef microbioTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_MICROBIO_TYPES);

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, MICROBIO_TYPE);

		nodeService.createNode(microbioTypesFolder, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, MICROBIO_TYPE), BeCPGModel.TYPE_LIST_VALUE, properties);
	}

	private NodeRef createProductWithMicrobioLine(NodeRef microbioNodeRef) {

		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Product with microbio criteria");
		finishedProduct.setUnit(ProductUnit.kg);

		List<MicrobioListDataItem> microbioList = new ArrayList<>();
		MicrobioListDataItem microbioListItem = new MicrobioListDataItem();
		microbioListItem.setMicrobio(microbioNodeRef);
		microbioListItem.setValue(10d);
		microbioList.add(microbioListItem);
		finishedProduct.setMicrobioList(microbioList);

		return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	}

	private String readListType(NodeRef productNodeRef) {

		FinishedProductData finishedProduct = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
		NodeRef listItemNodeRef = finishedProduct.getMicrobioList().get(0).getNodeRef();

		return (String) nodeService.getProperty(listItemNodeRef, PLMModel.PROP_MICROBIOLIST_TYPE);
	}

}
