package fr.becpg.test.repo.entity.catalog;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.entity.catalog.EntityCatalogServiceImpl;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.PLMBaseTestCase;

/**
 *
 * @author matthieu
 *
 */
public class EntityCatalogIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(EntityCatalogIT.class);
	private static final String CATALOG_STRING = "{\"id\":\"incoFinishedProduct\",\"label\":\"EU 1169/2011 (INCO)\",\"entityType\":[\"bcpg:finishedProduct\"],\"uniqueFields\":[\"bcpg:erpCode\",\"cm:name\"],\"fields\":[\"bcpg:legalName\",\"bcpg:useByDate|bcpg:bestBeforeDate\",\"bcpg:storageConditionsRef|bcpg:preparationTips\",\"cm:title\"],\"auditedFields\": [\"cm:name\",\"bcpg:compoList\"],\"modifiedField\": \"bcpg:modifiedCatalog1\"}";

	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private EntityCatalogService entityCatalogService;
	@Autowired
	private BeCPGCacheService cacheService;
	@Autowired
	private EntityListDAO entityListDAO;
	@Autowired
	private AssociationService associationService;
	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	@Autowired
	private FormulationService<FormulatedEntity> formulationService;
	

	@Override
	public void setUp() throws Exception {
		super.setUp();

		cacheService.clearCache(EntityCatalogService.class.getName());
		ClassPathResource resource = new ClassPathResource("beCPG/test/audited_fields.json");
		try (InputStream in = resource.getInputStream()) {
			List<JSONArray> res = new ArrayList<>();
			res.add(new JSONArray(IOUtils.toString(in, "UTF-8")));
			cacheService.storeInCache(EntityCatalogService.class.getName(), EntityCatalogService.CATALOG_DEFS, res);
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		cacheService.clearCache(EntityCatalogService.class.getName());
	}

	
	@Test
	public void testMissingFields() {

		final NodeRef sfNodeRef = inWriteTx(() -> {

			SemiFinishedProductData sfData = new SemiFinishedProductData();
			sfData.setName("EntityCatalogServiceIT");
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, null, true, true, null, null, allergens.get(0), false));
			allergenList.add(new AllergenListDataItem(null, null, false, true, null, null, allergens.get(1), false));
			allergenList.add(new AllergenListDataItem(null, null, true, false, null, null, allergens.get(2), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergens.get(3), false));
			sfData.setAllergenList(allergenList);

			return alfrescoRepository.create(getTestFolderNodeRef(), sfData).getNodeRef();

		});

		final SemiFinishedProductData sampleProduct = (SemiFinishedProductData) inReadTx(() -> alfrescoRepository.findOne(sfNodeRef));

		
		inWriteTx(() -> {
			formulationService.formulate(sampleProduct);
			
			
			JSONObject scoresObject = new JSONObject(sampleProduct.getEntityScore());
			
			JSONArray missingFieldsArray = scoresObject.getJSONArray("catalogs").getJSONObject(0).getJSONArray("missingFields");
			assertNotNull(missingFieldsArray);

			String missingFieldsString = missingFieldsArray.toString();
			logger.info("Missing fields: " + missingFieldsString);
			assertTrue(missingFieldsString.contains("bcpg:legalName"));
			assertTrue(missingFieldsString.contains("bcpg:useByDate|bcpg:bestBeforeDate"));
			assertTrue(missingFieldsString.contains("bcpg:storageConditionsRef|bcpg:preparationTips"));
			assertTrue(missingFieldsString.contains("formula1"));
			assertTrue(missingFieldsString.contains("formula2"));
			assertTrue(missingFieldsString.contains("Conditions de conservation ou Conseils de préparation et d'utilisation"));
			assertTrue(missingFieldsString.contains("formula2.missingKey"));

			sampleProduct.setUnit(ProductUnit.L);
			MLText title = new MLText();
			title.addValue(Locale.getDefault(), "Sample");
			
			sampleProduct.setTitle(title);
			sampleProduct.setLegalName("Test");
			
			alfrescoRepository.save(sampleProduct);
			formulationService.formulate(sampleProduct);
			
			scoresObject = new JSONObject(sampleProduct.getEntityScore());
			
			 missingFieldsArray = scoresObject.getJSONArray("catalogs").getJSONObject(0).getJSONArray("missingFields");
			assertNotNull(missingFieldsArray);

			 missingFieldsString = missingFieldsArray.toString();
			logger.info("Missing fields 2: " + missingFieldsString);
			
			assertFalse(missingFieldsString.contains("bcpg:legalName"));
			assertFalse(missingFieldsString.contains("cm:titled"));
			assertTrue(missingFieldsString.contains("bcpg:storageConditionsRef|bcpg:preparationTips"));
			assertFalse(missingFieldsString.contains("formula1"));
			assertTrue(missingFieldsString.contains("formula2"));
			
			return true;
		});
		
	}
	
	
	@Test
	public void testAuditedFields() {

		final NodeRef sfNodeRef = inWriteTx(() -> {
			
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			NodeRef rawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

			SemiFinishedProductData sfData = new SemiFinishedProductData();
			sfData.setName("EntityCatalogServiceIT");
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, null, true, true, null, null, allergens.get(0), true));
			allergenList.add(new AllergenListDataItem(null, null, false, true, null, null, allergens.get(1), true));
			allergenList.add(new AllergenListDataItem(null, null, true, false, null, null, allergens.get(2), true));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergens.get(3), true));
				
			List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
			labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterialNodeRef));
		
			sfData.getCompoListView().setCompoList(compoList1);
			sfData.getLabelingListView().setLabelingRuleList(labelingRuleList);
			sfData.setAllergenList(allergenList);
			

			return alfrescoRepository.create(getTestFolderNodeRef(), sfData).getNodeRef();

		});

		final SemiFinishedProductData sampleProduct = (SemiFinishedProductData) inReadTx(() -> alfrescoRepository.findOne(sfNodeRef));
		

		inWriteTx(() -> {
			return formulationService.formulate(sfNodeRef);
		});


		long timestamps = Calendar.getInstance().getTimeInMillis();

		/*
		 * Test list change
		 */

		// setProperty of allergen without changing anything => nothing changed
		inWriteTx(() -> {

			NodeRef nodeRef = sampleProduct.getAllergenList().get(0).getAllergen();
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, true);
			return null;

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, false);

		// setProperty of allergen and change smth => modified
		inWriteTx(() -> {

			NodeRef nodeRef = sampleProduct.getAllergenList().get(0).getNodeRef();
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);

			return null;

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);

		// add an allergen
		inWriteTx(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(sampleProduct.getNodeRef());
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_ALLERGENLIST);
			NodeRef allergen = allergens.get(5);
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PLMModel.PROP_ALLERGENLIST_INVOLUNTARY, true);
			properties.put(PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);
			ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, allergen.getId()), PLMModel.TYPE_ALLERGENLIST, properties);
			NodeRef linkNodeRef = childAssocRef.getChildRef();
			nodeService.createAssociation(linkNodeRef, allergen, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN);

			return null;

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);

		// remove an allergen
		inWriteTx(() -> {

			NodeRef nodeRef = sampleProduct.getAllergenList().get(1).getNodeRef();
			nodeService.deleteNode(nodeRef);

			return null;

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);

		/*
		 * Test property change
		 */

		inWriteTx(() -> {

			MLText title = new MLText();
			title.addValue(Locale.getDefault(),"Test new title");
			
			sampleProduct.setTitle(title);

			return alfrescoRepository.save(sampleProduct);

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);

		// Not audited field
		inWriteTx(() -> {

			sampleProduct.setDensity(60d);

			return alfrescoRepository.save(sampleProduct);

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, false);

		inWriteTx(() -> {

			sampleProduct.setTitle(null);

			return alfrescoRepository.save(sampleProduct);

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);

		// Test assoc change

		inWriteTx(() -> {

			ClientData client = new ClientData();
			client.setName("EntityCatalogServiceIT - client");
			client.setParentNodeRef(getTestFolderNodeRef());
			alfrescoRepository.save(client);

			associationService.update(sampleProduct.getNodeRef(), PLMModel.ASSOC_CLIENTS, Arrays.asList(client.getNodeRef()));
			return null;

		});

		timestamps = checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);

		inWriteTx(() -> {

			sampleProduct.setClients(new ArrayList<>());

			associationService.update(sampleProduct.getNodeRef(), PLMModel.ASSOC_CLIENTS, new ArrayList<>());

			return null;

		});

		timestamps =  checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);
		
		
		inWriteTx(() -> {
			return formulationService.formulate(sfNodeRef);
		});
		
		timestamps =  checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);
		
		inWriteTx(() -> {
			return formulationService.formulate(sfNodeRef);
		});
		
		timestamps =  checkIsAudited(sampleProduct.getNodeRef(), timestamps, false);
		
		inWriteTx(() -> {
			nodeService.setProperty(sampleProduct.getCompoList().get(0).getCharactNodeRef(), ContentModel.PROP_NAME, "Test update ingLabelling");
			
			return null;
		});
		
		timestamps =  checkIsAudited(sampleProduct.getNodeRef(), timestamps, false);
		
		inWriteTx(() -> {
			try {
				policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

					L2CacheSupport.doInCacheContext(() -> 
						AuthenticationUtil.runAsSystem(() -> {
							
							return formulationService.formulate(sfNodeRef);
						
						}), false, true);
					
					return null;

			} finally {
				policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
			
		
		});
		
		timestamps =  checkIsAudited(sampleProduct.getNodeRef(), timestamps, true);
	}

	@Test
	public void catalogHelperTest() {
		JSONObject catalog;
		try {
			catalog = new JSONObject(CATALOG_STRING);
			assertTrue(((EntityCatalogServiceImpl) entityCatalogService).isMatchEntityType(catalog, PLMModel.TYPE_FINISHEDPRODUCT, namespaceService));
			assertFalse(((EntityCatalogServiceImpl)entityCatalogService).isMatchEntityType(catalog, PLMModel.TYPE_RAWMATERIAL, namespaceService));
			assertEquals(new HashSet<>(Arrays.asList(new QName[] { ContentModel.PROP_NAME, QName.createQName("bcpg:compoList", namespaceService) })),
					((EntityCatalogServiceImpl)entityCatalogService).getAuditedFields(catalog, namespaceService));
		} catch (JSONException e) {
			logger.error("Unable to load catalog", e);
		}

	}

	private long checkIsAudited(NodeRef nodeRef, long timestamps, boolean hasChanged) {

		Date modified = retrieveCatalogDate(nodeRef);

		if (hasChanged) {
			assertTrue(timestamps < modified.getTime());
		} else {
			assertFalse(timestamps < modified.getTime());
		}

		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps < modified.getTime());
		return timestamps;
	}

	private Date retrieveCatalogDate(NodeRef noderef) {

		return inReadTx(() -> (Date) nodeService.getProperty(noderef, PLMModel.PROP_MODIFIED_CATALOG3));
	}

}