package fr.becpg.test.repo.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.test.PLMBaseTestCase;

public class AlfrescoRepositoryCacheIT extends PLMBaseTestCase {
	
	private static Log logger = LogFactory.getLog(AlfrescoRepositoryCacheIT.class);
	
	@Test
	public void testRepositoryCache() {
		inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " - Cached Spec ");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();
			
			properties.put(ContentModel.PROP_NAME, name + " - Cached Spec 2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();


			ProductSpecificationData cachedProductSpec = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);
			
			 assertEquals( name + " - Cached Spec ", cachedProductSpec.getName());
			 
			 nodeService.setProperty(productSpecificationNodeRef1, ContentModel.PROP_NAME, name + " - Cached Spec 1 bis");
			
			 assertEquals( name + " - Cached Spec ", cachedProductSpec.getName());
			 
			 cachedProductSpec = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);
			 
			 assertEquals( name + " - Cached Spec 1 bis", cachedProductSpec.getName());
			 
			 logger.info("Create assoc on :"+productSpecificationNodeRef1);
			 nodeService.createAssociation(productSpecificationNodeRef1, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			
			 
			 assertEquals( 0, cachedProductSpec.getProductSpecifications().size());
			 
			 cachedProductSpec = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);
			 
			 assertEquals( 1, cachedProductSpec.getProductSpecifications().size());
			 
			 assertEquals( name + " - Cached Spec 2", cachedProductSpec.getProductSpecifications().get(0).getName());
			
			 nodeService.setProperty(productSpecificationNodeRef2, ContentModel.PROP_NAME, name + " - Cached Spec 2 bis");
			 	 
			 cachedProductSpec = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);
			 
			 assertEquals( name + " - Cached Spec 2 bis", cachedProductSpec.getProductSpecifications().get(0).getName());
			 
			
			return null;
		});
		
		
	}

	/**
	 * A cached ingredient keeps its ingredient type as an entity. Deleting that type leaves the
	 * {@code bcpg:ingTypeV2} d:noderef property in place: reloading the ingredient must drop the
	 * type, as a read from the database does, instead of failing on the deleted node (#36737:
	 * every formulation of a product using the ingredient failed).
	 */
	@Test
	public void testCachedEntityPropertyOnDeletedNode() {
		NodeRef[] nodes = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, name + " - Deleted ing type");
			NodeRef ingTypeNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "deletedIngType"), PLMModel.TYPE_ING_TYPE_ITEM, properties)
					.getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, name + " - Ing with deleted type");
			properties.put(PLMModel.PROP_ING_TYPE_V2, ingTypeNodeRef);
			NodeRef ingNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ingWithDeletedType"), PLMModel.TYPE_ING, properties).getChildRef();
			return new NodeRef[] { ingTypeNodeRef, ingNodeRef };
		});

		inReadTx(() -> {
			assertNotNull(((IngItem) alfrescoRepository.findOne(nodes[1])).getIngType());
			return null;
		});

		inWriteTx(() -> {
			nodeService.deleteNode(nodes[0]);
			return null;
		});

		inReadTx(() -> {
			assertNull(((IngItem) alfrescoRepository.findOne(nodes[1])).getIngType());
			return null;
		});
	}

}
