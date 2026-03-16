package fr.becpg.test.repo.decernis;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.RegulatoryService;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.regulatory.plugins.DecernisRegulatoryPlugin;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;
import fr.becpg.util.MutexFactory;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class DecernisServiceIT extends AbstractFinishedProductTest {

	private NodeRef usage1NodeRef;
	private NodeRef usage2NodeRef;
	private NodeRef country1NodeRef;
	private NodeRef country2NodeRef;
	private NodeRef country3NodeRef;
	private NodeRef nutrientNodeRef;
	private NodeRef flavorNodeRef;
	private NodeRef antioxidantNodeRef;
	private NodeRef preservativeNodeRef;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@Autowired
	private FormulationService<FormulatedEntity> formulationService;
	
	@Autowired
	private EntityActivityService entityActivityService;
	
	@Autowired
	private MutexFactory mutexFactory;
	
	private MockWebServer mockWebServer;
	
	private String mockServerUrl;
	
	private MockWebServer mockWebAnalysis;
	
	private String mockAnalysisUrl;
	
	private RegulatoryService regulatoryService;

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		mockWebServer.shutdown();
		mockWebAnalysis.shutdown();
	}
	
	@Override
	public void setUp() throws Exception {
		
		DecernisRegulatoryPlugin decernisRegulatoryPlugin = new DecernisRegulatoryPlugin(systemConfigurationService, nodeService, alfrescoRepository);
		regulatoryService = new RegulatoryService(nodeService, List.of(decernisRegulatoryPlugin), alfrescoRepository, formulationService,
				batchQueueService, systemConfigurationService, policyBehaviourFilter, entityActivityService, mutexFactory);
		
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockServerUrl = "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
		mockWebAnalysis = new MockWebServer();
		mockWebAnalysis.start();
		mockAnalysisUrl = "http://" + mockWebAnalysis.getHostName() + ":" + mockWebAnalysis.getPort();
		super.setUp();
		initParts();
		
		usage1NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Thickening agents");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "Thickening agents");
			properties.put(PLMModel.PROP_REGULATORY_ID, "Thickening agents");
			properties.put(PLMModel.PROP_REGULATORY_MODULE, "FOOD_ADDITIVES");
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_REGULATORY_USAGE, properties).getChildRef();
		});
		
		usage2NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Margarine and fat spreads");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "Margarine and fat spreads");
			properties.put(PLMModel.PROP_REGULATORY_ID, "Margarine and fat spreads");
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
		
		country3NodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "France");
			properties.put(PLMModel.PROP_REGULATORY_CODE, "France");
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
			properties.put(BeCPGModel.PROP_LV_VALUE, "flavor");
			properties.put(PLMModel.PROP_REGULATORY_CODE, 1010);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		});
		
		preservativeNodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, "preservative");
			properties.put(PLMModel.PROP_REGULATORY_CODE, 1019);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		});
		
		antioxidantNodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, "antioxidant");
			properties.put(PLMModel.PROP_REGULATORY_CODE, 1002);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		});
		
		nutrientNodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, "nutrient supplement");
			properties.put(PLMModel.PROP_REGULATORY_CODE, 1023);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		});
		
		inWriteTx(() -> {
			nodeService.setProperty(ing1, PLMModel.PROP_REGULATORY_CODE, 6327);
			nodeService.setProperty(ing1, PLMModel.PROP_ING_TYPE_V2, nutrientNodeRef);
			
			nodeService.setProperty(ing2, PLMModel.PROP_REGULATORY_CODE, 80018299);
			nodeService.setProperty(ing2, PLMModel.PROP_ING_TYPE_V2, flavorNodeRef);
			
			nodeService.setProperty(ing3, PLMModel.PROP_REGULATORY_CODE, 4476);
			nodeService.setProperty(ing3, PLMModel.PROP_ING_TYPE_V2, antioxidantNodeRef);
			
			nodeService.setProperty(ing4, PLMModel.PROP_REGULATORY_CODE, 4476);
			nodeService.setProperty(ing4, PLMModel.PROP_ING_TYPE_V2, preservativeNodeRef);
			
			return null;
		});
		
	}
	
	private NodeRef createFinishedProduct(final String finishedProductName) {
		return inWriteTx(() -> {
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName(finishedProductName);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});
	}

	/**
	 * Reads a JSON mock response from the classpath.
	 *
	 * @param resourcePath the classpath resource path
	 * @return the normalized JSON response
	 */
	private String readJsonResource(String resourcePath) {
		try {
			ClassPathResource resource = new ClassPathResource(resourcePath);
			return JsonHelper.read(resource.getContentAsString(StandardCharsets.UTF_8)).toString();
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read resource: " + resourcePath, e);
		}
	}
	
	@Test
	public void testProductUpdateFromList()  {
		NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testProductUpdateFromList");
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.token", "TEST_TOKEN");
			systemConfigurationService.updateConfValue("beCPG.decernis.ingredient.analysis.enabled", "false");
			return null;
		});
		
		try {
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				List<IngListDataItem> ingList = product.getIngList();
				ingList.add(IngListDataItem.build().withQtyPerc(1d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null).withIsIonized(null).withIsProcessingAid(null).withIngredient(ing1).withIsManual(null));
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage1NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country1NodeRef)));
				item1.setRegulatoryState(SystemState.Valid);
				RegulatoryListDataItem item2 = new RegulatoryListDataItem();
				item2.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage2NodeRef)));
				item2.setRegulatoryCountriesRef(new ArrayList<>(List.of(country2NodeRef)));
				regulatoryList.add(item1);
				regulatoryList.add(item2);
				return alfrescoRepository.save(product);
			});
				
			inWriteTx(() -> {
				return regulatoryService.checkCompliance(finishedProductNodeRef, false);
			});
				
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				assertTrue(product.getRegulatoryCountriesRef().contains(country1NodeRef));
				assertFalse(product.getRegulatoryCountriesRef().contains(country2NodeRef));
				assertTrue(product.getRegulatoryUsagesRef().contains(usage1NodeRef));
				assertFalse(product.getRegulatoryUsagesRef().contains(usage2NodeRef));
				product.getRegulatoryList().stream().filter(i -> i.getRegulatoryCountriesRef().contains(country1NodeRef)).findFirst().orElseThrow().setRegulatoryState(SystemState.Simulation);
				product.getRegulatoryList().stream().filter(i -> i.getRegulatoryCountriesRef().contains(country2NodeRef)).findFirst().orElseThrow().setRegulatoryState(SystemState.Valid);
				return alfrescoRepository.save(product);
			});
			
			inWriteTx(() -> {
				return regulatoryService.checkCompliance(finishedProductNodeRef, false);
			});
				
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				assertFalse(product.getRegulatoryCountriesRef().contains(country1NodeRef));
				assertTrue(product.getRegulatoryCountriesRef().contains(country2NodeRef));
				assertFalse(product.getRegulatoryUsagesRef().contains(usage1NodeRef));
				assertTrue(product.getRegulatoryUsagesRef().contains(usage2NodeRef));
				return null;
			});
		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.decernis.token");
				systemConfigurationService.resetConfValue("beCPG.decernis.ingredient.analysis.enabled");
				return null;
			});
		}
	}
	
	@Test
	public void testV5Analysis()  {
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockAnalysisUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.ingredient.analysis.enabled", "false");
			mockWebAnalysis.enqueue(new MockResponse().setBody(readJsonResource("beCPG/decernis/functions.json")));
			mockWebAnalysis.enqueue(new MockResponse().setBody(readJsonResource("beCPG/decernis/v5-analysis-response.json")));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testV5Analysis");
			
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(IngListDataItem.build().withQtyPerc(1d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null).withIsIonized(null).withIsProcessingAid(null).withIngredient(ing1).withIsManual(null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage1NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country1NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				return alfrescoRepository.save(product);
			});
			
			inWriteTx(() -> {
				List<RequirementListDataItem> requirements = regulatoryService.checkCompliance(finishedProductNodeRef, false).getContext().getRequirements();
				assertEquals(1, requirements.size());
				assertEquals(RequirementType.Tolerated, requirements.get(0).getReqType());
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				NodeRef charact = requirements.get(0).getCharact();
				IngListDataItem ingListDataItem = (IngListDataItem) alfrescoRepository.findOne(charact);
				assertEquals(ing1, ingListDataItem.getIng());
				return null;
			});
		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.decernis.serverUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.analysisUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.ingredient.analysis.enabled");
				return null;
			});
		}
		
	}

	@Test
	public void testV5AnalysisUsesIngTypes() throws Exception {
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockAnalysisUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.ingredient.analysis.enabled", "false");
			mockWebAnalysis.enqueue(new MockResponse().setBody(readJsonResource("beCPG/decernis/functions.json")));
			mockWebAnalysis.enqueue(new MockResponse().setBody(readJsonResource("beCPG/decernis/v5-analysis-ing-types-response.json")));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testV5AnalysisUsesIngTypes");
			
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				ingList.add(IngListDataItem.build()
						.withQtyPerc(1d)
						.withGeoOrigin(null)
						.withBioOrigin(null)
						.withIsGMO(null)
						.withIsIonized(null)
						.withIsProcessingAid(null)
						.withIngredient(ing1)
						.withIngTypes(List.of(flavorNodeRef, nutrientNodeRef))
						.withIsManual(null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage1NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country1NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				regulatoryList.add(item1);
				
				return alfrescoRepository.save(product);
			});
			
			inWriteTx(() -> {
				List<RequirementListDataItem> requirements = regulatoryService.checkCompliance(finishedProductNodeRef, false).getContext().getRequirements();
				assertEquals(1, requirements.size());
				assertEquals(RequirementType.Tolerated, requirements.get(0).getReqType());
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				NodeRef charact = requirements.get(0).getCharact();
				IngListDataItem ingListDataItem = (IngListDataItem) alfrescoRepository.findOne(charact);
				assertEquals(ing1, ingListDataItem.getIng());
				return null;
			});

			RecordedRequest functionsRequest = mockWebAnalysis.takeRequest();
			assertEquals("GET", functionsRequest.getMethod());
			assertEquals("/scope/function?topic=ADD", functionsRequest.getPath());

			RecordedRequest analysisRequest = mockWebAnalysis.takeRequest();
			assertEquals("POST", analysisRequest.getMethod());
			assertEquals("/recipe-analysis/transaction?report=tabular", analysisRequest.getPath());

			JSONObject payload = new JSONObject(analysisRequest.getBody().readUtf8());
			JSONArray ingredients = payload.getJSONObject("transaction")
					.getJSONObject("recipe")
					.getJSONArray("ingredients");
			assertEquals(2, ingredients.length());

			JSONObject firstIngredient = ingredients.getJSONObject(0);
			assertEquals("6327", firstIngredient.get("idValue").toString());
			assertEquals("Flavor", firstIngredient.getString("function"));
			assertEquals(1d, firstIngredient.getDouble("percentage"));

			JSONObject secondIngredient = ingredients.getJSONObject(1);
			assertEquals("6327", secondIngredient.get("idValue").toString());
			assertEquals("Nutrient Supplement", secondIngredient.getString("function"));
			assertEquals(1d, secondIngredient.getDouble("percentage"));
		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.decernis.serverUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.analysisUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.ingredient.analysis.enabled");
				return null;
			});
		}
	}
	
	@Test
	public void testV5DoubleFunction()  {
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockAnalysisUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.ingredient.analysis.enabled", "false");
			mockWebAnalysis.enqueue(new MockResponse().setBody(readJsonResource("beCPG/decernis/functions.json")));
			mockWebAnalysis.enqueue(new MockResponse().setBody(readJsonResource("beCPG/decernis/v5-double-function-response.json")));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testDefaultDoubleFunction");
			
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(IngListDataItem.build().withQtyPerc(2d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null).withIsIonized(null).withIsProcessingAid(null).withIngredient(ing3).withIsManual(null));
				ingList.add(IngListDataItem.build().withQtyPerc(2d).withGeoOrigin(null).withBioOrigin(null).withIsGMO(null).withIsIonized(null).withIsProcessingAid(null).withIngredient(ing4).withIsManual(null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage2NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country3NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				return alfrescoRepository.save(product);
			});
			
			inWriteTx(() -> {
				List<RequirementListDataItem> requirements = regulatoryService.checkCompliance(finishedProductNodeRef, false).getContext().getRequirements();
				assertEquals(2, requirements.size());
				assertEquals(RequirementType.Forbidden, requirements.get(0).getReqType());
				assertEquals(0.01 / 2 * 100, requirements.get(0).getReqMaxQty());
				assertEquals(RequirementType.Forbidden, requirements.get(1).getReqType());
				assertEquals(5.0 / 2 * 100, requirements.get(1).getReqMaxQty());
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				assertEquals(RequirementDataType.Specification, requirements.get(1).getReqDataType());
				int checks = 0;
				for (RequirementListDataItem req : requirements) {
					NodeRef charact = req.getCharact();
					IngListDataItem ingListDataItem = (IngListDataItem) alfrescoRepository.findOne(charact);
					if (ing3.equals(ingListDataItem.getIng()) || ing4.equals(ingListDataItem.getIng())) {
						checks++;
					}
				}
				assertEquals(2, checks);
				return null;
			});
		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.decernis.serverUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.analysisUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.ingredient.analysis.enabled");
				return null;
			});
		}
		
	}
	
}
