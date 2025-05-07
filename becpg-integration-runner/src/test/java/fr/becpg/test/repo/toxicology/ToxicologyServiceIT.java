package fr.becpg.test.repo.toxicology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.PLMBaseTestCase;

public class ToxicologyServiceIT extends PLMBaseTestCase {

	@Autowired
	private Repository repository;
	
	@Autowired
	private ProductService productService;
	
	private NodeRef adultROHairNodeRef;
	
	private NodeRef adultROBodyNodeRef;
	
	private NodeRef adultROFaceNodeRef;
	
	private NodeRef glycerinNodeRef;
	
	private NodeRef citricAcidNodeRef;
	
	private NodeRef alpiniaNodeRef;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		NodeRef companyHomeNodeRef = repository.getCompanyHome();
		NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
		NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
		NodeRef listContainer = nodeService.getChildByName(charactsNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
		NodeRef toxFolder = nodeService.getChildByName(listContainer, ContentModel.ASSOC_CONTAINS, PlmRepoConsts.PATH_TOXICITIES);
		
		inWriteTx(() -> {
			NodeRef nodeRef = BeCPGQueryBuilder.createQuery().andPropEquals(BeCPGModel.PROP_CHARACT_NAME, "Adult RO Hair - test").singleValue();
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Adult RO Hair - test");
			properties.put(PLMModel.PROP_TOX_VALUE, 110);
			properties.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, true);
			properties.put(PLMModel.PROP_TOX_CALCULATE_MAX, true);
			properties.put(PLMModel.PROP_TOX_TYPES, new ArrayList<>(List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
			adultROHairNodeRef = nodeService.createNode(toxFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_TOX, properties).getChildRef();
		
			nodeRef = BeCPGQueryBuilder.createQuery().andPropEquals(BeCPGModel.PROP_CHARACT_NAME, "Adult RO Body - test").singleValue();
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Adult RO Body - test");
			properties.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, true);
			properties.put(PLMModel.PROP_TOX_CALCULATE_MAX, true);
			properties.put(PLMModel.PROP_TOX_VALUE, 220);
			properties.put(PLMModel.PROP_TOX_TYPES, new ArrayList<>(List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
			adultROBodyNodeRef = nodeService.createNode(toxFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_TOX, properties).getChildRef();
		
			nodeRef = BeCPGQueryBuilder.createQuery().andPropEquals(BeCPGModel.PROP_CHARACT_NAME, "Adult RO Face - test").singleValue();
			if (nodeRef != null) {
				nodeService.deleteNode(nodeRef);
			}
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Adult RO Face - test");
			properties.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, true);
			properties.put(PLMModel.PROP_TOX_CALCULATE_MAX, true);
			properties.put(PLMModel.PROP_TOX_VALUE, 140);
			properties.put(PLMModel.PROP_TOX_TYPES, new ArrayList<>(List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.OcularIrritation, ToxType.SystemicIngredient)));
			adultROFaceNodeRef = nodeService.createNode(toxFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_TOX, properties).getChildRef();
		
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Glycerin");
			properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, 10000);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 18);
			properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 80);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 85);
			glycerinNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Citric acid");
			properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, 1200);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 0.2);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 100);
			citricAcidNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Alpania");
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 50);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 1);
			properties.put(PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION, 5);
			properties.put(PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC, 5);
			alpiniaNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			return null;
		});
		waitForIndex(adultROHairNodeRef);
		waitForIndex(adultROBodyNodeRef);
		waitForIndex(adultROFaceNodeRef);
	}
	
	@Test
	public void testToxIngTableCreation() {
		inWriteTx(() -> {
			nodeService.setProperty(glycerinNodeRef, PLMModel.PROP_ING_TOX_DATA, true);
			return null;
		});
		waitForSolr();
		inWriteTx(() -> {
			NodeRef toxIngNodeRef1 = BeCPGQueryBuilder.createQuery().andPropEquals(PLMModel.PROP_TOX_ING_ING, glycerinNodeRef.toString()).andPropEquals(PLMModel.PROP_TOX_ING_TOX, adultROHairNodeRef.toString()).singleValue();
			assertNotNull(toxIngNodeRef1);
			assertEquals(30303.0303030303, (Double) nodeService.getProperty(toxIngNodeRef1, PLMModel.PROP_TOX_ING_SYSTEMIC_VALUE), 0.001);
			assertEquals(80, (Double) nodeService.getProperty(toxIngNodeRef1, PLMModel.PROP_TOX_ING_MAX_VALUE), 0.001);
			nodeService.deleteNode(toxIngNodeRef1);
			
			NodeRef toxIngNodeRef2 = BeCPGQueryBuilder.createQuery().andPropEquals(PLMModel.PROP_TOX_ING_ING, glycerinNodeRef.toString()).andPropEquals(PLMModel.PROP_TOX_ING_TOX, adultROBodyNodeRef.toString()).singleValue();
			assertNotNull(toxIngNodeRef2);
			assertEquals(15151.51515151515, (Double) nodeService.getProperty(toxIngNodeRef2, PLMModel.PROP_TOX_ING_SYSTEMIC_VALUE), 0.001);
			assertEquals(80, (Double) nodeService.getProperty(toxIngNodeRef2, PLMModel.PROP_TOX_ING_MAX_VALUE), 0.001);
			nodeService.deleteNode(toxIngNodeRef2);
			
			NodeRef toxIngNodeRef3 = BeCPGQueryBuilder.createQuery().andPropEquals(PLMModel.PROP_TOX_ING_ING, glycerinNodeRef.toString()).andPropEquals(PLMModel.PROP_TOX_ING_TOX, adultROFaceNodeRef.toString()).singleValue();
			assertNotNull(toxIngNodeRef3);
			assertEquals(23809.52380952381, (Double) nodeService.getProperty(toxIngNodeRef3, PLMModel.PROP_TOX_ING_SYSTEMIC_VALUE), 0.001);
			assertEquals(50, (Double) nodeService.getProperty(toxIngNodeRef3, PLMModel.PROP_TOX_ING_MAX_VALUE), 0.001);
			nodeService.deleteNode(toxIngNodeRef3);
			
			assertEquals(false, nodeService.getProperty(glycerinNodeRef, PLMModel.PROP_ING_TOX_DATA));
			
			List<NodeRef> otherToxItems = BeCPGQueryBuilder.createQuery().andPropEquals(PLMModel.PROP_TOX_ING_ING, glycerinNodeRef.toString()).list();
			
			for (NodeRef otherToxItem : otherToxItems) {
				nodeService.deleteNode(otherToxItem);
			}
			
			return null;
		});
	}
	
	private void waitForIndex(NodeRef nodeRef) {

		inReadTx(() -> {
			int j = 0;
			while ((BeCPGQueryBuilder.createQuery().andPropEquals(BeCPGModel.PROP_CHARACT_NAME, (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CHARACT_NAME)).singleValue() == null)
					&& (j < 30)) {

				Thread.sleep(2000);
				j++;
			}
			
			if(j == 30) {
				Assert.fail("Solr is taking too long!");
			}

			return null;

		});

	}
	
	@Test
	public void testToxListFormulation() throws Exception {
		
		inWriteTx(() -> {
			FinishedProductData product = new FinishedProductData();
			product.setToxList(new ArrayList<>());
			product.setIngList(new ArrayList<>());
			
			List<ToxListDataItem> toxList = product.getToxList();
			
			ToxListDataItem tox1 = new ToxListDataItem();
			tox1.setTox(adultROBodyNodeRef);
			toxList.add(tox1);
			
			ToxListDataItem tox2 = new ToxListDataItem();
			tox2.setTox(adultROHairNodeRef);
			toxList.add(tox2);
			
			ToxListDataItem tox3 = new ToxListDataItem();
			tox3.setTox(adultROFaceNodeRef);
			toxList.add(tox3);
			
			List<IngListDataItem> ingList = product.getIngList();
			
			IngListDataItem ing1 = new IngListDataItem();
			ing1.setIng(glycerinNodeRef);
			ing1.setQtyPerc(90.0);
			ingList.add(ing1);
			
			IngListDataItem ing2 = new IngListDataItem();
			ing2.setIng(citricAcidNodeRef);
			ing2.setQtyPerc(5.0);
			ingList.add(ing2);
			
			IngListDataItem ing3 = new IngListDataItem();
			ing3.setIng(alpiniaNodeRef);
			ing3.setQtyPerc(5.0);
			ingList.add(ing3);
			
			productService.formulate(product);
			
			assertEquals(20.0, tox1.getValue(), 0.1);
			assertEquals(20.0, tox2.getValue(), 0.1);
			assertEquals(4.0, tox3.getValue(), 0.1);
			
			return null;
		});
	}
	
}
