/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductListPoliciesTest.
 * 
 * @author querephi
 */
public class ProductListPoliciesTest extends RepoBaseTestCase {

	@Resource
	private EntityListDAO entityListDAO;

	/**
	 * simulate UI creation of datalist, we create a datalist with a GUID in the
	 * property name.
	 */
	@Test
	public void testGetList() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				RawMaterialData rawMaterialData = new RawMaterialData();
				rawMaterialData.setName("RM");
				NodeRef rawMaterialNodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterialData).getNodeRef();

				NodeRef containerListNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);

				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, GUID.generate());
				properties.put(DataListModel.PROP_DATALISTITEMTYPE, BeCPGModel.BECPG_PREFIX + ":" + BeCPGModel.TYPE_COSTLIST.getLocalName());
				NodeRef costListCreatedNodeRef = nodeService.createNode(containerListNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), DataListModel.TYPE_DATALIST, properties)
						.getChildRef();

				NodeRef costListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(rawMaterialNodeRef), BeCPGModel.TYPE_COSTLIST);
				assertNotNull("cost list should exist", costListNodeRef);
				assertEquals("cost list should be the same", costListCreatedNodeRef, costListNodeRef);

				costListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(rawMaterialNodeRef), BeCPGModel.TYPE_COSTLIST);
				assertNotNull("cost list should exist", costListNodeRef);
				assertEquals("cost list should be the same", costListCreatedNodeRef, costListNodeRef);

				return null;

			}
		}, false, true);

	}

}
