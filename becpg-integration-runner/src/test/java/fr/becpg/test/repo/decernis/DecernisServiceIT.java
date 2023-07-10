package fr.becpg.test.repo.decernis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class DecernisServiceIT extends AbstractFinishedProductTest {

	private NodeRef usage1NodeRef;
	private NodeRef usage2NodeRef;
	private NodeRef country1NodeRef;
	private NodeRef country2NodeRef;
	private NodeRef nutrientNodeRef;
	private NodeRef flavorNodeRef;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
		
		usage1NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Thickening agents");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "Thickening agents");
			properties.put(PLMModel.PROP_REGULATORY_MODULE, "FOOD_ADDITIVES");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_REGULATORY_USAGE, properties).getChildRef();
		});
		
		usage2NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Margarine and fat spreads");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "Margarine and fat spreads");
			properties.put(PLMModel.PROP_REGULATORY_MODULE, "FOOD_ADDITIVES");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_REGULATORY_USAGE, properties).getChildRef();
		});
		
		country1NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Germany");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "Germany");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
		});
		
		country2NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Korea");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "Korea");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
		});
		
		country2NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Korea");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "Korea");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
		});
		
		flavorNodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LKV_VALUE, "flavor");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LKV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		});
		
		nutrientNodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LKV_VALUE, "nutrient");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LKV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		});
		
		inWriteTx(() -> {
			nodeService.setProperty(ing1, PLMModel.PROP_REGULATORY_CODE, 6327);
			nodeService.setProperty(ing1, PLMModel.PROP_ING_TYPE_V2, nutrientNodeRef);
			
			nodeService.setProperty(ing2, PLMModel.PROP_REGULATORY_CODE, 80018299);
			nodeService.setProperty(ing2, PLMModel.PROP_ING_TYPE_V2, flavorNodeRef);
			
			return null;
		});
		
	}
	
	public NodeRef createFinishedProduct(final String finishedProductName) throws Exception {
		return inWriteTx(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName(finishedProductName);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});
	}
	
	@Test
	public void testProductUpdateFromList() throws Exception {
		NodeRef finishedProductNodeRef = createFinishedProduct("PF test Decernis 1");
		
		inWriteTx(() -> {
			ProductData product = alfrescoRepository.findOne(finishedProductNodeRef);
			
			List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
			
			RegulatoryListDataItem item1 = new RegulatoryListDataItem();
			item1.setRegulatoryUsages(new ArrayList<>(List.of(usage1NodeRef)));
			item1.setRegulatoryCountries(new ArrayList<>(List.of(country1NodeRef)));
			item1.setRegulatoryState(SystemState.Valid);
			
			RegulatoryListDataItem item2 = new RegulatoryListDataItem();
			item2.setRegulatoryUsages(new ArrayList<>(List.of(usage2NodeRef)));
			item2.setRegulatoryCountries(new ArrayList<>(List.of(country2NodeRef)));
			
			regulatoryList.add(item1);
			regulatoryList.add(item2);
			
			productService.formulate(product);
			
			assertTrue(product.getRegulatoryCountries().contains(country1NodeRef));
			assertFalse(product.getRegulatoryCountries().contains(country2NodeRef));
			assertTrue(product.getRegulatoryUsages().contains(usage1NodeRef));
			assertFalse(product.getRegulatoryUsages().contains(usage2NodeRef));
			
			return null;
		});
	}
	
}
