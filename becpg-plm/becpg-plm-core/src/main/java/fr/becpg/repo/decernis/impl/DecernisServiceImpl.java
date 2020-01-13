package fr.becpg.repo.decernis.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.becpg.repo.decernis.DecernisService;

public class DecernisServiceImpl  implements DecernisService {

	@Value("beCPG.decernis.token")
	private String token;

	@Value("beCPG.decernis.serverUrl")
	private String serverUrl;

	@Value("beCPG.decernis.companyName")
	private String companyName;
	
	private enum DecernisModule {
//		1, Food Additives
//		2, Standards Of Identity
//		3, Contaminants
//		5, Food Contact
//		11; Product Check
		
	}
	

	// curl
	// 'customer_ingredients?current_company=Decernis2&q=erythritol&identifier_type=Name&module_id=1&limit=25'
	// -H "Authorization: Bearer $token" -H 'Content-Type: application/json'

	@Autowired
	RestTemplate restTemplate;

	HttpEntity<String> createEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<String>(headers);

	}
	
	public String getAvaillableCountries() {
		//countries/for_module?current_company=&module_id=5
		
		return null;
	}

//	{"count":2,"next":null,"previous":null,"results”:[
//
//		{"id":23427127,"did":103329,"libidents":{"CAS":["149-32-6"],"CNS No.":["19.018"],"E No.":["E968","IL E968","KZ E968","RU E968"],"EC No.":["205-737-3"],"ECL":["KE-13121"],"EINECS":["205-737-3"],"ENCS":["2-3651"],"FDA Cat.":["C07.6"],"FEMA No.":["4819"],"GRAS No.":["208","382","401","76","789"],"INCI name":["Erythritol"],"INS No.":["968"],"ISHL":["2-(8)-595"],"Mol. Form.":["C4H10O4"],"PAFA":["1704"],"RTECS":["KQ0448000"],"SuperDID":["10000205","10000291"]},"synonyms":["Erythritol","meso-Erythritol","Erythritol, meso-","Erythritol","ERYTHRITOL","Erythritol (1,2,3,4-Butanetetrol)"],"name":"Erythritol","name_lower":"erythritol","module_id":1,"company_id":62},
//
//		{"id":23424873,"did":1124,"libidents":{"CAS":["977031-85-8","977080-46-8"],"CFSAN":["977031-85-8","977080-46-8"],"FDA Cat.":["B07"],"NAS No.":["392"],"PAFA":["2519","7214"],"REGNUM":["175.105"]},"synonyms":["Gum rosin, pentaerythritol ester"],"name":"Pectinase from Aspergillus niger","name_lower":"pectinase from aspergillus niger","module_id":1,"company_id":62}
//
//		]}
//	
	public String getIngredients(String casId) throws RestClientException, JSONException {
		String url = serverUrl + "customer_ingredients?current_company={company}&q={query}&identifier_type=CAS&module_id={module}&limit=1";

		Map<String, String> params = new HashMap<String, String>();
	    params.put("company", companyName);
	    params.put("module", "1");
	    params.put("query", casId);
		
	    JSONObject jsonObject = new JSONObject(restTemplate.getForObject(url, String.class, params));
	    if(jsonObject.has("results")) {
	    	JSONArray results = jsonObject.getJSONArray("results");
	    	for (int i = 0; i < results.length(); i++) {
				return results.getJSONObject(i).getString("id");
			}
	    }
	   
	    
		return null;

	}
	
	public String startQueryJob() {
		String url = String.format(serverUrl +"landscape?current_company=%s", companyName);
		
		//	--data-binary '{"module_id":1,"country":["China","European Union","United States","Saudi Arabia"],"usage":[1113],"did":[103329,3608,6112]}’
			
			
			return null;
		
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
	 * @throws JSONException 
	 * @throws RestClientException 
	 */
	
	public void recipeAnalysis() throws RestClientException, JSONException {
		String url = serverUrl + "recipe_analysis?current_company={company}&q={query}&identifier_type=CAS&module_id={module}&limit=1";

		Map<String, String> params = new HashMap<String, String>();
	    params.put("company", companyName);
	    params.put("module", "1");
	    params.put("country", "tODO");
	    
	    HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", token);
		headers.setContentType(MediaType.APPLICATION_JSON);
	 
		//TODO Recipe in xml format.
		
		String formula = "<xml/>";
		
		
		HttpEntity entity =  new HttpEntity<String>(formula, headers);
		
	    JSONObject jsonObject = new JSONObject(restTemplate.postForObject(url,entity, String.class, params ));
		
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
