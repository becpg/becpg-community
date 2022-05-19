package fr.becpg.repo.glop.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.glop.GlopConstraint;
import fr.becpg.repo.glop.GlopException;
import fr.becpg.repo.glop.GlopService;
import fr.becpg.repo.glop.GlopTarget;
import fr.becpg.repo.glop.model.GlopData;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * Implementation for Glop service using an auxiliary Glop server
 *
 * @author pierrecolin
 * @version 1.0
 */
@Service("glopService")
public class GlopServiceImpl implements GlopService {





	private static final Log logger = LogFactory.getLog(GlopServiceImpl.class);
	
	private static final String CONSTRAINTS = "constraints";
	private static final String VARIABLES = "variables";
	private static final String OBJECTIVE = "objective";
	private static final String OPTIMAL = "optimal";
	private static final String SUBOPTIMAL = "suboptimal";
	private static final String COEFFICIENTS = "coefficients";
	private static final String STATUS = "status";
	private static final String COMPONENTS = "components";
	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String ID = "id";

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private NodeService nodeService;


	@Value("${beCPG.glop.serverUrl}")
	private String serverUrl;

	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * {@inheritDoc}
	 *
	 * Finds the optimal recipe for a product given a target to optimize and a
	 * list of constraints.
	 */
	@Override
	public GlopData optimize(ProductData productData, List<GlopConstraint> constraints, GlopTarget target)
			throws GlopException, RestClientException, URISyntaxException, JSONException {
		
		StopWatch stopWatch = null;
		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch();
			stopWatch.start();
		}
		
		Set<CompoListDataItem> variables = new HashSet<>();
		
		Map<CompoListDataItem, Double> targetContributionMap = new HashMap<>();
		
		Map<Object, Map<CompoListDataItem, Double>> constraintContributionMaps = buildConstraintContributionMaps(constraints);
		
		for (CompoListDataItem compoListDataItem : productData.getCompoList()) {
			
			final ProductData componentProductData = alfrescoRepository.findOne(compoListDataItem.getProduct());

			// compute target contribution
			Double targetContributionValue = computeComponentContribution(componentProductData, target.getTarget());

			if (targetContributionValue != null) {
				variables.add(compoListDataItem);
				targetContributionMap.put(compoListDataItem, targetContributionValue);
			}
			
			// compute constraint contributions
			for (Map.Entry<Object, Map<CompoListDataItem, Double>> entry : constraintContributionMaps.entrySet()) {

				Object constraintItem = entry.getKey();
				
				Map<CompoListDataItem, Double> constraintContribution = entry.getValue();
				
				if (constraintItem instanceof SimpleCharactDataItem) {
					Double constraintContributionValue = computeComponentContribution(componentProductData, (SimpleCharactDataItem) constraintItem);
					if (constraintContributionValue != null) {
						variables.add(compoListDataItem);
						constraintContribution.put(compoListDataItem, constraintContributionValue);
					}
				} else if ("recipeQtyUsed".equals(constraintItem)) {
					constraintContribution.put(compoListDataItem, 1d);
					variables.add(compoListDataItem);
				}
			}
		}

		JSONObject jsonRequest = buildRequest(variables, constraints, constraintContributionMaps, target, targetContributionMap, false);

		String response = sendRequest(jsonRequest);
		
		if (response == null) {
			
			JSONObject requestWithTolerance = buildRequest(variables, constraints, constraintContributionMaps, target, targetContributionMap, true);
			
			if (requestWithTolerance != null) {
				response = sendRequest(requestWithTolerance);
				
				if (response != null) {
					JSONObject jsonResponse = new JSONObject(response);
					
					jsonResponse.put(STATUS, SUBOPTIMAL);
					
					if (logger.isDebugEnabled()) {
						stopWatch.stop();
						logger.debug("Took " + stopWatch.getTotalTimeMillis() + " ms");
					}
					
					completeResponse(constraintContributionMaps, jsonResponse);
					
					return buildGlopData(productData, jsonResponse, constraints, variables);
				}
			}
			
			if (response == null) {
				throw new GlopException();
			}
		}
		
		JSONObject jsonResponse = new JSONObject(response);
		
		completeResponse(constraintContributionMaps, jsonResponse);
		
		if (logger.isDebugEnabled()) {
			stopWatch.stop();
			logger.debug("Took " + stopWatch.getTotalTimeMillis() + " ms");
		}
		
