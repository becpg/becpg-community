package fr.becpg.repo.glop.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.becpg.repo.glop.GlopConstraintSpecification;
import fr.becpg.repo.glop.GlopException;
import fr.becpg.repo.glop.GlopService;
import fr.becpg.repo.glop.GlopTargetSpecification;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Implementation for Glop service using an auxiliary Glop server
 * 
 * @author pierrecolin
 * @version 1.0
 */
@Service("glopService")
public class GlopServiceImpl implements GlopService {

	private static final Log logger = LogFactory.getLog(GlopServiceImpl.class);

	@Autowired
	private AlfrescoRepository<ProductData> repositoryProduct;

	@Value("${beCPG.glop.serverUrl}")
	private String serverUrl;

	private RestTemplate restTemplate = new RestTemplate();
	
	private void initConstraintContributions(List<GlopConstraintSpecification> characts, Map<BeCPGDataObject, Map<CompoListDataItem, Double>> constraintContributions, Map<String, Map<CompoListDataItem, Double>> specialContributions) {
		for (GlopConstraintSpecification constraint: characts) {
			constraint.initContributions(constraintContributions, specialContributions);
		}
	}
	
	private static double defaultValue(Double x, double def) {
		if (x != null) {
			return x;
		} else {
			return def;
		}
	}
	
