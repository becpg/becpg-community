package fr.becpg.test.repo.decernis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.web.client.RestTemplate;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.decernis.DecernisAnalysisPlugin;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.decernis.impl.DecernisServiceImpl;
import fr.becpg.repo.decernis.impl.DefaultDecernisAnalysisPlugin;
import fr.becpg.repo.decernis.impl.V5DecernisAnalysisPlugin;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;
import fr.becpg.util.rest.LogRestTemplate;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class DecernisServiceIT extends AbstractFinishedProductTest {

	private NodeRef usage1NodeRef;
	private NodeRef usage2NodeRef;
	private NodeRef country1NodeRef;
	private NodeRef country2NodeRef;
	private NodeRef nutrientNodeRef;
	private NodeRef flavorNodeRef;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	private MockWebServer mockWebServer;
	
	private String mockServerUrl;
	
	private MockWebServer mockWebAnalysis;
	
	private String mockAnalysisUrl;
	
	private DecernisService decernisService;
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		mockWebServer.shutdown();
		mockWebAnalysis.shutdown();
	}
	
	@Override
	public void setUp() throws Exception {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockServerUrl = "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
		mockWebAnalysis = new MockWebServer();
		mockWebAnalysis.start();
		mockAnalysisUrl = "http://" + mockWebAnalysis.getHostName() + ":" + mockWebAnalysis.getPort();
		RestTemplate restTemplate = new LogRestTemplate();
		DecernisAnalysisPlugin[] decernisPlugins = { new DefaultDecernisAnalysisPlugin(nodeService, systemConfigurationService, restTemplate),
				new V5DecernisAnalysisPlugin(nodeService, systemConfigurationService, restTemplate) };
		decernisService = new DecernisServiceImpl(restTemplate, nodeService, decernisPlugins, systemConfigurationService);
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
			properties.put(BeCPGModel.PROP_LV_CODE, "flavor");
			properties.put(PLMModel.PROP_REGULATORY_CODE, 1010);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_CODE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		});
		
		nutrientNodeRef = inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_CODE, "nutrient supplement");
			properties.put(PLMModel.PROP_REGULATORY_CODE, 1023);
			return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_CODE)),
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
		NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testProductUpdateFromList");
		
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
			
			item1.setRegulatoryState(SystemState.Simulation);
			item2.setRegulatoryState(SystemState.Valid);
			
			productService.formulate(product);
			
			assertFalse(product.getRegulatoryCountries().contains(country1NodeRef));
			assertTrue(product.getRegulatoryCountries().contains(country2NodeRef));
			assertFalse(product.getRegulatoryUsages().contains(usage1NodeRef));
			assertTrue(product.getRegulatoryUsages().contains(usage2NodeRef));
			
			return null;
		});
	}
	
	@Test
	public void testDefaultAnalysis() throws Exception {
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockServerUrl);
			mockWebServer.enqueue(new MockResponse().setBody("{\"id\":118344,\"spec\":\"PF10000122741689244081618\",\"name\":\"PF10000122741689244081618 PF Decernis testDefaultAnalysis\",\"usage\":null,\"usage_verbose\":null,\"company\":140,\"ingredients\":[{\"id\":1510619,\"name\":\"ing1 french\",\"ingredient_did\":6327,\"percentage\":\"1.000000000000000000000000\",\"function\":1023,\"function_verbose\":\"Nutrient Supplement\",\"recipe\":118344,\"customer_code\":null,\"spec_parameters\":null,\"upper_limit\":null,\"orig_subs_ident\":\"CAS\",\"orig_ident_value\":\"68-26-8\",\"dye_content_percent\":null,\"aluminum_percent\":null,\"created\":\"2023-07-13T10:28:06.140575Z\",\"modified\":\"2023-07-13T10:28:06.140608Z\",\"function_does_not_exists\":false}],\"category\":null,\"category_verbose\":null,\"can_be_analyzed_add\":false,\"tag\":null,\"prod_parameters\":null,\"upper_limit\":null,\"author\":\"beCPG Demo\",\"certificates_data\":{},\"module_id\":null,\"created\":\"2023-07-13T10:28:05.442425Z\",\"modified\":\"2023-07-13T10:28:06.148645Z\",\"can_be_analyzed_soi\":true,\"usage_does_not_exists\":false,\"category_does_not_exists\":false}"));
			mockWebServer.enqueue(new MockResponse().setBody("{\"count\":1,\"next\":null,\"previous\":null,\"results\":[{\"country\":\"Germany\",\"region\":\"European Union Members\"}]}"));
			mockWebServer.enqueue(new MockResponse().setBody("{\"analysis_results\":{\"Germany\":{\"matrix\":{\"6327\":{\"name\":\"ing1 french\",\"did\":\"6327\",\"result_indicators\":[{\"result_indicator\":\"Not Listed\",\"amount\":1,\"is_bypass\":false}],\"result_indicator_color\":\"#CCCCCC\"}},\"tabular\":{\"INGREDIENT_DATA_PDF\":[{\"ingredient\":\"Germany|ing1 french\",\"did\":\"6327\",\"function\":\"Nutrient Supplement\",\"functionCode\":\"Thickening agents\",\"resultIndicator\":\"Not Listed\",\"percentage\":\"1.0\",\"threshold\":\"SME LOGIC: This nutrient supplement is not listed in this country's regulation, under review by Decernis. Please use the \\\"report a problem\\\" link for further information/questions.\",\"citation\":\"\",\"citationLink\":\"\",\"function_help_text\":null,\"usage_help_text\":null,\"usage\":11106.0,\"usage_name\":\"Thickening agents\",\"function_name\":\"Nutrient Supplement\",\"country\":\"Germany\",\"result_indicator_color\":\"#CCCCCC\",\"verbose_description\":\"New term is not listed in rules\"}],\"ADDSUM_DATA\":[],\"SCOPE DETAIL\":[]},\"result_indicator\":\"REVIEW\",\"xml\":\"<int:recipeResponse xmlns:int=\\\"http://www.decernis.com/gcomplyplus-rm/service/interface1.0.xsd\\\">\\n  <int:responseIdentifier>\\n    <int:requestGUID>23071310060722215</int:requestGUID>\\n    <int:userID>23071310060722215</int:userID>\\n    <int:requestDT>2023-07-13T10:06:11.000Z</int:requestDT>\\n    <int:requestType>ADD_ANALYSIS_40</int:requestType>\\n    <int:responseGUID>5798bc92b9b54f4f82c1a20dcfdd4815</int:responseGUID>\\n    <int:responseDT>2023-07-13 10:06:12</int:responseDT>\\n  </int:responseIdentifier>\\n  <int:errorInfo>\\n    <int:errorCode>0</int:errorCode>\\n    <int:errorMessage>Successful</int:errorMessage>\\n  </int:errorInfo>\\n  <int:resultInfo>\\n    <int:recipe>\\n      <int:recipeName>PF10000122681689242693451 PF Decernis testDefaultAnalysis</int:recipeName>\\n      <int:recipeID>pf10000122681689242693451-pf-decernis-testdefaultanalysis-118339</int:recipeID>\\n      <int:effectivityDate>2023-07-13</int:effectivityDate>\\n      <int:result>\\n        <int:countryCode>Germany</int:countryCode>\\n        <int:usageCode>Thickening agents</int:usageCode>\\n        <int:resultIndicator>REVIEW</int:resultIndicator>\\n      </int:result>\\n      <int:ingredientResult>\\n        <int:ingredient>\\n          <int:ingredientName>ing1 french</int:ingredientName>\\n          <int:ingredientID>6327-1023</int:ingredientID>\\n          <int:ingredientDID>6327</int:ingredientDID>\\n          <int:result>\\n            <int:functionCode>Nutrient Supplement</int:functionCode>\\n            <int:usageCode>Thickening agents</int:usageCode>\\n            <int:resultIndicator>Not Listed</int:resultIndicator>\\n          </int:result>\\n        </int:ingredient>\\n      </int:ingredientResult>\\n    </int:recipe>\\n  </int:resultInfo>\\n  <int:detailInfo>\\n    <int:detail>\\n      <int:reportType>RECIPE_DATA</int:reportType>\\n      <int:level1>pf10000122681689242693451-pf-decernis-testdefaultanalysis-118339</int:level1>\\n      <int:level2>PF10000122681689242693451 PF Decernis testDefaultAnalysis</int:level2>\\n      <int:level3>Germany</int:level3>\\n      <int:level4>Thickening agents</int:level4>\\n      <int:level5>REVIEW</int:level5>\\n    </int:detail>\\n  </int:detailInfo>\\n  <int:detailInfo>\\n    <int:detail>\\n      <int:reportType>INGREDIENT_DATA_PDF</int:reportType>\\n      <int:level1>Germany|ing1 french</int:level1>\\n      <int:level2>6327</int:level2>\\n      <int:level3>Nutrient Supplement</int:level3>\\n      <int:level4>Thickening agents</int:level4>\\n      <int:level5>Not Listed</int:level5>\\n      <int:level6>1.0</int:level6>\\n      <int:level7>SME LOGIC: This nutrient supplement is not listed in this country's regulation, under review by Decernis. Please use the \\\"report a problem\\\" link for further information/questions.</int:level7>\\n      <int:level8>--</int:level8>\\n      <int:level9>--</int:level9>\\n    </int:detail>\\n  </int:detailInfo>\\n</int:recipeResponse>\\n\"}},\"search_parameters\":{\"recipe_name\":\"PF10000122681689242693451 PF Decernis testDefaultAnalysis\",\"usage\":\"Thickening agents\",\"country\":[\"Germany\"],\"request_id\":23071310060722215,\"module_id\":1},\"sorted_sum_data_all_countries\":[],\"overall_recipe_conclusion\":{\"description\":\"Not Listed\",\"result_indicator_color\":\"#CCCCCC\"},\"report_ids\":{\"combined_pdf\":199481,\"combined_xlsx\":199482}}"));
			mockWebServer.enqueue(new MockResponse().setBody("{\"count\":1,\"next\":null,\"prev\":null,\"results\":{\"Additives\":[{\"id\":657,\"scope_id\":11106,\"category\":\"Additives\",\"phrase\":\"Thickening agents\",\"synonyms\":[],\"help_text\":null}]}}"));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testDefaultAnalysis");
			
			inWriteTx(() -> {
				ProductData product = alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(new IngListDataItem(null, 1d, null, null, null, null, null, ing1, null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsages(new ArrayList<>(List.of(usage1NodeRef)));
				item1.setRegulatoryCountries(new ArrayList<>(List.of(country1NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				List<ReqCtrlListDataItem> requirements = decernisService.extractRequirements(product);
				
				assertEquals(1, requirements.size());
				
				assertEquals(RequirementType.Tolerated, requirements.get(0).getReqType());
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				assertEquals(ing1, requirements.get(0).getCharact());
				
				assertEquals(I18NUtil.getMessage("message.decernis.ingredient.result.permitted"), item1.getRegulatoryResult());
				
				return null;
			});
		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.decernis.serverUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.analysisUrl");
				return null;
			});
		}
		
	}

	@Test
	public void testV5Analysis() throws Exception {
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockAnalysisUrl);
			mockWebAnalysis.enqueue(new MockResponse().setBody("{\"recipeAnalaysisReport\": {\"reportDateTime\": \"2023-07-13T08:44:34.206361Z\", \"recipeName\": \"PF10000122391689237843340 PF test Decernis 2\", \"recipeSpec\": \"PF10000122391689237843340\", \"recipeReport\": [{\"country\": \"Germany\", \"resultIndicator\": \"REVIEW\", \"matrixReport\": [{\"did\": \"6327\", \"resultIndicator\": \"Not Listed\", \"name\": \"ing1 french\", \"spec\": \"ing1 french\", \"idType\": \"Decernis ID\", \"idValue\": \"6327\", \"decernisName\": \"Vitamin A [Retinol]\"}], \"tabularReport\": [{\"name\": \"ing1 french\", \"spec\": \"ing1 french\", \"did\": \"6327\", \"resultIndicator\": \"Not Listed\", \"percentage\": \"1.0\", \"usage\": \"Thickening agents\", \"threshold\": \"SME LOGIC: This nutrient supplement is not listed in this country's regulation, under review by Decernis. Please use the \\\"report a problem\\\" link for further information/questions.\", \"citation\": \"\", \"idType\": \"Decernis ID\", \"idValue\": \"6327\", \"function\": \"Nutrient Supplement\", \"otherIdentifiers\": {\"CAS\": \"11103-57-4, 1341-18-0, 5979-23-7, 68-26-8\", \"INCI name\": \"Retinol\", \"E No.\": \"E672\", \"EC No.\": \"200-683-7, 234-328-2\"}, \"decernisName\": \"Vitamin A [Retinol]\"}], \"detailReport\": []}]}}"));
			mockWebAnalysis.enqueue(new MockResponse().setBody("{\"countries\": [ {\"country_id\": 78, \"country\": \"Germany\"}]}"));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			mockWebAnalysis.enqueue(new MockResponse().setBody("{\"functions\": [\"Acidity Regulator/Buffer/Alkalizing Agents\", \"Anticaking Agent\", \"Antioxidant\", \"Flour Treatment Agent\", \"Bleaching Agent (Not for Flour)\", \"Bulking Agent\", \"Carrier/Solvent\", \"Colorant\", \"Emulsifier\", \"Enzyme/Catalyst\", \"Flavor\", \"Foam Control Agent\", \"Gases\", \"Gelling, Thickening, Stabilizing and Firming Agents\", \"Chewing Gum Base\", \"Humectant\", \"Leavening/Raising Agent\", \"Release Agent\", \"Surface Finishing/Glazing Agent\", \"Preservative\", \"Processing Aid\", \"Sequestrant/Chelating Agent\", \"Sweetener\", \"Nutrient Supplement\", \"Food\", \"Flavor Enhancer\", \"Fat Replacer\", \"Carry-Over\", \"Microorganisms\"]}"));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testV5Analysis");
			
			inWriteTx(() -> {
				ProductData product = alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(new IngListDataItem(null, 1d, null, null, null, null, null, ing1, null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsages(new ArrayList<>(List.of(usage1NodeRef)));
				item1.setRegulatoryCountries(new ArrayList<>(List.of(country1NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				List<ReqCtrlListDataItem> requirements = decernisService.extractRequirements(product);
				
				assertEquals(1, requirements.size());
				
				assertEquals(RequirementType.Tolerated, requirements.get(0).getReqType());
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				assertEquals(ing1, requirements.get(0).getCharact());
				
				assertEquals(I18NUtil.getMessage("message.decernis.ingredient.result.notListed"), item1.getRegulatoryResult());
				
				return null;
			});
		} finally {
			inWriteTx(() -> {
				systemConfigurationService.resetConfValue("beCPG.decernis.serverUrl");
				systemConfigurationService.resetConfValue("beCPG.decernis.analysisUrl");
				return null;
			});
		}
		
	}
	
}