		return buildGlopData(productData, jsonResponse, constraints, variables);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Sends a request to the Glop server. Mostly for testing and may be
	 * deprecated in the future.
	 */
	@Override
	public String sendRequest(JSONObject request) throws RestClientException, URISyntaxException, JSONException, GlopException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);
	
		URI uri = new URI(serverUrl);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending " + request + " to " + serverUrl);
		}
		String response = restTemplate.postForObject(uri, requestEntity, String.class);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Server returned " + response);
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	private GlopData buildGlopData(ProductData productData, JSONObject obj, List<GlopConstraint> constraints, Set<CompoListDataItem> variables) throws JSONException {
		
		GlopData ret = new GlopData();
		
		JSONArray components = new JSONArray();
		
		JSONObject coeff = (JSONObject) obj.get(COEFFICIENTS);
		
		Iterator<String> keys = coeff.keys();

		while (keys.hasNext()) {
			String key = keys.next();
			
			JSONObject component = new JSONObject();
			
			component.put(STATUS, OPTIMAL);
			component.put(VALUE, coeff.getDouble(key));
			
			if (key.contains("/")) {
				NodeRef nodeRef = new NodeRef(key);
				component.put(ID, nodeRef.toString());
				component.put(NAME, attributeExtractorService.extractPropName(nodeRef));
				
				GlopConstraint constraint = findMatchingConstraint(constraints, nodeRef);
				
				if (constraint != null) {
					if (coeff.getDouble(key) < constraint.getMinValue() || coeff.getDouble(key) > constraint.getMaxValue()) {
						component.put(STATUS, SUBOPTIMAL);
					}
					
					Double netWeight = 1d;
					
					if (constraint.getData() instanceof SimpleListDataItem && productData.getNetWeight() != null && productData.getNetWeight() != 0d) {
						netWeight = productData.getNetWeight();
					}
					
					component.put(VALUE, coeff.getDouble(key) / netWeight);
					
					nodeService.setProperty(((SimpleCharactDataItem) constraint.getData()).getNodeRef(), PLMModel.PROP_GLOP_VALUE, component.toString());
				} else {
					
					CompoListDataItem variable = findMatchingVariable(variables, nodeRef);
					
					if (variable != null) {
						nodeService.setProperty(variable.getNodeRef(), PLMModel.PROP_GLOP_VALUE, component.toString());
					}
				}
				
			} else {
				component.put(NAME, key);
			}
			
			components.put(component);
		}
		
		ret.put(COMPONENTS, components);
		ret.put(VALUE, obj.get(VALUE));
		ret.put(STATUS, obj.get(STATUS));
		
		return ret;
	}

	private CompoListDataItem findMatchingVariable(Set<CompoListDataItem> variables, NodeRef nodeRef) {
		for (CompoListDataItem variable : variables) {
			if (nodeRef.equals(variable.getCharactNodeRef())) {
				return variable;
			}
		}
		return null;
	}

	private GlopConstraint findMatchingConstraint(List<GlopConstraint> constraints, NodeRef nodeRef) {
		for (GlopConstraint constraint : constraints) {
			if (constraint.getData() instanceof SimpleCharactDataItem) {
				if (nodeRef.equals(((SimpleCharactDataItem) constraint.getData()).getCharactNodeRef())) {
					return constraint;
				}
			}
		}
		return null;
	}

	private void completeResponse(Map<Object, Map<CompoListDataItem, Double>> constraintContributionMaps,
			JSONObject jsonResponse) throws JSONException {
		
		for (Map.Entry<Object, Map<CompoListDataItem, Double>> entry : constraintContributionMaps.entrySet()) {
	
			Object constraintItem = entry.getKey();
			
			Map<CompoListDataItem, Double> constraintContribution = entry.getValue();
			
			Double totalItemQuantity = 0d;
			
			for (Entry<CompoListDataItem, Double> contribution : constraintContribution.entrySet()) {
				CompoListDataItem compoListDataItem = contribution.getKey();
				Double value = contribution.getValue();
				
				if (jsonResponse.getJSONObject(COEFFICIENTS).has(compoListDataItem.getProduct().toString())) {
					totalItemQuantity += value * jsonResponse.getJSONObject(COEFFICIENTS).getDouble(compoListDataItem.getProduct().toString());
				}
			}
				
			if (constraintItem instanceof SimpleCharactDataItem) {
				jsonResponse.getJSONObject(COEFFICIENTS).put(((SimpleCharactDataItem) constraintItem).getCharactNodeRef().toString(), totalItemQuantity);
			} else {
				jsonResponse.getJSONObject(COEFFICIENTS).put(constraintItem.toString(), totalItemQuantity);
			}
			
		}
	}

	private Double computeComponentContribution(final ProductData component, SimpleCharactDataItem itemImpacted) {
		
		if (itemImpacted instanceof CompoListDataItem) {
			if (component.getNodeRef().equals(((CompoListDataItem) itemImpacted).getProduct())) {
				return 1d;
			}
		} else {
			for (SimpleCharactDataItem item : alfrescoRepository.getList(component, itemImpacted.getClass())) {
				if (item.getCharactNodeRef().equals(itemImpacted.getCharactNodeRef())) {
					return item.getValue() != null ? item.getValue() : 0d;
				}
			}
		}
		
		return null;
	}

	private Map<Object, Map<CompoListDataItem, Double>> buildConstraintContributionMaps(List<GlopConstraint> constraints) {
		
		Map<Object, Map<CompoListDataItem, Double>> constraintContributions = new HashMap<>();
		
		for (GlopConstraint constraint : constraints) {
			constraintContributions.put(constraint.getData(), new HashMap<>());
		}
		
		return constraintContributions;
	}
	
	private JSONObject buildRequest(Set<CompoListDataItem> variables, List<GlopConstraint> constraints,
			Map<Object, Map<CompoListDataItem, Double>> constraintContributions, GlopTarget target,
			Map<CompoListDataItem, Double> targetContributions, boolean applyTolerance)
			throws JSONException {


		if (applyTolerance) {
			if (!applyTolerance(constraints)) {
				return null;
			}
		}
		
		JSONObject ret = new JSONObject();
		
		JSONArray jsonVariables = new JSONArray();
		for (CompoListDataItem variable : variables) {
			jsonVariables.put(variable.getProduct().toString());
		}
		ret.put(VARIABLES, jsonVariables);
		for (GlopConstraint constraint : constraints) {
			ret.append(CONSTRAINTS, serializeConstraint(constraint, constraintContributions));
		}
		
		JSONObject jsonTarget = new JSONObject();
		target.putIntoJson(jsonTarget);
		JSONObject jsonCoefficients = new JSONObject();
		
		for (Map.Entry<CompoListDataItem, Double> entry : targetContributions.entrySet()) {
			jsonCoefficients.put(entry.getKey().getProduct().toString(), entry.getValue());
		}
		
		jsonTarget.put(COEFFICIENTS, jsonCoefficients);
		ret.put(OBJECTIVE, jsonTarget);
		
		return ret;
	}

	private boolean applyTolerance(List<GlopConstraint> constraints) {

		boolean hasTolerance = false;
		
		for (GlopConstraint constraint : constraints) {
			if (constraint.getTolerance() != null) {
				hasTolerance = true;
				if (constraint.getMinValue() != null) {
					constraint.setMinTolerance(constraint.getMinValue() - constraint.getTolerance() * Math.abs(constraint.getMinValue()) * 0.01);
				}
				if (constraint.getMaxValue() != null) {
					constraint.setMaxTolerance(constraint.getMaxValue() + constraint.getTolerance() * Math.abs(constraint.getMaxValue()) * 0.01);
				}
			}
		}
		
		return hasTolerance;
	}
	
	private JSONObject serializeConstraint(GlopConstraint constraint, Map<Object, Map<CompoListDataItem, Double>> constraintContributions) throws JSONException {
		
		JSONObject ret = new JSONObject();
		
		ret.put(COEFFICIENTS, serializeCoefficients(constraint, constraintContributions));
		
		ret.put("lower", serializeValue(constraint.getMinTolerance() != null ? constraint.getMinTolerance() : constraint.getMinValue()));
		
		ret.put("upper", serializeValue(constraint.getMaxTolerance() != null ? constraint.getMaxTolerance() : constraint.getMaxValue()));
		
		return ret;
	}

	private JSONObject serializeCoefficients(GlopConstraint constraint, Map<Object, Map<CompoListDataItem, Double>> constraintContributions) throws JSONException {
		JSONObject coefficients = new JSONObject();
		for (Map.Entry<CompoListDataItem, Double> entry : constraintContributions.get(constraint.getData()).entrySet()) {
			coefficients.put(entry.getKey().getProduct().toString(), entry.getValue());
		}
		return coefficients;
	}
	
	private Object serializeValue(double value) {
		if (Double.isFinite(value)) {
			return value;
		}
		
		if (value > 0) {
			return "inf";
		} 
		
		return "-inf";
	}

}