	/**
	 * Finds the optimal recipe for a product given a target to optimize and a list of constraints.
	 * 
	 * @param productData the data of the product to optimize
	 * @param characts the list of constraints the optimization is subject to
	 * @param target the target function specification
	 * @return the solution computed by the Glop server
	 * @throws GlopException if the linear program is unfeasible
	 * @throws RestClientException if an error was met while communicating with the Glop server
	 * @throws URISyntaxException if the Glop server URL specified is syntactically incorrect
	 * @throws JSONException if an error was met building one of the JSON objects involved
	 */
	@Override
	public JSONObject optimize(ProductData productData, List<GlopConstraintSpecification> characts, GlopTargetSpecification target) throws GlopException, RestClientException, URISyntaxException, JSONException {
		Set<CompoListDataItem> variables = new HashSet<>();
		Map<BeCPGDataObject, Map<CompoListDataItem, Double>> constraintContributions = new HashMap<>();
		Map<String, Map<CompoListDataItem, Double>> specialContributions = new HashMap<>();
		initConstraintContributions(characts, constraintContributions, specialContributions);
		Map<CompoListDataItem, Double> targetContributions = new HashMap<>();

		// Gather coefficients
		// TODO: find a way to factor everything but the CompoListDataItem cases
		for (CompoListDataItem compoListDataItem: productData.getCompoList()) {
			NodeRef componentRef = compoListDataItem.getProduct();
			final ProductData componentProductData = repositoryProduct.findOne(componentRef);
			
			// Calculate target
			if (target.getTarget() instanceof CostListDataItem) {
				CostListDataItem targetCostListDataItem = (CostListDataItem) target.getTarget();
				for (CostListDataItem componentCostListDataItem: componentProductData.getCostList()) {
					if (componentCostListDataItem.getCost().equals(targetCostListDataItem.getCost())) {
						final double value = componentCostListDataItem.getValue();
						addContributions(variables, targetContributions, compoListDataItem, value);
						break;
					}
				}
			} else if (target.getTarget() instanceof IngListDataItem) {
				IngListDataItem targetIngListDataItem = (IngListDataItem) target.getTarget();
				for (IngListDataItem componentIngListDataItem: componentProductData.getIngList()) {
					if (componentIngListDataItem.getIng().equals(targetIngListDataItem.getIng())) {
						final double value = componentIngListDataItem.getValue();
						addContributions(variables, targetContributions, compoListDataItem, value);
						break;
					}
				}
			} else if (target.getTarget() instanceof NutListDataItem) {
				NutListDataItem targetNutListDataItem = (NutListDataItem) target.getTarget();
				for (NutListDataItem componentNutListDataItem: componentProductData.getNutList()) {
					if (componentNutListDataItem.getNut().equals(targetNutListDataItem.getNut())) {
						final double value = componentNutListDataItem.getValue();
						addContributions(variables, targetContributions, compoListDataItem, value);
						break;
					}
				}
			} else if (target.getTarget() instanceof AllergenListDataItem) {
				AllergenListDataItem targetAllergenListDataItem = (AllergenListDataItem) target.getTarget();
				for (AllergenListDataItem componentAllergenListDataItem: componentProductData.getAllergenList()) {
					if (componentAllergenListDataItem.getAllergen().equals(targetAllergenListDataItem.getAllergen())) {
						final double value = componentAllergenListDataItem.getValue();
						addContributions(variables, targetContributions, compoListDataItem, value);
						break;
					}
				}
			} else if (target.getTarget() instanceof PackagingListDataItem) {
				PackagingListDataItem targetPackagingListDataItem = (PackagingListDataItem) target.getTarget();
				for (PackagingListDataItem componentPackagingListDataItem: componentProductData.getPackagingList()) {
					if (componentPackagingListDataItem.getProduct().equals(targetPackagingListDataItem.getProduct())) {
						final double value = componentPackagingListDataItem.getQty();
						addContributions(variables, targetContributions, compoListDataItem, value);
						break;
					}
				}
			} else if (target.getTarget() instanceof CompoListDataItem) {
				// Much simpler to compute
				CompoListDataItem targetCompoListDataItem = (CompoListDataItem) target.getTarget();
				if (compoListDataItem.getComponent().equals(targetCompoListDataItem.getComponent())) {
					addContributions(variables, targetContributions, compoListDataItem, 1d);
				}
			} else {
				throw new RuntimeException("Target unimplemented for " + target.getTarget().getClass().getName());
			}
			
			// Calculate data object constraints
			for (Map.Entry<BeCPGDataObject, Map<CompoListDataItem, Double>> entry: constraintContributions.entrySet()) {
				if (entry.getKey() instanceof CostListDataItem) {
					CostListDataItem constraintCostListDataItem = (CostListDataItem) entry.getKey();
					for (CostListDataItem componentCostListDataItem: componentProductData.getCostList()) {
						if (componentCostListDataItem.getCost().equals(constraintCostListDataItem.getCost())) {
							final double value = defaultValue(componentCostListDataItem.getValue(), 0d);
							addContributions(variables, entry.getValue(), compoListDataItem, value);
							break;
						}
					}
				} else if (entry.getKey() instanceof NutListDataItem) {
					NutListDataItem constraintNutListDataItem = (NutListDataItem) entry.getKey();
					for (NutListDataItem componentNutListDataItem: componentProductData.getNutList()) {
						if (componentNutListDataItem.getNut().equals(constraintNutListDataItem.getNut())) {
							final double value = defaultValue(componentNutListDataItem.getValue(), 0d);
							addContributions(variables, entry.getValue(), compoListDataItem, value);
							break;
						}
					}
				} else if (entry.getKey() instanceof IngListDataItem) {
					IngListDataItem constraintIngListDataItem = (IngListDataItem) entry.getKey();
					for (IngListDataItem componentIngListDataItem: componentProductData.getIngList()) {
						if (componentIngListDataItem.getIng().equals(constraintIngListDataItem.getIng())) {
							final double value = componentIngListDataItem.getValue();
							addContributions(variables, entry.getValue(), compoListDataItem, value);
							break;
						}
					}
				} else if (entry.getKey() instanceof AllergenListDataItem) {
					AllergenListDataItem constraintAllergenListDataItem = (AllergenListDataItem) entry.getKey();
					for (AllergenListDataItem componentAllergenListDataItem: componentProductData.getAllergenList()) {
						if (componentAllergenListDataItem.getAllergen().equals(constraintAllergenListDataItem.getAllergen())) {
							final double value = componentAllergenListDataItem.getValue();
							addContributions(variables, entry.getValue(), compoListDataItem, value);
							break;
						}
					}
				} else if (entry.getKey() instanceof PackagingListDataItem) {
					PackagingListDataItem constraintPackagingListDataItem = (PackagingListDataItem) entry.getKey();
					for (PackagingListDataItem componentPackagingListDataItem: componentProductData.getPackagingList()) {
						if (componentPackagingListDataItem.getProduct().equals(constraintPackagingListDataItem.getProduct())) {
							final double value = componentPackagingListDataItem.getQty();
							addContributions(variables, entry.getValue(), compoListDataItem, value);
							break;
						}
					}
				} else if (entry.getKey() instanceof CompoListDataItem) {
					// Much simpler to compute
					CompoListDataItem constraintCompoListDataItem = (CompoListDataItem) entry.getKey();
					if (compoListDataItem.getComponent().equals(constraintCompoListDataItem.getComponent())) {
						addContributions(variables, entry.getValue(), compoListDataItem, 1d);
						break;
					}
				} else {
					throw new RuntimeException("Constraints unimplemented for " + entry.getKey().getClass().getName());
				}
			}
			
			// Calculate special constraints
			for (GlopConstraintSpecification constraint: characts) {
				constraint.fillIfSpecial(specialContributions, compoListDataItem, variables);
			}
		}

		// Build JSON
		JSONObject jsonRequest = buildJsonRequest(variables, characts, constraintContributions, target, targetContributions, specialContributions);

		if (logger.isDebugEnabled()) {
			logger.debug("Sending " + jsonRequest.toString());
		}
		
		// Send request and return result
		return sendRequest(jsonRequest);
	}

