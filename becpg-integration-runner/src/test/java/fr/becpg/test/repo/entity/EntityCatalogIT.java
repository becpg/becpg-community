package fr.becpg.test.repo.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.PLMBaseTestCase;

public class EntityCatalogIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(EntityCatalogIT.class);
	private static final String CATALOG_STRING = "{\"id\":\"incoFinishedProduct\",\"label\":\"EU 1169/2011 (INCO)\",\"entityType\":[\"bcpg:finishedProduct\"],\"uniqueFields\":[\"bcpg:erpCode\",\"cm:name\"],\"fields\":[\"bcpg:legalName\",\"bcpg:useByDate|bcpg:bestBeforeDate\",\"bcpg:storageConditionsRef|bcpg:preparationTips\",\"cm:title\"],\"auditedFields\": [\"cm:name\",\"bcpg:compoList\"],\"modifiedField\": \"bcpg:modifiedCatalog1\"}";
	private static final String CATALOGS_STRING = "[{\"id\":\"incoFinishedProduct\",\"label\":\"EU 1169/2011 (INCO)\",\"entityType\":[\"bcpg:finishedProduct\"],\"uniqueFields\":[\"bcpg:erpCode\",\"cm:name\"],\"fields\":[\"bcpg:legalName\",\"bcpg:useByDate|bcpg:bestBeforeDate\",\"bcpg:storageConditionsRef|bcpg:preparationTips\",\"cm:title\"],\"auditedFields\": [\"cm:name\",\"bcpg:costList\"],\"modifiedField\": \"bcpg:modifiedCatalog1\"},{\"id\":\"incoRawMaterials\",\"label\":\"EU 1169/2011 (INCO)\",\"entityType\":[\"bcpg:rawMaterial\"],\"uniqueFields\":[\"bcpg:erpCode\",\"cm:name\"],\"fields\":[\"bcpg:legalName\"],\"auditedFields\": [\"bcpg:legalName\"],\"modifiedField\": \"bcpg:modifiedCatalog2\"}]";
	@Resource
	protected ProductService productService;
	@Autowired
	TransactionService transactionService;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	EntityCatalogService entityCatalogService;
	@Autowired
	BeCPGCacheService cacheService;


	@Override
	public void setUp() throws Exception {
			super.setUp();
			cacheService.clearCache(EntityCatalogService.class.getName());
			List<JSONArray> res = new ArrayList<>();
			res.add(new JSONArray(CATALOGS_STRING));
			cacheService.storeInCache(EntityCatalogService.class.getName(), EntityCatalogService.CATALOG_DEFS, res);
	}
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		cacheService.clearCache(EntityCatalogService.class.getName());
	}
	
	@Test
	public void catalogServiceTest() {
		NodeRef productNodeRef = createFinishedProduct();
		NodeRef rawMaterialNodeRef = createRawMaterial();
		//update non audited fields
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(productNodeRef, BeCPGModel.PROP_LEGAL_NAME, "update product legal name");
			nodeService.setProperty(rawMaterialNodeRef, ContentModel.PROP_NAME, "update rawMaterial Name");
			return null;
		}, false, false);
		assertNull(nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService)));
		assertNull(nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService)));
		assertNull(nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService)));
		assertNull(nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService)));
		
		Date beforeRef =  new Date();
		
		// update audited fields
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(productNodeRef, ContentModel.PROP_NAME, "update product Name");
			nodeService.setProperty(rawMaterialNodeRef, BeCPGModel.PROP_LEGAL_NAME, "update rawMaterial legal name");
			return null;
		}, false, false);
		assertNotNull(nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService)));
		assertNull(nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService)));
		assertNotNull(nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService)));
		assertNull(nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService)));
		
		//update audited lists
		Date afterRef = new Date();
		assertTrue(beforeRef.before((Date)nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService))));
		assertTrue(afterRef.after((Date)nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService))));
		assertTrue(beforeRef.before((Date)nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService))));
		assertTrue(afterRef.after((Date)nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService))));

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
			RawMaterialData rawMaterialData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);
			
			NodeRef nodeRef = finishedProductData.getAllergenList().get(0).getAllergen();
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, true);
			rawMaterialData.getCostList().add(new CostListDataItem(null, 4000d, "€", null, costs.get(0), true));
			
			alfrescoRepository.save(finishedProductData);
			alfrescoRepository.save(rawMaterialData);
			return null;
		}, false, true);
		
		assertTrue(beforeRef.before((Date)nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService))));
		assertTrue(afterRef.after((Date)nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService))));
		assertTrue(beforeRef.before((Date)nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService))));
		assertTrue(afterRef.after((Date)nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService))));
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
			RawMaterialData rawMaterialData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);
			
			finishedProductData.getCostList().add(new CostListDataItem(null, 4000d, "€", null, costs.get(0), true));
			NodeRef nodeRef = rawMaterialData.getAllergenList().get(0).getAllergen();
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, true);
			
			alfrescoRepository.save(finishedProductData);
			alfrescoRepository.save(rawMaterialData);
			return null;
		}, false, true);
		
		assertTrue(beforeRef.before((Date)nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService))));
		assertFalse(afterRef.after((Date)nodeService.getProperty(productNodeRef, QName.createQName("bcpg:modifiedCatalog1", namespaceService))));
		assertTrue(beforeRef.before((Date)nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService))));
		assertTrue(afterRef.after((Date)nodeService.getProperty(rawMaterialNodeRef, QName.createQName("bcpg:modifiedCatalog2", namespaceService))));
	}

	@Test
	public void catalogHelperTest() {
		JSONObject catalog;
		try {
			catalog = new JSONObject(CATALOG_STRING);
			assertTrue(entityCatalogService.isMatchEntityType(catalog, PLMModel.TYPE_FINISHEDPRODUCT, namespaceService));
			assertFalse(entityCatalogService.isMatchEntityType(catalog, PLMModel.TYPE_RAWMATERIAL, namespaceService));
			assertEquals(new HashSet<QName>(Arrays.asList(new QName[] {ContentModel.PROP_NAME, QName.createQName("bcpg:compoList", namespaceService)})), 
					entityCatalogService.getAuditedFields(catalog, namespaceService));
		} catch (JSONException e) {
			logger.error("Unable to load catalog", e);
		}

	}

	private NodeRef createFinishedProduct() {
		
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("product test");
			finishedProduct.setLegalName("product one legal name");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setUnitPrice(22.4d);
			finishedProduct.setDensity(1d);
			finishedProduct.setServingSize(50d);// 50g
			finishedProduct.setProjectedQty(10000l);

			List<CostListDataItem> costList = new ArrayList<>();
			finishedProduct.setCostList(costList);

			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null,null, true, true, null, null, allergens.get(0), false));
			finishedProduct.setAllergenList(allergenList);
			
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);
	}
	
	private NodeRef createRawMaterial() {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.info("/*-- Create Raw Material --*/");
			RawMaterialData rawMaterial = new RawMaterialData();
			rawMaterial.setName("Raw Material one");
			rawMaterial.setLegalName("legal name");
			rawMaterial.setUnit(ProductUnit.kg);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 4000d, "€", null, costs.get(0), true));
			rawMaterial.setCostList(costList);

			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null,null, true, true, null, null, allergens.get(0), false));
			rawMaterial.setAllergenList(allergenList);
			
			return alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial).getNodeRef();

		}, false, true);
	}


}
