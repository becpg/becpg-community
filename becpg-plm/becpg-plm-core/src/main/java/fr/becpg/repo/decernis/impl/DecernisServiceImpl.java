package fr.becpg.repo.decernis.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

public class DecernisServiceImpl  implements DecernisService {

	private static Log logger = LogFactory.getLog(DecernisServiceImpl.class);

	private NodeService nodeService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private Map<String,NodeRef> ings = new HashMap<String, NodeRef>();

	private String serverUrl;

	private String companyName;

	private String token;

	private String module;

	private RestTemplate restTemplate = new RestTemplate();

	//		1, Food Additives
	//		2, Standards Of Identity
	//		3, Contaminants
	//		5, Food Contact
	//		11; Product Check

	private static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	private static final String MESSAGE_NO_RID_ING = "message.decernis.ingredient.noRid";
	private static final String MESSAGE_SEVERAL_RID_ING = "message.decernis.ingredient.severalRid";


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setModule(String module) {
		this.module = module;
	}

	HttpEntity<String> createEntity(String param) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<String>(param, headers);

	}

	private JSONObject getAvaillableCountries() {
		String url = serverUrl + "countries/for_module?current_company={company}&module_id={module}";
		Map<String, String> params = new HashMap<String, String>();
		params.put("company", companyName);
		params.put("module", module);
		try {
			JSONObject jsonObject = new JSONObject(restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params).getBody());
			if(jsonObject.has("count") && jsonObject.getInt("count") > 0 && jsonObject.has("results")) {
				return jsonObject;
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
		return null;
	}

	private boolean isAvaillableCountry(String country) {
		JSONObject availableCountries = getAvaillableCountries();
		if (availableCountries != null) {
			try {
				JSONArray results = availableCountries.getJSONArray("results");
				for (int row = 0; row < results.length(); row++) {
					if (country.equals(results.getJSONObject(row).getString("country"))){
						return true;
					}
				}
			}catch (Exception e) {
				logger.error(e, e);
			}
		}
		return false;
	}

	private List<String> getCountries(List<String> countries) {

		List<String> ret = new ArrayList<String>();
		for (String country : countries) {
			String countryName = nodeService.getProperty(new NodeRef(country), BeCPGModel.PROP_CHARACT_NAME).toString();
			if (countryName != null && !countryName.equals("") && isAvaillableCountry(countryName)) {
				ret.add(countryName);
			}
		}
		return ret;
	}


	private List<String> getUsages(ProductData product) {
		List<String> usages = new ArrayList<String>();
		List<AssociationRef> usageAssocs = nodeService.getTargetAssocs(product.getNodeRef(), PLMModel.ASSOC_REGULATORY_USAGE);
		if (usageAssocs != null) {
			for (AssociationRef usageAssoc : usageAssocs) {
				if (nodeService.getTargetAssocs(usageAssoc.getTargetRef(), BeCPGModel.PROP_CHARACT_NAME) != null) {
					String usage = nodeService.getProperty(usageAssoc.getTargetRef(), BeCPGModel.PROP_CHARACT_NAME).toString();
					usages.add(usage);
				}
			}
		}
		return usages;
	}

	private String getFunction(String function) {
		String url = serverUrl + "/functions/structurized?current_company={company}&page={page}&phrase={phrase}&module_id={module}";

		Map<String, String> params = new HashMap<String, String>();
		params.put("company", companyName);
		params.put("page", "1");
		params.put("phrase", function);
		params.put("module", module);

		try {
			JSONObject jsonObject = new JSONObject(restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params).getBody());
			if(jsonObject.has("count") && jsonObject.getInt("count") == 1 && jsonObject.has("results")) {
				JSONObject results = jsonObject.getJSONObject("results");
				if (results.has("Function")) {
					JSONArray functions = results.getJSONArray("Function");
					if (functions.length() == 1 && functions.getJSONObject(0).has("scope_id")) {
						return functions.getJSONObject(0).getString("scope_id");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e, e);
		}

		return null;
	}

	private JSONObject getIngredients(ProductData product) {

		JSONObject ret = new JSONObject();

		try {
			ret.put("spec", nodeService.getProperty(product.getNodeRef(), BeCPGModel.PROP_CODE));
			ret.put("name", product.getName());
			ret.put("company", companyName);

			JSONArray ingredients = new JSONArray();

			for (IngListDataItem ingListDataItem : product.getIngList()) {
				if (ingListDataItem.getIng() != null) {
					Serializable casCode = nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_CASCODE);
					Serializable ingName = nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME);
					Serializable rid = nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_RID);
					Serializable ingType =  nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_TYPE_V2);
					Serializable ingQtyPerc = nodeService.getProperty(ingListDataItem.getNodeRef(), PLMModel.PROP_INGLIST_QTY_PERC);
					Serializable function  = null;
					if (ingType != null) {
						function  = nodeService.getProperty((NodeRef)ingType, PLMModel.PROP_ING_FUNCTION_RID);
					}
					if (rid == null || rid.equals("")) {
						String url = serverUrl + "ingredients?current_company={company}&q={query}&identifier_type={type}&module_id={module}&limit=1";
						Map<String, String> params = new HashMap<String, String>();
						params.put("company", companyName);
						params.put("module", module);
						if (casCode != null && !casCode.equals("")) {
							params.put("query", casCode.toString());
							params.put("type", "CAS");
						} else if (ingName != null && !ingName.equals("")){
							params.put("query", ingName.toString());
							params.put("type", "Name");
						}
						try {
							if (params.containsKey("query")) {
								JSONObject jsonObject = new JSONObject(restTemplate.exchange(url, HttpMethod.GET, createEntity(null), String.class, params).getBody());
								if(jsonObject.has("count") && jsonObject.getInt("count") == 1 && jsonObject.has("results")) {
									JSONArray results = jsonObject.getJSONArray("results");
									nodeService.setProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_RID, results.getJSONObject(0).getString("did"));
								} else if (jsonObject.has("count") && jsonObject.getInt("count") == 0){
									createReqCtrl(product, ingListDataItem.getIng(), MESSAGE_NO_RID_ING);
								} else if (jsonObject.has("count") && jsonObject.getInt("count") > 1) {
									createReqCtrl(product, ingListDataItem.getIng(), MESSAGE_SEVERAL_RID_ING);
								}
							}
						} catch (Exception e) {
							logger.error(e, e);
						}
					} 
					rid = nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_ING_RID);
					if (rid != null && !rid.equals("") && (function != null && !function.equals(""))
							&& (ingName != null && !ingName.equals("")) && (ingQtyPerc != null && !ingQtyPerc.equals(""))) {
						ings.put(rid.toString(), ingListDataItem.getIng());
						JSONObject ingredient = new JSONObject();
						ingredient.put("name", ingName);
						ingredient.put("percentage", ingQtyPerc);
						ingredient.put("ingredient_did", rid);
						ingredient.put("function", function);
						ingredient.put("spec_parameters", JSONObject.NULL);
						ingredient.put("upper_limit", JSONObject.NULL);
						ingredients.put(ingredient);
					}
				}
			}
			if (ingredients.length()>0) {
				ret.put("ingredients", ingredients);
			} else {
				return null;
			}
		}catch (JSONException e) {
			logger.error(e, e);
		}
		return ret;
	}

	private String sendRecipe(JSONObject data) {
		String url = serverUrl + "formulas";
		if (data != null) {
			try {
				HttpEntity<String> request = createEntity(data.toString());
				JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url, request, String.class));
				if (jsonObject.has("id")) {
					return jsonObject.getString("id");
				} 
			} catch (HttpClientErrorException e) {
				logger.error("Error sending recipe: "+e.getResponseBodyAsString());
			} catch (Exception e) {
				logger.error(e,e);
			} 
		}
		return null;
	}

	private void deleteRecipe(String recipeId, String usage) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("company", companyName);
		params.put("formula", recipeId);
		params.put("usage", usage);
		params.put("module", module);

		String deleteUrl = serverUrl + "formulas/"+recipeId+"?current_company={company}";
		try {
			restTemplate.exchange(deleteUrl, HttpMethod.DELETE, createEntity(null), String.class, params);
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	/**
	 * {"search_parameters": {
      "usage": "Breakfast foods",
      "country": [
            "United States",
            "European Union"
      ],
      "recipe_name": "Berry Fruit Filling"
      },
      "analysis_results": {
      "European Union": {
            u"result_indicator": "PERMITTED",
            u"xml": "RAW_XML_RESPONSE",
            u"matrix": {
                  '1034': {'did': '1034',
                        'name': 'Color Yellow 5',
                        'result_indicator': 'REVIEW - NOT LISTED'},
            },
            u"tabular": {
                  'SCOPE DETAIL': [{
                        'country': 'United States - United States',
                        'ingredient': 'Carrageenan',
                        'function': 'Gelling, Thickening Agents',
                        'usage': 'Breakfast foods - Foods',
                        'resultIndicator': 'PERMITTED',
                        'threshold': '--  No Threshold',
                        'ingredientPercent': '1.9',
                        'citation': '21 CFR 172.620 Carrageenan',
                        'comments': 'labeling restriction',
                        'expressedAs': '--',
                        'citationLink': 'doc=21cfr172.620.pdf&pg=1'
                  }]
            },
      },
	}}
	 */

	private JSONObject recipeAnalysis(String recipeId, List<String> countries, String usage) {
		String countryParam = "";
		for (String country : countries) {
			countryParam += "&country=" + country;
		}

		String url = serverUrl + "recipe_analysis?current_company={company}&formula={formula}"+countryParam+"&usage={usage}&category=null&module_id={module}&limit=1";

		Map<String, String> params = new HashMap<String, String>();
		params.put("company", companyName);
		params.put("formula", recipeId);
		params.put("usage", usage);
		params.put("module", module);

		try {
			HttpEntity<String> entity =  createEntity(null);
			JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url,entity, String.class, params));
			if (jsonObject.has("analysis_results") && jsonObject.getJSONObject("analysis_results").length() > 0) {
				return jsonObject;
			}
		}catch (HttpClientErrorException e) {
			logger.error("Error analysing recipe: "+e.getResponseBodyAsString());
		} catch (Exception e) {
			logger.error(e, e);
		}
		return null;
	}

	private String createReqCtrl(ProductData product, List<String> countries, JSONObject analysisResults) {
		List<ReqCtrlListDataItem> reqCtrlList = new ArrayList<ReqCtrlListDataItem>();
		String ret = "";
		for (String country : countries) {
			int count = 0; 
			try {
				if (analysisResults.getJSONObject("analysis_results").has(country)){
					JSONObject countryResults = analysisResults.getJSONObject("analysis_results").getJSONObject(country);
					if (countryResults.has("tabular") && countryResults.getJSONObject("tabular").has("INGREDIENT_DATA_PDF")) {
						JSONArray tabularResults = countryResults.getJSONObject("tabular").getJSONArray("INGREDIENT_DATA_PDF");
						for (int row = 0; row < tabularResults.length(); row++) {
							JSONObject result = tabularResults.getJSONObject(row);
							if (result.has("did") && result.has("resultIndicator") && result.getString("resultIndicator").startsWith("PROHIBITED")){
								if (ings.containsKey(result.getString("did")) && ings.get(result.getString("did")) != null){
									ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
									reqCtrlItem.setReqType(RequirementType.Forbidden);
									reqCtrlItem.setReqDataType(RequirementDataType.Specification);
									reqCtrlItem.getSources().add(ings.get(result.getString("did")));
									String threshold = (result.has("threshold") && !result.getString("threshold").equals("None")?"("+result.getString("threshold")+")":"");
									String usage = (analysisResults.has("search_parameters") && analysisResults.getJSONObject("search_parameters").has("usage")?analysisResults.getJSONObject("search_parameters").getString("usage")+" - ":"");
									MLText reqMessage = MLTextHelper.getI18NMessage(MESSAGE_PROHIBITED_ING, country, usage, threshold);
									reqCtrlItem.setReqMlMessage(reqMessage);
									reqCtrlList.add(reqCtrlItem);
									count++;
								}	
							}
						}
					}
				}
			} catch (JSONException e) {
				logger.error(e, e);
			}
			ret += country+":"+count+"\n";
		}
		if (product.getReqCtrlList() == null) {
			product.setReqCtrlList(new LinkedList<>());
		}
		if (reqCtrlList.size() > 0) {
			product.getReqCtrlList().addAll(reqCtrlList);
			alfrescoRepository.save(product);
		}
		
		return ret;
	}


	private void createReqCtrl(ProductData product, NodeRef ing, String reqCtrlMessage) {
		MLText reqMessage = MLTextHelper.getI18NMessage(reqCtrlMessage);
		ReqCtrlListDataItem reqCtrlItem = new ReqCtrlListDataItem();
		reqCtrlItem.setReqType(RequirementType.Info);
		reqCtrlItem.getSources().add(ing);
		reqCtrlItem.setReqDataType(RequirementDataType.Specification);
		reqCtrlItem.setReqMlMessage(reqMessage);
		if (product.getReqCtrlList() == null) {
			product.setReqCtrlList(new LinkedList<>());
		}
		product.getReqCtrlList().add(reqCtrlItem);
		alfrescoRepository.save(product);	
	}

	public String launchDecernisAnalysis(ProductData product, List<String> countriesNoderef){
		String ret = "";
		if (module == null || module.equals("")) {
			return "No module found";
		} else {
			List<String> usages = getUsages(product);
			if (usages.size() > 0) {
				List<String> countries = getCountries(countriesNoderef);
				JSONObject data =  getIngredients(product);
				if (data != null && countries.size()>0) {
					for (String usage : usages) {
						String recipeId = sendRecipe(data);
						if (recipeId != null) {
							JSONObject analysisResults = recipeAnalysis(recipeId, countries, usage);
							deleteRecipe(recipeId, usage);
							if (analysisResults != null) {
								ret = createReqCtrl(product, countries, analysisResults);
							} else {
								return "Error analysing recipe";
							}							
						} else {
							return "Error sending recipe";
						}
					}
				} else if (countries.size()<=0) {
					return "No available country";
				} else if (data == null){
					return "No ingredients found";
				}
				
			} else{
				return "No usage found";
			}
		}
		return ret;
	}


	public String startQueryJob() {
		String url = String.format(serverUrl +"landscape?current_company=%s", companyName);

		//	--data-binary '{"module_id":1,"country":["China","European Union","United States","Saudi Arabia"],"usage":[1113],"did":[103329,3608,6112]}’


		return null;

	}

	public String getMatrixData(String id) {
		String url = String.format(serverUrl +"landscape/%s/data_matrix?current_company=%s",id, companyName);

		return null;

	}

	public String getTabularData(String id) {
		String url = String.format(serverUrl +"landscape/%s/data_tabular?current_company=%s",id, companyName);

		return null;

	}

}