	private void addContributions(Set<CompoListDataItem> variables, Map<CompoListDataItem, Double> contributions,
			CompoListDataItem compoListDataItem, double value) {
		variables.add(compoListDataItem);
		contributions.put(compoListDataItem, value);
	}

	private static JSONObject buildJsonRequest(
			Set<CompoListDataItem> variables,
			List<GlopConstraintSpecification> characts,
			Map<BeCPGDataObject, Map<CompoListDataItem, Double>> constraintContributions,
			GlopTargetSpecification target,
			Map<CompoListDataItem, Double> targetContributions,
			Map<String, Map<CompoListDataItem, Double>> specialContributions) throws JSONException {
		
		JSONObject ret = new JSONObject();
		
		JSONArray jsonVariables = new JSONArray();
		for (CompoListDataItem variable: variables) {
			jsonVariables.put(variable.getProduct().toString());
		}
		ret.put("variables", jsonVariables);
		for (GlopConstraintSpecification constraint: characts) {
			ret.append("constraints", constraint.toJson(constraintContributions, specialContributions));
		}
		JSONObject jsonTarget = new JSONObject();
		target.putIntoJson(jsonTarget);
		JSONObject jsonCoefficients = new JSONObject();
		for (Map.Entry<CompoListDataItem, Double> entry: targetContributions.entrySet()) {
			jsonCoefficients.put(entry.getKey().getProduct().toString(), entry.getValue());
		}
		jsonTarget.put("coefficients", jsonCoefficients);
		ret.put("objective", jsonTarget);
		
		return ret;
	}

	/**
	 * Sends a request to the Glop server. Mostly for testing and may be deprecated in the future.
	 * 
	 * @param request the request to be sent to the server (see the server source for syntax)
	 * @return the solution computed by the server
	 * @throws RestClientException if an error was met while communicating with the Glop server
	 * @throws URISyntaxException if the server URL specified is syntactically incorrect
	 * @throws JSONException if an error was met while building one of the JSON objects involved
	 * @throws GlopException if the linear program is unfeasible
	 */
	@Override
	public JSONObject sendRequest(JSONObject request) throws RestClientException, URISyntaxException, JSONException, GlopException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);
		
		URI uri = new URI(serverUrl);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending to " + serverUrl);
		}
		String response = restTemplate.postForObject(uri, requestEntity, String.class);
		if (response == null) {
			throw new GlopException();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Server returned " + response);
		}
		return new JSONObject(response);
	}

}
