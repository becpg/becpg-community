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
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

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
		DecernisAnalysisPlugin[] decernisPlugins = { new DefaultDecernisAnalysisPlugin(nodeService, systemConfigurationService),
				new V5DecernisAnalysisPlugin(nodeService, systemConfigurationService) };
		decernisService = new DecernisServiceImpl(nodeService, decernisPlugins, systemConfigurationService, alfrescoRepository);
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
	
	private NodeRef createFinishedProduct(final String finishedProductName) throws Exception {
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
			systemConfigurationService.updateConfValue("beCPG.decernis.token", "TEST_TOKEN");
			systemConfigurationService.updateConfValue("beCPG.decernis.ingredient.analysis.enabled", "false");
			return null;
		});
		
		try {
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
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
				
				productService.formulate(product, DecernisService.DECERNIS_CHAIN_ID);
				
				assertTrue(product.getRegulatoryCountriesRef().contains(country1NodeRef));
				assertFalse(product.getRegulatoryCountriesRef().contains(country2NodeRef));
				assertTrue(product.getRegulatoryUsagesRef().contains(usage1NodeRef));
				assertFalse(product.getRegulatoryUsagesRef().contains(usage2NodeRef));
				
				item1.setRegulatoryState(SystemState.Simulation);
				item2.setRegulatoryState(SystemState.Valid);
				
				productService.formulate(product, DecernisService.DECERNIS_CHAIN_ID);
				
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
	public void testDefaultAnalysis() throws Exception {
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockServerUrl);
			mockWebServer.enqueue(new MockResponse().setBody("{" +
				    "\"id\": 118344," +
				    "\"spec\": \"PF10000122741689244081618\"," +
				    "\"name\": \"PF10000122741689244081618 PF Decernis testDefaultAnalysis\"," +
				    "\"usage\": null," +
				    "\"usage_verbose\": null," +
				    "\"company\": 140," +
				    "\"ingredients\": [{" +
				        "\"id\": 1510619," +
				        "\"name\": \"ing1 french\"," +
				        "\"ingredient_did\": 6327," +
				        "\"percentage\": \"1.000000000000000000000000\"," +
				        "\"function\": 1023," +
				        "\"function_verbose\": \"Nutrient Supplement\"," +
				        "\"recipe\": 118344," +
				        "\"customer_code\": null," +
				        "\"spec_parameters\": null," +
				        "\"upper_limit\": null," +
				        "\"orig_subs_ident\": \"CAS\"," +
				        "\"orig_ident_value\": \"68-26-8\"," +
				        "\"dye_content_percent\": null," +
				        "\"aluminum_percent\": null," +
				        "\"created\": \"2023-07-13T10:28:06.140575Z\"," +
				        "\"modified\": \"2023-07-13T10:28:06.140608Z\"," +
				        "\"function_does_not_exists\": false" +
				    "}]," +
				    "\"category\": null," +
				    "\"category_verbose\": null," +
				    "\"can_be_analyzed_add\": false," +
				    "\"tag\": null," +
				    "\"prod_parameters\": null," +
				    "\"upper_limit\": null," +
				    "\"author\": \"beCPG Demo\"," +
				    "\"certificates_data\": {}," +
				    "\"module_id\": null," +
				    "\"created\": \"2023-07-13T10:28:05.442425Z\"," +
				    "\"modified\": \"2023-07-13T10:28:06.148645Z\"," +
				    "\"can_be_analyzed_soi\": true," +
				    "\"usage_does_not_exists\": false," +
				    "\"category_does_not_exists\": false" +
				"}"));
			mockWebServer.enqueue(new MockResponse().setBody("{\"count\":1,\"next\":null,\"previous\":null,\"results\":[{\"country\":\"Germany\",\"region\":\"European Union Members\"}]}"));
			mockWebServer.enqueue(new MockResponse().setBody("{" +
				    "\"analysis_results\":{" +
			        "\"Germany\":{" +
			            "\"matrix\":{" +
			                "\"6327\":{" +
			                    "\"name\":\"ing1 french\"," +
			                    "\"did\":\"6327\"," +
			                    "\"result_indicators\":[{" +
			                        "\"result_indicator\":\"Not Listed\"," +
			                        "\"amount\":1," +
			                        "\"is_bypass\":false" +
			                    "}]," +
			                    "\"result_indicator_color\":\"#CCCCCC\"" +
			                "}" +
			            "}," +
			            "\"tabular\":{" +
			                "\"INGREDIENT_DATA_PDF\":[{" +
			                    "\"ingredient\":\"Germany|ing1 french\"," +
			                    "\"did\":\"6327\"," +
			                    "\"function\":\"Nutrient Supplement\"," +
			                    "\"functionCode\":\"Thickening agents\"," +
			                    "\"resultIndicator\":\"Not Listed\"," +
			                    "\"percentage\":\"1.0\"," +
			                    "\"threshold\":\"SME LOGIC: This nutrient supplement is not listed in this country's regulation, under review by Decernis. Please use the \\\"report a problem\\\" link for further information/questions.\"," +
			                    "\"citation\":\"\"," +
			                    "\"citationLink\":\"\"," +
			                    "\"function_help_text\":null," +
			                    "\"usage_help_text\":null," +
			                    "\"usage\":11106.0," +
			                    "\"usage_name\":\"Thickening agents\"," +
			                    "\"function_name\":\"Nutrient Supplement\"," +
			                    "\"country\":\"Germany\"," +
			                    "\"result_indicator_color\":\"#CCCCCC\"," +
			                    "\"verbose_description\":\"New term is not listed in rules\"" +
			                "}]," +
			                "\"ADDSUM_DATA\":[]," +
			                "\"SCOPE DETAIL\":[]" +
			            "}," +
			            "\"result_indicator\":\"REVIEW\"" +
			        "}" +
			    "}," +
			    "\"search_parameters\":{" +
			        "\"recipe_name\":\"PF10000122681689242693451 PF Decernis testDefaultAnalysis\"," +
			        "\"usage\":\"Thickening agents\"," +
			        "\"country\":[\"Germany\"]," +
			        "\"request_id\":23071310060722215," +
			        "\"module_id\":1" +
			    "}," +
			    "\"sorted_sum_data_all_countries\":[]," +
			    "\"overall_recipe_conclusion\":{" +
			        "\"description\":\"Not Listed\"," +
			        "\"result_indicator_color\":\"#CCCCCC\"" +
			    "}," +
			    "\"report_ids\":{" +
			        "\"combined_pdf\":199481," +
			        "\"combined_xlsx\":199482" +
			    "}" +
			"}"
));
			mockWebServer.enqueue(new MockResponse().setBody("{\"count\":1,\"next\":null,\"prev\":null,\"results\":{\"Additives\":[{\"id\":657,\"scope_id\":11106,\"category\":\"Additives\",\"phrase\":\"Thickening agents\",\"synonyms\":[],\"help_text\":null}]}}"));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testDefaultAnalysis");
			
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(new IngListDataItem(null, 1d, null, null, null, null, null, ing1, null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage1NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country1NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				List<RequirementListDataItem> requirements = decernisService.extractRequirements(product);
				
				assertEquals(1, requirements.size());
				
				assertEquals(RequirementType.Tolerated, requirements.get(0).getReqType());
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				assertEquals(ing1, requirements.get(0).getCharact());
				
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
			systemConfigurationService.updateConfValue("beCPG.decernis.ingredient.analysis.enabled", "false");
			mockWebAnalysis.enqueue(new MockResponse().setBody("{" +
				    "\"functions\": [" +
			        "\"Acidity Regulator/Buffer/Alkalizing Agents\"," +
			        "\"Anticaking Agent\"," +
			        "\"Antioxidant\"," +
			        "\"Flour Treatment Agent\"," +
			        "\"Bleaching Agent (Not for Flour)\"," +
			        "\"Bulking Agent\"," +
			        "\"Carrier/Solvent\"," +
			        "\"Colorant\"," +
			        "\"Emulsifier\"," +
			        "\"Enzyme/Catalyst\"," +
			        "\"Flavor\"," +
			        "\"Foam Control Agent\"," +
			        "\"Gases\"," +
			        "\"Gelling, Thickening, Stabilizing and Firming Agents\"," +
			        "\"Chewing Gum Base\"," +
			        "\"Humectant\"," +
			        "\"Leavening/Raising Agent\"," +
			        "\"Release Agent\"," +
			        "\"Surface Finishing/Glazing Agent\"," +
			        "\"Preservative\"," +
			        "\"Processing Aid\"," +
			        "\"Sequestrant/Chelating Agent\"," +
			        "\"Sweetener\"," +
			        "\"Nutrient Supplement\"," +
			        "\"Food\"," +
			        "\"Flavor Enhancer\"," +
			        "\"Fat Replacer\"," +
			        "\"Carry-Over\"," +
			        "\"Microorganisms\"" +
			    "]" +
			"}"));
			mockWebAnalysis.enqueue(new MockResponse().setBody("{" +
				    "\"recipeAnalaysisReport\": {" +
			        "\"reportDateTime\": \"2023-07-13T08:44:34.206361Z\"," +
			        "\"recipeName\": \"PF10000122391689237843340 PF test Decernis 2\"," +
			        "\"recipeSpec\": \"PF10000122391689237843340\"," +
			        "\"recipeReport\": [{" +
			            "\"country\": \"Germany\"," +
			            "\"resultIndicator\": \"REVIEW\"," +
			            "\"matrixReport\": [{" +
			                "\"did\": \"6327\"," +
			                "\"resultIndicator\": \"Not Listed\"," +
			                "\"name\": \"ing1 french\"," +
			                "\"spec\": \"ing1 french\"," +
			                "\"idType\": \"Decernis ID\"," +
			                "\"idValue\": \"6327\"," +
			                "\"decernisName\": \"Vitamin A [Retinol]\"" +
			            "}]," +
			            "\"tabularReport\": [{" +
			                "\"name\": \"ing1 french\"," +
			                "\"spec\": \"ing1 french\"," +
			                "\"did\": \"6327\"," +
			                "\"resultIndicator\": \"Not Listed\"," +
			                "\"percentage\": \"1.0\"," +
			                "\"usage\": \"Thickening agents\"," +
			                "\"threshold\": \"SME LOGIC: This nutrient supplement is not listed in this country's regulation, under review by Decernis. Please use the \\\"report a problem\\\" link for further information/questions.\"," +
			                "\"citation\": \"\"," +
			                "\"idType\": \"Decernis ID\"," +
			                "\"idValue\": \"6327\"," +
			                "\"function\": \"Nutrient Supplement\"," +
			                "\"otherIdentifiers\": {" +
			                    "\"CAS\": \"11103-57-4, 1341-18-0, 5979-23-7, 68-26-8\"," +
			                    "\"INCI name\": \"Retinol\"," +
			                    "\"E No.\": \"E672\"," +
			                    "\"EC No.\": \"200-683-7, 234-328-2\"" +
			                "}," +
			                "\"decernisName\": \"Vitamin A [Retinol]\"" +
			            "}]," +
			            "\"detailReport\": []" +
			        "}]" +
			    "}" +
			"}"));
			mockWebAnalysis.enqueue(new MockResponse().setBody("{\"countries\": [ {\"country_id\": 78, \"country\": \"Germany\"}]}"));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testV5Analysis");
			
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(new IngListDataItem(null, 1d, null, null, null, null, null, ing1, null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage1NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country1NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				List<RequirementListDataItem> requirements = decernisService.extractRequirements(product);
				
				assertEquals(1, requirements.size());
				
				assertEquals(RequirementType.Tolerated, requirements.get(0).getReqType());
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				assertEquals(ing1, requirements.get(0).getCharact());
				
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
	public void testDefaultDoubleFunction() throws Exception {
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockServerUrl);
			mockWebServer.enqueue(new MockResponse().setBody("{" +
				    "\"id\": 118509," +
				    "\"spec\": \"PF10000122941689606901758\"," +
				    "\"name\": \"PF10000122941689606901758 PF Decernis testDefaultDoubleFunction\"," +
				    "\"usage\": null," +
				    "\"usage_verbose\": null," +
				    "\"company\": 140," +
				    "\"ingredients\": [{" +
				        "\"id\": 1513931," +
				        "\"name\": \"ing3 french\"," +
				        "\"ingredient_did\": 4476," +
				        "\"percentage\": \"2.000000000000000000000000\"," +
				        "\"function\": 1002," +
				        "\"function_verbose\": \"Antioxidant\"," +
				        "\"recipe\": 118509," +
				        "\"customer_code\": null," +
				        "\"spec_parameters\": null," +
				        "\"upper_limit\": null," +
				        "\"orig_subs_ident\": \"CAS\"," +
				        "\"orig_ident_value\": \"139-33-3\"," +
				        "\"dye_content_percent\": null," +
				        "\"aluminum_percent\": null," +
				        "\"created\": \"2023-07-17T15:15:06.433206Z\"," +
				        "\"modified\": \"2023-07-17T15:15:06.433240Z\"," +
				        "\"function_does_not_exists\": false" +
				    "}," +
				    "{" +
				        "\"id\": 1513932," +
				        "\"name\": \"ing4 french\"," +
				        "\"ingredient_did\": 4476," +
				        "\"percentage\": \"2.000000000000000000000000\"," +
				        "\"function\": 1019," +
				        "\"function_verbose\": \"Preservative\"," +
				        "\"recipe\": 118509," +
				        "\"customer_code\": null," +
				        "\"spec_parameters\": null," +
				        "\"upper_limit\": null," +
				        "\"orig_subs_ident\": \"CAS\"," +
				        "\"orig_ident_value\": \"139-33-3\"," +
				        "\"dye_content_percent\": null," +
				        "\"aluminum_percent\": null," +
				        "\"created\": \"2023-07-17T15:15:06.439222Z\"," +
				        "\"modified\": \"2023-07-17T15:15:06.439248Z\"," +
				        "\"function_does_not_exists\": false" +
				    "}]," +
				    "\"category\": null," +
				    "\"category_verbose\": null," +
				    "\"can_be_analyzed_add\": false," +
				    "\"tag\": null," +
				    "\"prod_parameters\": null," +
				    "\"upper_limit\": null," +
				    "\"author\": \"beCPG Demo\"," +
				    "\"certificates_data\": {}," +
				    "\"module_id\": null," +
				    "\"created\": \"2023-07-17T15:15:05.752553Z\"," +
				    "\"modified\": \"2023-07-17T15:15:06.444789Z\"," +
				    "\"can_be_analyzed_soi\": true," +
				    "\"usage_does_not_exists\": false," +
				    "\"category_does_not_exists\": false" +
				"}"));
			mockWebServer.enqueue(new MockResponse().setBody("{\"count\":1,\"next\":null,\"previous\":null,\"results\":[{\"country\":\"France\",\"region\":\"European Union Members\"}]}"));
			mockWebServer.enqueue(new MockResponse().setBody("{" +
				    "\"analysis_results\": {" +
			        "\"France\": {" +
			            "\"matrix\": {" +
			                "\"4476\": {" +
			                    "\"name\": \"ing4 french\"," +
			                    "\"did\": \"4476\"," +
			                    "\"result_indicators\": [" +
			                        "{" +
			                            "\"result_indicator\": \"Prohibited\"," +
			                            "\"amount\": 2," +
			                            "\"is_bypass\": false" +
			                        "}" +
			                    "]," +
			                    "\"result_indicator_color\": \"#FF6A6A\"" +
			                "}" +
			            "}," +
			            "\"tabular\": {" +
			                "\"INGREDIENT_DATA_PDF\": [" +
			                    "{" +
			                        "\"ingredient\": \"France|ing3 french\"," +
			                        "\"did\": \"4476\"," +
			                        "\"function\": \"Antioxidant\"," +
			                        "\"functionCode\": \"Margarine and fat spreads\"," +
			                        "\"resultIndicator\": \"Prohibited\"," +
			                        "\"percentage\": \"2.0\"," +
			                        "\"threshold\": \"<=100 mg/l or mg/kg\"," +
			                        "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
			                        "\"citationLink\": \"doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
			                        "\"function_help_text\": null," +
			                        "\"usage_help_text\": null," +
			                        "\"usage\": 12079.0," +
			                        "\"usage_name\": \"Margarine and fat spreads\"," +
			                        "\"function_name\": \"Antioxidant\"," +
			                        "\"country\": \"France\"," +
			                        "\"result_indicator_color\": \"#FF6A6A\"," +
			                        "\"verbose_description\": \"Not allowable per regulation or banned substance list.\"" +
			                    "}," +
			                    "{" +
			                        "\"ingredient\": \"France|ing4 french\"," +
			                        "\"did\": \"4476\"," +
			                        "\"function\": \"Preservative\"," +
			                        "\"functionCode\": \"Margarine and fat spreads\"," +
			                        "\"resultIndicator\": \"Prohibited\"," +
			                        "\"percentage\": \"2.0\"," +
			                        "\"threshold\": \"<=5.0%\"," +
			                        "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
			                        "\"citationLink\": \"doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
			                        "\"function_help_text\": null," +
			                        "\"usage_help_text\": null," +
			                        "\"usage\": 12079.0," +
			                        "\"usage_name\": \"Margarine and fat spreads\"," +
			                        "\"function_name\": \"Preservative\"," +
			                        "\"country\": \"France\"," +
			                        "\"result_indicator_color\": \"#FF6A6A\"," +
			                        "\"verbose_description\": \"Not allowable per regulation or banned substance list.\"" +
			                    "}" +
			                "]," +
			                "\"ADDSUM_DATA\": []," +
			                "\"SCOPE DETAIL\": [" +
			                    "{" +
			                        "\"country\": \"France - European Union\"," +
			                        "\"ingredient\": \"ing3 french\"," +
			                        "\"function\": \"Antioxidant - Food Additive\"," +
			                        "\"usage\": \"Margarine and fat spreads - 02.2.2 Other fat and oil emulsions including spreads as defined by Regulation (EC) No 1234/2007 and liquid emulsions: only spreadable fats as defined in Article 115 of and Annex XV to Regulation (EC) No 1234/2007, having a fat content of 41 % or less\"," +
			                        "\"resultIndicator\": \"Prohibited\"," +
			                        "\"threshold\": \"<=100 mg/l or mg/kg\"," +
			                        "\"ingredientPercent\": \"2.0\"," +
			                        "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
			                        "\"comments\": \"\"," +
			                        "\"expressedAs\": \"\"," +
			                        "\"citationLink\": \"doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
			                        "\"result_indicator_color\": \"#FF6A6A\"," +
			                        "\"verbose_description\": \"Not allowable per regulation or banned substance list.\"" +
			                    "}," +
			                    "{" +
			                        "\"country\": \"France - European Union\"," +
			                        "\"ingredient\": \"ing4 french\"," +
			                        "\"function\": \"Preservative - Food Additive\"," +
			                        "\"usage\": \"Margarine and fat spreads - 02.2.2 Other fat and oil emulsions including spreads as defined by Regulation (EC) No 1234/2007 and liquid emulsions: only spreadable fats as defined in Article 115 of and Annex XV to Regulation (EC) No 1234/2007, having a fat content of 41 % or less\"," +
			                        "\"resultIndicator\": \"Prohibited\"," +
			                        "\"threshold\": \"<=5.0%\"," +
			                        "\"ingredientPercent\": \"2.0\"," +
			                        "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
			                        "\"comments\": \"\"," +
			                        "\"expressedAs\": \"\"," +
			                        "\"citationLink\": \"doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
			                        "\"result_indicator_color\": \"#FF6A6A\"," +
			                        "\"verbose_description\": \"Not allowable per regulation or banned substance list.\"" +
			                    "}" +
			                "]" +
			            "}" +
			        "}" +
			    "}," +
			    "\"result_indicator\": \"PROHIBITED\"" +
			"}"));
			mockWebServer.enqueue(new MockResponse().setBody("{" +
				    "\"count\": 1," +
				    "\"next\": null," +
				    "\"prev\": null," +
				    "\"results\": {" +
				        "\"Fats and Oils\": [" +
				            "{" +
				                "\"id\": 1049," +
				                "\"scope_id\": 12079," +
				                "\"category\": \"Fats and Oils\"," +
				                "\"phrase\": \"Margarine and fat spreads\"," +
				                "\"synonyms\": []," +
				                "\"help_text\": null" +
				            "}" +
				        "]" +
				    "}" +
				"}"));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testDefaultDoubleFunction");
			
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(new IngListDataItem(null, 2d, null, null, null, null, null, ing3, null));
				ingList.add(new IngListDataItem(null, 2d, null, null, null, null, null, ing4, null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage2NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country3NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				List<RequirementListDataItem> requirements = decernisService.extractRequirements(product);
				
				assertEquals(2, requirements.size());
				
				assertEquals(RequirementType.Forbidden, requirements.get(0).getReqType());
				assertEquals(0.01 / 2 * 100, requirements.get(0).getReqMaxQty());
				assertEquals(RequirementType.Forbidden, requirements.get(1).getReqType());
				assertEquals(5.0 / 2 * 100, requirements.get(1).getReqMaxQty());
				
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				assertEquals(RequirementDataType.Specification, requirements.get(1).getReqDataType());
				
				int checks = 0;
				
				for (RequirementListDataItem req : requirements) {
					if (ing3.equals(req.getCharact())) {
						checks++;
					} else if (ing4.equals(req.getCharact())) {
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
				return null;
			});
		}
		
	}
	
	@Test
	public void testV5DoubleFunction() throws Exception {
		
		inWriteTx(() -> {
			systemConfigurationService.updateConfValue("beCPG.decernis.serverUrl", mockServerUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.analysisUrl", mockAnalysisUrl);
			systemConfigurationService.updateConfValue("beCPG.decernis.ingredient.analysis.enabled", "false");
			mockWebAnalysis.enqueue(new MockResponse().setBody("{" +
				    "\"functions\": [" +
			        "\"Acidity Regulator/Buffer/Alkalizing Agents\"," +
			        "\"Anticaking Agent\"," +
			        "\"Antioxidant\"," +
			        "\"Flour Treatment Agent\"," +
			        "\"Bleaching Agent (Not for Flour)\"," +
			        "\"Bulking Agent\"," +
			        "\"Carrier/Solvent\"," +
			        "\"Colorant\"," +
			        "\"Emulsifier\"," +
			        "\"Enzyme/Catalyst\"," +
			        "\"Flavor\"," +
			        "\"Foam Control Agent\"," +
			        "\"Gases\"," +
			        "\"Gelling, Thickening, Stabilizing and Firming Agents\"," +
			        "\"Chewing Gum Base\"," +
			        "\"Humectant\"," +
			        "\"Leavening/Raising Agent\"," +
			        "\"Release Agent\"," +
			        "\"Surface Finishing/Glazing Agent\"," +
			        "\"Preservative\"," +
			        "\"Processing Aid\"," +
			        "\"Sequestrant/Chelating Agent\"," +
			        "\"Sweetener\"," +
			        "\"Nutrient Supplement\"," +
			        "\"Food\"," +
			        "\"Flavor Enhancer\"," +
			        "\"Fat Replacer\"," +
			        "\"Carry-Over\"," +
			        "\"Microorganisms\"" +
			    "]" +
			"}"));
			mockWebAnalysis.enqueue(new MockResponse().setBody("{\"recipeAnalaysisReport\": {" +
					  "\"reportDateTime\": \"2023-07-17T15:41:49.431966Z\"," +
					  "\"recipeName\": \"PF10000122981689608493712 PF Decernis testDefaultDoubleFunction\"," +
					  "\"recipeSpec\": \"PF10000122981689608493712\"," +
					  "\"recipeReport\": [" +
					    "{" +
					      "\"country\": \"France\"," +
					      "\"resultIndicator\": \"PROHIBITED\"," +
					      "\"matrixReport\": [" +
					        "{" +
					          "\"did\": \"4476\"," +
					          "\"resultIndicator\": \"Prohibited\"," +
					          "\"name\": \"ing4 french\"," +
					          "\"spec\": \"ing4 french\"," +
					          "\"idType\": \"Decernis ID\"," +
					          "\"idValue\": \"4476\"," +
					          "\"decernisName\": \"Calcium disodium EDTA\"" +
					        "}" +
					      "]," +
					      "\"tabularReport\": [" +
					        "{" +
					          "\"name\": \"ing3 french\"," +
					          "\"spec\": \"ing4 french\"," +
					          "\"did\": \"4476\"," +
					          "\"resultIndicator\": \"Prohibited\"," +
					          "\"percentage\": \"2.0\"," +
					          "\"usage\": \"Margarine and fat spreads\"," +
					          "\"threshold\": \"<=100 mg/l or mg/kg\"," +
					          "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
					          "\"idType\": \"Decernis ID\"," +
					          "\"idValue\": \"4476\"," +
					          "\"function\": \"Antioxidant\"," +
					          "\"hyperlink\": \"https://www.decernis.com/reference/navpdf.jsp?doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
					          "\"otherIdentifiers\": {" +
					            "\"CAS\": \"12002-29-8, 1282-71-9, 19067-42-6, 304695-78-1, 39208-14-5, 5297-15-4, 56532-88-8, 61864-74-2, 62-33-9, 662-33-9, 6766-87-6, 69843-95-4, 7732-93-6\"," +
					            "\"INCI name\": \"Calcium Disodium EDTA\"," +
					            "\"E No.\": \"E385\"," +
					            "\"INS No.\": \"385\"," +
					            "\"EC No.\": \"200-529-9\"" +
					          "}," +
					          "\"decernisName\": \"Calcium disodium EDTA\"" +
					        "}," +
					        "{" +
					          "\"name\": \"ing4 french\"," +
					          "\"spec\": \"ing4 french\"," +
					          "\"did\": \"4476\"," +
					          "\"resultIndicator\": \"Prohibited\"," +
					          "\"percentage\": \"2.0\"," +
					          "\"usage\": \"Margarine and fat spreads\"," +
					          "\"threshold\": \"<=5.0%\"," +
					          "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
					          "\"idType\": \"Decernis ID\"," +
					          "\"idValue\": \"4476\"," +
					          "\"function\": \"Preservative\"," +
					          "\"hyperlink\": \"https://www.decernis.com/reference/navpdf.jsp?doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
					          "\"otherIdentifiers\": {" +
					            "\"CAS\": \"12002-29-8, 1282-71-9, 19067-42-6, 304695-78-1, 39208-14-5, 5297-15-4, 56532-88-8, 61864-74-2, 62-33-9, 662-33-9, 6766-87-6, 69843-95-4, 7732-93-6\"," +
					            "\"INCI name\": \"Calcium Disodium EDTA\"," +
					            "\"E No.\": \"E385\"," +
					            "\"INS No.\": \"385\"," +
					            "\"EC No.\": \"200-529-9\"" +
					          "}," +
					          "\"decernisName\": \"Calcium disodium EDTA\"" +
					        "}" +
					      "]," +
					      "\"detailReport\": [" +
					        "{" +
					          "\"country\": \"France - European Union\"," +
					          "\"function\": \"Antioxidant - Food Additive\"," +
					          "\"usage\": \"Margarine and fat spreads - 02.2.2 Other fat and oil emulsions including spreads as defined by Regulation (EC) No 1234/2007 and liquid emulsions: only spreadable fats as defined in Article 115 of and Annex XV to Regulation (EC) No 1234/2007, having a fat content of 41 % or less\"," +
					          "\"resultIndicator\": \"Prohibited\"," +
					          "\"threshold\": \"<=100 mg/l or mg/kg\"," +
					          "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
					          "\"comments\": \"\"," +
					          "\"expressedAs\": \"\"," +
					          "\"citationLink\": \"https://www.decernis.com/reference/navpdf.jsp?doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
					          "\"result_indicator_color\": \"#FF6A6A\"," +
					          "\"verbose_description\": \"Not allowable per regulation or banned substance list.\"," +
					          "\"name\": \"ing3 french\"," +
					          "\"spec\": \"ing3 french\"," +
					          "\"percentage\": \"2.0\"" +
					        "}," +
					        "{" +
					          "\"country\": \"France - European Union\"," +
					          "\"function\": \"Preservative - Food Additive\"," +
					          "\"usage\": \"Margarine and fat spreads - 02.2.2 Other fat and oil emulsions including spreads as defined by Regulation (EC) No 1234/2007 and liquid emulsions: only spreadable fats as defined in Article 115 of and Annex XV to Regulation (EC) No 1234/2007, having a fat content of 41 % or less\"," +
					          "\"resultIndicator\": \"Prohibited\"," +
					          "\"threshold\": \"<=5.0%\"," +
					          "\"citation\": \"Regulation (EC) No 1333/2008: Food Additives (Consolidated 2022-10-31)(English) - FoodAdditives\"," +
					          "\"comments\": \"\"," +
					          "\"expressedAs\": \"\"," +
					          "\"citationLink\": \"https://www.decernis.com/reference/navpdf.jsp?doc=e21ad59e-4080-4c59-841d-8758921400b4&pg=80\"," +
					          "\"result_indicator_color\": \"#FF6A6A\"," +
					          "\"verbose_description\": \"Not allowable per regulation or banned substance list.\"," +
					          "\"name\": \"ing4 french\"," +
					          "\"spec\": \"ing4 french\"," +
					          "\"percentage\": \"2.0\"" +
					        "}" +
					      "]" +
					    "}" +
					  "]" +
					"}}"
));
			mockWebAnalysis.enqueue(new MockResponse().setBody("{\"countries\": [ {\"country_id\": 73, \"country\": \"France\"}]}"));
			mockWebServer.enqueue(new MockResponse().setBody(""));
			return null;
		});
		
		try {
			NodeRef finishedProductNodeRef = createFinishedProduct("PF Decernis testDefaultDoubleFunction");
			
			inWriteTx(() -> {
				ProductData product = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
				
				List<IngListDataItem> ingList = product.getIngList();
				
				ingList.add(new IngListDataItem(null, 2d, null, null, null, null, null, ing3, null));
				ingList.add(new IngListDataItem(null, 2d, null, null, null, null, null, ing4, null));
				
				List<RegulatoryListDataItem> regulatoryList = product.getRegulatoryList();
				
				RegulatoryListDataItem item1 = new RegulatoryListDataItem();
				item1.setRegulatoryUsagesRef(new ArrayList<>(List.of(usage2NodeRef)));
				item1.setRegulatoryCountriesRef(new ArrayList<>(List.of(country3NodeRef)));
				item1.setRegulatoryState(SystemState.Simulation);
				
				regulatoryList.add(item1);
				
				List<RequirementListDataItem> requirements = decernisService.extractRequirements(product);
				
				assertEquals(2, requirements.size());
				
				assertEquals(RequirementType.Forbidden, requirements.get(0).getReqType());
				assertEquals(0.01 / 2 * 100, requirements.get(0).getReqMaxQty());
				assertEquals(RequirementType.Forbidden, requirements.get(1).getReqType());
				assertEquals(5.0 / 2 * 100, requirements.get(1).getReqMaxQty());
				
				assertEquals(RequirementDataType.Specification, requirements.get(0).getReqDataType());
				assertEquals(RequirementDataType.Specification, requirements.get(1).getReqDataType());
				
				int checks = 0;
				
				for (RequirementListDataItem req : requirements) {
					if (ing3.equals(req.getCharact())) {
						checks++;
					} else if (ing4.equals(req.getCharact())) {
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
