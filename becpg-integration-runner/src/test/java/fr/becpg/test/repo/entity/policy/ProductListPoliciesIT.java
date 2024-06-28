/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.PLMBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductListPoliciesTest.
 *
 * @author querephi
 */
public class ProductListPoliciesIT extends PLMBaseTestCase {

	@Autowired
	private EntityListDAO entityListDAO;

	/**
	 * simulate UI creation of datalist, we create a datalist with a GUID in the
	 * property name.
	 */
	@Test
	public void testGetList() {

		inWriteTx(() -> {

			RawMaterialData rawMaterialData = new RawMaterialData();
			rawMaterialData.setName("RM");
			NodeRef rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterialData)
					.getNodeRef();

			NodeRef containerListNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, GUID.generate());
			properties.put(DataListModel.PROP_DATALISTITEMTYPE,
					BeCPGModel.BECPG_PREFIX + ":" + PLMModel.TYPE_COSTLIST.getLocalName());
			NodeRef costListCreatedNodeRef = nodeService.createNode(containerListNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
							(String) properties.get(ContentModel.PROP_NAME)),
					DataListModel.TYPE_DATALIST, properties).getChildRef();

			NodeRef costListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(rawMaterialNodeRef),
					PLMModel.TYPE_COSTLIST);
			assertNotNull("cost list should exist", costListNodeRef);
			assertEquals("cost list should be the same", costListCreatedNodeRef, costListNodeRef);

			costListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(rawMaterialNodeRef),
					PLMModel.TYPE_COSTLIST);
			assertNotNull("cost list should exist", costListNodeRef);
			assertEquals("cost list should be the same", costListCreatedNodeRef, costListNodeRef);

			return null;

		});

	}

}
